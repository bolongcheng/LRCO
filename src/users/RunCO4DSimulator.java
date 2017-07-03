package users;

import java.io.File;

import simulators.CO4DSimulator;
import solvers.EB4DSolver;
import solvers.FRSolver_sparseLR;
import solvers.Solver;
import utilities.CSVIO;
import utilities.DiscreteHelpers;
import utilities.FastIO;
import utilities.IOHelpers;
import utilities.Parameter;

public class RunCO4DSimulator {
	public static void main(String[] args) {
		String dirName = "/Users/mac/Dropbox/JavaWorkspace/LRCO";
		if (args.length != 0)
			dirName = args[0];
		int month;
		if (args.length > 1)
			month = Integer.parseInt(args[1]);
		else
			month = 0;
		int quarter = month / 4;
		String[] quarters = { "123", "456", "789", "101112" };
		int[] daysInMonth = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
		String inputFolder = dirName + File.separator + "Input" + File.separator;
		String outputFolder = dirName + File.separator + "Output" + File.separator + "EB4D" + File.separator;
		String lmpFile = inputFolder + "fmlmp_" + month + "_2015.csv";
		String lmpClustFile = inputFolder + "fmlmp_cl_" + month + "_2015.csv";
		String rmpFile = inputFolder + "rmp_" + month + "_2015.csv";
		String rmpClustFile = inputFolder + "rmp_cl_" + month + "_2015.csv";
		String dtFile = inputFolder + "HrDt31.csv";
		String vfFile = outputFolder + "EBVF_Q" + quarter + ".dat";
		String oaFile = outputFolder + "EBOA_Q" + quarter + ".dat";
		float[][] lmp = CSVIO.read2DFloatArray(lmpFile, daysInMonth[month], Parameter.NO_FIVE_MIN_PER_HR * 24);
		int[][] lmpClusters = CSVIO.read2DIntArray(lmpClustFile, daysInMonth[month], Parameter.NO_FIVE_MIN_PER_HR * 24);
		float[][] rmp = CSVIO.read2DFloatArray(rmpFile, daysInMonth[month], 24);
		int[][] rmpClusters = CSVIO.read2DIntArray(rmpClustFile, daysInMonth[month], 24);
		int[][] dt = CSVIO.read2DIntArray(dtFile, daysInMonth[month],
				Parameter.NO_TWO_SEC_PER_FIVE_MIN * Parameter.NO_FIVE_MIN_PER_HR * 24);
		Parameter EBParam = new Parameter();
		EBParam.readStaticParameters(inputFolder + "static_param.csv");
		EBParam.readStateSpace(inputFolder + "eb_test0.csv");
		EBParam.setPErange(CSVIO.read1DFloatArray(inputFolder + "lmp_lvls" + quarters[quarter] + ".csv"));
		EBParam.setPDrange(CSVIO.read1DFloatArray(inputFolder + "rmp_lvls" + quarters[quarter] + ".csv"));

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
		
		EB4DSolver EBSolve = new EB4DSolver(EBParam);
		EBSolve.populateStates();
		EBSolve.initializeStates();
		float[][] VF = FastIO.read2DFloatArray(vfFile, Parameter.NO_FIVE_MIN_PER_HR * 24 + 1, EBSolve.getNumOfStates());
		float[][] OA = FastIO.read2DFloatArray(oaFile, Parameter.NO_FIVE_MIN_PER_HR * 24 + 1, EBSolve.getNumOfStates());
		EBSolve.setValueFunction(VF, Solver.VALUE_FUNCTION);
		EBSolve.setValueFunction(OA, Solver.OPTI_ACTION);
		FRSolver_sparseLR FRSolve = new FRSolver_sparseLR(FRParam);
		FRSolve.populateStates();
		String[] fileNames = { outputFolder };
		CO4DSimulator sim = new CO4DSimulator(EBParam, FRParam, daysInMonth[month]);
		sim.LoadSolver(EBSolve, FRSolve);
		sim.setLMP(lmp);
		sim.setLMPClusters(lmpClusters);
		sim.setRMP(rmp);
		sim.setRMPClusters(rmpClusters);
		sim.setRegDPath(dt);
		sim.setVFFileNames(fileNames);
		sim.RunSimulation();
		CSVIO.write2DFloatArray(dirName + File.separator + "Sim4D" + File.separator + "COSim_" + month + ".csv",
				IOHelpers.convert3Dto2D(sim.getPath()));

	}

}
