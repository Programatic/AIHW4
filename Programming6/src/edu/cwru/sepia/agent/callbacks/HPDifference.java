package edu.cwru.sepia.agent.callbacks;

import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;

import java.util.List;
import java.util.Map;

public class HPDifference extends Callback{
    @Override
    public double execute(List<Integer> enemies, List<Integer> footmen, Map<Integer, Integer> prev, State.StateView stateView, History.HistoryView historyView, int attacker, int defender) {
        Unit.UnitView attack = stateView.getUnit(attacker);
        Unit.UnitView defend = stateView.getUnit(defender);

        int ahp, dhp;

        if (attack == null)
            ahp = 0;
        else
            ahp = attack.getHP();

        if (defend == null)
            dhp = 0;
        else
            dhp = defend.getHP();

        return ahp - dhp;
    }
}
