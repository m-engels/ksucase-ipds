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


import edu.ksu.cis.macr.aasis.agent.persona.AbstractOrganizationCapability;
import edu.ksu.cis.macr.aasis.agent.persona.ICapability;
import edu.ksu.cis.macr.aasis.agent.persona.IOrganization;
import edu.ksu.cis.macr.aasis.agent.persona.IPersona;
import edu.ksu.cis.macr.aasis.simulator.clock.Clock;
import edu.ksu.cis.macr.obaa_pp.objects.IDisplayInformation;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

/**
 The {@code DateTimeCapability} provides the ability to access to the system time.  An agent must know the correct
 day of week and time of day in order to make good predictions and manage the system.  During simulations, the date
 time capability will act like a synchronized timer available to each distributed agent. The agent will provide the
 current time when requesting a simulated sensor reading or providing a control action.  On startup, the capability
 will set and remember the simulation simulationStartTime time and the interval set for sensor data time slices.  The
 capability will increment the number of time slices as the simulation runs and will be able to provide both the number
 of time slices elapsed and the corresponding simulation "current date and time".
 */
public class DateTimeCapability extends AbstractOrganizationCapability implements ICapability {
  private static final Logger LOG = LoggerFactory.getLogger(DateTimeCapability.class);
  private static final boolean debug = false;
  private double interval_seconds = 60.0;
  private GregorianCalendar simulationTime = new GregorianCalendar();
    private final Map<UniqueIdentifier, Map<String, String>> capabilities = new HashMap<>();

  /**
   Construct a new {@code DateTimeCapability} instance.

   @param owner - the agent possessing this capability.
   @param org - the immediate organization in which this agent operates.
   */
  public DateTimeCapability(final IPersona owner, final IOrganization org) {
    super(DateTimeCapability.class, owner, org);
  //  super(new ClassIdentifier(DateTimeCapability.class));
  }

 // @Override
  public double getFailure() {
    return 0;
  }

  /**
   @return the length of a time slice interval in milliseconds.
   */
  private int getInterval_milliseconds() {
    return (int) (this.getInterval_seconds() * 1000.0);
  }

  /**
   @return the the length of a time slice interval in seconds.
   */
  public double getInterval_seconds() {
    return interval_seconds;
  }

  public int getMaxTimeSlices() {
    return Clock.getMaxTimeSlices();
  }



  /**
   @return the simulationStartTime
   */
  public GregorianCalendar getSimulationStartTime() {
    return Clock.getSimulationStartTime();
  }

  /**
   @return the simulationTime
   */
  public GregorianCalendar getSimulationTime() {
    return Clock.getSimulationTime();

  }

  /**
   @return - the integer number of time slices elapsed since the simulation began
   */
  public int getTimeSlice() {
    return Clock.getTimeSlicesElapsedSinceStart();
  }

  /**
   @return the the number of time slices elapsed since the simulation began.
   */
  public int getTimeSlicesElapsedSinceStart() {
    int numElapsed = Clock.getTimeSlicesElapsedSinceStart();
    int maxTimeslices = Clock.getMaxTimeSlices();

    if (maxTimeslices > 0 && numElapsed > maxTimeslices) {
      LOG.info("SIMULATION COMPLETE after {} timeslices.....................................", maxTimeslices);
      System.exit(0);
    }
    return numElapsed;
  }

  public synchronized void setTimeSlicesElapsedSinceStart(final int timeSlicesElapsedSinceStart) {
    Clock.setTimeSlicesElapsedSinceStart(timeSlicesElapsedSinceStart);
  }


  public static long incrementTimeSlice(){
     Clock.setTimeSlicesElapsedSinceStart(Clock.getTimeSlicesElapsedSinceStart()+1);
     return Clock.getTimeSlicesElapsedSinceStart();
  }


    /**
     * Returns the {@code DisplayInformation} object containing the information for the {@code ICapability}.
     *
     * @param displayInformation the data display.
     */
    @Override
    public synchronized void populateCapabilitiesOfDisplayObject(IDisplayInformation displayInformation) {
        super.populateCapabilitiesOfDisplayObject(displayInformation);
    }

  @Override
  public synchronized void reset() {

  }

  /**
   Returns the {@code DOM} {@code Element} of the {@code IAttributable} or {@code ICapability}.  This method should be
   overwritten by subclasses if there are additional variables defined whose values should be saved if they affect the
   state of the object.  Overwritting can be done in two ways: adding additional information to the {@code Element}
   returned by the super class, or creating a completely new element from scratch.

   @param document the document in which to create the {@code DOM} {@code Element}s.
   @return the {@code DOM} {@code Element} of the {@code IAttributable} or {@code ICapability}.
   */
  @Override
  public Element toElement(Document document) {
    return null;
  }

  @Override
  public String toString() {
      double planningHorizon_minutes = 15.0;
      return "DateTimeCapability{" +
            ", interval_seconds=" + interval_seconds +
            ", simulationTime=" + simulationTime +
            ", planningHorizon_minutes=" + planningHorizon_minutes +
            '}';
  }
}
