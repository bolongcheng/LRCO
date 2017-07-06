package simulators;

import java.io.File;

import solvers.FRSolver_sparseLR;
import solvers.EBSolver;
import solvers.FRSolver;
import solvers.Solver;
import states.EBState;
import states.FRState;
import utilities.DiscreteHelpers;
import utilities.FastIO;
import utilities.Parameter;

public class CODaySimulator extends EBSimulator {
    Parameter FRParam;
    Solver FRSolve;
    String[] VFFileNames;
    int[][] RegDPaths;

    public static final int D_PATH = 5;
    public static final int XD_PATH = 6;

    public static final int OutputFolder = 0;

    public CODaySimulator(Parameter eb, Parameter fr, int numTrial) {
        super(eb, numTrial);
        FRParam = fr;
    }

    public void LoadSolver(Solver eb, Solver fr) {
        solver = eb;
        FRSolve = fr;
    }

    public void setVFFileNames(String[] fileNames) {
        VFFileNames = fileNames;
    }

    public void setRegDPath(int[][] dt) {
        if (dt.length < numTrial) {
            System.out.println("TRIAL NUMBER MISMATCH");
        } else {
            RegDPaths = dt;
        }
    }

    /**
     * Function for running the co-optimization simulation
     */
    public void RunSimulation() {
        int numHrs = 24;
        path = new float[numTrial][XD_PATH
                + 1][Parameter.NO_FIVE_MIN_PER_HR * Parameter.NO_TWO_SEC_PER_FIVE_MIN * numHrs + 1];
        int EBR = param.getRrange().length / 2;
        int EBG = 0;
        int GPELength = param.getGrange().length * param.getPErange().length;
        int GDLength = FRParam.getGrange().length * FRParam.getDrange().length;
        int PDIndex = DiscreteHelpers.getIndex(param.getPD(), param.getPDrange());
        int R = FRParam.getRrange().length / 2;
        int G = 0;

        int rowPart = Parameter.ROW_PART;
        int colPart = Parameter.COL_PART;
        int sizeRow = FRParam.getSizeRow();
        int sizeCol = FRParam.getSizeCol();
        // looping through 24 * 12 five-min interval
        // j indexing five-minute for 24 hours
        for (int j = 0; j < Parameter.NO_FIVE_MIN_PER_HR * 24; j++) {
            int hour = j / Parameter.NO_FIVE_MIN_PER_HR;
            int period = j % Parameter.NO_FIVE_MIN_PER_HR;
            int numTwoSecSoFar = j * Parameter.NO_TWO_SEC_PER_FIVE_MIN;
            // start of every hour, load EBVF and EBOA
            System.out.println("PERIOD: " + j);
            if (period == 0) {
                G = 0;
                float[][] EBVF = FastIO.read2DFloatArray(
                        VFFileNames[OutputFolder] + "PD" + PDIndex + File.separator + "EBVF_" + hour + "hr.dat",
                        Parameter.NO_FIVE_MIN_PER_HR + 1, solver.getNumOfStates());
                float[][] EBOA = FastIO.read2DFloatArray(
                        VFFileNames[OutputFolder] + "PD" + PDIndex + File.separator + "EBOA_" + hour + "hr.dat",
                        Parameter.NO_FIVE_MIN_PER_HR + 1, solver.getNumOfStates());
                solver.setValueFunction(EBVF, Solver.VALUE_FUNCTION);
                solver.setValueFunction(EBOA, Solver.OPTI_ACTION);
            }

            float xE = Float.MAX_VALUE;
            float xG = -1;
            int oldLMP = -1;
            // loop through num of LMP paths
            for (int i = 0; i < numTrial; i++) {
                if (j == 0) {
                    R = FRParam.getRrange().length / 2;
                } else {
                    if (period == 0)
                        G = 0;
                    else
                        G = (int) path[i][G_PATH][numTwoSecSoFar - 1];
                    R = (int) path[i][R_PATH][numTwoSecSoFar - 1];
                }

                EBR = DiscreteHelpers.getIndex(FRParam.getRrange()[R], param.getRrange());
                EBG = DiscreteHelpers.getRevIndex(FRParam.getGrange()[G], param.getGrange());

                EBState tempEBState = (EBState) solver
                        .getState(EBR * GPELength + EBG * param.getPErange().length + lmpClusters[i][j]);
                float newXE = param.getDrange()[EBState.getActionSpace()[tempEBState.getFeasibleActions()
                        .get(tempEBState.getOptAction(period))][EBState.XE_INDEX]];
                float newXG = param.getXGrange()[EBState.getActionSpace()[tempEBState.getFeasibleActions()
                        .get(tempEBState.getOptAction(period))][EBState.XG_INDEX]];
                FRParam.setPE(lmp[i][j]);

                if (newXE != xE || newXG != xG || oldLMP != lmpClusters[i][j]) {
                    FRParam.setXE(newXE);
                    FRParam.setXG(newXG);

                    // TODO this is a problem that needs to be fixed
                    float[][] VFTerminal = ((EBSolver) solver).getPartialValueFunction(lmpClusters[i][j], period + 1,
                            param.getGrange().length / 2 + 1);
                    FRSolve.populateStates(VFTerminal);

                    String VFDir = VFFileNames[OutputFolder] + hour + File.separator + "PD" + param.getPD()
                            + File.separator + "LR";
                    String prefix = VFDir + File.separator + "FRVF_Per" + period + "PE" + lmpClusters[i][j] + "_"
                            + FRParam.getXE();

                    String xfile = prefix + "_x.dat";
                    String yfile = prefix + "_y.dat";
                    String shiftfile = prefix + "_shift.dat";

                    File tempFile = new File(xfile);
                    if (tempFile.exists()) {
                        float[][] xIn = FastIO.read2DFloatArray(xfile, Parameter.NO_TWO_SEC_PER_FIVE_MIN + 1,
                                sizeRow * rowPart * colPart);
                        float[][] yIn = FastIO.read2DFloatArray(yfile, Parameter.NO_TWO_SEC_PER_FIVE_MIN + 1,
                                sizeCol * rowPart * colPart);
                        float[][] shiftIn = FastIO.read2DFloatArray(shiftfile, Parameter.NO_TWO_SEC_PER_FIVE_MIN + 1,
                                rowPart * colPart);
                        ((FRSolver_sparseLR) FRSolve).setVFApprox(xIn, yIn, shiftIn);
                    } else if (newXG > 0) {
                        File tempDir = new File(VFDir);
                        if (!tempDir.exists() || !tempDir.isDirectory()) {
                            tempDir.mkdirs();
                            System.out.println(tempDir + ": created");
                        }
                        callLRBDP((FRSolver_sparseLR) FRSolve, prefix);
                    } else {
                        xfile = VFFileNames[OutputFolder] + "defaultOA_x.dat";
                        yfile = VFFileNames[OutputFolder] + "defaultOA_y.dat";
                        shiftfile = VFFileNames[OutputFolder] + "defaultOA_shift.dat";
                        tempFile = new File(xfile);
                        if (tempFile.exists()) {
                            float[][] xIn = FastIO.read2DFloatArray(xfile, Parameter.NO_TWO_SEC_PER_FIVE_MIN + 1,
                                    sizeRow * rowPart * colPart);
                            float[][] yIn = FastIO.read2DFloatArray(yfile, Parameter.NO_TWO_SEC_PER_FIVE_MIN + 1,
                                    sizeCol * rowPart * colPart);
                            float[][] shiftIn = FastIO.read2DFloatArray(shiftfile,
                                    Parameter.NO_TWO_SEC_PER_FIVE_MIN + 1, rowPart * colPart);
                            ((FRSolver_sparseLR) FRSolve).setVFApprox(xIn, yIn, shiftIn);
                        } else {
                            callLRBDP((FRSolver_sparseLR) FRSolve, VFFileNames[OutputFolder] + "defaultOA");
                        }
                    }
                    xE = newXE;
                    xG = newXG;
                    oldLMP = lmpClusters[i][j];
                }

                // loop through 30 (75) intervals within five minutes
                // k indexing the dt interval within five minutes
                // j * 12 + k: indexing time step
                FRParam.setPE(lmp[i][j]);
                for (int k = 0; k < Parameter.NO_TWO_SEC_PER_FIVE_MIN; k++) {
                    int t = numTwoSecSoFar + k;
                    path[i][R_PATH][t] = R;
                    path[i][G_PATH][t] = G;
                    path[i][D_PATH][t] = FRParam.getDrange()[RegDPaths[i][t]];

                    FRState tempFRState = (FRState) FRSolve
                            .getState(R * GDLength + G + RegDPaths[i][t] * FRParam.getGrange().length);
                    tempFRState.initialize(FRParam);

                    if (tempFRState.getOptAction(k) == -1) {
                        FRSolve.findMax(tempFRState, k);
                    }
                    path[i][COST_PATH][t] = tempFRState.getCostFunction(tempFRState.getOptAction(k));
                    if (period == Parameter.NO_FIVE_MIN_PER_HR - 1 && k == Parameter.NO_TWO_SEC_PER_FIVE_MIN - 1) {
                        path[i][COST_PATH][t] += FRParam.getK() * FRParam.getGrange()[G] * FRParam.getPD();
                    }
                    path[i][XD_PATH][t] = FRParam.getDrange()[tempFRState.getOptActionString(k)];
                    path[i][LMP_PATH][t] = FRParam.getPE();
                    path[i][XE_PATH][t] = FRParam.getXE();
                    G = tempFRState.getGnext(tempFRState.getOptAction(k));
                    R = tempFRState.getRnext(tempFRState.getOptAction(k));
                }
            }
        }

    }

    public void callLRBDP(FRSolver_sparseLR solve, String filePrefix) {
        solve.initializeStates();
        solve.solveBDP();
        solve.closeMatlab();
        solve.writeVFSVD(filePrefix);
    }

}
