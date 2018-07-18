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
package edu.ksu.cis.macr.ipds.self;

import edu.ksu.cis.macr.aasis.agent.cc_m.IPersonaControlComponentMaster;
import edu.ksu.cis.macr.aasis.agent.persona.IPersona;
import edu.ksu.cis.macr.aasis.agent.persona.Organization;
import edu.ksu.cis.macr.aasis.agent.persona.SelfTurnCounter;
import edu.ksu.cis.macr.aasis.self.IInnerOrganization;
import edu.ksu.cis.macr.aasis.self.persona.ISelfPersona;
import edu.ksu.cis.macr.aasis.simulator.player.Player;
import edu.ksu.cis.macr.aasis.spec.OrganizationFocus;
import edu.ksu.cis.macr.aasis.types.IAgentType;
import edu.ksu.cis.macr.goal.model.InstanceParameters;
import edu.ksu.cis.macr.goal.model.InstanceTreeChanges;
import edu.ksu.cis.macr.ipds.self.capabilities.admin.AgentMaster;
import edu.ksu.cis.macr.ipds.self.guidelines.SelfGuidelineManager;
import edu.ksu.cis.macr.obaa_pp.events.OrganizationEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

/**
 Provides the reusable, recursive foundation for implementing multiply-governed,
 goal-directed intelligent cyber-physical systems (CPS).
 It has a set of associated specification files, including:
 -- an AgentGoalModel.goal the defines the agents goals and which can be configured to reflect differing degrees of
 selfishness or community interest.  -- an AgentRoleModel.role that describes the relationships between goals, roles,
 and capabilities.  -- an Agent.xml file that describes the types of sub-agents (called persona) including one master
 self persona and a persona for each affiliated organization.  -- an Environment.xml file (optional) specifying other
 objects (besides agents) that may be involved in the system.  -- an Initialize.xml file specifying the customizable
 goal guidelines describing who the agent should connect with and who their default master will be.   For
 standard organizational functionality, see {@code Organization}.   A holonic InnerOrganization may consist of
 multiple levels or may be a single organization or entity. The holonic nature allows an InnerOrganization to function as
 appropriate given both the possible physical connections and the temporal connections available.
 */
public class InnerOrganization extends Organization implements IInnerOrganization {
    private static final Logger LOG = LoggerFactory.getLogger(InnerOrganization.class);
    private static final boolean debug = false;
    private InnerOrganizationFactory factory;
    private IAgentType agentType;
    private IPersonaControlComponentMaster master = null;

    public InnerOrganization(final String absolutePath, final OrganizationFocus focus, final IAgentType agentType,
                             final String goalfile, final String rolefile, final String topGoal) {
        super(absolutePath, focus, goalfile, rolefile, topGoal);
        this.setOrganizationFactory(new InnerOrganizationFactory(this.getName(), OrganizationFocus.Agent));
        this.agentType = agentType;
        if (debug) LOG.info("Completed construction of {} Agent. ", this.getName());
    }


    public IAgentType getAgentType() {
        return agentType;
    }

    public void setAgentType(IAgentType agentType) {
        this.agentType = agentType;
    }

    public InnerOrganizationFactory getOrganizationFactory() {
        return this.factory;
    }

    public void setOrganizationFactory(final InnerOrganizationFactory factory) {
        this.factory = factory;
    }

    /**
     Load the initial top goal guidelines as defined in the Initialize.xml file.
     */
    @Override
    public void loadTopGoalGuidelines() {
        LOG.info("Entering loadTopGoalGuidelines() {}. " +
                        "The guidelines should match the top level goal parameters shown on the goal model.",
                this.getName());

        // get the source file (initialize.xml)

        String absPathToFile = this.getOrganizationSpecification().getGoalParametersFile();
        if (debug) LOG.debug("Initial top goal guidelines will be read from {}", absPathToFile);

        this.goalParameterValues = SelfGuidelineManager.getGuidelines(absPathToFile);
        LOG.info("Organization guidelines (i.e. top goal parameter values) set for {}. {}", this.getName(), this.goalParameterValues);
    }

    @Override
    public IPersonaControlComponentMaster getSelfControlComponentMaster() {
        LOG.info("Entering getSelfControlComponentMaster(). this InnerOrg={}",this);
        Collection<IPersona> allPersona = this.getAllPersona();
        LOG.info("allPersona={}",allPersona);

        // first find the master and get the org events
        for (IPersona p : allPersona) {
            if (debug) LOG.debug("Checking for master. This persona={}.",p);
            if (debug) LOG.debug("Persona {} cc type is {}.", p.getIdentifierString(), p.getPersonaControlComponent().getClass());
            //TODO: Change identification of CCM to a type, not a string
            if (p.getPersonaControlComponent().getClass().toString().contains("Master")) {
                if (debug)
                    LOG.debug("Found master persona {} cc type is {}.", p.getIdentifierString(), p.getPersonaControlComponent().getClass());
                master = (IPersonaControlComponentMaster) p.getPersonaControlComponent();
                this.setLocalMaster(p.getIdentifierString());
                return master;
            }
        }
        return null;
    }

    @Override
    public void initializeGoals() {
        InstanceTreeChanges changes = new InstanceTreeChanges();
        Collection<IPersona> allPersona = this.getAllPersona();

        for (IPersona p : allPersona) {
            if (p.getPersonaControlComponent().getClass().equals(ISelfPersona.class)) {
                IPersonaControlComponentMaster master = (IPersonaControlComponentMaster) p.getPersonaControlComponent();
                final InstanceParameters topParams = master.getTopGoalInstanceParameters();
                changes = master.getInitialGoalModelChangeList(topParams);
                master.updateInitialActiveGoals(changes);
                master.setInitialRoles();
            }
        }
        // after setting up the CC, copy the goal events to the EC
        this.setOrganizationEventsFromControlComponent();
    }

    /**
     Delegate loading agent information from XML to an {@code OrganizationInitializer} that understands XML.
     */
    @Override
    public void loadAgentFile() {
        if (debug)
            LOG.debug("Begin loading persona into self agent org {} with guidelines {}.", this.getName(), this.getGoalParameterValues());
        this.getOrganizationFactory().loadAgentFile(this.getOrganizationSpecification().getAgentFile(), this);
        LOG.info("Persona loaded into self agent org {}.", this.getName());
    }

    /**
     Delegate loading object information from XML to an {@code OrganizationInitializer} that understands XML.
     */
    public void loadObjectFile() {
        if (debug) LOG.debug("Begin loading objects into self agent org {}.", this.getName());
        // load the objects from the initial world state file
        if (this.getOrganizationSpecification().getObjectFile() != null && !this
                .getOrganizationSpecification().getObjectFile().isEmpty()) {
            InnerOrganizationFactory.loadObjectFile(this.getOrganizationSpecification()
                    .getObjectFile(), this);
        }
        if (debug) LOG.debug("Objects loaded into self agent org {}.", this.getName());
    }

    @Override
    public void run() {
        if (debug) LOG.debug("Entering run(). Turn {} of this inner organization {}.",
                SelfTurnCounter.getTurns(), this.getName());

        // this.setPaused(true);
        // this.setPaused(false);
        if (this.terminated.get()) {
            LOG.info("Terminated - cleaning up.");
            this.getTerminationCriteria().cleanUp();
            return;
        }
        if ((this.getTerminationCriteria() != null) && this.getTerminationCriteria().isAccomplished()) {
            for (IPersona p : getAllPersona()) {
                p.disable();
            }
            this.terminated.set(true);
            return;
        }

        Player.step();
        lockData();
        try {
            if (debug)
                LOG.debug("Getting the agents specified (see Agent.xml) in org {} under {}", this.getName(), this.getLocalMaster());
                    /*
             * reset executable capabilities as needed to indicate start of new turn
             * // will get all internal comm messages
             */
            for (IPersona ec : getAllPersona()) {
                LOG.debug("inner org, resetting ec={}", ec.getUniqueIdentifier());
                ec.reset();

            }
            /*
             * if there are new agents that are entering the simulation,
             * this is the time to add them
             */
            if (!this.personaWaitingToJoin.isEmpty()) {
                /*
                 * verify that agents that are in the queue can be
                 * placed physically
                 */
                Collection<IPersona> newParticipants = new ArrayList<>();
                while (!this.personaWaitingToJoin.isEmpty()) {
                    if (debug) LOG.debug("Agents waiting to join under {}.", this.getLocalMaster());
                    final IPersona persona =  this.personaWaitingToJoin.poll();
                    if (debug) LOG.info("Persona is waiting to join: {}.", persona.getIdentifierString());
                            /*
                     * if the simulation is just starting, then the
                     * agents have already been added to the
                     * instance; they just need to start a thread.
                     * otherwise, they need to be added to the
                     * instance (depends on if they can be added or
                     * not) and then start a thread for them
                     */
                    if (this.turns.get() == 0) {
                        newParticipants.add(persona);
                        AgentMaster cc=(AgentMaster)getSelfControlComponentMaster();
                        cc.register(persona);
                    } else {
                        if (addTangibleObject(persona)) {
                            newParticipants.add(persona);
                            this.agents.put(persona.getUniqueIdentifier(), persona);
                            AgentMaster cc=(AgentMaster)getSelfControlComponentMaster();
                            cc.register(persona);
                        }
                    }
                }

                /* create a new thread for every new persona in this organization - will call the initialization
                method for a CC Master or CC Slave  */
                for (final IPersona p : newParticipants) {
                    final Thread thread = new Thread(p, String.format("%1$30s",
                            this.getName() + "." + String.format("%1$15s", p.getUniqueIdentifier().toString())));
                    LOG.info("Starting new thread in {} for CC-EC pair {}-{}.",
                            this.getName(), p.getPersonaControlComponent().getLocalMaster(),
                            p.getUniqueIdentifier());
                    thread.start();
                }
            }
            Player.step();
            long t = this.turns.incrementAndGet();
            SelfTurnCounter.setTurns(t);
        } finally {
            unlockData();
        }
    }

    @Override
    public void setOrganizationEventsFromControlComponent() {
        Collection<IPersona> allPersona = this.getAllPersona();
        OrganizationEvents masterOrgEvents = null;

        // first find the master and get the org events
        for (IPersona p : allPersona) {

            if (p.getPersonaControlComponent().getClass().equals(ISelfPersona.class)) {
                IPersonaControlComponentMaster master = (IPersonaControlComponentMaster) p.getPersonaControlComponent();
                masterOrgEvents = (OrganizationEvents)master.getOrganizationEvents();
                if (masterOrgEvents == null) {
                    LOG.error("masterOrgEvents was null - will not be able to initialize EC org events");
                    System.exit(-55);
                }
                if (debug) LOG.debug("The SELF CC MASTER {}'s initial {} organization events were {}. Copying them to other " +
                        "agents.", master.getName(), masterOrgEvents.numberOfQueuedEvents(), masterOrgEvents);
            }
        }

        // then set them in all agents that aren't the master...
        for (IPersona p : allPersona) {
            if (p.getPersonaControlComponent().getClass().toString().contains("SelfControlComponentSlave")) {
                p.setOrganizationEvents(masterOrgEvents);
                if (debug) LOG.debug("The {} agent's {} initial organization events are now {} (copied from master).",
                        p.getIdentifierString(), p.getOrganizationEvents().getQueue().size(), p
                                .getOrganizationEvents());
            }

        }
    }

    public void setPaused() {
        Player.setPaused(false);
    }



}
