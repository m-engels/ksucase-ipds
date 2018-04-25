package edu.ksu.cis.macr.ipds.market.capabilities.participate;

import com.rabbitmq.client.*;
import edu.ksu.cis.macr.aasis.agent.persona.AbstractOrganizationCapability;
import edu.ksu.cis.macr.aasis.agent.persona.IInternalCommunicationCapability;
import edu.ksu.cis.macr.aasis.agent.persona.IOrganization;
import edu.ksu.cis.macr.aasis.agent.persona.IPersona;
import edu.ksu.cis.macr.aasis.common.IConnectionGuidelines;
import edu.ksu.cis.macr.aasis.common.IConnections;
import edu.ksu.cis.macr.aasis.messaging.IMessagingFocus;
import edu.ksu.cis.macr.aasis.messaging.MessagingReliabilityManager;
import edu.ksu.cis.macr.goal.model.InstanceParameters;
import edu.ksu.cis.macr.ipds.config.RunManager;
import edu.ksu.cis.macr.ipds.market.goals.MarketGoalParameters;
import edu.ksu.cis.macr.ipds.market.guidelines.auction.IAuctionGuidelines;
import edu.ksu.cis.macr.ipds.market.guidelines.broker.IBrokerGuidelines;
import edu.ksu.cis.macr.ipds.market.messages.AuctionMessage;
import edu.ksu.cis.macr.ipds.market.messages.AuctionPerformative;
import edu.ksu.cis.macr.ipds.market.messages.IAuctionMessage;
import edu.ksu.cis.macr.ipds.market.messages.IAuctionMessageContent;
import edu.ksu.cis.macr.ipds.market.messaging.MarketMessagingFocus;
import edu.ksu.cis.macr.ipds.market.messaging.MarketMessagingManager;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import edu.ksu.cis.macr.organization.model.identifiers.StringIdentifier;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Market participation communication capability.
 */
public class AuctionCommunicationCapability extends AbstractOrganizationCapability implements IAuctionCommunicationCapability {
    public static final String BROKER_CONNECTIONS = "brokerConnections";
    final static String AUCTION_CONNECTIONS = "auctionConnections";
    private static final Logger LOG = LoggerFactory.getLogger(AuctionCommunicationCapability.class);
    private static final boolean debug = false;
    private static final String COMMUNICATION_CHANNEL_ID = "AuctionCommunicationChannel";
    private static final String MARKET_SUFFIX = "A";
    private static final IMessagingFocus messagingFocus = MarketMessagingFocus.MARKET;
    private static Channel channel;
    private static ConcurrentLinkedQueue<IAuctionMessage> localMessages = new ConcurrentLinkedQueue<>();
    TreeMap<String, IAuctionMessageContent> currentReads = new TreeMap<String, IAuctionMessageContent>();
    double communicationReliability = 1.0;
    double communicationDelay = 0.0;
    private QueueingConsumer consumer;
    private IConnections brokerConnections;
    private IConnections auctionConnections;
    private IBrokerGuidelines brokerGuidelines;
    private IAuctionGuidelines auctionGuidelines;


    /**
     * Constructs a new instance of {@code AuctionCommunication}.
     *  @param owner        - the entity to which this capability belongs.
     * @param organization - the {@code Organization} in which this {@code IAgent} acts.
     */
    public AuctionCommunicationCapability(IPersona owner, IOrganization organization) {
        super(IAuctionCommunicationCapability.class, owner, organization);
        this.setOwner(Objects.requireNonNull(owner));
        channel = MarketMessagingManager.getChannel(messagingFocus);
        consumer = new QueueingConsumer(Objects.requireNonNull(channel, "Error null channel in receive()."));
        initializeReliabilityAndDelay();
    }

    /**
     * @param owner        - the entity to which this capability belongs.
     * @param organization - the {@code Organization} in which this {@code IAgent} acts.
     * @param Parameter    - optional parameter
     */
    public AuctionCommunicationCapability(IPersona owner, IOrganization organization, String Parameter) {
        super(IAuctionCommunicationCapability.class, owner, organization);
        this.setOwner(Objects.requireNonNull(owner));
        channel = MarketMessagingManager.getChannel(messagingFocus);
        initializeReliabilityAndDelay();
    }

    public static synchronized String buildQueueLinkFromSenderAndReceiver(final String remoteSender, final String remoteReceiver) {
        return remoteSender + "-" + remoteReceiver;
    }

    public synchronized static String getMapAuctionString(TreeMap<String, IAuctionMessageContent> treeMap) {
        StringBuilder b = new StringBuilder();
        for (Map.Entry<String, IAuctionMessageContent> entry : treeMap.entrySet()) {
            b.append(entry.getKey()).append(": ");
            b.append(entry.getValue().toString()).append("\n");
        }
        return b.toString();
    }

    private synchronized void initializeReliabilityAndDelay() {
        try {
            this.communicationReliability = MessagingReliabilityManager.getCommunicationReliability();
            if (debug) LOG.debug("\t New  communicationReliability.");
            communicationDelay = MessagingReliabilityManager.getCommunicationDelay();
        } catch (Exception e) {
            // just use the defaults
            communicationDelay = 0.0;
            communicationReliability = 1.0;
        }
        if (debug)
            LOG.debug("New comm cap with reliability = {} and delay = {}", communicationReliability, communicationDelay);
    }

    public synchronized IConnections getAuctionConnections() {
        return this.auctionConnections;
    }

    public synchronized void setAuctionConnections(IConnections auctionConnections) {
        this.auctionConnections = auctionConnections;
    }

    public synchronized IAuctionGuidelines getAuctionGuidelines() {
        return this.auctionGuidelines;
    }

    public synchronized void setAuctionGuidelines(IAuctionGuidelines auctionGuidelines) {
        this.auctionGuidelines = auctionGuidelines;
    }

    public synchronized IConnections getBrokerConnections() {
        return this.brokerConnections;
    }

    public synchronized void setBrokerConnections(IConnections brokerConnections) {
        this.brokerConnections = brokerConnections;
    }

    public synchronized IBrokerGuidelines getBrokerGuidelines() {
        return this.brokerGuidelines;
    }

    public synchronized void setBrokerGuidelines(IBrokerGuidelines brokerGuidelines) {
        this.brokerGuidelines = brokerGuidelines;
    }

    private Queue<IAuctionMessage> getLocalMessages() {
        LOG.debug(" {} LOCAL MARKET MESSAGES in Queue ", localMessages.size());
        return localMessages;
    }

    @Override
    public synchronized IAuctionMessage checkFromSelf() {
        if (debug &&  localMessages.size() > 0) LOG.info("There are {} messages to check.",  localMessages.size());
        for (IAuctionMessage checkMessage : this.getLocalMessagesAsList()) {
            String sender = checkMessage.getLocalSender().toString(); // e.g. selfH44
            String senderNoSelf = checkMessage.getLocalSender().toString().replace("self", "");  // e.g. H44
            String senderNoSelfWithOrgSuffix = checkMessage.getLocalSender().toString().replace("self", "")+MARKET_SUFFIX;  // e.g. H44A
            String me = getOwner().getUniqueIdentifier().toString(); // eg. H44AinN43A
            String meNoInOrg = me.substring(0, me.indexOf("in"));  // e.g. H44A
            LOG.debug("  CHECKING: checkFromSelf sender= {} senderNoSelf={} me={} meNoInOrg={}. {}", sender, senderNoSelf, me, meNoInOrg, checkMessage);
            if (sender.contains("self") && senderNoSelfWithOrgSuffix.equals(meNoInOrg)) {
                LOG.info("MESSAGE forwarded to participant RECEIVED: {}.", checkMessage.toString());
                return checkMessage;
            }
        }
        return null;
    }

    @Override
    public synchronized void sendUp(final IAuctionMessage localMessage, IConnections upConnections) {
        LOG.info("sendUp: received local message was: {}", localMessage);
        if (localMessage == null) return;

        // change receiver to admin and send over external messaging system.
        for (IConnectionGuidelines cg : upConnections.getListConnectionGuidelines()) {
            LOG.debug("Guidelines include super holon {}.", cg.getOtherAgentAbbrev());

            UniqueIdentifier newSender = localMessage.getLocalReceiver();
            UniqueIdentifier newReceiver = StringIdentifier.getIdentifier(cg.getExpectedMasterAbbrev());
            IAuctionMessage newLocalMessage = AuctionMessage.createLocal(newSender, newReceiver, localMessage.getPerformativeType(), localMessage.getContent());

            String sender = newSender.toString();
            String rec = newReceiver.toString();

            // new local message becomes the entire message contents for the remote message
            IAuctionMessage remoteMessage = AuctionMessage.createRemote(sender, rec, newLocalMessage.getPerformativeType(), newLocalMessage.getContent());
            LOG.info("Created REMOTE MESSAGE for org admin: {}", remoteMessage.toString());
            Objects.requireNonNull(remoteMessage, "ERROR: Can't send a null  message.");
            Objects.requireNonNull(channel, "ERROR: null channel in send().");
            sendRemoteMessage(remoteMessage, remoteMessage.getRemoteSender() + "-" + remoteMessage.getRemoteReceiver());
        }
    }

    @Override
    public synchronized boolean isTopTier() {
        //TODO: Greg: set a user-defined variable for numberOfAuctionTiers, add it to run.properties and make it work in RunManager as suggested below.
        int numAuctionTiers = 2;
       // int numAuctionTiers = RunManager.getNumberOfAuctionTiers();
        return (numAuctionTiers == this.brokerGuidelines.getTierNumber());
    }

    @Override
    public synchronized boolean sendDownResults(IAuctionMessage originalRemoteMessage) {
        LOG.info("sendDownResults: received remote message was: {}", originalRemoteMessage);
        if (originalRemoteMessage == null) return false;

        boolean success = false;
        // change sender to this agent and receiver to participant and send down via external messaging system.
        for (IConnectionGuidelines cg : this.getAuctionConnections().getListConnectionGuidelines()) {
            if (debug) LOG.debug("Guidelines include participant {}.", cg.getOtherAgentAbbrev());

            UniqueIdentifier newSender = this.owner.getUniqueIdentifier();
            UniqueIdentifier newReceiver = StringIdentifier.getIdentifier(cg.getOtherAgentAbbrev());
            IAuctionMessage newLocalMessage = AuctionMessage.createLocal(newSender, newReceiver, AuctionPerformative.AUCTION_RESULT, originalRemoteMessage.getContent());

            String sender = newSender.toString();
            String rec = newReceiver.toString();

            // new local message becomes the entire message contents for the remote message
            IAuctionMessage remoteMessage = AuctionMessage.createRemote(sender, rec, newLocalMessage.getPerformativeType(), newLocalMessage.getContent());
            LOG.info("Created REMOTE MESSAGE for org admin: {}", remoteMessage.toString());
            Objects.requireNonNull(remoteMessage, "ERROR: Can't send a null  message.");
            Objects.requireNonNull(channel, "ERROR: null channel in send().");
            String queueLink = buildQueueLinkFromSenderAndReceiver(sender, rec);
            sendRemoteMessage(remoteMessage, queueLink);
        }
        return success;
    }

    @Override
    public synchronized IAuctionMessage checkFromAdmin(final String ownerSelfPersona) {
        if (ownerSelfPersona.startsWith("selfH") || ownerSelfPersona.startsWith("selfF") || ownerSelfPersona.startsWith("selfS")) {
            if (debug)
                LOG.debug("This is persona {} and these agents don't have a market admin persona.", ownerSelfPersona);
            return null;
        }
        if (debug)
            LOG.debug("{} Checking for REMOTE AUCTION MESSAGE (Aggregate) from an internal admin persona to {}", ownerSelfPersona, ownerSelfPersona);
        try {
            final String master = this.getAuctionConnections().getListConnectionGuidelines().get(0).getExpectedMasterAbbrev(); // ownerSelfPersona.replace("self", "");
            final String queueLink = buildQueueLinkFromSenderAndReceiver(master, ownerSelfPersona);
            if (debug)
                LOG.debug("{} Checking for REMOTE AUCTION MESSAGE (Aggregate) on queue link = {} from an internal super persona to {}", ownerSelfPersona, queueLink, ownerSelfPersona);
            // IAuctionMessage recMessage = this.receiveRemoteFromAdmin(queueLink);
            IAuctionMessage recMessage = this.remoteRECEIVE(queueLink);
            if (recMessage != null) {
                LOG.info("{} REMOTE MESSAGE from internal acting MARKET admin persona RECEIVED: {}", ownerSelfPersona, recMessage.toString());

                // if this is the second tier auction and we've done all iterations, the auction is complete
                int tierNumber = this.brokerGuidelines.getTierNumber();
                int iteration = this.getBrokerGuidelines().getIteration();
                int maxIterations = this.getBrokerGuidelines().getMaxIteration();
                if (2 == tierNumber && iteration >= maxIterations) {
                    LOG.info("EVENT: AUCTION_COMPLETE. tierNumber={}, iteration={}, results={}.", tierNumber, iteration, recMessage.toString());

                }
                return recMessage;
            }
        } catch (ShutdownSignalException | ConsumerCancelledException | IOException | InterruptedException e) {
            LOG.error("ERROR: checking for power message from an internal acting super ({})", e.getMessage());
            System.exit(-49);
        }
        return null;
    }

    @Override
    public synchronized boolean forwardToParticipant(IAuctionMessage origMessage, UniqueIdentifier subIdentifier) {
        IAuctionMessage localMessage = AuctionMessage.createLocal(this.getOwner().getUniqueIdentifier(), subIdentifier, origMessage.getPerformativeType(), origMessage.getContent());
        if (debug) LOG.debug("New market message for sub is {}", localMessage);
        return sendLocal(localMessage);
    }

    @Override
    public synchronized boolean forwardToSelf(final IAuctionMessageContent content) {
        String selfPersona = this.owner.getPersonaControlComponent().getLocalMaster().toString();
        IAuctionMessage message = AuctionMessage.createRemote(this.owner.getUniqueIdentifier().toString(), selfPersona, AuctionPerformative.BID, content);
        String link = this.owner.getUniqueIdentifier().toString() + "-" + selfPersona;
        // send to self via external messaging system
        boolean sent = sendRemoteMessage(message, link);
        LOG.info("FORWARDED TO SELF. Original message content was: {}", sent, content.toString());
        return sent;
    }

    @Override
    public synchronized boolean getBidResponse() {
        if (debug) LOG.debug("Beginning getBidResponse ");
        final String broker = this.brokerConnections.getListConnectionGuidelines().get(0).getOtherAgentAbbrev();
        final String myPersona = this.owner.getUniqueIdentifier().toString();
        final String link = broker + "-" + this.owner.getUniqueIdentifier().toString();

        IAuctionMessage foundMessage = null;
        try {
            foundMessage = receiveRemoteAuctionMessage(link);
        } catch (IOException | InterruptedException e) {
            LOG.error("Error receiving remote auction message on link {}", link);
            e.printStackTrace();
        }
        IAuctionMessageContent content = null;
        if (foundMessage != null) {
            String other = foundMessage.getRemoteSender();
            if (debug)
                LOG.debug("Broker {} picked up a auction message from {} via {}. {}", myPersona, other, link, foundMessage.toString());

            if (debug) LOG.info("response received.");
            content = (IAuctionMessageContent) foundMessage.getContent();
        }
        if (content != null) {
            if (debug) LOG.debug("BID RESPONSE: {}", content);
        }
        return content != null;
    }

    @Override
    public TreeMap<String, IAuctionMessageContent> getBidMessages() {
        if (debug) LOG.debug("Beginning processBids ");
        TreeMap<String, IAuctionMessageContent> bidMap = this.getMostRecentReadings();
        int readCount = bidMap.size();
        int subCount = this.auctionConnections.getListConnectionGuidelines().size();
        if (debug) LOG.info("{} of {} reports. {}", readCount, subCount, getMapAuctionString(bidMap));
        return bidMap;
    }

    @Override
    public  IInternalCommunicationCapability.ICommunicationChannel getCommunicationChannel() {
        return this;
    }

    @Override
    public void initializeChildConnections(InstanceGoal<?> instanceGoal) {
        if (debug) LOG.debug("Initializing {} from goal: {}.", AUCTION_CONNECTIONS, instanceGoal);
        // Get the parameter values from the existing active instance goal
        final InstanceParameters params = Objects.
                requireNonNull((InstanceParameters) instanceGoal
                        .getParameter());
        if (debug) LOG.debug("Initializing {} from params: {}.", AUCTION_CONNECTIONS, params);
        setAuctionConnections(IConnections.extractConnections(params, AUCTION_CONNECTIONS));
    }

    @Override
    public synchronized void init(final InstanceGoal<?> ig) {
        if (debug) LOG.debug("Initializing guidelines from goal: {}.", ig);
        final InstanceParameters params = Objects.requireNonNull((InstanceParameters) ig.getParameter());
        if (debug) LOG.info("Initializing with the given goal parameter guidelines: {}.", params);
        if (params == null) {
            LOG.error("Error: we need goal parameters to guide the auction communiction. ");
            System.exit(-4);
        }
        // brokers have broker guidelines and auction connections (but auctioners won't)
        if (IBrokerGuidelines.extractBrokerGuidelines(params) != null) {
            this.setBrokerGuidelines(IBrokerGuidelines.extractBrokerGuidelines(params));
            if (debug) LOG.info("Broker guidelines = {}.", this.brokerGuidelines);
            if (this.brokerGuidelines == null) {
                LOG.error("Broker trying to initialize an external organization with no broker guidelines. params={}", params);
                System.exit(-51);
            }
            this.setAuctionConnections(IConnections.extractConnections(params, "auctionConnections"));
            if (this.auctionConnections == null) {
                LOG.error("Broker trying to initialize an external organization with no auction participants. params={}", params);
                System.exit(-56);
            } else {
                if (debug) LOG.info("Starting initialization of new organization by {}. ", this.getOwner());
                if (debug)
                    LOG.info("{} authorized connections to participants.", auctionConnections.getListConnectionGuidelines().size());
                if (debug) LOG.info("Starting initialization of market org by {}. ", this.getOwner());
                final IConnectionGuidelines cg = auctionConnections.getListConnectionGuidelines().get(0);
                if (debug) LOG.info("Organization guidelines found at {}:  ", cg.getSpecificationFilePath());
            }
        }
        // auctioners have auction guidelines and broker connections (but brokers won't)
        if (IAuctionGuidelines.extractAuctionGuidelines(params) != null) {
            this.setAuctionGuidelines(IAuctionGuidelines.extractAuctionGuidelines(params));
            if (debug) LOG.info("auctionGuidelines = {}.", this.auctionGuidelines);
            if (this.auctionGuidelines == null) {
                if (debug) LOG.info("Auction guidelines are null. params={}", params);
            }
            final IConnections bc = (IConnections) params.getParameters().get(MarketGoalParameters.brokerConnections);
            if (debug) LOG.info("Broker connections ={}. params={}", bc, params);
            this.setBrokerConnections(bc);
            if (this.brokerConnections == null) {
                if (debug) LOG.info("Broker connections are null. params={}", params);
            }
        }
        if (debug) LOG.debug("auctionConnections={}", this.auctionConnections);
        if (debug) LOG.debug("brokerGuidelines={}", this.brokerGuidelines);

        if (debug) LOG.debug("auctionGuidelines={}", this.auctionGuidelines);
        if (debug) LOG.debug("brokerConnections={}", this.brokerConnections);

        if (this.owner.getUniqueIdentifier().toString().startsWith("H") && this.auctionGuidelines == null) {
            System.exit(-93);
        }
    }

    /**
     * Get the parameters from this instance goal and use them to set the goal-specific guidelines for any parent
     * connections.
     *
     * @param instanceGoal - this instance of the specification goal
     */
    @Override
    public synchronized void initializeParentConnections(InstanceGoal<?> instanceGoal) {
        final String paramType = BROKER_CONNECTIONS;
        if (debug) LOG.debug("Initializing {} from goal: {}.", instanceGoal, paramType);

        // Get the parameter values from the existing active instance goal
        final InstanceParameters params = Objects.
                requireNonNull((InstanceParameters) instanceGoal
                        .getParameter());
        if (debug) LOG.debug("Initializing {} from params: {}.", paramType, params);

        this.setBrokerConnections((IConnections) params.getValue(StringIdentifier.getIdentifier(paramType)));
        if (debug)
            LOG.debug("There are {} authorized {}.", this.brokerConnections.getListConnectionGuidelines().size(), paramType);
    }

    @Override
    public synchronized int messages() {
        return localMessages.size();
    }

    @Override
    public synchronized boolean send(IAuctionMessage message) {
        return sendLocal(message);
    }

    public synchronized boolean sendRemoteAuctionMessageToSuperSelf(IAuctionMessage selfLocalMessage) {
        if (selfLocalMessage == null) return false;
        if (debug) LOG.debug("The local auction message to send is: {} ", selfLocalMessage);

        //  local message becomes the entire message contents for the remote message
        String sender = selfLocalMessage.getLocalSender().toString();
        String rec = selfLocalMessage.getLocalReceiver().toString();

        IAuctionMessage remoteMessage = AuctionMessage.createRemote(sender, rec, selfLocalMessage.getPerformativeType(), selfLocalMessage.getContent());
        LOG.info("The REMOTE MARKET MESSAGE for broker's self persona is {}", remoteMessage.toString());
        sendRemoteMessage(remoteMessage, remoteMessage.getRemoteSender() + "-" + remoteMessage.getRemoteReceiver());
        return true;
    }

    public synchronized boolean sendRemoteMessage(IAuctionMessage message) {
        Objects.requireNonNull(message, "ERROR: Can't send a null remote message.");
        String queueLink = message.getRemoteSender() + "-" + message.getRemoteReceiver();
        String fullQueueName = MarketMessagingManager.getFullQueueName(queueLink, MarketMessagingManager.getQueueFocus(messagingFocus));
        LOG.info("Forwarding REMOTE MESSAGE on {}. {}", fullQueueName, message);
        try {
            setConsumer(queueLink);
        } catch (IOException e) {
            e.printStackTrace();
        }
        publish(fullQueueName, serializeMessage(message));
        LOG.info("Sent REMOTE MESSAGE on {}. {}. ", fullQueueName, message.toString());
        return true;
    }

    @Override
    public synchronized void reset() {
    }

    @Override
    public synchronized void channelContent(final Object content) {
        localMessages.add((IAuctionMessage) content);
    }

    @Override
    public synchronized double getFailure() {
        return AbstractOrganizationCapability.MIN_FAILURE;
    }

   @Override
    public synchronized IAuctionMessage receive() {
        return localMessages.poll();
    }

    private IAuctionMessage checkForLocalAuctionMessageFromSelf() {
        int numMessages = this.getLocalMessagesAsList().size();
        if (debug && numMessages > 0) LOG.info("There are {} messages to check.", numMessages);
        for (IAuctionMessage checkMessage : this.getLocalMessagesAsList()) {
            String sender = checkMessage.getLocalSender().toString(); // e.g. selfH44
            String senderNoSelf = checkMessage.getLocalSender().toString().replace("self", "");  // e.g. H44
            String me = getOwner().getUniqueIdentifier().toString(); // eg. H44inN43
            String meNoInOrg = me.substring(0, me.indexOf("in"));

            if (debug)
                LOG.debug("  CHECKING: checkForLocalAuctionMessageFromSelf sender= {} senderNoSelf={} me={} meNoInOrg={}. {}", sender, senderNoSelf, me, meNoInOrg, checkMessage);
            if (sender.contains("self") && senderNoSelf.equals(meNoInOrg)) {
                LOG.info("MARKET MESSAGE forwarded to sub RECEIVED: {}.", checkMessage.toString());
                // this.getLocalMessagesAsList().remove(checkMessage);
                return checkMessage;
            }
        }
        return null;
    }

    private List<IAuctionMessage> getLocalMessagesAsList() {
        return new ArrayList<>(localMessages);
    }

    private IAuctionMessage checkForLocalAuctionMessageFromWorker() {
        int numMessagesToCheck = this.getLocalMessagesAsList().size();
        if (debug && numMessagesToCheck > 0)
            LOG.debug("There are {} local messages to check (looking for sensor worker messages).", numMessagesToCheck);
        for (IAuctionMessage checkMessage : this.getLocalMessagesAsList()) {
            String rec = checkMessage.getLocalReceiver().toString();
            String me = getOwner().getUniqueIdentifier().toString();
            if (debug)
                LOG.debug("  CHECKING: checkForLocalAuctionMessageFromWorker rec= {} me={}. {}", rec, me, checkMessage);

            if (rec.contains("self") && rec.equals(me)) {
                LOG.info("Local MARKET MESSAGE message to self RECEIVED from sensor: {}.", checkMessage.toString());
                // this.getLocalMessagesAsList().remove(checkMessage);
                return checkMessage;
            }
        }
        return null;
    }


    private IAuctionMessage createLocalAuctionMessageForSub(UniqueIdentifier me, UniqueIdentifier subPersona, AuctionPerformative perf, IAuctionMessageContent rptMessageContent) {
        IAuctionMessage localAuctionMessage = AuctionMessage.createLocal(me, subPersona, perf, rptMessageContent);
        if (debug) LOG.debug("Auction message for sub created: {}.", localAuctionMessage);
        return localAuctionMessage;
    }

    private boolean forwardAuctionMessageToSub(IAuctionMessage origMessage, UniqueIdentifier subIdentifier) {
        IAuctionMessage localAuctionMessage = AuctionMessage.createLocal(this.getOwner().getUniqueIdentifier(), subIdentifier, origMessage.getPerformativeType(), origMessage.getContent());
        if (debug) LOG.debug("New auction message for sub is {}", localAuctionMessage);
        return sendLocal(localAuctionMessage);
    }

    public synchronized boolean sendLocal(IAuctionMessage message) {
        if (debug) LOG.info(" sending local auction messages {} kW from {} to {}", message
                .toString(), message.getLocalSender().toString(), message.getLocalReceiver().toString());

        // note: using the sendLocal method in IInternalCommunicationCapability
        boolean success = getOwner().getCapability(IInternalCommunicationCapability.class).sendLocal(
                message.getLocalReceiver(), this.getCommunicationChannelID(), message);
        if (!success) {
            if (debug) LOG.debug(" sending local auction message failed ");
        }
        return success;
    }

    @Override
    public String getCommunicationChannelID() {
        return COMMUNICATION_CHANNEL_ID;
    }

    private void forwardLocalMessageIntoOrg(IAuctionMessage localAuctionMessage, IConnections superHolonList) {
        if (debug) LOG.info("sendUp: received local message was: {}", localAuctionMessage);
        sendRemoteAuctionMessageToSuperList(localAuctionMessage, superHolonList);
    }

    private synchronized boolean sendRemoteAuctionMessageToSuperList(IAuctionMessage localAuctionMessageFromSelf, IConnections brokerConnections) {
        if (localAuctionMessageFromSelf == null) return false;
        if (debug)
            LOG.debug("sendRemoteAuctionMessageToSuperList: {} {}", localAuctionMessageFromSelf, brokerConnections.getListConnectionGuidelines().get(0));

        // change receiver to broker and send over external messaging system.
        for (IConnectionGuidelines cg : this.brokerConnections.getListConnectionGuidelines()) {
            if (debug) LOG.debug("Guidelines include broker {}.", cg.getOtherAgentAbbrev());

            UniqueIdentifier newSender = localAuctionMessageFromSelf.getLocalReceiver();
            UniqueIdentifier newReceiver = StringIdentifier.getIdentifier(cg.getExpectedMasterAbbrev());
            IAuctionMessage newLocalMessage = AuctionMessage.createLocal(newSender, newReceiver, localAuctionMessageFromSelf.getPerformativeType(), localAuctionMessageFromSelf.getContent());

            String sender = newSender.toString();
            String rec = newReceiver.toString();

            // new local message becomes the entire message contents for the remote message
            IAuctionMessage remoteMessage = AuctionMessage.createRemote(sender, rec, newLocalMessage.getPerformativeType(), newLocalMessage.getContent());
            LOG.info("Created REMOTE MARKET MESSAGE for external organization broker: {}", remoteMessage.toString());
            Objects.requireNonNull(remoteMessage, "ERROR: Can't send a null remote auction message.");
            Objects.requireNonNull(channel, "ERROR: null channel in send().");
            sendRemoteMessage(remoteMessage, remoteMessage.getRemoteSender() + "-" + remoteMessage.getRemoteReceiver());
        }
        return true;
    }

    public synchronized boolean sendRemoteMessage(IAuctionMessage message, String queueLink) {
        Objects.requireNonNull(message, "ERROR: Can't send a null remote message.");
        String fullQueueName = MarketMessagingManager.getFullQueueName(queueLink, MarketMessagingManager.getQueueFocus(messagingFocus));
        LOG.info("Forwarding REMOTE MARKET MESSAGE on {}. {}", fullQueueName, message);
        MarketMessagingManager.declareAndBindConsumerQueue(messagingFocus, queueLink);
        publish(fullQueueName, serializeMessage(message));
        LOG.info("Sent REMOTE MARKET MESSAGE on {}. {}. ", fullQueueName, message.toString());
        return true;
    }

    public synchronized boolean publish(String fullQueueName, byte[] messageBodyBytes) {
        if (debug) LOG.debug("SENDING serialized MESSAGE to {}. bytes={}", fullQueueName, messageBodyBytes.length);
        Objects.requireNonNull(channel, "ERROR: null channel in send().");
        try {
            channel.basicPublish(MarketMessagingManager.getExchangeName(messagingFocus), fullQueueName, MessageProperties.PERSISTENT_TEXT_PLAIN, messageBodyBytes);
            LOG.info("SENT serialized MESSAGE to {}. bytes={}", fullQueueName, messageBodyBytes.length);
            return true;
        } catch (Exception e) {
            LOG.error("Error sending serialized message on {}", fullQueueName);
            System.exit(-56);
        }
        return false;
    }

    private synchronized byte[] serializeMessage(final IAuctionMessage message) {
        byte[] messageBodyBytes = null;
        try {
            messageBodyBytes = message.serialize();
        } catch (IOException e) {
            LOG.error("Error serializing message {}", message.toString());
            System.exit(-56);
        }
        return messageBodyBytes;
    }

    private TreeMap<String, IAuctionMessageContent> getMostRecentReadings() {
        if (debug) LOG.debug("Beginning getMostRecentReadings");
        if (debug) LOG.debug("Starting with currentReads ={}", currentReads);
        int subCount = 0;
        int checkCount = 1;
        if (debug) LOG.debug("Starting with subCount={}, checkCount={}", subCount, checkCount);
        if (debug) LOG.debug("Owner = {}", this.owner);
        final String myPersona = this.owner.getUniqueIdentifier().toString();
        if (debug) LOG.debug("Owner name ={}", myPersona);
        if (this.auctionConnections == null) {
            LOG.error("ERROR: there are no auction connections. ");
            System.exit(-56);
        }
        final List<? extends IConnectionGuidelines> list = this.auctionConnections.getListConnectionGuidelines();
        if (list == null) {
            LOG.error("ERROR: there are no auction connection guidelines. ");
            System.exit(-57);
        }
        int numSubs = list.size();
        if (debug) LOG.debug("{} auction connections ={}", list.size(), list);
        String other;
        for (IConnectionGuidelines cg : list) {
            final String link = cg.getOtherAgentAbbrev() + "-" + myPersona;
            if (debug) LOG.debug("Check for message {} of {} via {}. ", checkCount, numSubs, link);
            IAuctionMessage foundMessage = null;
            try {
                foundMessage = receiveRemoteAuctionMessage(link);
            } catch (IOException | InterruptedException e) {
                LOG.error("Error receiving remote auction message on link {}", link);
                e.printStackTrace();
            }
            if (foundMessage != null) {
                other = foundMessage.getRemoteSender();
                if (debug)
                    LOG.debug("Broker {} picked up a auction message from {} via {}. {}", myPersona, other, link, foundMessage.toString());
                subCount++;
                if (debug) LOG.info("{} participant report(s) received.", subCount);
                IAuctionMessageContent newContent = (IAuctionMessageContent) foundMessage.getContent();
                LOG.info("Broker {} received auction message from {}. {}", myPersona, other, newContent.toString());
                currentReads.put(other, newContent);
                if (debug) LOG.info("Current reads has {} entries.", subCount);
            }
            checkCount++;
        }
        int subsReported = subCount;
        if (subsReported > 0)
            if (debug)
                LOG.info("Broker {} received {} auction messages. {}", myPersona, subsReported, currentReads.toString());
        return currentReads;
    }

    /**
     * @param link - the queue link, e.g. H44AinN43A-N43AinN43A receiving the message
     * @return String messages - Grabs messages from Queue
     * @throws IOException                                    - Handles all IO Exceptions
     * @throws ShutdownSignalException                        - Handles any Shutdown Signal Exceptions
     * @throws com.rabbitmq.client.ConsumerCancelledException - Handles any ConsumerCanceled Exceptions through RabbitMQ
     * @throws InterruptedException                           - Handles all interrupts
     */
    public synchronized IAuctionMessage receiveRemoteAuctionMessage(String link) throws IOException, ShutdownSignalException, ConsumerCancelledException, InterruptedException {
        String fullQueueName = MarketMessagingManager.getFullQueueName(link, MarketMessagingManager.getQueueFocus(messagingFocus));
        if (debug) LOG.debug("fullQueueName =  {}", fullQueueName);
        setConsumer(link);
        // check for delivery for given milliseconds
        final QueueingConsumer.Delivery delivery = consumer.nextDelivery(RunManager.getDeliveryCheckTime_ms());
        if (delivery == null) {
            if (debug) LOG.debug("got nothing on {}", fullQueueName);
            return null;
        }
        if (debug) LOG.debug("got something on {}", fullQueueName);
        AuctionMessage message = AuctionMessage.createAuctionMessage();
        if (debug) LOG.debug("Attempting to deserialize. Set message to {}. ", message);
        try {
            message = (AuctionMessage) message.deserialize(delivery.getBody());
            LOG.info("Received MARKET MESSAGE on {}. {}.", fullQueueName, message);
        } catch (Exception ex) {
            LOG.error("ERROR deserializing auction message. {}", ex.toString());
            System.exit(-42);
        }
        return message;
    }

    private void setConsumer(String queueLink) throws IOException {
        MarketMessagingManager.declareAndBindConsumerQueue(messagingFocus, queueLink);
        if (debug) LOG.debug("declareAndBindConsumerQueue {}", queueLink);
        String fullQueueName = MarketMessagingManager.getFullQueueName(queueLink, MarketMessagingManager.getQueueFocus(messagingFocus));
        String basicConsume = channel.basicConsume(fullQueueName, true, consumer);
        if (debug) LOG.debug("basicConsume {}", basicConsume);
    }

    public synchronized IAuctionMessage remoteRECEIVE(final String queueLink) throws IOException, ShutdownSignalException, InterruptedException {
        final String fullQueueName = MarketMessagingManager.getFullQueueName(queueLink, MarketMessagingManager.getQueueFocus(messagingFocus));
        if (debug) LOG.debug("Checking for message on {}.", fullQueueName);
        setConsumer(queueLink);
        // check for delivery for given milliseconds
        final QueueingConsumer.Delivery delivery = consumer.nextDelivery(RunManager.getDeliveryCheckTime_ms());
        if (delivery != null) {
            if (debug) LOG.debug("got something on {}", fullQueueName);
            try {
                IAuctionMessage received = (IAuctionMessage) AuctionMessage.createAuctionMessage().deserialize(delivery.getBody());
                if (debug) LOG.debug("Deserialized remote message on {}. {}.", fullQueueName, received);
                return received;
            } catch (Exception ex) {
                LOG.error("ERROR deserializing auction message: {}", ex.getCause().toString());
                System.exit(-11);
            }
        }
        if (debug) LOG.debug("got nothing on {}", fullQueueName);
        return null;
    }


}
