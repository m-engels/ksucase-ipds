/**
 * Copyright 2012 Kansas State University MACR Laboratory
 * http://macr.cis.ksu.edu/ Department of Computing & Information Sciences
 *
 * See License.txt file for the license agreement.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package edu.ksu.cis.macr.ipds.primary.capabilities.manage;


import edu.ksu.cis.macr.aasis.agent.persona.AbstractOrganizationCapability;
import edu.ksu.cis.macr.aasis.agent.persona.IOrganization;
import edu.ksu.cis.macr.aasis.agent.persona.IPersona;
import edu.ksu.cis.macr.aasis.common.IConnections;
import edu.ksu.cis.macr.goal.model.InstanceParameters;
import edu.ksu.cis.macr.ipds.primary.goals.AgentGoalParameters;
import edu.ksu.cis.macr.ipds.primary.guidelines.IFeederGuidelines;
import edu.ksu.cis.macr.ipds.primary.messages.IPowerMessageContent;
import edu.ksu.cis.macr.ipds.primary.sensors.smartmeter.ISmartMeterRead;
import edu.ksu.cis.macr.obaa_pp.events.OrganizationEvent;
import edu.ksu.cis.macr.organization.model.Assignment;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import edu.ksu.cis.macr.organization.model.identifiers.StringIdentifier;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;


/**
 The {@code ManageFeederCapability} provides the functionality necessary to distribute combined load guidelines among
 participants in this local organization as well as pass along the power quality guidelines.
 */
public class ManageFeederCapability extends AbstractOrganizationCapability {

  private static final Logger LOG = LoggerFactory.getLogger(ManageFeederCapability.class);
  private static final boolean debug = false;
  private final IPersona ec;
  private int NUMBER_OF_PARTICPANT_MESSAGES_TO_STORE = 1;
  private Map<InstanceGoal<?>, BlockingQueue<IPowerMessageContent>> allParticipantData;
  private IFeederGuidelines feederGuidelines;
  private double currentTotal;
  private double maxKW = 0.0;
  private double maxVoltageMultiplier = 0.0;
  private double minKW = 0.0;
  private double minVoltageMultiplier = 0.0;
  private double netDeltaP = 0.0;
  private IConnections parentConnections;

  /**
   Construct a new {@code ManageFeederCapability} instance.

   @param owner - the agent possessing this capability.
   @param org - the local organization in which this agent operates.
   */
  public ManageFeederCapability(final IPersona owner, final IOrganization org) {
    super(ManageFeederCapability.class, owner, org);
    this.ec = Objects.requireNonNull(owner);
  }

  /**
   @return the combinedMaxKW
   */
  public double getCombinedMaxKW() {
    return this.feederGuidelines.getMaxKW();

  }

  /**
   Set the combinedMaxKW for this local organization (part of the combinedLoadGuidelines.)

   @param combinedMaxKW the combinedMaxKW to set
   */
  public synchronized void setCombinedMaxKW(double combinedMaxKW) {
    this.feederGuidelines.setMaxKW(combinedMaxKW);
  }

  /**
   @return the combinedMinKW
   */
  public double getCombinedMinKW() {
    return this.feederGuidelines.getMinKW();
  }

  /**
   Set the combinedMinKW for this local organization (part of the combinedLoadGuidelines.)

   @param combinedMinKW the combinedMinKW to set
   */
  public synchronized void setCombinedMinKW(double combinedMinKW) {
    this.feederGuidelines.setMinKW(combinedMinKW);
  }

//  /*
//   * Puts together a map with the current MeterRead for each agent within the
//   * feeder that possesses the smart meter capability.
//   */
//  public Map<String, ISmartMeterRead> getCurrentMeterReads() throws UnknownSensorException, SensorUnavailableException, IOException, ClassNotFoundException {
//
//    Map<String, ISmartMeterRead> meters = new HashMap<>();
//
//    Collection<IPersona> allPersona = this.getPersona();
//    allPersona.stream().filter(ec -> ec.getCapability(SmartMeterCapability.class) != null).forEach(ec -> {
//
//      String agentName = ec.getUniqueIdentifier().toString();
//      IRead<?> reading = null;
//      try {
//        reading = ec.getCapability(SmartMeterCapability.class).sense();
//      } catch (UnknownSensorException | ClassNotFoundException | IOException | SensorUnavailableException e) {
//        LOG.error("Error getting smart meter reading. {} {}", e.getClass(), e.getMessage());
//      }
//      ISmartMeterRead read = (ISmartMeterRead) reading.getSensorReadObject();
//      meters.put(agentName, read);
//    });
//    return meters;
//  }

  /*
    * Takes in a parameter of all the Agent meters within the local
    * organization, iterates through them and totals them and returns the
    * total.
    */
  public double getCurrentMeterTotal(Map<String, ISmartMeterRead> meters) {
    double totalRead = 0;
    for (Entry<String, ISmartMeterRead> entry : meters.entrySet()) {
      totalRead += entry.getValue().getElectricalData().getAllPhase_Pgrid();
    }
    return totalRead;
  }

  private Map<InstanceGoal<?>, Double> getCurrentReads(
          Map<InstanceGoal<?>, IPowerMessageContent> currentActuals) {

    Map<InstanceGoal<?>, Double> currentReads = new HashMap<>();

    for (Entry<InstanceGoal<?>, IPowerMessageContent> igoal : currentActuals
            .entrySet()) {
      IPowerMessageContent lastReading = igoal.getValue();
      currentReads.put(igoal.getKey(), lastReading.getActualKW());
    }
    return currentReads;
  }

  private double getCurrentTotal() {
    return this.currentTotal;
  }

  private double getCurrentTotal(Map<InstanceGoal<?>, Double> currentReads) {
    return this.currentTotal;
  }

  @Override
  public double getFailure() {
    return 0;
  }

  public IFeederGuidelines getFeederGuidelines() {
    return this.feederGuidelines;
  }

  public synchronized void setFeederGuidelines(IFeederGuidelines value) {
    this.feederGuidelines = value;
  }

  private InstanceGoal<?> getInstanceGoal(UniqueIdentifier agentIdentifier) {
    String agentName = agentIdentifier.toString();

    Set<Assignment> assignments = owner.getPersonaControlComponent()
            .getOrganizationModel().getAssignments();

    for (final Assignment assign : assignments) {
      final String assigned = assign.getAgent().toString();

      if (agentName.equals(assigned)) {
        return assign.getInstanceGoal();
      }
    }
    return null;
  }


  public double getMaxKW() {
    return this.maxKW;
  }

  public synchronized void setMaxKW(final double maxKW) {
    this.maxKW = maxKW;
  }

  /**
   @return the maxVoltageMultiplier
   */
  public double getMaxVoltageMultiplier() {
    return maxVoltageMultiplier;
  }

  /**
   @param maxVoltageMultiplier the maxVoltageMultiplier to set
   */
  public synchronized void setMaxVoltageMultiplier(double maxVoltageMultiplier) {
    this.maxVoltageMultiplier = maxVoltageMultiplier;
  }

  public double getMinKW() {
    return this.minKW;
  }

  public synchronized void setMinKW(final double minKW) {
    this.minKW = minKW;
  }

  /**
   @return the minVoltageMultiplier
   */
  public double getMinVoltageMultiplier() {
    return minVoltageMultiplier;
  }

  /**
   @param minVoltageMultiplier the minVoltageMultiplier to set
   */
  public synchronized void setMinVoltageMultiplier(double minVoltageMultiplier) {
    this.minVoltageMultiplier = minVoltageMultiplier;
  }

  /**
   Get the net delta P for the community request.  A positive net delta P is a request for additional smart inverter
   reactive power.  A negative net delta P is a request to reduce the smart inverter reactive power.

   @return the net delta p community request
   */
  public double getNetDeltaP() {
    return netDeltaP;
  }

  /**
   Set the net delta P for the community request. A positive net delta P is a request for additional smart inverter
   reactive power.  A negative net delta P is a request to reduce the smart inverter reactive power.

   @param netDeltaP - the net delta P community request
   */
  public synchronized void setNetDeltaP(double netDeltaP) {
    this.netDeltaP = netDeltaP;
  }

  /**
   Get the parameters from this instance goal and use them to initialize the capability.

   @param instanceGoal - this instance of the specification goal
   */
  public synchronized void initializeFromGoal(InstanceGoal<?> instanceGoal) {
    // Get the parameter values from the existing active instance goal
    final InstanceParameters params = Objects
            .requireNonNull((InstanceParameters) instanceGoal
                    .getParameter());
    IFeederGuidelines g = Objects
            .requireNonNull((IFeederGuidelines) params.getValue(StringIdentifier
                    .getIdentifier("feederGuidelines")));

    // Set the goal parameter guidelines
    this.setFeederGuidelines(g);

    IConnections pc = (IConnections) params.getValue(StringIdentifier.getIdentifier("parentConnections"));
    this.setParentConnections(pc);
    if (pc != null) {
      if (debug) LOG.debug("{} authorized connections to super holons.", pc.getListConnectionGuidelines().size());
    }
  }

  public boolean isWorkingAsMaster() {
    return this.owner.getPersonaControlComponent().isMaster();
  }

  public boolean isWorkingAsSlave() {
    return this.owner.getPersonaControlComponent().isSlave();
  }

  @Override
  public synchronized void reset() {
  }



  /**
   Get the parameters from this instance goal and use them to set the goal-specific guidelines.

   @param instanceGoal - the instance goal containing the guidelines
   */
  public synchronized void setGuidelines(InstanceGoal<?> instanceGoal) {

    // Get the parameter values from the existing active instance goal

    final InstanceParameters params = Objects
            .requireNonNull((InstanceParameters) instanceGoal
                    .getParameter());

    this.feederGuidelines = Objects
            .requireNonNull((IFeederGuidelines) params
                    .getValue(StringIdentifier
                            .getIdentifier("feederGuidelines")));
    LOG.info("Feeder begins with feederGuidelines of {}", feederGuidelines.toString());
  }

  public synchronized void setParentConnections(IConnections parentConnections) {
    this.parentConnections = parentConnections;
  }

  @Override
  public Element toElement(final Document document) {
    return super.toElement(document);
  }

  /**
   Calculate new guidelines and create new organization events with the updated goal parameters for each participant.

   @param guidelines - the guidelines
   */
  public synchronized void updateAssociatedForecasterGoal(final IFeederGuidelines guidelines) {
    // create new organization events
    List<OrganizationEvent> newEvents = new ArrayList<>();
    Map<UniqueIdentifier, Object> goals = new HashMap<>();
    goals.put(AgentGoalParameters.feederGuidelines, guidelines);
  }

//  public boolean updateMostRecentData(IPowerMessage localMessage) {
//    if (localMessage == null) {
//      return false;
//    }
//    UniqueIdentifier sender = localMessage.getLocalSender();
//    InstanceGoal<?> goalID = this.getInstanceGoal(sender);
//    if (goalID == null) {
//      return false;
//    }
//
//    IPowerMessageContent msgContent = (IPowerMessageContent) localMessage.getContent();
//    if (this.allParticipantData == null) {
//      this.allParticipantData = new HashMap<>();
//    }
//    ArrayBlockingQueue<IPowerMessageContent> participantDataQueue;
//    if (this.allParticipantData.containsKey(sender)) {
//      participantDataQueue = (ArrayBlockingQueue<IPowerMessageContent>) this.allParticipantData
//              .get(goalID);
//    } else {
//      NUMBER_OF_PARTICPANT_MESSAGES_TO_STORE = owner.getOrganization().getRegisteredParties();
//      participantDataQueue = new ArrayBlockingQueue<>(NUMBER_OF_PARTICPANT_MESSAGES_TO_STORE);
//    }
//    participantDataQueue.add(msgContent);
//    this.allParticipantData.put(goalID, participantDataQueue);
//
//    return true;
//
//  }

}
