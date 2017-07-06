package utilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * The Parameter class handles the input parameters. The base parameter requires two inputs a static
 * parameter file for characterizing the battery and a state space parameter file for describing the
 * state space. Transition function/probability is also handled by the parameter class. Most input
 * files are assumed to be in the .csv format
 * 
 * @author bcheng
 * @date 10/31/16
 *
 */
public class Parameter {
	// private final int NoTwoSecPerMin = 150;
	public static float EPSILON = (float) 0.00001;
	public static final int NO_TWO_SEC_PER_FIVE_MIN = 30; // Ten Second
	public static final int NO_FIVE_MIN_PER_HR = 12;
	public static final int BETA_C = 0; // charging capacity
	public static final int BETA_D = 1; // discharging capacity
	public static final int L_U = 2; // upper limit
	public static final int L_L = 3; // lower limit
	public static final int ETA_C = 4; // charging efficiency
	public static final int ETA_D = 5; // discharging efficiency

	private final int R_index = 0;
	private final int G_index = 1;
	private final int D_index = 2;
	private final int PE_index = 3;
	private final int PD_index = 4;

	public static final int ROW_PART = 6; // how many sub-matrices from top to bottom
	// 63 if we use |D| = 21;
	public static final int COL_PART = 63; // how many sub-matrices from left to right

	private float[] batteryParam; // stores the previous parameters
	private float K; // regulation capacity in MW during [0,T]
	private int deltaT;

	private float PE;
	private float PD;
	private float x_G;
	private float x_E;

	// pdstateSize is a n-dimensional vector of the state space discretization
	// along each dimension. pdstateMin/pdstateMax are the lower and upper
	// bounds of the state space in each dimension. linspace(pdstateMin(i),
	// pdstateMax(i), pdstateSize(i)) is the state space along the i-th
	// dimension. Each state is a vector in R^n
	// Same for predecision states (default: post space = pre space)
	private int[] stateSize = null;
	private float[] stateMin = null;
	private float[] stateMax = null;

	private float[] Rrange;
	private float[] Grange;
	private float[] Drange;
	private float[] PErange;
	private float[] PDrange;

	private float[] XGrange;
	private float[][] RTPrice;
	private float[][] fmProb;
	private float[][] priceProb;

	private List<int[]> Dnext;
	private List<float[]> DnextProb;

	private Hashtable<String, int[][]> RGtransOffset;
	private Hashtable<String, float[]> RGtransMat;

	public Parameter() {
		batteryParam = new float[ETA_D + 1];
		stateSize = new int[PD_index + 1];
		stateMin = new float[PD_index + 1];
		stateMax = new float[PD_index + 1];
	}

	public void readStaticParameters(String filename) {
		BufferedReader br = null;
		String line = "";
		String splitBy = "=";
		try {
			br = new BufferedReader(new FileReader(filename));
			while ((line = br.readLine()) != null) {
				String[] input = line.split(splitBy);
				if (input[0].equalsIgnoreCase("deltat")) {
					deltaT = Integer.parseInt(input[1]);
				} else if (input[0].equalsIgnoreCase("K")) {
					K = Float.parseFloat(input[1]);
				} else if (input[0].equalsIgnoreCase("beta_c")) {
					batteryParam[BETA_C] = Float.parseFloat(input[1]);
				} else if (input[0].equalsIgnoreCase("beta_d")) {
					batteryParam[BETA_D] = Float.parseFloat(input[1]);
				} else if (input[0].equalsIgnoreCase("eta_c")) {
					batteryParam[ETA_C] = Float.parseFloat(input[1]);
				} else if (input[0].equalsIgnoreCase("eta_d")) {
					batteryParam[ETA_D] = Float.parseFloat(input[1]);
				} else if (input[0].equalsIgnoreCase("L_u")) {
					batteryParam[L_U] = Float.parseFloat(input[1]);
				} else if (input[0].equalsIgnoreCase("L_l")) {
					batteryParam[L_L] = Float.parseFloat(input[1]);
				} else if (input[0].equalsIgnoreCase("pe")) {
					PE = Float.parseFloat(input[1]);
				} else if (input[0].equalsIgnoreCase("pd")) {
					PD = Float.parseFloat(input[1]);
				} else if (input[0].equalsIgnoreCase("x_g")) {
					x_G = Float.parseFloat(input[1]);
				} else if (input[0].equalsIgnoreCase("x_e")) {
					x_E = Float.parseFloat(input[1]);
				}
			}
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * This function reads in the state space file and edits the state space
	 * 
	 * @param filename
	 */
	public void readStateSpace(String filename) {
		BufferedReader br = null;
		String line = "";
		String splitBy = "=";
		try {
			br = new BufferedReader(new FileReader(filename));
			while ((line = br.readLine()) != null) {
				String[] input = line.split(splitBy);
				if (input[0].equalsIgnoreCase("rmax")) {
					stateMax[R_index] = Float.parseFloat(input[1]);
				} else if (input[0].equalsIgnoreCase("rmin")) {
					stateMin[R_index] = Float.parseFloat(input[1]);
				} else if (input[0].equalsIgnoreCase("gmax")) {
					// NOTE: G is reverse indexed
					stateMin[G_index] = Float.parseFloat(input[1]);
				} else if (input[0].equalsIgnoreCase("gmin")) {
					stateMax[G_index] = Float.parseFloat(input[1]);
				} else if (input[0].equalsIgnoreCase("pemax")) {
					stateMax[PE_index] = Float.parseFloat(input[1]);
				} else if (input[0].equalsIgnoreCase("pemin")) {
					stateMin[PE_index] = Float.parseFloat(input[1]);
				} else if (input[0].equalsIgnoreCase("pdmax")) {
					stateMax[PD_index] = Float.parseFloat(input[1]);
				} else if (input[0].equalsIgnoreCase("pdmin")) {
					stateMin[PD_index] = Float.parseFloat(input[1]);
				} else if (input[0].equalsIgnoreCase("dmax")) {
					stateMax[D_index] = Float.parseFloat(input[1]);
				} else if (input[0].equalsIgnoreCase("dmin")) {
					stateMin[D_index] = Float.parseFloat(input[1]);
				} else if (input[0].equalsIgnoreCase("rsize")) {
					stateSize[R_index] = Integer.parseInt(input[1]);
				} else if (input[0].equalsIgnoreCase("gsize")) {
					stateSize[G_index] = Integer.parseInt(input[1]);
				} else if (input[0].equalsIgnoreCase("pdsize")) {
					stateSize[PD_index] = Integer.parseInt(input[1]);
				} else if (input[0].equalsIgnoreCase("pesize")) {
					stateSize[PE_index] = Integer.parseInt(input[1]);
				} else if (input[0].equalsIgnoreCase("dsize")) {
					stateSize[D_index] = Integer.parseInt(input[1]);
				}
				// else if (input[0].equalsIgnoreCase("xgmax")) {
				// BatteryParam[L_L] = Float.parseFloat(input[1]);
				// } else if (input[0].equalsIgnoreCase("xgmin")) {
				// BatteryParam[L_L] = Float.parseFloat(input[1]);
				// } else if (input[0].equalsIgnoreCase("xgsize")) {
				// BatteryParam[L_L] = Float.parseFloat(input[1]);
				// }
			}
		} catch (

		FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		Rrange = new float[stateSize[R_index] + 1];
		Grange = new float[stateSize[G_index] + 1];
		Drange = new float[stateSize[D_index] + 1];
		PErange = new float[stateSize[PE_index] + 1];
		PDrange = new float[stateSize[PD_index] + 1];

		float diff = (stateMax[R_index] - stateMin[R_index]) / stateSize[R_index];
		for (int i = 0; i < Rrange.length; i++)
			Rrange[i] = diff * i + stateMin[R_index];
		diff = (stateMax[G_index] - stateMin[G_index]) / stateSize[G_index];
		for (int i = 0; i < Grange.length; i++)
			Grange[i] = diff * i + stateMin[G_index];
		diff = (stateMax[D_index] - stateMin[D_index]) / stateSize[D_index];
		for (int i = 0; i < Drange.length; i++)
			Drange[i] = diff * i + stateMin[D_index];
		diff = (stateMax[PE_index] - stateMin[PE_index]) / stateSize[PE_index];
		for (int i = 0; i < PErange.length; i++)
			PErange[i] = diff * i + stateMin[PE_index];
		diff = (stateMax[PD_index] - stateMin[PD_index]) / stateSize[PD_index];
		for (int i = 0; i < PDrange.length; i++)
			PDrange[i] = diff * i + stateMin[PD_index];

		// TODO needs clean up.
		XGrange = new float[3];
		for (int i = 0; i < XGrange.length; i++) {
			XGrange[i] = (stateMin[G_index] - stateMax[G_index]) / (XGrange.length - 1) * i + stateMax[G_index];
		}
	}

	public int getDeltat() {
		return deltaT;
	}

	public float getK() {
		return K;
	}

	public float[] getBatteryParam() {
		return batteryParam;
	}

	public float getPE() {
		return PE;
	}

	public void setPE(float PE_) {
		PE = PE_;
	}

	public float getPD() {
		return PD;
	}

	public float getXG() {
		return x_G;
	}

	public void setXG(float xg) {
		x_G = xg;
	}

	public float getXE() {
		return x_E;
	}

	public void setXE(float xe) {
		x_E = xe;
	}

	public float[] getRrange() {
		return Rrange;
	}

	public float[] getGrange() {
		return Grange;
	}

	public float[] getDrange() {
		return Drange;
	}

	public float[] getPErange() {
		return PErange;
	}

	public float[] getXGrange() {
		return XGrange;
	}

	public float getRTPrice(int index, int time) {
		return RTPrice[index][time];
	}

	public void setPD(float pd) {
		PD = pd;
	}

	public int getRowPart() {
		return ROW_PART;
	}

	public int getColPart() {
		return COL_PART;
	}

	// row size of sub-matrices
	public int getSizeRow() {
		return Rrange.length / ROW_PART;
	}

	// column size of sub-matrices
	public int getSizeCol() {
		return (Grange.length * Drange.length) / COL_PART;
	}

	// number of sub-matrices
	public int getNumSubs() {
		return ROW_PART * COL_PART;
	}

	public float[] getPDrange() {
		return PDrange;
	}

	public float[][] getFmProb() {
		return fmProb;
	}

	public void setFmProb(float[][] fmProb) {
		this.fmProb = fmProb;
	}

	public float[][] getPriceProb() {
		return priceProb;
	}

	public void setPriceProb(float[][] priceProb) {
		this.priceProb = priceProb;
	}

	public void setPErange(float[] pErange) {
		PErange = pErange;
	}

	public void setPDrange(float[] pDrange) {
		PDrange = pDrange;
	}

	public void setDnext(float[][] transProb) {
		Dnext = new ArrayList<int[]>();
		DnextProb = new ArrayList<float[]>();
		for (int i = 0; i < transProb.length; i++) {
			int count = 0;
			for (int j = 0; j < transProb[i].length; j++) {
				if (transProb[i][j] != 0) {
					count++;
				}
			}
			int[] temp = new int[count];
			float[] tempProb = new float[count];
			count = 0;
			for (int j = 0; j < transProb[i].length; j++) {
				if (transProb[i][j] != 0) {
					temp[count] = j;
					tempProb[count] = transProb[i][j];
					count++;
				}
			}
			Dnext.add(temp);
			DnextProb.add(tempProb);
		}
	}

	public int[] getDnext(int d) {
		return Dnext.get(d);
	}

	public float[] getDnextProb(int d) {
		return DnextProb.get(d);
	}

	public Hashtable<String, int[][]> getRGtransOffset() {
		return RGtransOffset;
	}

	public Hashtable<String, float[]> getRGtransmat() {
		return RGtransMat;
	}

	public void setRGtransOffset(Hashtable<String, int[][]> RGnext) {
		RGtransOffset = RGnext;
	}

	public void setRGtransMat(Hashtable<String, float[]> RGnextprob) {
		RGtransMat = RGnextprob;
	}
}