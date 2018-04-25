package edu.ksu.cis.macr.ipds.market.capabilities.connect;

import edu.ksu.cis.macr.organization.model.InstanceGoal;

/**
 * The
 */
public interface IConnectUp {

    void connectUp(InstanceGoal<?> instanceGoal);

    boolean checkUpConnections(InstanceGoal<?> instanceGoal);

    boolean checkUpConnections();
}
