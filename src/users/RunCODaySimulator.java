package users;

import java.io.File;

import simulators.CODaySimulator;
import solvers.EBSolver;
import solvers.FRSolver_sparseLR;
import utilities.CSVIO;
import utilities.DiscreteHelpers;
import utilities.IOHelpers;
import utilities.Parameter;

public class RunCODaySimulator {
	public static void main(String[] args) {
		String dirName = "/Users/mac/Dropbox/JavaWorkspace/LRCO";
		if (args.length != 0)
			dirName = args[0];
		int PDIndex;
		if (args.length > 1)
			PDIndex = Integer.parseInt(args[1]);
		else
			PDIndex = 0;

		int numTrial = 10;
		String inputFolder = dirName + File.separator + "Input" + File.separator;
		String lmpFile = inputFolder + "fmlmp_123_2015.csv";
		String lmpClustFile = inputFolder + "fmlmp_cl_123_2015.csv";
		String dtFile = inputFolder + "HrDt_gen10_new2.csv";
		float[][] lmp = CSVIO.read2DFloatArray(lmpFile, numTrial, Parameter.NO_FIVE_MIN_PER_HR * 24);
		int[][] lmpClusters = CSVIO.read2DIntArray(lmpClustFile, numTrial, Parameter.NO_FIVE_MIN_PER_HR * 24);
		int[][] dt = CSVIO.read2DIntArray(dtFile, numTrial,
				Parameter.NO_TWO_SEC_PER_FIVE_MIN * Parameter.NO_FIVE_MIN_PER_HR * 24);
		Parameter EBParam = new Parameter();
		EBParam.readStaticParameters(inputFolder + "static_param.csv");
		EBParam.readStateSpace(inputFolder + "eb_test0.csv");
		EBParam.setPErange(CSVIO.read1DFloatArray(inputFolder + "lmp_lvls.csv"));
		EBParam.setPDrange(CSVIO.read1DFloatArray(inputFolder + "rmp_lvls.csv"));
		EBParam.setPD(EBParam.getPDrange()[PDIndex]);
		
		String RGprob = inputFolder + "RGprob.csv";
		String RGstate = inputFolder + "RGstates.csv";
		String RGindex = inputFolder + "RGindex.csv";
		RGtransHelper helper = new RGtransHelper(RGprob, RGstate, RGindex);
		EBParam.setRGtransMat(helper.getRGtransProb());
		EBParam.setRGtransOffset(helper.getRGtransState());

		Parameter FRParam = new Parameter();
		FRParam.readStaticParameters(inputFolder + "static_param.csv");
		FRParam.readStateSpace(inputFolder + "fr_test0.csv");
		float[][] dtTransProb = CSVIO.read2DFloatArray(inputFolder + "dt_trans_prob.csv", FRParam.getDrange().length,
				FRParam.getDrange().length);
		FRParam.setDnext(dtTransProb);
		FRParam.setPD(EBParam.getPDrange()[PDIndex]);
		EBSolver EBSolve = new EBSolver(EBParam);
		EBSolve.populateStates();
		EBSolve.initializeStates();
		FRSolver_sparseLR FRSolve = new FRSolver_sparseLR(FRParam);
		FRSolve.populateStates();
		String[] fileNames = { dirName + File.separator + "Output" + File.separator };
		CODaySimulator sim = new CODaySimulator(EBParam, FRParam, numTrial);
		sim.LoadSolver(EBSolve, FRSolve);
		sim.setLMP(lmp);
		sim.setLMPClusters(lmpClusters);
		sim.setRegDPath(dt);
		sim.setVFFileNames(fileNames);
		sim.RunSimulation();
		CSVIO.write2DFloatArray(dirName + File.separator + "Sim" + File.separator + "COSim_" + PDIndex + ".csv",
				IOHelpers.convert3Dto2D(sim.getPath()));

	}

}
