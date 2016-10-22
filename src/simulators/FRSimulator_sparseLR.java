package simulators;

import solvers.FRSolver_sparseLR;
import states.FRState;
import utilities.Parameter;

public class FRSimulator_sparseLR extends FRSimulator{

	public FRSimulator_sparseLR(Parameter param_, int num) {
		super(param_, num);
	}
	
	public void RunSimulation() {
		path = new float[num_trial][5][Parameter.NoTwoSecPerFiveMin+1];
		int R = 0;
		int G = 0;
		System.out.println("================================");
		System.out.println("SIMULATE RegD: " + num_trial + " TRIALS.");
		System.out.println("================================");

		//Simulating the entire trajectories.
		for (int i = 0; i < num_trial; i++) {
			// we always start with half-full capacity and full perf score.
			R = param.getRrange().length/2;
			G = 0;
			for (int j = 0; j < Parameter.NoTwoSecPerFiveMin; j++) {
				path[i][RPath][j] = param.getRrange()[R];
				path[i][GPath][j] = param.getGrange()[G];
				path[i][DPath][j] = param.getDrange()[RegDPaths[i][j]];
			
				FRState tempState = (FRState) solver.getState(R + G + RegDPaths[i][j]);
				tempState.initialize(param);
				if (tempState.getOptAction(j) == -1) {
					((FRSolver_sparseLR) solver).findMax(tempState, j);
				}
				path[i][CostPath][j] = tempState.getCurrCost(tempState.getOptAction(j));
				path[i][XDPath][j] = param.getDrange()[tempState.getOptActionString(j)];
				G = tempState.getGnext(tempState.getOptAction(j));
				R = tempState.getRnext(tempState.getOptAction(j));
			}
		}		
	}

}
