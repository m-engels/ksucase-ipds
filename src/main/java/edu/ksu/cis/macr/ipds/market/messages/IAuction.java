package edu.ksu.cis.macr.ipds.market.messages;

import java.util.ArrayList;

/**
 * {@code IAuction} provides an interface for describing power auctions.
 */
public interface IAuction {

    /**
     * Adds a bid to the buyer or seller bid lists (depdending on {@code BidDirection}
     *
     * @param bid the bid to be added to this {@code IAuction}
     * @return the size of the bid list.
     */
    int add(IBid bid);

    /**
     * Execute the auction and either fill or reject all bids.
     */
    void execute();

    /**
     * Get the string name of the agent executing this auction.
     *
     * @return the string name of the agent executing this auctio
     */
    String getActioneer();

    /**
     * Get the list of bids from buyers.
     *
     * @return the ArrayList of buy bids.
     */
    ArrayList getBuyBidList();

    /**
     * Get the clearing price determined when the auction is filled.
     *
     * @return the clearing price determined when the auction is filled
     */
    double getClearningPrice_centsperkWh();

    /**
     * Get the time slice since simulation start for which the power is being bought and sold.
     *
     * @return the number of time slices since the simulation began
     */
    long getPurchaseTimeslice();

    /**
     * Get the list of bids from sellers.
     *
     * @return the ArrayList of sell bids.
     */
    ArrayList getSellBidList();

    /**
     * Get the total transaction quantity of Kilowatthours filled during execution of the auction.
     *
     * @return the total transaction quantity of Kilowatthours filled
     */
    double getTransactionQuantity_kWh();


}
