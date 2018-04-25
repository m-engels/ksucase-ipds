/**
 *
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
package edu.ksu.cis.macr.ipds.market.guidelines;

/**
 * An enumeration of the guideline types.  Each type name should exactly match the corresponding class name for the
 * actuator setting object.  There may be other organizations
 */
public enum GuidelineType {

    /**
     * A broker gets guidelines about the time and authorized participants for power auctions.
     */
    Market_Broker,

    /*
    An auction participant gets information about pricing strategies to use when bidding in auctions.
     */
    Market_Auction

}
