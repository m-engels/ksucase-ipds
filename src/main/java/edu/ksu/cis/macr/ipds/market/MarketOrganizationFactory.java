package edu.ksu.cis.macr.ipds.market;


import edu.ksu.cis.macr.aasis.agent.admin.OrganizationFactory;
import edu.ksu.cis.macr.aasis.agent.persona.IOrganization;
import edu.ksu.cis.macr.aasis.spec.OrganizationFocus;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for building organizations of sub-agents (persona). {@code AgentOrganizations} can be built from
 * individual Agent.xml files describing the persona for a single agent or they can be built from standard rules based on
 * their type.
 */
public class MarketOrganizationFactory extends OrganizationFactory {
    private static final Logger LOG = LoggerFactory.getLogger(MarketOrganizationFactory.class);
    private static final Boolean debug = false;
    /**
     * {@code ELEMENT_OBJECTS} is the description of object(s).
     */
    public static final String ELEMENT_OBJECTS = "objects";
    /**
     * {@code ATTRIBUTE_CATEGORY}
     */
    public static final String ATTRIBUTE_CATEGORY = "category";
    /**
     * {@code ELEMENT_SIMPLE}
     */
    public static final String ELEMENT_SIMPLE = "simple";
    /**
     * {@code ELEMENT_COMPLEX}
     */
    public static final String ELEMENT_COMPLEX = "complex";
    /**
     * {@code ELEMENT_ATTRIBUTE}
     */
    public static final String ELEMENT_ATTRIBUTE = "attribute";


    public MarketOrganizationFactory() {
        super();
    }

    /**
     * Loads up the given {@code Organization} with objects from the given file.
     *
     * @param objectFilename the {@code String} representing the file to load from.
     * @param organization   the {@code Organization} to load into.
     */
    public static void loadObjectFile(final String objectFilename,
                                      final IOrganization organization) {
        if (objectFilename == null || objectFilename.isEmpty()) {
            return;
        } // optional
        loadObjectFile(new File(objectFilename), organization);
    }

    /**
     * Loads up the given {@code Organization} with objects from the given {@code File}.
     *
     * @param objectFile   the {@code File} to load from.
     * @param organization the {@code Organization} to load into.
     */
    public static void loadObjectFile(final File objectFile,
                                      IOrganization organization) {
        if (objectFile == null) {
            return;
        } // it's optional
        if (objectFile.exists()) {
            try {
                DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
                DocumentBuilder b = f.newDocumentBuilder();
                NodeList n = b.parse(objectFile).getElementsByTagName(ELEMENT_OBJECTS);
                if (debug) LOG.debug(
                        "loadObjectFile() object node list with {} items from" +
                                " file {}",
                        n.getLength(), objectFile.getAbsolutePath()
                );
                setupObjects(n, organization);
            } catch (ParserConfigurationException | SAXException |
                    IOException e) {
                LOG.error("Error loading object file: {}",
                        objectFile.getPath());
            }
        } else {
            if (debug) LOG.debug("{} does not exist.", objectFile);
        }
    }

    /**
     * Create an external organization.
     *
     * @param absolutePath        - the absolute path to where the configuration files are - usually at least the Agent.xml.
     * @param focus               - the organization focus - either Agent or External. Should be external.
     * @param goalParameterValues - the goal parameters available (usually accessed from the INIT state of the org master role.
     * @return the organization created.
     */
    public IOrganization createBaseAffiliateOrganization(String absolutePath, OrganizationFocus focus,
                                                                 Map<UniqueIdentifier, Object> goalParameterValues, String orgModelFolder) {

        LOG.info("Creating external org with orgmodelfolder={}.", orgModelFolder);

        // get the org model folder from the goal parameter values

        MarketOrganization org = new MarketOrganization(absolutePath, focus, orgModelFolder);
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
        LOG.info("Creating external org with orgmodelfolder={}.", orgModelFolder);

        // get the org model folder from the goal parameter values

        MarketOrganization org = new MarketOrganization(absolutePath, focus, orgModelFolder, goalFilePath, roleFilePath, topGoal);
        try {
            org.setOrgModelFolder(orgModelFolder);
            org.loadInitialGoalGuidelines(goalParameterValues);
            loadObjectFile(org.getOrganizationSpecification().getObjectFile(), org);

        } catch (Exception e) {
            LOG.error("Error creating external organization. {}", e.getMessage());
            throw new RuntimeException(e);
        }
        return org;
    }
}
