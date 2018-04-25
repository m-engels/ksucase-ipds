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
 The {@code Latitude}
 */
public class LatitudeDegree implements IAttribute, Serializable {

  private static final long serialVersionUID = 1L;
  private final double latitudeDegree;

  /**
   @param latitudeDegree - latitude in degrees to set
   */
  public LatitudeDegree(final double latitudeDegree) {
    this.latitudeDegree = latitudeDegree;
  }

  /**
   @return latitude in decimal degrees
   */
  public double getValue() {
    return this.latitudeDegree;
  }

  @Override
  public String toString() {
    return String.format("%.6f", this.latitudeDegree);
  }

}
