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
 An {@code IAttribute} describing the elevation of this object.
 */
public class ElevationFeet implements IAttribute, Serializable {

  private static final long serialVersionUID = 1L;
  private final double elevationFeet;

  /**
   @param elevationFeet -the elevation in feet above sea level.
   */
  public ElevationFeet(final double elevationFeet) {
    this.elevationFeet = elevationFeet;
  }

  /**
   @return elevation in feet above sea level
   */
  public double getValue() {
    return this.elevationFeet;
  }

  @Override
  public String toString() {
    return String.format("%.2f", this.elevationFeet);
  }

}
