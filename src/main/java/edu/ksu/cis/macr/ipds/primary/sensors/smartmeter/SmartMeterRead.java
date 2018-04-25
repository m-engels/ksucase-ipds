package edu.ksu.cis.macr.ipds.primary.sensors.smartmeter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.GregorianCalendar;

/**
 {@code SmartMeterRead} contains measured values for real and reactive power for load and real power for generation, as
 well as all 4 values from the last electrical data set for a single sensor node.
 */
public class SmartMeterRead implements ISmartMeterRead {
  private static final Logger LOG = LoggerFactory.getLogger(SmartMeterRead.class);
  private static final long serialVersionUID = 1L;
  private int timeslice;
  private GregorianCalendar calendar;
  private String smartMeterName;
  private int sensorNodeID;
  private IElectricalData electricalData;
  private IElectricalData previousElectricalData;
  private IElectricalData electricalDataFromNewFormat;
  private IElectricalData previousElectricalDataFromNewFormat;

  /**
   *
   */
  public SmartMeterRead() {
    this.electricalData = new ElectricalData();
    this.previousElectricalData = new ElectricalData();
    this.electricalDataFromNewFormat = new ElectricalData();
    timeslice = 0;
    calendar = null;
    smartMeterName = null;
    sensorNodeID = 0;
  }




  public SmartMeterRead(final double useKW, final double genKW, final double gridKW, final double solarKW, final double Q_load_now, final double Q_load_last, final double P_load_now, final double P_load_last, final double P_gen_now, final double P_gen_last, final double Q_gen_last) {
    this.electricalData = new ElectricalData();
    this.previousElectricalData = new ElectricalData();
    this.electricalDataFromNewFormat = new ElectricalData();

    this.electricalData.setPhaseAPload(useKW);
    this.electricalData.setPgeneration(genKW);

    this.electricalData.setPhaseAQload(Q_load_now);
    this.previousElectricalData.setPhaseAQload(Q_load_last);

    this.electricalData.setPhaseAPload(P_load_now);
    this.previousElectricalData.setPhaseAPload(P_gen_last);

    // store last reading
    this.previousElectricalData = this.electricalData;

    // store new information
    this.electricalData = new ElectricalData();
    electricalData.setPgeneration(genKW);
    electricalData.setPhaseAPload(useKW);
    timeslice = 0;
    calendar = null;
    smartMeterName = null;
    sensorNodeID = 0;
  }

  /**
   @return the electricalData
   */
  @Override
  public IElectricalData getElectricalData() {
    return electricalData;
  }

  /**
   @param electricalData the electricalData to set
   */
  @Override
  public synchronized void setElectricalData(IElectricalData electricalData) {
    this.electricalData = electricalData;
  }

  @Override
  public IElectricalData getElectricalDataFromNewFormat() {
    return electricalDataFromNewFormat;
  }

  @Override
  public synchronized void setElectricalDataFromNewFormat(IElectricalData electricalDataFromNewFormat) {
    this.electricalDataFromNewFormat = electricalDataFromNewFormat;
  }

  /**
   @return the previousElectricalData
   */
  @Override
  public IElectricalData getPreviousElectricalData() {
    return previousElectricalData;
  }

  /**
   @param previousElectricalData the previousElectricalData to set
   */
  @Override
  public synchronized void setPreviousElectricalData(IElectricalData previousElectricalData) {
    this.previousElectricalData = previousElectricalData;
  }

  public IElectricalData getPreviousElectricalDataFromNewFormat() {
    return previousElectricalDataFromNewFormat;
  }

  public synchronized void setPreviousElectricalDataFromNewFormat(IElectricalData previousElectricalDataFromNewFormat) {
    this.previousElectricalDataFromNewFormat = previousElectricalDataFromNewFormat;
  }

  /**
   @return the sensorNodeID
   */
  @Override
  public int getSensorNodeID() {
    return sensorNodeID;
  }

  /**
   @param sensorNodeID the sensorNodeID to set
   */
  @Override
  public synchronized void setSensorNodeID(int sensorNodeID) {
    this.sensorNodeID = sensorNodeID;
  }

  /**
   @return the smartMeterName
   */
  @Override
  public String getSmartMeterName() {
    return smartMeterName;
  }

  /**
   @param smartMeterName the smartMeterName to set
   */
  @Override
  public synchronized void setSmartMeterName(String smartMeterName) {
    this.smartMeterName = smartMeterName;
  }

  /**
   @return the timeslice
   */
  @Override
  public int getTimeslice() {
    return timeslice;
  }

  /**
   @param timeslice the timeslice to set
   */
  @Override
  public synchronized void setTimeslice(int timeslice) {
    this.timeslice = timeslice;
  }

  @Override
  public String toString() {
    return "SmartMeterRead{" +
            "timeslice=" + timeslice +
            ", calendar=" + calendar +
            ", smartMeterName='" + smartMeterName + '\'' +
            ", sensorNodeID=" + sensorNodeID +
            ", electricalData=" + electricalData +
            ", previousElectricalData=" + previousElectricalData +
            ", electricalDataFromNewFormat=" + electricalDataFromNewFormat +
            '}';
  }


}
