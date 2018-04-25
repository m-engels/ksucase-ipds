package edu.ksu.cis.macr.ipds.market.messages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * Class representing a simple immutable bid.  After the auction is executed, a new bid is created that also includes the results.
 */
public class Bid implements Serializable, IBid {
    private static final Logger LOG = LoggerFactory.getLogger(Bid.class);
    private static final boolean debug = false;

    private static final long serialVersionUID = 1L;
    private final double maximumQuantity_kWh;
    private final double price_centsperkWh;
    private final BidDirection bidDirection;
    private final String bidder;
    private final double filledQuantity_kWh;
    private final double filledPrice_centsperkWh;
    private BidStatus bidStatus = BidStatus.UNDETERMINED;
    private Bid(final double maximumQuantity_kWh, final double price_centsperkWh, BidStatus bidStatus, final BidDirection bidDirection, final String bidder) {
        if (debug)
            LOG.debug("Creating bid:  maximumQuantity_kWh = {}, price={} ", maximumQuantity_kWh, price_centsperkWh);
        this.maximumQuantity_kWh = maximumQuantity_kWh;
        this.price_centsperkWh = price_centsperkWh;
        this.bidStatus = bidStatus;
        this.bidDirection = bidDirection;
        this.bidder = bidder;
        this.filledQuantity_kWh = 0.0;
        this.filledPrice_centsperkWh = 0.0;

    }
    private Bid(final double maximumQuantity_kWh, final double price_centsperkWh, BidStatus bidStatus,
                final BidDirection bidDirection, final String bidder, double filledQuantity_kWh, double filledPrice_centsperkWh) {
        this.maximumQuantity_kWh = maximumQuantity_kWh;
        this.price_centsperkWh = price_centsperkWh;
        this.bidStatus = bidStatus;
        this.bidDirection = bidDirection;
        this.bidder = bidder;
        this.filledQuantity_kWh = filledQuantity_kWh;
        this.filledPrice_centsperkWh = filledPrice_centsperkWh;

    }
    private Bid(final IBid inbid, double filledQuantity_kWh, double filledPrice_centsperkWh) {
        this.maximumQuantity_kWh = inbid.getMaximumQuantity_kWh();
        this.price_centsperkWh = inbid.getPrice_centsperkWh();
        this.bidDirection = inbid.getBidDirection();
        this.bidder = inbid.getBidder();
        this.filledQuantity_kWh = filledQuantity_kWh;
        this.filledPrice_centsperkWh = filledPrice_centsperkWh;
        if (this.filledQuantity_kWh < 0.00001) this.bidStatus = BidStatus.REJECTED;
        else this.bidStatus = BidStatus.FILLED;

    }

    public static Bid createResultBid(final IBid inbid, double filledQuantity_kWh, double filledPrice_centsperkWh) {
        return new Bid(inbid, filledQuantity_kWh, filledPrice_centsperkWh);
    }

    public static IBid createBid(double maximumQuantity, double price, BidStatus bidStatus, BidDirection bidDirection, String bidder) {
        if (debug) LOG.debug("Creating bid:  maximumQuantity = {}, price={} ", maximumQuantity, price);
        return new Bid(maximumQuantity, price, bidStatus, bidDirection, bidder);
    }

    public static IBid createBid(double maximumQuantity, double price, BidStatus bidStatus, BidDirection bidDirection, String bidder, double filledQuantity_kWh, double filledPrice_centsperkWh) {
        if (debug)
            LOG.debug("Creating bid:  filledQuantity = {}, filled price={} ", filledQuantity_kWh, filledPrice_centsperkWh);
        return new Bid(maximumQuantity, price, bidStatus, bidDirection, bidder, filledQuantity_kWh, filledPrice_centsperkWh);
    }

    public double getFilledQuantity_kWh() {
        return filledQuantity_kWh;
    }

    public double getFilledPrice_centsperkWh() {
        return filledPrice_centsperkWh;
    }

    @Override
    public BidDirection getBidDirection() {
        return this.bidDirection;
    }

    @Override
    public BidStatus getBidStatus() {
        return this.bidStatus;
    }

    @Override
    public String getBidder() {
        return bidder;
    }

    @Override
    public double getMaximumQuantity_kWh() {
        return maximumQuantity_kWh;
    }

    @Override
    public double getPrice_centsperkWh() {
        return price_centsperkWh;
    }

    @Override
    public String toString() {
        return "Bid{" +
                "maximumQuantity_kWh=" + maximumQuantity_kWh +
                ", price_centsperkWh=" + price_centsperkWh +
                ", bidDirection=" + bidDirection +
                ", bidder='" + bidder + '\'' +
                ", bidStatus=" + bidStatus +
                ", filledQuantity_kWh=" + filledQuantity_kWh +
                ", filledPrice_centsperkWh=" + filledPrice_centsperkWh +
                '}';
    }
}
