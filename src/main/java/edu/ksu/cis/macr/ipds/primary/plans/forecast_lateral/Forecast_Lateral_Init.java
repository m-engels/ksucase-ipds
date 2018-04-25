package edu.ksu.cis.macr.ipds.primary.plans.forecast_lateral;

import edu.ksu.cis.macr.goal.model.InstanceParameters;
import edu.ksu.cis.macr.ipds.config.RunManager;
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
public class Forecast_Lateral_Init implements IPlanState<Forecast_Lateral_Plan> {
  private static final Forecast_Lateral_Init instance = new Forecast_Lateral_Init();

  private static final Logger LOG = LoggerFactory
          .getLogger(Forecast_Lateral_Init.class);

  private Forecast_Lateral_Init() {
  }

  /**
   @return the singleton instance
   */
  public static Forecast_Lateral_Init Instance() {
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

    LOG.debug("Entering plan with Instance Goal {}",
            ig.getInstanceIdentifier());
    Objects.requireNonNull(ec);
    LOG.debug("execution component {}", ec);

    // TODO: Get all parameter values from the existing active instance goal

    final InstanceParameters params = (InstanceParameters) ig
            .getParameter();

    final double minKW = (double) params.getValue(StringIdentifier
            .getIdentifier("minKW"));
    final double maxKW = (double) params.getValue(StringIdentifier
            .getIdentifier("maxKW"));
    final double minVoltageMultiplier = (double) params
            .getValue(StringIdentifier
                    .getIdentifier("minVoltageMultiplier"));
    final double maxVoltageMultiplier = (double) params
            .getValue(StringIdentifier
                    .getIdentifier("maxVoltageMultiplier"));

    LOG.debug("param minKW is {}", minKW);
    LOG.debug("param maxKW is {}", maxKW);
    LOG.debug("param minVoltageMultiplier is {}", minVoltageMultiplier);
    LOG.debug("param maxVoltageMultiplier is {}", maxVoltageMultiplier);


    if ((RunManager.isStopped())) {
      plan.getStateMachine().changeState(Forecast_Lateral_Stop.Instance(), ec, ig);
    }
  }

  @Override
  public synchronized void Exit(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
    // Nothing
  }
}
