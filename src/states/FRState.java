package states;

import java.util.ArrayList;
import java.util.List;
import utilities.Parameter;
import utilities.DiscreteHelpers;

public class FRState extends State {
	/**
	 * @author bcheng
	 * @date 10/15/2016
	 * 
	 */

	// state variables S_t = (R, G_t, D_t) .
	private int R; // R_t: resource
	private int G; // G_t: perf score
	private int D; // D_t: RegD signal
	private static float[] actionSpace;

	private float xE;
	private float xG;
//	private List<Integer> feasibleActions;

	private int[] Rnext;
	private int[] Gnext;
	private int[] Dnext;
	private float[] DnextProb;

	private static float EPSILON = (float) 0.0001;

	public FRState(Parameter param_, int R_, int G_, int D_) {
		R = R_;
		G = G_;
		D = D_;
		xE = param_.getXE();
		xG = param_.getXG();
		actionSpace = param_.getDrange();
		valueFunction = new float[Parameter.NO_TWO_SEC_PER_FIVE_MIN + 1];
		optAction = new int[Parameter.NO_TWO_SEC_PER_FIVE_MIN + 1];
		for (int i = 0; i < valueFunction.length; i++) {
			valueFunction[i] = Float.NEGATIVE_INFINITY;
			optAction[i] = -1;
		}
	}

	public void initialize(Parameter param_) {
		setFeasibleActions(param_);
		setCostFunction(param_);
		setRnext(param_);
		setGnext(param_);
		setDnext(param_);
	}

	private void setDnext(Parameter param) {
		Dnext = param.getDnext(D);
		DnextProb = param.getDnextProb(D);
	}

	/**
	 * Gnext is the same size as feasibleActions. Each element of Gnext is corresponding to the
	 * particular feasibleAction.
	 * 
	 * @param param
	 */
	private void setGnext(Parameter param) {
		Gnext = new int[feasibleActions.size()];
		for (int i = 0; i < feasibleActions.size(); i++) {
			float Gnew = param
					.getGrange()[G]
					- Math
							.min(Math
									.abs(param.getDrange()[feasibleActions.get(i)]
											* (param.getDrange()[feasibleActions.get(i)] < 0
													? param.getBatteryParam()[Parameter.ETA_D] : 1)
											+ param.getDrange()[D])
									/ param.getK(), 1)
							/ (Parameter.NO_TWO_SEC_PER_FIVE_MIN * Parameter.NO_FIVE_MIN_PER_HR);
			Gnext[i] = DiscreteHelpers.getRevIndex(Gnew, param.getGrange());
		}
	}

	/**
	 * Rnext is the same size as feasibleActions. Each element of Rnext is corresponding to the
	 * particular feasibleAction
	 * 
	 * @param param
	 */

	private void setRnext(Parameter param) {
		Rnext = new int[feasibleActions.size()];
		for (int i = 0; i < feasibleActions.size(); i++) {
			float Rnew = param.getRrange()[R] + (param.getDrange()[feasibleActions.get(i)] + xE) * param.getDeltat()
					* (((xE + param.getDrange()[feasibleActions.get(i)] > 0) ? param.getBatteryParam()[Parameter.ETA_C]
							: 1));
			Rnext[i] = DiscreteHelpers.getIndex(Rnew, param.getRrange());
		}
	}

	/**
	 * This function sets the feasibleActions for state S.
	 * 
	 * @param param
	 */
	public void setFeasibleActions(Parameter param) {
		float Rmin = param.getRrange()[0];
		float Rmax = param.getRrange()[param.getRrange().length - 1];
		feasibleActions = new ArrayList<Integer>();
		// We assume the response has the same granularity as the the signal
		/**
		 * @date 07/10 NOTE we assume that the response goes from 0 to 1 or 0 to -1.
		 */
		if (D < param.getDrange().length / 2) {
			for (int i = param.getDrange().length / 2; i < param.getDrange().length; i++) {
				float XMag = Math.abs(param.getDrange()[i] + xE);
				float Rnew = param.getRrange()[R] + (param.getDrange()[i] + xE) * param.getDeltat();
				float Gdeg = Math.min(Math.abs(param.getDrange()[i] + param.getDrange()[D]) / param.getK(), 1);
				if (XMag <= param.getBatteryParam()[Parameter.BETA_C]) {
					if (Rnew >= Rmin && Rnew <= Rmax) {
						if (Gdeg <= xG + EPSILON) {
							feasibleActions.add(i);
						}
					}
				}
			}
		} else {
			for (int i = 0; i <= param.getDrange().length / 2; i++) {
				float XMag = Math.abs(param.getDrange()[i] + xE);
				float Rnew = param.getRrange()[R] + (param.getDrange()[i] + xE) * param.getDeltat();
				float Gdeg = Math.min(Math.abs(param.getDrange()[i] + param.getDrange()[D]) / param.getK(), 1);
				if (XMag <= param.getBatteryParam()[Parameter.BETA_C]) {
					if (Rnew >= Rmin && Rnew <= Rmax) {
						if (Gdeg <= xG + EPSILON) {
							feasibleActions.add(i);
						}
					}
				}
			}
		}
		if (feasibleActions.isEmpty()) {
			feasibleActions.add(param.getDrange().length / 2);
		}
	}

	/**
	 * Computes the stage-wise cost function C_t
	 * 
	 * @param param
	 */
	private void setCostFunction(Parameter param) {
		costFunction = FRObjFun.getCurrCost(this, param);
	}

//	public List<Integer> getFeasibleActions() {
//		return feasibleActions;
//	}

	public void setxE(float xE_) {
		xE = xE_;
	}

	public float getxE() {
		return xE;
	}

	public void setXG(float xg) {
		xG = xg;
	}

	public float getXG() {
		return xG;
	}

	public int getG() {
		return G;
	}

	public int getR() {
		return R;
	}

	public int getD() {
		return D;
	}

	public int getRnext(int i) {
		return Rnext[i];
	}

	public int getGnext(int i) {
		return Gnext[i];
	}

	public int[] getDnext() {
		return Dnext;
	}

	public float[] getDnextProb() {
		return DnextProb;
	}

	public int getOptActionString(int t) {
		return feasibleActions.get(optAction[t]);
	}
	
	//returns true if i is closer to the signal than the maxIndex
	public boolean getTieBreak(int i, int maxIndex) {
		return (Math.abs(actionSpace[getFeasibleActions().get(i)] + actionSpace[D]) < Math
				.abs(actionSpace[getFeasibleActions().get(maxIndex)] + actionSpace[D]));
	}

}
