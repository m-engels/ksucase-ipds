/**
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
package edu.ksu.cis.macr.ipds.self.persona;

import edu.ksu.cis.macr.aasis.agent.persona.IOrganization;
import edu.ksu.cis.macr.aasis.agent.persona.IPersona;
import edu.ksu.cis.macr.aasis.simulator.player.Player;
import edu.ksu.cis.macr.aasis.spec.OrganizationFocus;
import edu.ksu.cis.macr.goal.model.SpecificationEvent;
import edu.ksu.cis.macr.ipds.primary.persona.Persona;
import edu.ksu.cis.macr.ipds.self.plan_selector.SelfPlanSelector;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IExecutablePlan;
import edu.ksu.cis.macr.obaa_pp.ec_ps.IPlanSelector;
import edu.ksu.cis.macr.obaa_pp.ec_task.ITask;
import edu.ksu.cis.macr.obaa_pp.ec_task.Task;
import edu.ksu.cis.macr.obaa_pp.events.IOrganizationEvent;
import edu.ksu.cis.macr.obaa_pp.events.OrganizationEvent;
import edu.ksu.cis.macr.obaa_pp.events.OrganizationEventType;
import edu.ksu.cis.macr.organization.model.Agent;
import edu.ksu.cis.macr.organization.model.Assignment;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import edu.ksu.cis.macr.organization.model.RoleGoodnessFunction;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 The {@code Agent} is the base class for all custom agent types in the organization-based agent architecture.
 The derived type of agent is defined by the capabilities given to that agent type.  Each autonomous agent has one
 head or master running its internal organization of persona.  A persona is added for each affiliated organization in
 the agents list of connections provided with its "Self Control" goal.
 */
public class SelfPersona extends Persona {
    private static final Logger LOG = LoggerFactory.getLogger(SelfPersona.class);
    private static final Boolean debug =  true;
    private static final UniqueIdentifier ACHIEVED_EVENT = SpecificationEvent.ACHIEVED_EVENT.getIdentifier();



    /**
     Constructs a new instance of an agent using the organization-based agent architecture. Each prosumer agent will have
     the capabilities needed to cc in a peer-based organization both as a peer and as a supervisor and contains its own
     knowledge about the immediate organization in which it participates.

     @param org the organization, containing information about agents and objects in the organization system.
     @param identifierString a string containing a name that uniquely identifies this in the system.
     @param knowledge an XML specification of the organization.
     @param focus the enum to show what focus the organization is.
     */
    public SelfPersona(final IOrganization org, final String identifierString, final Element knowledge, final OrganizationFocus focus) {
        super(org, identifierString, knowledge, focus);

       LOG.info("\t..................CONSTRUCTING SELF PERSONA(org={}, identifier={}, knowledge={}, focus={})",org, identifierString, knowledge, focus);

        this.organization = org;
        this.focus = focus;
        if (debug) LOG.debug("Setting the {} EC initial organization events from the CC Organization Events. They are {}.",
                this.getOrganizationEvents().numberOfQueuedEvents(), this.getOrganizationEvents());
        this.setOrganizationEvents(this.controlComponent.getOrganizationEvents());
        this.planSelector = new SelfPlanSelector();

       LOG.info("\t..................EXITING SELF PERSONA(org={}, identifier={}, knowledge={}, focus={})",org, identifierString, knowledge, focus);
    }

    /**
     Constructs a new instance of an agent using the organization-based agent architecture. Each prosumer agent will have
     the capabilities needed to cc in a peer-based organization both as a peer and as a supervisor and contains its own
     knowledge about the immediate organization in which it participates.

     @param identifierString a string containing a name that uniquely
     */
    public SelfPersona(final String identifierString) {
        super(identifierString);
        LOG.info("\t..................CONSTRUCTING SELF PERSONA(identifier={}).", identifierString);

        this.setOrganizationEvents(this.controlComponent.getOrganizationEvents());
        this.planSelector = new SelfPlanSelector();

        LOG.info("\t..................EXITING SELF PERSONA(identifier={})",identifierString);
    }


    /**
     Removes the assignment task from the task queue.

     @param persona - the subagent assigned to the task
     @param assignedTask -
     */
    @Override
    public synchronized void doAssignmentTaskCompleted(final IPersona persona, final ITask assignedTask) {
        Objects.requireNonNull(persona, "IPersona cannot be null");
        Objects.requireNonNull(assignedTask, "assignmentTask cannot be null");

        this.taskManager.getTaskQueue().remove(assignedTask);
        final IOrganizationEvent organizationEvent = new OrganizationEvent(OrganizationEventType.EVENT,
                ACHIEVED_EVENT, assignedTask.getAssignment().getInstanceGoal(), null);
        final List<IOrganizationEvent> organizationEvents = new ArrayList<>();
        organizationEvents.add(organizationEvent);
        informControlComponent(organizationEvents);
    }

    /**
     Execute the behaviors required after failing a task.

     @param task - the assignment (Agent-Role-Goal) along with status information.
     */
    @Override
    public synchronized void doTaskFailed(final ITask task) {
        if (debug) LOG.debug("Entering assignmentTaskFailed() {}", String.format("Task \"%s\" Failed", task));
        this.taskManager.getTaskQueue().remove(task);
        final IOrganizationEvent organizationEvent = new OrganizationEvent(OrganizationEventType.TASK_FAILURE_EVENT,
                null, task.getAssignment().getInstanceGoal(), null);
        final List<IOrganizationEvent> organizationEvents = new ArrayList<>();
        organizationEvents.add(organizationEvent);
        informControlComponent(organizationEvents);
    }

    @Override
    public  void execute() {
        if (debug) LOG.debug("**** Entering execute(). {} assignments.",  assignments());
        while (isAlive()) {
            Player.step();
            /* first: update the ec */
            try {
                while (assignments() > 0) {
                    if (debug) LOG.debug("Number of New Assignments = {}", assignments());
                   Assignment a = this.pollAssignment();
                    if (debug) LOG.debug("new assignment={}", a);
                    Task t = new Task(a);
                    if (debug) LOG.debug("new task={}", t);
                    this.taskManager.addAssignedTask(t);
                    if (debug) LOG.debug("taskManager={}", taskManager);
                }
            } catch (Exception ex) {
                LOG.error("ERROR in EC execute Assignment processing.", ex);
                System.exit(-14);
            }

            try {
            /* second: remove the deassignments */
                while (deAssignments() > 0) {
                    if (debug) LOG.debug("Number of DeAssignments = {}", deAssignments());
                    this.taskManager.removeAssignments(this.pollDeAssignment());
                }
            } catch (Exception ex) {
                LOG.error("ERROR in EC execute deassignment processing.", ex);
                System.exit(-15);
            }


            ITask assignedTask = null;
            try {
            /* third: select & execute the highest priority task from queues */
                if (debug) LOG.debug("ASSIGNED TASK: Getting next assigned task.");
                assignedTask = this.taskManager.getNextAssignedTask();
                if (debug) LOG.debug("Assigned task is {}.", assignedTask);
            } catch (Exception ex) {
                LOG.error("ERROR in EC execute getNextAssignedTask.",ex);
                System.exit(-16);
            }
            try {
                if (assignedTask == null) {
                    //if (debug) LOG.debug("ASSIGNED TASK IS NULL: No assignment, calling endTurn() and executing CC plan()");
                    endTurn();
                } else if (assignedTask != null) {
                    LOG.info("Executing assigned task: {}", assignedTask);
                    this.executeTask(assignedTask);
                }
            } catch (Exception e) {
                LOG.error("ERROR EXECUTING ASSIGNED TASK assignedTask={}",assignedTask, e);
                System.exit(-17);
            }
        }
        if (debug) LOG.debug("exiting EC execute()..............................");
    }



    /**
     Execute the given task.

     @param task - the task to be executed.
     */
    @Override
    public synchronized void executeTask(ITask task) {
        if (debug) LOG.debug("Entering executeTask() {}",
                String.format("%s plays %s to achieve %s",
                        task.getAssignment().getAgent().getIdentifier(),
                        task.getAssignment().getRole()
                                .getIdentifier(), task.getAssignment().getInstanceGoal().getIdentifier()));

        final Agent<?> agent = task.getAssignment().getAgent();
        final InstanceGoal<?> goal = task.getAssignment().getInstanceGoal();
        final double goodnessScore = task.getAssignment().getRole().goodness(agent, goal, null);
        if (goodnessScore > RoleGoodnessFunction.MIN_SCORE) {
            if (debug) LOG.debug("---- executeTask() Inside real execution");
            IExecutablePlan executablePlan = task.getPlan();
            if (debug) LOG.debug("task.getPlan()={}",executablePlan);
            if (executablePlan == null) {
                // iExecutablePlan = this.taskManager.getTaskPlan(task);
                UniqueIdentifier roleIdentifier = Objects.requireNonNull(task.getAssignment().getRole().getIdentifier());
                if (debug) LOG.debug("Task role is {}.", roleIdentifier);
                UniqueIdentifier goalIdentifier = Objects.requireNonNull(task.getAssignment().getInstanceGoal().getSpecificationIdentifier());
                if (debug) LOG.debug("Task goal is {}.", goalIdentifier.toString());

                try {
                    executablePlan = new SelfPlanSelector().getPlan(roleIdentifier, goalIdentifier);
                    LOG.info("Selected plan for {} to achieve {} is {}.", roleIdentifier, goalIdentifier.toString(), executablePlan.toString());
                } catch (Exception e) {
                    LOG.error("Error getting plan from SelfPlanSelector.getPlan when role={} and goal={}", roleIdentifier.toString(), goalIdentifier.toString());
                    System.exit(-44);
                }
                if (executablePlan == null) {
                    LOG.error("Error: Plan is still null. Please create a plan for goal={}, role={}, task={}.",
                            task.getAssignment().getInstanceGoal(), task.getAssignment().getRole(), task.toString());
                    System.exit(-797);
                }
                try {
                    task.setExecutionPlan(executablePlan);
                }
                catch (Exception e){
                    LOG.error("Error setting execution plan.");
                    System.exit(-4);
                }
            }
            do {
                try {
                    if (debug)LOG.debug("------- Calling  executablePlan.execute( this={}, task.getAssignment().getInstanceGoal()={}) ", this, task.getAssignment().getInstanceGoal());
                    executablePlan.execute( this, task.getAssignment().getInstanceGoal());
                    endTurn();
                } catch (Exception ex) {
                    LOG.error("ERROR EXECUTING ASSIGNED TASK id={}, goal={}, role={}, plan={}, class={}, cause={}, trace={}",
                            this.identifierString, task.getAssignment().getInstanceGoal(), task.getAssignment().getRole(), task.getPlan(),
                            ex.getClass(), ex.getCause(),
                            ex.getStackTrace());
                    System.exit(-27);
                }
            } while (!executablePlan.isPreemptible( this));

            if (executablePlan.isDone()) {
                doAssignmentTaskCompleted(this, task);
            } else {
                this.taskManager.addAssignedTask(task);
            }
        } else {
            doTaskFailed(task);
        }
        if (debug) LOG.debug("Exiting executeTask().");
    }


    @Override
    public synchronized String toString() {
        return "Agent [identifierString=" + this.identifierString + "]";
    }


    @Override
    public void setPlanSelector(IPlanSelector planSelector) {
        this.planSelector = planSelector;
    }


}
