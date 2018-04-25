package edu.ksu.cis.macr.ipds.config;

import edu.ksu.cis.macr.aasis.types.IAgentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@code AgentType} enumeration lists the possible agent types.
 */
public enum AgentType implements IAgentType {
    Outside(0),
    Substation(1),
    Feeder(2),
    Lateral(3),
    Neighborhood(4),
    Home(5),
    Forecast(6);

    private static final Logger LOG = LoggerFactory.getLogger(AgentType.class);
    private final int value;

    private AgentType(int value) {
        this.value = value;
    }

    /**
     * Custom algorithm to determine the type of agent. Before evaluation, the algorithm will remove any "self" or "org"
     * strings and base the result on the first character.
     *
     * @param agentFolderName - the folder name with the specification files.
     * @return - the type of Agent.
     */
    public static AgentType getAgentTypeFromFolder(final String agentFolderName) {

        String name = agentFolderName.replace("self", "").replace("org",
                "").toUpperCase().trim();
        String firstChar = name.substring(0, 1);
        String lastTwoChar = name.substring(name.length() - 2);
        AgentType t = AgentType.Home;

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
                if (lastTwoChar.equals("_F")) t = Forecast;
                else t = Home;
                break;
            default:
                LOG.error("The type of agent could not be " +
                        "determined from input ({}).", agentFolderName);
                System.exit(-43);
        }
        return t;
    }

    /**
     * Get the integer value of the type.
     *
     * @return - the integer value (1 is the top level of the hierarchy)
     */
    public int getIntegerValue() {
        return this.value;
    }

}
