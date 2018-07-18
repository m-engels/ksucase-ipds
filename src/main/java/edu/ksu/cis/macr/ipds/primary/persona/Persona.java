/*
 *
 * Copyright 2012 Kansas State University MACR Laboratory
 * http://macr.cis.ksu.edu/ Department of Computing & Information Sciences
 *
 * See License.txt file for the license agreement.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package edu.ksu.cis.macr.ipds.primary.persona;


import edu.ksu.cis.macr.aasis.agent.persona.ICapability;
import edu.ksu.cis.macr.aasis.agent.persona.IOrganization;
import edu.ksu.cis.macr.aasis.agent.persona.IPersona;
import edu.ksu.cis.macr.aasis.simulator.player.PlayableCapability;
import edu.ksu.cis.macr.aasis.simulator.player.Player;
import edu.ksu.cis.macr.aasis.spec.OrganizationFocus;
import edu.ksu.cis.macr.goal.model.InstanceParameters;
import edu.ksu.cis.macr.goal.model.SpecificationEvent;
import edu.ksu.cis.macr.ipds.primary.capabilities.participate.PowerCommunicationCapability;
import edu.ksu.cis.macr.ipds.primary.plan_selector.PlanSelector;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IExecutablePlan;
import edu.ksu.cis.macr.obaa_pp.ec_ps.IPlanSelector;
import edu.ksu.cis.macr.obaa_pp.ec_task.ITask;
import edu.ksu.cis.macr.obaa_pp.ec_task.Task;
import edu.ksu.cis.macr.obaa_pp.events.IEventManager;
import edu.ksu.cis.macr.obaa_pp.events.IOrganizationEvent;
import edu.ksu.cis.macr.obaa_pp.events.OrganizationEvent;
import edu.ksu.cis.macr.obaa_pp.events.OrganizationEventType;
import edu.ksu.cis.macr.organization.model.Assignment;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The {@code Agent} is the base class for all custom agent types in the organization-based agent architecture.
 * The derived type of agent is defined by the capabilities given to that agent type.  Each autonomous agent has one
 * head or master running its internal organization of persona.  A persona is added for each affiliated organization in
 * the agents list of connections provided with its "Self Control" goal.
 */
public class Persona extends AbstractPersona {
    private static final UniqueIdentifier ACHIEVED_EVENT = SpecificationEvent.ACHIEVED_EVENT.getIdentifier();
    private static final Logger LOG = LoggerFactory.getLogger(Persona.class);
    private static final boolean debug = false;
    protected String identifierString;
    private PowerCommunicationCapability localPowerCommunicationCapability;
    private PlayableCapability playerCapability;
    protected IPlanSelector planSelector = new PlanSelector();
    private Assignment assignment;
    private Assignment deassignment;

    /**
     * Constructs a new instance of an agent using the organization-based agent architecture. Each prosumer agent will have
     * the capabilities needed to cc in a peer-based organization both as a peer and as a supervisor and contains its own
     * knowledge about the immediate organization in which it participates.
     *
     * @param identifierString a string containing a name that uniquely
     */
    public Persona(final String identifierString) {
        super(identifierString);
        LOG.info("\t..................CONSTRUCTING PERSONA {}.", identifierString);
        this.identifierString = identifierString;

        this.playerCapability = new PlayableCapability(this.identifierString);
        if (debug) LOG.debug("\t New playable capability={}.", this.playerCapability);

        this.localPowerCommunicationCapability = new PowerCommunicationCapability(this, organization);
        if (debug) LOG.debug("\t New PowerCommunicationCapability={}", this.localPowerCommunicationCapability);
        addCapability(localPowerCommunicationCapability);
        if (debug) LOG.debug("\t Added PowerCommunicationCapability={}.", this.localPowerCommunicationCapability);

        this.internalCommunicationCapability.addChannel(localPowerCommunicationCapability.getCommunicationChannelID(),
                this.localPowerCommunicationCapability);
        if (debug) LOG.debug("\t Added localPowerCommunicationCapability internally to add the channel.");

        LOG.info("\t..................EXITING PERSONA(identifier={})", identifierString);
    }

    /**
     * Constructs a new instance of an agent using the organization-based agent architecture. Each prosumer agent will have
     * the capabilities needed to cc in a peer-based organization both as a peer and as a supervisor and contains its own
     * knowledge about the immediate organization in which it participates.
     *
     * @param org              the organization, containing information about agents and objects in the organization system.
     * @param identifierString a string containing a name that uniquely identifies this in the system.
     * @param knowledge        an XML specification of the organization.
     * @param focus            an enum that shows what focus the current organization is.
     */
    public Persona(final IOrganization org, final String identifierString, final Element knowledge, final OrganizationFocus focus) {
        super(org, identifierString, knowledge, focus);
        LOG.info("\t..................CONSTRUCTING PERSONA(org={}, identifier={}, knowledge={}, focus={}", org, identifierString, knowledge, focus);
        this.identifierString = identifierString;
        organization = org;
        this.focus = focus;

        this.playerCapability = new PlayableCapability(this.identifierString);
        if (debug) LOG.debug("\t New playable capability={}.", this.playerCapability);

        this.localPowerCommunicationCapability = new PowerCommunicationCapability(this, org);
        if (debug) LOG.debug("\t New PowerCommunicationCapability={}", this.localPowerCommunicationCapability);
        addCapability(localPowerCommunicationCapability);
        if (debug) LOG.debug("\t Added PowerCommunicationCapability={}.", this.localPowerCommunicationCapability);


        if (this.internalCommunicationCapability == null) {
            LOG.debug("ERROR: internalCommunicationCapability is null - Can't add localPowerCommunicationCapability.");
        } else {
            if (debug)
                LOG.debug("\tlocalPowerCommunicationCapability.getCommunicationChannelID()={}.", localPowerCommunicationCapability.getCommunicationChannelID());

            this.internalCommunicationCapability.addChannel(localPowerCommunicationCapability.getCommunicationChannelID(),
                    this.localPowerCommunicationCapability);
            if (debug) LOG.debug("\t Added localPowerCommunicationCapability internally to add the channel.");
        }

        LOG.info("	..................EXITING PERSONA()...........................");
    }

    @Override
    public void addCapability(final ICapability capability) {
        if (debug) LOG.debug("Adding capability: {}", capability);
        this.capabilityManager.addCapability(capability, true);
    }

    public void addCapability(edu.ksu.cis.macr.obaa_pp.ec.ICapability capability) {
        // must override abstract method in IPersonaExecutionComponent
        LOG.error("Tried to add capability by wrong overload");
        System.exit(-1);
    }

    public void addGoalModification(InstanceGoal<InstanceParameters> modification) {
        // must override abstract method in IPersonaExecutionComponent
    }

    public IEventManager getEventManager() {
        return this.getOrganizationEvents();
    }

    /**
     * @return the identifierString
     */
    @Override
    public String getIdentifierString() {
        return identifierString;
    }

    /**
     * @param identifierString the identifierString to set
     */
    public synchronized void setIdentifierString(String identifierString) {
        this.identifierString = identifierString;
    }

    @Override
    public String toString() {
        return "Persona [identifierString=" + this.identifierString + "]";
    }

    @Override
    public void execute() {
        while (isAlive()) {
            Player.step();
            if (assignment != null && assignment.equals(deassignment)) assignment = null;
            deassignment = null;
            if (assignment == null) endTurn();
            else {
                Task task = new Task(assignment);
                LOG.info("Executing assigned task: {}", task);
                this.executeTask(task);
                assignment = null;
            }
        }
    }

    /**
     * Execute the given task.
     *
     * @param task - the task to be executed.
     */
    @Override
    public synchronized void executeTask(ITask task) {
        final InstanceGoal<?> goal = task.getAssignment().getInstanceGoal();
        UniqueIdentifier roleIdentifier = Objects.requireNonNull(assignment.getRole().getIdentifier());
        UniqueIdentifier goalIdentifier = Objects.requireNonNull(goal.getSpecificationIdentifier());
        IExecutablePlan executablePlan = planSelector.getPlan(roleIdentifier, goalIdentifier);
        task.setExecutionPlan(executablePlan);
        LOG.info("Selected plan for {} to achieve {} is {}.", roleIdentifier, goalIdentifier, executablePlan);
        do {
            executablePlan.execute(this, goal);
            endTurn();
        } while (!executablePlan.isDone());
        doAssignmentTaskCompleted(this, task);
    }

    /**
     * Removes the assignment task from the task queue.
     *
     * @param agent        - the agent assigned to the task
     * @param assignedTask -
     */
    public synchronized void doAssignmentTaskCompleted(final IPersona agent, final ITask assignedTask) {
        Objects.requireNonNull(agent, "IExecutionComponent cannot be null");
        Objects.requireNonNull(assignedTask, "assignmentTask cannot be null");
        assignment = null;
        final IOrganizationEvent organizationEvent = new OrganizationEvent(OrganizationEventType.EVENT,
                ACHIEVED_EVENT, assignedTask.getAssignment().getInstanceGoal(), null);
        List<IOrganizationEvent> organizationEvents = new ArrayList<>();
        organizationEvents.add(organizationEvent);
        informControlComponent(organizationEvents);
    }

    /**
     * Execute the behaviors required after failing a task.
     *
     * @param task - the assignment (Agent-Role-Goal) along with status information.
     */
    public synchronized void doTaskFailed(final ITask task) {
        if (debug) LOG.debug("Entering assignmentTaskFailed() {}", String.format("Task \"%s\" Failed", task));
        assignment = null;
        final IOrganizationEvent organizationEvent = new OrganizationEvent(OrganizationEventType.TASK_FAILURE_EVENT,
                null, task.getAssignment().getInstanceGoal(), null);
        final List<IOrganizationEvent> organizationEvents = new ArrayList<>();
        organizationEvents.add(organizationEvent);
        informControlComponent(organizationEvents);
    }

    public void addAssignment(Assignment a) {
        if (assignment != null) {
            LOG.error("Tried to add another assignment");
            System.exit(-1);
        }
        assignment = a;
    }

    public void addDeAssignment(Assignment d) {
        if (deassignment != null) {
            LOG.error("Tried to add another deassignment");
            System.exit(-1);
        }
        deassignment = d;
    }
}
