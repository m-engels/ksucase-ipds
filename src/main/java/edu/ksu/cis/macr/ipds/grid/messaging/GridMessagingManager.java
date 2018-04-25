package edu.ksu.cis.macr.ipds.grid.messaging;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import edu.ksu.cis.macr.aasis.messaging.Exchange;
import edu.ksu.cis.macr.aasis.messaging.IMessagingFocus;
import edu.ksu.cis.macr.aasis.messaging.IMessagingManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
/**
 The {@code MessagingManager} singleton provides a central exchange for communication between agents.
 Open a browser to http://localhost:15672/ when running a simulation to see associated information.
 */
public enum GridMessagingManager implements IMessagingManager {
    /**
     Singleton instance of the Communications Manager (one per JVM).
     */
    INSTANCE;

    /**
     The maximum time messages can remain on a queue before expiring (being deleted without being delivered).
     */
    public static final int MESSAGES_EXPIRE_IN_SECONDS = 60;
    /**
     The exchange is a named entity to which messages are sent. The type of exchange determines its routing
     behavior. For the IPDS simulation, we use different exchanges based on the types of information conveyed.
     */
    private static final Map<IMessagingFocus, Exchange> specs = new HashMap<IMessagingFocus, Exchange>() {{
        put(GridMessagingFocus.POWER, Exchange.createExchangeSpecification("POWER"));
        put(GridMessagingFocus.GRID, Exchange.createExchangeSpecification("GRID"));
        put(GridMessagingFocus.GRID_PARTICIPATE, Exchange.createExchangeSpecification("GRID_PARTICIPATE"));
    }
        private static final long serialVersionUID = 8323789558019617154L;
    };
    /**
     Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(GridMessagingManager.class);
    /**
     If true, the optional debug messages in this file will be shown. If false, they will not be issued.
     */
    private static boolean debug = false;

    public synchronized static boolean declareAndBindConsumerQueue(final IMessagingFocus focus, final String queueLink) {
        if (debug) LOG.debug("declareAndBindConsumerQueue focus={} queueLink={}  ", focus, queueLink);
        String fullQueueName = getFullQueueName(queueLink, GridMessagingManager.getQueueFocus(focus));
        Exchange spec = specs.get(focus);
        final String exchangeName = spec.getExchangeName();
        final String exchangeType = spec.getExchangeType();
        final Channel channel = spec.getChannel();
        if (debug) LOG.debug("declareAndBindConsumerQueue channel={}", channel);
        if (debug) LOG.debug("declareAndBindConsumerQueue Setup {} on Exchange = {}  ", fullQueueName, exchangeName);
        final boolean durable = false;
        final boolean exclusive = false;
        final boolean autoDelete = false;
        final String routingKey = fullQueueName;
        final int expireMillisecs = GridMessagingManager.MESSAGES_EXPIRE_IN_SECONDS * 1000;
        if (debug) LOG.debug("declareAndBindConsumerQueue routingKey={}  expireMillisecs={}  ", routingKey, expireMillisecs);
        try {
            channel.exchangeDeclare(exchangeName, exchangeType);
            if (debug) LOG.debug("declareAndBindConsumerQueue: exchange declared.");
            Map<String, Object> args = new HashMap<>();
            args.put("x-message-ttl", expireMillisecs);
            channel.queueDeclare(fullQueueName, durable, exclusive, autoDelete, args);
            if (debug) LOG.debug("declareAndBindConsumerQueue: queue declared.");
            channel.queueBind(fullQueueName, exchangeName, routingKey);
            if (debug) LOG.debug("declareAndBindConsumerQueue: queue bound.");
            return true;
        } catch (IOException e) {
            LOG.error("\"Error declaring and binding queue {}, {}. ", fullQueueName, e.getCause());
            System.exit(-37);
        }
        return false;
    }

    public static String getFullQueueName(final String queueLink, final String purpose) {
        return  purpose + "." + queueLink;
    }

    public static String getQueueFocus(IMessagingFocus focus){
        if(focus == GridMessagingFocus.GRID)              return  "GRID";
        if(focus == GridMessagingFocus.GRID_PARTICIPATE)  return  "_GRID";
        return "";
    }

    public static Channel getChannel(IMessagingFocus messagingFocus) {
        return GridMessagingManager.specs.get(messagingFocus).getChannel();
    }

    public static String getExchangeName(IMessagingFocus messagingFocus) {
        return GridMessagingManager.specs.get(messagingFocus).getExchangeName();
    }

    public static Map<IMessagingFocus, Exchange> getSpecs() {
        return specs;
    }

    public static void main(String[] args)  {
        initialize();
    }

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
}
