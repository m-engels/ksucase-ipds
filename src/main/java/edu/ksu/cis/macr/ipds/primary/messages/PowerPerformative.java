package edu.ksu.cis.macr.ipds.primary.messages;

/**
 Performatives indicate a primary characteristic of the message and may be used to route behavior in plans.
 */
public enum PowerPerformative {

  /**
   The agent is operating within the provided guidelines and (later) expects to remain within bounds throughout the
   planning horizon.
   */
  REPORT_OK,

  /**
   The agent is operating (or expects to operate) in violation of one or more of its specified guidelines.
   */
  REPORT_OUT_OF_BOUNDS
}
