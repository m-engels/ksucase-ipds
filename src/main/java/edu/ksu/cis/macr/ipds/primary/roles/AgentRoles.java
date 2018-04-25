/**
 * Copyright 2013 Denise Case Kansas State University MACR Laboratory
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
package edu.ksu.cis.macr.ipds.primary.roles;

import edu.ksu.cis.macr.organization.model.identifiers.StringIdentifier;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 A class listing the specified roles in the system.  The string names must exactly match the role names shown on the
 refined goal models and role models.  By convention, the unique identifier names should be exactly the same as the
 name strings.
 */
public class AgentRoles {

  public static final UniqueIdentifier Forecast_Feeder_Role =
          StringIdentifier.getIdentifier("Forecast Feeder Role");
  public static final UniqueIdentifier Forecast_Substation_Role =
          StringIdentifier.getIdentifier("Forecast Substation Role");
  public static final UniqueIdentifier Forecast_Lateral_Role =
          StringIdentifier.getIdentifier("Forecast Lateral Role");
  public static final UniqueIdentifier Forecast_Neighborhood_Role =
          StringIdentifier.getIdentifier("Forecast Neighborhood Role");
  public static final UniqueIdentifier Forecast_Home_Role =
          StringIdentifier.getIdentifier("Forecast Home Role");

  public static final UniqueIdentifier Manage_Substation_Role =
          StringIdentifier.getIdentifier("Manage Substation Role");
  public static final UniqueIdentifier Manage_Feeder_Role =
          StringIdentifier.getIdentifier("Manage Feeder Role");
  public static final UniqueIdentifier Manage_Lateral_Role =
          StringIdentifier.getIdentifier("Manage Lateral Role");
  public static final UniqueIdentifier Manage_Neighborhood_Role =
          StringIdentifier.getIdentifier("Manage Neighborhood Role");
  public static final UniqueIdentifier Manage_Home_Role =
          StringIdentifier.getIdentifier("Manage Home Role");

  public static final UniqueIdentifier Manage_Smart_Meter_Role =
          StringIdentifier.getIdentifier("Manage Smart Meter Role");
  public static final UniqueIdentifier Actuate_Smart_Inverter_Role =
          StringIdentifier.getIdentifier("Actuate Smart Inverter Role");
  public static final UniqueIdentifier Read_Smart_Meter_Role =
          StringIdentifier.getIdentifier("Sense Smart Meter Role");
  public static final UniqueIdentifier Self_Report_Power_Role =
          StringIdentifier.getIdentifier("Self Report Power Role");


  @Override
  public String toString() {
    try {
      final String s = "";
      final Field fields[] = Class.forName(this.getClass().getName())
              .getDeclaredFields();
      for (final Field field : fields) {
        s.concat(field + " ");
      }
      return s.concat(".");
    } catch (ClassNotFoundException ex) {
      Logger.getLogger(AgentRoles.class.getName()).log(Level.SEVERE,
              null, ex);
      return "";
    }

  }
}
