package edu.cwru.sepia.agent.Callbacks;

import edu.cwru.sepia.agent.RLAgent;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;

public abstract class FeatureCallback {
    public abstract double execute(RLAgent agent, State.StateView stateView, History.HistoryView historyView, int attackerId, int defenderId);
}
