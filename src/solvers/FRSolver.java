package solvers;

import java.text.SimpleDateFormat;
import java.util.Date;

import states.FRState;
import states.State;
import utilities.DiscreteHelpers;
import utilities.Parameter;

public class FRSolver extends Solver {
    /**
     * Constructor
     *
     * @param param_
     */
    public FRSolver(Parameter param_) {
        param = param_;
        numOfStates = param.getRrange().length * param.getGrange().length * param.getDrange().length;
        arrayOfStates = new FRState[numOfStates];
    }

    /**
     * Computes the expected downstream value given S_t, E [ V_{t+1} (S_{t+1}) | S_t, x_t]
     *
     * @param state       state object
     * @param actionIndex the actionIndex-th feasible action for the state
     * @param t           time index
     * @return E [ V_{t+1} (S_{t+1}) | S_t, x_t]
     */

    public float findNextStateExpectValue(State state, int actionIndex, int t) {
        int Rnext = ((FRState) state).getRnext(actionIndex);
        int Gnext = ((FRState) state).getGnext(actionIndex);
        int[] Dnext = ((FRState) state).getDnext();
        float[] ProbNext = ((FRState) state).getDnextProb();
        int baseIndex = Rnext * (param.getDrange().length * param.getGrange().length) + Gnext;
        float sum = 0;
        for (int j = 0; j < Dnext.length; j++) {
            float Vnext = arrayOfStates[baseIndex + Dnext[j] * param.getGrange().length].getValueFunction(t + 1);
            sum += ProbNext[j] * Vnext;
        }
        return sum;
    }


    public void populateStates(float[][] terminalValueFunction) {
        System.out.println("================================");
        System.out.println("FREQ REGULATION SOLVER");
        Date now = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("hh:mm:ss");
        System.out.println("BEGIN INIT: " + ft.format(now));
        float[][] extraTerminalValueFunction = new float[param.getRrange().length][param.getGrange().length];
        if (terminalValueFunction != null) {
            // Check Dimension First
            if (terminalValueFunction.length != param.getRrange().length
                    || terminalValueFunction[0].length != param.getGrange().length)
                extraTerminalValueFunction = DiscreteHelpers.interpolate(terminalValueFunction,
                        param.getRrange().length, param.getGrange().length);
        }

        int s = 0;
        if (terminalValueFunction == null) {
            for (int r = 0; r < param.getRrange().length; r++) {
                for (int d = 0; d < param.getDrange().length; d++) {
                    for (int g = 0; g < param.getGrange().length; g++) {
                        FRState newState = new FRState(param, r, g, d);
                        newState.setValueFunction(param.getK() * param.getPD() * param.getGrange()[g]
                                * (param.getGrange()[g] >= 0.4 ? 1 : 0), Parameter.NO_TWO_SEC_PER_FIVE_MIN);
                        arrayOfStates[s] = newState;
                        s++;
                    }
                }
            }
        } else {
            for (int r = 0; r < param.getRrange().length; r++) {
                for (int d = 0; d < param.getDrange().length; d++) {
                    for (int g = 0; g < param.getGrange().length; g++) {
                        FRState newState = new FRState(param, r, g, d);
                        newState.setValueFunction(extraTerminalValueFunction[r][g], Parameter.NO_TWO_SEC_PER_FIVE_MIN);
                        arrayOfStates[s] = newState;
                        s++;
                    }
                }
            }
        }
        now = new Date();
        System.out.println("FINISH INIT: " + ft.format(now));
        System.out.println("================================");
        System.out.println("State size/Time: " + numOfStates);
        System.out.println("================================");

    }
}
