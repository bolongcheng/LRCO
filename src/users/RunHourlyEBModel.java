package users;

import java.io.File;

import solvers.EBSolver;
import solvers.RSolver;
import solvers.Solver;
import utilities.CSVIO;
import utilities.DiscreteHelpers;
import utilities.FastIO;
import utilities.IOHelpers;
import utilities.Parameter;

public class RunHourlyEBModel {

	public static void main(String[] args) {
		// String dirName = "";
		String dirName = "/Users/mac/Dropbox/JavaWorkspace/LRCO";
		if (args.length != 0)
			dirName = args[0];
		int startHr;
		if (args.length > 1)
			startHr = Integer.parseInt(args[1]);
		else
			startHr = 0;
		Parameter param = new Parameter();
		String inputFolder = dirName + File.separator + "Input" + File.separator;
		String outputFolder = dirName + File.separator + "Output" + File.separator;
		param.readStaticParameters(inputFolder + "static_param.csv");
		param.readStateSpace(inputFolder + "r_test0.csv");
		param.setPErange(CSVIO.read1DFloatArray(inputFolder + "lmp_lvls.csv"));
		param.setPDrange(CSVIO.read1DFloatArray(inputFolder + "rmp_lvls.csv"));
		float[][] fmProbDay = CSVIO.read2DFloatArray(inputFolder + "fm_prob_alt_123_2015.csv",
				param.getPErange().length, Parameter.NO_FIVE_MIN_PER_HR * 24);
		param.setFmProb(fmProbDay);

		RSolver RSolve = new RSolver(param);
		RSolve.populateStates();

		String rVF = outputFolder + "ENGVF_123.dat";
		File f = new File(rVF);
		if (!f.exists()) {
			RSolve.initializeStates();
			RSolve.solveBDP();
			FastIO.write2DFloatArray(rVF, RSolve.getValueFunction(Solver.VALUE_FUNCTION));
		}
		float[][] VF = FastIO.read2DFloatArray(rVF, Parameter.NO_FIVE_MIN_PER_HR * 24 + 1, RSolve.getNumOfStates());
		float[][] terminalVF = IOHelpers.convert1Dto2D(VF[(startHr + 1) * Parameter.NO_FIVE_MIN_PER_HR],
				param.getRrange().length, param.getPErange().length);
		String RGprob = inputFolder + "RGprob.csv";
		String RGstate = inputFolder + "RGstates.csv";
		String RGindex = inputFolder + "RGindex.csv";
		RGtransHelper helper = new RGtransHelper(RGprob, RGstate, RGindex);
		param.setRGtransMat(helper.getRGtransProb());
		param.setRGtransOffset(helper.getRGtransState());
		param.readStateSpace(inputFolder + "eb_test0.csv");
		float[][] fmProb = DiscreteHelpers.getSubArrayByColumn(fmProbDay, startHr * Parameter.NO_FIVE_MIN_PER_HR,
				(startHr + 1) * Parameter.NO_FIVE_MIN_PER_HR);
		param.setFmProb(fmProb);
		EBSolver EBSolve = new EBSolver(param);
		for (int i = 0; i < param.getPDrange().length; i++) {
			param.setPD(param.getPDrange()[i]);
			EBSolve.populateStates(terminalVF);
			EBSolve.initializeStates();
			EBSolve.solveBDP();
			float[][] EBVF = EBSolve.getValueFunction(Solver.VALUE_FUNCTION);
			float[][] EBOA = EBSolve.getValueFunction(Solver.OPTI_ACTION);
			String EBVFDir = outputFolder + "PD" + i + File.separator;
			f = new File(EBVFDir);
			if (!f.isDirectory()) {
				f.mkdirs();
			}
			FastIO.write2DFloatArray(EBVFDir + "EBVF_" + startHr + "hr.dat", EBVF);
			FastIO.write2DFloatArray(EBVFDir + "EBOA_" + startHr + "hr.dat", EBOA);
		}
	}

}
