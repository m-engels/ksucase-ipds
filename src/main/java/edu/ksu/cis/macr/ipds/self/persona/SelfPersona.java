/*
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
package edu.ksu.cis.macr.ipds.self.persona;

import edu.ksu.cis.macr.aasis.agent.persona.IOrganization;
import edu.ksu.cis.macr.aasis.spec.OrganizationFocus;
import edu.ksu.cis.macr.ipds.primary.persona.Persona;
import edu.ksu.cis.macr.ipds.self.plan_selector.SelfPlanSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * The {@code Agent} is the base class for all custom agent types in the organization-based agent architecture.
 * The derived type of agent is defined by the capabilities given to that agent type.  Each autonomous agent has one
 * head or master running its internal organization of persona.  A persona is added for each affiliated organization in
 * the agents list of connections provided with its "Self Control" goal.
 */
public class SelfPersona extends Persona {
    private static final Logger LOG = LoggerFactory.getLogger(SelfPersona.class);
    private static final Boolean debug = true;

    /**
     * Constructs a new instance of an agent using the organization-based agent architecture. Each prosumer agent will have
     * the capabilities needed to cc in a peer-based organization both as a peer and as a supervisor and contains its own
     * knowledge about the immediate organization in which it participates.
     *
     * @param org              the organization, containing information about agents and objects in the organization system.
     * @param identifierString a string containing a name that uniquely identifies this in the system.
     * @param knowledge        an XML specification of the organization.
     * @param focus            the enum to show what focus the organization is.
     */
    public SelfPersona(final IOrganization org, final String identifierString, final Element knowledge, final OrganizationFocus focus) {
        super(org, identifierString, knowledge, focus);

        LOG.info("\t..................CONSTRUCTING SELF PERSONA(org={}, identifier={}, knowledge={}, focus={})", org, identifierString, knowledge, focus);

        this.organization = org;
        this.focus = focus;
        if (debug)
            LOG.debug("Setting the {} EC initial organization events from the CC Organization Events. They are {}.",
                    this.getOrganizationEvents().numberOfQueuedEvents(), this.getOrganizationEvents());
        this.setOrganizationEvents(this.controlComponent.getOrganizationEvents());
        this.planSelector = new SelfPlanSelector();

        LOG.info("\t..................EXITING SELF PERSONA(org={}, identifier={}, knowledge={}, focus={})", org, identifierString, knowledge, focus);
    }

    /**
     * Constructs a new instance of an agent using the organization-based agent architecture. Each prosumer agent will have
     * the capabilities needed to cc in a peer-based organization both as a peer and as a supervisor and contains its own
     * knowledge about the immediate organization in which it participates.
     *
     * @param identifierString a string containing a name that uniquely
     */
    public SelfPersona(final String identifierString) {
        super(identifierString);
        LOG.info("\t..................CONSTRUCTING SELF PERSONA(identifier={}).", identifierString);

        this.setOrganizationEvents(this.controlComponent.getOrganizationEvents());
        this.planSelector = new SelfPlanSelector();

        LOG.info("\t..................EXITING SELF PERSONA(identifier={})", identifierString);
    }

    @Override
    public synchronized String toString() {
        return "Agent [identifierString=" + this.identifierString + "]";
    }
}
