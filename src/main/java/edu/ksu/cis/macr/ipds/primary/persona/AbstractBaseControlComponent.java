/**
 * Copyright 2012 Kansas State University MACR Laboratory
 * http://macr.cis.ksu.edu/ Department of Computing & Information Sciences
 *
 * See License.txt file for the license agreement.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package edu.ksu.cis.macr.ipds.primary.persona;


import edu.ksu.cis.macr.aasis.agent.cc_a.AbstractControlComponent;
import edu.ksu.cis.macr.aasis.agent.cc_message.IBaseMessage;
import edu.ksu.cis.macr.aasis.agent.ec_cap.IOrganizationCommunicationCapability;
import edu.ksu.cis.macr.aasis.agent.persona.IAbstractBaseControlComponent;
import edu.ksu.cis.macr.aasis.agent.persona.IPersona;
import edu.ksu.cis.macr.aasis.spec.OrganizationFocus;
import edu.ksu.cis.macr.goal.model.InstanceParameters;
import edu.ksu.cis.macr.ipds.primary.capabilities.participate.OrganizationCommunicationCapability;
import edu.ksu.cis.macr.ipds.primary.organizer.ReorganizationAlgorithm;
import edu.ksu.cis.macr.obaa_pp.cc.gr.EmptyGoalModel;
import edu.ksu.cis.macr.obaa_pp.cc.gr.IGoalModel;
import edu.ksu.cis.macr.obaa_pp.cc.om.EmptyOrganizationModel;
import edu.ksu.cis.macr.obaa_pp.cc.om.IOrganizationModel;
import edu.ksu.cis.macr.obaa_pp.cc_cip.IdentifierProviderBuilder;
import edu.ksu.cis.macr.obaa_pp.cc_message.AssignmentContent;
import edu.ksu.cis.macr.obaa_pp.cc_message.ModificationContent;
import edu.ksu.cis.macr.obaa_pp.cc_p.IdentifierProvider;
import edu.ksu.cis.macr.obaa_pp.events.IOrganizationEvents;
import edu.ksu.cis.macr.organization.model.*;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import java.io.File;
import java.util.Map;
import java.util.Objects;

/**
 * Class common to all control components in this application. Includes app-specific organization communication.
 */
public abstract class AbstractBaseControlComponent extends AbstractControlComponent implements IAbstractBaseControlComponent {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractBaseControlComponent.class);
    private static final Boolean debug = false;
    protected OrganizationFocus focus = OrganizationFocus.External;
    protected IOrganizationCommunicationCapability organizationCommunicationCapability;


    /**
     * Constructor for the abstract {@code AbstractBaseControlComponent} class.
     *
     * @param name      - the agent name
     * @param persona   - the application-specific execution component
     * @param knowledge - the agent's knowledge
     */
    protected AbstractBaseControlComponent(final String name, final IPersona persona,
                                           final Element knowledge, final OrganizationFocus focus) {
        super(name, persona, knowledge, focus);
        if (debug) LOG.debug("Entering constructor AbstractBaseControlComponent (name={}, persona=, knowledge={}, focus={})", name, persona, knowledge, focus);

        this.focus = focus;
//        if (this.focus == OrganizationFocus.External) {
//            return;
//        }

        organizationCommunicationCapability = new OrganizationCommunicationCapability(persona);
        LOG.info("Created OrganizationCommunicationCapability for control component master {}.", name);


        if (debug) LOG.info("Creating AbstractBaseControlComponent {}", name);
        if (debug) LOG.debug("The ccm.executionComponent.getOrganization().getGoalParameterValues() has {} entries.",
                persona.getOrganization().getGoalParameterValues().keySet().size());

        final String topgoal = Objects.requireNonNull(persona
                .getOrganization().getOrganizationSpecification().getTopGoal(), "Base control component needs top goal.");
        if (debug) LOG.debug("ABCC Constructor: The top goal is {}", topgoal);
        final String goalfile = Objects.requireNonNull(persona.getOrganization()
                .getOrganizationSpecification().getGoalFile(), "Base control component needs goal file.");
        if (debug) LOG.debug("\"ABCC Constructor: The goalfile is {}", goalfile);
        final String rolefile = Objects.requireNonNull(persona.getOrganization()
                .getOrganizationSpecification().getRoleFile(), "Base control component needs role file.");
        if (debug) LOG.debug("\"ABCC Constructor: The rolefile is {}", rolefile);

        if (debug) LOG.debug("Setting goal model from {} with TOP GOAL {}", goalfile, topgoal);
        IGoalModel goalModel = new EmptyGoalModel();

        File goalFile = new File(goalfile);
        try {
            checkFile(goalFile);
            if (debug) LOG.debug("The goal model file was found. {} ", goalfile);
        } catch (Exception e) {
            LOG.error("Error failed checking the goal model file at {}.", goalfile);
            System.exit(-723);
        }
        try {
            goalModel.populate(goalFile.toPath(), topgoal);
            if (debug) LOG.debug("The goal model file was populated. {} ", goalModel.toString());
        } catch (Exception e) {
            LOG.error("ERROR populating goal model: 1) Make sure no goals are numbered 0 - use a text editor to change. 2) Then link all leaf goals under AND or OR.  {}. {}", goalfile, e.getLocalizedMessage());
            System.exit(-724);
        }
        this.setGoalModel(goalModel);
        if (debug) LOG.debug("Goal model set.");

        try {
            LOG.info("Setting up the organization model for agent {} self control component master. {}", name, persona);
            IdentifierProvider capProvider = IdentifierProviderBuilder.create();

            LOG.info("Capability provider initialized. Setting organization model.");// CapProvider={}", capProvider.toString());
            //LOG.debug("Capability provider = {}", capProvider);

            final edu.ksu.cis.macr.organization.model.xml.SpecificationGoalProvider gm = this.getGoalModel();
            LOG.info("Goal model = {}", gm);
            File rf = new File(rolefile);
            LOG.info("Role file = {}", rf);
            final java.nio.file.Path rp;
            rp = new File(rolefile).toPath();
            LOG.info("Role file path = {}", rp);

            IOrganizationModel om = new EmptyOrganizationModel();
            LOG.info("Empty organization model = {}", om);
            om.populate(rp, gm, capProvider);
            LOG.info("Populated organization model = {}", om);
            this.setOrganizationModel(om);
            LOG.info("The new organization model is = {}", getOrganizationModel());
            LOG.info("Org model capabilities = {}", this.getOrganizationModel().getCapabilities());
            if (debug) LOG.debug("New org model includes: {}. ", this.organizationModel.getCapabilities().toString());

            if ( this.organizationModel.getCapabilities().size() == 0){
                LOG.error("ERROR: Org model has no capabilities. Nothing can get done.");
                System.exit(-9);
            }


        } catch (Exception ex) {
            LOG.error("Error: could not set org model from {}. {}", rolefile, ex.getLocalizedMessage());
            System.exit(-5);
        }
        if (debug) LOG.debug("Setting reorg algo for {}", name);
        setReorganizationAlgorithm(ReorganizationAlgorithm.createReorganizationAlgorithm(name));
        if (debug) LOG.debug("Reorg algo set for {}", name);

        LOG.info("Exiting constructor AbstractBaseControlComponent: this={}", this.toString());
    }

    protected static boolean checkFile(final File file) {
        if (file.exists()) {
            if (file.isFile()) {
                if (file.canRead()) {
                    return true;
                } else {
                    LOG.error(String.format("File (%s) cannot be read", file));
                }
            } else {
                LOG.error(String.format("File (%s) is not a file", file));
            }
        } else {
            LOG.error(String.format("File (%s) does not exist", file));
        }
        return false;
    }

    @Override
    public IOrganizationEvents getOrganizationEvents() {
        return this.organizationEvents;
    }

    /**
     * This {@code IAgent} is a part of the {@code AbstractControlComponent}.
     */
    @Override
    public IPersona getPersona() {
        return this.getOwner();
    }

    @Override
    public void setPersona(IPersona persona) {
        this.setOwner(persona);
    }

    protected Agent<UniqueIdentifier> initializeECAgent() {
        IOrganizationModel organizationKnowledge = getOrganizationModel();
        if (debug)LOG.debug("organizationKnowledge = {}",organizationKnowledge);

        final UniqueIdentifier ecAgentIdentifier = getPersonaExecutionComponent().getUniqueIdentifier();
        LOG.info("ecAgentIdentifier = {}",ecAgentIdentifier);

        final Agent<UniqueIdentifier> ecAgent = new AgentImpl<UniqueIdentifier>(ecAgentIdentifier);
        if (debug)LOG.debug("Adding execution component agent {}",ecAgent);

        organizationKnowledge.addAgent(ecAgent);

        IPersona persona = Objects.requireNonNull(getPersonaExecutionComponent());
        if (debug)LOG.debug("Persona = {}",persona);

        for (final Map.Entry<UniqueIdentifier, ? extends Capability> entry : persona.getCapabilitiesMapping().entrySet()) {
            if (debug)  LOG.debug("CC (Abstract base class) Initializing. Agent={}, capability entry={}", ecAgent.getIdentifier(),  entry);
            final UniqueIdentifier capabilityIdentifier = entry.getKey();
            if (debug)LOG.debug("capabilityIdentifier={}",capabilityIdentifier);

            if (organizationKnowledge.getCapability(capabilityIdentifier) == null) {
                /* ensures that the capability exists in the model  <-- we want to let this slide */
                final String error = String
                        .format("Capability (%s) does not exist in the organization model",
                                capabilityIdentifier);
                //System.err.println(error);
                // just note it and forget it
                LOG.info("Agent Capability not found in organization model: {} {}", ecAgent.getIdentifier(), capabilityIdentifier.toString() + ". Warning: " + error);
            } else {
                final Capability capability = entry.getValue();
                final double score = persona.getCapabilityScore(capability);
                try {
                    organizationKnowledge.specifyPossessesRelation(ecAgentIdentifier, capabilityIdentifier, score);
                    if (debug) LOG.debug("{} possesses {} with score {}", ecAgentIdentifier, capabilityIdentifier, score);
                } catch (Exception ex) {
                    doNothing();
                }
            }
        }
        LOG.info("exiting initializeECAgent() Agent: \"{}\": {} - Capabilities", ecAgent.getIdentifier(), ecAgent.getPossessesSet());
        return ecAgent;
    }

    private void doNothing() {
        //just continue
    }

    protected void processAssignment(final IBaseMessage message) {
        final Object content = message.getContent();
        if (content instanceof AssignmentContent) {
            if (debug) LOG.info("Processing new assignment message. Message={}", message.toString());
            final AssignmentContent assignmentContent = (AssignmentContent) content;
            if (debug) LOG.info("Processing assignment. {}", assignmentContent);

            final Agent<?> agent = getOrganizationModel().getAgent(assignmentContent.getAgentIdentifier());
            if (debug) LOG.debug("Assignment agent = {}", agent.toString());

            final UniqueIdentifier roleIdentifier = assignmentContent.getRoleIdentifier();
            if (debug) LOG.debug("Assignment roleIdentifier = {}", roleIdentifier.toString());

            final UniqueIdentifier specGoalIdentifier = assignmentContent.getSpecificationGoalIdentifier();
            if (debug) LOG.debug("Assignment specGoalIdentifier = {}", specGoalIdentifier.toString());

            final UniqueIdentifier instGoalIdentifier = assignmentContent.getInstanceGoalIdentifier();
            if (debug) LOG.debug("Assignment instGoalIdentifier = {}", instGoalIdentifier.toString());

            Object params = assignmentContent.getParameter();
            if (debug) LOG.debug("Assignment parameter = {}", params.toString());

            // basic info obtained....
            if (getOrganizationModel().getRoles().size() == 0) {
                LOG.error("ERROR: The organization model for {} has no entries. Please verify the organization model was initialized correctly.", this.getOwner().toString());
                System.exit(-78);
            }

            final Role role = getOrganizationModel().getRole(roleIdentifier);
            if (debug) LOG.debug("role = {}", role.toString());

            final SpecificationGoal specGoal = getOrganizationModel().getSpecificationGoal(specGoalIdentifier);
            if (debug) LOG.debug("specGoal = {}", specGoal.toString());

            if (specGoal == null) {
                LOG.error("The assignment spec goal cannot be null.");
                System.exit(-62);
            }

            final InstanceGoal<?> instanceGoal = specGoal.getInstanceGoal(instGoalIdentifier, params);
            if (debug) LOG.debug("instanceGoal = {}", instanceGoal.toString());

            final Assignment assignment = new Assignment(agent, role, instanceGoal);
            if (debug) LOG.debug("New assignment = {}. Adding to the EC list.", assignment.toString());

            getPersonaExecutionComponent().addAssignment(assignment);
        }
    }

    protected void processDeAssignment(final IBaseMessage message) {
        final Object content = message.getContent();
        if (content instanceof AssignmentContent) {
            final AssignmentContent assignmentContent = (AssignmentContent) content;
            LOG.info("Processing de-assignment. {}", assignmentContent);
            final Agent<?> agent = getOrganizationModel().getAgent(assignmentContent.getAgentIdentifier());
            final Role role = getOrganizationModel().getRole(assignmentContent.getRoleIdentifier());
            final SpecificationGoal specificationGoal = getOrganizationModel()
                    .getSpecificationGoal(assignmentContent.getSpecificationGoalIdentifier());
            final InstanceGoal<?> instanceGoal = specificationGoal
                    .getInstanceGoal(assignmentContent.getInstanceGoalIdentifier(), assignmentContent.getParameter());
            final Assignment assignment = new Assignment(agent, role, instanceGoal);
            getPersonaExecutionComponent().addDeAssignment(assignment);
        }
    }

    protected void processGoalModification(final IBaseMessage message) {
        final Object content = message.getContent();
        if (content instanceof ModificationContent) {
            final ModificationContent modificationContent = (ModificationContent) content;
            LOG.info("Processing goal modification. {}", modificationContent);
            final SpecificationGoal specificationGoal = getOrganizationModel()
                    .getSpecificationGoal(modificationContent.getSpecificationGoalIdentifier());
            final InstanceGoal<InstanceParameters> instanceGoal = specificationGoal
                    .getInstanceGoal(
                            modificationContent.getInstanceGoalIdentifier(),
                            modificationContent.getInstanceParameters());
            getPersonaExecutionComponent().addGoalModification(instanceGoal);
        }
    }


}
