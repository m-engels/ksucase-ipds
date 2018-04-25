package edu.ksu.cis.macr.ipds.primary.capabilities.manage;

import edu.ksu.cis.macr.aasis.agent.persona.AbstractOrganizationCapability;
import edu.ksu.cis.macr.aasis.agent.persona.IOrganization;
import edu.ksu.cis.macr.aasis.agent.persona.IPersona;
import edu.ksu.cis.macr.aasis.common.IConnections;
import edu.ksu.cis.macr.goal.model.InstanceParameters;
import edu.ksu.cis.macr.ipds.primary.guidelines.IAgentGuidelines;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import edu.ksu.cis.macr.organization.model.identifiers.StringIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Objects;


/**
 The {@code ManageHomeCapability} provides the functionality necessary to manage power quality guidelines).
 */
public class ManageCapability extends AbstractOrganizationCapability {
  private static final Logger LOG = LoggerFactory.getLogger(ManageCapability.class);
  private static final boolean debug = false;

  private IAgentGuidelines homeGuidelines;
  private InstanceParameters params;
  private IPersona owner;


  /**
   Construct a new {@code ManageHomeCapability} instance.

   @param owner - the agent possessing this capability.
   @param org - the immediate organization in which this agent operates.
   */
  public ManageCapability(final IPersona owner, final IOrganization org) {
    super(ManageCapability.class, owner, org);
    this.owner = Objects.requireNonNull(owner);
  }

  @Override
  public double getFailure() {
    return 0;
  }

  public IAgentGuidelines getHomeGuidelines() {
    return homeGuidelines;
  }

  public synchronized void setHomeGuidelines(IAgentGuidelines homeGuidelines) {
    this.homeGuidelines = homeGuidelines;
  }

//  private InstanceGoal<?> getTriggeredGoal(final InstanceGoal<?> instanceGoal) {
//    // TODO: getTriggeredGoal needs to be tested
//    IGoalModel g = this.owner.getControlComponent().getGoalModel();
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
    final InstanceParameters params = Objects.requireNonNull((InstanceParameters) instanceGoal.getParameter());
    final IAgentGuidelines hg = Objects.requireNonNull((IAgentGuidelines) params.getValue(StringIdentifier.getIdentifier("homeGuidelines")));

    // Set the goal parameter guidelines
    this.setHomeGuidelines(hg);

    IConnections pc = (IConnections) params.getValue(StringIdentifier.getIdentifier("parentConnections"));
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
    final InstanceParameters params = Objects.requireNonNull((InstanceParameters) instanceGoal.getParameter());
    final IAgentGuidelines hg = Objects.requireNonNull((IAgentGuidelines) params.getValue(StringIdentifier.getIdentifier("homeGuidelines")));

    // Set the goal parameter guidelines
    this.setHomeGuidelines(hg);

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
}
