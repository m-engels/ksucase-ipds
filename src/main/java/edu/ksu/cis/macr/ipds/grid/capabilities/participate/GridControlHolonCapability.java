/*
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
package edu.ksu.cis.macr.ipds.grid.capabilities.participate;

import com.rabbitmq.client.Channel;
import edu.ksu.cis.macr.aasis.agent.persona.AbstractOrganizationCapability;
import edu.ksu.cis.macr.aasis.agent.persona.IOrganization;
import edu.ksu.cis.macr.aasis.agent.persona.IPersona;
import edu.ksu.cis.macr.aasis.common.Connections;
import edu.ksu.cis.macr.aasis.common.IConnectionGuidelines;
import edu.ksu.cis.macr.aasis.common.IConnections;
import edu.ksu.cis.macr.aasis.messaging.IMessagingFocus;
import edu.ksu.cis.macr.goal.model.InstanceParameters;
import edu.ksu.cis.macr.ipds.grid.messaging.GridMessagingFocus;
import edu.ksu.cis.macr.ipds.grid.messaging.GridMessagingManager;
import edu.ksu.cis.macr.ipds.primary.guidelines.IFeederGuidelines;
import edu.ksu.cis.macr.ipds.primary.guidelines.IHomeGuidelines;
import edu.ksu.cis.macr.ipds.primary.guidelines.ILateralGuidelines;
import edu.ksu.cis.macr.ipds.primary.guidelines.INeighborhoodGuidelines;
import edu.ksu.cis.macr.ipds.primary.messages.IPowerMessageContent;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import edu.ksu.cis.macr.organization.model.identifiers.StringIdentifier;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 Provides the ability to act as a sub holon in a power distribution control organization.
 */
public class GridControlHolonCapability extends AbstractOrganizationCapability {

  private static final Logger LOG = LoggerFactory.getLogger(GridControlHolonCapability.class);
  private static final boolean debug = false;
    private static final String QUEUE_PURPOSE = "GRID";
    private static UniqueIdentifier myID;
    private final String COMMUNICATION_CHANNEL_ID = "PowerCommunicationChannel";
    private int NUMBER_OF_PARTICPANT_MESSAGES_TO_STORE = 1;
    private Map<InstanceGoal<?>, BlockingQueue<IPowerMessageContent>> allParticipantData;
    private double currentTotal;
    private double maxKW = 0.0;
    private double maxVoltageMultiplier = 0.0;
    private double minKW = 0.0;
    private double minVoltageMultiplier = 0.0;
    private double netDeltaP = 0.0;

  private IHomeGuidelines homeGuidelines = null;
  private INeighborhoodGuidelines neighborhoodGuidelines = null;
  private ILateralGuidelines lateralGuidelines = null;
  private IFeederGuidelines feederGuidelines = null;
    private static final IMessagingFocus messagingFocus = GridMessagingFocus.GRID;
    private static Channel channel ;
    private IConnections gridConnections = null;
    private IConnections parentConnections = null;

  /**
   @param owner - the entity to which this capability belongs
   @param organization - the organization which this capability belongs
   */
  public GridControlHolonCapability(final IPersona owner, final IOrganization organization) {
    super(GridControlHolonCapability.class, owner, organization);
      channel = GridMessagingManager.getChannel(messagingFocus);
  }

  @Override
  public double getFailure() {
    return 0;
  }

  public IFeederGuidelines getFeederGuidelines() {
    return feederGuidelines;
  }

  public synchronized void setFeederGuidelines(IFeederGuidelines feederGuidelines) {
    this.feederGuidelines = feederGuidelines;
  }

  public IHomeGuidelines getHomeGuidelines() {
    return homeGuidelines;
  }

  public synchronized void setHomeGuidelines(IHomeGuidelines homeGuidelines) {
    this.homeGuidelines = homeGuidelines;
  }

  public ILateralGuidelines getLateralGuidelines() {
    return lateralGuidelines;
  }

  public synchronized void setLateralGuidelines(ILateralGuidelines lateralGuidelines) {
    this.lateralGuidelines = lateralGuidelines;
  }

  public INeighborhoodGuidelines getNeighborhoodGuidelines() {
    return neighborhoodGuidelines;
  }

  public synchronized void setNeighborhoodGuidelines(INeighborhoodGuidelines neighborhoodGuidelines) {
    this.neighborhoodGuidelines = neighborhoodGuidelines;
  }

  public IConnections getParentConnections() {
    return parentConnections;
  }

  public synchronized void setParentConnections(IConnections parentConnections) {
    this.parentConnections = parentConnections;
  }

  /**
   Get the parameters from this instance goal and use them to set the goal-specific guidelines.

   @param instanceGoal - this instance of the specification goal
   */
  public synchronized void init(InstanceGoal<?> instanceGoal) {
      LOG.debug("Initializing capability from goal: {}.", instanceGoal);
      // Get the parameter values from the existing active instance goal
    final InstanceParameters params = (InstanceParameters) instanceGoal
            .getParameter();
    LOG.debug("Initializing params: {}.", params);

    final IHomeGuidelines hg = (IHomeGuidelines) params.getValue(StringIdentifier
            .getIdentifier("homeGuidelines"));
    this.setHomeGuidelines(hg);

    final INeighborhoodGuidelines ng = (INeighborhoodGuidelines) params.getValue(StringIdentifier
            .getIdentifier("neighborhoodGuidelines"));
    this.setNeighborhoodGuidelines(ng);

    final ILateralGuidelines lg = (ILateralGuidelines) params.getValue(StringIdentifier
            .getIdentifier("lateralGuidelines"));
    this.setLateralGuidelines(lg);

    final IFeederGuidelines fg = (IFeederGuidelines) params.getValue(StringIdentifier
            .getIdentifier("feederGuidelines"));
    this.setFeederGuidelines(fg);

      final IConnections gridConnections = (IConnections) params
              .getValue(StringIdentifier
                      .getIdentifier("gridConnections"));
      if (debug) LOG.debug("Initializing all grid connections: {}.", gridConnections);
      this.setAllConnections(gridConnections);
      if (this.gridConnections == null) {
           IConnections parentConnections = (IConnections) params.getValue(StringIdentifier.getIdentifier("parentConnections"));
          LOG.debug("Super-holon connections: {}.", parentConnections);
           this.setParentConnections(parentConnections);
      } else {
          this.setAllConnections(gridConnections);
          if (debug) LOG.debug("Connections to other agents: {}", gridConnections.getListConnectionGuidelines());
           IConnections parentConnections = new Connections(this.getAllParentConnections(gridConnections.getListConnectionGuidelines()), "parentConnections");
          LOG.debug("Super-holon connections: {}.", parentConnections);
           this.setParentConnections(parentConnections);
      }
      if (this.parentConnections != null) {
          LOG.debug("There are {} authorized connections to super holons.", parentConnections.getListConnectionGuidelines().size());
      }
  }
    private synchronized void setAllConnections(final IConnections gridConnections) {
        this.gridConnections = gridConnections;
    }

    private synchronized List<? extends IConnectionGuidelines> getAllParentConnections(List<? extends IConnectionGuidelines> lstAll) {
        List<IConnectionGuidelines> justParents =  new ArrayList<>();
        for (IConnectionGuidelines cg : lstAll)
        {
            if (cg.isConnectionToParent()){ justParents.add(cg);}
        }
        return justParents;
    }



  @Override
  public synchronized void reset() {

  }

  @Override
  public Element toElement(final Document document) {
    final Element capability = super.toElement(document);
    return capability;
  }


}
