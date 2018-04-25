package edu.ksu.cis.macr.ipds.primary.messages;

import edu.ksu.cis.macr.aasis.agent.cc_message.IBaseMessage;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;

import java.io.IOException;

/**
 {@code IPowerMessage} provides an interface for describing messages between agents containing {@code IPowerMessageContent}.
 */
public interface IPowerMessage extends IBaseMessage<PowerPerformative> {

  /**
   Deserialize the message.

   @param bytes - an array of bytes
   @return the deserialized message
   @throws IOException - Handles any IO Exceptions
   @throws ClassNotFoundException - Handles any ClassNotFound Exceptions
   */
  @Override
  Object deserialize(byte[] bytes) throws Exception;

  /**
   Serialize the message.

   @return a byte array with the contents.
   @throws IOException - If an I/O error occurs.
   */
  @Override
  byte[] serialize() throws IOException;

    /**
     /** Constructs a new instance of {@code PowerMessage}.

     @param sender - String name of the agent sending the message
     @param receiver - String name of the agent to whom the message is sent
     @param performative - the {@code PowerPerformative} indicating the type of message
     @param content - the message content
     @return the IPowerMessage created
     */
    public static IPowerMessage createLocal(final UniqueIdentifier sender, final UniqueIdentifier receiver,
                                            final PowerPerformative performative, final Object content) {
        return new PowerMessage(sender, receiver, performative, content);
    }

    /**
     /** Constructs a new instance of {@code PowerMessage}.

     @param senderString - String name of the agent sending the message
     @param receiverString - String name of the agent to whom the message is sent
     @param performative - the {@code PowerPerformative} indicating the type of message
     @param content - the message content
     @return - the IPowerMessage created
     */
    public static IPowerMessage createRemote(final String senderString, final String receiverString,
                                             final PowerPerformative performative, final Object content) {
        return new PowerMessage(senderString, receiverString, performative, content);
    }
}
