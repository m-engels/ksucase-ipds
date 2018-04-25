package edu.ksu.cis.macr.ipds.primary.capabilities.manage;

import edu.ksu.cis.macr.ipds.config.SmartInverterAlgorithm;
import edu.ksu.cis.macr.ipds.primary.sensors.smartmeter.IElectricalData;
import edu.ksu.cis.macr.ipds.primary.sensors.smartmeter.ISmartMeterRead;


/**
 {@code ISmartInverterCapability} provides an interface for a smart meter capability.
 */
public interface ISmartInverterCapability {

  double calculateReactivePower(String phase, IElectricalData now, IElectricalData last, double powerFactor, SmartInverterAlgorithm algorithm);

  boolean calculateSmartInverterSetting(ISmartMeterRead smartMeterRead, double powerFactor, long timeSlice, double netDeltaP);

}
