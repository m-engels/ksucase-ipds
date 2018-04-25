package edu.ksu.cis.macr.ipds.simulators.smartmeter.process;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLDouble;
import edu.ksu.cis.macr.aasis.simulator.clock.Clock;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/**
 The {@code LiveMatLabAdapter} implements the {@code IEgaugeAdaptible} interface and is used to interact with devices
 simulated in MatLab.
 */
public class OfflineMatLabSmartMeterSimulator implements ISmartMeterSimulator {

  final static String CUR_DIR = System.getProperty("user.dir");
  private static final Logger LOG = LoggerFactory.getLogger(OfflineMatLabSmartMeterSimulator.class);
  private static String fullDataPathAndFile;
  private static String testCaseName;
  private static double[][] dataArray;
  private boolean debug = false;

  public OfflineMatLabSmartMeterSimulator() {
  }

  public static String getFullDataPathAndFile() {
    return fullDataPathAndFile;
  }

  public static void setFullDataPathAndFile(final String fullDataPathAndFile) {
    OfflineMatLabSmartMeterSimulator.fullDataPathAndFile = fullDataPathAndFile;
  }

  /**
   @return the testCaseName
   */
  public static String getTestCaseName() {
    return testCaseName;
  }

  /**
   @param aTestCaseName the testCaseName to set
   */
  public static void setTestCaseName(String aTestCaseName) {
    testCaseName = aTestCaseName;
  }

  /**
   @return the dataArray
   */
  public static double[][] getDataArray() {
    return OfflineMatLabSmartMeterSimulator.dataArray;
  }

  /**
   @param aDataArray the dataArray to set
   */
  public static void setDataArray(double[][] aDataArray) {
    OfflineMatLabSmartMeterSimulator.dataArray = aDataArray;
  }

  @Override
  public ArrayList<IRead<?>> getAllReadsAt(long timeSlice) {
    LOG.info("Getting all sensor readings from data adapter for time slice {}.", timeSlice);
    ArrayList<IRead<?>> allReads = new ArrayList<IRead<?>>();

    int numDataValueColumnsInSet = ElectricalDataColumnTranslator.getNumDataValues();
    int cursor = (int) timeSlice * numDataValueColumnsInSet;
    LOG.debug("cursor : {}", cursor);

    if (debug) {
      LOG.debug("cursor : {}", cursor);
      for (int r = 0; r < 62; r++) {
        for (int j = 0; j < 11; j++) {
          LOG.debug("  data[{}][{}] : {}", r, j, getDataArray()[r][j]);
        }
      }
    }
    // TODO: NUM_ROWS in the physical simulation needs to be determined from the Test Case
    // TODO: Physical simulation needs to provide NUM_ROWS to the data adapters.

    final int NUM_ROWS = 62;
    for (int i = 0; i < NUM_ROWS; i++) {
      IElectricalData current = new ElectricalData();
      int n = 0;
      current.setPhaseAPload(OfflineMatLabSmartMeterSimulator.getDataArray()[i][cursor + n++]);
      current.setPhaseAQload(OfflineMatLabSmartMeterSimulator.getDataArray()[i][cursor + n++]);
      current.setPhaseBPload(OfflineMatLabSmartMeterSimulator.getDataArray()[i][cursor + n++]);
      current.setPhaseBQload(OfflineMatLabSmartMeterSimulator.getDataArray()[i][cursor + n++]);
      current.setPhaseCPload(OfflineMatLabSmartMeterSimulator.getDataArray()[i][cursor + n++]);
      current.setPhaseCQload(OfflineMatLabSmartMeterSimulator.getDataArray()[i][cursor + n++]);
      current.setPgeneration(OfflineMatLabSmartMeterSimulator.getDataArray()[i][cursor + n++]);
      current.setQgeneration(OfflineMatLabSmartMeterSimulator.getDataArray()[i][cursor + n++]);
      current.setPhaseAvoltage(OfflineMatLabSmartMeterSimulator.getDataArray()[i][cursor + n++]);
      current.setPhaseBvoltage(OfflineMatLabSmartMeterSimulator.getDataArray()[i][cursor + n++]);
      current.setPhaseCvoltage(OfflineMatLabSmartMeterSimulator.getDataArray()[i][cursor + n++]);

      ISmartMeterRead smartMeterReading = new SmartMeterRead();
      smartMeterReading.setElectricalData(current);

      if (timeSlice == 0) {
        smartMeterReading.setPreviousElectricalData(current);
      } else {
        cursor = ((int) timeSlice - 1) * numDataValueColumnsInSet;
        IElectricalData previous = new ElectricalData();
        n = 0;
        previous.setPhaseAPload(OfflineMatLabSmartMeterSimulator.getDataArray()[i][cursor + n++]);
        previous.setPhaseAQload(OfflineMatLabSmartMeterSimulator.getDataArray()[i][cursor + n++]);
        previous.setPhaseBPload(OfflineMatLabSmartMeterSimulator.getDataArray()[i][cursor + n++]);
        previous.setPhaseBQload(OfflineMatLabSmartMeterSimulator.getDataArray()[i][cursor + n++]);
        previous.setPhaseCPload(OfflineMatLabSmartMeterSimulator.getDataArray()[i][cursor + n++]);
        previous.setPhaseCQload(OfflineMatLabSmartMeterSimulator.getDataArray()[i][cursor + n++]);
        previous.setPgeneration(OfflineMatLabSmartMeterSimulator.getDataArray()[i][cursor + n++]);
        previous.setQgeneration(OfflineMatLabSmartMeterSimulator.getDataArray()[i][cursor + n++]);
        previous.setPhaseAvoltage(OfflineMatLabSmartMeterSimulator.getDataArray()[i][cursor + n++]);
        previous.setPhaseBvoltage(OfflineMatLabSmartMeterSimulator.getDataArray()[i][cursor + n++]);
        previous.setPhaseCvoltage(OfflineMatLabSmartMeterSimulator.getDataArray()[i][cursor + n++]);
        smartMeterReading.setPreviousElectricalData(previous);
      }
      IRead<SensorType> reading = new Read<>();
      reading.setSensorReadObject(smartMeterReading);
      allReads.add(reading);
    }
    return allReads;
  }

  /**
   Get the discrete reading for current time slice for given smart meter (maintained by the {@code Clock}).

   @param smartMeterName - the string name of the smart meter
   @return {@code IRead} with the reading
   */
  @Override
  public IRead<SensorType> getRead(String smartMeterName) {
    return getReadAt(smartMeterName, Clock.getTimeSlicesElapsedSinceStart());
  }

  @Override
  public IRead<SensorType> getReadAt(String smartMeterName, long timeSlice) {
    if (timeSlice < 0) {
      return null;
    }
    if (smartMeterName.isEmpty()) {
      return null;
    }
    LOG.debug("GetDiscreteReading for {} at time slice {}", smartMeterName, timeSlice);

    int rowID = SmartMeterRowTranslator.getRowID(smartMeterName);
    if (rowID == -1) {
      LOG.error("Cannot find smart meter {}, please check configurations.", smartMeterName);
      System.exit(-1);
    }

    LOG.debug("GetDiscreteReading {} is at rowID {}", smartMeterName, rowID);

    int numDataValueColumnsInSet = ElectricalDataColumnTranslator.getNumDataValues();
    int cursor = (int) timeSlice * numDataValueColumnsInSet;
    if (debug) {
      LOG.debug("cursor : {}", cursor);
      for (int j = 0; j < 11; j++) {
        LOG.debug("  data[{}][{}] : {}", rowID, j, getDataArray()[rowID][j]);
      }
    }
    int i = 0;
    IElectricalData current = new ElectricalData();
    current.setPhaseAPload(OfflineMatLabSmartMeterSimulator.getDataArray()[rowID][cursor + i++]);
    current.setPhaseAQload(OfflineMatLabSmartMeterSimulator.getDataArray()[rowID][cursor + i++]);
    current.setPhaseBPload(OfflineMatLabSmartMeterSimulator.getDataArray()[rowID][cursor + i++]);
    current.setPhaseBQload(OfflineMatLabSmartMeterSimulator.getDataArray()[rowID][cursor + i++]);
    current.setPhaseCPload(OfflineMatLabSmartMeterSimulator.getDataArray()[rowID][cursor + i++]);
    current.setPhaseCQload(OfflineMatLabSmartMeterSimulator.getDataArray()[rowID][cursor + i++]);
    current.setPgeneration(OfflineMatLabSmartMeterSimulator.getDataArray()[rowID][cursor + i++]);
    current.setQgeneration(OfflineMatLabSmartMeterSimulator.getDataArray()[rowID][cursor + i++]);
    current.setPhaseAvoltage(OfflineMatLabSmartMeterSimulator.getDataArray()[rowID][cursor + i++]);
    current.setPhaseBvoltage(OfflineMatLabSmartMeterSimulator.getDataArray()[rowID][cursor + i++]);
    current.setPhaseCvoltage(OfflineMatLabSmartMeterSimulator.getDataArray()[rowID][cursor + i++]);

    ISmartMeterRead smartMeterReading;
    smartMeterReading = new SmartMeterRead();
    smartMeterReading.setElectricalData(current);

    if (timeSlice == 0) {
      smartMeterReading.setPreviousElectricalData(current);
    } else {
      cursor = ((int) timeSlice - 1) * numDataValueColumnsInSet;
      IElectricalData previous = new ElectricalData();
      i = 0;
      previous.setPhaseAPload(OfflineMatLabSmartMeterSimulator.getDataArray()[rowID][cursor + i++]);
      previous.setPhaseAQload(OfflineMatLabSmartMeterSimulator.getDataArray()[rowID][cursor + i++]);
      previous.setPhaseBPload(OfflineMatLabSmartMeterSimulator.getDataArray()[rowID][cursor + i++]);
      previous.setPhaseBQload(OfflineMatLabSmartMeterSimulator.getDataArray()[rowID][cursor + i++]);
      previous.setPhaseCPload(OfflineMatLabSmartMeterSimulator.getDataArray()[rowID][cursor + i++]);
      previous.setPhaseCQload(OfflineMatLabSmartMeterSimulator.getDataArray()[rowID][cursor + i++]);
      previous.setPgeneration(OfflineMatLabSmartMeterSimulator.getDataArray()[rowID][cursor + i++]);
      previous.setQgeneration(OfflineMatLabSmartMeterSimulator.getDataArray()[rowID][cursor + i++]);
      previous.setPhaseAvoltage(OfflineMatLabSmartMeterSimulator.getDataArray()[rowID][cursor + i++]);
      previous.setPhaseBvoltage(OfflineMatLabSmartMeterSimulator.getDataArray()[rowID][cursor + i++]);
      previous.setPhaseCvoltage(OfflineMatLabSmartMeterSimulator.getDataArray()[rowID][cursor + i++]);
      smartMeterReading.setPreviousElectricalData(previous);
    }

    IRead<SensorType> reading = new Read<>();
    reading.setSensorReadObject(smartMeterReading);
    return reading;
  }

  @Override
  public synchronized void initialize(String testCaseName, String fullDataPathAndFile) {
    LOG.info("\tUsing offline MatLab data adapter: {} for {}", fullDataPathAndFile, testCaseName);
    setFullDataPathAndFile(fullDataPathAndFile);
    setTestCaseName(testCaseName);
    File file;
    try {
      file = new File(fullDataPathAndFile);
      LOG.info("\tFile: {}", file.toString());
      //LOG.info("   File.exists {}", file.exists());
      MatFileReader matFileReader = new MatFileReader(file);
      //LOG.info("MatFileReader: {}", matFileReader.toString());
      MLDouble mlDouble = (MLDouble) matFileReader.getMLArray("OUTPUT");
      OfflineMatLabSmartMeterSimulator.setDataArray(mlDouble.getArray());
    } catch (FileNotFoundException ex) {
      LOG.error("FileNotFoundException Error intializing offline smartmeter adapter.");
      System.exit(-1);
    } catch (IOException ex) {
      LOG.error("IOException Error intializing offline smartmeter adapter. ");
      System.exit(-1);
    }
  }

  @Override
  public boolean isConnected() {
    return true;
  }

  @Override
  public boolean isInitialized() {
    return true;
  }

  /**
   Updates the sensor matrix to reflect new settings such as Smart Inverter Reactive Power settings. Only the live
   smartmeter can do this, both offline smartmeter and csv will not react to control actions.

   @param smartMeterName - the name of the associated smart meter, e.g. SM-44 set in matlab_smartmeter_row.properties
   @param timeslice - confirming the timeslice to be updated
   @param smartMeterRead - the full two-part reading (electrical data for previous and this timeslice.)
   @return - true if updated, false if not updated
   */
  public boolean issueControlAction(final String smartMeterName, final long timeslice, final ISmartMeterRead smartMeterRead) {
    return false;
  }

  /**
   Updates the sensor matrix to reflect new settings such as Smart Inverter Reactive Power settings. Only the live
   smartmeter can do this, both offline smartmeter and csv will not react to control actions.

   @param smartMeterName - the name of the associated smart meter, e.g. SM-44 set in matlab_smartmeter_row.properties
   @param timeSlice - confirming the time slice to be updated
   @param setting - the information about the action to be taken.
   */
  @Override
  public boolean issueControlAction(final String smartMeterName, final long timeSlice,
                                    final ISetting<?> setting) {
    boolean success = false;
    if (debug) LOG.debug("Not implemented in the offline MatLab adapter.");
    return success;
  }

  @Override
  public String toString() {
    return "OfflineMatLabSmartMeterSimulator{" +
            "debug=" + debug +
            '}';
  }
}
