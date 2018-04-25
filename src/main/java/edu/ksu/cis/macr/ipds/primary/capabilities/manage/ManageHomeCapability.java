package edu.ksu.cis.macr.ipds.primary.capabilities.manage;

import edu.ksu.cis.macr.aasis.agent.persona.AbstractOrganizationCapability;
import edu.ksu.cis.macr.aasis.agent.persona.IInternalCommunicationCapability;
import edu.ksu.cis.macr.aasis.agent.persona.IOrganization;
import edu.ksu.cis.macr.aasis.agent.persona.IPersona;
import edu.ksu.cis.macr.aasis.common.IConnections;
import edu.ksu.cis.macr.goal.model.InstanceParameters;
import edu.ksu.cis.macr.ipds.primary.guidelines.IHomeGuidelines;
import edu.ksu.cis.macr.ipds.primary.messages.*;
import edu.ksu.cis.macr.ipds.primary.sensors.smartmeter.IElectricalData;
import edu.ksu.cis.macr.ipds.primary.sensors.smartmeter.ISmartMeterRead;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import edu.ksu.cis.macr.organization.model.identifiers.StringIdentifier;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Objects;


/**
 The {@code ManageHomeCapability} provides the functionality necessary to manage power quality guidelines).
 */
public class ManageHomeCapability extends AbstractOrganizationCapability {

  private static final Logger LOG = LoggerFactory
          .getLogger(ManageHomeCapability.class);
  private static final boolean debug = false;
  private IHomeGuidelines homeGuidelines;
  private IConnections parentConnections;
  private IPersona owner;
  private double maxKW = 0.0;
  private double maxVoltageMultiplier = 0.0;
  private double minKW = 0.0;
  private double minVoltageMultiplier = 0.0;
  private double netDeltaP = 0.0;


  /**
   Construct a new {@code ManageHomeCapability} instance.

   @param owner - the agent possessing this capability.
   @param org - the immediate organization in which this agent operates.
   */
  public ManageHomeCapability(final IPersona owner, final IOrganization org) {
    super(ManageHomeCapability.class, owner, org);
    this.owner = Objects.requireNonNull(owner);
  }

  @Override
  public double getFailure() {
    return 0;
  }

  public IHomeGuidelines getHomeGuidelines() {
    return homeGuidelines;
  }

  public synchronized void setHomeGuidelines(IHomeGuidelines homeGuidelines) {
    this.homeGuidelines = homeGuidelines;
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

   @return the net delta P of the community request.
   */
  public double getNetDeltaP() {
    return netDeltaP;
  }

  /**
   Set the net delta P for the community request. A positive net delta P is a request for additional smart inverter
   reactive power.  A negative net delta P is a request to reduce the smart inverter reactive power.

   @param netDeltaP - the net delta P of the community request
   */
  public synchronized void setNetDeltaP(double netDeltaP) {
    this.netDeltaP = netDeltaP;
  }

//  private InstanceGoal<?> getTriggeredGoal(final InstanceGoal<?> instanceGoal) {
//    // TODO: getTriggeredGoal needs to be tested
//    IGoalModel g = this.owner.getControlComponent()
//            .getGoalModel();
//    Set<InstanceGoal<InstanceParameters>> triggeredGoals = g
//            .getTriggeredInstanceGoals();
//    // Set<InstanceGoal<InstanceParameters>> igoal =
//    // this.executionComponent.getControlComponent().getGoalModel().);
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
    final IHomeGuidelines hg = Objects
            .requireNonNull((IHomeGuidelines) params.getValue(StringIdentifier
                    .getIdentifier("homeGuidelines")));

    // Set the goal parameter guidelines
    this.setHomeGuidelines(hg);

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

   @param instanceGoal - this instance of the specification goal
   */
  public synchronized void setGuidelines(InstanceGoal<?> instanceGoal) {
    // Get the parameter values from the existing active instance goal
    final InstanceParameters params = Objects
            .requireNonNull((InstanceParameters) instanceGoal
                    .getParameter());
    final IHomeGuidelines hg = Objects
            .requireNonNull((IHomeGuidelines) params.getValue(StringIdentifier
                    .getIdentifier("homeGuidelines")));

    // Set the goal parameter guidelines
    this.setHomeGuidelines(hg);

  }

  public synchronized void setParentConnections(IConnections parentConnections) {
    this.parentConnections = parentConnections;
  }

  @Override
  public Element toElement(final Document document) {
    final Element capability = super.toElement(document);
    return capability;
  }

//  /**
//   Trigger an associated forecast goal.
//
//   @param instanceGoal - the instance goal that is triggering the new goal
//   */
//  public synchronized void triggerForecastGoal(final InstanceGoal<?> instanceGoal) {
//    // set up the associated goal instance parameters
//    final Map<UniqueIdentifier, Object> map = new HashMap<>();
//    map.put(GridGoalParameters.homeGuidelines, this.getHomeGuidelines());
//    final InstanceParameters instanceParams = new InstanceParameters(map);
//
//    // create a new organization event to trigger the forecast goal
//    final IOrganizationEvent organizationEvent = new OrganizationEvent(
//            OrganizationEventType.EVENT, GridGoalEvents.forecast, instanceGoal,
//            instanceParams);
//    if (debug) LOG.debug("Created NEW ORGANIZATION GOAL_MODEL_EVENT (to forecast): {}",
//            organizationEvent.toString());
//
//    // add the event to the organization events list
//    final List<IOrganizationEvent> eventManager = new ArrayList<>();
//    eventManager.add(organizationEvent);
//
//    // add the organization event list to the control component's event
//    // list
//    this.owner.getEventManager().addEventListToQueue(eventManager);
//    if (debug) LOG.debug("ADDED NEW ORGANIZATION GOAL_MODEL_EVENT (to forecast): {}",
//            organizationEvent.toString());
//  }
//
//  /**
//   Calculate new guidelines and create new organization events with the updated goal parameters for each participant.
//
//   @param instanceGoal - the instance goal triggering the new goal
//   @param guidelines - the goal guidelines to pass along to the forecaster
//   */
//  public synchronized void updateAssociatedForecasterGoal(
//          final InstanceGoal<?> instanceGoal, IHomeGuidelines guidelines) {
//    if (debug) LOG.debug("Starting forecast update: {}.", guidelines);
//    // create new organization events
//    List<OrganizationEvent> newEvents = new ArrayList<>();
//    Map<UniqueIdentifier, Object> goals = new HashMap<>();
//    goals.put(GridGoalParameters.homeGuidelines, guidelines);
//    if (debug) LOG.debug("Forecast updated: {}.", guidelines);
//  }


    public IPowerMessage createLocalPowerMessageForSelf(long timeSlice, double minKW, double maxKW,
                                                        ISmartMeterRead read, final IPowerAssessment power,
                                                        final Object quality) {
        IPowerMessageContent rptMessageContent = PowerMessageContent.createPowerMessageContent();
        rptMessageContent.setTimeSlice(timeSlice);
        rptMessageContent.setMaxKW(maxKW);
        rptMessageContent.setMinKW(minKW);
        rptMessageContent.setElectricalData(read.getElectricalData());
        rptMessageContent.setPreviousElectricalData(read.getPreviousElectricalData());

        if (debug)
            LOG.debug("Creating new power message for self at time slice {} with min={} and max={}", timeSlice, minKW, maxKW);

        IElectricalData now = read.getElectricalData();
        double gridKW = now.getAllPhase_Pgrid();
        rptMessageContent.setActualKW(gridKW);

        final UniqueIdentifier me = this.getOwner().getUniqueIdentifier();
        UniqueIdentifier localMaster = this.getOwner().getPersonaControlComponent().getLocalMaster();

        PowerPerformative perf = PowerPerformative.REPORT_OK;

        IPowerMessage localPowerMessage = PowerMessage.createLocal(me, localMaster, perf, rptMessageContent);
        LOG.debug("Power message created: {}.", localPowerMessage);
        return localPowerMessage;
    }


    public synchronized boolean sendLocal(IPowerMessage message) {
        if (debug) LOG.info(" sending local power messages {} kW from {} to {}", message
                .toString(), message.getLocalSender().toString(), message.getLocalReceiver().toString());

        // note: using the sendLocal method in IInternalCommunicationCapability
        boolean success = getOwner().getCapability(IInternalCommunicationCapability.class).sendLocal(
                message.getLocalReceiver(), this.getCommunicationChannelID(), message);
        if(!success){
            if (debug) LOG.debug(" sending local power message failed ");
        }
        return success;
    }
    private static final String COMMUNICATION_CHANNEL_ID = "PowerCommunicationChannel";

    public String getCommunicationChannelID() {
        return COMMUNICATION_CHANNEL_ID;
    }
}
