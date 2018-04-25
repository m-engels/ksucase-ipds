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
package edu.ksu.cis.macr.ipds.grid.capabilities.admin;

import edu.ksu.cis.macr.aasis.agent.persona.AbstractOrganizationCapability;
import edu.ksu.cis.macr.aasis.agent.persona.IOrganization;
import edu.ksu.cis.macr.aasis.agent.persona.IPersona;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

/*
 * DistributeLoadDirectCapability.java -
 *
 * Capability that takes care of calculating redistribution algorithm
 * using a simple direct ratio based on the most recent consumption readings.
 *
 */
public class DistributeLoadCapability extends AbstractOrganizationCapability {
  private static final Logger LOG = LoggerFactory.getLogger(DistributeLoadCapability.class);

  public DistributeLoadCapability( final IPersona owner,    final IOrganization org) {
    super(DistributeLoadCapability.class, owner, org);

  }

  /**
   Given the most recent power consumption values for each participating instance goal, use a direct ratio to
   redistribute the total maximum kW allocated.

   @param mostRecentValues - the map of current power consumption values in kW by associated instance goal
   @param total - the combined power consumption in kW
   @param max - the maximum kW to be distributed
   @return new load guidelines
   */
  public Map<InstanceGoal<?>, Double> calculateNewLoadGuidelines(Map<InstanceGoal<?>, Double> mostRecentValues, double total, double max) {
    Map<InstanceGoal<?>, Double> updatedGuidelines = new HashMap<>();
    double roundTotal = Math.round(1000 * total) / ((double) 1000);
    for (Map.Entry<InstanceGoal<?>, Double> thisGoalValues : mostRecentValues.entrySet()) {
      double newAssignment = (thisGoalValues.getValue() / roundTotal) * max;
      newAssignment = Math.round(1000 * newAssignment) / ((double) 1000);
      updatedGuidelines.put(thisGoalValues.getKey(), newAssignment);
    }

    return updatedGuidelines;
  }

  @Override
  public double getFailure() {
    return 0;
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
    return "DistributeLoadDirectCapability";
  }
}
