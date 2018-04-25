package edu.ksu.cis.macr.ipds.config;

import edu.ksu.cis.macr.aasis.messaging.IMessagingFocus;
import edu.ksu.cis.macr.aasis.simulator.clock.Clock;
import edu.ksu.cis.macr.aasis.simulator.player.Player;
import edu.ksu.cis.macr.aasis.types.IAgentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Reads the information provided in the run.properties file when the system first starts. Use this to add new new simulation variables and avoid hard-coding 'magic
 * user-defined constants as needed. Variables should be in all lower case letters. Do not use quotations. All properties are
 * automatically read as strings. Do not include empty spaces after the equal signs.Conversions from strings to ints,
 * arrays, etc should be done here when possible. If additional information is needed, then just pass along the string (less preferred). A singleton means there
 * will be one instance per JVM.
 */
public enum RunManager {
    INSTANCE;


    public static final boolean FORCE_STOP_AFTER_INITIALLY_CONNECTED = false;
    public static final boolean FORCE_STOP_AFTER_INITIALLY_REGISTERED = false;
    public static final boolean FORCE_STOP_AFTER_SELF_HOME_REPORTS = false;
    public static final boolean FORCE_STOP_AFTER_SELF_HOME_REPORTS_REC = false;
    public static final boolean FORCE_STOP_AFTER_SMART_METER_REPORTS = false;
    public static final boolean FORCE_STOP_AFTER_SMART_METER_REPORTS_REC = false;
    public static final boolean FORCE_STOP_AFTER_SUB_REPORTS_FROM_FEEDERS = false;
    public static final boolean FORCE_STOP_AFTER_SUB_REPORTS_FROM_FEEDERS_REC = false;
    public static final boolean FORCE_STOP_AFTER_SUB_REPORTS_FROM_HOMES = false;
    public static final boolean FORCE_STOP_AFTER_SUB_REPORTS_FROM_HOMES_REC = false;
    public static final boolean FORCE_STOP_AFTER_SUB_REPORTS_FROM_LATERALS = false;
    public static final boolean FORCE_STOP_AFTER_SUB_REPORTS_FROM_LATERALS_REC = false;
    public static final boolean FORCE_STOP_AFTER_SUB_REPORTS_FROM_NEIGHBORHOODS = false;
    public static final boolean FORCE_STOP_AFTER_SUB_REPORTS_FROM_NEIGHBORHOODS_REC = false;
    public static final double INITIAL_CONNECTION_THRESHOLD_FRACTION = 1.00;  // 1.00 means 100% must connect
    private static final String CUR_DIR = System.getProperty("user.dir");
    private static final Logger LOG = LoggerFactory.getLogger(RunManager.class);
    private static final boolean debug = false;
    private static String absolutePathToConfigsFolder;
    private static String absolutePathToLogbackXMLFile;
    private static String absolutePathToMatLabCodeFolder;
    private static String absolutePathToMatLabDataFolder;
    private static String absolutePathToSourceFolder;
    private static String absolutePathToStandardAgentGoalModel;
    private static String absolutePathToStandardAgentModelsFolder;
    private static String absolutePathToStandardAgentRoleModel;
    private static String absolutePathToStandardOrganizationGoalModel;
    private static String absolutePathToStandardOrganizationModelsFolder;
    private static String absolutePathToStandardOrganizationRoleModel;
    private static String absolutePathToStandardPropertiesFolder;
    private static String absolutePathToTestCaseFolder;
    private static String absolutePathtoSensorDataFile;
    private static String absolutePathtoStaticLayoutImageFile;
    private static HashMap<IAgentType, ArrayList<String>> agentMap = new HashMap<>();
    private static boolean showCommunicationsInBrowser;
    private static boolean showAgentOrganizations;
    private static int deliveryCheckTime_ms = 500;
    private static boolean fullyConnected = false;
    private static HashSet<String> gridRegistered = new HashSet<>();
    private static boolean initializeGoalsInConstructors;
    private static boolean initiallyConnected = false;
    private static boolean isLoaded = Boolean.FALSE;
    private static boolean isStopped = false;
    private static HashSet<String> marketRegistered = new HashSet<>();
    private static int planningHorizonInMinutes;
    private static double powerFactor;
    private static Properties properties;
    private static int[] pvenabled = new int[100];
    private static Boolean showSensors;
    private static HashMap<Long, TreeSet<String>> smartGridSelfToSubForwardsHome = new HashMap<>();
    private static HashMap<Long, TreeSet<String>> smartGridSelfToSubForwardsHomeRec = new HashMap<>();
    private static HashMap<Long, TreeSet<String>> smartGridSensorReports = new HashMap<>();
    private static HashMap<Long, TreeSet<String>> smartGridSensorReportsRec = new HashMap<>();
    private static SmartInverterAlgorithm smartInverterAlgorithm;
    private static int standardWaitTime_ms = 100;
    private static String testCaseName;
    private static String topGoal;
    private static int totalConnectionCount;
    private static int totalSmartInvertersReported;
    private static Boolean useLiveMatLab;
    private static int messageDelayMinMS;
    private static int messageDelayMaxMX;
    private static double fractionMessagesDelayed;
    private static double minCritical_kw;
    private static double maxCritical_kw;
    private static int participantRetryDelay_ms;
    private static int participantTimeout_ms;
    private static String selfAdminPackage;
    private static String selfTypesPackage;
    private static String participatePackage;
    private static String agentTypesPackage;
    private static String estimationSuffix;
    private static String estimationModelFolder;
    private static String estimationParticipatePackage;
    private static String estimationAdminPackage;
    private static String estimationTypesPackage;
    private static String gridSuffix;
    private static String gridModelFolder;
    private static String gridParticipatePackage;
    private static String gridAdminPackage;
    private static String gridTypesPackage;
    private static String marketSuffix;
    private static String marketModelFolder;
    private static String marketParticipatePackage;
    private static String marketAdminPackage;
    private static String marketTypesPackage;

    public synchronized static void add(IAgentType agentType, String agentName) {
        if (debug) LOG.debug("Adding {} {}.", agentType, agentName);
        ArrayList<String> lst = RunManager.agentMap.get(agentType);
        if (lst == null) lst = new ArrayList<>();
        lst.add(agentName);
        RunManager.agentMap.put(agentType, lst);
        if (debug) LOG.debug("List of {}= {}.", agentType, RunManager.agentMap.get(agentType));
    }

    public static void addPowerMessageSelfToSub(String sender, String receiver, long timeSlice) {
        TreeSet<String> tsReports = RunManager.smartGridSelfToSubForwardsHome.get(timeSlice);
        if (tsReports == null) tsReports = new TreeSet<>();
        tsReports.add(sender + "-" + receiver);
        RunManager.smartGridSelfToSubForwardsHome.put(timeSlice, tsReports);
        LOG.debug("Number of home self persona forwarding reviewed sensor report to sub persona in timeslice {}: {} of {}", timeSlice, RunManager.smartGridSelfToSubForwardsHome.get(timeSlice).size(), getAgentCountByType(AgentType.Home));
        if (allPowerMessageSelfToSub(timeSlice) && RunManager.FORCE_STOP_AFTER_SELF_HOME_REPORTS) {
            //terminateSuccessfully();
        }
    }

    public static int getAgentCountByType(IAgentType agentType) {
        return RunManager.agentMap.get(agentType).size();
    }

    public static boolean allPowerMessageSelfToSub(long timeSlice) {
        if (RunManager.smartGridSensorReports.get(timeSlice) == null) return false;
        int reports = RunManager.smartGridSelfToSubForwardsHome.get(timeSlice).size();
        int homes = getAgentCountByType(AgentType.Home);
        return (reports == homes);
    }

    public static void addPowerMessageSelfToSubRec(String sender, String receiver, long timeSlice) {
        TreeSet<String> tsReports = RunManager.smartGridSelfToSubForwardsHomeRec.get(timeSlice);
        if (tsReports == null) tsReports = new TreeSet<>();
        tsReports.add(sender + "-" + receiver);
        RunManager.smartGridSelfToSubForwardsHomeRec.put(timeSlice, tsReports);
        LOG.debug("Number of home self persona forwarding reviewed sensor report to sub persona in timeslice {}: {} of {} RECEIVED", timeSlice, RunManager.smartGridSelfToSubForwardsHomeRec.get(timeSlice).size(), getAgentCountByType(AgentType.Home));
    }

    public static void addPowerMessageSentFromSensorToSelf(final String sender, final String receiver, final long timeSlice) {
        TreeSet<String> tsReports = RunManager.smartGridSensorReports.get(timeSlice);
        if (tsReports == null) tsReports = new TreeSet<>();
        tsReports.add(sender + "-" + receiver);
        RunManager.smartGridSensorReports.put(timeSlice, tsReports);
        LOG.debug("Number of sensors reporting in timeslice {}: {} of {}", timeSlice, RunManager.smartGridSensorReports.get(timeSlice), getAgentCountByType(AgentType.Home));
        if (RunManager.allPowerMessagesSentFromSensorToSelf(timeSlice) && RunManager.FORCE_STOP_AFTER_SMART_METER_REPORTS) {
            // terminateSuccessfully();
        }
    }

    public static boolean allPowerMessagesSentFromSensorToSelf(long timeSlice) {
        if (RunManager.smartGridSensorReports.get(timeSlice) == null) return false;
        int reports = RunManager.smartGridSensorReports.get(timeSlice).size();
        int homes = getAgentCountByType(AgentType.Home);
        return (reports == homes);
    }

    public static void addPowerMessageSentFromSensorToSelfRec(final String sender, final String receiver, final long timeSlice) {
        TreeSet<String> tsReports = RunManager.smartGridSensorReportsRec.get(timeSlice);
        if (tsReports == null) tsReports = new TreeSet<>();
        tsReports.add(sender + "-" + receiver);
        RunManager.smartGridSensorReportsRec.put(timeSlice, tsReports);
        LOG.debug("Number of sensors reporting in timeslice {}: {} of {} RECEIVED", timeSlice, RunManager.smartGridSensorReportsRec.get(timeSlice).size(), getAgentCountByType(AgentType.Home));
        if (RunManager.smartGridSensorReportsRec.get(timeSlice).size() == getAgentCountByType(AgentType.Home)) {
            //terminateSuccessfully();
        }
    }

    public synchronized static void displayCounts() {
        RunManager.agentMap.keySet().stream().filter(t -> debug).forEach(t -> LOG.debug("{} : count={}", t, RunManager.getAgentCountByType(t)));
        if (debug) LOG.debug("TOTAL : count={}", getCountAllAgents());
    }

    public synchronized static int getCountAllAgents() {
        int total = 0;
        for (IAgentType t : RunManager.agentMap.keySet()) {
            total = total + RunManager.getAgentCountByType(t);
        }
        return total;
    }

    public static String getAbsolutePathToConfigsFolder() {
        return absolutePathToConfigsFolder;
    }

    public static void setAbsolutePathToConfigsFolder(final String absolutePathToConfigsFolder) {
        if (RunManager.absolutePathToConfigsFolder == null) {
            RunManager.absolutePathToConfigsFolder = absolutePathToConfigsFolder;
        }
    }

    public static String getAbsolutePathToMatLabCodeFolder() {
        return absolutePathToMatLabCodeFolder;
    }

    public static void setAbsolutePathToMatLabCodeFolder(
            final String absolutePathToMatLabCodeFolder) {
        if (RunManager.absolutePathToMatLabCodeFolder == null) {
            RunManager.absolutePathToMatLabCodeFolder =
                    absolutePathToMatLabCodeFolder;
        } else {
            throwException();
        }
    }

    public static String getAbsolutePathToMatLabDataFolder() {
        return absolutePathToMatLabDataFolder;
    }

    public static void setAbsolutePathToMatLabDataFolder(String absolutePathToMatLabDataFolder) {
        RunManager.absolutePathToMatLabDataFolder = absolutePathToMatLabDataFolder;
    }

    public static String getAbsolutePathToSensorDataFile() {
        return absolutePathtoSensorDataFile;
    }

    public static void setAbsolutePathToSensorDataFile(
            final String absolutePathToSensorDataFile) {
        if (RunManager.absolutePathtoSensorDataFile == null) {
            RunManager.absolutePathtoSensorDataFile =
                    absolutePathToSensorDataFile;
        } else {
            throwException();
        }
    }

    public static String getAbsolutePathToStandardAgentGoalModel(IAgentType agentType) {
        LOG.info("Getting standard agent goal model for {}", agentType);
        String strFile;
        if (agentType.toString().endsWith("Agent")) {
            strFile = getAbsolutePathToStandardAgentModelsFolder() + "/" +
                    agentType.toString() + "GoalModel.goal";
            LOG.info("Goal model = {}", strFile);
        } else {
            strFile = getAbsolutePathToStandardAgentModelsFolder() + "/" +
                    agentType.toString() + "AgentGoalModel.goal";
            LOG.info("Agent Goal model = {}", strFile);
        }

        File f = new File(strFile);
        if (f.exists() && !f.isDirectory()) {
            if (debug) LOG.info("{} Standard agent goal model file is {}", agentType.toString(),
                    strFile);
        } else {
            LOG.error("ERROR: {} Standard agent goal model file {} not found. ",
                    agentType.toString(), strFile);
        }
        return strFile;
    }

    public static String getAbsolutePathToStandardAgentModelsFolder() {
        return absolutePathToStandardAgentModelsFolder;
    }

    public static void setAbsolutePathToStandardAgentModelsFolder(final String path) {
        if (RunManager.absolutePathToStandardAgentModelsFolder == null) {
            RunManager.absolutePathToStandardAgentModelsFolder = path;
        }
    }

    public static String getAbsolutePathToStandardAgentRoleModel(IAgentType agentType) {
        LOG.info("Getting standard agent role model for {}", agentType);
        String strFile;
        if (agentType.toString().endsWith("Agent")) {
            strFile = getAbsolutePathToStandardAgentModelsFolder() + "/" +
                    agentType.toString() + "RoleModel.role";
            LOG.info("Role model = {}", strFile);
        } else {
            strFile = getAbsolutePathToStandardAgentModelsFolder() + "/" +
                    agentType.toString() + "AgentRoleModel.role";
            LOG.info("Agent Role model = {}", strFile);
        }
        File f = new File(strFile);
        if (f.exists() && !f.isDirectory()) {
            if (debug) LOG.info("{} Standard agent role model file is {}", agentType.toString(), strFile);
        } else {
            LOG.error("ERROR: {} Standard agent role model file {} not found. ",
                    agentType.toString(), strFile);
        }
        return strFile;
    }

    public static String getAbsolutePathToStandardOrganizationGoalModel(String orgModelFolder, GridHolonicLevel myLevel) {
        String strFile = getAbsolutePathToStandardOrganizationModelsFolder() + "/" + orgModelFolder + "/" +
                myLevel.toString() + "GoalModel.goal";
        File f = new File(strFile);
        if (f.exists() && !f.isDirectory()) {
            if (debug) LOG.info("{} Standard organization goal model file is {}", myLevel.toString(),
                    strFile);
        } else {
            LOG.error("ERROR: {} Standard organization goal model file {} not found. ",
                    myLevel.toString(), strFile);
        }
        RunManager.setAbsolutePathToStandardOrganizationGoalModel(strFile);
        return strFile;
    }

    public static void setAbsolutePathToStandardOrganizationGoalModel(final String absolutePathToStandardOrganizationGoalModel) {
        RunManager.absolutePathToStandardOrganizationGoalModel = absolutePathToStandardOrganizationGoalModel;
    }

    public static String getAbsolutePathToStandardOrganizationGoalModel(String orgModelFolder, String myLevel) {
        String strFile = getAbsolutePathToStandardOrganizationModelsFolder() + "/" + orgModelFolder + "/" +
                myLevel + "GoalModel.goal";
        File f = new File(strFile);
        if (f.exists() && !f.isDirectory()) {
            if (debug) LOG.info("{} Standard organization goal model file is {}", myLevel,
                    strFile);
        } else {
            LOG.error("ERROR: {} Standard organization goal model file {} not found. ",
                    myLevel, strFile);
        }
        RunManager.setAbsolutePathToStandardOrganizationGoalModel(strFile);
        return strFile;
    }

    public static String getAbsolutePathToStandardOrganizationModelsFolder() {
        return absolutePathToStandardOrganizationModelsFolder;
    }

    public static void setAbsolutePathToStandardOrganizationModelsFolder(final String path) {
        if (RunManager.absolutePathToStandardOrganizationModelsFolder == null) {
            RunManager.absolutePathToStandardOrganizationModelsFolder = path;
        }
    }

    public static String getAbsolutePathToStandardOrganizationRoleModel(String orgModelFolder, String myLevel) {
        String strFile = getAbsolutePathToStandardOrganizationModelsFolder() + "/" + orgModelFolder + "/" +
                myLevel.toString() + "RoleModel.role";
        File f = new File(strFile);
        if (f.exists() && !f.isDirectory()) {
            if (debug) LOG.info("{} Standard organization role model file is {}", myLevel.toString(),
                    strFile);
        } else {
            LOG.error("ERROR: {} Standard organization role model file {} not found. ",
                    myLevel.toString(), strFile);
        }
        RunManager.setAbsolutePathToStandardOrganizationRoleModel(strFile);
        return strFile;
    }

    public static void setAbsolutePathToStandardOrganizationRoleModel(final String absolutePathToStandardOrganizationRoleModel) {
        RunManager.absolutePathToStandardOrganizationRoleModel = absolutePathToStandardOrganizationRoleModel;
    }

    public static String getAbsolutePathToStandardPropertiesFolder() {
        return absolutePathToStandardPropertiesFolder;
    }

    public static void setAbsolutePathToStandardPropertiesFolder(String absolutePathToStandardPropertiesFolder) {
        RunManager.absolutePathToStandardPropertiesFolder = absolutePathToStandardPropertiesFolder;
    }

    public static String getAbsolutePathToTestCaseFolder() {
        return absolutePathToTestCaseFolder;
    }

    public static void setAbsolutePathToTestCaseFolder(
            final String getAbsolutePathToTestCaseFolder) {
        if (RunManager.absolutePathToTestCaseFolder == null) {
            RunManager.absolutePathToTestCaseFolder = getAbsolutePathToTestCaseFolder;
        } else {
            throwException();
        }
    }

    public static boolean getShowCommunicationsInBrowser() {
        return RunManager.showCommunicationsInBrowser;
    }

    public static void setShowCommunicationsInBrowser(boolean commInBrowser) {
        showCommunicationsInBrowser = commInBrowser;
    }

    public static int getDeliveryCheckTime_ms() {
        return deliveryCheckTime_ms;
    }

    public static void setDeliveryCheckTime_ms(int deliveryCheckTime_ms) {
        RunManager.deliveryCheckTime_ms = deliveryCheckTime_ms;
    }

    public synchronized static int getPlanningHorizonInMinutes() {
        return planningHorizonInMinutes;
    }

    public synchronized static void setPlanningHorizonInMinutes(final int planningHorizonInMinutes) {
        RunManager.planningHorizonInMinutes = planningHorizonInMinutes;
    }

    public static double getPowerFactor() {
        return powerFactor;
    }

    public static void setPowerFactor(final double powerFactor) {
        RunManager.powerFactor = powerFactor;
    }

    public static int[] getPvEnabled() {
        return pvenabled;
    }

    public static void setPvEnabled(final String pvenabled) {
        int count = 0;
        StringTokenizer st = new StringTokenizer(pvenabled, ",");
        while (st.hasMoreTokens()) {
            RunManager.pvenabled[count] = Integer.parseInt(st.nextToken());
            count++;
        }
    }

    public synchronized static Boolean getShowSensors() {
        return showSensors;
    }

    public synchronized static void setShowSensors(final Boolean showSensors) {
        if (RunManager.showSensors == null) {
            RunManager.showSensors = showSensors;
        } else {
            throwException();
        }
    }

    public static SmartInverterAlgorithm getSmartInverterAlgorithm() {
        return smartInverterAlgorithm;
    }

    public static void setSmartInverterAlgorithm(
            final SmartInverterAlgorithm smartInverterAlgorithm) {
        RunManager.smartInverterAlgorithm = smartInverterAlgorithm;
    }

    public static String getTestCaseName() {
        return testCaseName;
    }

    public static void setTestCaseName(final String testCaseName) {
        if (RunManager.testCaseName == null) {
            RunManager.testCaseName = testCaseName;
        } else {
            throwException();
        }
    }

    public static String getTopGoal() {
        return RunManager.topGoal;
    }

    public static int getTotalSmartInvertersReported() {
        return totalSmartInvertersReported;
    }

    public static void setTotalSmartInvertersReported(final int totalSmartInvertersReported) {
        RunManager.totalSmartInvertersReported = totalSmartInvertersReported;
    }

    public static Boolean getUseLiveMatLab() {
        return useLiveMatLab;
    }

    public static void setUseLiveMatLab(final Boolean useLiveMatLab) {
        if (RunManager.useLiveMatLab == null) {
            RunManager.useLiveMatLab = useLiveMatLab;
        } else {
            throwException();
        }
    }

    public static int getMessageDelayMinMS() {
        LOG.info("Getting Property {}", messageDelayMinMS);
        return messageDelayMinMS;
    }

    public static void setMessageDelayMinMS(int delayMin_MS) {
        RunManager.messageDelayMinMS = delayMin_MS;
    }

    public static int getMessageDelayMaxMX() {
        LOG.info("Getting Property {}", messageDelayMaxMX);
        return messageDelayMaxMX;
    }

    public static void setMessageDelayMaxMX(int delayMax_MX) {
        RunManager.messageDelayMaxMX = delayMax_MX;
    }

    public static double getFractionMessagesDelayed() {
        LOG.info("Getting Property {}", fractionMessagesDelayed);
        return fractionMessagesDelayed;
    }

    public static void setFractionMessagesDelayed(double fracDelayed) {
        RunManager.fractionMessagesDelayed = fracDelayed;
    }

    public static double getMinCritical_kw() {
        LOG.info("Getting Property {}", minCritical_kw);
        return minCritical_kw;
    }

    public static double getMaxCritical_kw() {
        LOG.info("Getting Property {}", maxCritical_kw);
        return maxCritical_kw;
    }

    public static int getParticipantRetryDelay_ms() {
        LOG.info("Getting Property {}", participantRetryDelay_ms);
        return participantRetryDelay_ms;
    }

    public static int getParticipantTimeout_ms() {
        LOG.info("Getting Property {}", participantTimeout_ms);
        return participantTimeout_ms;
    }

    public static String getSelfAdminPackage() {
        LOG.info("Getting Property {}", selfAdminPackage);
        return selfAdminPackage;
    }

    public static void setSelfAdminPackage(String admin) {
        RunManager.selfAdminPackage = admin;
    }

    public static String getSelfTypesPackage() {
        LOG.info("Getting Property {}", selfTypesPackage);
        return selfTypesPackage;
    }

    public static void setSelfTypesPackage(String types) {
        RunManager.selfTypesPackage = types;
    }

    public static String getParticipatePackage() {
        LOG.info("Getting Property {}", participatePackage);
        return participatePackage;
    }

    public static void setParticipatePackage(String participate) {
        RunManager.participatePackage = participate;
    }

    public static String getAgentTypesPackage() {
        LOG.info("Getting Property {}", agentTypesPackage);
        return agentTypesPackage;
    }

    public static void setAgentTypesPackage(String agents) {
        RunManager.agentTypesPackage = agents;
    }

    public static String getEstimationSuffix() {
        LOG.info("Getting Property {}", estimationSuffix);
        return estimationSuffix;
    }

    public static void setEstimationSuffix(String estimSuffix) {
        RunManager.estimationSuffix = estimSuffix;
    }

    public static String getEstimationModelFolder() {
        LOG.info("Getting Property {}", estimationModelFolder);
        return estimationModelFolder;
    }

    public static void setEstimationModelFolder(String estimModelFolder) {
        RunManager.estimationModelFolder = estimModelFolder;
    }

    public static String getEstimationParticipatePackage() {
        LOG.info("Getting Property {}", estimationParticipatePackage);
        return estimationParticipatePackage;
    }

    public static void setEstimationParticipatePackage(String estimParticipate) {
        RunManager.estimationParticipatePackage = estimParticipate;
    }

    public static String getEstimationAdminPackage() {
        LOG.info("Getting Property {}", estimationAdminPackage);
        return estimationAdminPackage;
    }

    public static void setEstimationAdminPackage(String estimAdminPackage) {
        RunManager.estimationAdminPackage = estimAdminPackage;
    }

    public static String getEstimationTypesPackage() {
        LOG.info("Getting Property {}", estimationTypesPackage);
        return estimationTypesPackage;
    }

    public static void setEstimationTypesPackage(String estimTypes) {
        RunManager.estimationTypesPackage = estimTypes;
    }

    public static String getGridSuffix() {
        LOG.info("Getting Property {}", gridSuffix);
        return gridSuffix;
    }

    public static void setGridSuffix(String gSuffix) {
        RunManager.gridSuffix = gSuffix;
    }

    public static String getGridModelFolder() {
        LOG.info("Getting Property {}", gridModelFolder);
        return gridModelFolder;
    }

    public static void setGridModelFolder(String gridModel) {
        RunManager.gridModelFolder = gridModel;
    }

    public static String getGridParticipatePackage() {
        LOG.info("Getting Property {}", gridParticipatePackage);
        return gridParticipatePackage;
    }

    public static void setGridParticipatePackage(String gridParticipate) {
        RunManager.gridParticipatePackage = gridParticipate;
    }

    public static String getGridAdminPackage() {
        LOG.info("Getting Property {}", gridAdminPackage);
        return gridAdminPackage;
    }

    public static void setGridAdminPackage(String gridAdmin) {
        RunManager.gridAdminPackage = gridAdmin;
    }

    public static String getGridTypesPackage() {
        LOG.info("Getting Property {}", gridTypesPackage);
        return gridTypesPackage;
    }

    public static void setGridTypesPackage(String gridTypes) {
        RunManager.gridTypesPackage = gridTypes;
    }

    public static String getMarketSuffix() {
        LOG.info("Getting Property {}", marketSuffix);
        return marketSuffix;
    }

    public static void setMarketSuffix(String mSuffix) {
        RunManager.marketSuffix = mSuffix;
    }

    public static String getMarketModelFolder() {
        LOG.info("Getting Property {}", marketModelFolder);
        return marketModelFolder;
    }

    public static void setMarketModelFolder(String marketModel) {
        RunManager.marketModelFolder = marketModel;
    }

    public static String getMarketParticipatePackage() {

        LOG.info("Getting Property {}", marketParticipatePackage);
        return marketParticipatePackage;
    }

    public static void setMarketParticipatePackage(String marketParticipate) {
        RunManager.marketParticipatePackage = marketParticipate;
    }

    public static String getMarketAdminPackage() {
        LOG.info("Getting Property {}", marketAdminPackage);
        return marketAdminPackage;
    }

    public static void setMarketAdminPackage(String marketAdmin) {
        LOG.info("Getting Property {}", marketAdmin);
        RunManager.marketAdminPackage = marketAdmin;
    }

    public static String getMarketTypesPackage() {
        LOG.info("Getting Property {}", marketTypesPackage);
        return marketTypesPackage;
    }

    public static void setMarketTypesPackage(String marketTypes) {
        RunManager.marketTypesPackage = marketTypes;
    }

    public static String getValue(String propertyName) {
        if (!isLoaded) {
            RunManager.load();
        }
        return RunManager.properties.getProperty(propertyName);
    }

    public static void load() {
        setProperties(new Properties());
        final File f = new File(CUR_DIR, "run.properties");
        final File file2 = new File(CUR_DIR, "self.properties");
        final File file3 = new File(CUR_DIR, "worker.properties");
        final File dir = new File(CUR_DIR);
        final File[] matches = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".org.properties");
            }
        });

        LOG.info("Reading user-specified information for {}.", f.getAbsolutePath());
        // try loading from the current directory
        try {
            try (FileInputStream fileInputStream = new FileInputStream(f)) {
                properties = new Properties();
                properties.load(fileInputStream);
            }
            try (FileInputStream fileInputStream = new FileInputStream(file2)) {
                properties.load(fileInputStream);
            }
            try (FileInputStream fileInputStream = new FileInputStream(file3)) {
                properties.load(fileInputStream);
            }
            for(File file : matches) {
                try (FileInputStream fileInputStream = new FileInputStream(file)) {
                    properties.load(fileInputStream);
                }
            }
            for (Object o : new TreeSet<>(properties.keySet())) {
                final String key = (String) o;
                final String value = properties.getProperty(key);
                LOG.info("\t Reading Property {}: {}", key, value);
            }

            isLoaded = true;
            initializeStepMode(getValue("stepmode"));
            initializeStepDelay(getValue("stepdelay"));
            initializeAbsolutePathToSourceFolder(getValue("sourcepath"));
            initializeAbsolutePathToLogConfigFile(getValue("logpath"));
            initializeAbsolutePathToConfigsFolder(getValue("configpath"));
            initializeSimulationStartTime(getValue("startdate"), getValue("starttime"));
            initializeAbsolutePathToMatLabCodeFolder(getValue("matlabcodepath"));
            initializeAbsolutePathToMatLabDataFolder(getValue("matlabdatapath"));
            initializeAbsolutePathToStandardAgentModelsFolder(getValue("standardagentmodelspath"));
            initializeAbsolutePathToStandardOrganizationModelsFolder(getValue("standardorganizationmodelspath"));
            initializeAbsolutePathToStandardPropertiesFolder(getValue("standardpropertiespath"));
            initializeAbsolutePathtoSensorDataFile(getValue("configpath"), getValue("datafile"));
            initializeTestCaseName(getValue("configpath"), getValue("testcase"));
            initializeGetAbsolutePathToTestCaseFolder(getValue("configpath"), getValue("testcase"));
            initializeUseLiveMatLab(getValue("uselivematlab"));
            initializeMaxTimeSlices(getValue("maxtimeslices"));
            initializePowerFactor(getValue("powerfactor"));
            initializeSmartInverterAlgorithm(getValue("smartinverteralgorithm"));
            initializeTopGoal(getValue("topgoal"));
            initializeLengthOfTimesliceInMilliseconds(getValue("lengthoftimesliceinmilliseconds"));
            initializePlanningHorizonInMinutes(getValue("planninghorizoninminutes"));
            initializePvEnabled(getValue("pvenabled"));
            initializeInitializeGoalsInConstructors(getValue("initializegoalsinconstructors"));
            initializeShowCommunicationsInBrowser(getValue("showcommunicationsinbrowser"));
            initializeShowAgentOrganizations(getValue("showagentorganizations"));
            initializeStaticLayoutImageResource(getValue("staticlayoutimageresource"));
            initializeShowSensors(getValue("showsensors"));
            initializeDeliveryCheckTime(getValue("deliverychecktimems"));
            initializeWaitTime(getValue("standardwaitms"));
            initializeMessageDelayMinMS(getValue("messagedelayminms"));
            initializeMessageDelayMaxMX(getValue("messagedelaymaxmx"));
            initializeFractionMessagesDelayed(getValue("fractionmessagesdelayed"));
            initializeMinCriticalKW(getValue("mincriticalkw"));
            initializeMaxCriticalKW(getValue("maxcriticalkw"));
            initializeParticipantRetryDelayMS(getValue("participantretrydelayms"));
            initializeParticipantTimeoutMS(getValue("participanttimeoutms"));
            initializeSelfAdminPackage(getValue("self_admin_package"));
            initializeSelfTypesPackage(getValue("self_types_package"));
            initializeParticipatePackage(getValue("participate_package"));
            initializeAgentTypesPackage(getValue("agent_types_package"));
            initializeEstimationSuffix(getValue("estimation_suffix"));
            initializeEstimationModelFolder(getValue("estimation_model_folder"));
            initializeEstimationParticipationPackage(getValue("estimation_participate_package"));
            initializeEstimationAdminPackage(getValue("estimation_admin_package"));
            initializeEstimationTypesPackage(getValue("estimation_types_package"));
            initializeGridSuffix(getValue("grid_suffix"));
            initializeGridModelFolder(getValue("grid_model_folder"));
            initializeGridParticipatePackage(getValue("grid_participate_package"));
            initializeGridAdminPackage(getValue("grid_admin_package"));
            initializeGridTypesPackage(getValue("grid_types_package"));
            initializeMarketSuffix(getValue("market_suffix"));
            initializeMarketModelFolder(getValue("market_model_folder"));
            initializeMarketParticipatePackage(getValue("market_participate_package"));
            initializeMarketAdminPackage(getValue("market_admin_package"));
            initializeMarketTypesPackage(getValue("market_types_package"));
        } catch (FileNotFoundException e) {
            LOG.error("Run properties file not found.");
        } catch (IOException e) {
            LOG.error("Run properties file - error reading contents.");
        }
    }

    /**
     * @param properties the properties to set
     */
    private static void setProperties(Properties properties) {
        RunManager.properties = properties;
    }

    private static void initializeStepMode(final String stepmode) {
        if (stepmode.trim().toLowerCase().equals(
                "yes") || stepmode.trim().toLowerCase().equals(
                "true") || stepmode
                .trim().toLowerCase().equals("on")) {
            Player.setStepMode(Player.StepMode.STEP_BY_STEP);
        }
    }

    private static void initializeStepDelay(final String stepdelay) {
        try {
            Player.setStepDelayInMilliseconds(Integer.parseInt(stepdelay.toLowerCase()));
        } catch (Exception e) {
            Player.setStepDelayInMilliseconds(0L);
        }
    }

    private static void initializeAbsolutePathToSourceFolder(final String sourcepath) {
        final String strFolder = CUR_DIR + sourcepath.trim();
        final String title = "source folder";
        RunManager.setAbsolutePathToSourceFolder(
                verifyFolderExists(strFolder, title));
    }

    public static int getTotalConnectionCount() {
        // there are 2 parallel organizations
        totalConnectionCount = 2 * RunManager.getCountAllAgents() - 1;
        return totalConnectionCount;
    }

    public static void setAbsolutePathToSourceFolder(final String absolutePathToSourceFolder) {
        if (RunManager.absolutePathToSourceFolder == null) {
            RunManager.absolutePathToSourceFolder = absolutePathToSourceFolder;
        }
    }

    public synchronized static void registered(final String identifierString, final IMessagingFocus messagingFocus) {
        if (messagingFocus.equals(MessagingFocus.GRID_PARTICIPATE)) {
            gridRegistered.add(identifierString);
            if (debug) LOG.info("{} is the {} agent to register.", identifierString, gridRegistered.size());
        }
        if (messagingFocus.equals(MessagingFocus.MARKET_PARTICIPATE)) {
            marketRegistered.add(identifierString);
            if (debug) LOG.info("{} is the {} agent to register.", identifierString, marketRegistered.size());
        }

    }

    private static String verifyFolderExists(final String strFolder, final String title) {
        final File f = new File(strFolder);
        if (f.exists() && f.isDirectory()) {
            if (debug) LOG.debug("{} path is {}", title, strFolder);
        } else {
            LOG.error("ERROR: {} path {} not found. ", title, strFolder);
        }
        return strFolder;
    }

    private static void initializeAbsolutePathToLogConfigFile(final String logpath) {
        final String strFile = CUR_DIR + logpath.trim();
        final String title = "log configuration file";
        RunManager.setAbsolutePathToLogbackXMLFile(
                verifyFileExists(strFile, title));
    }

    public static void setAbsolutePathToLogbackXMLFile(final String absolutePathToLogbackXMLFile) {
        RunManager.absolutePathToLogbackXMLFile = absolutePathToLogbackXMLFile;
    }

    private static String verifyFileExists(final String strFile, final String title) {
        final File f = new File(strFile);
        if (f.exists()){
            if(!f.isDirectory()){

            if (debug) LOG.debug("title is {}", strFile);
        }} else {
            LOG.info("INFO: {} {} not found. ", title, strFile);
        }
        return strFile;
    }

    private static void initializeAbsolutePathToConfigsFolder(final String configpath) {
        final String strFolder = CUR_DIR + configpath.trim();
        final String title = "config folder";
        RunManager.setAbsolutePathToConfigsFolder(
                verifyFolderExists(strFolder, title));
    }

    /**
     * @param startdate in format "Jan 2 2013"
     * @param starttime in format "11:00 AM"
     */
    private static void initializeSimulationStartTime(final String startdate, final String starttime) {
        try {
            final Date date = new SimpleDateFormat("MMM d yyyy h:m a",
                    Locale.ENGLISH).parse(startdate.trim() + " " + starttime
                    .trim());
            if (debug) LOG.debug("\tSimulation begins at: {} ", date);
            final GregorianCalendar cal = new GregorianCalendar(
                    TimeZone.getTimeZone("America/Chicago"), Locale.US);
            cal.setTime(date);
            cal.set(Calendar.MILLISECOND, 0);
            Clock.setSimulationStartTime(cal);
            Clock.setSimulationTime(cal);
            if (debug) LOG.debug("\tRun Manager simulation start time is: {}", cal);
        } catch (ParseException e) {
            LOG.error(
                    "ERROR: cannot read simulation start time from run" +
                            ".properties. Must be  in " +
                            "SimpleDateFormat('MMM d yyyy h:m a', " +
                            "Locale.ENGLISH) like startdate=Aug 19 2013 and " +
                            "starttime=11:00 AM"
            );
            System.exit(-1);
        }
    }

    private static void initializeAbsolutePathToMatLabCodeFolder(final String matlabcodepath) {
        String connector = "//";
        final String relpath = matlabcodepath.trim();
        if (relpath.startsWith("/")) {
            connector = "";
        }
        final String strFolder = CUR_DIR + connector + relpath;
        final String title = "MatLab code folder";
        RunManager.setAbsolutePathToMatLabCodeFolder(
                verifyFolderExists(strFolder, title));
    }

    public static int throwException() {
        // throw new RuntimeException("Cannot overwrite scenario settings.");
        return 0;
    }

    private static void initializeAbsolutePathToMatLabDataFolder(final String matlabdatapath) {
        String connector = "//";
        final String relpath = matlabdatapath.trim();
        if (relpath.startsWith("/")) {
            connector = "";
        }
        final String strFolder = CUR_DIR + connector + relpath;
        final String title = "MatLab code folder";
        RunManager.setAbsolutePathToMatLabDataFolder(
                verifyFolderExists(strFolder, title));
    }

    private static void initializeAbsolutePathToStandardAgentModelsFolder(String path) {
        final String strFolder = CUR_DIR + path.trim();
        final String title = "standard agent models folder";
        RunManager.setAbsolutePathToStandardAgentModelsFolder(verifyFolderExists(strFolder, title));
    }

    private static void initializeAbsolutePathToStandardOrganizationModelsFolder(final String path) {
        final String strFolder = CUR_DIR + path.trim();
        final String title = "standard org models folder";
        RunManager.setAbsolutePathToStandardOrganizationModelsFolder(verifyFolderExists(strFolder, title));
    }

    private static void initializeAbsolutePathToStandardPropertiesFolder(final String standardpropertiespath) {
        String connector = "//";
        final String relpath = standardpropertiespath.trim();
        if (relpath.startsWith("/")) {
            connector = "";
        }
        final String strFolder = CUR_DIR + connector + relpath;
        final String title = "Standard properties folder";
        RunManager.setAbsolutePathToStandardPropertiesFolder(
                verifyFolderExists(strFolder, title));
    }

    private static void initializeAbsolutePathtoSensorDataFile(final String configpath, final String datafile) {
        final String strFile = getAbsolutePathToConfigsFolder(configpath.trim()) + datafile.trim();
        final String title = "Sensor data file";
        RunManager.setAbsolutePathToSensorDataFile(verifyFileExists(strFile, title));
    }

    public static String getAbsolutePathToConfigsFolder(final String configpath) {
        String connector = "/";
        if (configpath.trim().endsWith("/")) {
            connector = "";
        }
        final String strFolder = CUR_DIR + configpath + connector;
        final String title = "Configs folder";
        return verifyFolderExists(strFolder, title);
    }

    private static void initializeTestCaseName(final String configpath, final String testcase) {
        String connector = "//";
        if (configpath.endsWith("/") || testcase.startsWith("/")) {
            connector = "";
        }
        final String strFolder = CUR_DIR + configpath.trim() + connector + testcase.trim();
        final String title = "Test case folder";
        if (!verifyFolderExists(strFolder, title).isEmpty()) {
            RunManager.setTestCaseName(testcase.trim());
        }
    }

    private static void initializeGetAbsolutePathToTestCaseFolder(final String configpath, final String testcase) {
        String connector = "//";
        if (configpath.endsWith("/") || testcase.startsWith("/")) {
            connector = "";
        }
        final String strFolder = CUR_DIR + configpath.trim() + connector + testcase.trim();
        final String title = "Test case folder";
        RunManager.setAbsolutePathToTestCaseFolder(verifyFolderExists(strFolder, title));
    }

    private static void initializeUseLiveMatLab(final String uselivematlab) {
        final String strUseLiveMatLab = uselivematlab.trim().toLowerCase();
        RunManager.setUseLiveMatLab(
                strUseLiveMatLab.equals("yes") || strUseLiveMatLab.equals(
                        "true") || strUseLiveMatLab.equals("y")
        );
    }

    private static void initializeMaxTimeSlices(final String maxtimeslices) {
        try {
            int max = Integer.parseInt(maxtimeslices.trim());
            Clock.setMaxTimeSlices(max);
        } catch (Exception e) {
            LOG.error("ERROR: maximum time slices could not be read. {}", maxtimeslices);
        }
    }

    private static void initializePowerFactor(final String powerfactor) {
        if (debug) LOG.debug("Getting power factor {} from run.properties {}",
                Double.valueOf(Double.parseDouble(powerfactor)),
                powerfactor);
        RunManager.setPowerFactor(Double.parseDouble(powerfactor));
    }

    private static void initializeSmartInverterAlgorithm(
            final String smartinverteralgorithm) {
        if (debug) LOG.debug("Getting smart inverter algorithm {} from run.properties {}",
                Integer.valueOf(Integer.parseInt(smartinverteralgorithm)),
                smartinverteralgorithm);
        final int i = Integer.parseInt(smartinverteralgorithm);
        if (!(i == 0 || i == 1)) {
            LOG.error("ERROR: Algorithm setting should be 0 (no smart inverter " +
                            "control) or 1 (fixed power factor with" +
                            " a bounded maximum) but it was {}",
                    smartinverteralgorithm
            );
            System.exit(-1);
        }
        if (debug) LOG.debug("Getting algorithm setting {} from run.properties {}",
                Integer.parseInt(smartinverteralgorithm),
                smartinverteralgorithm);
        if (i == 0) {
            RunManager.setSmartInverterAlgorithm(
                    SmartInverterAlgorithm.NO_INJECTION);
            LOG.warn("WARNING: setting the smart inverter algorithm to 0 or " +
                            "NO_INJECTION for gridConnections inverters will " +
                            "render them inoperable."
            );
        } else {
            RunManager.setSmartInverterAlgorithm(SmartInverterAlgorithm.FIXED_PF_BOUNDED_BY_MAXIMUM);
        }
    }

    private static void initializeTopGoal(final String topgoal) {
        RunManager.topGoal = topgoal.trim();
    }

    private static void initializeLengthOfTimesliceInMilliseconds(final String lengthoftimesliceinmilliseconds) {
        try {
            int msecs = Integer.parseInt(lengthoftimesliceinmilliseconds.trim());
            Clock.setLengthOfTimesliceInMilliseconds(msecs);
        } catch (Exception e) {
            LOG.error("ERROR: length of time slice could not be read. {}", lengthoftimesliceinmilliseconds);
        }
    }

    private static void initializePlanningHorizonInMinutes(String planninghorizoninminutes) {
        try {
            int planHorizon = Integer.parseInt(planninghorizoninminutes.trim());
            RunManager.setPlanningHorizonInMinutes(planHorizon);
        } catch (Exception e) {
            LOG.error("ERROR: length of planning horizon could not be read. {}", planninghorizoninminutes);
        }
    }

    private static void initializePvEnabled(final String pvenabled) {
        RunManager.setPvEnabled(pvenabled);
    }

    private static void initializeInitializeGoalsInConstructors(
            final String initializegoalsinconstructors) {
        RunManager.setInitializeGoalsInConstructors(Boolean.TRUE);
        try {
            final String strValue = initializegoalsinconstructors.trim().toLowerCase();
            RunManager.setInitializeGoalsInConstructors(
                    strValue.equals("yes") || strValue.equals("true") || strValue.equals("y"));
        } catch (Exception e) {
            // continue
        }
    }

    public static void setInitializeGoalsInConstructors(final boolean initializeGoalsInConstructors) {
        RunManager.initializeGoalsInConstructors = initializeGoalsInConstructors;
    }

    private static void initializeShowCommunicationsInBrowser(final String raw) {
        final String input = raw.trim().toLowerCase();
        RunManager.setShowCommunicationsInBrowser(
                input.equals("yes") || input.equals(
                        "true") || input.equals("y") || input.equals("on")
        );
    }

    private static void initializeShowAgentOrganizations(final String raw) {
        final String input = raw.trim().toLowerCase();
        RunManager.setShowAgentOrganizations(
                input.equals("yes") || input.equals(
                        "true") || input.equals("y") || input.equals("on")
        );
    }

    private static void initializeStaticLayoutImageResource(String staticlayoutimageresource) {
        final String strFile = CUR_DIR + "/" + staticlayoutimageresource.trim();
        LOG.info("Using image file {}", strFile);
        final String title = "static layout image resource file";
        RunManager.setStaticLayoutImageResourceFileName(
                verifyFileExists(strFile, title));
    }

    public static void setStaticLayoutImageResourceFileName(
            String absolutePathtoStaticLayoutImageFile) {
        if (RunManager.absolutePathtoStaticLayoutImageFile == null) {
            RunManager.absolutePathtoStaticLayoutImageFile = absolutePathtoStaticLayoutImageFile;
        } else {
            throwException();
        }
    }

    private static void initializeShowSensors(String showsensors) {
        final String strShowSensors = showsensors.trim().toLowerCase();
        RunManager.setShowSensors(
                strShowSensors.equals("yes") || strShowSensors.equals(
                        "true") || strShowSensors.equals("y")
        );
    }

    private static void initializeDeliveryCheckTime(String input) {
        try {
            int msecs = Integer.parseInt(input.trim());
            RunManager.setDeliveryCheckTime_ms(msecs);
        } catch (Exception e) {
            LOG.error("ERROR: delivery checktime in ms could not be read. {}", input);
        }
    }

    private static void initializeWaitTime(String input) {
        try {
            int msecs = Integer.parseInt(input.trim());
            RunManager.setStandardWaitTime_ms(msecs);
        } catch (Exception e) {
            LOG.error("ERROR: standard wait time in ms could not be read. {}", input);
        }
    }

    public static void setStandardWaitTime_ms(int standardWaitTime_ms) {
        RunManager.standardWaitTime_ms = standardWaitTime_ms;
    }

    private static void initializeMessageDelayMinMS(String input) {
        try {
            int minMS = Integer.parseInt(input.trim());
            RunManager.setMessageDelayMinMS(minMS);
        } catch (Exception e) {
            LOG.error("ERROR: message delay min could not be read. {}", input);
        }
    }

    private static void initializeMessageDelayMaxMX(String input) {
        try {
            int maxMX = Integer.parseInt(input.trim());
            RunManager.setMessageDelayMaxMX(maxMX);
        } catch (Exception e) {
            LOG.error("ERROR: message delay max could not be read. {}", input);
        }
    }

    private static void initializeFractionMessagesDelayed(String input) {
        try {
            double fractionDelayed = Double.parseDouble(input.trim());
            RunManager.setFractionMessagesDelayed(fractionDelayed);
        } catch (Exception e) {
            LOG.error("ERROR: fraction message delayed could not be read. {}", input);
        }
    }

    private static void initializeMinCriticalKW(String input) {
        try {
            double minCritical = Double.parseDouble(input.trim());
            RunManager.setMinCriticalKW(minCritical);
        } catch (Exception e) {
            LOG.error("ERROR: min critical kW could not be read. {}", input);
        }
    }

    public static void setMinCriticalKW(double minCrit) {
        RunManager.minCritical_kw = minCrit;
    }

    private static void initializeMaxCriticalKW(String input) {
        try {
            double maxCritical = Double.parseDouble(input.trim());
            RunManager.setMaxCriticalKW(maxCritical);
        } catch (Exception e) {
            LOG.error("ERROR: max critical kW could not be read. {}", input);
        }
    }

    public static void setMaxCriticalKW(double maxCrit) {
        RunManager.maxCritical_kw = maxCrit;
    }

    private static void initializeParticipantRetryDelayMS(String input) {
        try {
            int participantRetryDelay = Integer.parseInt(input.trim());
            RunManager.setParticipantRetryDelayMS(participantRetryDelay);
        } catch (Exception e) {
            LOG.error("ERROR: participant retry delay could not be read. {}", input);
        }
    }

    public static int getStandardWaitTime_ms() {
        return standardWaitTime_ms;
    }

    public static void setParticipantRetryDelayMS(int retryDelay) {
        RunManager.participantRetryDelay_ms = retryDelay;
    }

    private static void initializeParticipantTimeoutMS(String input) {
        try{
            int participantTimeoutDelay = Integer.parseInt(input.trim());
            RunManager.setParticipantTimeoutMS(participantTimeoutDelay);
        } catch(Exception e) {
            LOG.error("ERROR: participant timeouts could not be read. {}", input);
        }
    }

    public static void setParticipantTimeoutMS(int timeouts) {
        RunManager.setParticipantTimeout_ms(timeouts);
    }

    private static void initializeSelfAdminPackage(String input) {
        try{
            RunManager.setSelfAdminPackage(input);
        } catch (Exception e) {
            LOG.error("ERROR: Self admin package could not be read. {}", input);
        }
    }

    private static void initializeSelfTypesPackage(String input) {
        try {
            RunManager.setSelfTypesPackage(input);
        } catch (Exception e) {
            LOG.error("ERROR: Self Types Package could not be read. {}", input);
        }
    }

    private static void initializeParticipatePackage(String input) {
        try {
            RunManager.setParticipatePackage(input);
        } catch (Exception e) {
            LOG.error("ERROR: Participate package could not be read. {}", input);
        }
    }

    private static void initializeAgentTypesPackage(String input) {
        try{
            RunManager.setAgentTypesPackage(input);
        } catch(Exception e) {
            LOG.error("ERROR: Agent types package could not be read. {}", input);
        }
    }

    private static void initializeEstimationSuffix(String input) {
        try{
            RunManager.setEstimationSuffix(input);
        } catch(Exception e) {
            LOG.error("ERROR: Estimation Suffix could not be read. {}", input);
        }
    }

    private static void initializeEstimationModelFolder(String input) {
        try{
            RunManager.setEstimationModelFolder(input);
        } catch(Exception e) {
            LOG.error("ERROR: Estimation Model Folder could not be read. {}", input);
        }
    }

    private static void initializeEstimationParticipationPackage(String input) {
        try{
            RunManager.setEstimationParticipatePackage(input);
        } catch(Exception e) {
            LOG.error("ERROR: Estimation Participate Package could not be read. {}", input);
        }
    }

    private static void initializeEstimationAdminPackage(String input) {
        try{
            RunManager.setEstimationAdminPackage(input);
        } catch(Exception e){
            LOG.error("ERROR: Estimation Admin Package could not be read. {}", input);
        }
    }

    private static void initializeEstimationTypesPackage(String input) {
        try {
            RunManager.setEstimationTypesPackage(input);
        } catch(Exception e) {
            LOG.error("ERROR: Estimation Types Package could not be read. {}", input);
        }
    }

    private static void initializeGridSuffix(String input){
        try{
            RunManager.setGridSuffix(input);
        } catch(Exception e) {
            LOG.error("ERROR: Grid Suffix could not be read. {}", input);
        }
    }

    private static void initializeGridModelFolder(String input) {
        try{
            RunManager.setGridModelFolder(input);
        } catch(Exception e){
            LOG.error("ERROR: Grid Model Folder could not be read. {}", input);
        }
    }

    private static void initializeGridParticipatePackage(String input){
        try{
            RunManager.setGridParticipatePackage(input);
        } catch (Exception e){
            LOG.error("ERROR: Grid Model Folder could not be read. {}", input);

        }
    }

    private static void initializeGridAdminPackage(String input){
        try{
            RunManager.setGridAdminPackage(input);
        } catch (Exception e) {
            LOG.error("ERROR: Grid Admin Package could not be read. {}", input);
        }
    }

    private static void initializeGridTypesPackage(String input){
        try{
            RunManager.setGridTypesPackage(input);
        } catch(Exception e) {
            LOG.error("ERROR: Grid Types Package could not be read. {}", input);
        }
    }

    private static void initializeMarketSuffix(String input){
        try{
            RunManager.setMarketSuffix(input);
        } catch(Exception e){
            LOG.error("ERROR: Market Suffix could not be read. {}", input);
        }
    }

    private static void initializeMarketModelFolder(String input){
        try{
            RunManager.setMarketModelFolder(input);
        } catch(Exception e){
            LOG.error("ERROR: Market Model Folder could not be read. {}", input);
        }
    }

    private static void initializeMarketParticipatePackage(String input) {
        try{
            RunManager.setMarketParticipatePackage(input);
        }catch(Exception e){
            LOG.error("ERROR: Market Participate Package could not be read. {}", input);
        }
    }

    private static void initializeMarketAdminPackage(String input){
        try{
            RunManager.setMarketAdminPackage(input);
        }catch (Exception e){
            LOG.error("ERROR: Market Admin Package could not be read. {}", input);
        }
    }

    private static void initializeMarketTypesPackage(String input){
        try{
            RunManager.setMarketTypesPackage(input);
        } catch(Exception e) {
            LOG.error("ERROR: Market Types Package could not be read. {}", input);
        }
    }

    public static boolean isStopped() {
        return isStopped;
    }

    public static void setAbsolutePathToSensorDataFile(final String configpath, final String pathToDataFile) {
        String connector = "/";
        if (configpath.trim().endsWith("/")) {
            connector = "";
        }
        RunManager.absolutePathtoSensorDataFile = CUR_DIR + configpath.trim() + connector + pathToDataFile.trim();
    }

    private static void terminateSuccessfully() {
        LOG.info("TERMINATING: Ending partial run successfully. See Scenario for force stop conditions.");
        System.exit(-777);
    }

    public synchronized static boolean isInitiallyConnected() {
        return RunManager.initiallyConnected;
    }

    public synchronized static void setInitiallyConnected(final boolean initiallyConnected) {
        RunManager.initiallyConnected = initiallyConnected;
        if (RunManager.isInitiallyConnected() && RunManager.FORCE_STOP_AFTER_INITIALLY_CONNECTED) terminateSuccessfully();
        if (RunManager.isInitiallyConnected() && !RunManager.FORCE_STOP_AFTER_INITIALLY_CONNECTED)
            LOG.info(" ..........INITIALLY FULLY CONNECTED ");
    }

    public static void setIsStopped(boolean isStopped) {
        RunManager.isStopped = isStopped;
    }

    public static void setStopMonitor(final boolean isStopped) {
        RunManager.isStopped = isStopped;
    }

    public static boolean getShowAgentOrganizations() {
        return RunManager.showAgentOrganizations;
    }

    public static void setShowAgentOrganizations(boolean val) {
        RunManager.showAgentOrganizations = val;
    }

    public static void setParticipantTimeout_ms(int participantTimeout_ms) {
        RunManager.participantTimeout_ms = participantTimeout_ms;
    }

//
//  public synchronized void createCheckpointsForPowerUp(){
//
//    // sensor home to home self
//    MessagingCheckpoint.createMessagingCheckpoint(AgentType.Home, AgentType.Home, RoleIdentifiers.Manage_Home_Role, RoleIdentifiers.Self_Control_Role);
//
//    // home self to home sub
//    MessagingCheckpoint.createMessagingCheckpoint(AgentType.Home, AgentType.Home,  RoleIdentifiers.Self_Control_Role, RoleIdentifiers.Be_Holon_Role);
//
//    // home sub to neighborhood super
//    MessagingCheckpoint.createMessagingCheckpoint(AgentType.Home, AgentType.Neighborhood,  RoleIdentifiers.Be_Holon_Role, RoleIdentifiers.Be_Super_Holon_Role);
//
//    // neighborhood super to neighborhood self
//    MessagingCheckpoint.createMessagingCheckpoint(AgentType.Neighborhood, AgentType.Neighborhood,  RoleIdentifiers.Be_Super_Holon_Role, RoleIdentifiers.Self_Control_Role);
//
//    // neighborhood self to neighborhood sub
//    MessagingCheckpoint.createMessagingCheckpoint(AgentType.Neighborhood, AgentType.Neighborhood,  RoleIdentifiers.Self_Control_Role, RoleIdentifiers.Be_Holon_Role);
//
//    // neighborhood sub to lateral super
//    MessagingCheckpoint.createMessagingCheckpoint(AgentType.Neighborhood, AgentType.Lateral,   RoleIdentifiers.Be_Holon_Role, RoleIdentifiers.Be_Super_Holon_Role);
//
//    // lateral super to lateral self
//    MessagingCheckpoint.createMessagingCheckpoint(AgentType.Lateral, AgentType.Lateral,   RoleIdentifiers.Be_Super_Holon_Role, RoleIdentifiers.Self_Control_Role);
//
//    // lateral self to lateral sub
//    MessagingCheckpoint.createMessagingCheckpoint(AgentType.Lateral, AgentType.Lateral,  RoleIdentifiers.Self_Control_Role, RoleIdentifiers.Be_Holon_Role);
//
//    // lateral sub to lateral or feeder super
//    MessagingCheckpoint.createMessagingCheckpoint(AgentType.Lateral, AgentType.Lateral,   RoleIdentifiers.Be_Holon_Role, RoleIdentifiers.Be_Super_Holon_Role);
//    MessagingCheckpoint.createMessagingCheckpoint(AgentType.Lateral, AgentType.Feeder,   RoleIdentifiers.Be_Holon_Role, RoleIdentifiers.Be_Super_Holon_Role);
//
//    // feeder super to feeder self
//    MessagingCheckpoint.createMessagingCheckpoint(AgentType.Feeder, AgentType.Feeder,   RoleIdentifiers.Be_Super_Holon_Role, RoleIdentifiers.Self_Control_Role);
//
//    // feeder self to feeder sub
//    MessagingCheckpoint.createMessagingCheckpoint(AgentType.Feeder, AgentType.Feeder,  RoleIdentifiers.Self_Control_Role, RoleIdentifiers.Be_Holon_Role);
//
//    // feeder sub to feeder or substation supers
//    MessagingCheckpoint.createMessagingCheckpoint(AgentType.Feeder, AgentType.Feeder,   RoleIdentifiers.Be_Holon_Role, RoleIdentifiers.Be_Super_Holon_Role);
//    MessagingCheckpoint.createMessagingCheckpoint(AgentType.Feeder, AgentType.Substation,   RoleIdentifiers.Be_Holon_Role, RoleIdentifiers.Be_Super_Holon_Role);
//
//    // substation super to substation self
//    MessagingCheckpoint.createMessagingCheckpoint(AgentType.Substation, AgentType.Substation,   RoleIdentifiers.Be_Super_Holon_Role, RoleIdentifiers.Self_Control_Role);
//
//
//  }
}
