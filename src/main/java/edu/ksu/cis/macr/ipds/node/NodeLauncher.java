/**
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
package edu.ksu.cis.macr.ipds.node;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.util.StatusPrinter;
import edu.ksu.cis.macr.aasis.agent.cc_p.ConnectionModel;
import edu.ksu.cis.macr.aasis.agent.persona.SelfTurnCounter;
import edu.ksu.cis.macr.aasis.messaging.MessagingManager;
import edu.ksu.cis.macr.aasis.self.IInnerOrganization;
import edu.ksu.cis.macr.aasis.simulator.clock.Clock;
import edu.ksu.cis.macr.aasis.simulator.player.Player;
import edu.ksu.cis.macr.ipds.config.RunManager;
import edu.ksu.cis.macr.ipds.market.capabilities.participate.MarketCalculator;
import edu.ksu.cis.macr.ipds.self.InnerOrganizationFactory;
import edu.ksu.cis.macr.ipds.simulators.PhysicalSystemSimulator;
import edu.ksu.cis.macr.ipds.views.ConnectionView;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
/**
 This {@code NodeLauncher} starts a simulation for a set of SelfOrganization agents running at a single device location
 (in a single JVM) along with a simulation that will represent the physical sensors and actuators with which the system
 will interact.  A sample run.properties file might look like the following:  config=TC05
 configpath=/src/main/resources/configs topgoal=Support SelfOrganization uselivematlab=no
 datafile=matlab_data/TC62_UpdateTwoHourData.mat startdate=Aug 19 2013 starttime=11:00 AM stepdelay=1000 stepmode=yes
 maxTimeslices=0 planningHorizon_minutes=15
 */
public enum NodeLauncher {
    INSTANCE;
    private static final Logger LOG = LoggerFactory.getLogger(NodeLauncher.class);
    private static boolean stepInThisFile = false;  // change as desired to

    /**
     The main program that runs a single-location simulation.
     @param args the method arguments passed in. NOT USED.
     */
    public static void main(String[] args) {
        beginComplexMAS();
    }

    // test parts of the system

    private static void beginComplexMAS()  {

        // assume SLF4J bound to logback in the current environment
        Context lc = (Context) LoggerFactory.getILoggerFactory();

        // print logback's internal status
        StatusPrinter.print(lc);

        // delete any existing logfiles
        cleanLogFiles();

        // load user parameters for this simulation - includes scenario and
        // player information
        RunManager.load();
        step();

        // initialize and open the communication exchange for messaging
        MessagingManager.initialize();
        step();

        // open browser to the RabbitMQ local host website.
        OpenCommunicationsInBrowser(RunManager.getShowCommunicationsInBrowser());
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

        // set the simulation time to the first timeslice
        Clock.setTimeSlicesElapsedSinceStart(1);
        step();

        // show the application-specific organization connection view
        ConnectionModel.updateConnectionModel();
        ConnectionView.createConnectionView();
        step();

        // get the list of selfAgent folders
        final File[] selfAgentFolders = getTestCaseSelfFolders(RunManager.getAbsolutePathToTestCaseFolder());
        step();

        // set the number of organizations so we know when all organizations
        // can advance to the next turn
        SelfTurnCounter.setNumberOfOrganizations(selfAgentFolders.length);
        ConnectionModel.setNumberOfAgents(selfAgentFolders.length);

        // create the set of self agents based on the specifications provided
        final ArrayList<IInnerOrganization> allDeviceMAS = createAgentsAndCounts
                (selfAgentFolders);
        step();

        // set up the initial goal guidelines (must be done after the agents
        // are created)
        intializeGoals(allDeviceMAS);
        step();

        // load the environment objects from the environment files
        loadEnvironmentObjects(allDeviceMAS);
        step();

        // load the list of persona from the agent files
        loadPersona(allDeviceMAS);
        Player.step();


        // start all the self agents and run them in discrete time slices
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

    private static void OpenCommunicationsInBrowser(boolean CommInBrowser) {
        if (CommInBrowser) {
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
                final IInnerOrganization agentOrg = InnerOrganizationFactory.create(agentFolder.getAbsolutePath(), agentFolder.getName());
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
     Return a list of all self agent folders in this test case.
     @param testCaseFolder - the root test case folder
     @return File[] of subfolders for self agents
     */
    private static File[] getTestCaseSelfFolders(final String testCaseFolder) {
        final File[] hosts = getAllFoldersOnHost(new File(testCaseFolder));
        final ArrayList<File> list = new ArrayList<>();
        for (final File host : hosts) {
            LOG.debug("Getting folders from node {}", host.getAbsolutePath());

            final File[] allOrganizationFolders = getAllFoldersOnHost(host);
            LOG.debug("    {} agent folders found on node {}",
                    allOrganizationFolders.length, host);
            for (final File orgFolder : allOrganizationFolders) {
                if (orgFolder.getName().startsWith("self")) {
                    list.add(orgFolder);
                }
            }
        }
        final File[] selfFolders = list.toArray(new File[list.size()]);
        LOG.debug("Total: {} self agent folders found. First is {}",
                selfFolders.length, selfFolders[0].getName());
        return selfFolders;
    }

    /**
     Get all subfolders under a given device folder.
     @param hostDir - the File folder for the device (JVM)
     @return File[] with the subfolders
     */
    private static File[] getAllFoldersOnHost(final File hostDir) {
        final FileFilter selfFolderFilter = File::isDirectory;
        final File[] allOrganizationFolders = hostDir.listFiles(selfFolderFilter);
        return allOrganizationFolders;
    }

    /**
     Creates self organization agents and returns them as a list.
     @param agentFolders the File[] of self organization agent folders
     @return - a list
     */
    static ArrayList<IInnerOrganization> createSelfAgents(final File[] agentFolders) {
        LOG.info("\n\n========= CREATING ALL AGENTS FOR {} =========\n.", RunManager.getTestCaseName().toUpperCase());

        final ArrayList<IInnerOrganization> listSelfOrgs = new ArrayList<IInnerOrganization>();
        for (final File agentFolder : agentFolders) {
            try {
                final IInnerOrganization agentOrg =  InnerOrganizationFactory.create(agentFolder.getAbsolutePath(), agentFolder.getName());
                listSelfOrgs.add(agentOrg);
                RunManager.add(agentOrg.getAgentType(), agentOrg.getName());
            } catch (final Exception e) {
                LOG.error("ERROR: Could not initialize agent {}. {}",  agentFolder, e.getMessage());
            }
        }
        RunManager.displayCounts();
        LOG.info("==========  {} AGENTS CREATED FROM SPECIFICATION FILES  ==========\n\n", listSelfOrgs.size());
        return listSelfOrgs;
    }

    /**
     Initialize the goal models. Gets the top goal instance parameters, determines the initial change list of goal
     modifications, updates the initial active goals, and sets the roles.  We can do it all in the constructors where
     it's hidden, or do it out here in the open for testing, etc.
     @param list - the list of IAgentInternalOrganizations
     */
    static void intializeGoals(final ArrayList<IInnerOrganization> list) {
        for (final IInnerOrganization self : list) {
            self.loadTopGoalGuidelines();
        }
        LOG.info("============ {} INITIAL GOALS READY - READY TO CREATE " +
                "PERSONA THREADS AND RUN  " +
                "===========\n\n", list.size());
    }

    /**
     Load the environment objects in all the self organizations.
     @param list - the {@code ArrayList} of {@code SelfOrganization}.
     */
    static void loadEnvironmentObjects(final ArrayList<IInnerOrganization>
                                               list) {
        for (final IInnerOrganization self : list) {
            self.loadObjectFile();
        }
        LOG.info("========= {} AGENTS INITIALIZED WITH ENVIRONMENT " +
                "OBJECTS ==========\n\n", list.size());
    }

    /**
     Calls step. Allows shutting off the stepping just in the launcher file if stepInLauncher == false.
     */
    private static void step() {
        // change this as desired to test parts of the system
        if (stepInThisFile) {
            Player.step();
        }
    }

    /**
     Load the persona in all the self organizations.
     @param list - the {@code ArrayList} of {@code IAgentInternalOrganization}.
     */
    static void loadPersona(final ArrayList<IInnerOrganization> list) {
        for (final IInnerOrganization self : list) {
            self.loadAgentFile();
        }
        LOG.info("=========  {} AGENTS INITIALIZED WITH SUB " +
                "AGENT CONSTRUCTORS ==========\n\n", list.size());
    }

    @Override
    public String toString() {
        return "Launcher{" +
                "INSTANCE=" + INSTANCE +
                ", stepInLauncher=" + stepInThisFile +
                '}';
    }
}
