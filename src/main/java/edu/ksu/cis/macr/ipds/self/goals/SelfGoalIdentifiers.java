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
package edu.ksu.cis.macr.ipds.self.goals;

import edu.ksu.cis.macr.organization.model.identifiers.StringIdentifier;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;

import java.lang.reflect.Field;

/**
 A singleton enum GoalIdentifiers class mapping all goals shown on the refined goal models to associated {@code UniqueIdentifier}s.  The string names must exactly match the goal names shown on the refined goal models and role
 models.  By convention, the unique identifier names should be exactly the same as the name strings.
 */
public enum SelfGoalIdentifiers {
  INSTANCE;

    /**
     * The starting goal a self agent gets to begin establishing all authorized connections.
     */
    public static final UniqueIdentifier Self_Control = StringIdentifier.getIdentifier(
            "Self Control");

    /**
     * The starting for a worker sub agent - might be assigned to sensors, actuators, calculations, etc.
     */
    public static final UniqueIdentifier Manage = StringIdentifier.getIdentifier(
            "Manage");


    /**
     * The top goal in all goal models.
     */
    public static final UniqueIdentifier Succeed = StringIdentifier.getIdentifier(
            "Succeed");



  /**
   The goal to auction power.
   */
  public static final UniqueIdentifier Auction_Power = StringIdentifier.getIdentifier(
          "Auction Power");


  /**
   The goal to provide forecasts for a given 3-phase feeder participant including weather, generation, and load.
   */
  public static final UniqueIdentifier Forecast_Feeder = StringIdentifier.getIdentifier(
          "Forecast Feeder");

  /**
   The goal to provide forecasts for a given single-phase lateral participant including weather, generation, and load.
   */
  public static final UniqueIdentifier Forecast_Lateral = StringIdentifier.getIdentifier(
          "Forecast Lateral");

  /**
   The goal to provide forecasts for a given neighborhood transformer participant including weather, generation, and
   load.
   */
  public static final UniqueIdentifier Forecast_Neighborhood = StringIdentifier.getIdentifier(
          "Forecast Neighborhood");

  /**
   The goal to provide forecasts for a given home participant including weather, generation, and load.
   */
  public static final UniqueIdentifier Forecast_Home = StringIdentifier.getIdentifier(
          "Forecast Home");


  /**
   The goal for a participating feeder to monitor its own power consumption, negotiate locally, report to the local
   supervisor, and employ any assigned control equipment to meet the provided guidelines.  Also:  The goal to
   monitor and allocate the combined power characteristics among the immedidate lateral organization participants and to
   use any available control equipment to support the local organization.  Supervising laterals may also include the
   supervision of other feeders.
   */
  public static final UniqueIdentifier Manage_Feeder = StringIdentifier.getIdentifier(
          "Manage Feeder");

  /**
   The goal for a participating lateral to monitor its own power consumption, negotiate locally, report to the local
   supervisor, and employ any assigned control equipment to meet the provided guidelines.  Also:  The goal to
   monitor and allocate the combined power characteristics among the immedidate neighborhood organization participants
   and to use any available control equipment to support the local organization.
   */
  public static final UniqueIdentifier Manage_Lateral = StringIdentifier.getIdentifier(
          "Manage Lateral");

  /**
   The goal for a participating neighborhood transformer to monitor its own power consumption, negotiate locally, report
   to the local supervisor, and employ any assigned control equipment to meet the provided guidelines.  Also:
   The goal to monitor and allocate the combined power characteristics among the local prosumer participants and to use
   any available control equipment to support the local organization.
   */
  public static final UniqueIdentifier Manage_Neighborhood = StringIdentifier.getIdentifier(
          "Manage Neighborhood");

  /**
   The goal for a participating prosumer to monitor its own power consumption, negotiate locally, report to the local
   supervisor, and employ any assigned control equipment (e.g. local distributed generation such as home photovoltaic
   systems) to meet the provided guidelines.
   */
  public static final UniqueIdentifier Manage_Home = StringIdentifier.getIdentifier(
          "Manage Home");

  /**
   The goal to monitor and allocate the combined power characteristics among the immedidate feeder organization
   participants and to use any available control equipment to support the local organization, e.g. load tap changers.
   */
  public static final UniqueIdentifier Manage_Substation = StringIdentifier.getIdentifier
          ("Manage Substation");


  /**
   The goal to administer a holonic organization that typically has a 3-phase feeder line as a superholon with
   single-phase lateral children as immediate holons.
   */
  public static final UniqueIdentifier Administer_Feeder = StringIdentifier.getIdentifier(
          "Administer Feeder");

  /**
   The goal to administer a holonic organization that typically has a single-phase lateral line as a superholon and
   neighborhood transformer children as immediate holons.
   */
  public static final UniqueIdentifier Administer_Lateral = StringIdentifier.getIdentifier(
          "Administer Lateral");

  /**
   The goal to administer a holonic organization that typically has a neighborhood transformer as the superholon and
   pv-enabled homes or other prosumers as immediate holons.
   */
  public static final UniqueIdentifier Administer_Neighborhood = StringIdentifier.getIdentifier(
          "Administer Neighborhood");

  /**
   The goal to administer a home organization that typically includes a forecaster.
   */
  public static final UniqueIdentifier Administer_Home = StringIdentifier.getIdentifier(
          "Administer Home");

  /**
   The goal to administer a holonic organization that typically has a single substation as the top-level superholon with
   3-phase feeders as immediate holons.
   */
  public static final UniqueIdentifier Administer_Substation = StringIdentifier.getIdentifier
          ("Administer Substation");


  /**
   The goal to work as a recursive sub holon in a holonic organization.
   */
  public static final UniqueIdentifier Be_Holon = StringIdentifier.getIdentifier(
          "Be Holon");

  /**
   The goal to work as a recursive sub holon in a holonic organization.
   */
  public static final UniqueIdentifier Be_Sub_Feeder_Holon = StringIdentifier.getIdentifier(
          "Be Sub Feeder Holon");


  /**
   The goal to work as a recursive sub holon in a holonic organization.
   */
  public static final UniqueIdentifier  Be_Sub_Lateral_Holon = StringIdentifier.getIdentifier(
          "Be Sub Lateral Holon");
  /**
   The goal to work as a recursive super holon in a holonic organization.
   */
  public static final UniqueIdentifier Be_Super_Holon = StringIdentifier.getIdentifier(
          "Be Super Holon");

  /**
   The goal to manage a smart meter.
   */
  public static final UniqueIdentifier Manage_Smart_Meter = StringIdentifier.getIdentifier(
          "Manage Smart Meter");

  /**
   The goal to manage an authorized smart inverter.
   */
  public static final UniqueIdentifier Actuate_Smart_Inverter = StringIdentifier.getIdentifier(
          "Actuate Smart Inverter");

  /**
   The goal to regularly read from an authorized smart meter sensor.
   */
  public static final UniqueIdentifier Sense_Smart_Meter = StringIdentifier.getIdentifier(
          "Sense Smart Meter");

  /**
   The goal to report smart meter sensor values to the self for evaluation and forwarding.
   */
  public static final UniqueIdentifier Self_Report_Power = StringIdentifier.getIdentifier(
          "Self Report Power");



  @Override
  public String toString() {
    try {
      final Field fields[] = Class.forName(this.getClass().getName()).getDeclaredFields();
      final String s = "";
      for (final Field field : fields) {
        s.concat(field + " ");
      }
      return s.concat(".");
    } catch (ClassNotFoundException e) {
      return "";
    } catch (SecurityException e) {
      return "";
    }
  }

}
