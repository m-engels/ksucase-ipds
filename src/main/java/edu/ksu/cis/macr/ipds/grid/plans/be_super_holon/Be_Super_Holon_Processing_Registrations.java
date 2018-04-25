package edu.ksu.cis.macr.ipds.grid.plans.be_super_holon;

import edu.ksu.cis.macr.ipds.config.RunManager;
import edu.ksu.cis.macr.ipds.grid.capabilities.admin.GridAdminCapability;
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
 This is the  step.  This step will continue until the simulation requirements for
 being fully initially connected are met.  See {@code Scenario} for more information.
 When complete, it will move to the processing registration state.
 */
public enum Be_Super_Holon_Processing_Registrations implements IPlanState<Be_Super_Holon_Plan> {
  INSTANCE;
  private static final Logger LOG = LoggerFactory.getLogger(Be_Super_Holon_Processing_Registrations.class);
  private static final boolean debug = false;

  @Override
  public synchronized void Enter(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
    // Nothing
  }

  @Override
  public synchronized void Execute(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
    if (debug) LOG.debug("Starting with instance goal: {}.", ig);
    Objects.requireNonNull(ec);
    Objects.requireNonNull(ig);
    Objects.requireNonNull(ec.getCapability(IGridConnectCapability.class));
    Objects.requireNonNull(ec.getCapability(GridAdminCapability.class));

    plan.heartBeat(this.getClass().getName(),
            String.format("Unconnected=%s. All registered=%s.", ec.getCapability(IGridConnectCapability.class).getUnconnectedChildren().toString(),
                    ec.getCapability(GridAdminCapability.class).processingRegistrationIsComplete()));

      if (RunManager.isInitiallyConnected()) {
          LOG.debug("Begining registration. RunManager.isInitiallyConnected ");

          if (ec.getCapability(ParticipateCapability.class) != null) {
              if (debug) LOG.debug("agent: waiting. ");

              ec.getCapability(IGridConnectCapability.class).initializeChildConnections(ig);
              ec.getCapability(GridAdminCapability.class).initializeChildConnections(ig);
              if (debug) LOG.debug("agent: initialized capabilities based on goal parameters. ");

              // check for connection messages from each child
              ec.getCapability(IGridConnectCapability.class).connectToChildren();
              if (debug) LOG.debug("agent: Super holon sent connection messages to sub holons.");

              if (ec.getCapability(IGridConnectCapability.class).isAllConnected()) {

                  if (debug) LOG.debug("agent: All (or enough) sub holons connected. Beginning registration.");
                  boolean allRegistered =  ec.getCapability(GridAdminCapability.class).processRegistrationMessages(ig);
                  if (debug) LOG.debug("agent: all registered = {}.", allRegistered);

                  if (ec.getCapability(GridAdminCapability.class).processingRegistrationIsComplete()) {

                      if (debug) LOG.debug("Changing state.");
                      plan.getStateMachine().changeState(Be_Super_Holon.INSTANCE, ec, ig);
                  } else {
                      if (debug) LOG.debug("agent: Not all sub holons have registered - still waiting.");
                  }

              } else {
                  LOG.debug("agent: {} failed to connect to all sub holons. Will retry. Verify associated goals have been triggered. {}. Unconnected={}",
                          ec.getUniqueIdentifier().toString(),
                          ec.getCapability(IGridConnectCapability.class).getConnectionSummaryString(), ec.getCapability(IGridConnectCapability.class).getUnconnectedChildren());

                  if (debug) LOG.debug("Changing state.");
                  plan.getStateMachine().changeState(Be_Super_Holon_Connecting.INSTANCE, ec, ig);
              }
          }

          // if I'm the proxy persona running my new holonic organization
          else {
              if (debug) LOG.debug("proxy: super holon waiting to finish connecting to all subs. ");

              // initialize based on instance goal
              ec.getCapability(IGridConnectCapability.class).initializeChildConnections(ig);
              if (debug) LOG.debug("proxy: initialized connections to sub holons based on goal parameters. ");

              if (debug) LOG.debug("proxy: Changing to main super holon state");
              plan.getStateMachine().changeState(Be_Super_Holon.INSTANCE, ec, ig);
          }
      }

    if ((RunManager.isStopped())) {
      plan.getStateMachine().changeState(Be_Super_Holon_Stop.INSTANCE, ec, ig);
    }
  }

  @Override
  public synchronized void Exit(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
    // Nothing
  }
}
