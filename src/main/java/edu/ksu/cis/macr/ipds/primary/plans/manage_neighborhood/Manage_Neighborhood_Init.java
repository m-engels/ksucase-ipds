package edu.ksu.cis.macr.ipds.primary.plans.manage_neighborhood;


import edu.ksu.cis.macr.ipds.config.RunManager;
import edu.ksu.cis.macr.ipds.primary.capabilities.manage.ManageNeighborhoodCapability;
import edu.ksu.cis.macr.ipds.primary.capabilities.participate.ParticipateCapability;
import edu.ksu.cis.macr.obaa_pp.ec.base.IExecutor;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IExecutablePlan;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IPlanState;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 This is the first step in the plan.  It performs initialization tasks once at the beginning of the plan.  When complete,
 it will move to the main working state.
 */
public enum Manage_Neighborhood_Init implements IPlanState<Manage_Neighborhood_Plan> {
  INSTANCE;
  private static final Logger LOG = LoggerFactory.getLogger(Manage_Neighborhood_Init.class);
  private static final boolean debug = false;

  @Override
  public synchronized void Enter(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
    // nothing
  }

  @Override
  public synchronized void Execute(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
    if (debug) LOG.debug("Starting with instance goal: {}.", ig);
    Objects.requireNonNull(ig);
    Objects.requireNonNull(ec);
    Objects.requireNonNull(ec.getCapability(ManageNeighborhoodCapability.class), "Role needs ManageNeighborhoodCapability");

    plan.heartBeat(this.getClass().getName());

    if (RunManager.isInitiallyConnected()) {

        // if this is the real agent doing the work
        if (ec.getCapability(ParticipateCapability.class) != null) {

            // set guidelines from top-down instance goals
            ec.getCapability(ManageNeighborhoodCapability.class).setGuidelines(ig);

            if (debug) LOG.debug("Changing state.");
            plan.getStateMachine().changeState(Manage_Neighborhood.INSTANCE, ec, ig);


            if (RunManager.isStopped()) {
                plan.getStateMachine().changeState(Manage_Neighborhood_Stop.INSTANCE, ec, ig);
            }
        }

    }

  }

  @Override
  public synchronized void Exit(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
    // Nothing
  }
}
