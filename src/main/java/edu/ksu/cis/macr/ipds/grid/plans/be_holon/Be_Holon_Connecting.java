package edu.ksu.cis.macr.ipds.grid.plans.be_holon;

import edu.ksu.cis.macr.ipds.config.RunManager;
import edu.ksu.cis.macr.ipds.grid.capabilities.connect.IGridConnectCapability;
import edu.ksu.cis.macr.ipds.primary.capabilities.participate.ParticipateCapability;
import edu.ksu.cis.macr.obaa_pp.ec.base.IExecutor;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IExecutablePlan;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IPlanState;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 The {@code Be_Holon_Registering} state will check for a message from the super. Once received and the connection is
 verified, it will attempt to register. If registration is successful it will move to the main {@code Be_Holon}
 state.
 */
public enum Be_Holon_Connecting implements IPlanState<Be_Holon_Plan> {
  INSTANCE;
  private static final Logger LOG = LoggerFactory.getLogger(Be_Holon_Connecting.class);
  private static final boolean debug = false;

  @Override
  public synchronized void Enter(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {

  }

  @Override
  public synchronized void Execute(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
    if (debug) LOG.debug("Starting with instance goal: {}.", ig);
    Objects.requireNonNull(ec);
    Objects.requireNonNull(ig);
    Objects.requireNonNull(ec.getCapability(IGridConnectCapability.class), "Role requires IGridConnectCapability.");

    plan.heartBeat(this.getClass().getName());

//      plan.heartBeat(this.getClass().getName(),
//              String.format("Unconnected=%s. Registered=%s.", ec.getCapability(IGridConnectCapability.class).getUnconnectedChildren().toString(),
//                      ec.getCapability(ParticipateInOrganizationCapability.class).isRegistered()));

    // if I'm the "real" persona in the sub holon agent
    if (ec.getCapability(ParticipateCapability.class) != null) {

      if (debug) LOG.info("agent: connecting.");

      // initialize based on instance goal
      ec.getCapability(IGridConnectCapability.class).init(ig);
      if (debug) LOG.debug("agent: Set guidelines: {}.", ig);

      // check connections to parents - if any are not connected, attempt to connect
      ec.getCapability(IGridConnectCapability.class).connectToParents();

      if (ec.getCapability(IGridConnectCapability.class).isAllConnected()) {
        if (debug) LOG.info("{} connected to super holon. Changing state.", ec.getUniqueIdentifier().toString());

        plan.getStateMachine().changeState(Be_Holon_Registering.INSTANCE, ec, ig);

      } else {
        LOG.debug("agent: {} failed to connect to super holon. Will retry. {}. Unconnected={}",
                ec.getUniqueIdentifier().toString(),
                ec.getCapability(IGridConnectCapability.class).getConnectionSummaryString(), ec.getCapability(IGridConnectCapability.class).getUnconnectedParents());
      }
    }

    // if I'm the proxy persona participating in an outside organization
    else {
      LOG.debug("proxy: sub holon registering with super. ");

      // initialize based on instance goal
      ec.getCapability(IGridConnectCapability.class).initializeParentConnections(ig);

      if (debug) LOG.debug("proxy: Changing state.");
      plan.getStateMachine().changeState(Be_Holon_Registering.INSTANCE, ec, ig);
    }

    if ((RunManager.isStopped())) {
      LOG.info("STOP message received.");
      plan.getStateMachine().changeState(Be_Holon_Stop.INSTANCE, ec, ig);
    }
  }

  @Override
  public synchronized void Exit(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
    // Nothing
  }
}
