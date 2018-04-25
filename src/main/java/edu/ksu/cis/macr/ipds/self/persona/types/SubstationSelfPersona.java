/**
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
package edu.ksu.cis.macr.ipds.self.persona.types;

import edu.ksu.cis.macr.aasis.agent.persona.IOrganization;
import edu.ksu.cis.macr.aasis.spec.OrganizationFocus;
import edu.ksu.cis.macr.ipds.self.persona.SelfPersona;
import org.w3c.dom.Element;

/**
 A feeder agent represents a main feeder line coming off a substation. It may both remoteRECEIVE an electrical load and may
 also provide generation to the system. It is implemented as a holonic agent that may act as both an agent within a local
 organization, and as an organization itself.  A feeder agent may be selected to perform a supervisory role for a set
 of peers and represent the peer set in a higher-level organization.
 */
public class SubstationSelfPersona extends SelfPersona {

  /**
   Constructs a new instance of substation agent in accordance with the provided information. Additional agent
   capabilities can be specified in the agent configuration file (e.g. Agent.xml).

   @param organization the SelfOrganization organization, containing information about agents and objects in the
   SelfOrganization system.
   @param identifier a string containing the unique name of this agent.
   @param knowledge an XML representation of the agents knowledge of the SelfOrganization and the organization.
   @param focus the enum that contains what kind of focus the object is (Agent or External).
   */
  public SubstationSelfPersona(final IOrganization organization,
                               final String identifier, final Element knowledge, OrganizationFocus focus) {
    super(organization, identifier, knowledge, focus);
  }

  public SubstationSelfPersona(final String identifier) {
    super(identifier);
  }

  @Override
  public String toString() {
    return "Substation Self Agent";
  }

}
