package edu.ksu.cis.macr.ipds.self.persona;


import edu.ksu.cis.macr.aasis.agent.persona.ICapability;
import edu.ksu.cis.macr.aasis.agent.persona.IOrganization;
import edu.ksu.cis.macr.aasis.agent.persona.IPersona;
import edu.ksu.cis.macr.aasis.agent.persona.factory.ICapabilityConstructorValues;
import edu.ksu.cis.macr.aasis.self.IInnerOrganization;
import edu.ksu.cis.macr.aasis.spec.OrganizationFocus;
import edu.ksu.cis.macr.ipds.primary.persona.factory.PersonaFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Objects;

/**
 * The {@code SelfPersonaFactory} assists with the creation of persona in external organizations.
 */
public class SelfPersonaFactory extends PersonaFactory {
    private static final Logger LOG = LoggerFactory.getLogger(SelfPersonaFactory.class);
    private static final Boolean debug =  true;


    /**
     * Save agents from the given {@code Organization} to the specified {@code File}.
     *
     * @param agentFile    the {@code File} to save to.
     * @param organization the {@code Organization} to save from.
     */
    public static void savePersonaFile(final File agentFile, final IOrganization organization) {
        // nothing
    }

    /**
     * Setup agents with the given list of persona into the given organization.
     *
     * @param agentsList   the node list of persona sub-agents.
     * @param organization the {@code IOrganization} in which they participate.
     */
    public static void setupPersonaList(NodeList agentsList, IOrganization organization) {
        agentsList = Objects.requireNonNull(agentsList, "Persona cannot be null.");
        organization = Objects.requireNonNull(organization, "Organization cannot be null.");
        if (agentsList.getLength() < 1) {
            LOG.error("Error: Agent list can't be empty.");
            return;
        }
        for (int i = 0; i < agentsList.getLength(); i++) {
            if (debug) LOG.debug("");
            if (debug)
                LOG.debug("{}: getting constructors for persona type {} of {}.", organization.getName(), i + 1, agentsList.getLength());

            final Element agentsElement = (Element) agentsList.item(i);
            final String stringPackage = getPackageName(agentsElement);
            final String agentType = getType(agentsElement);

            final NodeList orgList = agentsElement.getElementsByTagName(ELEMENT_ORGANIZATION);
            final Node orgNode = orgList.item(0);
            Element orgElement = null;
            if (1 == orgNode.getNodeType()) {
                orgElement = (Element) orgNode;
            }

            final String orgControlPackage = getPackageName(orgElement);
            final String orgControlType = getType(orgElement);
            if (debug)
                LOG.info("orgControlType: {}, package={}, agentType={}", orgControlType, stringPackage, agentType);

            final AttributeConstructorValues[] attributes = setupAttributes(agentsElement
                    .getElementsByTagName(ELEMENT_ATTRIBUTE));

            final ICapabilityConstructorValues[] capabilityValues = setupCapabilities(
                    agentsElement.getElementsByTagName(ELEMENT_CAPABILITY),
                    organization);

            final NodeList personaList = agentsElement.getElementsByTagName(ELEMENT_AGENT);

            for (int j = 0; j < personaList.getLength(); j++) {

                final Node agentElement = personaList.item(j);
                final String agentName = agentElement.getFirstChild().getNodeValue();
                if (debug) LOG.debug("Instantiating persona {}. Getting {} constructor.", agentName,
                        agentType);

                Constructor<? extends IPersona> sec_con = SelfPersonaFactory.getExecutionComponentConstructor(
                        SelfPersonaFactory.getExecutionComponentClass(
                                stringPackage + "." + agentType));
                if (debug) LOG.debug("This self organization's top goal is: {}",
                        organization.getOrganizationSpecification().getTopGoal());
                if (debug)
                    LOG.debug("Constructing an EC persona {} which will be given an associated CC component.", agentName);
                if (debug)
                    LOG.debug("This agent's internal guidelines are: {}. Beginning creation of execution component. ", organization.getGoalParameterValues());

                // create a new Execution Component IAgent for this persona (which has a CC either Master or Slave).............................
                if (debug)
                    LOG.debug("Calling createEC org={}, orgEl={}, attribs={}, capValues={}, agentname={}, sec_con={}.", organization, orgElement, attributes, capabilityValues, agentName, sec_con);
                createEC(organization, orgElement, attributes, capabilityValues, agentName, sec_con);
                LOG.info("EVENT: SUB-AGENT_LOADED. agentOrg={}, aubAgentName={} added with EC-CC pair.", organization.getName(), agentName);

            }
        }
    }


    protected synchronized static void createEC(IInnerOrganization organization, Element orgElement, AttributeConstructorValues[] attributes, ICapabilityConstructorValues[] capabilityValues, String agentName, Constructor<? extends IPersona> sec_con) {
        LOG.info("Entering PersonaFactory.createEC(personaorg={}, orgElement={}, attributes={}, capconstructorvalues={}, agentName={}, sec_con).)",organization,orgElement,attributes,capabilityValues, agentName, sec_con);
        final IPersona persona = Objects
                .requireNonNull(newExecutionComponent(sec_con, organization, agentName,
                                orgElement, OrganizationFocus.Agent),
                        "Error - The executionComponent is null.");

        LOG.info("Created EC {}. Getting attribute constructor values.", agentName);
        for (final AttributeConstructorValues a : attributes) {
            persona.addAttribute(newAttribute(a.constructor, a.parameters));
        }
        if (debug) LOG.debug("Getting {} EC capability constructor values.", capabilityValues.length);
        for (final ICapabilityConstructorValues v : capabilityValues) {
            if (debug) LOG.debug("\tAdding: {}", v.getConstructor());
            v.getParameters()[0] = persona;
            if (debug) LOG.debug("Adding capability {} to execution_component {}.", v.getConstructor(),
                    persona.getIdentifierString());
            persona.addCapability(newCapability(v.getConstructor(), v.getParameters()));
        }

        LOG.info("Exiting createEC.");

    }


//    /**
//     * Setup agents with the given list of persona into the given organization.
//     *
//     * @param agentsList   the node list of persona sub-agents.
//     * @param organization the {@code IOrganization} in which they participate.
//     */
//    public synchronized static void setupPersonaList(NodeList agentsList, IOrganization organization) {
//        LOG.info("Entering setupPersonaList(agentsList={}, organization={})", agentsList,organization);
//        Objects.requireNonNull(agentsList, "Persona cannot be null.");
//        Objects.requireNonNull(organization, "Organization cannot be null.");
//        LOG.info("Entering setupPersonaList(organization.specification={})",organization.getOrganizationSpecification());
//        organization.setName(organization.getOrganizationSpecification().getOrganizationName());
//
//        if (agentsList.getLength() < 1) {
//            LOG.error("Error: Agent list can't be empty.");
//            return;
//        }
//        for (int i = 0; i < agentsList.getLength(); i++) {
//                 LOG.debug("{}: getting constructors for persona type {} of {}.", organization.getName(), i + 1, agentsList.getLength());
//
//            final Element agentsElement = (Element) agentsList.item(i);
//            final String stringPackage = getPackageName(agentsElement);
//            final String agentType = getType(agentsElement);
//
//            final NodeList orgList = agentsElement.getElementsByTagName(ELEMENT_ORGANIZATION);
//            final Node orgNode = orgList.item(0);
//            Element orgElement = null;
//            if (1 == orgNode.getNodeType()) {
//                orgElement = (Element) orgNode;
//            }
//
//            final String orgControlPackage = getPackageName(orgElement);
//            final String orgControlType = getType(orgElement);
//            LOG.info("orgControlType: {}, package={}, agentType={}", orgControlType, orgControlPackage, agentType);
//
//            final AttributeConstructorValues[] attributes = setupAttributes(agentsElement
//                    .getElementsByTagName(ELEMENT_ATTRIBUTE));
//
//            final ICapabilityConstructorValues[] capabilityValues = setupCapabilities(
//                    agentsElement.getElementsByTagName(ELEMENT_CAPABILITY),
//                    (IOrganization) organization);
//
//            final NodeList personaList = agentsElement.getElementsByTagName(ELEMENT_AGENT);
//
//            for (int j = 0; j < personaList.getLength(); j++) {
//
//                final Node agentElement = personaList.item(j);
//                final String agentName = agentElement.getFirstChild().getNodeValue();
//                if (debug) LOG.debug("Instantiating persona {}. Getting {} constructor.", agentName,
//                        agentType);
//
//                Constructor<? extends IPersona> sec_con = SelfPersonaFactory.getExecutionComponentConstructor(
//                        SelfPersonaFactory.getExecutionComponentClass(
//                                stringPackage + "." + agentType));
////                if (debug) LOG.debug("This self organization's top goal is: {}",
////                        organization.getOrganizationSpecification().getTopGoalName());
////                if (debug)
////                    LOG.debug("Constructing an EC persona {} which will be given an associated CC component.", agentName);
////                if (debug)
////                    LOG.debug("This agent's internal guidelines are: {}. Beginning creation of execution component. ", organization.getGoalParameterValues());
//
//                // create a new Execution Component IAgent for this persona (which has a CC either Master or Slave).............................
//                if (debug)
//                    LOG.debug("Calling createEC org={}, orgEl={}, attribs={}, capValues={}, agentname={}, sec_con={}.", organization, orgElement, attributes, capabilityValues, agentName, sec_con);
//                createEC( organization, orgElement, attributes, capabilityValues, agentName, sec_con);
//                LOG.info("EVENT: SUB-AGENT_LOADED. agentOrg={}, aubAgentName={} added with EC-CC pair.", organization.getName(), agentName);
//                if (agentName.contains("self")){
//                    organization.setLocalMaster(StringIdentifier.getIdentifier(agentName));
//                    LOG.info("localMaster={}", organization.getLocalMaster());
//                }
//
//            }
//        }
//        LOG.info("Exiting setupPersonaList.");
//    }

    /**
     * Returns the {@code Class} of an {@code ICapability}.
     *
     * @param className the {@code String} representing * the {@code Class} of an {@code ICapability}.
     * @return the {@code Class} of an {@code ICapability}.
     */
    public static Class<? extends ICapability> getCapabilityActionClass(
            final String className) {
        if (debug) LOG.debug("In getCapabilityActionClass looking up ICapability derived class for {}.", className);
        try {
            if (debug)
                LOG.debug("In getCapabilityActionClass    {}", Class.forName(className).asSubclass(ICapability.class));
            return Class.forName(className).asSubclass(ICapability.class);

        } catch (final ClassNotFoundException e) {
            LOG.error("ERROR: class not found. class={}. {}", className, e.getCause().toString());
        }
        return null;
    }

    /**
     * Returns the {@code Constructor} of the {@code ICapability} {@code Class}.
     *
     * @param capabilityClass the {@code Class} of * the {@code ICapability} to get * the {@code Constructor}.
     * @param parameterTypes  the parameters' type for * the {@code Constructor}.
     * @return the {@code Constructor} of * the {@code ICapability} .
     */
    public static Constructor<? extends ICapability> getCapabilityActionConstructor(
            final Class<? extends ICapability> capabilityClass,
            final Class<?>... parameterTypes) {
        Constructor<? extends ICapability> constructor = null;
        try {
            if (debug)
                LOG.debug("In getCapabilityActionConstructor    {} with params {}", capabilityClass, parameterTypes);
            constructor = capabilityClass.getConstructor(parameterTypes);
        } catch (SecurityException e) {
            LOG.error("Security exception in capability action constructor. {}", e.getMessage());
            System.exit(-33);
        } catch (NoSuchMethodException e) {
            LOG.error("No capability action constructor could be found. {}", e.getMessage());
            System.exit(-33);
        }
        return constructor;
    }


    /**
     * Returns the {@code Class} of an {@code IAgent}.
     *
     * @param className the {@code String} representing * the {@code Class} of an {@code IAgent}.
     * @return the {@code Class} of an {@code IAgent}.
     */
    public static Class<? extends IPersona> getExecutionComponentClass(final String className) {
        if (debug) LOG.debug("getting execution component class for {}", className);
        try {
            return Class.forName(className).asSubclass(IPersona.class);
        } catch (final ClassNotFoundException e) {
            LOG.error("ERROR: ClassNotFoundException for className={}.",
                    className);
            System.exit(-91);
        }
        return null;
    }

    /**
     * Returns the {@code Constructor} of the {@code IPersona} {@code Class}.
     *
     * @param executionComponentClass the {@code Class} of the {@code IPersona} to get the {@code Constructor}.
     * @return the {@code Constructor} of the {@code IAgent}.
     */
    public static Constructor<? extends IPersona> getExecutionComponentConstructor(
            final Class<? extends IPersona> executionComponentClass) {
           if (debug) LOG.debug("Getting execution component constructor for {}.", executionComponentClass);
        Constructor<? extends IPersona> constructor = null;
        try {
            constructor = executionComponentClass.getConstructor(
                    IOrganization.class,
                    String.class,
                    Element.class,
                    OrganizationFocus.class);
        } catch (Exception e) {
            LOG.error("ERROR: getting execution component constructor for {}.",
                    executionComponentClass, e);
            System.exit(-91);
        }
        return constructor;
    }

    /**
     * Instantiates a new instance of {@code ICapability} with the given {@code Constructor} and parameters.
     *
     * @param constructor the {@code Constructor} for the new instance.
     * @param parameters  the parameters for the {@code Constructor}.
     * @return a new instance of {@code ICapability}.
     */
    public static ICapability newCapability(
            final Constructor<? extends ICapability> constructor,
            final Object... parameters) {
        return instantiate(constructor, parameters);
    }


    /**
     * Instantiates a new instance of {@code IPersona} with the given {@code Constructor}, organization, identifier, and
     * organization.
     *
     * @param constructor  the {@code Constructor} for the new instance.
     * @param organization the organization where the new instance goes.
     * @param identifier   the {@code String} by which to identify the new instance.
     * @param knowledge    the organization information for the agent.
     * @param focus        the {@code Enum} by which to identify what the focus is.
     * @return a new instance of {@code IAgent}.
     */
    public static IPersona newExecutionComponent(
            Constructor<? extends IPersona> constructor,
            IOrganization organization,
            String identifier,
            Element knowledge,
            OrganizationFocus focus) {
            LOG.info("Creating NEW EXECUTION COMPONENT of the type IPersona.");

        constructor = Objects.requireNonNull(constructor,
                "New execution component agent (used to be SEC) constructor cannot be null.");
        organization = Objects.requireNonNull(organization,
                "New execution component organization (organization) cannot be null.");
        identifier = Objects.requireNonNull(identifier,
                "New execution component identifier cannot be null.");

        return instantiate(constructor, organization, identifier, knowledge, focus);
    }




    private static String getType(final Element xmlElement) {
        Objects.requireNonNull(xmlElement, "xml Element cannot be null");
        final String type = xmlElement.getAttribute(ATTRIBUTE_TYPE);
        //     LOG.info("Type: {}", type);
        return type;
    }

    private static String getPackageName(final Element xmlElement) {
        Objects.requireNonNull(xmlElement, "xml Element cannot be null");
        final String packageName = xmlElement
                .getAttribute(ATTRIBUTE_PACKAGE);
        return packageName;
    }


}
