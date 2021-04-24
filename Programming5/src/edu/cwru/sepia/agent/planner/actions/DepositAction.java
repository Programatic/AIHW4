package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.agent.planner.Position;

public class DepositAction implements StripsAction {
	private final int peasantId;
	private final Position peasantPos, townHallPos = GameState.TOWN_HALL_POSITION;
	private final boolean hasResource;

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
	public void apply(GameState state) {
		state.applyDepositAction(peasantId);
		state.update(this);
	}

	@Override
	public int getPeasantID() {
		return peasantId;
	}

	@Override
	public Position getPosition() {
		return townHallPos;
	}

	@Override
	public double getCost() {
		return 1;
	}
}
