/**
 *
 * Copyright 2012 
 * Kansas State University MACR Laboratory http://macr.cis.ksu.edu/
 * Department of Computing & Information Sciences
 *
 * See License.txt file for the license agreement. 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package edu.ksu.cis.macr.ipds.primary.messages;


import edu.ksu.cis.macr.ipds.primary.sensors.smartmeter.ElectricalData;
import edu.ksu.cis.macr.ipds.primary.sensors.smartmeter.IElectricalData;

import java.io.*;

/**
 The {@code PowerMessageContent} provides detailed information about a local prosumer agent's power needs and
 capabilities.
 */
public class PowerMessageContent implements Serializable, IPowerMessageContent {

  private static final long serialVersionUID = 1L;
  private double actualKW = 0.0;
  private double criticalActualKW = 0.0;
  private double maxKW = 0.0;
  private double minKW = 0.0;
  private double pvGeneration = 0.0;
  // TODO: find out how fast pv systems can power up
  private double pvGenerationRampRateKwPerMinute = 1.0;
  private EquipmentStatus pvStatus = EquipmentStatus.OUT_OF_SERVICE;
  private double ratedCapacityKW = 0.0;
  private long timeSlice = 0;
  private IElectricalData electricalData = null;
  private IElectricalData previousElectricalData = null;

  /**
   Constructs a new instance of {@code PowerMessageContent}.
   */
  private PowerMessageContent() {
    this.electricalData = new ElectricalData();
    this.previousElectricalData = new ElectricalData();
  }

  /**
   Constructs a new instance of {@code PowerMessageContent}.

   @param timeSlice - the current time slice.
   @param minKW - minimum power in KW
   @param maxKW - maximum power in KW
   @param actualKW - actual KW
   @param criticalKW - critical actual KW
   @param pvGeneration - solar generation in KW
   @param pvGenerationRampRateKwPerMinute - the pvGenerationRampRateKwPerMinute to set
   @param pvStatus - the pvStatus to set
   @param ratedCapacityKW - rated Capacity in kW
   */
  public PowerMessageContent(final long timeSlice, final double minKW, final double maxKW, final double actualKW, final double criticalKW,
                             final double pvGeneration, final double pvGenerationRampRateKwPerMinute,
                             final EquipmentStatus pvStatus, final double ratedCapacityKW) {
    this.timeSlice = timeSlice;
    this.maxKW = maxKW;
    this.minKW = minKW;
    this.actualKW = actualKW;
    this.criticalActualKW = criticalKW;
    this.pvGeneration = pvGeneration;
    this.pvGenerationRampRateKwPerMinute = pvGenerationRampRateKwPerMinute;
    this.pvStatus = pvStatus;
    this.ratedCapacityKW = ratedCapacityKW;

  }

  public static PowerMessageContent createPowerMessageContent() {
    return new PowerMessageContent();
  }

  @Override
  public synchronized void add(IPowerMessageContent item) {
    this.electricalData.add(item.getElectricalData());
    this.previousElectricalData.add(item.getPreviousElectricalData());

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
   @return the actualKW
   */
  @Override
  public double getActualKW() {
    return this.actualKW;
  }

  /**
   @param actualKW the actualKW to set
   */
  @Override
  public synchronized void setActualKW(final double actualKW) {
    this.actualKW = actualKW;
  }

  /**
   @return the criticalActualKW
   */
  @Override
  public double getCriticalActualKW() {
    return this.criticalActualKW;
  }

  /**
   @param criticalActualKW the criticalActualKW to set
   */
  @Override
  public synchronized void setCriticalActualKW(final double criticalActualKW) {
    this.criticalActualKW = criticalActualKW;
  }

  @Override
  public IElectricalData getElectricalData() {
    return electricalData;
  }

  @Override
  public synchronized void setElectricalData(IElectricalData electricalData) {
    this.electricalData = electricalData;
  }

  /**
   @return the maxKW
   */
  @Override
  public double getMaxKW() {
    return this.maxKW;
  }

  /**
   @param value - the max kw to set
   */
  @Override
  public synchronized void setMaxKW(final double value) {
    this.maxKW = value;

  }

  /**
   @return the minKW
   */
  @Override
  public double getMinKW() {
    return this.minKW;
  }

  /**
   @param value - the min kw to set
   */
  @Override
  public synchronized void setMinKW(final double value) {
    this.minKW = value;

  }

  @Override
  public IElectricalData getPreviousElectricalData() {
    return previousElectricalData;
  }

  @Override
  public synchronized void setPreviousElectricalData(IElectricalData previousElectricalData) {
    this.previousElectricalData = previousElectricalData;
  }

  /**
   @return the pvGeneration
   */
  @Override
  public double getPvGeneration() {
    return this.pvGeneration;
  }

  /**
   @param pvGeneration the pvGeneration to set
   */
  @Override
  public synchronized void setPvGeneration(final double pvGeneration) {
    this.pvGeneration = pvGeneration;
  }

  /**
   @return the pvGenerationRampRateKwPerMinute
   */
  @Override
  public double getPvGenerationRampRateKwPerMinute() {
    return this.pvGenerationRampRateKwPerMinute;
  }

  /**
   @param pvGenerationRampRateKwPerMinute the pvGenerationRampRateKwPerMinute to set
   */
  @Override
  public synchronized void setPvGenerationRampRateKwPerMinute(
          final double pvGenerationRampRateKwPerMinute) {
    this.pvGenerationRampRateKwPerMinute = pvGenerationRampRateKwPerMinute;
  }

  /**
   @return the pvStatus
   */
  @Override
  public EquipmentStatus getPvStatus() {
    return this.pvStatus;
  }

  /**
   @param pvStatus the pvStatus to set
   */
  @Override
  public synchronized void setPvStatus(final EquipmentStatus pvStatus) {
    this.pvStatus = pvStatus;
  }

  /**
   @return the ratedCapacityKW
   */
  @Override
  public double getRatedCapacityKW() {
    return this.ratedCapacityKW;
  }

  /**
   @param ratedCapacityKW the ratedCapacityKW to set
   */
  @Override
  public synchronized void setRatedCapacityKW(final double ratedCapacityKW) {
    this.ratedCapacityKW = ratedCapacityKW;
  }

  @Override
  public long getTimeSlice() {
    return timeSlice;
  }

  @Override
  public synchronized void setTimeSlice(long timeSlice) {
    this.timeSlice = timeSlice;
  }

  @Override
  public boolean isEmpty() {
    return this.getElectricalData().isEmpty();
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
    return "PowerMessageContent{" +
            "actualKW=" + actualKW +
            ", criticalActualKW=" + criticalActualKW +
            ", maxKW=" + maxKW +
            ", minKW=" + minKW +
            ", pvGeneration=" + pvGeneration +
            ", pvGenerationRampRateKwPerMinute=" + pvGenerationRampRateKwPerMinute +
            ", pvStatus=" + pvStatus +
            ", ratedCapacityKW=" + ratedCapacityKW +
            ", timeSlice=" + timeSlice +
            ", electricalData=" + electricalData +
            ", previousElectricalData=" + previousElectricalData +
            '}';
  }

  private void writeObject(java.io.ObjectOutputStream stream) throws java.io.IOException {
    stream.defaultWriteObject();
  }

  /**
   The {@code EquipmentStatus} indicates the availability of a piece of equipment.
   */
  public enum EquipmentStatus {

    /**
     not currently in use, but can be brought on for service
     */
    AVAILABLE,

    /**
     online and available
     */
    OPERATING,

    /**
     out of service and not available at this time
     */
    OUT_OF_SERVICE
  }

}
