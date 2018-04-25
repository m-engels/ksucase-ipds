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
package edu.ksu.cis.macr.ipds.market.persona;


import edu.ksu.cis.macr.aasis.agent.persona.IOrganization;
import edu.ksu.cis.macr.aasis.spec.OrganizationFocus;
import edu.ksu.cis.macr.ipds.market.capabilities.participate.AuctionCommunicationCapability;
import edu.ksu.cis.macr.ipds.market.capabilities.participate.AuctionPowerCapability;
import edu.ksu.cis.macr.ipds.market.capabilities.participate.IAuctionCommunicationCapability;
import edu.ksu.cis.macr.ipds.market.capabilities.participate.IAuctionPowerCapability;
import edu.ksu.cis.macr.ipds.market.plan_selector.MarketPlanSelector;
import edu.ksu.cis.macr.ipds.primary.persona.Persona;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;


/**
 A persona for this affiliate organization. Uses organization-specific plan selector and organization-specific communication capabilities.
 */
public abstract class MarketPersona extends Persona {
    private static final Logger LOG = LoggerFactory.getLogger(MarketPersona.class);
    private static final Boolean debug =  false;
    protected IAuctionCommunicationCapability localAuctionCommunicationCapability;
    protected IAuctionPowerCapability actionPowerCapability;

    /**
     * Constructs a new instance of an agent using the organization-based agent architecture. Each prosumer agent will have
     * the capabilities needed to cc in a peer-based organization both as a peer and as a supervisor and contains its own
     * knowledge about the immediate organization in which it participates.
     *
     * @param org              the organization, containing information about agents and objects in the organization system.
     * @param identifierString a string containing a name that uniquely identifies this in the system.
     * @param knowledge        an XML specification of the organization.
     * @param focus            an Enum defining what focus the organization is.
     */
    public MarketPersona(final IOrganization org, final String identifierString, final Element knowledge, final OrganizationFocus focus) {
        super(org, identifierString, knowledge, focus);
        planSelector = new MarketPlanSelector();

        this.localAuctionCommunicationCapability = new AuctionCommunicationCapability(this, org);
        if (debug) LOG.debug("\t New AuctionCommunicationCapability={}", this.localAuctionCommunicationCapability);
        addCapability(localAuctionCommunicationCapability);
        if (debug) LOG.debug("\t Added AuctionCommunicationCapability={}.", this.localAuctionCommunicationCapability);

        this.actionPowerCapability = new AuctionPowerCapability(this, org);
        if (debug) LOG.debug("\t New actionPowerCapability={}", this.actionPowerCapability);
        addCapability(actionPowerCapability);
        if (debug) LOG.debug("\t Added actionPowerCapability={}.", this.actionPowerCapability);

        if (this.internalCommunicationCapability == null) {
            LOG.debug("ERROR: internalCommunicationCapability is null - Can't add auction communication capabilities.");
        } else {
            if (debug)
                LOG.debug("\tlocalAuctionCommunicationCapability.getCommunicationChannelID()={}.", localAuctionCommunicationCapability.getCommunicationChannelID());
            if (debug)
                LOG.debug("\tactionPowerCapability.getCommunicationChannelID()={}.", actionPowerCapability.getCommunicationChannelID());

            this.internalCommunicationCapability.addChannel(localAuctionCommunicationCapability.getCommunicationChannelID(),
                    this.localAuctionCommunicationCapability);
            if (debug) LOG.debug("\t Added localAuctionCommunicationCapability internally to add the channel.");

            this.internalCommunicationCapability.addChannel(actionPowerCapability.getCommunicationChannelID(),
                    this.actionPowerCapability);
            if (debug) LOG.debug("\t Added actionPowerCapability internally to add the channel.");
       }
            LOG.info("\t..................DONE CONSTRUCTING MARKET PERSONA(org={}, identifier={}, knowledge={}, focus={}", org, identifierString, knowledge, focus);
    }

    /**
     * Constructs a new instance of an agent using the organization-based agent architecture. Each prosumer agent will have
     * the capabilities needed to cc in a peer-based organization both as a peer and as a supervisor and contains its own
     * knowledge about the immediate organization in which it participates.
     *
     * @param identifierString a string containing a name that uniquely
     */
    public MarketPersona(final String identifierString) {
        super(identifierString);
        planSelector = new MarketPlanSelector();
        this.localAuctionCommunicationCapability = new AuctionCommunicationCapability(this, this.organization);
        if (debug) LOG.debug("\t New AuctionCommunicationCapability={}", this.localAuctionCommunicationCapability);
        addCapability(localAuctionCommunicationCapability);
        if (debug) LOG.debug("\t Added AuctionCommunicationCapability={}.", this.localAuctionCommunicationCapability);

        if (this.internalCommunicationCapability == null) {
            LOG.debug("ERROR: internalCommunicationCapability is null - Can't add localAuctionCommunicationCapability.");
        } else {
            if (debug)
                LOG.debug("\tlocalAuctionCommunicationCapability.getCommunicationChannelID()={}.", localAuctionCommunicationCapability.getCommunicationChannelID());
            if (debug)
                LOG.debug("\tactionPowerCapability.getCommunicationChannelID()={}.", actionPowerCapability.getCommunicationChannelID());

            this.internalCommunicationCapability.addChannel(localAuctionCommunicationCapability.getCommunicationChannelID(),
                    this.localAuctionCommunicationCapability);
            if (debug) LOG.debug("\t Added localAuctionCommunicationCapability internally to add the channel.");

            this.internalCommunicationCapability.addChannel(actionPowerCapability.getCommunicationChannelID(),
                    this.actionPowerCapability);
            if (debug) LOG.debug("\t Added actionPowerCapability internally to add the channel.");
        }
        LOG.info("\t..................DONE CONSTRUCTING MARKET PERSONA {}.", identifierString);
    }

    @Override
    public String toString() {
        return "MarketPersona [identifierString=" + this.identifierString + "]";
    }

}
