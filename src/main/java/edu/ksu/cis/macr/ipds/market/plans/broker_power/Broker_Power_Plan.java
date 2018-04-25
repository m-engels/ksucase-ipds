package edu.ksu.cis.macr.ipds.market.plans.broker_power;

import edu.ksu.cis.macr.obaa_pp.ec.base.IExecutor;
import edu.ksu.cis.macr.obaa_pp.ec.plans.AbstractExecutablePlan;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IExecutablePlan;
import edu.ksu.cis.macr.organization.model.InstanceGoal;

public class Broker_Power_Plan extends AbstractExecutablePlan implements IExecutablePlan {

    public Broker_Power_Plan() {
        getStateMachine().setCurrentState(Broker_Power_Init.INSTANCE);
    }

    @Override
    public synchronized void execute(IExecutor ec, InstanceGoal<?> ig) {
        getStateMachine().update(ec, ig);
    }
}
