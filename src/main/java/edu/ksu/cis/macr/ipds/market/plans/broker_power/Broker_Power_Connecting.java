package edu.ksu.cis.macr.ipds.market.plans.broker_power;

import edu.ksu.cis.macr.ipds.config.RunManager;
import edu.ksu.cis.macr.ipds.market.capabilities.admin.MarketAdminCapability;
import edu.ksu.cis.macr.ipds.market.capabilities.connect.IMarketConnectCapability;
import edu.ksu.cis.macr.ipds.primary.capabilities.participate.ParticipateCapability;
import edu.ksu.cis.macr.obaa_pp.ec.base.IExecutor;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IExecutablePlan;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IPlanState;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * This is the connecting step.  This step will continue until the simulation requirements for
 * being fully initially connected are met.  See {@code Scenario} for more information.
 * When complete, it will move to the processing registration state.
 */
public enum Broker_Power_Connecting implements IPlanState<Broker_Power_Plan> {
    INSTANCE;
    private static final Logger LOG = LoggerFactory.getLogger(Broker_Power_Connecting.class);
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
        Objects.requireNonNull(ec.getCapability(IMarketConnectCapability.class));
        Objects.requireNonNull(ec.getCapability(MarketAdminCapability.class));

        // initialize based on instance goal
        ec.getCapability(IMarketConnectCapability.class).init(ig);
        ec.getCapability(MarketAdminCapability.class).init(ig);
        LOG.info("agent: initialized capabilities from goal.");

        try {
            plan.heartBeat(this.getClass().getName(),
                    String.format("Unconnected=%s. All registered=%s.",
                            ec.getCapability(IMarketConnectCapability.class).getUnconnectedChildren(ig).toString(),
                            ec.getCapability(MarketAdminCapability.class).processingRegistrationIsComplete(ig)));
        } catch (Exception ex) {
            plan.heartBeat(this.getClass().getName());
        }

        if (ec.getCapability(ParticipateCapability.class) != null) {
            if (debug) LOG.debug("agent: waiting. ");

//            ec.getCapability(IMarketConnectCapability.class).init(ig);
//            ec.getCapability(MarketAdminCapability.class).init(ig);
//            if (debug) LOG.debug("agent: initialized capabilities based on goal parameters. ");

            if (ec.getCapability(IMarketConnectCapability.class).isAllConnected(ig)) {
                if (debug) LOG.debug("Changing state.");
                plan.getStateMachine().changeState(Broker_Power_Processing_Registrations.INSTANCE, ec, ig);
            }

            // check for connection messages from each child
            ec.getCapability(IMarketConnectCapability.class).checkDownConnections(ig);
            if (debug) LOG.debug("agent: Super holon sent connection messages to sub holons.");

            else {
                if (debug)
                    LOG.debug("agent: {} failed to connect to all participants. Will retry. Verify associated goals have been triggered. {}. Unconnected={}",
                            ec.getUniqueIdentifier().toString(),
                            ec.getCapability(IMarketConnectCapability.class).getConnectionSummaryString(), ec.getCapability(IMarketConnectCapability.class).getUnconnectedChildren());
            }
        }

        // if I'm the proxy persona running my new organization
        else {
            if (debug) LOG.debug("proxy: waiting to finish connecting to all participants. ");

            if (debug) LOG.debug("proxy: Changing to main state");
            plan.getStateMachine().changeState(Broker_Power.INSTANCE, ec, ig);
        }

        if ((RunManager.isStopped())) {
            plan.getStateMachine().changeState(Broker_Power_Stop.INSTANCE, ec, ig);
        }
    }

    @Override
    public synchronized void Exit(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
        // Nothing
    }
}
