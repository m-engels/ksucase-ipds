package edu.ksu.cis.macr.ipds.grid.plans.be_holon;

import edu.ksu.cis.macr.ipds.config.RunManager;
import edu.ksu.cis.macr.ipds.grid.capabilities.connect.IGridConnectCapability;
import edu.ksu.cis.macr.ipds.grid.capabilities.participate.GridParticipateCapability;
import edu.ksu.cis.macr.ipds.primary.capabilities.participate.ParticipateCapability;
import edu.ksu.cis.macr.obaa_pp.ec.base.IExecutor;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IExecutablePlan;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IPlanState;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 This state will attempt to register with the organization head. It will change state once registration is complete.
 state.
 */
public enum Be_Holon_Registering implements IPlanState<Be_Holon_Plan> {
  INSTANCE;
  private static final Logger LOG = LoggerFactory.getLogger(Be_Holon_Registering.class);
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
    Objects.requireNonNull(ec.getCapability(GridParticipateCapability.class), "Role requires GridParticipateCapability.");

    plan.heartBeat(this.getClass().getName(),
            String.format("Unconnected=%s. Registered=%s.", ec.getCapability(IGridConnectCapability.class).getUnconnectedChildren().toString(),
                    ec.getCapability(GridParticipateCapability.class).isRegistered()));

      if (RunManager.isInitiallyConnected()) {

          // if I'm the "real" persona in the sub holon agent
          if (ec.getCapability(ParticipateCapability.class) != null) {

              if (debug) LOG.info("agent: registering.");

              // initialize based on instance goal
              ec.getCapability(IGridConnectCapability.class).initializeParentConnections(ig);
              ec.getCapability(GridParticipateCapability.class).initializeParentConnections(ig);


              // double-check still connected
              if (ec.getCapability(IGridConnectCapability.class).isAllConnected()) {
                  if (debug)
                      LOG.info("{} connected to super holon. Begin registration.", ec.getUniqueIdentifier().toString());

                  ec.getCapability(GridParticipateCapability.class).doRegistration();
                  if (debug) LOG.debug("{} tried registration. ", ec.getUniqueIdentifier().toString());

                  if (ec.getCapability(GridParticipateCapability.class).isRegistered()) {

                      if (debug) LOG.debug("Changing state.");
                      plan.getStateMachine().changeState(Be_Holon.INSTANCE, ec, ig);
                  } else {
                      LOG.debug("agent: {} connected but failed to register with super holon. Will retry. Verify associated goals have been triggered. {}. Unregistered={}",
                              ec.getUniqueIdentifier().toString(),
                              ec.getCapability(IGridConnectCapability.class).getConnectionSummaryString(), ec.getCapability(GridParticipateCapability.class).getUnregisteredParents().toString());
                  }


              } else {
                  LOG.debug("agent: {} registering, but not connected. Returning to connect state. Verify associated goals have been triggered. {}. Unconnected={}",
                          ec.getUniqueIdentifier().toString(),
                          ec.getCapability(IGridConnectCapability.class).getConnectionSummaryString(), ec.getCapability(IGridConnectCapability.class).getUnconnectedParents());

                  if (debug) LOG.debug("Changing state.");
                  plan.getStateMachine().changeState(Be_Holon_Connecting.INSTANCE, ec, ig);
              }
          }

          // if I'm the proxy persona participating in an outside organization
          else {
              LOG.debug("proxy: sub holon registering with super. ");

              // initialize based on instance goal
              ec.getCapability(IGridConnectCapability.class).initializeParentConnections(ig);

              if (debug) LOG.debug("proxy: Moving to Be_Holon");
              plan.getStateMachine().changeState(Be_Holon.INSTANCE, ec, ig);
          }
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
