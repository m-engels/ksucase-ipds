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
 An {@code IAttribute} describing the critical load in kW that must be available for this object if at all possible.
 */
public class CriticalLoadKW implements IAttribute, Serializable {

  private static final long serialVersionUID = 1L;
  private final double criticalLoadKW;

  /**
   *
   */
  public CriticalLoadKW() {
    this.criticalLoadKW = 0.;
  }

  /**
   @param criticalLoadKW - total critical load
   */
  public CriticalLoadKW(final double criticalLoadKW) {
    this.criticalLoadKW = criticalLoadKW;
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof CriticalLoadKW) {
      CriticalLoadKW obj = (CriticalLoadKW) object;
      return obj.getValue() == criticalLoadKW;
    }
    return false;
  }

  /**
   @return critical load in kW
   */
  public double getValue() {
    return criticalLoadKW;
  }

  @Override
  public String toString() {
    return String.format("%.2f", criticalLoadKW);
  }

}
