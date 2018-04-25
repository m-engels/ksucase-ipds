package edu.ksu.cis.macr.ipds.primary.plans.forecast_home;

import edu.ksu.cis.macr.goal.model.InstanceParameters;
import edu.ksu.cis.macr.ipds.config.RunManager;
import edu.ksu.cis.macr.ipds.primary.capabilities.manage.ForecastWeatherCapability;
import edu.ksu.cis.macr.ipds.primary.capabilities.manage.ManageHomeCapability;
import edu.ksu.cis.macr.ipds.primary.guidelines.IHomeGuidelines;
import edu.ksu.cis.macr.obaa_pp.ec.base.IExecutor;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IExecutablePlan;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IPlanState;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import edu.ksu.cis.macr.organization.model.identifiers.StringIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 This is the first step in the plan.  It performs initialization tasks once at the beginning of the plan.  When complete,
 it will move to the main working state.
 */
public class Forecast_Home_Init implements IPlanState<Forecast_Home_Plan> {
  private static final Forecast_Home_Init instance = new Forecast_Home_Init();

  private static final Logger LOG = LoggerFactory.getLogger(Forecast_Home_Init.class);

  private Forecast_Home_Init() {
  }

  /**
   @return the singleton instance
   */
  public static Forecast_Home_Init Instance() {
    return instance;
  }

  @Override
  public synchronized void Enter(final IExecutablePlan plan,
                    final IExecutor ec,
                    final InstanceGoal<?> ig) {
    // nothing
  }

  @Override
  public synchronized void Execute(final IExecutablePlan plan,
                      final IExecutor ec,
                      final InstanceGoal<?> ig) {

    Objects.requireNonNull(ig);
    Objects.requireNonNull(ec);
    LOG.debug(
            "Entering BEING_SLAVE Forecast_Home_Init with Instance Goal {}",
            ig.getInstanceIdentifier());


    Objects.requireNonNull(
            ec.getCapability(ForecastWeatherCapability.class),
            "local peer agent should have ForecastWeatherCapability");

    Objects.requireNonNull(
            ec.getCapability(ManageHomeCapability.class),
            "local peer agent should have ManageHomeCapability");


    // Get the parameter values from the existing active instance goal

    final InstanceParameters params = (InstanceParameters) ig
            .getParameter();

    ec
            .getCapability(ManageHomeCapability.class)
            .setHomeGuidelines(
                    (IHomeGuidelines) params.getValue(StringIdentifier
                            .getIdentifier("homeGuidelines")));


    // TODO: Implement forecasting process
    LOG.debug("Forecasting prosumer................");

    if ((RunManager.isStopped())) {
      plan.getStateMachine().changeState(
              Forecast_Home_Stop.Instance(), ec,
              ig);
    }
  }

  @Override
  public synchronized void Exit(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
    // Nothing
  }
}
