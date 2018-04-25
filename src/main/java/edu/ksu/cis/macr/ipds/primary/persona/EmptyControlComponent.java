/*
 * EmptyControlComponent.java
 *
 * Created on Feb 8, 2005
 *
 * See License.txt file the license agreement.
 */
package edu.ksu.cis.macr.ipds.primary.persona;

import edu.ksu.cis.macr.aasis.agent.cc_a.AbstractControlComponent;
import edu.ksu.cis.macr.aasis.agent.persona.IPersona;
import edu.ksu.cis.macr.aasis.spec.OrganizationFocus;
import edu.ksu.cis.macr.obaa_pp.events.IOrganizationEvents;
import org.w3c.dom.Element;

/**
 The {@code EmptyControlComponent} class represents a non-existent {@code IControlComponent}.

 @author Christopher Zhong
 @version $Revision: 1.1.2.2 $, $Date: 2011/09/19 14:25:39 $
 @see AbstractControlComponent
 @since 1.0 */
public class EmptyControlComponent extends AbstractControlComponent {

  private boolean isPaused;

  /**
   Constructs a new instance of {@code EmptyControlComponent}.

   @param name the name of the agent.
   @param executionComponent the type of the agent.
   @param organization the organization information for the agent.
   */
  public EmptyControlComponent(final String name,
                               final IPersona executionComponent,
                               final Element organization, OrganizationFocus focus) {
    super(name, executionComponent, organization, focus);
    this.isPaused = false;

  }

  /**
   The {@code content} that will be channeled by extensions.

   @param content the {@code content} to be passed along the {@code ICommunicationChannel}.
   */
  @Override
  public void channelContent(final Object content) {

  }

  @Override
  public void executeControlComponentPlan() {
        /* Do Nothing */
  }


  @Override
  public IOrganizationEvents getOrganizationEvents() {
    return null;
  }

  @Override
  public void updateAgentInformation() {
        /* Nothing To Do */
  }


}
