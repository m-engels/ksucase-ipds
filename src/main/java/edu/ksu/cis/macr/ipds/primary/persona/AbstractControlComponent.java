/*
 * Created on Nov 17, 2004
 *
 * See License.txt file the license agreement.
 */
package edu.ksu.cis.macr.ipds.primary.persona;

import edu.ksu.cis.macr.aasis.agent.cc_a.IAbstractControlComponent;
import edu.ksu.cis.macr.aasis.agent.persona.IOrganization;
import edu.ksu.cis.macr.aasis.agent.persona.IPersona;
import edu.ksu.cis.macr.aasis.spec.OrganizationFocus;
import edu.ksu.cis.macr.obaa_pp.cc.gr.EmptyGoalModel;
import edu.ksu.cis.macr.obaa_pp.cc.gr.IGoalModel;
import edu.ksu.cis.macr.obaa_pp.cc.om.EmptyOrganizationModel;
import edu.ksu.cis.macr.obaa_pp.cc.om.IOrganizationModel;
import edu.ksu.cis.macr.obaa_pp.cc.om.IXMLFormattableKnowledge;
import edu.ksu.cis.macr.obaa_pp.cc.reorg.EmptyReorganizationAlgorithm;
import edu.ksu.cis.macr.obaa_pp.cc.reorg.IReorganizationAlgorithm;
import edu.ksu.cis.macr.obaa_pp.events.OrganizationEvents;
import edu.ksu.cis.macr.organization.model.Agent;
import edu.ksu.cis.macr.organization.model.AgentImpl;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;


/**
 The {@code AbstractControlComponent} class facilities the creation of the organization part of the {@code
edu.ksu.cis.macr.aasis.agent.persona.IPersona}.

 @author Christopher Zhong
 */
public abstract class AbstractControlComponent implements IAbstractControlComponent {
  /**
   The {@code Logger} for providing logging information.
   */
  private static final Logger LOG = LoggerFactory.getLogger(AbstractControlComponent.class);
  private static final Boolean debug = false;
  /**
   The {@code ICommunicationChannel} keyword for registering organization getNumberOfMessages.
   */
  private static final String COMMUNICATION_CHANNEL_ID = "Organization Messages";
    /**
   The {@code Agent} representation.
   */
  private final Agent<UniqueIdentifier> agentRepresentation;
  protected OrganizationEvents organizationEvents;
  /**
   Contains the {@code IGoalModel}.
   */
  protected IGoalModel goalModel = null;
  private UniqueIdentifier localMaster = null;
  private IPersona owner = null;
  /**
   Contains the {@code IOrganizationModel}.
   */
  protected IOrganizationModel organizationModel = null;
  /**
   Contains the {@code IReorganizationAlgorithm}.
   */
  protected IReorganizationAlgorithm reorganizationAlgorithm = null;


  /**
   Constructs a new instance of {@code AbstractControlComponent}.

   @param name the name of the agent.
   @param owner the type of the agent.
   @param organization the organization information for the agent.
   */
  protected AbstractControlComponent(final String name, final IPersona owner, final Element organization,
                                     final OrganizationFocus focus) {
    this.setOwner(Objects.requireNonNull(owner, "ACC - the owner cannot be null."));
      String name1 = name;

    LOG.info("Creating AbstractControlComponent {}", name);
    LOG.debug("The ccm.executionComponent.getOrganization().getGoalParameterValues() are {}",
            this.getOwner().getOrganization().getGoalParameterValues());

    agentRepresentation = new AgentImpl<>(owner.getUniqueIdentifier());
    agentRepresentation.setContactInformation(owner.getUniqueIdentifier());

    this.setLocalMaster(agentRepresentation.getIdentifier());
    this.organizationEvents = new OrganizationEvents(owner.getUniqueIdentifier().toString());

    if (organization == null) {
      goalModel = new EmptyGoalModel();
      organizationModel = new EmptyOrganizationModel();
      reorganizationAlgorithm = new EmptyReorganizationAlgorithm();
    } else {
      goalModel = setupGoalModel(organization);
      if (goalModel == null) {
        if (debug) LOG.debug("Setting goalmodel in AbstractControlComponent constructor is null. " +
                "organizationKB = {}", organization.getFirstChild().getNodeName());
      }
      // organizationModel = new OrganizationModel();
      // organizationModel.initialize(organization);
      try {
        organizationModel = setupOrganizationModel(organization);
      } catch (ClassNotFoundException e) {
        LOG.error("ERROR: class not found. {}", e.getMessage());
        System.exit(-55);
      }
      reorganizationAlgorithm = setupReorganizationAlogorithm(organization);
    }
  }

  /**
   Initialize the {@code IGoalModel}.

   @param organization the {@code Element} that contains the necessary information for initializing the {@code IGoalModel}.
   @return the initialized {@code IGoalModel}.
   */
  public static IGoalModel setupGoalModel(
          // final IAbstractControlComponent controlComponent,
          final Element organization) {
    final Element reasoningElement = (Element) organization
            .getElementsByTagName(IXMLFormattableKnowledge.ELEMENT_REASONING).item(0);
    if (reasoningElement == null) {
      return new EmptyGoalModel();
      // return new EmptyGoalModel(controlComponent);
    }
    return null;
  }

  /**
   Initialize the {@code IOrganizationModel}.  // * @param controlComponent the {@code IAbstractControlComponent} of
   // * the {@code IOrganizationModel}.

   @param organization the {@code Element} that contains the necessary information for initializing the {@code IOrganizationModel}.
   @return the initialized {@code IOrganizationModel}.
   */
  public static IOrganizationModel setupOrganizationModel(
          //  final IAbstractControlComponent controlComponent,
          final Element organization) throws ClassNotFoundException {
    final Element knowledgeElement = (Element) organization
            .getElementsByTagName(IXMLFormattableKnowledge.ELEMENT_KNOWLEDGE).item(0);
    if (knowledgeElement == null) {
      return new EmptyOrganizationModel();
      //  return new EmptyOrganizationModel(controlComponent);
    } else {
      final String className = knowledgeElement
              .getAttribute(IXMLFormattableKnowledge.ATTRIBUTE_PACKAGE)
              + "."
              + knowledgeElement.getAttribute(IXMLFormattableKnowledge.ATTRIBUTE_TYPE);
      final Class<? extends IOrganizationModel> organizationModelClass = getOrganizationModelClass(className);
      final Constructor<? extends IOrganizationModel> organizationModelConstructor = getOrganizationModelConstructor(organizationModelClass);
      return newOrganizationModel(
              // organizationModelConstructor, controlComponent   );
              organizationModelConstructor);
    }
  }

  /**
   Initialize the {@code ReorganizationAlgorithm}.  //   * @param controlComponent the {@code IAbstractControlComponent} of //    * the {@code IReorganizationAlgorithm}.

   @param organization the {@code Element} that contains the necessary information for initializing the {@code IReorganizationAlgorithm}.
   @return the initialized {@code IReorganizationAlgorithm}.
   */
  public static IReorganizationAlgorithm setupReorganizationAlogorithm(
  //   final IAbstractControlComponent controlComponent,
          final Element organization) {
    final Element algorithmElement = (Element) organization
            .getElementsByTagName(IXMLFormattableKnowledge.ELEMENT_ALGORITHM).item(0);
    if (algorithmElement == null) {
      return new EmptyReorganizationAlgorithm();
      // return new EmptyReorganizationAlgorithm(controlComponent);
    } else {
      final String className = algorithmElement
              .getAttribute(IXMLFormattableKnowledge.ATTRIBUTE_PACKAGE)
              + "."
              + algorithmElement.getAttribute(IXMLFormattableKnowledge.ATTRIBUTE_TYPE);
      return newReorganizationAlgorithm(getReorganizationAlgorithmConstructor(getReorganizationAlgorithmClass
              (className)));
      //  controlComponent);
    }
  }

  /**
   Returns the {@code Class} of an {@code IOrganizationModel}.

   @param className the {@code String} representing the {@code Class} of an {@code IOrganizationModel}.
   @return the {@code Class} of an {@code IOrganizationModel}.
   */
  public static Class<? extends IOrganizationModel> getOrganizationModelClass(
          final String className) throws ClassNotFoundException {
    try {
      return Class.forName(className).asSubclass(IOrganizationModel.class);
    } catch (final ClassNotFoundException e) {
      logError(e);
      throw e;
    }

  }

  /**
   Returns the {@code Constructor} of the {@code IOrganizationModel} {@code Class}.

   @param organizationModelClass the {@code Class} of the {@code IOrganizationModel} to get the {@code Constructor}.
   @return the {@code Constructor} of the {@code IOrganizationModel}.
   */
  public static Constructor<? extends IOrganizationModel> getOrganizationModelConstructor(
          final Class<? extends IOrganizationModel> organizationModelClass) {
    try {
      return organizationModelClass
              .getConstructor(IAbstractControlComponent.class);
    } catch (SecurityException | NoSuchMethodException e) {
      logError(e);
    }
    return null;
  }

  /**
   Returns the {@code Class} of an {@code IReorganizationAlgorithm} .

   @param className the {@code String} representing the {@code Class} of an {@code IReorganizationAlgorithm}.
   @return the {@code Class} of an {@code IReorganizationAlgorithm} .
   */
  public static Class<? extends IReorganizationAlgorithm> getReorganizationAlgorithmClass(
          final String className) {
    try {
      return Class.forName(className).asSubclass(IReorganizationAlgorithm.class);
    } catch (final ClassNotFoundException e) {
      logError(e);
    }
    return null;
  }

  /**
   Returns the {@code Constructor} of the {@code IReorganizationAlgorithm} {@code Class}.

   @param reorganizationAlgorithmClass the {@code Class} of the {@code IReorganizationAlgorithm} to get the {@code Constructor}.
   @return the {@code Constructor} of the {@code IReorganizationAlgorithm}.
   */
  public static Constructor<? extends IReorganizationAlgorithm> getReorganizationAlgorithmConstructor(
          final Class<? extends IReorganizationAlgorithm> reorganizationAlgorithmClass) {
    try {
      return reorganizationAlgorithmClass.getConstructor(IAbstractControlComponent.class);
    } catch (SecurityException | NoSuchMethodException e) {
      logError(e);
    }
    return null;
  }

  /**
   Instantiates a new instance of {@code IOrganizationModel} with the given {@code Constructor}.

   @param constructor the {@code Constructor} for the new instance.
   @return a new instance of {@code IOrganizationModel}.
   */
  public static IOrganizationModel newOrganizationModel(
          final Constructor<? extends IOrganizationModel> constructor
          //,final IAbstractControlComponent controlComponent
  ) {
    //return instantiate(constructor, controlComponent);
    return instantiate(constructor);
  }

  /**
   Instantiates a new instance of {@code IReorganizationAlgorithm} with the given {@code Constructor} and {@code IAbstractControlComponent}.

   @param constructor the {@code Constructor} for the new instance.
   @return a new instance of {@code IReorganizationAlgorithm}.
   */
  public static IReorganizationAlgorithm newReorganizationAlgorithm(
          final Constructor<? extends IReorganizationAlgorithm> constructor
          //	,final IAbstractControlComponent controlComponent
  ) {
    //return instantiate(constructor, controlComponent);
    return instantiate(constructor);
  }

  /**
   Log the exception and print the stack trace.

   @param e - the Exception object
   */
  private static void logError(Exception e) {
    LOG.error("{} error type {}", e.getMessage(), e.getClass());
  }

  /**
   Instantiates a new instance of the object with the given {@code Constructor} and parameters.

   @param <T> the type of the new instance.
   @param constructor the {@code Constructor} for the new instance.
   @param parameters the parameters for the {@code Constructor}.
   @return a new instance of the object.
   */
  private static <T> T instantiate(final Constructor<T> constructor, final Object... parameters) {
    try {
      return constructor.newInstance(parameters);
    } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
      logError(e);
    }
    return null;
  }

  @Override
  public String getCommunicationChannelID() {
    return COMMUNICATION_CHANNEL_ID;
  }

  @Override
  public IPersona getPersonaExecutionComponent() {
    return getOwner();
  }

  @Override
  public IGoalModel getGoalModel() {
    return this.goalModel;
  }

  /**
   Replaces the current {@code IGoalModel} of this {@code edu.ksu.cis.macr.aasis.agent.persona.IControlComponent}
   with the given {@code IGoalModel}.

   @param goalModel the new {@code IGoalModel} to replace the existing one.
   */
  protected void setGoalModel(IGoalModel goalModel) {
    if (debug) LOG.debug("AbstractCC setting goal model. ");
    this.goalModel = Objects.requireNonNull(goalModel,
            "The goal model cannot be null.");
    if (this.goalModel == null) {
      System.exit(-91);
    }
  }

  @Override
  public UniqueIdentifier getIdentifier() {
    return agentRepresentation.getIdentifier();
  }

  /**
   Gets the {@code UniqueIdentifier} of the local organization master.
   */
  @Override
  public UniqueIdentifier getLocalMaster() {
    return this.localMaster;
  }

  /**
   Sets the local organization master.

   @param localMaster - the {@code UniqueIdentifier} sole master and supervisor for this holonic organization.
   */
  @Override
  public void setLocalMaster(UniqueIdentifier localMaster) {
    if (debug) LOG.debug("Setting local master of {} to {}", this.getName(), localMaster);
    this.localMaster = localMaster;
  }

  @Override
  public String getName() {
    return this.getIdentifier().toString();
  }

  /**
   Returns the {@code Organization} which contains information about the goals and roles.

   @return the  {@code Organization}.
   */
  IOrganization getOrganization() {
    return this.owner.getOrganization();
  }

  @Override
  public synchronized IOrganizationModel getOrganizationModel() {
    return this.organizationModel;
  }

  /**
   Replaces the current {@code IOrganizationModel} of this {@code edu.ksu.cis.macr.aasis.agent.persona.IControlComponent}
   with the given {@code IOrganizationModel}.

   @param iOrganizationModel the new {@code IOrganizationModel} to replaces the existing one.
   */
  protected void setOrganizationModel(final IOrganizationModel iOrganizationModel) {
    if (debug) LOG.debug("setOrganizationModel");
    this.organizationModel = iOrganizationModel;
  }

  @Override
  public synchronized IReorganizationAlgorithm getReorganizationAlgorithm() {
    return this.reorganizationAlgorithm;
  }

  /**
   This {@code IExecutionComponent} is a part of the {@code AbstractControlComponent}.
   */
  @Override
  public synchronized IPersona getOwner() {
    return owner;
  }

  @Override
  public synchronized void setOwner(IPersona owner) {
    this.owner = owner;
  }

  /**
   Returns true if this agent is the local organization master.
   */
  @Override
  public synchronized boolean isMaster() {

    return (this.getLocalMaster() == this.getOwner().getUniqueIdentifier());
  }

  /**
   Gets the {@code UniqueIdentifier} of the local organization master.
   */
  @Override
  public synchronized boolean isSlave() {

    return (!this.isMaster());
  }

  /**
   Replaces the current {@code IReorganizationAlgorithm} of this {@code edu.ksu.cis.macr.aasis.agent.persona.IControlComponent}
   with the given {@code IReorganizationAlgorithm}.

   @param reorganizationAlgorithm the new {@code IReorganizationAlgorithm} to replace the existing one.
   */
  @Override
  public void setReorganizationAlgorithm(final IReorganizationAlgorithm reorganizationAlgorithm) {
    this.reorganizationAlgorithm = reorganizationAlgorithm;
  }

}
