/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ksu.cis.macr.ipds.primary.sensors.smartmeter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 */
public class ElectricalData implements IElectricalData, Serializable {
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LoggerFactory.getLogger(ElectricalData.class);
  private double PhaseAPload;
  private double PhaseAQload;
  private double PhaseBPload;
  private double PhaseBQload;
  private double PhaseCPload;
  private double PhaseCQload;
  private double Pgeneration;
  private double Qgeneration;
  private double Qgeneration_calculated;
  private double PhaseAvoltage;
  private double PhaseBvoltage;
  private double PhaseCvoltage;

  public ElectricalData() {
    this.PhaseCvoltage = 0;
    this.PhaseBvoltage = 0;
    this.PhaseAvoltage = 0;
    this.Qgeneration_calculated = 0;
    this.Qgeneration = 0;
    this.Pgeneration = 0;
    this.PhaseCQload = 0;
    this.PhaseCPload = 0;
    this.PhaseBQload = 0;
    this.PhaseBPload = 0;
    this.PhaseAQload = 0;
    this.PhaseAPload = 0;
  }

  @Override
  public synchronized void add(IElectricalData electricalData) {
    LOG.debug("aggregating electrical data: {}", electricalData.toString());
    this.PhaseAPload += electricalData.getPhaseAPload();
    this.PhaseAQload += electricalData.getPhaseAQload();
    this.PhaseBPload += electricalData.getPhaseBPload();
    this.PhaseBQload += electricalData.getPhaseBQload();
    this.PhaseCPload += electricalData.getPhaseCPload();
    this.PhaseCQload += electricalData.getPhaseCQload();
    this.Pgeneration += electricalData.getPgeneration();
    this.Qgeneration += electricalData.getQgeneration();
    this.PhaseAvoltage += electricalData.getPhaseAvoltage();
    this.PhaseBvoltage += electricalData.getPhaseBvoltage();
    this.PhaseCvoltage += electricalData.getPhaseCvoltage();

  }

  /**
   Deserialize the message.

   @param bytes - an array of bytes
   @return the deserialized {@code Message}
   @throws Exception - if an exception occurs.
   */
  @Override
  public Object deserialize(final byte[] bytes) throws Exception {
    try (ByteArrayInputStream b = new ByteArrayInputStream(bytes)) {
      try (ObjectInput o = new ObjectInputStream(b)) {
        return o.readObject();
      }
    }
  }

  /**
   @return the load for all phases combined
   */
  @Override
  public double getAllPhase_Pgrid() {
    return Pgeneration - (PhaseAPload + PhaseBPload + PhaseCPload);
  }

  /**
   @return the load for all phases combined
   */
  @Override
  public double getAllPhase_Pload() {
    return PhaseAPload + PhaseBPload + PhaseCPload;
  }

  /**
   @return the load for allConnectiallbined
   */
  @Override
  public double getAllPhase_Qload() {
    return PhaseAQload + PhaseBQload + PhaseCQload;
  }

  /**
   @return the Pgeneration
   */
  @Override
  public double getPgeneration() {
    return Pgeneration;
  }

  /**
   @param Pgeneration the Pgeneration to set
   */
  @Override
  public synchronized void setPgeneration(double Pgeneration) {
    this.Pgeneration = Pgeneration;
  }

  /**
   @return the PhaseAPload
   */
  @Override
  public double getPhaseAPload() {
    return PhaseAPload;
  }

  /**
   @param PhaseAPload the PhaseAPload to set
   */
  @Override
  public synchronized void setPhaseAPload(double PhaseAPload) {
    this.PhaseAPload = PhaseAPload;
  }

  /**
   @return the PhaseAQload
   */
  @Override
  public double getPhaseAQload() {
    return PhaseAQload;
  }

  /**
   @param PhaseAQload the PhaseAQload to set
   */
  @Override
  public synchronized void setPhaseAQload(double PhaseAQload) {
    this.PhaseAQload = PhaseAQload;
  }

  /**
   @return the PhaseAvoltage
   */
  @Override
  public double getPhaseAvoltage() {
    return this.PhaseAvoltage;
  }

  /**
   @param PhaseAvoltage the PhaseAvoltage to set
   */
  @Override
  public synchronized void setPhaseAvoltage(double PhaseAvoltage) {
    this.PhaseAvoltage = PhaseAvoltage;
  }

  /**
   @return the PhaseBPload
   */
  @Override
  public double getPhaseBPload() {
    return this.PhaseBPload;
  }

  /**
   @param PhaseBPload the PhaseBPload to set
   */
  @Override
  public synchronized void setPhaseBPload(double PhaseBPload) {
    this.PhaseBPload = PhaseBPload;
  }

  /**
   @return the PhaseBQload
   */
  @Override
  public double getPhaseBQload() {
    return this.PhaseBQload;
  }

  /**
   @param PhaseBQload the PhaseBQload to set
   */
  @Override
  public synchronized void setPhaseBQload(double PhaseBQload) {
    this.PhaseBQload = PhaseBQload;
  }

  /**
   @return the PhaseBvoltage
   */
  @Override
  public double getPhaseBvoltage() {
    return this.PhaseBvoltage;
  }

  /**
   @param PhaseBvoltage the PhaseBvoltage to set
   */
  @Override
  public synchronized void setPhaseBvoltage(double PhaseBvoltage) {
    this.PhaseBvoltage = PhaseBvoltage;
  }

  /**
   @return the PhaseCPload
   */
  @Override
  public double getPhaseCPload() {
    return this.PhaseCPload;
  }

  /**
   @param PhaseCPload the PhaseCPload to set
   */
  @Override
  public synchronized void setPhaseCPload(double PhaseCPload) {
    this.PhaseCPload = PhaseCPload;
  }

  /**
   @return the PhaseCQload
   */
  @Override
  public double getPhaseCQload() {
    return this.PhaseCQload;
  }

  /**
   @param PhaseCQload the PhaseCQload to set
   */
  @Override
  public synchronized void setPhaseCQload(double PhaseCQload) {
    this.PhaseCQload = PhaseCQload;
  }

  /**
   @return the PhaseCvoltage
   */
  @Override
  public double getPhaseCvoltage() {
    return this.PhaseCvoltage;
  }

  /**
   @param PhaseCvoltage the PhaseCvoltage to set
   */
  @Override
  public synchronized void setPhaseCvoltage(double PhaseCvoltage) {
    this.PhaseCvoltage = PhaseCvoltage;
  }

  /**
   @return the Qgeneration
   */
  @Override
  public double getQgeneration() {
    return this.Qgeneration;
  }

  /**
   @param Qgeneration the Qgeneration to set
   */
  @Override
  public synchronized void setQgeneration(double Qgeneration) {
    this.Qgeneration = Qgeneration;
  }

  /**
   @return the Qgeneration_calculated
   */
  @Override
  public double getQgeneration_calculated() {
    return this.Qgeneration_calculated;
  }

  /**
   @param Qgeneration_calculated the Qgeneration_calculated to set
   */
  @Override
  public synchronized void setQgeneration_calculated(double Qgeneration_calculated) {
    this.Qgeneration_calculated = Qgeneration_calculated;
  }

  @Override
  public boolean isEmpty() {
    boolean isEmpty = true;
    if (this.getPhaseAPload() != 0.0 || this.getPhaseBPload() != 0.0 || this.getPhaseCPload() != 0.0 ||
            this.getPhaseAvoltage() != 0.0 || this.getPhaseBvoltage() != 0.0 || this.getPhaseCvoltage() != 0.0)
      isEmpty = false;
    return isEmpty;
  }

  private void readObject(java.io.ObjectInputStream stream) throws java.io.IOException, ClassNotFoundException {
    stream.defaultReadObject();
  }

  /**
   Serialize the message.

   @return a byte array with the contents.
   @throws IOException - If an I/O error occurs.
   */
  @Override
  public byte[] serialize() throws IOException {
    try (ByteArrayOutputStream b = new ByteArrayOutputStream()) {
      try (ObjectOutput o = new ObjectOutputStream(b)) {
        o.writeObject(this);
      }
      return b.toByteArray();
    }
  }

  @Override
  public String toString() {
    return "ElectricalData{" +
            "PhaseAPload=" + PhaseAPload +
            ", PhaseAQload=" + PhaseAQload +
            ", PhaseBPload=" + PhaseBPload +
            ", PhaseBQload=" + PhaseBQload +
            ", PhaseCPload=" + PhaseCPload +
            ", PhaseCQload=" + PhaseCQload +
            ", Pgeneration=" + Pgeneration +
            ", Qgeneration=" + Qgeneration +
            ", Qgeneration_calculated=" + Qgeneration_calculated +
            ", PhaseAvoltage=" + PhaseAvoltage +
            ", PhaseBvoltage=" + PhaseBvoltage +
            ", PhaseCvoltage=" + PhaseCvoltage +
            '}';
  }

  private void writeObject(java.io.ObjectOutputStream stream) throws java.io.IOException {
    stream.defaultWriteObject();
  }

}
