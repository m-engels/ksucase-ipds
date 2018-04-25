/*
 * Copyright 2012
 * Kansas State University MACR Laboratory http://macr.cis.ksu.edu/
 * Department of Computing & Information Sciences
 * 
 * See License.txt file for the license agreement. 
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package edu.ksu.cis.macr.ipds.grid.capabilities.admin;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;
import edu.ksu.cis.macr.aasis.agent.persona.AbstractOrganizationCapability;
import edu.ksu.cis.macr.aasis.agent.persona.IOrganization;
import edu.ksu.cis.macr.aasis.agent.persona.IPersona;
import edu.ksu.cis.macr.aasis.common.IConnectionGuidelines;
import edu.ksu.cis.macr.aasis.common.IConnections;
import edu.ksu.cis.macr.aasis.messaging.IMessagingFocus;
import edu.ksu.cis.macr.aasis.messaging.MessagingReliabilityManager;
import edu.ksu.cis.macr.goal.model.InstanceParameters;
import edu.ksu.cis.macr.ipds.config.RunManager;
import edu.ksu.cis.macr.ipds.grid.goals.GridGoalEvents;
import edu.ksu.cis.macr.ipds.grid.goals.GridGoalParameters;
import edu.ksu.cis.macr.ipds.grid.messaging.GridMessagingFocus;
import edu.ksu.cis.macr.ipds.grid.messaging.GridMessagingManager;
import edu.ksu.cis.macr.ipds.primary.guidelines.*;
import edu.ksu.cis.macr.ipds.primary.messages.IPowerMessage;
import edu.ksu.cis.macr.ipds.primary.messages.IPowerMessageContent;
import edu.ksu.cis.macr.ipds.primary.messages.PowerMessage;
import edu.ksu.cis.macr.ipds.primary.messages.PowerMessageContent;
import edu.ksu.cis.macr.obaa_pp.events.IOrganizationEvent;
import edu.ksu.cis.macr.obaa_pp.events.OrganizationEvent;
import edu.ksu.cis.macr.obaa_pp.events.OrganizationEventType;
import edu.ksu.cis.macr.organization.model.Agent;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import edu.ksu.cis.macr.organization.model.identifiers.StringIdentifier;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
/**
 Provides the ability to act as a super holon in a power distribution control organization.
 */
public class GridControlSuperHolonCapability extends AbstractOrganizationCapability {
    private static final Logger LOG = LoggerFactory.getLogger(GridControlSuperHolonCapability.class);
    private static final String QUEUE_PURPOSE = "GRID";
    private static Channel channel;
    private static final boolean debug = false;
    private static final IMessagingFocus messagingFocus = GridMessagingFocus.GRID;
    private static UniqueIdentifier myID;
    double communicationReliability = 1.0;
    double communicationDelay = 0.0;
    private final String COMMUNICATION_CHANNEL_ID = "PowerCommunicationChannel";
    private int NUMBER_OF_PARTICPANT_MESSAGES_TO_STORE = 1;
    private Map<InstanceGoal<?>, BlockingQueue<IPowerMessageContent>> allParticipantData;
    private double currentTotal;
    private double maxKW = 0.0;
    private double maxVoltageMultiplier = 0.0;
    private double minKW = 0.0;
    private double minVoltageMultiplier = 0.0;
    private double netDeltaP = 0.0;
    private IHomeGuidelines homeGuidelines;
    private INeighborhoodGuidelines neighborhoodGuidelines;
    private ILateralGuidelines lateralGuidelines;
    private IFeederGuidelines feederGuidelines;
    private ISubstationGuidelines substationGuidelines;
    private IConnections allChildConnections;
    private QueueingConsumer consumer;


    /**
     @param owner - the entity to which this capability belongs
     @param org - the organization in which it is using this capability
     */
    public GridControlSuperHolonCapability(final IPersona owner, final IOrganization org) {
        super(GridControlSuperHolonCapability.class, owner, org);
        this.setOwner(Objects.requireNonNull(owner));
        channel  = GridMessagingManager.getChannel(messagingFocus);
        consumer = new QueueingConsumer(Objects.requireNonNull(channel, "Error null channel in receive()."));
        initializeReliabilityAndDelay();
    }

    private void initializeReliabilityAndDelay() {
        try {
            this.communicationReliability = MessagingReliabilityManager.getCommunicationReliability();
            if (debug) LOG.debug("\t New  communicationReliability.");
            communicationDelay = MessagingReliabilityManager.getCommunicationDelay();
        }
        catch (Exception e){
            // just use the defaults
            communicationDelay = 0.0;
            communicationReliability = 1.0;
        }
        if (debug)
            LOG.debug("New comm cap with reliability = {} and delay = {}", communicationReliability, communicationDelay);
    }

    public IConnections getAllChildConnections() {
        return this.allChildConnections;
    }

    public synchronized void setAllChildConnections(IConnections allChildConnections) {
        this.allChildConnections = allChildConnections;
    }

    private double getCurrentTotal() {
        return this.currentTotal;
    }

    public IFeederGuidelines getFeederGuidelines() {
        return feederGuidelines;
    }

    public synchronized void setFeederGuidelines(IFeederGuidelines feederGuidelines) {
        this.feederGuidelines = feederGuidelines;
    }

    public IHomeGuidelines getHomeGuidelines() {
        return homeGuidelines;
    }

    public synchronized void setHomeGuidelines(IHomeGuidelines homeGuidelines) {
        this.homeGuidelines = homeGuidelines;
    }

    public ILateralGuidelines getLateralGuidelines() {
        return lateralGuidelines;
    }

    public synchronized void setLateralGuidelines(ILateralGuidelines lateralGuidelines) {
        this.lateralGuidelines = lateralGuidelines;
    }

    public INeighborhoodGuidelines getNeighborhoodGuidelines() {
        return neighborhoodGuidelines;
    }

    public synchronized void setNeighborhoodGuidelines(INeighborhoodGuidelines neighborhoodGuidelines) {
        this.neighborhoodGuidelines = neighborhoodGuidelines;
    }

    public ISubstationGuidelines getSubstationGuidelines() {
        return substationGuidelines;
    }

    public synchronized void setSubstationGuidelines(ISubstationGuidelines substationGuidelines) {
        this.substationGuidelines = substationGuidelines;
    }

    @Override
    public synchronized void reset() {
    }

    @Override
    public double getFailure() {
        return 0;
    }

    @Override
    public Element toElement(final Document document) {
        final Element capability = super.toElement(document);
        return capability;
    }

    /**
     Get the parameters from this instance goal and use them to set the goal-specific guidelines.
     @param instanceGoal - this instance of the specification goal
     */
    public synchronized void initializeFromGoal(InstanceGoal<?> instanceGoal) {
        if (debug) LOG.debug("Initializing guidelines from goal {} ", instanceGoal);

        try {
            // Get the parameter values from the existing active instance goal
            final InstanceParameters params = (InstanceParameters) instanceGoal.getParameter();

            final INeighborhoodGuidelines ng = (INeighborhoodGuidelines) params.getValue(StringIdentifier
                    .getIdentifier("neighborhoodGuidelines"));
            this.setNeighborhoodGuidelines(ng);

            final ILateralGuidelines lg = (ILateralGuidelines) params.getValue(StringIdentifier
                    .getIdentifier("lateralGuidelines"));
            this.setLateralGuidelines(lg);

            final IFeederGuidelines fg = (IFeederGuidelines) params.getValue(StringIdentifier
                    .getIdentifier("feederGuidelines"));
            this.setFeederGuidelines(fg);

            final ISubstationGuidelines sg = (ISubstationGuidelines) params.getValue(StringIdentifier
                    .getIdentifier("substationGuidelines"));
            this.setSubstationGuidelines(sg);
        }
        catch(Exception e){
            LOG.error("Error: {}", e.getCause());
            System.exit(-83);
        }
    }

    /**
     Get the parameters from this instance goal and use them to set the goal-specific guidelines.
     @param instanceGoal - this instance of the specification goal
     */
    public synchronized void initializeGuidelines(InstanceGoal<?> instanceGoal) {
        if (debug) LOG.debug("Initializing all guidelines from goal: {}.", instanceGoal);

        // Get the parameter values from the existing active instance goal
        final InstanceParameters params = Objects.requireNonNull((InstanceParameters) instanceGoal.getParameter());
        if (debug) LOG.debug("Initializing sub connections params: {}.", params);

        setAllChildConnections(IConnections.extractConnections(params, "childConnections"));

        final IHomeGuidelines hg = (IHomeGuidelines) params.getValue(StringIdentifier.getIdentifier("homeGuidelines"));
        this.setHomeGuidelines(hg);
        if (debug && hg != null) LOG.info("Set home guidelines for {} to {}.", this.getIdentifier().toString(), hg);

        final INeighborhoodGuidelines ng = (INeighborhoodGuidelines) params.getValue(StringIdentifier.getIdentifier("neighborhoodGuidelines"));
        this.setNeighborhoodGuidelines(ng);
        if (debug && ng != null) LOG.info("Set Neighborhood guidelines for {} to {}.", this.getIdentifier().toString(), ng);

        ILateralGuidelines lg = (ILateralGuidelines) params.getValue(StringIdentifier.getIdentifier("lateralGuidelines"));
        this.setLateralGuidelines(lg);
        if (debug && lg != null) LOG.info("Set Lateral guidelines for {} to {}.", this.getIdentifier().toString(), lg);

        IFeederGuidelines fg = (IFeederGuidelines) params.getValue(StringIdentifier.getIdentifier("feederGuidelines"));
        this.setFeederGuidelines(fg);
        if (debug && fg != null) LOG.info("Set Feeder guidelines for {} to {}.", this.getIdentifier().toString(), fg);

        ISubstationGuidelines sg = (ISubstationGuidelines) params.getValue(StringIdentifier.getIdentifier("substationGuidelines"));
        this.setSubstationGuidelines(sg);
        if (debug && sg != null) LOG.info("Set Substation guidelines for {} to {}.", this.getIdentifier().toString(), sg);
    }

    /**
     Calculate new guidelines, get reports from holons, and forward the information up the line. May create new
     organization events with the updated goal parameters for each participant if immediate local response is desired.
     @return true if a local solution is possible, false if not
     */
    public IPowerMessageContent processGridPowerReports() {
        if (debug) LOG.debug("Beginning processGridPowerReports - getting holonic guidelines");

        // get the combined guidelines
        final double combinedMaxKW = this.allChildConnections.getListConnectionGuidelines().get(0).getCombinedKW();
        String orgName = this.allChildConnections.getListConnectionGuidelines().get(0).getOrganizationAbbrev();
        String orgLevel = this.allChildConnections.getListConnectionGuidelines().get(0).getOrganizationLevel();
        if (debug) LOG.debug("{} org {} guidelines include a combined max of {}", orgLevel, orgName, combinedMaxKW);

        // get the most recent data from participant getNumberOfMessages

        if (debug) LOG.debug("Checking sub reports for {}.", orgName);
        Map<String, IPowerMessageContent> mostRecentReadings = this.getMostRecentReadings();

        int readCount = mostRecentReadings.size();
        int subCount = this.getAllChildConnections().getListConnectionGuidelines().size();
        // int subCount = org.getNumberOfChildProsumers();
        if (debug) LOG.info("{} recent reports out of {} total sub holons in {}. {}", readCount, subCount, orgName, Arrays.toString(mostRecentReadings.keySet().toArray()));

        // if the reporting is complete, aggregate and forward to internal self for processing
        IPowerMessageContent aggregateContent = null;
        if ((readCount > 0)) { // && (readCount == subCount)) {
            LOG.info("Reporting complete: {} of {} reads are available. Forwarding aggregated totals.", readCount, subCount);
            aggregateContent = createPowerMessageContent(mostRecentReadings);
            LOG.info("Aggregated message content to pass up is: {}", aggregateContent);
        }
        return aggregateContent;


//    if (readCount >0) {
//      if (debug) LOG.debug("{} of {} reads are available.", readCount, subCount);
//      final double totalKW = getCurrentTotal();
//
//      // if local solution won't be possible, record the crucial information
//
//      if (totalKW > combinedMaxKW) {
//        aggregateContent.setActualKW(totalKW);
//        aggregateContent.setConstantInelasticLoad_kw(0.0);
//        aggregateContent.setConstantInelasticLoad_fraction(combinedMaxKW);
//      }
//
////            // adjust the local organization
////            Map<InstanceGoal<?>, Double> newGuidelines = this.getAgent()
////                    .getCapability(DistributeLoadDirectCapability.class)
////                    .calculateNewLoadGuidelines(currentReads, totalKW, combinedMaxKW);
////
////            // create new organization events
////            List<IOrganizationEvent> newEvents = this.updateGoalParameters(newGuidelines);
////            this.owner.getEventManager().addEventListToQueue(newEvents);
//      return aggregateContent;
//    } else {
//      return null;
//    }
    }

    private Map<String, IPowerMessageContent> getMostRecentReadings() {
        if (debug) LOG.debug("Beginning getMostRecentReadings");
        Map<String, IPowerMessageContent> currentReads = new HashMap<>();
        this.currentTotal = 0.0;
        int subCount = 0;
        int checkCount = 1;
        final String myPersona = owner.getUniqueIdentifier().toString();
        final List<? extends IConnectionGuidelines> list = this.allChildConnections.getListConnectionGuidelines();
        int numSubs = list.size();
        if (debug) LOG.debug("My guidelines indicate there are {} sub holons. ", numSubs);
        String other;

        for (IConnectionGuidelines cg : list) {
            final String link = cg.getOtherAgentAbbrev() + "-" + this.getOwner().getUniqueIdentifier().toString();
            if (debug) LOG.debug("Check for message {} of {} via {}. ", checkCount, numSubs, link);

            IPowerMessage foundMessage = null;
            try {
                foundMessage = receiveRemotePowerMessage(link);
            } catch (IOException | InterruptedException e) {
                LOG.error("Error receiving remote power message on link {}", link);
                e.printStackTrace();
            }

            if (foundMessage != null) {
                other = foundMessage.getRemoteSender();
                if (debug)
                    LOG.debug("Super holon {} picked up a power message from {} via {}. {}", myPersona, other, link, foundMessage.toString());
                subCount++;
                LOG.info("{} sub holon report(s) received.", subCount);
                IPowerMessageContent newContent = (IPowerMessageContent) foundMessage.getContent();
                LOG.info("Super holon {} received power message from {}. {}", myPersona, other, newContent.toString());
                double newActualKW = newContent.getActualKW();
                this.currentTotal += newActualKW;
                LOG.info("New most current total actual KW = {} after super holon {} received message {} with power message content {}", this.currentTotal, myPersona, subCount, newContent.toString());
                currentReads.put(other, newContent);
                LOG.info("Current reads has {} entries for a total net ActualKW = {}", subCount, this.currentTotal);
            }
            checkCount++;
        }
        int subsReported = subCount;
        if (subsReported >0) LOG.info("Super holon {} received {} power messages with a total kw = {}. {}", myPersona, subsReported, currentTotal, currentReads.toString());
        return currentReads;
    }

    /**
     @param personaName - the name of the subagent
     @return String messages - Grabs messages from Queue
     @throws IOException - Handles all IO Exceptions
     @throws ShutdownSignalException - Handles all Shutdown signal Exceptions
     @throws ConsumerCancelledException - Handles all Consumer Cancelled Exceptions
     @throws InterruptedException - Handles Interrupted Exceptions
     */
    public synchronized IPowerMessage receiveRemotePowerMessage(String personaName) throws IOException, ShutdownSignalException, ConsumerCancelledException, InterruptedException {
        String fullQueueName = GridMessagingManager.getFullQueueName(personaName,GridMessagingManager.getQueueFocus(messagingFocus));
        GridMessagingManager.declareAndBindConsumerQueue(messagingFocus, personaName);
        String basicConsume = channel.basicConsume(fullQueueName, true, consumer);

        // check for delivery for given milliseconds
        final QueueingConsumer.Delivery delivery = consumer.nextDelivery(RunManager.getDeliveryCheckTime_ms());
        if (delivery == null) {
            if (debug) LOG.debug("got nothing on {}", fullQueueName);
            return null;
        }
        if(debug) LOG.debug("got something on {}", fullQueueName);
        IPowerMessage message = new PowerMessage();
        try {
            message = (IPowerMessage) message.deserialize(delivery.getBody());
            if (debug) LOG.info("Received REMOTE GRID MESSAGE on {}. {}.", fullQueueName, message);
        } catch (Exception ex) {
            LOG.error("ERROR: {}", ex.toString());
            System.exit(-42);
        }
        return message;
    }

    private IPowerMessageContent createPowerMessageContent(Map<String, IPowerMessageContent> mostRecentReadings) {
        LOG.info("Creating new aggregate power message");
        IPowerMessageContent agg = PowerMessageContent.createPowerMessageContent();
        for (Entry<String, IPowerMessageContent> entry : mostRecentReadings.entrySet()) {
            IPowerMessageContent item = entry.getValue();
            LOG.info("Adding message content from {}: {}.", entry.getKey(), entry.getValue().toString());
            agg.add(item);
        }
        return agg;
    }

    /**
     Trigger a "Manage Load" goal for each prosumer participant in this local organization. Use the combined guidelines and
     distribute them among the registered participants.
     @param instanceGoal - the supervisor's goal instance
     */
    public synchronized void triggerManageGoals(final InstanceGoal<?> instanceGoal) {
        // Set some initial goal parameter guidelines for each participant
        final Map<UniqueIdentifier, Object> participantGoalParameters = this
                .getInitialParticipantGoalParameters();
        if (participantGoalParameters == null) {
            return;
        }

        // create an organization event to trigger each power participant's manage goal
        final List<IOrganizationEvent> organizationEvents = this
                .createParticipantEvents(instanceGoal,
                        participantGoalParameters);

        // add each organization events to the control component's event list
        this.owner.getOrganizationEvents().addEventListToQueue(organizationEvents);
    }

    public Map<UniqueIdentifier, Object> getInitialParticipantGoalParameters() {
        Set<Agent<?>> prosumers = this.getLocalRegisteredProsumers();
        int numProsumers = prosumers.size();
        if (numProsumers == 0) {
            return null;
        }
        final double minKW = feederGuidelines.getMinKW() / numProsumers;
        final double maxKW = feederGuidelines.getMaxKW() / numProsumers;
        if (maxKW < 0.1) {
            LOG.error("ERROR: The initial maxKW for participants is zero.");
            System.exit(-1);
        }
        LOG.info("Each agent starts with {} of {} combined KW", maxKW, feederGuidelines.getMaxKW());
        final Map<UniqueIdentifier, Object> participantGoalParameters = new HashMap<>();
        ILateralGuidelines g = new LateralGuidelines();
        participantGoalParameters.put(GridGoalParameters.lateralGuidelines, g);
        return participantGoalParameters;
    }

    /**
     Gets the set of local registered prosumer agents given the set of all agents. Do not include the supervisors (control
     component masters in this local organization) and do not include any independent forecaster agents (but a child that
     is also performing a forecast role should be included).
     @return - the set of all prosumer agents registered in this local organization (does not include
     other types of agents such as forecasters, etc)
     */
    public Set<Agent<?>> getLocalRegisteredProsumers() {
        // TODO: add ability to indicate sub is a prosumer agent (not a forecaster).
        final Set<Agent<?>> allAgents = this.getOwner()
                .getPersonaControlComponent().getOrganizationModel().getAgents();

        final Set<Agent<?>> prosumers = new HashSet<>();
        for (Agent<?> agent : allAgents) {
            boolean isMaster = (agent.getIdentifier() == this.owner.getPersonaControlComponent().getLocalMaster());
            boolean isExternalForecaster = agent.getIdentifier().toString().contains("_F");
            if (!isMaster && !isExternalForecaster) {
                prosumers.add(agent);
            }
        }
        return prosumers;
    }

    public List<IOrganizationEvent> createParticipantEvents(InstanceGoal<?> instanceGoal,
                                                            Map<UniqueIdentifier, Object> participantParams) {
        final InstanceParameters params = new InstanceParameters(participantParams);
        final ArrayList<IOrganizationEvent> orgEvents = new ArrayList<>();

        for (int i = 1; i <= this.getLocalRegisteredProsumers().size(); i++) {
            final IOrganizationEvent event = new OrganizationEvent(
                    OrganizationEventType.EVENT, GridGoalEvents.manage, instanceGoal, params);
            if (debug) LOG.debug("Created new organization event in plan: {}", event.toString());
            orgEvents.add(event);
        }
        return orgEvents;
    }

    /*
    * Called when goal parameters need to be updated within the system.
    *
    * Reassigns the combinedMaxKW based on the percentage of current use by an
    * agent.
    */
    public List<IOrganizationEvent> updateGoalParameters(final Map<InstanceGoal<?>, Double> newGuidelines) {
        List<IOrganizationEvent> oes = new ArrayList<>();

        for (Entry<InstanceGoal<?>, Double> read : newGuidelines.entrySet()) {
            Map<UniqueIdentifier, Object> goals = new HashMap<>();

            ILateralGuidelines g = new LateralGuidelines();
            goals.put(GridGoalParameters.lateralGuidelines, g);

            InstanceParameters instanceParams = new InstanceParameters(goals);

            IOrganizationEvent oe = new OrganizationEvent(
                    OrganizationEventType.GOAL_MODEL_MODIFICATION, GridGoalEvents.manage,
                    read.getKey(), instanceParams);
            oes.add(oe);
        }
        return oes;
    }
}
