package edu.ksu.cis.macr.ipds.market.messages;

/**
 * {@code IBid} defines the interface for a bid to buy or sell a given amount of power (up to a maximum) at the given
 * price.
 */
public interface IBid {

    /**
     * Get the {@code BideDirection} indicating whether this is a bid to buy or to sell.
     *
     * @return the BidDirection as buy or sell
     */
    BidDirection getBidDirection();

    /**
     * Get the {@code BidStatus}.
     *
     * @return the bid status
     */
    BidStatus getBidStatus();

    String getBidder();

    /**
     * Get the maximum quantity availble for sale or purchase.
     *
     * @return - the maximum quantity in kWhr
     */
    double getMaximumQuantity_kWh();

    /**
     * Get the unit price in cents per kWh.
     *
     * @return - the offering price in cents per kWh
     */
    double getPrice_centsperkWh();

}
