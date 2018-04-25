package edu.ksu.cis.macr.ipds.market.capabilities.admin;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;
import edu.ksu.cis.macr.aasis.agent.cc_message.IBaseMessage;
import edu.ksu.cis.macr.aasis.agent.cc_message.participate.ParticipateMessage;
import edu.ksu.cis.macr.aasis.agent.cc_message.participate.ParticipatePerformative;
import edu.ksu.cis.macr.aasis.agent.ec_cap.IOrganizationCommunicationCapability;
import edu.ksu.cis.macr.aasis.agent.persona.AbstractOrganizationCapability;
import edu.ksu.cis.macr.aasis.agent.persona.IOrganization;
import edu.ksu.cis.macr.aasis.agent.persona.IPersona;
import edu.ksu.cis.macr.aasis.common.IConnectionGuidelines;
import edu.ksu.cis.macr.aasis.common.IConnections;
import edu.ksu.cis.macr.aasis.messaging.IMessagingFocus;
import edu.ksu.cis.macr.aasis.messaging.MessagingManager;
import edu.ksu.cis.macr.aasis.messaging.MessagingReliabilityManager;
import edu.ksu.cis.macr.aasis.simulator.player.PlayableCapability;
import edu.ksu.cis.macr.aasis.spec.OrganizationFocus;
import edu.ksu.cis.macr.goal.model.InstanceParameters;
import edu.ksu.cis.macr.goal.model.InstanceTreeChanges;
import edu.ksu.cis.macr.goal.model.InstanceTreeModifications;
import edu.ksu.cis.macr.goal.model.SpecificationEvent;
import edu.ksu.cis.macr.ipds.config.MarketHolonicLevel;
import edu.ksu.cis.macr.ipds.config.RunManager;
import edu.ksu.cis.macr.ipds.market.MarketOrganizationFactory;
import edu.ksu.cis.macr.ipds.market.capabilities.participate.AuctionPowerCapability;
import edu.ksu.cis.macr.ipds.market.guidelines.broker.IBrokerGuidelines;
import edu.ksu.cis.macr.ipds.market.messaging.MarketMessagingFocus;
import edu.ksu.cis.macr.ipds.market.messaging.MarketMessagingManager;
import edu.ksu.cis.macr.obaa_pp.cc.gr.IGoalModel;
import edu.ksu.cis.macr.obaa_pp.cc.om.IOrganizationModel;
import edu.ksu.cis.macr.obaa_pp.cc.reorg.IReorganizationAlgorithm;
import edu.ksu.cis.macr.obaa_pp.cc_message.*;
import edu.ksu.cis.macr.obaa_pp.events.IOrganizationEvent;
import edu.ksu.cis.macr.obaa_pp.events.OrganizationEvent;
import edu.ksu.cis.macr.obaa_pp.events.OrganizationEventType;
import edu.ksu.cis.macr.organization.model.*;
import edu.ksu.cis.macr.organization.model.identifiers.ClassIdentifier;
import edu.ksu.cis.macr.organization.model.identifiers.StringIdentifier;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
/**
 * The {@code MarketAdminCapability} provides the functionality necessary to create a new organization.
 */
public class MarketAdminCapability extends AbstractOrganizationCapability {
    private static final Logger LOG = LoggerFactory.getLogger(MarketAdminCapability.class);
    private static final boolean debug = false;
    private static final IMessagingFocus messagingFocus = MarketMessagingFocus.MARKET_PARTICIPATE;
    private static Channel channel;
    double communicationReliability = 1.0;
    double communicationDelay = 0.0;

    private ConcurrentHashMap<UniqueIdentifier, Agent<UniqueIdentifier>> registeredAgentQueue = new ConcurrentHashMap<>();
    private Set<Role> initialRoles;
    private IOrganization myorg;
    private boolean doneWaitingForInitialAssignments;
    private IOrganizationCommunicationCapability organizationCommunicationCapability;
    private PlayableCapability prrlayerCapability;
    private IConnections childConnections = null;
    private boolean allRegistered = false;
    private QueueingConsumer consumer;
    private IBrokerGuidelines brokerGuidelines;
    /**
     * Contains the {@code IGoalModel}.
     */
    private IGoalModel goalModel = null;
    /**
     * Contains the {@code IOrganizationModel}.
     */
    private IOrganizationModel organizationModel = null;
    /**
     * Contains the {@code IReorganizationAlgorithm}.
     */
    private IReorganizationAlgorithm reorganizationAlgorithm = null;
    private String orgModelFolder;


    /**
     * Construct a new {@code MARKETAdminCapability} instance.
     *
     * @param owner - the persona possessing this capability.
     * @param org   - the immediate organization in which this persona participates
     */
    public MarketAdminCapability(final IPersona owner, final IOrganization org) {
        super(MarketAdminCapability.class, owner, org);
        this.setOwner(Objects.requireNonNull(owner));
        channel = MarketMessagingManager.getChannel(messagingFocus);
        consumer = new QueueingConsumer(Objects.requireNonNull(channel, "Error null channel in receive()."));
        initializeReliabilityAndDelay();
        initialRoles = null;
    }

    private static File getOrganizationFolder(String strPath) {
        String curDir = System.getProperty("user.dir");
        File folder = null;
        try {
            folder = new File(curDir + strPath);
            if (debug) LOG.debug("The {} specification files are in: {}", folder.getName(),
                    curDir + strPath);
        } catch (Exception e) {
            LOG.error("Error: can't read org folder: {}", curDir + strPath);
        }
        if (folder == null) {
            LOG.error("Error: can't read org folder: {}", curDir + strPath);
            System.exit(1);
        }
        return folder;
    }

    private void initializeReliabilityAndDelay() {
        try {
            this.communicationReliability = MessagingReliabilityManager.getCommunicationReliability();
            if (debug) LOG.debug("\t New  communicationReliability.");
            this.communicationDelay = MessagingReliabilityManager.getCommunicationDelay();
        } catch (Exception e) {
            // just use the defaults
            this.communicationDelay = 0.0;
            this.communicationReliability = 1.0;
        }
        if (debug)
            LOG.debug("New comm cap with reliability = {} and delay = {}", communicationReliability, communicationDelay);
    }

    public IBrokerGuidelines getBrokerGuidelines() {
        return brokerGuidelines;
    }

    public synchronized void setBrokerGuidelines(IBrokerGuidelines brokerGuidelines) {
        this.brokerGuidelines = brokerGuidelines;
    }

    public synchronized IConnections getChildConnections() {
        return this.childConnections;
    }

    public synchronized void setChildConnections(final IConnections childConnections) {
        this.childConnections = childConnections;
    }

    public IGoalModel getGoalModel() {
        return goalModel;
    }

    public String getOrgModelFolder() {
        return orgModelFolder;
    }

    public synchronized void setOrgModelFolder(String orgModelFolder) {
        this.orgModelFolder = orgModelFolder;
    }

    public IOrganizationCommunicationCapability getOrganizationCommunicationCapability() {
        return this.organizationCommunicationCapability;
    }

    public IOrganizationModel getOrganizationModel() {
        return this.organizationModel;
    }

    public Map<UniqueIdentifier, Agent<UniqueIdentifier>> getRegisteredAgentQueue() {
        return registeredAgentQueue;
    }

    public synchronized void setRegisteredAgentQueue(ConcurrentHashMap<UniqueIdentifier, Agent<UniqueIdentifier>> registeredAgentQueue) {
        this.registeredAgentQueue = registeredAgentQueue;
    }

    public synchronized boolean isAllRegistered() {
        return this.allRegistered;
    }

    public synchronized void setAllRegistered(final boolean allRegistered) {
        this.allRegistered = allRegistered;
    }

    // analogous to launcher.........................
    @Override
    public String toString() {
        return "MarketAdminCapability{" +
                "doneWaitingForInitialAssignments=" + doneWaitingForInitialAssignments +
                ", initialRoles=" + initialRoles +
                ", childConnections=" + childConnections +
                ", myorg=" + myorg +
                '}';
    }

    @Override
    public synchronized void reset() {
    }

    @Override
    public double getFailure() {
        return 0;
    }

    @Override
    public Element toElement(final Document document) {
        final Element capability = super.toElement(document);
        return capability;
    }

    public synchronized boolean createAndStartNewOrganization(InstanceGoal<?> ig) {
        boolean success = false;

        this.init(ig);
        IOrganization org = this.createOrganization(ig);
        if (org == null){
            success = false;
        }
        else {
            this.loadPersona();
            this.startOrganization();
            success = true;
        }
        return success;
    }

    /**
     Loads the given organization with agents from the given file.
     */
    public synchronized void loadPersona() {
        this.myorg.loadPersona();
    }

    public synchronized void init(InstanceGoal<?> ig) {
        if (debug) LOG.debug("Creating external organization from goal: {}.", ig);
        // Get the parameter values from the existing active instance goal
        final InstanceParameters params = Objects.requireNonNull((InstanceParameters) ig.getParameter());
        LOG.info("Initializing with the given goal parameter guidelines: {}.", params);
        this.setBrokerGuidelines(IBrokerGuidelines.extractBrokerGuidelines(params));
        if (this.brokerGuidelines == null) {
            LOG.error("Broker trying to initialize an external organization with no broker guidelines. params={}", params);
            System.exit(-51);
        }
        this.setChildConnections(IConnections.extractConnections(params, "auctionConnections"));
        if (this.childConnections == null) {
            LOG.error("Broker trying to initialize an external organization with no auction participants. params={}", params);
            System.exit(-56);
        } else {
            if (debug) LOG.info("Starting initialization of new organization by {}. ", this.getOwner());
            if (debug)
                LOG.info("{} authorized connections to participants.", childConnections.getListConnectionGuidelines().size());
            if (debug) LOG.info("Starting initialization of market org by {}. ", this.getOwner());
            final IConnectionGuidelines cg = childConnections.getListConnectionGuidelines().get(0);
            LOG.info("Organization guidelines found at {}:  ", cg.getSpecificationFilePath());
        }
    }

    public synchronized IOrganization createOrganization(InstanceGoal<?> ig) {
        // analogous to launcher......
        File folder = new File(RunManager.getAbsolutePathToTestCaseFolder() + "/" +
                childConnections.getListConnectionGuidelines().get(0).getSpecificationFilePath());
        LOG.info("The organization information is in {}", folder);
        this.orgModelFolder = childConnections.getListConnectionGuidelines().get(0).getOrgModelFolder();
        String orgName = folder.getName();
        LOG.info("The organization name is {}", folder);
        MarketHolonicLevel level = MarketHolonicLevel.getOrganizationType(orgName);
        if (this.orgModelFolder.isEmpty()) {
            LOG.error("ERROR: could not get path the folder with this orgs standard models. ");
            System.exit(-22);
        }
        LOG.debug("Organization goal and role models are in the standard models {} folder.", this.orgModelFolder);
        LOG.debug("The organization level is {}", level);
        String goalFilePath = RunManager.getAbsolutePathToStandardOrganizationGoalModel(this.orgModelFolder, level.toString());
        String roleFilePath = RunManager.getAbsolutePathToStandardOrganizationRoleModel(this.orgModelFolder, level.toString());
        HashMap<UniqueIdentifier, Object> orgParams = extractNewOrganizationGuidelines(ig);

        try {
            this.myorg = new MarketOrganizationFactory().createBaseAffiliateOrganization(folder.getAbsolutePath(),
                    OrganizationFocus.External, orgParams,
                    this.orgModelFolder, goalFilePath, roleFilePath, RunManager.getTopGoal());
            LOG.info("Market org initialized with agents and top goal guidelines. Standard models in {} subfolder.", myorg.getOrgModelFolder());

        }
        catch (Exception e) {
            LOG.error("ERROR: Could not create market organization.");
            return null;
        }
        return this.myorg;
    }

    private HashMap<UniqueIdentifier, Object> extractNewOrganizationGuidelines(InstanceGoal<?> ig) {
        final InstanceParameters params = Objects.requireNonNull((InstanceParameters) ig.getParameter());
        HashMap<UniqueIdentifier, Object> newMap = new HashMap<>();

        for (final Map.Entry<UniqueIdentifier, Object> entry : params.getParameters().entrySet()) {
            UniqueIdentifier id = entry.getKey();
            Object value = entry.getValue();
            if (debug) LOG.debug("\tSource goal guidelines: id={}, guideline={}", id.toString(), value);
            if (id.toString().contains("brokerGuidelines") || id.toString().contains("auctionConnections")) {
                newMap.put(id, value);
            }
        }
        return newMap;
    }



    public synchronized void startOrganization() {
        this.myorg.run();
    }

    public boolean doWaitingForAgentsForInitialAssignments() {
        /*
         * determine if there are enough initial agents to proceed with
         * reorganization or to continue waiting for more agents to register
         */
        this.doneWaitingForInitialAssignments = areThereEnoughAgentsForInitialAssignments();

        boolean doneWaiting = false;
        if (this.doneWaitingForInitialAssignments) {
            doneWaiting = true;
        } else {
            /* continue to process ecAgent registration getNumberOfMessages */
            while (organizationCommunicationCapability.messages() > 0) {
                final edu.ksu.cis.macr.aasis.agent.cc_message.participate.ParticipateMessage message = (edu.ksu.cis.macr.aasis.agent.cc_message.participate.ParticipateMessage)
                        organizationCommunicationCapability.receive();
                switch (message.getPerformativeType()) {
                    case BROADCASTING_AGENT_REGISTRATION:
                        processAgentRegistration(message);
                        break;
                    case AGENT_REGISTRATION_CONFIRMATION_RECEIVED:
                        processAgentRegistrationConfirmationReceived(message);
                        break;
                    default:
                        /*
                         * other performatives should NOT be received at all
                         * because
                         * at this state, no other performatives should be in
                          * use
                         */

                        // it's getting goal GOAL_MODEL_EVENT performative - just ignore
                        // messages that don't apply - Denise
                        //    throw new IllegalArgumentException(String
                        // .format("Unacceptable Performatives \"%s\"",
                        //            message.getPerformativeType()));
                }
            }
        }
        return doneWaiting;
    }

    private boolean areThereEnoughAgentsForInitialAssignments() {
        if (debug) LOG.debug("In MARKETAdminCapability of {} " +
                "areThereEnoughAgentsForInitialAssignments() and the " +
                "initialroles = {}", this.getOwner().getIdentifierString(), this.initialRoles);

        //if (debug) LOG.debug("areThereEnoughAgentsForInitialAssignments() {}", String
        //          .format("Roles Left Before Removal: %s", this.initialRoles));

        final Set<Role> rolesToRemove = new HashSet<>();
        if (this.initialRoles == null) {
            setInitialRoles();
            if (this.initialRoles == null) {
                LOG.error("Master Capability initial roles are still null.");
                System.exit(-34);
            }
        }
        for (final Role role : this.initialRoles) {
            this.organizationModel.getInstanceGoals().stream().filter(role::achieves).forEach(goal ->
                    rolesToRemove.addAll(this.organizationModel.getAgents().stream().filter(agent ->
                            role.goodness(agent, goal, new HashSet<>()) > RoleGoodnessFunction.MIN_SCORE).map(agent ->
                            role).collect(Collectors.toList())));
        }
        this.initialRoles.removeAll(rolesToRemove);
        return this.initialRoles.isEmpty();
    }

    public Set<Role> setInitialRoles() {
        return null;
    }

    private void processAgentRegistration(final edu.ksu.cis.macr.aasis.agent.cc_message.participate.ParticipateMessage message) {
        final Object content = message.getContent();
        if (content instanceof RegistrationContent) {
            final RegistrationContent registrationContent =
                    (RegistrationContent) content;

            if (debug) LOG.debug("processAgentRegistration() RegistrationContent: {}",
                    registrationContent);

            final Agent<UniqueIdentifier> agent = new AgentImpl<>(
                    registrationContent.getAgentIdentifier());
            for (final Map.Entry<UniqueIdentifier,
                    Double> entry : registrationContent.getCapabilities()
                    .entrySet()) {
                // LOG.debug("processAgentRegistration() Checking capability:
                // {}-{}", entry.getKey(), entry.getValue());

                final Capability capability = this.organizationModel.getCapability(entry.getKey());
                if (capability != null) {
                    agent.addPossesses(capability, entry.getValue());
                } else {
                    if (debug) LOG.debug("processAgentRegistration() Unknown Capability {}", entry.getKey());
                }
            }

            this.registeredAgentQueue.put(agent.getIdentifier(), agent);
            organizationCommunicationCapability.send(edu.ksu.cis.macr.aasis.agent.cc_message.participate.ParticipateMessage.createLocalParticipateMessage(
                    getOwner().getUniqueIdentifier(),
                    registrationContent.getAgentIdentifier(),
                    edu.ksu.cis.macr.aasis.agent.cc_message.participate.ParticipatePerformative.AGENT_REGISTRATION_CONFIRMATION,
                    getOwner().getUniqueIdentifier()));

            if (debug)
                LOG.debug("sent registration confirmation to {}: {}", agent.getIdentifier().toString(), agent.getPossessesSet());
        }
    }

    private void processAgentRegistrationConfirmationReceived(
            final edu.ksu.cis.macr.aasis.agent.cc_message.participate.ParticipateMessage message) {
        final UniqueIdentifier agentIdentifier = message.getLocalSender();
        final Agent<UniqueIdentifier> attributeAgent = this.registeredAgentQueue.get(
                agentIdentifier);
        if (attributeAgent != null) {
            this.organizationModel.addAgent(attributeAgent);
        } else {
            LOG.error("processAgentRegistrationConfirmationReceived() {}",
                    String.format("Unknown Agent! (%s)",
                            agentIdentifier));
        }
        triggerReorganization(); // DMC added
    }

    private void triggerReorganization() {
        reorganize();
    }

    protected void reorganize() {
        LOG.info("Calling reorg from {}", this.getClass().getName());
        final Collection<Assignment> assignments = getOrganizer().reorganize(
                getOrganizationModel(),
                this.organizationModel.getInstanceGoals(),
                this.organizationModel.getAgents());
        if (assignments == null) {
            if (debug)
                LOG.debug("reorganize() {}", "Reorganization Algorithm IUnreliable - assignment collection is null");
            return;
        }

        if (assignments.isEmpty()) { // no new ec
            if (debug) LOG.debug("reorganize() {}", "No New Assignments");
        } else { // there are new ec
            if (debug) LOG.debug("New Assignments {}", String.format(" (%d): %s", assignments.size(), assignments));

            this.organizationModel.addAssignments(assignments);
            for (final Assignment assignment : assignments) {
                if (debug) LOG.debug("sending assignment ParticipateMessage New Assignments {}: {} {}",
                        assignments.size(), assignments, assignment.getAgent().getIdentifier());

                edu.ksu.cis.macr.aasis.agent.cc_message.participate.ParticipateMessage Tempmessage = new edu.ksu.cis.macr.aasis.agent.cc_message.participate.ParticipateMessage(
                        getOwner().getUniqueIdentifier(),
                        assignment.getAgent().getIdentifier(),
                        edu.ksu.cis.macr.aasis.agent.cc_message.participate.ParticipatePerformative.ASSIGNMENT,
                        AssignmentContent.createAssignmentContent(assignment));
                if (remoteSendAssignmentMessage(Tempmessage)) {
                    if (debug)
                        LOG.debug("Remote Reasoning messages sent assigment successfully, Contents: {}", Tempmessage.toString());
                } else {
                    LOG.info("Warning: Remote Reasoning messages failed to send over MQ");
                }
                organizationCommunicationCapability.send(new edu.ksu.cis.macr.aasis.agent.cc_message.participate.ParticipateMessage(
                        getOwner().getUniqueIdentifier(), assignment.getAgent().getIdentifier(),
                        edu.ksu.cis.macr.aasis.agent.cc_message.participate.ParticipatePerformative.ASSIGNMENT, AssignmentContent.createAssignmentContent(assignment)));
            }
        }
    }

    private boolean remoteSendAssignmentMessage(edu.ksu.cis.macr.aasis.agent.cc_message.participate.ParticipateMessage message) {
        Objects.requireNonNull(message, "ERROR: Can't send a null message.");
        Objects.requireNonNull(channel, "ERROR: null channel in send().");

        final String receiver = message.getLocalReceiver().toString();
        final String fullQueueName = MarketMessagingManager.getFullQueueName(receiver, MarketMessagingManager.getQueueFocus(messagingFocus));
        final String routingKey = fullQueueName;
        MessagingManager.declareAndBindConsumerQueue(messagingFocus, receiver);
        try {
            byte[] messageBodyBytes = "hello".getBytes();
            channel.basicPublish(MessagingManager.getExchangeName(messagingFocus), routingKey, MessageProperties.PERSISTENT_TEXT_PLAIN, messageBodyBytes);
            // LOG.debug("Message sent to Queue {}", routingKey);
            return true;
        } catch (Exception e) {
            LOG.error("ERROR send() messages {} from {}. ", message.toString(),
                    message.getLocalSender().toString());
            return false;
        }
    }

    /**
     * Gets the set of local registered prosumer agents.
     *
     * @param allAgents - the set of all agents registered in this organization
     * @return - the set of all prosumer agents registered in this local organization (does not include
     * other types of agents such as forecasters, etc)
     */
    public Set<Agent<?>> getLocalRegisteredProsumers(Set<Agent<?>> allAgents) {
        // get the list of registered prosumer peer agents in the local
        // organization

        if (debug) LOG.debug("Number of all agents found in the MARKETAdminCapability is {}",
                allAgents.size());

        final Set<Agent<?>> prosumers = new HashSet<>();
        Iterator<Agent<?>> it = allAgents.iterator();

        final Class<?> capabilityClass = AuctionPowerCapability
                .class;
        final ClassIdentifier capabilityIdentifier = new ClassIdentifier(
                capabilityClass);

        while (it.hasNext()) {
            Agent<?> agent = it.next();
            if (debug)
                LOG.debug("Checking registered persona {} for AssessReactivePowerQualityCapability", agent.toString());
            if (agent.getPossesses(capabilityIdentifier) != null) {
                prosumers.add(agent);
                if (debug) LOG.debug("Agent {} added to local prosumers list", agent.toString());
            }
        }
        return prosumers;
    }

    public IReorganizationAlgorithm getOrganizer() {
        return this.reorganizationAlgorithm;
    }

    public synchronized void initializeChildConnections(InstanceGoal<?> instanceGoal) {
        if (debug) LOG.debug("Initializing connections to sub holons from goal: {}.", instanceGoal);
        // Get the parameter values from the existing active instance goal
        final InstanceParameters params = Objects
                .requireNonNull((InstanceParameters) instanceGoal
                        .getParameter());
        if (debug) LOG.debug("Initializing connections (to sub holons) params: {}.", params);
        final IConnections childConnections = Objects
                .requireNonNull((IConnections) params
                        .getValue(StringIdentifier.getIdentifier("childConnections")));
        //   if (debug) LOG.debug("Initializing sub connections: {}.", guidelines);
        this.setAllChildConnectionGuidelines(childConnections);
        if (debug)
            LOG.debug("There are {} authorized connections to participants.", childConnections.getListConnectionGuidelines().size());
    }

    /**
     * @param childConnections - the list of all authorized connections to child or subordinate agents.
     */
    public synchronized void setAllChildConnectionGuidelines(final IConnections childConnections) {
        this.setChildConnections(childConnections);
    }

    /**
     * Get the parameters from this instance goal and use them to set the goal-specific guidelines for any child
     * connections.
     *
     * @param instanceGoal - the instance goal that describes the guidelines
     */
    public synchronized void initializeGuidelines(InstanceGoal<?> instanceGoal) {
        if (debug) LOG.debug("Initializing all guidelines from goal: {}.", instanceGoal);
        // Get the parameter values from the existing active instance goal
        final InstanceParameters params = Objects
                .requireNonNull((InstanceParameters) instanceGoal
                        .getParameter());
        if (debug) LOG.debug("Initializing sub connections params: {}.", params);
        final IConnections childConnections = Objects
                .requireNonNull((IConnections) params
                        .getValue(StringIdentifier
                                .getIdentifier("childConnections")));
        //   LOG.debug("Initializing sub connections: {}.", guidelines);
        this.setChildConnections(childConnections);
        if (debug)
            LOG.debug("There are {} authorized connections to participants.", childConnections.getListConnectionGuidelines().size());
    }

    private boolean processEvent(final IOrganizationEvent organizationEvent) {
        //TODO: Fix method call not descriptive enough for return
        final Object parameter = organizationEvent.getParameters();
        final OrganizationEventType eventType = organizationEvent.getEventType();
        if (debug) LOG.debug("processEvent OrganizationEvent: {}", organizationEvent);

        boolean reOrgNeeded = false;
        switch (eventType) {
            case EVENT:
                processGoalModelEvent(
                        organizationEvent.getInstanceGoal().getIdentifier(),
                        organizationEvent.getSubEvent(),
                        (InstanceParameters) parameter);
                reOrgNeeded = true;
                break;
            case GOAL_MODEL_MODIFICATION:
                processGoalModelModification(
                        organizationEvent.getInstanceGoal().getIdentifier(),
                        (InstanceParameters) parameter);
                break;
            case TASK_FAILURE_EVENT:
                processTaskFailureEvent(organizationEvent.getSubEvent(), parameter);
                break;

            case AGENT_GONE_EVENT:
                processAgentGoneEvent(parameter);
                reOrgNeeded = true;
                break;
            default:
                throw new IllegalArgumentException("Unknown Event Type at processEvent " + organizationEvent.getEventType());
        }
        return reOrgNeeded;
    }

    // same as control component master
    private void processGoalModelEvent(final UniqueIdentifier goalIdentifier,
                                       final UniqueIdentifier eventIdentifier,
                                       final InstanceParameters parameters) {
        /* all other events are handled by the goal model */
        final InstanceGoal<InstanceParameters> instanceGoal = getGoalModel().getInstanceGoal(goalIdentifier);
        if (this.organizationModel.getInstanceGoal(instanceGoal.getIdentifier()) != null) {
            SpecificationEvent specificationEvent = null;
            if (SpecificationEvent.ACHIEVED_EVENT.getIdentifier().equals(eventIdentifier)) {
                specificationEvent = SpecificationEvent.ACHIEVED_EVENT;
            } else if (SpecificationEvent.FAILED_EVENT.getIdentifier().equals(eventIdentifier)) {
                specificationEvent = SpecificationEvent.FAILED_EVENT;
            } else {
                specificationEvent = getGoalModel().getSpecificationEvent(
                        instanceGoal.getSpecificationIdentifier(),
                        eventIdentifier);
            }
            if (specificationEvent == null) {
                throw new IllegalArgumentException(String.format(
                        "Unspecified Event at processGoalModelEvent: %s",
                        eventIdentifier));
            }
            final InstanceTreeChanges changeList = getGoalModel().event(instanceGoal, specificationEvent, parameters);
            updateActiveGoals(changeList);
        }
    }

    private synchronized void updateActiveGoals(final InstanceTreeChanges changeList) {
        if (debug) LOG.debug("updateActiveGoals() - ChangeList (Adding: {}, Removing: {})",
                changeList.getAddedInstanceGoals(),
                changeList.getRemovedInstanceGoals());

        changeList
                .getAddedInstanceGoals().forEach(this.organizationModel::addInstanceGoal);
        for (final InstanceGoal<InstanceParameters> goal : changeList
                .getRemovedInstanceGoals()) {
            /* if the removed goal has been assigned,
            its need to be deassigned */
            final Assignment assignment = getAssignmentOfInstanceGoal(goal);
            if (assignment != null) {
                organizationCommunicationCapability.send(ParticipateMessage.createLocalParticipateMessage(
                        getOwner().getUniqueIdentifier()
                        , assignment.getAgent().getIdentifier(),
                        edu.ksu.cis.macr.aasis.agent.cc_message.participate.ParticipatePerformative.DEASSIGNMENT,
                        AssignmentContent.createAssignmentContent(assignment)));

                this.organizationModel.removeAssignment(assignment.getIdentifier());
            }
            this.organizationModel.removeInstanceGoal(goal.getIdentifier());
        }
        if (this.organizationModel.getInstanceGoals().isEmpty()) {
            //Don't end when  goals are empty
            //  TerminationCriteria.setIsDone(true);
        }
    }

    private Assignment getAssignmentOfInstanceGoal(final InstanceGoal<InstanceParameters> instanceGoal) {
        for (final Assignment assignment : this.organizationModel.getAssignments()) {
            if (instanceGoal.equals(assignment.getInstanceGoal())) {
                return assignment;
            }
        }
        return null;
    }

    private void processGoalModelModification(
            final UniqueIdentifier goalIdentifier,
            final InstanceParameters parameters) {
        final InstanceTreeModifications goalModifications = getGoalModel()
                .modifyInstanceGoal(goalIdentifier, parameters);
        final Set<InstanceGoal<InstanceParameters>> modifiedGoals =
                goalModifications.getModifiedInstanceGoals();

        for (final InstanceGoal<InstanceParameters> instanceGoal : modifiedGoals) {
            final Assignment assignment = getAssignmentOfInstanceGoal(instanceGoal);
            if (assignment != null) {
                final ModificationContent content = new ModificationContent(instanceGoal);
                final ParticipateMessage message = new ParticipateMessage(getOwner().getUniqueIdentifier(),
                        assignment.getAgent().getIdentifier(), ParticipatePerformative.GOAL_MODIFICATION, content);
                organizationCommunicationCapability.send(message);
            }
        }
    }

    private void processTaskFailureEvent(final UniqueIdentifier subEvent, final Object parameter) {
        /*
         * this reasoning handles the "task failure" event. remove the
         * assignment for the indicated failure
         */
        final AssignmentContent assignmentContent = (AssignmentContent) parameter;
        final UniqueIdentifier agentIdentifier = assignmentContent.getAgentIdentifier();
        final UniqueIdentifier roleIdentifier = assignmentContent.getRoleIdentifier();
        final UniqueIdentifier instanceGoalIdentifier = assignmentContent.getInstanceGoalIdentifier();
        final Assignment assignment = getAssignment(agentIdentifier, roleIdentifier, instanceGoalIdentifier);
        if (debug) LOG.debug("processEvent(): {} Failed To Achieve {}", agentIdentifier, instanceGoalIdentifier);

        if (assignment != null) {
            this.organizationModel.removeAssignment(assignment.getIdentifier());
        }
    }

    private Assignment getAssignment(final UniqueIdentifier agentIdentifier,
                                     final UniqueIdentifier roleIdentifier,
                                     final UniqueIdentifier goalIdentifier) {
        final Collection<Assignment> assignments = getOrganizationModel()
                .getAssignments();
        for (final Assignment assignment : assignments) {
            if (agentIdentifier.equals(
                    assignment.getAgent().getIdentifier()) && roleIdentifier
                    .equals(
                            assignment
                                    .getRole().getIdentifier()
                    ) && goalIdentifier
                    .equals(
                            assignment.getInstanceGoal().getIdentifier()
                    )) {
                return assignment;
            }
        }

        LOG.error("getAssignment() {}",
                String.format("No Assignment Found! (%s, %s, %s)",
                        agentIdentifier,
                        roleIdentifier, goalIdentifier));

        return null;
    }

    private void processAgentGoneEvent(final Object parameter) {
        final IAgentGoneContent IAgentGoneContent = (IAgentGoneContent) parameter;
        final UniqueIdentifier agentIdentifier = IAgentGoneContent
                .getAgentIdentifier();
        final Set<Assignment> assignments = this.organizationModel.getAssignmentsOfAgent(
                agentIdentifier);
        for (final Assignment assignment : assignments) {
            this.organizationModel.removeAssignment(assignment.getIdentifier());
        }
        this.organizationModel.removeAgent(agentIdentifier);
        triggerReorganization(); // DMC added
    }

    private void processMessage(final ParticipateMessage message) {
        //LOG.debug("processMessage() Message: {}", messages);
        if (message != null) {
            switch (message.getPerformativeType()) {
                case BROADCASTING_AGENT_REGISTRATION:
                    processAgentRegistration(message);
                    break;
                case AGENT_REGISTRATION_CONFIRMATION_RECEIVED:
                    processAgentRegistrationConfirmationReceived(message);
                    break;
                case AGENT_UPDATE_INFORMATION:
                    processAgentUpdateInformation(message);
                    break;
                case GOAL_MODEL_EVENT:
                    processEvent(message);
                    break;
                case ASSIGNMENT:
                    processAssignment(message);
                    break;
                case DEASSIGNMENT:
                    processDeAssignment(message);
                    break;
                case GOAL_MODIFICATION:
                    processGoalModification(message);
                    break;
                default:
                    /*
                     * other performatives should NOT be received at all because
                     * they are directed and this ecAgent CANNOT have any of
                     * those
                     */
                    LOG.error("Unacceptable performative in processMessage: {}. content = {}", message.getPerformativeType(), message.getContent().toString());
                    System.exit(-35);
                    throw new IllegalArgumentException(
                            String.format("Unacceptable Performatives \"%s\"",
                                    message.getPerformativeType()));
            }
        }
    }

    // same as self control component master
    private void processAgentUpdateInformation(
            final ParticipateMessage message) {
        final Object content = message.getContent();
        if (content instanceof RegistrationContent) {
            final RegistrationContent registrationContent =
                    (RegistrationContent) content;

            //LOG.debug("processUpdateAgentInformation() {}", String.format(
            //		"RegistrationContent: %s", registrationContent));

            final Agent<?> agent = this.organizationModel.getAgent(
                    registrationContent.getAgentIdentifier());
            for (final Map.Entry<UniqueIdentifier,
                    Double> entry : registrationContent.getCapabilities()
                    .entrySet()) {
                final Capability capability = this.organizationModel.getCapability(
                        entry.getKey());

                final double score = entry.getValue();
                // Updated DMC - 10/6/2013 - to handle the case when the
                // agent has a capability (score = 1) but the
                // capability is not needed (possesses = 0.0)
                final double agentPossessesScore = agent.getPossessesScore(
                        capability.getIdentifier());
                if (score != agentPossessesScore && agentPossessesScore != 0.0
                        && score == 1.0) {
                    agent.setPossessesScore(capability.getIdentifier(), score);
                }
            }
            /*
             * when ecAgent updates their information, a check has to be done to
             * ensure the ecAgent is still able to continue working on the
             * current
             * ec
             */
            checkAgent(agent);
        }
    }

    private void checkAgent(final Agent<?> agent) {
        final Collection<Assignment> assignmentsToRemove = new ArrayList<>();
        for (final Assignment assignment : this.organizationModel.getAssignmentsOfAgent(
                agent.getIdentifier())) {
            final double goodness = assignment.getRole().goodness(agent,
                    assignment.getInstanceGoal(),
                    new HashSet<>());
            if (goodness <= RoleGoodnessFunction.MIN_SCORE) {
                /* ecAgent is unable to work on the assignment */
                final IBaseMessage message = ParticipateMessage.createLocalParticipateMessage(
                        getOwner().getUniqueIdentifier(),
                        assignment.getAgent().getIdentifier(),
                        ParticipatePerformative.DEASSIGNMENT,
                        AssignmentContent.createAssignmentContent(assignment));
                this.organizationCommunicationCapability.send(message);
                assignmentsToRemove.add(assignment);
            }
        }
        for (final Assignment assignment : assignmentsToRemove) {
            getOrganizationModel().removeAssignment(assignment.getIdentifier());
        }
    }

    private void processEvent(final ParticipateMessage message) {
        final Object content = message.getContent();
        if (content instanceof ListEventsContent) {
            final ListEventsContent listEventsContent = (ListEventsContent) content;
            for (final OrganizationEventContent organizationEventContent : listEventsContent.getEvents()) {
                final InstanceGoal<?> instanceGoal = getOrganizationModel()
                        .getInstanceGoal(organizationEventContent.getInstanceGoalIdentifier());
                final OrganizationEvent organizationEvent = organizationEventContent.toOrganizationEvent(instanceGoal);
                // if it needs a reorganization, just do it - DMC
                if (processEvent(organizationEvent)) {
                    triggerReorganization();
                }
            }
        }
    }

    protected void processAssignment(final ParticipateMessage message) {
        final Object content = message.getContent();
        if (content instanceof AssignmentContent) {
            final AssignmentContent assignmentContent = (AssignmentContent) content;
            if (debug) LOG.debug("processAssignment(), AssignmentContent: {}", assignmentContent);

            final Agent<?> agent = this.organizationModel.getAgent(assignmentContent.getAgentIdentifier());
            final Role role = this.organizationModel.getRole(assignmentContent.getRoleIdentifier());
            final SpecificationGoal specificationGoal = this.organizationModel.getSpecificationGoal(assignmentContent.getSpecificationGoalIdentifier());
            final InstanceGoal<?> instanceGoal = specificationGoal.getInstanceGoal(assignmentContent.getInstanceGoalIdentifier(), assignmentContent.getParameter());
            final Assignment assignment = new Assignment(agent, role, instanceGoal);
            getOwner().addAssignment(assignment);
        }
    }

    protected void processDeAssignment(final ParticipateMessage message) {
        final Object content = message.getContent();
        if (content instanceof AssignmentContent) {
            final AssignmentContent assignmentContent = (AssignmentContent) content;

            if (debug) LOG.debug("processDeAssignment() {}",
                    String.format("De-Assignment: %s", assignmentContent));

            final Agent<?> agent = this.organizationModel.getAgent(assignmentContent.getAgentIdentifier());
            final Role role = this.organizationModel.getRole(assignmentContent.getRoleIdentifier());
            final SpecificationGoal specificationGoal = getOrganizationModel
                    ().getSpecificationGoal(assignmentContent.getSpecificationGoalIdentifier());
            final InstanceGoal<?> instanceGoal = specificationGoal
                    .getInstanceGoal(assignmentContent.getInstanceGoalIdentifier(), assignmentContent.getParameter());
            final Assignment assignment = new Assignment(agent, role, instanceGoal);
            getOwner().addDeAssignment(assignment);
        }
    }

    protected void processGoalModification(final ParticipateMessage message) {
        final Object content = message.getContent();
        if (content instanceof ModificationContent) {
            final ModificationContent modificationContent = (ModificationContent) content;

            if (debug)
                LOG.debug("processGoalModification() - {}", String.format("Modification: %s", modificationContent));

            final SpecificationGoal specificationGoal = getOrganizationModel
                    ().getSpecificationGoal
                    (modificationContent.getSpecificationGoalIdentifier());
            final InstanceGoal<InstanceParameters> instanceGoal = specificationGoal.getInstanceGoal
                    (modificationContent.getInstanceGoalIdentifier(), modificationContent.getInstanceParameters());
            getOwner().addGoalModification(instanceGoal);
        }
    }

    public synchronized boolean processRegistrationMessages() {
        if (debug) LOG.debug("Beginning attempts to get registration messages from all sub holons.");
        if (noChildren()) {
            if (debug) LOG.debug("No sub holons, so assumed to be fully registered.");
            this.allRegistered = true;
            return this.allRegistered;
        }
        final int tot = getChildConnections().getListConnectionGuidelines().size();
        if (debug)
            LOG.debug("Need {} of {} registrations to register sub holons.", childRegistrationsStillNeeded(), tot);

        if (childRegistrationsStillNeeded() == 0) {
            if (debug) LOG.debug("Already fully registered.");
            this.allRegistered = true;
            return this.allRegistered;
        }
        getChildConnections().getListConnectionGuidelines().stream().filter(IConnectionGuidelines::isRegistered).forEach(cg -> registerAnyChild());
        this.allRegistered = (childRegistrationsStillNeeded() == 0);
        return this.allRegistered;
    }

    public synchronized boolean noChildren() {
        if (debug) LOG.debug("Calling noChildren to see if {} has no sub holons.", owner.getIdentifierString());
        boolean noChildren = false;
        try {
            if (getChildConnections() == null || getChildConnections().getListConnectionGuidelines() == null ||
                    getChildConnections().getListConnectionGuidelines().isEmpty()) noChildren = true;
        } catch (Exception e) {
            LOG.error("Error checking to see if {} has no sub holons. childConnections = {}", owner.getIdentifierString(), this.getChildConnections());
            System.exit(-4);
        }
        if (debug) LOG.debug("{} no sub holons = {}", owner.getIdentifierString(), noChildren);
        return noChildren;
    }

    private synchronized boolean registerAnyChild() {
        try {
            final ParticipateMessage message = remoteRECEIVE(owner.getIdentifierString());
            if (message == null) {
                if (debug) LOG.debug("No registration received.");
                return false;
            }
            if (debug) LOG.debug("Received registration message: {}.", message);
            final String sender = message.getRemoteSender();  // other
            final String receiver = message.getRemoteReceiver();  // me

            // find the associated cg and update registration to true
            getChildConnections().getListConnectionGuidelines().stream().filter(cg ->
                    cg.getOtherAgentAbbrev().equals(sender)).forEach(cg -> cg.setRegistered(true));
            if (debug) LOG.info("Super holon {} registered sub holon {}.", receiver, sender);
        } catch (Exception e) {
            LOG.error("Error getting getting remote recieve registration message.");
            System.exit(-4);
        }
        return true;
    }

    /**
     * Checking for remote registration messages. Sub holons send them with their OrganizationCommunicationCapaability.
     *
     * @param queueLink - prefix for the messaging queue, e.g. N43inN43.
     * @return String messages - Grabs messages from Queue
     * @throws IOException             - Handles any IO Exceptions
     * @throws ShutdownSignalException - Handles any ShutdownSignal Exceptions
     * @throws InterruptedException    - Handles any Interrupted Exceptions
     */
    public synchronized ParticipateMessage remoteRECEIVE(final String queueLink) throws IOException, ShutdownSignalException, InterruptedException {
        final String fullQueueName = MarketMessagingManager.getFullQueueName(queueLink, MarketMessagingManager.getQueueFocus(messagingFocus));
        setConsumer(queueLink);
        // check for delivery for given milliseconds
        final QueueingConsumer.Delivery delivery = consumer.nextDelivery(RunManager.getDeliveryCheckTime_ms());

        if (delivery != null) {
            if (debug) LOG.debug("got something on {}", fullQueueName);
            ParticipateMessage message = ParticipateMessage.createParticipateMessage();
            if (debug)
                LOG.debug("Deserializing delivery. Created new ParticipateMessage={}. Delivery body={}", message.toString(), Arrays.toString(delivery.getBody()));
            try {
                message = (ParticipateMessage) message.deserialize(delivery.getBody());
                if (debug) LOG.debug("RECEIVED remote ParticipateMessage on {}. {}.", fullQueueName, message);
                return message;
            } catch (Exception ex) {
                LOG.error("ERROR deserializing ParticipateMessage: {}", ex.getCause().toString());
                System.exit(-11);
            }
        }
        if (debug) LOG.debug("got nothing on {}", fullQueueName);
        return null;
    }

    private void setConsumer(String queueLink) throws IOException {
        MessagingManager.declareAndBindConsumerQueue(messagingFocus, queueLink);
        String fullQueueName = MarketMessagingManager.getFullQueueName(queueLink, MarketMessagingManager.getQueueFocus(messagingFocus));
        String basicConsume = channel.basicConsume(fullQueueName, true, consumer);
    }

    private synchronized int childRegistrationsStillNeeded() {
        if (noChildren()) return 0;
        int registrationsStillNeeded = 0;

        List<? extends IConnectionGuidelines> listConnectionGuidelines = getChildConnections().getListConnectionGuidelines();
        for (IConnectionGuidelines cg : listConnectionGuidelines) {
            if (alreadyInRegisteredAgentQueue(cg.getOtherAgentAbbrev())) {
                cg.setRegistered(true);
            }
            if (cg.isRegistered()) {
                registrationsStillNeeded += 1;
            }
        }
        final int tot = getChildConnections().getListConnectionGuidelines().size();
        if (debug)
            LOG.debug("Need {} of {} registrations to fully register all sub holons.", registrationsStillNeeded, tot);
        return registrationsStillNeeded;
    }

    private synchronized boolean alreadyInRegisteredAgentQueue(final String otherAgentName) {
        UniqueIdentifier id = StringIdentifier.getIdentifier(otherAgentName);
        Object agent = this.getRegisteredAgentQueue().get(id);
        return (agent != null);
    }

    public synchronized boolean processRegistrationMessages(InstanceGoal<?> instanceGoal) {
        this.init(instanceGoal);
        return this.processRegistrationMessages();
    }

    public synchronized boolean processingRegistrationIsComplete() {
        return childRegistrationsStillNeeded() == 0;
    }

    public synchronized boolean processingRegistrationIsComplete(final InstanceGoal<?> instanceGoal) {
        init(instanceGoal);
        return childRegistrationsStillNeeded() == 0;
    }
}
