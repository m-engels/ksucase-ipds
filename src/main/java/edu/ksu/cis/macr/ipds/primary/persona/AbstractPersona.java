package edu.ksu.cis.macr.ipds.primary.persona;

import edu.ksu.cis.macr.aasis.agent.cc_a.IAbstractControlComponent;
import edu.ksu.cis.macr.aasis.agent.persona.*;
import edu.ksu.cis.macr.aasis.spec.OrganizationFocus;
import edu.ksu.cis.macr.obaa_pp.cc.om.IXMLFormattableKnowledge;
import edu.ksu.cis.macr.obaa_pp.ec.AgentDisabledException;
import edu.ksu.cis.macr.obaa_pp.ec_task.ITask;
import edu.ksu.cis.macr.obaa_pp.events.IEventManager;
import edu.ksu.cis.macr.obaa_pp.events.IOrganizationEvent;
import edu.ksu.cis.macr.obaa_pp.events.OrganizationEvents;
import edu.ksu.cis.macr.obaa_pp.objects.AbstractObject;
import edu.ksu.cis.macr.obaa_pp.objects.IDisplayInformation;
import edu.ksu.cis.macr.organization.model.Capability;
import edu.ksu.cis.macr.organization.model.identifiers.StringIdentifier;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.Map.Entry;

/**
 * The {@code AbstractAgent} class provides the necessary functionality for creating a {@code
 * IPersona}.
 *
 * @author Christopher Zhong Revision: 1.27.4.9 Date: 2011/11/04 14:31:58
 * @see edu.ksu.cis.macr.aasis.agent.persona.IPersona
 */
public abstract class AbstractPersona extends AbstractObject implements IPersona {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractPersona.class);
    private static final boolean debug = false;
    /**
     * {@code ELEMENT_AGENTS} is the description of agent(s).
     */
    private static final String ELEMENT_AGENTS = "agents";
    /**
     * {@code ELEMENT_AGENT}
     */
    private static final String ELEMENT_AGENT = "agent";
    /**
     * The {@code UniqueIdentifier} that uniquely identifies the {@code AbstractAgent}.
     */
    private final UniqueIdentifier agentIdentifier;

    public OrganizationFocus focus;
    CapabilityManager capabilityManager;
    /**
     * The {@code Organization} in which the {@code IAgent} exists.
     */
    protected IOrganization organization;
    /**
     * The organization-based reasoning component of the {@code IAgent}.
     */
    protected IAbstractControlComponent controlComponent;
    /**
     * The {@code IInternalCommunicationCapability} capability of this {@code IAgent}.
     */
    protected IInternalCommunicationCapability internalCommunicationCapability = null;

    protected OrganizationEvents organizationEvents;
    /**
     * If the {@code IAgent} is still running, {@code alive} is {@code true}, otherwise {@code alive} is {@code false}.
     */
    private boolean alive = true;

    /**
     * Constructs a new instance of {@code AbstractAgent}.
     *
     * @param organization the {@code Organization} in which the {@code IAgent} will exist.
     * @param identifier   the unique name to identify the {@code IAgent}.
     * @param knowledge    the {@code DOM} {@code Element} that contains information about the {@code Organization}.
     * @param focus        the {@code Enum} that show what focus the current organization is.
     */
    protected AbstractPersona(final IOrganization organization, final String identifier, final Element knowledge, final OrganizationFocus focus) {
        super(identifier);
        this.organization = organization;
        this.agentIdentifier = StringIdentifier.getIdentifier(identifier);
        this.capabilityManager = new CapabilityManager(agentIdentifier);
        this.setOrganizationEvents(new OrganizationEvents(identifier));

        LOG.info("\t..................INITIALIZING ABSTRACT PERSONA {}..............", agentIdentifier);

        if (organization == null) {
            if (debug) LOG.debug("Creating EMPTY CONTROL COMPONENT in null organization");
            controlComponent = new EmptyControlComponent(identifier, this, null, focus);
        } else if (knowledge.getAttribute(IXMLFormattableKnowledge.ATTRIBUTE_TYPE).equals("Participant")) {
            LOG.info("Skipping Participant");
            controlComponent = new EmptyControlComponent(identifier, this, knowledge, focus);
        } else {
            if (debug)
                LOG.debug("Creating CONTROL COMPONENT in {}.  Org={}, knowledge={}, focus={}", identifier, organization, knowledge, focus);
            controlComponent = setupControlComponent(identifier, this, knowledge, focus);
        }
        if (debug) LOG.debug("Finished creating CC. cc={}", controlComponent);

        InternalCommunicationCapability c = new InternalCommunicationCapability(this, this.organization,
                InternalCommunicationCapability.MAX_RANGE_IN_FEET, AbstractOrganizationCapability.MIN_FAILURE,
                AbstractOrganizationCapability.MIN_FAILURE);

        setInternalCommunicationCapability(c);

        // The following has been broken out .....false means run them in the launcher
        // boolean intiializeGoalsInConstructors = true;
        //  if (intiializeGoalsInConstructors) {

        //==============================================================================================
        this.setOrganizationEvents(controlComponent.getOrganizationEvents());
        if (debug)
            LOG.debug("Setting the {} EC initial organization events from the CC Organization Events. They are {}.",
                    this.getOrganizationEvents().numberOfQueuedEvents(), this.getOrganizationEvents());

        /*
         * if loading of agents was successful AND debugging is enable then log
         * success info, otherwise if loading of agents was NOT successful AND
         * debugging is enable then log failure info.
         */
        final boolean agentAdded = this.organization.addAgent(this);
        final String message;

        if (agentAdded) message = "Success";
        else message = "Failed";

        if (!agentAdded) LOG.info("AbstractAgent was NOT added to Organization: {}", message);
    }

    /**
     * Constructs a new instance of {@code AbstractAgent}.
     *
     * @param identifierString the unique name to identify the {@code IAgent}.
     */
    protected AbstractPersona(final String identifierString) {
        super(identifierString);

        LOG.info("\t..................INITIALIZING ABSTRACT PERSONA {} FROM JUST IDENTIFIER STRING..............", identifierString);

        agentIdentifier = StringIdentifier.getIdentifier(identifierString);
        this.organization = new Organization();
        this.capabilityManager = new CapabilityManager(agentIdentifier);
        this.organizationEvents = new OrganizationEvents(agentIdentifier.toString());
    }

    /**
     * Initialize the {@code IAbstractControlComponent}.
     *
     * @param identifier the {@code String} identifying the {@code IAbstractControlComponent}.
     * @param persona    the {@code IAgent} for the {@code IAbstractControlComponent}.
     * @param knowledge  the {@code Element} that contains the necessary information for initializing the {@code IAbstractControlComponent}.
     * @param focus      the (@code OrganizationFocus) that contains what kind of focus the object is (Agent or External).
     * @return the initialized {@code IAbstractControlComponent}.
     */
    private static IAbstractControlComponent setupControlComponent(
            final String identifier,
            final IPersona persona,
            final Element knowledge, final OrganizationFocus focus) {

        final String className = knowledge.getAttribute(IXMLFormattableKnowledge.ATTRIBUTE_PACKAGE)
                + "." + knowledge.getAttribute(IXMLFormattableKnowledge.ATTRIBUTE_TYPE);
        LOG.info("Setting up new control component class={}. Identifier={}, persona={}, knowledge={}, focus={}. ", className, identifier, persona, knowledge, focus);

        final Class<? extends IAbstractControlComponent> controlComponentClass = getControlComponentClass(className);
        if (debug) LOG.debug("Constructing new control component. Class ={}", controlComponentClass.getSimpleName());
        final Constructor<? extends IAbstractControlComponent> controlComponentConstructor = getControlComponentConstructor(controlComponentClass);
        if (debug) LOG.debug("Constructing new control component {}", controlComponentConstructor.getName());
        return newControlComponent(controlComponentConstructor, identifier, persona, knowledge, focus);
    }

    /**
     * Instantiates a new instance of {@code IAbstractControlComponent} with the given {@code Constructor}, identifier,
     * robot, and knowledge.
     *
     * @param constructor        the {@code Constructor} for the new instance.
     * @param identifier         the name by which to identify the new instance.
     * @param executionComponent the robot where the new instance goes.
     * @param knowledge          the knowledge for the new instance.
     * @param focus              the enum that contains what kind of focus the object is (Agent or External).
     * @return a new instance of {@code IAbstractControlComponent}.
     */
    private static IAbstractControlComponent newControlComponent(
            final Constructor<? extends IAbstractControlComponent> constructor,
            final String identifier,
            final IPersona executionComponent,
            final Element knowledge,
            final OrganizationFocus focus) {
        LOG.debug("Entering newControlComponent(constructor={}, identifier={},executionComponent={},knowledge={},focus={}).", constructor, identifier, executionComponent, knowledge, focus);

        return instantiate(constructor, Objects.requireNonNull(identifier), Objects.requireNonNull(executionComponent), Objects.requireNonNull(knowledge), focus);
    }

    /**
     * Returns the {@code Constructor} of the {@code IAbstractControlComponent} {@code Class}.
     *
     * @param controlComponentClass the {@code Class} of the {@code IAbstractControlComponent} to get the {@code Constructor}.
     * @return the {@code Constructor} of the {@code IAbstractControlComponent}.
     */
    private static Constructor<? extends IAbstractControlComponent> getControlComponentConstructor(
            final Class<? extends IAbstractControlComponent> controlComponentClass) {
        LOG.debug("Entering getControlComponentConstructor(controlComponentClass={}).", controlComponentClass);
        Constructor<? extends IAbstractControlComponent> constructor = null;
        try {

            constructor = controlComponentClass.getConstructor(
                    String.class,
                    IPersona.class,
                    Element.class,
                    OrganizationFocus.class
            );

        } catch (SecurityException e) {
            LOG.error("ERROR: getCC constructor security exception {} {}", e.getClass(), e.getMessage());
            System.exit(-4);
        } catch (NoSuchMethodException e) {
            LOG.error("ERROR: NO SUCH METHOD{} {}", e.getClass(), e.getMessage());
            System.exit(-5);
        }
        LOG.debug("Exiting getControlComponentConstructor. constructor={}).", constructor);
        return constructor;
    }

    /**
     * Returns the {@code Class} of an {@code IAbstractControlComponent}.
     *
     * @param className the {@code String} representing the {@code Class} of an {@code IAbstractControlComponent}.
     * @return the {@code Class} of an {@code IAbstractControlComponent}.
     */
    private static Class<? extends IAbstractControlComponent> getControlComponentClass(
            final String className) {
        try {
            LOG.debug("Entering getControlComponentClass(className={}).", className);

            return Class.forName(className).asSubclass(
                    IAbstractControlComponent.class);
        } catch (final ClassNotFoundException e) {
            LOG.error("ERROR: CLASS NOT FOUND {} {}", e.getClass(), e.getMessage());
            System.exit(-75);
        }
        return null;
    }

    /**
     * Instantiates a new instance of the object with the given {@code Constructor} and parameters.
     *
     * @param <T>         the type of the new instance.
     * @param constructor the {@code Constructor} for the new instance.
     * @param parameters  the parameters for the {@code Constructor}.
     * @return a new instance of the object.
     */
    private static <T> T instantiate(final Constructor<T> constructor, final Object... parameters) {
        LOG.debug("Entering instantiate(constructor={}, parameters={}).", constructor, parameters);
        try {
            if (debug) LOG.debug("instantiate() with constructor={}, parameters={}", constructor, parameters.length);
            for (Object o : parameters) {
                if (debug) LOG.debug("instance parameter = {}", o.toString());
            }
            return constructor.newInstance(parameters);
        } catch (InstantiationException e) {
            LOG.error("ERROR: {} {}", e.getClass(), e.getMessage());
            System.exit(-74);
        } catch (IllegalAccessException e) {
            LOG.error("ERROR: {} {}", e.getClass(), e.getMessage());
            System.exit(-73);
        } catch (InvocationTargetException e) {
            LOG.error("ERROR: instantiating T={} {} {}", constructor.getName(), e.getClass(), e.getMessage());
            System.exit(-72);
        }
        return null;
    }

    /**
     * Adds a new {@code ICapability} to the {@code IAgent}. If the {@code replace} parameter is {@code true}, then a
     * replacement operation will be done to ensure that all appropriate instances are replaced with the given {@code ICapability}.
     *
     * @param capability the {@code ICapability} to be added.
     */
    public void addCapability(final ICapability capability) {
        if (debug) LOG.debug("Adding capability: {}", capability);
        this.capabilityManager.addCapability(capability, true);
    }

    @Override
    public void disable() {
        alive = false;
    }

    @Override
    public void endTurn() {
        organization.endTurn();
        controlComponent.executeControlComponentPlan();
    }

    @Override
    public Collection<ICapability> getCapabilities() {
        return this.capabilityManager.getCapabilities();
    }

    @Override
    public Map<UniqueIdentifier, ICapability> getCapabilitiesMapping() {
        return this.capabilityManager.getCapabilitiesMapping();
    }

    @Override
    public <CapabilityType extends Capability> CapabilityType getCapability(
            final Class<CapabilityType> capabilityClass) {
        return this.capabilityManager.getCapability(capabilityClass);
    }

    @Override
    public double getCapabilityScore(final Capability capability) {
        return this.capabilityManager.getCapabilityScore(capability);
    }

    @Override
    public IPersonaControlComponent getPersonaControlComponent() {
        return this.controlComponent;
    }

    @Override
    public String getHostFromIdentifier(UniqueIdentifier uniqueIdentifier) {
        if (debug) LOG.debug("Getting host from persona name. ID={}.", uniqueIdentifier);
        String agent = "";

        if (uniqueIdentifier.toString().contains("in")) {
            agent = uniqueIdentifier.toString().substring(0, uniqueIdentifier.toString().indexOf("in"));
        }
        if (debug) LOG.debug("The host agent is {}.", agent);

        String host;
        if (agent.contains("_")) {
            host = agent.substring(0, agent.indexOf("_"));
        } else {
            host = agent;
        }
        // TODO: Make more general - the host name doesn't include a suffix for an organization proxy.
        if (host.endsWith("A")) {
            // then the "A" is just a suffix for the market organization, remove it
            host = host.replace("A", "");
        }
        return host;
    }

    @Override
    public IOrganization getOrganization() {
        return this.organization;
    }

    @Override
    public String getOrganizationFromIdentifier(UniqueIdentifier uniqueIdentifier) {
        return getOrganizationFromPersonaName(uniqueIdentifier.toString());
    }

    @Override
    public String getOrganizationFromPersonaName(String personaName) {
        // e.g. H44_FinN43
        String org;

        // if in an organization, get the name
        if (personaName.contains("in")) {
            org = personaName.substring(personaName.indexOf("in") + 2);
        }

        // if I'm a self persona, then I must be in the self organization
        else if (personaName.contains("self")) {
            org = personaName;
        }

        // otherwise, I'm also not in an external org, so assume I'm in my own self org
        else {
            org = "self" + personaName;
        }
        return org;
    }

    @Override
    public double getPossessesScore(final UniqueIdentifier capabilityIdentifier) {
        return this.getPersonaControlComponent().getOrganizationModel().getAgent(this.getUniqueIdentifier()).getPossessesScore(capabilityIdentifier);
    }

    @Override
    public UniqueIdentifier getUniqueIdentifier() {
        return this.agentIdentifier;
    }

    /**
     * Informs the {@code IControlComponent} of an {@code OrganizationEvent}.
     *
     * @param events the {@code OrganizationEvent} to inform the {@code IControlComponent}.
     */
    void informControlComponent(final List<IOrganizationEvent> events) {
        this.controlComponent.getOrganizationEvents().addEventListToQueue(events);
    }

    @Override
    public boolean isAlive() {
        return alive;
    }

    /**
     * Receives a messages that was sent to the {@code IAgent}.
     *
     * @return the {@code Object} that was received, {@code null} otherwise.
     * @see IInternalCommunicationCapability#receive()
     */
    protected Object receive() {
        return internalCommunicationCapability.receive();
    }

    @Override
    public void reset() {
        if (debug) LOG.debug("Entering reset(). Will call all capability's reset()");
        this.capabilityManager.reset();
    }

    /**
     * Calls the {@code IAbstractControlComponent#executeControlComponentPlan()}
     * and the {@code #execute()} method.
     *
     * @see Runnable#run()
     */
    @Override
    public void run() {
        if (debug) LOG.debug("Entering run(). Will call organization.enableAgent()");
        this.organization.enableAgent();
        try {
            // first we call the cc execute which runs the cc plan

            if (debug) LOG.debug("***** Calling initial executeControlComponentPlan()");
            controlComponent.executeControlComponentPlan();
            execute(); // runs while alive and calls cc with every endturn
        } catch (final AgentDisabledException e) {
            /* agent has been disabled for some reason */
            organization.disableAgent();  // Denise - moved to inside catch
        } catch (final IllegalArgumentException ex) {

            LOG.error("ERROR in run() {}. Illegal arg exception: {}  {}", this.agentIdentifier.toString(), ex.getMessage(), Arrays
                    .toString(ex.getStackTrace()));
            System.exit(-11);
        }

        if (debug) LOG.debug("Exiting ap.run().");
    }

    /**
     * Sends a messages to the given {@code IAgent}, only if the {@code IAgent} is within range.
     *
     * @param toAgent   the {@code IAgent} to receive the messages.
     * @param channelID the type of messages, used * * * * * * * * * * * * * * * by {@code ICommunicationChannel} .
     * @param content   the messages to be sent.
     * @return {@code true} if the messages was sent successfully, {@code false} otherwise.
     */
    protected boolean send(final UniqueIdentifier toAgent,
                           final String channelID, final Object content) {
        return internalCommunicationCapability.send(toAgent, channelID, content);
    }

    /**
     * Sets the {@code IInternalCommunicationCapability} to the given {@code IInternalCommunicationCapability}.
     *
     * @param internalCommunicationCapability the new {@code IInternalCommunicationCapability} to set.
     */
    private <CommunicationType extends IInternalCommunicationCapability & ICapability> void setInternalCommunicationCapability(CommunicationType internalCommunicationCapability) {
        LOG.debug("Entering setInternalCommunicationCapability(internalCommunicationCapability={})", internalCommunicationCapability);
        if (this.internalCommunicationCapability == null) {
            controlComponent = Objects.requireNonNull(controlComponent, "controlComponent cannot be null");
            if (debug)
                LOG.debug("Setting up new internalCommunicationCapability. CC is {}", controlComponent.getIdentifier());
            internalCommunicationCapability.addChannel(controlComponent.getCommunicationChannelID(), controlComponent);
        } else {
            if (debug)
                LOG.debug("Setting up existing internalCommunicationCapability. CC is {}", controlComponent.getIdentifier());
            this.capabilityManager.capabilities.put(
                    this.internalCommunicationCapability.getIdentifier(),
                    new CapabilityManager.CapabilityWrapper(internalCommunicationCapability));
            for (final Entry<String, IInternalCommunicationCapability.ICommunicationChannel> entry : this.internalCommunicationCapability
                    .getChannels()) {
                internalCommunicationCapability.addChannel(entry.getKey(),
                        entry.getValue());
            }
        }
        this.internalCommunicationCapability = internalCommunicationCapability;
        LOG.debug("Before adding internalCommunicationCapability={}", internalCommunicationCapability);

        addCapability(internalCommunicationCapability);
        if (debug) LOG.debug("Exiting setInternalCommunicationCapability(): cap={}", internalCommunicationCapability);
    }

    @Override
    public void setPossessesScore(final UniqueIdentifier capabilityIdentifier, final double score) {
        this.getPersonaControlComponent().getOrganizationModel().getAgent(this.getUniqueIdentifier()).setPossessesScore(capabilityIdentifier, score);

    }

    /**
     * Returns the {@code DisplayInformation} representation of the {@code IAttributable} or {@code ICapability}.
     *
     * @return the {@code DisplayInformation} for visual display base.
     */
    @Override
    public IDisplayInformation toDisplayInformation() {
        final IDisplayInformation displayInformation = super.toDisplayInformation();

        for (final ICapability capability : getCapabilities()) {
            capability.populateCapabilitiesOfDisplayObject(displayInformation);
        }

        return displayInformation;
    }

    /**
     * Returns the {@code DOM} {@code Element} of the {@code AbstractAgent}.
     *
     * @param document the document in which to create the {@code DOM} {@code Element}s.
     * @return the {@code DOM} {@code Element} of the {@code AbstractAgent}.
     */
    @Override
    public Element toElement(final Document document) {
        final Element agent = document.createElement(
                ELEMENT_AGENTS);
        agent.setAttribute(ATTRIBUTE_TYPE, getClass().getSimpleName());
        agent.setAttribute(ATTRIBUTE_PACKAGE, getClass().getPackage()
                .getName());

        final Node name = agent.appendChild(document
                .createElement(ELEMENT_AGENT));
        name.appendChild(document.createTextNode(getUniqueIdentifier()
                .toString()));

        for (final ICapability capability : getCapabilities()) {
            agent.appendChild(capability.toElement(document));
        }

        return agent;
    }

    public abstract void doAssignmentTaskCompleted(IPersona agent, ITask assignedTask);

    public abstract void doTaskFailed(ITask task);

    protected abstract void executeTask(ITask task);

    @Override
    public edu.ksu.cis.macr.obaa_pp.ec.IControlComponent getControlComponent() {
        return null;
    }

    @Override
    public OrganizationEvents getOrganizationEvents() {
        return organizationEvents;
    }

    @Override
    public void setOrganizationEvents(IEventManager organizationEvents) {
        this.organizationEvents = (OrganizationEvents) organizationEvents;
    }
}
