package edu.ksu.cis.macr.ipds.market.messages;

/**
 * The {@code AuctionPerformative} indicates the types of {@code IAuctionMessages} that can be exchanged. Performatives
 * indicate a primary characteristic of the message and may be used to configure behavior in plans.
 */

public enum AuctionPerformative {

    /**
     * Associated message contains a bid to buy or sell power at a future time.
     */
    BID,


    /**
     * Associated message contains the results of an executed (brokered) auction.
     */
    AUCTION_RESULT
}
