/**
 * MatLabRemoteClient.java
 *
 * Copyright 2012 Kansas State University MACR Laboratory
 * http://macr.cis.ksu.edu/ Department of Computing & Information Sciences
 *
 * See License.txt file for the license agreement.
 *
 */
package edu.ksu.cis.macr.ipds.simulators.smartmeter.process;

import edu.ksu.cis.macr.ipds.simulators.smartmeter.model.SmartMeterSimulatorData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 The {@code MatLabRemoteClient} Used to send and receive data from MatLab Server Program.

 @author Matt Brown */
public class MatLabRemoteClient {

  private static final Logger LOG = LoggerFactory
          .getLogger(MatLabRemoteClient.class);
  private final int portNo;
  private final String address;
  private Socket connection = null;
  private PrintWriter out;

  /*
   * Port number and IP address of remote server.
   */
  public MatLabRemoteClient(int port, String add) {

    this.portNo = port;
    this.address = add;
  }

  /*
   * Receive DataSets from MatLab remote server
   */
  public List<SmartMeterSimulatorData> receiveData(int count) throws IOException, ClassNotFoundException {

    connection = new Socket(address, portNo);
    out = new PrintWriter(connection.getOutputStream());

    LOG.debug("SENDING - MatLabRemoteClient");
    out.println("RECEIVE");
    out.println(count);
    out.flush();

    List<SmartMeterSimulatorData> data = new ArrayList<>();
    InputStream is = connection.getInputStream();
    ObjectInputStream ois = new ObjectInputStream(is);

    SmartMeterSimulatorData ds = null;
    for (int i = 0; i < count; i++) {
      ds = (SmartMeterSimulatorData) ois.readObject();
      LOG.debug("{} received!", ds.toString());
      data.add(ds);
    }

    ois.close();
    is.close();
    connection.close();

    return data;
  }

  /*
   * Send a .csv data file to the server.
   */
  public synchronized void sendData(File file, String meterName) throws IOException {

    connection = new Socket(address, portNo);
    out = new PrintWriter(connection.getOutputStream());

    out.println("SEND");
    out.println(meterName);
    out.println(file.toString());
    out.flush();

    byte[] bfile = new byte[64 * 1024];
    FileInputStream fis = new FileInputStream(file);

    OutputStream os = connection.getOutputStream();

    int bytesRead = 0;
    long totalSent = 0;

    while ((bytesRead = fis.read(bfile)) != -1) {
      if (bytesRead > 0) {
        os.write(bfile, 0, bytesRead);
      }
    }
    fis.close();
    connection.close();
    connection = null;
  }
}
