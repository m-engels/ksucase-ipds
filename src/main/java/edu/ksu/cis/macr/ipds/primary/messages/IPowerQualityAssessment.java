package edu.ksu.cis.macr.ipds.primary.messages;

/**
 {@code IPowerQualityAssessment} provides an interface for describing a power quality assessment - e.g. is the voltage
 within limits; is the margin increasing or decreasing.
 */
public interface IPowerQualityAssessment {
  double getLastVoltage();

  void setLastVoltage(double lastVoltage);

  /**
   @return the margin
   */
  double getMargin();

  /**
   @param margin the margin to set
   */
  void setMargin(double margin);

  /**
   @return the marginTrend
   */
  TrendAssessment getMarginTrend();

  /**
   @param marginTrend the marginTrend to set
   */
  void setMarginTrend(TrendAssessment marginTrend);

  double getMaxVoltage();

  void setMaxVoltage(double maxVoltage);

  double getMinVoltage();

  void setMinVoltage(double minVoltage);

  /**
   @return the boundaryAssessment
   */
  VoltageAssessment getVoltageAssessment();

  /**
   @param assessment the assessment to set
   */
  void setVoltageAssessment(VoltageAssessment assessment);

  void setThisVoltage(double thisVoltage);

  public enum VoltageAssessment {
    HIGH, INBOUNDS, LOW
  }

  public enum TrendAssessment {
    DECREASING, INCREASING, LEVEL
  }
}
