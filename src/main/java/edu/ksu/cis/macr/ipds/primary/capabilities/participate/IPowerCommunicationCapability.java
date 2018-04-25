/*
 * Copyright 2012
 * Kansas State University MACR Laboratory http://macr.cis.ksu.edu/
 * Department of Computing & Information Sciences
 *
 * See License.txt file for the license agreement.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package edu.ksu.cis.macr.ipds.primary.capabilities.participate;


import edu.ksu.cis.macr.aasis.agent.persona.ICapability;
import edu.ksu.cis.macr.aasis.agent.persona.IInternalCommunicationCapability;
import edu.ksu.cis.macr.aasis.common.IConnections;
import edu.ksu.cis.macr.ipds.primary.messages.IPowerAssessment;
import edu.ksu.cis.macr.ipds.primary.messages.IPowerMessage;
import edu.ksu.cis.macr.ipds.primary.messages.IPowerMessageContent;
import edu.ksu.cis.macr.ipds.primary.messages.PowerPerformative;
import edu.ksu.cis.macr.ipds.primary.sensors.smartmeter.ISmartMeterRead;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;

import java.util.List;
import java.util.Queue;

/**
 The {@code IPowerCommunicationCapability} interface for defining power-related communication capabilities.
 */
public interface IPowerCommunicationCapability extends ICapability, IInternalCommunicationCapability.ICommunicationChannel {

   IPowerMessage checkFromSelf();

  IPowerMessage checkForLocalPowerMessageFromWorker();

  IPowerMessage checkFromAdmin(String ownerSelfPersona);

  /**
   Used by the sensor persona to create a local power message to send to the self persona for review.
   @param timeSlice - the time slice since the simulation began
   @param minKW - min kw
   @param maxKW - max kw
   @param read - the smart meter read
   @param power - the power assessment
   @param quality - the power quality assessment
   @return the {@code IPowerMessage} created
   */
  IPowerMessage createLocalPowerMessageForSelf(long timeSlice, double minKW, double maxKW, ISmartMeterRead read, final IPowerAssessment power,
                                               final Object quality);

  IPowerMessage createLocalPowerMessageForSuperSelf(IPowerMessageContent newContent, IPowerAssessment power, Object quality);

  void sendUp(IPowerMessage localMessage, IConnections upConnections);

  boolean forwardToParticipant(IPowerMessage localPowerMessage, UniqueIdentifier subIdentifier);

  /**
   @return the {@code ICommunicationChannel} this {@code Capability} uses.
   */
 IInternalCommunicationCapability.ICommunicationChannel getCommunicationChannel();

  /**
   @return - the communication channel string associated with this communication capability.
   */
  String getCommunicationChannelID();

  /*
 * Returns the set of {@code IPowerMessage} as a queue.
 */
  Queue<IPowerMessage> getLocalMessages();

  /*
 * Returns the set of {@code IPowerMessage} as a list.
 */
  List<IPowerMessage> getLocalMessagesAsList();

  /**
   Get the parameters from this instance goal and use them to set the goal-specific guidelines for any child
   connections.

   @param instanceGoal - the instance goal provided
   */
  void initializeChildConnections(InstanceGoal<?> instanceGoal);

  /**
   Get the parameters from this instance goal and use them to set the goal-specific guidelines.

   @param ig - the instance goal with the behavior information
   */
  void init(InstanceGoal<?> ig);

  void initializeParentConnections(InstanceGoal<?> instanceGoal);

  /**
   @return - the number of {@code IPowerMessage} on this local messages queue
   */
  int messages();

  /**
   @return {@code PowerMessage} received
   */
  IPowerMessage receive();

  /**
   @param message - the PowerMessage messages to be sent
   @return {@code true} if the messages was sent, {@code false} otherwise.
   */
  boolean send(IPowerMessage message);

  void sendControllerRequest(PowerPerformative reportOutOfBounds, IPowerMessageContent request);

  /**
   Send local message.

   @param message - the PowerMessage messages to be sent through the internal agent communication system.
   @return {@code true} if the messages was sent, {@code false } otherwise.
   */
  boolean sendLocal(IPowerMessage message);


  // boolean sendRemotePowerMessageToSuperList(IPowerMessage localPowerMessage, IConnections parentConnections);

  boolean sendRemotePowerMessageAggregateToSuperSelf(IPowerMessage selfLocalMessage);

  /**
   Sets up the RabbitMQ queues and binding keys required for operation.
   */
  void setupPowerMessagingToSuper();


  void setupQueuesAndBindingsForSelfPersonaFromSuper();

  /**
   Sets up the RabbitMQ queues and binding keys required for operation.
   */
  void setupQueuesAndBindingsForSuperSelfPersona();
}