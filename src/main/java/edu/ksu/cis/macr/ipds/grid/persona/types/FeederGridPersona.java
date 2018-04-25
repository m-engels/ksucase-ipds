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
package edu.ksu.cis.macr.ipds.grid.persona.types;

import edu.ksu.cis.macr.aasis.agent.persona.IOrganization;
import edu.ksu.cis.macr.aasis.spec.OrganizationFocus;
import edu.ksu.cis.macr.ipds.grid.persona.GridPersona;
import org.w3c.dom.Element;

/**
 A lateral agent represents a lateral line coming off a main feeder line. It may both remoteRECEIVE an electrical load and may
 also provide generation to the system. It is implemented as a holonic agent that may act as both an agent within a local
 organization, and as an organization itself.  A lateral agent may be selected to perform a supervisory role for a
 set of peers and represent the peer set in a higher-level organization.
 */
public class FeederGridPersona extends GridPersona {

  /**
   Constructs a new instance of neighborhood agent in accordance with the provided information. Additional agent
   capabilities can be specified in the agent configuration file (e.g. Agent.xml).

   @param organization the self organization, containing information about the various personae it should create to
   organization in various affliated organizations.
   @param identifier a string containing the unique name of this agent.
   @param knowledge an XML representation of the agents knowledge of the self organization.
   @param focus an Enum that defines the focus of the organization.
   */
  public FeederGridPersona(final IOrganization organization,
                           final String identifier, final Element knowledge, OrganizationFocus focus) {
    super(organization, identifier, knowledge, focus);
  }

  public FeederGridPersona(final String identifier) {
    super(identifier);
  }

  @Override
  public String toString() {
    return "Feeder Grid Agent";
  }
}
