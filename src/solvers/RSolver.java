package solvers;

import java.text.SimpleDateFormat;
import java.util.Date;

import states.RState;
import states.State;
import utilities.Parameter;

public class RSolver extends Solver {

	public RSolver(Parameter param_) {
		param = param_;
		numOfStates = param.getRrange().length * param.getPErange().length;
		arrayOfStates = new RState[numOfStates];
	}

	public float findNextStateExpectValue(State state, int actionIndex, int t) {
		int Rnext = ((RState) state).getRnext(actionIndex);
		float cost = 0;
		if (t == state.getValueFunction().length - 2) {
			RState nextState = (RState) arrayOfStates[Rnext * param.getPErange().length + ((RState) state).getPE()];
			return nextState.getValueFunction(t + 1);
		} else {
			for (int i = 0; i < param.getPErange().length; i++) {
				if (param.getFmProb()[i][t + 1] > 0.000001) {
					RState nextState = (RState) arrayOfStates[Rnext * param.getPErange().length + i];
					cost += param.getFmProb()[i][t + 1] * nextState.getValueFunction(t + 1);
				}
			}
			return cost;
		}
	}

	public void populateStates(float[][] ValueFunction) {
		System.out.println("================================");
		System.out.println("ENERGY SHIFTING SOLVER");
		Date now = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("MM/dd hh:mm:ss");
		System.out.println("BEGIN INIT: " + ft.format(now));
		int OpHrs = 24;
		int s = 0;
		for (int r = 0; r < param.getRrange().length; r++) {
			for (int pe = 0; pe < param.getPErange().length; pe++) {
				RState newState = new RState(param, r, pe);
				newState.setValueFunction(0, Parameter.NO_FIVE_MIN_PER_HR * OpHrs);
				arrayOfStates[s] = newState;
				s++;
			}
		}

		now = new Date();
		System.out.println("FINISH INIT: " + ft.format(now));
		System.out.println("================================");
		System.out.println("State size/Step: " + s);
		System.out.println("================================");

	}

}
