package edu.ksu.cis.macr.ipds.market.messages;

/**
 * The status of a bid. Either the bid is undetermined (prior to the auction execution), or it has been considered and
 * either accepted or rejected.
 */
public enum BidStatus {

    /**
     * The bid is part of an auction that has not been executed yet.
     */
    UNDETERMINED,

    /**
     * This bid was filled during auction execution.
     */
    FILLED,

    /**
     * This bid was rejected during auction execution.
     */
    REJECTED
}
