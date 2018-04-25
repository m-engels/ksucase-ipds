package edu.ksu.cis.macr.ipds.grid.plans.be_super_holon;

import edu.ksu.cis.macr.aasis.agent.persona.IOrganization;
import edu.ksu.cis.macr.ipds.config.RunManager;
import edu.ksu.cis.macr.ipds.grid.capabilities.admin.GridAdminCapability;
import edu.ksu.cis.macr.ipds.grid.capabilities.admin.GridControlSuperHolonCapability;
import edu.ksu.cis.macr.ipds.grid.capabilities.connect.IGridConnectCapability;
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
 * This is the first step in the plan.  It performs initialization tasks once at the beginning of the plan. When complete,
 * it will move to the next state.
 */
public enum Be_Super_Holon_Init implements IPlanState<Be_Super_Holon_Plan> {
    INSTANCE;
    private static final Logger LOG = LoggerFactory.getLogger(Be_Super_Holon_Init.class);
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
        Objects.requireNonNull(ec.getCapability(GridAdminCapability.class), "Role requires GridAdminCapability.");
        Objects.requireNonNull(ec.getCapability(GridControlSuperHolonCapability.class), "Role requires GridControlSuperHolonCapability.");
        Objects.requireNonNull(ec.getCapability(IPowerCommunicationCapability.class), "Role requires IPowerCommunicationCapability.");

        plan.heartBeat(this.getClass().getName());

        // if I'm the "real" persona in the super holon agent, create and start the new org (which will handle registration)
        if (ec.getCapability(ParticipateCapability.class) != null) {

            // create external (and load guidelines, read objects and agents from files)
            IOrganization org = ec.getCapability(GridAdminCapability.class).createAndStartNewOrganization(ig);
            if (debug) LOG.debug("agent: New organization created. {}", org);

            // load the list of persona from the agent file
            ec.getCapability(GridAdminCapability.class).loadPersona();
            if (debug) LOG.debug("agent: Agents loaded from specification into {}.", org.getName());

            // start the new organization (kick off the goal model)
            ec.getCapability(GridAdminCapability.class).startOrganization();
            LOG.info("agent: New organization {} started.", org.getName());

            // set holon guidelines from top-down instance goals
            ec.getCapability(GridControlSuperHolonCapability.class).initializeFromGoal(ig);
            if (debug) LOG.debug("agent: Set holonic guidelines for {}.", org.getName());

            if (debug) LOG.debug("Changing state.");
            plan.getStateMachine().changeState(Be_Super_Holon_Connecting.INSTANCE, ec, ig);
        }

        // if I'm the proxy persona running my new holonic organization
        else {
            LOG.info("Initializing proxy to use grid goal model to generate control assignments. ");

            // initialize my guidelines as defined in the parametrized instance goal
            ec.getCapability(IGridConnectCapability.class).init(ig);
            if (debug) LOG.debug("proxy: Set child connection guidelines.");

            ec.getCapability(IPowerCommunicationCapability.class).initializeChildConnections(ig);
            if (debug) LOG.debug("proxy: Set child power communication (external comm) guidelines.");

            // setup queues and bindings and send a hello to message to all children
            ec.getCapability(IGridConnectCapability.class).connectDown();
            if (debug) LOG.debug("proxy: Setup queues and bindings to connect to sub holons.");

            if (debug) LOG.debug("Changing state.");
            plan.getStateMachine().changeState(Be_Super_Holon_Connecting.INSTANCE, ec, ig);
        }
        if ((RunManager.isStopped())) {
            if (debug) LOG.debug("Changing state.");
            plan.getStateMachine().changeState(Be_Super_Holon_Stop.INSTANCE, ec, ig);
        }

    }

    @Override
    public synchronized void Exit(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
        // Nothing
    }
}
