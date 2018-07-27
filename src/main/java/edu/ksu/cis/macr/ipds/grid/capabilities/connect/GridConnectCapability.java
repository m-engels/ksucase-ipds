package edu.ksu.cis.macr.ipds.grid.capabilities.connect;

import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;
import edu.ksu.cis.macr.aasis.agent.cc_message.connect.ConnectMessage;
import edu.ksu.cis.macr.aasis.agent.cc_message.connect.IConnectMessage;
import edu.ksu.cis.macr.aasis.agent.cc_p.ConnectionModel;
import edu.ksu.cis.macr.aasis.agent.persona.HierarchicalConnectCapability;
import edu.ksu.cis.macr.aasis.agent.persona.IOrganization;
import edu.ksu.cis.macr.aasis.agent.persona.IPersona;
import edu.ksu.cis.macr.aasis.common.Connections;
import edu.ksu.cis.macr.aasis.common.IConnectionGuidelines;
import edu.ksu.cis.macr.aasis.common.IConnections;
import edu.ksu.cis.macr.aasis.messaging.IMessagingFocus;
import edu.ksu.cis.macr.goal.model.InstanceParameters;
import edu.ksu.cis.macr.ipds.config.RunManager;
import edu.ksu.cis.macr.ipds.grid.goals.GridGoalEvents;
import edu.ksu.cis.macr.ipds.grid.goals.GridGoalParameters;
import edu.ksu.cis.macr.ipds.grid.messaging.GridMessagingFocus;
import edu.ksu.cis.macr.ipds.grid.messaging.GridMessagingManager;
import edu.ksu.cis.macr.obaa_pp.events.IOrganizationEvent;
import edu.ksu.cis.macr.obaa_pp.events.OrganizationEvent;
import edu.ksu.cis.macr.obaa_pp.events.OrganizationEventType;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import edu.ksu.cis.macr.organization.model.identifiers.StringIdentifier;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * The {@code GridConnectCapability} implements communication capabilities needed to establish initial connections. To monitor
 * RabbitMQ, point a browser to (the final slash is required): http://localhost:15672/ and login with: guest / guest
 */
public class GridConnectCapability extends HierarchicalConnectCapability implements IGridConnectCapability {
    private static final Logger LOG = LoggerFactory.getLogger(GridConnectCapability.class);
    private static final boolean debug = false;
    private static IMessagingFocus messagingFocus;

    /**
     * @param owner        - the entity to which this capability belongs.
     * @param organization - the {@code Organization} in which this {@code IAgent} acts.
     */
    public GridConnectCapability(final IPersona owner, final IOrganization organization) {
        super(IGridConnectCapability.class, owner, organization);
        COMMUNICATION_CHANNEL_ID = "GridConnectCommunicationChannel";
        messagingFocus = GridMessagingFocus.GRID_PARTICIPATE;
        LOG.debug("Before getting channel from Messaging Manager, channel = {}", channel);
        channel = GridMessagingManager.getChannel(messagingFocus);
        LOG.debug("After getting channel from Messaging Manager, channel = {}", channel);
    }

    /**
     * Constructs a new instance of {@code ConnectCapability}.
     *
     * @param owner        - the entity to which this capability belongs.
     * @param organization - the {@code IAgentInternalOrganization} in which this {@code IAgent} acts.
     * @param connections  - the guidelines for all authorized grid connections.
     */
    public GridConnectCapability(final IPersona owner, final IOrganization organization, IConnections connections) {
        super(IGridConnectCapability.class, owner, organization, connections);
        COMMUNICATION_CHANNEL_ID = "GridConnectCommunicationChannel";
        messagingFocus = GridMessagingFocus.GRID_PARTICIPATE;
        LOG.debug("Before getting channel from Messaging Manager, channel = {}", channel);
        channel = GridMessagingManager.getChannel(messagingFocus);
        LOG.debug("After getting channel from Messaging Manager, channel = {}", channel);
        this.connections = connections;
    }

    @Override
    public void sendREMOTE(final IConnectMessage message) {
        LOG.debug("sendREMOTE. message={}", message);
        final String queueLink = buildQueueLinkFromSenderAndReceiver(message.getRemoteSender(), message.getRemoteReceiver());
        final String fullQueueName = GridMessagingManager.getFullQueueName(queueLink, GridMessagingManager.getQueueFocus(messagingFocus));
        GridMessagingManager.declareAndBindConsumerQueue(messagingFocus, queueLink);
        LOG.debug("SENDING MESSAGE TO {}. {}", fullQueueName, message.toString());
        try {
            byte[] messageBodyBytes = message.serialize();
            if (debug)
                LOG.debug("Serialized HELLO TO routingKey: {} Size: ({} bytes) ", fullQueueName, messageBodyBytes.length);
            channel.basicPublish(GridMessagingManager.getExchangeName(messagingFocus), fullQueueName, MessageProperties.PERSISTENT_TEXT_PLAIN, messageBodyBytes);
            LOG.info("SENT HELLO TO {}: {}", fullQueueName, message.toString());
        } catch (Exception e) {
            LOG.error("ERROR send() messages {} from {}. ", message.toString(), message.getRemoteSender());
            System.exit(-56);
        }
    }

    public static String getCommunicationChannelID() {
        return GridConnectCapability.COMMUNICATION_CHANNEL_ID;
    }

    @Override
    public String toString() {
        return "ConnectCapability{" +
                "connections=" + getAllConnections() +
                ", parentConnections=" + getParentConnections() +
                ", childConnections=" + getChildConnections() +
                ", ec=" + ec +
                '}';
    }

    /**
     * Get all parameters from this instance goal and use them to initialize the capability.
     *
     * @param instanceGoal - this instance of the specification goal
     */
    @Override
    public void init(InstanceGoal<?> instanceGoal) {
        LOG.info("Entering init(instanceGoal={}.", instanceGoal);
        // Get the parameter values from the existing active instance goal
        final InstanceParameters params = Objects
                .requireNonNull((InstanceParameters) instanceGoal
                        .getParameter());
        if (debug) LOG.debug("Initializing params: {}.", params);

        final IConnections gridConnections = (IConnections) params
                .getValue(StringIdentifier
                        .getIdentifier("gridConnections"));
        if (debug) LOG.debug("Initializing all grid connections: {}.", gridConnections);
        this.setAllConnections(gridConnections);
        if (this.connections == null) {
            IConnections childConnections = (IConnections) params.getValue(GridGoalParameters.childConnections);
            if (debug) LOG.debug("Holon connections: {}.", childConnections);
            IConnections parentConnections = (IConnections) params.getValue(GridGoalParameters.parentConnections);
            if (debug) LOG.debug("Super-holon connections: {}.", parentConnections);
            this.setAllChildConnectionGuidelines(childConnections);
            this.setParentConnections(parentConnections);
        } else {
            if (debug) LOG.debug("Connections to other agents: {}", gridConnections.getListConnectionGuidelines());
            IConnections childConnections = new Connections(this.getAllChildConnections(gridConnections.getListConnectionGuidelines()), GridGoalParameters.childConnections.toString());
            if (debug) LOG.debug("Child connections: {}.", childConnections);
            IConnections parentConnections = new Connections(this.getAllParentConnections(gridConnections.getListConnectionGuidelines()), GridGoalParameters.parentConnections.toString());
            if (debug) LOG.debug("Parent connections: {}.", parentConnections);
            this.setAllChildConnectionGuidelines(childConnections);
            this.setParentConnections(parentConnections);
        }
        if (this.childConnections != null) {
            LOG.debug("{} authorized connections to holons.", childConnections.getListConnectionGuidelines().size());
        }
        if (this.parentConnections != null) {
            LOG.debug("There are {} authorized connections to super holons.", parentConnections.getListConnectionGuidelines().size());
        }
    }

    @Override
    public boolean registerWithExchange() {
        RunManager.registered(ec.getIdentifierString(), messagingFocus);
        return true;
    }


    /**
     * Trigger an associated "be sub holon" goal.
     *
     * @param instanceGoal - the instance goal that is triggering the new goal.
     */
    @Override
    public void triggerChildGoal(final InstanceGoal<?> instanceGoal) {
        final List<? extends IConnectionGuidelines> lstAll = this.connections.getListConnectionGuidelines();
        if (debug) LOG.debug("All connections are: {}", lstAll);

        // get the parent connections from the list of all connections
        final List<? extends IConnectionGuidelines> lstParentConnections = getAllParentConnections(lstAll);
        LOG.info("!!Super holon connections are: {}", lstParentConnections);
        if (lstParentConnections == null || lstParentConnections.isEmpty()) return;

        // create new guidelines
        final IConnections parentConnections = Connections.createConnections(lstParentConnections, "gridConnections");

        // set the guidelines from the triggering goal
        HashMap<UniqueIdentifier, Object> map = new HashMap<>();
        map.put(GridGoalParameters.parentConnections, parentConnections);
        final InstanceParameters instanceParams = new InstanceParameters(map);

        // create an organization event
        final IOrganizationEvent event = new OrganizationEvent(
                OrganizationEventType.EVENT, GridGoalEvents.beginSub, instanceGoal, instanceParams);
        LOG.info("!!Created new organization GOAL_MODEL_EVENT (to beginSub): {}", event.toString());

        // add the event to an organization events list
        List<IOrganizationEvent> lstEvents = new ArrayList<>();
        LOG.info("!!Adding event={}", event.toString());
        lstEvents.add(event);

        // add the event list to the control component's event list
        this.owner.getOrganizationEvents().addEventListToQueue(lstEvents);
    }

    /**
     * Trigger an associated "be super holon" goal.
     *
     * @param instanceGoal - the instance goal that is triggering the new goal.
     */
    @Override
    public synchronized void triggerParentGoal(final InstanceGoal<?> instanceGoal) {
        final List<? extends IConnectionGuidelines> lstAll = this.connections.getListConnectionGuidelines();
        if (debug) LOG.debug("All connections are: {}", lstAll);

        // get the child connections from the list of all connections
        List<? extends IConnectionGuidelines> lstChildConnections = getAllChildConnections(lstAll);
        if (debug) LOG.debug("Sub holon connections are: {}", lstChildConnections);

        if (lstChildConnections == null || lstChildConnections.isEmpty()) return;
        final IConnections childConnections = Connections.createConnections(lstChildConnections, "gridConnections");

        // set the guidelines from the triggering goal
        HashMap<UniqueIdentifier, Object> map = new HashMap<>();
        map.put(GridGoalParameters.childConnections, childConnections);
        final InstanceParameters instanceParams = new InstanceParameters(map);

        // create an organization event
        final IOrganizationEvent organizationEvent = new OrganizationEvent(
                OrganizationEventType.EVENT, GridGoalEvents.beginSuper,
                instanceGoal, instanceParams);
        LOG.info("!!Created new organization GOAL_MODEL_EVENT (to beginSuper): {}", organizationEvent.toString());

        // add the event to an organization events list
        ArrayList<IOrganizationEvent> organizationEvents = new ArrayList<>();
        organizationEvents.add(organizationEvent);

        // add the event list to the control component's event list
        this.owner.getOrganizationEvents().addEventListToQueue(organizationEvents);
    }

    /**
     * @param queueLink - prefix for the messaging queue
     * @return String messages - Grabs messages from Queue
     * @throws IOException             - Handles any IO Exceptions
     * @throws ShutdownSignalException - Handles any ShutdownSignal Exceptions
     * @throws InterruptedException    - Handles any Interrupted Exceptions.
     */
    @Override
    public IConnectMessage remoteRECEIVE(final String queueLink) throws IOException, ShutdownSignalException, InterruptedException {
        if (debug) LOG.debug("Setting consumer with queueLink={}", queueLink);
        GridMessagingManager.declareAndBindConsumerQueue(messagingFocus, queueLink);
        if (debug) LOG.debug("declareAndBindConsumerQueue {}", queueLink);
        String fullQueueName = GridMessagingManager.getFullQueueName(queueLink, GridMessagingManager.getQueueFocus(messagingFocus));
        QueueingConsumer consumer = new QueueingConsumer(Objects.requireNonNull(channel, "Error null channel in receive()."));
        String basicConsume = channel.basicConsume(fullQueueName, true, consumer);
        if (debug) LOG.debug("basicConsume {}", basicConsume);

        // check for delivery for given milliseconds
        QueueingConsumer.Delivery delivery = consumer.nextDelivery(RunManager.getDeliveryCheckTime_ms());
        if (debug) LOG.debug("in remoteRECEIVE. delivery={}", delivery);
        if (delivery != null) {
            if (debug) LOG.debug("got something on {}", fullQueueName);
            try {
                IConnectMessage received = (IConnectMessage) ConnectMessage.createEmptyConnectMessage().deserialize(delivery.getBody());
                if (debug) LOG.debug("Deserialized remote GRID CONNECT message on {}. {}.", fullQueueName, received);
                if (!queueLink.contains(received.getRemoteSender())) {
                    LOG.error("ERROR: Got Message On Wrong Queue. Deserialized remote GRID CONNECT message on {}. {}.", fullQueueName, received);
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

    protected synchronized void updateConnectionList(final String sender, final String receiver) {
        if (debug) LOG.debug("Starting updateConnectionList() with sender ={} and receiver={}.", sender, receiver);
        // assume sender comes before receiver alphabetically
        String first = sender;
        String second = receiver;
        // if instead, receiver comes before sender alphabetically, adjust
        if (receiver.compareTo(sender) < 0) {
            first = receiver;
            second = sender;
        }
        TreeSet<String> conns = ConnectionModel.getConnectionSet();
        if (debug) LOG.debug("The connection set already had {} entries.", conns.size());
        //add it (only in the alphabetical order - don't duplicate the connection)
        final String connection = first + " - " + second + "\n";
        ConnectionModel.insertNewConnection(connection);

        final int numConnections = ConnectionModel.getConnectionSet().size();
        final int totalPossibleParentConnections = RunManager.getTotalConnectionCount();
        LOG.debug("{}", ConnectionModel.getSummaryString());
        RunManager.setInitiallyConnected(numConnections >= RunManager.INITIAL_CONNECTION_THRESHOLD_FRACTION * totalPossibleParentConnections);
    }

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
        IConnections parentConnections = (IConnections) params.getValue(GridGoalParameters.parentConnections);
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
                        .getValue(GridGoalParameters.childConnections));

        this.setAllChildConnectionGuidelines(childConnections);
        if (childConnections != null && childConnections.getListConnectionGuidelines() != null && childConnections.getListConnectionGuidelines().size() > 0) {
            LOG.info("{} child connections.", childConnections.getListConnectionGuidelines().size());
        }
    }

}
