package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Position;

public class BuildPeasantAction implements StripsAction {

	@Override
	public boolean preconditionsMet(GameState state) {
		return state.canBuild();
	}

	@Override
	public void apply(GameState state) {
		state.applyBuildPeasantAction();
		state.update(this);
	}

	@Override
	public int getPeasantID() {
		return GameState.TOWN_HALL_ID;
	}

	@Override
	public Position getPosition() {
	    return null;
	}

	@Override
	public double getCost() {
		return 1;
	}
}