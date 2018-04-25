package edu.ksu.cis.macr.ipds.market.plan_selector;

import edu.ksu.cis.macr.ipds.market.goals.MarketGoalIdentifiers;
import edu.ksu.cis.macr.ipds.market.plans.auction_power.Auction_Power_Plan;
import edu.ksu.cis.macr.ipds.market.plans.broker_power.Broker_Power_Plan;
import edu.ksu.cis.macr.ipds.market.roles.MarketRoleIdentifiers;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IExecutablePlan;
import edu.ksu.cis.macr.obaa_pp.ec_ps.IPlanSelector;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;

/**
 * Class that defines the logic for selecting the plan to perform a role assigned to achieve a goal.
 */
public class MarketPlanSelector implements IPlanSelector {


    public MarketPlanSelector() {
    }

    public static MarketPlanSelector createPlanSelector() {
        return new MarketPlanSelector();
    }

    /**
     * Returns the plan that will perform this role to achieve the desired goal.
     *
     * @param roleIdentifier - the assigned role
     * @param goalIdentifier - the goal to be achieved
     * @return - the execution plan to perform the role
     */
    public IExecutablePlan getPlan(final UniqueIdentifier roleIdentifier, final UniqueIdentifier goalIdentifier) {

        IExecutablePlan result = null;


        // Auction power role
        if (roleIdentifier.equals(MarketRoleIdentifiers.Auction_Power_Role) && goalIdentifier.equals(
                MarketGoalIdentifiers.Auction_Power)) {
            result = new Auction_Power_Plan();
        } else if (roleIdentifier.equals(MarketRoleIdentifiers.Broker_Power_Role) && goalIdentifier.equals(
                MarketGoalIdentifiers.Broker_Power)) {
            result = new Broker_Power_Plan();
        }


        return result;
    }


}
