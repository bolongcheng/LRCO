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
	public static final int x_svd = 1;
	public static final int y_svd = 2;
	public static final int shift_mat = 3;

	private static final String matlab_directory = "/opt/matlab-r2015a/bin/matlab";
	private static final String cd_directory = "cd('/home/vault/bcheng/LRCO')";

	private MatlabProxyFactory factory;
	private MatlabProxy proxy;
	private MatlabTypeConverter processor;
	private VFApprox[] VF_approx;
	private double[][] sampled_states;
	private double[][] sample_VF; // value function computed for sampled states
	private FRState[] sampStates;

	public FRSolver_sparseLR(Parameter param_) {
		super(param_);
		VF_approx = new VFApprox[Parameter.NoTwoSecPerFiveMin+1];
	}

	public void initializeStates() {
		MatlabProxyFactoryOptions options = new MatlabProxyFactoryOptions.Builder().setMatlabLocation(matlab_directory)
				.setUsePreviouslyControlledSession(true).setHidden(true).build();
		factory = new MatlabProxyFactory(options);
		try {
			proxy = factory.getProxy();
		} catch (MatlabConnectionException e) {
			// TODO Auto-generated catch block
			System.out.println("Error: MatlabProxyFactory connection");
			e.printStackTrace();
		}

		// string input to call random_states.m function and find the sampled states
		int RD_length = param.getGrange().length * param.getDrange().length;
		String random_states_input = "random_states(" + param.getRrange().length + "," + RD_length + ",1,3,"
				+ param.get_row_part() + "," + param.get_col_part() + ");";
		try {
			// CHANGE TO YOUR DIRECTORY
			proxy.eval(cd_directory);
			proxy.eval("sample_states = " + random_states_input); // find sample
			processor = new MatlabTypeConverter(proxy);
			MatlabNumericArray sample_states = processor.getNumericArray("sample_states");
			double[][] java_sample_states = sample_states.getRealArray2D();
			sampled_states = java_sample_states;
			sampStates = new FRState[sampled_states.length];
			for (int i = 0; i < sampled_states.length; i++) {
				int r = (int) sampled_states[i][0];
				int g_d_idx = (int) sampled_states[i][1];
				sampStates[i] = (FRState) ArrayOfStates[r * RD_length + g_d_idx];
				sampStates[i].initialize(param);
			}
		} catch (MatlabInvocationException e) {
			System.out.println("Error: random_states.m initialization.");
			e.printStackTrace();
		}
		System.out.println("Sample size: " + sampled_states.length);
		sample_VF = new double[sampled_states.length][1];
		VF_approx[Parameter.NoTwoSecPerFiveMin] = new VFApprox(param);
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
		for (int t = Parameter.NoTwoSecPerFiveMin - 1; t >= 0; t--) {
			VF_approx[t] = new VFApprox(param);
			for (int i = 0; i < sampStates.length; i++) {
				findMax(sampStates[i], t, i);
			}
			try {
				processor.setNumericArray("sample_VF", new MatlabNumericArray(sample_VF, null));
				String vf_approx_input = "svd_approx_partitionLS(sample_VF," + param.get_row_part() + ","
						+ param.get_col_part() + "," + t + ");";

				proxy.eval("[x_vec,y_vec,shift_vec] = " + vf_approx_input);
				double[][][] x = processor.getNumericArray("x_vec").getRealArray3D();
				double[][][] y = processor.getNumericArray("y_vec").getRealArray3D();
				double[][] shift = processor.getNumericArray("shift_vec").getRealArray2D();
				VF_approx[t].set_x_vector(x);
				VF_approx[t].set_y_vector(y);
				VF_approx[t].set_shift(shift);

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
			float cost = state.getCurrCost(i) + findNextStateExpectValue(state, i, t);
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
		sample_VF[sample_i][0] = max;
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
		float[] ProbNext = ((FRState) state).getNextProb();
		float sum = 0;
		int baseIndex = Rnext * (param.getDrange().length * param.getGrange().length) + Gnext;
		for (int j = 0; j < Dnext.length; j++) {
			float Vnext = (float) VF_approx[t + 1].get_V_approx(Rnext, Gnext, Dnext[j]);
			if (t + 1 == Parameter.NoTwoSecPerFiveMin)
				Vnext = ArrayOfStates[baseIndex + Dnext[j] * param.getGrange().length].getValueFunction(t + 1);
			sum += ProbNext[j] * Vnext;
		}
		return sum;
	}
	
	/**
	 *  TODO: should this be kept here?
	 * @param x_in
	 * @param y_in
	 * @param shift_in
	 */
	
	public void setVFApprox(float[][] x_in, float[][] y_in, float[][] shift_in) {
		for (int i = 0; i < Parameter.NoTwoSecPerFiveMin+1; i++) {
			VF_approx[i] = new VFApprox(param);
			VF_approx[i].set_x_vector(x_in[i]);
			VF_approx[i].set_y_vector(y_in[i]);
			VF_approx[i].set_shift(shift_in[i]);
		}
	}

	public VFApprox[] getVFApprox() {
		return VF_approx;
	}

	public float[][] getVFApprox(int choice) {
		float[][] output = null;
		switch (choice) {
		case x_svd:
			output = new float[Parameter.NoTwoSecPerFiveMin+1][VF_approx[0].get_x_vector1d().length];
			for (int i = 0; i < Parameter.NoTwoSecPerFiveMin+1; i++) {
				output[i] = VF_approx[i].get_x_vector1d();
			}
			break;
		case y_svd:
			output = new float[Parameter.NoTwoSecPerFiveMin+1][VF_approx[0].get_y_vector1d().length];
			for (int i = 0; i < Parameter.NoTwoSecPerFiveMin+1; i++) {
				output[i] = VF_approx[i].get_y_vector1d();
			}
			break;
		case shift_mat:
			output = new float[Parameter.NoTwoSecPerFiveMin+1][VF_approx[0].get_shift1d().length];
			for (int i = 0; i < Parameter.NoTwoSecPerFiveMin+1; i++) {
				output[i] = VF_approx[i].get_shift1d();
			}
			break;
		}
		return output;
	}

	public void writeVFSVD(String filePrefix) {
		String xfile = filePrefix + "_x.csv";
		String yfile = filePrefix + "_y.csv";
		String shiftfile = filePrefix + "_shift.csv";
		FastIO.Write2DFloatArray(xfile, getVFApprox(FRSolver_sparseLR.x_svd));
		FastIO.Write2DFloatArray(yfile, getVFApprox(FRSolver_sparseLR.y_svd));
		FastIO.Write2DFloatArray(shiftfile, getVFApprox(FRSolver_sparseLR.shift_mat));
	}
	
	/**
	 * TODO: needs to be changed
	 */

	public float[][] getValueFunction(int choice, int horizon) {
		float[][] output = new float[horizon][NumOfStates];
		int s = 0;
		switch (choice) {
		case ValueFunction:
			for (int r = 0; r < param.getRrange().length; r++) {
				for (int d = 0; d < param.getDrange().length; d++) {
					for (int g = 0; g < param.getGrange().length; g++) {
						for (int t = 0; t < horizon; t++) {
							output[t][s] = (float) VF_approx[t].get_V_approx(r, g, d);
							s++;
						}
					}
				}
			}
			break;
		case OptiAction:
//			for (int s = 0; s < NumOfStates; s++) {
//				for (int t = 0; t < horizon; t++) {
//					output[t][s] = ArrayOfStates[s].getOptAction(t);
//				}
//			}
			break;
		}
		return output;
	}

}
