/**
 * XmlReader.java
 *
 * Copyright 2012 Denise Case Kansas State University MACR Laboratory
 * http://macr.cis.ksu.edu/ Department of Computing & Information Sciences
 *
 * See License.txt file for the license agreement.
 *
 */
package edu.ksu.cis.macr.ipds.node.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 The {@code XmlReader}
 */
public class XmlReader {

  private static final String IPDS_SIM_DIR = System.getProperty("user.dir");
  private static final String SCENARIO02_DIR = IPDS_SIM_DIR
          + "/src/main/resources/configs/scenario02/";
  // Change this file to a different simulation to generate files for that
  // simulation
  private static final File SCENARIO = new File(SCENARIO02_DIR);
  private static final File[] fileList = SCENARIO.listFiles();
  private static final Logger LOG = LoggerFactory.getLogger(XmlReader.class);
  private DocumentBuilderFactory docBuilderFactory;
  private DocumentBuilder docBuilder;

  /*
   * Constructor for the XML reader class - Initializes the docBuilderFactory
   * and docBuilder which are necessary for reading in the xml files
   */
  public XmlReader() {
    try {
      docBuilderFactory = DocumentBuilderFactory.newInstance();
      docBuilder = docBuilderFactory.newDocumentBuilder();
    } catch (Exception e) {
      LOG.error("Error getting docBuilder.");
    }
  }

  public static Document getDocumentFromXml(File xmlFile) {
    Document doc = null;
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      doc = db.parse(xmlFile);
      doc.getDocumentElement().normalize();
    } catch (ParserConfigurationException | SAXException | IOException e) {
      LOG.error("Error getting docBuilder.");
    }
    return doc;
  }

  public static List<String> getElementValues(File xmlFile, String parentTag, String elementTag) {
    List<String> toReturn = new ArrayList<>();
    try {
      Document doc = getDocumentFromXml(xmlFile);

      // Get all nodes under the parent node
      NodeList nodes = doc.getElementsByTagName(parentTag);
      for (int i = 0; i < nodes.getLength(); i++) {
        Node currentNode = nodes.item(i);
        if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
          Element currentElement = (Element) currentNode;
          NodeList nameNodeList = currentElement.getElementsByTagName(elementTag);
          for (int j = 0; j < nameNodeList.getLength(); j++) {
            NodeList names = nameNodeList.item(j).getChildNodes();
            toReturn.add(names.item(0).getNodeValue());
          }
        }
      }
    } catch (NullPointerException e) {
      /**
       * A NPE will be thrown if the element tag does not exist. Just
       * return an empty list in this case.
       */
    } catch (Exception e) {

      LOG.error("Error getting docBuilder.");
    }
    return toReturn;
  }

  /*
   * Looks in the current simulation directory to find all of the .goal files
   * and returns them in a List.
   */
  public List<File> FindGoalModels() {
    List<File> goalModels = new ArrayList<>();
    for (File file : fileList) {
      if (file.toString().contains(".goal")) {
        goalModels.add(file);
      }
    }
    return goalModels;
  }

  /*
   * Creates a list of all of the goal names that need files generated for
   * them from a List of all the .goal files in the current simulation
   * directory.
   */
  public List<String> ReadGoalModels(List<File> goalFiles) {
    List<String> goalNameList = new ArrayList<>();
    try {
      for (File file : goalFiles) {
        ReadGoals(file, goalNameList);
      }
    } catch (SAXException | IOException e) {
      LOG.error("Error reading goal models: {}", goalFiles);
    }
    return goalNameList;
  }

  /*
   * Reads all of the goal names from a single file and adds them to the goal
   * name list.
   */
  public List<String> ReadGoals(File goalFile, List<String> goalNameList)
          throws SAXException, IOException {

    Document doc = docBuilder.parse(goalFile);
    NodeList nodeList = doc.getElementsByTagName("Goal");

    for (int i = 0; i < nodeList.getLength(); i++) {
      Element goalElement = (Element) nodeList.item(i);
      goalNameList.add(goalElement.getAttribute("name"));
    }
    return goalNameList;
  }
}
