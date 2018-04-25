/**
 *
 * Copyright 2012 Denise Case Kansas State University MACR Laboratory
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
package edu.ksu.cis.macr.ipds.grid.capabilities.admin;

import edu.ksu.cis.macr.aasis.agent.persona.AbstractOrganizationCapability;
import edu.ksu.cis.macr.aasis.agent.persona.IOrganization;
import edu.ksu.cis.macr.aasis.agent.persona.IPersona;
import edu.ksu.cis.macr.ipds.primary.messages.*;
import edu.ksu.cis.macr.ipds.primary.sensors.smartmeter.IElectricalData;
import edu.ksu.cis.macr.organization.model.Agent;
import edu.ksu.cis.macr.organization.model.identifiers.ClassIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

/**
 The {@code AggregationCapability} provides the ability to aggregate multiple local power readings. It assesses total
 critical power needs, total load, and can assess possible local sources of additional generation as well as determine if
 the combined values are outside suggested bounds.
 */
public class AggregatePowerCapability extends AbstractOrganizationCapability {

  private static final Logger LOG = LoggerFactory.getLogger(AggregatePowerCapability.class);
  private static final boolean debug = false;

  private IPowerAssessment assess = new PowerAssessment();

    private double actualMultiplier;
  private double lastMultiplier;
  private double maxMultiplier;
  private double minMultiplier;

  /**
   Construct a new {@code AggregationCapability} instance.

   @param owner - the agent possessing this capability.
   @param org - the immediate organization in which this agent operates.
   */
  public AggregatePowerCapability(
          final IPersona owner,
          final IOrganization org) {
    super(AggregatePowerCapability.class, owner, org);
  }


  @Override
  public double getFailure() {
    return 0;
  }

  /**
   Gets the set of local registered prosumer agents.

   @param allAgents - the set of all agents registered in this organization
   @return  - the set of all prosumer agents registered in this local organization (does not include
   other types of agents such as forecasters, etc)
   */
  public Set<Agent<?>> getLocalRegisteredProsumers(Set<Agent<?>> allAgents) {
    // get the list of registered prosumer peer agents in the local organization

    if (debug) LOG.debug("Number of all agents found in the AggregationCapability is {}", allAgents.size());

    final Set<Agent<?>> prosumers = new HashSet<>();
    Iterator<Agent<?>> it = allAgents.iterator();

    final Class<?> capabilityClass = AggregatePowerCapability.class;
    final ClassIdentifier capabilityIdentifier = new ClassIdentifier(
            capabilityClass);

    while (it.hasNext()) {
      Agent<?> agent = it.next();
      if (debug) LOG.debug("Checking registered agent {} for AggregationCapability", agent.toString());
      if (agent.getPossesses(capabilityIdentifier) != null) {
        prosumers.add(agent);
        if (debug) LOG.debug("Agent {} added to local prosumers list", agent.toString());
      }
    }
    return prosumers;
  }

  public IPowerAssessment getPowerAssessment(IPowerMessageContent content) {
      double minKW = Objects.requireNonNull(content.getMinKW());
      double maxKW = Objects.requireNonNull(content.getMaxKW());

    IElectricalData now = content.getElectricalData();
    IElectricalData last = content.getPreviousElectricalData();

      double actualKW = now.getPhaseAPload() + now.getPhaseBPload() + now.getPhaseCPload();
      double lastKW = last.getPhaseAPload() + last.getPhaseBPload() + last.getPhaseCPload();

    switch (Double.compare(actualKW, minKW)) {
      case -1: // lower than minimum
        assess.setAssessment(IPowerAssessment.RangeAssessment.LOW);
        assess.setMargin(minKW - actualKW);
        switch (Double.compare(actualKW, lastKW)) {
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
    switch (Double.compare(actualKW, maxKW)) {
      case -1: // lower than maximum
      case 0: // at maximum
        assess.setAssessment(IPowerAssessment.RangeAssessment.INBOUNDS);
        assess.setMargin(maxKW - actualKW);
        switch (Double.compare(actualKW, lastKW)) {
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
        assess.setMargin(actualKW - maxKW);
        switch (Double.compare(actualKW, lastKW)) {
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

  /**
   @param content - the message payload - the custom content to be delivered
   @return - the power quality assessment
   */
  public IPowerQualityAssessment getPowerQualityAssessment(IPowerMessageContent content) {

    // TODO: Greg: Power quality assessment is limited to a single phase. Needs to be updated to work for 3-phase feeders.

    // determine the phase(s)
      IPowerQualityAssessment qualityassess = new PowerQualityAssessment();

    if (content.getElectricalData().getPhaseAvoltage() > 0) {
      this.actualMultiplier = content.getElectricalData().getPhaseAvoltage();
      this.lastMultiplier = content.getPreviousElectricalData().getPhaseAvoltage();
    } else if (content.getElectricalData().getPhaseBvoltage() > 0) {
      this.actualMultiplier = content.getElectricalData().getPhaseBvoltage();
      this.lastMultiplier = content.getPreviousElectricalData().getPhaseBvoltage();
    } else if (content.getElectricalData().getPhaseCvoltage() > 0) {
      this.actualMultiplier = content.getElectricalData().getPhaseCvoltage();
      this.lastMultiplier = content.getPreviousElectricalData().getPhaseCvoltage();
    }


    switch (Double.compare(this.actualMultiplier, minMultiplier)) {
      case -1: // lower than minimum
        qualityassess.setVoltageAssessment(IPowerQualityAssessment.VoltageAssessment.LOW);
        qualityassess.setMargin(minMultiplier - this.actualMultiplier);
        switch (Double.compare(this.actualMultiplier, this.lastMultiplier)) {
          case -1:
            qualityassess.setMarginTrend(IPowerQualityAssessment.TrendAssessment.INCREASING);
            break;
          case 0:
            qualityassess.setMarginTrend(IPowerQualityAssessment.TrendAssessment.LEVEL);
            break;
          case 1:
            qualityassess.setMarginTrend(IPowerQualityAssessment.TrendAssessment.DECREASING);
            break;
          default:
            qualityassess.setMarginTrend(IPowerQualityAssessment.TrendAssessment.LEVEL);
            break;
        }
        break;
    }
    // greater than or equal to min
    switch (Double.compare(this.actualMultiplier, maxMultiplier)) {
      case -1: // lower than maximum

        if (this.actualMultiplier < this.minMultiplier) {
          qualityassess.setVoltageAssessment(IPowerQualityAssessment.VoltageAssessment.LOW);
          qualityassess.setMargin(minMultiplier - this.actualMultiplier);
        } else {
          qualityassess.setVoltageAssessment(IPowerQualityAssessment.VoltageAssessment.INBOUNDS);
          qualityassess.setMargin(this.actualMultiplier - minMultiplier);
        }
        switch (Double.compare(this.actualMultiplier, this.lastMultiplier)) {
          case -1:
            qualityassess.setMarginTrend(IPowerQualityAssessment.TrendAssessment.INCREASING); // assumes we care
            // more
            // about max boundary
            break;
          case 0:
            qualityassess.setMarginTrend(IPowerQualityAssessment.TrendAssessment.LEVEL);
            break;
          case 1:
            qualityassess.setMarginTrend(IPowerQualityAssessment.TrendAssessment.DECREASING);
            break;
          default:
            qualityassess.setMarginTrend(IPowerQualityAssessment.TrendAssessment.LEVEL);
            break;
        }
        break;
      case 0: // at maximum
        qualityassess.setVoltageAssessment(IPowerQualityAssessment.VoltageAssessment.INBOUNDS);
        if (this.actualMultiplier > 1.0) {
          qualityassess.setMargin(maxMultiplier - this.actualMultiplier);
        } else {
          qualityassess.setMargin(this.actualMultiplier - minMultiplier);
        }
        switch (Double.compare(this.actualMultiplier, this.lastMultiplier)) {
          case -1:
            qualityassess.setMarginTrend(IPowerQualityAssessment.TrendAssessment.INCREASING); // assumes we care
            // more
            // about max boundary
            break;
          case 0:
            qualityassess.setMarginTrend(IPowerQualityAssessment.TrendAssessment.LEVEL);
            break;
          case 1:
            qualityassess.setMarginTrend(IPowerQualityAssessment.TrendAssessment.DECREASING);
            break;
          default:
            qualityassess.setMarginTrend(IPowerQualityAssessment.TrendAssessment.LEVEL);
            break;
        }
        break;

      case 1: // greater than max
        qualityassess.setVoltageAssessment(IPowerQualityAssessment.VoltageAssessment.HIGH);
        qualityassess.setMargin(this.actualMultiplier - maxMultiplier);
        switch (Double.compare(this.actualMultiplier, this.lastMultiplier)) {
          case -1:
            qualityassess.setMarginTrend(IPowerQualityAssessment.TrendAssessment.DECREASING); // assumes we care
            // more
            // about max boundary
            break;
          case 0:
            qualityassess.setMarginTrend(IPowerQualityAssessment.TrendAssessment.LEVEL);
            break;
          case 1:
            qualityassess.setMarginTrend(IPowerQualityAssessment.TrendAssessment.INCREASING);
            break;
        }
        break;
    }
    qualityassess.setLastVoltage(this.lastMultiplier);
    qualityassess.setThisVoltage(this.actualMultiplier);
    qualityassess.setMinVoltage(this.minMultiplier);
    qualityassess.setMaxVoltage(this.maxMultiplier);
    LOG.info(qualityassess.toString());
    return qualityassess;
  }

  @Override
  public synchronized void reset() {
  }

  @Override
  public Element toElement(final Document document) {
    final Element capability = super.toElement(document);
    return capability;
  }

}
