package edu.cwru.sepia.agent.callbacks;

import edu.cwru.sepia.agent.RLAgent;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ClosestEnemy extends Callback{
    @Override
    public double execute(List<Integer> enemies, List<Integer> footmen, Map<Integer, Integer> prev, State.StateView stateView, History.HistoryView historyView, int attackerId, int defender) {
        int closestid = -1;
        double close = Double.POSITIVE_INFINITY;

        for (int enemy : enemies) {
            double distance = RLAgent.manhattanDistance(stateView, attackerId, enemy);
            if (close > distance) {
                closestid = enemy;
                close = distance;
            }
        }

        return closestid == defender ? 1 : 0;
    }
}
