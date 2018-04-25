package edu.ksu.cis.macr.ipds.views;

import edu.ksu.cis.macr.aasis.agent.cc_m.IPersonaControlComponentMaster;
import edu.ksu.cis.macr.aasis.agent.persona.IAbstractBaseControlComponent;
import edu.ksu.cis.macr.aasis.agent.persona.IOrganization;
import edu.ksu.cis.macr.aasis.agent.persona.IPersona;
import edu.ksu.cis.macr.aasis.self.IInnerOrganization;
import edu.ksu.cis.macr.ipds.views.panels.LocalMessagePanel;
import edu.ksu.cis.macr.ipds.views.panels.StepPlayControlPanel;
import edu.ksu.cis.macr.obaa_pp.cc.om.IOrganizationModel;
import edu.ksu.cis.macr.obaa_pp.cc_m.IControlComponentMaster;
import edu.ksu.cis.macr.obaa_pp.views.IOrganizationView;
import edu.ksu.cis.macr.obaa_pp.views.panels.AgentVisualizationPanel;
import edu.ksu.cis.macr.obaa_pp.views.panels.AssignmentVisualizationPanel;
import edu.ksu.cis.macr.obaa_pp.views.panels.GoalVisualizationPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import java.awt.*;
import java.awt.event.*;
import java.util.Collection;

/**
 * The {@code OrganizationView} provides a basic view for visualizing the SelfOrganization
 */
public class OrganizationView extends JFrame implements IOrganizationView {
    private static final Dimension DEFAULT_PREFERRED_SIZE = new Dimension(225, 900);
    private static final Logger LOG = LoggerFactory.getLogger(OrganizationView.class);
    private static final int CONNECTION_FRAME_WIDTH = 875;
    private static final int NUM_LEVELS = 5; // ss, feeder, lateral, neighborhood, home
    private static final int PREFERRED_WIDTH = 200;
    private static final boolean debug = false;
    private static final long serialVersionUID = 1L;
    private static boolean stepInLauncher = true;
    private JMenuBar MenuBar = new JMenuBar();
    private JFrame frame = new JFrame();
    private JMenu File = new JMenu("File");
    private JMenuItem Exit = new JMenuItem("Exit");


    static {
        final LookAndFeelInfo[] installedLookAndFeels = UIManager.getInstalledLookAndFeels();
        try {
            for (final LookAndFeelInfo info : installedLookAndFeels) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
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
     Constructs a new instance of {@code OrganizationView}.

     @param self - the self organization to display
     */
    private OrganizationView(IInnerOrganization self) {
       // super(self.getName() + " Organization View");
           LOG.info("Entering OrganizationView(IAgentInternalOrganization={}).", self);
        addStandardFeatures();

        String name = self.getName();
        frame.setTitle(self.getName());
        LOG.info("Constructing agent organizationModel {} view.", self.getName());

        IPersonaControlComponentMaster master = self.getSelfControlComponentMaster();
        LOG.info("PersonaControlComponentMaster={}.", master);

       IOrganizationModel om = master.getOrganizationModel();
        if (debug) LOG.debug("Organization model={}.", om);

        Color color = getColor(master.getIdentifier().toString());
        if (debug) LOG.debug("Color={}", color);

        int level = getTypeNumber(master.getIdentifier().toString());
        if (debug) LOG.debug("Level={}", level);

        JPanel agentPanel = new AgentVisualizationPanel(om);
        if (debug) LOG.debug("{} new agent panel", name);
        JPanel goalPanel = new GoalVisualizationPanel(om);
        if (debug) LOG.debug("{} new goal panel", name);
        JPanel assignmentPanel = new AssignmentVisualizationPanel(om);
        if (debug) LOG.debug("{} new assignment panel", name);

        JPanel goalsAndAgentsPanel = new JPanel(new GridLayout(1, 2));
        JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, goalPanel, agentPanel);
        sp.setResizeWeight(0.5);
        sp.setOneTouchExpandable(false);
        goalsAndAgentsPanel.add(sp);
        if (debug) LOG.debug("{} new goals and agents panel", name);

        // JPanel electricalChartPanel = new JPanel(new GridLayout(1,1));
        //final SmartMeterChartPanel chartPanel = new SmartMeterChartPanel();

          JPanel messageAndControlPanel = new JPanel(new GridLayout(1, 1));
           final LocalMessagePanel messageBoxPanel = new LocalMessagePanel(self.getSelfControlComponentMaster());
           messageBoxPanel.startListener(self.getSelfControlComponentMaster().getPersonaExecutionComponent());

           StepPlayControlPanel controlPanel = new StepPlayControlPanel((IAbstractBaseControlComponent) self.getSelfControlComponentMaster());
           controlPanel.setBackground(color);
           JSplitPane spMsg = new JSplitPane(JSplitPane.VERTICAL_SPLIT, messageBoxPanel, controlPanel);
           spMsg.setResizeWeight(0.8);
           spMsg.setOneTouchExpandable(false);
            messageAndControlPanel.add(spMsg);

        JPanel overallPanel = new JPanel(new GridLayout(3, 1));
        overallPanel.add(goalsAndAgentsPanel);
        if (debug) LOG.debug("{} added goals and agents panel", name);
        overallPanel.add(assignmentPanel);
        if (debug) LOG.debug("{} added assignments panel", name);

        frame.add(overallPanel);
        LOG.info("Added overall panel. If the simulation stops here, please restart.");

        //  overallPanel.add(electricalChartPanel);
        overallPanel.add(messageAndControlPanel);

        //  setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenHeight = screenSize.height;
        int screenWidth = screenSize.width;
        setSize(screenWidth / 2, screenHeight / 2);
        setLocation(screenWidth / 4, screenHeight / 4);

        Container container = getContentPane();
        container.add(overallPanel, BorderLayout.CENTER);
        container.setPreferredSize(new Dimension(225, screenHeight*3/4));
        frame.add(container);

        double horiz_total_space = screenSize.getSize().getWidth() - CONNECTION_FRAME_WIDTH;
        if (debug) LOG.debug("horiz_total_space={}", horiz_total_space);
        int startHorizontal = CONNECTION_FRAME_WIDTH + (level-1)* (int) horiz_total_space/NUM_LEVELS;

        int startVertical = 0;
          frame.setLocation(startHorizontal, startVertical);
        if (debug) LOG.debug("set location x={}, y={}", frame.getX(), frame.getY());

        this.startListener();
        if (debug) LOG.debug("Started listener");
        LOG.info("If the simulation stops here, please restart.");
        frame.pack();
        frame.setVisible(true);
    }



    public synchronized static IOrganizationView createOrganizationView(IInnerOrganization self) {
        return new OrganizationView(self);
    }

    public OrganizationView() {
        addStandardFeatures();

        frame.pack();
        frame.setVisible(true);
    }

    private synchronized void addStandardFeatures() {
        File.add(Exit);
        MenuBar.add(File);
        Exit.addActionListener(new ExitListener());
        WindowListener exitListener = new WindowAdapter() {
            @Override
            public synchronized void windowClosing(WindowEvent e) {
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
        frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);                       //Denise - Changed this line from EXIT_ON_CLOSE to that. let me know if it works for you
        frame.setJMenuBar(MenuBar);
    }

    /**
     * Constructs a new instance of {@code OrganizationView}.
     *
     * @param org - the self organization to display
     */
    private OrganizationView(IOrganization org) {
       // super(organization.getName() + " View");
        LOG.info("Entering OrganizationView(IOrganization={}).", org);
        String name = org.getName();
        addStandardFeatures();

        frame.setTitle(name);
        final Collection<IPersona> allAgents = org.getAllPersona();
        if (debug) LOG.debug("allAgents size is {}.", allAgents.size());

        String localMaster = null;
        IControlComponentMaster master = null;
        for (IPersona a : allAgents) {

            LOG.debug("agent={}",a);
             if (a.getPersonaControlComponent().getClass().toString().toLowerCase().contains("master")) {
                master = (IControlComponentMaster)a.getPersonaControlComponent();
                localMaster = a.getUniqueIdentifier().toString();
                LOG.info("Local master is {}.", localMaster);
                org.setLocalMaster(localMaster.toString());
                 break;
             }
        }

        IOrganizationModel om = master.getOrganizationModel();
        if (debug) LOG.debug("Organization model={}.", om);

        Color color = getColor(localMaster.toString());
        if (debug) LOG.debug("Color={}", color);

        int level = getTypeNumber(master.toString());
        if (debug) LOG.debug("Level={}", level);

        JPanel agentPanel = new AgentVisualizationPanel(om);
        if (debug) LOG.debug("{} new agent panel", name);

        JPanel goalPanel = new GoalVisualizationPanel(om);
        if (debug) LOG.debug("{} new goal panel", name);

        JPanel assignmentPanel = new AssignmentVisualizationPanel(om);
        if (debug) LOG.debug("{} new assignment panel", name);

        JPanel goalsAndAgentsPanel = new JPanel(new GridLayout(1, 2));
        JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, goalPanel, agentPanel);

        sp.setResizeWeight(0.5);
        sp.setOneTouchExpandable(false);
        goalsAndAgentsPanel.add(sp);
        if (debug) LOG.debug("{} new goals and agents panel", name);

        // JPanel electricalChartPanel = new JPanel(new GridLayout(1,1));
        //final SmartMeterChartPanel chartPanel = new SmartMeterChartPanel();

        //  JPanel messageAndControlPanel = new JPanel(new GridLayout(1, 1));
        //   final LocalMessagePanel messageBoxPanel = new LocalMessagePanel(self.getControlComponentMaster());
        //   messageBoxPanel.startListener(self.getControlComponentMaster().getAgent());

        //   StepPlayControlPanel controlPanel = new StepPlayControlPanel((IAbstractBaseControlComponent) self.getControlComponentMaster());
        //   controlPanel.setBackground(color);
        //   JSplitPane spMsg = new JSplitPane(JSplitPane.VERTICAL_SPLIT, messageBoxPanel, controlPanel);
        //   spMsg.setResizeWeight(0.8);
        //   spMsg.setOneTouchExpandable(false);
        //    messageAndControlPanel.add(spMsg);

        JPanel overallPanel = new JPanel(new GridLayout(3, 1));
        overallPanel.add(goalsAndAgentsPanel);
        if (debug) LOG.debug("{} added goals and agents panel", name);
        overallPanel.add(assignmentPanel);
        if (debug) LOG.debug("{} added assignments panel", name);

        frame.add(overallPanel);
        LOG.info("Added overall panel. If the simulation stops here, please restart.");

        //  overallPanel.add(electricalChartPanel);
        //overallPanel.add(messageAndControlPanel);

      //  setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        Container container = getContentPane();
        container.add(overallPanel, BorderLayout.CENTER);
        container.setPreferredSize(DEFAULT_PREFERRED_SIZE);
        if (debug) LOG.debug("{} set sizes", name);

        frame.add(container);
        // figure out where to position the view

        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

        double horiz_total_space = d.getSize().getWidth() - CONNECTION_FRAME_WIDTH;
        if (debug) LOG.debug("horiz_total_space={}", horiz_total_space);
        int startHorizontal = CONNECTION_FRAME_WIDTH + (level-1)* (int) horiz_total_space/NUM_LEVELS;

//        int startHorizontal = (int) d.getWidth() / 2 - (int) d.getWidth() -
//                (int) PREFERRED_WIDTH / 2 +
//                (level - 1) * (int) PREFERRED_WIDTH;

        if (debug) LOG.debug("startHorizontal={}", startHorizontal);
      //  setLocation(startHorizontal, START_VERTICAL);
        if (debug) LOG.debug("set location");

        this.startListener();
        if (debug) LOG.debug("Started listener");
        LOG.info("If the simulation stops here, please restart.");
        frame.pack();
        frame.setVisible(true);    }

    public synchronized static IOrganizationView createOrganizationView(IOrganization org) {
        return new OrganizationView(org);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public synchronized void run() {
                OrganizationView view = new OrganizationView();
            }
        });
    }

    private int getTypeNumber(final String masterName) {
        char first = masterName.replace("self", "").toUpperCase().charAt(0);
        switch (first) {
            case 'H':
                return 1;
            case 'N':
                return 2;
            case 'L':
                return 3;
            case 'F':
                return 4;
            default:
                return 5;
        }
    }

    private synchronized void startListener() {
        if (debug) LOG.debug("starting listener");
        Thread t = new Thread(() -> {
            while (true) {
                updateView();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    LOG.error("Organization view listener InterruptedException. {}", e.getCause().toString());
                    System.exit(-8);
                }
            }
        });
        t.start();
    }

    @Override
    public synchronized void updateView() {
        //   setTimeSliceNumber(Clock.getTimeSlicesElapsedSinceStart());
        //  this.setTimeSliceNumber(timeSliceNumber);
    }

    private Color getColor(final String masterName) {
        char first = masterName.replace("self", "").toUpperCase().charAt(0);
        switch (first) {
            case 'H':
                return new Color(2, 132, 130);  // teal
            case 'N':
                return new Color(122, 186, 122);  // green
            case 'L':
                return new Color(183, 110, 184);  //magenta
            case 'F':
                return new Color(31, 102, 153);  // blue
            default:
                return new Color(102, 102, 102);  // grey
        }
    }

    private void readObject(java.io.ObjectInputStream stream) throws java.io.IOException, ClassNotFoundException {
        stream.defaultReadObject();
    }

    private synchronized void writeObject(java.io.ObjectOutputStream stream) throws java.io.IOException {
        stream.defaultWriteObject();
    }

    private class ExitListener implements ActionListener {
        @Override
        public synchronized void actionPerformed(ActionEvent e) {
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
