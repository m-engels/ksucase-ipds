package edu.ksu.cis.macr.ipds.market.guidelines.auction;

import edu.ksu.cis.macr.ipds.market.goals.MarketGoalParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class AuctionGuidelinesBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(AuctionGuidelinesBuilder.class);
    private static final boolean debug = false;
    private static final String PARAMETER_NAME = MarketGoalParameters.auctionGuidelines.toString();
    private static double kWh;
    private static int isSell;
    private static double desiredSellPrice_centsperkWh;
    private static double desiredBuyPrice_centsperkWh;
    private static double sellPriceFlexibilityPercent;
    private static double buyPriceFlexibilityPercent;
    private static long openingTimeSlice;
    private static long purchaseTimeSlice;
    private static int tierNumber;

    public static IAuctionGuidelines createAuctionGuidelines() {
        LOG.debug("Creating default empty auction guidelines.");
        return new AuctionGuidelines(kWh, isSell, desiredSellPrice_centsperkWh,
                desiredBuyPrice_centsperkWh, sellPriceFlexibilityPercent,
                buyPriceFlexibilityPercent, openingTimeSlice, purchaseTimeSlice, tierNumber);
    }

    public synchronized static IAuctionGuidelines createAuctionGuidelines(final Document configDoc) {
        LOG.debug("Entering createAuctionGuidelines(configDoc={})",configDoc);
        NodeList nodeList = configDoc.getElementsByTagName(PARAMETER_NAME);
        LOG.info("There are {} initial {}.", nodeList.getLength(), PARAMETER_NAME);
        IAuctionGuidelines g = AuctionGuidelinesBuilder.createAuctionGuidelines();
        if (nodeList.getLength() > 0) {
            AuctionGuidelinesBuilder.readElements(nodeList, g);
        }
        LOG.debug("Exiting createAuctionGuidelines: g={}",g);
            return g;

    }

    public synchronized static void readElements(NodeList nodeList, IAuctionGuidelines g) {
        LOG.debug("Reading auction guidelines object from XML elements.");
        for (int i = 0; i < nodeList.getLength(); ++i) {
            Element element = (Element) nodeList.item(i);
            String s;
            try {
                g.setBuyPriceFlexibilityPercent(Double.parseDouble(element.getAttribute("buyPriceFlexibilityPercent")));
            } catch (Exception e) {
                LOG.info("buyPriceFlexibilityPercent could not be read from the XML element. Using default value. ",
                        element);
                System.exit(-46);
            }
            s = "desiredBuyPrice_centsperkWh";
            try {
                g.setDesiredBuyPrice_centsperkWh(Double.parseDouble(element.getAttribute(s)));
            } catch (Exception e) {
                LOG.info("{} could not be read from the XML element. {} ", s, element);
                System.exit(-47);
            }
            try {
                g.setDesiredSellPrice_centsperkWh(Double.parseDouble(element.getAttribute("desiredSellPrice_centsperkWh")));
            } catch (Exception e) {
                LOG.info("desiredSellPrice_centsperkWh could not be read from the XML element. ", element);
                System.exit(-48);
            }
            try {
                g.setSellPriceFlexibilityPercent(Double.parseDouble(element.getAttribute("sellPriceFlexibilityPercent")));
            } catch (Exception e) {
                LOG.info("sellPriceFlexibilityPercent could not be read from the XML element.  ", element);
                System.exit(-49);
            }
            try {
                g.setIsSell(Integer.parseInt(element.getAttribute("isSell")));
            } catch (Exception e) {
                LOG.info("isSell could not be read from the XML element. ", element);
                System.exit(-49);
            }
            try {
                g.setkWh(Double.parseDouble(element.getAttribute("kWh")));
            } catch (Exception e) {
                LOG.info("kWh could not be read from the XML element. ", element);
                System.exit(-49);
            }
            s = "openingTimeSlice";
            try {
                g.setOpeningTimeSlice(Long.parseLong(element.getAttribute(s)));
            } catch (Exception e) {
                LOG.info("{} could not be read from the XML element. {} ", s, element);
                System.exit(-47);
            }
            s = "purchaseTimeSlice";
            try {
                g.setPurchaseTimeSlice(Long.parseLong(element.getAttribute(s)));
            } catch (Exception e) {
                LOG.info("{} could not be read from the XML element. {} ", s, element);
                System.exit(-47);
            }
            s = "tierNumber";
            try {
                g.setTierNumber(Integer.parseInt(element.getAttribute(s)));
                if (g.getTierNumber()<1 || g.getTierNumber()>2){
                    LOG.error("The auction tier must be 1 to 2. tier = {}", g.getTierNumber());
                    System.exit(-55);
                }
            } catch (Exception e) {
                LOG.info("{} could not be read from the XML element. {} ", s, element);
                System.exit(-49);
            }
        }
    }

    public AuctionGuidelinesBuilder setOpeningTimeSlice(long openingTimeSlice) {
        AuctionGuidelinesBuilder.openingTimeSlice = openingTimeSlice;
        return this;
    }

    public AuctionGuidelinesBuilder setPurchaseTimeSlice(long purchaseTimeSlice) {
        AuctionGuidelinesBuilder.purchaseTimeSlice = purchaseTimeSlice;
        return this;
    }

    public AuctionGuidelinesBuilder setkWh(double kWh) {
        AuctionGuidelinesBuilder.kWh = kWh;
        return this;
    }

    public AuctionGuidelinesBuilder setIsSell(int isSell) {
        AuctionGuidelinesBuilder.isSell = isSell;
        return this;
    }

    public AuctionGuidelinesBuilder setDesiredSellPrice_centsperkWh(double desiredSellPrice_centsperkWh) {
        AuctionGuidelinesBuilder.desiredSellPrice_centsperkWh = desiredSellPrice_centsperkWh;
        return this;
    }

    public AuctionGuidelinesBuilder setDesiredBuyPrice_centsperkWh(double desiredBuyPrice_centsperkWh) {
        AuctionGuidelinesBuilder.desiredBuyPrice_centsperkWh = desiredBuyPrice_centsperkWh;
        return this;
    }

    public AuctionGuidelinesBuilder setSellPriceFlexibilityPercent(double sellPriceFlexibilityPercent) {
        AuctionGuidelinesBuilder.sellPriceFlexibilityPercent = sellPriceFlexibilityPercent;
        return this;
    }

    public AuctionGuidelinesBuilder setBuyPriceFlexibilityPercent(double buyPriceFlexibilityPercent) {
        AuctionGuidelinesBuilder.buyPriceFlexibilityPercent = buyPriceFlexibilityPercent;
        return this;
    }


}