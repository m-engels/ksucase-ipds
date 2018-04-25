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
package edu.ksu.cis.macr.ipds.primary.capabilities.participate;

//import edu.ksu.cis.macr.aasis.agent.ec_cap.BaseAbstractOrganizationCapability;

import edu.ksu.cis.macr.aasis.agent.persona.AbstractOrganizationCapability;
import edu.ksu.cis.macr.aasis.agent.persona.IOrganization;
import edu.ksu.cis.macr.aasis.agent.persona.IPersona;
import edu.ksu.cis.macr.organization.model.Agent;
import edu.ksu.cis.macr.organization.model.identifiers.ClassIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 The {@code SelfParticipateCapability} provides the ability for this agent to participate in an authorized organization.
 */
public class ParticipateCapability extends AbstractOrganizationCapability {

  private static final Logger LOG = LoggerFactory.getLogger(ParticipateCapability.class);

  /**
   Construct a new {@code SelfParticipateCapability} instance.

   @param owner - the agent possessing this capability.
   @param org - the immediate organization in which this agent operates.
   */
  public ParticipateCapability(final IPersona owner, final IOrganization org) {
    super(ParticipateCapability.class, owner, org);
    this.owner = owner;
  }

  @Override
  public double getFailure() {
    return 0;
  }

  /**
   Gets the set of local registered prosumer agents.

   @param allAgents - the set of all agents registered in this organization
   @return - the set of all prosumer agents registered in this local organization (does not include
   other types of agents such as forecasters, etc)
   */
  public Set<Agent<?>> getLocalRegisteredProsumers(Set<Agent<?>> allAgents) {
    // get the list of registered prosumer peer agents in the local organization

    LOG.debug("Number of all agents found in the SelfParticipateCapability is {}", allAgents.size());

    final Set<Agent<?>> prosumers = new HashSet<>();
    Iterator<Agent<?>> it = allAgents.iterator();

    final Class<?> capabilityClass = ParticipateCapability.class;
    final ClassIdentifier capabilityIdentifier = new ClassIdentifier(
            capabilityClass);

    while (it.hasNext()) {
      Agent<?> agent = it.next();
      LOG.debug("Checking registered agent {} for SelfParticipateCapability", agent.toString());
      if (agent.getPossesses(capabilityIdentifier) != null) {
        prosumers.add(agent);
        LOG.debug("Agent {} added to local prosumers list", agent.toString());
      }
    }
    return prosumers;
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
    return "SelfParticipateCapability [no content yet=]";
  }
}
