package edu.cwru.sepia.agent.planner;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionFeedback;
import edu.cwru.sepia.action.ActionResult;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.agent.planner.actions.StripsAction;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;

public class PEAgent extends Agent {
    private static final long serialVersionUID = 1L;

    private Stack<StripsAction> plan = null;

    public PEAgent(int playernum, Stack<StripsAction> plan) {
        super(playernum);
        this.plan = plan;
    }

    @Override
    public Map<Integer, Action> initialStep(State.StateView stateView, History.HistoryView historyView) {
        return middleStep(stateView, historyView);
    }

    @Override
    public Map<Integer, Action> middleStep(State.StateView stateView, History.HistoryView historyView) {
        Map<Integer, Action> actions = new HashMap<Integer, Action>();

        if (plan.isEmpty()) {
            return actions;
        }

        int prevNum = stateView.getTurnNumber() - 1;

        Map<Integer, ActionResult> previousActions = historyView.getCommandFeedback(playernum, prevNum);
        while (!plan.empty()) {
            StripsAction action = plan.peek();
            ActionResult previousAction = previousActions.get(action.getUnitId());

            if (!peasantAvailable(actions, action, previousAction)) {
                break;
            } else {
                if (waitOnBuild(actions, action)) {
                    break;
                } else {
                    addNextAction(actions, stateView);
                }
            }
        }
        return actions;
    }

    private boolean peasantAvailable(Map<Integer, Action> actionMap, StripsAction next, ActionResult previous) {
        return !actionMap.containsKey(next.getUnitId()) &&
                !(previous != null && previous.getFeedback().ordinal() == ActionFeedback.INCOMPLETE.ordinal());
    }

    private boolean waitOnBuild(Map<Integer, Action> actionMap, StripsAction next) {
        return next.getUnitId() == GameState.TOWN_HALL_ID && !actionMap.isEmpty();
    }

    private void addNextAction(Map<Integer, Action> actionMap, State.StateView state) {
        StripsAction action = plan.pop();
        Action sepiaAction = null;
        if (!action.isDirectedAction()) {
            sepiaAction = action.createSepiaAction(null);
        } else {
            UnitView peasant = state.getUnit(action.getUnitId());
            if (peasant == null) {
                plan.push(action);
                return;
            }
            Position peasantPos = new Position(peasant.getXPosition(), peasant.getYPosition());
            Position destinationPos = action.getPositionForDirection();
            sepiaAction = action.createSepiaAction(peasantPos.getDirection(destinationPos));
        }
        actionMap.put(sepiaAction.getUnitId(), sepiaAction);
    }

    @Override
    public void terminalStep(State.StateView stateView, History.HistoryView historyView) {

    }

    @Override
    public void savePlayerData(OutputStream outputStream) {

    }

    @Override
    public void loadPlayerData(InputStream inputStream) {

    }
}
