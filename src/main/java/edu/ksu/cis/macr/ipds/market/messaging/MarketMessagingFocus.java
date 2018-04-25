package edu.ksu.cis.macr.ipds.market.messaging;

import edu.ksu.cis.macr.aasis.messaging.IMessagingFocus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The primary focus of a targeted type of communications and messaging used in the simulation.
 */
public enum MarketMessagingFocus implements IMessagingFocus {

    AGENT_INTERNAL(0),

    /**
     * Used for exchanging messages related to buying and selling power in authorized auctions.
     */
    MARKET(6),

    /**
     * Used for exchanging messages related to organizational administration in power broker organizations.
     */
    MARKET_PARTICIPATE(7);
    private static final Logger LOG = LoggerFactory.getLogger(MarketMessagingFocus.class);
    private final int value;
    MarketMessagingFocus(int value) {
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