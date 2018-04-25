package edu.ksu.cis.macr.ipds.market.messages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The {@code Auction} class is used by the {@code IAuctionPowerCapability} to conduct auctions.
 */
public class Auction implements IAuction {
    private static final Logger LOG = LoggerFactory.getLogger(Auction.class);
    private static final boolean debug = false;
    private long purchaseTimeslice = 0;
    private String actioneer;
    private List<IBid> buyBidList = new ArrayList<>();
    private List<IBid> sellBidList = new ArrayList<>();
    private AuctionStatus status = AuctionStatus.UNEXECUTED;
    private double transactionQuantity_kWh = 0;
    private double clearningPrice_centsperkWh = 0;

    private Auction(long purchaseTimeslice, String actioneer) {
        this.purchaseTimeslice = purchaseTimeslice;
        this.actioneer = actioneer;
        if (debug) LOG.debug("Created new bid. {}", this.toString());
    }

    /**
     * Create a new auction for buying and selling power at the given timeslice by the given auctioneer.
     *
     * @param purchaseTimeslice - the number of time slices since the simulation when the purchased power will be taken.
     * @param actioneer         - the unique string name of the super holon agent running this auction for its holons.
     * @return the IAuction object created
     */
    public static IAuction createAuction(final long purchaseTimeslice, final String actioneer) {
        return new Auction(purchaseTimeslice, actioneer);
    }

    @Override
    public int add(IBid bid) {
        if (bid.getBidDirection() == BidDirection.BUY) {
            final boolean add = buyBidList.add(bid);
            if (debug) LOG.debug("Adding buy bid. {}. New list={}.", add, buyBidList);
            return buyBidList.size();
        } else {
            final boolean add = sellBidList.add(bid);
            if (debug) LOG.debug("Adding sell bid. {}. New list={}.", add, sellBidList);
            return sellBidList.size();
        }
    }

    @Override
    public synchronized void execute() {
        if (debug) LOG.debug("Executing. Before auction execution: {}", this.toString());
        if (debug) LOG.debug("Executed. After auction execution: {}", this.toString());
    }

    @Override
    public String getActioneer() {
        return this.actioneer;
    }

    @Override
    public ArrayList<IBid> getBuyBidList() {
        return (ArrayList<IBid>) Collections.unmodifiableList(this.buyBidList);
    }

    @Override
    public double getClearningPrice_centsperkWh() {
        return this.clearningPrice_centsperkWh;
    }

    @Override
    public long getPurchaseTimeslice() {
        return purchaseTimeslice;
    }

    @Override
    public ArrayList<IBid> getSellBidList() {
        return (ArrayList<IBid>) Collections.unmodifiableList(this.sellBidList);
    }

    @Override
    public double getTransactionQuantity_kWh() {
        return this.transactionQuantity_kWh;
    }

    @Override
    public String toString() {
        return "Auction{" +
                "purchaseTimeslice=" + purchaseTimeslice +
                ", actioneer='" + actioneer + '\'' +
                ", buyBidList=" + buyBidList +
                ", sellBidList=" + sellBidList +
                ", transactionQuantity_kWh=" + transactionQuantity_kWh +
                ", clearningPrice_centsperkWh=" + clearningPrice_centsperkWh +
                '}';
    }


}

