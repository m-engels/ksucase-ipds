package edu.ksu.cis.macr.ipds.grid.plans.be_holon;

import edu.ksu.cis.macr.ipds.config.RunManager;
import edu.ksu.cis.macr.ipds.grid.capabilities.connect.IGridConnectCapability;
import edu.ksu.cis.macr.ipds.grid.capabilities.participate.GridControlHolonCapability;
import edu.ksu.cis.macr.ipds.primary.capabilities.participate.IPowerCommunicationCapability;
import edu.ksu.cis.macr.ipds.primary.capabilities.participate.ParticipateCapability;
import edu.ksu.cis.macr.obaa_pp.ec.base.IExecutor;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IExecutablePlan;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IPlanState;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 This is the first step in the plan.  It performs initialization tasks once at the beginning of the plan. When complete,
 it will move to the next state.
 */
public enum Be_Holon_Init implements IPlanState<Be_Holon_Plan> {
    INSTANCE;
    private static final Logger LOG = LoggerFactory.getLogger(Be_Holon_Init.class);
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
    Objects.requireNonNull(ec.getCapability(IGridConnectCapability.class), "Role requires IGridConnectCapability.");
      Objects.requireNonNull(ec.getCapability(GridControlHolonCapability.class), "Role requires GridControlHolonCapability.");
      Objects.requireNonNull(ec.getCapability(IPowerCommunicationCapability.class), "Role requires IPowerCommunicationCapability.");

    plan.heartBeat(this.getClass().getName());

    // if I'm the "real" persona in the agent
    if (ec.getCapability(ParticipateCapability.class) != null) {
        LOG.info("Sub-agent participant beginning plan.");

      // set guidelines for capabilities
        ec.getCapability(GridControlHolonCapability.class).init(ig);
        if (debug) LOG.debug("agent: Set holonic guidelines.");

      // setup queues and bindings and send a hello to message to supers
      ec.getCapability(IGridConnectCapability.class).connectUp(ig);
      if (debug) LOG.debug("agent: Setup all queues and bindings to connect to super holon.");

        // then switch to Be_Holon_Registering
        if (debug) LOG.debug("agent: Changing state.");
        plan.getStateMachine().changeState(Be_Holon_Connecting.INSTANCE, ec, ig);
    }

    // if I'm the proxy persona participating in an outside organization
    else {
        LOG.info("Organization proxy beginning plan (used to determine assignments).");
      // set holon guidelines from top-down instance goals
        ec.getCapability(GridControlHolonCapability.class).init(ig);
        if (debug) LOG.debug("proxy: Set holonic guidelines.");

      // get my connection guidelines for this one authorized connection
      ec.getCapability(IGridConnectCapability.class).init(ig);
      if (debug) LOG.debug("proxy: Set parent connection guidelines.");

         // then switch to Be_Holon_Registering state
        if (debug) LOG.debug("proxy: Changing state.");
        plan.getStateMachine().changeState(Be_Holon_Connecting.INSTANCE, ec, ig);
    }
    // if a (Scenario.isStopped()) is received, move to the stop state
    if ((RunManager.isStopped())) {
        LOG.debug("proxy: Moving to Be_Holon_Stop");
        plan.getStateMachine().changeState(Be_Holon_Stop.INSTANCE, ec, ig);
    }
  }


    @Override
    public synchronized void Exit(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
    // Nothing
  }
}
