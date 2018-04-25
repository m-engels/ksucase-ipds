package edu.ksu.cis.macr.ipds.simulators.smartmeter.interfaces;

import edu.ksu.cis.macr.ipds.config.RunManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

/**
 Singleton class that reads the list of SelfOrganization smart meters along with their corresponding row identifier in
 MatLab from the matlab_smartmeter_row.properties file.  A singleton means there will be one instance per JVM.
 */
public enum SmartMeterRowTranslator {

  /**
   Singleton instance of the smart meter row translator (one per JVM).
   */
  INSTANCE;

  final static String CUR_DIR = System.getProperty("user.dir");
  private static final Logger LOG = LoggerFactory.getLogger(SmartMeterRowTranslator.class);
  private static final boolean debug = false;
  private static final String FILENAME = "matlab_smartmeter_row.properties";
  private static Properties properties = null;
  private static boolean isLoaded = false;
  private static int numValues = -1;

  /**
   Lookup the run number for a given a smart meter name in the list of sensor nodes.

   @param smartMeterName - the name of the smart meter to look up, e.g. "SM-44"
   @return - the integer row in the smart meter data file with data for that smart meter.
   */
  public static int getRowID(final String smartMeterName) {
    if (!isLoaded) {
      SmartMeterRowTranslator.load();
    }
    if (SmartMeterRowTranslator.getNumDataValues() < 1) {
      return SmartMeterRowTranslator.getNumDataValues();
    }
    String strRow = SmartMeterRowTranslator.properties.getProperty(smartMeterName);
    return Integer.parseInt(strRow.trim());
  }

  /**
   Get the total number of entries in this file.

   @return int - the number of properties specified
   */
  public static int getNumDataValues() {
    if (!isLoaded) {
      SmartMeterRowTranslator.load();
    }
    return SmartMeterRowTranslator.numValues;
  }

  /**
   Set the properties from a given set.

   @param properties the given set of properties
   */
  private static void setProperties(final Properties properties) {
    SmartMeterRowTranslator.properties = properties;
  }

  /**
   Read in the list of smart meter sensor nodes and their associated row numbers in the sensor data file.
   */
  public static synchronized void load() {
    properties = new Properties();
    File f = new File(RunManager.getAbsolutePathToConfigsFolder() + "/standardproperties", FILENAME);
    LOG.info(" READING SMART METER SENSOR NODES INFORMATION .......... from {}", f.getAbsolutePath());

    try {
      try (FileInputStream fileInputStream = new FileInputStream(f)) {
        properties = new Properties();
        properties.load(fileInputStream);
      }
      Enumeration enuKeys;
      final Iterator<Object> iterator = properties.keySet().iterator();
      numValues = 0;
      while (iterator.hasNext()) {
        String key = (String) iterator.next();
        String value = properties.getProperty(key);
        if (debug) LOG.debug("\t{}: {}", key, value);
        numValues++;
      }
      isLoaded = true;

    } catch (FileNotFoundException e) {
      LOG.error("RRROR: Smart meter sensor matlab_smartmeter_row.properties file not found.");
    } catch (IOException e) {
      LOG.error("ERROR: Smart meter sensor matlab_smartmeter_row.properties file - error reading contents.");
    }
  }
}
