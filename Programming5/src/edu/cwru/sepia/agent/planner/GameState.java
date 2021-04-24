package edu.cwru.sepia.agent.planner;

import java.util.*;

import edu.cwru.sepia.agent.planner.actions.*;
import edu.cwru.sepia.agent.planner.actions.BuildPeasantAction;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;

/**
 * 
 * @author Sarah Whelan
 */
public class GameState implements Comparable<GameState> {
	public static Position TOWN_HALL_POSITION;
	public static int TOWN_HALL_ID;
	
	private static final int RESOURCE_AMOUNT_TO_TAKE = 100; // Unit-less amount of resource
	private static final int BUILD_PESANT_OFFSET = 20000; // Unit-less
	private static final int BUILD_GOLD_NEEDED = 400; // in Gold amount
	private static final int MAX_NUM_PEASANTS = 3;

	private static Set<Position> resourcePositions = new HashSet<Position>();

	private Map<Integer, Peasant> peasants = new HashMap<Integer, Peasant>(3);
	private Map<Integer, Resource> resources = new HashMap<Integer, Resource>(7);
	private List<StripsAction> plan = new ArrayList<StripsAction>(300);


	/////////////////


	private static int REQUIRED_GOLD, REQUIRED_WOOD;
	private static boolean BUILD_PEASANTS;

	private int currGold = 0, currWood = 0, currFood = 2, nextId = Integer.MIN_VALUE;

	private double cost = 0, heuristic;

	/**
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
			resourcePositions.add(pos);;
		}

		for (Unit.UnitView unit : state.getAllUnits()) {
			Position pos = new Position(unit.getXPosition(), unit.getYPosition());
			if (unit.getTemplateView().getName().equalsIgnoreCase("townhall")) {
				TOWN_HALL_ID = unit.getID();
				TOWN_HALL_POSITION = pos;
			} else {
				Peasant peasant = new Peasant(unit.getID(), pos);
				this.peasants.put(unit.getID(), peasant);
				this.currFood--;
			}
		}
		this.nextId = 1 + this.peasants.size() + this.resources.size();
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
	
	/**
	 *
	 * @return true if the goal conditions are met in this instance of game state.
	 */
	public boolean isGoal() {
		return currGold >= REQUIRED_GOLD && currWood >= REQUIRED_WOOD;
	}

	public List<GameState> generateChildren() {
		List<GameState> children = new ArrayList<GameState>();
		if(BUILD_PEASANTS && this.canBuild()) {
			GameState buildChild = new GameState(this);
			BuildPeasantAction action = new BuildPeasantAction();
			if(action.preconditionsMet(buildChild)) {
				action.apply(buildChild);
				children.add(buildChild);
			}
			return children;
		}

		GameState child = new GameState(this);
		for(Peasant peasant : this.peasants.values()) {			
			if(peasant.isCarrying()) {
				if(peasant.getPosition().equals(TOWN_HALL_POSITION)) {
					DepositAction action = new DepositAction(peasant);
					if(action.preconditionsMet(child)) {
						action.apply(child);
					}
				} else {
					MoveAction action = new MoveAction(peasant, TOWN_HALL_POSITION);
					if(action.preconditionsMet(child)) {
						action.apply(child);
					}
				}
			} else if(peasantCanHarvest(peasant)) {
				for(Resource resource : this.resources.values()) {
					HarvestAction action = new HarvestAction(peasant, resource);
					if(action.preconditionsMet(child)) {
						action.apply(child);
					}
				}
			} else {
				for(Resource resource : this.resources.values()) {
					GameState innerChild = new GameState(child);
					MoveAction action = new MoveAction(peasant, resource.getPosition());
					if(action.preconditionsMet(innerChild)) {
						action.apply(innerChild);
					}
					for(Peasant other : this.peasants.values()) {
						if(!other.equals(peasant) && !other.isCarrying() && !peasantCanHarvest(peasant)) {
							if(resource.getAmount() >= RESOURCE_AMOUNT_TO_TAKE * 2) {
								MoveAction otherAction = new MoveAction(other, resource.getPosition());
								if(otherAction.preconditionsMet(innerChild)) {
									otherAction.apply(innerChild);
								}
							}
						}
					}
					children.add(innerChild);
				}
			}
		}
		children.add(child);
		
		for(Peasant peasant : this.peasants.values()) {
			GameState innerChild = new GameState(this);
			
			DepositAction depositAction = new DepositAction(peasant);
			if(depositAction.preconditionsMet(innerChild)) {
				depositAction.apply(innerChild);
			}
			
			for(Resource resource : this.resources.values()) {
				GameState innerInnerChild = new GameState(innerChild);
				StripsAction action = null;
				if(peasant.getPosition().equals(resource.getPosition())) {
					action = new HarvestAction(peasant, resource);
				} else {
					action = new MoveAction(peasant, resource.getPosition());
				}
				if(action.preconditionsMet(innerInnerChild)) {
					action.apply(innerInnerChild);
				}
				children.add(innerInnerChild);
			}
			
			MoveAction moveAction = new MoveAction(peasant, TOWN_HALL_POSITION);
			if(moveAction.preconditionsMet(innerChild)) {
				moveAction.apply(innerChild);
			}
			
			children.add(innerChild);
		}
		
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

	public void applyBuildPeasantAction() {
		Peasant peasant = new Peasant(nextId, new Position(TOWN_HALL_POSITION));
		peasants.put(nextId, peasant);
		this.currFood--;
        this.currGold -= BUILD_GOLD_NEEDED;
		this.nextId++;
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
		if (this.heuristic != 0) {
			return heuristic;
		}

		if (currGold <= REQUIRED_GOLD) {
			this.heuristic += (REQUIRED_GOLD - currGold);
		}

		if (currWood <= REQUIRED_WOOD) {
			this.heuristic += (REQUIRED_WOOD - currWood);
		}

		if(BUILD_PEASANTS) {
			this.heuristic += currFood * 1000 - (canBuild() ? 1 : 0) * 1000;
		}

		for(Peasant peasant : this.peasants.values()) {
		    this.heuristic += (peasant.isCarrying() ? -(peasant.getCurrGold() + peasant.getCurrWood()) : peasantCanHarvest(peasant) ? -100 : 100);
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