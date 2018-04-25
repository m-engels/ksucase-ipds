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
package edu.ksu.cis.macr.ipds.market.capabilities.admin;


import edu.ksu.cis.macr.aasis.agent.persona.AbstractOrganizationCapability;
import edu.ksu.cis.macr.aasis.agent.persona.IOrganization;
import edu.ksu.cis.macr.aasis.agent.persona.IPersona;
import edu.ksu.cis.macr.aasis.common.IConnectionGuidelines;
import edu.ksu.cis.macr.aasis.common.IConnections;
import edu.ksu.cis.macr.aasis.messaging.IMessagingFocus;
import edu.ksu.cis.macr.goal.model.InstanceParameters;
import edu.ksu.cis.macr.ipds.market.capabilities.participate.MarketCalculator;
import edu.ksu.cis.macr.ipds.market.guidelines.broker.IBrokerGuidelines;
import edu.ksu.cis.macr.ipds.market.messages.*;
import edu.ksu.cis.macr.ipds.market.messaging.MarketMessagingFocus;
import edu.ksu.cis.macr.organization.model.Agent;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;

/**
 * Provides the ability to broker power auctions.
 */
public class BrokerPowerCapability extends AbstractOrganizationCapability {
    private static final Logger LOG = LoggerFactory.getLogger(BrokerPowerCapability.class);
    private static final boolean debug = false;
    private static final IMessagingFocus messagingFocus = MarketMessagingFocus.MARKET;
    private IConnections childConnections;
    private IBrokerGuidelines brokerGuidelines;
    private int currentIteration = 0;

    /**
     * @param owner - the entity to which this capability belongs
     * @param org   - the organization in which the entity is operating.
     */
    public BrokerPowerCapability(final IPersona owner, final IOrganization org) {
        super(BrokerPowerCapability.class, owner, org);
    }

    public synchronized static String getMapAuctionString(TreeMap<String, IAuctionMessageContent> treeMap) {
        StringBuilder b = new StringBuilder("\n");
        for (Map.Entry<String, IAuctionMessageContent> entry : treeMap.entrySet()) {
            b.append(entry.getKey() + ": ");
            b.append(entry.getValue().toString() + "\n");
        }
        return b.toString();
    }

    public synchronized int getCurrentIteration() {
        return currentIteration;
    }

    public synchronized void setCurrentIteration(int currentIteration) {
        this.currentIteration = currentIteration;
    }

    public synchronized IBrokerGuidelines getBrokerGuidelines() {
        return brokerGuidelines;
    }

    public synchronized void setBrokerGuidelines(IBrokerGuidelines brokerGuidelines) {
        this.brokerGuidelines = brokerGuidelines;
    }

    public synchronized IConnections getChildConnections() {
        return this.childConnections;
    }

    public synchronized void setChildConnections(IConnections childConnections) {
        this.childConnections = childConnections;
    }

    private IAuctionMessageContent createAuctionMessageContent(Map<String, IAuctionMessageContent> mostRecentReadings) {
        return null;
    }

    @Override
    public synchronized void reset() {
    }

    @Override
    public synchronized double getFailure() {
        return 0;
    }

    @Override
    public Element toElement(final Document document) {
        final Element capability = super.toElement(document);
        return capability;
    }

    public synchronized boolean isDoneIterating() {
        boolean doneIterating = this.currentIteration >= this.brokerGuidelines.getMaxIteration();
        if (debug) LOG.debug("Done iterating = {}. This iteration = {}, max iterations allowed = {}", doneIterating,
                this.currentIteration, this.brokerGuidelines.getMaxIteration());
        return doneIterating;
    }

    /**
     * Gets the set of local registered prosumer agents given the set of all agents. Do not include the supervisors (control
     * component masters in this local organization) and do not include any independent forecaster agents (but a child that
     * is also performing a forecast role should be included).
     *
     * @return - the set of all prosumer agents registered in this local organization (does not include
     * other types of agents such as forecasters, etc)
     */
    public synchronized Set<Agent<?>> getLocalRegisteredProsumers() {
        // TODO: add ability to indicate sub is a prosumer agent (not a forecaster).
        final Set<Agent<?>> allAgents = this.getOwner()
                .getPersonaControlComponent().getOrganizationModel().getAgents();

        final Set<Agent<?>> prosumers = new HashSet<>();
        for (Agent<?> agent : allAgents) {
            boolean isMaster = (agent.getIdentifier() == this.owner.getPersonaControlComponent().getLocalMaster());
            boolean isExternalForecaster = agent.getIdentifier().toString().contains("_F");
            if (!isMaster && !isExternalForecaster) {
                prosumers.add(agent);
            }
        }
        return prosumers;
    }

    /**
     * Get the parameters from this instance goal and use them to set the goal-specific guidelines.
     *
     * @param ig - this instance of the specification goal
     */
    public synchronized void init(InstanceGoal<?> ig) {
        if (debug) LOG.info("Creating external organization from goal: {}.", ig);
        // Get the parameter values from the existing active instance goal
        final InstanceParameters params = Objects.requireNonNull((InstanceParameters) ig.getParameter());
        if (debug) LOG.debug("Initializing with the given goal parameter guidelines: {}.", params);
        this.setBrokerGuidelines(IBrokerGuidelines.extractBrokerGuidelines(params));
        if (this.brokerGuidelines == null) {
            LOG.error("Broker initializing organization with no broker guidelines. params={}", params);
            System.exit(-51);
        }
        this.setChildConnections(IConnections.extractConnections(params, "auctionConnections"));
        if (this.childConnections == null) {
            LOG.error("Broker initializing external organization with no auction participants. params={}", params);
            System.exit(-56);
        } else {
            if (debug) LOG.info("Starting initialization of new organization. ");
            if (debug)
                LOG.debug("{} authorized connections to child participants.", childConnections.getListConnectionGuidelines().size());
            final IConnectionGuidelines cg = childConnections.getListConnectionGuidelines().get(0);
            if (debug) LOG.debug("Organization guidelines found at {}:  ", cg.getSpecificationFilePath());
        }
    }


    public synchronized IAuctionMessageContent brokerAuction(TreeMap<String, IAuctionMessageContent> map) {
        final int nTier = this.getBrokerGuidelines().getTierNumber();
        final long purchaseTimeSlice = this.getBrokerGuidelines().getPurchaseTimeSlice();
        LOG.debug("Beginning auction with inputs: {}", getMapAuctionString(map));
        double[][] input = initializeInputs(map);
        LOG.debug("Auction inputs: {}", input.toString());
        double[][] output = executeAuction(nTier, input, currentIteration);
        LOG.debug("Auction output: {}", output.toString());
        TreeMap<String, IAuctionMessageContent> outMap = convertResultArrayToMessageContentMap(input, output, nTier, purchaseTimeSlice);
        LOG.debug("Auction outMap: {}", getMapAuctionString(outMap));
        return generateSummaryContent(outMap);

    }

    private synchronized double[][] executeAuction(int nTier, double[][] input, int currentIteration) {
        double[][] result = new double[4][5];
        try {
            result = MarketCalculator.calculateAuction(nTier,
                    this.getBrokerGuidelines().getPurchaseTimeSlice(),
                    this.getBrokerGuidelines().getAuctionAlgorithm(), currentIteration, input);
        } catch (Exception ex) {
            LOG.error("Error getting auction results: {}", ex.getMessage());
            System.exit(-44);
        }
        return result;
    }

    private synchronized double[][] initializeInputs(TreeMap<String, IAuctionMessageContent> inContent) {
        double[][] input = new double[4][5];
        int iRow = 0;
        for (Map.Entry<String, IAuctionMessageContent> entry : inContent.entrySet()) {
            String key = entry.getKey();
            if (debug) LOG.debug("key = {}", key);
            IAuctionMessageContent content = entry.getValue();
            double quantity = content.getBid().getMaximumQuantity_kWh();
            if (debug) LOG.debug("quantity = {}", quantity);
            double isSell;
            double buyPrice;
            double sellPrice;
            if (content.getBid().getBidDirection().equals(BidDirection.SELL)) {
                isSell = 1;
                buyPrice = 0;
                sellPrice = content.getBid().getPrice_centsperkWh();
            } else {
                isSell = 0;
                sellPrice = 0;
                buyPrice = content.getBid().getPrice_centsperkWh();
            }
            if (debug) LOG.debug("buyPrice = {}", buyPrice);
            if (debug)
                LOG.debug("content.getBid().getBidder().substring(1) = {}", content.getBid().getBidder().substring(1, 3));
            double idNumber = 0;
            try {
                idNumber = Double.parseDouble(content.getBid().getBidder().substring(1, 3));
            } catch (Exception ex) {
                LOG.error("Error getting int from agent name {}", content.getBid().getBidder().substring(1, 3));
                System.exit(-44);
            }
            if (debug) LOG.debug("idNumber = {}", idNumber);
            input[iRow][0] = idNumber;
            if (debug) LOG.debug("idNumber input[{}][0] = {}", iRow, input[iRow][0]);
            input[iRow][1] = buyPrice;
            if (debug) LOG.debug("buyPrice input[{}][1] = {}", iRow, input[iRow][1]);
            input[iRow][2] = sellPrice;
            if (debug) LOG.debug("sellPrice input[{}][2] = {}", iRow, input[iRow][2]);
            input[iRow][3] = isSell;
            if (debug) LOG.debug("isSell input[{}][3] = {}", iRow, input[iRow][3]);
            input[iRow][4] = quantity;
            if (debug) LOG.debug("quantity input[{}][4] = {}", iRow, input[iRow][4]);
            iRow += 1;
        }
        return input;
    }

    private synchronized TreeMap<String, IAuctionMessageContent> convertResultArrayToMessageContentMap(double[][] input, double[][] output, int nTier, long purchaseTimeSlice) {
        TreeMap<String, IAuctionMessageContent> map = new TreeMap<>();

        double aggregateSellQuantity = 0;
        for (int i = 0; i < 4; i++) {

            double idNumber = input[i][0];
            if (debug) LOG.debug("idNumber = {}", idNumber);

            double price = output[i][1];
            if (debug) LOG.debug("buyPrice = {}", price);

            double isSellDouble = input[i][3];
            boolean isSell = (isSellDouble > 0);
            if (debug) LOG.debug("isSell = {}", isSell);

            BidDirection direction = BidDirection.BUY;
            if (isSell) direction = BidDirection.SELL;

            double quantity = output[i][0];
            if (quantity < .00001) quantity = 0;
            if (debug) LOG.debug("quantity = {}", quantity);

            String bidder = "";
            if (nTier == 1) bidder = "H" + (int) idNumber + "inN43";
            else bidder = "N" + (int) idNumber + "inL39";
            if (debug) LOG.debug("agentName = {}", bidder);

            if (isSell) aggregateSellQuantity += quantity;
            else aggregateSellQuantity -= quantity;

            IAuctionMessageContent content = getAuctionMessageContent(price, purchaseTimeSlice, direction, quantity);
            map.put(bidder, content);
        }
        return map;
    }

    public synchronized void incrementIteration() {
        this.currentIteration++;
        LOG.info("EVENT: AUCTION_ITERATION_EXECUTED. Tier = {}, Number of iterations performed = {}", this.brokerGuidelines.getTierNumber(), this.currentIteration);
    }

    public synchronized boolean allBidsReceived(TreeMap<String, IAuctionMessageContent> bidMap) {
        return (bidMap.size() == this.childConnections.getListConnectionGuidelines().size());
    }

    public synchronized IAuctionMessageContent generateSummaryContent(TreeMap<String, IAuctionMessageContent> map) {
        LOG.debug("Generating summary bid from {}", getMapAuctionString(map));
        double newPrice = map.firstEntry().getValue().getBid().getPrice_centsperkWh();  // prices are same
        LOG.debug("Clearing price is {}.", newPrice);
        long purchaseTimeSlice = map.firstEntry().getValue().getPurchaseTimeSlice();
        LOG.debug("For power to be exchanged at time slice {}.", purchaseTimeSlice);

        double aggregateSellQuantity = 0.0;
        for (Map.Entry<String, IAuctionMessageContent> entry : map.entrySet()) {
            String key = entry.getKey();
            IAuctionMessageContent value = entry.getValue();
            IBid bid = value.getBid();
            if (bid.getBidDirection() == BidDirection.SELL) {
                aggregateSellQuantity += bid.getMaximumQuantity_kWh();
                LOG.debug("This bid has a sell quantity ={}. New aggregate sell qty={}.", bid.getMaximumQuantity_kWh(), aggregateSellQuantity);
            } else {
                aggregateSellQuantity -= bid.getMaximumQuantity_kWh();
                LOG.debug("This bid has a buy quantity ={}. New aggregate sell qty={}.", bid.getMaximumQuantity_kWh(), aggregateSellQuantity);
            }
        }

        BidDirection direction;
        double newQuantity;

        if (aggregateSellQuantity > 0) {
            direction = BidDirection.SELL;
            newQuantity = aggregateSellQuantity;
        } else {
            direction = BidDirection.BUY;
            newQuantity = -1 * aggregateSellQuantity;
        }
        if (debug) LOG.debug("direction = {}, qty={}", direction, newQuantity);

        IAuctionMessageContent content = getAuctionMessageContent(newPrice, purchaseTimeSlice, direction, newQuantity);
        return content;

    }

    private synchronized IAuctionMessageContent getAuctionMessageContent(double price, long purchaseTimeSlice, BidDirection direction, double quantity) {
        IBid bid = Bid.createBid(quantity, price, BidStatus.FILLED, direction, this.owner.getUniqueIdentifier().toString());
        if (debug) LOG.debug("bid = {}", bid);

        IAuctionMessageContent content = AuctionMessageContent.create(purchaseTimeSlice, this.owner.getUniqueIdentifier().toString(), bid);
        if (debug) LOG.debug("summary bid content = {}", content);
        return content;
    }
}
