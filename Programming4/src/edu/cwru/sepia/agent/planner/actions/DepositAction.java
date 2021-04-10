package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.agent.planner.Position;

public class DepositAction implements StripsAction {
    private int peasantId;
    private Position peasantPos;
    private Position townHallPos = GameState.TOWN_HALL_POSITION;
    private boolean hasResource;

    public DepositAction(Peasant peasant) {
        this.peasantId = peasant.getId();
        this.peasantPos = peasant.getPosition();
        this.hasResource = peasant.isCarrying();
    }

    @Override
    public boolean preconditionsMet(GameState state) {
        return hasResource && peasantPos.equals(townHallPos);
    }

    @Override
    public GameState apply(GameState state) {
        state.applyDepositAction(peasantId);
        state.update(this);
        return state;
    }

    @Override
    public int getPeasantID() {
        return peasantId;
    }

    @Override
    public Position getPosition() {
        return townHallPos;
    }

    @Override
    public double getCost() {
        return 1;
    }
}
