package users;

import java.io.File;

import solvers.EB4DSolver;
import solvers.Solver;
import utilities.CSVIO;
import utilities.FastIO;
import utilities.Parameter;

public class RunEB4DSolver {

	public static void main(String[] args) {
		// String dirName = "";
		String dirName = "/Users/mac/Dropbox/JavaWorkspace/LRCO";
		if (args.length != 0)
			dirName = args[0];
		int quarter;
		if (args.length > 1)
			quarter = Integer.parseInt(args[1]);
		else
			quarter = 0;
		String[] quarters = { "123", "456", "789", "101112" };
		Parameter param = new Parameter();
		String inputFolder = dirName + File.separator + "Input" + File.separator;
		String outputFolder = dirName + File.separator + "Output" + File.separator;
		param.readStaticParameters(inputFolder + "static_param.csv");
		param.readStateSpace(inputFolder + "eb_test0.csv");
		param.setPErange(CSVIO.read1DFloatArray(inputFolder + "lmp_lvls" + quarters[quarter] + ".csv"));
		param.setPDrange(CSVIO.read1DFloatArray(inputFolder + "rmp_lvls" + quarters[quarter] + ".csv"));
		float[][] hrProb = CSVIO.read2DFloatArray(inputFolder + "prices_prob_"
				+ quarters[quarter] + "_2015.csv", param.getPErange().length * param.getPDrange().length, 24);
		float[][] fmProbDay = CSVIO.read2DFloatArray(inputFolder + "fm_prob_alt_" + quarters[quarter] + "_2015.csv",
				param.getPErange().length * param.getPDrange().length, Parameter.NO_FIVE_MIN_PER_HR * 24);
		param.setPriceProb(hrProb);
		param.setFmProb(fmProbDay);

		String RGprob = inputFolder + "RGprob.csv";
		String RGstate = inputFolder + "RGstates.csv";
		String RGindex = inputFolder + "RGindex.csv";
		RGtransHelper helper = new RGtransHelper(RGprob, RGstate, RGindex);
		param.setRGtransMat(helper.getRGtransProb());
		param.setRGtransOffset(helper.getRGtransState());
		EB4DSolver EBSolve = new EB4DSolver(param);

		EBSolve.populateStates();
		EBSolve.initializeStates();
		EBSolve.solveBDP();
		float[][] EBVF = EBSolve.getValueFunction(Solver.VALUE_FUNCTION);
		float[][] EBOA = EBSolve.getValueFunction(Solver.OPTI_ACTION);
		String EBVFDir = outputFolder + "EB4D" + File.separator;
		FastIO.write2DFloatArray(EBVFDir + "EBVF_Q" + quarter + ".dat", EBVF);
		FastIO.write2DFloatArray(EBVFDir + "EBOA_Q" + quarter + ".dat", EBOA);

	}

}
