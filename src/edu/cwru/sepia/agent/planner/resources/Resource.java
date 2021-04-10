package edu.cwru.sepia.agent.planner.resources;

import edu.cwru.sepia.agent.planner.Position;

public abstract class Resource {
	protected int id;
	protected int amountLeft;
	protected Position position;
	
	public abstract boolean isGold();
	
	public abstract boolean isWood();
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getAmountLeft() {
		return amountLeft;
	}
	
	public void setAmountLeft(int amountLeft) {
		this.amountLeft = amountLeft;
	}
	
	public Position getPosition() {
		return position;
	}
	
	public void setPosition(Position position) {
		this.position = position;
	}

	public boolean hasRemaining() {
		return amountLeft > 0;
	}
	
}