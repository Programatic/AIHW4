package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.agent.planner.Resource;

public class HarvestAction implements StripsAction {
	private final int resourceId, peasantID;
	private final Position peasantPos, resourcePos;
	private final boolean hasResource, peasantCarrying;

	public HarvestAction(Peasant peasant, Resource resource) {
		this.peasantID = peasant.getId();
		this.resourceId = resource.getID();
		this.peasantPos = peasant.getPosition();
		this.resourcePos = resource.getPosition();
		this.hasResource = resource.hasRemaining();
		this.peasantCarrying = peasant.isCarrying();
	}

	@Override
	public boolean preconditionsMet(GameState state) {
		return hasResource && !peasantCarrying && peasantPos.equals(resourcePos);
	}

	@Override
	public void apply(GameState state) {
		state.applyHarvestAction(peasantID, resourceId);
		state.update(this);
	}

	@Override
	public int getPeasantID() {
		return peasantID;
	}

	@Override
	public Position getPosition() {
		return this.resourcePos;
	}

	@Override
	public double getCost() {
		return 1;
	}
}
