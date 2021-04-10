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

	@Override
	public boolean preconditionsMet(GameState state) {
		return !peasant.getPosition().equals(destination);
	}

	@Override
	public void applyAction(GameState state) {
		state.applyMoveAction(this, peasant.getId(), destination);
	}

	@Override
	public Action createSepiaAction(Direction direction) {
		return Action.createCompoundMove(peasant.getId(), destination.x, destination.y);
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
