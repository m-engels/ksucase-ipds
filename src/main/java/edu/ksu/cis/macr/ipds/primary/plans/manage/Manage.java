package edu.ksu.cis.macr.ipds.primary.plans.manage;

import edu.ksu.cis.macr.aasis.agent.cc_message.custom.EquipmentStatus;
import edu.ksu.cis.macr.aasis.agent.cc_message.custom.ICustomMessage;
import edu.ksu.cis.macr.aasis.agent.cc_message.custom.ICustomMessageContent;
import edu.ksu.cis.macr.aasis.config.RunManager;
import edu.ksu.cis.macr.ipds.primary.capabilities.manage.DateTimeCapability;
import edu.ksu.cis.macr.ipds.primary.capabilities.manage.ManageCapability;
import edu.ksu.cis.macr.ipds.primary.capabilities.participate.ICustomMessageCommunicationCapability;
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
public enum Manage implements IPlanState<Manage_Plan> {
  INSTANCE;
  private static final Logger LOG = LoggerFactory.getLogger(Manage.class);
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
    Objects.requireNonNull(ec.getCapability(ManageCapability.class), "Role requires ManageHomeCapability.");
    Objects.requireNonNull(ec.getCapability(ICustomMessageCommunicationCapability.class), "Role requires ICustomMessageCommunicationCapability.");
    Objects.requireNonNull(ec.getCapability(DateTimeCapability.class), "Role requires DateTimeCapability.");

    plan.heartBeat(this.getClass().getName());

    // initialize my guidelines as defined in the parametrized instance goal
    ec.getCapability(ManageCapability.class).initializeFromGoal(ig);
    if (debug) LOG.debug("Initialized capabilities.");

    // get current timeSlice from datetime capability

      long timeSlice = Objects.requireNonNull(ec.getCapability(DateTimeCapability.class).
              getTimeSlicesElapsedSinceStart(), "ERROR: Need a timeSlice to get sensor data.");

      GregorianCalendar simulationTime = Objects.requireNonNull(ec.getCapability(DateTimeCapability.class)
              .getSimulationTime(), "ERROR: Need to know the current simulation time.");
    if (debug) LOG.debug("{} elapsed time slices. ", timeSlice);


      // create local message for internal self control
      ICustomMessageContent content =ec.getCapability(ICustomMessageCommunicationCapability.class).createContent("test message", 1, EquipmentStatus.AVAILABLE);
      ICustomMessage msg = ec.getCapability(ICustomMessageCommunicationCapability.class).createLocalMessageForSelf(content);

      boolean sent = ec.getCapability(ICustomMessageCommunicationCapability.class).sendLocal(msg);
      LOG.info("HOME SENT LOCAL MESSAGE TO SELF = {}. {}", sent, msg.toString());




    if (RunManager.isStopped()) {
      plan.getStateMachine().changeState(Manage_Stop.INSTANCE, ec, ig);
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
