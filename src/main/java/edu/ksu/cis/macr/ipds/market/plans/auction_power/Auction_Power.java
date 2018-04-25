package edu.ksu.cis.macr.ipds.market.plans.auction_power;

import edu.ksu.cis.macr.aasis.common.IConnections;
import edu.ksu.cis.macr.ipds.config.RunManager;
import edu.ksu.cis.macr.ipds.market.capabilities.participate.IAuctionCommunicationCapability;
import edu.ksu.cis.macr.ipds.market.capabilities.participate.IAuctionPowerCapability;
import edu.ksu.cis.macr.ipds.market.capabilities.participate.MarketParticipateCapability;
import edu.ksu.cis.macr.ipds.market.messages.IAuctionMessage;
import edu.ksu.cis.macr.ipds.market.messages.IAuctionMessageContent;
import edu.ksu.cis.macr.ipds.primary.capabilities.manage.DateTimeCapability;
import edu.ksu.cis.macr.obaa_pp.ec.base.IExecutor;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IExecutablePlan;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IPlanState;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * This is the main managing step in the auction power plan. If it receives a stop messages, it will move to the stop
 * state.
 */
public enum Auction_Power implements IPlanState<Auction_Power_Plan> {
    INSTANCE;
    private static final Logger LOG = LoggerFactory.getLogger(Auction_Power.class);
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
        Objects.requireNonNull(ec.getCapability(IAuctionPowerCapability.class), "Role requires IAuctionPowerCapability.");

        plan.heartBeat(this.getClass().getName());

        // initialize my guidelines as defined in the parametrized instance goal
        ec.getCapability(IAuctionPowerCapability.class).init(ig);
        if (debug) LOG.debug("Initialized IAuctionPowerCapability.");

        ec.getCapability(MarketParticipateCapability.class).init(ig);
        if (debug) LOG.debug("Initialized MarketParticipateCapability.");

        ec.getCapability(IAuctionCommunicationCapability.class).init(ig);
        if (debug) LOG.debug("Initialized AuctionCommunicationCapability.");

        if (!ec.getCapability(MarketParticipateCapability.class).isRegistered()) {
            LOG.debug("{} no longer registered with parent. Changing state.", ec.getUniqueIdentifier().toString());
            plan.getStateMachine().changeState(Auction_Power_Registering.INSTANCE, ec, ig);
            return;
        }
        LOG.info("{} registered with broker. Available for auctions.", ec.getUniqueIdentifier().toString());

        // get current timeslice
        int currentTimeSlice = Objects.requireNonNull(ec.getCapability(DateTimeCapability.class).
                getTimeSlicesElapsedSinceStart(), "ERROR: Need a current time to bid in auctions.");
        if (debug) LOG.debug("Current time slice is {}.", currentTimeSlice);

        // initialize my guidelines as defined in the parametrized instance goal
        ec.getCapability(IAuctionPowerCapability.class).init(ig);
        LOG.debug("Initialized IAuctionPowerCapability.");
        ec.getCapability(IAuctionCommunicationCapability.class).init(ig);
        if (debug) LOG.debug("Initialized IAuctionCommunicationCapability.");
        if (debug)
            LOG.debug("Auction guidelines are {}.", ec.getCapability(IAuctionPowerCapability.class).getAuctionGuidelines());

        if (ec.getCapability(IAuctionPowerCapability.class).getAuctionGuidelines() == null) {
            LOG.error("Auction guidelines = null. Cannot auction without auction guidelines.");
            System.exit(-59);
        }
        if(ec.getCapability(IAuctionPowerCapability.class).getAuctionGuidelines().getTierNumber()<1){
            LOG.error("Auction tier number cannot be less than 1. ");
            System.exit(-14);
        }
        if(ec.getCapability(IAuctionPowerCapability.class).getAuctionGuidelines().getTierNumber()>2){
            LOG.error("Auction tier number cannot be greater than 2. ");
            System.exit(-14);
        }
        if (ec.getCapability(IAuctionPowerCapability.class).getAuctionGuidelines().getTierNumber() == 1) {
            LOG.debug("This is the lowest tier auction (level 1) - guidelines are provided with the goals.");

            final long openingTimeSlice = ec.getCapability(IAuctionPowerCapability.class).getAuctionGuidelines().getOpeningTimeSlice();
            if (debug) LOG.debug("Auction opening TS= {}.", openingTimeSlice);

            final double bidQuantity = ec.getCapability(IAuctionPowerCapability.class).getAuctionGuidelines().getkWh();
            if (debug) LOG.debug("Auction bidQuantity = {}.", bidQuantity);

            if (currentTimeSlice == openingTimeSlice && (bidQuantity > 0)) {
                if (debug)
                    LOG.debug("Current TS={}, Opening TS={}. Ready to send {} kWh bid. ", currentTimeSlice, openingTimeSlice, bidQuantity);

                // create auction message only once when equal (just for initial testing)
                IAuctionMessage msg = ec.getCapability(IAuctionPowerCapability.class).createAuctionMessage(currentTimeSlice);
                if (debug) LOG.debug("Action message = {}", msg);
                boolean sent = ec.getCapability(IAuctionCommunicationCapability.class).sendRemoteMessage(msg);
                if (sent) {
                    LOG.info("EVENT: POWER_BID_SENT. Bidder = {} sent market message with auction bid. Message = {}",
                            ec.getUniqueIdentifier().toString(), msg.toString());
                    plan.getStateMachine().changeState(Auction_Power_Sent.INSTANCE, ec, ig);
                }
            }
        }
        if (ec.getCapability(IAuctionPowerCapability.class).getAuctionGuidelines().getTierNumber() == 2) {
            LOG.info("This is a top-level auction.  Checking for summary auction messages.");

            // check for a new power message from self
            IAuctionMessage m = ec.getCapability(IAuctionCommunicationCapability.class).checkFromSelf();
            if (m == null) {
                LOG.info("top-level auction message from self was null. Will retry.");
                return;
            }
            LOG.info("agent: org participant received reviewed message from self: {}", m.toString());

            long messageTimeSlice = ((IAuctionMessageContent) m.getContent()).getPurchaseTimeSlice();
            LOG.debug("agent: This timeslice={}, message timeslice={}", currentTimeSlice, messageTimeSlice);

            // track with scenario for simulation testing
//                  RunManager.addAuctionMessageSelfToSub(m.getLocalSender().toString(), m.getLocalReceiver().toString(), messageTimeSlice);

            ec.getCapability(IAuctionPowerCapability.class).init(ig);
           LOG.debug("Initialized IAuctionPowerCapability.");

            // if not null and the time slice is different, then foward it...
            final IConnections parents = ec.getCapability(IAuctionPowerCapability.class).getParentConnections();
            if (parents == null) {
                LOG.error("Can't auction - there are no broker connections");
                System.exit(-4);
            }
            LOG.debug("agent: got list of up connections {}.", parents.toString());

            ec.getCapability(IAuctionCommunicationCapability.class).sendUp(m, parents);
            LOG.info("EVENT: tier 2 auction participant forwarded message to org administrator: {}. Up list: {}", m, parents);
            LOG.info("this is almost the end of the tier 2 auction - exiting early to confirm progress so far. Remove exit call to continue.");
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
