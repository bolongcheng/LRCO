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
		((FRSolver_sparseLR) solve).getVFApprox();

		String OutputPrefix = dirName + File.separator + "Output" + File.separator;
		String xfile = OutputPrefix + "FRVF_x.csv";
		String yfile = OutputPrefix + "FRVF_y.csv";
		String shiftfile = OutputPrefix + "FRVF_shift.csv";
		FastIO.Write2DFloatArray(xfile, ((FRSolver_sparseLR) solve).getVFApprox(FRSolver_sparseLR.x_svd));
		FastIO.Write2DFloatArray(yfile, ((FRSolver_sparseLR) solve).getVFApprox(FRSolver_sparseLR.y_svd));
		FastIO.Write2DFloatArray(shiftfile, ((FRSolver_sparseLR) solve).getVFApprox(FRSolver_sparseLR.shift_mat));

		// float[][] VF = solve.getValueFunction(Solver.ValueFunction, Parameter.NoTwoSecPerFiveMin
		// + 1);
		// FastIO.Write2DFloatArray(dirName + File.separator + "Output" + File.separator + "VF.dat",
		// VF);
		//
		// VF = FastIO.Read2DFloatArray(dirName + File.separator + "Output" + File.separator +
		// "VF.dat",
		// Parameter.NoTwoSecPerFiveMin + 1, solve.getNumOfStates());
		// solve.setValueFunction(VF, Solver.ValueFunction);
		solve = new FRSolver_sparseLR(param);
		solve.populateStates();
		int row_part = param.get_row_part();
		int col_part = param.get_col_part();
		int size_row = param.get_size_row();
		int size_col = param.get_size_col();

		float[][] x_in = FastIO.Read2DFloatArray(xfile, Parameter.NoTwoSecPerFiveMin+1, size_row * row_part * col_part);
		float[][] y_in = FastIO.Read2DFloatArray(yfile, Parameter.NoTwoSecPerFiveMin+1, size_col * row_part * col_part);
		float[][] shift_in = FastIO.Read2DFloatArray(shiftfile, Parameter.NoTwoSecPerFiveMin+1, row_part * col_part);
		((FRSolver_sparseLR) solve).setVFApprox(x_in, y_in, shift_in);
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
