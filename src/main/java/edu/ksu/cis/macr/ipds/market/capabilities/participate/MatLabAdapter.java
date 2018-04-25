package edu.ksu.cis.macr.ipds.market.capabilities.participate;

import edu.ksu.cis.macr.ipds.config.RunManager;
import edu.ksu.cis.macr.ipds.market.guidelines.broker.AuctionAlgorithm;
import edu.ksu.cis.macr.ipds.simulators.smartmeter.interfaces.SmartMeterRowTranslator;
import edu.ksu.cis.macr.ipds.simulators.smartmeter.process.MatLabConnection;
import matlabcontrol.MatlabConnectionException;
import matlabcontrol.extensions.MatlabNumericArray;
import matlabcontrol.extensions.MatlabTypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * The {@code MatLabAdapter} is used to interact with MatLab calculational routines.
 */
public class MatLabAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(MatLabAdapter.class);
    private static final Boolean debug =  true;
    private static MatLabConnection matLabConnection = null;
    private static boolean useRemoteConnection = false;
    private static boolean isInitialized = false;
    private static boolean isConnected = false;
    private static double[][] input = new double[4][5];
    private static double[][] output = new double[4][2];
    private static MatlabTypeConverter matlabTypeConverter = null;

    /**
     * Construct a new adapter from a running MatLab instance and connects to the MatLab instance.
     */
    public MatLabAdapter() {
        if (debug) LOG.debug("MatLab Connection: {} Use Remote Conn: {}", matLabConnection, useRemoteConnection);
        if (MatLabAdapter.matLabConnection == null) {
            if (useRemoteConnection) {
                try {
                    remoteConnect("scenario01");
                    matlabTypeConverter = new MatlabTypeConverter(
                            matLabConnection.getMatLabProxy());
                } catch (Exception e) {
                    LOG.error("Error: unable to establish remote MatLab connection (useRemoteConnection == true).");
                    System.exit(-1);
                }
            } else {  // use local connection (this is more common)
                try {
                    MatLabAdapter.matLabConnection = new MatLabConnection();
                    matlabTypeConverter = new MatlabTypeConverter(matLabConnection.getMatLabProxy());
                    if (debug) LOG.debug("\tSUCCESS: MatLab Connection is now {}", matLabConnection);
                    SmartMeterRowTranslator.load();
                } catch (MatlabConnectionException e) {
                    LOG.error("ERROR: unable to establish MatLab connection");
                    System.exit(-5);
                }
            }
        }
        setIsConnected(true);
//    for (int i = 0; i < 4; i++) {
//      PV_candidate[0][i] = Scenario.getPvEnabled()[i];
//    }
    }

    public synchronized static void setIsConnected(final boolean isConnected) {
        MatLabAdapter.isConnected = isConnected;
    }

    /**
     * Get a two-dimensional array of doubles with the new inputs.
     *
     * @return - two-dimensional array of doubles
     */
    public synchronized static double[][] getInputs() {
        return input;
    }

    public synchronized static void setInputs(final double[][] inputs) {
        MatLabAdapter.input = inputs.clone();

    }

    public synchronized static void setOutputs(final double[][] outputs) {
        MatLabAdapter.output = outputs.clone();

    }

    /**
     * Indicates whether connected to a remote instance of MatLab.
     *
     * @return the useRemoteConnection - true if remote, false if running on localhost.
     */
    public synchronized static boolean isUseRemoteConnection() {
        return useRemoteConnection;
    }

    /*
    Execute the auction algorithm on the given bids.
     */
    public synchronized double[][] calculateAuction(int tierNumber, long timeSlice, AuctionAlgorithm algorithm, int i, double[][] arr) {
        LOG.info("Calling calculate {} tier-{} auction for purchase time slice={}. {}", algorithm, tierNumber, timeSlice);
        input = arr.clone();
        if (timeSlice < 0) {
            throw new IllegalArgumentException("ERROR: Time slice must be 0 or more.");
        }
        try {
            if (i == 0) {
                // clear  for fresh start
                String p = "From Java, sending 'clc'. ";
                matLabConnection.getMatLabProxy().feval("disp", p);
                if (debug) LOG.debug(p);
                matLabConnection.getMatLabProxy().feval("clc");

                p = "From Java, sending 'close all'. ";
                matLabConnection.getMatLabProxy().feval("disp", p);
                if (debug) LOG.debug(p);
                matLabConnection.getMatLabProxy().eval("close all");

                p = "From Java, sending 'clear all'. ";
                matLabConnection.getMatLabProxy().feval("disp", p);
                if (debug) LOG.debug(p);
                matLabConnection.getMatLabProxy().eval("clear all");
            }

            // add our code folder to search path
            String codeFolder = RunManager.getAbsolutePathToMatLabCodeFolder();
            matLabConnection.getMatLabProxy().feval("addpath", codeFolder);
            matLabConnection.getMatLabProxy().feval("disp", "From Java, adding our code path " + codeFolder + ".");
            if (debug) LOG.debug("MatLab:  adding MatLab search path {}", codeFolder);

            if (tierNumber == 1) {
                LOG.info("Calling Tier-{} auction algorithm.", tierNumber);
                // set input array
                matlabTypeConverter.setNumericArray("input", new MatlabNumericArray(input, null));
                String p = "From Java, setting tier 1 auction input array equal to: ";
                matLabConnection.getMatLabProxy().feval("disp", p);
                matLabConnection.getMatLabProxy().feval("disp", (Object) input);
                if (debug) LOG.debug("MatLab at t {}: setting input array to {}", timeSlice, input);

                // call function
                matLabConnection.getMatLabProxy().feval("disp", "From Java, calling ttda1(input)");
                String cmd = "[output] = ttda1(input)";
                if (debug) LOG.debug("Sending matlab command:  {}", cmd);
                matLabConnection.getMatLabProxy().eval(cmd);

                // remove our code path
                matLabConnection.getMatLabProxy().feval("rmpath", codeFolder);
                matLabConnection.getMatLabProxy().feval("disp", "From Java, removing our code path " + codeFolder + ".");
                if (debug) LOG.debug("MatLab:  removing tier 1 MatLab search path {}", codeFolder);

                // retrieve the results
                output = matlabTypeConverter.getNumericArray("output").getRealArray2D();
                LOG.info("MatLab at t {}:  returned output {}", timeSlice, output);
            } else if (tierNumber == 2) {
                LOG.info("Calling Tier-{} auction algorithm.", tierNumber);

                // set input array
                matlabTypeConverter.setNumericArray("input", new MatlabNumericArray(input, null));
                String p = "From Java, setting tier 2 input array equal to: ";
                matLabConnection.getMatLabProxy().feval("disp", p);
                matLabConnection.getMatLabProxy().feval("disp", (Object) input);
                if (debug) LOG.debug("MatLab at t {}: setting tier 2 input array to {}", timeSlice, input);

                matLabConnection.getMatLabProxy().feval("disp", "From Java, calling ttda2(input)");
                String cmd = "[output] = ttda2(input)";
                if (debug) LOG.debug("Sending matlab command:  {}", cmd);
                matLabConnection.getMatLabProxy().eval(cmd);

                // remove our code path
                matLabConnection.getMatLabProxy().feval("rmpath", codeFolder);
                matLabConnection.getMatLabProxy().feval("disp", "From Java, removing our code path " + codeFolder + ".");
                if (debug) LOG.debug("MatLab:  removing tier 2 MatLab search path {}", codeFolder);

                // retrieve the results
                output = matlabTypeConverter.getNumericArray("output").getRealArray2D();
                LOG.info("MatLab at t {}:  returned output {}", timeSlice, output);
            }
        } catch (Exception ex) {
            LOG.error("ERROR: {}", ex.getMessage());
            System.exit(-99);
        }
        //  if (debug) LOG.debug("MatLab: writing Net.Sub array data to {}. WriteFiles is set to {}.",  outFile2, writeFiles);

        // SubNode = matlabTypeConverter.getNumericArray( "Net.SubNode").getRealArray2D();
        // if (debug) LOG.debug("MatLab at t {}:  returned SubNode {}", timeSlice, SubNode);
        //   matLabConnection.getMatLabProxy().eval( "xlswrite(outFile3, SubNode)");
        //  if (debug) LOG.debug("MatLab: writing net Sub SubNode data to {}", outFile3);

        // copy original returned results to an editable array
        // it will be updated with the actuator values and used as input for the next timeslice
        MatLabAdapter.setOutputs(output);
        if (debug) LOG.debug("MatLab at t {}:  setting working new output array to {}", timeSlice, input);
        return output;
    }

    /*
    Execute the opf algorithm on the given set of inputs.
     */
    public synchronized double[][] calculateOPF(int holonicLevel, int iteration, double[][] arr, long timeSlice) {
        LOG.info("In adapter calculate OPF level {}, iteration {}, for time slice={}. {}", holonicLevel, iteration, timeSlice, input);
        input = arr.clone();

        try {
            if (iteration == 0) {
                // clear  for fresh start
                String p = "From Java, sending 'clc'. ";
                matLabConnection.getMatLabProxy().feval("disp", p);
                if (debug) LOG.debug(p);
                matLabConnection.getMatLabProxy().feval("clc");

                p = "From Java, sending 'close all'. ";
                matLabConnection.getMatLabProxy().feval("disp", p);
                if (debug) LOG.debug(p);
                matLabConnection.getMatLabProxy().eval("close all");

                p = "From Java, sending 'clear all'. ";
                matLabConnection.getMatLabProxy().feval("disp", p);
                if (debug) LOG.debug(p);
                matLabConnection.getMatLabProxy().eval("clear all");
            }

            // add our code folder to search path
            String codeFolder = RunManager.getAbsolutePathToMatLabCodeFolder();
            matLabConnection.getMatLabProxy().feval("addpath", codeFolder);
            matLabConnection.getMatLabProxy().feval("disp", "From Java, adding our code path " + codeFolder + ".");
            if (debug) LOG.debug("MatLab:  adding MatLab search path {}", codeFolder);

            if (holonicLevel == 1) {

                //TODO: Implement this for the substation first, then for the remaining holonic levels (see code in the matlab code folder for the opf calcs)
                // this is just starter code - should complile and points generally in the right direction...
                // the opf code will be in a different path (as it's all grouped in its own subfolder
                // please set a constant variable to the subfolder containing the opf code  and update the paths as needed below.

                LOG.info("Calling substation OPF (level={}).", holonicLevel);
                // set input array
                matlabTypeConverter.setNumericArray("input", new MatlabNumericArray(input, null));
                String p = "From Java, setting opf holonic level 1 input array equal to: ";
                matLabConnection.getMatLabProxy().feval("disp", p);
                matLabConnection.getMatLabProxy().feval("disp", (Object) input);
                if (debug) LOG.debug("MatLab at t {}: setting input array to {}", timeSlice, input);

                // call function
                matLabConnection.getMatLabProxy().feval("disp", "From Java, calling OPF_Sub(input)");
                String cmd = "[output] = OPF_Sub(input)";
                if (debug) LOG.debug("Sending matlab command:  {}", cmd);
                matLabConnection.getMatLabProxy().eval(cmd);

                // remove our code path
                matLabConnection.getMatLabProxy().feval("rmpath", codeFolder);
                matLabConnection.getMatLabProxy().feval("disp", "From Java, removing our code path.");
                if (debug) LOG.debug("MatLab:  removing MatLab search path {}", codeFolder);

                // retrieve the results
                output = matlabTypeConverter.getNumericArray("output").getRealArray2D();
                LOG.info("MatLab at t {}:  returned output {}", timeSlice, output);
            } else if (holonicLevel == 2) {
                LOG.info("Calling Feeder OPF (level={}).", holonicLevel);

                // set input array
                matlabTypeConverter.setNumericArray("input", new MatlabNumericArray(input, null));
                String p = "From Java, setting input array equal to: ";
                matLabConnection.getMatLabProxy().feval("disp", p);
                matLabConnection.getMatLabProxy().feval("disp", (Object) input);
                if (debug) LOG.debug("MatLab at t {}: setting input array to {}", timeSlice, input);

                matLabConnection.getMatLabProxy().feval("disp", "From Java, calling OPF_F(input)");
                String cmd = "[output] = OPF_F(input)";
                if (debug) LOG.debug("Sending matlab command:  {}", cmd);
                matLabConnection.getMatLabProxy().eval(cmd);

                // remove our code path
                matLabConnection.getMatLabProxy().feval("rmpath", codeFolder);
                matLabConnection.getMatLabProxy().feval("disp", "From Java, removing our code path.");
                if (debug) LOG.debug("MatLab:  removing MatLab search path {}", codeFolder);

                // retrieve the results
                output = matlabTypeConverter.getNumericArray("output").getRealArray2D();
                LOG.info("MatLab at t {}:  returned output {}", timeSlice, output);
            }
        } catch (Exception ex) {
            LOG.error("ERROR: {}", ex.getMessage());
            System.exit(-98);
        }
        //  if (debug) LOG.debug("MatLab: writing Net.Sub array data to {}. WriteFiles is set to {}.",  outFile2, writeFiles);

        // SubNode = matlabTypeConverter.getNumericArray( "Net.SubNode").getRealArray2D();
        // if (debug) LOG.debug("MatLab at t {}:  returned SubNode {}", timeSlice, SubNode);
        //   matLabConnection.getMatLabProxy().eval( "xlswrite(outFile3, SubNode)");
        //  if (debug) LOG.debug("MatLab: writing net Sub SubNode data to {}", outFile3);

        // copy original returned results to an editable array
        // it will be updated with the actuator values and used as input for the next timeslice
        MatLabAdapter.setOutputs(output);
        if (debug) LOG.debug("MatLab at t {}:  setting working new output array to {}", timeSlice, input);
        return output;
    }

  /*
  * Gets sensor names.
  * @see edu.ksu.cis.macr.ipds.hostdata.IEgaugeAdaptible#initialize(java
  * .lang.String)
   */

    public synchronized void initialize(String defaultScenario, String offlineMatLabDataSource) {
        if (debug) LOG.debug(
                "Beginning initialize call to local running smart meter with " +
                        "{} and {}.",
                defaultScenario, offlineMatLabDataSource);

        if (isUseRemoteConnection()) {
            if (debug) LOG.debug("Using remote connection: {}", defaultScenario);
            remoteConnect(defaultScenario);
        } else {
            if (debug) LOG.debug("Using local running connection connection. ");

            final String scenarioPath = RunManager.getAbsolutePathToTestCaseFolder() + defaultScenario + "/";
            final File dir = new File(scenarioPath);

        }
        isInitialized = true;
    }

    /**
     * Indicates whether currently connected to a running MatLab instance.
     *
     * @return - true if connected, false if not.
     */
    public synchronized boolean isConnected() {
        return isConnected;
    }

    /**
     * Indicates whether currently connected to a running MatLab instance.
     *
     * @return - true if connected, false if not.
     */
    public synchronized boolean isInitialized() {
        return isInitialized;
    }


    /*
     * Start a remote connection with the Server on the MatLab machine.
     */
    public synchronized void remoteConnect(String defaultScenario) {
        LOG.error("Error: unable to establish remote MatLab connection");
        System.exit(-1);
    }

    @Override
    public String toString() {
        return "MatLabSmartMeterSimulator (running MatLab instance)";
    }


}
