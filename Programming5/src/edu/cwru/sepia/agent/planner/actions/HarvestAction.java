package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.agent.planner.Resource;
import edu.cwru.sepia.util.Direction;

public class HarvestAction implements StripsAction {
	Peasant peasant;
	int resourceId;
	Position peasantPos;
	Position resourcePos;
	boolean hasResource;

	public HarvestAction(Peasant peasant, Resource resource) {
		this.peasant = peasant;
		this.resourceId = resource.getID();
		this.peasantPos = peasant.getPosition();
		this.resourcePos = resource.getPosition();
		this.hasResource = resource.hasRemaining();
	}
	
	@Override
	public boolean preconditionsMet(GameState state) {
		return hasResource && !peasant.isCarrying() && peasantPos.equals(resourcePos);
	}

	@Override
	public GameState apply(GameState state) {
		state.applyHarvestAction(this, peasant.getId(), resourceId);
		state.updatePlanAndCost(this);
		return state;
	}

	@Override
	public Position getPositionForDirection() {
		return resourcePos;
	}
	
	@Override
	public int getUnitId() {
		return peasant.getId();	
	}
}
