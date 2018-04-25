/**
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
package edu.ksu.cis.macr.ipds.primary.guidelines;

import java.io.Serializable;

public class AgentGuidelines implements Serializable, IAgentGuidelines {
  private static final long serialVersionUID = 1L;
  private double maxVoltageMultiplier = 0.95;
  private double minVoltageMultiplier = 1.05;
  private double powerFactor = 0.75;
  private double netDeltaP = 0.0;
  private double constantInelasticLoad_kw = 0.0;
  private double constantInelasticLoad_fraction = 0.0;

  /**
   Construct new {@code}HomeGuidelines}.

   @param minVoltageMultiplier - the minimum voltage multiplier for the local organization, e.g. 0.95.
   @param maxVoltageMultiplier - the maximum voltage multiplier for the local organization, e.g. 1.05.
   */
  private AgentGuidelines(double minVoltageMultiplier, double maxVoltageMultiplier,
                          double constantInelasticLoad_fraction) {
    this.setMinVoltageMultiplier(minVoltageMultiplier);
    this.setMaxVoltageMultiplier(maxVoltageMultiplier);
    this.setConstantInelasticLoad_fraction(constantInelasticLoad_fraction);

  }

  /**
   Construct new new {@code}HomeGuidelines} with default values. minVoltageMultiplier = 0.95. maxVoltageMultiplier =
   1.05. power factor = 0.75.
   */
  public AgentGuidelines() {
    powerFactor = 0.75;
    minVoltageMultiplier = 0.95;
    maxVoltageMultiplier = 1.05;
    netDeltaP = 0.0;

  }

  public static IAgentGuidelines createHomeGuidelines(double minVoltageMultiplier, double maxVoltageMultiplier,
                                                    double constantInelasticLoad_fraction) {
    return new AgentGuidelines(minVoltageMultiplier, maxVoltageMultiplier, constantInelasticLoad_fraction);
  }


  @Override
  public double getConstantInelasticLoad_fraction() {
    return this.constantInelasticLoad_fraction;
  }

  @Override
  public synchronized void setConstantInelasticLoad_fraction(double constantInelasticLoad_fraction) {
    this.constantInelasticLoad_fraction = constantInelasticLoad_fraction;
  }

  @Override
  public double getMaxVoltageMultiplier() {
    return this.maxVoltageMultiplier;
  }

  @Override
  public synchronized void setMaxVoltageMultiplier(double maxVoltageMultiplier) {
    this.maxVoltageMultiplier = maxVoltageMultiplier;
  }

  @Override
  public double getConstantInelasticLoad_kw() {
    return this.constantInelasticLoad_kw;
  }

  @Override
  public synchronized void setConstantInelasticLoad_kw(double constantInelasticLoad_kw) {
    this.constantInelasticLoad_kw = constantInelasticLoad_kw;
  }

  @Override
  public double getMinVoltageMultiplier() {
    return this.minVoltageMultiplier;
  }

  @Override
  public synchronized void setMinVoltageMultiplier(double minVoltageMultiplier) {
    this.minVoltageMultiplier = minVoltageMultiplier;
  }

  @Override
  public double getNetDeltaP() {
    return netDeltaP;
  }

  @Override
  public synchronized void setNetDeltaP(double netDeltaP) {
    this.netDeltaP = netDeltaP;
  }

  @Override
  public double getPowerFactor() {
    return this.powerFactor;
  }

  @Override
  public synchronized void setPowerFactor(final double powerFactor) {
    this.powerFactor = powerFactor;
  }


  @Override
  public String toString() {
    return "Guidelines{" +
            "maxVoltageMultiplier=" + maxVoltageMultiplier +
            ", minVoltageMultiplier=" + minVoltageMultiplier +
            ", powerFactor=" + powerFactor +
            ", netDeltaP=" + netDeltaP +
            ", constantInelasticLoad_kw=" + constantInelasticLoad_kw +
            ", constantInelasticLoad_fraction=" + constantInelasticLoad_fraction +
            '}';
  }
}
