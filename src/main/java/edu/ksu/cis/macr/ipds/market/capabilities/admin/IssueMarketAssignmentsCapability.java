package edu.ksu.cis.macr.ipds.market.capabilities.admin;

import edu.ksu.cis.macr.aasis.agent.persona.AbstractOrganizationCapability;
import edu.ksu.cis.macr.aasis.agent.persona.IOrganization;
import edu.ksu.cis.macr.aasis.agent.persona.IPersona;
import edu.ksu.cis.macr.organization.model.Agent;
import edu.ksu.cis.macr.organization.model.identifiers.ClassIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class IssueMarketAssignmentsCapability extends AbstractOrganizationCapability {

    private static final Logger LOG = LoggerFactory.getLogger(IssueMarketAssignmentsCapability.class);
    private static final boolean debug = false;

    /**
     * Construct a new instance.
     *
     * @param owner - the agent possessing this capability.
     * @param org   - the immediate organization in which this agent operates.
     */
    public IssueMarketAssignmentsCapability(final IPersona owner, final IOrganization org) {
        super(IssueMarketAssignmentsCapability.class, owner, org);
    }


    @Override
    public double getFailure() {
        return 0;
    }

    /**
     * Gets the set of local registered prosumer agents.
     *
     * @param allAgents - the set of all agents registered in this organization
     * @return - the set of all prosumer agents registered in this local organization (does not include
     * other types of agents such as forecasters, etc)
     */
    public Set<Agent<?>> getLocalRegisteredProsumers(Set<Agent<?>> allAgents) {
        // get the list of registered prosumer peer agents in the local organization

        if (debug) LOG.debug("Number of all agents found in the MakeAssignmentsCapability is {}", allAgents.size());

        final Set<Agent<?>> prosumers = new HashSet<>();
        Iterator<Agent<?>> it = allAgents.iterator();

        final Class<?> capabilityClass = BrokerPowerCapability.class;
        final ClassIdentifier capabilityIdentifier = new ClassIdentifier(
                capabilityClass);

        while (it.hasNext()) {
            Agent<?> agent = it.next();
            if (debug) LOG.debug("Checking registered agent {} for HolonCapability", agent.toString());
            if (agent.getPossesses(capabilityIdentifier) != null) {
                prosumers.add(agent);
                if (debug) LOG.debug("Agent {} added to local prosumers list", agent.toString());
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


}
