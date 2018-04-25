package edu.ksu.cis.macr.ipds.primary.capabilities.manage;

import edu.ksu.cis.macr.aasis.agent.cc_a.comm.IInternalOrganizationCommunicationCapability;
import edu.ksu.cis.macr.aasis.agent.cc_m.IPersonaControlComponentMaster;
import edu.ksu.cis.macr.aasis.agent.cc_message.participate.ParticipateMessage;
import edu.ksu.cis.macr.aasis.agent.cc_message.participate.ParticipatePerformative;
import edu.ksu.cis.macr.aasis.agent.persona.IPersona;
import edu.ksu.cis.macr.aasis.simulator.player.IPlayable;
import edu.ksu.cis.macr.aasis.simulator.player.PlayableCapability;
import edu.ksu.cis.macr.aasis.spec.OrganizationFocus;
import edu.ksu.cis.macr.goal.model.InstanceParameters;
import edu.ksu.cis.macr.goal.model.InstanceTreeChanges;
import edu.ksu.cis.macr.goal.model.InstanceTreeModifications;
import edu.ksu.cis.macr.goal.model.SpecificationEvent;
import edu.ksu.cis.macr.ipds.primary.organizer.ReorganizationAlgorithm;
import edu.ksu.cis.macr.ipds.primary.persona.AbstractBaseControlComponent;
import edu.ksu.cis.macr.obaa_pp.cc.om.EmptyOrganizationModel;
import edu.ksu.cis.macr.obaa_pp.cc.om.IOrganizationModel;
import edu.ksu.cis.macr.obaa_pp.cc_cip.IdentifierProviderBuilder;
import edu.ksu.cis.macr.obaa_pp.cc_message.*;
import edu.ksu.cis.macr.obaa_pp.events.IOrganizationEvent;
import edu.ksu.cis.macr.obaa_pp.events.OrganizationEvent;
import edu.ksu.cis.macr.obaa_pp.events.OrganizationEventType;
import edu.ksu.cis.macr.organization.model.*;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
/**
 * Adds the functionality
 * necessary to act as the master for the local organization.
 */
public class Master extends AbstractBaseControlComponent implements IPersonaControlComponentMaster {
    private static final Logger LOG = LoggerFactory.getLogger(Master.class);
    private static final boolean debug = false;
    protected final Map<UniqueIdentifier, Agent<UniqueIdentifier>> agentQueue = new ConcurrentHashMap<>();
    protected IPersona persona;
    protected Set<Role> initialRoles;

    protected PlayableCapability playerCapability;

    protected ExecutionState state = ExecutionState.INITIALIZING;
    protected boolean doneWaitingForInitialAssignments;
    protected String topgoal;
    protected Path rolepath;
    protected edu.ksu.cis.macr.organization.model.xml.UniqueIdentifierProvider capProvider;

    /**
     * @param name      -
     * @param persona   - the subagent
     * @param knowledge - the XML knowledge about the organization
     * @param focus     - an Enum that defines the focus of the organization
     */
    public Master(final String name, final IPersona persona, final Element knowledge, final OrganizationFocus focus) {
        super(name, persona, knowledge, focus);
        if (debug) LOG.debug("Entering constructor.");

        this.persona = Objects.requireNonNull(persona, "Error: Cannot create CCMaster with null EC.");
        if (debug) LOG.info("Creating control component master: {}.", name);
        if (debug) LOG.debug("The ccm.execution_component.create().getGoalParameterValues() are {}",
                persona.getOrganization().getGoalParameterValues());
        this.initialRoles = null;
        doneWaitingForInitialAssignments = false;
        try {
            this.playerCapability = new PlayableCapability(this.getPersonaExecutionComponent().getUniqueIdentifier().toString());
        } catch (Exception e) {
            LOG.error("ERROR in Organization Master constructor. {}", e.getMessage());
            System.exit(-56);
        }


        String rolefile = Objects.requireNonNull(persona.getOrganization()
                .getOrganizationSpecification().getRoleFile(), "External master control component needs access to a role file.");
        LOG.info("external organization constructor: rolefile={}", rolefile);

        String goalfile = Objects.requireNonNull(persona.getOrganization()
                .getOrganizationSpecification().getGoalFile(), "External master control component needs access to a goal file.");
        LOG.info("external organization master constructor: goalfile={}", goalfile);

        this.topgoal = Objects.requireNonNull(persona
                .getOrganization().getOrganizationSpecification().getTopGoal(), "Base control component needs top goal.");

        File goalFile = new File(goalfile);
        try {
            checkFile(goalFile);
            if (debug) LOG.debug("The goal model file was found. {} ", goalfile);
        } catch (Exception e) {
            LOG.error("Error failed checking the goal model file at {}.", goalfile);
            System.exit(-723);
        }
        try {
            goalModel.populate(goalFile.toPath(), topgoal);
            if (debug) LOG.debug("The goal model file was populated. {} ", goalModel.toString());
        } catch (Exception e) {
            LOG.error("ERROR populating goal model: 1) Make sure no goals are numbered 0 - use a text editor to change. 2) Then link all leaf goals under AND or OR.  {}. {}", goalfile, e.getLocalizedMessage());
            System.exit(-724);
        }
        this.setGoalModel(goalModel);
        LOG.info("Goal model set.");

        this.rolepath = new File(rolefile).toPath();
        LOG.info("Org role model path ={}", rolepath);

        this.capProvider = null;
        try {
            LOG.info("Creating Org capability unique identifier provider");
            capProvider = IdentifierProviderBuilder.create();
            LOG.info("Created Org capability unique identifier provider ={}", capProvider);
        } catch (Exception ex) {
            LOG.error("ERROR: could not set org model from rolefile={}, goalfile={}. {}", rolefile, getGoalModel().toString(), ex.getMessage());
            System.exit(-2);
        }

        if (debug) LOG.debug("Setting reorg algo for {}", name);
        this.setReorganizationAlgorithm(ReorganizationAlgorithm.createReorganizationAlgorithm(name));
        LOG.info("The external organization reorganization alogorithm={}.", this.getReorganizationAlgorithm().getClass().getSimpleName());

        try {
            final IOrganizationModel emptyOrgModel = new EmptyOrganizationModel().populate(rolepath, this.goalModel, capProvider);
            LOG.info("Org emptyOrgModel ={}", emptyOrgModel);
            setOrganizationModel(emptyOrgModel);
        } catch (Exception ex) {
            LOG.error("ERROR: could not set org model from rolefile={}, goalfile={}. {}", rolefile, getGoalModel().toString(), ex.getMessage());
            LOG.error("ERROR: Suggested debug - compare (1) organization role model (2) org agent capabilities in agent.xml and (3) CapabilityIdentifierProvider to make sure they are consistent. ");
            System.exit(-6);
        }
        if (this.focus == OrganizationFocus.External) {
            if (debug) LOG.info("Initializing goals...............................................................");
            final InstanceParameters topParams = getTopGoalInstanceParameters();
            InstanceTreeChanges changeList = getInitialGoalModelChangeList(topParams);
            updateInitialActiveGoals(changeList);
            setInitialRoles();
            if (debug) LOG.info("Done initializing goals..........................................................");
        }
    }


    @Override
    public  InstanceParameters getTopGoalInstanceParameters() {// set the top level goal instance parameters
        LOG.info("Getting top goal instance parameters (CCM {})", this.getIdentifier().toString());
        Map<UniqueIdentifier, Object> map = Objects.requireNonNull(this.persona.getOrganization()
                .getGoalParameterValues(), "Error: an CCMaster cannot have empty goal parameters.");
        LOG.info("Top goal instance parameters map has {} items  (CCM {})", this.persona.getOrganization()
                .getGoalParameterValues().size(), this.getIdentifier().toString());
        final InstanceParameters topInstanceParameters = Objects.requireNonNull(new InstanceParameters(map),
                "Error: top goal instance parameters are required.");
        LOG.info("Top goal instance parameters for CCM {}: {}", this.getIdentifier().toString(), topInstanceParameters);
        return topInstanceParameters;
    }

    @Override
    public synchronized InstanceTreeChanges getInitialGoalModelChangeList(final InstanceParameters topInstanceParameters) {//
        // initialize the goal model for this organization
        AtomicReference<InstanceTreeChanges> changeList = new AtomicReference<>(Objects.requireNonNull(getGoalModel().initialize(topInstanceParameters), "Error: The organization's goal model was initially empty. Nothing to do."));
        LOG.info("Goal model for CCM {} initially has {} leaf specification goals.", this.getIdentifier().toString(),
                getGoalModel().getLeafSpecificationGoals().size());
        if (debug) LOG.debug("Instance goal tree initial change list for CCM {} has size={}: {}", this.getIdentifier().toString(),
                changeList.get().getAddedInstanceGoals().size(), changeList.get().getAddedInstanceGoals().iterator().next()
        );
        return changeList.get();
    }

    @Override
    public synchronized void updateInitialActiveGoals(final InstanceTreeChanges changeList) {
        if (debug) LOG.debug("Calling updateInitialActiveGoals with changeList={}. ", changeList);
        // goal change list (so there is something to do)
        if (changeList.getAddedInstanceGoals().isEmpty()) {
            LOG.error("ERROR: Starting goal parameter is null. Double-check GoalModel.xml and Initialize.xml for {}: " +
                    "{}", this.getIdentifier().toString(), changeList.getAddedInstanceGoals());
            System.exit(1);
        }
        // update active goals  - this is too much work to do in the constructor.. inside the CC... inside the EC...
        updateActiveGoals(changeList);
    }

    @Override
    public synchronized Set<Role> setInitialRoles() {
        Set<Role> roles = new HashSet<Role>();
        for (final InstanceGoal<?> goal : this.getOrganizationModel().getInstanceGoals()) {
            for (final Role role : goal.getAchievedBySet()) {
                roles.add(role);
                if (debug) LOG.debug("MASTER {} adding goal achieved by role: {} ", this.getIdentifier().toString(), role);
            }
        }
        this.initialRoles = roles;
        return roles;  // returned for external evaluation and testing
    }

    @Override
    public  PlayableCapability getPlayerCapability() {
        return playerCapability;
    }

    @Override
    public synchronized void setPlayerCapability(final IPlayable playerCapability) {
        this.playerCapability = (PlayableCapability)playerCapability;
    }

    @Override
    public  void executeControlComponentPlan() {
        if (debug) LOG.debug("Executing external CC master state {}. ", this.state);
        if (this.playerCapability.getPaused()) {
            if (debug) LOG.debug("Paused in CC master state {}.", this.state);
            return;
        }
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

    @Override
    public IPersona getPersona() {
        return this.getOwner();
    }

    @Override
    public synchronized void updateAgentInformation() {
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

    /**
     * The {@code content} that will be channeled by extensions.
     *
     * @param content the {@code content} to be passed along the {@code ICommunicationChannel}.
     */
    @Override
    public synchronized void channelContent(final Object content) {
        internalOrganizationCommunicationCapability.channelContent(content);
    }

    protected void doInitialization() {
        initializeECAgent();
        this.state = ExecutionState.WAITING_FOR_AGENTS_FOR_INITIAL_ASSIGNMENTS;
    }

    protected void doWaitingForAgentsForInitialAssignments() {
        /*
         * determine if there are enough initial agents to proceed with
         * reorganization or to continue waiting for more agents to register
         */
        this.doneWaitingForInitialAssignments = areThereEnoughAgentsForInitialAssignments();

        if (this.doneWaitingForInitialAssignments) {
            this.state = ExecutionState.WORKING;
        } else {
            /* continue to process ecAgent registration getNumberOfMessages */
            while (internalOrganizationCommunicationCapability.messages() > 0) {
                ParticipateMessage message = (ParticipateMessage) internalOrganizationCommunicationCapability.receiveLocal();
                switch (message.getPerformativeType()) {
                    case BROADCASTING_AGENT_REGISTRATION:
                        processAgentRegistration(message);
                        break;
                    case AGENT_REGISTRATION_CONFIRMATION_RECEIVED:
                        processAgentRegistrationConfirmationReceived(message);
                        break;
                    default:
                        /*
                         * other performatives should NOT be received at all because
                         * at this state, no other performatives should be in use
                         */
                        LOG.error("Unacceptable performative in doWaitingForAgentsForInitialAssignments: {}. content = {}", message.getPerformativeType(), message.getContent().toString());
                        System.exit(-19);
                        throw new IllegalArgumentException(String.format("Unacceptable Performatives \"%s\"",
                                message.getPerformativeType()));
                }
            }
        }
    }

    protected boolean areThereEnoughAgentsForInitialAssignments() {
        if (debug) LOG.debug("Roles Left Before Removal: {}.", this.initialRoles);
        final Set<Role> rolesToRemove = new HashSet<>();
        if (this.initialRoles == null) {
            setInitialRoles();
            if (this.initialRoles == null) {
                LOG.error("CCM initial roles are still null.");
                System.exit(-34);
            }
        }
        for (final Role role : this.initialRoles) {
            getOrganizationModel().getInstanceGoals().stream().filter(role::achieves).forEach(goal ->
                    rolesToRemove.addAll(getOrganizationModel().getAgents().stream().filter(agent ->
                            role.goodness(agent, goal, new HashSet<>()) > RoleGoodnessFunction.MIN_SCORE).map(agent ->
                            role).collect(Collectors.toList())));
        }
        this.initialRoles.removeAll(rolesToRemove);
        if (debug) LOG.debug("Initial roles empty? {}.", this.initialRoles.isEmpty());
        return this.initialRoles.isEmpty();
    }

    protected void processAgentRegistration(final ParticipateMessage message) {
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

    protected void processAgentRegistrationConfirmationReceived(final ParticipateMessage message) {
        final UniqueIdentifier agentIdentifier = message.getLocalSender();
        final Agent<UniqueIdentifier> attributeAgent = this.agentQueue.get(agentIdentifier);
        if (attributeAgent != null) {
            getOrganizationModel().addAgent(attributeAgent);
        } else {
            LOG.error("Unknown Agent:  {}.", agentIdentifier);
        }
        triggerReorganization();
    }

    protected void triggerReorganization() {
        reorganize();
    }

    @Override
    public synchronized void reorganize() {
        LOG.info("Calling reorg from {}", this.getClass().getName());
        Collection<Assignment> assignments = getReorganizationAlgorithm().reorganize(getOrganizationModel(),
                getOrganizationModel().getInstanceGoals(), getOrganizationModel().getAgents());
        if (assignments == null) {
            if (debug) {
                LOG.error("reorganize() {}", "Reorganization Algorithm IUnreliable - assignment collection is null");
            }
            return;
        }

        if (debug) LOG.debug("There are currently {} assignments.", assignments.size());

        if (assignments.isEmpty()) { // no new ec
            if (debug) LOG.debug("No New Assignments");
        } else { // there are new ec
            LOG.debug("New Assignments {}", String.format(" (%d): %s", assignments.size(), assignments));

            getOrganizationModel().addAssignments(assignments);
            for (final Assignment assignment : assignments) {
                LOG.debug("sending assignment ParticipateMessage {}",
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
                this.internalOrganizationCommunicationCapability.sendLocal(msg);
            }
        }
    }

    protected void doWork() {
        if (debug) LOG.debug("Master doWork - the {} active instance goals are: {}",
                this.getGoalModel().getActiveInstanceGoals().size(), this.getGoalModel().getActiveInstanceGoals());
        if (debug)
            LOG.debug("Master doWork - there are {} organization events. ", this.organizationEvents.numberOfQueuedEvents());
        if (debug)
            LOG.debug("Master doWork - there are {} getNumberOfInternalMessages.", this.internalOrganizationCommunicationCapability.messages());

        if (this.playerCapability.getPaused()) {
            if (debug) LOG.debug("PAUSED In CC MASTER STATE doWork()");
            return;
        }
        boolean reorgNeeded = false;
        final Map<UniqueIdentifier, Agent<UniqueIdentifier>> startingQueue = this.agentQueue;
        /* process all events */
        while (this.organizationEvents.numberOfQueuedEvents() > 0) {
            final List<IOrganizationEvent> organizationEvents = this.organizationEvents.getNextEvent();
            for (final IOrganizationEvent organizationEvent : organizationEvents) {
                if (processEvent(organizationEvent)) reorgNeeded = true;
            }
        }
        /* process all getNumberOfMessages */
        while (internalOrganizationCommunicationCapability.messages() > 0) {
            //  final ParticipateMessage message = (ParticipateMessage) selfComm.receive();
            final ParticipateMessage message = (ParticipateMessage) internalOrganizationCommunicationCapability.receiveLocal(); // added to force LOCAL
            if (debug) LOG.debug("The performative of this messages is : {}", message.getPerformativeType());
            if (debug) LOG.debug("Reasoning messages sent successfully, Contents: {}", message);

            if (message.getPerformativeType().equals(ParticipatePerformative.ASSIGNMENT)) {
                LOG.info("The market master proxy received an ASSIGNMENT MESSAGE: {}", message.toString());
                processAssignment(message);
            }
            // if step mode is on, wait for user, otherwise just continue
            // this.playerCapability.step();
            //   waitForUserToAdvance(this.playerCapability.getStepMode(), 0);
        }
        if (reorgNeeded) triggerReorganization();
    }

    protected boolean processEvent(final IOrganizationEvent organizationEvent) {
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

    protected void processGoalModelEvent(final UniqueIdentifier goalIdentifier,
                                         final UniqueIdentifier eventIdentifier,
                                         final InstanceParameters parameters) {
        if (debug) LOG.debug("Processing GOAL_MODEL_EVENT. {} {}", goalIdentifier, eventIdentifier);

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
        }
    }

    protected synchronized void updateActiveGoals(final InstanceTreeChanges changeList) {
        LOG.info("CCM {} updateActiveGoals() - ChangeList (Adding: {}, Removing: {})", this.getIdentifier().toString(),
                changeList.getAddedInstanceGoals(), changeList.getRemovedInstanceGoals());
        for (final InstanceGoal<InstanceParameters> goal : changeList.getAddedInstanceGoals()) {
            getOrganizationModel().addInstanceGoal(goal);
        }
        for (final InstanceGoal<InstanceParameters> goal : changeList.getRemovedInstanceGoals()) {
            /* if the removed goal has been assigned, its need to be deassigned */
            final Assignment assignment = getAssignmentOfInstanceGoal(goal);
            if (assignment != null) {
                this.internalOrganizationCommunicationCapability.sendLocal(
                        new ParticipateMessage(getPersonaExecutionComponent()
                                .getUniqueIdentifier(), assignment.getAgent().getIdentifier(),
                                ParticipatePerformative.DEASSIGNMENT,
                                AssignmentContent.createAssignmentContent(assignment)
                        )
                );
                getOrganizationModel().removeAssignment(assignment.getIdentifier());
            }
            getOrganizationModel().removeInstanceGoal(goal.getIdentifier());
        }
        if (getOrganizationModel().getInstanceGoals().isEmpty()) {
            //Don't end when goals are empty
            //  TerminationCriteria.setIsDone(true);
        }
    }

    protected synchronized Assignment getAssignmentOfInstanceGoal(final InstanceGoal<InstanceParameters> instanceGoal) {
        for (final Assignment assignment : getOrganizationModel().getAssignments()) {
            if (instanceGoal.equals(assignment.getInstanceGoal())) {
                return assignment;
            }
        }
        return null;
    }

    protected void processGoalModelModification(final UniqueIdentifier goalIdentifier,
                                                final InstanceParameters parameters) {
        if (debug) LOG.debug("MCC | Incoming modification {} for goal {}", parameters, goalIdentifier);
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

    protected void processTaskFailureEvent(final UniqueIdentifier subEvent, final Object parameter) {
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
            this.getOrganizationModel().removeAssignment(assignment.getIdentifier());
        }
    }

    protected synchronized Assignment getAssignment(final UniqueIdentifier agentIdentifier, final UniqueIdentifier roleIdentifier,
                                                    final UniqueIdentifier goalIdentifier) {
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

    protected void processAgentGoneEvent(final Object parameter) {
        final IAgentGoneContent IAgentGoneContent = (IAgentGoneContent) parameter;
        final UniqueIdentifier agentIdentifier = IAgentGoneContent.getAgentIdentifier();
        final Set<Assignment> assignments = getOrganizationModel().getAssignmentsOfAgent(agentIdentifier);
        for (final Assignment assignment : assignments) {
            getOrganizationModel().removeAssignment(assignment.getIdentifier());
        }
        getOrganizationModel().removeAgent(agentIdentifier);
        triggerReorganization(); // DMC added
    }

    public synchronized IInternalOrganizationCommunicationCapability getInternalOrganizationCommunicationCapability() {
        return this.internalOrganizationCommunicationCapability;
    }

    public  synchronized IPersona getPersonaExecutionComponent() {
        return this.persona;
    }

    protected void processMessage(final ParticipateMessage message) {
        if (debug) LOG.debug("processMessage() Message: {}", message);
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
                    // should not happen
                    LOG.error("Unacceptable performative in processMessage: {}. content = {}", message.getPerformativeType(), message.getContent().toString());
                    System.exit(-19);
                    throw new IllegalArgumentException(String.format("Unacceptable Performatives \"%s\"",
                            message.getPerformativeType()));
            }
        }
    }

    protected void processAgentUpdateInformation(final ParticipateMessage message) {
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
            /*
             * when ecAgent updates their information, a check has to be done to
             * ensure the ecAgent is still able to continue working on the current
             * ec
             */
            checkAgent(agent);
        }
    }

    protected void checkAgent(final Agent<?> agent) {
        final Collection<Assignment> assignmentsToRemove = new ArrayList<>();
        for (final Assignment assignment : getOrganizationModel().getAssignmentsOfAgent(agent.getIdentifier())) {
            final double goodness = assignment.getRole().goodness(agent, assignment.getInstanceGoal(),
                    new HashSet<>());
            if (goodness <= RoleGoodnessFunction.MIN_SCORE) {
                /* ecAgent is unable to work on the assignment */
                final ParticipateMessage message = new ParticipateMessage(getPersonaExecutionComponent().getUniqueIdentifier
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

    protected void processEvent(final ParticipateMessage message) {
        final Object content = message.getContent();
        if (content instanceof ListEventsContent) {
            final ListEventsContent listEventsContent = (ListEventsContent) content;
            for (final OrganizationEventContent organizationEventContent : listEventsContent.getEvents()) {
                final InstanceGoal<?> instanceGoal = getOrganizationModel().getInstanceGoal(organizationEventContent
                        .getInstanceGoalIdentifier());
                final OrganizationEvent organizationEvent = organizationEventContent.toOrganizationEvent(instanceGoal);
                // if it needs a reorganization, just do it - DMC
                if (processEvent(organizationEvent)) {
                    triggerReorganization();
                }
            }
        }
    }

    protected enum ExecutionState {
        INITIALIZING,
        WAITING_FOR_AGENTS_FOR_INITIAL_ASSIGNMENTS,
        WORKING
    }
}
