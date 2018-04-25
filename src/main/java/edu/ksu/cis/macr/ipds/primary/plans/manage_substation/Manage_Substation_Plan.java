package edu.ksu.cis.macr.ipds.primary.plans.manage_substation;

import edu.ksu.cis.macr.obaa_pp.ec.base.IExecutor;
import edu.ksu.cis.macr.obaa_pp.ec.plans.AbstractExecutablePlan;
import edu.ksu.cis.macr.organization.model.InstanceGoal;


public class Manage_Substation_Plan extends AbstractExecutablePlan {

  public Manage_Substation_Plan() {
    getStateMachine().setCurrentState(Manage_Substation_Init.INSTANCE);
  }

  @Override
  public synchronized void execute(IExecutor ec, InstanceGoal<?> ig) {
    getStateMachine().update(ec, ig);
  }
}
