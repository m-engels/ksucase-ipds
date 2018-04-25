package edu.ksu.cis.macr.ipds.primary.sensors.smartmeter;

import java.io.IOException;

/**
 {@code IElectricalData} provides an interface describing a single set of electrical data sensed by the system. It
 includes information about power load, generation, and voltages.
 */
public interface IElectricalData {

  void add(IElectricalData electricalData);

  Object deserialize(byte[] bytes) throws Exception;

  /**
   @return the load for all phases combined
   */
  double getAllPhase_Pgrid();

  /**
   @return the load for all phases combined
   */
  double getAllPhase_Pload();

  /**
   @return the load for allConnectiallbined
   */
  double getAllPhase_Qload();

  /**
   @return the Pgeneration
   */
  double getPgeneration();

  /**
   @param Pgeneration the Pgeneration to set
   */
  void setPgeneration(double Pgeneration);

  /**
   @return the PhaseAPload
   */
  double getPhaseAPload();

  /**
   @param PhaseAPload the PhaseAPload to set
   */
  void setPhaseAPload(double PhaseAPload);

  /**
   @return the PhaseAQload
   */
  double getPhaseAQload();

  /**
   @param PhaseAQload the PhaseAQload to set
   */
  void setPhaseAQload(double PhaseAQload);

  /**
   @return the PhaseAvoltage
   */
  double getPhaseAvoltage();

  /**
   @param PhaseAvoltage the PhaseAvoltage to set
   */
  void setPhaseAvoltage(double PhaseAvoltage);

  /**
   @return the PhaseBPload
   */
  double getPhaseBPload();

  /**
   @param PhaseBPload the PhaseBPload to set
   */
  void setPhaseBPload(double PhaseBPload);

  /**
   @return the PhaseBQload
   */
  double getPhaseBQload();

  /**
   @param PhaseBQload the PhaseBQload to set
   */
  void setPhaseBQload(double PhaseBQload);

  /**
   @return the PhaseBvoltage
   */
  double getPhaseBvoltage();

  /**
   @param PhaseBvoltage the PhaseBvoltage to set
   */
  void setPhaseBvoltage(double PhaseBvoltage);

  /**
   @return the PhaseCPload
   */
  double getPhaseCPload();

  /**
   @param PhaseCPload the PhaseCPload to set
   */
  void setPhaseCPload(double PhaseCPload);

  /**
   @return the PhaseCQload
   */
  double getPhaseCQload();

  /**
   @param PhaseCQload the PhaseCQload to set
   */
  void setPhaseCQload(double PhaseCQload);

  /**
   @return the PhaseCvoltage
   */
  double getPhaseCvoltage();

  /**
   @param PhaseCvoltage the PhaseCvoltage to set
   */
  void setPhaseCvoltage(double PhaseCvoltage);

  /**
   @return the Qgeneration
   */
  double getQgeneration();

  /**
   @param Qgeneration the Qgeneration to set
   */
  void setQgeneration(double Qgeneration);

  /**
   @return the Qgeneration_calculated
   */
  double getQgeneration_calculated();

  /**
   @param Qgeneration_calculated the Qgeneration_calculated to set
   */
  void setQgeneration_calculated(double Qgeneration_calculated);

  boolean isEmpty();

  byte[] serialize() throws IOException;
}
