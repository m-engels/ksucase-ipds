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

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import edu.ksu.cis.macr.aasis.agent.persona.AbstractOrganizationCapability;
import edu.ksu.cis.macr.aasis.agent.persona.IOrganization;
import edu.ksu.cis.macr.aasis.agent.persona.IPersona;
import edu.ksu.cis.macr.aasis.common.IConnections;
import edu.ksu.cis.macr.aasis.messaging.IMessagingFocus;
import edu.ksu.cis.macr.aasis.messaging.MessagingReliabilityManager;
import edu.ksu.cis.macr.goal.model.InstanceParameters;
import edu.ksu.cis.macr.ipds.market.goals.MarketGoalParameters;
import edu.ksu.cis.macr.ipds.market.guidelines.auction.IAuctionGuidelines;
import edu.ksu.cis.macr.ipds.market.messages.*;
import edu.ksu.cis.macr.ipds.market.messaging.MarketMessagingFocus;
import edu.ksu.cis.macr.ipds.market.messaging.MarketMessagingManager;
import edu.ksu.cis.macr.obaa_pp.objects.IDisplayInformation;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Objects;

/**
 */
public class AuctionPowerCapability extends AbstractOrganizationCapability implements IAuctionPowerCapability {
    private static final Logger LOG = LoggerFactory.getLogger(AuctionPowerCapability.class);
    private static final boolean debug = false;
    private static final String COMMUNICATION_CHANNEL_ID = "AuctionPowerChannel";
    private static final IMessagingFocus messagingFocus = MarketMessagingFocus.MARKET;
    private static Channel channel;
    double communicationReliability = 1.0;
    double communicationDelay = 0.0;
    private IAuctionGuidelines auctionGuidelines;
    private IConnections connections;
    private IConnections childConnections;
    private IConnections parentConnections;
    private QueueingConsumer consumer;


    /**
     * Constructs a new instance of {@code PowerCommunication}.
     *
     * @param owner        - the entity to which this capability belongs.
     * @param organization - the {@code Organization} in which this {@code IAgent} acts.
     */
    public AuctionPowerCapability(IPersona owner, IOrganization organization) {
        super(IAuctionPowerCapability.class, owner, organization);
        channel = MarketMessagingManager.getChannel(messagingFocus);
        consumer = new QueueingConsumer(Objects.requireNonNull(channel, "Error null channel in receive()."));
        initializeReliabilityAndDelay();

    }
    /**
     * @param owner        - the entity to which this capability belongs.
     * @param organization - the {@code Organization} in which this {@code IAgent} acts.
     * @param Parameter    - additional custom parameter
     */
    public AuctionPowerCapability(IPersona owner, IOrganization organization, String Parameter) {
        super(IAuctionPowerCapability.class, owner, organization);
        this.setOwner(Objects.requireNonNull(owner));
        channel = MarketMessagingManager.getChannel(messagingFocus);
        initializeReliabilityAndDelay();
    }

    private void initializeReliabilityAndDelay() {
        try {
            this.communicationReliability = MessagingReliabilityManager.getCommunicationReliability();
            if (debug) LOG.debug("\t New  communicationReliability.");
            communicationDelay = MessagingReliabilityManager.getCommunicationDelay();
        } catch (Exception e) {
            // just use the defaults
            communicationDelay = 0.0;
            communicationReliability = 1.0;
        }
        if (debug)
            LOG.debug("New comm cap with reliability = {} and delay = {}", communicationReliability, communicationDelay);
    }

    public IConnections getChildConnections() {
        return childConnections;
    }

    public void setChildConnections(IConnections childConnections) {
        this.childConnections = childConnections;
    }

    @Override
    public IConnections getParentConnections() {
        return this.parentConnections;
    }

    public void setParentConnections(final IConnections parentConnections) {
        this.parentConnections = parentConnections;
        LOG.debug("Set parent connections to broker. {}", this.parentConnections.toString());
    }

    /**
     * The {@code content} that will be channeled by extensions.
     *
     * @param content the {@code content} to be passed along the {@code ICommunicationChannel}.
     */
    @Override
    public void channelContent(final Object content) {
    }

    /**
     * Used to create an Auction message to send to the auction exchange.
     *
     * @return Auction message of the newly created message.
     */
    @Override
    public IAuctionMessage createAuctionMessage(long timeSlice) {
        if (debug) LOG.debug("Creating auction message.");

        double bPrice = this.auctionGuidelines.getDesiredBuyPrice_centsperkWh();
        if (debug) LOG.debug("  Buy price = {}", bPrice);

        double sPrice = this.auctionGuidelines.getDesiredSellPrice_centsperkWh();
        if (debug) LOG.debug("  Sell price = {}", sPrice);

        double qty = this.auctionGuidelines.getkWh();
        if (debug) LOG.debug("  qty = {}", qty);

        BidDirection direction = BidDirection.BUY;
        double price = bPrice;
        if (this.auctionGuidelines.getIsSell() != 0) {
            direction = BidDirection.SELL;
            price = sPrice;
        }
        if (debug) LOG.debug(" bidDirection = {}", direction);
        if (debug) LOG.debug(" bidPrice = {}", price);
        String bidder = this.owner.getUniqueIdentifier().toString();
        if (debug) LOG.debug(" bidder = {}", bidder);

        // create a bid
        IBid bid = Bid.createBid(qty, price, BidStatus.UNDETERMINED, direction, bidder);
        if (debug) LOG.debug(" new bid = {}", bid);

        String broker = this.parentConnections.getListConnectionGuidelines().get(0).getExpectedMasterAbbrev();
        if (debug) LOG.debug(" broker = {}", broker);

        // add the broker and the timeslice to make message content
        IAuctionMessageContent content = AuctionMessageContent.create(timeSlice, broker, bid);
        if (debug) LOG.debug(" bid content = {}", content);

        // create the remote auction message and return it
        IAuctionMessage msg = AuctionMessage.createRemote(this.owner.getUniqueIdentifier().toString(), broker, AuctionPerformative.BID, content);
        if (debug) LOG.debug(" bid message = {}", msg);
        return msg;
    }

    @Override
    public  IAuctionGuidelines getAuctionGuidelines() {
        return this.auctionGuidelines;
    }

    public  void setAuctionGuidelines(final IAuctionGuidelines auctionGuidelines) {
        this.auctionGuidelines = auctionGuidelines;
    }

    /**
     * @return - the communication channel string associated with this communication capability.
     */
    @Override
    public String getCommunicationChannelID() {
        return COMMUNICATION_CHANNEL_ID;
    }

    /**
     * Get a failure rate between the MIN_FAILURE and MAX_FAILURE rates.
     *
     * @return - the failure value.
     */
    @Override
    public  synchronized double getFailure() {
        return 0;
    }

    public synchronized IPersona getOwner() {
        return owner;
    }

    public synchronized void setOwner(IPersona owner) {
        this.owner = owner;
    }

    /**
     * Get the parameters from this instance goal and use them to set the goal-specific guidelines.
     *
     * @param ig - the instance goal with the behavior information
     */
    @Override
    public synchronized void init(InstanceGoal<?> ig) {
        if (debug) LOG.debug("Initializing AuctionPowerCapability guidelines from goal: {}.", ig);

        // Get the parameter values from the existing active instance goal
        InstanceParameters params = Objects.requireNonNull((InstanceParameters) ig.getParameter());
        if (debug)
            LOG.info("Initializing AuctionPowerCapability with the given goal parameter guidelines: {}.", params);
        if (params == null) {
            LOG.error("Error: we need goal parameters to guide the auction communiction. ");
            System.exit(-4);
        }
        this.setAuctionGuidelines(IAuctionGuidelines.extractAuctionGuidelines(params));
        if (this.auctionGuidelines == null) {
            if (debug) LOG.info("Auction guidelines are null. params={}", params);
        }
        final IConnections bc = (IConnections) params.getParameters().get(MarketGoalParameters.brokerConnections);
        LOG.info("Broker connections ={}. params={}", bc, params);

        this.setParentConnections(bc);
        if (this.parentConnections == null) {
            if (debug) LOG.info("Broker connections are null. params={}", params);
        }
    }

    /**
     * @return - the number of {@code IAuctionMessage} on this local messages queue
     */
    @Override
    public int messages() {
        return 0;
    }

    /**
     * Returns the {@code DisplayInformation} object containing the information for the {@code ICapability}.
     *
     * @param displayInformation the data display.
     */
    @Override
    public synchronized void populateCapabilitiesOfDisplayObject(IDisplayInformation displayInformation) {
        super.populateCapabilitiesOfDisplayObject(displayInformation);
    }

    /**
     * @return {@code AuctionMessage} received
     */
    @Override
    public IAuctionMessage receive() {
        return null;
    }

    /**
     * Resets the {@code ICapability} and allows actions in the {@code ICapability} to be performed again for the new turn.
     */
    @Override
    public synchronized void reset() {

    }

    public synchronized boolean isOpen(long currentTS) {
        final long openingTS = this.auctionGuidelines.getOpeningTimeSlice();
        if (debug) LOG.debug("Auction opening TS= {}.", openingTS);
        final long purchaseTS = this.auctionGuidelines.getPurchaseTimeSlice();
        if (debug) LOG.debug("Auction purchse TS= {}.", openingTS);
        return currentTS >= openingTS && currentTS < purchaseTS;
    }

    /**
     * Returns the {@code DOM} {@code Element} of the {@code IAttributable} or {@code ICapability}.  This method should
     * be overwritten by subclasses if there are additional variables defined whose values should be saved if they affect the
     * state of the object.  Overwritting can be done in two ways: adding additional information to the {@code Element}
     * returned by the super class, or creating a completely new element from scratch.
     *
     * @param document the document in which to create the {@code DOM} {@code Element}s.
     * @return the {@code DOM} {@code Element} of the {@code IAttributable} or {@code ICapability}.
     */
    @Override
    public Element toElement(Document document) {
        final Element capability = super.toElement(document);
        return capability;
    }


}
