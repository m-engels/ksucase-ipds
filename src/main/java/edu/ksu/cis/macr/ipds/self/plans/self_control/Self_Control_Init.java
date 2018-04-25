package edu.ksu.cis.macr.ipds.self.plans.self_control;

import edu.ksu.cis.macr.aasis.common.IConnectionGuidelines;
import edu.ksu.cis.macr.ipds.config.RunManager;
import edu.ksu.cis.macr.ipds.grid.capabilities.connect.IGridConnectCapability;
import edu.ksu.cis.macr.ipds.market.capabilities.connect.IMarketConnectCapability;
import edu.ksu.cis.macr.ipds.self.capabilities.admin.SelfControlCapability;
import edu.ksu.cis.macr.obaa_pp.ec.base.IExecutor;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IExecutablePlan;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IPlanState;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

/**
 This is the first step in the plan. It includes initialization tasks
 for social agents such as reading goal guidelines, calling centralized control centers for authentication, authorization, software updates, etc.
 It also includes establishing access to the needed communications exchanges, and triggering appropriate
 participation goals based on the agent guidelines provided.
 */
public enum Self_Control_Init implements IPlanState<Self_Control_Plan> {
    INSTANCE;
    private static final Logger LOG = LoggerFactory.getLogger(Self_Control_Init.class);
    private static final boolean debug = false;
    private static boolean doneMarket = false;
    private static boolean doneGrid = false;

    @Override
    public synchronized void Enter(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
        // Nothing
    }

    @Override
    public synchronized void Execute(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
        if (debug) LOG.debug("Starting with instance goal: {}.", ig);
        Objects.requireNonNull(ec);
        Objects.requireNonNull(ig);
        Objects.requireNonNull(ec.getCapability(SelfControlCapability.class), "Role requires SelfControlCapability.");

        plan.heartBeat(this.getClass().getName());

        // initialize capabilities
        ec.getCapability(SelfControlCapability.class).init(ig);
        LOG.info("Capability initialized from goal guidelines. SelfControlCapability.");

        // Contact the central power distribution system control center to get any
        // configuration updates needed.
        ec.getCapability(SelfControlCapability.class).callForConfiguration();
        LOG.info("Simulated: the agent starts up and would call the grid control center for configuration.");

        // if this agent has authorized connections in the grid control holarchy.....
        if (ec.getCapability(IGridConnectCapability.class) == null) {
            doneGrid = true;
        } else {
            ec.getCapability(IGridConnectCapability.class).init(ig);
            LOG.info("Capability initialized from goal guidelines. IGridConnectCapability.");

            if (ec.getCapability(IGridConnectCapability.class).getAllConnections() == null) {
                doneGrid = true;
            } else {

                // get the list of all connection guidelines (goal parameters)
                List<? extends IConnectionGuidelines> lst = ec.getCapability(IGridConnectCapability.class)
                        .getAllConnections().getListConnectionGuidelines();

                if (lst == null) {
                    LOG.info("No grid connections");
                    doneGrid = true;
                } else {
                    LOG.info("Retrieved {} authorized connections from IGridConnectCapability.", lst.size());

                    // register with the central messages exchange (verify that rabbitMQ is running

                    if (ec.getCapability(IGridConnectCapability.class).registerWithExchange()) {
                        LOG.info("Verified connection to grid exchange.");

                        // trigger participation goals (if appropriate) - these need to be consistent with the goal model
                        ec.getCapability(IGridConnectCapability.class).triggerChildGoal(ig);
                        LOG.info("Triggered any goals to act as a sub holon in the grid control system.");

                        ec.getCapability(IGridConnectCapability.class).triggerParentGoal(ig);
                        LOG.debug("Triggered any goals to act as a super holon in the grid control system.");

                        ec.getCapability(IGridConnectCapability.class).connectUp();
                        LOG.debug("Sent connect messages to grid control super holons.");

                        ec.getCapability(IGridConnectCapability.class).connectDown();
                        LOG.debug("Sent connect messages to grid control sub holons.");

                        doneGrid = true;
                    }
                }
            }
        }

        LOG.info("Done with grid connections... switching to market connections.");
        // if this agent has authorized connections in the power market holarchy.....

        if (ec.getCapability(IMarketConnectCapability.class) == null)  {
            doneMarket = true;
            if (debug) LOG.info("This agent does not have the necessary market connect capability.");
        }
        else {
            // initialize information regarding all authorized connections
            ec.getCapability(IMarketConnectCapability.class).init(ig);
            if (debug) LOG.info("Capability initialized from goal guidelines. IMarketConnectCapability.");

            if (ec.getCapability(IMarketConnectCapability.class).getAllConnections() == null) {
                doneMarket = true;
                LOG.info("This agent has necessary market connect capability, but there are no market connections.");
            } else {


                // get the list of all connection guidelines (goal parameters)
                List<? extends IConnectionGuidelines> lst = ec.getCapability(IMarketConnectCapability.class)
                        .getAllConnections().getListConnectionGuidelines();

                if (lst == null) {
                    LOG.info("The list of market connection guidelines was null");
                    doneMarket = true;
                } else {

                    if (debug)
                        LOG.info("Retrieved {} authorized connections from IMarketConnectCapability.", lst.size());

                    // initialize all to "not yet connected"
//                  ec.getCapability(IMarketConnectCapability.class).setIsConnectedForAll(false);
//                  if (debug) LOG.debug("{} affiliated agents. All unconnected", lst.size());


                    // register with the central messages exchange (verify that rabbitMQ is
                    // running and that I can access it)

                    if (ec.getCapability(IMarketConnectCapability.class).registerWithExchange()) {
                        LOG.info("Verified connection to grid exchange.");

                        // trigger participation goals (if appropriate) - these need to be consistent with the goal model
                        ec.getCapability(IMarketConnectCapability.class).triggerChildGoal(ig);
                        LOG.info("Triggered any goals to participate in power sales auctions.");

                        ec.getCapability(IMarketConnectCapability.class).triggerParentGoal(ig);
                        LOG.info("Triggered any goals to broker power sales auctions.");

                        ec.getCapability(IMarketConnectCapability.class).connectUp(ig);
                        LOG.info("Sent connect messages to power auction brokers.");

                        ec.getCapability(IMarketConnectCapability.class).connectDown();
                        LOG.info("Sent connect messages to power auction participants.");

                        doneMarket = true;
                    }
                }
            }
        }
        if (doneMarket && doneGrid) {
            LOG.info("Initially registered with all authorized connections in all affiliated organizations. Changing state.");
            plan.getStateMachine().changeState(Self_Control.INSTANCE, ec, ig);
        }
        if ((RunManager.isStopped())) {
            LOG.info("Changing state.");
            plan.getStateMachine().changeState(Self_Control_Stop.INSTANCE, ec, ig);
        }
    }

    @Override
    public synchronized void Exit(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
        // Nothing
    }
}
