package edu.cwru.sepia.agent.planner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import edu.cwru.sepia.agent.planner.actions.*;
import edu.cwru.sepia.agent.planner.actions.BuildPeasantAction;
import edu.cwru.sepia.environment.model.state.State;

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
		GameState.REQUIRED_GOLD = requiredGold;
		GameState.REQUIRED_WOOD = requiredWood;
		GameState.BUILD_PEASANTS = buildPeasants;
		state.getAllResourceNodes().stream().forEach(e -> {
			Position position = new Position(e.getXPosition(), e.getYPosition());
			GameState.resourcePositions.add(position);
			Resource r = new Resource(e.getID(), e.getAmountRemaining(), position, e.getType());
			resources.put(e.getID(), r);
		});
		state.getAllUnits().stream().forEach(e -> {
			Position position = new Position(e.getXPosition(), e.getYPosition());
			if(e.getTemplateView().getName().toLowerCase().equals(TOWNHALL_NAME)) {
				GameState.TOWN_HALL_POSITION = position;
				GameState.TOWN_HALL_ID = e.getID();
			} else {
				GameState.PEASANT_TEMPLATE_ID = e.getTemplateView().getID();
				this.peasants.put(e.getID(), new Peasant(e.getID(), TOWN_HALL_POSITION));
			}
		});
		this.nextId = 1 + this.peasants.size() + this.resources.size();
	}

	public GameState(GameState state) {
		this.currGold = state.currGold;
		this.currWood = state.currWood;
		this.nextId = state.nextId;
		this.cost = state.cost;
		state.peasants.values().stream().forEach(e -> this.peasants.put(e.getId(), new Peasant(e)));
		state.resources.values().stream().forEach(e -> {
			this.resources.put(e.getID(), new Resource(e));
		});	
		state.plan.stream().forEach(e -> plan.add(e));
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
		this.currWood += numWood;
	}

	private void addToObtainedGold(int numGold) {
		this.currGold += numGold;
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
		if(this.heuristic != 0) {
			return heuristic;
		}
		
		if(currWood > currGold) {
			this.heuristic += 100;
		}
		if(currGold <= REQUIRED_GOLD) {
			this.heuristic += (REQUIRED_GOLD - currGold);
		} else {
			this.heuristic += (currGold - REQUIRED_GOLD);
		}
		if(currWood <= REQUIRED_WOOD) {
			this.heuristic += (REQUIRED_WOOD - currWood);
		} else {
			this.heuristic += currWood - REQUIRED_WOOD;
		}
		if(BUILD_PEASANTS) {
			this.heuristic += (MAX_NUM_PEASANTS - this.peasants.size()) * BUILD_PESANT_OFFSET;
			if(canBuild()){
				this.heuristic -= BUILD_PESANT_OFFSET;
			}
		}
		
		for(Peasant peasant : this.peasants.values()) {
			if(peasant.isCarrying()) {
				this.heuristic -= peasant.getCurrGold() + peasant.getCurrWood();
			} else {
				if(peasantCanHarvest(peasant)) {
					this.heuristic -= 50;
				} else if(!isResourceLocation(peasant.getPosition())) {
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

	public void applyBuildPeasantAction() {
		this.currGold = this.currGold - REQUIRED_GOLD_TO_BUILD;
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
