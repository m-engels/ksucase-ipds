package edu.ksu.cis.macr.ipds.views;

import edu.ksu.cis.macr.aasis.agent.cc_p.ConnectionModel;
import edu.ksu.cis.macr.ipds.config.RunManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

/**
 * The SelfOrganization Connection view shows the agent connections as they are established.
 */
public class ConnectionView extends JFrame implements IConnectionView {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectionView.class);
    private static final long serialVersionUID = 1L;
    private static final int START_HORIZONTAL = 10;
    private static final int START_VERTICAL = 10;
    private static final int FRAME_WIDTH = 875;
    private static final Dimension DEFAULT_PREFERRED_SIZE = new Dimension(450, FRAME_WIDTH);
    private static ImageIcon imageIcon;
    private BufferedImage image;
    private boolean stepMode = false;
    private JMenuBar MenuBar = new JMenuBar();
    private JFrame frame = new JFrame();
    private JMenu File = new JMenu("File");
    private JMenuItem Exit = new JMenuItem("Exit");

    private GregorianCalendar simulationTime;
    private int simulationTimeSlices;
    private String connectionList;
    private HashMap<String, Component> componentMap;
    private javax.swing.JPanel imagePanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblPurpose;
    private javax.swing.JLabel lblSimTime;
    private javax.swing.JPanel sidePanel;
    private javax.swing.JPanel topPanel;
    private javax.swing.JTextArea txtConnectionList;
    private javax.swing.JLabel txtSimTime;
    private javax.swing.JTextField txtSimulationTime;


    static {
        final LookAndFeelInfo[] installedLookAndFeels = UIManager
                .getInstalledLookAndFeels();
        try {
            for (final LookAndFeelInfo info : installedLookAndFeels) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            LOG.error("Error: frame instantiation error. Please restart.");
            System.exit(-3);
        } catch (final UnsupportedLookAndFeelException e) {
            LOG.error("Error: UnsupportedLookAndFeelException. Please restart.");
            System.exit(-4);
        }
    }


    /**
     * Creates new form
     */
    public ConnectionView(String testCaseName) {
        addStandardFeatures();
        frame.setPreferredSize(DEFAULT_PREFERRED_SIZE);
        frame.setLocation(START_HORIZONTAL, START_VERTICAL);

        frame.setTitle("Connections - " + testCaseName);
        getImageWithClassLoader();
        initComponents();
        createComponentMap();
        initializeAndDisplay(RunManager.getTestCaseName());
     }

    private void addStandardFeatures() {
        File.add(Exit);
        MenuBar.add(File);
        Exit.addActionListener(new ExitListener());
        WindowListener exitListener = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int confirm = JOptionPane.showOptionDialog(frame,
                        "Are you sure you want to close this application?",
                        "Exit Confirmation", JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, null, null);
                if (confirm == JOptionPane.YES_OPTION) {
                    dispose();
                    System.exit(0);
                }
            }
        };
        frame.addWindowListener(exitListener);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setJMenuBar(MenuBar);
    }

    public static IConnectionView createConnectionView() {
        return new ConnectionView(RunManager.getTestCaseName());
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ConnectionView view = new ConnectionView(RunManager.getTestCaseName());
            }
        });
       }

    private void getImageWithClassLoader() {
        try {
            ClassLoader cl = this.getClass().getClassLoader();
            imageIcon = new ImageIcon(cl.getResource("images/01nodemap.png"));
            LOG.info("Trying image icon from = images/01nodemap.png = {}", imageIcon);
            if (imageIcon == null) {
                imageIcon = new ImageIcon(cl.getResource("resources/images/01nodemap.png"));
                LOG.info("Trying image icon from = resources/images/01nodemap.png = {}", imageIcon);
            }
        } catch (Exception ex) {
            LOG.error("Could not find image icon");
            System.exit(-8);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        topPanel = new javax.swing.JPanel();
        txtSimTime = new javax.swing.JLabel();
        lblSimTime = new javax.swing.JLabel();
        lblPurpose = new javax.swing.JLabel();
        txtSimulationTime = new javax.swing.JTextField();
        sidePanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtConnectionList = new javax.swing.JTextArea();
        imagePanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        topPanel.setBackground(new java.awt.Color(153, 153, 255));

        txtSimTime.setLabelFor(lblSimTime);
        txtSimTime.setName("simTime"); // NOI18N

        lblSimTime.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblSimTime.setText("Simulation Time:");

        lblPurpose.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblPurpose.setText("Connection Status");

        txtSimulationTime.setEditable(false);
        txtSimulationTime.setBackground(new java.awt.Color(153, 153, 255));
        txtSimulationTime.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        txtSimulationTime.setText("jTextField1");
        txtSimulationTime.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        javax.swing.GroupLayout topPanelLayout = new javax.swing.GroupLayout(topPanel);
        topPanel.setLayout(topPanelLayout);
        topPanelLayout.setHorizontalGroup(
                topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(topPanelLayout.createSequentialGroup()
                                .addGap(16, 16, 16)
                                .addGroup(topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(topPanelLayout.createSequentialGroup()
                                                .addComponent(lblPurpose)
                                                .addGap(34, 34, 34)
                                                .addComponent(lblSimTime)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(txtSimulationTime, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(topPanelLayout.createSequentialGroup()
                                                .addGap(404, 404, 404)
                                                .addComponent(txtSimTime, javax.swing.GroupLayout.PREFERRED_SIZE, 319, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap(92, Short.MAX_VALUE))
        );
        topPanelLayout.setVerticalGroup(
                topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(topPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(topPanelLayout.createSequentialGroup()
                                                .addComponent(lblPurpose)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGroup(topPanelLayout.createSequentialGroup()
                                                .addGroup(topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(lblSimTime, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(txtSimulationTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                                .addComponent(txtSimTime))
        );

        txtSimTime.getAccessibleContext().setAccessibleDescription("simTime");

        sidePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("ConnectionModel"));
        sidePanel.setMinimumSize(new java.awt.Dimension(45, 45));

        txtConnectionList.setColumns(1);
        txtConnectionList.setLineWrap(true);
        txtConnectionList.setRows(62);
        txtConnectionList.setFocusCycleRoot(true);
        txtConnectionList.setMinimumSize(new java.awt.Dimension(22, 22));
        jScrollPane1.setViewportView(txtConnectionList);

        javax.swing.GroupLayout sidePanelLayout = new javax.swing.GroupLayout(sidePanel);
        sidePanel.setLayout(sidePanelLayout);
        sidePanelLayout.setHorizontalGroup(
                sidePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane1)
        );
        sidePanelLayout.setVerticalGroup(
                sidePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );

        imagePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Layout"));
        imagePanel.setMinimumSize(new java.awt.Dimension(45, 45));

        jLabel1.setIcon(imageIcon); // NOI18N
        jLabel1.setText("jLabel1");
        jLabel1.setMinimumSize(new java.awt.Dimension(45, 45));

        javax.swing.GroupLayout imagePanelLayout = new javax.swing.GroupLayout(imagePanel);
        imagePanel.setLayout(imagePanelLayout);
        imagePanelLayout.setHorizontalGroup(
                imagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 559, Short.MAX_VALUE)
        );
        imagePanelLayout.setVerticalGroup(
                imagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(topPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(sidePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(imagePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(topPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(sidePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(imagePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void createComponentMap() {
        componentMap = new HashMap<>();
        Component[] components = this.getContentPane().getComponents();
        for (Component component : components) {
            componentMap.put(component.getName(), component);
        }
    }

    @Override
    public String getConnectionList() {
        return this.connectionList;
    }

    @Override
    public void setConnectionList(String connList) {
        this.connectionList = connList;
        txtConnectionList.setText(connList);
    }

    @Override
    public GregorianCalendar getSimulationTime() {
        return this.simulationTime;
    }

    @Override
    public void setSimulationTime(GregorianCalendar cal) {
        this.simulationTime = cal;
        Date creationDate = cal.getTime();
        SimpleDateFormat date_format = new SimpleDateFormat("EEE, MMM d yyyy h:mm:ss a");
        this.txtSimulationTime.setText(date_format.format(creationDate));
    }

    @Override
    public int getSimulationTimeSlices() {
        return this.simulationTimeSlices;
    }

    @Override
    public void setSimulationTimeSlices(final int simulationTimeSlices) {
        this.simulationTimeSlices = simulationTimeSlices;
        this.txtSimTime.setText(String.valueOf(this.simulationTimeSlices));
    }

    @Override
    public Component getComponentByName(String name) {
        if (componentMap.containsKey(name)) {
            return componentMap.get(name);
        } else {
            return null;
        }
    }

    @Override
    public void initializeAndDisplay(String testCaseName) {
        updateData();
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setTitle("Connections - " + testCaseName);
        Date creationDate = this.simulationTime.getTime();
        SimpleDateFormat date_format = new SimpleDateFormat("EEE, MMM d yyyy h:mm:ss a");
        this.txtSimulationTime.setText(date_format.format(creationDate));
        this.txtConnectionList.setText(this.connectionList);
        this.txtSimTime.setText(String.valueOf(this.simulationTimeSlices));
        this.pack();
        this.setVisible(true);
        startListener();
    }

    @Override
    public void startListener() {
        LOG.debug("starting listener");
        Thread t = new Thread(() -> {
            while (true) {
                updateView();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    LOG.error("Connection view listener InterruptedException. {}", e.getCause().toString());
                    System.exit(-8);
                }
            }
        });
        t.start();
    }

    /**
     * Update the view with the given information.
     *
     * @param simulationTime       - the current simulated time
     * @param simulationTimeSlices - the number of time slices that have elapsed since the simulation began
     * @param connList             - the list of established connections
     */
    @Override
    public void updateView(GregorianCalendar simulationTime, int simulationTimeSlices, String connList) {
        this.simulationTime = simulationTime;
        this.simulationTimeSlices = simulationTimeSlices;
        this.connectionList = connList;

        Date creationDate = this.simulationTime.getTime();
        SimpleDateFormat date_format = new SimpleDateFormat("EEE, MMM d yyyy h:mm:ss a");
        this.txtSimulationTime.setText(date_format.format(creationDate));

        this.txtConnectionList.setText(this.connectionList);
        this.txtSimTime.setText(String.valueOf(this.simulationTimeSlices));
    }

    private void updateView() {
        updateData();
        Date creationDate = this.simulationTime.getTime();
        SimpleDateFormat date_format = new SimpleDateFormat("EEE, MMM d yyyy h:mm:ss a");
        this.txtSimulationTime.setText(date_format.format(creationDate));
        this.txtConnectionList.setText(this.connectionList);
        this.txtSimTime.setText(String.valueOf(this.simulationTimeSlices));
    }

    @Override
    public void updateData() {
        this.setConnectionList(ConnectionModel.getConnectionList());
        this.setSimulationTime(ConnectionModel.getSimulationTime());
        this.setSimulationTimeSlices(ConnectionModel.getSimulationTimeSlices());
    }

    private class ExitListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int confirm = JOptionPane.showOptionDialog(frame,
                    "Are you sure you want to close this application?",
                    "Exit Confirmation", JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null, null, null);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                System.exit(0);
            }
        }
    }

}
