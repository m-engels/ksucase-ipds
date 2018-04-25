package edu.ksu.cis.macr.ipds.config;

import edu.ksu.cis.macr.aasis.messaging.IMessagingFocus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The primary focus of a targeted type of communications and messaging used in the simulation.
 */
public enum MessagingFocus implements IMessagingFocus {

    AGENT_INTERNAL(0),

    /**
     * Special exchange for secure authorization and authentication from central power distribution system control center (to configure an agent to participate in reactive power control).
     */
    GRID_CONTROL_CENTER(1),

    /**
     * Used for exchanging messages related to state estimation.
     */
    STATE_ESTIMATION(2),

    /**
     * Used for exchanging messages related to organizational administration in an estimation organization.
     */
    STATE_ESTIMATION_PARTICIPATE(3),

    /**
     * Used for communications related to sharing electrical power and power quality messages up the holorachy.
     */
    GRID(4),

    /**
     * Used for communications related to organizational administration in a distributed control system for the grid.
     */
    GRID_PARTICIPATE(5),

    /**
     * Used for exchanging messages related to buying and selling power in authorized auctions.
     */
    MARKET(6),

    /**
     * Used for exchanging messages related to organizational administration in power broker organizations.
     */
    MARKET_PARTICIPATE(7);
    private static final Logger LOG = LoggerFactory.getLogger(MessagingFocus.class);
    private final int value;
    MessagingFocus(int value) {
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


