/*
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 The {@code LoadTapChangerControlCapability} provides the ability to control a specific load tap changer device.
 */
public class LoadTapChangerControlCapability extends AbstractOrganizationCapability {

  private static final Logger LOG = LoggerFactory.getLogger(LoadTapChangerControlCapability.class);


  /**
   @param owner - the entity to which this capability belongs
   @param org - the organization in which this entity is participating
   */
  public LoadTapChangerControlCapability(final IPersona owner, final IOrganization org) {
    super(LoadTapChangerControlCapability.class, owner, org);

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

}
