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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class SelfConnectionGuidelines implements Serializable, ISelfConnectionGuidelines {
  private static final Logger LOG = LoggerFactory.getLogger(SelfConnectionGuidelines.class);
  private static final long serialVersionUID = 1L;
  private String otherAgentAbbrev;
  private String organizationAbbrev;
  private String organizationLevel;
  private String expectedMasterAbbrev;
  private String specificationFilePath;
  private double combinedKW;
  private String orgModelFolder;

  public String getOrgModelFolder() {
    return this.orgModelFolder;
  }

  private boolean isConnected = false;
  private boolean isRegistered = false;

  private SelfConnectionGuidelines() {
    otherAgentAbbrev = "";
    organizationAbbrev = "";
    organizationLevel = "";
    expectedMasterAbbrev = "";
    specificationFilePath = "";
    combinedKW = 0.0;
    this.orgModelFolder = "";
  }

  public SelfConnectionGuidelines(final String otherAgentAbbrev, final String organizationAbbrev,
                                  final String organizationLevel, final String expectedMasterAbbrev, final double combinedKW,
                                  final String specificationFilePath, final String orgModelFolder) {
      this.otherAgentAbbrev = otherAgentAbbrev;
      this.organizationAbbrev = organizationAbbrev;
      this.organizationLevel = organizationLevel;
      this.expectedMasterAbbrev = expectedMasterAbbrev;
      this.combinedKW = combinedKW;
      this.specificationFilePath = specificationFilePath;
      this.orgModelFolder = orgModelFolder;
   }

  public SelfConnectionGuidelines(final String otherAgentAbbrev, final String organizationAbbrev,
                                  final String organizationLevel, final String expectedMasterAbbrev) {
      this.otherAgentAbbrev = otherAgentAbbrev;
      this.organizationAbbrev = organizationAbbrev;
      this.organizationLevel = organizationLevel;
      this.expectedMasterAbbrev = expectedMasterAbbrev;
 }



  /**
   @return the combinedKW
   */
  @Override
  public synchronized double getCombinedKW() {
    return combinedKW;
  }

  /**
   @param combinedKW the combinedKW to set
   */
  public synchronized void setCombinedKW(double combinedKW) {
    this.combinedKW = combinedKW;
  }

  /**
   @return the expectedMasterAbbrev
   */
  @Override
  public synchronized String getExpectedMasterAbbrev() {
    return expectedMasterAbbrev;
  }

  /**
   @param expectedMasterAbbrev the expectedMasterAbbrev to set
   */
  @Override
  public synchronized void setExpectedMasterAbbrev(String expectedMasterAbbrev) {
    this.expectedMasterAbbrev = expectedMasterAbbrev;
  }



  @Override
  public synchronized void setOrgModelFolder(String orgModelFolder) {
    this.orgModelFolder = orgModelFolder;
  }

  /**
   @return the organizationAbbrev
   */
  @Override
  public synchronized String getOrganizationAbbrev() {
    return organizationAbbrev;
  }

  /**
   @param organizationAbbrev the organizationAbbrev to set
   */
  @Override
  public synchronized void setOrganizationAbbrev(String organizationAbbrev) {
    this.organizationAbbrev = organizationAbbrev;
  }

  @Override
  public synchronized String getOrganizationLevel() {
    return this.organizationLevel;
  }

  @Override
  public synchronized void setOrganizationLevel(final String organizationLevel) {
    this.organizationLevel = organizationLevel;
  }

  /**
   @return the otherAgentAbbrev
   */
  @Override
  public synchronized String getOtherAgentAbbrev() {
    return otherAgentAbbrev;
  }

  /**
   @param otherAgentAbbrev the otherAgentAbbrev to set
   */
  @Override
  public synchronized void setOtherAgentAbbrev(String otherAgentAbbrev) {
    this.otherAgentAbbrev = otherAgentAbbrev;
  }

  /**
   @return the specificationFilePath
   */
  @Override
  public synchronized String getSpecificationFilePath() {
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
  public synchronized boolean isConnected() {
    return this.isConnected;
  }

  @Override
  public synchronized  void setConnected(final boolean isConnected) {
    this.isConnected = isConnected;
  }

  /**
   Determines if this is a connection to a child - an agent directly lower (subordinate) in the organization.

   @return true if this is a connection to a child, false if not
   */
  @Override
  public synchronized boolean isConnectionToChild() {
    boolean connectionToChild = false;
    if (!otherIsExpectedMaster()) connectionToChild = true;
    return connectionToChild;
  }

  /**
   Determines if this is a connection to a parent - an agent directly higher (superior) in the organization.

   @return true if this is a connection to a parent, false if not
   */
  @Override
  public synchronized boolean isConnectionToParent() {
    boolean connectionToParent = false;
    if (otherIsExpectedMaster()) connectionToParent = true;
    return connectionToParent;
  }

  /**
   Determines if this connection has been registered in the shared organization. For now, we assume all connections are
   also part of an affiliated organization. In an open system, there could be connections without registration, but not
   in our closed system.

   @return true if registered, false if not
   */
  @Override
  public synchronized boolean isRegistered() {
    return this.isRegistered;
  }

  @Override
  public synchronized void setRegistered(final boolean isRegistered) {
    this.isRegistered = isRegistered;

  }

  private boolean otherIsExpectedMaster() {
    return this.getExpectedMasterAbbrev().equals(this.getOtherAgentAbbrev());
  }

  @Override
  public String toString() {
    return "ConnectionGuidelines{" +
            "otherAgentAbbrev='" + otherAgentAbbrev + '\'' +
            ", organizationAbbrev='" + organizationAbbrev + '\'' +
            ", organizationLevel='" + organizationLevel + '\'' +
            ", expectedMasterAbbrev='" + expectedMasterAbbrev + '\'' +
            ", specificationFilePath='" + specificationFilePath + '\'' +
            ", combinedKW=" + combinedKW +
            ", orgModelFolder= " + orgModelFolder + '\'' +
            ", isConnected=" + isConnected +
            '}';
  }
}
