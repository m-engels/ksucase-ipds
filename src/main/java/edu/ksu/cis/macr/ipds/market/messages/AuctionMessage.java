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
package edu.ksu.cis.macr.ipds.market.messages;

import edu.ksu.cis.macr.aasis.agent.cc_message.BaseMessage;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Objects;


public class AuctionMessage extends BaseMessage<AuctionPerformative> implements IAuctionMessage {
    private static final Logger LOG = LoggerFactory.getLogger(AuctionMessage.class);
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new instance of {@code AuctionMessage}.
     *
     * @param sender       - the UniqueIdentifier of the sending agent
     * @param receiver     - the UniqueIdentifier of the receiving agent
     * @param performative - the unique name of the associated messages action on the plan model
     * @param content      - the object containing the custom contents of the messages
     */
    private AuctionMessage(final UniqueIdentifier sender, final UniqueIdentifier receiver,
                           final AuctionPerformative performative, final Object content) {
        super(sender, receiver, performative, content);
        Objects.requireNonNull(content, "Auction Content cannot be null.");
    }

    /**
     * Constructs a new instance of a {@code AuctionMessage}.
     */
    public AuctionMessage() {
        super("", "", AuctionPerformative.BID, null);
        // LOG.debug("Created new ConnectMessage {}", this.toString());
    }

    /**
     * Constructs a new instance of {@code AuctionMessage}.
     *
     * @param sender       - the String name of the sending agent
     * @param receiver     - the String name of the receiving agent
     * @param performative - the unique name of the associated messages action on the plan model
     * @param content      - the object containing the custom contents of the messages
     */
    public AuctionMessage(final String sender, final String receiver,
                          final AuctionPerformative performative, final Object content) {
        super(sender, receiver, performative, content);
        Objects.requireNonNull(content, "Remote Power Message Content cannot be null.");
    }

    /**
     * /** Constructs a new instance of a default {@code AuctionMessage}.
     *
     * @return the {@code IPowerMessage} created
     */
    public static AuctionMessage createAuctionMessage() {
        return new AuctionMessage();
    }

    /**
     * /** Constructs a new instance of {@code AuctionMessage}.
     *
     * @param sender       - String name of the agent sending the message
     * @param receiver     - String name of the agent to whom the message is sent
     * @param performative - the {@code PowerPerformative} indicating the type of message
     * @param content      - the message content
     * @return the IAuctionMessage created
     */
    public static IAuctionMessage createLocal(final UniqueIdentifier sender, final UniqueIdentifier receiver,
                                              final AuctionPerformative performative, final Object content) {
        return new AuctionMessage(sender, receiver, performative, content);
    }

    /**
     * /** Constructs a new instance of {@code AuctionMessage}.
     *
     * @param senderString   - String name of the agent sending the message
     * @param receiverString - String name of the agent to whom the message is sent
     * @param performative   - the {@code PowerPerformative} indicating the type of message
     * @param content        - the message content
     * @return the IAuctionMessage created
     */
    public static IAuctionMessage createRemote(final String senderString, final String receiverString,
                                               final AuctionPerformative performative, final Object content) {
        return new AuctionMessage(senderString, receiverString, performative, content);
    }


    @Override
    public Object deserialize(final byte[] bytes) throws Exception {
        try (ByteArrayInputStream b = new ByteArrayInputStream(bytes)) {
            try (ObjectInput o = new ObjectInputStream(b)) {
                return o.readObject();
            }
        }
    }

    @Override
    public byte[] serialize() throws IOException {
        try (ByteArrayOutputStream b = new ByteArrayOutputStream()) {
            try (ObjectOutput o = new ObjectOutputStream(b)) {
                o.writeObject(this);
            }
            return b.toByteArray();
        }
    }


}
