package edu.ksu.cis.macr.ipds.grid.plan_selector;

import edu.ksu.cis.macr.ipds.grid.goals.GridGoalIdentifiers;
import edu.ksu.cis.macr.ipds.grid.plans.be_holon.Be_Holon_Plan;
import edu.ksu.cis.macr.ipds.grid.plans.be_super_holon.Be_Super_Holon_Plan;
import edu.ksu.cis.macr.ipds.grid.roles.GridRoleIdentifiers;
import edu.ksu.cis.macr.ipds.primary.plans.forecast_feeder.Forecast_Feeder_Plan;
import edu.ksu.cis.macr.ipds.primary.plans.forecast_home.Forecast_Home_Plan;
import edu.ksu.cis.macr.ipds.primary.plans.forecast_lateral.Forecast_Lateral_Plan;
import edu.ksu.cis.macr.ipds.primary.plans.forecast_neighborhood.Forecast_Neighborhood_Plan;
import edu.ksu.cis.macr.ipds.primary.plans.manage_feeder.Manage_Feeder_Plan;
import edu.ksu.cis.macr.ipds.primary.plans.manage_home.Manage_Home_Plan;
import edu.ksu.cis.macr.ipds.primary.plans.manage_lateral.Manage_Lateral_Plan;
import edu.ksu.cis.macr.ipds.primary.plans.manage_neighborhood.Manage_Neighborhood_Plan;
import edu.ksu.cis.macr.ipds.primary.plans.manage_substation.Manage_Substation_Plan;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IExecutablePlan;
import edu.ksu.cis.macr.obaa_pp.ec_ps.IPlanSelector;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;


/**
 Class that defines the logic for selecting the plan to perform a role assigned to achieve a goal.
 */
public class GridPlanSelector implements IPlanSelector {


  public GridPlanSelector() {
  }

  public static GridPlanSelector createPlanSelector() {
    return new GridPlanSelector();
  }

  /**
   Returns the plan that will perform this role to achieve the desired goal.

   @param roleIdentifier - the assigned role
   @param goalIdentifier - the goal to be achieved
   @return - the execution plan to perform the role
   */
  public IExecutablePlan getPlan(final UniqueIdentifier roleIdentifier, final UniqueIdentifier goalIdentifier) {

    IExecutablePlan result = null;

    //  Be_Holon_Role
    if (roleIdentifier.equals(GridRoleIdentifiers.Be_Holon_Role) && goalIdentifier.equals(
            GridGoalIdentifiers.Be_Holon)) {
      result = new Be_Holon_Plan();
    } else if (roleIdentifier.equals(GridRoleIdentifiers.Be_Sub_Feeder_Holon_Role) && goalIdentifier.equals(
            GridGoalIdentifiers.Be_Sub_Feeder_Holon)) {
      result = new Be_Holon_Plan();
    } else if (roleIdentifier.equals(GridRoleIdentifiers.Be_Sub_Lateral_Holon_Role) && goalIdentifier.equals(
            GridGoalIdentifiers.Be_Sub_Lateral_Holon)) {
      result = new Be_Holon_Plan();
    } else if (roleIdentifier.equals(GridRoleIdentifiers.Be_Super_Holon_Role) && goalIdentifier.equals(
            GridGoalIdentifiers.Be_Super_Holon)) {
      result = new Be_Super_Holon_Plan();
    }


    // sub goals combined into a higher plan: Manage Home
    else if (roleIdentifier.equals(GridRoleIdentifiers.Read_Smart_Meter_Role) && goalIdentifier.equals(
            GridGoalIdentifiers.Sense_Smart_Meter)) {
      result = new Manage_Home_Plan();
    }
    else if (roleIdentifier.equals(GridRoleIdentifiers.Self_Report_Power_Role) && goalIdentifier.equals(
            GridGoalIdentifiers.Self_Report_Power)) {
      result = new Manage_Home_Plan();
    }
    else if (roleIdentifier.equals(GridRoleIdentifiers.Actuate_Smart_Inverter_Role) && goalIdentifier.equals(
            GridGoalIdentifiers.Actuate_Smart_Inverter)) {
      result = new Manage_Home_Plan();
    }

    // if the goal is read smart meter and the role is manage home role
    else if (roleIdentifier.equals(GridRoleIdentifiers.Manage_Home_Role) && goalIdentifier.equals(
            GridGoalIdentifiers.Sense_Smart_Meter)) {
      result = new Manage_Home_Plan();
    }

    // if the goal is manage smart inverter and the role is manage home role
    else if (roleIdentifier.equals(GridRoleIdentifiers.Manage_Home_Role) && goalIdentifier.equals(
            GridGoalIdentifiers.Actuate_Smart_Inverter)) {
      result = new Manage_Home_Plan();
    }




    // Manage_Feeder_Role
    else if (roleIdentifier.equals(GridRoleIdentifiers.Manage_Feeder_Role) &&
            goalIdentifier.equals(GridGoalIdentifiers.Manage_Feeder)) {
      result = new Manage_Feeder_Plan();
    }

    // Manage_Home_Role
    else if (roleIdentifier.equals(
            GridRoleIdentifiers.Manage_Home_Role) && goalIdentifier.equals(GridGoalIdentifiers
            .Manage_Home)) {
      result = new Manage_Home_Plan();
    }

    //  Manage_Lateral_Role
    else if (roleIdentifier.equals(
            GridRoleIdentifiers.Manage_Lateral_Role) &&
            goalIdentifier.equals(GridGoalIdentifiers.Manage_Lateral)) {
      result = new Manage_Lateral_Plan();
    }

    // Manage_Neighborhood_Role
    else if (roleIdentifier.equals(
            GridRoleIdentifiers.Manage_Neighborhood_Role) && goalIdentifier.equals
            (GridGoalIdentifiers.Manage_Neighborhood)) {
      result = new Manage_Neighborhood_Plan();
    }

    // Manage_Substation_Role
    else if (roleIdentifier.equals(GridRoleIdentifiers.Manage_Substation_Role) &&
            goalIdentifier.equals(GridGoalIdentifiers.Manage_Substation)) {
      result = new Manage_Substation_Plan();
    }


    // Forecast_Feeder_Role
    else if (roleIdentifier.equals(GridRoleIdentifiers.Forecast_Feeder_Role) &&
            goalIdentifier.equals(GridGoalIdentifiers.Forecast_Feeder)) {
      result = new Forecast_Feeder_Plan();
    }

    // Forecast_Home_Role
    else if (roleIdentifier.equals(GridRoleIdentifiers.Forecast_Home_Role) && goalIdentifier.equals
            (GridGoalIdentifiers.Forecast_Home)) {
      result = new Forecast_Home_Plan();
    }

    // Forecast_Lateral_Role
    else if (roleIdentifier.equals(
            GridRoleIdentifiers.Forecast_Lateral_Role)
            && goalIdentifier.equals(GridGoalIdentifiers.Forecast_Lateral)) {
      result = new Forecast_Lateral_Plan();
    }

    // Forecast_Neighborhood_Role
    else if (roleIdentifier.equals(
            GridRoleIdentifiers.Forecast_Neighborhood_Role) && goalIdentifier.equals
            (GridGoalIdentifiers.Forecast_Neighborhood)) {
      result = new Forecast_Neighborhood_Plan();
    }


    return result;
  }




}
