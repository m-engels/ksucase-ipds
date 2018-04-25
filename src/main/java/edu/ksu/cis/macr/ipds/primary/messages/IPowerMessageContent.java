package edu.ksu.cis.macr.ipds.primary.messages;



import edu.ksu.cis.macr.ipds.primary.sensors.smartmeter.IElectricalData;

import java.io.IOException;

/**
 {@code IElectricalData} provides an interface for defining the content in an {@code IPowerMessage}, used to relay power
 and power quality information up the holarchy.
 */
public interface IPowerMessageContent {

  void add(IPowerMessageContent item);

  Object deserialize(byte[] bytes) throws Exception;

  double getActualKW();

  void setActualKW(double actualKW);

  double getCriticalActualKW();

  void setCriticalActualKW(double criticalActualKW);

  IElectricalData getElectricalData();

  void setElectricalData(IElectricalData electricalData);

  double getMaxKW();

  void setMaxKW(double value);

  double getMinKW();

  void setMinKW(double value);

  IElectricalData getPreviousElectricalData();

  void setPreviousElectricalData(IElectricalData previousElectricalData);

  double getPvGeneration();

  void setPvGeneration(double pvGeneration);

  double getPvGenerationRampRateKwPerMinute();

  void setPvGenerationRampRateKwPerMinute(double pvGenerationRampRateKwPerMinute);

  PowerMessageContent.EquipmentStatus getPvStatus();

  void setPvStatus(PowerMessageContent.EquipmentStatus pvStatus);

  double getRatedCapacityKW();

  void setRatedCapacityKW(double ratedCapacityKW);

  long getTimeSlice();

  void setTimeSlice(long timeSlice);

  boolean isEmpty();

  byte[] serialize() throws IOException;
}
