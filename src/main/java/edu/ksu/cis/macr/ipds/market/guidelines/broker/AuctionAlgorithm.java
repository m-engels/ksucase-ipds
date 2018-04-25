package edu.ksu.cis.macr.ipds.market.guidelines.broker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The auction alogorithm used to determine which bids get accepted or rejected and at what price.
 */
public enum AuctionAlgorithm {

    DOUBLE_AUCTION(1),

    KELLY_AUCTION(2);

    private static final Logger LOG = LoggerFactory.getLogger(AuctionAlgorithm.class);
    private final int value;

    private AuctionAlgorithm(int value) {
        this.value = value;
    }

    public static AuctionAlgorithm getActionAlgorithm(int value) {
        if (value == 1) return AuctionAlgorithm.DOUBLE_AUCTION;
        else return AuctionAlgorithm.KELLY_AUCTION;
    }

    /**
     * Get the integer value of the type.
     *
     * @return - the integer value (1 is the top level of the hierarchy)
     */
    public int getIntegerValue() {
        return this.value;
    }
}
