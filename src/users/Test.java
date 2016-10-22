package users;

import java.io.File;

import simulators.FRSimulator;
import solvers.FRSolver;
import solvers.Solver;
import utilities.CSVIO;
import utilities.Parameter;

public class Test {
	public static void main(String[] args) {
		// String dirName = "";
		String dirName = "/Users/mac/Dropbox/JavaWorkspace/PJM_RegD";
		if (args.length != 0)
			dirName = args[0];
		int numTest = 0;
		if (args.length > 1)
			numTest = Integer.parseInt(args[1]);

		Parameter param = new Parameter(
				dirName + File.separator + "RegDInput" + File.separator + "test" + numTest + ".csv");
		float[][] dt_trans_prob = CSVIO.Read2DArray(
				dirName + File.separator + "RegDInput" + File.separator + "dt_trans_prob.csv", param.getDrange().length,
				param.getDrange().length);
		param.LoadDnext(dt_trans_prob);
		FRSolver solve = new FRSolver(param);
		solve.populateStates();
		solve.initializeStates();
		solve.solveBDP();
		float[][] VF = solve.getValueFunction(Solver.ValueFunction, Parameter.NoTwoSecPerFiveMin);

		int num_trial = 10;
		int[][] dt = CSVIO.read2DArray(dirName + File.separator + "RegDInput" + File.separator + "FiveMinDt_gen20.csv",
				num_trial, Parameter.NoTwoSecPerFiveMin);
		FRSimulator simulate = new FRSimulator(param, num_trial);
		simulate.LoadSolver(solve);
		simulate.setRegDPath(dt);
		simulate.RunSimulation();
	}
}
