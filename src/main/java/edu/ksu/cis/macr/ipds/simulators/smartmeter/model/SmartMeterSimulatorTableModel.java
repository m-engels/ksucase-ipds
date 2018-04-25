package edu.ksu.cis.macr.ipds.simulators.smartmeter.model;

import edu.ksu.cis.macr.ipds.primary.sensors.smartmeter.IElectricalData;
import edu.ksu.cis.macr.ipds.primary.sensors.smartmeter.ISmartMeterRead;
import edu.ksu.cis.macr.obaa_pp.sensor.IRead;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;

/**
 *
 */
public class SmartMeterSimulatorTableModel extends AbstractTableModel {

  private static final Logger LOG = LoggerFactory.getLogger(SmartMeterSimulatorTableModel.class);

  private static final long serialVersionUID = 1L;
  private String[] columnNames = {"RowID", "NodeLauncher",
          "P(load) A",
          "Q(load) A",
          "P(load) B",
          "Q(load) B",
          "P(load) C",
          "Q(load) C",
          "P(gen)",
          "Q(gen)",
          "Voltage A",
          "Voltage B",
          "Voltage C"};
  private ArrayList<IRead<?>> allReadings = new ArrayList<>();
  private Object[][] data = {
          {1, "S1", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {2, "F2", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {3, "F3", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {4, "F4", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {5, "F5", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {6, "F6", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {7, "F7", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {8, "F8", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {9, "F9", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {10, "F10", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {11, "F11", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {12, "F12", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {13, "F13", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {14, "F14", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {15, "F15", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {16, "F16", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {17, "F17", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {18, "F18", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {19, "F19", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {20, "F20", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {21, "F21", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {22, "F22", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {23, "F23", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {24, "F24", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {25, "F25", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {26, "F26", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {27, "F27", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {28, "F28", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {29, "F29", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {30, "F30", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {31, "F31", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {32, "F32", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {33, "F33", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {34, "F34", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {35, "F35", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {36, "F36", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {37, "F37", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {38, "F38", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {39, "L39", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {40, "L40", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {41, "L41", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {42, "L42", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {43, "N43", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {44, "H44", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {45, "H45", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {46, "H46", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {47, "H47", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {48, "N48", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {49, "H49", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {50, "H50", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {51, "H51", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {52, "H52", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {53, "N53", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {54, "H54", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {55, "H55", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {56, "H56", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {57, "H57", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {58, "N58", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {59, "H59", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {60, "H60", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {61, "H61", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0},
          {62, "H62", (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0, (double) 0}
  };

  public SmartMeterSimulatorTableModel() {
  }

  public SmartMeterSimulatorTableModel(ArrayList<IRead<?>> inputData) {
    allReadings = (ArrayList<IRead<?>>) inputData.clone();
    if (allReadings == null || allReadings.isEmpty()) {
      LOG.error("The data table is empty. ");
      System.exit(-1);
    }
  }

  @Override
  public Class getColumnClass(int c) {
    return getValueAt(0, c).getClass();
  }

  @Override
  public int getColumnCount() {
    return columnNames.length;
  }

  @Override
  public String getColumnName(int col) {
    return columnNames[col];
  }

  @Override
  public int getRowCount() {
    //   return data.length;
    return this.allReadings.size();
  }

  @Override
  public Object getValueAt(int row, int col) {
    int iRow = row + 1;
    if (col == 0) {
      return row;
    }
    if (col == 1) {
      if (iRow < 2) {
        return "S" + String.valueOf(iRow);
      } else if (iRow <= 38) {
        return "F" + String.valueOf(iRow);
      } else if (iRow <= 42) {
        return "L" + String.valueOf(iRow);
      } else if (iRow == 43 || iRow == 48 || iRow == 53 || iRow == 58) {
        return "N" + String.valueOf(iRow);
      } else {
        return "H" + String.valueOf(iRow);
      }
    } else {
      IRead<?> reading = allReadings.get(row);
      ISmartMeterRead r = (ISmartMeterRead) reading.getSensorReadObject();
      IElectricalData d = r.getElectricalData();
      int iColArray = col - 2;
      switch (iColArray) {
        case 0:
          return d.getPhaseAPload();
        case 1:
          return d.getPhaseAQload();
        case 2:
          return d.getPhaseBPload();
        case 3:
          return d.getPhaseBQload();
        case 4:
          return d.getPhaseCPload();
        case 5:
          return d.getPhaseCQload();
        case 6:
          return d.getPgeneration();
        case 7:
          return d.getQgeneration();
        case 8:
          return d.getPhaseAvoltage();
        case 9:
          return d.getPhaseBvoltage();
        case 10:
          return d.getPhaseCvoltage();
        default:
          return 0.0;
      }
    }
  }

  /*
   * We're not editable yet - kept though incase we want to test Q values.
   */
  @Override
  public boolean isCellEditable(int row, int col) {
    //Note that the data/cell address is constant,
    //no matter where the cell appears onscreen.
    return col >= 2;
  }

  /*
   * Only needed if data can change.
   */
  @Override
  public synchronized void setValueAt(Object value, int row, int col) {
    data[row][col] = value;
    fireTableCellUpdated(row, col);
  }
}
