package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.agent.planner.Position;

public class MoveAction implements StripsAction {
	private Position goalPosition, peasantPosition;
	private int peasantID;

	public MoveAction(Peasant peasant, Position goalPosition) {
		this.peasantPosition = peasant.getPosition();
		this.goalPosition = goalPosition;
		this.peasantID = peasant.getId();
	}

	public Position getDestination() {
		return goalPosition;
	}

	@Override
	public boolean preconditionsMet(GameState state) {
		return !peasantPosition.equals(goalPosition);
	}

	@Override
	public GameState apply(GameState state) {
		state.applyMoveAction(peasantID, goalPosition);
		state.update(this);
		return state;
	}

	@Override
	public int getUnitId() {
		return peasantID;
	}
	
	@Override
	public double getCost() {
		return peasantPosition.chebyshevDistance(goalPosition) - 1;
	}

}
