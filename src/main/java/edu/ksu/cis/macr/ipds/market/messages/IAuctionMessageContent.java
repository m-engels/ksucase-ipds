package edu.ksu.cis.macr.ipds.market.messages;


import java.io.IOException;

/**
 * {@code IAuctionMessageContent} provides an interface for defining the payload content in an {@code IAuctionMessage}.
 */
public interface IAuctionMessageContent {

    Object deserialize(byte[] bytes) throws Exception;

    String getBroker();

    IBid getBid();

    long getPurchaseTimeSlice();


    byte[] serialize() throws IOException;

}
