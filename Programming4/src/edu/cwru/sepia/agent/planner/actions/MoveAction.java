package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.agent.planner.Position;

public class MoveAction implements StripsAction {
    private Peasant peasant;
    private Position goalPosition;

    public MoveAction(Peasant peasant, Position goalPosition) {
        this.peasant = peasant;
        this.goalPosition = goalPosition;
    }

    @Override
    public boolean preconditionsMet(GameState state) {
        return !peasant.getPosition().equals(goalPosition);
    }

    @Override
    public GameState apply(GameState state) {
        return null;
    }
}
