package edu.ksu.cis.macr.ipds.market.capabilities.connect;

import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;
import edu.ksu.cis.macr.aasis.agent.cc_message.connect.ConnectMessage;
import edu.ksu.cis.macr.aasis.agent.cc_message.connect.IConnectMessage;
import edu.ksu.cis.macr.aasis.agent.persona.HierarchicalConnectCapability;
import edu.ksu.cis.macr.aasis.agent.persona.IOrganization;
import edu.ksu.cis.macr.aasis.agent.persona.IPersona;
import edu.ksu.cis.macr.aasis.common.Connections;
import edu.ksu.cis.macr.aasis.common.IConnectionGuidelines;
import edu.ksu.cis.macr.aasis.common.IConnections;
import edu.ksu.cis.macr.aasis.messaging.IMessagingFocus;
import edu.ksu.cis.macr.goal.model.InstanceParameters;
import edu.ksu.cis.macr.ipds.config.RunManager;
import edu.ksu.cis.macr.ipds.market.goals.MarketGoalEvents;
import edu.ksu.cis.macr.ipds.market.goals.MarketGoalParameters;
import edu.ksu.cis.macr.ipds.market.guidelines.auction.AuctionGuidelines;
import edu.ksu.cis.macr.ipds.market.guidelines.auction.IAuctionGuidelines;
import edu.ksu.cis.macr.ipds.market.guidelines.broker.BrokerGuidelines;
import edu.ksu.cis.macr.ipds.market.guidelines.broker.IBrokerGuidelines;
import edu.ksu.cis.macr.ipds.market.messaging.MarketMessagingFocus;
import edu.ksu.cis.macr.ipds.market.messaging.MarketMessagingManager;
import edu.ksu.cis.macr.obaa_pp.events.IOrganizationEvent;
import edu.ksu.cis.macr.obaa_pp.events.OrganizationEvent;
import edu.ksu.cis.macr.obaa_pp.events.OrganizationEventType;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import edu.ksu.cis.macr.organization.model.identifiers.StringIdentifier;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
/**
 * The {@code ConnectCapability} implements communication capabilities needed to establish initial connections. To monitor
 * RabbitMQ, point a browser to (the final slash is required): http://localhost:15672/ and login with: guest / guest
 */
public class MarketConnectCapability extends HierarchicalConnectCapability implements IMarketConnectCapability {
    private static final Logger LOG = LoggerFactory.getLogger(MarketConnectCapability.class);
    private static final boolean debug = false;
    private static IMessagingFocus messagingFocus ;

    /**
     * @param owner        - the entity to which this capability belongs.
     * @param organization - the {@code Organization} in which this {@code IAgent} acts.
     */
    public MarketConnectCapability(final IPersona owner, final IOrganization organization) {
        super(IMarketConnectCapability.class, owner, organization);
        COMMUNICATION_CHANNEL_ID = "MarketConnectCommunicationChannel";
        messagingFocus = MarketMessagingFocus.MARKET_PARTICIPATE;
        LOG.debug("Before getting channel from Messaging Manager, channel = {}", channel);
        channel = MarketMessagingManager.getChannel(messagingFocus);
        LOG.debug("After getting channel from Messaging Manager, channel = {}", channel);
     }

    /**
     * Constructs a new instance of {@code ConnectCapability}.
     *
     * @param owner        - the entity to which this capability belongs.
     * @param organization - the {@code IAgentInternalOrganization} in which this {@code IAgent} acts.
     * @param connections  - the guidelines for all authorized market connections.
     */
    public MarketConnectCapability(final IPersona owner, final IOrganization organization, IConnections connections) {
        super(IMarketConnectCapability.class, owner, organization);
        COMMUNICATION_CHANNEL_ID = "MarketConnectCommunicationChannel";
        messagingFocus = MarketMessagingFocus.MARKET_PARTICIPATE;
        LOG.debug("Before getting channel from Messaging Manager, channel = {}", channel);
        channel = MarketMessagingManager.getChannel(messagingFocus);
        LOG.debug("After getting channel from Messaging Manager, channel = {}", channel);
        this.connections = connections;
    }

    public static String getCommunicationChannelID() {
        return MarketConnectCapability.COMMUNICATION_CHANNEL_ID;
    }

    @Override
    public void sendREMOTE(final IConnectMessage message) {
        LOG.debug("Beginning sendREMOTE. messagingFocus={} message={}.", messagingFocus, message);
        final String queueLink = buildQueueLinkFromSenderAndReceiver(message.getRemoteSender(), message.getRemoteReceiver());
        LOG.debug("sendREMOTE queueLink ={}.", queueLink);
        String fullQueueName = MarketMessagingManager.getFullQueueName(queueLink, MarketMessagingManager.getQueueFocus(messagingFocus));
        final String routingKey = fullQueueName;
        LOG.debug("sendREMOTE fullQueueName = routingKey ={}.", fullQueueName);
        MarketMessagingManager.declareAndBindConsumerQueue(messagingFocus, queueLink);
        LOG.info("SENDING HELLO TO {}. {}", fullQueueName, message.toString());
        try {
            byte[] messageBodyBytes = message.serialize();
            if (debug)
                LOG.debug("Serialized HELLO TO routingKey: {} Size: ({} bytes) ", routingKey, messageBodyBytes.length);
            channel.basicPublish(MarketMessagingManager.getExchangeName(messagingFocus), routingKey, MessageProperties.PERSISTENT_TEXT_PLAIN, messageBodyBytes);
            if (debug) LOG.debug("SENT HELLO TO {}: {}", fullQueueName, message.toString());
        } catch (Exception e) {
            LOG.error("ERROR in sendREMOTE message {} from {}. ", message.toString(), message.getRemoteSender());
            System.exit(-56);
        }
    }



    @Override
    public String toString() {
        return "MarketConnectCapability{" +
                ", connections=" + connections +
                ", childConnections=" + childConnections +
                ", parentConnections=" + parentConnections +
                ", allConnected=" + allConnected +
                '}';
    }


    @Override
    public synchronized IConnectMessage remoteRECEIVE(final String queueLink) throws IOException, ShutdownSignalException, InterruptedException {
        if (debug) LOG.debug("Setting consumer with queueLink={}", queueLink);
        MarketMessagingManager.declareAndBindConsumerQueue(messagingFocus, queueLink);
        if (debug) LOG.debug("declareAndBindConsumerQueue {}", queueLink);
        String fullQueueName = MarketMessagingManager.getFullQueueName(queueLink, MarketMessagingManager.getQueueFocus(messagingFocus));
        QueueingConsumer consumer = new QueueingConsumer(Objects.requireNonNull(channel, "Error null channel in receive()."));
        String basicConsume = channel.basicConsume(fullQueueName, true, consumer);
        if (debug) LOG.debug("basicConsume {}", basicConsume);
        QueueingConsumer.Delivery delivery = consumer.nextDelivery(RunManager.getDeliveryCheckTime_ms());
        if (debug) LOG.debug("in remoteRECEIVE. delivery={}", delivery);
        if (delivery != null) {
            LOG.debug("got something on {}", fullQueueName);
            try {
                IConnectMessage received = (IConnectMessage) ConnectMessage.createEmptyConnectMessage().deserialize(delivery.getBody());
                LOG.debug("Deserialized remote MARKET CONNECT message on {}. {}.", fullQueueName, received);
                if (!queueLink.contains(received.getRemoteSender())){
                    LOG.error("ERROR: Got Message On Wrong Queue. Deserialized remote MARKET CONNECT message on {}. {}.", fullQueueName, received);
                    System.exit(-9);
                }
                return received;
            } catch (Exception ex) {
                LOG.error("ERROR deserializing connect message: {}", ex.getCause().toString());
                System.exit(-11);
            }
        }
        if (debug) LOG.debug("got nothing on {}", fullQueueName);
        return null;
    }
    @Override
    public boolean registerWithExchange() {
        RunManager.registered(ec.getIdentifierString(), messagingFocus);
        return true;
    }

    /**
     /**
     * Get the parameters from this instance goal and use them to set the goal-specific guidelines for any parent
     * connections.
     *
     * @param instanceGoal - this instance of the specification goal
     */
    public synchronized void initializeParentConnections(InstanceGoal<?> instanceGoal) {
        if (debug) LOG.debug("Initializing connections to  parents from goal: {}.", instanceGoal);

        // Get the parameter values from the existing active instance goal
        final InstanceParameters params = Objects.requireNonNull((InstanceParameters) instanceGoal.getParameter());
        if (debug) LOG.debug("Initializing broker connections from params: {}.", params);
        IConnections parentConnections = (IConnections) params.getValue(MarketGoalParameters.brokerConnections);
        this.setParentConnections(parentConnections);
        if (noParents()) {
            LOG.debug("{} child initialized without any parent.", this.getOwner().getUniqueIdentifier().toString(), instanceGoal.toString());
        }
        if (debug)
            LOG.debug("{} parent connections.", parentConnections.getListConnectionGuidelines().size());
    }


    @Override
    public synchronized void initializeChildConnections(InstanceGoal<?> instanceGoal) {
        if (debug) LOG.debug("Initializing connections to children from goal: {}.", instanceGoal);
        // Get the parameter values from the existing active instance goal
        final InstanceParameters params = Objects
                .requireNonNull((InstanceParameters) instanceGoal
                        .getParameter());
        if (debug) LOG.debug("Initializing child connections from params: {}.", params);

        final IConnections childConnections = Objects
                .requireNonNull((IConnections) params
                        .getValue(MarketGoalParameters.auctionConnections));

        this.setAllChildConnectionGuidelines(childConnections);
        if (childConnections != null && childConnections.getListConnectionGuidelines() != null && childConnections.getListConnectionGuidelines().size() > 0) {
            LOG.info("{} child connections.", childConnections.getListConnectionGuidelines().size());
        }
    }

    /**
     * Get all parameters from this instance goal and use them to initialize the capability.
     *
     * @param instanceGoal - this instance of the specification goal
     */
    @Override
    public void init(final InstanceGoal<?> instanceGoal) {
        LOG.info("Entering init(instanceGoal={}.", instanceGoal);
        // Get the parameter values from the existing active instance goal
        final InstanceParameters params = Objects.requireNonNull((InstanceParameters) instanceGoal.getParameter());
        if (debug) LOG.debug("Initializing params: {}.", params);

        this.childConnections = (IConnections) params.getValue(StringIdentifier.getIdentifier("auctionConnections"));
        if (debug) LOG.debug("Initializing auction connections: {}.", childConnections);

        if (childConnections == null) {
            if (debug) LOG.debug("There are no auction connections to other agents.");
        }
        if (childConnections != null && childConnections.getListConnectionGuidelines() != null
                && childConnections.getListConnectionGuidelines().size() > 0) {
            if (debug)
                LOG.info("{} possible auction connections.", childConnections.getListConnectionGuidelines().size());
        }
        this.parentConnections = (IConnections) params.getValue(StringIdentifier.getIdentifier("brokerConnections"));
        if (debug) LOG.debug("Initializing broker connections: {}.", parentConnections);

        if (parentConnections == null) {
            if (debug) LOG.debug("There are no broker connections to other agents.");
        }
        if (parentConnections != null && parentConnections.getListConnectionGuidelines() != null && parentConnections.getListConnectionGuidelines().size() > 0) {
            if (debug)
                LOG.debug("{} possible broker connections.", parentConnections.getListConnectionGuidelines().size());
        }
        List<IConnectionGuidelines> lst = new ArrayList<>();
        if (childConnections != null){
            for (IConnectionGuidelines cg : childConnections.getListConnectionGuidelines()){
                        lst.add(cg);
            }
           // lst.addAll(childConnections.getListConnectionGuidelines().stream().collect(Collectors.toList()));
        }
        if (parentConnections != null){
            for (IConnectionGuidelines cg : parentConnections.getListConnectionGuidelines()){
                lst.add(cg);
            }
            //  lst.addAll(parentConnections.getListConnectionGuidelines().stream().collect(Collectors.toList()));
        }

        if (debug) LOG.debug("{} authorized market connections.", lst.size());
        this.connections = Connections.createConnections(lst, "marketConnections");
        if (this.connections.getListConnectionGuidelines() != null) {
            if (debug)
                LOG.debug("{} possible market connections.", this.connections.getListConnectionGuidelines().size());
        }
    }




    @Override
    public boolean send(IConnectMessage message) {
        LOG.error("Too general - do not use. ");
        System.exit(-99);
        return false;
    }



    /**
     * Trigger the associated goal.
     *
     * @param ig - the instance goal that is triggering the new goal.
     */
    public synchronized void triggerChildGoal(final InstanceGoal<?> ig) {
        this.init(ig);
        LOG.debug("Broker connections are: {}", this.parentConnections);

        // get the parent connections from the list of all connections

        if (this.parentConnections == null) return;
        List<? extends IConnectionGuidelines> lst = this.parentConnections.getListConnectionGuidelines();
        LOG.info("Broker connection guidelines are: {}", lst);
        if (lst == null || lst.isEmpty()) return;
        // create new guidelines

        InstanceParameters params = (InstanceParameters) ig.getParameter();
        LOG.info("Instance goal parameters = {}", params);
        IAuctionGuidelines auctionGuidelines = AuctionGuidelines.extractAuctionGuidelines(params);
        LOG.info("Auction guidelines = {}", auctionGuidelines);

        if (auctionGuidelines == null) {
            LOG.error("WARNING: agent has broker connections, but no auction guidelines yet. ");
            return;
        }

        // set the guidelines from the triggering goal
        HashMap<UniqueIdentifier, Object> map = new HashMap<>();
        map.put(MarketGoalParameters.brokerConnections, this.parentConnections);
        LOG.info("Passing on goal guidelines ={}", parentConnections);

        map.put(MarketGoalParameters.auctionGuidelines, auctionGuidelines);
        LOG.info("Passing on goal guidelines ={}", auctionGuidelines);

        final InstanceParameters newParams = new InstanceParameters(map);
        LOG.info("New params ={}", newParams);


        // create an organization event
        IOrganizationEvent event = new OrganizationEvent(
                OrganizationEventType.EVENT, MarketGoalEvents.beginAuction, ig, newParams);
        LOG.info("Created new organization GOAL_MODEL_EVENT (to beginAuction): {}", event.toString());

        // add the event to an organization events list
        List<IOrganizationEvent> lstEvents = new ArrayList<>();
        LOG.info("Adding event={}", event.toString());
        lstEvents.add(event);

        // add the event list to the control component's event list
        this.owner.getOrganizationEvents().addEventListToQueue(lstEvents);
    }

    /**
     * Trigger the associated goal.
     *
     * @param instanceGoal - the instance goal that is triggering the new goal.
     */
    public synchronized void triggerParentGoal(final InstanceGoal<?> instanceGoal) {
        if (debug) LOG.debug("Auction connections are: {}", this.childConnections);

        // get the child connections from the list of all connections
        if (this.childConnections != null) {
            List<? extends IConnectionGuidelines> lstChildConnections = childConnections.getListConnectionGuidelines();
            if (debug) LOG.debug("participant connections are: {}", lstChildConnections);

            if (lstChildConnections == null || lstChildConnections.isEmpty()) return;
            IConnections childConnections = Connections.createConnections(lstChildConnections, MarketGoalParameters.auctionConnections.toString());

            IBrokerGuidelines brokerGuidelines = BrokerGuidelines.extractBrokerGuidelines((InstanceParameters) instanceGoal
                    .getParameter());

            if (brokerGuidelines == null) {
                LOG.error("WARNING: agent has auction connections, but no broker guidelines. ");
                return;
            }

            // set the guidelines from the triggering goal
            HashMap<UniqueIdentifier, Object> map = new HashMap<>();

            map.put(MarketGoalParameters.auctionConnections, this.childConnections);
            LOG.info("Passing on goal guidelines ={}", childConnections);

            map.put(MarketGoalParameters.brokerGuidelines, brokerGuidelines);
            LOG.info("Passing on goal guidelines ={}", brokerGuidelines);

            final InstanceParameters newParams = new InstanceParameters(map);

            // create an organization event
            IOrganizationEvent event = new OrganizationEvent(
                    OrganizationEventType.EVENT, MarketGoalEvents.beginBroker,
                    instanceGoal, newParams);
            LOG.info("Created new organization GOAL_MODEL_EVENT (to beginBroker): {}", event.toString());

            // add the event to an organization events list
            List<IOrganizationEvent> organizationEvents = new ArrayList<>();
            organizationEvents.add(event);

            // add the event list to the control component's event list
            this.owner.getOrganizationEvents().addEventListToQueue(organizationEvents);
        }
    }



    public synchronized boolean checkDownConnections() {
        if (debug) LOG.debug("Beginning attempts to connect to all participants.");
        int tot = 0;
        if (noChildren()) {
            this.allConnected = true;
        } else {
           tot = getChildConnections().getListConnectionGuidelines().size();
            if (debug)
                LOG.debug("Need {} of {} connections to fully connect to participants.", childConnectionsStillNeeded(), tot);
            if (childConnectionsStillNeeded() == 0) {
                this.allConnected = true;
            } else {
                for (IConnectionGuidelines cg : getChildConnections().getListConnectionGuidelines()) {
                    try {
                        final boolean isConnected = connectToChild(cg);
                        if (debug)
                            LOG.debug("Connection to participant {} success={}.", cg.getOtherAgentAbbrev(), isConnected);
                    } catch (Exception e) {
                        LOG.error("Error attempting to connect to participant: {}", cg);
                        System.exit(-59);
                    }
                }
                 resendToUnconnectedChildren();
                // check again and if no additional connections are still needed return true (completely connected)
                this.allConnected = (childConnectionsStillNeeded() == 0);
            }
        }

        LOG.info("Exiting checkDownConnections. ALL_CONNECTED={}, {} of {} connections to fully connect to brokers.", this.allConnected, this.childConnectionsStillNeeded(), tot);
        return this.allConnected;
    }

}
