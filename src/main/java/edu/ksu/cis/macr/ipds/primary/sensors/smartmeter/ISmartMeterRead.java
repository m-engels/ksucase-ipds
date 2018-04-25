package edu.ksu.cis.macr.ipds.primary.sensors.smartmeter;

import java.io.Serializable;

/**
 Created by deniselive on 025 6 25 14.
 */
public interface ISmartMeterRead extends Serializable {

  IElectricalData getElectricalData();

  void setElectricalData(IElectricalData electricalData);

  IElectricalData getElectricalDataFromNewFormat();

  void setElectricalDataFromNewFormat(IElectricalData electricalDataFromNewFormat);

  IElectricalData getPreviousElectricalData();

  void setPreviousElectricalData(IElectricalData previousElectricalData);

  int getSensorNodeID();

  void setSensorNodeID(int sensorNodeID);

  String getSmartMeterName();

  void setSmartMeterName(String smartMeterName);

  int getTimeslice();

  void setTimeslice(int timeslice);

  void setPreviousElectricalDataFromNewFormat(IElectricalData previousElectricalDataFromNewFormat);
}
