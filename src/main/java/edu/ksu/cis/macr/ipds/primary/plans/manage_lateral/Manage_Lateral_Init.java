package edu.ksu.cis.macr.ipds.primary.plans.manage_lateral;

import edu.ksu.cis.macr.ipds.config.RunManager;
import edu.ksu.cis.macr.ipds.primary.capabilities.manage.ManageLateralCapability;
import edu.ksu.cis.macr.ipds.primary.capabilities.participate.ParticipateCapability;
import edu.ksu.cis.macr.obaa_pp.ec.base.IExecutor;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IExecutablePlan;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IPlanState;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 This is the first step in the plan.  It performs initialization tasks once at the beginning of the plan. When complete,
 it will move to the main working state.
 */
public enum Manage_Lateral_Init implements IPlanState<Manage_Lateral_Plan> {
  INSTANCE;
  private static final Logger LOG = LoggerFactory.getLogger(Manage_Lateral_Init.class);
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
    Objects.requireNonNull(ec.getCapability(ManageLateralCapability.class), "Role needs ManageLateralCapability");

    plan.heartBeat(this.getClass().getName());

    if (RunManager.isInitiallyConnected()) {

      // if this is the real agent doing the work
      if (ec.getCapability(ParticipateCapability.class) != null) {

        // set guidelines from top-down instance goals
        ec.getCapability(ManageLateralCapability.class).initializeFromGoal(ig);
        if (debug) LOG.debug("agent: Set guidelines.");

      }
    }

    if (debug) LOG.debug("Changing state.");
    plan.getStateMachine().changeState(Manage_Lateral.INSTANCE, ec, ig);
  }

  @Override
  public synchronized void Exit(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
    // nothing
  }
}
