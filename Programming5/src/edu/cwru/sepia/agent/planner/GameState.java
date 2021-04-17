package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.agent.planner.actions.*;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;

import java.util.*;

/**
 * This class is used to represent the state of the game after applying one of the avaiable actions. It will also
 * track the A* specific information such as the parent pointer and the cost and heuristic function. Remember that
 * unlike the path planning A* from the first assignment the cost of an action may be more than 1. Specifically the cost
 * of executing a compound action such as move can be more than 1. You will need to account for this in your heuristic
 * and your cost function.
 *
 * The first instance is constructed from the StateView object (like in PA2). Implement the methods provided and
 * add any other methods and member variables you need.
 *
 * Some useful API calls for the state view are
 *
 * state.getXExtent() and state.getYExtent() to get the map size
 *
  * Note that SEPIA saves the townhall as a unit. Therefore when you create a GameState instance,
 * you must be able to distinguish the townhall from a peasant. This can be done by getting
 * the name of the unit type from that unit's TemplateView:
 * state.getUnit(id).getTemplateView().getName().toLowerCase(): returns "townhall" or "peasant"
 *
 * You will also need to distinguish between gold mines and trees.
 * state.getResourceNode(id).getType(): returns the type of the given resource
 *
 * You can compare these types to values in the ResourceNode.Type enum:
 * ResourceNode.Type.GOLD_MINE and ResourceNode.Type.TREE
 *
 * You can check how much of a resource is remaining with the following:
 * state.getResourceNode(id).getAmountRemaining()
 *
 * I recommend storing the actions that generated the instance of the GameState in this class using whatever
 * class/structure you use to represent actions.
 */
public class GameState implements Comparable<GameState> {
    public static Position TOWN_HALL_POSITION;
    public static int TOWN_HALL_ID;
    public static final int BUILD_GOLD_NEEDED = 400;

    private static int REQUIRED_GOLD, REQUIRED_WOOD;
    private static boolean BUILD_PEASANTS;

    private int currGold = 0, currWood = 0, currFood = 2, nextId = 0;

    private double cost = 0, heuristic;

    private Map<Integer, Peasant> peasants = new HashMap<Integer, Peasant>();
    private Map<Integer, Resource> resources = new HashMap<Integer, Resource>();
    private List<StripsAction> plan = new ArrayList<StripsAction>();

    /**
     * Construct a GameState from a stateview object. This is used to construct the initial search node. All other
     * nodes should be constructed from the another constructor you create or by factory functions that you create.
     *
     * @param state The current stateview at the time the plan is being created
     * @param playernum The player number of agent that is planning
     * @param requiredGold The goal amount of gold (e.g. 200 for the small scenario)
     * @param requiredWood The goal amount of wood (e.g. 200 for the small scenario)
     * @param buildPeasants True if the BuildPeasant action should be considered
     */
    public GameState(State.StateView state, int playernum, int requiredGold, int requiredWood, boolean buildPeasants) {
        REQUIRED_GOLD = requiredGold;
        REQUIRED_WOOD = requiredWood;
        BUILD_PEASANTS = buildPeasants;

        this.currFood = 3;

        for (ResourceNode.ResourceView resource : state.getAllResourceNodes()) {
            Position pos = new Position(resource.getXPosition(), resource.getYPosition());
            Resource res = new Resource(resource.getID(), resource.getAmountRemaining(), pos, resource.getType());
            this.resources.put(resource.getID(), res);

            if (res.getID() > nextId)
                nextId = res.getID() + 1;
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

            if (unit.getID() > nextId)
                nextId = unit.getID() + 1;
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

    /**
     * Unlike in the first A* assignment there are many possible goal states. As long as the wood and gold requirements
     * are met the peasants can be at any location and the capacities of the resource locations can be anything. Use
     * this function to check if the goal conditions are met and return true if they are.
     *
     * @return true if the goal conditions are met in this instance of game state.
     */
    public boolean isGoal() {
        return currGold >= REQUIRED_GOLD && currWood >= REQUIRED_WOOD;
    }

    /**
     * The branching factor of this search graph are much higher than the planning. Generate all of the possible
     * successor states and their associated actions in this method.
     *
     * @return A list of the possible successor states and their associated actions
     */
    public List<GameState> generateChildren() {
        List<GameState> children = new ArrayList<>();

        GameState child = new GameState(this);

        if (BUILD_PEASANTS && this.canBuild()) {
//            BuildPeasantAction action = new BuildPeasantAction(this.currGold, this.currFood);
//            if (action.preconditionsMet(child)) {}
//                action.apply(child);

        }

        Resource res;
        for (Peasant peasant : this.peasants.values()) {
            if (peasant.isCarrying()) {
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

    public Stack<StripsAction> getPlan() {
        Stack<StripsAction> plan = new Stack<StripsAction>();
        for (int i = this.plan.size() - 1; i > -1; i--) {
            plan.push(this.plan.get(i));
        }
        return plan;
    }

    public void applyMoveAction(int peasantId, Position destination) {
        this.peasants.get(peasantId).setPosition(destination);
    }

    public void applyHarvestAction(int peasantId, int resourceId) {
        Resource resource = this.resources.get(resourceId);
        Peasant peasant = this.peasants.get(peasantId);
        if (resource.isGold()) {
            peasant.setCurrGold(Math.min(100, resource.getAmount()));
        } else {
            peasant.setCurrWood(Math.min(100, resource.getAmount()));
        }
        resource.setAmountLeft(Math.max(0, resource.getAmount() - 100));
    }

    public void applyDepositAction(int peasantId) {
        Peasant peasant = this.peasants.get(peasantId);
        if (peasant.carryingGold()) {
            this.currGold += peasant.getCurrGold();
            peasant.setCurrGold(0);
        } else {
            this.currWood += peasant.getCurrWood();
            peasant.setCurrWood(0);
        }
    }

    public void applyBuildAction() {
        this.currGold -= BUILD_GOLD_NEEDED;
        Peasant peasant = new Peasant(nextId, new Position(TOWN_HALL_POSITION));
        this.peasants.put(nextId, peasant);
        nextId++;
    }

    public void update(StripsAction action) {
        plan.add(action);
        this.cost += action.getCost();
    }

    /**
     * Write your heuristic function here. Remember this must be admissible for the properties of A* to hold. If you
     * can come up with an easy way of computing a consistent heuristic that is even better, but not strictly necessary.
     *
     * Add a description here in your submission explaining your heuristic.
     *
     * @return The value estimated remaining cost to reach a goal state from this state.
     */
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
            // TODO: Implement building peasants
        }

        return this.heuristic;
    }

    /**
     *
     * Write the function that computes the current cost to get to this node. This is combined with your heuristic to
     * determine which actions/states are better to explore.
     *
     * @return The current cost to reach this goal
     */
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


    /**
     * This is necessary to use your state in the Java priority queue. See the official priority queue and Comparable
     * interface documentation to learn how this function should work.
     *
     * @return 1 if this state costs more than the other, 0 if equal, -1 otherwise
     */
    @Override
    public int compareTo(GameState gameState) {
        return Double.compare(this.heuristic(), gameState.heuristic());
    }

    /**
     * This will be necessary to use the GameState as a key in a Set or Map.
     *
     * @param o The game state to compare
     * @return True if this state equals the other state, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameState gameState = (GameState) o;
        return currGold == gameState.currGold && currWood == gameState.currWood && currFood == gameState.currFood && peasants.equals(gameState.peasants);
    }

    /**
     * This is necessary to use the GameState as a key in a HashSet or HashMap. Remember that if two objects are
     * equal they should hash to the same value.
     *
     * @return An integer hashcode that is equal for equal states.
     */
    @Override
    public int hashCode() {
        return Objects.hash(currGold, currWood, currFood, peasants);
    }
}
