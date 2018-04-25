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
 A singleton enum GoalParameters class mapping all goal parameters shown on the refined goal models to associated {@code UniqueIdentifier}s.  The string names must exactly match the variable names (not types) of the goal parameters shown
 on the refined goal models.  By convention, the unique identifier names should be exactly the same as the name
 strings.
 */
public enum SelfGoalParameters {
  INSTANCE;



    //TODO: separate guideline identifiers by organization
  /**
   The auction guidelines for participating in a power auction.
   */
  public static final UniqueIdentifier auctionGuidelines =
          StringIdentifier.getIdentifier("auctionGuidelines");

    /**
     The auction guidelines for brokering a power auction.
     */
    public static final UniqueIdentifier brokerGuidelines =
            StringIdentifier.getIdentifier("brokerGuidelines");

  /**
   The combined load guidelines for a local organization.
   */
  public static final UniqueIdentifier combinedLoadGuidelines =
          StringIdentifier.getIdentifier("combinedLoadGuidelines");

  /**
   The load guidelines for a single participant in a local organization.
   */
  public static final UniqueIdentifier loadGuidelines = StringIdentifier
          .getIdentifier("loadGuidelines");

  /**
   The combined power quality guidelines for a local organization.
   */
  public static final UniqueIdentifier powerQualityGuidelines =
          StringIdentifier.getIdentifier
                  ("powerQualityGuidelines");

  /**
   The combined evaluation guidelines for a local organization.
   */
  public static final UniqueIdentifier evaluationGuidelines =
          StringIdentifier.getIdentifier("evaluationGuidelines");

  /**
   The guidelines for all authorized grid control connections for this agent.
   */
  public static final UniqueIdentifier gridConnections =
          StringIdentifier.getIdentifier("gridConnections");

  /**
   The guidelines for all authorized power market auction connections for this broker agent.
   */
  public static final UniqueIdentifier auctionConnections =
          StringIdentifier.getIdentifier("auctionConnections");

    /**
     The guidelines for all authorized power market broker connections for this auction agent.
     */
    public static final UniqueIdentifier brokerConnections =
            StringIdentifier.getIdentifier("brokerConnections");

    /**
     The guidelines for all authorized power market broker connections for this auction agent.
     */
    public static final UniqueIdentifier marketConnections =
            StringIdentifier.getIdentifier("marketConnections");


    /**
   The guidelines for all parent connections for this agent used when acting as a sub holon.
   */
  public static final UniqueIdentifier parentConnections =
          StringIdentifier.getIdentifier("parentConnections");

  /**
   The guidelines for all parent connections for this agent used when acting as a super holon.
   */
  public static final UniqueIdentifier childConnections = StringIdentifier.getIdentifier("childConnections");


  /**
   The connection guidelines for a single agent-to-agent connection.
   */
  public static final UniqueIdentifier connectionGuidelines =
          StringIdentifier.getIdentifier("connectionGuidelines");

  /**
   The goal parameters needed to dynamically customize the behavior of a specific potentially PV-enabled home.
   */
  public static final UniqueIdentifier homeGuidelines =
          StringIdentifier.getIdentifier
                  ("homeGuidelines");

  /**
   The goal parameters needed to dynamically customize the behavior of a specific neighborhood transformer.
   */
  public static final UniqueIdentifier neighborhoodGuidelines =
          StringIdentifier.getIdentifier
                  ("neighborhoodGuidelines");

  /**
   The goal parameters needed to dynamically customize the behavior of a specific single-phase lateral line.
   */
  public static final UniqueIdentifier lateralGuidelines =
          StringIdentifier.getIdentifier
                  ("lateralGuidelines");

  /**
   The goal parameters needed to dynamically customize the behavior of a specific feeder.
   */
  public static final UniqueIdentifier feederGuidelines =
          StringIdentifier.getIdentifier
                  ("feederGuidelines");

  /**
   The goal parameters needed to dynamically customize the behavior of a specific substation.
   */
  public static final UniqueIdentifier substationGuidelines =
          StringIdentifier.getIdentifier
                  ("substationGuidelines");


  /**
   The goal parameters needed to dynamically customize the behavior of a specific potentially PV-enabled home.
   */
  public static final UniqueIdentifier homeForecasterGuidelines =
          StringIdentifier.getIdentifier
                  ("homeForecasterGuidelines");



  @Override
  public String toString() {
    try {
      Field fields[] = Class.forName(
              this.getClass().getName()).getDeclaredFields();
      String s = "";
      for (Field field : fields) s.concat(field + " ");
      return s.concat(".");
    } catch (Exception e) {
      return "";
    }
  }
}
