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
 An {@code AssociatedSmartInverter} describing the specific smart inverter associated with this object.
 */
public class AssociatedSmartInverter implements IAttribute, Serializable {

  private static final long serialVersionUID = 1L;
  private final String associatedSmartInverter;

  /**
   @param associatedSmartInverter - the String name of the associated smart inverter.
   */
  public AssociatedSmartInverter(final String associatedSmartInverter) {
    this.associatedSmartInverter = associatedSmartInverter;
  }

  /**
   @return string name of associated smart inverter
   */
  public String getValue() {
    return this.associatedSmartInverter;
  }

  @Override
  public String toString() {
    return this.associatedSmartInverter;
  }

}
