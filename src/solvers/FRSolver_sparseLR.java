package solvers;

import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;
import matlabcontrol.extensions.*;
import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxyFactoryOptions;

import states.State;
import states.FRState;
import utilities.FastIO;
import utilities.Parameter;
import utilities.VFApprox;

public class FRSolver_sparseLR extends FRSolver {
	public static final int X_SVD = 1;
	public static final int Y_SVD = 2;
	public static final int SHIFT_MAT = 3;

	private final String MATLAB_DIR = "/opt/matlab-r2015a/bin/matlab";
	private final String CD_DIR = "cd('/home/vault/bcheng/LRCO')";

	private MatlabProxyFactory factory;
	private MatlabProxy proxy;
	private MatlabTypeConverter processor;
	private VFApprox[] vfApprox;
	private double[][] sampleStatesCoord;
	private double[][] sampleVF; // value function computed for sampled states
	private FRState[] sampledStates;

	public FRSolver_sparseLR(Parameter param_) {
		super(param_);
		vfApprox = new VFApprox[Parameter.NO_TWO_SEC_PER_FIVE_MIN + 1];
	}

	public void initializeStates() {
		MatlabProxyFactoryOptions options = new MatlabProxyFactoryOptions.Builder().setMatlabLocation(MATLAB_DIR)
				.setUsePreviouslyControlledSession(true).setHidden(true).build();
		factory = new MatlabProxyFactory(options);
		try {
			proxy = factory.getProxy();
		} catch (MatlabConnectionException e) {
			System.out.println("Error: MatlabProxyFactory connection");
			e.printStackTrace();
		}

		// string input to call random_states.m function and find the sampled states
		int DGLength = param.getGrange().length * param.getDrange().length;
		String random_states_input = "random_states(" + param.getRrange().length + "," + DGLength + ",1,3,"
				+ param.getRowPart() + "," + param.getColPart() + ");";
		try {
			// CHANGE TO YOUR DIRECTORY
			proxy.eval(CD_DIR);
			proxy.eval("sample_states = " + random_states_input); // find sample
			processor = new MatlabTypeConverter(proxy);
			MatlabNumericArray sample_states = processor.getNumericArray("sample_states");
			double[][] java_sample_states = sample_states.getRealArray2D();
			sampleStatesCoord = java_sample_states;
			sampledStates = new FRState[sampleStatesCoord.length];
			for (int i = 0; i < sampleStatesCoord.length; i++) {
				int RIdx = (int) sampleStatesCoord[i][0];
				int DGIdx = (int) sampleStatesCoord[i][1];
				sampledStates[i] = (FRState) arrayOfStates[RIdx * DGLength + DGIdx];
				sampledStates[i].initialize(param);
			}
		} catch (MatlabInvocationException e) {
			System.out.println("Error: random_states.m initialization.");
			e.printStackTrace();
		}
		System.out.println("Sample size: " + sampleStatesCoord.length);
		sampleVF = new double[sampleStatesCoord.length][1];
		vfApprox[Parameter.NO_TWO_SEC_PER_FIVE_MIN] = new VFApprox(param);
	}

	public void closeMatlab() {
		try {
			proxy.exit();
		} catch (MatlabInvocationException e) {
			e.printStackTrace();
		}
	}

	public void solveBDP() {
		long start = System.currentTimeMillis();
		for (int t = Parameter.NO_TWO_SEC_PER_FIVE_MIN - 1; t >= 0; t--) {
			vfApprox[t] = new VFApprox(param);
			for (int i = 0; i < sampledStates.length; i++) {
				findMax(sampledStates[i], t, i);
			}
			try {
				processor.setNumericArray("sampleVF", new MatlabNumericArray(sampleVF, null));
				String vfaInputString = "svd_approx_partitionLS(sampleVF," + param.getRowPart() + ","
						+ param.getColPart() + "," + t + ");";
				proxy.eval("[x_vec,y_vec,shift_vec] = " + vfaInputString);
				double[][][] x = processor.getNumericArray("x_vec").getRealArray3D();
				double[][][] y = processor.getNumericArray("y_vec").getRealArray3D();
				double[][] shift = processor.getNumericArray("shift_vec").getRealArray2D();
				vfApprox[t].setXVector(x);
				vfApprox[t].setYVector(y);
				vfApprox[t].setShift(shift);

			} catch (MatlabInvocationException e) {
				System.out.println("Error: svd_approx_partition.m");
				e.printStackTrace();
			}
			long now = System.currentTimeMillis();
			System.out.println("Step: " + t + " elspased:" + (now - start) + "ms");
			start = now;
		}
	}

	/**
	 * Computes V(S_t) = max_{x_t} C(S_t, x_t) + E [ V_{t+1} (S_{t+1}) | S_t]
	 * 
	 * @param state
	 * @param t:
	 *            indexing time
	 */
	public void findMax(State state, int t, int sample_i) {
		float max = Float.NEGATIVE_INFINITY;
		int maxIndex = -1;
		for (int i = 0; i < state.getFeasibleActions().size(); i++) {
			float cost = state.getCostFunction(i) + findNextStateExpectValue(state, i, t);
			if (cost > max) {
				max = cost;
				maxIndex = i;
			} else if (cost == max) {
				if (state.getTieBreak(i, maxIndex)) {
					max = cost;
					maxIndex = i;
				}
			}
		}
		sampleVF[sample_i][0] = max;
		state.setValueFunction(max, t);
		state.setOptAction(maxIndex, t);
	}

	/**
	 * Computes E [ V_{t+1} (S_{t+1}) | S_t]
	 * 
	 * @param state,
	 * @param i,
	 *            indexing the i-th feasible action for the state
	 * @param t,
	 *            indexing time
	 * @return
	 */
	public float findNextStateExpectValue(State state, int i, int t) {
		int Rnext = ((FRState) state).getRnext(i);
		int Gnext = ((FRState) state).getGnext(i);
		int[] Dnext = ((FRState) state).getDnext();
		float[] ProbNext = ((FRState) state).getDnextProb();
		float sum = 0;
		int baseIndex = Rnext * (param.getDrange().length * param.getGrange().length) + Gnext;
		for (int j = 0; j < Dnext.length; j++) {
			float Vnext = (float) vfApprox[t + 1].getVApprox(Rnext, Gnext, Dnext[j]);
			if (t + 1 == Parameter.NO_TWO_SEC_PER_FIVE_MIN)
				Vnext = arrayOfStates[baseIndex + Dnext[j] * param.getGrange().length].getValueFunction(t + 1);
			sum += ProbNext[j] * Vnext;
		}
		return sum;
	}

	/**
	 * TODO: should this be kept here?
	 * 
	 * @param x_in
	 * @param y_in
	 * @param shift_in
	 */

	public void setVFApprox(float[][] x_in, float[][] y_in, float[][] shift_in) {
		for (int i = 0; i < vfApprox.length; i++) {
			vfApprox[i] = new VFApprox(param);
			vfApprox[i].setXVector(x_in[i]);
			vfApprox[i].setYVector(y_in[i]);
			vfApprox[i].setShift(shift_in[i]);
		}
	}

	public VFApprox[] getVFApprox() {
		return vfApprox;
	}

	public float[][] getVFApprox(int choice) {
		float[][] output = null;
		switch (choice) {
		case X_SVD:
			output = new float[Parameter.NO_TWO_SEC_PER_FIVE_MIN + 1][vfApprox[0].getXVector1D().length];
			for (int i = 0; i < Parameter.NO_TWO_SEC_PER_FIVE_MIN + 1; i++) {
				output[i] = vfApprox[i].getXVector1D();
			}
			break;
		case Y_SVD:
			output = new float[Parameter.NO_TWO_SEC_PER_FIVE_MIN + 1][vfApprox[0].getYVector1D().length];
			for (int i = 0; i < Parameter.NO_TWO_SEC_PER_FIVE_MIN + 1; i++) {
				output[i] = vfApprox[i].getYVector1D();
			}
			break;
		case SHIFT_MAT:
			output = new float[Parameter.NO_TWO_SEC_PER_FIVE_MIN + 1][vfApprox[0].getShift1D().length];
			for (int i = 0; i < Parameter.NO_TWO_SEC_PER_FIVE_MIN + 1; i++) {
				output[i] = vfApprox[i].getShift1D();
			}
			break;
		}
		return output;
	}

	public void writeVFSVD(String filePrefix) {
		String xfile = filePrefix + "_x.dat";
		String yfile = filePrefix + "_y.dat";
		String shiftfile = filePrefix + "_shift.dat";
		FastIO.write2DFloatArray(xfile, getVFApprox(FRSolver_sparseLR.X_SVD));
		FastIO.write2DFloatArray(yfile, getVFApprox(FRSolver_sparseLR.Y_SVD));
		FastIO.write2DFloatArray(shiftfile, getVFApprox(FRSolver_sparseLR.SHIFT_MAT));
	}

	/**
	 * TODO: needs to be changed
	 */

	public float[][] getValueFunction(int choice, int horizon) {
		float[][] output = new float[horizon][numOfStates];
		int s = 0;
		switch (choice) {
		case VALUE_FUNCTION:
			for (int r = 0; r < param.getRrange().length; r++) {
				for (int d = 0; d < param.getDrange().length; d++) {
					for (int g = 0; g < param.getGrange().length; g++) {
						for (int t = 0; t < horizon; t++) {
							output[t][s] = (float) vfApprox[t].getVApprox(r, g, d);
							s++;
						}
					}
				}
			}
			break;
		case OPTI_ACTION:
			// for (int s = 0; s < NumOfStates; s++) {
			// for (int t = 0; t < horizon; t++) {
			// output[t][s] = ArrayOfStates[s].getOptAction(t);
			// }
			// }
			break;
		}
		return output;
	}

}
