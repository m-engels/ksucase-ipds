/**
 *
 * Copyright 2012 Denise Case Kansas State University MACR Laboratory
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
package edu.ksu.cis.macr.ipds.market;




import edu.ksu.cis.macr.aasis.agent.persona.Organization;
import edu.ksu.cis.macr.obaa_pp.org.DefaultTerminationCriteria;
import edu.ksu.cis.macr.obaa_pp.org.IExecutableOrganization;

/**
 * A class describing the termination criteria for an organization.
 */
public class MarketTerminationCriteria extends DefaultTerminationCriteria {

    private static boolean isDone = false;

    /**
     * Constructor.
     *
     * @param organization - the organization to evaluate
     */
    public MarketTerminationCriteria(final Organization organization) {
        super((IExecutableOrganization) organization);
    }

    /**
     * Determines if the criteria is met and the organization has completed its objectives.
     *
     * @return true if criteria has been met and org. had been completed.d
     */
    public static boolean isIsDone() {

        return isDone;
    }

    /**
     * Sets the value of whether the organization has completed its objectives.
     *
     * @param isDone - true if terminated, false if not.
     */
    public static void setIsDone(final boolean isDone) {
        MarketTerminationCriteria.isDone = isDone;
    }

    @Override
    public boolean isAccomplished() {
        return isDone;
    }
}
