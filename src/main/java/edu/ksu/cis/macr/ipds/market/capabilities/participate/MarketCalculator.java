package edu.ksu.cis.macr.ipds.market.capabilities.participate;

import edu.ksu.cis.macr.aasis.simulator.player.Player;
import edu.ksu.cis.macr.ipds.market.guidelines.broker.AuctionAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides access to MatLab enabled calculations. There will be only one instance per JVM.
 */
public enum MarketCalculator {
    /**
     * Singleton instance  (one per JVM).
     */
    INSTANCE;
    private static final Logger LOG = LoggerFactory.getLogger(MarketCalculator.class);
    private static final boolean debug = false;
    private static MatLabAdapter adapter = null;
    private static boolean useLiveMatLab = false;
    private static String absPathToMatLabCodeFolder = "";
    private static String testCaseName = "";


    public static void initialize(boolean useLiveMatLab, String absPathToMatLabCodeFolder, String testCaseName) {
        MarketCalculator.useLiveMatLab = useLiveMatLab;
        MarketCalculator.absPathToMatLabCodeFolder = absPathToMatLabCodeFolder;
        MarketCalculator.testCaseName = testCaseName;
        LOG.info("INITIALIZING CALCULATOR ......................................");
        if (useLiveMatLab && adapter == null) {
            adapter = new MatLabAdapter();
            if (!adapter.isConnected()) {
                LOG.error("ERROR: Attempting to use live matlab. Cannot connect to running matlab adapter. Please try again.");
                System.exit(-1);
            }
        }
        LOG.info("Creating market calculator for {}.................", getTestCase());
        LOG.info("\t'useLiveMatLab' is set to {}.", useLiveMatLab);
        LOG.info("\t Matlab code path= {}", absPathToMatLabCodeFolder);
        LOG.info("Calculator initialization for {} complete.", testCaseName);
    }

    /**
     * @return the testCase
     */
    private static String getTestCase() {
        return MarketCalculator.testCaseName;
    }

    private static void step() {
        Player.step();
    }


    public static double[][] calculateAuction(int tierNumber, long timeSlice, AuctionAlgorithm algorithm, int i, double[][] input) {
        LOG.info("Calling calculate {} auction for purchase time slice={}. {}", algorithm, timeSlice);
        return adapter.calculateAuction(tierNumber, timeSlice, algorithm, i, input);
    }
}