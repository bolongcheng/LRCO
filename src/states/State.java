package states;

import java.util.List;

import utilities.Parameter;

public abstract class State {
	protected int[] OptAction;
	protected float[] V;
	protected List<Integer> feasibleActions;

	public void setValueFunction(float v, int t) {
		V[t] = v;
	}

	public float getValueFunction(int t) {
		return V[t];
	}

	public void setOptAction(int action, int t) {
		OptAction[t] = action;
	}

	public int getOptAction(int t) {
		return OptAction[t];
	}

	public abstract void setFeasibleActions(Parameter param_);

	public List<Integer> getFeasibleActions() {
		return feasibleActions;
	}

	public abstract boolean getTieBreak(int action_index, int maxIndex);

	public abstract float getCurrCost(int action_index);

	public abstract void initialize(Parameter param);

	public float[] getValueFunction() {
		return V;
	}

}
