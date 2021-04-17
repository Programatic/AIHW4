package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Position;

public class BuildPeasantAction implements StripsAction {
    public BuildPeasantAction() {
    }

    @Override
    public boolean preconditionsMet(GameState state) {
        return state.canBuild();
    }

    @Override
    public GameState apply(GameState state) {
        state.applyBuildPeasantAction();
        state.update(this);
        return state;
    }

    @Override
    public int getPeasantID() {
        return -1;
    }

    @Override
    public Position getPosition() {
        return GameState.TOWN_HALL_POSITION;
    }

    @Override
    public double getCost() {
        return 1;
    }
}
