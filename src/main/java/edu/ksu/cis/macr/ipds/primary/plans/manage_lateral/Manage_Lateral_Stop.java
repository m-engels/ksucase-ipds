package edu.ksu.cis.macr.ipds.primary.plans.manage_lateral;

import edu.ksu.cis.macr.obaa_pp.ec.base.IExecutor;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IExecutablePlan;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IPlanState;
import edu.ksu.cis.macr.organization.model.InstanceGoal;

/**
 The last step in the plan. It allows for any functionality needed when exiting the plan.
 */
public enum Manage_Lateral_Stop implements IPlanState<Manage_Lateral_Plan> {
  INSTANCE;

  @Override
  public synchronized void Enter(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
    // Nothing
  }

  @Override
  public synchronized void Execute(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
    plan.setDone(true);
  }

  @Override
  public synchronized void Exit(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
    // Nothing
  }
}