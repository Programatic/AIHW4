package edu.cwru.sepia.agent.callbacks;

import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;

import java.util.List;
import java.util.Map;

public class Constant extends Callback {
    @Override
    public double execute(List<Integer> enemies, List<Integer> friend, Map<Integer, Integer> prev, State.StateView stateView, History.HistoryView historyView, int id, int id2) {
        return 1;
    }
}
