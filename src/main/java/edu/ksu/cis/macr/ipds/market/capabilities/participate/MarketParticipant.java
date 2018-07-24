package edu.ksu.cis.macr.ipds.market.capabilities.participate;


import edu.ksu.cis.macr.aasis.agent.cc_message.participate.ParticipateMessage;
import edu.ksu.cis.macr.aasis.agent.cc_message.participate.ParticipatePerformative;
import edu.ksu.cis.macr.aasis.agent.cc_p.IBaseControlComponentSlave;
import edu.ksu.cis.macr.aasis.agent.persona.IPersona;
import edu.ksu.cis.macr.aasis.spec.OrganizationFocus;
import edu.ksu.cis.macr.ipds.config.RunManager;
import edu.ksu.cis.macr.ipds.primary.capabilities.participate.Participant;
import edu.ksu.cis.macr.obaa_pp.cc.om.IOrganizationModel;
import edu.ksu.cis.macr.obaa_pp.cc_message.RegistrationContent;
import edu.ksu.cis.macr.organization.model.Agent;
import edu.ksu.cis.macr.organization.model.Capability;
import edu.ksu.cis.macr.organization.model.identifiers.StringIdentifier;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
/**
 * Adds the organization-specific functionality needed for participating.
 */
public class MarketParticipant extends Participant implements IBaseControlComponentSlave {
    private static final Logger LOG = LoggerFactory.getLogger(MarketParticipant.class);
    private static final boolean debug = false;

    /**
     * A control component slave ecAgent registers with the local control component master ecAgent.
     *
     * @param name      - unique name of agent
     * @param persona   - the subagent
     * @param knowledge - the XML knowledge about the organization
     * @param focus     - the enum of what kind of focus the organization is
     */
    public MarketParticipant(final String name, final IPersona persona, final Element knowledge, final OrganizationFocus focus) {
        super(name, persona, knowledge, focus);
        LOG.info("Entering constructor MarketParticipant( name={}, persona={}, knowledge={}, focus={}.", name, persona, knowledge, focus);

        try {
             this.masterOrgName = persona.getOrganizationFromPersonaName(persona.getUniqueIdentifier().toString());
        } catch (Exception e) {
            LOG.error("Error in constructor.");
            System.exit(-88);
        }
    }

    protected void doRegistration() {
        if (debug) LOG.debug("Beginning doRegistration().");
        IOrganizationModel knowledge = getOrganizationModel();
        Objects.requireNonNull(knowledge);

        Agent<?> agent = knowledge.getAgent(getPersonaExecutionComponent().getUniqueIdentifier());
        Objects.requireNonNull(agent);
        Map<UniqueIdentifier, Double> capabilities = new HashMap<UniqueIdentifier, Double> ();

        /* verify that ecAgent entity is updated with the latest scores */
        for (final Capability capability : getPersonaExecutionComponent().getCapabilities()) {
            Objects.requireNonNull(capability);
            final double score = getPersonaExecutionComponent().getCapabilityScore(capability);
            // Updated DMC - 10/6/2013 - to handle the case when the agent has a capability (score = 1) but the
            // capability is not needed (possesses = 0.0)
            final double agentPossessesScore = agent.getPossessesScore(capability.getIdentifier());
            if (score != agentPossessesScore && agentPossessesScore != 0.0 && score == 1.0) {
                agent.setPossessesScore(capability.getIdentifier(), score);
            }
            capabilities.put(capability.getIdentifier(), score);
        }
        if (debug) LOG.debug("In doRegistration() {}", String.format("Agent: \"%s\": %s",
                agent.getIdentifier(), agent.getPossessesSet()));
        final RegistrationContent registrationContent = new RegistrationContent(getPersonaExecutionComponent()
                .getUniqueIdentifier(), capabilities);
        if (debug) LOG.debug("registration content: {}", registrationContent);
        String org = this.getPersonaExecutionComponent().getOrganizationFromPersonaName(getPersonaExecutionComponent().getUniqueIdentifier().toString());
        if (debug) LOG.debug("My self org is {}.", org);
        String host = this.getPersonaExecutionComponent().getHostFromIdentifier(getPersonaExecutionComponent().getUniqueIdentifier());
        if (debug) LOG.debug("I'm running on host {}.", host);
        if (debug) LOG.debug("This is  {} in organization {} running on node {}", this.getOwner().getUniqueIdentifier().toString(), org, host);
        this.startTime = System.currentTimeMillis();  // will be used to see if we timeout

        if (debug) LOG.debug("Internal persona is attempting to register. org={} node={} ", org, host);
        final ParticipateMessage reasoningMessage = new ParticipateMessage(getPersonaExecutionComponent()
                .getUniqueIdentifier(), StringIdentifier.getIdentifier("broadcast"),
                ParticipatePerformative.BROADCASTING_AGENT_REGISTRATION, registrationContent);
        if (debug) LOG.debug("reasoning message: {}", reasoningMessage);
        if (reasoningMessage != null) {
            if (this.internalOrganizationCommunicationCapability.broadcast(reasoningMessage)) {
            /* broadcast was successful, so proceed to next state */
                this.state = ExecutionState.CONFIRMING_REGISTRATION;
                LOG.debug("REGISTRATION MESSAGE broadcast. {} messages on queue.  Moving to {}.  MESSAGE={}. internal org messages are: {}",
                        this.internalOrganizationCommunicationCapability.getInternalOrganizationMessages().size(),
                        this.state, reasoningMessage, this.internalOrganizationCommunicationCapability.getInternalOrganizationMessages());
            } else {
            /* broadcast has failed, so retry again in a bit */
                this.state = ExecutionState.RETRYING_REGISTRATION;
                LOG.error("failed broadcast of registration message: {}", this.state);
                System.exit(-4222);
            }
        }
    }

    protected void doRegistrationConfirmation() {
        if (debug) LOG.debug("External SLAVE doRegistrationConfirmation.....");
        if (this.internalOrganizationCommunicationCapability.messages() > 0) {
            final ParticipateMessage message = (ParticipateMessage) this.internalOrganizationCommunicationCapability.receiveLocal();
            if (debug)
                LOG.debug("In External CC SLAVE confirming reg. Message perf = {}", message.getPerformativeType());
            switch (message.getPerformativeType()) {
                case BROADCASTING_AGENT_REGISTRATION:
                    /*
                     * it is possible to receive ecAgent registrations from other
                     * agents while waiting for a confirmation, so ignore it
                     */
                    if (debug) LOG.debug("doRegConf() ignoring in order to confirm: {}", message);
                    this.state = ExecutionState.TIMING_OUT_DURING_CONFIRMATION;
                    break;
                case AGENT_REGISTRATION_CONFIRMATION:
                    if (debug) LOG.debug("Got AGENT_REGISTRATION_CONFIRMATION. content is {}", message.getContent());
                    final Object content = message.getContent();
                    if (content instanceof UniqueIdentifier) {
                        this.master = (UniqueIdentifier) content;
                        if (debug) LOG.debug("In registration confirmation external master =({})", this.master);

                        this.setLocalMaster(this.master);
                        if (debug) LOG.debug("doRegistrationConfirmation()confirmation from {}", this.master);
                        this.internalOrganizationCommunicationCapability.sendLocal(new ParticipateMessage(getPersonaExecutionComponent()
                                .getUniqueIdentifier(), this.master,
                                ParticipatePerformative.AGENT_REGISTRATION_CONFIRMATION_RECEIVED, null));
                        this.state = ExecutionState.WORKING;
                    }
                    break;
                default:
                    // other performatives should NOT be received at all because they are directed and this ecAgent CANNOT have any of those
                    LOG.error("Unacceptable performative in doRegistrationConfirmation: {}. content = {}", message.getPerformativeType(), message.getContent().toString());
                    System.exit(-19);
                    //   throw new IllegalArgumentException(String.format("Unacceptable Performatives \"%s\"",
                    //          message.getPerformativeType()));
            }
        } else {
            if (debug) LOG.debug("In CC SLAVE confirming registration, but no messages to process.");
            this.state = ExecutionState.TIMING_OUT_DURING_CONFIRMATION;
        }
    }


}
