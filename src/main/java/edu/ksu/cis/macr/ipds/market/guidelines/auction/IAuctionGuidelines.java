package edu.ksu.cis.macr.ipds.market.guidelines.auction;

import edu.ksu.cis.macr.goal.model.InstanceParameters;
import edu.ksu.cis.macr.organization.model.identifiers.StringIdentifier;

/**
 * The goal parameter guidelines that tailor this agent's personal behavior regarding power {@code IBid}s.
 */
public interface IAuctionGuidelines {

    public static IAuctionGuidelines extractAuctionGuidelines(final InstanceParameters params) {
        return (IAuctionGuidelines) params.getValue(StringIdentifier.getIdentifier("auctionGuidelines"));
    }

    public long getOpeningTimeSlice();

    void setOpeningTimeSlice(long openingTimeSlice);

    public long getPurchaseTimeSlice();

    void setPurchaseTimeSlice(long purchaseTimeSlice);

    int getIsSell();

    void setIsSell(int isSell);

    double getkWh();

    void setkWh(double kWh);

    int getTierNumber();

    void setTierNumber(int tierNumber);

    /**
     * Set the percent this agent will allow its preferred buying price to vary. Default is plus or minus 10 percent.
     *
     * @return the percent the preferred buying price can vary.
     */
    double getBuyPriceFlexibilityPercent();

    /**
     * Set the percent this agent will allow its preferred buying price to vary. Default is plus or minus 10 percent.
     *
     * @param buyingPriceFlexibility_percent the percent the preferred buying price can vary.
     */
    void setBuyPriceFlexibilityPercent(double buyingPriceFlexibility_percent);

    /**
     * Get the desired price this agent prefers when selling power. Default is 2.5 cents per kWh.
     *
     * @return the desired selling price in cents per kWh
     */
    double getDesiredSellPrice_centsperkWh();

    /**
     * Set the desired price this agent prefers when selling power.
     *
     * @param desiredSellingPrice_centsperkWh - the desired selling price in cents per kWh
     */
    void setDesiredSellPrice_centsperkWh(double desiredSellingPrice_centsperkWh);

    /**
     * Get the desired price this agent prefers when buying power. Default is 2.0 cents per kWh.
     *
     * @return the preferred buying price in cents per kWh
     */
    double getDesiredBuyPrice_centsperkWh();

    /**
     * Set the preferred buying price in cents per kWh.
     *
     * @param preferredBuyingPrice_centsperkWh - the cost in cents per kWh
     */
    void setDesiredBuyPrice_centsperkWh(double preferredBuyingPrice_centsperkWh);

    /**
     * Get the percent this agent will allow its preferred selling price to vary. Default is plus or minus 10 percent.
     *
     * @return the percent the preferred selling price can vary.
     */
    double getSellPriceFlexibilityPercent();

    /**
     * Set the percent this agent will allow its preferred selling price to vary. Default is plus or minus 10 percent.
     *
     * @param sellingPriceFlexibility_percent - how flexible the price is in percent
     */
    void setSellPriceFlexibilityPercent(double sellingPriceFlexibility_percent);
}
