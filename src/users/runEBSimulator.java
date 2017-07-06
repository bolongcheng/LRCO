package users;

import java.io.File;

import simulators.EBSimulator;
import simulators.RSimulator;
import solvers.EBSolver;
import solvers.RSolver;
import solvers.Solver;
import utilities.CSVIO;
import utilities.FastIO;
import utilities.IOHelpers;
import utilities.Parameter;

public class runEBSimulator {
    public static void main(String[] args) {
        // String dirName = "";
        String dirName = "/Users/mac/Dropbox/JavaWorkspace/LRCO";
        if (args.length != 0)
            dirName = args[0];
        int numTest = 0;
        if (args.length > 1)
            numTest = Integer.parseInt(args[1]);

        Parameter param = new Parameter();
        String inputFolder = dirName + File.separator + "Input" + File.separator;
        String outputFolder = dirName + File.separator + "Output" + File.separator;
        String RGprob = inputFolder + "RGprob.csv";
        String RGstate = inputFolder + "RGstates.csv";
        String RGindex = inputFolder + "RGindex.csv";
        RGtransHelper helper = new RGtransHelper(RGprob, RGstate, RGindex);
        param.readStaticParameters(inputFolder + "static_param.csv");
        param.readStateSpace(inputFolder + "eb_test0.csv");
        param.setRGtransMat(helper.getRGtransProb());
        param.setRGtransOffset(helper.getRGtransState());
        param.setPErange(CSVIO.read1DFloatArray(inputFolder + "lmp_lvls.csv"));
        param.setPDrange(CSVIO.read1DFloatArray(inputFolder + "rmp_lvls.csv"));
        param.setPD(param.getPDrange()[4]);

        float[][] fmProb = CSVIO.read2DFloatArray(inputFolder + "fm_prob_alt_123_2015.csv", param.getPErange().length,
                Parameter.NO_FIVE_MIN_PER_HR);
        param.setFmProb(fmProb);
        System.out.println(param.getPD());
        EBSolver solve = new EBSolver(param);
        solve.populateStates();
        solve.initializeStates();
        String rVF = outputFolder + "PD4" + File.separator + "EBVF_0hr.dat";
        float[][] VF = FastIO.read2DFloatArray(rVF, Parameter.NO_FIVE_MIN_PER_HR + 1, solve.getNumOfStates());
        // CSVIO.write2DFloatArray(outputFolder + "EBVF_0hr.dat", VF);
        solve.setValueFunction(VF, Solver.VALUE_FUNCTION);
        String lmpFile = inputFolder + "fmlmp_123_2015.csv";
        String lmpClustFile = inputFolder + "fmlmp_cl_123_2015.csv";

        int num_trial = 10;
        float[][] lmp = CSVIO.read2DFloatArray(lmpFile, num_trial, Parameter.NO_FIVE_MIN_PER_HR * 24);
        int[][] lmpClusters = CSVIO.read2DIntArray(lmpClustFile, num_trial, Parameter.NO_FIVE_MIN_PER_HR * 24);
        EBSimulator sim = new EBSimulator(param, num_trial);
        sim.loadSolver(solve);
        sim.setLMP(lmp);
        sim.setLMPClusters(lmpClusters);
        sim.runSimulation();
        CSVIO.write2DFloatArray(outputFolder + "RSim.csv", IOHelpers.convert3Dto2D(sim.getPath()));

    }

}
