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
package edu.ksu.cis.macr.ipds.objects.attributes;

import edu.ksu.cis.macr.obaa_pp.objects.IAttribute;

import java.io.Serializable;


/**
 An {@code IAttribute} describing the maximum number of load tap changes the associated load tap changer can make in a
 day, typically 26.
 */
public class MaxDailyLoadTapChanges implements IAttribute, Serializable {

  private static final long serialVersionUID = 1L;
  private final int maxDailyLoadTapChanges;

  /**
   @param maxDailyLoadTapChanges - max amount of load tap changes that can be made daily.
   */
  public MaxDailyLoadTapChanges(final int maxDailyLoadTapChanges) {
    this.maxDailyLoadTapChanges = maxDailyLoadTapChanges;
  }

  /**
   @return elevation in feet above sea level
   */
  public int getValue() {
    return this.maxDailyLoadTapChanges;
  }

  @Override
  public String toString() {
    return String.format("%s", this.maxDailyLoadTapChanges);
  }

}
