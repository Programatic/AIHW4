package edu.cwru.sepia.agent.callbacks;

import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;

import java.util.List;
import java.util.Map;

public abstract class Callback {
    public abstract double execute(List<Integer> enemies, List<Integer> footmen, Map<Integer, Integer> prevTarget,
                                   State.StateView stateView, History.HistoryView historyView, int attacker, int defender);
}
