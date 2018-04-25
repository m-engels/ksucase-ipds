package edu.ksu.cis.macr.ipds.market.guidelines.broker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class BrokerGuidelinesBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(BrokerGuidelinesBuilder.class);
    private static final boolean debug = false;
    private static final String STRING_IDENTIFIER = "brokerGuidelines";
    private static int tierNumber;
    private static long purchaseTimeslice = 10;
    private static AuctionAlgorithm auctionAlgorithm = AuctionAlgorithm.DOUBLE_AUCTION;
    private static long purchaseTimeSlice = 0;
    private static int iteration = 0;
    private static int maxIteration = 1;
    private static long openTimeSlice = 0;
    private static HashSet<String> authorizedParticipants = new HashSet<>();


    public static int getIteration() {
        return iteration;
    }

    public static void setIteration(int iteration) {
        BrokerGuidelinesBuilder.iteration = iteration;
    }

    public static void setPurchaseTimeSlice(long purchaseTimeSlice) {
        BrokerGuidelinesBuilder.purchaseTimeSlice = purchaseTimeSlice;
    }

    public static int getMaxIteration() {
        return maxIteration;
    }

    public static void setMaxIteration(int maxIteration) {
        BrokerGuidelinesBuilder.maxIteration = maxIteration;
    }

    public static BrokerGuidelines create() {
        return new BrokerGuidelines(tierNumber, auctionAlgorithm, purchaseTimeslice, iteration, maxIteration, openTimeSlice, authorizedParticipants);
    }

    public static BrokerGuidelines createBrokerGuidelines(Document configDoc) {
        LOG.debug("Entering createBrokerGuidelines(configDoc={})",configDoc);
        NodeList nodeList = configDoc.getElementsByTagName(STRING_IDENTIFIER);
        if (debug) LOG.debug("There are {} initial {}.", nodeList.getLength(), STRING_IDENTIFIER);
        BrokerGuidelines g = new BrokerGuidelines();
        if (nodeList.getLength() > 0){
            BrokerGuidelinesBuilder.readElements(nodeList, g);
        }
        LOG.debug("Exiting createBrokerGuidelines: g={}",g);
        return g;
    }

    public static void readElements(NodeList nodeList, IBrokerGuidelines g) {
        for (int i = 0; i < nodeList.getLength(); ++i) {
            Element element = (Element) nodeList.item(i);
            String s = "";
            try {
                s = "auctionAlgorithm";
                g.setAuctionAlgorithm(Integer.parseInt(element.getAttribute(s)));
            } catch (Exception e) {
                LOG.info("{} could not be read from the XML element. {} ", s, element);
                System.exit(-46);
            }
            try {
                s = "purchaseTimeSlice";
                g.setPurchaseTimeSlice(Long.parseLong(element.getAttribute(s)));
            } catch (Exception e) {
                LOG.info("{} could not be read from the XML element. {} ", s, element);
                System.exit(-48);
            }
            try {
                s = "openTimeSlice";
                g.setOpenTimeSlice(Long.parseLong(element.getAttribute(s)));
            } catch (Exception e) {
                LOG.info("{} could not be read from the XML element. {} ", s, element);
                System.exit(-47);
            }
            try {
                s = "authorizedParticipants";
                String input = element.getAttribute(s);
                List<String> lst = Arrays.asList(input.split("/s*,/s*"));
                g.setAuthorizedParticipants(new HashSet<>(lst));
            } catch (Exception e) {
                LOG.info("{} could not be read from the XML element. {} ", s, element);
                System.exit(-47);
            }

            try {
                s = "tierNumber";
                g.setTierNumber(Integer.parseInt(element.getAttribute(s)));
            } catch (Exception e) {
                LOG.info("{} could not be read from the XML element. {} ", s, element);
                System.exit(-47);
            }
            try {
                s = "iteration";
                g.setIteration(Integer.parseInt(element.getAttribute(s)));
            } catch (Exception e) {
                LOG.info("{} could not be read from the XML element. {} ", s, element);
                System.exit(-47);
            }
            try {
                s = "maxIteration";
                g.setMaxIteration(Integer.parseInt(element.getAttribute(s)));
            } catch (Exception e) {
                LOG.info("{} could not be read from the XML element. {} ", s, element);
                System.exit(-47);
            }
        }
    }

    /**
     * Add the broker guidelines to the given xml document.
     *
     * @param doc               - xml document
     * @param rootElement       - the root element that will get updated
     * @param n                 - the broker agent id number
     * @param firstLetter       - the first letter of the broker agent name
     * @param childNumberString - the child number string, eg. 44, 45, 46, 47
     */
    public static void addBrokerGuidelines(Document doc, Element rootElement, int n, String firstLetter, String childNumberString) {
        if (debug) LOG.debug("Broker num = {}.", n);
        Element conn = doc.createElement("brokerGuidelines");
        conn.setAttribute("auctionAlgorithm", Integer.toString(1));
        conn.setAttribute("purchaseTimeSlice", Long.toString(10));
        conn.setAttribute("openTimeSlice", Long.toString(1));
        conn.setAttribute("iteration", Integer.toString(0));
        conn.setAttribute("maxIteration", Integer.toString(1));

        if (firstLetter.equals("N")) conn.setAttribute("tierNumber", Integer.toString(1));
        else if (firstLetter.equals("L")) conn.setAttribute("tierNumber", Integer.toString(2));
        conn.setAttribute("authorizedParticipants", childNumberString);
        rootElement.appendChild(conn);
    }

    public BrokerGuidelinesBuilder setAuctionAlgorithm(AuctionAlgorithm auctionAlgorithm) {
        BrokerGuidelinesBuilder.auctionAlgorithm = auctionAlgorithm;
        return this;
    }

    public BrokerGuidelinesBuilder setAuthorizedParticipants(HashSet<String> authorizedParticipants) {
        BrokerGuidelinesBuilder.authorizedParticipants = authorizedParticipants;
        return this;
    }

    public BrokerGuidelinesBuilder setOpenTimeSlice(long openTimeSlice) {
        BrokerGuidelinesBuilder.openTimeSlice = openTimeSlice;
        return this;
    }

    public BrokerGuidelinesBuilder setPurchaseTimeslice(long purchaseTimeslice) {
        BrokerGuidelinesBuilder.purchaseTimeslice = purchaseTimeslice;
        return this;
    }

    public BrokerGuidelinesBuilder setTierNumber(int tierNumber) {
        BrokerGuidelinesBuilder.tierNumber = tierNumber;
        return this;
    }
}
