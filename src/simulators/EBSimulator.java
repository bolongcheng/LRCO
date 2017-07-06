package simulators;

import states.EBObjFun;
import states.EBState;
import utilities.DiscreteHelpers;
import utilities.Parameter;

public class EBSimulator extends Simulator {

    protected float[][] lmp;
    protected int[][] lmpClusters;

    public static final int R_PATH = 0;
    public static final int G_PATH = 1;
    public static final int LMP_PATH = 2;
    public static final int COST_PATH = 3;
    public static final int XE_PATH = 4;
    public static final int XG_PATH = 5;

    public EBSimulator(Parameter param_, int num) {
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
        path = new float[numTrial][XG_PATH + 1][Parameter.NO_FIVE_MIN_PER_HR + 1];
        int R = 0;
        int G = 0;
        int GPELength = param.getGrange().length * param.getPErange().length;
        // simulating R,G trajectories
        for (int i = 0; i < numTrial; i++) {
            R = param.getRrange().length / 2;
            G = 0;
            for (int j = 0; j < Parameter.NO_FIVE_MIN_PER_HR; j++) {
                path[i][R_PATH][j] = param.getRrange()[R];
                path[i][G_PATH][j] = param.getGrange()[G];
                path[i][LMP_PATH][j] = lmp[i][j];

                EBState tempState = (EBState) solver
                        .getState(R * GPELength + G * param.getPErange().length + lmpClusters[i][j]);
                if (tempState.getOptAction(j) == -1) {
                    solver.findMax(tempState, j);
                }
                int RGIndex = DiscreteHelpers.getProb((float) Math.random(),
                        tempState.getRGnextProbs(tempState.getOptAction(j)));
                R = tempState.getRGnextStates(tempState.getOptAction(j))[RGIndex][EBState.R_INDEX];
                G = tempState.getRGnextStates(tempState.getOptAction(j))[RGIndex][EBState.G_INDEX];
                // Using real-time LMP price
                path[i][COST_PATH][j] = EBObjFun.getRandCost(param, tempState, lmp[i][j], tempState.getOptAction(j), R);
                path[i][XE_PATH][j] = param
                        .getDrange()[EBState.getActionSpace()[tempState.getFeasibleActions()
                        .get(tempState.getOptAction(j))][EBState.XE_INDEX]]
                        * Parameter.NO_TWO_SEC_PER_FIVE_MIN * param.getDeltat();
                path[i][XG_PATH][j] = param.getXGrange()[EBState.getActionSpace()[tempState.getFeasibleActions()
                        .get(tempState.getOptAction(j))][EBState.XG_INDEX]];

            }
            path[i][R_PATH][Parameter.NO_FIVE_MIN_PER_HR] = param.getRrange()[R];
            path[i][G_PATH][Parameter.NO_FIVE_MIN_PER_HR] = param.getGrange()[G];

            EBState tempState = (EBState) solver
                    .getState(R * GPELength + G * param.getPErange().length + lmpClusters[i][0]);

            path[i][COST_PATH][Parameter.NO_FIVE_MIN_PER_HR] = tempState.getValueFunction(Parameter.NO_FIVE_MIN_PER_HR);
        }

    }

}
