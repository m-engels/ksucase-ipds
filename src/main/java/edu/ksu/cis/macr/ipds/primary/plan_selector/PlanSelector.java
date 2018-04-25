package edu.ksu.cis.macr.ipds.primary.plan_selector;

import edu.ksu.cis.macr.ipds.primary.goals.AgentGoalIdentifiers;
import edu.ksu.cis.macr.ipds.primary.plans.forecast_feeder.Forecast_Feeder_Plan;
import edu.ksu.cis.macr.ipds.primary.plans.forecast_home.Forecast_Home_Plan;
import edu.ksu.cis.macr.ipds.primary.plans.forecast_lateral.Forecast_Lateral_Plan;
import edu.ksu.cis.macr.ipds.primary.plans.forecast_neighborhood.Forecast_Neighborhood_Plan;
import edu.ksu.cis.macr.ipds.primary.plans.manage_feeder.Manage_Feeder_Plan;
import edu.ksu.cis.macr.ipds.primary.plans.manage_home.Manage_Home_Plan;
import edu.ksu.cis.macr.ipds.primary.plans.manage_lateral.Manage_Lateral_Plan;
import edu.ksu.cis.macr.ipds.primary.plans.manage_neighborhood.Manage_Neighborhood_Plan;
import edu.ksu.cis.macr.ipds.primary.plans.manage_substation.Manage_Substation_Plan;
import edu.ksu.cis.macr.ipds.primary.roles.AgentRoles;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IExecutablePlan;
import edu.ksu.cis.macr.obaa_pp.ec_ps.IPlanSelector;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;

/**
 Class that defines the logic for selecting the plan to perform a role assigned to achieve a goal.
 */
public class PlanSelector implements IPlanSelector {


  public PlanSelector() {
  }

  public static PlanSelector createPlanSelector() {
    return new PlanSelector();
  }

  /**
   Returns the plan that will perform this role to achieve the desired goal.

   @param roleIdentifier - the assigned role
   @param goalIdentifier - the goal to be achieved
   @return - the execution plan to perform the role
   */
  public IExecutablePlan getPlan(final UniqueIdentifier roleIdentifier, final UniqueIdentifier goalIdentifier) {
    IExecutablePlan result = null;

      // sub goals combined into a higher plan: Manage Home
       if (roleIdentifier.equals(AgentRoles.Read_Smart_Meter_Role) && goalIdentifier.equals(
              AgentGoalIdentifiers.Sense_Smart_Meter)) {
          result = new Manage_Home_Plan();
      }
      else if (roleIdentifier.equals(AgentRoles.Self_Report_Power_Role) && goalIdentifier.equals(
               AgentGoalIdentifiers.Self_Report_Power)) {
          result = new Manage_Home_Plan();
      }
      else if (roleIdentifier.equals(AgentRoles.Actuate_Smart_Inverter_Role) && goalIdentifier.equals(
               AgentGoalIdentifiers.Actuate_Smart_Inverter)) {
          result = new Manage_Home_Plan();
      }

      // if the goal is read smart meter and the role is manage home role
      else if (roleIdentifier.equals(AgentRoles.Manage_Home_Role) && goalIdentifier.equals(
               AgentGoalIdentifiers.Sense_Smart_Meter)) {
          result = new Manage_Home_Plan();
      }

      // if the goal is manage smart inverter and the role is manage home role
      else if (roleIdentifier.equals(AgentRoles.Manage_Home_Role) && goalIdentifier.equals(
               AgentGoalIdentifiers.Actuate_Smart_Inverter)) {
          result = new Manage_Home_Plan();
      }




      // Manage_Feeder_Role
      else if (roleIdentifier.equals(AgentRoles.Manage_Feeder_Role) &&
              goalIdentifier.equals(AgentGoalIdentifiers.Manage_Feeder)) {
          result = new Manage_Feeder_Plan();
      }

      // Manage_Home_Role
      else if (roleIdentifier.equals(
               AgentRoles.Manage_Home_Role) && goalIdentifier.equals(AgentGoalIdentifiers
              .Manage_Home)) {
          result = new Manage_Home_Plan();
      }

      //  Manage_Lateral_Role
      else if (roleIdentifier.equals(
               AgentRoles.Manage_Lateral_Role) &&
              goalIdentifier.equals(AgentGoalIdentifiers.Manage_Lateral)) {
          result = new Manage_Lateral_Plan();
      }

      // Manage_Neighborhood_Role
      else if (roleIdentifier.equals(
               AgentRoles.Manage_Neighborhood_Role) && goalIdentifier.equals
              (AgentGoalIdentifiers.Manage_Neighborhood)) {
          result = new Manage_Neighborhood_Plan();
      }

      // Manage_Substation_Role
      else if (roleIdentifier.equals(AgentRoles.Manage_Substation_Role) &&
              goalIdentifier.equals(AgentGoalIdentifiers.Manage_Substation)) {
          result = new Manage_Substation_Plan();
      }


      // Forecast_Feeder_Role
      else if (roleIdentifier.equals(AgentRoles.Forecast_Feeder_Role) &&
              goalIdentifier.equals(AgentGoalIdentifiers.Forecast_Feeder)) {
          result = new Forecast_Feeder_Plan();
      }

      // Forecast_Home_Role
      else if (roleIdentifier.equals(AgentRoles.Forecast_Home_Role) && goalIdentifier.equals
              (AgentGoalIdentifiers.Forecast_Home)) {
          result = new Forecast_Home_Plan();
      }

      // Forecast_Lateral_Role
      else if (roleIdentifier.equals(
               AgentRoles.Forecast_Lateral_Role)
              && goalIdentifier.equals(AgentGoalIdentifiers.Forecast_Lateral)) {
          result = new Forecast_Lateral_Plan();
      }

      // Forecast_Neighborhood_Role
      else if (roleIdentifier.equals(
               AgentRoles.Forecast_Neighborhood_Role) && goalIdentifier.equals
              (AgentGoalIdentifiers.Forecast_Neighborhood)) {
          result = new Forecast_Neighborhood_Plan();
      }


    return result;
  }




}
