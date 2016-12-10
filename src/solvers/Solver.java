package solvers;

import states.State;
import utilities.Parameter;

public abstract class Solver {
	protected Parameter param;
	protected State[] arrayOfStates;
	protected int numOfStates;

	public static final int VALUE_FUNCTION = 1;
	public static final int OPTI_ACTION = 2;

	public int getNumOfStates() {
		return numOfStates;
	}

	public void solveBDP() {
//		long start = System.currentTimeMillis();
		for (int t = arrayOfStates[0].getValueFunction().length - 2; t >= 0; t--) {
			for (State s : arrayOfStates) {
				findMax(s, t);
			}
//			long end = System.currentTimeMillis();
//			System.out.println("STEP: " + t + " elpased: " + (end - start) + "ms");
//			start = end;
		}
	}

	/**
	 * Computes V(S_t) = max_{x_t} C(S_t, x_t) + E [ V_{t+1} (S_{t+1}) | S_t] and X(S_t)
	 * 
	 * @param state:
	 *            state S_t
	 * @param t:
	 *            time index
	 */

	public void findMax(State state, int t) {
		float max = Float.NEGATIVE_INFINITY;
		int maxIndex = -1;
		for (int i = 0; i < state.getFeasibleActions().size(); i++) {
			 float cost = state.getCostFunction(i) + findNextStateExpectValue(state, i, t);
			if (cost > max) {
				max = cost;
				maxIndex = i;
			} else if (cost == max) {
				// Tie-breaker, choose the decision with the smallest magnitude
				if (state.getTieBreak(i, maxIndex)) {
					maxIndex = i;
				}
			}
		}
		state.setValueFunction(max, t);
		state.setOptAction(maxIndex, t);
	}

	public abstract float findNextStateExpectValue(State s, int actionIndex, int t);

	public void populateStates() {
		populateStates(null);
	}
	
	public void initializeStates(){
		for (State s: arrayOfStates){
			s.initialize(param);
		}
	}

	public abstract void populateStates(float[][] ValueFunction);

	/**
	 * Output a value function matrix of size T x |S|
	 * 
	 * @param choice
	 *            selector of ValueFnction or OptiAction
	 * @param horizon
	 *            T, the time horizon
	 * @return 2D float array of the value function of dimension T x |S|
	 */

	public float[][] getValueFunction(int choice) {
		int horizon = arrayOfStates[0].getValueFunction().length;
		float[][] output = new float[horizon][numOfStates];
		switch (choice) {
		case VALUE_FUNCTION:
			for (int s = 0; s < numOfStates; s++) {
				for (int t = 0; t < horizon; t++) {
					output[t][s] = arrayOfStates[s].getValueFunction(t);
				}
			}

			break;
		case OPTI_ACTION:
			for (int s = 0; s < numOfStates; s++) {
				for (int t = 0; t < horizon; t++) {
					output[t][s] = arrayOfStates[s].getOptAction(t);
				}
			}
			break;
		}

		return output;
	}

	/**
	 * Input a value function matrix of size T x |S|
	 * 
	 * @param VF
	 *            value function in 2D array of dimension T x |S|
	 * @param choice
	 *            selector of ValueFnction or OptiAction
	 */

	public void setValueFunction(float[][] VF, int choice) {
		switch (choice) {
		case VALUE_FUNCTION:
			for (int s = 0; s < numOfStates; s++) {
				for (int t = 0; t < VF.length; t++) {
					arrayOfStates[s].setValueFunction(VF[t][s], t);
				}
			}
			break;
		case OPTI_ACTION:
			for (int s = 0; s < numOfStates; s++) {
				for (int t = 0; t < VF.length; t++) {
					arrayOfStates[s].setOptAction((int) VF[t][s], t);
				}
			}
			break;
		}
	}

	public State getState(int s) {
		return arrayOfStates[s];
	}
}
