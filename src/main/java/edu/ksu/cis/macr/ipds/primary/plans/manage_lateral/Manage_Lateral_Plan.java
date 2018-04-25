package edu.ksu.cis.macr.ipds.primary.plans.manage_lateral;

import edu.ksu.cis.macr.obaa_pp.ec.base.IExecutor;
import edu.ksu.cis.macr.obaa_pp.ec.plans.AbstractExecutablePlan;
import edu.ksu.cis.macr.organization.model.InstanceGoal;


public class Manage_Lateral_Plan extends AbstractExecutablePlan {

  public Manage_Lateral_Plan() {
    getStateMachine().setCurrentState(Manage_Lateral_Init.INSTANCE);
  }

  @Override
  public synchronized void execute(IExecutor ec, InstanceGoal<?> ig) {
    getStateMachine().update(ec, ig);
  }
}
