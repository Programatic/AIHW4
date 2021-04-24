package edu.cwru.sepia.agent.planner;

import java.util.Objects;

public class Peasant {
	private int id;
	private Position position;
	private int numGold = 0;
	private int numWood = 0;

	public Peasant(int id, Position position) {
		this.id = id;
		this.position = position;
	}

	public Peasant(Peasant value) {
		this.id = value.id;
		this.position = new Position(value.position);
		this.numGold = value.numGold;
		this.numWood = value.numWood;
	}

	public boolean isCarrying() {
		return this.numGold > 0 || this.numWood > 0;
	}

	public boolean carryingGold() {
		return numGold > 0;
	}

	public int getCurrGold() {
		return numGold;
	}

	public void setCurrGold(int currGold) {
		this.numGold = currGold;
	}

	public int getCurrWood() {
		return numWood;
	}

	public void setCurrWood(int currWood) {
		this.numWood = currWood;
	}

	public int getId() {
		return id;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Peasant peasant = (Peasant) o;
		return id == peasant.id && numGold == peasant.numGold && numWood == peasant.numWood && position.equals(peasant.position);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, position, numGold, numWood);
	}
}