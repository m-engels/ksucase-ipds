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
package edu.ksu.cis.macr.ipds.market.guidelines.auction;

import edu.ksu.cis.macr.goal.model.InstanceParameters;
import edu.ksu.cis.macr.organization.model.identifiers.StringIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * Class describing the prosumers behavior in the power market - indicates the time, quantity, price, and whether this is
 * available to buy or sell (current guidelines are for a single auction at a single future time); AO-MaSE Process for
 * adding fields:
 * 1) Add new fields with getters and setters.
 * 2) Add them as constructor parameters;
 * 3) Regenerate toString();
 * 4) Add getters and setters to associated interface class;
 * 5) Add fields to AuctionGuidelinesBuilder class;
 * 6) Add code to Builder util as needed to create Initialize.xml file.
 * 7) Run builder.
 */
public class AuctionGuidelines implements IAuctionGuidelines, Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(AuctionGuidelines.class);
    private static final boolean debug = false;
    public static final String STRING_IDENTIFIER = "auctionGuidelines";
    private static final long serialVersionUID = 1L;
    private double desiredSellPrice_centsperkWh;
    private double desiredBuyPrice_centsperkWh;
    private double sellPriceFlexibilityPercent;
    private double buyPriceFlexibilityPercent;
    private double kWh;
    private int isSell; // 0 is buy, 1 is Sell
    private long openingTimeSlice;
    private long purchaseTimeSlice;
    private int tierNumber;

    public AuctionGuidelines() {

    }


    public AuctionGuidelines(final double kWh, final int isSell, final double desiredSellPrice_centsperkWh,
                             final double desiredBuyPrice_centsperkWh,
                             final double sellPriceFlexibilityPercent, final double buyPriceFlexibilityPercent,
                             final long openingTimeSlice, final long purchaseTimeSlice, final int tierNumber) {
        this.kWh = kWh;
        this.isSell = isSell;
        this.desiredSellPrice_centsperkWh = desiredSellPrice_centsperkWh;
        this.desiredBuyPrice_centsperkWh = desiredBuyPrice_centsperkWh;
        this.sellPriceFlexibilityPercent = sellPriceFlexibilityPercent;
        this.buyPriceFlexibilityPercent = buyPriceFlexibilityPercent;
        this.openingTimeSlice = openingTimeSlice;
        this.purchaseTimeSlice = purchaseTimeSlice;
        this.tierNumber = tierNumber;
     }

    public synchronized static IAuctionGuidelines extractAuctionGuidelines(final InstanceParameters params) {
        return (IAuctionGuidelines) params.getValue(StringIdentifier.getIdentifier(STRING_IDENTIFIER));
    }

    @Override
    public synchronized int getTierNumber() {
        return this.tierNumber;
    }

    @Override
    public synchronized void setTierNumber(final int tierNumber) {
        this.tierNumber = tierNumber;
    }

    @Override
    public synchronized long getOpeningTimeSlice() {
        return this.openingTimeSlice;
    }

    @Override
    public synchronized void setOpeningTimeSlice(final long openingTimeSlice) {
        this.openingTimeSlice = openingTimeSlice;
    }

    public synchronized long getPurchaseTimeSlice() {
        return this.purchaseTimeSlice;
    }

    @Override
    public synchronized void setPurchaseTimeSlice(final long purchaseTimeSlice) {
        this.purchaseTimeSlice = purchaseTimeSlice;
    }

    @Override
    public synchronized double getkWh() {
        return kWh;
    }

    @Override
    public synchronized void setkWh(final double kWh) {
        this.kWh = kWh;
    }

    @Override
    public synchronized int getIsSell() {
        return isSell;
    }

    @Override
    public synchronized void setIsSell(final int isSell) {
        this.isSell = isSell;
    }

    @Override
    public synchronized double getBuyPriceFlexibilityPercent() {
        return buyPriceFlexibilityPercent;
    }

    @Override
    public synchronized void setBuyPriceFlexibilityPercent(final double buyPriceFlexibilityPercent) {
        this.buyPriceFlexibilityPercent = buyPriceFlexibilityPercent;
    }

    @Override
    public synchronized double getDesiredBuyPrice_centsperkWh() {
        return desiredBuyPrice_centsperkWh;
    }

    @Override
    public synchronized void setDesiredBuyPrice_centsperkWh(final double desiredBuyPrice_centsperkWh) {
        this.desiredBuyPrice_centsperkWh = desiredBuyPrice_centsperkWh;
    }

    @Override
    public synchronized double getDesiredSellPrice_centsperkWh() {
        return desiredSellPrice_centsperkWh;
    }

    @Override
    public synchronized void setDesiredSellPrice_centsperkWh(final double desiredSellPrice_centsperkWh) {
        this.desiredSellPrice_centsperkWh = desiredSellPrice_centsperkWh;
    }

    @Override
    public synchronized double getSellPriceFlexibilityPercent() {
        return sellPriceFlexibilityPercent;
    }

    @Override
    public synchronized void setSellPriceFlexibilityPercent(final double sellPriceFlexibilityPercent) {
        this.sellPriceFlexibilityPercent = sellPriceFlexibilityPercent;
    }

    @Override
    public String toString() {
        return "AuctionGuidelines{" +
                "desiredSellPrice_centsperkWh=" + desiredSellPrice_centsperkWh +
                ", desiredBuyPrice_centsperkWh=" + desiredBuyPrice_centsperkWh +
                ", sellPriceFlexibilityPercent=" + sellPriceFlexibilityPercent +
                ", buyPriceFlexibilityPercent=" + buyPriceFlexibilityPercent +
                ", kWh=" + kWh +
                ", isSell=" + isSell +
                ", openingTimeSlice=" + openingTimeSlice +
                ", purchaseTimeSlice=" + purchaseTimeSlice +
                ", tierNumber=" + tierNumber +
                '}';
    }
}
