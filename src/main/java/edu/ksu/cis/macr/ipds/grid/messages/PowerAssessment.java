package edu.ksu.cis.macr.ipds.grid.messages;

import java.io.Serializable;

/**
 The {@code PowerAssessment} contains the results of assessing an actual power value to see if it is within acceptable
 boundaries.
 */
public class PowerAssessment implements Serializable, IPowerAssessment {

  private static final long serialVersionUID = 1L;
  private RangeAssessment assessment;
  private double margin;
  private TrendAssessment marginTrend;
  private double actualKW;
  private double lastKW;
  private double maxKW;
  private double minKW;

  public PowerAssessment() {
    minKW = 0.0;
    maxKW = 0.0;
    lastKW = 0.0;
    actualKW = 0.0;
    margin = 0.0;
    marginTrend = TrendAssessment.LEVEL;
    assessment = RangeAssessment.INBOUNDS;
  }

  @Override
  public double getActualKW() {
    return actualKW;
  }

  @Override
  public synchronized void setActualKW(final double actualKW) {
    this.actualKW = actualKW;
  }

  /**
   @return the boundaryAssessment
   */
  @Override
  public RangeAssessment getAssessment() {
    return this.assessment;
  }

  /**
   @param assessment the assessment to set
   */
  @Override
  public synchronized void setAssessment(final RangeAssessment assessment) {
    this.assessment = assessment;
  }

  public double getLastKW() {
    return lastKW;
  }

  public synchronized void setLastKW(final double lastKW) {
    this.lastKW = lastKW;
  }

  /**
   @return the margin
   */
  @Override
  public double getMargin() {
    return this.margin;
  }

  /**
   @param margin the margin to set
   */
  @Override
  public synchronized void setMargin(final double margin) {
    this.margin = margin;
  }

  /**
   @return the marginTrend
   */
  @Override
  public TrendAssessment getMarginTrend() {
    return this.marginTrend;
  }

  /**
   @param marginTrend the marginTrend to set
   */
  @Override
  public synchronized void setMarginTrend(final TrendAssessment marginTrend) {
    this.marginTrend = marginTrend;
  }

  @Override
  public double getMaxKW() {
    return maxKW;
  }

  @Override
  public synchronized void setMaxKW(final double maxKW) {
    this.maxKW = maxKW;
  }

  @Override
  public double getMinKW() {
    return minKW;
  }

  @Override
  public synchronized void setMinKW(final double minKW) {
    this.minKW = minKW;
  }

  @Override
  public String toString() {
    return String.format("PowerAssessment{assessment=%s, margin=%s, marginTrend=%s, actualKW=%s, lastKW=%s, maxKW=%s, minKW=%s}", assessment, margin, marginTrend, actualKW, lastKW, maxKW, minKW);
  }
}
