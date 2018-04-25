/**
 * Copyright 2012 
 * Kansas State University MACR Laboratory http://macr.cis.ksu.edu/
 * Department of Computing & Information Sciences
 *
 * See License.txt file for the license agreement. 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package edu.ksu.cis.macr.ipds.primary.guidelines;

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
import java.io.Serializable;
import java.util.Objects;

public class FeederGuidelines implements Serializable, IFeederGuidelines {
  private static final Logger LOG = LoggerFactory.getLogger(FeederGuidelines.class);
  private static final boolean debug = false;
  private static final long serialVersionUID = 1L;
  private String specificationFilePath;
  private double maxVoltageMultiplier;
  private double minVoltageMultiplier;
  private double netDeltaP;
  private double minKW;
  private double maxKW;

  /**
   Construct new {@code}HomeGuidelines}.

   @param minVoltageMultiplier - the minimum voltage multiplier for the local organization, e.g. 0.95.
   @param maxVoltageMultiplier - the maximum voltage multiplier for the local organization, e.g. 1.05.
   */
  public FeederGuidelines(double minVoltageMultiplier, double maxVoltageMultiplier) {
    this.setMinVoltageMultiplier(minVoltageMultiplier);
    this.setMaxVoltageMultiplier(maxVoltageMultiplier);
    if (debug) LOG.debug("New guidelines created. {}", this.toString());
  }

  /**
   Construct new new {@code}HomeGuidelines} with default values. minVoltageMultiplier = 0.95. maxVoltageMultiplier =
   1.05.
   */
  public FeederGuidelines() {
    minVoltageMultiplier = 0.95;
    maxVoltageMultiplier = 1.05;
    if (debug) LOG.info("New guidelines created. {}", this.toString());
  }

  /**
   Top-level goal guidelines must have constructor with absolute path to the content.

   @param absPathToFile - the absolute path and file of the Initialize.xml file.
   */
  public FeederGuidelines(String absPathToFile) {
    DocumentBuilder db = null;
    try {
      db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      if (db == null) {
        throw new ParserConfigurationException();
      }
    } catch (ParserConfigurationException e3) {
      LOG.error("PARSER ERROR: Cannot read guidelines from initialize.xml ({}).",
              absPathToFile);
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
      LOG.error("IO ERROR: Cannot read  guidelines from initialize.xml ({}).",
              absPathToFile);
      System.exit(-45);
    }

    NodeList nodeList = configDoc.getElementsByTagName("feederGuidelines");
    LOG.debug("There are {} initial guidelines.", nodeList.getLength());
    for (int i = 0; i < nodeList.getLength(); ++i) {
      Element element = (Element) nodeList.item(i);
      IFeederGuidelines g;

      // attributes........................
    }
    LOG.debug("FeederGuidelines are read from Initialize.xml.");
  }

  /**
   Construct new power quality guidelines.

   @param value - a given power quality guideline object.
   */
  public FeederGuidelines(IFeederGuidelines value) {
    Objects.requireNonNull(value);
    this.setMinVoltageMultiplier(value.getMinVoltageMultiplier());
    this.setMaxVoltageMultiplier(value.getMaxVoltageMultiplier());
    LOG.debug("creating: {}", this.toString());
    minVoltageMultiplier = 0.0;
    maxVoltageMultiplier = 0.0;
  }

  @Override
  public double getMaxKW() {
    return this.maxKW;
  }

  @Override
  public synchronized void setMaxKW(double kw) {
    this.maxKW = kw;
  }

  @Override
  public double getMaxVoltageMultiplier() {
    return this.maxVoltageMultiplier;
  }

  @Override
  public synchronized void setMaxVoltageMultiplier(double maxVoltageMultiplier) {
    this.maxVoltageMultiplier = maxVoltageMultiplier;
  }

  @Override
  public double getMinKW() {
    return this.minKW;
  }

  @Override
  public synchronized void setMinKW(double kw) {
    this.minKW = kw;
  }

  @Override
  public double getMinVoltageMultiplier() {
    return this.minVoltageMultiplier;
  }

  @Override
  public synchronized void setMinVoltageMultiplier(double minVoltageMultiplier) {
    this.minVoltageMultiplier = minVoltageMultiplier;
  }

  @Override
  public double getNetDeltaP() {
    return netDeltaP;
  }

  @Override
  public synchronized void setNetDeltaP(double netDeltaP) {
    this.netDeltaP = netDeltaP;
  }

  /**
   @return the specificationFilePath
   */
  @Override
  public String getSpecificationFilePath() {
    return specificationFilePath;
  }

  /**
   @param specificationFilePath the specificationFilePath to set
   */
  @Override
  public synchronized void setSpecificationFilePath(String specificationFilePath) {
    this.specificationFilePath = specificationFilePath;
  }

  @Override
  public String toString() {
    return String.format("FeederGuidelines{maxVoltageMultiplier=%s, minVoltageMultiplier=%s}", maxVoltageMultiplier, minVoltageMultiplier);
  }


}
