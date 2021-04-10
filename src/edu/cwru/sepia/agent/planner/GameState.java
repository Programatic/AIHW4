package edu.cwru.sepia.agent.planner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import edu.cwru.sepia.agent.planner.actions.DepositAction;
import edu.cwru.sepia.agent.planner.actions.HarvestAction;
import edu.cwru.sepia.agent.planner.actions.MoveAction;
import edu.cwru.sepia.agent.planner.actions.StripsAction;
import edu.cwru.sepia.agent.planner.resources.Resource;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;

/**
 * @author Sarah Whelan
 */
public class GameState implements Comparable<GameState> {
    public static Position TOWN_HALL_POSITION;
    public static int TOWN_HALL_ID;

    private static final int BUILD_PESANT_OFFSET = 20000; // Unit-less
    private static final int REQUIRED_GOLD_TO_BUILD = 400; // in Gold amount
    private static final int MAX_NUM_PEASANTS = 3;
    private static int REQUIRED_GOLD, REQUIRED_WOOD;
    private static boolean BUILD_PEASANTS;

    private static Set<Position> resourcePositions = new HashSet<Position>();

    private int currGold = 0, currWood = 0, nextId;

    private double cost = 0, heuristic;

    private Map<Integer, Peasant> peasants = new HashMap<Integer, Peasant>(3);
    private Map<Integer, Resource> resources = new HashMap<Integer, Resource>(7);
    private List<StripsAction> plan = new ArrayList<StripsAction>(300);

    /**
     * @param state         The current stateview at the time the plan is being created
     * @param playernum     The player number of agent that is planning
     * @param requiredGold  The goal amount of gold (e.g. 200 for the small scenario)
     * @param requiredWood  The goal amount of wood (e.g. 200 for the small scenario)
     * @param buildPeasants True if the BuildPeasant action should be considered
     */
    public GameState(State.StateView state, int playernum, int requiredGold, int requiredWood, boolean buildPeasants) {
        GameState.REQUIRED_GOLD = requiredGold;
        GameState.REQUIRED_WOOD = requiredWood;
        GameState.BUILD_PEASANTS = buildPeasants;

        for (ResourceNode.ResourceView resource : state.getAllResourceNodes()) {
            Position pos = new Position(resource.getXPosition(), resource.getYPosition());
            resourcePositions.add(pos);
            Resource res = new Resource(resource.getID(), resource.getAmountRemaining(), pos, resource.getType());
            this.resources.put(resource.getID(), res);
        }

        for (Unit.UnitView unit : state.getAllUnits()) {
            Position pos = new Position(unit.getXPosition(), unit.getYPosition());
            if (unit.getTemplateView().getName().equalsIgnoreCase("townhall")) {
                TOWN_HALL_ID = unit.getID();
                TOWN_HALL_POSITION = pos;
            } else {
                Peasant peasant = new Peasant(unit.getID(), pos);
                this.peasants.put(unit.getID(), peasant);
            }
        }
    }

    public GameState(GameState state) {
        this.currGold = state.currGold;
        this.currWood = state.currWood;
        this.nextId = state.nextId;
        this.cost = state.cost;

        for (Peasant peasant : state.peasants.values()) {
            Peasant copy = new Peasant(peasant);
            this.peasants.put(copy.getId(), copy);
        }

        for (Resource resource : state.resources.values()) {
            Resource copy = new Resource(resource);
            this.resources.put(resource.getID(), copy);
        }
        ;

        plan.addAll(state.plan);
    }

    private boolean peasantCanHarvest(Peasant peasant) {
        return getResourceAt(peasant.getPosition()) != null && getResourceAt(peasant.getPosition()).hasRemaining();
    }

    public Stack<StripsAction> getPlan() {
        Stack<StripsAction> plan = new Stack<StripsAction>();
        for (int i = this.plan.size() - 1; i > -1; i--) {
            plan.push(this.plan.get(i));
        }
        return plan;
    }

    /**
     * @return true if the goal conditions are met in this instance of game state.
     */
    public boolean isGoal() {
        return currGold >= REQUIRED_GOLD && currWood >= REQUIRED_WOOD;
    }

    /**
     * Adds for the amount of resources still needing to be collected.
     * Adds for not having peasants
     * Adds for not being near resources if not holding anything
     * Adds for not being near town all if holding something
     * Subtracts for if you can make peasants or if you are next to a resource and not holding anything
     *
     * @return The value estimated remaining cost to reach a goal state from this state.
     */
    public double heuristic() {
        if (this.heuristic != 0) {
            return heuristic;
        }

        if (currWood > currGold) {
            this.heuristic += 100;
        }
        if (currGold <= REQUIRED_GOLD) {
            this.heuristic += (REQUIRED_GOLD - currGold);
        } else {
            this.heuristic += (currGold - REQUIRED_GOLD);
        }
        if (currWood <= REQUIRED_WOOD) {
            this.heuristic += (REQUIRED_WOOD - currWood);
        } else {
            this.heuristic += currWood - REQUIRED_WOOD;
        }
        if (BUILD_PEASANTS) {
            this.heuristic += (MAX_NUM_PEASANTS - this.peasants.size()) * BUILD_PESANT_OFFSET;
            if (canBuild()) {
                this.heuristic -= BUILD_PESANT_OFFSET;
            }
        }

        for (Peasant peasant : this.peasants.values()) {
            if (peasant.hasResource()) {
                this.heuristic -= peasant.getNumGold() + peasant.getNumWood();
            } else {
                if (peasantCanHarvest(peasant)) {
                    this.heuristic -= 50;
                } else if (getResourceAt(peasant.getPosition()) == null) {
                    this.heuristic += 100;
                }
            }
        }

        return this.heuristic;
    }

    /**
     * Cost is updated every time a move is applied.
     *
     * @return The current cost to reach this goal
     */
    public double getCost() {
        return this.cost;
    }

    public boolean canBuild() {
        return currGold >= REQUIRED_GOLD_TO_BUILD && this.peasants.size() < MAX_NUM_PEASANTS;
    }

    private Resource getResourceAt(Position position) {
        for (Resource resource : this.resources.values()) {
            if (resource.getPosition().equals(position))
                return resource;
        }

        return null;
    }

    public List<GameState> generateChildren() {
        List<GameState> children = new ArrayList<>();

        GameState child = new GameState(this);

        if (BUILD_PEASANTS && this.canBuild()) {
            // TODO: Build action
        }

        Resource res;
        for (Peasant peasant : this.peasants.values()) {
            if (peasant.hasResource()) {
                if (peasant.getPosition().isAdjacent(TOWN_HALL_POSITION)) {
                    DepositAction action = new DepositAction(peasant);
                    if (action.preconditionsMet(child))
                        action.apply(child);
                } else {
                    MoveAction action = new MoveAction(peasant, TOWN_HALL_POSITION);
                    if (action.preconditionsMet(child))
                        action.apply(child);
                }
            } else if ((res = getResourceAt(peasant.getPosition())) != null) {
                HarvestAction action = new HarvestAction(peasant, res);
                if (action.preconditionsMet(child))
                    action.apply(child);
            } else {
                for (Resource resource : this.resources.values()) {
                    GameState innerChild = new GameState(child);
                    MoveAction action = new MoveAction(peasant, resource.getPosition());
                    if (action.preconditionsMet(innerChild)) {
                        action.apply(innerChild);
                    }

                    children.add(innerChild);
                }
            }
        }
        children.add(child);

        return children;
    }

    public void applyBuildAction(StripsAction action) {
        this.currGold = this.currGold - REQUIRED_GOLD_TO_BUILD;
        Peasant peasant = new Peasant(nextId, new Position(TOWN_HALL_POSITION));
        nextId++;
        this.peasants.put(peasant.getId(), peasant);
    }

    public void applyMoveAction(StripsAction action, int peasantId, Position destination) {
        this.peasants.get(peasantId).setPosition(destination);
    }

    public void applyHarvestAction(StripsAction action, int peasantId, int resourceId) {
        Resource resource = this.resources.get(resourceId);
        Peasant peasant = this.peasants.get(peasantId);
        if (resource.isGold()) {
            peasant.setNumGold(Math.min(100, resource.getAmountLeft()));
            resource.setAmountLeft(Math.max(0, resource.getAmountLeft() - 100));
        } else {
            peasant.setNumWood(Math.min(100, resource.getAmountLeft()));
            resource.setAmountLeft(Math.max(0, resource.getAmountLeft() - 100));
        }
    }

    public void applyDepositAction(StripsAction action, int peasantId) {
        Peasant peasant = this.peasants.get(peasantId);
        if (peasant.hasGold()) {
            this.currGold += peasant.getNumGold();
            peasant.setNumGold(0);
        } else {
            this.currWood += peasant.getNumWood();
            peasant.setNumWood(0);
        }
    }

    public void updatePlanAndCost(StripsAction action) {
        plan.add(action);
        this.cost += action.getCost();
    }

    /**
     * @param o The other game state to compare
     * @return 1 if this state costs more than the other, 0 if equal, -1 otherwise
     */
    @Override
    public int compareTo(GameState o) {
        if (this.heuristic() > o.heuristic()) {
            return 1;
        } else if (this.heuristic() < o.heuristic()) {
            return -1;
        }
        return 0;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + currGold;
        result = prime * result + currWood;
        result = prime * result + ((peasants == null) ? 0 : peasants.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GameState other = (GameState) obj;
        if (currGold != other.currGold)
            return false;
        if (currWood != other.currWood)
            return false;
        if (peasants == null) {
            if (other.peasants != null)
                return false;
        } else if (!peasants.equals(other.peasants))
            return false;
        return true;
    }
}
