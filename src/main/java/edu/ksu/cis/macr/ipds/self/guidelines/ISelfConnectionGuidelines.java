package edu.ksu.cis.macr.ipds.self.guidelines;

import edu.ksu.cis.macr.aasis.common.IConnectionGuidelines;

/**
 {@code ISelfConnectionGuidelines} provides an interface for describing a single agent-to-agent connection in an
 intelligent power distribution system.
 */
public interface ISelfConnectionGuidelines extends IConnectionGuidelines {

  double getCombinedKW();

  String getOrganizationLevel();

  void setOrganizationLevel(String organizationIPDSLevel);

}
