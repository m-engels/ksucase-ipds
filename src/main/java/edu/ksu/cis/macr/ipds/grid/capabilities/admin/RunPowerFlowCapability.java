/**
 *
 * Copyright 2012 Denise Case Kansas State University MACR Laboratory
 * http://macr.cis.ksu.edu/ Department of Computing & Information Sciences
 *
 * See License.txt file for the license agreement.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package edu.ksu.cis.macr.ipds.grid.capabilities.admin;

import edu.ksu.cis.macr.aasis.agent.persona.AbstractOrganizationCapability;
import edu.ksu.cis.macr.aasis.agent.persona.IOrganization;
import edu.ksu.cis.macr.aasis.agent.persona.IPersona;
import edu.ksu.cis.macr.aasis.common.IConnections;
import edu.ksu.cis.macr.ipds.grid.capabilities.participate.GridCalculator;
import edu.ksu.cis.macr.ipds.primary.guidelines.IFeederGuidelines;
import edu.ksu.cis.macr.ipds.primary.guidelines.IHomeGuidelines;
import edu.ksu.cis.macr.ipds.primary.guidelines.ILateralGuidelines;
import edu.ksu.cis.macr.ipds.primary.guidelines.INeighborhoodGuidelines;
import edu.ksu.cis.macr.ipds.primary.messages.IPowerMessageContent;
import edu.ksu.cis.macr.organization.model.Agent;
import edu.ksu.cis.macr.organization.model.identifiers.ClassIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;

/**
 The {@code RunPowerFlowCapability} provides the ability to execute MatLab to run Optimal Power Flow calculations.
 */
public class RunPowerFlowCapability extends AbstractOrganizationCapability {
    private static final Logger LOG = LoggerFactory.getLogger(RunPowerFlowCapability.class);
    private static final boolean debug = false;
    private IHomeGuidelines homeGuidelines = null;
    private INeighborhoodGuidelines neighborhoodGuidelines = null;
    private ILateralGuidelines lateralGuidelines = null;
    private IFeederGuidelines feederGuidelines = null;

    private IConnections childConnections;
    private int currentIteration = 0;
    private int maxIteration = 1;

    /**
     * Construct a new {@code RunPowerFlowCapability} instance.
     *
     * @param owner - the agent possessing this capability.
     * @param org   - the immediate organization in which this agent operates.
     */
    public RunPowerFlowCapability(final IPersona owner, final IOrganization org) {
        super(RunPowerFlowCapability.class, owner, org);
    }


    @Override
    public synchronized double getFailure() {
        return 0;
    }

    /**
     * Gets the set of local registered prosumer agents.
     *
     * @param allAgents - the set of all agents registered in this organization
     * @return - the set of all prosumer agents registered in this local organization (does not include
     * other types of agents such as forecasters, etc)
     */
    public synchronized Set<Agent<?>> getLocalRegisteredProsumers(Set<Agent<?>> allAgents) {
        // get the list of registered prosumer peer agents in the local organization

        LOG.debug("Number of all agents found in the AggregationCapability is {}", allAgents.size());

        final Set<Agent<?>> prosumers = new HashSet<>();
        Iterator<Agent<?>> it = allAgents.iterator();

        final Class<?> capabilityClass = RunPowerFlowCapability.class;
        final ClassIdentifier capabilityIdentifier = new ClassIdentifier(
                capabilityClass);

        while (it.hasNext()) {
            Agent<?> agent = it.next();
            LOG.debug("Checking registered agent {} for AggregationCapability", agent.toString());
            if (agent.getPossesses(capabilityIdentifier) != null) {
                prosumers.add(agent);
                LOG.debug("Agent {} added to local prosumers list", agent.toString());
            }
        }
        return prosumers;
    }


    @Override
    public synchronized void reset() {
    }

    @Override
    public Element toElement(final Document document) {
        final Element capability = super.toElement(document);
        return capability;
    }

    public synchronized boolean isDoneIterating() {
        boolean doneIterating = this.getCurrentIteration() >= maxIteration;
        //this.feederGuidelines.getMaxIteration(); or whichever applies
        if (debug) LOG.debug("Done iterating = {}. This iteration = {}, max iterations allowed = {}", doneIterating,
                this.getCurrentIteration(), maxIteration);
        return doneIterating;
    }


    @Override
    public String toString() {
        return "RunPowerFlowCapability [no content yet=]";
    }

    public synchronized IHomeGuidelines getHomeGuidelines() {
        return homeGuidelines;
    }

    public synchronized void setHomeGuidelines(IHomeGuidelines homeGuidelines) {
        this.homeGuidelines = homeGuidelines;
    }

    public synchronized INeighborhoodGuidelines getNeighborhoodGuidelines() {
        return neighborhoodGuidelines;
    }

    public synchronized void setNeighborhoodGuidelines(INeighborhoodGuidelines neighborhoodGuidelines) {
        this.neighborhoodGuidelines = neighborhoodGuidelines;
    }

    public synchronized ILateralGuidelines getLateralGuidelines() {
        return lateralGuidelines;
    }

    public synchronized void setLateralGuidelines(ILateralGuidelines lateralGuidelines) {
        this.lateralGuidelines = lateralGuidelines;
    }

    public synchronized IFeederGuidelines getFeederGuidelines() {
        return feederGuidelines;
    }

    public synchronized void setFeederGuidelines(IFeederGuidelines feederGuidelines) {
        this.feederGuidelines = feederGuidelines;
    }

    public synchronized IConnections getChildConnections() {
        return childConnections;
    }

    public synchronized void setChildConnections(IConnections childConnections) {
        this.childConnections = childConnections;
    }

    public synchronized int getCurrentIteration() {
        return currentIteration;
    }

    public synchronized void setCurrentIteration(int currentIteration) {
        this.currentIteration = currentIteration;
    }

    public synchronized void incrementIteration() {
        this.currentIteration++;
        LOG.info("EVENT: GRID_CONTROL_ITERATION_EXECUTED. Number of iterations performed = {}", this.currentIteration);
    }

    public synchronized boolean allChildMessagesReceived(TreeMap<String, IPowerMessageContent> inputMap) {
        return (inputMap.size() == this.childConnections.getListConnectionGuidelines().size());
    }

    public synchronized IPowerMessageContent generateSummaryContent(TreeMap<String, IPowerMessageContent> map) {
        LOG.debug("Generating summary/aggregation from {}", getMapString(map));

        // TODO: create new aggregated content by summing the child power messages and return it.



        IPowerMessageContent content = null;
        return content;
    }

    public synchronized static String getMapString(TreeMap<String, IPowerMessageContent> treeMap) {
        StringBuilder b = new StringBuilder("\n");
        for (Map.Entry<String, IPowerMessageContent> entry : treeMap.entrySet()) {
            b.append(entry.getKey() + ": ");
            b.append(entry.getValue().toString() + "\n");
        }
        return b.toString();
    }

    private synchronized double[][] initializeInputs(TreeMap<String, IPowerMessageContent> inContent) {
        //TODO: Greg - implement this function to initialize the power information from all children.
        //See:  BrokerPowerCapability initializeInputs function for an example.
        double[][] input = new double[4][5];


        return input;
    }

    public synchronized IPowerMessageContent performOPF(TreeMap<String, IPowerMessageContent> map, int holonicLevel, long timeSlice) {
        LOG.debug("Beginning OPF with inputs: {}", getMapString(map));
        double[][] input = initializeInputs(map);
        LOG.debug("OPF inputs: {}", input.toString());
        double[][] output = executeOPF(holonicLevel, input, currentIteration,timeSlice );
        LOG.debug("OPF output: {}", output.toString());
        TreeMap<String, IPowerMessageContent> outMap = convertResultArrayToMessageContentMap(input, output, holonicLevel);
        LOG.debug("OPF outMap: {}", getMapString(outMap));
        return generateSummaryContent(outMap);
    }

    private synchronized TreeMap<String, IPowerMessageContent> convertResultArrayToMessageContentMap(double[][] input, double[][] output, int holonicLevel) {
        TreeMap<String, IPowerMessageContent> map = new TreeMap<>();
        //TODO: Implement - see BrokerPowerCapability


        return map;
    }

    private synchronized double[][] executeOPF(int holonicLevel, double[][] input, int currentIteration, long timeSlice) {
        double[][] result = new double[4][5];
        try {
            result = GridCalculator.calculateOPF(holonicLevel, currentIteration, input, timeSlice);
        } catch (Exception ex) {
            LOG.error("Error getting OPF results: {}", ex.getMessage());
            System.exit(-44);
        }
        return result;
    }
}