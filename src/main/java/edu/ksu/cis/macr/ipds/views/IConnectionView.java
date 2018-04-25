package edu.ksu.cis.macr.ipds.views;

import java.awt.*;
import java.util.GregorianCalendar;

/**
 * Interface for the connection view.
 */
public interface IConnectionView {

    Component getComponentByName(String name);

    String getConnectionList();

    void setConnectionList(String connList);

    GregorianCalendar getSimulationTime();

    void setSimulationTime(GregorianCalendar cal);

    int getSimulationTimeSlices();

    void setSimulationTimeSlices(int simulationTimeSlices);

    void initializeAndDisplay(String testCaseName);

    void startListener();

    void updateData();

    void updateView(GregorianCalendar simulationTime, int simulationTimeSlices, String connList);
}
