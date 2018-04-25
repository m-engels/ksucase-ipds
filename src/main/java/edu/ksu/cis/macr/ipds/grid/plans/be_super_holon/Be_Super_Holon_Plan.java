package edu.ksu.cis.macr.ipds.grid.plans.be_super_holon;

import edu.ksu.cis.macr.obaa_pp.ec.base.IExecutor;
import edu.ksu.cis.macr.obaa_pp.ec.plans.AbstractExecutablePlan;
import edu.ksu.cis.macr.organization.model.InstanceGoal;


public class Be_Super_Holon_Plan extends AbstractExecutablePlan {

  public Be_Super_Holon_Plan() {
    getStateMachine().setCurrentState(Be_Super_Holon_Init.INSTANCE);
  }

  @Override
  public synchronized void execute(IExecutor ec, InstanceGoal<?> ig) {
    getStateMachine().update(ec, ig);
  }
}
