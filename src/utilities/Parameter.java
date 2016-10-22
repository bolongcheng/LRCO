package utilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * The Parameter class handles the input parameters. The input csv file is
 * formatted in the following order:
 * 
 * Delta t, K, R_max, beta^c, beta^d, L^u, L^l, eta^c, eta^d R_size,
 * G_size, D_size, P^E_size R_max, G_min, D_max, P^E_max R_min, G_max, D_min,
 * P^E_min P^E, P^D, x^G, X^E
 * 
 * @author bcheng
 * @date 10/26
 *
 */
public class Parameter {
	// public static final int NoTwoSecPerMin = 150;
	public static final int NoTwoSecPerFiveMin = 30; // Ten Second
	public static final int NoFiveMinPerHr = 12;
	public static final int RC = 0; // Energy capacity in MWh
	public static final int betac = 1; // charging capacity
	public static final int betad = 2; // discharging capacity
	public static final int Lu = 3; // upper limit
	public static final int Ll = 4; // lower limit
	public static final int etac = 5; // charging efficiency
	public static final int etad = 6; // discharging efficiency

	public static final int Rtag = 0;
	public static final int Gtag = 1;
	public static final int Dtag = 2;
	public static final int PEtag = 3;
	public static final int PDtag = 4;

	public static final int row_part = 6; // how many sub-matrices from top to
											// bottom
	// 63 if we use |D| = 21;
	public static final int col_part = 63; // how many sub-matrices from left to
											// right

	private float[] BatteryParam; // stores the previous parameters
	private int TStart; // Time horizon
	private float K; // regulation capacity in MW during [0,T]
	private int deltat;

	private float PE;
	private float PD;
	private float Gfac;
	private float EcoBase;

	// pdstate_size is a n-dimensional vector of the state space discretization
	// along each dimension. pdstate_min/pdstate_max are the lower and upper
	// bounds of the state space in each dimension. linspace(pdstate_min(i),
	// pdstate_max(i), pdstate_size(i)) is the state space along the i-th
	// dimension. Each state is a vector in R^n
	// Same for predecision states (default: post space = pre space)
	private int[] prestate_size = null;
	private float[] prestate_min = null;
	private float[] prestate_max = null;

	private float[] Rrange;
	private float[] Grange;
	private float[] Drange;
	private float[] PErange;
	private float[] XGrange;
	private float[][] LMPdensity;
	private float[][] PERTnextprob;
	private float[][] RTPrice;

	private float[][] fm_prob;
	private float[][] price_prob;

	private float[] PDrange;
	private float[][] PDRTnextprob;

	private List<int[]> RnextOffset;
	private List<float[]> RnextProb;
	private List<int[]> GnextOffset;
	private List<float[]> GnextProb;

	private List<int[]> Dnext;
	private List<float[]> DnextProb;
	
	private Hashtable<String, int[][]> RGtransOffset;
	private Hashtable<String, float[]> RGtransMat;
	
	public Parameter(String filename_) {
		/* TODO: clean up i/o here */
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		try {
			br = new BufferedReader(new FileReader(filename_));
			if ((line = br.readLine()) != null) {
				// use comma as separator
				String[] input = line.split(cvsSplitBy);
				TStart = Math.max(0, Math.min(Integer.parseInt(input[0]), 23));
				deltat = Integer.parseInt(input[1]);
				K = Float.parseFloat(input[2]);
				BatteryParam = new float[input.length - 3];
				for (int i = 3; i < input.length; i++) {
					BatteryParam[i - 3] = Float.parseFloat(input[i]);
				}
			}

			if ((line = br.readLine()) != null) {
				String[] input = line.split(cvsSplitBy);
				prestate_size = new int[input.length];
				for (int i = 0; i < input.length; i++)
					prestate_size[i] = Integer.parseInt(input[i]);
			}

			if ((line = br.readLine()) != null) {
				String[] input = line.split(cvsSplitBy);
				prestate_min = new float[input.length];
				for (int i = 0; i < input.length; i++)
					prestate_min[i] = Float.parseFloat(input[i]);
			}

			if ((line = br.readLine()) != null) {
				String[] input = line.split(cvsSplitBy);
				prestate_max = new float[input.length];
				for (int i = 0; i < input.length; i++)
					prestate_max[i] = Float.parseFloat(input[i]);
			}

			if ((line = br.readLine()) != null) {
				// use comma as separator
				String[] input = line.split(cvsSplitBy);
				PE = Float.parseFloat(input[0]);
				PD = Float.parseFloat(input[1]);
				Gfac = Float.parseFloat(input[2]);
				EcoBase = Float.parseFloat(input[3]);
			}

			Rrange = new float[prestate_size[Rtag] + 1];
			Grange = new float[prestate_size[Gtag] + 1];
			Drange = new float[prestate_size[Dtag] + 1];
			PErange = new float[prestate_size[PEtag] + 1];
			PDrange = new float[prestate_size[PDtag] + 1];

			PERTnextprob = new float[NoFiveMinPerHr][prestate_size[PEtag] + 1];

			float diff = (prestate_max[Rtag] - prestate_min[Rtag]) / prestate_size[Rtag];
			for (int i = 0; i < Rrange.length; i++)
				Rrange[i] = diff * i + prestate_min[Rtag];
			diff = (prestate_max[Gtag] - prestate_min[Gtag]) / prestate_size[Gtag];
			for (int i = 0; i < Grange.length; i++)
				Grange[i] = diff * i + prestate_min[Gtag];
			diff = (prestate_max[Dtag] - prestate_min[Dtag]) / prestate_size[Dtag];
			for (int i = 0; i < Drange.length; i++)
				Drange[i] = diff * i + prestate_min[Dtag];
			diff = (prestate_max[PEtag] - prestate_min[PEtag]) / prestate_size[PEtag];
			for (int i = 0; i < PErange.length; i++)
				PErange[i] = diff * i + prestate_min[PEtag];
			diff = (prestate_max[PDtag] - prestate_min[PDtag]) / prestate_size[PDtag];
			for (int i = 0; i < PDrange.length; i++)
				PDrange[i] = diff * i + prestate_min[PDtag];

			// FIXME: This is hardcoded!!
			XGrange = new float[3];
			for (int i = 0; i < XGrange.length; i++) {
				XGrange[i] = (prestate_min[Gtag] - prestate_max[Gtag]) / (XGrange.length - 1) * i + prestate_max[Gtag];
				// System.out.println(XGrange[i]);
			}

			if (BatteryParam[Ll] * BatteryParam[RC] != prestate_min[0])
				System.out.println("Error: Rmin inconsistent.");
			if (BatteryParam[Lu] * BatteryParam[RC] != prestate_max[0])
				System.out.println("Error: Rmax inconsistent.");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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

	public void modifyRTPrice(String fileName, int startHr) {
		/* TODO: set up i/o here */
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		LMPdensity = new float[24 * NoFiveMinPerHr][prestate_size[PEtag] + 1];
		try {
			br = new BufferedReader(new FileReader(fileName));
			int i = 0;
			// The first line displays the clustered centers
			if ((line = br.readLine()) != null) {
				String[] input = line.split(cvsSplitBy);
				for (int j = 0; j < input.length; j++) {
					PErange[j] = Float.parseFloat(input[j]);
				}
			}
			// The rest of the file is the price distribution
			while ((line = br.readLine()) != null) {
				String[] input = line.split(cvsSplitBy);
				for (int j = 0; j < input.length; j++) {
					LMPdensity[i][j] = Float.parseFloat(input[j]);
				}
				i++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		// pass by value
		for (int i = 0; i < NoFiveMinPerHr; i++) {
			for (int j = 0; j < prestate_size[PEtag] + 1; j++) {
				PERTnextprob[i][j] = LMPdensity[startHr * NoFiveMinPerHr + i][j];
			}
		}
	}

	public float[][] getLMPdensity() {
		return LMPdensity;
	}

	public int getTStart() {
		return TStart;
	}

	public int getDeltat() {
		return deltat;
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

	public float getGfac() {
		return Gfac;
	}

	public void setGfac(float Gfac_) {
		Gfac = Gfac_;
	}

	public float getEcoBase() {
		return EcoBase;
	}

	public void setEcoBase(float EcoBase_) {
		EcoBase = EcoBase_;
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

	public float[][] getPERTnextprob() {
		return PERTnextprob;
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
		// TODO Auto-generated method stub
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

	public void LoadDnext(float[][] trans_prob) {
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
	// D_t is a first order markov
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
	
	public void setRGtransOffset(Hashtable<String, int[][]> RGnext){
		RGtransOffset = RGnext;
	}
	
	public void setRGtransmat(Hashtable<String, float[]> RGnextprob){
		RGtransMat = RGnextprob;
	}
}