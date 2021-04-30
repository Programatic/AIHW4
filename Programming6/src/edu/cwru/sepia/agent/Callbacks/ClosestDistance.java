package edu.cwru.sepia.agent.Callbacks;

import edu.cwru.sepia.agent.RLAgent;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;

public class ClosestDistance extends FeatureCallback {
    @Override
    public double execute(RLAgent agent, State.StateView stateView, History.HistoryView historyView, int attackerId, int defenderId) {
        int closestEnemy = -1;
        int closestDistance = Integer.MAX_VALUE;

        Unit.UnitView attacker = stateView.getUnit(attackerId);

        for (int enemyID : agent.getEnemyFootmen()) {
            Unit.UnitView enemyUnit = stateView.getUnit(enemyID);
            int distance = RLAgent.manhattanDistance(attacker.getXPosition(), attacker.getYPosition(), enemyUnit.getXPosition(), enemyUnit.getYPosition());
            if (distance < closestDistance) {
                closestEnemy = enemyID;
                closestDistance = distance;
            }
        }

        if (closestEnemy == defenderId)
            return 1;

        return 0;
    }
}
