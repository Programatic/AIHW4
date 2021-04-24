package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.util.Direction;

public class DepositAction implements StripsAction {
	int peasantId;
	Position peasantPos;
	Position townHallPos = GameState.TOWN_HALL_POSITION;
	boolean hasResource;
	
	public DepositAction(Peasant peasant) {
		this.peasantId = peasant.getId();
		this.peasantPos = peasant.getPosition();
		this.hasResource = peasant.isCarrying();
	}
	
	@Override
	public boolean preconditionsMet(GameState state) {
		return hasResource && peasantPos.equals(townHallPos);
	}

	@Override
	public GameState apply(GameState state) {
		state.applyDepositAction(this, peasantId);
		state.updatePlanAndCost(this);
		return state;
	}

	@Override
	public Position getPositionForDirection() {
		return townHallPos;
	}
	
	@Override
	public int getUnitId() {
		return peasantId;	
	}

}
