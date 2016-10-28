package simulators;

import solvers.FRSolver;
import states.FRState;
import utilities.Parameter;

public class FRSimulator extends Simulator {

	public static final int RPath = 0;
	public static final int GPath = 1;
	public static final int DPath = 2;
	public static final int CostPath = 3;
	public static final int XDPath = 4;

	protected int[][] RegDPaths;

	public FRSimulator(Parameter param_, int num) {
		param = param_;
		num_trial = num;
	}

	public void setRegDPath(int[][] dt) {
		if (dt.length < num_trial) {
			System.out.println("TRIAL NUMBER MISMATCH");
		} else {
			RegDPaths = dt;
		}
	}

	public void RunSimulation() {
		path = new float[num_trial][5][Parameter.NoTwoSecPerFiveMin + 1];
		int R = 0;
		int G = 0;
		System.out.println("================================");
		System.out.println("SIMULATE RegD: " + num_trial + " TRIALS.");
		System.out.println("================================");
		int DG_length = (param.getGrange().length * param.getDrange().length);
		// Simulating the entire trajectories.
		for (int i = 0; i < num_trial; i++) {
			// we always start with half-full capacity and full perf score.
			R = param.getRrange().length / 2;
			G = 0;
			for (int j = 0; j < Parameter.NoTwoSecPerFiveMin; j++) {
				path[i][RPath][j] = param.getRrange()[R];
				path[i][GPath][j] = param.getGrange()[G];
				path[i][DPath][j] = param.getDrange()[RegDPaths[i][j]];

				FRState tempState = (FRState) solver
						.getState(R * DG_length + RegDPaths[i][j] * param.getGrange().length + G);
				tempState.initialize(param);
				if (tempState.getOptAction(j) == -1) {
					solver.findMax(tempState, j);
				}
				path[i][CostPath][j] = tempState.getCurrCost(tempState.getOptAction(j));
				path[i][XDPath][j] = param.getDrange()[tempState.getOptActionString(j)];
				G = tempState.getGnext(tempState.getOptAction(j));
				R = tempState.getRnext(tempState.getOptAction(j));

			}
		}

	}
}
