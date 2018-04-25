package edu.ksu.cis.macr.ipds.primary.plans.manage_neighborhood;

import edu.ksu.cis.macr.ipds.config.RunManager;
import edu.ksu.cis.macr.ipds.primary.capabilities.manage.DateTimeCapability;
import edu.ksu.cis.macr.ipds.primary.capabilities.manage.ManageNeighborhoodCapability;
import edu.ksu.cis.macr.ipds.primary.guidelines.INeighborhoodGuidelines;
import edu.ksu.cis.macr.obaa_pp.ec.base.IExecutor;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IExecutablePlan;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IPlanState;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.GregorianCalendar;
import java.util.Objects;

/**
 The main managing step for an autonomous agent managing a neighborhood, typically from a power pole providing power to
 4-6 homes.  The plan primarily involves managing this agent's associated devices (if any).
 */
public enum Manage_Neighborhood implements IPlanState<Manage_Neighborhood_Plan> {
  INSTANCE;
  private static final Logger LOG = LoggerFactory.getLogger(Manage_Neighborhood.class);
  private static final boolean debug = false;

  @Override
  public synchronized void Enter(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
    // Nothing
  }

  @Override
  public synchronized void Execute(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
    if (debug) LOG.debug("Starting with instance goal: {}.", ig);
    Objects.requireNonNull(ig);
    Objects.requireNonNull(ec);
    Objects.requireNonNull(ec.getCapability(DateTimeCapability.class), "Role requires DateTimeCapability.");
    Objects.requireNonNull(ec.getCapability(ManageNeighborhoodCapability.class));

    // wait extra long - not much to do
    plan.heartBeat(this.getClass().getName(), 10);

    LOG.debug("Reading simulated sensor data from a smart meter at this location.");

    // initialize my guidelines as defined in the parametrized instance goal
    ec.getCapability(ManageNeighborhoodCapability.class).initializeFromGoal(ig);
    if (debug) LOG.debug("Initialized capabilities.");


    // get current timeSlice from datetime capability

    long timeslice = Objects.requireNonNull(ec.getCapability(DateTimeCapability.class).
            getTimeSlicesElapsedSinceStart(), "ERROR: Need a timeSlice to get sensor data.");

    GregorianCalendar simulationTime = Objects.requireNonNull(ec.getCapability(DateTimeCapability.class)
            .getSimulationTime(), "ERROR: Need to know the current simulation time.");
    LOG.info("{} elapsed time slices. ", timeslice);


    // get values from goal guidelines
    INeighborhoodGuidelines guidelines = Objects.requireNonNull(ec.getCapability(ManageNeighborhoodCapability.class).getNeighborhoodGuidelines());
    if (debug) LOG.debug("Guidelines : {} ", guidelines.toString());


//    if (ec.getCapability(IPowerCommunicationCapability.class).messages() > 0) {
//      final IPowerMessage msg = ec.getCapability(IPowerCommunicationCapability.class).receive();
//      if (msg != null) {
//        if (debug) LOG.debug(" supervisor first received local power messages from {} is {}", msg.getLocalSender(), msg
//                .getContent());
//
//        // if this is the correct receiver
//        if (msg.getLocalReceiver().equals(ec.getUniqueIdentifier())) {
//          String debugString = null;
//          switch (msg.getPerformativeType()) {
//            case REPORT_OK:
//              LOG.debug("Got OK report from : {} says {}", msg.getLocalSender(), msg.getContent());
//              ec.getCapability(ManageNeighborhoodCapability.class)
//                      .updateMostRecentData(msg);
//              // tune the goals anyway (stop if too computationally intense)
//              if (ec.getCapability(SuperHolonCapability.class) != null) {
//                ec.getCapability(SuperHolonCapability.class).updateParticipants();
//              }
//              break;
//            case REPORT_OUT_OF_BOUNDS:
//              debugString = String
//                      .format("Got OUT_OF_BOUNDS report from : %s says %s",
//                              msg.getLocalSender(),
//                              msg.getContent());
//              LOG.debug(debugString);
//              boolean success = ec
//                      .getCapability(ManageNeighborhoodCapability.class)
//                      .updateMostRecentData(msg);
//              if (!success) {
//                break;
//              }
//              // find a local solution
//              if (ec.getCapability(SuperHolonCapability.class) != null) {
//                IPowerMessageContent request = ec
//                        .getCapability(SuperHolonCapability.class)
//                        .updateParticipants();
//                if (request != null) {
//                  LOG.info("Tried to find local solution-request is: {}", request);
//                  // if local solution wasn't found, send our controller a request
//                  if (ec.getCapability(IPowerCommunicationCapability.class) != null) {
//                    ec.getCapability(IPowerCommunicationCapability.class)
//                            .sendControllerRequest(PowerPerformative.REPORT_OUT_OF_BOUNDS, request);
//                  } else if (
//                          ec.getCapability(IPowerCommunicationCapability.class) != null) {
//                    ec.getCapability(IPowerCommunicationCapability.class)
//                            .sendControllerRequest(PowerPerformative.REPORT_OUT_OF_BOUNDS, request);
//                  }
//                }
//              }
//              break;
//          }
//        }
//      }
    // }

    if ((RunManager.isStopped())) {
      plan.getStateMachine().changeState(Manage_Neighborhood_Stop.INSTANCE, ec, ig);
    }

  }

  @Override
  public synchronized void Exit(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
    // Nothing
  }
}
