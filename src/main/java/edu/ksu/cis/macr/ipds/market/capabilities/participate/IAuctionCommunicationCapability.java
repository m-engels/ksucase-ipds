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
import edu.ksu.cis.macr.ipds.market.messages.IAuctionMessage;
import edu.ksu.cis.macr.ipds.market.messages.IAuctionMessageContent;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;

import java.util.TreeMap;

/**
 * Provides an interface for defining market-related communication capabilities.
 */
public interface IAuctionCommunicationCapability extends ICapability, IInternalCommunicationCapability.ICommunicationChannel {

    /**
     * @param ownerSelfPersona - the owner of the self Persona
     * @return string containing the admin
     */
    IAuctionMessage checkFromAdmin(String ownerSelfPersona);

    /**
     * @return an Auction message from itself
     */
    IAuctionMessage checkFromSelf();

    /**
     * @param localPowerMessage - the local power message being sent
     * @param subIdentifier     - the partial identifier to who the message is sent
     * @return - true if message successfully sent
     */
    boolean forwardToParticipant(IAuctionMessage localPowerMessage, UniqueIdentifier subIdentifier);

    /**
     * @param content - the content of the message
     * @return - true if message successfully sent
     */
    boolean forwardToSelf(IAuctionMessageContent content);

    /**
     * @return Map of the Bid messages
     */
    TreeMap<String, IAuctionMessageContent> getBidMessages();

    boolean getBidResponse();

    /**
     * @return the {@code ICommunicationChannel} this {@code Capability} uses.
     */
    IInternalCommunicationCapability.ICommunicationChannel getCommunicationChannel();

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
     * Get the parameters from this instance goal and use them to set the goal-specific guidelines for any child
     * connections.
     *
     * @param instanceGoal - the instance goal provided
     */
    void initializeChildConnections(InstanceGoal<?> instanceGoal);

    /**
     * @param instanceGoal - the goal to be initialized to the parent
     */
    void initializeParentConnections(InstanceGoal<?> instanceGoal);

    /**
     * @return - the number of {@code IAuctionMessage} on this local messages queue
     */
    int messages();

    /**
     * @return {@code AuctionMessage} received
     */
    IAuctionMessage receive();

    /**
     * @param message - the AuctionMessage messages to be sent.
     * @return {@code true} if the messages was sent, {@code false} otherwise.
     */
    boolean send(IAuctionMessage message);

    /**
     * Send an auction message to an agent in this local organization.
     *
     * @param message - the message to send.
     * @return true if successfully sent, false if not successfully sent.
     */
    boolean sendLocal(IAuctionMessage message);

    /**
     * Send an auction message to an agent in a different organization.
     *
     * @param message - the message to send
     * @return true if successfully sent, false if not successfully sent.
     */
    boolean sendRemoteMessage(IAuctionMessage message);

    /**
     * Send aggregate bid up to the next highest tier.
     *
     * @param localMessage  - the aggregate local bid message
     * @param upConnections - the list of upward connections (typically only one).
     */
    void sendUp(IAuctionMessage localMessage, IConnections upConnections);

    /**
     * Is this auction the top-level auction in a multi-tier auction.
     *
     * @return - true if top-tier (no higher levels), false if this is not the highest tier in the auction.
     */
    boolean isTopTier();

    /**
     * Send down the results of an executed (brokered) auction to the participants.
     *
     * @param originalRemoteMessage - the original message received for review
     * @return true if all messages were sent successfully, false if not
     */
    boolean sendDownResults(IAuctionMessage originalRemoteMessage);
}