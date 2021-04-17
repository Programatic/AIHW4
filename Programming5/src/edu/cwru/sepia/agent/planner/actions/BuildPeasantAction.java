package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Position;

public class BuildPeasantAction implements  StripsAction {
    private int townGold, townFood;

    public BuildPeasantAction(int townGold, int townFood) {
        this.townFood = townFood;
        this.townGold = townGold;
    }

    @Override
    public boolean preconditionsMet(GameState state) {
        return townFood >= 0 && townGold >= GameState.BUILD_GOLD_NEEDED;
    }

    @Override
    public GameState apply(GameState state) {
        state.applyBuildAction();
        state.update(this);
        return state;
    }

    @Override
    public int getPeasantID() {
        return 0;
    }

    @Override
    public Position getPosition() {
        return null;
    }

    @Override
    public double getCost() {
        return 1;
    }
}
