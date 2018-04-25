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
package edu.ksu.cis.macr.ipds.market.capabilities.participate;

import edu.ksu.cis.macr.aasis.agent.persona.ICapability;
import edu.ksu.cis.macr.aasis.agent.persona.IInternalCommunicationCapability;
import edu.ksu.cis.macr.aasis.common.IConnections;
import edu.ksu.cis.macr.ipds.market.guidelines.auction.IAuctionGuidelines;
import edu.ksu.cis.macr.ipds.market.messages.IAuctionMessage;
import edu.ksu.cis.macr.organization.model.InstanceGoal;

/**
 * Defines auction-related communication and processing capabilities.
 */
public interface IAuctionPowerCapability extends ICapability, IInternalCommunicationCapability.ICommunicationChannel {

    IConnections getParentConnections();

    /**
     * Used to create an Auction message to send to the auction exchange.
     *
     * @param currentTimeSlice - the long that is the current time slice.
     * @return - Auction Message that is just now created.
     */
    IAuctionMessage createAuctionMessage(long currentTimeSlice);

    /**
     * Get the auction guidelines.
     * @return the auction guidelines
     */
    IAuctionGuidelines getAuctionGuidelines();

    /**
     * @return - the communication channel string associated with this communication capability.
     */
    String getCommunicationChannelID();

    /**
     * Get the parameters from this instance goal and use them to set the goal-specific guidelines.
     *
     * @param ig - the instance goal with the behavior information
     */
    void init(InstanceGoal<?> ig);

    /**
     * Get the number of messages.
     * @return - the number of {@code IAuctionMessage} on this local messages queue
     */
    int messages();

    /**
     * Receive a message.
     * @return {@code AuctionMessage} received
     */
    IAuctionMessage receive();

    /**
     * Indicates whether this auction is still open (hasn't been executed yet).
     * @param currentTimeSlice - the long that is the current time slice.
     * @return true if open, false if executed.
     */
    boolean isOpen(long currentTimeSlice);
}