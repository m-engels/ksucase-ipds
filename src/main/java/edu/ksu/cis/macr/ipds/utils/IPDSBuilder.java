package edu.ksu.cis.macr.ipds.utils;

import edu.ksu.cis.macr.ipds.config.GridHolonicLevel;
import edu.ksu.cis.macr.ipds.config.MarketHolonicLevel;
import edu.ksu.cis.macr.ipds.config.RunManager;
import edu.ksu.cis.macr.ipds.market.guidelines.broker.BrokerGuidelinesBuilder;
import edu.ksu.cis.macr.obaa_pp.cc_cip.IdentifierProviderBuilder;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Builds agent and initialize XML configuration files.  Use capability interface classes where available on role models.
 * Use implementation classes in agent XML configuration files, as it will need to find the constructor values.
 * IInternalCommunicationCapability must be included in every role in all role models.
 * But InternalCommunicationCapability SHOULD NOT be included in the agent.xml.
 * Iterative process (for each new organization): Build org goal model. Build org role model. Add code as described in
 * readme.txt. Update builder to generate agent.xml to get the necessary persona. Update builder to generate necessary
 * Initialize.xml with connections and guidelines.
 * This class is custom built for each simulation to auto-generate the XML files.
 * It is not required if the XML is created by some other means.
 */
public class IPDSBuilder {
    //TODO: In thesis, 1) goal model 2) role model for orgs and agents.
    //TODO: In thesis, RM for org --> RM for agents.
    //TODO: RM for agents --> equip agent build utility. ParticipateCapability only in agent persona, not org persona.
    //TODO: In thesis, add the algorithm for equipping agents.
    //TODO: In thesis, add the process to map capabiltiies to their packages - include reasoning/logic.
    //TODO: Put assessreactivepower and assessreactivepowerquality together.

    private static final Logger LOG = LoggerFactory.getLogger(IPDSBuilder.class);
    private static final boolean debug =  true;
    private static final String CUR_DIR = System.getProperty("user.dir");

    private static final Double MIN_CRITICAL_KW = RunManager.getMinCritical_kw();
    private static final Double MAX_CRITICAL_KW = RunManager.getMaxCritical_kw();

    private static final String SELF_ADMIN_PACKAGE = "edu.ksu.cis.macr.ipds.self.capabilities.admin";
    private static final String SELF_TYPES_PACKAGE = "edu.ksu.cis.macr.ipds.self.persona.types";

    private static final String AGENT_PARTICIPATE_PACKAGE = "edu.ksu.cis.macr.ipds.primary.capabilities.participate";
    private static final String AGENT_TYPES_PACKAGE = "edu.ksu.cis.macr.ipds.primary.persona.types";

    private static final String GRID_SUFFIX = "";
    private static final String GRID_MODEL_FOLDER = "grid";
    private static final String GRID_PARTICIPATE_PACKAGE ="edu.ksu.cis.macr.ipds.grid.capabilities.participate";
    private static final String GRID_ADMIN_PACKAGE = "edu.ksu.cis.macr.ipds.grid.capabilities.admin";
    private static final String GRID_TYPES_PACKAGE = "edu.ksu.cis.macr.ipds.grid.persona.types";

    private static final String MARKET_SUFFIX = "A";
    private static final String MARKET_MODEL_FOLDER = "market";
    private static final String MARKET_PARTICIPATE_PACKAGE = "edu.ksu.cis.macr.ipds.market.capabilities.participate";
    private static final String MARKET_ADMIN_PACKAGE = "edu.ksu.cis.macr.ipds.market.capabilities.admin";
    private static final String MARKET_TYPES_PACKAGE = "edu.ksu.cis.macr.ipds.market.persona.types";

    //TODO: Greg - these have the getters, but they values don't appear to be set yet....
    //TODO: Coding note - use full, correctly spelled variable names. We want long-term readability.

//    private static final String SELF_ADMIN_PACKAGE = RunManager.getSelfAdminPackage();
//    private static final String SELF_TYPES_PACKAGE = RunManager.getSelfTypesPackage();
//
//    private static final String AGENT_PARTICIPATE_PACKAGE = RunManager.getParticipatePackage();
//    private static final String AGENT_TYPES_PACKAGE = RunManager.getAgentTypesPackage();
//
//    private static final String GRID_SUFFIX = RunManager.getGridSuffix();
//    private static final String GRID_MODEL_FOLDER = RunManager.getGridModelFolder();
//    private static final String GRID_PARTICIPATE_PACKAGE = RunManager.getGridParticipatePackage();
//    private static final String GRID_ADMIN_PACKAGE = RunManager.getGridAdminPackage();
//    private static final String GRID_TYPES_PACKAGE = RunManager.getGridTypesPackage();
//
//    private static final String MARKET_SUFFIX = RunManager.getMarketSuffix();
//    private static final String MARKET_MODEL_FOLDER = RunManager.getMarketModelFolder();
//    private static final String MARKET_PARTICIPATE_PACKAGE = RunManager.getMarketParticipatePackage();
//    private static final String MARKET_ADMIN_PACKAGE = RunManager.getMarketAdminPackage();
//    private static final String MARKET_TYPES_PACKAGE = RunManager.getMarketTypesPackage();

    private static HashMap<Integer, String> map = new HashMap<>();
    private static TreeMap<String, String> parentMap;
    private static TreeMap<String, ArrayList<String>> childMap = new TreeMap<String, ArrayList<String>>();
    private static String testCase = "568";   // <--- Set a single test case here
    private static String csvFile;
    private static Random rand = new Random();

    private static void addAuctionGuidelinesRandom(Document doc, Node rootElement) {
        Element conn = doc.createElement("auctionGuidelines");
        double buy = getRandomBetween(200, 400) / 100.0;
        double sell = buy * 1.1;
        conn.setAttribute("desiredSellPrice_centsperkWh", Double.toString(sell));
        conn.setAttribute("desiredBuyPrice_centsperkWh", Double.toString(buy));
        conn.setAttribute("sellPriceFlexibilityPercent", Integer.toString(getRandomBetween(0, 15)));
        conn.setAttribute("buyPriceFlexibilityPercent", Integer.toString(getRandomBetween(0, 15)));
        rootElement.appendChild(conn);
    }

    private static int getRandomBetween(final int min, final int max) {
        Random random = new SecureRandom();
        return random.nextInt(max - min + 1) + min;
    }

    private static void addCap(Document doc, Node agents, String path, String thisCapability) {
        Element capability = doc.createElement("capability");
        capability.setAttribute("type", thisCapability);
        capability.setAttribute("package", path);
        agents.appendChild(capability);
    }

    public static void main(String argv[]) {
        createAllTestCases();
    }

    private static void createAllTestCases() {
        createSimulationInputFiles("02");
        createSimulationInputFiles("05");
        createSimulationInputFiles("06");
        createSimulationInputFiles("21");
        createSimulationInputFiles("59");
        createSimulationInputFiles("62");
        createSimulationInputFiles("560");
        createSimulationInputFiles("568");
        createSimulationInputFiles("462");
    }

    private static void createSimulationInputFiles(String testCaseNumberString) {
        LOG.info("Starting test case {}", testCaseNumberString);
        testCase = testCaseNumberString;
        map = new HashMap<Integer, String>();  //e.g. 1, "S1"
        parentMap = new TreeMap<>();
        childMap = new TreeMap<String, ArrayList<String>>();
        String abs_path_test_case;
        abs_path_test_case = CUR_DIR + "/src/main/resources/configs/TC" + testCase;
        csvFile = CUR_DIR + "/src/main/resources/configs/TC" + testCase + "/devices.csv";

        // read the csv file with parent info; populate the map
        readDeviceFile(csvFile);

        LOG.info("There are {} parents (or organizations) in the device list.", childMap.keySet().size());
        LOG.info("There are {} devices (or agents) in the device list.", map.values().size());

        LOG.info("========== Parent: Children Summary for TC{}. ========================\n\n", testCase);
        LOG.info("{} devices: summary", map.values().size());
        for (String parent : childMap.keySet()){
            LOG.info("{} \t has children: {}.", parent, childMap.get(parent));
        }

        LOG.info("========== Creating device folders for TC{}. ========================\n\n", testCase);

        createDeviceFoldersIfNeeded(abs_path_test_case);

        LOG.info("========== Device folders created for TC{}. ========================\n\n", testCase);


        File[] selfAgentFolders = getTestCaseSelfFolders(abs_path_test_case);
        LOG.debug("Got self agent folders={}.", Arrays.toString(selfAgentFolders));

        // create the Agent.xml for each self agent
        createAllAgentXML(selfAgentFolders);

        // create the Initialize.xml for each self agent
        createAllInitializeXML(selfAgentFolders);

        if (!testCase.equals("01")) {
            File[] gridOrgFolders = createGridOrgFolders(selfAgentFolders, GRID_SUFFIX);
            File[] marketOrgFolders = createMarketOrgFolders(selfAgentFolders, MARKET_SUFFIX);

            // create the Agent.xml for each external organization
            createAllAgentXMLForOrgs("GRID", GRID_SUFFIX, gridOrgFolders);
            createAllAgentXMLForOrgs("MARKET", MARKET_SUFFIX, marketOrgFolders);
        }
        LOG.info("========== Generated files for TC{}. ========================\n\n", testCase);

    }

    /*
     Create the device folders for each test case (e.g. "S1")
     */
    private static void createDeviceFoldersIfNeeded(String abs_path_test_case) {
       for (String device : map.values()){
           String folder = abs_path_test_case + "/"+ device;
           File f = new File(folder);
           if(!f.exists()){
               LOG.info("Creating {}", f);
               f.mkdir();
           }
           File selfFolder = new File(folder +"/self"+device);
           if(!selfFolder.exists()){
               LOG.info("Creating {}", selfFolder);
               selfFolder.mkdir();
           }
       }
    }

    /*
    * Return a list of all self agent folders in this test case.
     *
      * @param testCaseFolder - the root test case folder
    * @return File[] of subfolders for self agents
    */
    private static File[] getTestCaseSelfFolders(final String testCaseFolder) {
        LOG.debug("Entering getTestCaseSelfFolders(testCaseFolder={}", testCaseFolder);
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
        if (debug) LOG.debug("Total: {} self agent folders found. First is {}",
                selfFolders.length, selfFolders[0].getName());
        LOG.debug("Exiting getTestCaseSelfFolders: selfFolders={}", selfFolders.toString());
        return selfFolders;
    }

    /**
     * Delete the device folders in this test case.
     *
     * @param testCaseFolder - the root test case folder
     */
    private static void  deleteDeviceFolders(final String testCaseFolder) {
        LOG.debug("Entering getTestCaseSelfFolders(testCaseFolder={}", testCaseFolder);
        final File[] devices = getAllFoldersOnDevice(new File(testCaseFolder));
        for (final File device : devices) {
            try {
                IPDSBuilder.deleteRecursive(device);
            }
            catch (FileNotFoundException e) {
                LOG.error("Error FileNotFoundException:", e);
            }
        }
    }

    /**
     * Create the device folders for this test case based on devices.csv.
     *
     * @param testCaseFolder - the root test case folder

     */
    private static void  createDeviceFolders(final String testCaseFolder) {
        LOG.debug("Entering getTestCaseSelfFolders(testCaseFolder={}", testCaseFolder);
        final File[] devices = getAllFoldersOnDevice(new File(testCaseFolder));
        for (final File device : devices) {
            try {
                IPDSBuilder.deleteRecursive(device);
            }
            catch (FileNotFoundException e) {
                LOG.error("Error FileNotFoundException:", e);
            }
        }
    }


    /**
     * Delete all folders and files under the given path.
     * @param path - the folder that will remain.
     * @return true if successful
     * @throws FileNotFoundException
     */
    public static boolean deleteRecursive(File path) throws FileNotFoundException{
        if (!path.exists()) throw new FileNotFoundException(path.getAbsolutePath());
        boolean ret = true;
        if (path.isDirectory()){
            for (File f : path.listFiles()){
                ret = ret && deleteRecursive(f);
            }
        }
        return ret && path.delete();
    }


    private static void deleteFoldersBeforeRebuild(File[] allOrganizationFolders) {
        for (final File org : allOrganizationFolders) {
            try {

                // delete mistakes
                if (org.getAbsolutePath().endsWith("A") || org.getAbsolutePath().contains("self")) {
                    LOG.debug("deleting folder={}", org);
                    String[] entries = org.list();
                    for (String s : entries) {
                        File currentFile = new File(org.getPath(), s);
                        currentFile.delete();
                    }
                    org.delete();
                }

            } catch (final Exception e) {
                LOG.error("ERROR: deleting folder which will be rebuilt. org ={}.", org, e);
                System.exit(-4);
            }
        }
        LOG.info(" AGENT XML files deleted  ==========");
    }

    private static File createNewSelfFolder(File org) {
        File f = new File(org.getAbsolutePath().replace("org", "self"));
        f.mkdir();
        LOG.debug("Created folder={}", f);
        return f;
    }

    private static File createNewMarketFolder(File org) {
        File f = new File(org.getAbsolutePath() + "A");
        f.mkdir();
        LOG.debug("Created folder={}", f);
        return f;
    }

    private static void clean(File marketFolder) throws IOException {
        FileUtils.cleanDirectory(marketFolder);
        LOG.debug("Cleaned folder={}", marketFolder);
    }

    /**
     * Get all subfolders under a given device folder.
     *
     * @param deviceDir - the File folder for the device (JVM)
     * @return File[] with the subfolders
     */
    private static File[] getAllFoldersOnDevice(final File deviceDir) {
        LOG.debug("Entering getAllFoldersOnDevice(deviceDir={}", deviceDir);
        final FileFilter selfFolderFilter = File::isDirectory;
        File[] result = deviceDir.listFiles(selfFolderFilter);
        LOG.debug("Exiting getAllFoldersOnDevice: result={}", Arrays.toString(result));
        return result;
    }

    private static void readDeviceFile(final String csvFile) {
        LOG.info("Entering readDeviceFile(csvFile={})", csvFile);
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(csvFile));
            String cvsSplitBy = ",";
            String line = "";
            while ((line = br.readLine()) != null) {
                // use comma as separator
                LOG.debug("Reading {}", line);
                String[] oneLine = line.split(cvsSplitBy);
                String istr = oneLine[0];
                if (!istr.equals("")) {
                    // ignore the first row as it just has header information
                    if (istr.equals("id")) continue;
                    String possibleNull = oneLine[1];

                    // top element has no parents
                    if (istr.equals("1") || possibleNull.toUpperCase().equals("NULL")) {
                        Integer iAgent = Integer.valueOf(istr);
                        String agentName = oneLine[2];
                        map.put(iAgent, agentName);
                        if (debug) LOG.debug("Adding first element. Iagent = {} and name is {}.", iAgent, agentName);
                    } else {
                        // the rest have exactly one parent
                        Integer iAgent = Integer.valueOf(istr);
                        Integer iParent = Integer.valueOf(oneLine[1]);
                        String parent = map.get(iParent);
                        String agentName = oneLine[2];
                        map.put(iAgent, agentName);
                        if (debug)
                            LOG.debug("Adding element. Iagent = {} and name is {}. The parent id is {} ({}).", iAgent, agentName, iParent, parent);
                        parentMap.put(agentName, parent);

                        // check for existing children
                        ArrayList<String> myParentsChildren = childMap.get(parent);
                        if (myParentsChildren == null) myParentsChildren = new ArrayList<>();
                        myParentsChildren.add(agentName);  // add me to my parents child list
                        childMap.put(parent, myParentsChildren);
                        LOG.info("Adding {} to my parent's children. Now {} has children: {}.", agentName, parent, childMap.get(parent));
                    }
                }
            }
            LOG.info("Exiting readDeviceFile.");
        } catch (IOException e) {
            LOG.error("ERROR reading parent csv file. {}", e.getMessage());
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    LOG.error("ERROR closing parent csv file. {}", e.getMessage());
                }
            }
        }
    }

    /**
     * Creates Agent.xml files
     *
     * @param selfAgentFolders the File[] of self organization agent folders
     */
    static void createAllAgentXML(final File[] selfAgentFolders) {
        LOG.debug("Entering createAllAgentXML(selfAgentFolders={}", selfAgentFolders.toString());
        for (final File folder : selfAgentFolders) {
            try {
                FileUtils.cleanDirectory(folder);
                createAgentXML(folder);
                if (debug) LOG.debug(" AGENT XML file created in {}.", folder);
            } catch (final Exception e) {
                LOG.error("ERROR: creating Agent.xml for {}. {}.", folder, e.getCause().toString());
                System.exit(-4);
            }
        }
        LOG.debug("Exiting createAllAgentXML:");
        LOG.info(" AGENT XML files created  ==========");
    }

    /**
     * Logic to create agent persona based on folder name. Includes a call to look up the parent.
     *
     * @param folder - the folder where the specification files reside
     * @throws TransformerException         - Handles any Transformer Exceptions
     * @throws ParserConfigurationException - Handles any ParserConfiguration Exceptions
     */
    public static void createAgentXML(File folder) throws TransformerException, ParserConfigurationException {
        LOG.debug("Entering createAgentXML(folder={}", folder.getAbsolutePath());
        try {
            String shortName = folder.getName();
            String shortNameNoSelf = shortName.replace("self", "");
            GridHolonicLevel ot = GridHolonicLevel.getOrganizationType(shortNameNoSelf);
            String firstLetter = shortNameNoSelf.substring(0, 1).toUpperCase();

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("environment");
            doc.appendChild(rootElement);

            // add the self cc master persona - external-facing
            addSelfPersonaMaster(shortName, firstLetter, doc, rootElement, "master");

            // add the self cc worker persona - the autonomous private self
            addSelfPersonaWorker(shortName, firstLetter, doc, rootElement, "slave");

            //=====================  GRID CONTROL =======================================

            // add a persona to connect to children in my local grid control organization
            addPersonaSuperHolon(shortName, firstLetter, doc, rootElement);

            // add a persona to connect to my parent in my parent's organization
            if (!shortNameNoSelf.toUpperCase().equals("S1")) {
                String parent = parentMap.get(shortNameNoSelf);
                if (!(parent == null)) {
                    if (debug) LOG.debug("Adding a participant in my parent's organization {}.", parent);
                    addPersonaHolon(shortName, firstLetter, doc, rootElement, parent);
                }
            }
            //========================= MARKET =====================================

            // add a persona to connect to children in my local power market organization
            String marketShortName = shortName + MARKET_SUFFIX;

            // if not a home, then add a broker..............
            if (firstLetter.equals("N")|| firstLetter.equals("L")) {
                addPersonaBroker(marketShortName, firstLetter, doc, rootElement);
            }
            // add a persona to connect to my parent in my parent's organization
           // if (!shortNameNoSelf.toUpperCase().equals("S1")) {
                if (firstLetter.equals("H")|| firstLetter.equals("N")) {
                String parent = parentMap.get(shortNameNoSelf);
                if (!(parent == null)) {
                    String marketParent = parent + MARKET_SUFFIX;
                    if (debug) LOG.debug("Adding a participant in my parent's organization {}.", parent);
                    addPersonaMarketParticipant(marketShortName, firstLetter, doc, rootElement, marketParent);
                }
            }
            //=================================================================
            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            Source source = new DOMSource(doc);
            String fname = folder + "/Agent.xml";
            Result result = new StreamResult(new File(fname));
            if (debug) LOG.debug("Writing xml to {}.", fname);

            // Output to console for testing
            StreamResult screenout = new StreamResult(System.out);
            transformer.transform(source, result);
        } catch (ParserConfigurationException | TransformerException pce) {
            LOG.error("Error creating agent.xml file. {}", pce.getMessage());
            throw pce;
        }
    }

    private static void addSelfPersonaMaster(String shortName, String firstLetter, Document doc, Node rootElement, String personaType) {
        LOG.debug("Entering addSelfPersonaMaster(shortName={}", shortName);
        Element agents = doc.createElement("agents");
        String myLevel = getLevelType(firstLetter);
        // set attributes to agents element
        agents.setAttribute("type", myLevel + "SelfPersona");  //
        agents.setAttribute("package", SELF_TYPES_PACKAGE);  // 3

        // organization elements
        Element organization = doc.createElement("organization");
        organization.setAttribute("type", "AgentMaster");
        organization.setAttribute("package", SELF_ADMIN_PACKAGE);
        agents.appendChild(organization);

        // agent elements
        Element agent = doc.createElement("agent");
        // the self ccm is just the simple name (without the word self) - the external-facing name, e.g. "N43"
        // agent.appendChild(doc.createTextNode(shortName.replace("self", "")));
        agent.appendChild(doc.createTextNode(shortName));
        agents.appendChild(agent);
        equipSelfMaster(doc, agents, myLevel);
        rootElement.appendChild(agents);
    }

    private static String getLevelType(String firstLetter) {
        switch (firstLetter) {
            case "S":
                return "Substation";
            case "F":
                return "Feeder";
            case "L":
                return "Lateral";
            case "N":
                return "Neighborhood";
            case "H":
                return "Home";
            default:
                LOG.error("The type of organization could not be determined from input ({}). ", firstLetter);
                System.exit(-42);
        }
        return null;
    }

    private static void equipSelfMaster(Document doc, Element agents, String myLevel) {
        addCap(doc, agents, "GridConnectCapability");
        addCap(doc, agents, "MarketConnectCapability");
        addCap(doc, agents, "AuctionCommunicationCapability");
        addCap(doc, agents, "SelfControlCapability");
        addCap(doc, agents, "PowerCommunicationCapability");
        addCap(doc, agents, "DateTimeCapability");
    }

    private static void addCap(Document doc, Element agents, String implementationClass) {
        String path = IdentifierProviderBuilder.getCapabilityPackage(implementationClass);
        addCap(doc, agents, path, implementationClass);
    }

    private static void addSelfPersonaWorker(String shortName, String firstLetter, Document doc, Node rootElement, String personaType) {
        LOG.debug("Entering addSelfPersonaWorker(shortName={}", shortName);
        Element agents = doc.createElement("agents");
        String myLevel = getLevelType(firstLetter);

        agents.setAttribute("type", myLevel + "Persona");  //
        agents.setAttribute("package", AGENT_TYPES_PACKAGE);  // 3 tier

        // organization elements
        Element organization = doc.createElement("organization");
        addSelfParticipant(organization);
        agents.appendChild(organization);

        // agent elements
        Element agent = doc.createElement("agent");
        // the self ccm is just the simple name (without the word self) - the external-facing name, e.g. "N43"
        // the self cc slave is the autonomous worker - the internal, private, secure self, e.g. selfN43
        agent.appendChild(doc.createTextNode(shortName.replace("self", "")));
        agents.appendChild(agent);
        equipSelfWorker(shortName, doc, agents, myLevel);
        rootElement.appendChild(agents);
    }

    private static void addSelfParticipant(Element organization) {
        LOG.debug("Entering addSelfParticipant(organization={}", organization);
        organization.setAttribute("type", "EmptyControlComponent");
        organization.setAttribute("package", "edu.ksu.cis.macr.ipds.primary.persona");
    }

    private static void equipSelfWorker(String shortName, Document doc, Element agents, String myLevel) {
        LOG.debug("Entering addSelfParticipant(shortName={}", shortName);
        addCap(doc, agents, "ParticipateCapability");
        addCap(doc, agents, "AutonomousOperationCapability");
        addCap(doc, agents, "AssessReactivePowerQualityCapability");
        addCap(doc, agents, "AssessReactivePowerCapability");
        addCap(doc, agents, "PowerCommunicationCapability");
        addCap(doc, agents, "DateTimeCapability");
        addCap(doc, agents, "Manage" + myLevel + "Capability");
        if (myLevel.equals("Home")) {
            addCapabilitySmartInverter(doc, agents, "SmartInverterCapability", shortName);
            addCapabilitySmartMeter(doc, agents, "SmartMeterCapability", shortName);
        }
    }

    private static void addCapabilitySmartInverter(Document doc, Node agents, String thisCapability, String shortName) {
        LOG.debug("Entering addCapabilitySmartInverter(thisCapability={}", thisCapability);
        Element capability = doc.createElement("capability");
        capability.setAttribute("type", thisCapability);

        //TODO: Greg: change the rest of the code to use the IdentifierProviderBuilder to find the package as shown below.
        capability.setAttribute("package", IdentifierProviderBuilder.getCapabilityPackage("SmartInverterCapability"));
        Element parameter = doc.createElement("parameter");
        parameter.setAttribute("type", "String");
        String abbrev = shortName.replace("self", "");
        String justNumber = abbrev.replace("H", "");
        String name = "SI-" + justNumber;
        if (debug) LOG.debug("Adding inverter parameter {}.", name);
        parameter.appendChild(doc.createTextNode(name));
        capability.appendChild(parameter);
        agents.appendChild(capability);
    }

    private static void addCapabilitySmartMeter(Document doc, Node agents, String thisCapability, String shortName) {
        LOG.debug("Entering addCApabilitySmartMeter(thisCapability={}", thisCapability);
        Element capability = doc.createElement("capability");
        capability.setAttribute("type", thisCapability);
        capability.setAttribute("package", IdentifierProviderBuilder.getCapabilityPackage("SmartMeterCapability"));

        // add smart meter name parameter

        Element parameter = doc.createElement("parameter");
        parameter.setAttribute("type", "String");
        String abbrev = shortName.replace("self", "");
        String justNumber = abbrev.replace("H", "");
        String name = "SM-" + justNumber;
        if (debug) LOG.debug("Adding smart meter name parameter {}.", name);
        parameter.appendChild(doc.createTextNode(name));
        capability.appendChild(parameter);

        // add critical kw parameter (random between the min and max specified by user in run.properties)

        Element kwParameter = doc.createElement("parameter");
        kwParameter.setAttribute("type", "double");  // must match the capability constructor arguments
        double criticalKW = getRandomDoubleBetween(MIN_CRITICAL_KW, MAX_CRITICAL_KW);
        if (debug) LOG.debug("Adding smart meter criticalKW parameter {}.", criticalKW);
        DecimalFormat df = new DecimalFormat("#.####");
        String formattedString = df.format(criticalKW);
        kwParameter.appendChild(doc.createTextNode(formattedString));
        capability.appendChild(kwParameter);

        agents.appendChild(capability);
    }

    public static double getRandomDoubleBetween(double min, double max) {
        double randomNum = rand.nextDouble(); //between 0 and 1
        return min + randomNum * (max - min);
    }

    private static void addPersonaSuperHolon(String shortName, String firstLetter, Document doc, Node rootElement) {
        LOG.debug("Entering addPersonaSuperHolon(shortName={}", shortName);
        Element agents = doc.createElement("agents");
        String myLevel = getLevelType(firstLetter);
        agents.setAttribute("type", myLevel + "GridPersona");
        agents.setAttribute("package", GRID_TYPES_PACKAGE);

        // organization elements
        Element organization = doc.createElement("organization");
        addSelfParticipant(organization);
        agents.appendChild(organization);

        // firstname elements
        Element agent = doc.createElement("agent");
        String abbrev = shortName.replace("self", "");
        String name = abbrev + "in" + abbrev;
        agent.appendChild(doc.createTextNode(name));
        agents.appendChild(agent);

        // capability elements
        equipSuperHolons(doc, agents, myLevel);
        rootElement.appendChild(agents);
    }

    private static void equipSuperHolons(Document doc, Element agents, String myLevel) {
        // all agent internal persona (except self) must have:
        addCap(doc, agents, "ParticipateCapability");
        equipSuperHolonsForOrg(doc, agents, myLevel);
    }

    private static void equipSuperHolonsForOrg(Document doc, Element agents, String myLevel) {
        // all in org have these
        addCap(doc, agents, "GridConnectCapability");
        addCap(doc, agents, "PowerCommunicationCapability");
        addCap(doc, agents, "DateTimeCapability");

        // only admins have these
        addCap(doc, agents, "GridAdminCapability");
        addCap(doc, agents, "GridControlSuperHolonCapability");
        addCap(doc, agents, "DistributeLoadCapability");
        addCap(doc, agents, "AggregatePowerCapability");
        addCap(doc, agents, "AssessReactivePowerQualityCapability");
        addCap(doc, agents, "AssessReactivePowerCapability");

        // level-specific equipment capabilities
        addCap(doc, agents, "Manage" + myLevel + "Capability");
    }

    private static void addPersonaHolon(String shortName, String firstLetter, Document doc, Node rootElement, String parent) {
        if (debug) LOG.debug("Adding holon with parent {}.", parent);

        Element agents = doc.createElement("agents");
        String myLevel = getLevelType(firstLetter);
        agents.setAttribute("type", myLevel + "GridPersona");
        agents.setAttribute("package", GRID_TYPES_PACKAGE);

        // organization elements
        Element organization = doc.createElement("organization");
        addSelfParticipant(organization);
        agents.appendChild(organization);

        // agent elements
        Element agent = doc.createElement("agent");
        String abbrev = shortName.replace("self", "");
        String name = abbrev + "in" + parent;
        if (debug) LOG.debug("Adding holon persona {}.", name);
        agent.appendChild(doc.createTextNode(name));
        agents.appendChild(agent);

        equipHolons(doc, agents, myLevel);
        rootElement.appendChild(agents);
    }

    private static void equipHolons(Document doc, Element agents, String myLevel) {
        // all agent internal persona (except self) must have:
        addCap(doc, agents, "ParticipateCapability");
        equipHolonsForOrg(doc, agents, myLevel);
    }

    private static void equipHolonsForOrg(Document doc, Element agents, String myLevel) {
        addCap(doc, agents, "GridConnectCapability");
        addCap(doc, agents, "PowerCommunicationCapability");
        addCap(doc, agents, "DateTimeCapability");

        addCap(doc, agents, "GridParticipateCapability");
        addCap(doc, agents, "GridControlHolonCapability");

        addCap(doc, agents, "AssessReactivePowerCapability");
        addCap(doc, agents, "AssessReactivePowerQualityCapability");

        // level-specific equipment capabilities
        addCap(doc, agents, "Manage" + myLevel + "Capability");
    }

    private static void addPersonaBroker(String shortName, String firstLetter, Document doc, Node rootElement) {
        LOG.debug("Entering addPersonaBroker(shortName={}", shortName);
        Element agents = doc.createElement("agents");
        String myLevel = getLevelType(firstLetter);
        agents.setAttribute("type", myLevel + "MarketPersona");
        agents.setAttribute("package", MARKET_TYPES_PACKAGE);

        // organization elements
        Element organization = doc.createElement("organization");
        addSelfParticipant(organization);
        agents.appendChild(organization);

        // firstname elements
        Element agent = doc.createElement("agent");
        String abbrev = shortName.replace("self", "");
        String name = abbrev + "in" + abbrev;
        agent.appendChild(doc.createTextNode(name));
        agents.appendChild(agent);

        equipBrokers(doc, agents);
        rootElement.appendChild(agents);
    }

    private static void equipBrokers(Document doc, Element agents) {
        // all agent internal persona (except self) must have:
        addCap(doc, agents, "ParticipateCapability");
        equipBrokersForOrg(doc, agents);
    }

    private static void equipBrokersForOrg(Document doc, Element agents) {
        // all org participants have:
        addCap(doc, agents, "AuctionCommunicationCapability");
        addCap(doc, agents, "DateTimeCapability");
        addCap(doc, agents, "MarketConnectCapability");

        // all admins have:
        addCap(doc, agents, "MarketAdminCapability");
        addCap(doc, agents, "BrokerPowerCapability");
    }

    private static void addPersonaMarketParticipant(String shortName, String firstLetter, Document doc, Node rootElement, String parent) {
        LOG.debug("Entering addPersonaMarketParticipant(shortName={}", shortName);
        Element agents = doc.createElement("agents");
        String myLevel = getLevelType(firstLetter);
        agents.setAttribute("type", myLevel + "MarketPersona");
        agents.setAttribute("package", MARKET_TYPES_PACKAGE);

        // organization elements
        Element organization = doc.createElement("organization");
        addSelfParticipant(organization);
        agents.appendChild(organization);

        // agent elements
        Element agent = doc.createElement("agent");
        String abbrev = shortName.replace("self", "");
        String name = abbrev + "in" + parent;
        if (debug) LOG.debug("Adding holon persona {}.", name);
        agent.appendChild(doc.createTextNode(name));
        agents.appendChild(agent);

        equipAuctioners(doc, agents);
        rootElement.appendChild(agents);
    }

    private static void equipAuctioners(Document doc, Element agents) {
        // all agent internal persona (except self) must have:
        addCap(doc, agents, "ParticipateCapability");
        equipAuctionersForOrg(doc, agents);
    }

    private static void equipAuctionersForOrg(Document doc, Element agents) {
        // all org participants have:
        addCap(doc, agents, "AuctionCommunicationCapability");
        addCap(doc, agents, "DateTimeCapability");
        addCap(doc, agents, "MarketConnectCapability");

        // only non-admins have:
        addCap(doc, agents, "MarketParticipateCapability");
        addCap(doc, agents, "AuctionPowerCapability");
    }

    /**
     * Creates Initialize.xml files
     *
     * @param selfAgentFolders the File[] of self organization agent folders
     */
    static void createAllInitializeXML(final File[] selfAgentFolders) {
        for (final File selfFolder : selfAgentFolders) {
            try {
                createInitializeXML(selfFolder);
            } catch (final Exception e) {
                LOG.error("ERROR: creating Initialize.xml in {}. {}", selfFolder, e.getMessage());
                System.exit(-23);
            }
        }
        LOG.info(" INITIALIZE XML files created  ==========");
    }

    public static void createInitializeXML(File selfFolder) throws TransformerException, ParserConfigurationException {
        LOG.debug("Entering createInitializeXML(selfFolder= {}).", selfFolder);
        try {
            String shortName = selfFolder.getName();
            String shortNameNoSelf = shortName.replace("self", "");
            GridHolonicLevel ot = GridHolonicLevel.getOrganizationType(shortNameNoSelf);
            String firstLetter = shortNameNoSelf.substring(0, 1).toUpperCase();

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("goalparameters");
            doc.appendChild(rootElement);

            if (!testCase.equals("01")) {

                // add grid control connections (no org suffix)
                addGridConnections(GRID_SUFFIX, shortNameNoSelf, firstLetter, doc, rootElement);

                String strID = shortNameNoSelf.substring(1);
                int agentNodeNumber = Integer.parseInt(strID);

                if (firstLetter.equals("H") || firstLetter.equals("N") || firstLetter.equals("L")) {
                    // add market control connections (no org suffix)
                    addMarketConnections(MARKET_SUFFIX, shortNameNoSelf, firstLetter, doc, rootElement);
                    addAuctionGuidelines(doc, rootElement, agentNodeNumber, firstLetter);
                }

                if (!firstLetter.equals("H")) {
                    //  if not a home
                    // add org spec for grid control - first arg is the suffix, last is the folder to std models
                    addOrgSpec(GRID_SUFFIX, shortNameNoSelf, firstLetter, doc, rootElement, GRID_MODEL_FOLDER);
                    if (debug)
                        LOG.debug("  Added grid control org spec: {}. model folder={}", shortNameNoSelf, GRID_MODEL_FOLDER);
                }

                if (firstLetter.equals("N") || firstLetter.equals("L")) {
                    BrokerGuidelinesBuilder.addBrokerGuidelines(doc, rootElement, agentNodeNumber, firstLetter, getChildNumberString(agentNodeNumber));
                    // add org spec for power market - first arg is the suffix, last is the folder to std models
                    addOrgSpec(MARKET_SUFFIX, shortNameNoSelf, firstLetter, doc, rootElement, MARKET_MODEL_FOLDER);
                    if (debug)
                        LOG.debug("  Added power market org spec: {}. model folder={}", shortNameNoSelf, MARKET_MODEL_FOLDER);
                }
            }

            // add manage guidelines by level
            addInitialAgentLevelGuidelines(firstLetter, doc, rootElement);
            if (debug) LOG.debug("  Added initial guidelines: {}", shortNameNoSelf);

            //============================================================

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            Source source = new DOMSource(doc);
            Result result = new StreamResult(new File(selfFolder + "/Initialize.xml"));

            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);
            transformer.transform(source, result);
            LOG.debug("Exiting createInitializeXML(result= {}).", result.toString());
        } catch (ParserConfigurationException | TransformerException pce) {
            LOG.error("Error creating agent.xml file. {}", pce.getMessage());
            throw pce;
        }
    }

    private static void addGridConnections(String orgSuffix, String shortNameNoSelf, String firstLetter, Document doc, Element rootElement) {
        // shortNameNoSelf is the power control name used in the maps

        // add connection to parent if not a substation
        if (!firstLetter.equals("S")) {
            String agentStringID = shortNameNoSelf.substring(1);
            String parent = parentMap.get(shortNameNoSelf);
            if (parent != null) {
                String orgParent = parent + orgSuffix;
                String orgSelf = shortNameNoSelf + orgSuffix;
                addConnectionToParentGrid(orgSelf, orgParent, doc, rootElement);
                if (debug) LOG.debug("  Added Connection to parent: {} to {}", orgSelf, orgParent);
            }
        }

        // add connection to each child if it has them
        Iterable<String> myChildren = childMap.get(shortNameNoSelf);
        if (myChildren != null) {
            for (String child : myChildren) {
                String orgChild = child + orgSuffix;
                String orgSelf = shortNameNoSelf + orgSuffix;
                addConnectionToChildGrid(orgSelf, orgChild, doc, rootElement);
                if (debug) LOG.debug("  Added Connection to market child: {} to {}", orgSelf, orgChild);
            }
        }
    }

    private static void addConnectionToParentGrid(String shortNameNoSelf, String parent, Document doc, Node rootElement) {
        Element conn = doc.createElement("gridConnections");
        conn.setAttribute("to", parent + "in" + parent);
        conn.setAttribute("in", "org" + parent);
        conn.setAttribute("expectedmaster", parent + "in" + parent);
        rootElement.appendChild(conn);
    }

    private static void addConnectionToChildGrid(final String shortNameNoSelf, String child, Document doc, Node rootElement) {
        Element conn = doc.createElement("gridConnections");
        conn.setAttribute("to", child + "in" + shortNameNoSelf);
        conn.setAttribute("in", "org" + shortNameNoSelf);
        conn.setAttribute("expectedmaster", shortNameNoSelf + "in" + shortNameNoSelf);
        rootElement.appendChild(conn);
    }

    private static void addMarketConnections(String orgSuffix, String shortNameNoSelf, String firstLetter, Document doc, Element rootElement) {
        // shortNameNoSelf is the power control name used in the maps

        // add connection to parent if not a substation
        if (!firstLetter.equals("S")) {
            String agentStringID = shortNameNoSelf.substring(1);
            String parent = parentMap.get(shortNameNoSelf);
            if (parent != null) {
                String orgParent = parent + orgSuffix;
                String orgSelf = shortNameNoSelf + orgSuffix;
                addConnectionToParentMarket(orgSelf, orgParent, doc, rootElement);
                if (debug) LOG.debug("  Added Connection to parent: {} to {}", orgSelf, orgParent);
            }
        }

        // add connection to each child if it has them
        Iterable<String> myChildren = childMap.get(shortNameNoSelf);
        if (myChildren != null) {
            for (String child : myChildren) {
                String orgChild = child + orgSuffix;
                String orgSelf = shortNameNoSelf + orgSuffix;
                addConnectionToChildMarket(orgSelf, orgChild, doc, rootElement);
                if (debug) LOG.debug("  Added Connection to market child: {} to {}", orgSelf, orgChild);
            }
        }
    }

    private static void addConnectionToParentMarket(String shortNameNoSelf, String parent, Document doc, Node rootElement) {
        Element conn = doc.createElement("brokerConnections");
        conn.setAttribute("to", parent + "in" + parent);
        conn.setAttribute("in", "org" + parent);
        conn.setAttribute("expectedmaster", parent + "in" + parent);
        rootElement.appendChild(conn);
    }

    private static void addConnectionToChildMarket(final String shortNameNoSelf, String child, Document doc, Node rootElement) {
        Element conn = doc.createElement("auctionConnections");
        conn.setAttribute("to", child + "in" + shortNameNoSelf);
        conn.setAttribute("in", "org" + shortNameNoSelf);
        conn.setAttribute("expectedmaster", shortNameNoSelf + "in" + shortNameNoSelf);
        rootElement.appendChild(conn);
    }

    private static void addAuctionGuidelines(Document doc, Node rootElement, int agentNodeNumber, String firstLetter) {
        if ((firstLetter.equals("H") || firstLetter.equals("N")) && agentNodeNumber <= 62) {
            // n is the ID 44 to 200 and from 206 to 400
            if (debug) LOG.debug("Home num = {}.", agentNodeNumber);

            Element conn = doc.createElement("auctionGuidelines");
            int base;

            switch (agentNodeNumber) {
                case 44:
                    setAuctionAttributes(conn, 0., 0.1133, 1, 7.7108);
                    break;
                case 45:
                    setAuctionAttributes(conn, 0.1238, 0, 0, 5.0775);
                    break;
                case 46:
                    setAuctionAttributes(conn, 0.1161, 0, 0, 4.7629);
                    break;
                case 47:
                    setAuctionAttributes(conn, 0.1000, 0, 0, 4.1024);
                    break;

                case 49:
                    setAuctionAttributes(conn, 0.1707, 0, 0, 4.6020);
                    break;
                case 50:
                    setAuctionAttributes(conn, 0.2320, 0, 0, 6.2554);
                    break;
                case 51:
                    setAuctionAttributes(conn, 0.1000, 0, 0, 2.6958);
                    break;
                case 52:
                    setAuctionAttributes(conn, 0, 0.1676, 1, 7.4055);
                    break;

                case 54:
                    setAuctionAttributes(conn, 0.1915, 0, 0, 4.2155);
                    break;
                case 55:
                    setAuctionAttributes(conn, 0.1000, 0., 0, 2.2018);
                    break;
                case 56:
                    setAuctionAttributes(conn, 0.1449, 0., 0, 3.1529);
                    break;
                case 57:
                    setAuctionAttributes(conn, 0., 0.1432, 1, 14.4844);
                    break;

                case 59:
                    setAuctionAttributes(conn, 0.1000, 0., 0, 2.8695);
                    break;
                case 60:
                    setAuctionAttributes(conn, 0, 0.1759, 1, 13.7737);
                    break;
                case 61:
                    setAuctionAttributes(conn, 0.2198, 0., 0, 6.3070);
                    break;
                case 62:
                    setAuctionAttributes(conn, 0.2081, 0., 0, 5.9703);
                    break;


                // neighborhoods
                case 43:
                    setAuctionAttributes(conn, 0, 0, 0, 0);
                    break;

                case 48:
                    setAuctionAttributes(conn, 0, 0, 0, 0);
                    break;
                case 53:
                    setAuctionAttributes(conn, 0, 0, 0, 0);
                    break;
                case 58:
                    setAuctionAttributes(conn, 0, 0, 0, 0);
                    break;
                default:
                    break;
            }

            if (firstLetter.equals("H")) conn.setAttribute("tierNumber", Integer.toString(1));
            else if (firstLetter.equals("N")) conn.setAttribute("tierNumber", Integer.toString(2));

            // not used yet
            conn.setAttribute("openingTimeSlice", Long.toString(1));
            conn.setAttribute("purchaseTimeSlice", Long.toString(10));
            conn.setAttribute("sellPriceFlexibilityPercent", Double.toString(0.0));
            conn.setAttribute("buyPriceFlexibilityPercent", Double.toString(0.0));
            rootElement.appendChild(conn);
        }
    }

    private static void setAuctionAttributes(Element conn, double pBuy, double pSell, int isSell, double qty) {
        conn.setAttribute("desiredSellPrice_centsperkWh", Double.toString(pSell));
        conn.setAttribute("desiredBuyPrice_centsperkWh", Double.toString(pBuy));
        conn.setAttribute("kWh", Double.toString(qty));
        conn.setAttribute("isSell", Integer.toString(isSell));
    }

    private static String getChildNumberString(int n) {
        if (n == 43) return "44, 45, 46, 47";
        if (n == 48) return "49 50 51 52";
        if (n == 53) return "54 55 56 57";
        if (n == 58) return "59 60 61 62";
        if (n == 39) return "43 48 53 58";
        return "0";
    }

    private static void addOrgSpec(String orgSuffix, final String shortNameNoSelf, String firstLetter, Document doc, Node rootElement, String modelFolder) {
        Element conn = doc.createElement("org" + shortNameNoSelf + orgSuffix);
        double max = getMaxKW(firstLetter);
        conn.setAttribute("combinedkw", Double.toString(max));
        conn.setAttribute("orgModelFolder", modelFolder);
        conn.setAttribute("specpath", shortNameNoSelf + "/org" + shortNameNoSelf + orgSuffix);
        rootElement.appendChild(conn);
    }

    private static double getMaxKW(String firstLetter) {
        double maxKW = 0.0;
        switch (firstLetter) {
            case "S":
                maxKW = 33280.0;
                break;
            case "F":
                maxKW = 1280.0;
                break;
            case "L":
                maxKW = 160.0;
                break;
            case "N":
                maxKW = 20.0;
                break;
            case "H":
                maxKW = 5.0;
                break;
            default:
                LOG.error("The type of organization could not be determined from input ({}). ", firstLetter);
                break;
        }
        return maxKW;
    }

    private static void addInitialAgentLevelGuidelines(final String firstLetter, Document doc, Node rootElement) {
        String myLevel = getLevelType(firstLetter).toLowerCase();
        Element conn = doc.createElement(myLevel + "Guidelines");
        double max = getMaxKW(firstLetter);
        conn.setAttribute("maxKW", Double.toString(max));
        rootElement.appendChild(conn);
    }

    private static File[] createGridOrgFolders(File[] selfAgentFolders, String folderSuffix) {
        final ArrayList<File> list = new ArrayList<File>();
        for (final File selfFolder : selfAgentFolders) {
            // create an org folder for all but the homes
            if (!selfFolder.getName().contains("selfH") && hasChild(selfFolder)) {
                String name = selfFolder.getAbsolutePath().replace("self", "org") + folderSuffix;
                File folder = new File(name);
                folder.mkdirs();  // create if doesn't exist
                list.add(folder);
            }
        }
        final File[] orgFolderArray = list.toArray(new File[list.size()]);
        if (orgFolderArray != null && orgFolderArray.length > 0)
            if (debug)
                LOG.debug("Total: {} self agent folders found. {} org folders created. The first is {}. ", selfAgentFolders.length, orgFolderArray.length, orgFolderArray[0].getName());
        return orgFolderArray;
    }

    private static File[] createMarketOrgFolders(File[] selfAgentFolders, String folderSuffix) {
        final ArrayList<File> list = new ArrayList<File>();
        for (final File selfFolder : selfAgentFolders) {
            // create an org folder for neighborhoods and laterals
            if ((selfFolder.getName().contains("selfN") || selfFolder.getName().contains("selfL"))
                    && hasChild(selfFolder)) {
                String name = selfFolder.getAbsolutePath().replace("self", "org") + folderSuffix;
                File folder = new File(name);
                folder.mkdirs();  // create if doesn't exist
                list.add(folder);
            }
        }
        final File[] orgFolderArray = list.toArray(new File[list.size()]);
        if (orgFolderArray != null && orgFolderArray.length > 0) {
            LOG.debug("Total: {} self agent folders found. {} org folders created. The first is {}. ", selfAgentFolders.length, orgFolderArray.length, orgFolderArray[0].getName());
        }
        return orgFolderArray;
    }

    private static void deleteMarketOrgFolders(File[] selfAgentFolders, String folderSuffix) {
        final ArrayList<File> list = new ArrayList<File>();
        for (final File selfFolder : selfAgentFolders) {
            // create an org folder for neighborhoods and laterals
            if (selfFolder.getName().contains("selfS") || selfFolder.getName().contains("selfF")) {
                LOG.debug("Self s or f self name is = {}", selfFolder.getName());
                String badOrgName = selfFolder.getAbsolutePath().replace("self", "org") + folderSuffix;
                LOG.debug("Bad market org name      = {}", badOrgName);
                File folder = new File(badOrgName);
                deleteDir(folder);

            }
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // The directory is now empty or this is a file so delete it
        return dir.delete();
    }

    private static boolean hasChild(File selfOrOrgFolder) {
        String agentName = selfOrOrgFolder.getName().replace("self", "").replace("org", "");
        return childMap.get(agentName) != null;
    }

    /**
     * Creates Agent.xml files for the external organizations.
     *
     * @param orgString  - either GRID or MARKET
     * @param orgFolders - the set of org folders for this organization
     */
    static void createAllAgentXMLForOrgs(String orgString, String orgSuffix, final File[] orgFolders) {
       LOG.info("Entering createAllAgentXMLForOrgs(orgString={}, orgSuffix={}, orgFolders={})", orgString, orgSuffix, orgFolders);
        for (final File orgFolder : orgFolders) {
           LOG.info("orgSuffix={} orgFolder={}", orgSuffix, orgFolder);
            String orgFolderWithoutSuffix;
            if (orgSuffix!= null && !orgSuffix.isEmpty() && orgFolder.getName().endsWith(orgSuffix)) {
                orgFolderWithoutSuffix = orgFolder.getName().substring(0, orgString.length() - orgSuffix.length() + 1);
            } else {
                orgFolderWithoutSuffix = orgFolder.getName();
            }
            if (debug) LOG.debug(" Org folder without suffix is {}.", orgFolderWithoutSuffix);

            if (hasChild(orgFolderWithoutSuffix)) {
                try {
                    FileUtils.cleanDirectory(orgFolder);
                    if (debug)
                        LOG.debug(" Beginning to create {} AGENT XML for orgFolder={}. orgSuffix={}", orgString, orgFolder, orgSuffix);
                    if (orgSuffix.equals(GRID_SUFFIX)) {
                        createAgentXMLForGridOrg(orgString, orgSuffix, orgFolder);
                    } else {
                        createAgentXMLForMarketOrg(orgString, orgSuffix, orgFolder);
                    }
                    if (debug) LOG.debug(" {} ORG Agent XML file created in {}.", orgString, orgFolder);
                } catch (final Exception e) {
                    LOG.error("ERROR: creating {} ORG Agent.xml in {}. {}.", orgString, orgFolder, e.getCause().toString());
                    System.exit(-54);
                }
            }
        }
        LOG.info(" AGENT XML files created  ==========");
    }

    private static boolean hasChild(String selfOrOrgFolderNameWithoutSuffix) {
        String agentName = selfOrOrgFolderNameWithoutSuffix.replace("self", "").replace("org", "");
        return childMap.get(agentName) != null;
    }

    /**
     * Logic to create agent persona based on folder name. Includes a call to look up the parent.
     *
     * @param orgString - either GRID or MARKET
     * @param orgSuffix - the suffix of the organization. NOT USED HERE.
     * @param folder    - the set of agent org folder for this organization
     * @throws TransformerException         - Handles any Transformer Exceptions
     * @throws ParserConfigurationException - Handles any ParserConfiguration Exceptions
     */
    public static void createAgentXMLForGridOrg(String orgString, String orgSuffix, File folder) throws TransformerException, ParserConfigurationException {
        try {
            String orgName = folder.getName();  // e.g. orgN43 or orgN43A for the market holarchy
            String shortNameNoOrg = orgName.replace("org", "");  // e.g. N43
            if (debug) LOG.debug(" orgName={}. shortNameNoOrg={}", orgName, shortNameNoOrg);
            GridHolonicLevel ot = GridHolonicLevel.getOrganizationType(shortNameNoOrg);
            if (debug) LOG.debug(" OrganizationType={}.", ot);

            String firstLetter = shortNameNoOrg.substring(0, 1).toUpperCase();
            if (debug) LOG.debug(" firstLetter={}.", firstLetter);

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("environment");
            doc.appendChild(rootElement);

            // ===================================================================

            // add a persona for the organization master proxy persona
            addPersonaSuperHolonForOrg(orgName, firstLetter, doc, rootElement);

            // add a persona for each organization participant proxy persona (for all but the homes)
            if (!shortNameNoOrg.toUpperCase().contains("H")) {
                for (String child : childMap.get(shortNameNoOrg)) {
                    if (debug)
                        LOG.debug("Adding participant {} to {}'s organization {}.", child, shortNameNoOrg, orgName);
                    addPersonaHolonForOrg(orgName, doc, rootElement, child);
                }
            }
            // =============================================================

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            Source source = new DOMSource(doc);
            Result result = new StreamResult(new File(folder + "/Agent.xml"));

            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);
            transformer.transform(source, result);
        } catch (ParserConfigurationException | TransformerException pce) {
            LOG.error("ERROR: {}", pce.getMessage());
            throw pce;
        }
    }

    private static void addPersonaSuperHolonForOrg(String shortName, String firstLetter, Document doc, Node rootElement) {
        Element agents = doc.createElement("agents");
        String myLevel = getLevelType(firstLetter);
        agents.setAttribute("type", myLevel + "GridPersona");
        agents.setAttribute("package", GRID_TYPES_PACKAGE);

        Element organization = doc.createElement("organization");
        organization.setAttribute("type", "GridMaster");
        organization.setAttribute("package", GRID_ADMIN_PACKAGE);
        agents.appendChild(organization);

        Element agent = doc.createElement("agent");
        String abbrev = shortName.replace("org", "");
        String name = abbrev + "in" + abbrev;
        agent.appendChild(doc.createTextNode(name));
        agents.appendChild(agent);

        equipSuperHolonsForOrg(doc, agents, myLevel);
        rootElement.appendChild(agents);
    }

    private static void addPersonaHolonForOrg(String shortName, Document doc, Node rootElement, String child) {
        if (debug) LOG.debug("Adding holon {} to {}.", child, shortName);

        Element agents = doc.createElement("agents");
        String childLevel = getLevelType(child.substring(0, 1).toUpperCase());
        agents.setAttribute("type", childLevel + "Persona");
        agents.setAttribute("package", AGENT_TYPES_PACKAGE);

        // organization elements
        Element organization = doc.createElement("organization");
        organization.setAttribute("type", "GridParticipant");
        organization.setAttribute("package", GRID_PARTICIPATE_PACKAGE);
        agents.appendChild(organization);

        // agent elements
        Element agent = doc.createElement("agent");
        String abbrev = shortName.replace("org", "");
        String name = child + "in" + abbrev;
        if (debug) LOG.debug("Adding sub holon persona {}.", name);
        agent.appendChild(doc.createTextNode(name));
        agents.appendChild(agent);

        // capability elements
        equipHolonsForOrg(doc, agents, childLevel);
        rootElement.appendChild(agents);
    }

    /**
     * Logic to create agent persona based on folder name. Includes a call to look up the parent.
     *
     * @param orgString - either GRID or MARKET
     * @param orgSuffix - the suffix of the organization. Used to find org name without suffix
     * @param folder    - the set of agent org folder for this organization
     * @throws TransformerException         - Handles any Transformer Exceptions
     * @throws ParserConfigurationException - Handles any ParserConfiguration Exceptions
     */
    public static void createAgentXMLForMarketOrg(String orgString, String orgSuffix, File folder) throws TransformerException, ParserConfigurationException {
        try {
            String orgName = folder.getName();  // e.g. orgN43 or orgN43A for the market holarchy
            String shortNameNoOrg = orgName.replace("org", "");  // e.g. N43
            if (debug) LOG.debug(" orgName={}. shortNameNoOrg={}", orgName, shortNameNoOrg);

            MarketHolonicLevel ot = null;
            try {
                ot = MarketHolonicLevel.getOrganizationType(shortNameNoOrg);
            } catch (Exception ex) {
                return;
            }

            if (debug) LOG.debug(" OrganizationType={}.", ot);
            String firstLetter = shortNameNoOrg.substring(0, 1).toUpperCase();
            if (debug) LOG.debug(" firstLetter={}.", firstLetter);

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("environment");
            doc.appendChild(rootElement);

            // ===================================================================

            // add a persona for the organization master proxy persona
            addPersonaBrokerForOrg(orgName, firstLetter, doc, rootElement);

            // add a persona for each organization participant proxy persona (for all but the homes)
            if (!shortNameNoOrg.toUpperCase().contains("H")) {
                String shortNameNoOrgNoSuffix = shortNameNoOrg.substring(0, shortNameNoOrg.length() - orgSuffix.length());
                for (String child : childMap.get(shortNameNoOrgNoSuffix)) {
                    if (debug) LOG.debug("Adding participant {} to {}'s organization {}.", child, orgName, orgName);
                    addPersonaAuctionForOrg(orgName, doc, rootElement, child);
                }
            }
            // =============================================================

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            Source source = new DOMSource(doc);
            Result result = new StreamResult(new File(folder + "/Agent.xml"));

            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);
            transformer.transform(source, result);
        } catch (ParserConfigurationException | TransformerException pce) {
            LOG.error("ERROR: {}", pce.getMessage());
            throw pce;
        }
    }

    private static void addPersonaBrokerForOrg(String shortName, String firstLetter, Document doc, Node rootElement) {
        Element agents = doc.createElement("agents");
        String myLevel = getLevelType(firstLetter);
        agents.setAttribute("type", myLevel + "MarketPersona");
        agents.setAttribute("package", MARKET_TYPES_PACKAGE);

        Element organization = doc.createElement("organization");
        organization.setAttribute("type", "MarketMaster");
        organization.setAttribute("package", MARKET_ADMIN_PACKAGE);
        agents.appendChild(organization);

        Element agent = doc.createElement("agent");
        String abbrev = shortName.replace("org", "");
        String name = abbrev + "in" + abbrev;
        agent.appendChild(doc.createTextNode(name));
        agents.appendChild(agent);

        equipBrokersForOrg(doc, agents);
        rootElement.appendChild(agents);
    }

    private static void addPersonaAuctionForOrg(String shortName, Document doc, Node rootElement, String child) {
        if (debug) LOG.debug("Adding holon {} to {}.", child, shortName);
        Element agents = doc.createElement("agents");
        String childLevel = getLevelType(child.substring(0, 1).toUpperCase());
        agents.setAttribute("type", childLevel + "Persona");
        agents.setAttribute("package", AGENT_TYPES_PACKAGE);

        Element organization = doc.createElement("organization");
        organization.setAttribute("type", "MarketParticipant");
        organization.setAttribute("package", MARKET_PARTICIPATE_PACKAGE);
        agents.appendChild(organization);

        Element agent = doc.createElement("agent");
        String abbrev = shortName.replace("org", "");
        String name = child + MARKET_SUFFIX + "in" + abbrev;
        if (debug) LOG.debug("Adding auction participant persona {}.", name);
        agent.appendChild(doc.createTextNode(name));
        agents.appendChild(agent);

        equipAuctionersForOrg(doc, agents);
        rootElement.appendChild(agents);
    }
}
