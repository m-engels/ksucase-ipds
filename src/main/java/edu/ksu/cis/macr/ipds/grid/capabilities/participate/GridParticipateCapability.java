package edu.ksu.cis.macr.ipds.grid.capabilities.participate;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import edu.ksu.cis.macr.aasis.agent.cc_message.IBaseMessage;
import edu.ksu.cis.macr.aasis.agent.cc_message.participate.ParticipateMessage;
import edu.ksu.cis.macr.aasis.agent.cc_message.participate.ParticipatePerformative;
import edu.ksu.cis.macr.aasis.agent.ec_cap.IOrganizationCommunicationCapability;
import edu.ksu.cis.macr.aasis.agent.persona.*;
import edu.ksu.cis.macr.aasis.common.IConnectionGuidelines;
import edu.ksu.cis.macr.aasis.common.IConnections;
import edu.ksu.cis.macr.aasis.messaging.IMessagingFocus;
import edu.ksu.cis.macr.aasis.messaging.MessagingManager;
import edu.ksu.cis.macr.aasis.simulator.player.PlayableCapability;
import edu.ksu.cis.macr.goal.model.InstanceParameters;
import edu.ksu.cis.macr.ipds.config.RunManager;
import edu.ksu.cis.macr.ipds.grid.messaging.GridMessagingFocus;
import edu.ksu.cis.macr.ipds.grid.messaging.GridMessagingManager;
import edu.ksu.cis.macr.ipds.primary.capabilities.manage.AssessReactivePowerQualityCapability;
import edu.ksu.cis.macr.obaa_pp.cc.om.IOrganizationModel;
import edu.ksu.cis.macr.obaa_pp.cc_message.AssignmentContent;
import edu.ksu.cis.macr.obaa_pp.cc_message.ModificationContent;
import edu.ksu.cis.macr.obaa_pp.cc_message.RegistrationContent;
import edu.ksu.cis.macr.obaa_pp.events.IOrganizationEvent;
import edu.ksu.cis.macr.organization.model.*;
import edu.ksu.cis.macr.organization.model.Organization;
import edu.ksu.cis.macr.organization.model.identifiers.ClassIdentifier;
import edu.ksu.cis.macr.organization.model.identifiers.StringIdentifier;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GridParticipateCapability extends AbstractOrganizationCapability implements IOrganizationCommunicationCapability, ICapability {
    /**
     The {@code ICommunicationChannel} keyword for registering organization getNumberOfMessages.
     */
    private static final String COMMUNICATION_CHANNEL_ID = "Organization Messages";
    private static final Logger LOG = LoggerFactory.getLogger(GridParticipateCapability.class);
    private static final String QUEUE_PURPOSE = "_GRID";

    /*
    The time an agent will wait to re-send registration when sending fails. Can be set by the user.
     */
    private static int retry_delay_ms;

    /*
    The time an agent will wait to get a confirmation before moving back to registration and re-starting the process. Can be set by the user.
     */
    private static int confirmation_timeout_ms;

    private static Channel channel;
    private static final boolean debug = false;
    private static final IMessagingFocus messagingFocus = GridMessagingFocus.GRID_PARTICIPATE;


    /**
     A {@code Queue} that holds {@code Message} sent to this {@code IAbstractControlComponent}.
     */
    private final Queue<IBaseMessage<?>> organizationMessages;
    private final IPersona owner;
    private final Queue<List<IOrganizationEvent>> queuedOrganizationEvents = new ConcurrentLinkedQueue<>();
    private String masterName = "";
    private boolean reorganize = false;
    private Long startTime = null;
    private GregorianCalendar simulationStartTime;
    private GregorianCalendar simulationTime;
    private PlayableCapability playerCapability;
    private boolean isRegistered = false;
    private IConnections parentConnections = null;
    /**
     Contains the {@code IOrganizationModel}.
     */
    private IOrganizationModel iOrganizationModel = null;


    /**
     Construct a new {@code ParticipateInOrganizationCapability} instance.

     @param owner - the agent possessing this capability.
     @param org - the immediate organization in which this agent operates.
     */
    public GridParticipateCapability(final IPersona owner, final IOrganization org) {
        super(GridParticipateCapability.class, owner, org);
        channel =  GridMessagingManager.getChannel(messagingFocus);
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

    @Override
    public synchronized void reset() {
    }

    @Override
    public synchronized void channelContent(Object content) {
        LOG.info("Entering channelContent(). Gets raw content and adds message. content={}", content);
        try {
            organizationMessages.add((IBaseMessage<?>) content);
        } catch (Exception e) {
            LOG.error("Cannot cast channelContent to Message<?>");
            System.exit(-73);
        }
    }

    @Override
    public String getCommunicationChannelID() {
        return COMMUNICATION_CHANNEL_ID;
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
            String routingKey = GridMessagingManager.getFullQueueName(message.getRemoteReceiver(), GridMessagingManager.getQueueFocus(messagingFocus));
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

    @Override
    public Element toElement(final Document document) {
        return super.toElement(document);
    }

    public boolean doInitialization() {
        return true;
    }

    /**
     Attempt to register with the organization master.  Organization master may be within my self organization, or
     may be another local agent on this device, or may be a remote agent running on a different device.
     @return - true if successful, false if not.
     */
    public synchronized boolean doRegistration() {
        LOG.debug("Beginning do Registration.");
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
            LOG.debug("The remote org to join={}, this master={}, orgmaster={}", org, this.masterName, StringIdentifier.getIdentifier(org));
        final ParticipateMessage reasoningMessage = new ParticipateMessage(this.owner.getUniqueIdentifier(),
                StringIdentifier.getIdentifier(org + "in" + org), ParticipatePerformative.BROADCASTING_AGENT_REGISTRATION,
                registrationContent);
        if (debug) LOG.debug("Created remote reasoning message: {}", reasoningMessage.toString());

        if (this.sendRemote(reasoningMessage)) {
            LOG.debug("successful registration message sent (remote) {}", reasoningMessage.toString());
            this.isRegistered = true;
        } else {      /* send has failed, so retry again in a bit */
            LOG.error("failed sending registration: {}", reasoningMessage.toString());
            System.exit(-2222);
        }
        return this.isRegistered;
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

//  public int doRegistrationConfirmation() {
//    int response = 0;  // 1 is timed out , 2 is successful
//
//    if (this.selfComm.messages() > 0) {
//      final ParticipateMessage message = (ParticipateMessage) selfComm.receive();
//      switch (message.getPerformativeType()) {
//        case BROADCASTING_AGENT_REGISTRATION:
//                    /*
//                     * it is possible to receive ecAgent registrations from other
//                     * agents while waiting for a confirmation, so ignore it
//                     */
//
//          LOG.debug("doRegistrationConfirmation() Discarding Agent Registration in order to confirm: " +
//                  "{}", message.toString());
//
//
//          response = 0; // continue
//          break;
//        case AGENT_REGISTRATION_CONFIRMATION:
//          final Object content = message.getContent();
////                    if (content instanceof UniqueIdentifier) {
////                        this.master = (UniqueIdentifier) content;
////
////
////                        LOG.debug("doRegistrationConfirmation() Agent Registration Confirmation Received From " +
////                                "{}", this.master);
////
////
////                        selfComm.send(new ParticipateMessage(this.owner
////                                .getUniqueIdentifier(), this.master,
////                                ParticipatePerformative.AGENT_REGISTRATION_CONFIRMATION_RECEIVED, null));
////                        response = 2; // success
////                    }
//          String msg = content.toString();
//          LOG.debug("doRegistrationConfirmation() Agent Registration Confirmation Received From " +
//                  "{}", msg);
//
//
//          selfComm.send(new ParticipateMessage(this.owner
//                  .getUniqueIdentifier().toString(), this.masterName,
//                  ParticipatePerformative.AGENT_REGISTRATION_CONFIRMATION_RECEIVED, null));
//          response = 2; // success
//          break;
//        default:
//                    /*
//                     * other performatives should NOT be received at all because
//                     * they are directed and this ecAgent CANNOT have any of those
//                     */
//          LOG.error("Unacceptable performative in processMessage in doRegistrationConfirmation: {}. content = {}", message.getPerformativeType(), message.getContent().toString());
//          System.exit(-38);
//          throw new IllegalArgumentException(String.format("Unacceptable Performatives \"%s\"",
//                  message.getPerformativeType()));
//      }
//    } else {
//      response = 0;
//      //  this.currentExecutionState = ExecutionState.TIMING_OUT_DURING_CONFIRMATION;
//    }
//    return response;
//  }

    public boolean doRegistrationConfirmationTimeout() {
        boolean timedout = false;
        /* no confirmation received */
        final long currentTime = System.currentTimeMillis();
        if ((currentTime - this.startTime) > this.confirmation_timeout_ms) {
            /* waited long enough for a confirmation */

            if (debug) LOG.debug("doRegistrationConfirmation() {}",
                    String.format("Waiting Done: ((%d - %d) = %d) > %d",
                            currentTime, this.startTime, currentTime - this.startTime, confirmation_timeout_ms));

            timedout = true;
            //  this.currentExecutionState = ExecutionState.REGISTERING;
        } else {
            timedout = false;
            //   this.currentExecutionState = ExecutionState.CONFIRMING_REGISTRATION;
        }
        return timedout;
    }

    public boolean doRegistrationRetry() {
        boolean timedout = false;
        /* waiting to retry registration */
        final long currentTime = System.currentTimeMillis();
        final long timeDifference = currentTime - this.startTime;
        if (timeDifference > this.retry_delay_ms) {
            /* waited long enough, so retry registration again */

            LOG.debug("doRegistrationRetry() {}",
                    String.format("Waiting Done: ((%d - %d) = %d) > %d", currentTime,
                            this.startTime, timeDifference, retry_delay_ms));


            timedout = true;
        } else {
            /* continue to wait */

            LOG.debug("doRegistrationRetry() persona {} seeking master {} Continue Waiting: (({}- {}) = {}) <= {}",
                    owner.getIdentifierString(), this.masterName, String.format("%d", currentTime),
                    String.format("%d", this.startTime), String.format("%d", timeDifference), String.format("%d",
                            retry_delay_ms));
            timedout = false;
        }
        return timedout;
    }

//  public boolean doWork() {
//    boolean success = true;
//    LOG.debug("doing slave work ");
//         /* process queuedOrganizationEvents first */
//      while (organizationEvents.numberOfQueuedEvents() > 0) {
//          final List<IOrganizationEvent> organizationEvents = this.organizationEvents.getNextEvent();
//
//          if (debug) {
//              LOG.debug("numberOfQueuedEvents  {}",
//                      String.format("processing OrganizationEvent: %s",
//                              organizationEvents));
//          }
//          processEvent(organizationEvents);
//      }
//        /* process getNumberOfMessages next */
//    while (selfComm.messages() > 0) {
//      final IBaseMessage<?> message = selfComm.receive();
//
//      if (message instanceof IParticipateMessage) {
//        processMessage((ParticipateMessage) message);
//      }
//      if (message instanceof IParticipateMessage) {
//        processMessage((ParticipateMessage) message);
//      }
//    }
//    // if step mode is on, wait for user, otherwise just continue
//    //waitForUserToAdvance(this.playerCapability.getStepMode(),0 );
//    //this.playerCapability.step();
//    return true;
//    //triggerReorganization();
//  }

//  private void processEvent(final List<IOrganizationEvent> organizationEvents) {
//    if (this.masterName.equals("")) {
//            /*
//             * this code should rarely be executed if it is executed at all.
//             * queue up the queuedOrganizationEvents so they can be sent later
//             */
//
//      //LOG.debug("processEvent()", String.format(
//      //		"Queueing OrganizationEvent: %s", numberOfQueuedEvents));
//
//      final boolean added = this.queuedOrganizationEvents.offer(organizationEvents);
//
//      if (!added) {
//        // LOG.debug("processEvent() {}", String.format(
//        //         "Events Queue Full: %d",
//        //         this.queuedOrganizationEvents.size()));
//      }
//    } else {
//            /*
//             * if there is a queue of events that are to be sent, send them
//             * first
//             */
//      while (!this.queuedOrganizationEvents.isEmpty()) {
//        final List<IOrganizationEvent> priorEvents = this.queuedOrganizationEvents.poll();
//        if (debug) {
//          LOG.debug("processEvent()", String.format("Sending Previously Queued OrganizationEvent: %s",
//                  priorEvents));
//        }
//        final ListEventsContent priorContent = new ListEventsContent(OrganizationEventContent.convert(priorEvents));
//        final IBaseMessage messagePrior = ParticipateMessage.createRemoteParticipateMessage(this.owner
//                .getUniqueIdentifier().toString(), this.masterName, ParticipatePerformative.GOAL_MODEL_EVENT, priorContent);
//        selfComm.send(messagePrior);
//      }
//      if (debug) {
//        LOG.debug("processEvent()", String.format("Sending Current OrganizationEvent: %s", organizationEvents));
//      }
//      final ListEventsContent currentContent = new ListEventsContent(OrganizationEventContent.convert(organizationEvents));
//      final IBaseMessage messageCurrent = ParticipateMessage.createRemoteParticipateMessage(this.owner.getUniqueIdentifier().toString(),
//              this.masterName, ParticipatePerformative.GOAL_MODEL_EVENT, currentContent);
//      selfComm.send(messageCurrent);
//    }
//  }

    private void processMessage(final ParticipateMessage message) {
        if (message != null) {
            switch (message.getPerformativeType()) {
                case BROADCASTING_AGENT_REGISTRATION:
                    /*
                     * it is possible to receive ecAgent registrations from other
                     * agents as new agents could join the organization
                     */
                    if (debug) {
                        LOG.debug("processMessage() {}", String.format("Discarding Agent Registration: %s",
                                message.toString()));
                    }
                    break;
                case ASSIGNMENT:
                    if (debug) {
                        LOG.debug("GOT AN ASSIGNMENT MESSAGE {}", String.format("Agent Assignment: %s",
                                message.toString()));
                    }
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
                     * they are directed and this ecAgent CANNOT have any of those
                     */
                    LOG.error("Unacceptable performative in processMessage: {}. content = {}", message.getPerformativeType(), message.getContent().toString());
                    System.exit(-39);
                    throw new IllegalArgumentException(String.format("Unacceptable Performatives \"%s\"",
                            message.getPerformativeType()));
            }
        }
    }

    protected void processAssignment(final IBaseMessage message) {
        final Object content = message.getContent();
        if (content instanceof AssignmentContent) {
            final AssignmentContent assignmentContent = (AssignmentContent) content;
            if (debug) {
                LOG.debug("processAssignment(),  {}", String.format("AssignmentContent: %s", assignmentContent));
            }
            final Agent<?> agent = (Agent<?>) this.owner.getUniqueIdentifier();
            //   final Agent<?> agent = getOrganizationModel().getAllPersona(assignmentContent.getAgentIdentifier());
            //    this.master  = assignmentContent.getAgentIdentifier();
            final Role role = getOrganizationModel().getRole(assignmentContent.getRoleIdentifier());
            final SpecificationGoal specificationGoal = getOrganizationModel().getSpecificationGoal(assignmentContent
                    .getSpecificationGoalIdentifier());
            final InstanceGoal<?> instanceGoal = specificationGoal.getInstanceGoal(assignmentContent
                    .getInstanceGoalIdentifier(), assignmentContent.getParameter());
            final Assignment assignment = new Assignment(agent, role, instanceGoal);
            this.owner.addAssignment(assignment);
        }
    }

    protected void processDeAssignment(final IBaseMessage message) {
        final Object content = message.getContent();
        if (content instanceof AssignmentContent) {
            final AssignmentContent assignmentContent = (AssignmentContent) content;

            if (debug) LOG.debug("processDeAssignment() {}",
                    String.format("De-Assignment: %s", assignmentContent));

            final Agent<?> agent = (Agent<?>) this.owner.getUniqueIdentifier();
            final Role role = getOrganizationModel().getRole(assignmentContent.getRoleIdentifier());
            final SpecificationGoal specificationGoal = getOrganizationModel().getSpecificationGoal(assignmentContent
                    .getSpecificationGoalIdentifier());
            final InstanceGoal<?> instanceGoal = specificationGoal.getInstanceGoal(assignmentContent
                    .getInstanceGoalIdentifier(), assignmentContent.getParameter());
            final Assignment assignment = new Assignment(agent, role, instanceGoal);
            this.getOwner().addDeAssignment(assignment);
        }
    }

    protected void processGoalModification(final IBaseMessage message) {
        final Object content = message.getContent();
        if (content instanceof ModificationContent) {
            final ModificationContent modificationContent = (ModificationContent) content;

            LOG.debug("processGoalModification() - {}", String.format("Modification: %s", modificationContent));

            final SpecificationGoal specificationGoal = getOrganizationModel().getSpecificationGoal
                    (modificationContent.getSpecificationGoalIdentifier());
            final InstanceGoal<InstanceParameters> instanceGoal = specificationGoal.getInstanceGoal
                    (modificationContent.getInstanceGoalIdentifier(), modificationContent.getInstanceParameters());
            this.getOwner().addGoalModification(instanceGoal);
        }
    }

    public IConnectionGuidelines getConnectionGuidelines(InstanceGoal<?> instanceGoal) {
        return null;
    }

    /**
     Gets the set of local registered prosumer agents.
     @param allAgents - the set of all agents registered in this organization
     @return - the set of all prosumer agents registered in this local organization (does not include
     other types of agents such as forecasters, etc)
     */
    public Set<Agent<?>> getLocalRegisteredProsumers(Set<Agent<?>> allAgents) {
        // get the list of registered prosumer peer agents in the local organization

        LOG.debug("Number of all agents found in the ParticipateInOrganizationCapability is {}", allAgents.size());

        final Set<Agent<?>> prosumers = new HashSet<>();
        Iterator<Agent<?>> it = allAgents.iterator();

        final Class<?> capabilityClass = AssessReactivePowerQualityCapability.class;
        final ClassIdentifier capabilityIdentifier = new ClassIdentifier(capabilityClass);

        while (it.hasNext()) {
            Agent<?> agent = it.next();
            LOG.debug("Checking registered persona {} for AssessReactivePowerQualityCapability", agent.toString());
            if (agent.getPossesses(capabilityIdentifier) != null) {
                prosumers.add(agent);
                LOG.debug("Agent {} added to local prosumers list", agent.toString());
            }
        }
        return prosumers;
    }

    public Organization getOrganizationModel() {
        return iOrganizationModel;
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
        if (debug) LOG.debug("Need {} of {} registrations to fully connect to sub holons.", stillNeeded, tot);
        return unregistered;
    }

    /**
     Get the parameters from this instance goal and use them to set the goal-specific guidelines for any parent
     connections.
     @param instanceGoal - this instance of the specification goal
     */
    public synchronized void initializeParentConnections(InstanceGoal<?> instanceGoal) {
        if (debug) LOG.debug("Initializing connections to  super holons from goal: {}.", instanceGoal);

        // Get the parameter values from the existing active instance goal
        final InstanceParameters params = Objects.requireNonNull((InstanceParameters) instanceGoal.getParameter());
        if (debug) LOG.debug("Initializing super holon connections params: {}.", params);
        IConnections parentConnections = (IConnections) params.getValue(StringIdentifier.getIdentifier("parentConnections"));
        this.setParentConnections(parentConnections);
        if (debug)
            LOG.debug("{} authorized connections to super holons.", parentConnections.getListConnectionGuidelines().size());
    }

    public synchronized void setParentConnections(IConnections parentConnections) {
        this.parentConnections = parentConnections;
    }

    public synchronized boolean isRegistered() {
        return this.isRegistered;
    }

    public synchronized boolean sendExternal(final IBaseMessage<?> message) {
        Objects.requireNonNull(message, "ERROR: Can't send a null reasoning messages.");
        LOG.debug("  entering send local messages externally from {} to {}", message.getLocalSender(), message.getLocalReceiver());
        String routingKey = message.getLocalReceiver().toString();  // e.g. N43inN43
        MessagingManager.declareAndBindConsumerQueue(messagingFocus, routingKey + QUEUE_PURPOSE);
        try {
            byte[] messageBodyBytes = message.serialize();
            LOG.info("Sending message to routing key: {}", routingKey);
            channel.basicPublish(MessagingManager.getExchangeName(messagingFocus), routingKey, MessageProperties.PERSISTENT_TEXT_PLAIN, messageBodyBytes);
            return true;
        } catch (Exception e) {
            LOG.error("ERROR send() messages {} from {}. ", message.toString(),
                    message.getRemoteSender());
            return false;
        }
    }

    public synchronized void setRegistered(boolean isRegistered) {
        this.isRegistered = isRegistered;
    }
}
