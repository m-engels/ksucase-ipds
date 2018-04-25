package edu.ksu.cis.macr.ipds.grid.plans.be_holon;


import edu.ksu.cis.macr.aasis.common.IConnections;
import edu.ksu.cis.macr.ipds.config.RunManager;
import edu.ksu.cis.macr.ipds.grid.capabilities.participate.GridControlHolonCapability;
import edu.ksu.cis.macr.ipds.primary.capabilities.manage.DateTimeCapability;
import edu.ksu.cis.macr.ipds.primary.capabilities.participate.IPowerCommunicationCapability;
import edu.ksu.cis.macr.ipds.primary.capabilities.participate.ParticipateCapability;
import edu.ksu.cis.macr.ipds.primary.messages.IPowerMessage;
import edu.ksu.cis.macr.ipds.primary.messages.IPowerMessageContent;
import edu.ksu.cis.macr.obaa_pp.ec.base.IExecutor;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IExecutablePlan;
import edu.ksu.cis.macr.obaa_pp.ec.plans.IPlanState;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * This is the main execute step in the associated plan and can be shared by all agents employing this plan.  If the list
 * of registered agents changes, it will revert to the prior initialization ("Be_Holon_Init") state.  If it receives a
 * stop message, it will move to the stop state.
 */
public enum Be_Holon implements IPlanState<Be_Holon_Plan> {
    INSTANCE;
    private static final Logger LOG = LoggerFactory.getLogger(Be_Holon.class);
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
        Objects.requireNonNull(ec.getCapability(ParticipateCapability.class), "Role requires SelfParticipateCapability.");
        Objects.requireNonNull(ec.getCapability(GridControlHolonCapability.class), "Role requires HolonCapability.");
        Objects.requireNonNull(ec.getCapability(IPowerCommunicationCapability.class), "Role requires IPowerCommunicationCapability.");
        Objects.requireNonNull(ec.getCapability(DateTimeCapability.class), "Role requires DateTimeCapability.");

        // wait extra long
        plan.heartBeat(this.getClass().getName(), 10);

        // if I'm the "real" persona in the sub holon agent, listen for messages to forward
        if (ec.getCapability(ParticipateCapability.class) != null) {

            // set holon guidelines from top-down instance goals
            ec.getCapability(GridControlHolonCapability.class).init(ig);
            if (debug) LOG.debug("agent: Set guidelines from instance goal. ");

            // get the timeslice
            long thisTimeSlice = ec.getCapability(DateTimeCapability.class).getTimeSlicesElapsedSinceStart();

            // check for a new power message from self
            IPowerMessage m = ec.getCapability(IPowerCommunicationCapability.class).checkFromSelf();

            if (m != null) {
                LOG.info("agent: org particapant received message from self: {}", m.toString());

                long messageTimeSlice = ((IPowerMessageContent) m.getContent()).getTimeSlice();
                LOG.debug("agent: This timeslice={}, message timeslice={}", thisTimeSlice, messageTimeSlice);

                // track with scenario for simulation testing
                RunManager.addPowerMessageSelfToSub(m.getLocalSender().toString(), m.getLocalReceiver().toString(), messageTimeSlice);

                // if not null and the time slice is different, then foward it...
                IConnections parents = ec.getCapability(GridControlHolonCapability.class).getParentConnections();
                if (debug) LOG.debug("agent: get list of up connections {}.", parents);

                ec.getCapability(IPowerCommunicationCapability.class).sendUp(m, parents);
                LOG.info("agent: participant forwarded message to org administrator: {}. Up list: {}", m, parents);
            } else {
                LOG.info("agent: got nothing from up. Current time slice is {}.", thisTimeSlice);
            }

        }
        // if I'm the proxy persona running my new holonic organization
        else {

            // set holon guidelines from top-down instance goals
            ec.getCapability(GridControlHolonCapability.class).init(ig);
            if (debug) LOG.debug("proxy: Set guidelines from instance goal.");
        }

        if ((RunManager.isStopped())) {
            plan.getStateMachine().changeState(Be_Holon_Stop.INSTANCE, ec, ig);
        }
    }

    @Override
    public synchronized void Exit(final IExecutablePlan plan, final IExecutor ec, final InstanceGoal<?> ig) {
        // Nothing
    }
}
