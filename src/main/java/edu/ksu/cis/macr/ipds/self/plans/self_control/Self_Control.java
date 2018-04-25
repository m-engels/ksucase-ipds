package edu.ksu.cis.macr.ipds.self.plans.self_control;


import edu.ksu.cis.macr.ipds.config.RunManager;
import edu.ksu.cis.macr.ipds.grid.capabilities.participate.GridParticipateCapability;
import edu.ksu.cis.macr.ipds.market.capabilities.connect.IMarketConnectCapability;
import edu.ksu.cis.macr.ipds.market.capabilities.participate.IAuctionCommunicationCapability;
import edu.ksu.cis.macr.ipds.market.capabilities.participate.MarketParticipateCapability;
import edu.ksu.cis.macr.ipds.market.messages.AuctionMessage;
import edu.ksu.cis.macr.ipds.market.messages.AuctionPerformative;
import edu.ksu.cis.macr.ipds.market.messages.IAuctionMessage;
import edu.ksu.cis.macr.ipds.market.messages.IAuctionMessageContent;
import edu.ksu.cis.macr.ipds.primary.capabilities.manage.DateTimeCapability;
import edu.ksu.cis.macr.ipds.primary.capabilities.participate.IPowerCommunicationCapability;
import edu.ksu.cis.macr.ipds.primary.messages.IPowerMessage;
import edu.ksu.cis.macr.ipds.primary.messages.IPowerMessageContent;
import edu.ksu.cis.macr.ipds.primary.messages.PowerMessage;
import edu.ksu.cis.macr.ipds.self.capabilities.admin.SelfControlCapability;
import edu.ksu.cis.macr.obaa_pp.ec.base.IExecutor;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IExecutablePlan;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IPlanState;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * The {@code Self_Control} state is the main step in the {@code Self_Control_Plan}.  It will monitor connections and
 * attempt to restart any that have been dropped.  It retrieves messages from sensor sub agents and forwards them to sub
 * agents participating in external organizations, reviewing and biasing content before sending to reflect the multiple
 * objectives and biases of this agent.
 */
public enum Self_Control implements IPlanState<Self_Control_Plan> {
    INSTANCE;
    private static final Logger LOG = LoggerFactory.getLogger(Self_Control.class);
    private static final boolean debug = false;

    @Override
    public synchronized void Enter(final IExecutablePlan plaen, final IExecutor ec, final InstanceGoal<?> ig) {
        // Nothing
    }

    @Override
    public synchronized void Execute(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
        if (debug) LOG.debug("Starting with instance goal: {}.", ig);
        Objects.requireNonNull(ec);
        Objects.requireNonNull(ig);
        Objects.requireNonNull(ec.getCapability(SelfControlCapability.class), "Role requires SelfControlCapability.");
        Objects.requireNonNull(ec.getCapability(DateTimeCapability.class), "Role requires DateTimeCapability.");
        Objects.requireNonNull(ec.getCapability(IMarketConnectCapability.class), "Role requires IMarketConnectCapability.");

        plan.heartBeat(this.getClass().getName());

        // initialize guidelines from instance goal
        ec.getCapability(SelfControlCapability.class).init(ig);
        LOG.debug("Set SelfControlCapability guidelines from instance goal. {}", ig);

        String myPersona = ec.getUniqueIdentifier().toString();

        // listen for disconnects and updates...........................
        //   List<? extends IConnectionGuidelines> lst = ec.getCapability(IConnectCapability.class).getAllConnections().getListConnectionGuidelines();
        //  if (debug) LOG.debug("Maintaining {} authorized connections.", lst.size());

        // get current timeSlice
        long currentTimeSlice = Objects.requireNonNull(ec.getCapability(DateTimeCapability.class).
                getTimeSlicesElapsedSinceStart(), "ERROR: Need a timeSlice to get sensor data.");

        if (RunManager.allPowerMessagesSentFromSensorToSelf(currentTimeSlice)) {

            // initialize guidelines from instance goal
            ec.getCapability(IPowerCommunicationCapability.class).init(ig);
            LOG.debug("Set guidelines from instance goal. {}", ig);

            // check for messages from sensor persona
            IPowerMessage localPowerMessage = ec.getCapability(IPowerCommunicationCapability.class).checkForLocalPowerMessageFromWorker();
            if (debug) LOG.debug("Self checked for local power message from sensor worker and got {}.", localPowerMessage);

            // if not null and the time slice is different, then process it...
            if (localPowerMessage != null) {
                LOG.info("Self RECEIVED LOCAL GRID MESSAGE FROM SENSOR SUBAGENT {}. Finding sub holon persona to forward to.", localPowerMessage);
                if (debug) LOG.debug("localPowerMessage={}", localPowerMessage.toString());

                long messageTimeSlice = ((IPowerMessageContent) localPowerMessage.getContent()).getTimeSlice();
                if (debug) LOG.debug("This timeslice={}, message timeslice={}", currentTimeSlice, messageTimeSlice);

                // for simulation reasons, track the number in the scenario object...
                RunManager.addPowerMessageSentFromSensorToSelfRec(localPowerMessage.getLocalSender().toString(), localPowerMessage.getLocalReceiver().toString(), messageTimeSlice);

                UniqueIdentifier subIdentifier = ec.getCapability(SelfControlCapability.class).findHolon();
                if (debug) LOG.debug("The sub holon persona is {}.", subIdentifier);
                if (subIdentifier != null) {
                    boolean success = ec.getCapability(IPowerCommunicationCapability.class).forwardToParticipant(localPowerMessage, subIdentifier);
                    LOG.info("FORWARDED GRID MESSAGE to SUB HOLON {}. Success={}. Message originally received by self was {}.", subIdentifier, success, localPowerMessage);
                }
            }

            if (!ec.getUniqueIdentifier().toString().startsWith("selfH")) {
                if (debug) LOG.debug("Self checking for grid messages to forward.");
                IPowerMessage m = ec.getCapability(IPowerCommunicationCapability.class).checkFromAdmin(ec.getUniqueIdentifier().toString());
                if (debug) LOG.debug("SELF got admin message {}.", m);

                // if not null and the time slice is different, then process it...
                if (m != null) {
                    LOG.info("SELF RECEIVED ADMIN GRID MESSAGE from super: {}. ", m.toString());
                    long messageTimeSlice = ((IPowerMessageContent) m.getContent()).getTimeSlice();
                    if (debug) LOG.debug("This timeslice={}, message timeslice={}", currentTimeSlice, messageTimeSlice);

                    //TODO: Greg: lookup org identifier - for grid control, it's ""
                    UniqueIdentifier subIdentifier = ec.getCapability(SelfControlCapability.class).findParticipant(GridParticipateCapability.class.getCanonicalName(),"");
                    if (debug) LOG.debug("The sub holon persona is {}.", subIdentifier);
                    if (subIdentifier != null) {
                        // the local message is the content of the remote messsage
                        IPowerMessage lmsg = PowerMessage.createLocal(ec.getUniqueIdentifier(), subIdentifier, m.getPerformativeType(), m.getContent());
                        boolean success = ec.getCapability(IPowerCommunicationCapability.class).forwardToParticipant(lmsg, subIdentifier);
                        LOG.info("FORWARDED MESSAGE to PARTICIPANT. Success={}. {}.", success, lmsg);
                    }
                }
            }
        }
        LOG.debug("{} is all connected. Will check for auction messages.", ec.getUniqueIdentifier().toString());
        if (null == ec.getCapability(IAuctionCommunicationCapability.class)) return;

        if (!myPersona.startsWith("selfH")) {
            LOG.debug("Not the lowest level. Will initialize capabilities from instance goal = {}.", ig);
            ec.getCapability(IAuctionCommunicationCapability.class).init(ig);
            LOG.debug("Set IAuctionCommunicationCapability guidelines from instance goal. {}", ig);

            if (debug) LOG.debug("Self checking for market messages to forward.");
            IAuctionMessage m = ec.getCapability(IAuctionCommunicationCapability.class).checkFromAdmin(myPersona);
            if (m == null) return;

            LOG.info("EVENT: SELF_REVIEW_OF_BROKERED_AUCTION. message={}. ", m.toString());
            if (m.getPerformativeType() == AuctionPerformative.BID) {
                if (debug) LOG.debug("Bid message received for review.");
                if (!ec.getCapability(IAuctionCommunicationCapability.class).isTopTier()) {
                    if (debug) LOG.debug("This aggregate bid message review is NOT the final auction tier.");
                    // if message is a bid and this is not the top tier action than forward up to next higher tier.
                    long messageTimeSlice = ((IAuctionMessageContent) m.getContent()).getPurchaseTimeSlice();
                    if (debug)
                        LOG.debug("This timeslice={}, message timeslice={}", currentTimeSlice, messageTimeSlice);

                    //TODO: Greg: lookup org identifier - for power market options, it's "A"
                    UniqueIdentifier subIdentifier = ec.getCapability(SelfControlCapability.class).findParticipant(MarketParticipateCapability.class.getCanonicalName(), "A");
                    LOG.debug("The auction participant is {}.", subIdentifier);
                    if (subIdentifier != null) {
                        // the local message is the content of the remote messsage
                        IAuctionMessage lmsg = AuctionMessage.createLocal(ec.getUniqueIdentifier(), subIdentifier, m.getPerformativeType(), m.getContent());
                        boolean success = ec.getCapability(IAuctionCommunicationCapability.class).forwardToParticipant(lmsg, subIdentifier);
                        LOG.info("FORWARDED MESSAGE TO SUB AGENT PARTICIPATING IN HIGHER TIER AUCTION. Success={}. {}.", success, lmsg);
                    }
                } else {
                    LOG.debug("This aggregate bid message review IS the final auction tier.");
                    // if message is a bid and this is the top tier action than send down iteration results
                    boolean success = ec.getCapability(IAuctionCommunicationCapability.class).sendDownResults(m);
                    LOG.info("SENT TOP-TIER AUCTION RESULTS DOWN to PARTICIPANTS. Success={}. orginal message = {}.", success, m);
                    // if top-tier and this is the final iteration, then my work on this goal is done.
                }
            } // end bid message processing
        }
        if ((RunManager.isStopped())) {
            LOG.info("Changing state.");
            plan.getStateMachine().changeState(Self_Control_Stop.INSTANCE, ec, ig);
        }
    }

    @Override
    public synchronized void Exit(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
        // Nothing
    }
}
