package solvers;

import states.FRState;
import states.State;
import utilities.Parameter;

public class FRSolver_SVD extends FRSolver {
    private float[][][] U;
    private float[][][] S;
    private float[][][] V;
    private int rank;

    public FRSolver_SVD(Parameter param, int rank) {
        super(param);
        this.rank = rank;
    }

    /**
     * Input the three matrices from SVD for the value function, note that the 3rd dimension is time
     *
     * @param U,
     * @param S,
     * @param V,
     */
    public void setValueFunction(float[][][] U, float[][][] S, float[][][] V) {
        this.U = U;
        this.S = S;
        this.V = V;
    }

    /**
     * Computes E [ V_{t+1} (S_{t+1}) | S_t]
     *
     * @param state,
     * @param i,     indexing the i-th feasible action for the state
     * @param t,     indexing time
     * @return
     */
    public float findNextStateExpectValue(State state, int i, int t) {
        int Rnext = ((FRState) state).getRnext(i);
        int Gnext = ((FRState) state).getGnext(i);
        int[] Dnext = ((FRState) state).getDnext();
        float[] ProbNext = ((FRState) state).getDnextProb();
        float sum = 0;
        int baseIndex = Rnext * (param.getDrange().length * param.getGrange().length) + Gnext;
        if (U != null && S != null && V != null) {
            for (int j = 0; j < Dnext.length; j++) {
                float Vnext = findValueFromSVD(Rnext, Dnext[j], Gnext, t);
                sum += ProbNext[j] * Vnext;
            }
        } else {
            for (int j = 0; j < Dnext.length; j++) {
                float Vnext = arrayOfStates[baseIndex + Dnext[j] * param.getGrange().length].getValueFunction(t + 1);
                sum += ProbNext[j] * Vnext;
            }
        }
        return sum;
    }

    /**
     * Reconstruct V_t(S_t) from the SVD,
     * VF_t(r, d, g) = U_t(r,1:rank) * diag(Sigma_t(1:rank)) * V_t(1:rank, d * G_size + g);
     *
     * @param r
     * @param d
     * @param g
     * @param t
     * @return
     */
    public float findValueFromSVD(int r, int d, int g, int t) {
        float vf = 0;
        for (int i = 0; i < rank; i++) {
            vf += U[r][i][t] * S[0][i][t] * V[i][d * param.getGrange().length + g][t];
        }
        arrayOfStates[r * param.getGrange().length * param.getDrange().length + d * param.getGrange().length + g]
                .setValueFunction(vf, t);
        return vf;
    }
}
