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
import edu.ksu.cis.macr.ipds.primary.goals.AgentGoalEvents;
import edu.ksu.cis.macr.ipds.primary.goals.AgentGoalParameters;
import edu.ksu.cis.macr.ipds.primary.guidelines.ILateralGuidelines;
import edu.ksu.cis.macr.ipds.primary.guidelines.ISubstationGuidelines;
import edu.ksu.cis.macr.ipds.primary.guidelines.LateralGuidelines;
import edu.ksu.cis.macr.ipds.primary.messages.IPowerMessage;
import edu.ksu.cis.macr.ipds.primary.messages.IPowerMessageContent;
import edu.ksu.cis.macr.ipds.primary.sensors.smartmeter.ISmartMeterRead;
import edu.ksu.cis.macr.obaa_pp.events.IOrganizationEvent;
import edu.ksu.cis.macr.obaa_pp.events.OrganizationEvent;
import edu.ksu.cis.macr.obaa_pp.events.OrganizationEventType;
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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 The {@code ManageSubstationCapability} provides the functionality necessary to distribute combined load guidelines among
 participants in this local organization as well as pass along the power quality guidelines.
 */
public class ManageSubstationCapability extends AbstractOrganizationCapability {

  private static final Logger LOG = LoggerFactory.getLogger(ManageSubstationCapability.class);
  private static final boolean debug = false;
  private final IPersona ec;
  private int NUMBER_OF_PARTICPANT_MESSAGES_TO_STORE = 1;
  private Map<InstanceGoal<?>, BlockingQueue<IPowerMessageContent>> allParticipantData;
  private ISubstationGuidelines substationGuidelines;
  private double currentTotal;
  private double maxKW = 0.0;
  private double maxVoltageMultiplier = 0.0;
  private double minKW = 0.0;
  private double minVoltageMultiplier = 0.0;
  private double netDeltaP = 0.0;
  private IConnections childConnections;

  /**
   Construct a new {@code ManageSubstationCapability} instance.

   @param owner - the agent possessing this capability.
   @param org - the local organization in which this agent operates.
   */
  public ManageSubstationCapability(final IPersona owner, final IOrganization org) {
    super(ManageSubstationCapability.class, owner, org);
    this.ec = Objects.requireNonNull(owner);
  }

  public IConnections getChildConnections() {
    return childConnections;
  }

  public synchronized void setChildConnections(IConnections childConnections) {
    this.childConnections = childConnections;
  }

  /**
   @return the combinedMaxKW
   */
  public double getCombinedMaxKW() {
    return this.substationGuidelines.getMaxKW();

  }
  public ISubstationGuidelines getSubstationGuidelines() {
    return this.substationGuidelines;
  }


  /**
   Set the combinedMaxKW for this local organization (part of the combinedLoadGuidelines.)

   @param combinedMaxKW the combinedMaxKW to set
   */
  public synchronized void setCombinedMaxKW(double combinedMaxKW) {
    this.substationGuidelines.setMaxKW(combinedMaxKW);
  }

  /**
   @return the combinedMinKW
   */
  public double getCombinedMinKW() {
    return this.substationGuidelines.getMinKW();
  }

  /**
   Set the combinedMinKW for this local organization (part of the combinedLoadGuidelines.)

   @param combinedMinKW the combinedMinKW to set
   */
  public synchronized void setCombinedMinKW(double combinedMinKW) {
    this.substationGuidelines.setMinKW(combinedMinKW);
  }

//  /*
//   * Puts together a map with the current MeterRead for each agent within the
//   * lateral that possesses the smart meter capability.
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
//        reading = ec.getCapability(
//                SmartMeterCapability.class).sense();
//      } catch (UnknownSensorException | SensorUnavailableException | IOException | ClassNotFoundException e) {
//        LOG.error("Error getting smart meter reading. {} {}", e.getClass().toString(),e.getMessage());
//        System.exit(-76);
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
    // if (debug) LOG.debug("Beginning Meter reads for Local Organization: "
    // + meters.entrySet().size() + " entries");
    for (Entry<String, ISmartMeterRead> entry : meters.entrySet()) {
      // if (debug) LOG.debug("Reading entry: value is " +
      // entry.getValue().getGridKW()
      // + " KW");
      totalRead += entry.getValue().getElectricalData().getAllPhase_Pgrid();
    }
    // if (debug) LOG.debug("Total Meter Read for this local organization: " +
    // totalRead
    // + " KW");

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

  // public LoadGuidelines getInitialLoadGuidelines(int numProsumers2) {
  // // TODO Auto-generated method stub
  // return null;
  // }
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


  /**
   @param maxVoltageMultiplier the maxVoltageMultiplier to set
   */
  public synchronized void setMaxVoltageMultiplier(double maxVoltageMultiplier) {
    this.maxVoltageMultiplier = maxVoltageMultiplier;
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

  // TODO: This also updates current total - may want to move that out
  private Map<InstanceGoal<?>, IPowerMessageContent> getMostRecentReadings() {

    Map<InstanceGoal<?>, IPowerMessageContent> currentReads = new HashMap<>();
    this.currentTotal = 0.0;

    for (Entry<InstanceGoal<?>, BlockingQueue<IPowerMessageContent>> igoal : this
            .allParticipantData
            .entrySet()) {
      BlockingQueue<IPowerMessageContent> participantHistory = igoal
              .getValue();
      IPowerMessageContent lastReading = participantHistory.peek();
      currentReads.put(igoal.getKey(), lastReading);
      this.currentTotal += lastReading.getActualKW();
    }
    return currentReads;
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


  public synchronized void setSubstationGuidelines(ISubstationGuidelines value) {
    this.substationGuidelines = value;

  }

//  private InstanceGoal<?> getTriggeredGoal(final InstanceGoal<?> instanceGoal) {
//    // TODO: getTriggeredGoal needs to be tested
//    IGoalModel g = this.owner.getControlComponent().getGoalModel();
//    Set<InstanceGoal<InstanceParameters>> triggeredGoals = g
//            .getTriggeredInstanceGoals();
//    // Set<InstanceGoal<InstanceParameters>> igoal =
//    // this.ec.getControlComponent().getGoalModel().);
//    return null;// (InstanceGoal<?>) triggeredGoals.iterator();
//  }


  /**
   Get the parameters from this instance goal and use them to initialize the capability.

   @param instanceGoal - this instance of the specification goal
   */
  public synchronized void initializeFromGoal(InstanceGoal<?> instanceGoal) {
    // Get the parameter values from the existing active instance goal
    final InstanceParameters params = Objects
            .requireNonNull((InstanceParameters) instanceGoal
                    .getParameter());
    final ISubstationGuidelines g = Objects
            .requireNonNull((ISubstationGuidelines) params.getValue(StringIdentifier
                    .getIdentifier("substationGuidelines")));

    // Set the goal parameter guidelines
    this.setSubstationGuidelines(g);
    IConnections cc = (IConnections) params.getValue(StringIdentifier.getIdentifier("childConnections"));
    this.setChildConnections(cc);
    if (cc != null) {
      if (debug)
        LOG.debug("There are {} authorized connections to sub holons.", cc.getListConnectionGuidelines().size());
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

    this.substationGuidelines = Objects
            .requireNonNull((ISubstationGuidelines) params
                    .getValue(StringIdentifier
                            .getIdentifier("substationGuidelines")));
    LOG.info("Lateral begins with substationGuidelines of {}", substationGuidelines.toString());
  }

  /**
   Set the combined load guidelines for this local organization.

   @param combinedMinKW - minimum KW
   @param combinedMaxKW - maximum KW
   */
  public synchronized void setSubstationGuidelines(double combinedMinKW,
                                      double combinedMaxKW) {
    this.substationGuidelines.setMinKW(combinedMinKW);
    this.substationGuidelines.setMaxKW(combinedMaxKW);

  }

  @Override
  public Element toElement(final Document document) {
    return super.toElement(document);
  }


  /*
   * Called when goal parameters need to be updated within the system.
   *
   * Reassigns the combinedMaxKW based on the percentage of current use by an
   * agent.
   */
  public List<IOrganizationEvent> updateGoalParameters(
          final Map<InstanceGoal<?>, Double> newGuidelines) {
    List<IOrganizationEvent> oes = new ArrayList<>();

    for (Entry<InstanceGoal<?>, Double> read : newGuidelines.entrySet()) {
      Map<UniqueIdentifier, Object> goals = new HashMap<>();

      ILateralGuidelines g = new LateralGuidelines();
      goals.put(AgentGoalParameters.lateralGuidelines, g);


      InstanceParameters instanceParams = new InstanceParameters(goals);

      IOrganizationEvent oe = new OrganizationEvent(
              OrganizationEventType.GOAL_MODEL_MODIFICATION, AgentGoalEvents.manage,
              read.getKey(), instanceParams);
      oes.add(oe);

    }
    return oes;
  }

  public boolean updateMostRecentData(IPowerMessage localMessage) {
    if (localMessage == null) {
      return false;
    }
    UniqueIdentifier sender = localMessage.getLocalSender();
    InstanceGoal<?> goalID = this.getInstanceGoal(sender);
    if (goalID == null) {
      return false;
    }

    IPowerMessageContent msgContent = (IPowerMessageContent) localMessage
            .getContent();

    if (this.allParticipantData == null) {
      this.allParticipantData = new HashMap<>();
    }

    ArrayBlockingQueue<IPowerMessageContent> participantDataQueue;
    if (this.allParticipantData.containsKey(sender)) {
      participantDataQueue = (ArrayBlockingQueue<IPowerMessageContent>) this.allParticipantData
              .get(goalID);
    } else {
      NUMBER_OF_PARTICPANT_MESSAGES_TO_STORE = owner.getOrganization().getRegisteredParties();
      participantDataQueue = new ArrayBlockingQueue<>(NUMBER_OF_PARTICPANT_MESSAGES_TO_STORE);
    }
    participantDataQueue.add(msgContent);
    this.allParticipantData.put(goalID, participantDataQueue);

    return true;

  }


}
