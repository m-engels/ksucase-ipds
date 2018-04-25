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
package edu.ksu.cis.macr.ipds.self.guidelines;

import edu.ksu.cis.macr.aasis.common.IConnectionGuidelines;
import edu.ksu.cis.macr.aasis.common.IConnections;
import edu.ksu.cis.macr.ipds.config.GridHolonicLevel;
import edu.ksu.cis.macr.ipds.config.MarketHolonicLevel;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Connections implements IConnections {

  private static final Logger LOG = LoggerFactory.getLogger(Connections.class);
  private static final boolean debug = false;
  private static final long serialVersionUID = 1L;
  private List<ISelfConnectionGuidelines> listConnectionGuidelines;
  private String tagName = "gridConnections"; // as an example

  public Connections() {
  }

  public Connections(Iterable<ISelfConnectionGuidelines> listConnectionGuidelines, final String tagName) {
    if (debug) LOG.debug("New connections tagName={}", tagName);
    this.tagName = tagName;
    this.listConnectionGuidelines = new ArrayList<>();
    for (final ISelfConnectionGuidelines p : listConnectionGuidelines) {
      this.listConnectionGuidelines.add(p);
    }
  }

  /**
   Construct all connections given the absolute path and file name of the XML file with the content.

   @param absolutePathToInitializeFile - the absolute path and file of the Initialize.xml file.

   */
  private Connections(String absolutePathToInitializeFile, final String tagName) {
    if (debug) LOG.debug("New connections tagName={}", tagName);
    this.tagName = tagName;
    this.listConnectionGuidelines = new ArrayList<>();

    DocumentBuilder db = null;
    try {
      db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      if (db == null) {
        throw new ParserConfigurationException();
      }
    } catch (ParserConfigurationException e3) {
      LOG.error("PARSER ERROR: Cannot read combined guidelines from initialize.xml ({}).",
              absolutePathToInitializeFile);
      System.exit(-44);
    }

    // Read goal parameters initialization file to configure goals

    Document configDoc = null;
    try {
      configDoc = db.parse(new File(absolutePathToInitializeFile));
      if (configDoc == null) {
        throw new IOException();
      }
    } catch (SAXException | IOException e2) {
      LOG.error("IO ERROR: Cannot read Connections from initialize.xml ({}).",
              absolutePathToInitializeFile);
      System.exit(-45);
    }

    if (debug) LOG.debug("Getting XML for tagName={}", tagName);
    NodeList nodeList = configDoc.getElementsByTagName(this.tagName);
    if (nodeList != null) {
        if(debug) LOG.info("There are {} {}.", nodeList.getLength(), this.tagName);
    }

    for (int i = 0; i < nodeList.getLength(); ++i) {

      Element element = (Element) nodeList.item(i);
      ISelfConnectionGuidelines Connection;
      String to = element.getAttribute("to");
      String in = element.getAttribute("in");
      String expMaster = element.getAttribute("expectedmaster");
        String orgLevel;

        if (tagName.equals("gridConnections")) {
            orgLevel = GridHolonicLevel.getOrganizationType(expMaster).toString();
        }
        else {
            orgLevel = MarketHolonicLevel.getOrganizationType(expMaster).toString();
        }

      //see if there's an organization specification included for this org

      NodeList orgNodes = configDoc.getDocumentElement().getElementsByTagName(in);
      int len = orgNodes.getLength();
      if (len > 0) {

        // read in initial guidelines........

        Element orgElement = (Element) configDoc.getDocumentElement().getElementsByTagName(in).item(0);
        // should only be one entry for this org
        double kw = this.getCombinedKW(orgElement);
        String specpath = this.getSpecPath(orgElement);
        String orgModelFolder = this.getOrgModelFolder(orgElement);
        Connection = new SelfConnectionGuidelines(to, in, orgLevel, expMaster, kw, specpath, orgModelFolder);
       LOG.info("Adding initial ConnectionGuidelines: to {} in {} for {} under {} at {} defined in {}. Models under folder {}.", to, in, orgLevel, expMaster, kw, specpath, orgModelFolder);

        // optional: read in agent biases.............


      }
      else {  // there's no organization information to read - just set up a participant connection

        Connection = new SelfConnectionGuidelines(to, in, orgLevel, expMaster);
        LOG.debug("Adding participant ConnectionGuidelines: to {} in {} for {} under {} with no standard model folder provided.",
                to, in, orgLevel, expMaster);

      }

      this.listConnectionGuidelines.add(Connection);
      if (debug) LOG.debug("{} connection guidelines have been added.", this.listConnectionGuidelines.size());
    }

  }


  public synchronized static Connections createConnections(String absolutePathToInitializeFile, final String tagName) {
    return new Connections(absolutePathToInitializeFile, tagName);
  }

  /**
   Gets the combined KW maximum net power for the organization expected from the XML element.

   @param element - the xml Element from which to read
   @return double - the combined KW maximum net power for the organization. Hard exit if it cannot be parsed.
   */
  public double getCombinedKW(Element element) {
    double kw = 0.0;
    try {
      kw = Double.parseDouble(element.getAttribute("combinedkw"));
    } catch (Exception e) {
      LOG.error("ERROR: The combined KW for this organization could not be read from the XML element ({}).",
              element);
      //    System.exit(-46);
    }
    return kw;
  }

  @Override
  public synchronized List<? extends IConnectionGuidelines> getListConnectionGuidelines() {
    return this.listConnectionGuidelines;
  }




    public synchronized String getTagName() {
    return tagName;
  }

  @Override
  public synchronized void setListConnectionGuidelines(final List<? extends IConnectionGuidelines> listConnectionGuidelines) {
    this.listConnectionGuidelines.clear();
    this.listConnectionGuidelines.addAll(this.listConnectionGuidelines.stream().collect(Collectors.toList()));
      if (debug)LOG.debug("Setting the list of all market connections. {} entries.", this.listConnectionGuidelines.size());
  }

  /**
   Gets the relative specification path (from the test case folder) provided in the XML element.

   @param element - the xml Element from which to read
   @return - the String with the relative specification path, e.g. N43/orgN43
   */
  public String getSpecPath(Element element) {
    String specpath = "";
    try {
      specpath = element.getAttribute("specpath");
    } catch (Exception e) {
      LOG.error("Could not determine the relative path given Element {}.", element);
      System.exit(-47);
    }
    return specpath;
  }

  /**
   Gets the folder containg the default goal and role models for this organization (under the standard models directry) provided in the XML element.

   @param element - the xml Element from which to read
   @return - the String with the relative specification path, e.g. N43/orgN43
   */
  public String getOrgModelFolder(Element element) {
    String orgModelFolder = "";
    try {
      orgModelFolder = element.getAttribute("orgModelFolder");
    } catch (Exception e) {
      LOG.error("Could not determine the orgModelFolder given Element {}.", element);
      System.exit(-47);
    }
    return orgModelFolder;
  }

  public synchronized void setTagName(String tagName) {
    this.tagName = tagName;
  }

  @Override
  public String toString() {
    return "Connections{" +
            "listConnectionGuidelines=" + listConnectionGuidelines +
            '}';
  }


}
