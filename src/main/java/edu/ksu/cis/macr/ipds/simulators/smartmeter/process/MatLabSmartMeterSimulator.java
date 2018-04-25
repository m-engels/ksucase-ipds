/**
 * MatLabSmartMeterSimulator.java
 *
 * Copyright 2012 Kansas State University MACR Laboratory
 * http://macr.cis.ksu.edu/ Department of Computing & Information Sciences
 *
 * See License.txt file for the license agreement.
 *
 */
package edu.ksu.cis.macr.ipds.simulators.smartmeter.process;

import edu.ksu.cis.macr.aasis.simulator.clock.Clock;
import edu.ksu.cis.macr.ipds.config.RunManager;
import edu.ksu.cis.macr.ipds.primary.actuators.SmartInverterSetting;
import edu.ksu.cis.macr.ipds.primary.sensors.SensorType;
import edu.ksu.cis.macr.ipds.primary.sensors.smartmeter.ElectricalData;
import edu.ksu.cis.macr.ipds.primary.sensors.smartmeter.IElectricalData;
import edu.ksu.cis.macr.ipds.primary.sensors.smartmeter.ISmartMeterRead;
import edu.ksu.cis.macr.ipds.primary.sensors.smartmeter.SmartMeterRead;
import edu.ksu.cis.macr.ipds.simulators.smartmeter.interfaces.ElectricalDataColumnTranslator;
import edu.ksu.cis.macr.ipds.simulators.smartmeter.interfaces.ISmartMeterSimulator;
import edu.ksu.cis.macr.ipds.simulators.smartmeter.interfaces.SmartMeterRowTranslator;
import edu.ksu.cis.macr.obaa_pp.actuator.ISetting;
import edu.ksu.cis.macr.obaa_pp.sensor.IRead;
import edu.ksu.cis.macr.obaa_pp.sensor.Read;
import matlabcontrol.MatlabConnectionException;
import matlabcontrol.extensions.MatlabNumericArray;
import matlabcontrol.extensions.MatlabTypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 The {@code MatLabSmartMeterSimulator} implements the {@code ISmartMeterSimulator} interface and is used to interact with
 routines that can generate new smart meter sensor data in MatLab. with intial implementation by Matt Brown
 */
public class MatLabSmartMeterSimulator implements ISmartMeterSimulator {
  private static final Logger LOG = LoggerFactory.getLogger(MatLabSmartMeterSimulator.class);
  private static final boolean debug = false;
  private static MatLabConnection matLabConnection = null;
  private static boolean useRemoteConnection = false;
  private static double[][] allSensorsPrevious;  // holds array 62 nodes (rows) by 11 data values (columns)
  private static double[][] allSensorsNow;  // holds array 62 nodes (rows) by 11 data values (columns)
  private static boolean isInitialized = false;
  private static boolean isConnected = false;
  private static double[][] Sensor_Data_Denise = new double[62][11];
  private static double[][] Sensor_Data_NewInputs = new double[62][11];
  private static double[][] Sensor_Data_partial = new double[62][11];
  private static double[][] Sensor_Data_full = new double[62][11];
  private static double[][] ieee37_temp = new double[560][16];
  private static double[][] ieee37_previous = new double[560][16];
  private static double[][] Sub = new double[37][16];
  private static double[][] SubNode = new double[1][38];
  private static double[][] PV_candidate = new double[1][100];
  private static MatlabTypeConverter matlabTypeConverter = null;
  private static boolean writeFiles = false;

  /**
   Construct a new smart meter simulator from a running MatLab instance and connect to the MatLab instance.
   */
  public MatLabSmartMeterSimulator() {
    if (debug) LOG.debug("MatLab Connection: {} Use Remote Conn: {}", matLabConnection, useRemoteConnection);
    if (MatLabSmartMeterSimulator.matLabConnection == null) {
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
          MatLabSmartMeterSimulator.matLabConnection = new MatLabConnection();
          matlabTypeConverter = new MatlabTypeConverter(matLabConnection.getMatLabProxy());
          if (debug) LOG.debug("\tSUCCESS: MatLab Connection is now {}", matLabConnection);
          SmartMeterRowTranslator.load();
        } catch (MatlabConnectionException e) {
          LOG.error("ERROR: MatLabSmartMeterSimulator unable to establish MatLab connection");
          System.exit(-5);
        }
      }
    }
    setIsConnected(true);
    for (int i = 0; i < 4; i++) {
      PV_candidate[0][i] = RunManager.getPvEnabled()[i];
    }
  }

  public static void setIsConnected(final boolean isConnected) {
    MatLabSmartMeterSimulator.isConnected = isConnected;
  }

  /**
   Update the matrices based on control actions at the given time slice.

   @param timeSlice - the number of time slices elapsed since the simulation start.
   */
  private static void updateMatricesForNewTimeSlice(long timeSlice) {
    //MatLabSmartMeterSimulator.allSensorsPrevious = Sensor_Data_full;
    if (debug) LOG.debug("MatLab at t {}: Before executing, the full data set is {}", timeSlice, allSensorsPrevious);
    //MatLabSmartMeterSimulator.allSensorsNow = Sensor_Data_partial;
    if (debug) LOG.debug("MatLab at t {}: Before executing, the partial data set is {}", timeSlice, allSensorsNow);

    MatLabSmartMeterSimulator.Sensor_Data_Denise = Sensor_Data_NewInputs;
    if (debug)
      LOG.debug("MatLab at t {}: Before executing, the partial input set NOW {}", timeSlice, Sensor_Data_Denise);
    if (debug) LOG.debug("MatLab at t {}: New home Q gen is {}", timeSlice, Sensor_Data_Denise[43][7]);
  }

  /**
   Update the simulated values in the sensor data array for the next time slice from a running MatLab instance.
   Populates: MatLabSmartMeterSimulator.allSensorsPrevious with a full set of data for all sensors (including
   voltages). MatLabSmartMeterSimulator.allSensorsNow with a partial set of data (no voltages). In June of 2014, we
   updated the calcs.  Those are now running alongside the original projections, but offer a single timeslice, so for
   now, the simulation continues to retrieve and display the original values. These are used to determine the smart
   inverter reactive power settings on the PV enabled homes. The first time it will be called with a time slice of zero
   (no prior data). NOTE:  All MatLab arrays are 2 or more dimensions. See: http://matlabcontrol.googlecode.com/svn/javadocs/doc/index.html
   and https://code.google.com/p/matlabcontrol/wiki/Walkthrough

   @param timeSlice - the current time slice.
   @throws Exception - Handles any exceptions during this method.
   */
  public static void updateAllSensors(long timeSlice) throws Exception {
    if (timeSlice < 0) {
      throw new IllegalArgumentException("ERROR: Time slice must be 0 or more.");
    }
    if (debug) LOG.debug("MATLAB: Getting dataArray for time slice {}", timeSlice);
    double t = timeSlice + 1;  // java starts with timeSlice zero; smart meter at 1
    if (timeSlice == 0) {
      // clear smart meter for fresh start (same for 560 case - see OPF_Test_Denise.m)
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

      // set the list of pv enabled homes
      matlabTypeConverter.setNumericArray("PV_candidate", new MatlabNumericArray(PV_candidate, null));
      p = "From Java, setting PV_candidate equal to: ";
      matLabConnection.getMatLabProxy().feval("disp", p);
      matLabConnection.getMatLabProxy().feval("disp", (Object) PV_candidate);
      if (debug) LOG.debug("MatLab at t {}: setting PV_candidate to {}", timeSlice, PV_candidate);

      // start with an empty input array
      matlabTypeConverter.setNumericArray("Sensor_Data_Denise", new MatlabNumericArray(Sensor_Data_Denise, null));
      matLabConnection.getMatLabProxy().feval("disp", "From Java, initializing Sensor_Data_Denise. ");
      if (debug) LOG.debug("MatLab at t {}: setting Sensor_Data_Denise to {}", timeSlice, Sensor_Data_Denise);
    } else { // timeslice > 0
      ieee37_previous = ieee37_temp.clone();
      MatLabSmartMeterSimulator.updateMatricesForNewTimeSlice(timeSlice);
    }

    // MatLab uses 1-based arrays so our java 0 time slice is MatLab's 1
    matLabConnection.getMatLabProxy().setVariable("t", timeSlice + 1);
    matLabConnection.getMatLabProxy().feval("disp", "From Java, setting t equal to: ");
    matLabConnection.getMatLabProxy().feval("disp", t);
    if (debug) LOG.debug("MatLab: setting t to {}", timeSlice + 1);

    // add our smart meter code folder to the smartmeter search path
    String codeFolder = RunManager.getAbsolutePathToMatLabCodeFolder();
    matLabConnection.getMatLabProxy().feval("addpath", codeFolder);
    matLabConnection.getMatLabProxy().feval("disp", "From Java, adding our code path " + codeFolder + ".");
    if (debug) LOG.debug("MatLab:  adding MatLab search path {}", codeFolder);

    // add aggregation code folder, too
    String codeFolderAggregation = RunManager.getAbsolutePathToMatLabCodeFolder() + "/aggregation";
    matLabConnection.getMatLabProxy().feval("addpath", codeFolderAggregation);
    matLabConnection.getMatLabProxy().feval("disp", "From Java, adding our code path " + codeFolderAggregation + ".");
    if (debug) LOG.debug("MatLab:  adding MatLab search path {}", codeFolderAggregation);

    // set MatLab variable xlspath_sys to xls input file
    String xlspath_sys = codeFolderAggregation + "/System_Data_3phase_OPF1.xlsx";
    matLabConnection.getMatLabProxy().setVariable("xlspath_sys", xlspath_sys);
    matLabConnection.getMatLabProxy().feval("disp", "From Java, setting xlspath_sys equal to: ");
    matLabConnection.getMatLabProxy().feval("disp", "'" + xlspath_sys + "'");
    if (debug) LOG.debug("MatLab: setting xlspath_sys to {}", xlspath_sys);

    // set MatLab variable xlspath_sun to xls input file
    String xlspath_sun = codeFolderAggregation + "/Generation_Full_Sunny.xlsx";
    matLabConnection.getMatLabProxy().setVariable("xlspath_sun", xlspath_sun);
    matLabConnection.getMatLabProxy().feval("disp", "From Java, setting xlspath_sun equal to: ");
    matLabConnection.getMatLabProxy().feval("disp", "'" + xlspath_sun + "'");
    if (debug) LOG.debug("MatLab: setting xlspath_sun to {}", xlspath_sun);

    String outFile1 = "c:/ipds1.xls";
    matLabConnection.getMatLabProxy().setVariable("outFile1", outFile1);
    matLabConnection.getMatLabProxy().feval("disp", "From Java, setting outFile1 equal to: ");
    matLabConnection.getMatLabProxy().feval("disp", "'" + outFile1 + "'");
    LOG.info("MatLab: setting outFile1 to {} and writeFiles to {}. Use this to view the system data.", outFile1, writeFiles);

    String outFile2 = "c:/ipds2.xls";
    matLabConnection.getMatLabProxy().setVariable("outFile2", outFile2);
    matLabConnection.getMatLabProxy().feval("disp", "From Java, setting outFile2 equal to: ");
    matLabConnection.getMatLabProxy().feval("disp", "'" + outFile2 + "'");
    LOG.info("MatLab: setting outFile2 to {} and writeFiles to {}. Use this to view the net sensor data Sub array.", outFile2, writeFiles);

    String outFile3 = "c:/ipds3.xls";
    matLabConnection.getMatLabProxy().setVariable("outFile3", outFile3);
    matLabConnection.getMatLabProxy().feval("disp", "From Java, setting outFile3 equal to: ");
    matLabConnection.getMatLabProxy().feval("disp", "'" + outFile3 + "'");
    // LOG.info("MatLab: setting outFile3 to {}. Use this to view the net sensor data SubNode array.", outFile3);

    // call our original ipds function (must be on the path)
    matLabConnection.getMatLabProxy().feval("disp", "From Java, calling get_Voltage(Sensor_Data_Denise,t)");
    matLabConnection.getMatLabProxy().eval("[Sensor_Data_full,Sensor_Data_partial] = get_Voltage(Sensor_Data_Denise,t)");

    // call our power system data OPF function with the 2 path variables (must also be on the path)
    matLabConnection.getMatLabProxy().feval("disp", "From Java, calling ieee37_temp=PowerSystemDataOPF(xlspath_sys, xlspath_sun)");
    matLabConnection.getMatLabProxy().eval("[ieee37_temp] = PowerSystemDataOPF(xlspath_sys, xlspath_sun)");
    // [ieee37_temp] = PowerSystemDataOPF(U:/_KSU/PHD/workspace/ipds/src/main/resources/configs/matlab_code/aggregation/System_Data_3phase_OPF1.xlsx, U:/_KSU/PHD/workspace/ipds/src/main/resources/configs/matlab_code/aggregation/Generation_Full_Sunny.xlsx)

    if (writeFiles) {
      matLabConnection.getMatLabProxy().eval("xlswrite(outFile1, ieee37_temp)");
    }
    if (debug) LOG.debug("MatLab: writing system generation data to {}. WriteFiles is set to {}.", outFile1, writeFiles);

    // also get the Net array (Net = Sub: [37x16 double] SubNode: [1x38 struct]
    matLabConnection.getMatLabProxy().feval("disp", "From Java, calling Net=ReadSensorData(xlspath_sys, xlspath_sun)");
    matLabConnection.getMatLabProxy().eval("[Net] = ReadSensorData(xlspath_sys, xlspath_sun)");

    // remove our smart meter code folder to the smart meter search path
    matLabConnection.getMatLabProxy().feval("rmpath", codeFolder);
    matLabConnection.getMatLabProxy().feval("disp", "From Java, removing our code path.");
    LOG.debug("MatLab:  removing MatLab search path {}", codeFolder);

    // remove our smart meter code folder too
    matLabConnection.getMatLabProxy().feval("rmpath", codeFolderAggregation);
    matLabConnection.getMatLabProxy().feval("disp", "From Java, removing our aggregation code path.");
    LOG.debug("MatLab:  removing MatLab search path {}", codeFolderAggregation);

    // retrieve the two original result arrays
    Sensor_Data_full = matlabTypeConverter.getNumericArray("Sensor_Data_full").getRealArray2D();
    if (debug) LOG.debug("MatLab at t {}:  returned Sensor_Data_full {}", timeSlice, Sensor_Data_full);
    Sensor_Data_partial = matlabTypeConverter.getNumericArray("Sensor_Data_partial").getRealArray2D();
    if (debug) LOG.debug("MatLab at t {}:  returned partialArray {}", timeSlice, Sensor_Data_partial);

    // retrieve the ieee37_temp result, too
    ieee37_temp = matlabTypeConverter.getNumericArray("ieee37_temp").getRealArray2D();
    if (debug)
      LOG.debug("MatLab at t {}:  returned ieee37_temp {} rows by {} cols: {}", timeSlice, ieee37_temp.length, ieee37_temp[0].length, ieee37_temp);

    // retrieve the two Net result arrays (Sub for now, SubNode is a struct and unavailable)
    Sub = matlabTypeConverter.getNumericArray("Net.Sub").getRealArray2D();
    if (debug) LOG.debug("MatLab at t {}:  returned Sub {}", timeSlice, Sub);

    if (writeFiles) {
      matLabConnection.getMatLabProxy().eval("xlswrite(outFile2, Net.Sub)");

    }
    if (debug) LOG.debug("MatLab: writing Net.Sub array data to {}. WriteFiles is set to {}.",  outFile2, writeFiles);

    // SubNode = matlabTypeConverter.getNumericArray( "Net.SubNode").getRealArray2D();
    // if (debug) LOG.debug("MatLab at t {}:  returned SubNode {}", timeSlice, SubNode);
    //   matLabConnection.getMatLabProxy().eval( "xlswrite(outFile3, SubNode)");
    //  if (debug) LOG.debug("MatLab: writing net Sub SubNode data to {}", outFile3);

    // copy original returned results to an editable array
    // it will be updated with the actuator values and used as input for the next timeslice
    MatLabSmartMeterSimulator.setSensor_Data_NewInputs(Sensor_Data_partial);
    if (debug) LOG.debug("MatLab at t {}:  setting working new inputs array to {}", timeSlice, Sensor_Data_partial);

    if (timeSlice == 0) {
      if (debug) LOG.debug("MatLab at time zero.");
      try {
        for (double[] doubles : MatLabSmartMeterSimulator
                .Sensor_Data_Denise = Sensor_Data_NewInputs.clone()) {
        }

        for (double[] doubles : MatLabSmartMeterSimulator
                .allSensorsPrevious = Sensor_Data_full.clone()) {
        }


        for (double[] doubles : MatLabSmartMeterSimulator.allSensorsNow =
                Sensor_Data_partial.clone()) {
        }

      } catch (Exception e) {
        LOG.error("Error cloning arrays. {}",
                e.getMessage());
        System.exit(-99);
      }
    }
  }


  private static void setSensor_Data_Denise(final double[][] arr) {
    Sensor_Data_Denise = arr.clone();
  }

  /**
   Get a two-dimensional array of doubles with the new inputs.

   @return - two-dimensional array of doubles
   */
  public static double[][] getSensor_Data_NewInputs() {
    return Sensor_Data_NewInputs;
  }

  public static void setSensor_Data_NewInputs(final double[][] sensor_Data_NewInputs) {
    Sensor_Data_NewInputs = sensor_Data_NewInputs.clone();
    Sensor_Data_Denise = sensor_Data_NewInputs.clone();
  }

  /**
   Indicates whether connected to a remote instance of MatLab.

   @return the useRemoteConnection - true if remote, false if running on localhost.
   */
  public static boolean isUseRemoteConnection() {
    return useRemoteConnection;
  }

  /**
   Gets a list of sensor readings for all sensors at a given time slice.  Sensor names are mapped to the data source
   identifiers (e.g. row numbers) in the matlab_smartmeter_row.properties file in the main SelfOrganization folder.

   @return - the Read object with the set of all sense results (for this sensor type)
    * @param timeSlice - the desired time slice  (as the count of elapsed time slices  since the simulation began)
   */
  @Override
  public ArrayList<IRead<?>> getAllReadsAt(long timeSlice) {
    if (debug) LOG.debug("Getting all sensor reads from data adapter for java time slice {}.", timeSlice);

    // first use MatLab to simulate all the data at this time slice.
    try {
      MatLabSmartMeterSimulator.updateAllSensors(timeSlice);
      LOG.info("Simulated values calculated for time slice {}.", timeSlice);
    } catch (Exception e) {
      LOG.error("ERROR: Could not get sensor data from MatLab. ");
    }
    ArrayList<IRead<?>> list = new ArrayList<>();

    for (int i = 0; i < MatLabSmartMeterSimulator.ieee37_temp.length; i++) {
      ISmartMeterRead smartMeterRead = getReadByRow(i);
      if (debug) LOG.debug("Setting sensor values at time slice {} for sensor row {}. New voltages are {}, {}, and {}.",
              timeSlice, i,
              smartMeterRead.getElectricalDataFromNewFormat().getPhaseAvoltage(),
              smartMeterRead.getElectricalDataFromNewFormat().getPhaseBvoltage(),
              smartMeterRead.getElectricalDataFromNewFormat().getPhaseCvoltage());
      IRead<SensorType> read = new Read<>();
      read.setSensorReadObject(smartMeterRead);
      list.add(read);
    }
    return list;
  }

  /*
  * Gets the list of meter names from data template files in the simulation directory.
  */
  private List<String> getMeterNames(File dir) {
    List<String> meterName = null;
    if (dir.isDirectory()) {
      meterName = new ArrayList<>();
      String[] list = dir.list();
      for (String file : list) {
        if (file.contains(".csv")) {
          meterName.add(file.replace(".csv", ""));
        }
      }
    }
    return meterName;
  }

  /**
   Get a set of sensor readings for the given sensor name at the most recent simulation time slice as maintained by the
   {@code Clock}.  Sensor names are mapped to the data source identifiers (e.g. row numbers) in the
   matlab_smartmeter_row.properties file in the main SelfOrganization folder.

   @return {@code Read<SensorType>} - the Read object with the sense results (for this sensor type)
    * @param sensorName - the sensor name defined in matlab_smartmeter_row.properties, e.g. SM-44
   */
  @Override
  public IRead<SensorType> getRead(String sensorName) {
    return getReadAt(sensorName, Clock.getTimeSlicesElapsedSinceStart());
  }

  /**
   Get a set of sensor readings for the given sensor name at the given time slice. Sensor names are mapped to the data
   source identifiers (e.g. row numbers) in the matlab_smartmeter_row.properties file in the main SelfOrganization
   folder.

   @return {@code Read<SensorType>} - the Read object with the sense results (for this sensor type)
    * @param sensorName - the sensor name defined in matlab_smartmeter_row.properties, e.g. SM-44
   * @param timeSlice -  the desired time slice (as the count of elapsed time slices since the simulation began)
   */
  @Override
  public IRead<SensorType> getReadAt(String sensorName, long timeSlice) {
    if (debug) LOG.debug("getReadAt for {} at time slice {}", sensorName, timeSlice);
    IRead<SensorType> read = new Read<SensorType>();
    if (timeSlice >= 0 && !sensorName.isEmpty()) {
      if (debug) LOG.debug("GetDiscreteReading from MatLab for {} at time slice {}", sensorName, timeSlice);
      int rowID = SmartMeterRowTranslator.getRowID(sensorName);
      if (rowID == -1) {
        LOG.error("ERROR: Cannot find sensor {}. Please check configurations.", sensorName);
        System.exit(-1);
      }
      if (debug) LOG.debug("GetDiscreteReading {} is at rowID {}", sensorName, rowID);
      ISmartMeterRead smartMeterRead = getReadByRow(rowID);
      read.setSensorReadObject(smartMeterRead);
      if (debug) LOG.debug("Sensor data for {} at time slice {} is {}", sensorName, timeSlice, read.toString());
    }
    return read;
  }

  private ISmartMeterRead getReadByRow(int iRow) {
    if (debug) LOG.debug("Looking up the smart meter sensor data for row {}.", iRow);
    IElectricalData current = new ElectricalData();
    IElectricalData previous = new ElectricalData();
    IElectricalData currentFromTheNewFormat = new ElectricalData();
    IElectricalData previousFromTheNewFormat = new ElectricalData();

//    if (i < MatLabSmartMeterSimulator.allSensorsNow.length) {
//      loadElectricalData(MatLabSmartMeterSimulator.allSensorsNow[i], current);
//      loadElectricalData(MatLabSmartMeterSimulator.allSensorsPrevious[i], previous);
//    }
    if (debug) LOG.debug("ieee37_temp[0] =  {}.", MatLabSmartMeterSimulator.ieee37_temp[0]);
    if (debug) LOG.debug("ieee37_temp[1] =  {}.", MatLabSmartMeterSimulator.ieee37_temp[1]);
    if (debug) LOG.debug("ieee37_temp[558] =  {}.", MatLabSmartMeterSimulator.ieee37_temp[558]);
    loadElectricalDataExtended(MatLabSmartMeterSimulator.ieee37_temp[iRow], currentFromTheNewFormat);
    loadElectricalDataExtended(MatLabSmartMeterSimulator.ieee37_previous[iRow], previousFromTheNewFormat);
    ISmartMeterRead read = new SmartMeterRead();
    read.setElectricalData(currentFromTheNewFormat);
    read.setPreviousElectricalData(previousFromTheNewFormat);
    read.setElectricalDataFromNewFormat(currentFromTheNewFormat);
    read.setPreviousElectricalDataFromNewFormat(currentFromTheNewFormat);
    return read;
  }


  /*
  * Gets sensor names.
  * @see edu.ksu.cis.macr.ipds.hostdata.IEgaugeAdaptible#initialize(java
  * .lang.String)
   */
  @Override
  public synchronized void initialize(String defaultScenario,
                         String offlineMatLabDataSource) {
    if (debug) LOG.debug(
            "Beginning initialize call to local running smart meter with " +
                    "{} and {}.",
            defaultScenario, offlineMatLabDataSource);

    if (isUseRemoteConnection()) {
      if (debug) LOG.debug("Using smart meter remote connection: {}", defaultScenario);
      remoteConnect(defaultScenario);
    } else {
      if (debug) LOG.debug("Using local live smart meter connection. ");

      final String scenarioPath = RunManager.getAbsolutePathToTestCaseFolder() + defaultScenario + "/";
      final File dir = new File(scenarioPath);
      if (debug) LOG.debug("{} - connecting to local smart meter to get simulated sensor data.", dir.toString());
      List<String> meterName = getMeterNames(dir);
      if (debug) LOG.debug("Reading simulated data for {} meters from running MatLab instance. ", meterName.size());
    }
    isInitialized = true;
  }

  /**
   Indicates whether currently connected to a running MatLab instance.

   @return - true if connected, false if not.
   */
  @Override
  public boolean isConnected() {
    return isConnected;
  }

  /**
   Indicates whether currently connected to a running MatLab instance.

   @return - true if connected, false if not.
   */
  @Override
  public boolean isInitialized() {
    return isInitialized;
  }

  /**
   Updates the sensor matrix to reflect new settings such as Smart Inverter Reactive Power settings. Only the live smart
   meter adapter can do this, both offline smart meter and csv do not react to control actions.

   @param meterName - the name of the associated smart meter, e.g. SM-44 set in matlab_smartmeter_row.properties
   @param timeSlice - confirming the time slice to be updated
   @param setting - the full two-part reading (electrical data for previous and this timeSlice.)
   @return - true if updated, false if not updated
   */
  @Override
  public boolean issueControlAction(final String meterName, final long timeSlice, final ISetting<?> setting) {
    if (debug)
      LOG.debug("MatLab at t {}: updated new inputs array before  {} is {}", timeSlice, meterName, Sensor_Data_NewInputs);

    // get the row number from the smart inverter name
    final int iRow = SmartMeterRowTranslator.getRowID(meterName);

    // get the column number for just the calculated generation reactive
    // power, Q
    final int iCol = ElectricalDataColumnTranslator.getColID("Qgeneration");

    // update the data
    double[][] arr = MatLabSmartMeterSimulator.getSensor_Data_NewInputs().clone();
    if (debug) LOG.debug("CELL BEFORE: {}", arr[iRow][iCol]);
    arr[iRow][iCol] = ((SmartInverterSetting) setting.getActuatorSettingObject()).getQ_gen_now();
    if (debug) LOG.debug("CELL AFTER: {}", arr[iRow][iCol]);
    MatLabSmartMeterSimulator.setSensor_Data_NewInputs(arr);
    MatLabSmartMeterSimulator.setSensor_Data_Denise(arr);

    if (debug) LOG.debug("MatLab at t {}: CONTROL ACTION SETS iROW {} and iCOL {} for {} to {} ",
            timeSlice, iRow, iCol, meterName, Sensor_Data_NewInputs[iRow][iCol]);
    if (debug) LOG.debug("MatLab at t {}: updated new inputs array due to {} is now  {}",
            timeSlice, meterName, Sensor_Data_NewInputs);
    boolean success = false;
    return success;
  }


    /**
   Load an {@code IElectricalData} from a given MatLab array of doubles.

   @param doubles - the source data
   @param electricalData - the newly loaded structure
   */
  private void loadElectricalData(double[] doubles, IElectricalData electricalData) {
    electricalData.setPhaseAPload(doubles[0]);
    electricalData.setPhaseAQload(doubles[1]);
    electricalData.setPhaseBPload(doubles[2]);
    electricalData.setPhaseBQload(doubles[3]);
    electricalData.setPhaseCPload(doubles[4]);
    electricalData.setPhaseCQload(doubles[5]);
    electricalData.setPgeneration(doubles[6]);
    electricalData.setQgeneration(doubles[7]);
    electricalData.setPhaseAvoltage(doubles[8]);
    electricalData.setPhaseBvoltage(doubles[9]);
    electricalData.setPhaseCvoltage(doubles[10]);
  }

  /**
   Load an {@code IElectricalData} from a given MatLab array of doubles using the new format.  Updated in June 2014
   to accommodate the new MatLab format.

   @param doubles - the source data
   @param electricalData - the newly loaded structure
   */
  private void loadElectricalDataExtended(double[] doubles, IElectricalData electricalData) {
    electricalData.setPhaseAPload(doubles[6]);
    electricalData.setPhaseAQload(doubles[7]);
    electricalData.setPhaseBPload(doubles[8]);
    electricalData.setPhaseBQload(doubles[9]);
    electricalData.setPhaseCPload(doubles[10]);
    electricalData.setPhaseCQload(doubles[11]);
    electricalData.setPgeneration(0.0);
    electricalData.setQgeneration(0.0);
    electricalData.setPhaseAvoltage(doubles[13]);
    electricalData.setPhaseBvoltage(doubles[14]);
    electricalData.setPhaseCvoltage(doubles[15]);
    if (debug && doubles[3] > 0) {
      LOG.debug("Loaded electrical data from source array: {}", electricalData.toString());
    }
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
