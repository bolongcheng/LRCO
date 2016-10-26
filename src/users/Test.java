package users;

import java.io.File;

import simulators.FRSimulator;
import solvers.FRSolver;
import solvers.FRSolver_sparseLR;
import solvers.Solver;
import utilities.CSVIO;
import utilities.FastIO;
import utilities.Parameter;

public class Test {
	public static void main(String[] args) {
		// String dirName = "";
		String dirName = "/Users/mac/Dropbox/JavaWorkspace/LRCO";
		if (args.length != 0)
			dirName = args[0];
		int numTest = 0;
		if (args.length > 1)
			numTest = Integer.parseInt(args[1]);

		Parameter param = new Parameter();
		param.readStaticParameters(dirName + File.separator + "Input" + File.separator + "static_param.csv");
		param.readStateSpace(dirName + File.separator + "Input" + File.separator + "fr_test" + numTest + ".csv");
		
		float[][] dt_trans_prob = CSVIO.Read2DArray(
				dirName + File.separator + "Input" + File.separator + "dt_trans_prob.csv", param.getDrange().length,
				param.getDrange().length);
		param.setDnext(dt_trans_prob);
		FRSolver solve = new FRSolver_sparseLR(param);
		solve.populateStates();
		solve.initializeStates();
		solve.solveBDP();
		((FRSolver_sparseLR)solve).closeMatlab();
		
//		float[][] VF = solve.getValueFunction(Solver.ValueFunction, Parameter.NoTwoSecPerFiveMin + 1);
//		long start = System.currentTimeMillis();
//		CSVIO.Write2DArray(dirName + File.separator + "Output" + File.separator + "VF.csv", VF);
//		long middle = System.currentTimeMillis();
//		FastIO.Write2DFloatArray(dirName + File.separator + "Output" + File.separator + "VF.dat", VF);
//		long done = System.currentTimeMillis();
//		System.out.println("write normal time: " + (middle - start));
//		System.out.println("write faster time: " + (done - middle));
		
		

//		start = System.currentTimeMillis();
//		VF = CSVIO.Read2DArray(dirName + File.separator + "Output" + File.separator + "VF.csv",
//				Parameter.NoTwoSecPerFiveMin + 1, solve.getNumOfStates());
//		middle = System.currentTimeMillis();
//		float[][] VF2 = FastIO.Read2DFloatArray(dirName + File.separator + "Output" + File.separator + "VF.dat",
//				Parameter.NoTwoSecPerFiveMin + 1, solve.getNumOfStates());
//		done = System.currentTimeMillis();
//		System.out.println("read normal time: " + (middle - start));
//		System.out.println("read faster time: " + (done - middle));
//		System.out.println("diff " + isSame(VF, VF2));

		// int num_trial = 10;
		// int[][] dt = CSVIO.read2DArray(dirName + File.separator + "RegDInput" + File.separator +
		// "FiveMinDt_gen20.csv",
		// num_trial, Parameter.NoTwoSecPerFiveMin);
		// FRSimulator simulate = new FRSimulator(param, num_trial);
		// simulate.LoadSolver(solve);
		// simulate.setRegDPath(dt);
		// simulate.RunSimulation();
	}

	public static float isSame(float[][] f1, float[][] f2) {
		float sum = 0;
		for (int i = 0; i < f1.length; i++) {
			for (int j = 0; j < f1[0].length; j++) {
				sum += Math.abs(f1[i][j] - f2[i][j]);
			}
		}
		return sum;
	}
}
