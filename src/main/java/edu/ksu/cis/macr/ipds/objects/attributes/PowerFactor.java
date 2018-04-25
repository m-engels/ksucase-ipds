package edu.ksu.cis.macr.ipds.objects.attributes;

import edu.ksu.cis.macr.obaa_pp.objects.IAttribute;

import java.io.Serializable;


/**
 A {@code IAttribute} describing the power factor of from the neighborhood organization xml

 @author Greg Martin */


public class PowerFactor implements IAttribute, Serializable {

  private static final long serialVersionUID = 1L;
  private final double powerFactor;


  public PowerFactor() {
    this.powerFactor = 0.8;
  }

  /**
   @param powerFactor - power factor used in calculation
   */
  public PowerFactor(final double powerFactor) {
    this.powerFactor = powerFactor;
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof PowerFactor) {
      PowerFactor obj = (PowerFactor) object;
      return obj.getValue() == powerFactor;
    }
    return false;
  }

  /**
   @return critical load in kW
   */
  public double getValue() {
    return powerFactor;
  }

  @Override
  public String toString() {
    return String.format("%.2f", powerFactor);
  }

}
