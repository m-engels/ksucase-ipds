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


import edu.ksu.cis.macr.aasis.agent.cc_message.custom.EquipmentStatus;
import edu.ksu.cis.macr.aasis.agent.cc_message.custom.ICustomMessage;
import edu.ksu.cis.macr.aasis.agent.cc_message.custom.ICustomMessageContent;
import edu.ksu.cis.macr.aasis.agent.persona.ICapability;
import edu.ksu.cis.macr.aasis.agent.persona.IInternalCommunicationCapability;
import edu.ksu.cis.macr.organization.model.InstanceGoal;

import java.util.List;
import java.util.Queue;

/**
 * Interface for defining a message-related communication capability.
 */
public interface ICustomMessageCommunicationCapability extends ICapability, IInternalCommunicationCapability.ICommunicationChannel {

    ICustomMessage checkFromSelf();

    ICustomMessage checkForMessageFromWorker();

    ICustomMessage checkFromAdmin(String ownerSelfPersona);

    ICustomMessageContent createContent(String sampleText, int sampleValue, EquipmentStatus equipmentStatus);

    ICustomMessage createLocalMessageForSelf(ICustomMessageContent content);


    /**
     * @return the {@code ICommunicationChannel} this {@code Capability} uses.
     */
    IInternalCommunicationCapability.ICommunicationChannel getCommunicationChannel();

    /**
     * @return - the communication channel string associated with this communication capability.
     */
    String getCommunicationChannelID();

    /*
   * Returns the set of {@code ICustomMessage} as a queue.
   */
    Queue<ICustomMessage> getLocalMessages();

    /*
   * Returns the set of {@code ICustomMessage} as a list.
   */
    List<ICustomMessage> getLocalMessagesAsList();

    /**
     * Get the parameters from this instance goal and use them to set the goal-specific guidelines.
     *
     * @param ig - the instance goal with the behavior information
     */
    void init(InstanceGoal<?> ig);

    /**
     * @return - the number of {@code ICustomMessage} on this local messages queue
     */
    int messages();

    /**
     * @return {@code PowerMessage} received
     */
    ICustomMessage receive();

    /**
     * @param message - the PowerMessage messages to be sent
     * @return {@code true} if the messages was sent, {@code false} otherwise.
     */
    boolean send(ICustomMessage message);

    /**
     * Send local message.
     *
     * @param message - the PowerMessage messages to be sent through the internal agent communication system.
     * @return {@code true} if the messages was sent, {@code false } otherwise.
     */
    boolean sendLocal(ICustomMessage message);


}