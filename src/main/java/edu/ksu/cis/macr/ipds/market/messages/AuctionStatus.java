package edu.ksu.cis.macr.ipds.market.messages;

/**
 * The status of an auction.
 */
public enum AuctionStatus {

    /**
     * The auction has not been executed yet (the default).
     */
    UNEXECUTED,

    /**
     * The auction has been executed and bids have been filled or rejected.
     */
    EXECUTED
}
