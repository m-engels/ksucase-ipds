package edu.ksu.cis.macr.ipds.primary.plans.manage_substation;

import edu.ksu.cis.macr.ipds.config.RunManager;
import edu.ksu.cis.macr.ipds.primary.capabilities.manage.DateTimeCapability;
import edu.ksu.cis.macr.ipds.primary.capabilities.manage.ManageSubstationCapability;
import edu.ksu.cis.macr.ipds.primary.guidelines.ISubstationGuidelines;
import edu.ksu.cis.macr.obaa_pp.ec.base.IExecutor;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IExecutablePlan;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IPlanState;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.GregorianCalendar;
import java.util.Objects;

/**
 <p> This is the main managing step in the associated plan and can be shared by all agents employing this plan. </p> This
 step includes the supervision of the registered participants, and the updating each participant's power allocations by
 creating goal modification events that update the participants operating guidelines.  If the list of registered agents
 changes, it will revert to the prior initialization ("init") state.  If it receives a stop messages, it will move to the
 stop state.
 */
public enum Manage_Substation implements IPlanState<Manage_Substation_Plan> {
  INSTANCE;
  private static final Logger LOG = LoggerFactory.getLogger(Manage_Substation.class);
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
    Objects.requireNonNull(ec.getCapability(ManageSubstationCapability.class));

    // wait extra long - not much to do
    plan.heartBeat(this.getClass().getName(), 10);

    LOG.debug("Reading simulated sensor data from a smart meter at this location.");

    // initialize my guidelines as defined in the parametrized instance goal
    ec.getCapability(ManageSubstationCapability.class).initializeFromGoal(ig);
    if (debug) LOG.debug("Initialized capabilities.");


    // get current timeSlice from datetime capability

    long timeslice = Objects.requireNonNull(ec.getCapability(DateTimeCapability.class).
            getTimeSlicesElapsedSinceStart(), "ERROR: Need a timeSlice to get sensor data.");

    GregorianCalendar simulationTime = Objects.requireNonNull(ec.getCapability(DateTimeCapability.class)
            .getSimulationTime(), "ERROR: Need to know the current simulation time.");
    LOG.info("{} elapsed time slices. ", timeslice);


    // get values from goal guidelines
    ISubstationGuidelines guidelines = ec.getCapability(ManageSubstationCapability.class).getSubstationGuidelines();
    if (debug) LOG.debug("Guidelines : {} ", guidelines.toString());



    if ((RunManager.isStopped())) {
      plan.getStateMachine().changeState(Manage_Substation_Stop.INSTANCE, ec, ig);
    }
  }

  @Override
  public synchronized void Exit(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
    // Nothing
  }
}
