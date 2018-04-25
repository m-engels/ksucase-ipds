package edu.ksu.cis.macr.ipds.market.guidelines;


import edu.ksu.cis.macr.aasis.common.Connections;
import edu.ksu.cis.macr.aasis.common.IConnections;
import edu.ksu.cis.macr.ipds.market.goals.MarketGoalParameters;
import edu.ksu.cis.macr.ipds.market.guidelines.auction.AuctionGuidelinesBuilder;
import edu.ksu.cis.macr.ipds.market.guidelines.auction.IAuctionGuidelines;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * The {@code GuidelineManager} reads goal guidelines (the top goal parameters) from a given XML
 * file.
 */
public enum MarketGuidelineManager {
    INSTANCE;
    private static final Logger LOG = LoggerFactory.getLogger(MarketGuidelineManager.class);
    private static final boolean debug = false;
    private static final String CUR_DIR = System.getProperty("user.dir");

    public synchronized static Map<UniqueIdentifier, Object> getGuidelines(String absPathToFile) {
        HashMap<UniqueIdentifier, Object> goalParameterValues = new HashMap<>();
        DocumentBuilder db = null;
        try {
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            if (db == null) {
                throw new ParserConfigurationException();
            }
        } catch (ParserConfigurationException e3) {
            LOG.error("PARSER ERROR: Cannot read guidelines from initialize.xml ({}).", absPathToFile);
            System.exit(-44);
        }
        // Read goal parameters initialization file to configure goals
        Document configDoc = null;
        try {
            configDoc = db.parse(new File(absPathToFile));
            if (configDoc == null) {
                throw new IOException();
            }
        } catch (SAXException | IOException e2) {
            LOG.error("IO ERROR: Cannot read  guidelines from initialize.xml ({}).", absPathToFile);
            System.exit(-45);
        }
        initializeAuctionConnections(absPathToFile, goalParameterValues);
        initializeAuctionGuidelines(goalParameterValues, configDoc);
        if (debug) LOG.debug("Before returning, the top goal guidelines are {}.", goalParameterValues);
        return goalParameterValues;
    }

    private static void initializeAuctionConnections(String absPathToFile, HashMap<UniqueIdentifier, Object> goalParameterValues) {
        // get market connections
        IConnections auctionConnections = Connections.createConnections(absPathToFile, "auctionConnections");
        if (!auctionConnections.getListConnectionGuidelines().isEmpty()) {
            goalParameterValues.put(MarketGoalParameters.auctionConnections, auctionConnections);
        }
    }

    private static void initializeAuctionGuidelines(HashMap<UniqueIdentifier, Object> goalParameterValues, Document configDoc) {
        // check for auction guidelines...
        NodeList nodeList = configDoc.getElementsByTagName("auctionGuidelines");
        if (debug) LOG.debug("There are {} initial auction guidelines.", nodeList.getLength());
        if (nodeList.getLength() == 0) goalParameterValues.put(MarketGoalParameters.auctionGuidelines, null);
        else {
            IAuctionGuidelines g = new AuctionGuidelinesBuilder().createAuctionGuidelines();
          AuctionGuidelinesBuilder.readElements(nodeList, g);
            goalParameterValues.put(MarketGoalParameters.auctionGuidelines, g);
            if (debug)
                LOG.debug("{} read from Initialize.xml: {}.", MarketGoalParameters.auctionGuidelines, g.toString());
        }
    }

}
