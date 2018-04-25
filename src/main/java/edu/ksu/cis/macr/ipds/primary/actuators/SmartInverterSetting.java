/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ksu.cis.macr.ipds.primary.actuators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 {@code SmartInverter} sends the new setting for the associated smart inverter's reactive power setting.
 */
public class SmartInverterSetting implements Serializable {
  private static final Logger LOG = LoggerFactory.getLogger(SmartInverterSetting.class);

  private static final long serialVersionUID = 1L;
  private double Q_gen_now;
  private long timeSlice = 0;
  private String smartMeterName;
  private String smartInverterName;
  private int actuatorNodeID;

  public SmartInverterSetting() {
    smartMeterName = "";
    smartInverterName = "";
    actuatorNodeID = 0;
    Q_gen_now = 0.0;
  }

  /**
   @return the actuatorNodeID
   */
  public int getActuatorNodeID() {
    return actuatorNodeID;
  }

  /**
   @param actuatorNodeID the actuatorNodeID to set
   */
  public synchronized void setActuatorNodeID(int actuatorNodeID) {
    this.actuatorNodeID = actuatorNodeID;
  }

  /**
   @return the Q_gen_now
   */
  public double getQ_gen_now() {
    return Q_gen_now;
  }

  /**
   @param Q_gen_now the Q_gen_now to set
   */
  public synchronized void setQ_gen_now(double Q_gen_now) {
    this.Q_gen_now = Q_gen_now;
  }

  /**
   @return the smartInverterName
   */
  public String getSmartInverterName() {
    return smartInverterName;
  }

  /**
   @param smartInverterName the smartInverterName to set
   */
  public synchronized void setSmartInverterName(String smartInverterName) {
    this.smartInverterName = smartInverterName;
  }

  /**
   @return the smartMeterName
   */
  public String getSmartMeterName() {
    return smartMeterName;
  }

  /**
   @param smartMeterName the smartMeterName to set
   */
  public synchronized void setSmartMeterName(String smartMeterName) {
    this.smartMeterName = smartMeterName;
  }

  /**
   @return the timeSlice
   */
  public long getTimeSlice() {
    return timeSlice;
  }

  /**
   @param timeSlice the timeSlice to set
   */
  public synchronized void setTimeSlice(long timeSlice) {
    this.timeSlice = timeSlice;
  }
}
