package edu.ksu.cis.macr.ipds.grid.capabilities.admin;

import edu.ksu.cis.macr.aasis.agent.cc_m.IPersonaControlComponentMaster;
import edu.ksu.cis.macr.aasis.agent.persona.IPersona;
import edu.ksu.cis.macr.aasis.spec.OrganizationFocus;
import edu.ksu.cis.macr.goal.model.InstanceParameters;
import edu.ksu.cis.macr.goal.model.InstanceTreeChanges;
import edu.ksu.cis.macr.ipds.grid.organizer.GridReorganizationAlgorithm;
import edu.ksu.cis.macr.ipds.primary.capabilities.manage.Master;
import edu.ksu.cis.macr.obaa_pp.cc.om.EmptyOrganizationModel;
import edu.ksu.cis.macr.obaa_pp.cc.om.IOrganizationModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
/**
 * Adds the functionality
 * necessary to act as the master for the local organization.
 */
public class GridMaster extends Master implements IPersonaControlComponentMaster {
    private static final Logger LOG = LoggerFactory.getLogger(GridMaster.class);
    private static final boolean debug = false;



    /**
     * @param name      - the agent / organization name
     * @param persona   - the subagent
     * @param knowledge - the XML knowledge about the organization
     * @param focus     - the enum of the focus the current organization is
     */
    public GridMaster(final String name, final IPersona persona, final Element knowledge, final OrganizationFocus focus) {
        super(name, persona, knowledge, focus);
        if (debug) LOG.debug("Entering constructor.");

        if (debug) LOG.debug("Setting reorg algo for {}", name);
        this.setReorganizationAlgorithm(GridReorganizationAlgorithm.createReorganizationAlgorithm(name));
        LOG.info("The external organization reorganization alogorithm={}.", this.getReorganizationAlgorithm().getClass().getSimpleName());

        try {
            LOG.debug("Setting affiliate org model from rolefile={}, goalfile={}, capProvider={}. ", this.rolepath, this.goalModel, capProvider);
            final IOrganizationModel om = new EmptyOrganizationModel().populate(rolepath, this.goalModel, capProvider);
            LOG.info("Org emptyOrgModel ={}", om);
            setOrganizationModel(om);
        } catch (Exception ex) {
            LOG.error("ERROR: could not set org model from rolefile={}, goalfile={}, capProvider={}. ", this.rolepath, this.goalModel, capProvider, ex);
            LOG.error("ERROR: Suggested debug - compare (1) organization role model (2) org agent capabilities in agent.xml and (3) CapabilityIdentifierProvider to make sure they are consistent. ");
            System.exit(-7);
        }
        if (this.focus == OrganizationFocus.External) {
            if (debug) LOG.info("Initializing goals...............................................................");
            final InstanceParameters topParams = getTopGoalInstanceParameters();
            InstanceTreeChanges changeList = getInitialGoalModelChangeList(topParams);
            updateInitialActiveGoals(changeList);
            setInitialRoles();
            if (debug)
                LOG.info("Done initializing goals (exiting GridMaster constructor and continuing to create persona)..........................................................");
        }
    }

}
