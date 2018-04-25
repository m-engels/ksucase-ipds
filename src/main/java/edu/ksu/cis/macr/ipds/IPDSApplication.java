package edu.ksu.cis.macr.ipds;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.util.StatusPrinter;
import edu.ksu.cis.macr.aasis.agent.cc_p.ConnectionModel;
import edu.ksu.cis.macr.aasis.agent.persona.SelfTurnCounter;
import edu.ksu.cis.macr.aasis.self.IInnerOrganization;
import edu.ksu.cis.macr.aasis.simulator.clock.Clock;
import edu.ksu.cis.macr.aasis.simulator.player.Player;
import edu.ksu.cis.macr.aasis.simulator.scenario.MessagingCheckpoint;
import edu.ksu.cis.macr.ipds.config.AgentType;
import edu.ksu.cis.macr.ipds.config.RunManager;
import edu.ksu.cis.macr.ipds.grid.roles.GridRoleIdentifiers;
import edu.ksu.cis.macr.ipds.market.capabilities.participate.MarketCalculator;
import edu.ksu.cis.macr.ipds.messaging.MessagingManager;
import edu.ksu.cis.macr.ipds.self.InnerOrganizationFactory;
import edu.ksu.cis.macr.ipds.simulators.PhysicalSystemSimulator;
import edu.ksu.cis.macr.ipds.views.ConnectionView;
import edu.ksu.cis.macr.ipds.views.OrganizationView;
import edu.ksu.cis.macr.obaa_pp.views.IOrganizationView;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;


/**
 * Starts a simulation of systems of intelligent systems, involving smart devices at distributed locations
 * along with a simulation of the physical environment (sensors and actuators) with which the system interacts.
 * See run.properties to set user-defined parameters.
 * */
public class IPDSApplication {
    private static final Logger LOG = LoggerFactory.getLogger(IPDSApplication.class);
    private static final boolean debug =  true;
    private static boolean stepInThisFile = false;

    public static void main(String[] args) {
        beginComplexMAS();
    }

    private static void beginComplexMAS()  {

        // assume SLF4J bound to logback in the current environment
        Context lc = (Context) LoggerFactory.getILoggerFactory();

        // print logback's internal status
        StatusPrinter.print(lc);

        // delete existing logfiles
        cleanLogFiles();

        // load user inputs
        RunManager.load();
        step();


        LOG.info("Market Suffix is {}",RunManager.getMarketSuffix());
        LOG.info("Self Admin package is {}", RunManager.getSelfAdminPackage());
        LOG.info("Absolute path to config is {}", RunManager.getAbsolutePathToMatLabCodeFolder());
        LOG.info("Test case name is {}", RunManager.getTestCaseName());


        // initialize communication exchange for messaging
        MessagingManager.initialize();
        step();

        // open browser to the RabbitMQ local host website.
        DisplayCommunicationsInBrowser(RunManager.getShowCommunicationsInBrowser());
        step();

        // initialize calculator for enabling matlab market calcs
        MarketCalculator.initialize(RunManager.getUseLiveMatLab(), RunManager.getAbsolutePathToMatLabCodeFolder(), RunManager.getTestCaseName());
        step();

        if (RunManager.getShowSensors()) {
            // initialize the physical system simulation of sensors and actuators
            PhysicalSystemSimulator.initialize();
            // display with initial set of sensor data and actuator settings at t=0
            PhysicalSystemSimulator.createAndShowSensorData();
        }

        // set the simulation time to first time slice
        Clock.setTimeSlicesElapsedSinceStart(1);
        step();

        // show overall connection view
        ConnectionModel.updateConnectionModel();
        ConnectionView.createConnectionView();

        // get list of agents
        final File[] selfAgentFolders = getTestCaseSelfFolders(RunManager.getAbsolutePathToTestCaseFolder());
        step();

        // set total number of agents
        SelfTurnCounter.setNumberOfOrganizations(selfAgentFolders.length);
        ConnectionModel.setNumberOfAgents(selfAgentFolders.length);

        // create agents based on specifications provided
        // if GIS GUI, agent locations could be displayed on map as they are created.
        ArrayList<IInnerOrganization> allDeviceMAS = createAgentsAndCounts(selfAgentFolders);
        step();

        // use the counts to create a set of scenario messaging checkpoints

        // initialize goal parameters (must be done after the agents are created)
        initializeGoalGuidelines(allDeviceMAS);
        step();

        // load environment objects from environment configuration files
        loadEnvironmentObjects(allDeviceMAS);
        step();

        // load persona from agent files
        loadPersona(allDeviceMAS);
        Player.step();

        // display inner organizations for each agent
        displayAgentOrganizations(allDeviceMAS, RunManager.getShowAgentOrganizations());
        Player.step();

        // start all self agents and run them in discrete time slices
        for (final IInnerOrganization self : allDeviceMAS) {
            self.run();
        }
    }

     private static void cleanLogFiles() {
        try {
            String curDir = System.getProperty("user.dir");
            FileUtils.cleanDirectory(new File(String.format("%s//logs", curDir)));
        } catch (Exception ex) {
            LOG.debug("Some logfiles in use. Cannot delete.");
        }
    }

    private static void DisplayCommunicationsInBrowser(boolean isShown) {
        if (isShown) {
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                    try {
                        java.net.URI uri = new java.net.URI("http://localhost:15672/");
                        desktop.browse(uri);
                    } catch (Exception e) {
                        // Handle exception
                    }
                }
            }
        }
    }

    /**
     * Return a list of all self agent folders in this test case.
     *
     * @param testCaseFolder - the root test case folder
     * @return File[] of subfolders for self agents
     */
    private static File[] getTestCaseSelfFolders(final String testCaseFolder) {
        if (debug) LOG.debug("Getting folders from node {}", testCaseFolder);

        final File[] devices = getAllFoldersOnDevice(new File(testCaseFolder));
        final ArrayList<File> list = new ArrayList<>();
        for (final File device : devices) {
            if (debug) LOG.debug("Getting folders from node {}", device.getAbsolutePath());

            final File[] allOrganizationFolders = getAllFoldersOnDevice(device);
            if (debug) LOG.debug("    {} agent folders found on node {}",
                    allOrganizationFolders.length, device);
            for (final File orgFolder : allOrganizationFolders) {
                if (orgFolder.getName().startsWith("self")) {
                    list.add(orgFolder);
                }
            }
        }
        final File[] selfFolders = list.toArray(new File[list.size()]);
        LOG.info("Total: {} self agent folders found. First is {}",
                selfFolders.length, selfFolders[0].getName());
        return selfFolders;
    }

    /**
     * Get all subfolders under a given device folder.
     *
     * @param deviceDir - the File folder for the device (JVM)
     * @return File[] with the subfolders
     */
    private static File[] getAllFoldersOnDevice(final File deviceDir) {
        final FileFilter selfFolderFilter = File::isDirectory;
        return deviceDir.listFiles(selfFolderFilter);
    }

    /**
     * Creates agents and returns them as a list.
     *
     * @param agentFolders the File[] of self organization agent folders
     * @return - an ArrayList<{@code}IAgentInternalOrganization}>
     */
    synchronized static ArrayList createAgentsAndCounts(final File[] agentFolders) {
        LOG.info("\n\n========= CREATING ALL AGENTS FOR {} =========\n.", RunManager.getTestCaseName().toUpperCase());

        final ArrayList<IInnerOrganization> listSelfOrgs = new ArrayList<>();
        for (final File agentFolder : agentFolders) {
            try {
                //  IPersonaOrganization agentOrg = SelfOrganizationFactory.create(agentFolder.getAbsolutePath(), agentFolder.getName());
                IInnerOrganization agentOrg = InnerOrganizationFactory.create(agentFolder.getAbsolutePath(), agentFolder.getName());
                LOG.debug("agentOrg={}", agentOrg);
                listSelfOrgs.add(agentOrg);
                RunManager.add(agentOrg.getAgentType(), agentOrg.getName());
            } catch (final Exception e) {
                LOG.error("ERROR: Could not initialize agent {}. {}", agentFolder, e.getMessage());
                throw e;
            }
        }
        RunManager.displayCounts();
        LOG.info("==========  {} AGENTS CREATED FROM SPECIFICATION FILES  ==========\n\n", listSelfOrgs.size());
        return listSelfOrgs;
    }

    /**
     * Initialize the goal models. Gets the top goal instance parameters, determines the initial change list of goal
     * modifications, updates the initial active goals, and sets the roles.  We can do it all in the constructors where
     * it's hidden, or do it out here in the open for testing, etc.
     *
     * @param allDeviceMAS - the list of IAgentOrganizations
     */
    synchronized static void initializeGoalGuidelines(final ArrayList<IInnerOrganization> allDeviceMAS) {
        for (IInnerOrganization deviceMASAgent : allDeviceMAS) {
            deviceMASAgent.loadTopGoalGuidelines();
        }
        LOG.info("========= {} INITIAL GOALS SET - READY TO CREATE PERSONA THREADS AND RUN  =========\n\n", allDeviceMAS.size());
    }

    /**
     * Load the environment objects in all the self organizations.
     *
     * @param allDeviceMAS - the {@code ArrayList} of {@code SelfOrganization}.
     */
    static void loadEnvironmentObjects( ArrayList<IInnerOrganization> allDeviceMAS) {
        for (IInnerOrganization self : allDeviceMAS) {
            self.loadObjectFile();
        }
        LOG.info("========= {} AGENTS INITIALIZED WITH ENVIRONMENT OBJECTS ==========\n\n", allDeviceMAS.size());
    }

    /**
     * Calls step. Allows shutting off the stepping just in the launcher file if stepInLauncher == false.
     */
    private static void step() {
        // change this as desired to test parts of the system
        if (stepInThisFile) {
            Player.step();
        }
    }

    /**
     * Load the persona in all the self organizations.
     *
     * @param allDeviceMAS - the {@code ArrayList} of {@code IAgentInternalOrganization}.
     */
    static void loadPersona( ArrayList<IInnerOrganization> allDeviceMAS) {
        for (IInnerOrganization agent : allDeviceMAS) {
            agent.loadAgentFile();
            LOG.info("\nEVENT: AGENT_INITIALIZED. =========  AGENT {} INITIALIZED WITH SUB AGENTS ===\n",agent.getName());

        }
        LOG.info("EVENT: AGENT_INITIALIZATION_COMPLETE. =========  {} AGENTS INITIALIZED WITH SUB AGENTS ==========\n\n", allDeviceMAS.size());
    }

    /**
     * Display the agents.
     *
     * @param allDeviceMAS - the {@code ArrayList} of {@code IOrganization}.
     * @param isShown - true if they should be displayed; false if not
     */
    static void displayAgentOrganizations(final ArrayList<IInnerOrganization> allDeviceMAS, boolean isShown) {
        if (isShown) {
            for (IInnerOrganization deviceMASAgent : allDeviceMAS) {
                if (debug) LOG.debug("Displaying {}.", deviceMASAgent);
                //    OrganizationView.createOrganizationView(deviceMASAgent);
                IOrganizationView view = OrganizationView.createOrganizationView(deviceMASAgent);
                LOG.debug("Created org view for {}.  If it stops here, please restart the simulation.", deviceMASAgent.getName());
            }
            LOG.info("================  {} AGENT DISPLAYS STARTED =============\n\n", allDeviceMAS.size());
        }
    }

    @Override
    public String toString() {
        return "Launcher{" +
                ", stepInThisFile=" + stepInThisFile +
                '}';
    }

    public synchronized void createCheckpointsForPowerUp() {
        // sensor home to home self
        MessagingCheckpoint.createMessagingCheckpoint(AgentType.Home, AgentType.Home, GridRoleIdentifiers.Manage_Home_Role, GridRoleIdentifiers.Self_Control_Role);

        // home self to home sub
        MessagingCheckpoint.createMessagingCheckpoint(AgentType.Home, AgentType.Home, GridRoleIdentifiers.Self_Control_Role, GridRoleIdentifiers.Be_Holon_Role);

        // home sub to neighborhood super
        MessagingCheckpoint.createMessagingCheckpoint(AgentType.Home, AgentType.Neighborhood, GridRoleIdentifiers.Be_Holon_Role, GridRoleIdentifiers.Be_Super_Holon_Role);

        // neighborhood super to neighborhood self
        MessagingCheckpoint.createMessagingCheckpoint(AgentType.Neighborhood, AgentType.Neighborhood, GridRoleIdentifiers.Be_Super_Holon_Role, GridRoleIdentifiers.Self_Control_Role);

        // neighborhood self to neighborhood sub
        MessagingCheckpoint.createMessagingCheckpoint(AgentType.Neighborhood, AgentType.Neighborhood, GridRoleIdentifiers.Self_Control_Role, GridRoleIdentifiers.Be_Holon_Role);

        // neighborhood sub to lateral super
        MessagingCheckpoint.createMessagingCheckpoint(AgentType.Neighborhood, AgentType.Lateral, GridRoleIdentifiers.Be_Holon_Role, GridRoleIdentifiers.Be_Super_Holon_Role);

        // lateral super to lateral self
        MessagingCheckpoint.createMessagingCheckpoint(AgentType.Lateral, AgentType.Lateral, GridRoleIdentifiers.Be_Super_Holon_Role, GridRoleIdentifiers.Self_Control_Role);

        // lateral self to lateral sub
        MessagingCheckpoint.createMessagingCheckpoint(AgentType.Lateral, AgentType.Lateral, GridRoleIdentifiers.Self_Control_Role, GridRoleIdentifiers.Be_Holon_Role);

        // lateral sub to lateral or feeder super
        MessagingCheckpoint.createMessagingCheckpoint(AgentType.Lateral, AgentType.Lateral, GridRoleIdentifiers.Be_Holon_Role, GridRoleIdentifiers.Be_Super_Holon_Role);
        MessagingCheckpoint.createMessagingCheckpoint(AgentType.Lateral, AgentType.Feeder, GridRoleIdentifiers.Be_Holon_Role, GridRoleIdentifiers.Be_Super_Holon_Role);

        // feeder super to feeder self
        MessagingCheckpoint.createMessagingCheckpoint(AgentType.Feeder, AgentType.Feeder, GridRoleIdentifiers.Be_Super_Holon_Role, GridRoleIdentifiers.Self_Control_Role);

        // feeder self to feeder sub
        MessagingCheckpoint.createMessagingCheckpoint(AgentType.Feeder, AgentType.Feeder, GridRoleIdentifiers.Self_Control_Role, GridRoleIdentifiers.Be_Holon_Role);

        // feeder sub to feeder or substation supers
        MessagingCheckpoint.createMessagingCheckpoint(AgentType.Feeder, AgentType.Feeder, GridRoleIdentifiers.Be_Holon_Role, GridRoleIdentifiers.Be_Super_Holon_Role);
        MessagingCheckpoint.createMessagingCheckpoint(AgentType.Feeder, AgentType.Substation, GridRoleIdentifiers.Be_Holon_Role, GridRoleIdentifiers.Be_Super_Holon_Role);

        // substation super to substation self
        MessagingCheckpoint.createMessagingCheckpoint(AgentType.Substation, AgentType.Substation, GridRoleIdentifiers.Be_Super_Holon_Role, GridRoleIdentifiers.Self_Control_Role);
    }
}
