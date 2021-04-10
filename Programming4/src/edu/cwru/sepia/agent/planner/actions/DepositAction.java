package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.agent.planner.Position;

public class DepositAction implements StripsAction {
    int peasantId;
    Position peasantPos;
    Position townHallPos = GameState.TOWN_HALL_POSITION;
    boolean hasResource;

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
}
