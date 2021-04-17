package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionResult;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.agent.planner.actions.*;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Template;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.util.Direction;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * This is an outline of the PEAgent. Implement the provided methods. You may add your own methods and members.
 */
public class PEAgent extends Agent {

	// The plan being executed
	private Stack<StripsAction> plan = null;

	// maps the real unit Ids to the plan's unit ids
	// when you're planning you won't know the true unit IDs that sepia assigns. So you'll use placeholders (1, 2, 3).
	// this maps those placeholders to the actual unit IDs.
	private Map<Integer, Integer> peasantIdMap;
	private int townhallId;
	private int peasantTemplateId;

	public PEAgent(int playernum, Stack<StripsAction> plan) {
		super(playernum);
		peasantIdMap = new HashMap<Integer, Integer>();
		this.plan = plan;

	}

	@Override
	public Map<Integer, Action> initialStep(State.StateView stateView, History.HistoryView historyView) {
		// gets the townhall ID and the peasant ID
		for(int unitId : stateView.getUnitIds(playernum)) {
			Unit.UnitView unit = stateView.getUnit(unitId);
			String unitType = unit.getTemplateView().getName().toLowerCase();
			if(unitType.equals("townhall")) {
				townhallId = unitId;
			} else if(unitType.equals("peasant")) {
				peasantIdMap.put(unitId, unitId);
			}
		}

		// Gets the peasant template ID. This is used when building a new peasant with the townhall
		for(Template.TemplateView templateView : stateView.getTemplates(playernum)) {
			if(templateView.getName().toLowerCase().equals("peasant")) {
				peasantTemplateId = templateView.getID();
				break;
			}
		}

		return middleStep(stateView, historyView);
	}

	/**
	 * This is where you will read the provided plan and execute it. If your plan is correct then when the plan is empty
	 * the scenario should end with a victory. If the scenario keeps running after you run out of actions to execute
	 * then either your plan is incorrect or your execution of the plan has a bug.
	 *
	 * For the compound actions you will need to check their progress and wait until they are complete before issuing
	 * another action for that unit. If you issue an action before the compound action is complete then the peasant
	 * will stop what it was doing and begin executing the new action.
	 *
	 * To check a unit's progress on the action they were executing last turn, you can use the following:
	 * historyView.getCommandFeedback(playernum, stateView.getTurnNumber() - 1).get(unitID).getFeedback()
	 * This returns an enum ActionFeedback. When the action is done, it will return ActionFeedback.COMPLETED
	 *
	 * Alternatively, you can see the feedback for each action being executed during the last turn. Here is a short example.
	 * if (stateView.getTurnNumber() != 0) {
	 *   Map<Integer, ActionResult> actionResults = historyView.getCommandFeedback(playernum, stateView.getTurnNumber() - 1);
	 *   for (ActionResult result : actionResults.values()) {
	 *     <stuff>
	 *   }
	 * }
	 * Also remember to check your plan's preconditions before executing!
	 */
	@Override
	public Map<Integer, Action> middleStep(State.StateView stateView, History.HistoryView historyView) {
		Map<Integer, Action> actions = new HashMap<>();
		Map<Integer, ActionResult> prev = historyView.getCommandFeedback(this.playernum, stateView.getTurnNumber() - 1);

		while (!plan.empty()) {
			StripsAction action = plan.peek();
			ActionResult previousAction = prev.get(action.getPeasantID());

			if (previousAction != null || actions.containsKey(action.getPeasantID()))
				break;

			if (!actions.containsKey(action.getPeasantID())) {
				StripsAction a = plan.pop();
				Action sepiaAction = createSepiaAction(a, stateView);
				actions.put(sepiaAction.getUnitId(), sepiaAction);
			}
		}

		return actions;
	}

	/**
	 * Returns a SEPIA version of the specified Strips Action.
	 *
	 * You can create a SEPIA deposit action with the following method
	 * Action.createPrimitiveDeposit(int peasantId, Direction townhallDirection)
	 *
	 * You can create a SEPIA harvest action with the following method
	 * Action.createPrimitiveGather(int peasantId, Direction resourceDirection)
	 *
	 * You can create a SEPIA build action with the following method
	 * Action.createPrimitiveProduction(int townhallId, int peasantTemplateId)
	 *
	 * You can create a SEPIA move action with the following method
	 * Action.createCompoundMove(int peasantId, int x, int y)
	 *
	 * Hint:
	 * peasantId could be found in peasantIdMap
	 *
	 * these actions are stored in a mapping between the peasant unit ID executing the action and the action you created.
	 *
	 * @param action StripsAction
	 * @param stateView
	 * @return SEPIA representation of same action
	 */
	private Action createSepiaAction(StripsAction action, State.StateView stateView) {
		Unit.UnitView peasant = stateView.getUnit(action.getPeasantID());
		Position pos = new Position(peasant.getXPosition(), peasant.getYPosition());
		Position dest = action.getPosition();
		Direction d = null;

		if (dest != null)
			d = pos.getDirection(dest);


		if (action instanceof MoveAction) {
			return Action.createCompoundMove(action.getPeasantID(), ((MoveAction) action).getDestination().x, ((MoveAction) action).getDestination().y);
		} else if (action instanceof HarvestAction) {
			return Action.createPrimitiveGather(peasant.getID(), d);
		} else if (action instanceof DepositAction) {
			return Action.createPrimitiveDeposit(peasant.getID(), d);
		}


		// Action for building a new peasant, not yet needed but nice base case
		return Action.createPrimitiveProduction(townhallId, peasantTemplateId);
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
