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
import edu.ksu.cis.macr.ipds.primary.guidelines.HomeGuidelines;
import edu.ksu.cis.macr.ipds.primary.guidelines.IHomeGuidelines;
import edu.ksu.cis.macr.ipds.primary.guidelines.INeighborhoodGuidelines;
import edu.ksu.cis.macr.ipds.primary.messages.IPowerMessageContent;
import edu.ksu.cis.macr.ipds.primary.sensors.smartmeter.ISmartMeterRead;
import edu.ksu.cis.macr.obaa_pp.events.IOrganizationEvent;
import edu.ksu.cis.macr.obaa_pp.events.OrganizationEvent;
import edu.ksu.cis.macr.obaa_pp.events.OrganizationEventType;
import edu.ksu.cis.macr.organization.model.Agent;
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
 The {@code ManageNeighborhoodCapability} provides the functionality necessary to distribute combined load guidelines
 among participants in this local organization as well as pass along the power quality guidelines.
 */
public class ManageNeighborhoodCapability extends AbstractOrganizationCapability {

  private static final Logger LOG = LoggerFactory.getLogger(ManageNeighborhoodCapability.class);
  private static final boolean debug = false;
  private final IPersona ec;
  private int NUMBER_OF_PARTICPANT_MESSAGES_TO_STORE = 1;
  private Map<InstanceGoal<?>, BlockingQueue<IPowerMessageContent>> allParticipantData;
  private INeighborhoodGuidelines neighborhoodGuidelines;
  private double currentTotal;
  private IHomeGuidelines homeGuidelines;
  private double maxKW = 0.0;
  private double maxVoltageMultiplier = 0.0;
  private double minKW = 0.0;
  private double minVoltageMultiplier = 0.0;
  private double netDeltaP = 0.0;
  private IConnections parentConnections;
  private IConnections childConnections;

  /**
   Construct a new {@code ManageNeighborhoodCapability} instance.

   @param owner - the agent possessing this capability.
   @param org - the local organization in which this agent operates.
   */
  public ManageNeighborhoodCapability(final IPersona owner, final IOrganization org) {
    super(ManageNeighborhoodCapability.class, owner, org);
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
    return this.neighborhoodGuidelines.getMaxKW();

  }

  /**
   Set the combinedMaxKW for this local organization (part of the combinedLoadGuidelines.)

   @param combinedMaxKW the combinedMaxKW to set
   */
  public synchronized void setCombinedMaxKW(double combinedMaxKW) {
    this.neighborhoodGuidelines.setMaxKW(combinedMaxKW);
  }

  /**
   @return the combinedMinKW
   */
  public double getCombinedMinKW() {
    return this.neighborhoodGuidelines.getMinKW();
  }

  /**
   Set the combinedMinKW for this local organization (part of the combinedLoadGuidelines.)

   @param combinedMinKW the combinedMinKW to set
   */
  public synchronized void setCombinedMinKW(double combinedMinKW) {
    this.neighborhoodGuidelines.setMinKW(combinedMinKW);
  }

//  /*
//   * Puts together a map with the current MeterRead for each agent within the
//   * neighborhood that possesses the smart meter capability.
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
    for (Map.Entry<String, ISmartMeterRead> entry : meters.entrySet()) {
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

  public Map<UniqueIdentifier, Object> getInitialParticipantGoalParameters() {

    Set<Agent<?>> prosumers = this.getLocalRegisteredProsumers();
    int numProsumers = prosumers.size();
    if (numProsumers == 0) {
      return null;
    }

    final double maxKW = neighborhoodGuidelines.getMaxKW() / numProsumers;
    if (maxKW < 0.1) {
      LOG.error("ERROR: The initial maxKW for participants is zero.");
      System.exit(-1);
    }
    LOG.info("Each agent starts with {} of {} combined KW", maxKW, neighborhoodGuidelines.getMaxKW());
    final Map<UniqueIdentifier, Object> mapSubGuidelines = new HashMap<>();
    IHomeGuidelines hg = HomeGuidelines.createHomeGuidelines(neighborhoodGuidelines.getMinVoltageMultiplier(),
            neighborhoodGuidelines.getMaxVoltageMultiplier(), maxKW);
    mapSubGuidelines.put(AgentGoalParameters.homeGuidelines, hg);
    return mapSubGuidelines;
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

  /**
   Gets the set of local registered prosumer agents given the set of all agents. Do not include the supervisors (control
   component masters in this local organization) and do not include any independent forecaster agents (but a child that
   is also performing a forecast role should be included).

   @return - the set of all prosumer agents registered in this local organization (does not include
   other types of agents such as forecasters, etc)
   */
  public Set<Agent<?>> getLocalRegisteredProsumers() {
    // TODO: This is a hack. We may want to provide inheritance or an
    // explicit capability to indicate this agent is a prosumer agent. - DMC

    final Set<Agent<?>> allAgents = this.getOwner()
            .getPersonaControlComponent().getOrganizationModel().getAgents();

    final Set<Agent<?>> prosumers = new HashSet<>();

    for (Agent<?> agent : allAgents) {
      boolean isMaster = (agent.getIdentifier() == this.owner
              .getPersonaControlComponent().getLocalMaster());
      boolean isExternalForecaster = agent.getIdentifier().toString()
              .contains("_F");
      if (!isMaster && !isExternalForecaster) {
        prosumers.add(agent);
      }
    }
    return prosumers;
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

  public INeighborhoodGuidelines getNeighborhoodGuidelines() {
    return this.neighborhoodGuidelines;
  }

  public synchronized void setNeighborhoodGuidelines(INeighborhoodGuidelines value) {
    this.neighborhoodGuidelines = value;

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

  public IConnections getParentConnections() {
    return parentConnections;
  }

  public synchronized void setParentConnections(IConnections parentConnections) {
    this.parentConnections = parentConnections;
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
    final INeighborhoodGuidelines g = Objects
            .requireNonNull((INeighborhoodGuidelines) params.getValue(StringIdentifier
                    .getIdentifier("neighborhoodGuidelines")));

    // Set the goal parameter guidelines
    this.setNeighborhoodGuidelines(g);

    IConnections pc = (IConnections) params.getValue(StringIdentifier.getIdentifier("parentConnections"));
    this.setParentConnections(pc);
    if (pc != null) {
      if (debug) LOG.debug("{} authorized connections to super holons.", pc.getListConnectionGuidelines().size());
    }

    IConnections cc = (IConnections) params.getValue(StringIdentifier.getIdentifier("childConnections"));
    this.setChildConnections(cc);
    if (cc != null) {
      if (debug) LOG.debug("{} authorized connections to sub holons.", cc.getListConnectionGuidelines().size());
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
    try {

      // Get the parameter values from the existing active instance goal

      final InstanceParameters params = Objects
              .requireNonNull((InstanceParameters) instanceGoal
                      .getParameter());

      this.neighborhoodGuidelines = Objects
              .requireNonNull((INeighborhoodGuidelines) params
                      .getValue(StringIdentifier
                              .getIdentifier("neighborhoodGuidelines")));

    } catch (Exception ex) {
      LOG.error("ERROR: in set guidelines. {}", ex.getMessage());
      System.exit(-23);
    }
    LOG.info("Neighborhood begins with neighborhoodGuidelines of {}", neighborhoodGuidelines.toString());
  }

  /**
   Set the combined load guidelines for this local organization.

   @param combinedMinKW - minimum KW
   @param combinedMaxKW - maximum KW
   */
  public synchronized void setNeighborhoodGuidelines(double combinedMinKW,
                                        double combinedMaxKW) {
    this.neighborhoodGuidelines.setMinKW(combinedMinKW);
    this.neighborhoodGuidelines.setMaxKW(combinedMaxKW);

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
    try {
      for (Map.Entry<InstanceGoal<?>, Double> read : newGuidelines.entrySet()) {
        Map<UniqueIdentifier, Object> goals = new HashMap<>();

        IHomeGuidelines q = this.homeGuidelines;
        goals.put(AgentGoalParameters.homeGuidelines, q);

        InstanceParameters instanceParams = new InstanceParameters(goals);

        IOrganizationEvent oe = new OrganizationEvent(
                OrganizationEventType.GOAL_MODEL_MODIFICATION, AgentGoalEvents.manage,
                read.getKey(), instanceParams);
        oes.add(oe);

      }
    } catch (Exception ex) {
      LOG.error("ERROR: in update most recent data. {}", ex.getMessage());
      System.exit(-51);
    }
    return oes;
  }

//  public boolean updateMostRecentData(IPowerMessage localMessage) {
//    LOG.info("LOCAL POWER MESSAGE received: {}", localMessage);
//    try {
//      if (localMessage == null) {
//        if (debug) LOG.debug("Power message was null.");
//        return false;
//      }
//      UniqueIdentifier sender = localMessage.getLocalSender();
//      if (debug) LOG.debug("Power message sender identifier was: {}", sender);
//
//      InstanceGoal<?> goalID = this.getInstanceGoal(sender);
//      if (debug) LOG.debug("Power message goal from the sender was: {}", goalID);
//
//
//      if (goalID == null) {
//        if (debug) LOG.debug("Power message sender goalID was null.");
//        return false;
//      }
//
//      IPowerMessageContent msgContent = (IPowerMessageContent) localMessage.getContent();
//      if (debug) LOG.debug("Power message content was: {}", msgContent);
//
//      ArrayBlockingQueue<IPowerMessageContent> participantDataQueue;
//
//      if (this.allParticipantData == null) {
//        this.allParticipantData = new HashMap<>();
//      }
//
//      if (this.allParticipantData.containsKey(sender)) {
//        participantDataQueue = (ArrayBlockingQueue<IPowerMessageContent>) this.allParticipantData
//                .get(goalID);
//      } else {
//        NUMBER_OF_PARTICPANT_MESSAGES_TO_STORE = owner.getOrganization().getRegisteredParties();
//        participantDataQueue = new ArrayBlockingQueue<>(NUMBER_OF_PARTICPANT_MESSAGES_TO_STORE);
//      }
//      participantDataQueue.add(msgContent);
//      this.allParticipantData.put(goalID, participantDataQueue);
//
//
//    } catch (Exception ex) {
//      LOG.error("ERROR: in update most recent data. {}", ex.getMessage());
//      System.exit(-51);
//    }
//    return true;
//
//  }


}
