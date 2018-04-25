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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class NeighborhoodGuidelines implements Serializable, INeighborhoodGuidelines {
  private static final Logger LOG = LoggerFactory.getLogger(NeighborhoodGuidelines.class);
  private static final boolean debug = false;
  private static final long serialVersionUID = 1L;
  private double maxVoltageMultiplier;
  private double minVoltageMultiplier;
  private double minKW;
  private double maxKW;
  private double netDeltaP;

  /**
   Construct new {@code}HomeGuidelines}.

   @param minVoltageMultiplier - the minimum voltage multiplier for the local organization, e.g. 0.95.
   @param maxVoltageMultiplier - the maximum voltage multiplier for the local organization, e.g. 1.05.
   @param minKW - the minimum power in KiloWatts that is set for the guideline.
   @param maxKW - the maximum power in KiloWatts tha tis set for the guideline.
   */
  public NeighborhoodGuidelines(double minVoltageMultiplier, double maxVoltageMultiplier,
                                double minKW, double maxKW) {
    this.setMinVoltageMultiplier(minVoltageMultiplier);
    this.setMaxVoltageMultiplier(maxVoltageMultiplier);
    this.setMinKW(minKW);
    this.setMaxKW(maxKW);
    if (debug) LOG.debug("New guidelines created. {}", this.toString());
  }

  /**
   Construct new new {@code}HomeGuidelines} with default values. minVoltageMultiplier = 0.95. maxVoltageMultiplier =
   1.05.
   */
  public NeighborhoodGuidelines() {
    minVoltageMultiplier = 0.95;
    maxVoltageMultiplier = 1.05;
    if (debug) LOG.info("New guidelines created. {}", this.toString());
  }

  @Override
  public double getMaxKW() {
    return this.maxKW;
  }

  @Override
  public synchronized void setMaxKW(double kw) {
    this.maxKW = kw;
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
  public double getMinKW() {
    return this.minKW;
  }

  @Override
  public synchronized void setMinKW(double kw) {
    this.minKW = kw;
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
  public String toString() {
    return "NeighborhoodGuidelines{" +
            "maxVoltageMultiplier=" + maxVoltageMultiplier +
            ", minVoltageMultiplier=" + minVoltageMultiplier +
            ", netDeltaP=" + netDeltaP +
            ", minKW=" + minKW +
            ", maxKW=" + maxKW +
            '}';
  }
}
