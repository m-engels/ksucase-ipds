package edu.ksu.cis.macr.ipds.market.plans.auction_power;

import edu.ksu.cis.macr.ipds.config.RunManager;
import edu.ksu.cis.macr.ipds.market.capabilities.participate.IAuctionCommunicationCapability;
import edu.ksu.cis.macr.ipds.market.capabilities.participate.IAuctionPowerCapability;
import edu.ksu.cis.macr.ipds.market.capabilities.participate.MarketParticipateCapability;
import edu.ksu.cis.macr.ipds.primary.capabilities.manage.DateTimeCapability;
import edu.ksu.cis.macr.obaa_pp.ec.base.IExecutor;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IExecutablePlan;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IPlanState;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * The last step in the plan. It allows for any functionality needed when exiting the plan.
 */
public enum Auction_Power_Sent implements IPlanState<Auction_Power_Plan> {
    INSTANCE;
    private static final Logger LOG = LoggerFactory.getLogger(Auction_Power_Sent.class);
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

        plan.heartBeat(this.getClass().getName());

        if (!ec.getCapability(MarketParticipateCapability.class).isRegistered()) {
            LOG.info("{} no longer registered with parent. Changing state.", ec.getUniqueIdentifier().toString());
            plan.getStateMachine().changeState(Auction_Power_Registering.INSTANCE, ec, ig);
        } else {
            LOG.info("{} registered and bid sent. Waiting for response.", ec.getUniqueIdentifier().toString());

            // get current timeslice
            int currentTS = Objects.requireNonNull(ec.getCapability(DateTimeCapability.class).
                    getTimeSlicesElapsedSinceStart(), "ERROR: Need a current time to bid in auctions.");
            if (debug) LOG.debug("Current time slice is {}.", currentTS);

            // initialize my guidelines as defined in the parametrized instance goal
            ec.getCapability(IAuctionPowerCapability.class).init(ig);
            if (debug) LOG.debug("Initialized IAuctionPowerCapability.");
            ec.getCapability(IAuctionCommunicationCapability.class).init(ig);
            if (debug) LOG.debug("Initialized IAuctionCommunicationCapability.");
            if (debug)
                LOG.debug("Auction guidelines are {}.", ec.getCapability(IAuctionPowerCapability.class).getAuctionGuidelines());

            if (ec.getCapability(IAuctionPowerCapability.class).isOpen(currentTS)) {
                if (debug) LOG.debug("Waiting for bid reponse. ");

                boolean done = ec.getCapability(IAuctionCommunicationCapability.class).getBidResponse();
                if (done) {
                    LOG.debug("Auction participation complete.");
                    plan.getStateMachine().changeState(Auction_Power_Stop.INSTANCE, ec, ig);
                }
            }
        }

        if ((RunManager.isStopped())) {
            plan.getStateMachine().changeState(Auction_Power_Stop.INSTANCE, ec, ig);
        }

    }

    @Override
    public synchronized void Exit(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
        // Nothing
    }
}
