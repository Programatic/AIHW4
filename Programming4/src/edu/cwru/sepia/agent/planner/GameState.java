package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.agent.planner.actions.DepositAction;
import edu.cwru.sepia.agent.planner.actions.HarvestAction;
import edu.cwru.sepia.agent.planner.actions.MoveAction;
import edu.cwru.sepia.agent.planner.resources.Resource;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private static int REQUIRED_GOLD, REQUIRED_WOOD;
    private static boolean BUILD_PEASANTS;
    public static Position TOWN_HALL_POSITION;
    public static int TOWN_HALL_ID;
    private static final int BUILD_PEASANT_GOLD = 400, BUILD_PEASANT_FOOD = 1, TOWNHALL_FOOD = 3;


    private Map<Integer, Peasant> peasants = new HashMap<>(TOWNHALL_FOOD);
    private Map<Integer, Resource> resources = new HashMap<>();

    private int currGold, currWood, currFood;
    private boolean buildPeasants;

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

    }

    /**
     * Unlike in the first A* assignment there are many possible goal states. As long as the wood and gold requirements
     * are met the peasants can be at any location and the capacities of the resource locations can be anything. Use
     * this function to check if the goal conditions are met and return true if they are.
     *
     * @return true if the goal conditions are met in this instance of game state.
     */
    public boolean isGoal() {
        // TODO: Implement me!
        return false;
    }

    private boolean canBuild() {
        return this.currGold >= BUILD_PEASANT_GOLD && currFood >= BUILD_PEASANT_FOOD;
    }

    private boolean canHarvestNow(Peasant peasant) {
        return true;
    }

    private Resource getResourceAt(Position position) {
        return null;
    }

    /**
     * The branching factor of this search graph are much higher than the planning. Generate all of the possible
     * successor states and their associated actions in this method.
     *
     * @return A list of the possible successor states and their associated actions
     */
    public List<GameState> generateChildren() {
        List<GameState> children = new ArrayList<>();

        GameState next_state = new GameState(this);

        if (buildPeasants && this.canBuild()) {
            // TODO: Build action
        }

        for (Peasant peasant : this.peasants.values()) {
            if (peasant.isCarrying()) {
                if (peasant.getPosition().isAdjacent(TOWN_HALL_POSITION)) {
                    DepositAction action = new DepositAction(peasant);
                    // TODO: Apply action
                } else {
                    MoveAction action = new MoveAction(peasant, TOWN_HALL_POSITION);
                    // TODO: Apply action
                }
            } else if (canHarvestNow(peasant)) {
                Resource resource = getResourceAt(peasant.getPosition());
                HarvestAction action = new HarvestAction(peasant, resource);
            } else {
                for (Resource resource : this.resources.values()) {
                    MoveAction action = new MoveAction(peasant, resource.getPosition());
                    // TODO: Apply action
                }
            }
        }

        children.add(next_state);


        return children;
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
        // TODO: Implement me!
        return 0.0;
    }

    /**
     *
     * Write the function that computes the current cost to get to this node. This is combined with your heuristic to
     * determine which actions/states are better to explore.
     *
     * @return The current cost to reach this goal
     */
    public double getCost() {
        // TODO: Implement me!
        return 0.0;
    }

    /**
     * This is necessary to use your state in the Java priority queue. See the official priority queue and Comparable
     * interface documentation to learn how this function should work.
     *
     * @param o The other game state to compare
     * @return 1 if this state costs more than the other, 0 if equal, -1 otherwise
     */
    @Override
    public int compareTo(GameState o) {
        // TODO: Implement me!
        return 0;
    }

    /**
     * This will be necessary to use the GameState as a key in a Set or Map.
     *
     * @param o The game state to compare
     * @return True if this state equals the other state, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        // TODO: Implement me!
        return false;
    }

    /**
     * This is necessary to use the GameState as a key in a HashSet or HashMap. Remember that if two objects are
     * equal they should hash to the same value.
     *
     * @return An integer hashcode that is equal for equal states.
     */
    @Override
    public int hashCode() {
        // TODO: Implement me!
        return 0;
    }
}
