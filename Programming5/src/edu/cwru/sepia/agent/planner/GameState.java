//package edu.cwru.sepia.agent.planner;
//
//import edu.cwru.sepia.agent.planner.actions.*;
//import edu.cwru.sepia.environment.model.state.ResourceNode;
//import edu.cwru.sepia.environment.model.state.State;
//import edu.cwru.sepia.environment.model.state.Unit;
//
//import java.util.*;
//
///**
// * This class is used to represent the state of the game after applying one of the avaiable actions. It will also
// * track the A* specific information such as the parent pointer and the cost and heuristic function. Remember that
// * unlike the path planning A* from the first assignment the cost of an action may be more than 1. Specifically the cost
// * of executing a compound action such as move can be more than 1. You will need to account for this in your heuristic
// * and your cost function.
// *
// * The first instance is constructed from the StateView object (like in PA2). Implement the methods provided and
// * add any other methods and member variables you need.
// *
// * Some useful API calls for the state view are
// *
// * state.getXExtent() and state.getYExtent() to get the map size
// *
//  * Note that SEPIA saves the townhall as a unit. Therefore when you create a GameState instance,
// * you must be able to distinguish the townhall from a peasant. This can be done by getting
// * the name of the unit type from that unit's TemplateView:
// * state.getUnit(id).getTemplateView().getName().toLowerCase(): returns "townhall" or "peasant"
// *
// * You will also need to distinguish between gold mines and trees.
// * state.getResourceNode(id).getType(): returns the type of the given resource
// *
// * You can compare these types to values in the ResourceNode.Type enum:
// * ResourceNode.Type.GOLD_MINE and ResourceNode.Type.TREE
// *
// * You can check how much of a resource is remaining with the following:
// * state.getResourceNode(id).getAmountRemaining()
// *
// * I recommend storing the actions that generated the instance of the GameState in this class using whatever
// * class/structure you use to represent actions.
// */
//public class GameState implements Comparable<GameState> {
//    // TODO: REMOVE
////    private static int PEASANT_TEMPLATE_ID;
//
//
//    public static Position TOWN_HALL_POSITION;
//    public static int TOWN_HALL_ID;
//    public static final int BUILD_GOLD_NEEDED = 400;
//
//    private static int REQUIRED_GOLD, REQUIRED_WOOD;
//    private static boolean BUILD_PEASANTS;
//
//    private int currGold = 0, currWood = 0, currFood = 2, nextId = Integer.MIN_VALUE;
//
//    private double cost = 0, heuristic;
//
//    private Map<Integer, Peasant> peasants = new HashMap<Integer, Peasant>();
//    private Map<Integer, Resource> resources = new HashMap<Integer, Resource>();
//    private List<StripsAction> plan = new ArrayList<StripsAction>();
//
//    /**
//     * Construct a GameState from a stateview object. This is used to construct the initial search node. All other
//     * nodes should be constructed from the another constructor you create or by factory functions that you create.
//     *
//     * @param state The current stateview at the time the plan is being created
//     * @param playernum The player number of agent that is planning
//     * @param requiredGold The goal amount of gold (e.g. 200 for the small scenario)
//     * @param requiredWood The goal amount of wood (e.g. 200 for the small scenario)
//     * @param buildPeasants True if the BuildPeasant action should be considered
//     */
//    public GameState(State.StateView state, int playernum, int requiredGold, int requiredWood, boolean buildPeasants) {
//        REQUIRED_GOLD = requiredGold;
//        REQUIRED_WOOD = requiredWood;
//        BUILD_PEASANTS = true;
//
//        for (ResourceNode.ResourceView resource : state.getAllResourceNodes()) {
//            Position pos = new Position(resource.getXPosition(), resource.getYPosition());
//            Resource res = new Resource(resource.getID(), resource.getAmountRemaining(), pos, resource.getType());
//            this.resources.put(resource.getID(), res);
//        }
//
//        for (Unit.UnitView unit : state.getAllUnits()) {
//            Position pos = new Position(unit.getXPosition(), unit.getYPosition());
//            if (unit.getTemplateView().getName().equalsIgnoreCase("townhall")) {
//                TOWN_HALL_ID = unit.getID();
//                TOWN_HALL_POSITION = pos;
//            } else {
//                Peasant peasant = new Peasant(unit.getID(), pos);
//                this.peasants.put(unit.getID(), peasant);
//            }
//        }
//        this.nextId = 1 + this.peasants.size() + this.resources.size();
//    }
//
//    public GameState(GameState state) {
//        this.currGold = state.currGold;
//        this.currWood = state.currWood;
//        this.currFood = state.currFood;
//        this.nextId = state.nextId;
//        this.cost = state.cost;
//
//        for (Peasant peasant : state.peasants.values()) {
//            Peasant copy = new Peasant(peasant);
//            this.peasants.put(copy.getId(), copy);
//        }
//
//        for (Resource resource : state.resources.values()) {
//            Resource copy = new Resource(resource);
//            this.resources.put(resource.getID(), copy);
//        }
//        ;
//
//        plan.addAll(state.plan);
//    }
//
//    /**
//     * Unlike in the first A* assignment there are many possible goal states. As long as the wood and gold requirements
//     * are met the peasants can be at any location and the capacities of the resource locations can be anything. Use
//     * this function to check if the goal conditions are met and return true if they are.
//     *
//     * @return true if the goal conditions are met in this instance of game state.
//     */
//    public boolean isGoal() {
//        return currGold >= REQUIRED_GOLD && currWood >= REQUIRED_WOOD;
//    }
//
//    /**
//     * The branching factor of this search graph are much higher than the planning. Generate all of the possible
//     * successor states and their associated actions in this method.
//     *
//     * @return A list of the possible successor states and their associated actions
//     */
//    public List<GameState> generateChildren() {
//        List<GameState> children = new ArrayList<>();
//
//        GameState child = new GameState(this);
//
//        if (BUILD_PEASANTS && this.canBuild()) {
//            BuildPeasantAction action = new BuildPeasantAction();
//            if (action.preconditionsMet(child))
//                action.apply(child);
//
//            children.add(child);
//            return children;
//        }
//
//        Resource res;
//        for (Peasant peasant : this.peasants.values()) {
//            if (peasant.isCarrying()) {
//                if (peasant.getPosition().isAdjacent(TOWN_HALL_POSITION)) {
//                    DepositAction action = new DepositAction(peasant);
//                    if (action.preconditionsMet(child))
//                        action.apply(child);
//                } else {
//                    MoveAction action = new MoveAction(peasant, TOWN_HALL_POSITION);
//                    if (action.preconditionsMet(child))
//                        action.apply(child);
//                }
//            } else if ((res = getResourceAt(peasant.getPosition())) != null) {
//                HarvestAction action = new HarvestAction(peasant, res);
//                if (action.preconditionsMet(child))
//                    action.apply(child);
//            } else {
//                for (Resource resource : this.resources.values()) {
//                    GameState innerChild = new GameState(child);
//                    MoveAction action = new MoveAction(peasant, resource.getPosition());
//                    if (action.preconditionsMet(innerChild)) {
//                        action.apply(innerChild);
//                    }
//
//                    children.add(innerChild);
//                }
//            }
//        }
//        children.add(child);
//
//        for(Peasant peasant : this.peasants.values()) {
//            GameState innerChild = new GameState(this);
//
//            MoveAction moveAction = new MoveAction(peasant, TOWN_HALL_POSITION);
//            if(moveAction.preconditionsMet(innerChild)) {
//                moveAction.apply(innerChild);
//            }
//
////            children.add(innerChild);
//        }
//
//        return children;
//    }
//
//
//    public Stack<StripsAction> getPlan() {
//        Stack<StripsAction> plan = new Stack<StripsAction>();
//        for (int i = this.plan.size() - 1; i > -1; i--) {
//            plan.push(this.plan.get(i));
//        }
//        return plan;
//    }
//
//    public void applyMoveAction(int peasantId, Position destination) {
//        this.peasants.get(peasantId).setPosition(destination);
//    }
//
//    public void applyHarvestAction(int peasantId, int resourceId) {
//        Resource resource = this.resources.get(resourceId);
//        Peasant peasant = this.peasants.get(peasantId);
//        if (resource.isGold()) {
//            peasant.setCurrGold(Math.min(100, resource.getAmount()));
//        } else {
//            peasant.setCurrWood(Math.min(100, resource.getAmount()));
//        }
//        resource.setAmountLeft(Math.max(0, resource.getAmount() - 100));
//    }
//
//    public void applyDepositAction(int peasantId) {
//        Peasant peasant = this.peasants.get(peasantId);
//        if (peasant.carryingGold()) {
//            this.currGold += peasant.getCurrGold();
//            peasant.setCurrGold(0);
//        } else {
//            this.currWood += peasant.getCurrWood();
//            peasant.setCurrWood(0);
//        }
//    }
//
//    public void applyBuildPeasantAction() {
//        Peasant peasant = new Peasant(nextId, new Position(TOWN_HALL_POSITION));
//        peasants.put(nextId, peasant);
//        this.currFood--;
////        this.currGold -= BUILD_GOLD_NEEDED;
//        this.nextId++;
//    }
//
//    public void update(StripsAction action) {
//        plan.add(action);
//        this.cost += action.getCost();
//    }
//
//    /**
//     * Write your heuristic function here. Remember this must be admissible for the properties of A* to hold. If you
//     * can come up with an easy way of computing a consistent heuristic that is even better, but not strictly necessary.
//     *
//     * Add a description here in your submission explaining your heuristic.
//     *
//     * @return The value estimated remaining cost to reach a goal state from this state.
//     */
//    public double heuristic() {
//        if (this.heuristic > 0) {
//            return heuristic;
//        }
//
//        if (currGold <= REQUIRED_GOLD) {
//            this.heuristic += (REQUIRED_GOLD - currGold);
//        }
//
//        if (currWood <= REQUIRED_WOOD) {
//            this.heuristic += (REQUIRED_WOOD - currWood);
//        }
//
//        if (BUILD_PEASANTS) {
//            // TODO: Implement building peasants
////            this.heuristic += 10000;
//        }
//
//        return this.heuristic;
//    }
//
//    /**
//     *
//     * Write the function that computes the current cost to get to this node. This is combined with your heuristic to
//     * determine which actions/states are better to explore.
//     *
//     * @return The current cost to reach this goal
//     */
//    public double getCost() {
//        return this.cost;
//    }
//
//    public boolean canBuild() {
//        return currGold >= BUILD_GOLD_NEEDED && currFood > -1;
//    }
//
//    private Resource getResourceAt(Position position) {
//        for (Resource resource : this.resources.values()) {
//            if (resource.getPosition().equals(position))
//                return resource;
//        }
//
//        return null;
//    }
//
//
//    /**
//     * This is necessary to use your state in the Java priority queue. See the official priority queue and Comparable
//     * interface documentation to learn how this function should work.
//     *
//     * @return 1 if this state costs more than the other, 0 if equal, -1 otherwise
//     */
//    @Override
//    public int compareTo(GameState gameState) {
//        return Double.compare(this.heuristic(), gameState.heuristic());
//    }
//
//    /**
//     * This will be necessary to use the GameState as a key in a Set or Map.
//     *
//     * @param o The game state to compare
//     * @return True if this state equals the other state, false otherwise.
//     */
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        GameState gameState = (GameState) o;
//        return currGold == gameState.currGold && currWood == gameState.currWood && currFood == gameState.currFood && peasants.equals(gameState.peasants);
//    }
//
//    /**
//     * This is necessary to use the GameState as a key in a HashSet or HashMap. Remember that if two objects are
//     * equal they should hash to the same value.
//     *
//     * @return An integer hashcode that is equal for equal states.
//     */
//    @Override
//    public int hashCode() {
//        return Objects.hash(currGold, currWood, currFood, peasants);
//    }
//}


package edu.cwru.sepia.agent.planner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import edu.cwru.sepia.agent.planner.actions.*;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;

/**
 *
 * @author Sarah Whelan
 */
public class GameState implements Comparable<GameState> {
    public static int PEASANT_TEMPLATE_ID;
    public static Position TOWN_HALL_POSITION;
    public static final String TOWNHALL_NAME = "townhall";
    public static int TOWN_HALL_ID;

    private static final String GOLD_RESOURCE_NAME = "GOLD_MINE";
    private static final int RESOURCE_AMOUNT_TO_TAKE = 100; // Unit-less amount of resource
    private static final int BUILD_PESANT_OFFSET = 20000; // Unit-less
    private static final int REQUIRED_GOLD_TO_BUILD = 400; // in Gold amount
    private static final int MAX_NUM_PEASANTS = 3;
    private static int requiredGold;
    private static int requiredWood;
    private static boolean buildPeasants;

    private static Set<Position> resourcePositions = new HashSet<Position>();

    private int obtainedGold = 0;
    private int obtainedWood = 0;

    private int nextId = 0;

    private double cost = 0;
    private double heuristic = 0;

    private Map<Integer, Peasant> peasants = new HashMap<Integer, Peasant>(3);
    private Map<Integer, Resource> resources = new HashMap<Integer, Resource>(7);
    private List<StripsAction> plan = new ArrayList<StripsAction>(300);

    /**
     *
     * @param state The current stateview at the time the plan is being created
     * @param playernum The player number of agent that is planning
     * @param requiredGold The goal amount of gold (e.g. 200 for the small scenario)
     * @param requiredWood The goal amount of wood (e.g. 200 for the small scenario)
     * @param buildPeasants True if the BuildPeasant action should be considered
     */
    public GameState(State.StateView state, int playernum, int requiredGold, int requiredWood, boolean buildPeasants) {
        GameState.requiredGold = requiredGold;
        GameState.requiredWood = requiredWood;
        GameState.buildPeasants = buildPeasants;
//		state.getAllResourceNodes().stream().forEach(e -> {
//			Position position = new Position(e.getXPosition(), e.getYPosition());
//			GameState.resourcePositions.add(position);
//			if(e.getType().name().equals(GOLD_RESOURCE_NAME)) {
//				resources.put(e.getID(), new Gold(e.getID(), e.getAmountRemaining(), position));
//			} else {
//				resources.put(e.getID(), new Wood(e.getID(), e.getAmountRemaining(), position));
//			}
//		});

        for (ResourceNode.ResourceView resource : state.getAllResourceNodes()) {
            Position pos = new Position(resource.getXPosition(), resource.getYPosition());
            Resource res = new Resource(resource.getID(), resource.getAmountRemaining(), pos, resource.getType());

            this.resources.put(resource.getID(), res);
            GameState.resourcePositions.add(pos);

            if (resource.getID() > nextId)
                nextId = resource.getID() + 1;
        }


        for (Unit.UnitView unit : state.getAllUnits()) {
            Position pos = new Position(unit.getXPosition(), unit.getYPosition());
            if (unit.getTemplateView().getName().equalsIgnoreCase(TOWNHALL_NAME)) {
                TOWN_HALL_ID = unit.getID();
                TOWN_HALL_POSITION = pos;
            } else {
                Peasant peasant = new Peasant(unit.getID(), pos);
                this.peasants.put(unit.getID(), peasant);
                GameState.PEASANT_TEMPLATE_ID = unit.getTemplateView().getID();
            }

            if (unit.getID() > nextId)
                nextId = unit.getID() + 1;
        }

        this.nextId = 1 + this.peasants.size() + this.resources.size();
    }

    public GameState(GameState state) {
        this.obtainedGold = state.obtainedGold;
        this.obtainedWood = state.obtainedWood;
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

    private Peasant getPeasantWithId(int peasantId) {
        return this.peasants.get(peasantId);
    }

    private Resource getResourceWithId(int resourceId) {
        return this.resources.get(resourceId);
    }

    private boolean peasantCanHarvest(Peasant peasant) {
        return isResourceLocation(peasant.getPosition()) && getResourceForPosition(peasant.getPosition()).hasRemaining();
    }

    /**
     * Be sure there is a resource there first.
     * @param position
     * @return
     */
    private Resource getResourceForPosition(Position position) {
        return this.resources.values().stream().filter(e -> e.getPosition().equals(position)).findFirst().get();
    }

    private boolean isResourceLocation(Position destination) {
        return GameState.resourcePositions.contains(destination);
    }

    private void addToObtainedWood(int numWood) {
        this.obtainedWood += numWood;
    }

    private void addToObtainedGold(int numGold) {
        this.obtainedGold += numGold;
    }

    public Stack<StripsAction> getPlan() {
        Stack<StripsAction> plan = new Stack<StripsAction>();
        for(int i = this.plan.size() - 1; i > -1; i--) {
            plan.push(this.plan.get(i));
        }
        return plan;
    }

    /**
     *
     * @return true if the goal conditions are met in this instance of game state.
     */
    public boolean isGoal() {
        return obtainedGold >= requiredGold && obtainedWood >= requiredWood;
    }

    /**
     * Adds for the amount of edu.cwru.sepia.agent.planner.resources still needing to be collected.
     * Adds for not having peasants
     * Adds for not being near edu.cwru.sepia.agent.planner.resources if not holding anything
     * Adds for not being near town all if holding something
     * Subtracts for if you can make peasants or if you are next to a resource and not holding anything
     *
     * @return The value estimated remaining cost to reach a goal state from this state.
     */
    public double heuristic() {
        if(this.heuristic != 0) {
            return heuristic;
        }

        if(obtainedWood > obtainedGold) {
            this.heuristic += 100;
        }
        if(obtainedGold <= requiredGold) {
            this.heuristic += (requiredGold - obtainedGold);
        } else {
            this.heuristic += (obtainedGold - requiredGold);
        }
        if(obtainedWood <= requiredWood) {
            this.heuristic += (requiredWood - obtainedWood);
        } else {
            this.heuristic += obtainedWood - requiredWood;
        }
        if(buildPeasants) {
            this.heuristic += (MAX_NUM_PEASANTS - this.peasants.size()) * BUILD_PESANT_OFFSET;
            if(canBuild()){
                this.heuristic -= BUILD_PESANT_OFFSET;
            }
        }

//        for(Peasant peasant : this.peasants.values()) {
//            if(peasant.isCarrying()) {
//                this.heuristic -= peasant.getCurrGold() + peasant.getCurrWood();
//            } else {
//                if(peasantCanHarvest(peasant)) {
//                    this.heuristic -= 50;
//                } else if(!isResourceLocation(peasant.getPosition())) {
//                    this.heuristic += 100;
//                }
//            }
//        }

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
        return obtainedGold >= REQUIRED_GOLD_TO_BUILD && this.peasants.size() < MAX_NUM_PEASANTS;
    }

//	public List<GameState> generateChildren() {
//		List<GameState> children = new ArrayList<GameState>();
//		if(buildPeasants && this.canBuild()) {
//			GameState buildChild = new GameState(this);
//			BuildAction action = new BuildAction(TOWN_HALL_ID, PEASANT_TEMPLATE_ID);
//			if(action.preconditionsMet(buildChild)) {
//				action.apply(buildChild);
//				children.add(buildChild);
//			}
//			return children;
//		}
//
//		GameState child = new GameState(this);
//		for(Peasant peasant : this.peasants.values()) {
//			if(peasant.hasResource()) {
//				if(peasant.getPosition().equals(TOWN_HALL_POSITION)) {
//					DepositAction action = new DepositAction(peasant);
//					if(action.preconditionsMet(child)) {
//						action.apply(child);
//					}
//				} else {
//					MoveAction action = new MoveAction(peasant, TOWN_HALL_POSITION);
//					if(action.preconditionsMet(child)) {
//						action.apply(child);
//					}
//				}
//			} else if(peasantCanHarvest(peasant)) {
//				for(Resource resource : this.resources.values()) {
//					HarvestAction action = new HarvestAction(peasant, resource);
//					if(action.preconditionsMet(child)) {
//						action.apply(child);
//					}
//				}
//			} else {
//				for(Resource resource : this.resources.values()) {
//					GameState innerChild = new GameState(child);
//					MoveAction action = new MoveAction(peasant, resource.getPosition());
//					if(action.preconditionsMet(innerChild)) {
//						action.apply(innerChild);
//					}
//					for(Peasant other : this.peasants.values()) {
//						if(!other.equals(peasant) && !other.hasResource() && !peasantCanHarvest(peasant)) {
//							if(resource.getAmount() >= RESOURCE_AMOUNT_TO_TAKE * 2) {
//								MoveAction otherAction = new MoveAction(other, resource.getPosition());
//								if(otherAction.preconditionsMet(innerChild)) {
//									otherAction.apply(innerChild);
//								}
//							}
//						}
//					}
//					children.add(innerChild);
//				}
//			}
//		}
//		children.add(child);
//
//		for(Peasant peasant : this.peasants.values()) {
//			GameState innerChild = new GameState(this);
//
//			DepositAction depositAction = new DepositAction(peasant);
//			if(depositAction.preconditionsMet(innerChild)) {
//				depositAction.apply(innerChild);
//			}
//
//			for(Resource resource : this.resources.values()) {
//				GameState innerInnerChild = new GameState(innerChild);
//				StripsAction action = null;
//				if(peasant.getPosition().equals(resource.getPosition())) {
//					action = new HarvestAction(peasant, resource);
//				} else {
//					action = new MoveAction(peasant, resource.getPosition());
//				}
//				if(action.preconditionsMet(innerInnerChild)) {
//					action.apply(innerInnerChild);
//				}
//				children.add(innerInnerChild);
//			}
//
//			MoveAction moveAction = new MoveAction(peasant, TOWN_HALL_POSITION);
//			if(moveAction.preconditionsMet(innerChild)) {
//				moveAction.apply(innerChild);
//			}
//
//			children.add(innerChild);
//		}
//
//		return children;
//	}

    public List<GameState> generateChildren() {
        List<GameState> children = new ArrayList<GameState>();
        if(buildPeasants && this.canBuild()) {
            GameState buildChild = new GameState(this);
            BuildPeasantAction action = new BuildPeasantAction(TOWN_HALL_ID, PEASANT_TEMPLATE_ID);
            if(action.preconditionsMet(buildChild)) {
                action.apply(buildChild);
                children.add(buildChild);
            }
            return children;
        }

        GameState child = new GameState(this);
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
            } else if (peasantCanHarvest(peasant)) {
                HarvestAction action = new HarvestAction(peasant, getResourceForPosition(peasant.getPosition()));
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

        for(Peasant peasant : this.peasants.values()) {
            GameState innerChild = new GameState(this);

//			DepositAction depositAction = new DepositAction(peasant);
//			if(depositAction.preconditionsMet(innerChild)) {
//				depositAction.apply(innerChild);
//			}
//
//			for(Resource resource : this.resources.values()) {
//				GameState innerInnerChild = new GameState(innerChild);
//				StripsAction action = null;
//				if(peasant.getPosition().equals(resource.getPosition())) {
//					action = new HarvestAction(peasant, resource);
//				} else {
//					action = new MoveAction(peasant, resource.getPosition());
//				}
//				if(action.preconditionsMet(innerInnerChild)) {
//					action.apply(innerInnerChild);
//				}
//				children.add(innerInnerChild);
//			}

            MoveAction moveAction = new MoveAction(peasant, TOWN_HALL_POSITION);
            if(moveAction.preconditionsMet(innerChild)) {
                moveAction.apply(innerChild);
            }

            children.add(innerChild);
        }

        return children;
    }


    public void applyBuildPeasantAction() {
        this.obtainedGold = this.obtainedGold - REQUIRED_GOLD_TO_BUILD;
        Peasant peasant = new Peasant(nextId, new Position(TOWN_HALL_POSITION));
        nextId++;
        this.peasants.put(peasant.getId(), peasant);
    }

    public void applyMoveAction(int peasantId, Position destination) {
        getPeasantWithId(peasantId).setPosition(destination);
    }

    public void applyHarvestAction(int peasantId, int resourceId) {
        Resource resource = getResourceWithId(resourceId);
        Peasant peasant = getPeasantWithId(peasantId);
        if(resource.isGold()) {
            peasant.setCurrGold(Math.min(100, resource.getAmount()));
            resource.setAmountLeft(Math.max(0, resource.getAmount() - 100));
        } else {
            peasant.setCurrWood(Math.min(100, resource.getAmount()));
            resource.setAmountLeft(Math.max(0, resource.getAmount() - 100));
        }
    }

    public void applyDepositAction(int peasantId) {
        Peasant peasant = getPeasantWithId(peasantId);
        if(peasant.carryingGold()) {
            addToObtainedGold(peasant.getCurrGold());
            peasant.setCurrGold(0);
        } else {
            addToObtainedWood(peasant.getCurrWood());
            peasant.setCurrWood(0);
        }
    }

    public void update(StripsAction action) {
        plan.add(action);
        this.cost += action.getCost();
    }

    /**
     *
     * @param o The other game state to compare
     * @return 1 if this state costs more than the other, 0 if equal, -1 otherwise
     */
    @Override
    public int compareTo(GameState o) {
        if(this.heuristic() > o.heuristic()){
            return 1;
        } else if(this.heuristic() < o.heuristic()){
            return -1;
        }
        return 0;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + obtainedGold;
        result = prime * result + obtainedWood;
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
        if (obtainedGold != other.obtainedGold)
            return false;
        if (obtainedWood != other.obtainedWood)
            return false;
        if (peasants == null) {
            if (other.peasants != null)
                return false;
        } else if (!peasants.equals(other.peasants))
            return false;
        return true;
    }
}
