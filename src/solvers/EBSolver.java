package solvers;

import java.text.SimpleDateFormat;
import java.util.Date;

import states.EBState;
import states.State;
import utilities.Parameter;

public class EBSolver extends Solver {

	public EBSolver(Parameter param_) {
		param = param_;
		NumOfStates = param.getRrange().length * param.getGrange().length * param.getPErange().length;
		ArrayOfStates = new EBState[NumOfStates];
	}

	// public void findMax(State state, int t) {
	// float max = Float.NEGATIVE_INFINITY;
	// int maxIndex = -1;
	// for (int i = 0; i < state.getFeasibleActions().size(); i++) {
	// float cost = state.getCurrCost(i) + findNextStateExpectValue(state, i, t);
	// if (cost > max) {
	// max = cost;
	// maxIndex = i;
	// } else if (cost == max) {
	// // Tie-breaker, choose the decision with the smallest magnitude
	// if (state.getTieBreak(i, maxIndex)) {
	// maxIndex = i;
	// }
	// }
	// }
	// state.setValueFunction(max, t);
	// state.setOptAction(maxIndex, t);
	// }

	@Override
	public float findNextStateExpectValue(State state, int actionIndex, int t) {
		float value = 0;
		int i = 0;
		// intra-hour transition, PD fixed, PE changes

		EBState[] nextstates = new EBState[((EBState) state).getRGnextProbs(actionIndex).length
				* param.getPErange().length];
		float[] prob = new float[nextstates.length];
		i = 0;
		for (int pe = 0; pe < param.getPErange().length; pe++) {
			if (param.getFm_prob()[1 * param.getPErange().length + pe][t + 1] > 0.000001) {

				for (int rg = 0; rg < ((EBState) state).getRGnextProbs(actionIndex).length; rg++) {
					nextstates[i] = (EBState) ArrayOfStates[((EBState) state).getRGnextStates(actionIndex)[rg][0]
							* (param.getPErange().length * param.getGrange().length)
							+ ((EBState) state).getRGnextStates(actionIndex)[rg][1] * param.getPErange().length + pe];
					prob[i] = ((EBState) state).getRGnextProbs(actionIndex)[rg]
							* param.getFm_prob()[1 * param.getPErange().length + pe][t + 1];
					i++;
				}
			}
		}

		for (i = 0; i < nextstates.length; i++) {
			value += prob[i] * nextstates[i].getValueFunction(t + 1);
		}

		return value;
	}

	public void populateStates(float[][] ValueFunction) {
		System.out.println("================================");
		System.out.println("ECO BASEPOINT SOLVER w/ TERMINAL VF");
		Date now = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("MM/dd hh:mm:ss");
		System.out.println("BEGIN INIT: " + ft.format(now));

		int s = 0;
		for (int r = 0; r < param.getRrange().length; r++) {
			for (int g = 0; g < param.getGrange().length; g++) {
				for (int pe = 0; pe < param.getPErange().length; pe++) {
					EBState newState = new EBState(param, r, g, pe);
					// set all V_t^1 = 0, for all t;
					newState.setValueFunction(
							param.getK() * param.getPD() * param.getGrange()[g] * (param.getGrange()[g] >= 0.4 ? 1 : 0),
							Parameter.NoFiveMinPerHr * 24);
					ArrayOfStates[s] = newState;
					s++;
				}
			}
		}
		now = new Date();
		System.out.println("FINISH INIT: " + ft.format(now));
		System.out.println("================================");
		System.out.println("State size/Step: " + NumOfStates);
		System.out.println("================================");

	}

}
