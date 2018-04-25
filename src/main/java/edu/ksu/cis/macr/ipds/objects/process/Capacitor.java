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
package edu.ksu.cis.macr.ipds.objects.process;

import edu.ksu.cis.macr.ipds.objects.attributes.ElevationFeet;
import edu.ksu.cis.macr.ipds.objects.attributes.LatitudeDegree;
import edu.ksu.cis.macr.ipds.objects.attributes.LongitudeDegree;
import edu.ksu.cis.macr.obaa_pp.objects.AbstractObject;

/**
 A device used to adjust the voltage along electrical lines.
 */
public class Capacitor extends AbstractObject {

  /**
   @param identifierString the string name of this unique instance of this type of object
   */
  public Capacitor(String identifierString) {
    super(identifierString);

    addAttribute(new ElevationFeet(1.0));
    addAttribute(new LatitudeDegree(1.0));
    addAttribute(new LongitudeDegree(1.0));


  }

}
