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
package edu.ksu.cis.macr.ipds.objects.process;

/**
 An enumeration of the various types of real-world objects that operate in the organizations.  Type names used in the
 organization configuration file must exactly match one of these options.  Each type name should exactly match the
 corresponding class name for the object type.
 */
public enum ObjectType {

  /**
   A device used to adjust voltages along a power distribution line - a mid-level control option affecting all
   neighborhood transformers and associated homes downstream of the capacitor.
   */
  Capacitor,
  /**
   A device used to change the tapping connection of a transformer. High-level control option - changing tap settings
   affect the entire distribution system.
   */
  LoadTapChanger,

  /**
   A device that can generate electricity from sunlight. The device may also remoteRECEIVE a small amount of electricity when
   sunlight is not available.
   */
  PhotoVoltaicSystem,

  /**
   A device used to control the flow and quality of electrical power.
   */
  SmartInverter,

  /**
   A device that records the power consumption and production for a prosumer. May include information about critical
   loads that must be available if at all possible. May include location information such as longitude, latitude, and
   altitude.
   */
  SmartMeter,

  /**
   A substation defines the most upstream (closest to the main grid) point in the distribution system and in the
   SelfOrganization system. The substation typically provides power to approximately 5-7 feeder lines.
   */
  Substation
}
