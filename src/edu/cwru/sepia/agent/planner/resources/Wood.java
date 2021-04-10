package edu.cwru.sepia.agent.planner.resources;

import edu.cwru.sepia.agent.planner.Position;

public class Wood extends Resource {
	public Wood(int id, int amountLeft, Position position){
		this.id = id;
		this.amountLeft = amountLeft;
		this.position = position;
	}

	public Wood(Resource value) {
		this.id = value.id;
		this.amountLeft = value.amountLeft;
		this.position = new Position(value.position);
	}

	@Override
	public boolean isGold() {
		return false;
	}

	@Override
	public boolean isWood() {
		return true;
	}
}