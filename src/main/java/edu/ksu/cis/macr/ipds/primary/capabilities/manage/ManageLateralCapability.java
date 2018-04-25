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
import edu.ksu.cis.macr.ipds.primary.guidelines.LateralGuidelines;
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
 The {@code ManageFeederCapability} provides the functionality necessary to distribute combined load guidelines among
 participants in this local organization as well as pass along the power quality guidelines.
 */
public class ManageLateralCapability extends AbstractOrganizationCapability {
  private static final Logger LOG = LoggerFactory.getLogger(ManageLateralCapability.class);
  private static final boolean debug = false;
  private final IPersona ec;
  private int NUMBER_OF_PARTICPANT_MESSAGES_TO_STORE = 1;
  private Map<InstanceGoal<?>, BlockingQueue<IPowerMessageContent>> allParticipantData;
  private ILateralGuidelines lateralGuidelines;
  private double currentTotal;
  private double maxKW = 0.0;
  private double maxVoltageMultiplier = 0.0;
  private double minKW = 0.0;
  private double minVoltageMultiplier = 0.0;
  private double netDeltaP = 0.0;
  private IConnections parentConnections;
  private IConnections childConnections;

  /**
   Construct a new {@code ManageFeederCapability} instance.

   @param owner - the agent possessing this capability.
   @param org - the local organization in which this agent operates.
   */
  public ManageLateralCapability(final IPersona owner, final IOrganization org) {
    super(ManageLateralCapability.class, owner, org);
    this.ec = Objects.requireNonNull(owner);
  }

  public List<IOrganizationEvent> createParticipantEvents(
          InstanceGoal<?> instanceGoal,
          Map<UniqueIdentifier, Object> participantParams) {

    final InstanceParameters params = new InstanceParameters(
            participantParams);
    final List<IOrganizationEvent> orgEvents = new ArrayList<>();

    for (int i = 1; i <= this.getLocalRegisteredProsumers().size(); i++) {
      final IOrganizationEvent event = new OrganizationEvent(
              OrganizationEventType.EVENT, AgentGoalEvents.manage,
              instanceGoal, params);
      if (debug) LOG.debug("Created new organization event in plan: {}",
              event.toString());
      orgEvents.add(event);
    }
    return orgEvents;
  }

  public IConnections getChildConnections() {
    return childConnections;
  }

  public synchronized void setChildConnections(IConnections childConnections) {
    this.childConnections = childConnections;
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
//       LOG.error("Error getting smart meter reading. {} {}", e.getClass().toString(),e.getMessage());
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

  public Map<UniqueIdentifier, Object> getInitialParticipantGoalParameters() {

    Set<Agent<?>> prosumers = this.getLocalRegisteredProsumers();
    int numProsumers = prosumers.size();
    if (numProsumers == 0) {
      return null;
    }
    final double minKW = lateralGuidelines.getMinKW() / numProsumers;
    final double maxKW = lateralGuidelines.getMaxKW() / numProsumers;
    if (maxKW < 0.1) {
      LOG.error("ERROR: The initial maxKW for participants is zero.");
      System.exit(-1);
    }
    LOG.info("Each agent starts with {} of {} combined KW", maxKW, lateralGuidelines.getMaxKW());
    final Map<UniqueIdentifier, Object> participantGoalParameters = new HashMap<>();
    ILateralGuidelines g = new LateralGuidelines();
    participantGoalParameters.put(AgentGoalParameters.lateralGuidelines,
            g);

    return participantGoalParameters;
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

  public ILateralGuidelines getLateralGuidelines() {
    return this.lateralGuidelines;
  }

  public synchronized void setLateralGuidelines(ILateralGuidelines value) {
    this.lateralGuidelines = value;

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

  public IConnections getParentConnections() {
    return parentConnections;
  }

  public synchronized void setParentConnections(IConnections parentConnections) {
    this.parentConnections = parentConnections;
  }

//  private InstanceGoal<?> getTriggeredGoal(final InstanceGoal<?> instanceGoal) {
//    // TODO: getTriggeredGoal needs to be tested
//    IGoalModel g = this.owner.getControlComponent()
//            .getGoalModel();
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
    final ILateralGuidelines g = Objects
            .requireNonNull((ILateralGuidelines) params.getValue(StringIdentifier
                    .getIdentifier("lateralGuidelines")));
    // Set the goal parameter guidelines
    this.setLateralGuidelines(g);
    IConnections pc = (IConnections) params.getValue(StringIdentifier.getIdentifier("parentConnections"));
    this.setParentConnections(pc);
    if (pc != null) {
      if (debug)
        LOG.debug("There are {} authorized connections to super holons.", pc.getListConnectionGuidelines().size());
    }
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

    this.lateralGuidelines = Objects
            .requireNonNull((ILateralGuidelines) params
                    .getValue(StringIdentifier
                            .getIdentifier("lateralGuidelines")));
    LOG.info("Lateral begins with lateralGuidelines of {}", lateralGuidelines.toString());
  }

  /**
   Set the combined load guidelines for this local organization.

   @param combinedMinKW - minimum KW
   @param combinedMaxKW - maximum KW
   */
  public synchronized void setLateralGuidelines(double combinedMinKW,
                                   double combinedMaxKW) {
    this.lateralGuidelines.setMinKW(combinedMinKW);
    this.lateralGuidelines.setMaxKW(combinedMaxKW);
  }

  @Override
  public Element toElement(final Document document) {
    return super.toElement(document);
  }

  /**
   Trigger a "Manage Load" goal for each prosumer participant in this local organization. Use the combined guidelines and
   distribute them among the registered participants.

   @param instanceGoal - the supervisor's goal instance
   */
  public synchronized void triggerManageGoals(final InstanceGoal<?> instanceGoal) {

    // Set some initial goal parameter guidelines for each participant

    final Map<UniqueIdentifier, Object> participantGoalParameters = this
            .getInitialParticipantGoalParameters();
    if (participantGoalParameters == null) {
      return;
    }

    // create an organization event to trigger each power participant's
    // manage goal

    final List<IOrganizationEvent> organizationEvents = this
            .createParticipantEvents(instanceGoal,
                    participantGoalParameters);

    // add each organization events to the control component's event list
    this.owner.getOrganizationEvents().addEventListToQueue(organizationEvents);
  }

  /**
   Calculate new guidelines and create new organization events with the updated goal parameters for each participant.

   @param guidelines - the guidelines
   */
  public synchronized void updateAssociatedForecasterGoal(final ILateralGuidelines guidelines) {
    // create new organization events
    List<OrganizationEvent> newEvents = new ArrayList<>();
    Map<UniqueIdentifier, Object> goals = new HashMap<>();
    goals.put(AgentGoalParameters.lateralGuidelines, guidelines);
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
//    IPowerMessageContent msgContent = (IPowerMessageContent) localMessage
//            .getContent();
//
//    if (this.allParticipantData == null) {
//      this.allParticipantData = new HashMap<>();
//    }
//
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
