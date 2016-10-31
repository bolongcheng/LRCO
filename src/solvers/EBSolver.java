package solvers;

import java.text.SimpleDateFormat;
import java.util.Date;

import states.EBState;
import states.State;
import utilities.Parameter;

/**
 * This version of the EBsolver assumes that the regulation market clearing price PD is constant for
 * the horizon of 24 hours; therefore, the EBsolver only needs to solve for the problem for the
 * horizon of 1 hour and only needs to deal with the dynamics of one price process (LMP).
 * 
 * @author bcheng
 * @version 10/26/16
 *
 */
public class EBSolver extends Solver {

	/**
	 * Constructor
	 * 
	 * @param param_
	 */
	public EBSolver(Parameter param_) {
		param = param_;
		NumOfStates = param.getRrange().length * param.getGrange().length * param.getPErange().length;
		ArrayOfStates = new EBState[NumOfStates];
	}

	/**
	 * TODO: need to check if this part works or not.
	 */
	public float findNextStateExpectValue(State state, int actionIndex, int t) {
		float value = 0;
		int i = 0;
		// intra-hour transition, PD fixed, PE changes
		int GPE_length = param.getGrange().length * param.getPErange().length;
		EBState[] nextstates;
		float[] prob;
		if (t == Parameter.NO_FIVE_MIN_PER_HR - 1) {
			nextstates = new EBState[((EBState) state).getRGnextProbs(actionIndex).length];
			prob = new float[nextstates.length];
			for (int rg = 0; rg < nextstates.length; rg++) {
				nextstates[rg] = (EBState) ArrayOfStates[((EBState) state).getRGnextStates(actionIndex)[rg][0]
						* GPE_length + ((EBState) state).getRGnextStates(actionIndex)[rg][1] * param.getPErange().length
						+ ((EBState) state).getPE()];
				prob[i] = ((EBState) state).getRGnextProbs(actionIndex)[rg];
			}
		} else {
			nextstates = new EBState[((EBState) state).getRGnextProbs(actionIndex).length * param.getPErange().length];
			prob = new float[nextstates.length];
			i = 0;
			for (int pe = 0; pe < param.getPErange().length; pe++) {
				if (param.getFmProb()[pe][t + 1] > 0.000001) {
					for (int rg = 0; rg < ((EBState) state).getRGnextProbs(actionIndex).length; rg++) {
						nextstates[i] = (EBState) ArrayOfStates[((EBState) state).getRGnextStates(actionIndex)[rg][0]
								* GPE_length
								+ ((EBState) state).getRGnextStates(actionIndex)[rg][1] * param.getPErange().length
								+ pe];
						prob[i] = ((EBState) state).getRGnextProbs(actionIndex)[rg] * param.getFmProb()[pe][t + 1];
						i++;
					}
				}
			}
		}

		for (i = 0; i < nextstates.length; i++) {
			if (nextstates[i] != null)
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
							Parameter.NO_FIVE_MIN_PER_HR);
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
