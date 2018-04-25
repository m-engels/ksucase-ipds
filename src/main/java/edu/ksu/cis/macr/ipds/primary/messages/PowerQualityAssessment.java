package edu.ksu.cis.macr.ipds.primary.messages;

import java.io.Serializable;

/**
 The {@code PowerAssessment} contains the results of assessing an actual power value to see if it is within acceptable
 boundaries.
 */
public class PowerQualityAssessment implements Serializable, IPowerQualityAssessment {

  private static final long serialVersionUID = 1L;
  private VoltageAssessment voltageAssessment = VoltageAssessment.INBOUNDS;
  private double margin = 0.0;
  private TrendAssessment marginTrend = TrendAssessment.LEVEL;
  private double lastVoltage = 0.0;
  private double thisVoltage = 0.0;
  private double minVoltage = 0.0;
  private double maxVoltage = 0.0;

  @Override
  public double getLastVoltage() {
    return lastVoltage;
  }

  @Override
  public synchronized void setLastVoltage(final double lastVoltage) {
    this.lastVoltage = lastVoltage;
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
  public double getMaxVoltage() {
    return maxVoltage;
  }

  @Override
  public synchronized void setMaxVoltage(final double maxVoltage) {
    this.maxVoltage = maxVoltage;
  }

  @Override
  public double getMinVoltage() {
    return minVoltage;
  }

  @Override
  public synchronized void setMinVoltage(final double minVoltage) {
    this.minVoltage = minVoltage;
  }

  /**
   @return the boundaryAssessment
   */
  @Override
  public VoltageAssessment getVoltageAssessment() {
    return this.voltageAssessment;
  }

  /**
   @param voltageAssessment the voltageAssessment to set
   */
  @Override
  public synchronized void setVoltageAssessment(
          final VoltageAssessment voltageAssessment) {
    this.voltageAssessment = voltageAssessment;
  }

  @Override
  public synchronized void setThisVoltage(final double thisVoltage) {
    this.thisVoltage = thisVoltage;
  }

  @Override
  public String toString() {
    return "PowerQualityAssessment{" +
            "voltageAssessment=" + voltageAssessment +
            ", margin=" + margin +
            ", marginTrend=" + marginTrend +
            ", lastVoltage=" + lastVoltage +
            ", thisVoltage=" + thisVoltage +
            ", minVoltage=" + minVoltage +
            ", maxVoltage=" + maxVoltage +
            '}';
  }
}