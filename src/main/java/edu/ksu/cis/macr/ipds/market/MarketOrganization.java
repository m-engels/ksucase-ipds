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
package edu.ksu.cis.macr.ipds.market;


import edu.ksu.cis.macr.aasis.agent.cc_m.IPersonaControlComponentMaster;
import edu.ksu.cis.macr.aasis.agent.persona.IPersona;
import edu.ksu.cis.macr.aasis.agent.persona.Organization;
import edu.ksu.cis.macr.aasis.common.IConnectionGuidelines;
import edu.ksu.cis.macr.aasis.common.IConnections;
import edu.ksu.cis.macr.aasis.simulator.player.Player;
import edu.ksu.cis.macr.aasis.spec.OrganizationFocus;
import edu.ksu.cis.macr.goal.model.InstanceParameters;
import edu.ksu.cis.macr.goal.model.InstanceTreeChanges;
import edu.ksu.cis.macr.ipds.market.goals.MarketGoalParameters;
import edu.ksu.cis.macr.ipds.market.guidelines.MarketGuidelineManager;
import edu.ksu.cis.macr.ipds.market.guidelines.auction.AuctionGuidelinesBuilder;
import edu.ksu.cis.macr.ipds.market.guidelines.auction.IAuctionGuidelines;
import edu.ksu.cis.macr.ipds.market.guidelines.broker.BrokerGuidelinesBuilder;
import edu.ksu.cis.macr.ipds.market.guidelines.broker.IBrokerGuidelines;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
/**
 * The {@code ExternalOrganization} class provides the ability for agents to implement new organizations.  It has a set
 * of associated specification files, including:  -- a GoalModel.goal the defines the agents goals and which can be
 * configured to reflect differing degrees of selfishness or community interest.  -- a RoleModel.role that describes
 * the relationships between goals, roles, and capabilities.  -- an Agent.xml file that describes the types of
 * sub-agents (called persona) including one master subagent, and one persona for each affliated organization.  -- an
 * Environment.xml file (optional) specifying other objects (besides agents) that may be involved in the system.  -- an
 * Initialize.xml file specifying the customizable goal guidelines describing who the agent should connect with and who the
 * master will be.   For standard organizational functionality, see {@code Organization}.
 */
public class MarketOrganization extends Organization {
    private static final Logger LOG = LoggerFactory.getLogger(MarketOrganization.class);
    private static final boolean debug = false;

    private MarketHolonicLevel defaultMasterLevel;
    private MarketHolonicLevel masterLevel;


    /**
     * Constructs a new empty instance of an application-specific organization.
     *
     * @param folder         - the folder.
     * @param focus          - the enum defining what focus the organization is.
     * @param orgModelFolder - the folder containing the org models.
     */
    public MarketOrganization(final String folder, final OrganizationFocus focus, String orgModelFolder) {
        super(folder, focus, orgModelFolder);
        init();
    }

    public MarketOrganization(String folder, OrganizationFocus focus, String orgModelFolder, String goalFilePath, String roleFilePath, String topGoal) {
        super(folder, focus, orgModelFolder, goalFilePath, roleFilePath, topGoal);
        init();
    }

    @Override
    public synchronized void init() {
        this.setMasterLevel(MarketHolonicLevel.getOrganizationType(this.getName()));
        if (debug) LOG.debug("Exiting constructor: {}", this.getName());
    }

    public synchronized void setMasterLevel(MarketHolonicLevel masterLevel) {
        this.masterLevel = masterLevel;
    }

    /**
     * Returns the {@code Collection} of the {@code IAgent} immediate child prosumers in this {@code Organization}.
     *
     * @return the {@code Collection} of {@code IAgent}.
     */
    public   Collection<IPersona> getChildren() {
        if (debug) LOG.debug("{} total agents in {} organization.", this.getAllPersona().size(), this.getName());
        if (this.getAllPersona() == null) {
            LOG.error("counting children. this.getAllPersona() is NULL");
            System.exit(-55);
        }
        if (this.getAllPersona().isEmpty()) {
            LOG.error("counting children. this.getAllPersona() is EMPTY");
            System.exit(-56);
        }
        Collection<IPersona> c = this.getAllPersona().stream().filter(persona -> persona.getClass().getSimpleName().equals("HomePersona")).collect(Collectors.toList());
        return c;
    }




    /**
     * Load the initial top goal guidelines as defined in the Initialize.xml file.
     */
    public  void loadTopGoalGuidelines() {
        LOG.info("Begin reading top goal guidelines for {}. " +
                        "The guidelines should match the top level goal parameters shown on the goal model.",
                this.getName());

        // get the source file (initialize.xml)

        String absPathToFile = this.getOrganizationSpecification().getGoalParametersFile();
        if (debug) LOG.debug("Initial top goal guidelines will be read from {}", absPathToFile);

        this.goalParameterValues = MarketGuidelineManager.getGuidelines(absPathToFile);
        LOG.info("Organization guidelines (i.e. top goal parameter values) set for {}. {}", this.getName(), this.goalParameterValues);
    }

    /**
     * Load the initial goal guidelines as defined in the specification files.
     *
     * @param goalParameterValues - the map of the goal parameters (aka guidelines) provided
     */
    @Override
    public  void loadInitialGoalGuidelines(
            Map<UniqueIdentifier, Object> goalParameterValues) {
        if (debug) LOG.info("Begin setting guidelines for external org {}.", this.getName());
        if (debug) LOG.debug("Initial goal parameters set from {}", goalParameterValues);
        if (debug) LOG.debug("{} items in the goal parameter values key set.", goalParameterValues.size());

        // transfer in the collation provided
        for (UniqueIdentifier key : goalParameterValues.keySet()) {
            this.getGoalParameterValues().put(key, goalParameterValues.get(key));
            if (debug) LOG.debug("Getting input goal guideline. Key= {} Value={}", key, goalParameterValues.get(key));
        }

        IConnections childConnections = (IConnections) this.getGoalParameterValues().get(MarketGoalParameters.auctionConnections);
        // there may be more than one... get the one that has a value for combinedKW

        // get the folder for the default organization goal and role models from any child connection as well

        String orgModelFolder = "";
        double combinedKW = 0.0;
        for (IConnectionGuidelines cg : childConnections.getListConnectionGuidelines()) {
            if (!cg.getOrgModelFolder().isEmpty()) {
                orgModelFolder = cg.getOrgModelFolder();
            }
            if (cg.getCombinedKW() > combinedKW) {
                combinedKW = cg.getCombinedKW();
            }
        }
        if (combinedKW < 0.0001) {
            LOG.error("This organization has no power allocation. Nothing to do. ");
            System.exit(-28);
        }
        if (orgModelFolder.isEmpty()) {
            LOG.info("The folder under the standard models directory is not available. No default goal and role models for this organization can be used. A goal model and role model must be available with the Agent.xml file. ");
        } else {
            this.getOrganizationSpecification().setOrgModelFolder(orgModelFolder);
        }

        IAuctionGuidelines ag = AuctionGuidelinesBuilder.createAuctionGuidelines(); // defaults
        this.getGoalParameterValues().put(MarketGoalParameters.auctionGuidelines, ag);
        if (debug) LOG.info("Set auction guidelines for {} to {}.", this.getName(), ag);

        IBrokerGuidelines bg = BrokerGuidelinesBuilder.create();  // defaults all
        this.getGoalParameterValues().put(MarketGoalParameters.brokerGuidelines, bg);
        if (debug) LOG.info("Set broker guidelines for {} to {}.", this.getName(), bg);
    }

    @Override
    public  void initializeGoals() {
        AtomicReference<InstanceTreeChanges> changes = new AtomicReference<>(null);
        Collection<IPersona> allPersona = this.getAllPersona();

        allPersona.stream().filter(p -> p.getPersonaControlComponent().getClass().toString().contains("ControlComponentMaster")).forEach(p -> {
            IPersonaControlComponentMaster master = (IPersonaControlComponentMaster) p.getPersonaControlComponent();
            final InstanceParameters topParams = master.getTopGoalInstanceParameters();
            changes.set(master.getInitialGoalModelChangeList(topParams));
            master.updateInitialActiveGoals(changes.get());
            master.setInitialRoles();
        });
        // after setting up the CC, copy the goal events to the EC
        this.setOrganizationEventsFromControlComponent();
    }

    @Override
    public  void run() {
        if (debug) LOG.debug("Starting organization turn {} of external org {}.", turns, this.getName());
        if (this.terminated.get()) {
            if (debug) LOG.debug("Terminated - cleaning up.");
            this.getTerminationCriteria().cleanUp();
            return;
        }
        if ((this.getTerminationCriteria() != null) && this.getTerminationCriteria().isAccomplished()) {
            for (final IPersona p : getAllPersona()) {
                p.disable();
            }
            this.terminated.set(true);
            return;
        }
        lockData();
        try {
            if (debug) LOG.debug("Getting the agents specified (see Agent.xml) in external organization {} " +
                    "under local master {}", this.getName(), this.getLocalMaster());
                    /*
             * reset executable capabilities as needed to indicate start of new turn
             */
            if (this.getAllPersona() == null) {
                LOG.error("this.getAllPersona() is NULL");
                System.exit(-55);
            }
            if (this.getAllPersona().isEmpty()) {
                LOG.error("this.getAllPersona() is EMPTY");
                System.exit(-56);
            }
            if (debug)
                LOG.debug("There are {} agents in this {} organization.", getAllPersona().size(), this.getName());

            for (final IPersona agent : getAllPersona()) {
                agent.reset();
            }
            getChildren();  // just sets the number for now.
            /*
             * if there are new agents that are entering the simulation,
             * this is the time to add them
             */
            if (!this.personaWaitingToJoin.isEmpty()) {
                /*
                 * verify that agents that are in the queue can be
                 * placed physically
                 */
                final ArrayList<IPersona> newParticipants = new ArrayList<>();
                while (!this.personaWaitingToJoin.isEmpty()) {
                    if (debug) LOG.debug("Agents waiting to join external organization {}.", this.getLocalMaster());
                    final IPersona persona = this.personaWaitingToJoin.poll();
                    if (debug) LOG.info("Waiting to join organization {} persona: {}.", this.getName(),
                            persona.getIdentifierString());
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
                    } else {
                        if (addTangibleObject(persona)) {
                            newParticipants.add(persona);
                            this.agents.put(persona.getUniqueIdentifier(), persona);
                        }
                    }
                }

                /* create a new thread for every new persona in this organization - will call the initialization
                method for a CC Master or CC Slave  */
                for (final IPersona p : newParticipants) {
                    // all participants get a thread or they don't show up on the board
                    // if (persona.getControlComponent().getClass().getSimpleName().toString().equals
                    // ("ControlComponentMaster")) {
                    final Thread thread = new Thread(p, String.format("%1$30s",
                            this.getName() + "." + String.format("%1$15s", p.getUniqueIdentifier() + "ORG")));
                    if (debug) LOG.info("ORG: CC local master = {} this persona = {}",
                            p.getPersonaControlComponent().getLocalMaster(), p.getUniqueIdentifier());
                    if (debug)
                        LOG.info("Starting new participant thread in external organization {} for CC-EC pair {}-{}.",
                                this.getName(), p.getPersonaControlComponent().getLocalMaster(),
                                p.getUniqueIdentifier());
                    thread.start();

                    //}
                }
            }
            Player.step();
            this.turns.incrementAndGet();
        } finally {
            unlockData();
        }
    }

    public  void initialize(final File folder) {
        if (debug) LOG.info("Initializing external org {}", folder.getName());
        // the specification gets initialized with the general organization
        if (this.getOrganizationSpecification() == null) {
            LOG.error("ERROR: Specification cannot be null");
            System.exit(-8);
        }
        this.getOrganizationSpecification().verifyAgentFile();
        // get the organization name, typically something like selfN43 or orgN43
        this.setName(folder.getName());
    }
}