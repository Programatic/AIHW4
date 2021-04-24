package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;

public class BuildAction implements StripsAction {
	int townhallId;
	int peasantTemplateId;	
	
	public BuildAction(int townhallId, int peasantTemplateId) {
		this.townhallId = townhallId;
		this.peasantTemplateId = peasantTemplateId;
	}

	@Override
	public boolean preconditionsMet(GameState state) {
		return state.canBuild();
	}

	@Override
	public void apply(GameState state) {
		state.applyBuildAction();
		state.update(this);
	}

	@Override
	public int getPeasantID() {
		return townhallId;	
	}

}
