package edu.ksu.cis.macr.ipds.market.messages;

import edu.ksu.cis.macr.aasis.agent.cc_message.IBaseMessage;

import java.io.IOException;

/**
 * {@code IPowerMessage} provides an interface for describing messages between agents containing {@code IAuctionMessageContent}.
 */
public interface IAuctionMessage extends IBaseMessage<AuctionPerformative> {

    /**
     * Deserialize the message.
     *
     * @param bytes - an array of bytes
     * @return the deserialized {@code ParticipateMessage}
     * @throws IOException            - Handles all IO Exceptions
     * @throws ClassNotFoundException - Handles any Class not Found Exceptions.
     */
    @Override
    Object deserialize(byte[] bytes) throws Exception;

    /**
     * Serialize the message.
     *
     * @return a byte array with the contents.
     * @throws IOException - If an I/O error occurs.
     */
    @Override
    byte[] serialize() throws IOException;


}
