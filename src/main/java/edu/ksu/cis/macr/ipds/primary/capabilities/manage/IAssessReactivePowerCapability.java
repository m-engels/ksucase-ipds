package edu.ksu.cis.macr.ipds.primary.capabilities.manage;


import edu.ksu.cis.macr.ipds.primary.messages.IPowerAssessment;
import edu.ksu.cis.macr.ipds.primary.sensors.smartmeter.ISmartMeterRead;

/**
 {@code IAssessReactivePowerCapability} provides an interface for describing power assessments.
 */
public interface IAssessReactivePowerCapability {
  /**
   @return the actualKW
   */
  double getActualKW();

  /**
   @param actualKW2 the actualKW to set
   */
  void setActualKW(double actualKW2);

  /**
   @return the maxKW
   */
  double getMaxKW();

  /**
   @param maxKW2 the maxKW to set
   */
  void setMaxKW(double maxKW2);

  /**
   @return the minKW
   */
  double getMinKW();

  /**
   @param minKW2 the minKW to set
   */
  void setMinKW(double minKW2);

  /**
   @param minKW - the minimum kW
   @param maxKW - the maximum kW
   @param read - the smart meter read to evaluate
   @return the power assessment
   */
  IPowerAssessment getPowerAssessment(double minKW, double maxKW, ISmartMeterRead read);
}
