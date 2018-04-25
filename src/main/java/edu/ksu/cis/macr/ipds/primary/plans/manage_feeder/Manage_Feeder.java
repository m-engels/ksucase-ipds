package edu.ksu.cis.macr.ipds.primary.plans.manage_feeder;

import edu.ksu.cis.macr.ipds.config.RunManager;
import edu.ksu.cis.macr.ipds.primary.capabilities.manage.DateTimeCapability;
import edu.ksu.cis.macr.ipds.primary.capabilities.manage.ManageFeederCapability;
import edu.ksu.cis.macr.ipds.primary.guidelines.IFeederGuidelines;
import edu.ksu.cis.macr.obaa_pp.ec.base.IExecutor;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IExecutablePlan;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IPlanState;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.GregorianCalendar;
import java.util.Objects;

/**
 This is the main working step in the associated plan.  It monitors the list of registered agents, updating each agent's
 power allocations as needed by updating their goal parameters.  If the list of registered agents changes, it will return
 to the prior init state.  If it receives a stop messages, it will move to the stop state.
 */
public enum Manage_Feeder implements IPlanState<Manage_Feeder_Plan> {
  INSTANCE;
  private static final Logger LOG = LoggerFactory.getLogger(Manage_Feeder.class);
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
    Objects.requireNonNull(ec.getCapability(DateTimeCapability.class), "Role requires DateTimeCapability.");
    Objects.requireNonNull(ec.getCapability(ManageFeederCapability.class));

    // wait extra long - not much to do
    plan.heartBeat(this.getClass().getName(), 10);

    LOG.debug("Reading simulated sensor data from a smart meter at this location.");

    // initialize my guidelines as defined in the parametrized instance goal
    ec.getCapability(ManageFeederCapability.class).initializeFromGoal(ig);
    if (debug) LOG.debug("Initialized capabilities.");


    // get current timeSlice from datetime capability

    long timeslice = Objects.requireNonNull(ec.getCapability(DateTimeCapability.class).
            getTimeSlicesElapsedSinceStart(), "ERROR: Need a timeSlice to get sensor data.");

    GregorianCalendar simulationTime = Objects.requireNonNull(ec.getCapability(DateTimeCapability.class)
            .getSimulationTime(), "ERROR: Need to know the current simulation time.");
    LOG.info("{} elapsed time slices. ", timeslice);


    // get values from goal guidelines
    IFeederGuidelines guidelines = Objects.requireNonNull(ec.getCapability(ManageFeederCapability.class).getFeederGuidelines());
    if (debug) LOG.debug("Guidelines : {} ", guidelines.toString());

    if ((RunManager.isStopped())) {
      plan.getStateMachine().changeState(Manage_Feeder_Stop.INSTANCE, ec, ig);
    }
  }

  @Override
  public synchronized void Exit(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
    // Nothing
  }
}
