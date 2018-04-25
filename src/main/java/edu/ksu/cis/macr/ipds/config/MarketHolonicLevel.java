package edu.ksu.cis.macr.ipds.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This enumeration lists the possible levels for an intelligent power distribution control system. A
 * substation is the highest level and a home is currently the lowest possible level for an organization or agent.
 */
public enum MarketHolonicLevel {
    Outside(0),
    Lateral(3),
    Neighborhood(4),
    Home(5),
    Custom(99);

    private static final Logger LOG = LoggerFactory.getLogger(MarketHolonicLevel.class);
    private final int value;

    private MarketHolonicLevel(int value) {
        this.value = value;
    }

    /**
     * Custom algorithm to determine the type of organization. Ours our named by the upper level in the local organization.
     * An organization that typically consists of on neighborhood transformer and four homes would be considered to be a
     * neighborhood-level organization.  Before evaluation, the algorithm will remove any "self" or "org" strings and
     * base the result on the first character.
     *
     * @param organizationName - the string name of the device folder or default master or level of the organization.
     * @return - the organizational type.
     */
    public static MarketHolonicLevel getOrganizationType(final String organizationName) {

        String name = organizationName.replace("self", "").replace("org", "").toUpperCase().trim();
        String firstChar = name.substring(0, 1);
        MarketHolonicLevel t;

        switch (firstChar) {
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
        }
        return t;
    }

    /**
     * Get the integer value of the type.  Substation = 1 down to Home = 5.
     *
     * @return - the integer value (1 is the top level of the hierarchy)
     */
    public int getIntegerValue() {
        return this.value;
    }

}
