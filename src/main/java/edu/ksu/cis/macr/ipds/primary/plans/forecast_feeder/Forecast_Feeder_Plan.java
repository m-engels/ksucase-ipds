package edu.ksu.cis.macr.ipds.primary.plans.forecast_feeder;


import edu.ksu.cis.macr.obaa_pp.ec.base.IExecutor;
import edu.ksu.cis.macr.obaa_pp.ec.plans.AbstractExecutablePlan;
import edu.ksu.cis.macr.organization.model.InstanceGoal;

/**
 The {@code Forecast_Feeder_Plan} is a simple and standard implementation of the {@code
AbstractExecutablePlan}.  This class is specified by the role model
 (exactly one plan per role). Standard naming conventions should be followed.
 */
public class Forecast_Feeder_Plan extends AbstractExecutablePlan {

  /**
   Construct the plan and set the initial plan state.
   */
  public Forecast_Feeder_Plan() {
    getStateMachine().setCurrentState(Forecast_Feeder_Init.Instance());
  }

  /**
   BEING_SLAVE the plan.

   @param ec - the executor of the plan
   @param ig - the instance of the specification goal
   */
  @Override
  public synchronized void execute(IExecutor ec,  InstanceGoal<?> ig) {
    getStateMachine().update(ec, ig);
  }
}
