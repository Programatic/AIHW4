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

    /**
     * @return a map from unitId to Action containing all actions that should be applied at the next turn
     */
    @Override
    public Map<Integer, Action> middleStep(State.StateView stateView, History.HistoryView historyView) {
    	Map<Integer, Action> actionMap = new HashMap<Integer, Action>();

    	if(plan.isEmpty()) {
    		return actionMap;
    	}
    	
    	int previousTurnNumber = stateView.getTurnNumber() - 1;
    	
    	if(previousTurnNumber < 0) {
    		addNextAction(actionMap, stateView);
    		return actionMap;
    	}

		Map<Integer, ActionResult> previousActions = historyView.getCommandFeedback(playernum, previousTurnNumber);
		boolean done = false;
		while(!done) {
			if(plan.empty()) {
				done = true;
			} else {
				StripsAction next = plan.peek();
				ActionResult previous = previousActions.get(next.getUnitId());
				if(lastActionFailed(previous)) {
					actionMap.put(previous.getAction().getUnitId(), previous.getAction());
				}
				if(!peasantAvailable(actionMap, next, previous)) {
					done = true;
				} else {
					if(waitOnBuild(actionMap, next)) { 
						done = true;
					} else {
						addNextAction(actionMap, stateView);
					}
				}
			}
		}
    	return actionMap;
    }

	private boolean lastActionFailed(ActionResult previous) {
		return previous != null && previous.getFeedback() == ActionFeedback.FAILED;
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
    	if(!action.isDirectedAction()) {
    		sepiaAction = action.createSepiaAction(null);
    	} else {
    		UnitView peasant = state.getUnit(action.getUnitId());
    		if(peasant == null) {
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
