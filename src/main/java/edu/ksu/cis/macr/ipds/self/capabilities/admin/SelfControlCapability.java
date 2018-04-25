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
package edu.ksu.cis.macr.ipds.self.capabilities.admin;


import edu.ksu.cis.macr.aasis.agent.persona.AbstractOrganizationCapability;
import edu.ksu.cis.macr.aasis.agent.persona.IOrganization;
import edu.ksu.cis.macr.aasis.agent.persona.IPersona;
import edu.ksu.cis.macr.goal.model.InstanceParameters;
import edu.ksu.cis.macr.ipds.grid.capabilities.participate.GridControlHolonCapability;
import edu.ksu.cis.macr.ipds.primary.guidelines.IFeederGuidelines;
import edu.ksu.cis.macr.ipds.primary.guidelines.IHomeGuidelines;
import edu.ksu.cis.macr.ipds.primary.guidelines.ILateralGuidelines;
import edu.ksu.cis.macr.ipds.primary.guidelines.INeighborhoodGuidelines;
import edu.ksu.cis.macr.obaa_pp.objects.IDisplayInformation;
import edu.ksu.cis.macr.organization.model.Agent;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import edu.ksu.cis.macr.organization.model.identifiers.ClassIdentifier;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;

/**
 The {@code SelfControlCapability} provides the ability to act autonomously.  To startup, access central control
 systems, get authorizations and new capabilities, and initiate communications with other agents.
 */
public class SelfControlCapability extends AbstractOrganizationCapability {
  private static final Logger LOG = LoggerFactory.getLogger(SelfControlCapability.class);
  private static final boolean debug = false;
  private IHomeGuidelines homeGuidelines = null;
  private INeighborhoodGuidelines neighborhoodGuidelines = null;
  private ILateralGuidelines lateralGuidelines = null;
  private IFeederGuidelines feederGuidelines = null;

  /**
   Construct a new {@code AggregationCapability} instance.

   @param owner - the agent possessing this capability.
   @param org - the immediate organization in which this agent operates.
   */
  public SelfControlCapability(final IPersona owner, final IOrganization org) {
    super(SelfControlCapability.class, owner, org);
    this.owner = owner;

  }

  public synchronized void callForConfiguration() {
    // TODO Add ability "phone home" on startup to get agent configuration
    // information from the utility distribution main control center.
  }

  public UniqueIdentifier findHolon() {
    if (debug) LOG.debug("Entering findHolon.");
    UniqueIdentifier sub = null;
    // agent N43 has a sub holon persona if there's an N43inL39 persona that has a holon capability
    String meNoSelf = owner.getUniqueIdentifier().toString().replace("self", "");
    final Collection<IPersona> allPersona = this.getOrganization().getAllPersona();
    for (IPersona p : allPersona) {
      String name = p.getIdentifierString();
      if (debug) LOG.debug("This persona in my org has name : {}", name);
      if (name.contains(meNoSelf + "in")) {
        if (debug) LOG.debug("Possible sub: {}", name);
        final GridControlHolonCapability subCap = p.getCapability(GridControlHolonCapability.class);
        if (debug) LOG.debug("sub cap={}", subCap);
        if (subCap != null) {
          sub = p.getUniqueIdentifier();
          if (debug) LOG.debug("Found sub: {}", name);
        }
      }
    }
    return sub;
  }

    public UniqueIdentifier findParticipant(String participantCapability, String orgIdentifier) {
        if (debug) LOG.debug("Entering findParticipant with {}.", participantCapability);
        Class cap = null;
        try {
            cap = Class.forName(participantCapability);
        } catch (ClassNotFoundException e) {
            LOG.error("ERROR: trying to find participant but could not get class for participant capability = {}.", participantCapability);
            System.exit(-52);
        }
        UniqueIdentifier sub = null;
        // agent N43 has a auctioner if there's an N43inL39 persona that has a auction participant capability
        String meNoSelf = owner.getUniqueIdentifier().toString().replace("self", "");
        final Collection<IPersona> allPersona = this.getOrganization().getAllPersona();
        for (IPersona p : allPersona) {
            String name = p.getIdentifierString();
            if (debug) LOG.debug("This persona in my org has name : {}", name);
            if (name.contains(meNoSelf + orgIdentifier + "in")) {
                if (debug) LOG.debug("Possible sub: {}", name);
             if (debug) LOG.debug("sub cap={}", participantCapability);

                if ( p.getCapability( cap) != null) {
                    sub = p.getUniqueIdentifier();
                    LOG.info("Found sub: {}", name);
                }
            }
        }
        return sub;
    }


    @Override
  public double getFailure() {
    return 0;
  }

  public IFeederGuidelines getFeederGuidelines() {
    return feederGuidelines;
  }

  public synchronized void setFeederGuidelines(IFeederGuidelines feederGuidelines) {
    this.feederGuidelines = feederGuidelines;
  }

  public IHomeGuidelines getHomeGuidelines() {
    return homeGuidelines;
  }

  public synchronized void setHomeGuidelines(IHomeGuidelines homeGuidelines) {
    this.homeGuidelines = homeGuidelines;
  }

  public ILateralGuidelines getLateralGuidelines() {
    return lateralGuidelines;
  }

  public synchronized void setLateralGuidelines(ILateralGuidelines lateralGuidelines) {
    this.lateralGuidelines = lateralGuidelines;
  }

  /**
   Gets the set of local registered prosumer agents.

   @param allAgents - the set of all agents registered in this organization
   @return - the set of all prosumer agents registered in this local organization (does not include
   other types of agents such as forecasters, etc)
   */
  public Set<Agent<?>> getLocalRegisteredProsumers(Set<Agent<?>> allAgents) {
    // get the list of registered prosumer peer agents in the local organization

    if (debug) LOG.debug("Number of all agents found in the SelfControlCapability is {}", allAgents.size());

    final HashSet<Agent<?>> prosumers = new HashSet<>();
    Iterator<Agent<?>> it = allAgents.iterator();

    final Class<?> capabilityClass = SelfControlCapability.class;
    final ClassIdentifier capabilityIdentifier = new ClassIdentifier(
            capabilityClass);

    while (it.hasNext()) {
      Agent<?> agent = it.next();
      if (debug) LOG.debug("Checking registered agent {} for SelfControlCapability", agent.toString());
      if (agent.getPossesses(capabilityIdentifier) != null) {
        prosumers.add(agent);
        if (debug) LOG.debug("Agent {} added to local prosumers list", agent.toString());
      }
    }
    return prosumers;
  }

  public INeighborhoodGuidelines getNeighborhoodGuidelines() {
    return neighborhoodGuidelines;
  }

  public synchronized void setNeighborhoodGuidelines(INeighborhoodGuidelines neighborhoodGuidelines) {
    this.neighborhoodGuidelines = neighborhoodGuidelines;
  }

  @Override
  public synchronized void populateCapabilitiesOfDisplayObject(
          final IDisplayInformation displayInformation) {
    super.populateCapabilitiesOfDisplayObject(displayInformation);

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
    return "SelfControlCapability{" +
            "homeGuidelines=" + homeGuidelines +
            ", neighborhoodGuidelines=" + neighborhoodGuidelines +
            ", lateralGuidelines=" + lateralGuidelines +
            ", feederGuidelines=" + feederGuidelines +
            '}';
  }

    /**
     * Get all parameters from this instance goal and use them to initialize the capability.
     *
     * @param instanceGoal - this instance of the specification goal
     */
    public synchronized void init(InstanceGoal<?> instanceGoal) {
        LOG.info("Initializing capability from goal: {}.", instanceGoal);
        // Get the parameter values from the existing active instance goal
        final InstanceParameters params = Objects
                .requireNonNull((InstanceParameters) instanceGoal
                        .getParameter());
        if (debug) LOG.debug("Initializing params: {}.", params);
    }
}
