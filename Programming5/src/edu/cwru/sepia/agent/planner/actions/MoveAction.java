package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.util.Direction;

public class MoveAction implements StripsAction {
	Peasant peasant;
	Position destination;

	public MoveAction(Peasant peasant, Position destination) {
		this.peasant = peasant;
		this.destination = destination;
	}

	public Position getDestination() {
		return destination;
	}

	@Override
	public boolean preconditionsMet(GameState state) {
		return !peasant.getPosition().equals(destination);
	}

	@Override
	public GameState apply(GameState state) {
		state.applyMoveAction(this, peasant.getId(), destination);
		state.updatePlanAndCost(this);
		return state;
	}

	@Override
	public int getUnitId() {
		return peasant.getId();	
	}
	
	@Override
	public double getCost() {
		return peasant.getPosition().chebyshevDistance(destination) - 1;
	}

}
