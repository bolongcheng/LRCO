package simulators;

import states.RState;
import utilities.DiscreteHelpers;
import utilities.Parameter;

public class RSimulator extends Simulator {
	protected float[][] lmp;
	protected int[][] lmpClusters;

	public static final int R_PATH = 0;
	public static final int LMP_PATH = 1;
	public static final int COST_PATH = 2;
	public static final int XE_PATH = 3;

	public RSimulator(Parameter param_, int num) {
		param = param_;
		numTrial = num;
	}

	public void setLMP(float[][] lmp_) {
		lmp = lmp_;
	}

	public void setLMPClusters(int[][] lmpClusters_) {
		lmpClusters = lmpClusters_;
	}

	public void runSimulation() {
		path = new float[numTrial][XE_PATH + 1][Parameter.NO_FIVE_MIN_PER_HR * 24];
		int R = 0;
		int G = 0;
		// simulating R trajectories
		for (int i = 0; i < numTrial; i++) {
			R = param.getRrange().length / 2;
			G = 0;
			for (int j = 0; j < Parameter.NO_FIVE_MIN_PER_HR * 24; j++) {
				path[i][R_PATH][j] = param.getRrange()[R];
				path[i][LMP_PATH][j] = lmp[i][j];

				RState tempState = (RState) solver.getState(R * param.getPErange().length + lmpClusters[i][j]);
				if (tempState.getOptAction(j) == -1) {
					solver.findMax(tempState, j);
				}
				R = tempState.getRnext(tempState.getOptAction(j));
				// Using real-time LMP price
				path[i][COST_PATH][j] = tempState.getCostFunction(tempState.getOptAction(j)) * lmp[i][j]
						/ param.getPErange()[lmpClusters[i][j]];
				path[i][XE_PATH][j] = param.getDrange()[tempState.getFeasibleActions().get(tempState.getOptAction(j))]
						* Parameter.NO_TWO_SEC_PER_FIVE_MIN * param.getDeltat();
			}
		}
	}

}
