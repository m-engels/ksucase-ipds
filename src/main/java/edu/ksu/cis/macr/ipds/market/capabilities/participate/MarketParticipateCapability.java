package edu.ksu.cis.macr.ipds.market.capabilities.participate;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import edu.ksu.cis.macr.aasis.agent.cc_message.BaseMessage;
import edu.ksu.cis.macr.aasis.agent.cc_message.IBaseMessage;
import edu.ksu.cis.macr.aasis.agent.cc_message.participate.ParticipateMessage;
import edu.ksu.cis.macr.aasis.agent.cc_message.participate.ParticipatePerformative;
import edu.ksu.cis.macr.aasis.agent.ec_cap.IOrganizationCommunicationCapability;
import edu.ksu.cis.macr.aasis.agent.persona.*;
import edu.ksu.cis.macr.aasis.common.IConnectionGuidelines;
import edu.ksu.cis.macr.aasis.common.IConnections;
import edu.ksu.cis.macr.aasis.messaging.IMessagingFocus;
import edu.ksu.cis.macr.aasis.messaging.MessagingManager;
import edu.ksu.cis.macr.goal.model.InstanceParameters;
import edu.ksu.cis.macr.ipds.config.RunManager;
import edu.ksu.cis.macr.ipds.market.goals.MarketGoalParameters;
import edu.ksu.cis.macr.ipds.market.guidelines.auction.IAuctionGuidelines;
import edu.ksu.cis.macr.ipds.market.messaging.MarketMessagingFocus;
import edu.ksu.cis.macr.ipds.market.messaging.MarketMessagingManager;
import edu.ksu.cis.macr.obaa_pp.cc_message.RegistrationContent;
import edu.ksu.cis.macr.organization.model.Capability;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import edu.ksu.cis.macr.organization.model.identifiers.StringIdentifier;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Market participation communication capability.
 */
public class MarketParticipateCapability extends AbstractOrganizationCapability implements IOrganizationCommunicationCapability, ICapability {
    private static final Logger LOG = LoggerFactory.getLogger(MarketParticipateCapability.class);
    private static final boolean debug = false;
    private static final int RETRY_DELAY = 10000 * 100;  // extended due to stepdelay - denise
    private static final int TIMEOUT = 20000 * 100;   // extended due to stepdelay - denise
    private static final IMessagingFocus messagingFocus = MarketMessagingFocus.MARKET_PARTICIPATE;
    /**
     * The {@code ICommunicationChannel} keyword for registering organization getNumberOfMessages.
     */
    private static final String COMMUNICATION_CHANNEL_ID = "Organization Messages";
    private static Channel channel;
    /**
     * A {@code Queue} that holds {@code Message} sent to this {@code IAbstractControlComponent}.
     */
    private final Queue<IBaseMessage<?>> organizationMessages;
    private final IPersona owner;

    private boolean isRegistered = false;
    private Long startTime = null;
    private String masterName = "";
    private IConnections parentConnections = null;
    /*
  The time an agent will wait to re-send registration when sending fails. Can be set by the user.
   */
    private static int retry_delay_ms;

    /*
    The time an agent will wait to get a confirmation before moving back to registration and re-starting the process. Can be set by the user.
     */
    private static int confirmation_timeout_ms;

    public MarketParticipateCapability(final IPersona owner, IOrganization organization) {
        super(MarketParticipateCapability.class, owner, organization);
        channel = MarketMessagingManager.getChannel(messagingFocus);
        this.owner = owner;
        this.organizationMessages = new ConcurrentLinkedQueue<>();
        retry_delay_ms = RunManager.getParticipantRetryDelay_ms();
        confirmation_timeout_ms = RunManager.getParticipantTimeout_ms();
    }

    @Override
    public Queue<IBaseMessage<?>> getOrganizationMessages() {
        return this.organizationMessages;
    }

    @Override
    public IPersona getOwner() {
        return owner;
    }

    public IConnections getParentConnections() {
        return parentConnections;
    }

    public void setParentConnections(IConnections parentConnections) {
        this.parentConnections = parentConnections;
    }


    @Override
    public String toString() {
        return "IOrganizationCommunicationCapability{" +
                "organizationMessages=" + organizationMessages +
                ", owner=" + owner +
                '}';
    }


    @Override
    public synchronized void reset() {
    }


    @Override
    public synchronized void channelContent(final Object content) {
        LOG.info("Entering channelContent(). Gets raw content and adds message. content={}", content);
        try {
            organizationMessages.add((IBaseMessage<?>) content);
        } catch (Exception e) {
            LOG.error("Cannot cast channelContent to Message<?>");
            System.exit(-73);
        }
    }

    @Override
    public int messages() {
        return organizationMessages.size();
    }

    @Override
    public IBaseMessage<?> receive() {
        return organizationMessages.poll();
    }

    @Override
    public boolean send(final IBaseMessage<?> message) {
        boolean success = false;
        boolean isLocal = message.isLocal();
        if (isLocal) {
            if (debug) LOG.debug("Attempting to send on channel {} to receiver {} local message {}",
                    getCommunicationChannelID(), message.getLocalReceiver(), message);
            success = getOwner().getCapability(IInternalCommunicationCapability.class).sendLocal(message.getLocalReceiver(),
                    getCommunicationChannelID(), message);
        } else {
            String routingKey = MarketMessagingManager.getFullQueueName(message.getRemoteReceiver(), MarketMessagingManager.getQueueFocus(messagingFocus));
            if (debug) LOG.debug("Attempting to send message to remote persona: {}", message);
            success = sendExternal(message);
        }
        if (!success) {
            LOG.error("ERROR attempting to send on channel {} to receiver {} local message {}",
                    getCommunicationChannelID(), message.getLocalReceiver(), message);
            System.exit(-2221);
        }
        return success;
    }


    @Override
    public double getFailure() {
        return 0;
    }


    /**
     * Broadcasts an {@code Message}.
     *
     * @param message the {@code Message} to be sent.
     * @return {@code true} if the {@code Message} was sent, {@code false} otherwise.
     * @see IInternalCommunicationCapability#broadcast(String, Object)
     */
    protected boolean broadcast(final BaseMessage<?> message) {
        Objects.requireNonNull(getOwner(),
                "execution component cannot be null");
        Objects.requireNonNull(
                getOwner().getCapability(IInternalCommunicationCapability.class),
                "capability cannot be null");
        Objects.requireNonNull(getCommunicationChannelID(),
                "communication channelID cannot be null");
        try {
            return getOwner().getCapability(IInternalCommunicationCapability.class)
                    .broadcast(getCommunicationChannelID(), message);
        } catch (Exception e) {
            LOG.error("ERROR broadcasting message {}: {}", message, e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public String getCommunicationChannelID() {
        return COMMUNICATION_CHANNEL_ID;
    }

    /**
     * Broadcasts an {@code Message}. The sender will also receive the {@code Message}.
     *
     * @param message the {@code Message} to be sent.
     * @return {@code true} if the {@code Message} was sent, {@code false} otherwise.
     * @see IInternalCommunicationCapability#broadcastIncludeSelf(String,
     * Object)
     */
    protected boolean broadcastIncludeSelf(
            final BaseMessage<?> message) {
        return getOwner().getCapability(IInternalCommunicationCapability.class)
                .broadcastIncludeSelf(getCommunicationChannelID(), message);
    }

    /**
     * Attempt to register with the organization master.  Organization master may be within my self organization, or
     * may be another local agent on this device, or may be a remote agent running on a different device.
     *
     * @param instanceGoal - the goal of the current instance
     * @return - true if successful, false if not.
     */
    public synchronized boolean doRegistration(InstanceGoal<?> instanceGoal) {
        init(instanceGoal);
        return doRegistration();
    }


    /**
     * Attempt to register with the organization master.  Organization master may be within my self organization, or
     * may be another local agent on this device, or may be a remote agent running on a different device.
     *
     * @return - true if successful, false if not.
     */
    public synchronized boolean doRegistration() {
        if (debug) LOG.debug("Beginning do Registration.");

        final Map<UniqueIdentifier, Double> capabilities = new HashMap<>();
        // verify that ecAgent entity is updated with the latest scores
        for (final Capability capability : owner.getCapabilities()) {
            Objects.requireNonNull(capability);
            final double score = owner.getCapabilityScore(capability);

            // Updated DMC - 10/6/2013 - to handle the case when the agent has a capability (score = 1) but the
            // capability is not needed (possesses = 0.0)
            final double agentPossessesScore = owner.getPossessesScore(capability.getIdentifier());
            if (score != agentPossessesScore && agentPossessesScore != 0.0 && score == 1.0) {
                owner.setPossessesScore(capability.getIdentifier(), score);
            }
            capabilities.put(capability.getIdentifier(), score);
        }
        if (debug) LOG.debug("{} doRegistration() {}", getClass().getName(), String.format("Agent: \"%s\": %s",
                owner.getIdentifierString(), owner.getCapabilitiesMapping().toString()));

        final RegistrationContent registrationContent = new RegistrationContent(this.owner.getUniqueIdentifier(), capabilities);
        if (debug) LOG.debug("registration content: {}", registrationContent.toString());

        String org = this.owner.getOrganizationFromIdentifier(this.owner.getUniqueIdentifier());
        String host = this.owner.getHostFromIdentifier(this.owner.getUniqueIdentifier());
        if (debug) LOG.debug("This CC Slave  is  {} in organization {} running on node {}",
                this.owner.getOrganization().getName(), org, host);

        this.startTime = System.currentTimeMillis();
        //  if (!org.equals(host)) { // then we must send to remote.  uses string names which is nice.

        if (debug)
            LOG.debug("Creating participate message. The remote org to join={}, this master={}, orgmaster={}", org, this.masterName, StringIdentifier.getIdentifier(org));
        final ParticipateMessage reasoningMessage = new ParticipateMessage(this.owner.getUniqueIdentifier(),
                StringIdentifier.getIdentifier(org + "in" + org), ParticipatePerformative.BROADCASTING_AGENT_REGISTRATION,
                registrationContent);
        if (debug) LOG.debug("Created registration message: {}", reasoningMessage.toString());
        this.isRegistered = true;
//        if (this.sendRemote(reasoningMessage)) {
//            LOG.debug("successful registration message sent (remote) {}", reasoningMessage.toString());
//            this.isRegistered = true;
//        } else {      /* send has failed, so retry again in a bit */
//            LOG.error("failed sending registration: {}", reasoningMessage.toString());
//            System.exit(-2222);
//        }
        return this.isRegistered;
    }

    public List<? extends IConnectionGuidelines> getUnregisteredParents() {
        List<IConnectionGuidelines> unregistered = new ArrayList<>();
        List<? extends IConnectionGuidelines> listConnectionGuidelines = parentConnections.getListConnectionGuidelines();
        for (IConnectionGuidelines cg : listConnectionGuidelines){
            if (!cg.isRegistered()){           unregistered.add(cg);        }
        }


       // unregistered.addAll(listConnectionGuidelines.stream().filter(IConnectionGuidelines::isRegistered).collect(Collectors.toList()));
        final int tot = parentConnections.getListConnectionGuidelines().size();
        int stillNeeded = unregistered.size();
        if (debug) LOG.debug("Need {} of {} registrations to fully connect to participants.", stillNeeded, tot);
        return unregistered;
    }

    /**
     * Get all parameters from this instance goal and use them to initialize the capability.
     *
     * @param instanceGoal - this instance of the specification goal
     */
    public synchronized void init(final InstanceGoal<?> instanceGoal) {
        if (debug) LOG.debug("Initializing capability from goal: {}.", instanceGoal);
        final InstanceParameters params = Objects.requireNonNull((InstanceParameters) instanceGoal.getParameter());
        if (debug) LOG.info("Initializing with the given goal parameter guidelines: {}.", params);
        if (params == null) {
            LOG.error("Error: we need goal parameters to guide the market participation. ");
            System.exit(-4);
        }
        // auctioners have auction guidelines and broker connections (but brokers won't)
        if (IAuctionGuidelines.extractAuctionGuidelines(params) != null) {
//            this.setAuctionGuidelines(IAuctionGuidelines.extractAuctionGuidelines(params));
//            LOG.info("e{}.", this.auctionGuidelines);
//            if (this.auctionGuidelines == null) {
//                LOG.info("Auction guidelines are null. params={}", params);
//            }
            this.parentConnections = (IConnections) params.getParameters().get(MarketGoalParameters.brokerConnections);
            LOG.info("Broker connections ={}. params={}", parentConnections, params);
            if (this.parentConnections == null) {
                LOG.info("Broker connections are null. params={}", params);
            }
        }


        //  extractParentConnections(params);


    }

    private void extractParentConnections(InstanceParameters params) {
        this.parentConnections = (IConnections) params.getValue(StringIdentifier.getIdentifier("brokerConnections"));
        if (debug) LOG.debug("Initializing broker connections: {}.", parentConnections);

        if (parentConnections == null) {
            if (debug) LOG.debug("There are no broker connections to other agents.");
        }
        if (parentConnections != null && parentConnections.getListConnectionGuidelines() != null && parentConnections.getListConnectionGuidelines().size() > 0) {
            LOG.info("{} broker connections.", parentConnections.getListConnectionGuidelines().size());
        }
    }

    public synchronized boolean isRegistered() {
        return this.isRegistered;
    }

    public IBaseMessage<?> receiveLocal() {
        if (debug) LOG.debug("Checking for internal organization messages");
        return organizationMessages.poll();
    }

    public boolean sendLocal(final BaseMessage<?> message) {
        if (debug) LOG.debug("Attempting to send local message {}", message);
        if (debug)
            LOG.debug("Attempting to broadcast local message on com channel (filter)={} with content={}. Message={}", getCommunicationChannelID(),
                    message.getContent(), message);
        boolean success = getOwner().getCapability(IInternalCommunicationCapability.class).broadcast(getCommunicationChannelID(), message);
        if (!success) {
            LOG.error("ERROR sending message (local): {}", message);
            System.exit(-2223);
        }
        return success;
    }

    public synchronized boolean sendRemote(final IBaseMessage<?> message) {
        LOG.debug("Attempting to send message to remote persona: {}", message);
        // boolean success = getAgent().getCapability(IInternalCommunicationCapability.class).sendExternal(message);
        boolean success = sendExternal(message);
        if (!success) {
            LOG.error("ERROR sending remote local={} message: {}", message.isLocal(), message);
            System.exit(-2222);
        }
        return success;
    }

    public synchronized boolean sendExternal(final IBaseMessage<?> message) {
        Objects.requireNonNull(message, "ERROR: Can't send a null reasoning messages.");
        LOG.debug("  entering send local messages externally from {} to {}", message.getLocalSender(), message.getLocalReceiver());
        String routingKey = message.getLocalReceiver().toString();  // e.g. N43inN43
        MessagingManager.declareAndBindConsumerQueue(messagingFocus, MarketMessagingManager.getFullQueueName(routingKey, MarketMessagingManager.getQueueFocus(messagingFocus)));
        try {
            byte[] messageBodyBytes = message.serialize();
            LOG.info("Sending message to routing key: {}", routingKey);
            channel.basicPublish(MarketMessagingManager.getExchangeName(messagingFocus), routingKey, MessageProperties.PERSISTENT_TEXT_PLAIN, messageBodyBytes);
            return true;
        } catch (Exception e) {
            LOG.error("ERROR send() messages {} from {}. ", message.toString(),
                    message.getRemoteSender());
            return false;
        }
    }
}
