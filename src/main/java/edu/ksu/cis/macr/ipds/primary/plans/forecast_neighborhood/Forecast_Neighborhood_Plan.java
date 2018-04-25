package edu.ksu.cis.macr.ipds.primary.plans.forecast_neighborhood;

import edu.ksu.cis.macr.obaa_pp.ec.base.IExecutor;
import edu.ksu.cis.macr.obaa_pp.ec.plans.AbstractExecutablePlan;
import edu.ksu.cis.macr.organization.model.InstanceGoal;


public class Forecast_Neighborhood_Plan extends AbstractExecutablePlan {

  public Forecast_Neighborhood_Plan() {
    getStateMachine().setCurrentState(Forecast_Neighborhood_Init.Instance());
  }

  @Override
  public synchronized void execute(IExecutor ec,
                      InstanceGoal<?> ig) {
    getStateMachine().update(ec, ig);
  }
}
