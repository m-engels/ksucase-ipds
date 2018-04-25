package edu.ksu.cis.macr.ipds.primary.plans.manage_home;

import edu.ksu.cis.macr.ipds.config.RunManager;
import edu.ksu.cis.macr.ipds.primary.capabilities.manage.*;
import edu.ksu.cis.macr.ipds.primary.guidelines.IHomeGuidelines;
import edu.ksu.cis.macr.ipds.primary.messages.IPowerAssessment;
import edu.ksu.cis.macr.ipds.primary.messages.IPowerMessage;
import edu.ksu.cis.macr.ipds.primary.messages.IPowerQualityAssessment;
import edu.ksu.cis.macr.ipds.primary.sensors.smartmeter.ISmartMeterRead;
import edu.ksu.cis.macr.obaa_pp.ec.base.IExecutor;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IExecutablePlan;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IPlanState;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.GregorianCalendar;
import java.util.Objects;

/**
 This is the main managing step in the associated plan and can be shared by all agents employing this plan. This step
 includes the supervision of the registered participants, and the updating each participant's power allocations by
 creating goal modification events that update the participants operating guidelines. If the list of registered agents
 changes, it will revert to the prior initialization ("init") state. If it receives a stop messages, it will move to the
 stop state.
 */
public enum Manage_Home implements IPlanState<Manage_Home_Plan> {
  INSTANCE;
  private static final Logger LOG = LoggerFactory.getLogger(Manage_Home.class);
  private static final boolean debug = false;

    @Override
  public synchronized void Enter(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
    // Nothing
  }

  @Override
  public synchronized void Execute(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
    if (debug) LOG.debug("Starting with instance goal: {}.", ig);
    Objects.requireNonNull(ig);
    Objects.requireNonNull(ec);
    Objects.requireNonNull(ec.getCapability(ManageHomeCapability.class), "Role requires ManageHomeCapability.");
    Objects.requireNonNull(ec.getCapability(DateTimeCapability.class), "Role requires DateTimeCapability.");
    Objects.requireNonNull(ec.getCapability(AssessReactivePowerCapability.class), "Role requires AssessReactivePowerCapability.");
    Objects.requireNonNull(ec.getCapability(AssessReactivePowerQualityCapability.class), "Role requires AssessReactivePowerQualityCapability.");
    Objects.requireNonNull(ec.getCapability(SmartMeterCapability.class), "Role requires SmartMeterCapability.");

    plan.heartBeat(this.getClass().getName());


    // initialize my guidelines as defined in the parametrized instance goal
    ec.getCapability(ManageHomeCapability.class).initializeFromGoal(ig);
    ec.getCapability(SmartMeterCapability.class).initializeFromGoal(ig);
    if (ec.getCapability(SmartInverterCapability.class) != null) {
        ec.getCapability(SmartInverterCapability.class).initializeFromGoal(ig);
    }
    if (debug) LOG.debug("Initialized capabilities.");


    // get current timeSlice from datetime capability

      long timeslice = Objects.requireNonNull(ec.getCapability(DateTimeCapability.class).
              getTimeSlicesElapsedSinceStart(), "ERROR: Need a timeSlice to get sensor data.");

      GregorianCalendar simulationTime = Objects.requireNonNull(ec.getCapability(DateTimeCapability.class)
              .getSimulationTime(), "ERROR: Need to know the current simulation time.");
    if (debug) LOG.debug("{} elapsed time slices. ", timeslice);


    // get values from goal guidelines
    IHomeGuidelines guidelines = Objects.requireNonNull(ec.getCapability(ManageHomeCapability.class).getHomeGuidelines());
    double minKW = guidelines.getConstantInelasticLoad_kw();
    double maxKW = guidelines.getConstantInelasticLoad_fraction();
    double minVoltageMultiplier = guidelines.getMinVoltageMultiplier();
    double maxVoltageMultiplier = guidelines.getMaxVoltageMultiplier();
    double powerFactor = guidelines.getPowerFactor();
    double netDeltaP = guidelines.getNetDeltaP();
    int timeSlice = ec.getCapability(DateTimeCapability.class).getTimeSlicesElapsedSinceStart();

    //TODO:  complete power factor / algorithm transfer process
    if (RunManager.getPowerFactor() < 0.01) {
      powerFactor = 0.75;
    }
    // get current sensor readings (which should cause the associated display to update automatically)
    // if I'm an organization agent (in the master's head) then ask the real self agent's persona to fetch a reading)
    if (debug) LOG.debug("ec = {}", ec.getUniqueIdentifier());

    boolean isSameTimeSlice = ec.getCapability(SmartMeterCapability.class).isSameTimeSlice(timeSlice);
    if (!isSameTimeSlice) {
      ISmartMeterRead read = ec.getCapability(SmartMeterCapability.class).getSmartMeterRead(timeSlice);
      LOG.info("Smart meter read at time slice {}: {}.", timeSlice, read.toString());

      // manage my local smart inverter in accordance with my goals and sensor data
      boolean isLast = ec.getCapability(SmartInverterCapability.class).calculateSmartInverterSetting(read, powerFactor, timeSlice, netDeltaP);

      // if I'm the last reporting, then increment time slice
//        try {
//            if (isLast) {
//                ec.getCapability(DateTimeCapability.class).setTimeSlicesElapsedSinceStart(timeSlice + 1);
//            }
//            int max = ec.getCapability(DateTimeCapability.class).getMaxTimeSlices();
//            int cur = ec.getCapability(DateTimeCapability.class).getTimeSlicesElapsedSinceStart();
//            if (cur >= max) {
//                LOG.info("Simulation complete. {} time slices executed.", max);
//                System.exit(0);
//            }
//
//        } catch (Exception e) {
//            LOG.error("I'm last but couldn't update the time slice.");
//        }
/**/
      // build comparisons and assess trends
      final IPowerAssessment power = ec.getCapability(AssessReactivePowerCapability.class).getPowerAssessment(minKW,
              maxKW, read);

      final IPowerQualityAssessment quality = ec
              .getCapability(AssessReactivePowerQualityCapability.class)
              .getPowerQualityAssessment(minVoltageMultiplier, maxVoltageMultiplier, read);

      // create local power message for supervisor
      IPowerMessage msg = ec.getCapability(ManageHomeCapability.class).createLocalPowerMessageForSelf(
              timeSlice, minKW, maxKW, read, power, quality);

      boolean sent = ec.getCapability(ManageHomeCapability.class).sendLocal(msg);
      LOG.info("HOME SENT LOCAL MESSAGE TO SELF FOR REVIEW = {}. {}", sent, msg.toString());

      // for simulation testing
      RunManager.addPowerMessageSentFromSensorToSelf(msg.getLocalSender().toString(), msg.getLocalReceiver().toString(), timeSlice);
    }

    if (RunManager.isStopped()) {
      plan.getStateMachine().changeState(Manage_Home_Stop.INSTANCE, ec, ig);
    }
  }


  // update the associated forecast goal with current goals
//        ec.getCapability(
//                ManageHomeCapability.class)
//                .updateAssociatedForecasterGoal(ig, guidelines);



  @Override
  public synchronized void Exit(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
    // Nothing
  }
}
