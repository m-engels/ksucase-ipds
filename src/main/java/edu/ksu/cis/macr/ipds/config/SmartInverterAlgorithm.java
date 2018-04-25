package edu.ksu.cis.macr.ipds.config;

/**
 * An enumeration of the smart inverter control algorithms. <p> We are currently using FIXED_PF_FIXED_Q_INJECTION.
 */
public enum SmartInverterAlgorithm {

    /**
     * There is no injection available.
     */
    NO_INJECTION(0),

    /**
     * The reactive power is based on a fixed power factor bounded by a maximum.
     */
    FIXED_PF_BOUNDED_BY_MAXIMUM(1);

    private final int value;

    private SmartInverterAlgorithm(int value) {
        this.value = value;
    }

    /**
     * Get the integer value of the type.  No injection = 0 and a fixed power factor bounded by a maximum = 2.
     *
     * @return - the integer value of the algorithm type
     */
    public int getIntegerValue() {
        return this.value;
    }

}
