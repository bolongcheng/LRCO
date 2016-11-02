package users;

import java.io.File;

import solvers.EBSolver;
import solvers.RSolver;
import solvers.Solver;
import utilities.CSVIO;
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
			startHr = 10;
		Parameter param = new Parameter();
		param.readStaticParameters(dirName + File.separator + "Input" + File.separator + "static_param.csv");
		param.readStateSpace(dirName + File.separator + "Input" + File.separator + "eb_test0.csv");
		param.setPErange(CSVIO.read1DFloatArray(dirName + File.separator + "Input" + File.separator + "lmp_lvls.csv"));
		float[][] fmProb = CSVIO.read2DFloatArray(
				dirName + File.separator + "Input" + File.separator + "fm_prob_alt_123_2015.csv", param.getPErange().length,
				Parameter.NO_FIVE_MIN_PER_HR*24);
		param.setFmProb(fmProb);

		RSolver RSolve = new RSolver(param);
		RSolve.populateStates();

		String rVF = dirName + File.separator + "Output" + File.separator + "ENGVF_123.dat";
		File f = new File(rVF);
		if (!f.exists()) {
			RSolve.initializeStates();
			RSolve.solveBDP();
			FastIO.write2DFloatArray(rVF, RSolve.getValueFunction(Solver.VALUE_FUNCTION));
		}
		float[][] VF = FastIO.read2DFloatArray(rVF, Parameter.NO_FIVE_MIN_PER_HR * 24 + 1, RSolve.getNumOfStates());
		int t = startHr;
		float[][] terminalVF = IOHelpers.convert1Dto2D(VF[(t + 1) * Parameter.NO_FIVE_MIN_PER_HR],
				param.getRrange().length, param.getPErange().length);

		String RGprob = dirName + File.separator + "Input" + File.separator + "RGprob.csv";
		String RGstate = dirName + File.separator + "Input" + File.separator + "RGstates.csv";
		String RGindex = dirName + File.separator + "Input" + File.separator + "RGindex.csv";
		RGtransHelper helper = new RGtransHelper(RGprob, RGstate, RGindex);
		param.setRGtransMat(helper.getRGtransProb());
		param.setRGtransOffset(helper.getRGtransState());

		EBSolver EBSolve = new EBSolver(param);
		EBSolve.populateStates(terminalVF);
		for (int i = 0; i < param.getPDrange().length; i++) {
			param.setPD(param.getPDrange()[i]);
			EBSolve.initializeStates();
			EBSolve.solveBDP();
			float[][] EBVF = EBSolve.getValueFunction(Solver.VALUE_FUNCTION);
			float[][] EBOA = EBSolve.getValueFunction(Solver.OPTI_ACTION);
			String EBVFDir = dirName + File.separator + "Output" + File.separator + "PD" + i + File.separator;
			f = new File(EBVFDir);
			if (!f.isDirectory()) {
				f.mkdirs();
			}
			FastIO.write2DFloatArray(EBVFDir + "EBVF_" + t + "hr.dat", EBVF);
			FastIO.write2DFloatArray(EBVFDir + "EBOA_" + t + "hr.dat", EBOA);
		}
	}

}
