package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Position;

/**
 * A useful start of an interface representing strips actions. You may add new methods to this interface if needed, but
 * you should implement the ones provided. You may also find it useful to specify a method that returns the effects
 * of a StripsAction.
 */
public interface StripsAction {

    /**    
     * 
     * @param state GameState to check if action is applicable
     * @return true if apply can be called, false otherwise
     */
    public boolean preconditionsMet(GameState state);

    /**
     *
     * @param state State to apply action to
	 */
    public void apply(GameState state);

    public default Position getPosition() {
    	return null;
    }
	
	/**
	 * 
	 * @return the id of the unit to perform the action
	 */
	public int getPeasantID();
	
	public default double getCost() {
		return 1;
	}
}
