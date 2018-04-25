package edu.ksu.cis.macr.ipds.primary.capabilities.manage;


import edu.ksu.cis.macr.ipds.primary.messages.IPowerQualityAssessment;
import edu.ksu.cis.macr.ipds.primary.sensors.smartmeter.ISmartMeterRead;


/**
 {@code IAssessReactivePowerQualityCapability} provides an interface for describing power quality assessments.
 */
public interface IAssessReactivePowerQualityCapability {
  /**
   @return the actualMultiplier
   */
  double getActualMultiplier();

  /**
   @param actualMultiplier the actualMultiplier to set
   */
  void setActualMultiplier(double actualMultiplier);

  /**
   @return the lastMultiplier
   */
  double getLastMultiplier();

  /**
   @param lastMultiplier the lastMultiplier to set
   */
  void setLastMultiplier(double lastMultiplier);

  /**
   @return the maxMultiplier
   */
  double getMaxMultiplier();

  /**
   @param maxMultiplier the maxMultiplier to set
   */
  void setMaxMultiplier(double maxMultiplier);

  /**
   @return the minMultiplier
   */
  double getMinMultiplier();

  /**
   @param minMultiplier the minMultiplier to set
   */
  void setMinMultiplier(double minMultiplier);

  /**
   @param minMultiplier - the minimum multiplier used for the assessment
   @param maxMultiplier - the maximum multiplier used for the assessment
   @param read - the data from the smart meter reading. (electrical data and time slices)
   @return PowerQualityAssessment
   */
  IPowerQualityAssessment getPowerQualityAssessment(double minMultiplier, double maxMultiplier, ISmartMeterRead read);
}
