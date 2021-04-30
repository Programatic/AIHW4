package edu.cwru.sepia.agent.Callbacks;

import edu.cwru.sepia.agent.RLAgent;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;

public class RatioHP extends FeatureCallback {
    @Override
    public double execute(RLAgent agent, State.StateView stateView, History.HistoryView historyView, int attackerId, int defenderId) {
        Unit.UnitView attacker = stateView.getUnit(attackerId);
        Unit.UnitView defender = stateView.getUnit(defenderId);

        return ((double) attacker.getHP())/((double) defender.getHP());
    }
}
