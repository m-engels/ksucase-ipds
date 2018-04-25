package edu.ksu.cis.macr.ipds.self.guidelines;


import edu.ksu.cis.macr.aasis.common.IConnections;
import edu.ksu.cis.macr.ipds.grid.goals.GridGoalParameters;
import edu.ksu.cis.macr.ipds.market.goals.MarketGoalParameters;
import edu.ksu.cis.macr.ipds.market.guidelines.auction.AuctionGuidelinesBuilder;
import edu.ksu.cis.macr.ipds.market.guidelines.broker.BrokerGuidelinesBuilder;
import edu.ksu.cis.macr.ipds.primary.goals.AgentGoalParameters;
import edu.ksu.cis.macr.ipds.primary.guidelines.*;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 The {@code SelfGuidelineManager} reads goal guidelines (the top goal parameters) from a given XML
 file.
 */
public enum SelfGuidelineManager {
    INSTANCE;
    private static final Logger LOG = LoggerFactory.getLogger(SelfGuidelineManager.class);
    private static final boolean debug = false;

    public synchronized static Map<UniqueIdentifier, Object> getGuidelines(final String absPathToFile) {
        LOG.info("Entering getGuidelines(absPathToFile={})", absPathToFile);
        HashMap<UniqueIdentifier, Object> goalParameterValues = new HashMap<>();
        DocumentBuilder db = null;
        try {
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            if (db == null) {
                throw new ParserConfigurationException();
            }
        } catch (ParserConfigurationException e3) {
            LOG.error("PARSER ERROR: Cannot read guidelines from initialize.xml ({}).", absPathToFile);
            System.exit(-44);
        }

        // Read goal parameters initialization file to configure goals
        Document configDoc = null;
        try {
            configDoc = db.parse(new File(absPathToFile));
            if (configDoc == null) {
                throw new IOException();
            }
        } catch (SAXException | IOException e2) {
            LOG.error("IO ERROR: Cannot read  guidelines from initialize.xml ({}).", absPathToFile);
            System.exit(-45);
        }
        initializeGridConnections(absPathToFile, goalParameterValues);
        initializeAuctionConnections(absPathToFile, goalParameterValues);
        initializeBrokerConnections(absPathToFile, goalParameterValues);


        goalParameterValues.put(MarketGoalParameters.brokerGuidelines, BrokerGuidelinesBuilder.createBrokerGuidelines(configDoc));
        goalParameterValues.put(MarketGoalParameters.auctionGuidelines, AuctionGuidelinesBuilder.createAuctionGuidelines(configDoc));

        initializeHomeGuidelines(goalParameterValues, configDoc);
        initializeNeighborhoodGuidelines(goalParameterValues, configDoc);
        initializeLateralGuidelines(goalParameterValues, configDoc);
        initializeFeederGuidelines(goalParameterValues, configDoc);
        initializeSubstationGuidelines(goalParameterValues, configDoc);
        if (debug) LOG.debug("Before returning, the top goal guidelines are {}.", goalParameterValues);
        return goalParameterValues;
    }

    private static void initializeSubstationGuidelines(HashMap<UniqueIdentifier, Object> goalParameterValues, Document configDoc) {
        LOG.debug("Entering initializeSubstationGuidelines(goalParameterValues={}, configDoc={}. ",goalParameterValues,configDoc);
        NodeList nodeList = configDoc.getElementsByTagName("substationGuidelines");
        if (debug) LOG.debug("There are {} initial substationGuidelines.", nodeList.getLength());
        SubstationGuidelines g =null;
        if (nodeList.getLength() > 0) {

             g = new SubstationGuidelines();
            readElements(nodeList, g);
            goalParameterValues.put(AgentGoalParameters.substationGuidelines, g);

        }
         LOG.info("Exiting initializeSubstationGuidelines: g={}. ", g);
    }

    private static void initializeFeederGuidelines(HashMap<UniqueIdentifier, Object> goalParameterValues, Document configDoc) {
        LOG.debug("Entering initializeFeederGuidelines(goalParameterValues={}, configDoc={}. ",goalParameterValues,configDoc);
        NodeList nodeList = configDoc.getElementsByTagName("feederGuidelines");
        if (debug) LOG.debug("There are {} initial feederGuidelines.", nodeList.getLength());
        FeederGuidelines g = null;
        if (nodeList.getLength() > 0) {
            g = new FeederGuidelines();
            readElements(nodeList, g);
            goalParameterValues.put(AgentGoalParameters.feederGuidelines, g);

        }
        LOG.info("Exiting initializeFeederGuidelines: g={}. ", g);
    }

    private static void initializeLateralGuidelines(HashMap<UniqueIdentifier, Object> goalParameterValues, Document configDoc) {
        LOG.debug("Entering initializeLateralGuidelines(goalParameterValues={}, configDoc={}. ",goalParameterValues,configDoc);
        NodeList nodeList = configDoc.getElementsByTagName("lateralGuidelines");
        if (debug) LOG.debug("There are {} initial lateralGuidelines.", nodeList.getLength());
        LateralGuidelines g = null;
        if (nodeList.getLength() >0) {

            g = new LateralGuidelines();
            readElements(nodeList, g);
            goalParameterValues.put(AgentGoalParameters.lateralGuidelines, g);

        }
        LOG.info("Exiting initializeLateralGuidelines: g={}. ", g);
    }

    private static void initializeNeighborhoodGuidelines(HashMap<UniqueIdentifier, Object> goalParameterValues, Document configDoc) {
        LOG.debug("Entering initializeNeighborhoodGuidelines(goalParameterValues={}, configDoc={}. ",goalParameterValues,configDoc);
        NodeList nodeList;
        nodeList = configDoc.getElementsByTagName("neighborhoodGuidelines");
        if (debug) LOG.debug("There are {} initial neighborhoodGuidelines.", nodeList.getLength());
        NeighborhoodGuidelines g = null;
        if (nodeList.getLength() >0) {
           g = new NeighborhoodGuidelines();
            readElements(nodeList, g);
            goalParameterValues.put(AgentGoalParameters.neighborhoodGuidelines, g);

        }
        LOG.info("Exiting initializeNeighborhoodGuidelines: g={}. ", g);
    }

    private static void initializeHomeGuidelines(HashMap<UniqueIdentifier, Object> goalParameterValues, Document configDoc) {
        LOG.debug("Entering initializeHomeGuidelines(goalParameterValues={}, configDoc={}. ",goalParameterValues,configDoc);
        NodeList nodeList;
        nodeList = configDoc.getElementsByTagName("homeGuidelines");
        HomeGuidelines g = null;
        if (debug) LOG.debug("There are {} initial home guidelines.", nodeList.getLength());
        if (nodeList.getLength() > 0) {
            g = new HomeGuidelines();
            readElements(nodeList, g);
            goalParameterValues.put(AgentGoalParameters.homeGuidelines, g);

        }
        LOG.info("Exiting initializeHomeGuidelines: g={}. ",g);
    }

    private static void readElements(NodeList nodeList, HomeGuidelines g) {
        LOG.info("Entering readElements(class={},nodeList={}",g.getClass().toString(),nodeList);
        for (int i = 0; i < nodeList.getLength(); ++i) {
            Element element = (Element) nodeList.item(i);
            try {
                g.setMinVoltageMultiplier(Double.parseDouble(element.getAttribute("minVoltageMultiplier")));
            } catch (Exception e) {
               if (debug) LOG.info("INFO: The minVoltageMultiplier for this organization could not be read from the XML element. Using default value. ",
                        element);
                //    System.exit(-46);
            }
            try {
                g.setPowerFactor(Double.parseDouble(element.getAttribute("powerFactor")));
            } catch (Exception e) {
                if (debug)  LOG.info("INFO: The powerFactor for this organization could not be read from the XML element. Using default value. ",
                        element);
                //    System.exit(-46);
            }
            try {
                g.setNetDeltaP(Double.parseDouble(element.getAttribute("netDeltaP")));
            } catch (Exception e) {
                if (debug)  LOG.info("INFO: The netDeltaP for this organization could not be read from the XML element. Using default value. ",
                        element);
                //    System.exit(-46);
            }
            try {
                g.setMaxVoltageMultiplier(Double.parseDouble(element.getAttribute("maxVoltageMultiplier")));
            } catch (Exception e) {
                if (debug)  LOG.info("INFO: The maxVoltageMultiplier for this organization could not be read from the XML element. Using default value. ",
                        element);
                //    System.exit(-46);
            }
            try {
                g.setConstantInelasticLoad_kw(Double.parseDouble(element.getAttribute("minKW")));
            } catch (Exception e) {
                if (debug)  LOG.info("INFO: The minKW for this organization could not be read from the XML element. Using default value. ",
                        element);
                //    System.exit(-46);
            }
            try {
                g.setConstantInelasticLoad_fraction(Double.parseDouble(element.getAttribute("maxKW")));
                LOG.info("INFO: The maxKW ={}", element.getAttribute("maxKW"));
            } catch (Exception e) {
                if (debug)  LOG.info("INFO: The maxKW for this organization could not be read from the XML element. Using default value. ",
                        element);
                //    System.exit(-46);
            }
        }
    }

    private static void readElements(NodeList nodeList, LateralGuidelines g) {
        LOG.info("Entering readElements(class={},nodeList={}",g.getClass().toString(),nodeList);

        for (int i = 0; i < nodeList.getLength(); ++i) {
            Element element = (Element) nodeList.item(i);
            try {
                g.setMinVoltageMultiplier(Double.parseDouble(element.getAttribute("minVoltageMultiplier")));
            } catch (Exception e) {
                if (debug)    LOG.info("INFO: The minVoltageMultiplier for this organization could not be read from the XML element. Using default value. ",
                        element);
                //    System.exit(-46);
            }

            try {
                g.setNetDeltaP(Double.parseDouble(element.getAttribute("netDeltaP")));
            } catch (Exception e) {
                if (debug)    LOG.info("INFO: The netDeltaP for this organization could not be read from the XML element. Using default value. ",
                        element);
                //    System.exit(-46);
            }
            try {
                g.setMaxVoltageMultiplier(Double.parseDouble(element.getAttribute("maxVoltageMultiplier")));
            } catch (Exception e) {
                if (debug)     LOG.info("INFO: The maxVoltageMultiplier for this organization could not be read from the XML element. Using default value. ",
                        element);
                //    System.exit(-46);
            }
            try {
                g.setMinKW(Double.parseDouble(element.getAttribute("minKW")));
            } catch (Exception e) {
                if (debug)      LOG.info("INFO: The minKW for this organization could not be read from the XML element. Using default value. ",
                        element);
                //    System.exit(-46);
            }
            try {
                g.setMaxKW(Double.parseDouble(element.getAttribute("maxKW")));
            } catch (Exception e) {
                if (debug)     LOG.info("INFO: The maxKW for this organization could not be read from the XML element. Using default value. ",
                        element);
                //    System.exit(-46);
            }
        }
    }

    private static void readElements(NodeList nodeList, NeighborhoodGuidelines g) {
        LOG.info("Entering readElements(class={},nodeList={}",g.getClass().toString(),nodeList);

        for (int i = 0; i < nodeList.getLength(); ++i) {
            Element element = (Element) nodeList.item(i);
            try {
                g.setMinVoltageMultiplier(Double.parseDouble(element.getAttribute("minVoltageMultiplier")));
            } catch (Exception e) {
                if (debug)       LOG.info("INFO: The minVoltageMultiplier for this organization could not be read from the XML element. Using default value. ",
                        element);
                //    System.exit(-46);
            }
            try {
                g.setNetDeltaP(Double.parseDouble(element.getAttribute("netDeltaP")));
            } catch (Exception e) {
                if (debug)      LOG.info("INFO: The netDeltaP for this organization could not be read from the XML element. Using default value. ",
                        element);
                //    System.exit(-46);
            }
            try {
                g.setMaxVoltageMultiplier(Double.parseDouble(element.getAttribute("maxVoltageMultiplier")));
            } catch (Exception e) {
                if (debug)      LOG.info("INFO: The maxVoltageMultiplier for this organization could not be read from the XML element. Using default value. ",
                        element);
                //    System.exit(-46);
            }
            try {
                g.setMinKW(Double.parseDouble(element.getAttribute("minKW")));
            } catch (Exception e) {
                if (debug)        LOG.info("INFO: The minKW for this organization could not be read from the XML element. Using default value. ",
                        element);
                //    System.exit(-46);
            }
            try {
                g.setMaxKW(Double.parseDouble(element.getAttribute("maxKW")));
            } catch (Exception e) {
                if (debug)         LOG.info("INFO: The maxKW for this organization could not be read from the XML element. Using default value. ",
                        element);
                //    System.exit(-46);
            }
        }
    }

    private static void readElements(NodeList nodeList, FeederGuidelines g) {
        LOG.info("Entering readElements(class={},nodeList={}",g.getClass().toString(),nodeList);

        for (int i = 0; i < nodeList.getLength(); ++i) {
            Element element = (Element) nodeList.item(i);
            try {
                g.setMinVoltageMultiplier(Double.parseDouble(element.getAttribute("minVoltageMultiplier")));
            } catch (Exception e) {
                if (debug)      LOG.info("INFO: The minVoltageMultiplier for this organization could not be read from the XML element. Using default value. ",
                        element);
                //    System.exit(-46);
            }
            try {
                g.setNetDeltaP(Double.parseDouble(element.getAttribute("netDeltaP")));
            } catch (Exception e) {
                if (debug)      LOG.info("INFO: The netDeltaP for this organization could not be read from the XML element. Using default value. ",
                        element);
                //    System.exit(-46);
            }
            try {
                g.setMaxVoltageMultiplier(Double.parseDouble(element.getAttribute("maxVoltageMultiplier")));
            } catch (Exception e) {
                if (debug)    LOG.info("INFO: The maxVoltageMultiplier for this organization could not be read from the XML element. Using default value. ",
                        element);
                //    System.exit(-46);
            }
            try {
                g.setMinKW(Double.parseDouble(element.getAttribute("minKW")));
            } catch (Exception e) {
                if (debug)     LOG.info("INFO: The minKW for this organization could not be read from the XML element. Using default value. ",
                        element);
                //    System.exit(-46);
            }
            try {
                g.setMaxKW(Double.parseDouble(element.getAttribute("maxKW")));
            } catch (Exception e) {
                if (debug)       LOG.info("INFO: The maxKW for this organization could not be read from the XML element. Using default value. ",
                        element);
                //    System.exit(-46);
            }
        }
    }

    private static void readElements(NodeList nodeList, SubstationGuidelines g) {
        LOG.info("Entering readElements(class={},nodeList={}",g.getClass().toString(),nodeList);

        for (int i = 0; i < nodeList.getLength(); ++i) {
            Element element = (Element) nodeList.item(i);
            try {
                g.setMinVoltageMultiplier(Double.parseDouble(element.getAttribute("minVoltageMultiplier")));
            } catch (Exception e) {
                if (debug)         LOG.info("INFO: The minVoltageMultiplier for this organization could not be read from the XML element. Using default value. ",
                        element);
                //    System.exit(-46);
            }
            try {
                g.setNetDeltaP(Double.parseDouble(element.getAttribute("netDeltaP")));
            } catch (Exception e) {
                if (debug)        LOG.info("INFO: The netDeltaP for this organization could not be read from the XML element. Using default value. ",
                        element);
                //    System.exit(-46);
            }
            try {
                g.setMaxVoltageMultiplier(Double.parseDouble(element.getAttribute("maxVoltageMultiplier")));
            } catch (Exception e) {
                if (debug)          LOG.info("INFO: The maxVoltageMultiplier for this organization could not be read from the XML element. Using default value. ",
                        element);
                //    System.exit(-46);
            }
            try {
                g.setMinKW(Double.parseDouble(element.getAttribute("minKW")));
            } catch (Exception e) {
                if (debug)         LOG.info("INFO: The minKW for this organization could not be read from the XML element. Using default value. ",
                        element);
                //    System.exit(-46);
            }
            try {
                g.setMaxKW(Double.parseDouble(element.getAttribute("maxKW")));
            } catch (Exception e) {
                if (debug)      LOG.info("INFO: The maxKW for this organization could not be read from the XML element. Using default value. ",
                        element);
                //    System.exit(-46);
            }
        }
    }

    private static void initializeGridConnections(String absPathToFile, HashMap<UniqueIdentifier, Object> goalParameterValues) {
        LOG.debug("Entering initializeGridConnections(absPathToFile={},goalParameterValues={}",absPathToFile,goalParameterValues);
        IConnections c = Connections.createConnections(absPathToFile, "gridConnections");
        if (!c.getListConnectionGuidelines().isEmpty()) {
            goalParameterValues.put(GridGoalParameters.gridConnections, c);
        }
    }

    private static void initializeAuctionConnections(String absPathToFile, HashMap<UniqueIdentifier, Object> goalParameterValues) {
        LOG.debug("Entering initializeAuctionConnections(absPathToFile={},goalParameterValues={}",absPathToFile,goalParameterValues);
        IConnections c = Connections.createConnections(absPathToFile, "auctionConnections");
        if (!c.getListConnectionGuidelines().isEmpty()) {
            goalParameterValues.put(MarketGoalParameters.auctionConnections, c);
        }
    }
    private static void initializeBrokerConnections(String absPathToFile, HashMap<UniqueIdentifier, Object> goalParameterValues) {
        LOG.debug("Entering initializeBrokerConnections(absPathToFile={},goalParameterValues={}",absPathToFile,goalParameterValues);
        IConnections c = Connections.createConnections(absPathToFile, "brokerConnections");
        if (!c.getListConnectionGuidelines().isEmpty()) {
            goalParameterValues.put(MarketGoalParameters.brokerConnections, c);
        }
    }


}
