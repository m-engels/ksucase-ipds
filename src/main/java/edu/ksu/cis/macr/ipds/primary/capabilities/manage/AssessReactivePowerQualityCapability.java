/**
 *
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
package edu.ksu.cis.macr.ipds.primary.capabilities.manage;

import edu.ksu.cis.macr.aasis.agent.persona.AbstractOrganizationCapability;
import edu.ksu.cis.macr.aasis.agent.persona.IOrganization;
import edu.ksu.cis.macr.aasis.agent.persona.IPersona;
import edu.ksu.cis.macr.ipds.primary.messages.IPowerQualityAssessment;
import edu.ksu.cis.macr.ipds.primary.messages.PowerQualityAssessment;
import edu.ksu.cis.macr.ipds.primary.sensors.smartmeter.ISmartMeterRead;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 The {@code AssessReactivePowerQualityCapability} provides the ability to compare historical actual power quality
 measurements to suggested guidelines.  For example, voltages should remain between 0.95 and 1.05.  Voltage
 flucations should be minimized if possible. Voltages along a line should be level rather than differing too much between
 adjacent nodes.
 */
public class AssessReactivePowerQualityCapability extends AbstractOrganizationCapability implements IAssessReactivePowerQualityCapability {
  private static final Logger LOG = LoggerFactory.getLogger(AssessReactivePowerQualityCapability.class);
  private IPowerQualityAssessment assess = null;
  private double actualMultiplier;
  private double lastMultiplier;
  private double maxMultiplier;
  private double minMultiplier;


  /**
   Construct a new {@code AssessReactivePowerCapability} instance.

   @param owner - the agent possessing this capability.
   @param org - the immediate organization in which this agent operates.
   */
  public AssessReactivePowerQualityCapability(
          final IPersona owner,
          final IOrganization org) {
    super(AssessReactivePowerQualityCapability.class, owner,
            org);
    this.actualMultiplier = 0.0;
    this.lastMultiplier = 0.0;
    this.maxMultiplier = 0.0;
    this.minMultiplier = 0.0;
  }

  /**
   @return the actualMultiplier
   */
  @Override
  public double getActualMultiplier() {
    return this.actualMultiplier;
  }


  /**
   @param actualMultiplier the actualMultiplier to set
   */
  @Override
  public synchronized void setActualMultiplier(final double actualMultiplier) {
    this.actualMultiplier = actualMultiplier;
  }


  @Override
  public double getFailure() {
    return 0;
  }

  /**
   @return the lastMultiplier
   */
  @Override
  public double getLastMultiplier() {
    return this.lastMultiplier;
  }

  /**
   @param lastMultiplier the lastMultiplier to set
   */
  @Override
  public synchronized void setLastMultiplier(final double lastMultiplier) {
    this.lastMultiplier = lastMultiplier;
  }

  /**
   @return the maxMultiplier
   */
  @Override
  public double getMaxMultiplier() {
    return this.maxMultiplier;
  }

  /**
   @param maxMultiplier the maxMultiplier to set
   */
  @Override
  public synchronized void setMaxMultiplier(final double maxMultiplier) {
    this.maxMultiplier = maxMultiplier;
  }

  /**
   @return the minMultiplier
   */
  @Override
  public double getMinMultiplier() {
    return this.minMultiplier;
  }

  /**
   @param minMultiplier the minMultiplier to set
   */
  @Override
  public synchronized void setMinMultiplier(final double minMultiplier) {
    this.minMultiplier = minMultiplier;
  }

  /**
   @param read - the data from the smart meter reading. (electrical data and time slices)
   @return PowerQualityAssessment
   */
  @Override
  public IPowerQualityAssessment getPowerQualityAssessment(double minMultiplier, double maxMultiplier, final ISmartMeterRead read) {

    // TODO: Power quality assessment is limited to a single phase. Needs to be updated to work for 3-phase feeders.

    // determine the phase(s)
    this.assess = new PowerQualityAssessment();

    if (read.getElectricalData().getPhaseAvoltage() > 0) {
      this.actualMultiplier = read.getElectricalData().getPhaseAvoltage();
      this.lastMultiplier = read.getPreviousElectricalData().getPhaseAvoltage();
    } else if (read.getElectricalData().getPhaseBvoltage() > 0) {
      this.actualMultiplier = read.getElectricalData().getPhaseBvoltage();
      this.lastMultiplier = read.getPreviousElectricalData().getPhaseBvoltage();
    } else if (read.getElectricalData().getPhaseCvoltage() > 0) {
      this.actualMultiplier = read.getElectricalData().getPhaseCvoltage();
      this.lastMultiplier = read.getPreviousElectricalData().getPhaseCvoltage();
    }


    switch (Double.compare(this.actualMultiplier, minMultiplier)) {
      case -1: // lower than minimum
        this.assess.setVoltageAssessment(IPowerQualityAssessment.VoltageAssessment.LOW);
        this.assess.setMargin(minMultiplier - this.actualMultiplier);
        switch (Double.compare(this.actualMultiplier, this.lastMultiplier)) {
          case -1:
            this.assess.setMarginTrend(IPowerQualityAssessment.TrendAssessment.INCREASING);
            break;
          case 0:
            this.assess.setMarginTrend(IPowerQualityAssessment.TrendAssessment.LEVEL);
            break;
          case 1:
            this.assess.setMarginTrend(IPowerQualityAssessment.TrendAssessment.DECREASING);
            break;
          default:
            this.assess.setMarginTrend(IPowerQualityAssessment.TrendAssessment.LEVEL);
            break;
        }
        break;
    }
    // greater than or equal to min
    switch (Double.compare(this.actualMultiplier, maxMultiplier)) {
      case -1: // lower than maximum

        if (this.actualMultiplier < this.minMultiplier) {
          this.assess.setVoltageAssessment(IPowerQualityAssessment.VoltageAssessment.LOW);
          this.assess.setMargin(minMultiplier - this.actualMultiplier);
        } else {
          this.assess.setVoltageAssessment(IPowerQualityAssessment.VoltageAssessment.INBOUNDS);
          this.assess.setMargin(this.actualMultiplier - minMultiplier);
        }
        switch (Double.compare(this.actualMultiplier, this.lastMultiplier)) {
          case -1:
            this.assess.setMarginTrend(IPowerQualityAssessment.TrendAssessment.INCREASING); // assumes we care
            // more
            // about max boundary
            break;
          case 0:
            this.assess.setMarginTrend(IPowerQualityAssessment.TrendAssessment.LEVEL);
            break;
          case 1:
            this.assess.setMarginTrend(IPowerQualityAssessment.TrendAssessment.DECREASING);
            break;
          default:
            this.assess.setMarginTrend(IPowerQualityAssessment.TrendAssessment.LEVEL);
            break;
        }
        break;
      case 0: // at maximum
        this.assess.setVoltageAssessment(IPowerQualityAssessment.VoltageAssessment.INBOUNDS);
        if (this.actualMultiplier > 1.0) {
          this.assess.setMargin(maxMultiplier - this.actualMultiplier);
        } else {
          this.assess.setMargin(this.actualMultiplier - minMultiplier);
        }
        switch (Double.compare(this.actualMultiplier, this.lastMultiplier)) {
          case -1:
            this.assess.setMarginTrend(IPowerQualityAssessment.TrendAssessment.INCREASING); // assumes we care
            // more
            // about max boundary
            break;
          case 0:
            this.assess.setMarginTrend(IPowerQualityAssessment.TrendAssessment.LEVEL);
            break;
          case 1:
            this.assess.setMarginTrend(IPowerQualityAssessment.TrendAssessment.DECREASING);
            break;
          default:
            this.assess.setMarginTrend(IPowerQualityAssessment.TrendAssessment.LEVEL);
            break;
        }
        break;

      case 1: // greater than max
        this.assess.setVoltageAssessment(IPowerQualityAssessment.VoltageAssessment.HIGH);
        this.assess.setMargin(this.actualMultiplier - maxMultiplier);
        switch (Double.compare(this.actualMultiplier, this.lastMultiplier)) {
          case -1:
            this.assess.setMarginTrend(IPowerQualityAssessment.TrendAssessment.DECREASING); // assumes we care
            // more
            // about max boundary
            break;
          case 0:
            this.assess.setMarginTrend(IPowerQualityAssessment.TrendAssessment.LEVEL);
            break;
          case 1:
            this.assess.setMarginTrend(IPowerQualityAssessment.TrendAssessment.INCREASING);
            break;
        }
        break;
    }
    this.assess.setLastVoltage(this.lastMultiplier);
    this.assess.setThisVoltage(this.actualMultiplier);
    this.assess.setMinVoltage(this.minMultiplier);
    this.assess.setMaxVoltage(this.maxMultiplier);
    LOG.info(this.assess.toString());
    return this.assess;
  }


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
    return String.format("AssessReactivePowerQualityCapability{assess=%s, actualMultiplier=%s, lastMultiplier=%s, maxMultiplier=%s, minMultiplier=%s}", assess, actualMultiplier, lastMultiplier, maxMultiplier, minMultiplier);
  }


}
