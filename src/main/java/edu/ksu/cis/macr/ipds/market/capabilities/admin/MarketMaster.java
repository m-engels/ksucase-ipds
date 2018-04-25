package edu.ksu.cis.macr.ipds.market.capabilities.admin;

import edu.ksu.cis.macr.aasis.agent.cc_m.IPersonaControlComponentMaster;
import edu.ksu.cis.macr.aasis.agent.persona.IPersona;
import edu.ksu.cis.macr.aasis.spec.OrganizationFocus;
import edu.ksu.cis.macr.goal.model.InstanceParameters;
import edu.ksu.cis.macr.goal.model.InstanceTreeChanges;
import edu.ksu.cis.macr.ipds.market.organizer.MarketReorganizationAlgorithm;
import edu.ksu.cis.macr.ipds.primary.capabilities.manage.Master;
import edu.ksu.cis.macr.obaa_pp.cc.om.EmptyOrganizationModel;
import edu.ksu.cis.macr.obaa_pp.cc.om.IOrganizationModel;
import edu.ksu.cis.macr.organization.model.Agent;
import edu.ksu.cis.macr.organization.model.Role;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
/**
 * Adds the functionality
 * necessary to act as the master for the local organization.
 */
public class MarketMaster extends Master implements IPersonaControlComponentMaster {
    private static final Logger LOG = LoggerFactory.getLogger(MarketMaster.class);
    private static final boolean debug = false;
    protected final Map<UniqueIdentifier, Agent<UniqueIdentifier>> agentQueue = new ConcurrentHashMap<>();
    protected IPersona persona;
    protected Set<Role> initialRoles;

    /**
     * @param name      -
     * @param persona   - the subagent
     * @param knowledge - the XML knowledge about the organization
     * @param focus     - an Enum that defines the focus of the organization
     */
    public MarketMaster(final String name, final IPersona persona, final Element knowledge, final OrganizationFocus focus) {
        super(name, persona, knowledge, focus);
        if (debug) LOG.debug("Done constructing abstract base control component.");

        if (debug) LOG.debug("Setting reorg algo for {}", name);
        this.setReorganizationAlgorithm(MarketReorganizationAlgorithm.createReorganizationAlgorithm(name));
        LOG.info("The external organization reorganization alogorithm={}.", this.getReorganizationAlgorithm().getClass().getSimpleName());

         try {
            final IOrganizationModel emptyOrgModel = new EmptyOrganizationModel().populate(rolepath, this.goalModel, capProvider);
            LOG.info("Org emptyOrgModel ={}", emptyOrgModel);
            setOrganizationModel(emptyOrgModel);
        } catch (Exception ex) {
             LOG.error("ERROR: could not set org model from rolefile={}, goalfile={}, capProvider={}. ", this.rolepath, this.goalModel, capProvider, ex);
             LOG.error("ERROR: Suggested debug - compare (1) organization role model (2) org agent capabilities in agent.xml and (3) CapabilityIdentifierProvider to make sure they are consistent. ");
            System.exit(-6);
        }
        if (this.focus == OrganizationFocus.External) {
            if (debug) LOG.info("Initializing goals...............................................................");
            final InstanceParameters topParams = getTopGoalInstanceParameters();
            InstanceTreeChanges changeList = getInitialGoalModelChangeList(topParams);
            updateInitialActiveGoals(changeList);
            setInitialRoles();
            if (debug) LOG.info("Done initializing goals..........................................................");
        }
    }





}
