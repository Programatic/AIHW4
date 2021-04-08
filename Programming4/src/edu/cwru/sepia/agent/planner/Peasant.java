package edu.cwru.sepia.agent.planner;

public class Peasant {
    private Position position;
    private int currGold, currWood;

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

    public Position getPosition() {
        return position;
    }
}
