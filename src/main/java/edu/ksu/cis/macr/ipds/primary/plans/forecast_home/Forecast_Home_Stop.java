package edu.ksu.cis.macr.ipds.primary.plans.forecast_home;

import edu.ksu.cis.macr.obaa_pp.ec.base.IExecutor;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IExecutablePlan;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IPlanState;
import edu.ksu.cis.macr.organization.model.InstanceGoal;

/**
 The last step in the plan. It allows for any functionality needed when exiting the plan.
 */
public class Forecast_Home_Stop implements IPlanState<Forecast_Home_Plan> {

  private static final Forecast_Home_Stop instance = new Forecast_Home_Stop();

  private Forecast_Home_Stop() {
  }

  /**
   @return the singleton instance
   */
  public static Forecast_Home_Stop Instance() {
    return instance;
  }

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
