package edu.ksu.cis.macr.ipds.self.plans.self_control;

import edu.ksu.cis.macr.obaa_pp.ec.base.IExecutor;
import edu.ksu.cis.macr.obaa_pp.ec.plans.AbstractExecutablePlan;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IExecutablePlan;
import edu.ksu.cis.macr.organization.model.InstanceGoal;


public class Self_Control_Plan extends AbstractExecutablePlan implements IExecutablePlan {

  public Self_Control_Plan() {
    getStateMachine().setCurrentState(Self_Control_Init.INSTANCE);
  }

  @Override
  public synchronized void execute(IExecutor ec, InstanceGoal<?> ig) {
    getStateMachine().update(ec, ig);
  }
}
