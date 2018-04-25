package edu.ksu.cis.macr.ipds.primary.capabilities.participate;


import edu.ksu.cis.macr.aasis.agent.cc_message.IBaseMessage;
import edu.ksu.cis.macr.aasis.agent.cc_message.participate.ParticipateMessage;
import edu.ksu.cis.macr.aasis.agent.cc_message.participate.ParticipatePerformative;
import edu.ksu.cis.macr.aasis.agent.cc_p.IBaseControlComponentSlave;
import edu.ksu.cis.macr.aasis.agent.persona.IPersona;
import edu.ksu.cis.macr.aasis.simulator.player.IPlayable;
import edu.ksu.cis.macr.aasis.simulator.player.PlayableCapability;
import edu.ksu.cis.macr.aasis.spec.OrganizationFocus;
import edu.ksu.cis.macr.ipds.config.RunManager;
import edu.ksu.cis.macr.ipds.primary.persona.AbstractBaseControlComponent;
import edu.ksu.cis.macr.obaa_pp.cc.om.IOrganizationModel;
import edu.ksu.cis.macr.obaa_pp.cc_message.AssignmentContent;
import edu.ksu.cis.macr.obaa_pp.cc_message.ListEventsContent;
import edu.ksu.cis.macr.obaa_pp.cc_message.OrganizationEventContent;
import edu.ksu.cis.macr.obaa_pp.cc_message.RegistrationContent;
import edu.ksu.cis.macr.obaa_pp.events.IOrganizationEvent;
import edu.ksu.cis.macr.obaa_pp.events.OrganizationEvents;
import edu.ksu.cis.macr.organization.model.Agent;
import edu.ksu.cis.macr.organization.model.Assignment;
import edu.ksu.cis.macr.organization.model.Capability;
import edu.ksu.cis.macr.organization.model.identifiers.StringIdentifier;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
/**
 * Adds the organization-specific functionality needed for registering and
 * participating in the agent's internal organization.
 */
public class Participant extends AbstractBaseControlComponent implements IBaseControlComponentSlave {
    private static final Logger LOG = LoggerFactory.getLogger(Participant.class);

    /*
    The time an agent will wait to re-send registration when sending fails. Can be set by the user.
     */
    private static int retry_delay_ms;

    /*
    The time an agent will wait to get a confirmation before moving back to registration and re-starting the process. Can be set by the user.
     */
    private static int confirmation_timeout_ms;

    private static final Boolean debug =  false;
    protected boolean reorganize = false;
    protected Long startTime = null;
    protected PlayableCapability playerCapability;
    protected Queue<List<IOrganizationEvent>> queuedOrganizationEvents = new ConcurrentLinkedQueue<List<IOrganizationEvent>>();
    protected String masterOrgName;
    protected ExecutionState state = ExecutionState.INITIALIZING;
    protected UniqueIdentifier master = null;



    /**
     * A subagent participant registers with the local control component self persona.
     *
     * @param name      - participant name
     * @param persona   - registering persona
     * @param knowledge - the XML knowledge about the organization
     * @param focus     - an Enum defining the focus of the organization
     */
    public Participant(final String name, final IPersona persona, final Element knowledge, final OrganizationFocus focus) {
        super(name, persona, knowledge, focus);
        LOG.info("Entering constructor Participant( name={}, persona={}, knowledge={}, focus={}.", name, persona, knowledge, focus);
        this.owner = Objects.requireNonNull(persona, "Error: Cannot create Participant with null persona EC .");

        retry_delay_ms = RunManager.getParticipantRetryDelay_ms();
        confirmation_timeout_ms = RunManager.getParticipantTimeout_ms();

        try {
            this.playerCapability = new PlayableCapability(this.getPersonaExecutionComponent().getUniqueIdentifier().toString());
            this.organizationEvents = new OrganizationEvents(this.getPersonaExecutionComponent().getUniqueIdentifier()
                    .toString());
            if (debug) LOG.debug("Created org events for control component master: {}.", name);

            this.masterOrgName = getSelfOrganizationFromPersonaName(persona.getUniqueIdentifier().toString());
            LOG.info("Participating in internal agent organization {}.", masterOrgName);
        } catch (Exception e) {
            LOG.error("Error: The subagent participant needs to get the simulation starting time from the Run Manager.");
            System.exit(-88);
        }
    }





    public synchronized String getSelfOrganizationFromPersonaName(String personaName) {
        // e.g. H44_FinN43
        String org = "";
        String selfOrg = "";
        String agentName = "";
        // if in an organization, find agentName and externalOrgName
        if (personaName.contains("in")) {
            org = personaName.substring(personaName.indexOf("in") + 2);
            agentName = personaName.substring(0, personaName.indexOf("in"));
            LOG.info("For persona {}, the agent name = {} and the external org name is {}.", personaName, agentName, org);
            if (agentName.endsWith("A")) agentName = agentName.substring(0, agentName.length() - 1);
            selfOrg = "self" + agentName;
        }
        // if I'm a self persona, then I must be in the self organization
        else if (personaName.contains("self")) {
            selfOrg = personaName;
        }
        // otherwise, I'm also not in an external org, so assume I'm in my own self org
        else {
            selfOrg = "self" + personaName;
        }
        return selfOrg;
    }

    public synchronized IPlayable getPlayerCapability() {
        return playerCapability;
    }

    @Override
    public  void executeControlComponentPlan() {
        if (debug)   LOG.info("EXECUTING Internal Subagent Participant plan. STATE={} under {}", this.state, this.masterOrgName);
        if (this.playerCapability.getPaused()) {
            if (debug) LOG.debug("PAUSED In CC SLAVE {} with master org {}", this.state, this.masterOrgName);
            return;
        }
        //waitForUserToAdvance();
        // this.playerCapability.step();
        switch (this.state) {
            case INITIALIZING:
                doInitialization();
                break;
            case REGISTERING:
                doRegistration();
                break;
            case RETRYING_REGISTRATION:
                doRegistrationRetry();
                break;
            case CONFIRMING_REGISTRATION:
                doRegistrationConfirmation();
                break;
            case TIMING_OUT_DURING_CONFIRMATION:
                doRegistrationConfirmationTimeout();
                break;
            case WORKING:
                doWork();
                break;
            default:
                throw new IllegalArgumentException("Undefined State");
        }
    }

    @Override
    public synchronized void updateAgentInformation() {
        final Agent<?> agent = getOrganizationModel().getAgent(getPersonaExecutionComponent().getUniqueIdentifier());
        if (agent != null) {
            final Map<UniqueIdentifier, Double> capabilities = new HashMap<>();
            for (final Capability capability : getPersonaExecutionComponent().getCapabilities()) {
                final double score = getPersonaExecutionComponent().getCapabilityScore(capability);
                // Updated DMC - 10/6/2013 - to handle the case when the agent has a capability (score = 1) but the
                // capability is not needed (possesses = 0.0)
                final double agentPossessesScore = agent.getPossessesScore(capability.getIdentifier());
                if (score != agentPossessesScore && agentPossessesScore != 0.0 && score == 1.0) {
                    agent.setPossessesScore(capability.getIdentifier(), score);
                }
            }
            if (debug) LOG.debug("updateAgentInformation() AttributeAgent: {} with {}", agent.getIdentifier(), agent.getPossessesSet());
            final RegistrationContent registrationContent = new RegistrationContent(agent.getIdentifier(),
                    capabilities);
            internalOrganizationCommunicationCapability.sendLocal(new ParticipateMessage(getPersonaExecutionComponent()
                    .getUniqueIdentifier(), this.master, ParticipatePerformative.AGENT_UPDATE_INFORMATION,
                    registrationContent));
        }
    }

    /**
     * The {@code content} that will be channeled by extensions.
     *
     * @param content the {@code content} to be passed along the {@code ICommunicationChannel}.
     */
    @Override
    public void channelContent(final Object content) {
        internalOrganizationCommunicationCapability.channelContent(content);
    }

    protected synchronized void doInitialization() {
        if (debug) LOG.debug("Entering doInitialization()");
        Agent<UniqueIdentifier> ecAgent = initializeECAgent();
        this.state = ExecutionState.REGISTERING;
    }

    protected synchronized void doRegistration() {
        if (debug) LOG.debug("Entering doRegistration()");
        IOrganizationModel knowledge = getOrganizationModel();
        Objects.requireNonNull(knowledge);

        final Agent<?> agent = knowledge.getAgent(getPersonaExecutionComponent().getUniqueIdentifier());
        Objects.requireNonNull(agent);

        Map<UniqueIdentifier, Double> capabilities = new HashMap<>();

        /* verify that ecAgent entity is updated with the latest scores */
        for (final Capability capability : getPersonaExecutionComponent().getCapabilities()) {
            Objects.requireNonNull(capability);
            final double score = getPersonaExecutionComponent().getCapabilityScore(capability);
            // Updated DMC - 10/6/2013 - to handle the case when the agent has a capability (score = 1) but the
            // capability is not needed (possesses = 0.0)
            final double agentPossessesScore = agent.getPossessesScore(capability.getIdentifier());
            if (score != agentPossessesScore && agentPossessesScore != 0.0 && score == 1.0) {
                agent.setPossessesScore(capability.getIdentifier(), score);
            }
            capabilities.put(capability.getIdentifier(), score);
        }
        Set<Capability> caps = Objects.requireNonNull(agent.getPossessesSet(), "Error: Agent has no capabilities.");
        UniqueIdentifier id = agent.getIdentifier();
        if (debug)  LOG.debug("In doRegistration. Agent= {}.  Possesses={}. ", id, caps);

        final RegistrationContent registrationContent = new RegistrationContent(getPersonaExecutionComponent()
                .getUniqueIdentifier(), capabilities);
        if (debug) LOG.debug("registration content: {}", registrationContent);
        if (debug) LOG.debug("My org is {}.", this.masterOrgName);

        String host = "";
        try {
            host = this.masterOrgName;
            host = host.replace("self", "");
        } catch (Exception ex) {
            LOG.error("Error getting host.");
            System.exit(-44);
        }
        if (debug) LOG.debug("running on host {}.", host);
        if (debug) LOG.debug("in organization {} running on node {}",  this.masterOrgName, host);

        this.startTime = System.currentTimeMillis();  // used to see if we timeout

       LOG.info("ATTEMPTING Participant doRegistration() org={} node={} ", this.masterOrgName, host);
        final ParticipateMessage reasoningMessage = new ParticipateMessage(getPersonaExecutionComponent()
                .getUniqueIdentifier(), StringIdentifier.getIdentifier("broadcast"),
                ParticipatePerformative.BROADCASTING_AGENT_REGISTRATION, registrationContent);
        if (debug) LOG.debug("reasoning message: {}", reasoningMessage);

        if (reasoningMessage != null) {
            try {
                if (this.internalOrganizationCommunicationCapability.broadcastIncludeSelf(reasoningMessage)) {
            /* broadcast was successful, so proceed to next state */
                    this.state = ExecutionState.CONFIRMING_REGISTRATION;
                    LOG.info("EVENT: SUCCESSFUL_REGISTRATION_MESSAGE broadcast. {} messages on queue.  Moving to {}.  MESSAGE={}. internal org messages are: {}",
                            this.internalOrganizationCommunicationCapability.getInternalOrganizationMessages().size(),
                            this.state, reasoningMessage, this.internalOrganizationCommunicationCapability.getInternalOrganizationMessages());

                } else {
                    // broadcast has failed, retry again in a bit
                    this.state = ExecutionState.RETRYING_REGISTRATION;
                    LOG.error("failed broadcast of registration message: {}", this.state);
                    System.exit(-4222);
                }
            } catch (Exception e) {
                LOG.error("ERROR: in Participant.doRegistration().", e);
                System.exit(-44);
            }
        }
    }

    protected synchronized void doRegistrationConfirmation() {
        if (debug) LOG.debug("Entering doRegistrationConfirmation()");
        if (this.internalOrganizationCommunicationCapability.messages() > 0) {
            final ParticipateMessage message = (ParticipateMessage) this.internalOrganizationCommunicationCapability.receiveLocal();
            LOG.debug("In External CC SLAVE confirming reg. Message perf = {}", message.getPerformativeType());
            switch (message.getPerformativeType()) {
                case BROADCASTING_AGENT_REGISTRATION:
                    // possible to receive ecAgent registrations from other agents while waiting for a confirmation, so ignore it
                    LOG.debug(" ignoring in order to confirm: {}", message);
                    this.state = ExecutionState.TIMING_OUT_DURING_CONFIRMATION;
                    break;
                case AGENT_REGISTRATION_CONFIRMATION:
                    LOG.info("EVENT: ORGANIZATION_PARTICIPANT_REGISTERED.  received registration confirmation. content = {}",  message.getContent());
                    final Object content = message.getContent();
                    if (content instanceof UniqueIdentifier) {
                        LOG.debug("Retrieved message content =({})", content.toString());
                        this.master = (UniqueIdentifier) content;
                        LOG.debug("In registration confirmation external master =({})", this.master);
                        this.setLocalMaster(this.master);
                        LOG.debug("doRegistrationConfirmation()confirmation from {}. Sending master conf received message.", this.master);
                        this.internalOrganizationCommunicationCapability.sendLocal(new ParticipateMessage(getPersonaExecutionComponent()
                                .getUniqueIdentifier(), this.master,
                                ParticipatePerformative.AGENT_REGISTRATION_CONFIRMATION_RECEIVED, null));
                        LOG.debug("MOVE TO WORKING.");
                        this.state = ExecutionState.WORKING;
                    }
                    break;
                default:
                    // other performatives should NOT be received at all because they are directed and this ecAgent CANNOT have any of those
                    LOG.error("Unacceptable performative in doRegistrationConfirmation: {}. content = {}", message.getPerformativeType(), message.getContent().toString());
                    System.exit(-19);
                    //     throw new IllegalArgumentException(String.format("Unacceptable Performatives \"%s\"",
                    //             message.getPerformativeType()));
            }
        } else {
            if (debug) LOG.debug("In CC SLAVE confirming registration, but no messages to process.");
            this.state = ExecutionState.TIMING_OUT_DURING_CONFIRMATION;
        }
    }

    protected synchronized void doRegistrationConfirmationTimeout() {
        if (debug) LOG.debug("Entering doRegistrationConfirmationTimeout()");
        // no confirmation received
        final long currentTime = System.currentTimeMillis();
        final long waited = currentTime - this.startTime;
        if (waited > confirmation_timeout_ms) {
            //waited long enough for a confirmation
            if (debug) LOG.debug("doRegistrationConfirmationTimeout() {}",
                    String.format("Waiting Done: ((%d - %d) = %d) > %d",
                            currentTime, this.startTime, currentTime - this.startTime, confirmation_timeout_ms));
            if (debug) LOG.debug("Going back to registering.");
            this.state = ExecutionState.REGISTERING;
        } else {
            if (debug) LOG.debug("Going to confirming registration.");
            this.state = ExecutionState.CONFIRMING_REGISTRATION;
        }
    }

    protected synchronized void doRegistrationRetry() {
        if (debug) LOG.debug("Entering doRegistrationRetry()");
        // waiting to retry registration
        final long currentTime = System.currentTimeMillis();
        final long timeDifference = currentTime - this.startTime;

        if (timeDifference > retry_delay_ms) {
            if (debug) LOG.debug("Waited for getParticipantRetryDelay_ms. Will try again. doRegistrationRetry() {}",
                    String.format("Waiting Done: ((%d - %d) = %d) > %d", currentTime,
                            this.startTime, timeDifference, retry_delay_ms));
            this.state = ExecutionState.REGISTERING;
        } else { // continue to wait
            if (debug) LOG.debug("SELF CCS doRegistrationRetry() {}",
                    String.format("Continue Waiting: ((%d - %d) = %d) <= %d",
                            currentTime, this.startTime, timeDifference, retry_delay_ms));
        }
    }

    protected synchronized void doWork() {
        LOG.debug("Entering doWork().");
        if (this.playerCapability.getPaused()) {
            if (debug) LOG.debug("PAUSED In CC SLAVE STATE doWork()");
            return;
        }
        // if step mode is on, wait for user, otherwise just continue
        //waitForUserToAdvance(this.playerCapability.getStepMode(),0 );
        //this.playerCapability.step();
        while (this.organizationEvents.numberOfQueuedEvents() > 0) {
            final List<IOrganizationEvent> organizationEvents = this.organizationEvents.getNextEvent();
            if (debug) LOG.debug("numberOfQueuedEvents {}", String.format("processing OrganizationEvent:" +
                    " %s", organizationEvents));
            processEvent(organizationEvents);
        }
        if (debug) LOG.debug("Checking internal messages.");
        while (internalOrganizationCommunicationCapability.messages() > 0) {
            final IBaseMessage<?> message = internalOrganizationCommunicationCapability.receiveLocal();
            if (message instanceof edu.ksu.cis.macr.aasis.agent.cc_message.participate.IParticipateMessage) {
                processMessage((ParticipateMessage) message);
            }
        }
        triggerReorganization();
    }

    protected synchronized void processEvent(final List<IOrganizationEvent> organizationEvents) {
        if (debug) LOG.debug("Entering processEvent(organizationEvents={})", organizationEvents);
        if (this.master == null) {
            // this code should rarely be executed if it is executed at all.
            final boolean added = this.queuedOrganizationEvents.offer(organizationEvents);
            if (!added) {
                // if (debug) LOG.debug("processEvent() {}", String.format(
                //         "Events Queue Full: %d",
                //         this.queuedOrganizationEvents.size()));
            }
        } else {
            //if there is a queue of events that are to be sent, send them first
            while (!this.queuedOrganizationEvents.isEmpty()) {
                final List<IOrganizationEvent> priorEvents = this.queuedOrganizationEvents.poll();
                if (debug) LOG.debug("Sending Previously Queued OrganizationEvent: {}", priorEvents);
                final ListEventsContent content = new ListEventsContent(OrganizationEventContent.convert(priorEvents));
                final ParticipateMessage reasoningMessage = new ParticipateMessage(getPersonaExecutionComponent()
                        .getUniqueIdentifier(), this.master, ParticipatePerformative.GOAL_MODEL_EVENT, content);
                internalOrganizationCommunicationCapability.sendLocal(reasoningMessage);
            }
            if (debug) LOG.debug("processEvent() Sending Current OrganizationEvent:{}", organizationEvents);
            final ListEventsContent content = new ListEventsContent(OrganizationEventContent.convert
                    (organizationEvents));
            this.internalOrganizationCommunicationCapability.sendLocal(new ParticipateMessage(getPersonaExecutionComponent()
                    .getUniqueIdentifier(), this.master, ParticipatePerformative.GOAL_MODEL_EVENT, content)); // added to force local send
        }
    }

    protected synchronized void processMessage(final ParticipateMessage message) {
        if (debug) LOG.debug("Entering processMessage(message={})", message);
        if (message != null) {
            switch (message.getPerformativeType()) {
                case BROADCASTING_AGENT_REGISTRATION:
                    if (debug) LOG.debug("processMessage() {}", String.format("Discarding Agent Registration: %s",
                            message.toString()));
                    break;
                case AGENT_REGISTRATION_CONFIRMATION:
                    if (debug) LOG.debug("processMessage() {}", String.format("Discarding Agent Registration: %s",
                            message.toString()));
                    break;
                case ASSIGNMENT:
                    LOG.info("EVENT: ASSIGNMENT_RECEIVED. Name = {} received assignment. content = {}", this.getOwner().getUniqueIdentifier().toString(), message.getContent());
                    processAssignment(message);
                    break;
                case DEASSIGNMENT:
                    processDeAssignment(message);
                    break;
                case GOAL_MODIFICATION:
                    processGoalModification(message);
                    break;
                default:
                    // other performatives should NOT be received as they are directed and this CANNOT have any of those
                    LOG.error("Unacceptable performative in processMessage: {}. content = {}", message.getPerformativeType(), message.getContent().toString());
             //       System.exit(-19);
//                    throw new IllegalArgumentException(String.format("Unacceptable Performatives \"%s\"",
//                            message.getPerformativeType()));
            }
        }
    }

    protected synchronized void triggerReorganization() {
        if (debug) LOG.debug("Entering triggerReorganization(})");
        if (this.reorganize) {
            this.reorganize = false;
            reorganize();
        }
    }


    protected void reorganize() {
        if (debug) LOG.debug("Entering reorganize(})");
        final Collection<Assignment> assignments = getReorganizationAlgorithm().reorganize(getOrganizationModel(),
                getOrganizationModel().getInstanceGoals(), getOrganizationModel().getAgents());
        if (assignments == null) {
                LOG.info("reorganize() {}", "Reorganization Algorithm unreliable - assignment collection is null");
            return;
        }
        if (assignments.isEmpty()) { // no new ec
            if (debug) LOG.debug("reorganize() {}", "No New Assignments");
        } else { // there are new ec
            if (debug) LOG.debug("{} New Assignments: {}", assignments.size(), assignments);
            getOrganizationModel().addAssignments(assignments);
            for (final Assignment assignment : assignments) {
                LOG.info("sending assignment ParticipateMessage. {} new Assignments: {}  {}", assignments.size(), assignments,
                                assignment.getAgent().getIdentifier());
                internalOrganizationCommunicationCapability.sendLocal(new ParticipateMessage(getPersonaExecutionComponent()
                        .getUniqueIdentifier(), assignment.getAgent().getIdentifier(),
                        ParticipatePerformative.ASSIGNMENT, AssignmentContent.createAssignmentContent(assignment)));
            }
        }
    }



    protected enum ExecutionState {
        CONFIRMING_REGISTRATION, INITIALIZING, REGISTERING, RETRYING_REGISTRATION, TIMING_OUT_DURING_CONFIRMATION,
        WORKING
    }
}
