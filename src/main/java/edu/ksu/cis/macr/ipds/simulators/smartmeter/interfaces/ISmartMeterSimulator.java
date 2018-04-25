/**
 * IEgaugeAdaptible.java
 *
 * Copyright 2012 Kansas State University MACR Laboratory
 * http://macr.cis.ksu.edu/ Department of Computing & Information Sciences
 *
 * See License.txt file for the license agreement.
 *
 */
package edu.ksu.cis.macr.ipds.simulators.smartmeter.interfaces;

import edu.ksu.cis.macr.ipds.primary.sensors.SensorType;
import edu.ksu.cis.macr.obaa_pp.actuator.ISetting;
import edu.ksu.cis.macr.obaa_pp.sensor.IRead;

import java.util.ArrayList;

/**
 The {@code IEgaugeAdaptible} interface for data sources that can be used by the {@code PhysicalSystemSimulator}.
 */
public interface ISmartMeterSimulator {

  /**
   Gets a list of sensor readings for all sensors at a given time slice.  Sensor names are mapped to the data source
   identifiers (e.g. row numbers) in the matlab_smartmeter_row.properties file in the main SelfOrganization folder.

   @return - the Read object with the set of all sense results (for this sensor type)
    * @param timeSlice - the desired time slice  (as the count of elapsed time slices  since the simulation began)
   */
  public ArrayList<IRead<?>> getAllReadsAt(long timeSlice);


  /**
   Get a set of sensor readings for the given sensor name at the most recent simulation time slice as maintained by the
   {@code Clock}.  Sensor names are mapped to the data source identifiers (e.g. row numbers) in the
   matlab_smartmeter_row.properties file in the main SelfOrganization folder.

   @param sensorName - the sensor name defined in matlab_smartmeter_row.properties, e.g. SM-44
   @return {@code Read<SensorType>} - the Read object with the sense results (for this sensor type)
   */
  public IRead<SensorType> getRead(String sensorName);

  /**
   Get a set of sensor readings for the given sensor name at the given time slice.  Sensor names are mapped to the
   data source identifiers (e.g. row numbers) in the matlab_smartmeter_row.properties file in the main SelfOrganization
   folder.

   @param sensorName - the sensor name defined in matlab_smartmeter_row.properties, e.g. SM-44
   @param timeSlice -  the desired time slice (as the count of elapsed time slices since the simulation began)
   @return {@code Read<SensorType>} - the Read object with the sense results (for this sensor type)
   */
  public IRead<?> getReadAt(String sensorName, long timeSlice);

  /**
   Initializes the adapter with information about the desired simulation.

   @param testCaseName - the name of this test case or simulation
   @param fullDataPathAndFile - the full path and file name containing the simulated sensor data
   */
  public abstract void initialize(String testCaseName, String fullDataPathAndFile);

  /**
   Returns a value indicating whether this adapter is connected to the simulation data sourcee.

   @return true if connected; false if not connected.
   */
  public boolean isConnected();

  /**
   Returns a value indicating whether the connected data source been initialized.

   @return true if initialized; false if not initialized.
   */
  public boolean isInitialized();

  /**
   Updates the sensor matrix to reflect new settings such as Smart Inverter Reactive Power settings. Only the live smart
   meter can do this, both offline smart meter and csv will not react to control actions.

   @param sensorName - the name of the associated smart meter, e.g. SM-44 set in matlab_smartmeter_row.properties
   @param timeSlice - confirming the time slice to be updated
   @param setting - the setting information describing the action to be taken.
   @return - true if updated, false if not updated
   */
  public boolean issueControlAction(String sensorName, long timeSlice, ISetting<?> setting);

}
