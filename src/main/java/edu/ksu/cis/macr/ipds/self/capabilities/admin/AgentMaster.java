package edu.ksu.cis.macr.ipds.self.capabilities.admin;


import edu.ksu.cis.macr.aasis.agent.cc_m.IPersonaControlComponentMaster;
import edu.ksu.cis.macr.aasis.agent.cc_message.participate.IParticipateMessage;
import edu.ksu.cis.macr.aasis.agent.cc_message.participate.ParticipateMessage;
import edu.ksu.cis.macr.aasis.agent.cc_message.participate.ParticipatePerformative;
import edu.ksu.cis.macr.aasis.agent.ec_cap.IOrganizationCommunicationCapability;
import edu.ksu.cis.macr.aasis.agent.persona.IPersona;
import edu.ksu.cis.macr.aasis.simulator.player.IPlayable;
import edu.ksu.cis.macr.aasis.simulator.player.PlayableCapability;
import edu.ksu.cis.macr.aasis.spec.OrganizationFocus;
import edu.ksu.cis.macr.goal.model.InstanceParameters;
import edu.ksu.cis.macr.goal.model.InstanceTreeChanges;
import edu.ksu.cis.macr.goal.model.InstanceTreeModifications;
import edu.ksu.cis.macr.goal.model.SpecificationEvent;
import edu.ksu.cis.macr.ipds.primary.persona.AbstractBaseControlComponent;
import edu.ksu.cis.macr.ipds.self.organizer.SelfReorganizationAlgorithm;
import edu.ksu.cis.macr.obaa_pp.cc.om.IOrganizationModel;
import edu.ksu.cis.macr.obaa_pp.cc_message.*;
import edu.ksu.cis.macr.obaa_pp.events.IOrganizationEvent;
import edu.ksu.cis.macr.obaa_pp.events.OrganizationEvent;
import edu.ksu.cis.macr.obaa_pp.events.OrganizationEventType;
import edu.ksu.cis.macr.obaa_pp.events.OrganizationEvents;
import edu.ksu.cis.macr.organization.model.*;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;


/**
 * Provides organizational assignments for persona (subagents) running on a common device.
 */
public class AgentMaster extends AbstractBaseControlComponent implements IPersonaControlComponentMaster {
    private static final Logger LOG = LoggerFactory.getLogger(AgentMaster.class);
    private static final Boolean debug =  true;
    protected final Map<UniqueIdentifier, Agent<UniqueIdentifier>> agentQueue = new ConcurrentHashMap<>();

    protected Set<Role> initialRoles;
    protected PlayableCapability playerCapability;
    private ExecutionState state = ExecutionState.INITIALIZING;
    private boolean doneWaitingForInitialAssignments;


    /**
     * @param name      - the agent / organization name
     * @param persona   - the subagent
     * @param knowledge - the XML knowledge about the organization
     * @param focus     - the Enum defining what the focus of the organization is
     */
    public AgentMaster(final String name, final IPersona persona, final Element knowledge, final OrganizationFocus focus) {
        super(name, persona, knowledge, focus);
        LOG.info("Entering constructor AgentMaster( name={}, persona={}, knowledge={}, focus={}.", name, persona, knowledge, focus);
        if (debug) LOG.debug("Done constructing abstract base control component.");
        if (debug) LOG.info("Creating control component master: {}.", name);

        this.owner = Objects.requireNonNull(persona, "Error: Cannot create CCMaster with null EC.");
        if (debug)LOG.info("Creating control component master: {}.", name);
       if (debug) LOG.debug("The ccm.execution_component.create().getGoalParameterValues() are {}",
                persona.getOrganization().getGoalParameterValues());
        this.initialRoles = null;
        doneWaitingForInitialAssignments = false;
        try {
            this.playerCapability = new PlayableCapability(this.getPersonaExecutionComponent().getUniqueIdentifier().toString());
            this.organizationEvents = new OrganizationEvents(this.getPersonaExecutionComponent().getUniqueIdentifier()
                    .toString());
            if (debug) LOG.debug("Created org events for control component master: {}.", name);




        } catch (Exception e) {
            LOG.error("ERROR in Control Component Master constructor. {}", e.getMessage());
            System.exit(-55);
        }
        if (debug) LOG.debug("Setting reorg algo for {}", name);
        this.setReorganizationAlgorithm(SelfReorganizationAlgorithm.createReorganizationAlgorithm(name));
        LOG.info("The Agent Master reorg alogorithm is set to {}.", this.getReorganizationAlgorithm().getClass().getSimpleName());
        if (debug) LOG.info("Initializing goals...............................................................");
        final InstanceParameters topParams = getTopGoalInstanceParameters();
        InstanceTreeChanges changeList = getInitialGoalModelChangeList(topParams);
        updateInitialActiveGoals(changeList);
        setInitialRoles();
        if (debug) LOG.info("Done initializing goals (exiting AgentMaster constructor and continuing to create persona)..........................................................");


        LOG.info("Exiting constructor AgentMaster: this={}",this.toString());
    }

    /**
     * Replaces the current {@code IOrganizationModel} of this {@code ControlComponent}
     * with the given {@code IOrganizationModel}.
     *
     * @param orgModel the new {@code IOrganizationModel} to replaces the existing one.
     */
    @Override
    public void setOrganizationModel(final IOrganizationModel orgModel) {
        if (debug)
            LOG.debug("Setting organization model={}. NUM CAPABILITIES={}, NUM AGENTS={}, NUM GOALS={}", orgModel.getCapabilities().size(), orgModel.getAgents().size(), orgModel.getInstanceGoals().size());
        this.organizationModel = orgModel;
    }

    private boolean areThereEnoughAgentsForInitialAssignments() {
        if (debug) LOG.debug("Entering areThereEnoughAgentsForInitialAssignments()");
        if (debug) LOG.debug("Agents={}.", organizationModel.getAgents());
        if (debug) LOG.debug("Roles Left Before Removal={}.", this.initialRoles);
        Set<Role> rolesToRemove = new HashSet<>();
        if (this.initialRoles == null) {
            setInitialRoles();
            if (this.initialRoles == null) {
                LOG.error("CCM initial roles are still null.");
                System.exit(-34);
            }
        }
        for (final Role role : initialRoles) {

            for (final InstanceGoal<?> goal : organizationModel.getInstanceGoals()) {
                if (role.achieves(goal)) {
                    for (final Agent<?> agent : organizationModel.getAgents()) {

                        Set<Capability> requiresSet = role.getRequiresSet();
                        Set<Capability> possessesSet = agent.getPossessesSet();

                        LOG.debug("agent = {}, agent.possessSet={}", agent,possessesSet);
                        LOG.debug("role  = {}, role.requiresSet={}", role,requiresSet);

                        double goodness = role.goodness(agent, goal, new HashSet<Assignment>());
                        LOG.debug("goodness  = {}",goodness);
                        if (goodness > RoleGoodnessFunction.MIN_SCORE) {
                            rolesToRemove.add(role);
                        }
                    }
                }
            }
        }
        this.initialRoles.removeAll(rolesToRemove);
        if (debug) LOG.debug("Initial roles empty (Can all roles be staffed)? {}.", this.initialRoles.isEmpty());
        if (debug) LOG.debug("Can one role be staffed? {}.",rolesToRemove.size() > 0);
       // return this.initialRoles.isEmpty();
        return (rolesToRemove.size() > 0 );

    }

    /**
     * The {@code content} that will be channeled by extensions.
     *
     * @param content the {@code content} to be passed along the {@code ICommunicationChannel}.
     */
    @Override
    public void channelContent(final Object content) {
        LOG.debug("Entering channelContent(). Gets raw content and adds message. content={}", content);
        internalOrganizationCommunicationCapability.channelContent(content);
    }


    private void checkAgent(final Agent<?> agent) {
        LOG.info("Entering checkAgent(). agent={}", agent);

        final Collection<Assignment> assignmentsToRemove = new ArrayList<>();
        for (final Assignment assignment : getOrganizationModel().getAssignmentsOfAgent(agent.getIdentifier())) {
            final double goodness = assignment.getRole().goodness(agent, assignment.getInstanceGoal(),
                    new HashSet<>());
            if (goodness <= RoleGoodnessFunction.MIN_SCORE) {
                /* ecAgent is unable to work on the assignment */
                final IParticipateMessage message = new ParticipateMessage(getPersonaExecutionComponent().getUniqueIdentifier
                        (), assignment.getAgent().getIdentifier(), ParticipatePerformative.DEASSIGNMENT,
                        AssignmentContent.createAssignmentContent(assignment)
                );
                this.internalOrganizationCommunicationCapability.sendLocal(message);
                assignmentsToRemove.add(assignment);
            }
        }
        for (final Assignment assignment : assignmentsToRemove) {
            getOrganizationModel().removeAssignment(assignment.getIdentifier());
        }
    }

    private void doInitialization() {
        LOG.info("Entering doInitialization(). ");
        initializeECAgent();
        if (debug) LOG.debug("After initializeECAgent Agents={}.", organizationModel.getAgents());
      //  this.state = ExecutionState.WORKING;
     this.state = ExecutionState.WAITING_FOR_AGENTS_FOR_INITIAL_ASSIGNMENTS;
        LOG.info("Exiting doInitialization(). ");
    }

    private void doWaitingForAgentsForInitialAssignments() {
        LOG.info("Entering doWaitingForAgentsForInitialAssignments.");
        // determine if there are enough initial agents to proceed with
        // reorganization or to continue waiting for more agents to register
        this.doneWaitingForInitialAssignments = areThereEnoughAgentsForInitialAssignments();
        if (this.doneWaitingForInitialAssignments) {
            LOG.info("doneWaitingForInitialAssignments... moving to working state.");
            triggerReorganization(true);
            this.state = ExecutionState.WORKING;
        } else {
            if (debug) LOG.debug("Checking internal messages.");
            while (this.internalOrganizationCommunicationCapability.messages() > 0) {
                    if (debug)
                    LOG.debug("There are {} internal messages.", this.internalOrganizationCommunicationCapability.messages());
                ParticipateMessage message = (ParticipateMessage) this.internalOrganizationCommunicationCapability.receiveLocal();
                processMessage(message);
            }
        }
    }

    private void doWork() {
        LOG.debug("Entering doWork.");
        if (debug) LOG.debug("AgentMaster doWork - the {} active instance goals are: {}",
                this.getGoalModel().getActiveInstanceGoals().size(), this.getGoalModel().getActiveInstanceGoals());
        if (debug)
            LOG.debug("AgentMaster doWork - there are {} organization events. ", this.organizationEvents.numberOfQueuedEvents());
        if (debug)
            LOG.debug("AgentMaster doWork - there are {} INTERNAL messages.", this.internalOrganizationCommunicationCapability.messages());
        if (debug)
            LOG.debug("AgentMaster doWork - there are {} getNumberOfMessages.", organizationCommunicationCapability.getOrganizationMessages().size());
        if (debug)
            LOG.debug("Master doWork - there are {} REMOTE messages.", organizationCommunicationCapability.messages());

        boolean reorgNeeded = false;
        Map<UniqueIdentifier, Agent<UniqueIdentifier>> startingQueue = this.agentQueue;
        /* process all events */
        while (this.organizationEvents.numberOfQueuedEvents() > 0) {
            final List<IOrganizationEvent> organizationEvents = this.organizationEvents.getNextEvent();
            for (final IOrganizationEvent organizationEvent : organizationEvents) {
                if (processEvent(organizationEvent)) {
                    reorgNeeded = true;
                }
            }
        }
        if (debug) LOG.debug("internalCCComm={}", internalOrganizationCommunicationCapability);
        while (this.internalOrganizationCommunicationCapability.messages() > 0) {
            reorgNeeded = true;
            //  final ParticipateMessage message = (ParticipateMessage) selfComm.receive();
            final ParticipateMessage message = (ParticipateMessage) this.internalOrganizationCommunicationCapability.receiveLocal(); // added to force LOCAL
            processMessage(message);
            // if step mode is on, wait for user, otherwise just continue
            // this.playerCapability.step();
            //   waitForUserToAdvance(this.playerCapability.getStepMode(), 0);
        }
        if (reorgNeeded) {
            if (debug) LOG.debug("Doing work, beginning reorg.");
            triggerReorganization(reorgNeeded);
        }
        LOG.debug("Exiting doWork.");
    }

    @Override
    public void executeControlComponentPlan() {
        if (debug) LOG.debug("Entering executeControlComponentPlan(). state={}. ", this.state);
        if (this.playerCapability.getPaused()) {
            if (debug) LOG.debug("Paused in CC master state {}.", this.state);
            return;
        }
        // this.playerCapability.step();
        switch (this.state) {
            case INITIALIZING:
                doInitialization();
                break;
            case WAITING_FOR_AGENTS_FOR_INITIAL_ASSIGNMENTS:
                doWaitingForAgentsForInitialAssignments();
                break;
            case WORKING:
                doWork();
                break;
            default:
                throw new IllegalArgumentException(String.format("Unknown State:%s",
                        this.state));
        }

    }

    private Assignment getAssignment(final UniqueIdentifier agentIdentifier, final UniqueIdentifier roleIdentifier,
                                                  final UniqueIdentifier goalIdentifier) {
        LOG.debug("Entering getAssignment()");
        for (final Assignment assignment : getOrganizationModel().getAssignments()) {
            if (agentIdentifier.equals(assignment.getAgent().getIdentifier()) && roleIdentifier.equals(assignment
                    .getRole().getIdentifier()) && goalIdentifier.equals(assignment.getInstanceGoal().getIdentifier()
            )) {
                return assignment;
            }
        }
        if (debug) LOG.debug("No Assignment Found: {}, {}, {}.", agentIdentifier, roleIdentifier, goalIdentifier);
        return null;
    }

    private Assignment getAssignmentOfInstanceGoal(final InstanceGoal<InstanceParameters> instanceGoal) {
        LOG.info("Entering getAssignmentOfInstanceGoal(). instanceGoal={}", instanceGoal);
        Assignment assignment = null;
        for (final Assignment a : getOrganizationModel().getAssignments()) {
            if (instanceGoal.equals(a.getInstanceGoal())) {
                assignment = a;
            }
        }
        LOG.info("Exiting getAssignmentOfInstanceGoal(). assignment={}", assignment);
        return assignment;
    }

    @Override
    public InstanceTreeChanges getInitialGoalModelChangeList(final InstanceParameters topInstanceParameters) {//
        // initialize the goal model for this organization
        LOG.info("Entering getInitialGoalModelChangeList(). topInstanceParameters={}", topInstanceParameters);

        AtomicReference<InstanceTreeChanges> changeList = new AtomicReference<>(Objects.requireNonNull(getGoalModel().initialize(topInstanceParameters), "Error: The organization's goal model was initially empty. Nothing to do."));

        LOG.info("Goal model for CCM {} initially has {} leaf specification goals.", this.getIdentifier().toString(),
                getGoalModel().getLeafSpecificationGoals().size());
        if (debug) LOG.debug("Instance goal tree initial change list for CCM {} has size={}: {}", this.getIdentifier().toString(),
                changeList.get().getAddedInstanceGoals().size(), changeList.get().getAddedInstanceGoals().iterator().next()
        );
        return changeList.get();
    }

    @Override
    public PlayableCapability getPlayerCapability() {
        return playerCapability;
    }

    @Override
    public synchronized void setPlayerCapability(final IPlayable playerCapability) {
        this.playerCapability = (PlayableCapability)playerCapability;
    }

    @Override
    public synchronized InstanceParameters getTopGoalInstanceParameters() {
        if (debug) LOG.debug("Entering getTopGoalInstanceParameters(). organization={}", this.owner.getOrganization());
        // set the top level goal instance parameters

        Map<UniqueIdentifier, Object> map = Objects.requireNonNull(this.owner.getOrganization().getGoalParameterValues(), "Error: an CCMaster cannot have empty goal parameters.");
        LOG.info("Top goal instance parameters map has {} items  (CCM {})", this.owner.getOrganization()
                .getGoalParameterValues().size(), this.getIdentifier().toString());

        for (final Map.Entry<UniqueIdentifier, Object> entry : map.entrySet()) {
            UniqueIdentifier id = entry.getKey();
            Object value = entry.getValue();
            if (debug) LOG.debug("\tTop goal guidelines: id={}, guideline={}", id.toString(), value);
        }
        final InstanceParameters topInstanceParameters = Objects.requireNonNull(new InstanceParameters(map),
                "Error: top goal instance parameters are required.");
        if (debug) LOG.info("Top goal instance parameters for CCM {}: {}", this.getIdentifier().toString(), topInstanceParameters);
        return topInstanceParameters;
    }

    private synchronized void processAgentGoneEvent(final Object parameter) {
        if (debug) LOG.debug("Entering processAgentGoneEvent(parameter={})", parameter);
        final IAgentGoneContent IAgentGoneContent = (IAgentGoneContent) parameter;
        final UniqueIdentifier agentIdentifier = IAgentGoneContent.getAgentIdentifier();
        final Set<Assignment> assignments = getOrganizationModel().getAssignmentsOfAgent(agentIdentifier);
        for (final Assignment assignment : assignments) {
            getOrganizationModel().removeAssignment(assignment.getIdentifier());
        }
        getOrganizationModel().removeAgent(agentIdentifier);
        if (debug) LOG.debug("Processing agent gone event, beginning reorg.");
        triggerReorganization(true);
    }

    protected synchronized void processAgentRegistration(final IParticipateMessage message) {
        if (debug) LOG.debug("Entering processAgentRegistration(message={})", message);

        final Object content = message.getContent();
        final UniqueIdentifier participant = message.getLocalSender();
        if (content instanceof RegistrationContent) {
            final RegistrationContent registrationContent = (RegistrationContent) content;
            if (debug) LOG.debug("Registration from {} RegContent: {}", participant, registrationContent);

            final Agent<UniqueIdentifier> agent = new AgentImpl<>(registrationContent.getAgentIdentifier());
            for (final Entry<UniqueIdentifier, Double> entry : registrationContent.getCapabilities().entrySet()) {
                if (debug)
                    LOG.debug("Registration from {} Checking capability: {}-{}", participant, entry.getKey(), entry.getValue());
                final Capability capability = getOrganizationModel().getCapability(entry.getKey());
                if (capability != null) {
                    agent.addPossesses(capability, entry.getValue());
                } else {
                    if (debug) LOG.debug("Registration from {} Unknown Capability {}", participant, entry.getKey());
                }
            }
            // TODO include checks for existing agents
            this.agentQueue.put(agent.getIdentifier(), agent);
            if (debug) LOG.debug("agentQueue={}", this.agentQueue);
            this.internalOrganizationCommunicationCapability.sendLocal(
                     new ParticipateMessage(getPersonaExecutionComponent()
                            .getUniqueIdentifier(), registrationContent.getAgentIdentifier(),
                            ParticipatePerformative.AGENT_REGISTRATION_CONFIRMATION,
                            getPersonaExecutionComponent().getUniqueIdentifier()
                    )
            );

            if (debug) LOG.debug("processAgentRegistration() ThisAgent={}: {}",
                    this.getPersonaExecutionComponent().getUniqueIdentifier(), agent.getPossessesSet()
            );
        }
    }

    protected synchronized void processAgentRegistrationConfirmationReceived(final IParticipateMessage message) {
        if (debug) LOG.debug("Entering processAgentRegistrationConfirmationReceived(message={})", message);
        final UniqueIdentifier agentIdentifier = message.getLocalSender();
        final Agent<UniqueIdentifier> agent = this.agentQueue.get(agentIdentifier);
        if (agent != null) {
            getOrganizationModel().addAgent(agent);
            LOG.info("EVENT: ADDED_AGENT={}", agent, agent.getPossessesSet());
        } else {
            LOG.error("Unknown Agent:  {}.", agentIdentifier);
        }
        if (debug) LOG.debug("Processing agent registration confirmation received, beginning reorg.");
        triggerReorganization(true);
    }

    private synchronized void processAgentUpdateInformation(final IParticipateMessage message) {
        if (debug) LOG.debug("Entering processAgentUpdateInformation(message={})", message);
        final Object content = message.getContent();
        if (content instanceof RegistrationContent) {
            final RegistrationContent registrationContent = (RegistrationContent) content;
            if (debug) LOG.debug("processUpdateAgentInformation() {}", String.format(
                    "RegistrationContent: %s", registrationContent));
            final Agent<?> agent = getOrganizationModel().getAgent(registrationContent.getAgentIdentifier());
            for (final Entry<UniqueIdentifier, Double> entry : registrationContent.getCapabilities().entrySet()) {
                final Capability capability = getOrganizationModel().getCapability(entry.getKey());

                final double score = entry.getValue();
                // Updated to handle the case when the agent has a capability (score = 1) but the
                // capability is not needed (possesses = 0.0)
                final double agentPossessesScore = agent.getPossessesScore(capability.getIdentifier());
                if (score != agentPossessesScore && agentPossessesScore != 0.0 && score == 1.0) {
                    agent.setPossessesScore(capability.getIdentifier(), score);
                }
            }
            // when ecAgent updates their information, a check has to be done to
            // ensure the ecAgent is still able to continue working on the current assignment
            checkAgent(agent);
        }
    }

    private synchronized boolean processEvent(final IOrganizationEvent organizationEvent) {
        if (debug) LOG.debug("Entering processEvent(organizationEvent={})", organizationEvent);
        final Object parameter = organizationEvent.getParameters();
        final OrganizationEventType eventType = organizationEvent.getEventType();
        if (debug) LOG.debug("eventType {}", String.format("OrganizationEvent: %s", organizationEvent));
        boolean reOrgNeeded = false;
        switch (eventType) {
            case EVENT:
                processGoalModelEvent(organizationEvent.getInstanceGoal().getIdentifier(),
                        organizationEvent.getSubEvent(), (InstanceParameters) parameter);
                reOrgNeeded = true;
                break;
            case GOAL_MODEL_MODIFICATION:
                processGoalModelModification(organizationEvent.getInstanceGoal().getIdentifier(),
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
                throw new IllegalArgumentException(String.format("Unknown Event Type at processEvent\"%s\"",
                        organizationEvent.getEventType()));
        }
        return reOrgNeeded;
    }

    private synchronized void processEvent(final IParticipateMessage message) {
        if (debug) LOG.debug("Entering processEvent(message={})", message);
        final Object content = message.getContent();
        if (content instanceof ListEventsContent) {
            final ListEventsContent listEventsContent = (ListEventsContent) content;
            for (final OrganizationEventContent organizationEventContent : listEventsContent.getEvents()) {
                final InstanceGoal<?> instanceGoal = getOrganizationModel().getInstanceGoal(organizationEventContent
                        .getInstanceGoalIdentifier());
                final OrganizationEvent organizationEvent = organizationEventContent.toOrganizationEvent(instanceGoal);
                // if it needs a reorganization, just do it - DMC
                if (processEvent(organizationEvent)) {
                    LOG.debug("Processing event, beginning reorg.");
                    triggerReorganization(true);
                }
            }
        }
    }

    protected synchronized void processGoalModelEvent(final UniqueIdentifier goalIdentifier,
                                                      final UniqueIdentifier eventIdentifier,
                                                      final InstanceParameters parameters) {
        if (debug)
            LOG.debug("Entering processGoalModelEvent(goalIdentifier={}, eventIdentifier={}, parameters={})", goalIdentifier, eventIdentifier, parameters);

        /* all other events are handled by the goal model */
        final InstanceGoal<InstanceParameters> instanceGoal =
                getGoalModel().getInstanceGoal(goalIdentifier);
        if (getOrganizationModel().getInstanceGoal(instanceGoal.getIdentifier()) != null) {

            SpecificationEvent specificationEvent = null;
            if (SpecificationEvent.ACHIEVED_EVENT.getIdentifier().equals(eventIdentifier)) {
                specificationEvent = SpecificationEvent.ACHIEVED_EVENT;
            } else if (SpecificationEvent.FAILED_EVENT.getIdentifier().equals(eventIdentifier)) {
                specificationEvent = SpecificationEvent.FAILED_EVENT;
            } else {
                specificationEvent = getGoalModel().getSpecificationEvent(instanceGoal.getSpecificationIdentifier(),
                        eventIdentifier);
            }
            if (specificationEvent == null) {
                throw new IllegalArgumentException(String.format("Unspecified Event at processGoalModelEvent: %s",
                        eventIdentifier));
            }
            final InstanceTreeChanges changeList = getGoalModel().event(instanceGoal, specificationEvent, parameters);
            updateActiveGoals(changeList);
            LOG.debug("Exiting processGoalModelEvent. Change list={}", changeList);
        }
    }

    private synchronized void processGoalModelModification(final UniqueIdentifier goalIdentifier,
                                                           final InstanceParameters parameters) {
        if (debug)
            LOG.debug("Entering processGoalModelModification(goalIdentifier={}, parameters={}", goalIdentifier, parameters);
        final InstanceTreeModifications goalModifications = getGoalModel().modifyInstanceGoal(goalIdentifier,
                parameters);
        final Set<InstanceGoal<InstanceParameters>> modifiedGoals = goalModifications.getModifiedInstanceGoals();
        if (debug) LOG.debug(String.format("MCC | Goals %s affected by modification", modifiedGoals));
        for (final InstanceGoal<InstanceParameters> instanceGoal : modifiedGoals) {
            final Assignment assignment = getAssignmentOfInstanceGoal(instanceGoal);
            if (assignment != null) {
                if (debug) LOG.debug("MCC | Goal {} is assigned to ecAgent {}", instanceGoal,
                        assignment.getAgent());
                final ModificationContent content = new ModificationContent(instanceGoal);
                final ParticipateMessage message = new ParticipateMessage(getPersonaExecutionComponent().getUniqueIdentifier
                        (), assignment.getAgent().getIdentifier(), ParticipatePerformative.GOAL_MODIFICATION, content);
                this.internalOrganizationCommunicationCapability.sendLocal(message);
            }
        }
    }

    private synchronized void processMessage(final ParticipateMessage message) {
        if (debug) LOG.debug("Entering processMessage() Message: {}", message);
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
                    processDeAssignment( message);
                    break;
                case GOAL_MODIFICATION:
                    processGoalModification( message);
                    break;
                default:
                    // should not happen
                    LOG.error("Unacceptable performative in processMessage: {}. content = {}", message.getPerformativeType(), message.getContent().toString());
                    System.exit(-19);
                    throw new IllegalArgumentException(String.format("Unacceptable Performatives \"%s\"",
                            message.getPerformativeType()));
            }
        }
    }

    private synchronized void processTaskFailureEvent(final UniqueIdentifier subEvent, final Object parameter) {
        //this reasoning handles the "task failure" event. remove the
        //assignment for the indicated failure
        final AssignmentContent assignmentContent = (AssignmentContent) parameter;
        final UniqueIdentifier agentIdentifier = assignmentContent.getAgentIdentifier();
        final UniqueIdentifier roleIdentifier = assignmentContent.getRoleIdentifier();
        final UniqueIdentifier instanceGoalIdentifier = assignmentContent.getInstanceGoalIdentifier();
        final Assignment assignment = getAssignment(agentIdentifier, roleIdentifier, instanceGoalIdentifier);
        if (debug) LOG.debug("processEvent(): {} Failed To Achieve {}", agentIdentifier, instanceGoalIdentifier);
        if (assignment != null) {
            this.getOrganizationModel().removeAssignment(assignment.getIdentifier());
        }
    }

    @Override
    public synchronized void reorganize() {
        if (debug) LOG.info("Entering reorganize() from {}", this.getClass().getName());
        Collection<Assignment> assignments = this.getReorganizationAlgorithm().reorganize(getOrganizationModel(),
                getOrganizationModel().getInstanceGoals(), getOrganizationModel().getAgents());
        if (assignments == null) {
            if (debug) {
                LOG.error("reorganize() {}", "Reorganization Algorithm IUnreliable - assignment collection is null");
            }
            return;
        }
        LOG.debug("There are currently {} assignments.", assignments.size());
        if (assignments.isEmpty()) { // no new ec
            if (debug) LOG.debug("No New Assignments");
        } else { // there are new ec
            LOG.debug("New Assignments {}", String.format(" (%d): %s", assignments.size(), assignments));
            getOrganizationModel().addAssignments(assignments);
            for (final Assignment assignment : assignments) {

                LOG.info("CREATING ASSIGNMENT MESSAGE. ParticipateMessage= {}",
                        String.format("New Assignments (%d): %s  " +
                                        "%s", assignments.size(), assignments,
                                assignment.getAgent().getIdentifier()
                        )
                );
                ParticipateMessage msg = new ParticipateMessage(
                        getPersonaExecutionComponent().getUniqueIdentifier(),
                        assignment.getAgent().getIdentifier(),
                        ParticipatePerformative.ASSIGNMENT,
                        AssignmentContent.createAssignmentContent(assignment));
                LOG.info("SENDING ASSIGNMENT MESSAGE via internalOrganizationCommunicationCapability: {}", msg);
                this.internalOrganizationCommunicationCapability.sendLocal(msg);
            }
        }
    }

    @Override
    public synchronized Set<Role> setInitialRoles() {
        LOG.info("Entering setInitialRoles().");
        Set<Role> roles = new HashSet<>();
        for (final InstanceGoal<?> goal : this.getOrganizationModel().getInstanceGoals()) {
            for (final Role role : goal.getAchievedBySet()) {
                roles.add(role);
                if (debug) LOG.debug("MASTER {} adding goal achieved by role: {} ", this.getIdentifier().toString(), role);
            }
        }
        this.initialRoles = roles;
        LOG.info("Exiting setInitialRoles: initialRoles={}",initialRoles);
        return roles;  // returned for external evaluation and testing
    }

    private synchronized void triggerReorganization(boolean reorgNeeded) {
        LOG.debug("Entering triggerReorganization(reorgNeeded={}).", reorgNeeded);
        if (reorgNeeded) {   reorganize();}
    }

    private synchronized void updateActiveGoals(final InstanceTreeChanges changeList) {
        LOG.info("Entering updateActiveGoals(changeList (Adding={}, Removing={})",
                changeList.getAddedInstanceGoals(), changeList.getRemovedInstanceGoals());
        // add
        for (final InstanceGoal<InstanceParameters> goal : changeList.getAddedInstanceGoals()) {
            getOrganizationModel().addInstanceGoal(goal);
        }
        LOG.info("Added {} instance goals.", changeList.getAddedInstanceGoals().size());
        //remove
        for (final InstanceGoal<InstanceParameters> goal : changeList.getRemovedInstanceGoals()) {
            if (debug) LOG.debug("Removing {} instance goals.", changeList.getRemovedInstanceGoals().size());
            /* if the removed goal has been assigned, its need to be deassigned */
            final Assignment assignment = getAssignmentOfInstanceGoal(goal);
            if (debug) LOG.debug("New deassignment = {}.", assignment.toString());
            if (assignment != null) {
                if (debug) LOG.debug("Sending local deassignment = {}.", assignment.toString());
                this.internalOrganizationCommunicationCapability.sendLocal(
                        ParticipateMessage.createLocalParticipateMessage(getPersonaExecutionComponent()
                                        .getUniqueIdentifier(), assignment.getAgent().getIdentifier(),
                                ParticipatePerformative.DEASSIGNMENT,
                                AssignmentContent.createAssignmentContent(assignment)
                        )
                );
                getOrganizationModel().removeAssignment(assignment.getIdentifier());
            }
            getOrganizationModel().removeInstanceGoal(goal.getIdentifier());
        }
        //Don't end when goals are empty
        //  if (getOrganizationModel().getInstanceGoals().isEmpty()) {
        //Don't end when goals are empty
        //  TerminationCriteria.setIsDone(true);
        //   }
    }

    @Override
    public synchronized void updateAgentInformation() {
        LOG.info("Entering updateAgentInformation().");
        final Agent<?> agent = getOrganizationModel().getAgent(getPersonaExecutionComponent().getUniqueIdentifier());
        if (agent != null) {
            for (final Capability agentCapability : getPersonaExecutionComponent().getCapabilities()) {
                if (debug)
                    LOG.debug("updateAgentInformation() scores on Agent info -  capability: {}", agentCapability);
                final double score = getPersonaExecutionComponent().getCapabilityScore(agentCapability);
                // Updated DMC - 10/6/2013 - to handle the case when the agent has a agentCapability (score = 1) but
                // the agentCapability is not needed (possesses = 0.0)
                final double agentPossessesScore = agent.getPossessesScore(agentCapability.getIdentifier());
                if (score != agentPossessesScore && agentPossessesScore != 0.0 && score == 1.0) {
                    agent.setPossessesScore(agentCapability.getIdentifier(), score);
                }
            }
            checkAgent(agent);
        }
    }

    @Override
    public synchronized void updateInitialActiveGoals(final InstanceTreeChanges changeList) {
        if (debug) LOG.debug("Entering updateInitialActiveGoals(changeList={}). ", changeList);
        // goal change list (so there is something to do)
        if (changeList.getAddedInstanceGoals().isEmpty()) {
            LOG.error("ERROR: Starting goal parameter is null. Double-check GoalModel.xml and Initialize.xml for {}: " +
                    "{}", this.getIdentifier().toString(), changeList.getAddedInstanceGoals());
            System.exit(1);
        }
        // update active goals .. inside the CC... inside the EC...
        updateActiveGoals(changeList);
        LOG.debug("Exiting updateInitialActiveGoals(): instGoals={}", this.getOrganizationModel().getInstanceGoals());
    }

    public IOrganizationCommunicationCapability getOrganizationCommunicationCapability() {
        return this.organizationCommunicationCapability;
    }

    private enum ExecutionState {
        INITIALIZING,
        WAITING_FOR_AGENTS_FOR_INITIAL_ASSIGNMENTS,
        WORKING
    }

}
