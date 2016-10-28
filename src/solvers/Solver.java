package solvers;

import states.State;
import utilities.Parameter;

public abstract class Solver {
	protected Parameter param;
	protected State[] ArrayOfStates;
	protected int NumOfStates;

	public static final int ValueFunction = 1;
	public static final int OptiAction = 2;

	public int getNumOfStates() {
		return NumOfStates;
	}

	public void solveBDP() {
		long start = System.currentTimeMillis();
		for (int t = ArrayOfStates[0].getValueFunction().length - 2; t >= 0; t--) {
			for (int s = 0; s < ArrayOfStates.length; s++) {
				findMax(ArrayOfStates[s], t);
			}
			long end = System.currentTimeMillis();
			System.out.println("STEP: " + t + " elpased: " + (end - start) + "ms");
			start = end;
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
			 float cost = state.getCurrCost(i) + findNextStateExpectValue(state, i, t);
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

	public float[][] getValueFunction(int choice, int horizon) {
		float[][] output = new float[horizon][NumOfStates];
		switch (choice) {
		case ValueFunction:
			for (int s = 0; s < NumOfStates; s++) {
				for (int t = 0; t < horizon; t++) {
					output[t][s] = ArrayOfStates[s].getValueFunction(t);
				}
			}

			break;
		case OptiAction:
			for (int s = 0; s < NumOfStates; s++) {
				for (int t = 0; t < horizon; t++) {
					output[t][s] = ArrayOfStates[s].getOptAction(t);
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
		case ValueFunction:
			for (int s = 0; s < NumOfStates; s++) {
				for (int t = 0; t < VF.length; t++) {
					ArrayOfStates[s].setValueFunction(VF[t][s], t);
				}
			}
			break;
		case OptiAction:
			for (int s = 0; s < NumOfStates; s++) {
				for (int t = 0; t < VF.length; t++) {
					ArrayOfStates[s].setOptAction((int) VF[s][t], t);
				}
			}
			break;
		}
	}

	public State getState(int s) {
		return ArrayOfStates[s];
	}
}
