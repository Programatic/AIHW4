package edu.cwru.sepia.agent.callbacks;

import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;

import java.util.List;
import java.util.Map;

public class GroupAttack extends Callback{
    @Override
    public double execute(List<Integer> enemies, List<Integer> footmen, Map<Integer, Integer> prevTarget, State.StateView stateView, History.HistoryView historyView, int attacker, int defender) {
        if (stateView.getTurnNumber() - 1 < 0)
            return 0;

        int count = 0;

        for (int foot : footmen) {
            if (prevTarget.get(foot) == defender)
                count++;
        }

        return count;
    }
}
