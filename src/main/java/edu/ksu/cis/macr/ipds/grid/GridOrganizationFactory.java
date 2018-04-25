package edu.ksu.cis.macr.ipds.grid;


import edu.ksu.cis.macr.aasis.agent.admin.OrganizationFactory;
import edu.ksu.cis.macr.aasis.agent.persona.IOrganization;
import edu.ksu.cis.macr.aasis.spec.OrganizationFocus;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 Utility class for building organizations of sub-agents (persona). {@code AgentOrganizations} can be built from
 individual Agent.xml files describing the persona for a single agent or they can be built from standard rules based on
 their type.
 */
public class GridOrganizationFactory extends OrganizationFactory {
    private static final Logger LOG = LoggerFactory.getLogger(GridOrganizationFactory.class);
    private static final Boolean debug = false;


    public GridOrganizationFactory() {
    super();
  }



  /**
   Create an external organization.
   @return the organization created.
   @param absolutePath - the absolute path to where the configuration files are - usually at least the Agent.xml.
   @param focus - the organization focus - either Agent or External. Should be external.
   @param goalParameterValues - the goal parameters available (usually accessed from the INIT state of the org master role.
   */
  public IOrganization createBaseAffiliateOrganization(String absolutePath, OrganizationFocus focus,
                                                               Map<UniqueIdentifier, Object> goalParameterValues, String orgModelFolder) {

    LOG.info("Creating external org with orgmodelfolder={}.",orgModelFolder);

    // get the org model folder from the goal parameter values

    GridOrganization org = new GridOrganization(absolutePath, focus, orgModelFolder);
    try {
      org.setOrgModelFolder(orgModelFolder);
      org.loadInitialGoalGuidelines(goalParameterValues);
      loadObjectFile(org.getOrganizationSpecification().getObjectFile(), org);

    } catch (Exception e) {
      LOG.error("Error creating external organization. {}", e.getMessage());
      System.exit(-32);
    }
    return org;
  }


    public IOrganization createBaseAffiliateOrganization(String absolutePath,
                                                            OrganizationFocus focus, HashMap<UniqueIdentifier, Object> goalParameterValues,
                                                            String orgModelFolder, String goalFilePath, String roleFilePath, String topGoal) {
        LOG.info("Creating external org with orgmodelfolder={}.",orgModelFolder);

        // get the org model folder from the goal parameter values

        GridOrganization org = new GridOrganization(absolutePath, focus, orgModelFolder, goalFilePath,roleFilePath, topGoal);
        try {
            org.setOrgModelFolder(orgModelFolder);
            org.loadInitialGoalGuidelines(goalParameterValues);
            loadObjectFile(org.getOrganizationSpecification().getObjectFile(), org);

        } catch (Exception e) {
            LOG.error("Error creating external organization. {}", e.getMessage());
            System.exit(-32);
        }
        return org;
    }
}
