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
package edu.ksu.cis.macr.ipds.grid.persona;


import edu.ksu.cis.macr.aasis.agent.persona.IOrganization;
import edu.ksu.cis.macr.aasis.spec.OrganizationFocus;
import edu.ksu.cis.macr.ipds.grid.plan_selector.GridPlanSelector;
import edu.ksu.cis.macr.ipds.primary.persona.Persona;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 A persona for this affiliate organization. Uses organization-specific plan selector and organization-specific communication capabilities.
 */
public class GridPersona extends Persona {
    private static final Logger LOG = LoggerFactory.getLogger(GridPersona.class);
    private static final boolean debug = false;



    /**
     Constructs a new instance of an agent using the organization-based agent architecture. Each prosumer agent will have
     the capabilities needed to cc in a peer-based organization both as a peer and as a supervisor and contains its own
     knowledge about the immediate organization in which it participates.

     @param org the organization, containing information about agents and objects in the organization system.
     @param identifierString a string containing a name that uniquely identifies this in the system.
     @param knowledge an XML specification of the organization.
     @param focus an Enum that defines the focus of the organization.
     */
    public GridPersona(final IOrganization org, final String identifierString, final Element knowledge, final OrganizationFocus focus) {
        super(org, identifierString, knowledge, focus);
        planSelector = new GridPlanSelector();
        LOG.info("\t..................DONE CONSTRUCTING GRID PERSONA(org={}, identifier={}, knowledge={}, focus={}", org, identifierString, knowledge, focus);
    }

    /**
     Constructs a new instance of an agent using the organization-based agent architecture. Each prosumer agent will have
     the capabilities needed to cc in a peer-based organization both as a peer and as a supervisor and contains its own
     knowledge about the immediate organization in which it participates.

     @param identifierString a string containing a name that uniquely
     */
    public GridPersona(final String identifierString) {
        super(identifierString);
        planSelector = new GridPlanSelector();
        LOG.info("\t..................DONE CONSTRUCTING GRID PERSONA {}.", identifierString);
    }


    @Override
    public String toString() {
        return "GridPersona [identifierString=" + this.identifierString + "]";
    }


}

