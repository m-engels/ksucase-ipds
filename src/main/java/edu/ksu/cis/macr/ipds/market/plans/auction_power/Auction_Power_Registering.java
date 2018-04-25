package edu.ksu.cis.macr.ipds.market.plans.auction_power;

import edu.ksu.cis.macr.ipds.config.RunManager;
import edu.ksu.cis.macr.ipds.market.capabilities.connect.IMarketConnectCapability;
import edu.ksu.cis.macr.ipds.market.capabilities.participate.MarketParticipateCapability;
import edu.ksu.cis.macr.ipds.primary.capabilities.participate.ParticipateCapability;
import edu.ksu.cis.macr.obaa_pp.ec.base.IExecutor;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IExecutablePlan;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IPlanState;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * This state will attempt to register with the organization head. It will change state once registration is complete.
 * state.
 */
public enum Auction_Power_Registering implements IPlanState<Auction_Power_Plan> {
    INSTANCE;
    private static final Logger LOG = LoggerFactory.getLogger(Auction_Power_Registering.class);
    private static final boolean debug = false;

    @Override
    public synchronized void Enter(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
    }

    @Override
    public synchronized void Execute(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
        if (debug) LOG.debug("Starting with instance goal: {}.", ig);
        Objects.requireNonNull(ec);
        Objects.requireNonNull(ig);
        Objects.requireNonNull(ec.getCapability(IMarketConnectCapability.class), "Role requires IMarketConnectCapability.");
        Objects.requireNonNull(ec.getCapability(MarketParticipateCapability.class), "Role requires MarketParticipateCapability.");

        plan.heartBeat(this.getClass().getName());

        // initialize based on instance goal
        ec.getCapability(IMarketConnectCapability.class).init(ig);
        ec.getCapability(MarketParticipateCapability.class).init(ig);
        LOG.info("agent: initialized capabilities from goal.");


        if (!ec.getCapability(IMarketConnectCapability.class).isAllConnected()) {
            LOG.info("{} no longer connected to parent. Changing state.", ec.getUniqueIdentifier().toString());
            plan.getStateMachine().changeState(Auction_Power_Connecting.INSTANCE, ec, ig);
        } else {
            LOG.info("{} connected to broker. Registering.", ec.getUniqueIdentifier().toString());

            // if I'm the "real" persona in the sub holon agent
            if (ec.getCapability(ParticipateCapability.class) != null) {
                LOG.info("agent: registering.");

                ec.getCapability(MarketParticipateCapability.class).doRegistration();
                if (debug) LOG.debug("{} tried registration. ", ec.getUniqueIdentifier().toString());

                if (ec.getCapability(MarketParticipateCapability.class).isRegistered()) {
                    LOG.info("{} REGISTERED.  Changing state. ", ec.getUniqueIdentifier().toString());
                    plan.getStateMachine().changeState(Auction_Power.INSTANCE, ec, ig);
                } else {
                    LOG.debug("agent: {} Connected but failed to register with broker. Will retry. Verify associated goals have been triggered. {}. Unregistered={}",
                            ec.getUniqueIdentifier().toString(),
                            ec.getCapability(IMarketConnectCapability.class).getConnectionSummaryString(), ec.getCapability(MarketParticipateCapability.class).getUnregisteredParents().toString());
                }
            }

            // if I'm the proxy persona participating in an outside organization
            else {
                LOG.debug("proxy: registering. ");

                // initialize based on instance goal
                ec.getCapability(IMarketConnectCapability.class).init(ig);
                LOG.info("proxy: intialized capabilities from goal.");

                ec.getCapability(MarketParticipateCapability.class).doRegistration(ig);
                if (debug) LOG.debug("proxy {} tried registration. ", ec.getUniqueIdentifier().toString());

                if (ec.getCapability(MarketParticipateCapability.class).isRegistered()) {

                    if (debug) LOG.debug("proxy REGISTERED. changing state");
                    plan.getStateMachine().changeState(Auction_Power.INSTANCE, ec, ig);
                } else {
                    LOG.debug("proxy {} Connected but failed to register with broker. Will retry. Verify associated goals have been triggered. {}. Unregistered={}",
                            ec.getUniqueIdentifier().toString(),
                            ec.getCapability(IMarketConnectCapability.class).getConnectionSummaryString(), ec.getCapability(MarketParticipateCapability.class).getUnregisteredParents().toString());
                }
            }
        }

        if ((RunManager.isStopped())) {
            LOG.info("STOP message received.");
            plan.getStateMachine().changeState(Auction_Power_Stop.INSTANCE, ec, ig);
        }
    }

    @Override
    public synchronized void Exit(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
        // Nothing
    }
}
