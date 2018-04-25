/**
 *
 * Copyright 2012 Kansas State University MACR Laboratory
 * http://macr.cis.ksu.edu/ Department of Computing & Information Sciences
 *
 * See License.txt file for the license agreement.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package edu.ksu.cis.macr.ipds.primary.capabilities.manage;

//import edu.ksu.cis.macr.aasis.agent.ec_cap.BaseAbstractOrganizationCapability;

import edu.ksu.cis.macr.aasis.agent.persona.AbstractOrganizationCapability;
import edu.ksu.cis.macr.aasis.agent.persona.IOrganization;
import edu.ksu.cis.macr.aasis.agent.persona.IPersona;
import edu.ksu.cis.macr.aasis.agent.views.SmartMeterSensorsFrame;
import edu.ksu.cis.macr.aasis.simulators.PhysicalSystemSimulator;
import edu.ksu.cis.macr.goal.model.InstanceParameters;
import edu.ksu.cis.macr.ipds.primary.sensors.SensorType;
import edu.ksu.cis.macr.ipds.primary.sensors.smartmeter.ISmartMeterRead;
import edu.ksu.cis.macr.obaa_pp.objects.IDisplayInformation;
import edu.ksu.cis.macr.obaa_pp.sensor.IRead;
import edu.ksu.cis.macr.obaa_pp.sensor.ISensor;
import edu.ksu.cis.macr.obaa_pp.sensor.SensorUnavailableException;
import edu.ksu.cis.macr.obaa_pp.sensor.UnknownSensorException;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.*;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 {@code SmartMeterCapability} provides the ability to read a specific smart meter. Smart meters are assumed to know the total power used
 (in KW) and the two parts that combine to make this total: the amount generated (positive) or consumed by an associated
 photovoltaic (PV) system and the amount taken from (positive) or provided to the grid (negative).   Usage (kw) =
 grid load (kW) + solar generation (kW)  Smart meters may also provide voltage information.  The smart meter
 provides both a full set of electrical data from the previous time slice and a partial set of data for the current time
 slice.
 */
public class SmartMeterCapability extends AbstractOrganizationCapability implements ISensor {

  /**
   {@code ELEMENT_PARAMETER}
   */
  protected static final String ELEMENT_PARAMETER = "parameter";
  private static final Logger LOG = LoggerFactory.getLogger(SmartMeterCapability.class);
  private static final boolean debug = false;
  private double actualQualityMultiplier = 1.0;
  // Assumes constant critical load. Add to reading if critical load is variable.
  // Read criticalKW from Smart Inverter config file
  private double criticalKW = 0.2;
  private String smartMeterName;
  private ISmartMeterRead smartMeterRead = null;
    private long timeSliceLastRead = 0;
  private ISmartMeterRead lastSmartMeterRead = null;

  /**
   Construct a new {@code SmartMeterCapability} instance with the special attributes defined in agent.xml.

   @param owner - the agent possessing this capability.
   @param organization - the immediate organization in which this agent operates.
   @param smartMeterName - the unique string name of this smart meter.
   @param criticalKW - the constant critical load required at this site.
   */
  public SmartMeterCapability(
          final IPersona owner,
          final IOrganization organization, final String smartMeterName,
          final double criticalKW) {
    super(SmartMeterCapability.class, owner, organization);

    setSmartMeterName(smartMeterName);
    setCriticalKW(criticalKW);
  }

  public synchronized void createAndShowElectricalChart() {

    final SmartMeterSensorsFrame frame = new SmartMeterSensorsFrame();
    frame.setTitle(this.getExecutorAbbreviation());
    frame.startGrapher();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final SmartMeterCapability other = (SmartMeterCapability) obj;
    if (this.smartMeterName == null) {
      if (other.smartMeterName != null) {
        return false;
      }
    } else if (!this.smartMeterName.equals(other.smartMeterName)) {
      return false;
    }
    return true;
  }

  /**
   @return the actualQualityMultiplier
   */
  public double getActualQualityMultiplier() {
    return this.actualQualityMultiplier;
  }

  /**
   @param actualQualityMultiplier the actualQualityMultiplier to set
   */
  public synchronized void setActualQualityMultiplier(final double actualQualityMultiplier) {
    this.actualQualityMultiplier = actualQualityMultiplier;
  }

  /**
   @return the criticalKW
   */
  public double getCriticalKW() {
    return this.criticalKW;
  }

  /**
   @param criticalKW the criticalKW to set
   */
  public synchronized void setCriticalKW(final double criticalKW) {
    this.criticalKW = criticalKW;
  }

  @Override
  public double getFailure() {
    return 0;
  }

  public ISmartMeterRead getLastSmartMeterRead() {
    return lastSmartMeterRead;
  }

  public synchronized void setLastSmartMeterRead(ISmartMeterRead lastSmartMeterRead) {
    this.lastSmartMeterRead = lastSmartMeterRead;
  }

  /**
   @return - the unique string name of this specific smart meter.
   */
  public String getSmartMeterName() {
    return this.smartMeterName;
  }

  /**
   Set the smart meter name to the given String.

   @param value - the name to set
   */
  public synchronized void setSmartMeterName(final String value) {
    this.smartMeterName = value;
  }

  public ISmartMeterRead getSmartMeterRead(long timeSlice) {
    if (timeSlice == this.timeSliceLastRead) return this.getLastSmartMeterRead();

    if (debug) LOG.debug("getSmartMeterRead():  TS={} SM={}", timeSlice, this.smartMeterName);
    IRead<SensorType> reading = null;
    try {
      reading = (IRead<SensorType>) PhysicalSystemSimulator.getReadAt(this.smartMeterName, timeSlice);
    } catch (Exception ex) {
      LOG.error("Error getting smart sensor reading: {}", ex.getMessage());
      System.exit(-44);
    }
    if (reading == null) {
      LOG.error("getSmartMeterRead(): ERROR: Could not obtain PSS READ for TS={} SM={}", timeSlice,
              this.smartMeterName);
      System.exit(-55);
    }
    if (debug) LOG.debug("getSmartMeterRead(): OBTAINED PSS READ for TS={} SM={}", timeSlice,
            this.smartMeterName);
    this.smartMeterRead = (ISmartMeterRead) reading.getSensorReadObject();
    if (debug) LOG.debug("getSmartMeterRead(): READ for TS={} SM={} is {}", timeSlice,
            this.smartMeterName, smartMeterRead.toString());
      double genKW = smartMeterRead.getElectricalData().getPgeneration();
    // same as solar if no battery
    LOG.info("\tSMART METER READING at time slice {}: {}", timeSlice, reading.toString());

    // cache the data so we don't have to get it again
    this.lastSmartMeterRead = smartMeterRead;
    this.setTimeSliceLastRead(timeSlice);
    return smartMeterRead;
  }

  public double getTimeSlice() {
    return smartMeterRead.getTimeslice();
  }

  public long getTimeSliceLastRead() {
    return timeSliceLastRead;
  }

  public synchronized void setTimeSliceLastRead(long timeSliceLastRead) {
    this.timeSliceLastRead = timeSliceLastRead;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = (prime * result)
            + ((this.smartMeterName == null) ? 0 : this.smartMeterName
            .hashCode());
    return result;
  }

  /**
   Get the parameters from this instance goal and use them to initialize the capability.

   @param instanceGoal - this instance of the specification goal
   */
  public synchronized void initializeFromGoal(InstanceGoal<?> instanceGoal) {
    if (debug) LOG.debug("Initializing capability from goal: {}.", instanceGoal);
    // Get the parameter values from the existing active instance goal
    final InstanceParameters params = Objects
            .requireNonNull((InstanceParameters) instanceGoal
                    .getParameter());
    if (debug) LOG.debug("Initializing params: {}.", params);
  }

  public boolean isSameTimeSlice(long newTimeSlice) {
    boolean result = (newTimeSlice == this.getTimeSliceLastRead());
    if (debug)
      LOG.debug("Checking to see if time slice {} is the same as last one ({}). {}.", newTimeSlice, this.getTimeSliceLastRead(), result);
    return result;
  }

  @Override
  public synchronized void populateCapabilitiesOfDisplayObject(final IDisplayInformation displayInformation) {
    super.populateCapabilitiesOfDisplayObject(displayInformation);
    final Map<String, String> fields = displayInformation
            .getCapability(getIdentifier());
    fields.put("smartMeterName", this.smartMeterName);
    fields.put("criticalKW", Double.toString(this.criticalKW));
  }

  @Override
  public synchronized void reset() {

  }

  @Override
  public IRead<SensorType> sense() throws UnknownSensorException, SensorUnavailableException, IOException, ClassNotFoundException {
    if (debug) LOG.debug("Calling sense for {}", this.smartMeterName);
    return (IRead<SensorType>) PhysicalSystemSimulator.getRead(this.smartMeterName);
  }

  @Override
  public IRead<SensorType> senseAt(long timeSlice) throws IOException, ClassNotFoundException, SensorUnavailableException {
    if (debug) LOG.debug("Calling senseAt at timeSlice  {} for {}", timeSlice, this.smartMeterName);
    return (IRead<SensorType>) PhysicalSystemSimulator.getReadAt(this.smartMeterName, timeSlice);
  }

  @Override
  public Element toElement(final Document document) {
    final Element capability = super.toElement(document);
    Element parameter = (Element) capability.appendChild(document
            .createElement(ELEMENT_PARAMETER));
    parameter.setAttribute(ATTRIBUTE_TYPE,
            int.class.getSimpleName());
    parameter.appendChild(document.createTextNode(this.smartMeterName));

    parameter = (Element) capability.appendChild(document
            .createElement(ELEMENT_PARAMETER));
    parameter.setAttribute(ATTRIBUTE_TYPE,
            double.class.getSimpleName());
    parameter.appendChild(document.createTextNode(Double
            .toString(this.criticalKW)));
    return capability;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "SmartMeterCapability [actualQualityMultiplier="
            + this.actualQualityMultiplier + ", criticalKW="
            + this.criticalKW + ", smartMeterName=" + this.smartMeterName
            + "]";
  }

  public synchronized void updateSmartMeterView() {
    SmartMeterSensorsFrame.setPGen(this.smartMeterRead.getElectricalData().getPgeneration());
    SmartMeterSensorsFrame.setQGen(this.smartMeterRead.getElectricalData().getQgeneration());
    SmartMeterSensorsFrame.setPALoad(this.smartMeterRead.getElectricalData().getPhaseAPload());
    SmartMeterSensorsFrame.setQALoad(this.smartMeterRead.getElectricalData().getPhaseAQload());
    SmartMeterSensorsFrame.setPBLoad(this.smartMeterRead.getElectricalData().getPhaseBPload());
    SmartMeterSensorsFrame.setQBLoad(this.smartMeterRead.getElectricalData().getPhaseBQload());
    SmartMeterSensorsFrame.setPCLoad(this.smartMeterRead.getElectricalData().getPhaseCPload());
    SmartMeterSensorsFrame.setQCLoad(this.smartMeterRead.getElectricalData().getPhaseCQload());
    SmartMeterSensorsFrame.UpdateView();
  }
}
