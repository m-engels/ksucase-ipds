package edu.ksu.cis.macr.ipds.primary.guidelines;

import edu.ksu.cis.macr.ipds.primary.goals.AgentGoalParameters;
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
 The {@code GuidelineManager} reads goal guidelines (the top goal parameters) from a given XML
 file.
 */
public enum PowerGuidelineManager {

    INSTANCE;
    private static final Logger LOG = LoggerFactory.getLogger(PowerGuidelineManager.class);
    private static final boolean debug = false;
    private static final String CUR_DIR = System.getProperty("user.dir");

  public synchronized static Map<UniqueIdentifier, Object> getGuidelines(String absPathToFile) {
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

      intializeHomeGuidelines(goalParameterValues, configDoc);
      intializeNeighborhoodGuidelines(goalParameterValues, configDoc);
      initializeLateralGuidelines(goalParameterValues, configDoc);
      initializeFeederGuidelines(goalParameterValues, configDoc);
      initializeSubstationGuidelines(goalParameterValues, configDoc);
      if (debug) LOG.debug("Before returning, the top goal guidelines are {}.", goalParameterValues);
      return goalParameterValues;
  }

    private static void initializeSubstationGuidelines(HashMap<UniqueIdentifier, Object> goalParameterValues, Document configDoc) {
        NodeList nodeList = configDoc.getElementsByTagName("substationGuidelines");
        if (debug) LOG.debug("There are {} initial substationGuidelines.", nodeList.getLength());
        if (nodeList.getLength() == 0) {
            goalParameterValues.put(AgentGoalParameters.substationGuidelines, null);
        } else {
            SubstationGuidelines g = new SubstationGuidelines();
            readElements(nodeList, g);
            goalParameterValues.put(AgentGoalParameters.substationGuidelines, g);
            if (debug)
                LOG.debug("{} read from Initialize.xml: {}.", AgentGoalParameters.substationGuidelines, g.toString());
        }
    }

    private static void initializeFeederGuidelines(HashMap<UniqueIdentifier, Object> goalParameterValues, Document configDoc) {
        NodeList nodeList = configDoc.getElementsByTagName("feederGuidelines");
        if (debug) LOG.debug("There are {} initial feederGuidelines.", nodeList.getLength());
        if (nodeList.getLength() == 0) {
            goalParameterValues.put(AgentGoalParameters.feederGuidelines, null);
        } else {
            FeederGuidelines g = new FeederGuidelines();
            readElements(nodeList, g);
            goalParameterValues.put(AgentGoalParameters.feederGuidelines, g);
            if (debug) LOG.debug("{} read from Initialize.xml: {}.", AgentGoalParameters.feederGuidelines, g.toString());
        }
    }

    private static void initializeLateralGuidelines(HashMap<UniqueIdentifier, Object> goalParameterValues, Document configDoc) {
        NodeList nodeList = configDoc.getElementsByTagName("lateralGuidelines");
        if (debug) LOG.debug("There are {} initial lateralGuidelines.", nodeList.getLength());
        if (nodeList.getLength() == 0) {
            goalParameterValues.put(AgentGoalParameters.lateralGuidelines, null);
        } else {
            LateralGuidelines g = new LateralGuidelines();
            readElements(nodeList, g);
            goalParameterValues.put(AgentGoalParameters.lateralGuidelines, g);
            if (debug)
                LOG.debug("{} read from Initialize.xml: {}.", AgentGoalParameters.lateralGuidelines, g.toString());
        }
    }

    private static void intializeNeighborhoodGuidelines(HashMap<UniqueIdentifier, Object> goalParameterValues, Document configDoc) {
        NodeList nodeList;
        nodeList = configDoc.getElementsByTagName("neighborhoodGuidelines");
        if (debug) LOG.debug("There are {} initial neighborhoodGuidelines.", nodeList.getLength());
        if (nodeList.getLength() == 0) {
            goalParameterValues.put(AgentGoalParameters.neighborhoodGuidelines, null);
        } else {
            NeighborhoodGuidelines g = new NeighborhoodGuidelines();
            readElements(nodeList, g);
            goalParameterValues.put(AgentGoalParameters.neighborhoodGuidelines, g);
            if (debug)
                LOG.debug("{} read from Initialize.xml: {}.", AgentGoalParameters.neighborhoodGuidelines, g.toString());
        }
    }

    private static void intializeHomeGuidelines(HashMap<UniqueIdentifier, Object> goalParameterValues, Document configDoc) {
        NodeList nodeList;
        nodeList = configDoc.getElementsByTagName("homeGuidelines");
        if (debug) LOG.debug("There are {} initial home guidelines.", nodeList.getLength());
        if (nodeList.getLength() == 0) goalParameterValues.put(AgentGoalParameters.homeGuidelines, null);
        else {
            HomeGuidelines g = new HomeGuidelines();
            readElements(nodeList, g);
            goalParameterValues.put(AgentGoalParameters.homeGuidelines, g);
            if (debug) LOG.debug("{} read from Initialize.xml: {}.", AgentGoalParameters.homeGuidelines, g.toString());
        }
    }

    private static void readElements(NodeList nodeList, HomeGuidelines g) {
    for (int i = 0; i < nodeList.getLength(); ++i) {
      Element element = (Element) nodeList.item(i);
      try {
        g.setMinVoltageMultiplier(Double.parseDouble(element.getAttribute("minVoltageMultiplier")));
      } catch (Exception e) {
        LOG.info("INFO: The minVoltageMultiplier for this organization could not be read from the XML element. Using default value. ",
                element);
        //    System.exit(-46);
      }
      try {
        g.setPowerFactor(Double.parseDouble(element.getAttribute("powerFactor")));
      } catch (Exception e) {
        LOG.info("INFO: The powerFactor for this organization could not be read from the XML element. Using default value. ",
                element);
        //    System.exit(-46);
      }
      try {
        g.setNetDeltaP(Double.parseDouble(element.getAttribute("netDeltaP")));
      } catch (Exception e) {
        LOG.info("INFO: The netDeltaP for this organization could not be read from the XML element. Using default value. ",
                element);
        //    System.exit(-46);
      }
      try {
        g.setMaxVoltageMultiplier(Double.parseDouble(element.getAttribute("maxVoltageMultiplier")));
      } catch (Exception e) {
        LOG.info("INFO: The maxVoltageMultiplier for this organization could not be read from the XML element. Using default value. ",
                element);
        //    System.exit(-46);
      }
      try {
        g.setConstantInelasticLoad_kw(Double.parseDouble(element.getAttribute("minKW")));
      } catch (Exception e) {
        LOG.info("INFO: The minKW for this organization could not be read from the XML element. Using default value. ",
                element);
        //    System.exit(-46);
      }
      try {
        g.setConstantInelasticLoad_fraction(Double.parseDouble(element.getAttribute("maxKW")));
      } catch (Exception e) {
        LOG.info("INFO: The maxKW for this organization could not be read from the XML element. Using default value. ",
                element);
        //    System.exit(-46);
      }

    }

  }

  private static void readElements(NodeList nodeList, LateralGuidelines g) {
    for (int i = 0; i < nodeList.getLength(); ++i) {
      Element element = (Element) nodeList.item(i);
      try {
        g.setMinVoltageMultiplier(Double.parseDouble(element.getAttribute("minVoltageMultiplier")));
      } catch (Exception e) {
        LOG.info("INFO: The minVoltageMultiplier for this organization could not be read from the XML element. Using default value. ",
                element);
        //    System.exit(-46);
      }

      try {
        g.setNetDeltaP(Double.parseDouble(element.getAttribute("netDeltaP")));
      } catch (Exception e) {
        LOG.info("INFO: The netDeltaP for this organization could not be read from the XML element. Using default value. ",
                element);
        //    System.exit(-46);
      }
      try {
        g.setMaxVoltageMultiplier(Double.parseDouble(element.getAttribute("maxVoltageMultiplier")));
      } catch (Exception e) {
        LOG.info("INFO: The maxVoltageMultiplier for this organization could not be read from the XML element. Using default value. ",
                element);
        //    System.exit(-46);
      }
      try {
        g.setMinKW(Double.parseDouble(element.getAttribute("minKW")));
      } catch (Exception e) {
        LOG.info("INFO: The minKW for this organization could not be read from the XML element. Using default value. ",
                element);
        //    System.exit(-46);
      }
      try {
        g.setMaxKW(Double.parseDouble(element.getAttribute("maxKW")));
      } catch (Exception e) {
        LOG.info("INFO: The maxKW for this organization could not be read from the XML element. Using default value. ",
                element);
        //    System.exit(-46);
      }

    }

  }

  private static void readElements(NodeList nodeList, NeighborhoodGuidelines g) {
    for (int i = 0; i < nodeList.getLength(); ++i) {
      Element element = (Element) nodeList.item(i);
      try {
        g.setMinVoltageMultiplier(Double.parseDouble(element.getAttribute("minVoltageMultiplier")));
      } catch (Exception e) {
        LOG.info("INFO: The minVoltageMultiplier for this organization could not be read from the XML element. Using default value. ",
                element);
        //    System.exit(-46);
      }

      try {
        g.setNetDeltaP(Double.parseDouble(element.getAttribute("netDeltaP")));
      } catch (Exception e) {
        LOG.info("INFO: The netDeltaP for this organization could not be read from the XML element. Using default value. ",
                element);
        //    System.exit(-46);
      }
      try {
        g.setMaxVoltageMultiplier(Double.parseDouble(element.getAttribute("maxVoltageMultiplier")));
      } catch (Exception e) {
        LOG.info("INFO: The maxVoltageMultiplier for this organization could not be read from the XML element. Using default value. ",
                element);
        //    System.exit(-46);
      }
      try {
        g.setMinKW(Double.parseDouble(element.getAttribute("minKW")));
      } catch (Exception e) {
        LOG.info("INFO: The minKW for this organization could not be read from the XML element. Using default value. ",
                element);
        //    System.exit(-46);
      }
      try {
        g.setMaxKW(Double.parseDouble(element.getAttribute("maxKW")));
      } catch (Exception e) {
        LOG.info("INFO: The maxKW for this organization could not be read from the XML element. Using default value. ",
                element);
        //    System.exit(-46);
      }

    }

  }

  private static void readElements(NodeList nodeList, FeederGuidelines g) {
    for (int i = 0; i < nodeList.getLength(); ++i) {
      Element element = (Element) nodeList.item(i);
      try {
        g.setMinVoltageMultiplier(Double.parseDouble(element.getAttribute("minVoltageMultiplier")));
      } catch (Exception e) {
        LOG.info("INFO: The minVoltageMultiplier for this organization could not be read from the XML element. Using default value. ",
                element);
        //    System.exit(-46);
      }
      try {
        g.setNetDeltaP(Double.parseDouble(element.getAttribute("netDeltaP")));
      } catch (Exception e) {
        LOG.info("INFO: The netDeltaP for this organization could not be read from the XML element. Using default value. ",
                element);
        //    System.exit(-46);
      }
      try {
        g.setMaxVoltageMultiplier(Double.parseDouble(element.getAttribute("maxVoltageMultiplier")));
      } catch (Exception e) {
        LOG.info("INFO: The maxVoltageMultiplier for this organization could not be read from the XML element. Using default value. ",
                element);
        //    System.exit(-46);
      }
      try {
        g.setMinKW(Double.parseDouble(element.getAttribute("minKW")));
      } catch (Exception e) {
        LOG.info("INFO: The minKW for this organization could not be read from the XML element. Using default value. ",
                element);
        //    System.exit(-46);
      }
      try {
        g.setMaxKW(Double.parseDouble(element.getAttribute("maxKW")));
      } catch (Exception e) {
        LOG.info("INFO: The maxKW for this organization could not be read from the XML element. Using default value. ",
                element);
        //    System.exit(-46);
      }

    }

  }

  private static void readElements(NodeList nodeList, SubstationGuidelines g) {
    for (int i = 0; i < nodeList.getLength(); ++i) {
      Element element = (Element) nodeList.item(i);
      try {
        g.setMinVoltageMultiplier(Double.parseDouble(element.getAttribute("minVoltageMultiplier")));
      } catch (Exception e) {
        LOG.info("INFO: The minVoltageMultiplier for this organization could not be read from the XML element. Using default value. ",
                element);
        //    System.exit(-46);
      }
      try {
        g.setNetDeltaP(Double.parseDouble(element.getAttribute("netDeltaP")));
      } catch (Exception e) {
        LOG.info("INFO: The netDeltaP for this organization could not be read from the XML element. Using default value. ",
                element);
        //    System.exit(-46);
      }
      try {
        g.setMaxVoltageMultiplier(Double.parseDouble(element.getAttribute("maxVoltageMultiplier")));
      } catch (Exception e) {
        LOG.info("INFO: The maxVoltageMultiplier for this organization could not be read from the XML element. Using default value. ",
                element);
        //    System.exit(-46);
      }
      try {
        g.setMinKW(Double.parseDouble(element.getAttribute("minKW")));
      } catch (Exception e) {
        LOG.info("INFO: The minKW for this organization could not be read from the XML element. Using default value. ",
                element);
        //    System.exit(-46);
      }
      try {
        g.setMaxKW(Double.parseDouble(element.getAttribute("maxKW")));
      } catch (Exception e) {
        LOG.info("INFO: The maxKW for this organization could not be read from the XML element. Using default value. ",
                element);
        //    System.exit(-46);
      }

    }

  }


}
