package solvers;

import java.text.SimpleDateFormat;
import java.util.Date;

import states.EBState;
import states.EBState4D;
import states.State;
import utilities.Parameter;

public class EB4DSolver extends EBSolver {

	public EB4DSolver(Parameter param_) {
		super(param_);
		numOfStates = param.getRrange().length * param.getGrange().length * param.getPErange().length
				* param.getPDrange().length;
		arrayOfStates = new EBState[numOfStates];
	}

	/**
	 * TODO: need to check if this part works or not.
	 */
	public float findNextStateExpectValue(State state, int actionIndex, int t) {
		float value = 0;
		int i = 0;
		// intra-hour transition, PD fixed, PE changes
		int PEPDLength = param.getPErange().length * param.getPDrange().length;
		int GPEPDLength = param.getGrange().length * PEPDLength;
		EBState[] nextstates;
		float[] prob;
		if (t == Parameter.NO_FIVE_MIN_PER_HR * 24 - 1) {
			nextstates = new EBState[((EBState) state).getRGnextProbs(actionIndex).length];
			prob = new float[nextstates.length];
			for (int rg = 0; rg < nextstates.length; rg++) {
				nextstates[rg] = (EBState) arrayOfStates[((EBState) state)
						.getRGnextStates(actionIndex)[rg][EBState.R_INDEX] * GPEPDLength
						+ ((EBState) state).getRGnextStates(actionIndex)[rg][EBState.G_INDEX] * PEPDLength
						+ ((EBState) state).getPE() * param.getPDrange().length + ((EBState4D) state).getPD()];
				prob[rg] = ((EBState) state).getRGnextProbs(actionIndex)[rg];
			}
		} else if ((t + 1) % Parameter.NO_FIVE_MIN_PER_HR != 0) {
			nextstates = new EBState[((EBState) state).getRGnextProbs(actionIndex).length * param.getPErange().length];
			prob = new float[nextstates.length];
			i = 0;
			for (int pe = 0; pe < param.getPErange().length; pe++) {
				if (param.getFmProb()[((EBState4D) state).getPD() * param.getPErange().length + pe][t + 1] > 0.000001) {
					for (int rg = 0; rg < ((EBState) state).getRGnextProbs(actionIndex).length; rg++) {
						nextstates[i] = (EBState) arrayOfStates[((EBState) state)
								.getRGnextStates(actionIndex)[rg][EBState.R_INDEX] * GPEPDLength
								+ ((EBState) state).getRGnextStates(actionIndex)[rg][EBState.G_INDEX] * PEPDLength
								+ pe * param.getPDrange().length + ((EBState4D) state).getPD()];
						prob[i] = ((EBState) state).getRGnextProbs(actionIndex)[rg] * param.getFmProb()[pe][t + 1];
						i++;
					}
				}
			}
		} else {
			nextstates = new EBState[((EBState) state).getRGnextProbs(actionIndex).length * PEPDLength];
			EBState[] nextstatesAlt = new EBState[nextstates.length];
			prob = new float[nextstates.length];
			i = 0;
			for (int pd = 0; pd < param.getPDrange().length; pd++) {
				for (int pe = 0; pe < param.getPErange().length; pe++) {
					if (param.getPriceProb()[pd * param.getPErange().length + pe][t / Parameter.NO_FIVE_MIN_PER_HR
							+ 1] > 0.000001) {
						for (int rg = 0; rg < ((EBState) state).getRGnextProbs(actionIndex).length; rg++) {
							nextstatesAlt[i] = (EBState4D) arrayOfStates[((EBState) state)
									.getRGnextStates(actionIndex)[rg][EBState.R_INDEX] * GPEPDLength
									+ ((EBState) state).getRGnextStates(actionIndex)[rg][EBState.G_INDEX] * PEPDLength
									+ pd * param.getPErange().length + pe];
							nextstates[i] = (EBState4D) arrayOfStates[((EBState) state)
									.getRGnextStates(actionIndex)[rg][EBState.R_INDEX] * GPEPDLength
									+ pd * param.getPErange().length + pe];

							prob[i] = ((EBState) state).getRGnextProbs(actionIndex)[rg]
									* param.getPriceProb()[pd * param.getPErange().length + pe][t / 12 + 1];
							i++;
						}
					}
				}
			}
			for (i = 0; i < nextstatesAlt.length && nextstatesAlt[i] != null; i++) {
				value += prob[i] * param.getGrange()[nextstatesAlt[i].getG()]
						* (param.getGrange()[nextstatesAlt[i].getG()] >= 0.4 ? 1 : 0);
			}
			value *= param.getK() * param.getPDrange()[((EBState4D) state).getPD()];

		}

		for (i = 0; i < nextstates.length && nextstates[i] != null; i++) {
			value += prob[i] * nextstates[i].getValueFunction(t + 1);
		}

		return value;

	}

	public void populateStates() {
		System.out.println("================================");
		System.out.println("ECO BASEPOINT SOLVER w/ TERMINAL VF");
		Date now = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("MM/dd hh:mm:ss");
		System.out.println("BEGIN INIT: " + ft.format(now));

		int s = 0;
		for (int r = 0; r < param.getRrange().length; r++) {
			for (int g = 0; g < param.getGrange().length; g++) {
				float GPenalty = param.getK() * param.getGrange()[g] * (param.getGrange()[g] >= 0.4 ? 1 : 0);
				for (int pe = 0; pe < param.getPErange().length; pe++) {
					for (int pd = 0; pd < param.getPDrange().length; pd++) {
						EBState4D newState = new EBState4D(param, r, g, pe, pd);
						newState.setValueFunction(param.getPDrange()[pd] * GPenalty, Parameter.NO_FIVE_MIN_PER_HR * 24);
						arrayOfStates[s] = newState;
						s++;
					}
				}
			}
		}
		now = new Date();
		System.out.println("FINISH INIT: " + ft.format(now));
		System.out.println("================================");
		System.out.println("State size/Step: " + numOfStates);
		System.out.println("================================");
	}

	public float[][] getPartialValueFunction(int pe, int pd, int t, int GLast) {
		int PEPDLength = param.getPErange().length * param.getPDrange().length;
		int GPEPDLength = param.getGrange().length * PEPDLength;
		int PEPDIndex = pe * param.getPDrange().length + pd;
		float[][] output = new float[param.getRrange().length][GLast];
		for (int r = 0; r < param.getRrange().length; r++) {
			for (int g = 0; g < GLast; g++) {
				output[r][g] = arrayOfStates[r * GPEPDLength + g * PEPDLength + PEPDIndex].getValueFunction(t);
			}
		}
		return output;
	}
}
