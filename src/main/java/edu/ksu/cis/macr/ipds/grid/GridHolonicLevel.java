package edu.ksu.cis.macr.ipds.grid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 This enumeration lists the possible levels for an intelligent power distribution control system. A
 substation is the highest level and a home is currently the lowest possible level for an organization or agent.
 */
public enum GridHolonicLevel {

  Outside(0),

  Substation(1),

  Feeder(2),

  Lateral(3),

  Neighborhood(4),

  Home(5),

  Custom(99);

  private static final Logger LOG = LoggerFactory.getLogger(GridHolonicLevel.class);
  private final int value;

  private GridHolonicLevel(int value) {
    this.value = value;
  }

  /**
   Custom algorithm to determine the type of organization. Ours our named by the upper level in the local organization.
   An organization that typically consists of on neighborhood transformer and four homes would be considered to be a
   neighborhood-level organization.  Before evaluation, the algorithm will remove any "self" or "org" strings and
   base the result on the first character.

   @param organizationName - the string name of the device folder or default master or level of the organization.
   @return the type of organization.
   */
  public static GridHolonicLevel getOrganizationType(final String
                                                             organizationName) {

    String name = organizationName.replace("self", "").replace("org",
            "").toUpperCase().trim();
    String firstChar = name.substring(0, 1);
    GridHolonicLevel t = GridHolonicLevel.Home;

    switch (firstChar) {
      case "S":
        t = Substation;
        break;
      case "F":
        t = Feeder;
        break;
      case "L":
        t = Lateral;
        break;
      case "N":
        t = Neighborhood;
        break;
      case "H":
        t = Home;
        break;
      default:
        LOG.error("The type of organization could not be determined " +
                        "from input ({}). Trying checking the initialize file org and specpath.",
                organizationName);
        t = Custom;
        // System.exit(-42);
    }
    return t;
  }

  /**
   Custom algorithm to determine the type of organization. Ours our named by the upper level in the local organization.
   An organization that typically consists of on neighborhood transformer and four homes would be considered to be a
   neighborhood-level organization.  Before evaluation, the algorithm will remove any "self" or "org" strings and
   base the result on the first character.

   @param organizationName - the string name of the device folder or default master or level of the organization.
   @return the type of organization
   */
  public static GridHolonicLevel getDefaultControllerOrganizationType(final
                                                                      String organizationName) {

    String name = organizationName.replace("self", "").replace("org",
            "").toUpperCase().trim();
    String firstChar = name.substring(0, 1);
    GridHolonicLevel t = GridHolonicLevel.Home;

    switch (firstChar) {
      case "S":
        t = Outside;
        break;
      case "F":
        t = Substation;  // not always - some feeders report to
        // higher-level feeders. update when master is picked.
        break;
      case "L":
        t = Feeder;
        break;
      case "N":
        t = Lateral;
        break;
      case "H":
        t = Neighborhood;
        break;
      default:
        LOG.error("The type of controller organization could not be " +
                "determined from input ({}).", organizationName);

        t = Custom;
        // System.exit(-43);
    }
    return t;
  }

  /**
   Get the integer value of the type.  Substation = 1 down to Home = 5.

   @return - the integer value (1 is the top level of the hierarchy)
   */
  public int getIntegerValue() {
    return this.value;
  }

}
