package users;

import java.io.File;

import simulators.FRSimulator;
import solvers.FRSolver;
import solvers.FRSolver_sparseLR;
import solvers.Solver;
import utilities.CSVIO;
import utilities.FastIO;
import utilities.IOHelpers;
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
		((FRSolver_sparseLR) solve).closeMatlab();

		// float[][] VF = solve.getValueFunction(Solver.ValueFunction, Parameter.NoTwoSecPerFiveMin
		// + 1);
		// FastIO.Write2DFloatArray(dirName + File.separator + "Output" + File.separator + "VF.dat",
		// VF);
		//
		// VF = FastIO.Read2DFloatArray(dirName + File.separator + "Output" + File.separator +
		// "VF.dat",
		// Parameter.NoTwoSecPerFiveMin + 1, solve.getNumOfStates());
		// solve.setValueFunction(VF, Solver.ValueFunction);
		int num_trial = 10;
		int[][] dt = CSVIO.read2DArray(dirName + File.separator + "Input" + File.separator + "FiveMinDt_gen10.csv",
				num_trial, Parameter.NoTwoSecPerFiveMin);
		FRSimulator simulate = new FRSimulator(param, num_trial);
		simulate.LoadSolver(solve);
		simulate.setRegDPath(dt);
		simulate.RunSimulation();
		CSVIO.Write2DArray(dirName + File.separator + "Output" + File.separator + "TestSim.csv",
				IOHelpers.Convert3Dto2D(simulate.getPath()));
	}
}
