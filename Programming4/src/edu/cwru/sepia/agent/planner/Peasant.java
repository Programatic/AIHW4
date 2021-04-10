package edu.cwru.sepia.agent.planner;

public class Peasant {
    private int id;
    private Position position;
    private int currGold, currWood;

    public Peasant(int id, Position position) {
        this.id = id;
        this.position = position;
    }

    public boolean isCarrying() {
        return this.currGold > 0 || this.currWood > 0;
    }

    public int getCurrGold() {
        return currGold;
    }

    public void setCurrGold(int currGold) {
        this.currGold = currGold;
    }

    public int getCurrWood() {
        return currWood;
    }

    public void setCurrWood(int currWood) {
        this.currWood = currWood;
    }

    public int getId() {
        return id;
    }

    public Position getPosition() {
        return position;
    }
}
