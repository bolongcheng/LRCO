package simulators;

import solvers.Solver;
import states.EBObjFun;
import states.EBState;
import utilities.DiscreteHelpers;
import utilities.FastIO;
import utilities.Parameter;

public class EBDaySimulator extends EBSimulator {
	private String[] VFFileNames;

	public EBDaySimulator(Parameter param_, int num) {
		super(param_, num);
	}

	public void setVFFileNames(String[] fileNames) {
		VFFileNames = fileNames;
	}

	public void RunSimulation() {
		int numHrs = 24;
		path = new float[numTrial][6][Parameter.NO_FIVE_MIN_PER_HR * numHrs + 1];
		int R = param.getRrange().length / 2;
		int G = 0;
		int GPELength = param.getGrange().length * param.getPErange().length;
		// simulating R,G trajectories
		for (int h = 0; h < numHrs; h++) {
			float[][] VF = FastIO.read2DFloatArray(VFFileNames[h], Parameter.NO_FIVE_MIN_PER_HR + 1,
					solver.getNumOfStates());
			solver.setValueFunction(VF, Solver.VALUE_FUNCTION);
			for (int i = 0; i < numTrial; i++) {
				G = 0;
				if (h == 0) {
					R = param.getRrange().length / 2;
				} else {
					// reset R to the previous point
					R = DiscreteHelpers.getIndex(path[i][R_PATH][h * Parameter.NO_FIVE_MIN_PER_HR - 1],
							param.getRrange());
				}
				for (int j = 0; j < Parameter.NO_FIVE_MIN_PER_HR; j++) {
					path[i][R_PATH][h * Parameter.NO_FIVE_MIN_PER_HR + j] = param.getRrange()[R];
					path[i][G_PATH][h * Parameter.NO_FIVE_MIN_PER_HR + j] = param.getGrange()[G];
					path[i][LMP_PATH][h * Parameter.NO_FIVE_MIN_PER_HR + j] = lmp[i][h * Parameter.NO_FIVE_MIN_PER_HR
							+ j];

					EBState tempState = (EBState) solver.getState(R * GPELength + G * param.getPErange().length
							+ lmpClusters[i][h * Parameter.NO_FIVE_MIN_PER_HR + j]);
					if (tempState.getOptAction(j) == -1) {
						solver.findMax(tempState, j);
					}
					int RGIndex = DiscreteHelpers.getProb((float) Math.random(),
							tempState.getRGnextProbs(tempState.getOptAction(j)));
					R = tempState.getRGnextStates(tempState.getOptAction(j))[RGIndex][0];
					G = tempState.getRGnextStates(tempState.getOptAction(j))[RGIndex][1];
					// Using real-time LMP price
					path[i][COST_PATH][h * Parameter.NO_FIVE_MIN_PER_HR + j] = EBObjFun.getRandCost(param, tempState,
							lmp[i][h * Parameter.NO_FIVE_MIN_PER_HR + j], tempState.getOptAction(j), R);
					path[i][XE_PATH][h * Parameter.NO_FIVE_MIN_PER_HR + j] = param
							.getDrange()[EBState.getActionSpace()[tempState.getFeasibleActions()
									.get(tempState.getOptAction(j))][0]]
							* Parameter.NO_TWO_SEC_PER_FIVE_MIN * param.getDeltat();
					path[i][XG_PATH][h * Parameter.NO_FIVE_MIN_PER_HR + j] = param.getXGrange()[EBState
							.getActionSpace()[tempState.getFeasibleActions().get(tempState.getOptAction(j))][1]];

				}
				// Add in the regulation market credit
				EBState tempState = (EBState) solver.getState(R * GPELength + G * param.getPErange().length
						+ lmpClusters[i][(h + 1) * Parameter.NO_FIVE_MIN_PER_HR - 1]);
				path[i][COST_PATH][(h + 1) * Parameter.NO_FIVE_MIN_PER_HR - 1] += tempState
						.getValueFunction(Parameter.NO_FIVE_MIN_PER_HR);
			}
		}
	}

}
