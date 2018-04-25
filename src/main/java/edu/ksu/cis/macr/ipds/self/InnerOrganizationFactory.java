package edu.ksu.cis.macr.ipds.self;


import edu.ksu.cis.macr.aasis.agent.admin.OrganizationFactory;
import edu.ksu.cis.macr.aasis.agent.persona.IPersonaPopulatable;
import edu.ksu.cis.macr.aasis.self.IInnerOrganization;
import edu.ksu.cis.macr.aasis.spec.OrganizationFocus;
import edu.ksu.cis.macr.aasis.types.IAgentType;
import edu.ksu.cis.macr.ipds.config.AgentType;
import edu.ksu.cis.macr.ipds.config.RunManager;
import edu.ksu.cis.macr.ipds.self.persona.SelfPersonaFactory;
import edu.ksu.cis.macr.obaa_pp.objects.IAttributable;
import edu.ksu.cis.macr.obaa_pp.objects.ITangibleObject;
import edu.ksu.cis.macr.obaa_pp.objects.ObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

//import edu.ksu.cis.macr.ipds.organizations.Organization;

/**
 Utility class for building organizations of sub-agents (persona). {@code AgentOrganizations} can be built from
 individual Agent.xml files describing the persona for a single agent or they can be built from standard rules based on
 their type.
 */
public class InnerOrganizationFactory extends OrganizationFactory {
    private static final Logger LOG = LoggerFactory.getLogger(InnerOrganizationFactory.class);
    private static final Boolean debug = false;
  /**
   {@code ELEMENT_ENVIRONMENT} is the root element.
   */
  public static final String ELEMENT_ENVIRONMENT = "environment";

  /**
   {@code ELEMENT_AGENTS} is the description of agent(s).
   */
  public static final String ELEMENT_AGENTS = "agents";

  /**
   {@code ELEMENT_AGENT}
   */
  public static final String ELEMENT_AGENT = "agent";

  /**
   {@code ATTRIBUTE_STYLE}
   */
  public static final String ATTRIBUTE_STYLE = "style";

  private String orgName;
  private String agentFilename;
  private IInnerOrganization organization;
  private File agentFile;
  private OrganizationFocus focus;

  protected InnerOrganizationFactory() {
  }

  public InnerOrganizationFactory(String orgName, OrganizationFocus focus) {
    this.focus = focus;
    this.orgName = orgName;
    this.agentFilename = "";
    this.organization = null;
  }

  public static IInnerOrganization create(String absolutePathFolder, String agentName) {
      IAgentType agentType = getAgentTypeFromFolder(agentName);
      String goalfile = "";
      String rolefile = "";


      // check for custom goal model and/or role model in the folder
      File folder = new File(absolutePathFolder);
      File[] files = folder.listFiles();

      for (File file : files) {
          String filename = file.getAbsolutePath();
          if (filename.contains("GoalModel.goal")) {
              goalfile = filename;
              if (debug) LOG.debug("Specification - Goal model is {}",goalfile);
          } else if (filename.contains("RoleModel.role")) {
              rolefile = filename;
              if (debug) LOG.debug("Specification - Role model is {}",rolefile);
          }
      }
      if (goalfile.equals("")) {
          goalfile = RunManager.getAbsolutePathToStandardAgentGoalModel(agentType);
          if (debug) LOG.debug("Specification - Goal model is {}", goalfile);
      }
      if (rolefile.equals("")) {
          rolefile = RunManager.getAbsolutePathToStandardAgentRoleModel(agentType);
          if (debug) LOG.debug("Specification - Role model is {}", rolefile);
      }
      LOG.info("Creating {} {} from {}.", agentName, agentType, absolutePathFolder);
      IInnerOrganization org = new InnerOrganization(absolutePathFolder, OrganizationFocus.Agent,
              agentType, goalfile, rolefile, RunManager.getTopGoal());
      LOG.info("EVENT: AGENT_CREATED. agentName={} agentType={} absolutePathFolder={}.", agentName, agentType, absolutePathFolder);
      return org;
  }

    /**
     Custom algorithm to determine the type of agent. Before evaluation, the algorithm will remove any "self" or "org"
     strings and base the result on the first character.

     @param agentFolderName - the folder name with the specification files.
     @return the type of Agent.
     */
    public static IAgentType getAgentTypeFromFolder(final String agentFolderName) {

        String name = agentFolderName.replace("self", "").replace("org",
                "").toUpperCase().trim();
        String firstChar = name.substring(0, 1);
        String lastTwoChar = name.substring(name.length() - 2);
        AgentType t = AgentType.Home;

        switch (firstChar) {
            case "S":
                t = AgentType.Substation;
                break;
            case "F":
                t = AgentType.Feeder;
                break;
            case "L":
                t = AgentType.Lateral;
                break;
            case "N":
                t = AgentType.Neighborhood;
                break;
            case "H":
                if (lastTwoChar.equals("_F")) t = AgentType.Forecast;
                else t = AgentType.Home;
                break;
            default:
                LOG.error("The type of agent could not be " +
                        "determined from input ({}).", agentFolderName);
                System.exit(-43);
        }
        return t;
    }

  /**
   Save objects from the given {@code Organization} to the specified file.

   @param filename the {@code String} representing the file to save to.
   @param organization the {@code Organization} to save from.
   */
  public static void saveFile(final String filename,
                              final IInnerOrganization organization) {
   // saveFile(new File(filename), organization);
  }


  /**
   Set up the {@code Organization} with objects from the given list of objects.

   @param objectsList the list of objects.
   @param organization the {@code Organization} to set up the objects.
   */
  public static void setupObjects(final NodeList objectsList,
                                  IPersonaPopulatable organization) {
    for (int i = 0; i < objectsList.getLength(); i++) {
      if(debug) LOG.debug("Setting up {} environment objects.",
              objectsList.getLength());
      final Element objectsElement = (Element) objectsList.item(i);
      final String packageName = objectsElement
              .getAttribute(ATTRIBUTE_PACKAGE);
      final String className = objectsElement
              .getAttribute(ATTRIBUTE_TYPE);
      final String categoryName = objectsElement
              .getAttribute(ATTRIBUTE_CATEGORY);

      final ObjectFactory.AttributeConstructorValues[] attributes = setupAttributes(
              objectsElement
                      .getElementsByTagName(
                              ELEMENT_ATTRIBUTE));
      final NodeList simpleList = objectsElement
              .getElementsByTagName(ELEMENT_SIMPLE);

      switch (categoryName) {
        case "tangible":
          ObjectFactory.setupSimpleTangibleObjects(
                  getTangibleObjectClass(
                          packageName + "." + className), attributes,
                  simpleList
          );
          break;
        case "intangible":
          ObjectFactory.setupSimpleIntangibleObjects(
                  getIntangibleObjectClass(
                          packageName + "." + className), attributes,
                  simpleList
          );
          break;
        case "basic":
          ArrayList<IAttributable> list = ObjectFactory
                  .setupEnvironmentObjects(
                          getWorldstateObjectClass(
                                  packageName + "." + className),
                          attributes,
                          simpleList
                  );
          list.forEach(organization::addObject);
          break;
        default:
          LOG.error("Unknown Category: \'{}\'", categoryName);
          System.exit(-1);
          break;
      }
    }
  }

  /**
   Returns the {@code Class} of an {@code IAttributable}

   @param className the {@code String} representing * the {@code Class} of an {@code IIntangibleObject}.
   @return the {@code Class} of an {@code IAttributable}.
   */
  public static Class<? extends IAttributable> getWorldstateObjectClass(
          final String className) {
    try {
      return Class.forName(className).asSubclass(IAttributable.class);
    } catch (final ClassNotFoundException e) {
        LOG.error("ERROR: ClassNotFoundException for className={}.",
                className);
        System.exit(-99);
    }
    return null;
  }

  /**
   Save objects from the given {@code Organization} to the specified {@code File}.

   @param file the {@code File} to save to.
   @param organization the {@code Organization} to save from.
   */
  public static void saveFile(final File file,
                              final IPersonaPopulatable organization) {
    try {
      final Document document = DocumentBuilderFactory.newInstance()
              .newDocumentBuilder().newDocument();
      final Node root = document.appendChild(document.createElement(ELEMENT_ENVIRONMENT));

      for (final ITangibleObject o : organization.getTangibleObjects()) {
        root.appendChild(o.toElement(document));
      }

      TransformerFactory
              .newInstance()
              .newTransformer()
              .transform(new DOMSource(document),
                      new StreamResult(new FileOutputStream(file)));
    } catch (ParserConfigurationException |
            TransformerFactoryConfigurationError | TransformerException |
            FileNotFoundException e) {
      LOG.error("ERROR: reading objects. {}", e.getMessage());
      System.exit(-51);
    }
  }

  /**
   Save objects from the given {@code Organization} to the specified file.

   @param objectFilename the {@code String} representing the file to save to.
   @param organization the {@code Organization} to save from.
   */
  public static void saveObjectFile(final String objectFilename,
                                    final IInnerOrganization organization) {
    saveObjectFile(new File(objectFilename), organization);
  }


  // object file...........................................

  /**
   Save objects from the given {@code Organization} to the specified {@code File}.

   @param objectFile the {@code File} to save to.
   @param organization the {@code Organization} to save from.
   */
  public static void saveObjectFile(final File objectFile,
                                    final IInnerOrganization organization) {
    // nothing
  }





  /**
   Loads up the given {@code Organization} with agents from the given file.

   @param agentFilename the {@code String} representing the file to load from.
   @param organization the {@code Organization} to load to.
   */
  public void loadAgentFile(final String agentFilename,  IInnerOrganization organization) {
      if(debug) LOG.debug("Loading sub agent persona from the {} file.", agentFilename);
    Objects.requireNonNull(organization, "Organization should not be null; need a place to load agents.");
    this.agentFilename = agentFilename;
    this.agentFile = new File(this.agentFilename);

    if (!this.agentFile.exists()) {
      LOG.error("{} does not exist", this.agentFile.toString());
      System.exit(-4);
    } else {
      try {
        final Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(agentFile);
        Objects.requireNonNull(document);
        SelfPersonaFactory.setupPersonaList(document.getElementsByTagName(ELEMENT_AGENTS), organization);
      } catch (ParserConfigurationException | SAXException |
              IOException e) {
        LOG.error("Error loading agent file ({}): {}", agentFile,
                e.getMessage());
        System.exit(-33);
      }
    }
  }

  /**
   Loads up the given {@code Organization} with objects from the given file.

   @param objectFilename the {@code String} representing the file to load from.
   @param organization the {@code Organization} to load into.
   */
  public static void loadObjectFile(final String objectFilename, IInnerOrganization organization) {
    InnerOrganizationFactory.loadObjectFile(new File(objectFilename), organization);
  }

  /**
   /** Loads up the given {@code Organization} with objects from the given {@code File}.

   @param objectFile the {@code File} to load from.
   @param organization the {@code Organization} to load into.
   */
  public static void loadObjectFile(final File objectFile, IInnerOrganization organization) {
    if (objectFile == null) {
      return;
    } // it's optional
    if (objectFile.exists()) {
      try {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        DocumentBuilder b = f.newDocumentBuilder();
        NodeList n = b.parse(objectFile).getElementsByTagName(
                ELEMENT_OBJECTS);
          if(debug) LOG.debug(
                "loadObjectFile() object node list with {} items from" +
                        " file {}",
                n.getLength(), objectFile.getAbsolutePath());
        setupObjects(n, organization);
      } catch (ParserConfigurationException | SAXException | IOException e) {
        LOG.error("Error loading object file: {}",
                objectFile.getPath());
      }
    } else {
      LOG.info("{} does not exist.", objectFile);
    }
  }


}
