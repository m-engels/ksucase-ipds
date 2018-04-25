package edu.ksu.cis.macr.ipds.grid.messages;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import edu.ksu.cis.macr.aasis.messaging.Exchange;
import edu.ksu.cis.macr.aasis.messaging.IMessagingFocus;
import edu.ksu.cis.macr.ipds.grid.messaging.GridMessagingFocus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 Performatives indicate a primary characteristic of the message and may be used to route behavior in plans.
 */
public enum PowerPerformative {

  /**
   The agent is operating within the provided guidelines and (later) expects to remain within bounds throughout the
   planning horizon.
   */
  REPORT_OK,

  /**
   The agent is operating (or expects to operate) in violation of one or more of its specified guidelines.
   */
  REPORT_OUT_OF_BOUNDS;

    /**
     The {@code MessagingManager} singleton provides a central exchange for communication between agents.

     Open a browser to http://localhost:15672/ when running a simulation to see associated information.
     */
    public static enum MessagingManager {

      /**
       Singleton instance of the Communications Manager (one per JVM).
       */
      INSTANCE;
      /**
       The exchange is a named entity to which messages are sent. The type of exchange determines its routing
       behavior. For the IPDS simulation, we use different exchanges based on the types of information conveyed.
       */
      public static final Map<IMessagingFocus, Exchange> specs = new HashMap<IMessagingFocus, Exchange>() {{
        put(GridMessagingFocus.GRID, Exchange.createExchangeSpecification("GRID"));
        put(GridMessagingFocus.GRID_PARTICIPATE, Exchange.createExchangeSpecification("GRID_PARTICIPATE"));
      }
        private static final long serialVersionUID = 8323789558019617154L;
      };

        public static void main(String[] args)  {
            initialize();
        }
      /**
       The maximum time messages can remain on a queue before expiring (being deleted without being delivered).
       */
      public static final int MESSAGES_EXPIRE_IN_SECONDS = 60;
      /**
       Logger.
       */
      private static final Logger LOG = LoggerFactory.getLogger(MessagingManager.class);
      /**
       If true, the optional debug messages in this file will be shown. If false, they will not be issued.
       */
      private static boolean debug = false;


      public static void initialize() {
        LOG.info("INITIALIZING MESSAGING CENTRAL EXCHANGES ......................................");
        try {
          for (Map.Entry<IMessagingFocus, Exchange> entry : specs.entrySet()) {
            Exchange spec = entry.getValue();
            ConnectionFactory factory = new ConnectionFactory();

            factory.setHost(spec.getExchangeHost());
            Connection conn = factory.newConnection();
            spec.setConnection( conn);
            spec.setChannel(spec.getConnection().createChannel());

            // declare exchange - created if does not exist
            spec.getChannel().exchangeDeclare(spec.getExchangeName(), spec.getExchangeType());
            specs.put(entry.getKey(), spec);
            if (debug) LOG.debug("   Exchange: {}", spec.toString());
          }

        } catch (Exception e) {
          LOG.error("Error: Could not initialize message exchanges. Verify RabbitMQ is running. README.txt for info. {}",
                  e.getMessage());
             System.exit(1);
        }
        LOG.info("\"\t SUCCESS: MessagingManager initialized. ");
      }

        public String getQueueFocus(IMessagingFocus focus){
            if(focus == GridMessagingFocus.GRID)  return  "GRID";
            return "_GRID";
        }

      public synchronized static boolean declareAndBindConsumerQueue(final GridMessagingFocus messagingFocus, final String fullQueueName) {
        Exchange spec = specs.get(messagingFocus);
        final String exchangeName = spec.getExchangeName();
        final String exchangeType = spec.getExchangeType();
        final Channel channel = spec.getChannel();
        if(debug)  LOG.debug("Setup {} on Exchange = {}  ", fullQueueName, exchangeName);
        final boolean durable = false;
        final boolean exclusive = false;
        final boolean autoDelete = false;
        final String routingKey = fullQueueName;

        final int expireMillisecs = MessagingManager.MESSAGES_EXPIRE_IN_SECONDS * 1000;

        try {
          //   ConnectionFactory factory = new ConnectionFactory();
          //  factory.setHost(MessagingManager.BROKER_HOST_CONNECT);
          //   Connection tempConnection = factory.newConnection();

          //  Channel tempChannel = tempConnection.createChannel();
          channel.exchangeDeclare(exchangeName, exchangeType);

          Map<String, Object> args = new HashMap<>();
          args.put("x-message-ttl", expireMillisecs);

          channel.queueDeclare(fullQueueName, durable, exclusive, autoDelete, args);
          channel.queueBind(fullQueueName, exchangeName, routingKey);

          // tempChannel.close();
          // tempConnection.close();
          return true;
        } catch (IOException e) {
          LOG.error("\"Error declaring and binding quueue {}, {}. ", fullQueueName, e.getCause());
          System.exit(-37);
        }
        return false;
      }


      public static Channel getChannel(IMessagingFocus messagingFocus) {
        return MessagingManager.specs.get(messagingFocus).getChannel();
      }

      public static String getExchangeName(IMessagingFocus messagingFocus) {
        return MessagingManager.specs.get(messagingFocus).getExchangeName();
      }
    }

    /**
     The {@code MessagingReliabilityManager} singleton provides support for simulating the impacts of messages reliability
     and delays on the system behavior.
     */
    public static enum MessagingReliabilityManager {

      // TODO: Custom delays and reliability is not fully implemented yet.

      /**
       Singleton instance of the MessagingReliabilityManager (one per JVM).
       */
      INSTANCE;
      private static final Logger LOG = LoggerFactory.getLogger(MessagingReliabilityManager.class);
      private static final boolean debug = false;
      private static double communicationReliability = 1.0;
      private static double communicationDelay_millisecs = 0.0;
        private static boolean isLoaded = Boolean.FALSE;


      public static void load() {
        String curDir = System.getProperty("user.dir");
        String path = curDir + "/src/main/resources/configs/";

        // get Scenario file, name, and path from main instance.xml
        File configFile = new File(path + "messaging_reliability.xml");
        if (!configFile.exists()) {
          //TODO: IDE problems - in eclipse user.dir is project/project in IDEA it's project....
          curDir = curDir + "/aasis";          // Total hack
          path = curDir + "/src/main/resources/configs/";
          configFile = new File(path + "appconfig.xml");  // for IDEA
        }
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
          db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
          throw new ConfigurationException(e);
        }
        if (db == null) {
          throw new ConfigurationException();
        }
        Document configDoc = null;
        try {
          configDoc = db.parse(configFile);
        } catch (SAXException | IOException f) {
          throw new ConfigurationException(f);
        }

        Element docEle = configDoc.getDocumentElement();
        Element localDefaultReliability = (Element) docEle.getElementsByTagName("DefaultCommunicationReliability").item(0);
        Element localDefaultDelay = (Element) docEle.getElementsByTagName("DefaultCommunicationDelay_millisecs").item(0);
        Element localPowerReliability = (Element) docEle.getElementsByTagName("DefaultCommunicationReliability").item(0);
        Element localPowerDelay = (Element) docEle.getElementsByTagName("DefaultCommunicationDelay_millisecs").item(0);


        communicationReliability = getReliabilityValue(localDefaultReliability);
        communicationDelay_millisecs = getDelayValue(localDefaultDelay);
          double powerCommunicationReliability = getReliabilityValue(localPowerReliability);
          double powerCommunicationDelay_millisecs = getDelayValue(localPowerDelay);
        isLoaded = true;
      }

      private static Double getDelayValue(Element element) {

        try {
          String strValue = element.getAttribute("value");
          return Double.parseDouble(strValue);
        } catch (Exception ex) {
          LOG.info("Value not available.. using standard delay.");
        }
        return 0.0;
      }

      private static Double getReliabilityValue(Element element) {

        try {
          String strValue = element.getAttribute("value");
          return Double.parseDouble(strValue);
        } catch (Exception ex) {
          LOG.info("Value not available.. using standard reliability.");
        }
        return 1.0;
      }



      public static double getCommunicationDelay() {
        if (!isLoaded) load();
        return communicationDelay_millisecs;
      }

      public static double getCommunicationReliability() {
        if (!isLoaded) load();
        return communicationReliability;
      }

      static class ConfigurationException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        ConfigurationException(Throwable cause) {
          super(cause);
        }

        ConfigurationException() {
          super();
        }
      }
    }
}
