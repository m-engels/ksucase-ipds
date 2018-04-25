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


import java.io.*;


/**
 * The {@code AuctionMessageContent} provides information needed for power auction messages. capabilities.
 */
public class AuctionMessageContent implements Serializable, IAuctionMessageContent {

    private static final long serialVersionUID = 1L;

    private final long purchaseTimeSlice;
    private final String broker;
    private final IBid bid;


    private AuctionMessageContent(long purchaseTimeSlice, String broker, IBid bid) {
        this.purchaseTimeSlice = purchaseTimeSlice;
        this.broker = broker;
        this.bid = bid;
    }

    /**
     * Create auction message content.
     *
     * @param purchaseTimeSlice - the time slice when the power will be used.
     * @param broker            - the unique name of the agent brokering this auction
     * @param bid               - the auction bid
     * @return a new Auction Message content.
     */
    public static AuctionMessageContent create(long purchaseTimeSlice, String broker, IBid bid) {
        return new AuctionMessageContent(purchaseTimeSlice, broker, bid);
    }

    public String getBroker() {
        return broker;
    }

    public IBid getBid() {
        return bid;
    }

    /**
     * Deserialize the message.
     *
     * @param bytes - an array of bytes
     * @return the deserialized {@code Message}
     * @throws Exception - if an exception occurs.
     */
    @Override
    public Object deserialize(final byte[] bytes) throws Exception {
        try (ByteArrayInputStream b = new ByteArrayInputStream(bytes)) {
            try (ObjectInput o = new ObjectInputStream(b)) {
                return o.readObject();
            }
        }
    }


    @Override
    public long getPurchaseTimeSlice() {
        return purchaseTimeSlice;
    }


    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
    }

    @Override
    public String toString() {
        return "AuctionMessageContent{" +
                "purchaseTimeSlice=" + purchaseTimeSlice +
                ", broker='" + broker + '\'' +
                ", bid=" + bid +
                '}';
    }

    /**
     * Serialize the message.
     *
     * @return a byte array with the contents.
     * @throws IOException - If an I/O error occurs.
     */
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
