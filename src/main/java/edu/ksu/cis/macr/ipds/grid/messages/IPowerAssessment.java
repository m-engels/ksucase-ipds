package edu.ksu.cis.macr.ipds.grid.messages;

/**
 {@code IPowerAssessment} provides an interface for describing a power assessment - e.g. is the amount within reason; is
 it trending up or down.
 */
public interface IPowerAssessment {

  double getActualKW();

  void setActualKW(double actualKW);

  /**
   @return the boundaryAssessment
   */
  RangeAssessment getAssessment();

  /**
   @param assessment the assessment to set
   */
  void setAssessment(
          RangeAssessment assessment);

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

  double getMaxKW();

  void setMaxKW(double maxKW);

  double getMinKW();

  void setMinKW(double minKW);

  public enum RangeAssessment {
    HIGH, INBOUNDS, LOW
  }

  public enum TrendAssessment {
    DECREASING, INCREASING, LEVEL
  }
}
