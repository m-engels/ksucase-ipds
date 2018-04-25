/**
 *
 * Copyright 2012 Denise Case
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
 An {@code IAttribute} describing the maximum generation capacity of this object in kW.
 */
public class CapacityKW implements IAttribute, Serializable {
  private static final long serialVersionUID = 1L;
  private final double capacityKW;

  /**
   *
   */
  public CapacityKW() {
    this.capacityKW = 0.;
  }

  /**
   @param capacityKW - the total capacity of this item in KW
   */
  public CapacityKW(final double capacityKW) {
    this.capacityKW = capacityKW;
  }

  /**
   @return capacity in KW
   */
  public double getValue() {
    return capacityKW;
  }

  @Override
  public String toString() {
    return String.format("%.2f", capacityKW);
  }

}
