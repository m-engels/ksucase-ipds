package edu.ksu.cis.macr.ipds.market.plans.broker_power;

import edu.ksu.cis.macr.ipds.config.RunManager;
import edu.ksu.cis.macr.ipds.market.MarketTerminationCriteria;
import edu.ksu.cis.macr.ipds.market.capabilities.admin.BrokerPowerCapability;
import edu.ksu.cis.macr.ipds.market.capabilities.admin.MarketAdminCapability;
import edu.ksu.cis.macr.ipds.market.capabilities.connect.IMarketConnectCapability;
import edu.ksu.cis.macr.ipds.market.capabilities.participate.IAuctionCommunicationCapability;
import edu.ksu.cis.macr.ipds.market.messages.IAuctionMessageContent;
import edu.ksu.cis.macr.ipds.primary.capabilities.manage.DateTimeCapability;
import edu.ksu.cis.macr.ipds.primary.capabilities.participate.ParticipateCapability;
import edu.ksu.cis.macr.obaa_pp.ec.base.IExecutor;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IExecutablePlan;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IPlanState;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.TreeMap;
/**
 * This is the main managing step in the auction Auction plan. If it receives a stop messages, it will move to the stop
 * state.
 */
public enum Broker_Power implements IPlanState<Broker_Power_Plan> {
    INSTANCE;
    private static final Logger LOG = LoggerFactory.getLogger(Broker_Power.class);
    private static final boolean debug = false;

    @Override
    public void Enter(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
        // Nothing
    }

    @Override
    public void Execute(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
        if (debug) LOG.debug("Starting with instance goal: {}.", ig);
        Objects.requireNonNull(ig);
        Objects.requireNonNull(ec);
        Objects.requireNonNull(ec.getCapability(BrokerPowerCapability.class), "Role requires BrokerPowerCapability.");
        Objects.requireNonNull(ec.getCapability(DateTimeCapability.class), "Role requires DateTimeCapability.");

        try {
            plan.heartBeat(this.getClass().getName(),
                    String.format("Unconnected=%s. All registered=%s.", ec.getCapability(IMarketConnectCapability.class).getUnconnectedChildren().toString(),
                            ec.getCapability(MarketAdminCapability.class).processingRegistrationIsComplete()));
        } catch (Exception ex) {
            plan.heartBeat(this.getClass().getName());
        }

        if (!ec.getCapability(MarketAdminCapability.class).isAllRegistered()) {
            LOG.info("{} all participants no longer fully registered. Changing state.", ec.getUniqueIdentifier().toString());
            plan.getStateMachine().changeState(Broker_Power_Processing_Registrations.INSTANCE, ec, ig);
        } else {

            // if I'm the "real" persona in the agent, do real work
            if (ec.getCapability(ParticipateCapability.class) != null) {

                // initialize my guidelines as defined in the parametrized instance goal
                ec.getCapability(BrokerPowerCapability.class).init(ig);
                ec.getCapability(IAuctionCommunicationCapability.class).init(ig);
                if (debug) LOG.debug("Initialized capabilities.");

                // get current timeSlice from datetime capability
                int currentTimeSlice = Objects.requireNonNull(ec.getCapability(DateTimeCapability.class).
                        getTimeSlicesElapsedSinceStart(), "ERROR: Need a timeSlice to get broker data.");
                if (debug) LOG.debug("Current time slice ={}. ", currentTimeSlice);

                if (!ec.getCapability(BrokerPowerCapability.class).isDoneIterating()) {
                    LOG.info("{} all participants have registered. Ready and brokering auctions.", ec.getUniqueIdentifier().toString());

                    int tierNumber = Objects.requireNonNull(ec.getCapability(BrokerPowerCapability.class).
                            getBrokerGuidelines().getTierNumber(), "ERROR: Need a tierNumber.");
                    if (debug) LOG.debug("tierNumber ={}. ", tierNumber);

                    TreeMap<String, IAuctionMessageContent> inputs = ec.getCapability(IAuctionCommunicationCapability.class).getBidMessages();
                    if (inputs != null) {
                        if (debug) LOG.debug("Getting bids. {} bids so far = {}", inputs.size(), inputs);

                        if (ec.getCapability(BrokerPowerCapability.class).allBidsReceived(inputs)) {
                            LOG.info("EVENT: ALL_POWER_AUCTION_BIDS_RECEIVED. Num bids = {} for purchase time slice = {}.",
                                    inputs.size(), inputs.firstEntry().getValue().getPurchaseTimeSlice());

                            IAuctionMessageContent summaryContent = ec.getCapability(BrokerPowerCapability.class).brokerAuction(inputs);
                            LOG.info("Brokered auction. Auction summary content= {}", summaryContent);

                            boolean sent = ec.getCapability(IAuctionCommunicationCapability.class).forwardToSelf(summaryContent);
                            if (debug) LOG.debug("Forward to self for review. Auction summary content sent = {}", sent);

                            if (sent) {
                                ec.getCapability(BrokerPowerCapability.class).incrementIteration();
                                if (ec.getCapability(BrokerPowerCapability.class).getBrokerGuidelines().getTierNumber() == 2) {
                                    LOG.info("SUCCESSFULLY SENT TIER 2 AUCTION RESULTS. ENDING EARLY.");
                                    MarketTerminationCriteria.setIsDone(true);
                                    LOG.info("SUCCESSFULLY SENT TIER 2 AUCTION RESULTS. ENDING EARLY.");
                                    System.exit(0);
                                } else {
                                    LOG.info("EVENT: FORWARDED UP TIER 1 AUCTION RESULTS.({}).", ec.getUniqueIdentifier());
                                }
                            }
                            LOG.debug("Auction iteration complete.");
                        }
                    }
                } else {
                    if (!(ec.getCapability(BrokerPowerCapability.class) == null)) {
                        int tierNumber = Objects.requireNonNull(ec.getCapability(BrokerPowerCapability.class).
                                getBrokerGuidelines().getTierNumber(), "ERROR: Need a tierNumber.");
                        if (debug) LOG.debug("tierNumber ={}. ", tierNumber);
                        LOG.info("Done iterating - auction results sent. Waiting for response. Tier = {}.", tierNumber);
                    }
                }
            }
        }
        if ((RunManager.isStopped())) {
            plan.getStateMachine().changeState(Broker_Power_Stop.INSTANCE, ec, ig);
        }
    }


    @Override
    public void Exit(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
        // Nothing
    }
}
