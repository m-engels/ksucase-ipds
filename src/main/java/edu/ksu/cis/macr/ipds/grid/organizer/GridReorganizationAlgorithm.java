package edu.ksu.cis.macr.ipds.grid.organizer;

import edu.ksu.cis.macr.obaa_pp.cc.om.IOrganizationModel;
import edu.ksu.cis.macr.obaa_pp.cc.reorg.AbstractReorganizationAlgorithm;
import edu.ksu.cis.macr.organization.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 The {@code ReorganizationAlgorithm} extends {@code AbstractReorganizationAlgorithm} to provide a custom algorithm for
 adapting the participant assignments to best meet the organization goals.
 */
public class GridReorganizationAlgorithm extends AbstractReorganizationAlgorithm {

  private static final Logger LOG = LoggerFactory.getLogger(GridReorganizationAlgorithm.class);
  private static final boolean debug = false;
  private static String owner;
  private Comparator<Agent<?>> agentComparator = (agent1, agent2) -> agent1.getIdentifier().toString()
          .compareTo(agent2.getIdentifier().toString());
  private Comparator<InstanceGoal<?>> goalComparator = (goal1, goal2) -> goal1.getIdentifier().toString()
          .compareTo(goal2.getIdentifier().toString());

  /**
   Create an instance of a {@code ReorganizationAlgorithm}.
   */
  private GridReorganizationAlgorithm(String owner) {
    super();
    GridReorganizationAlgorithm.owner = owner;
  }

  public static GridReorganizationAlgorithm createReorganizationAlgorithm(final String owner) {
    return new GridReorganizationAlgorithm(owner);
  }

  private Role findBestRole(final InstanceGoal<?> goal,
                            final Agent<?> agent) {
    Objects.requireNonNull(goal, "goal cannot be null");
    Objects.requireNonNull(agent, "agent cannot be null");

    Role result = null;
    double bestScore = RoleGoodnessFunction.MIN_SCORE;
    for (final Role role : goal.getAchievedBySet()) {
      if (debug) LOG.debug("goal.getAchievedBySet()(goal={},role={}", goal, role);
      final double score = role.goodness(agent, goal, new HashSet<>());
      if (debug) LOG.debug("score role.goodness ={}", score);
      if (score > bestScore) {
        result = role;
        bestScore = score;
      }
    }
    return result;
  }

  private Assignment findMostSuitableAssignment(final InstanceGoal<?> goal,
                                                final Set<Agent<?>> agents,
                                                final Set<Assignment> currentAssignments,
                                                final Set<Assignment> results) {
        /*
         * if there are multiple agents that can achieve the goal, find the best
         * agent
         */
    final List<Agent<?>> sortedAgents = new ArrayList<>(agents);
    Collections.sort(sortedAgents, agentComparator);
    Role bestRole = null;
    double bestAgentScore = 0.0;
    Agent<?> bestAgent = null;
    for (final Agent<?> agent : sortedAgents) {
            /*
             * find the best role, if there are multiple roles that can achieve
             * the goal
             */
      final Role role = findBestRole(goal, agent);
      if (role != null) {
        final double score = weightedScore(agent, role, goal,
                currentAssignments, results);
        if (debug) LOG.debug("This agent {}", agent);
        if (debug) LOG.debug("  for a role of {}", role);
        if (debug) LOG.debug("  and a goal of {}", goal);
        if (debug) LOG.debug("  has a score of {}", score);
        if (score > bestAgentScore) {
          bestAgent = agent;
          bestAgentScore = score;
          bestRole = role;
        }
      }
    }
    Assignment result = null;
    if (bestAgent != null && bestRole != null) {
      result = new Assignment(bestAgent, bestRole, goal);
    }
    return result;
  }

  private Set<Agent<?>> getAgentsUnassignedToGoal(final Set<Agent<?>> agents,
                                                  final InstanceGoal<?> goal, final Set<Assignment> assignments) {
    final Set<Agent<?>> results = agents.stream().filter(agent -> !hasAgentBeenAssignedToGoal(agent, goal, assignments)).collect(Collectors.toSet());
    return results;
  }

  private Set<InstanceGoal<?>> getUnassignedGoals(
          final Set<InstanceGoal<?>> goals,
          final Set<Assignment> assignments) {
    final Set<InstanceGoal<?>> results = goals.stream().filter(goal -> !hasGoalBeenAssigned(goal, assignments)).collect(Collectors.toSet());
    return results;
  }

  private boolean hasAgentBeenAssignedToGoal(final Agent<?> agent,
                                             final InstanceGoal<?> goal, final Set<Assignment> assignments) {
    boolean result = false;
    for (final Assignment assignment : assignments) {
      result |= assignment.getAgent().equals(agent)
              && assignment.getInstanceGoal().equals(goal);
    }
    return result;
  }

  private boolean hasGoalBeenAssigned(final InstanceGoal<?> goal,
                                      final Set<Assignment> assignments) {
    boolean result = false;
    for (final Assignment assignment : assignments) {
      result |= assignment.getInstanceGoal().equals(goal);
    }
    return result;
  }

  @Override
  public synchronized Set<Assignment> reorganize(final IOrganizationModel org, final Set<InstanceGoal<?>> goals, final Set<Agent<?>> agents) {

    final Set<Assignment> currentAssignments = org.getAssignments();

    if (debug) LOG.debug("Reorg: Goals={}, Agents={}, Current Assignments={}", goals,agents, currentAssignments);

    Set<Assignment> updatedAssignments = new HashSet<>();
        /*
         * for every goal that has not been assigned yet, find the most suitable
         * agent for the goal
         */
    final Set<InstanceGoal<?>> unassignedGoals = getUnassignedGoals(goals, currentAssignments);

    if (debug) LOG.debug("Reorg: Goals={}, Agents={}, Current Assignments={}, UnassignedGoals={}", goals,agents, currentAssignments, unassignedGoals);

    final List<InstanceGoal<?>> sortedGoals = new ArrayList<>(unassignedGoals);
    Collections.sort(sortedGoals, goalComparator);

    for (final InstanceGoal<?> goal : sortedGoals) {
      final Set<Agent<?>> agentsUnassignedToGoal = getAgentsUnassignedToGoal(
              agents, goal, currentAssignments);
      final Assignment assignment = findMostSuitableAssignment(goal,
              agentsUnassignedToGoal, currentAssignments, updatedAssignments);
      if (assignment != null) {
        updatedAssignments.add(assignment);
      } else {
                /*
                 * failed to find an assignment for the goal, so this is a goal
                 * failure: as we cannot be certain if there will be any new
                 * agents entering the system, this will be considered a goal
                 * failure at this point
                 *
                 * even though it can be considered a system failure, no system
                 * failure should be determined autonomously but instead by a
                 * human
                 */
        if (debug)LOG.info("Unable To Assign Goal={} with parameters={}.", goal.getIdentifier(), goal.getParameter());
      }
    }
    if (!updatedAssignments.isEmpty()) {
      if (debug) LOG.info("exiting reorganize. {} new Assignments", updatedAssignments.size());
      updatedAssignments.stream().filter(a -> debug).forEach(a -> LOG.debug("New assignment:  {} TO {} TO ACHIEVE {}", a.getAgent(), a.getRole(), a.getInstanceGoal()));
    }
    return updatedAssignments;
  }

  private double weightedScore(final Agent<?> agent, final Role role,
                               final InstanceGoal<?> goal,
                               final Set<Assignment> currentAssignments,
                               final Set<Assignment> results) {
    final Set<Assignment> allAssignments = new HashSet<>();
    allAssignments.addAll(currentAssignments);
    allAssignments.addAll(results);
    double result = role.goodness(agent, goal, allAssignments);
    if (result > RoleGoodnessFunction.MIN_SCORE) {
      result = 1.0;
    }
    int count = 1;
    for (final Assignment assignment : allAssignments) {
      if (assignment.getAgent().equals(agent) && assignment.getRole().equals(role)) {
        count++;
      }
    }
    return result / count;
  }
}
