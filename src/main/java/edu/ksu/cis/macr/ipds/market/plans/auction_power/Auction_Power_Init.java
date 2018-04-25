package edu.ksu.cis.macr.ipds.market.plans.auction_power;

import edu.ksu.cis.macr.ipds.config.RunManager;
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
 * This is the first step in the plan.  It performs initialization tasks once at the beginning of the plan. When complete,
 * it will move to the main working state.
 */
public enum Auction_Power_Init implements IPlanState<Auction_Power_Plan> {
    INSTANCE;
    private static final Logger LOG = LoggerFactory.getLogger(Auction_Power_Init.class);
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

        plan.heartBeat(this.getClass().getName());

        if (ec.getCapability(ParticipateCapability.class) != null) {
            Objects.requireNonNull(ec.getCapability(IMarketConnectCapability.class), "Role requires IMarketConnectCapability.");
            ec.getCapability(IMarketConnectCapability.class).connectUp(ig);
            plan.getStateMachine().changeState(Auction_Power_Connecting.INSTANCE, ec, ig);
        }

        // if I'm the proxy persona
        else {
            plan.getStateMachine().changeState(Auction_Power_Connecting.INSTANCE, ec, ig);
        }

        if ((RunManager.isStopped())) {
            plan.getStateMachine().changeState(Auction_Power_Stop.INSTANCE, ec, ig);
        }
    }

    @Override
    public synchronized void Exit(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
        // nothing
    }
}
