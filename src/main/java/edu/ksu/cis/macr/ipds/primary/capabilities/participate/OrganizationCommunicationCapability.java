package edu.ksu.cis.macr.ipds.primary.capabilities.participate;

import edu.ksu.cis.macr.aasis.agent.cc_message.BaseMessage;
import edu.ksu.cis.macr.aasis.agent.cc_message.IBaseMessage;
import edu.ksu.cis.macr.aasis.agent.ec_cap.IOrganizationCommunicationCapability;
import edu.ksu.cis.macr.aasis.agent.persona.IInternalCommunicationCapability;
import edu.ksu.cis.macr.aasis.agent.persona.IPersona;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 Internal organization communication capability.
 */
public class OrganizationCommunicationCapability implements IOrganizationCommunicationCapability {
    private static final Logger LOG = LoggerFactory.getLogger(OrganizationCommunicationCapability.class);
  private static final boolean debug = false;


  /**
   The {@code ICommunicationChannel} keyword for registering organization getNumberOfMessages.
   */
  private static final String COMMUNICATION_CHANNEL_ID = "Organization Messages";

  /**
   A {@code Queue} that holds {@code Message} sent to this {@code IAbstractControlComponent}.
   */
  private final Queue<IBaseMessage<?>> organizationMessages;

  private final IPersona owner;

    public OrganizationCommunicationCapability(final IPersona owner) {
    this.owner = owner;
    this.organizationMessages = new ConcurrentLinkedQueue<>();
  }


  /**
   Broadcasts an {@code Message}.

   @param message the {@code Message} to be sent.
   @return {@code true} if the {@code Message} was sent, {@code false} otherwise.
   @see IInternalCommunicationCapability#broadcast(String, Object)
   */
  protected boolean broadcast(final BaseMessage<?> message) {
    Objects.requireNonNull(getOwner(),
            "execution component cannot be null");
    Objects.requireNonNull(
            getOwner().getCapability(IInternalCommunicationCapability.class),
            "capability cannot be null");
    Objects.requireNonNull(getCommunicationChannelID(),
            "communication channelID cannot be null");
    try {
      return getOwner().getCapability(IInternalCommunicationCapability.class)
              .broadcast(getCommunicationChannelID(), message);
    } catch (Exception e) {
      LOG.error("ERROR broadcasting message {}: {}", message, e.getMessage());
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   Broadcasts an {@code Message}. The sender will also receive the {@code Message}.

   @param message the {@code Message} to be sent.
   @return {@code true} if the {@code Message} was sent, {@code false} otherwise.
   @see IInternalCommunicationCapability#broadcastIncludeSelf(String,
           Object)
   */
  protected boolean broadcastIncludeSelf(
          final BaseMessage<?> message) {
    return getOwner().getCapability(IInternalCommunicationCapability.class)
            .broadcastIncludeSelf(getCommunicationChannelID(), message);
  }

  @Override
  public synchronized void channelContent(final Object content) {
      LOG.info("Entering channelContent(). Gets raw content and adds message. content={}", content);
    try {
      organizationMessages.add((IBaseMessage<?>) content);
    } catch (Exception e) {
      LOG.error("Cannot cast channelContent to Message<?>");
      System.exit(-73);
    }

  }

  @Override
  public String getCommunicationChannelID() {
    return COMMUNICATION_CHANNEL_ID;
  }

  @Override
  public Queue<IBaseMessage<?>> getOrganizationMessages() {
    return this.organizationMessages;
  }

  @Override
  public IPersona getOwner() {
    return owner;
  }

  @Override
  public int messages() {
    return organizationMessages.size();
  }

  @Override
  public IBaseMessage<?> receive() {
    return organizationMessages.poll();
  }

  public IBaseMessage<?> receiveLocal() {
    if (debug) LOG.debug("Checking for internal organization messages");
    return organizationMessages.poll();
  }


  @Override
  public boolean send(final IBaseMessage<?> message) {
    boolean success = false;
    boolean isLocal = message.isLocal();
    if (isLocal) {
      if (debug) LOG.debug("Attempting to send on channel {} to receiver {} local message {}",
              getCommunicationChannelID(), message.getLocalReceiver(), message);
      success = getOwner().getCapability(IInternalCommunicationCapability.class).sendLocal(message.getLocalReceiver(),
              getCommunicationChannelID(), message);
    }
    if (!success) {
      LOG.error("ERROR attempting to send on channel {} to receiver {} local message {}",
              getCommunicationChannelID(), message.getLocalReceiver(), message);
      System.exit(-2221);
    }
    return success;
  }



  public boolean sendLocal(final BaseMessage<?> message) {
    if (debug) LOG.debug("Attempting to send local message {}", message);
    if (debug) LOG.debug("Attempting to broadcast local message on com channel (filter)={} with content={}. Message={}", getCommunicationChannelID(),
              message.getContent(), message);
    boolean success = getOwner().getCapability(IInternalCommunicationCapability.class).broadcast(getCommunicationChannelID(), message);
    if (!success) {
      LOG.error("ERROR sending message (local): {}", message);
      System.exit(-2223);
    }
    return success;
  }





  @Override
  public String toString() {
    return "IOrganizationCommunicationCapability{" +
            "organizationMessages=" + organizationMessages +
            ", owner=" + owner +
            '}';
  }
}
