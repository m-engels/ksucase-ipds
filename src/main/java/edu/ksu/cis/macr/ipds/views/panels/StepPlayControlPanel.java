package edu.ksu.cis.macr.ipds.views.panels;

import edu.ksu.cis.macr.aasis.agent.cc_m.IPersonaControlComponentMaster;
import edu.ksu.cis.macr.aasis.agent.persona.IAbstractBaseControlComponent;
import edu.ksu.cis.macr.aasis.agent.persona.IPersona;
import edu.ksu.cis.macr.ipds.primary.capabilities.participate.Participant;
import edu.ksu.cis.macr.obaa_pp.ec.base.IExecutionComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collection;

public class StepPlayControlPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private JButton btnRunContinously;
    private JButton btnStepOnce;
    private JButton btnPause;
    private JLabel displayText;
    private IAbstractBaseControlComponent abstractBaseCC;

    public StepPlayControlPanel(IAbstractBaseControlComponent abstractBaseCC) {
        super();

        this.abstractBaseCC = abstractBaseCC;

        // Adds runAction, stepAction, and pauseAction buttons for stepAction mode to a new panel for the gui
        btnRunContinously = new JButton(">>");
        btnStepOnce = new JButton(">");
        btnPause = new JButton("||");

        btnRunContinously.setToolTipText("Run continuously");
        btnStepOnce.setToolTipText("Step once");
        btnPause.setToolTipText("Pause");

        this.add(btnRunContinously, BorderLayout.SOUTH);
        //this.add(btnStepOnce, BorderLayout.SOUTH);
        this.add(btnPause, BorderLayout.SOUTH);

        displayText = new JLabel();
        displayText.setOpaque(false);
        displayText.setFont(new Font("Tahoma", 0, 12)); // NOI18N
        displayText.setBorder(null);
        this.add(displayText, BorderLayout.SOUTH);

        addButtonListeners();
     //   this.setPreferredSize(new Dimension(900, 50));
    }

    public final void addButtonListeners() {

        // Sets actions for runAction, pauseAction, and stepAction
        AbstractAction runAction = new AbstractAction("run") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == btnRunContinously) {
                    displayText.setText("");
                   // Collection<IAgent> agents = abstractBaseCC.getExecutionComponent().getOrganization().getAgents();
                    Collection<IPersona> agents = abstractBaseCC.getPersonaExecutionComponent().getOrganization().getAllPersona();
                    for (IExecutionComponent agent : agents) {
                        try {
                            IPersonaControlComponentMaster cc = (IPersonaControlComponentMaster) agent.getControlComponent();
                            cc.getPlayerCapability().unpause();
                        } catch (Exception ex) {
                            try {
                                Participant cc = (Participant) agent.getControlComponent();
                                cc.getPlayerCapability().unpause();
                            } catch (Exception ee) {
                            }
                        }
                    }
                }
            }
        };

        AbstractAction pauseAction = new AbstractAction("pause") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == btnPause) {
                    displayText.setText("Paused");
                    Collection<IPersona> agents = abstractBaseCC.getPersonaExecutionComponent().getOrganization().getAllPersona();
                    for (IExecutionComponent agent : agents) {
                        try {
                            IPersonaControlComponentMaster cc = (IPersonaControlComponentMaster) agent.getControlComponent();
                            cc.getPlayerCapability().pause();
                        } catch (Exception ex) {
                            try {
                                Participant cc = (Participant) agent.getControlComponent();
                                cc.getPlayerCapability().pause();
                            } catch (Exception ee) {
                            }
                        }
                    }
                }
            }
        };

        AbstractAction stepAction = new AbstractAction("step") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == btnStepOnce) {
                    displayText.setText("Stepping once");
                    Collection<IPersona> agents = abstractBaseCC.getPersonaExecutionComponent().getOrganization().getAllPersona();
                    for (IExecutionComponent agent : agents) {
                        IAbstractBaseControlComponent bcc = (IAbstractBaseControlComponent) agent.getControlComponent();
                        //bcc.step();
                    }
                }
            }
        };

        // Add listeners for the buttons
        btnPause.addActionListener(pauseAction);
        btnStepOnce.addActionListener(stepAction);
        btnRunContinously.addActionListener(runAction);

        // Maps keys to the buttons - Run: R, Pause: P, Step: Enter
        btnRunContinously.getInputMap(StepPlayControlPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("R"), "R");
        btnRunContinously.getActionMap().put("R", runAction);
        btnPause.getInputMap(StepPlayControlPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("P"), "P");
        btnPause.getActionMap().put("P", pauseAction);
        btnStepOnce.getInputMap(StepPlayControlPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), "ENTER");
        btnStepOnce.getActionMap().put("ENTER", stepAction);
    }
}
