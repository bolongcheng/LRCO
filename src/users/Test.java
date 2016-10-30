package users;

import java.io.File;

import solvers.EBSolver;
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
		param.readStateSpace(dirName + File.separator + "Input" + File.separator + "eb_test" + numTest + ".csv");

		String RGprob = dirName + File.separator + "Input" + File.separator + "RGprob.csv";
		String RGstate = dirName + File.separator + "Input" + File.separator + "RGstates.csv";
		String RGindex = dirName + File.separator + "Input" + File.separator + "RGindex.csv";
		RGtransHelper helper = new RGtransHelper(RGprob, RGstate, RGindex);
		param.setRGtransmat(helper.getRGtransProb());
		param.setRGtransOffset(helper.getRGtransState());

		param.setPErange(CSVIO.Read1DArray(dirName + File.separator + "Input" + File.separator + "lmp_lvls.csv"));
		float[][] fm_prob = CSVIO.Read2DArray(
				dirName + File.separator + "Input" + File.separator + "fm_prob_123_2015.csv", param.getPErange().length,
				Parameter.NoFiveMinPerHr);
		param.setFm_prob(fm_prob);

		EBSolver solve = new EBSolver(param);
		solve.populateStates();
		solve.initializeStates();
		solve.solveBDP();

	}
}
