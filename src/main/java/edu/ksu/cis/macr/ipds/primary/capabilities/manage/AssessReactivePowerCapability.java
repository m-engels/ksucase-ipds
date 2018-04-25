/**
 *
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
package edu.ksu.cis.macr.ipds.primary.capabilities.manage;

import edu.ksu.cis.macr.aasis.agent.persona.AbstractOrganizationCapability;
import edu.ksu.cis.macr.aasis.agent.persona.IOrganization;
import edu.ksu.cis.macr.aasis.agent.persona.IPersona;
import edu.ksu.cis.macr.ipds.primary.messages.IPowerAssessment;
import edu.ksu.cis.macr.ipds.primary.messages.PowerAssessment;
import edu.ksu.cis.macr.ipds.primary.sensors.smartmeter.IElectricalData;
import edu.ksu.cis.macr.ipds.primary.sensors.smartmeter.ISmartMeterRead;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Objects;

/**
 The {@code AssessReactivePowerCapability} provides the ability to compare historical power readings with provided
 guidelines and make assessments about compliance and trends.  This refers to reactive as being opposed to proactive
 - it simply responds based on recent the and immediately prior measurements. (It has nothing to do with "reactive power
 as in Q values" and could be renamed, but would require changing all agent capability configuration names as well.
 */
public class AssessReactivePowerCapability extends AbstractOrganizationCapability implements IAssessReactivePowerCapability {
  private static final Logger LOG = LoggerFactory.getLogger(AssessReactivePowerCapability.class);
  private IPowerAssessment assess = new PowerAssessment();
  private double actualKW;
  private double lastKW;
  private double maxKW;
  private double minKW;

  /**
   Construct a new {@code AssessReactivePowerCapability} instance.

   @param owner - the agent possessing this capability.
   @param org - the immediate organization in which this agent operates.
   */
  public AssessReactivePowerCapability( final IPersona owner, IOrganization org) {
    super(AssessReactivePowerCapability.class, owner, org);
    minKW = 0.0;
    maxKW = 0.0;
    lastKW = 0.0;
    actualKW = 0.0;
  }

  /**
   @return the actualKW
   */
  @Override
  public double getActualKW() {
    return this.actualKW;
  }


  /**
   @param actualKW2 the actualKW to set
   */
  @Override
  public synchronized void setActualKW(final double actualKW2) {
    this.actualKW = actualKW2;
  }

  @Override
  public double getFailure() {
    return 0;
  }

  /**
   @return the lastKW
   */
  public double getLastKW() {
    return this.lastKW;
  }

  /**
   @param lastKW2 the lastKW to set
   */
  public synchronized void setLastKW(final double lastKW2) {
    this.lastKW = lastKW2;
  }

  /**
   @return the maxKW
   */
  @Override
  public double getMaxKW() {
    return this.maxKW;
  }

  /**
   @param maxKW2 the maxKW to set
   */
  @Override
  public synchronized void setMaxKW(final double maxKW2) {
    this.maxKW = maxKW2;
  }

  /**
   @return the minKW
   */
  @Override
  public double getMinKW() {
    return this.minKW;
  }

  /**
   @param minKW2 the minKW to set
   */
  @Override
  public synchronized void setMinKW(final double minKW2) {
    this.minKW = minKW2;
  }


  /**
   Get a power assessment.

   @param minKW - the min KW allowed
   @param maxKW - the max KW allowed
   @param read - the given smart meter read
   @return PowerAssessment - the resulting power assessment
   */
  @Override
  public IPowerAssessment getPowerAssessment(final double minKW,
                                             final double maxKW, ISmartMeterRead read) {

    this.minKW = Objects.requireNonNull(minKW);
    this.maxKW = Objects.requireNonNull(maxKW);

    IElectricalData now = read.getElectricalData();
    IElectricalData last = read.getPreviousElectricalData();

    this.actualKW = now.getPhaseAPload() + now.getPhaseBPload() + now.getPhaseCPload();
    this.lastKW = last.getPhaseAPload() + last.getPhaseBPload() + last.getPhaseCPload();

    switch (Double.compare(this.actualKW, this.minKW)) {
      case -1: // lower than minimum
        assess.setAssessment(IPowerAssessment.RangeAssessment.LOW);
        assess.setMargin(this.minKW - this.actualKW);
        switch (Double.compare(this.actualKW, this.lastKW)) {
          case -1:
            assess.setMarginTrend(IPowerAssessment.TrendAssessment.INCREASING);
            break;
          case 0:
            assess.setMarginTrend(IPowerAssessment.TrendAssessment.LEVEL);
            break;
          case 1:
            assess.setMarginTrend(IPowerAssessment.TrendAssessment.DECREASING);
            break;
          default:
            break;
        }
        break;
    }
    // greater than or equal to min
    switch (Double.compare(this.actualKW, this.maxKW)) {
      case -1: // lower than maximum
      case 0: // at maximum
        assess.setAssessment(IPowerAssessment.RangeAssessment.INBOUNDS);
        assess.setMargin(this.maxKW - this.actualKW);
        switch (Double.compare(this.actualKW, this.lastKW)) {
          case -1:
            assess.setMarginTrend(IPowerAssessment.TrendAssessment.INCREASING); // assumes we care more
            break; // about max boundary
          case 0:  // covers 0 return
            assess.setMarginTrend(IPowerAssessment.TrendAssessment.LEVEL);
            break;
          case 1:
            assess.setMarginTrend(IPowerAssessment.TrendAssessment.DECREASING);
            break;
          default:  // covers 0 return
            assess.setMarginTrend(IPowerAssessment.TrendAssessment.LEVEL);
            break;
        }
        break;

      case 1: // greater than max
        assess.setAssessment(IPowerAssessment.RangeAssessment.HIGH);
        assess.setMargin(this.actualKW - this.maxKW);
        switch (Double.compare(this.actualKW, this.lastKW)) {
          case -1:
            assess.setMarginTrend(IPowerAssessment.TrendAssessment.DECREASING); // assumes we care more
            break; // about max boundary
          case 0:
            assess.setMarginTrend(IPowerAssessment.TrendAssessment.LEVEL);
            break;
          case 1:
            assess.setMarginTrend(IPowerAssessment.TrendAssessment.INCREASING);
            break;
          default:
            assess.setMarginTrend(IPowerAssessment.TrendAssessment.LEVEL);

        }
        break;
    }
    LOG.info(assess.toString());
    return assess;

  }


  /*
   * (non-Javadoc)
   *
   * @see edu.ksu.cis.macr.aasis.agent.architecture.capability.ICapability#reset()
   */
  @Override
  public synchronized void reset() {
  }

  @Override
  public Element toElement(final Document document) {
    final Element capability = super.toElement(document);

    return capability;
  }

  @Override
  public String toString() {
    return String.format("AssessReactivePowerCapability{assess=%s, actualKW=%s, lastKW=%s, maxKW=%s, minKW=%s}", assess, actualKW, lastKW, maxKW, minKW);
  }
}
