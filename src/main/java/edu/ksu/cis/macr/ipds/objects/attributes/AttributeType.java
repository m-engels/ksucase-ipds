/**
 *
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

/**
 The {@code AttributeType}
 */
public enum AttributeType {


  /**
   The associated PV system.
   */
  AssociatedPVSystem,

  /**
   The associated smart inverter.
   */
  AssociatedSmartInverter,

  /**
   The capacity of this generator in kilowatts.
   */
  CapacityKW,


  /**
   The critical load that is requested at all times in kilowatts.
   */
  CriticalLoadKW,

  /**
   The elevation of this object in feet.
   */
  ElevationFeet,

  /**
   The latitude of this object in degrees.
   */
  LatitudeDegree,

  /**
   The longitude of this object in degrees.
   */
  LongitudeDegree,

  /**
   The maximum number of load tap setting changes that can be made in one 24-hour day (12 a.m. to 11:59 p.m.)
   */
  MaxDailyLoadTapChanges
}

	

	
