package utilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * The Parameter class handles the input parameters. The input csv file is formatted in the
 * following order:
 * 
 * Delta t, K, R_max, beta^c, beta^d, L^u, L^l, eta^c, eta^d R_size, G_size, D_size, P^E_size R_max,
 * G_min, D_max, P^E_max R_min, G_max, D_min, P^E_min P^E, P^D, x^G, X^E
 * 
 * @author bcheng
 * @date 10/26
 *
 */
public class Parameter {
	// public static final int NoTwoSecPerMin = 150;
	public static final int NoTwoSecPerFiveMin = 30; // Ten Second
	public static final int NoFiveMinPerHr = 12;
	public static final int betac = 0; // charging capacity
	public static final int betad = 1; // discharging capacity
	public static final int Lu = 2; // upper limit
	public static final int Ll = 3; // lower limit
	public static final int etac = 4; // charging efficiency
	public static final int etad = 5; // discharging efficiency

	public static final int R_index = 0;
	public static final int G_index = 1;
	public static final int D_index = 2;
	public static final int PE_index = 3;
	public static final int PD_index = 4;

	public static final int row_part = 6; // how many sub-matrices from top to bottom
	// 63 if we use |D| = 21;
	public static final int col_part = 63; // how many sub-matrices from left to right

	private float[] BatteryParam; // stores the previous parameters
	private float K; // regulation capacity in MW during [0,T]
	private int delta_t;

	private float PE;
	private float PD;
	private float x_G;
	private float x_E;

	// pdstate_size is a n-dimensional vector of the state space discretization
	// along each dimension. pdstate_min/pdstate_max are the lower and upper
	// bounds of the state space in each dimension. linspace(pdstate_min(i),
	// pdstate_max(i), pdstate_size(i)) is the state space along the i-th
	// dimension. Each state is a vector in R^n
	// Same for predecision states (default: post space = pre space)
	private int[] state_size = null;
	private float[] state_min = null;
	private float[] state_max = null;

	private float[] Rrange;
	private float[] Grange;
	private float[] Drange;
	private float[] PErange;
	private float[] PDrange;
	
	private float[] XGrange;
	private float[][] RTPrice;
	private float[][] fm_prob;
	private float[][] price_prob;

	private float[][] PDRTnextprob;

	private List<int[]> Dnext;
	private List<float[]> DnextProb;

	private Hashtable<String, int[][]> RGtransOffset;
	private Hashtable<String, float[]> RGtransMat;

	public Parameter() {
		BatteryParam = new float[etad + 1];
		state_size = new int[PD_index+1];
		state_min = new float[PD_index+1];
		state_max = new float[PD_index+1];
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
					delta_t = Integer.parseInt(input[1]);
				} else if (input[0].equalsIgnoreCase("K")) {
					K = Float.parseFloat(input[1]);
				} else if (input[0].equalsIgnoreCase("beta_c")) {
					BatteryParam[betac] = Float.parseFloat(input[1]);
				} else if (input[0].equalsIgnoreCase("beta_d")) {
					BatteryParam[betad] = Float.parseFloat(input[1]);
				} else if (input[0].equalsIgnoreCase("eta_c")) {
					BatteryParam[etac] = Float.parseFloat(input[1]);
				} else if (input[0].equalsIgnoreCase("eta_d")) {
					BatteryParam[etac] = Float.parseFloat(input[1]);
				} else if (input[0].equalsIgnoreCase("L_u")) {
					BatteryParam[Lu] = Float.parseFloat(input[1]);
				} else if (input[0].equalsIgnoreCase("L_l")) {
					BatteryParam[Ll] = Float.parseFloat(input[1]);
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
					state_max[R_index] = Float.parseFloat(input[1]);
				} else if (input[0].equalsIgnoreCase("rmin")) {
					state_min[R_index] = Float.parseFloat(input[1]);
				} else if (input[0].equalsIgnoreCase("gmax")) {
					//NOTE: G is reverse indexed
					state_min[G_index] = Float.parseFloat(input[1]);
				} else if (input[0].equalsIgnoreCase("gmin")) {
					state_max[G_index] = Float.parseFloat(input[1]);
				} else if (input[0].equalsIgnoreCase("pemax")) {
					state_max[PE_index] = Float.parseFloat(input[1]);
				} else if (input[0].equalsIgnoreCase("pemin")) {
					state_min[PE_index] = Float.parseFloat(input[1]);
				} else if (input[0].equalsIgnoreCase("pdmax")) {
					state_max[PD_index] = Float.parseFloat(input[1]);
				} else if (input[0].equalsIgnoreCase("pdmin")) {
					state_min[PD_index] = Float.parseFloat(input[1]);
				} else if (input[0].equalsIgnoreCase("dmax")) {
					state_max[D_index] = Float.parseFloat(input[1]);
				} else if (input[0].equalsIgnoreCase("dmin")) {
					state_min[D_index] = Float.parseFloat(input[1]);
				} else if (input[0].equalsIgnoreCase("rsize")) {
					state_size[R_index] = Integer.parseInt(input[1]);
				} else if (input[0].equalsIgnoreCase("gsize")) {
					state_size[G_index] = Integer.parseInt(input[1]);
				} else if (input[0].equalsIgnoreCase("pdsize")) {
					state_size[PD_index] = Integer.parseInt(input[1]);
				} else if (input[0].equalsIgnoreCase("pesize")) {
					state_size[PE_index] = Integer.parseInt(input[1]);
				} else if (input[0].equalsIgnoreCase("dsize")) {
					state_size[D_index] = Integer.parseInt(input[1]);
				} 
//				else if (input[0].equalsIgnoreCase("xgmax")) {
//					BatteryParam[Ll] = Float.parseFloat(input[1]);
//				} else if (input[0].equalsIgnoreCase("xgmin")) {
//					BatteryParam[Ll] = Float.parseFloat(input[1]);
//				} else if (input[0].equalsIgnoreCase("xgsize")) {
//					BatteryParam[Ll] = Float.parseFloat(input[1]);
//				}
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
		
		Rrange = new float[state_size[R_index] + 1];
		Grange = new float[state_size[G_index] + 1];
		Drange = new float[state_size[D_index] + 1];
		PErange = new float[state_size[PE_index] + 1];
		PDrange = new float[state_size[PD_index] + 1];

		float diff = (state_max[R_index] - state_min[R_index]) / state_size[R_index];
		for (int i = 0; i < Rrange.length; i++)
			Rrange[i] = diff * i + state_min[R_index];
		diff = (state_max[G_index] - state_min[G_index]) / state_size[G_index];
		for (int i = 0; i < Grange.length; i++)
			Grange[i] = diff * i + state_min[G_index];
		diff = (state_max[D_index] - state_min[D_index]) / state_size[D_index];
		for (int i = 0; i < Drange.length; i++)
			Drange[i] = diff * i + state_min[D_index];
		diff = (state_max[PE_index] - state_min[PE_index]) / state_size[PE_index];
		for (int i = 0; i < PErange.length; i++)
			PErange[i] = diff * i + state_min[PE_index];
		diff = (state_max[PD_index] - state_min[PD_index]) / state_size[PD_index];
		for (int i = 0; i < PDrange.length; i++)
			PDrange[i] = diff * i + state_min[PD_index];
		
		//TODO needs clean up.
		XGrange = new float[3];
		for (int i = 0; i < XGrange.length; i++) {
			XGrange[i] = (state_min[G_index] - state_max[G_index]) / (XGrange.length - 1) * i + state_max[G_index];
		}
	}

	public int getDeltat() {
		return delta_t;
	}

	public float getK() {
		return K;
	}

	public float[] getBatteryParam() {
		return BatteryParam;
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

	public int get_row_part() {
		return row_part;
	}

	public int get_col_part() {
		return col_part;
	}

	// row size of sub-matrices
	public int get_size_row() {
		return Rrange.length / row_part;
	}

	// column size of sub-matrices
	public int get_size_col() {
		return (Grange.length * Drange.length) / col_part;
	}

	// number of sub-matrices
	public int get_num_subs() {
		return row_part * col_part;
	}

	public float[] getPDrange() {
		return PDrange;
	}

	public float[][] getPDRTnextprob() {
		return PDRTnextprob;
	}

	public float[][] getFm_prob() {
		return fm_prob;
	}

	public void setFm_prob(float[][] fm_prob) {
		this.fm_prob = fm_prob;
	}

	public float[][] getPrice_prob() {
		return price_prob;
	}

	public void setPrice_prob(float[][] price_prob) {
		this.price_prob = price_prob;
	}

	public void setPErange(float[] pErange) {
		PErange = pErange;
	}

	public void setPDrange(float[] pDrange) {
		PDrange = pDrange;
	}

	public void setDnext(float[][] trans_prob) {
		Dnext = new ArrayList<int[]>();
		DnextProb = new ArrayList<float[]>();
		for (int i = 0; i < trans_prob.length; i++) {
			int count = 0;
			for (int j = 0; j < trans_prob[i].length; j++) {
				if (trans_prob[i][j] != 0) {
					count++;
				}
			}
			int[] temp = new int[count];
			float[] temp_prob = new float[count];
			count = 0;
			for (int j = 0; j < trans_prob[i].length; j++) {
				if (trans_prob[i][j] != 0) {
					temp[count] = j;
					temp_prob[count] = trans_prob[i][j];
					count++;
				}
			}
			Dnext.add(temp);
			DnextProb.add(temp_prob);
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

	public void setRGtransmat(Hashtable<String, float[]> RGnextprob) {
		RGtransMat = RGnextprob;
	}
}