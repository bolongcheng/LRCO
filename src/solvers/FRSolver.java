package solvers;

import java.text.SimpleDateFormat;
import java.util.Date;

import states.FRState;
import states.State;
import utilities.DiscreteHelpers;
import utilities.Parameter;

public class FRSolver extends Solver {

	public FRSolver(Parameter param_) {
		param = param_;
		NumOfStates = param.getRrange().length * param.getGrange().length * param.getDrange().length;
		ArrayOfStates = new FRState[NumOfStates];
	}

	/**
	 * Computes the expected downstream value given S_t, E [ V_{t+1} (S_{t+1}) | S_t, x_t]
	 * 
	 * @param state
	 * @param actionIndex
	 *            the actionIndex-th feasible action for the state
	 * @param t
	 *            time index
	 * @return E [ V_{t+1} (S_{t+1}) | S_t, x_t]
	 */

	public float findNextStateExpectValue(State state, int actionIndex, int t) {
		int Rnext = ((FRState) state).getRnext(actionIndex);
		int Gnext = ((FRState) state).getGnext(actionIndex);
		int[] Dnext = ((FRState) state).getDnext();
		float[] ProbNext = ((FRState) state).getNextProb();
		int baseIndex = Rnext * (param.getDrange().length * param.getGrange().length) + Gnext;
		float sum = 0;
		for (int j = 0; j < Dnext.length; j++) {
			float Vnext = ArrayOfStates[baseIndex + Dnext[j] * param.getGrange().length].getValueFunction(t + 1);
			sum += ProbNext[j] * Vnext;
		}
		return sum;
	}

	public void populateStates(float[][] terminal_value_function) {
		System.out.println("================================");
		System.out.println("FREQ REGULATION SOLVER");
		Date now = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("hh:mm:ss");
		System.out.println("BEGIN INIT: " + ft.format(now));
		float[][] Truterminal_value_function = new float[param.getRrange().length][param.getGrange().length];
		if (terminal_value_function != null) {
			// Check Dimension First
			if (terminal_value_function.length != param.getRrange().length
					|| terminal_value_function[0].length != param.getGrange().length)
				Truterminal_value_function = DiscreteHelpers.interpolate(terminal_value_function,
						param.getRrange().length, param.getGrange().length);
		}

		int s = 0;
		if (terminal_value_function == null) {
			for (int r = 0; r < param.getRrange().length; r++) {
				for (int d = 0; d < param.getDrange().length; d++) {
					for (int g = 0; g < param.getGrange().length; g++) {
						FRState newState = new FRState(param, r, g, d);
						newState.setValueFunction(param.getK() * param.getPD() * param.getGrange()[g]
								* (param.getGrange()[g] >= 0.4 ? 1 : 0), Parameter.NoTwoSecPerFiveMin);
						ArrayOfStates[s] = newState;
						s++;
					}
				}
			}
		} else {
			for (int r = 0; r < param.getRrange().length; r++) {
				for (int d = 0; d < param.getDrange().length; d++) {
					for (int g = 0; g < param.getGrange().length; g++) {
						FRState newState = new FRState(param, r, g, d);
						newState.setValueFunction(Truterminal_value_function[r][g], Parameter.NoTwoSecPerFiveMin);
						ArrayOfStates[s] = newState;
						s++;
					}
				}
			}
		}
		now = new Date();
		System.out.println("FINISH INIT: " + ft.format(now));
		System.out.println("================================");
		System.out.println("State size/Time: " + NumOfStates);
		System.out.println("================================");

	}
}
