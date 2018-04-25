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
package edu.ksu.cis.macr.ipds.market.goals;

import edu.ksu.cis.macr.organization.model.identifiers.StringIdentifier;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;

import java.lang.reflect.Field;

/**
 * A singleton enum GoalParameters class mapping all goal parameters shown on the refined goal models to associated {@code UniqueIdentifier}s.  The string names must exactly match the variable names (not types) of the goal parameters shown
 * on the refined goal models.  By convention, the unique identifier names should be exactly the same as the name
 * strings.
 */
public enum MarketGoalParameters {
    INSTANCE;

    /**
     * The auction guidelines for participating in a power auction.
     */
    public static final UniqueIdentifier auctionGuidelines =
            StringIdentifier.getIdentifier("auctionGuidelines");

    /**
     * The auction guidelines for brokering a power auction.
     */
    public static final UniqueIdentifier brokerGuidelines =
            StringIdentifier.getIdentifier("brokerGuidelines");


    /**
     * The guidelines for all authorized auction connections for this agent.
     */
    public static final UniqueIdentifier auctionConnections =
            StringIdentifier.getIdentifier("auctionConnections");

    /**
     * The guidelines for all authorized broker connections for this agent.
     */
    public static final UniqueIdentifier brokerConnections =
            StringIdentifier.getIdentifier("brokerConnections");

    /**
     * The guidelines for all market connections for this agent.
     */
    public static final UniqueIdentifier marketConnections =
            StringIdentifier.getIdentifier("marketConnections");


    /**
     * The connection guidelines for a single agent-to-agent connection.
     */
    public static final UniqueIdentifier connectionGuidelines =
            StringIdentifier.getIdentifier("connectionGuidelines");


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
