package edu.ksu.cis.macr.ipds.views.panels;

import edu.ksu.cis.macr.obaa_pp.views.panels.AbstractListVisualizationPanel;
import edu.ksu.cis.macr.organization.model.Agent;
import edu.ksu.cis.macr.organization.model.Organization;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 The {@code AgentVisualizationPanel} class is a Swing component to visualize the {@code Agent} currently in the {@code Organization}.

 @author Christopher Zhong
 @version $Revision: 1.3.4.4 $, $Date: 2011/09/19 14:26:34 $
 @since 3.4 */
public class AgentVisualizationPanel extends AbstractListVisualizationPanel<Agent<?>> {

  /**
   Default serial version ID.
   */
  private static final long serialVersionUID = 1L;

  /**
   Constructs a new instance of {@code AgentVisualizationPanel}.

   @param organization the {@code Organization} that is used to visualize the {@code Agent}.
   */
  public AgentVisualizationPanel(final Organization organization) {
    super(organization, "Persona",
            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED,
            JSplitPane.VERTICAL_SPLIT, true,
            ListSelectionModel.SINGLE_SELECTION, JList.VERTICAL, 5);
  }

  @Override
  public List<Agent<?>> getCollection() {
    final List<Agent<?>> agents = new ArrayList<>(getOrganization()
            .getAgents());
    final Comparator<Agent<?>> comparator = (agent1, agent2) -> agent1.toString().compareTo(agent2.toString());
    Collections.sort(agents, comparator);
    return agents;
  }

  @Override
  public synchronized void valueChanged(final ListSelectionEvent e) {
    final Agent<?> agent = getList().getSelectedValue();
    if (agent != null) {
      getDetailedInformationPanel().showDetailedInformation(agent);
    }
  }
}
