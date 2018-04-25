package edu.ksu.cis.macr.ipds.market.plans.auction_power;


import edu.ksu.cis.macr.obaa_pp.ec.base.IExecutor;
import edu.ksu.cis.macr.obaa_pp.ec.plans.AbstractExecutablePlan;
import edu.ksu.cis.macr.organization.model.InstanceGoal;


public class Auction_Power_Plan extends AbstractExecutablePlan {

    public Auction_Power_Plan() {
        getStateMachine().setCurrentState(Auction_Power_Init.INSTANCE);
    }

    @Override
    public synchronized void execute(IExecutor ec, InstanceGoal<?> ig) {
        getStateMachine().update(ec, ig);
    }
}
