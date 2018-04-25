package edu.ksu.cis.macr.ipds.views.panels;

import edu.ksu.cis.macr.aasis.agent.cc_m.IPersonaControlComponentMaster;
import edu.ksu.cis.macr.aasis.agent.cc_message.IBaseMessage;
import edu.ksu.cis.macr.aasis.agent.persona.IOrganization;
import edu.ksu.cis.macr.aasis.agent.persona.IPersona;
import edu.ksu.cis.macr.aasis.agent.persona.IPersonaExecutionComponent;
import edu.ksu.cis.macr.ipds.primary.capabilities.participate.IPowerCommunicationCapability;
import edu.ksu.cis.macr.ipds.primary.messages.IPowerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

/*
 * View for display the Local Power Messages (JPanel object)
 *
 * Currently set up to tack on bottom of default GUI
 */
public class LocalMessagePanel extends JPanel implements ListSelectionListener {
    private static final Logger LOG = LoggerFactory.getLogger(LocalMessagePanel.class);
    private static final long serialVersionUID = 1L;
    private DefaultTableModel tableModel = new DefaultTableModel();
    // table overrides isCellEditable so it is not editable by the user in the gui
    private JTable table = new JTable(tableModel) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    // info displays the LocalPowerMessage when selected in the table by the user
    private JTextPane info = new JTextPane();
    private JScrollPane tablePane = new JScrollPane(table);
    private JScrollPane infoPane = new JScrollPane(info);
    private List<IBaseMessage<?>> listMessages = new ArrayList<>();
    // Messages are mapped with a String of their content so they are easily retrievable to display when selected
    private Map<IBaseMessage<?>, String> displayedMessages = new HashMap<>();
    private IOrganization worldstate = null;
    private String masterName;

    /*
     * Local Message View Contructor - Takes Organization
     * as a parameter.
     *
     * Initializes Messages View - Current Dimensions: Width: 575px
     * table: Height: 100px
     * info: Height: 60px
     */
    public LocalMessagePanel(IOrganization e) {

        super();
        info.setEditable(false);
        table.getSelectionModel().addListSelectionListener(this);
        tablePane.setPreferredSize(new Dimension(575, 100));
        infoPane.setPreferredSize(new Dimension(575, 60));

        table.setColumnSelectionAllowed(false);
        table.setCellSelectionEnabled(false);
        table.setRowSelectionAllowed(true);

        tableModel.addColumn("Date/Time");
        tableModel.addColumn("Sender");
        tableModel.addColumn("Receiver");
        tableModel.addColumn("Performative");

        tablePane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createCompoundBorder(
                BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createRaisedBevelBorder()), "Local Power Messages "));

        infoPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createCompoundBorder(
                BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createRaisedBevelBorder())));


        final JPanel splitPanel = new JPanel(new GridLayout(1, 1));
        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tablePane, infoPane);
        sp.setResizeWeight(0.5);
        sp.setOneTouchExpandable(false);
        splitPanel.add(sp);


        this.setLayout(new GridLayout(1, 1));
        this.add(splitPanel);


        //this.setPreferredSize(new Dimension(575, 160));
        //this.setLayout(new GridLayout(2, 1));
        //this.add(tablePane);
        //this.add(infoPane);
    }

    /* Local Message View Contructor - Takes Organization
     * as a parameter.
     *
     * Initializes Messages View - Current Dimensions: Width: 575px
     * table: Height: 100px
     * info: Height: 60px
     */
    public LocalMessagePanel(IPersonaControlComponentMaster master) {
        super();
        Objects.requireNonNull(master, "ERROR. LocalMessageView constructor needs a CC Master");

        info.setEditable(false);
        table.getSelectionModel().addListSelectionListener(this);
        tablePane.setPreferredSize(new Dimension(575, 100));
        infoPane.setPreferredSize(new Dimension(575, 60));

        table.setColumnSelectionAllowed(false);
        table.setCellSelectionEnabled(false);
        table.setRowSelectionAllowed(true);

        tableModel.addColumn("Date/Time");
        tableModel.addColumn("Sender");
        tableModel.addColumn("Receiver");
        tableModel.addColumn("Performative");

        tablePane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createCompoundBorder(
                BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createRaisedBevelBorder()), "Local Messages - " + master.getIdentifier().toString()));

        infoPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createCompoundBorder(
                BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createRaisedBevelBorder())));

        final JPanel splitPanel = new JPanel(new GridLayout(1, 1));
        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tablePane, infoPane);
        sp.setResizeWeight(0.5);
        sp.setOneTouchExpandable(false);
        splitPanel.add(sp);
        masterName =  master.getIdentifier().toString();
        //this.setPreferredSize(new Dimension(575, 160));
        this.setLayout(new GridLayout(1, 1));
        this.add(splitPanel);
        // this.add(infoPane);
    }

    /*
     * Populates the list of Local Power Messages to be displayed by iterating
     * through all the current agents from the organization object.
     */
    public List<IBaseMessage<?>> getCollection() {
        List<IBaseMessage<?>> results = new ArrayList<>();
        // All Agents from the organization parameter
        Collection<IPersona> agentlist = worldstate.getAllPersona();

        for (IPersonaExecutionComponent agent : agentlist) {
            IPowerCommunicationCapability bcc = agent.getCapability(IPowerCommunicationCapability.class);
            List<IPowerMessage> messages = bcc.getLocalMessagesAsList();
            List<IBaseMessage<?>> listMessages = new ArrayList<>(messages);
            results.addAll(listMessages);
        }

        return results;
    }

    /*
     * Updates the view - Acts as listener for adding new getNumberOfMessages to the table
     */
    public void updateVisualizationPanel() {

        // List of all LocalPowerMessage objects in the system
        final List<IBaseMessage<?>> updatedList = getCollection();
        if (updatedList == null || updatedList.isEmpty()) {
            return;
        }

        // List that will add only objects that aren't already included in the table
        final List<IBaseMessage<?>> toAdd = new ArrayList<>();

        // Iterates through all LocalPowerMessage objects and checks if they have already been displayed
        // If not they are added to the toAdd list so they can be added to the view
        for (IBaseMessage<?> o : updatedList) {
            if (!displayedMessages.containsKey(o)) {
                toAdd.add(o);
            }
        }
        if (toAdd.isEmpty()) {
            return;
        }

        // Thread to update the view
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                for (final IBaseMessage<?> o : toAdd) {

                    if (!displayedMessages.containsKey(o)) {
                        tableModel.insertRow(0, convertMessage(o));
                        listMessages.add(o);
                        displayedMessages.put(o, o.getContent().toString());
                    }
                }
            }
        });
    }
    /*
     * Updates the view - Acts as listener for adding new getNumberOfMessages to the table
     */

    public void updateVisualizationPanel(final IPersonaExecutionComponent ec) {
        Objects.requireNonNull(ec, "ERROR - LocalMessageView.updateVisualizationPanel() requires EC");
        if (ec.getCapability(IPowerCommunicationCapability.class) == null) {
            return;
        }

        List<IPowerMessage> allCurrentMessages = ec.getCapability(IPowerCommunicationCapability.class).getLocalMessagesAsList();
        for (final IBaseMessage<?> o : allCurrentMessages) {
        	if(o.getLocalSender().toString().substring(0, 3).equals(masterName)|| o.getLocalReceiver().toString().substring(0, 3).equals(masterName)){
	            tableModel.insertRow(0, convertMessage(o));
	            listMessages.add(o);
	            displayedMessages.put(o, o.getContent().toString());
        	}
        }
//		// Thread to update the view
//		SwingUtilities.invokeLater(new Runnable() {
//
//			@Override
//			public void run() {
//
//			for (final LocalPowerMessage o :  allCurrentMessages) {
//
//				if (!displayedMessages.containsKey(o)) {
//					tableModel.insertRow(0, convertMessage(o));
//					listMessages.add(o);
//					displayedMessages.put(o, o.getContent().toString());
//				}
//			}
//			}});
    }

    private void displayDummyMessage(IBaseMessage<?> msg, int size) {
        tableModel.insertRow(0, convertMessage(msg));
        listMessages.add(msg);
        displayedMessages.put(msg, "Msg 1 of " + size + " " + msg.getContent().toString());

    }

    /*
     * Converts LocalPowerMessage to a String array so that it can
     * be added to the table
     *
     * Column 1: Current Date/time
     * Column 2: Message Sender
     * Column 3: Message Receiver
     * Column 4: Message Performative
     */
    private String[] convertMessage(IBaseMessage<?> message) {
        String[] result = new String[4];

        result[0] = (new Date()).toString();
        result[1] = message.getLocalSender().toString();
        result[2] = message.getLocalReceiver().toString();
        result[3] = message.getPerformativeType().toString();

        return result;
    }

    /*
     * Overrides the valueChanged() method for a ListSelectionListener
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     *
     * Handles the events for when the user selects a new row in the table
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {
        info.setText(displayedMessages.get(listMessages.get((listMessages.size() - 1) - table.getSelectedRow())));
        info.repaint();
    }

    /*
     * Starts the view object listener that keeps the getNumberOfMessages updated
     */
    public void startListener() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    updateVisualizationPanel();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

            }
        });

        t.start();
    }
    /*
     * Starts a new listener for this execution component
     * to keep the messages display updated.
     * The listener continuously updates the visualization panel.
     */

    public void startListener(final IPersonaExecutionComponent ec) {
        Objects.requireNonNull(ec, "ERROR - LocalMessageView.startListener() requires EC");
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    updateVisualizationPanel(ec);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

            }
        });

        t.start();
    }
}
