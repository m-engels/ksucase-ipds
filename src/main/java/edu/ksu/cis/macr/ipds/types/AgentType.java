package edu.ksu.cis.macr.ipds.types;

import edu.ksu.cis.macr.aasis.types.IAgentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@code AgentType} enumeration lists the possible agent types.
 */
public enum AgentType implements IAgentType {
    Agent(0), ForecastAgent(1);


    private static final Logger LOG = LoggerFactory.getLogger(AgentType.class);
    private final int value;


    AgentType(int value) {
        this.value = value;
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
