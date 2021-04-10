package edu.cwru.sepia.agent.planner.resources;

import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.environment.model.state.ResourceNode;

public class Resource {
	protected int id;
	protected int amount;
	protected Position position;
	private ResourceNode.Type type;

	public Resource(int id, int amount, Position position, ResourceNode.Type type) {
		this.position = position;
		this.amount = amount;
		this.type = type;
		this.id = id;
	}

	public Resource(Resource resource) {
		this.position = new Position(resource.position);
		this.amount = resource.amount;
		this.type = resource.type;
		this.id = resource.id;
	}
	
	public boolean isGold() {
		return this.type == ResourceNode.Type.GOLD_MINE;
	}
	
	public int getID() {
		return id;
	}
	
	public int getAmountLeft() {
		return amount;
	}
	
	public void setAmountLeft(int amountLeft) {
		this.amount = amountLeft;
	}
	
	public Position getPosition() {
		return position;
	}
	
	public void setPosition(Position position) {
		this.position = position;
	}

	public boolean hasRemaining() {
		return amount > 0;
	}
	
}