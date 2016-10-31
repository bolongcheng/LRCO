package simulators;

import states.EBObjFun;
import states.EBState;
import utilities.DiscreteHelpers;
import utilities.Parameter;

public class EBSimulator extends Simulator {

	protected float[][] lmp;
	protected int[][] lmpClusters;
	
	public EBSimulator(Parameter param_, int num) {
		param = param_;
		numTrial = num;
	}
	
	public void setLMP(float[][] lmp_) {
		lmp = lmp_;
	}
	
	public void setLMPClusters(int[][] lmpClusters_){
		lmpClusters = lmpClusters_;
	}
	
	public void RunSimulation() {
		path = new float[numTrial][6][Parameter.NO_FIVE_MIN_PER_HR + 1];
		int R = 0;
		int G = 0;
		int GPE_length = param.getGrange().length * param.getPErange().length;
		// simulating R,G trajectories
		for (int i = 0; i < numTrial; i++) {
			R = param.getRrange().length / 2;
			G = 0;
			for (int j = 0; j < Parameter.NO_FIVE_MIN_PER_HR; j++) {
				path[i][0][j] = param.getRrange()[R];
				path[i][1][j] = param.getGrange()[G];
				path[i][2][j] = lmp[i][j];

				EBState tempState = (EBState) solver
						.getState(R * GPE_length + G * param.getPErange().length + lmpClusters[i][j]);
				// FindMax(tempState, j);
				if (tempState.getOptAction(j) == -1) {
					solver.findMax(tempState, j);
				}
				int RGIndex = DiscreteHelpers.getProb((float) Math.random(),
						tempState.getRGnextProbs(tempState.getOptAction(j)));
				R = tempState.getRGnextStates(tempState.getOptAction(j))[RGIndex][0];
				G = tempState.getRGnextStates(tempState.getOptAction(j))[RGIndex][1];
				// Using real-time LMP price
				path[i][3][j] = EBObjFun.getRandCost(param, tempState, lmp[i][j], tempState.getOptAction(j), R);
				path[i][4][j] = param.getDrange()[EBState.getActionSpace()[tempState.getFeasibleActions()
						.get(tempState.getOptAction(j))][0]] * Parameter.NO_TWO_SEC_PER_FIVE_MIN * param.getDeltat();
				path[i][5][j] = param.getXGrange()[EBState.getActionSpace()[tempState.getFeasibleActions()
						.get(tempState.getOptAction(j))][1]];

			}
			path[i][0][Parameter.NO_FIVE_MIN_PER_HR] = param.getRrange()[R];
			path[i][1][Parameter.NO_FIVE_MIN_PER_HR] = param.getGrange()[G];

			EBState tempState = (EBState) solver
					.getState(R * GPE_length + G * param.getPErange().length + lmpClusters[i][0]);

			path[i][3][Parameter.NO_FIVE_MIN_PER_HR] = tempState.getValueFunction(Parameter.NO_FIVE_MIN_PER_HR);
		}

	}

}
