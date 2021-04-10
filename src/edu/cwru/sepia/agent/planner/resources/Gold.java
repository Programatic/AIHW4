package edu.cwru.sepia.agent.planner.resources;

import edu.cwru.sepia.agent.planner.Position;

public class Gold extends Resource {
	public Gold(int id, int amountLeft, Position position) {
		this.id = id;
		this.amountLeft = amountLeft;
		this.position = position;
	}

	public Gold(Resource value) {
		this.id = value.id;
		this.amountLeft = value.amountLeft;
		this.position = new Position(value.position);
	}

	@Override
	public boolean isGold() {
		return true;
	}

	@Override
	public boolean isWood() {
		return false;
	}
}