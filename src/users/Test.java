package users;

import java.io.File;

import simulators.EBSimulator;
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
		param.setRGtransMat(helper.getRGtransProb());
		param.setRGtransOffset(helper.getRGtransState());

		param.setPErange(CSVIO.read1DFloatArray(dirName + File.separator + "Input" + File.separator + "lmp_lvls.csv"));
		float[][] fmProb = CSVIO.read2DFloatArray(
				dirName + File.separator + "Input" + File.separator + "fm_prob_123_2015.csv", param.getPErange().length,
				Parameter.NO_FIVE_MIN_PER_HR);
		param.setFmProb(fmProb);

		EBSolver solve = new EBSolver(param);
		solve.populateStates();
		solve.initializeStates();
		solve.solveBDP();

		String lmpFile = dirName + File.separator + "Input" + File.separator + "fmlmp_123_2015.csv";
		String lmpClustFile = dirName + File.separator + "Input" + File.separator + "fmlmp_cl_123_2015.csv";

		int num_trial = 10;
		float[][] lmp = CSVIO.read2DFloatArray(lmpFile, num_trial, Parameter.NO_FIVE_MIN_PER_HR);
		int[][] lmpClusters = CSVIO.read2DIntArray(lmpClustFile, num_trial, Parameter.NO_FIVE_MIN_PER_HR);
		EBSimulator sim = new EBSimulator(param, num_trial);
		sim.LoadSolver(solve);
		sim.setLMP(lmp);
		sim.setLMPClusters(lmpClusters);
		sim.RunSimulation();
		CSVIO.write2DFloatArray(dirName + File.separator + "Output" + File.separator + "EBSim.csv",
				IOHelpers.convert3Dto2D(sim.getPath()));

	}
}
