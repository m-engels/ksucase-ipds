/**
 * SimulatorData.java
 *
 * Copyright 2012 Kansas State University MACR Laboratory
 * http://macr.cis.ksu.edu/ Department of Computing & Information Sciences
 *
 * See License.txt file for the license agreement.
 *
 */
package edu.ksu.cis.macr.ipds.simulators.smartmeter.model;



import edu.ksu.cis.macr.ipds.primary.sensors.smartmeter.ISmartMeterRead;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

/**
 The {@code SmartMeterSimulatorData} class stores data for a SmartMeterSensorsFrame object  Uses a timeslice date for
 a key and stores meter readings in a HashTable
 */
public class SmartMeterSimulatorData implements Serializable {

  private static final long serialVersionUID = 730728011300902192L;
  // SmartMeterSensorsFrame Name
  private final String smartMeterName;
  // Meter IEgaugeAdaptible
  private HashMap<String, ISmartMeterRead> data;

  /*
   * Constructor, takes SmartMeterSensorsFrame smartMeterName as parameter and initializes
   * the HashMap data.
   */
  public SmartMeterSimulatorData(String smartMeterName) {
    this.smartMeterName = smartMeterName;
    data = new HashMap<>();
  }

  /*
   * Takes a date as parameter and returns meter read for date parameter.
   * Else returns null if date not found in
   */
  public ISmartMeterRead getMeterReadByDate(final Date date) {
    final String d = date.toString();

    if (data.containsKey(d)) {
      return data.get(d);
    }

    return null;
  }

  /*
   * Returns SmartMeterSensorsFrame smartMeterName
   */
  public String getSmartMeterName() {
    return smartMeterName;
  }
}
