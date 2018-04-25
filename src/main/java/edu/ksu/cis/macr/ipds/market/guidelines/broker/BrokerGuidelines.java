/**
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
package edu.ksu.cis.macr.ipds.market.guidelines.broker;

import edu.ksu.cis.macr.goal.model.InstanceParameters;
import edu.ksu.cis.macr.organization.model.identifiers.StringIdentifier;

import java.io.Serializable;
import java.util.HashSet;

public class BrokerGuidelines implements IBrokerGuidelines, Serializable {
    private static final long serialVersionUID = 1L;
    private int tierNumber;
    private AuctionAlgorithm auctionAlgorithm;
    private long purchaseTimeSlice;
    private int iteration;
    private int maxIteration;
    private long openTimeSlice;
    private HashSet<String> authorizedParticipants = new HashSet<>();


    public BrokerGuidelines() {
    }

    public BrokerGuidelines(int tierNumber, AuctionAlgorithm auctionAlgorithm, long purchaseTimeSlice,
                            int iteration, int maxIteration, long openTimeSlice,
                            HashSet<String> authorizedParticipants) {
        this.tierNumber = tierNumber;
        this.auctionAlgorithm = auctionAlgorithm;
        this.purchaseTimeSlice = purchaseTimeSlice;
        this.iteration = iteration;
        this.maxIteration = maxIteration;
        this.openTimeSlice = openTimeSlice;
        this.authorizedParticipants = authorizedParticipants;
    }

    public synchronized static IBrokerGuidelines extractBrokerGuidelines(InstanceParameters params) {
        return (IBrokerGuidelines) params.getValue(StringIdentifier.getIdentifier("brokerGuidelines"));
    }

    public synchronized AuctionAlgorithm getAuctionAlgorithm() {
        return auctionAlgorithm;
    }

    public synchronized void setAuctionAlgorithm(int value) {
        this.auctionAlgorithm = AuctionAlgorithm.getActionAlgorithm(value);
    }

    public synchronized void setAuctionAlgorithm(AuctionAlgorithm auctionAlgorithm) {
        this.auctionAlgorithm = auctionAlgorithm;
    }

    public synchronized HashSet<String> getAuthorizedParticipants() {
        return authorizedParticipants;
    }

    public synchronized void setAuthorizedParticipants(HashSet<String> authorizedParticipants) {
        this.authorizedParticipants = authorizedParticipants;
    }

    public int getIteration() {
        return iteration;
    }

    public synchronized void setIteration(int iteration) {
        this.iteration = iteration;
    }

    public synchronized int getMaxIteration() {
        return this.maxIteration;
    }

    public synchronized void setMaxIteration(int maxIteration) {
        this.maxIteration = maxIteration;
    }

    public synchronized long getOpenTimeSlice() {
        return openTimeSlice;
    }

    public synchronized void setOpenTimeSlice(long openTimeSlice) {
        this.openTimeSlice = openTimeSlice;
    }

    public synchronized long getPurchaseTimeSlice() {
        return purchaseTimeSlice;
    }

    public synchronized void setPurchaseTimeSlice(long purchaseTimeSlice) {
        this.purchaseTimeSlice = purchaseTimeSlice;
    }

    public synchronized int getTierNumber() {
        return this.tierNumber;
    }

    public synchronized void setTierNumber(int tierNumber) {
        this.tierNumber = tierNumber;
    }

    @Override
    public String toString() {
        return "BrokerGuidelines{" +
                "tierNumber=" + tierNumber +
                ", auctionAlgorithm=" + auctionAlgorithm +
                ", purchaseTimeSlice=" + purchaseTimeSlice +
                ", iteration=" + iteration +
                ", maxIteration=" + maxIteration +
                ", openTimeSlice=" + openTimeSlice +
                ", authorizedParticipants=" + authorizedParticipants +
                '}';
    }
}
