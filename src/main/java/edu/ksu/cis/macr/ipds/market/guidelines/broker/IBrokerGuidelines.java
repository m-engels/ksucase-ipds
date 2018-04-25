package edu.ksu.cis.macr.ipds.market.guidelines.broker;

import edu.ksu.cis.macr.goal.model.InstanceParameters;
import edu.ksu.cis.macr.organization.model.identifiers.StringIdentifier;

import java.util.HashSet;

/**
 * The goal parameter guidelines that tailor this broker's behavior for brokering power auctions.
 */
public interface IBrokerGuidelines {

    public static IBrokerGuidelines extractBrokerGuidelines(InstanceParameters params) {
        return (IBrokerGuidelines) params
                .getValue(StringIdentifier.getIdentifier("brokerGuidelines"));
    }

    AuctionAlgorithm getAuctionAlgorithm();

    void setAuctionAlgorithm(int value);

    HashSet<String> getAuthorizedParticipants();

    void setAuthorizedParticipants(HashSet<String> authorizedParticipants);

    int getIteration();

    void setIteration(int iteration);

    int getMaxIteration();

    void setMaxIteration(int maxIteration);

    long getOpenTimeSlice();

    void setOpenTimeSlice(long openTimeSlice);

    long getPurchaseTimeSlice();

    void setPurchaseTimeSlice(long purchaseTimeslice);

    int getTierNumber();

    void setTierNumber(int tierNumber);

    void setAuctionAlgorithm(AuctionAlgorithm auctionAlgorithm);


}
