package edu.ksu.cis.macr.ipds.market.plans.broker_power;


import edu.ksu.cis.macr.ipds.config.RunManager;
import edu.ksu.cis.macr.ipds.market.capabilities.admin.BrokerPowerCapability;
import edu.ksu.cis.macr.ipds.market.capabilities.admin.MarketAdminCapability;
import edu.ksu.cis.macr.ipds.market.capabilities.connect.IMarketConnectCapability;
import edu.ksu.cis.macr.ipds.market.capabilities.participate.IAuctionCommunicationCapability;
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
 * it will move to the main working state.
 */
public enum Broker_Power_Init implements IPlanState<Broker_Power_Plan> {
    INSTANCE;
    private static final Logger LOG = LoggerFactory.getLogger(Broker_Power_Init.class);
    private static final boolean debug = false;

    @Override
    public synchronized void Enter(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
        // nothing
    }

    @Override
    public synchronized void Execute(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
        if (debug) LOG.debug("Starting with instance goal: {}.", ig);
        Objects.requireNonNull(ig);
        Objects.requireNonNull(ec);
        Objects.requireNonNull(ec.getCapability(BrokerPowerCapability.class), "Role needs BrokerPowerCapability");
        Objects.requireNonNull(ec.getCapability(IMarketConnectCapability.class), "Role requires IMarketConnectCapability.");
        Objects.requireNonNull(ec.getCapability(MarketAdminCapability.class), "Role requires MarketAdminCapability.");

        plan.heartBeat(this.getClass().getName());

        // initialize based on instance goal
        ec.getCapability(BrokerPowerCapability.class).init(ig);
        ec.getCapability(IMarketConnectCapability.class).init(ig);
        ec.getCapability(MarketAdminCapability.class).init(ig);
        LOG.info("agent: initialized capabilities from goal.");

        // if I'm the "real" persona in the  agent, create and start the new org (which will handle registration)
        if (ec.getCapability(ParticipateCapability.class) != null) {
            LOG.info("Ready to form a new market organization. Instance goal is: {}.", ig);

            // create external (and load guidelines, read objects and agents from files)
           boolean success = ec.getCapability(MarketAdminCapability.class).createAndStartNewOrganization(ig);
            if (debug) LOG.debug("agent: New organization created and started. success = {}", success);
            if (success) {

                if (debug) LOG.debug("Changing state.");
                plan.getStateMachine().changeState(Broker_Power_Connecting.INSTANCE, ec, ig);
            }
            // otherwise, keep trying..
        }

        // if I'm the proxy persona running my new organization
        else {
            LOG.info("Initializing proxy to use market goal model to generate auction/broker assignments. ");

            ec.getCapability(IAuctionCommunicationCapability.class).initializeChildConnections(ig);
            if (debug) LOG.debug("proxy: Set auction participant guidelines.");

            // setup queues and bindings and send a hello to message to all children
            ec.getCapability(IMarketConnectCapability.class).connectDown(ig);
            if (debug) LOG.debug("proxy: Setup queues and bindings to connect to auction participants.");

            if (debug) LOG.debug("Messages sent. Changing state.");
            plan.getStateMachine().changeState(Broker_Power_Connecting.INSTANCE, ec, ig);
        }
        if ((RunManager.isStopped())) {
            if (debug) LOG.debug("Changing state.");
            plan.getStateMachine().changeState(Broker_Power_Stop.INSTANCE, ec, ig);
        }
    }

    @Override
    public synchronized void Exit(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
        // nothing
    }
}
