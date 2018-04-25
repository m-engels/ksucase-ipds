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
package edu.ksu.cis.macr.ipds.objects.attributes;

import edu.ksu.cis.macr.obaa_pp.objects.IAttribute;

import java.io.Serializable;


/**
 An {@code IAttribute} describing the specific photovoltaic (PV) system associated with this object.
 */
public class AssociatedPVSystem implements IAttribute, Serializable {

  private static final long serialVersionUID = 1L;
  private final String associatedPVSystem;

  /**
   @param associatedPVSystem - the PV system associated with this smart meter.
   */
  public AssociatedPVSystem(final String associatedPVSystem) {
    this.associatedPVSystem = associatedPVSystem;
  }

  /**
   @return string name of associated photovoltaic system
   */
  public String getValue() {
    return this.associatedPVSystem;
  }

  @Override
  public String toString() {
    return this.associatedPVSystem;
  }

}
