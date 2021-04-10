package edu.cwru.sepia.agent.planner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import edu.cwru.sepia.agent.planner.actions.DepositAction;
import edu.cwru.sepia.agent.planner.actions.HarvestAction;
import edu.cwru.sepia.agent.planner.actions.MoveAction;
import edu.cwru.sepia.agent.planner.actions.StripsAction;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;

/**
 * @author Sarah Whelan
 */
public class GameState implements Comparable<GameState> {
    public static Position TOWN_HALL_POSITION;
    public static int TOWN_HALL_ID;

    private static final int BUILD_GOLD_NEEDED = 400;
    private static int REQUIRED_GOLD, REQUIRED_WOOD;
    private static boolean BUILD_PEASANTS;

    private int currGold = 0, currWood = 0, currFood = 2, nextId;

    private double cost = 0, heuristic;

    private Map<Integer, Peasant> peasants = new HashMap<Integer, Peasant>(3);
    private Map<Integer, Resource> resources = new HashMap<Integer, Resource>(7);
    private List<StripsAction> plan = new ArrayList<StripsAction>();

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
        this.currFood = state.currFood;
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

    private boolean canHarvestNow(Peasant peasant) {
        Resource resource = getResourceAt(peasant.getPosition());
        return resource != null && resource.getAmount() > 0;
    }

    public Stack<StripsAction> getPlan() {
        Stack<StripsAction> plan = new Stack<StripsAction>();
        for (int i = this.plan.size() - 1; i > -1; i--) {
            plan.push(this.plan.get(i));
        }
        return plan;
    }

    public boolean isGoal() {
        return currGold >= REQUIRED_GOLD && currWood >= REQUIRED_WOOD;
    }

    public double heuristic() {
        if (this.heuristic > 0) {
            return heuristic;
        }

        if (currGold <= REQUIRED_GOLD) {
            this.heuristic += (REQUIRED_GOLD - currGold);
        }

        if (currWood <= REQUIRED_WOOD) {
            this.heuristic += (REQUIRED_WOOD - currWood);
        }

        if (BUILD_PEASANTS) {
            this.heuristic += currFood * 5000;
        }

        return this.heuristic;
    }

    public double getCost() {
        return this.cost;
    }

    public boolean canBuild() {
        return currGold >= BUILD_GOLD_NEEDED && currFood > 0;
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

    public void applyBuildAction() {
        this.currGold = this.currGold - BUILD_GOLD_NEEDED;
        Peasant peasant = new Peasant(nextId, new Position(TOWN_HALL_POSITION));
        this.nextId++;
        this.currFood--;
        this.peasants.put(peasant.getId(), peasant);
    }

    public void applyMoveAction(int peasantId, Position destination) {
        this.peasants.get(peasantId).setPosition(destination);
    }

    public void applyHarvestAction(int peasantId, int resourceId) {
        Resource resource = this.resources.get(resourceId);
        Peasant peasant = this.peasants.get(peasantId);
        if (resource.isGold()) {
            peasant.setGold(Math.min(100, resource.getAmount()));
        } else {
            peasant.setWood(Math.min(100, resource.getAmount()));
        }
        resource.setAmountLeft(Math.max(0, resource.getAmount() - 100));
    }

    public void applyDepositAction(int peasantId) {
        Peasant peasant = this.peasants.get(peasantId);
        if (peasant.carryingGold()) {
            this.currGold += peasant.getNumGold();
            peasant.setGold(0);
        } else {
            this.currWood += peasant.getNumWood();
            peasant.setWood(0);
        }
    }

    public void update(StripsAction action) {
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
