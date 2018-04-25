package edu.ksu.cis.macr.ipds.grid.plans.be_super_holon;

import edu.ksu.cis.macr.ipds.config.RunManager;
import edu.ksu.cis.macr.ipds.grid.capabilities.admin.GridAdminCapability;
import edu.ksu.cis.macr.ipds.grid.capabilities.admin.GridControlSuperHolonCapability;
import edu.ksu.cis.macr.ipds.primary.capabilities.participate.ParticipateCapability;
import edu.ksu.cis.macr.obaa_pp.ec.base.IExecutor;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IExecutablePlan;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IPlanState;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 The {@code Be_Super_Holon_Super} step is the main working step in the {@code Be_Super_Holon_Plan}  If the list of
 registered agents changes, it will revert to the prior initialization ("init") state.  If it receives a stop messages,
 it will move to the stop state.
 */
public enum Be_Super_Holon implements IPlanState<Be_Super_Holon_Plan> {
  INSTANCE;
  private static final Logger LOG = LoggerFactory.getLogger(Be_Super_Holon.class);
  private static final boolean debug = false;

  @Override
  public synchronized void Enter(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
    // Nothing
  }

  @Override
  public synchronized void Execute(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
    if (debug) LOG.debug("Starting with instance goal: {}.", ig);
    Objects.requireNonNull(ig);
    Objects.requireNonNull(ec);
    Objects.requireNonNull(ec.getCapability(GridAdminCapability.class), "Role requires GridAdminCapability");
    Objects.requireNonNull(ec.getCapability(GridControlSuperHolonCapability.class), "Role requires SuperHolonCapability");


    // wait extra long
    plan.heartBeat(this.getClass().getName(), 10);


    // if I'm the "real" persona in the super holon agent, do real work
    if (ec.getCapability(ParticipateCapability.class) != null) {

      // get my custom goal guidelines
      ec.getCapability(GridAdminCapability.class).initializeGuidelines(ig);
        ec.getCapability(GridControlSuperHolonCapability.class).initializeGuidelines(ig);
      if (debug) LOG.debug("agent: initialized capabilities from goal.");

      final String myPersona = ec.getUniqueIdentifier().toString();
      if (debug) LOG.info("agent:  Super holon {} running organization. Will update participants", myPersona);

//      // update holons
//      IPowerMessageContent newContent = ec.getCapability(GridControlSuperHolonCapability.class).processGridPowerReports();
//      if (newContent != null) {
//        if (debug)
//          LOG.info("agent:  Super holon {} working as head of {} has new power reports: {} ", myPersona, myPersona, newContent.toString());
//
//        // build comparisons and assess trends
//        final IPowerAssessment power = ec.getCapability(AggregatePowerCapability.class).getPowerAssessment(newContent);
//        if (debug)
//          LOG.info("agent:  Super holon {} working as head of {} power assessment: {} ", myPersona, myPersona, power.toString());
//
//        final IPowerQualityAssessment quality = ec.getCapability(AggregatePowerCapability.class)
//                .getPowerQualityAssessment(newContent);
//        if (debug)
//          LOG.info("agent:  Super holon {} working as head of {} power quality assessment: {} ", myPersona, myPersona, quality.toString());
//
//        // create local power message for self (it will be from me to me since I'm master of my external organization)
//        IPowerMessage selfMessage = ec.getCapability(IPowerCommunicationCapability.class).createLocalPowerMessageForSuperSelf(newContent, power, quality);
//        if (selfMessage != null) {
//          if (debug)
//            LOG.info("agent:  Super holon {} created LOCAL GRID MESSAGE for self: {} ", myPersona, selfMessage.toString());
//
//          // send to self via external messaging system
//          boolean sent = ec.getCapability(IPowerCommunicationCapability.class).sendRemotePowerMessageAggregateToSuperSelf(selfMessage);
//          LOG.info("FORWARDED AGGREGATE GRID MESSAGE to SELF = {}. Original power message was: {}", sent, selfMessage.toString());
//        }
//      }
    }

    // if I'm the proxy persona running my new holonic organization, monitor assignments
    else {

      // get my custom goal guidelines
      ec.getCapability(GridControlSuperHolonCapability.class).initializeGuidelines(ig);
      if (debug) LOG.info("proxy: Acting as super holon. initializing capabiity guidelines.");


//      // update holons
//      IPowerMessageContent newContent = ec.getCapability(SuperHolonCapability.class).processGridPowerReports();
//      if (newContent != null) {
//        if (debug)     LOG.info("proxy:  Super holon {} working as head of {} has new aggregate content: {} ",  myPersona, myPersona, newContent.toString());
//
//        // build comparisons and assess trends
//        final IPowerAssessment power = ec.getCapability(AggregationCapability.class).getPowerAssessment(newContent);
//        if (debug)
//          LOG.info("proxy:  Super holon {} working as head of {} power assessment: {} ",  myPersona, myPersona, power.toString());
//
//        final IPowerQualityAssessment quality = ec.getCapability(AggregationCapability.class)
//                .getPowerQualityAssessment(newContent);
//        if (debug)
//          LOG.info("proxy:  Super holon {} working as head of {} power quality assessment: {} ",  myPersona, myPersona, quality.toString());
//
//        // create local power message for self (it will be from me to me since I'm master of my external organization)
//        IPowerMessage selfMessage = ec.getCapability(IPowerCommunicationCapability.class).createLocalPowerMessageForSuperSelf(newContent, power, quality);
//        if (selfMessage != null) {
//          if (debug)
//            LOG.info("proxy:  Super holon {} {} created local power message for self: {} ",  myPersona, selfMessage.toString());
//
//          // send to self via external messaging system
//          boolean sent = ec.getCapability(IPowerCommunicationCapability.class).sendRemotePowerMessageAggregateToSuperSelf(selfMessage);
//          LOG.info("FORWARDED AGGREGATE GRID MESSAGE to SELF = {}. Original power message was: {}", sent, selfMessage.toString());
        }


    if ((RunManager.isStopped())) {
      plan.getStateMachine().changeState(Be_Super_Holon_Stop.INSTANCE, ec, ig);
    }
  }

  @Override
  public synchronized void Exit(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
    // Nothing
  }
}
