package states;

import java.util.ArrayList;
import java.util.List;

import utilities.DiscreteHelpers;
import utilities.Parameter;

public class EBState extends State {

	// state variables S_t = (R, G_t, D_t, P^E_t).
	// We use the indexing system for each state variable
	protected int R; // R_t: resource
	protected int G; // G_t: perf score
	protected int PE;// P^E_t: 5 min LMP
	private static int[][] actionSpace;

	// first element is xE, second element is xG
	private List<int[][]> RGnext;
	private List<float[]> RGnextProb;
	
	public static final int XE_INDEX = 0;
	public static final int XG_INDEX = 1;
	
	public static final int R_INDEX = 0;
	public static final int G_INDEX = 1;

	public EBState(Parameter param, int R_, int G_, int PE_) {
		R = R_;
		G = G_;
		PE = PE_;

		valueFunction = new float[Parameter.NO_FIVE_MIN_PER_HR + 1];
		optAction = new int[Parameter.NO_FIVE_MIN_PER_HR + 1];
		for (int i = 0; i < valueFunction.length; i++) {
			valueFunction[i] = Float.NEGATIVE_INFINITY;
			optAction[i] = -1;
		}
		if (actionSpace == null) {
			actionSpace = new int[param.getDrange().length*param.getXGrange().length][2];
			int a = 0;
			for (int i = 0; i < param.getDrange().length; i++) {
				for (int j = 0; j < param.getXGrange().length; j++) {
					actionSpace[a][XE_INDEX] = i;
					actionSpace[a][XG_INDEX] = j;
					a++;
				}
			}
		}
	}

	public void setFeasibleActions(Parameter param) {
		float Rmin = param.getRrange()[0];
		float Rmax = param.getRrange()[param.getRrange().length - 1];
		feasibleActions = new ArrayList<Integer>();

		for (int i = 0; i < actionSpace.length; i++) {
			float Rnewup = param.getRrange()[R] + (param.getDrange()[actionSpace[i][XE_INDEX]]
					+ param.getK() * (1 - param.getXGrange()[actionSpace[i][XG_INDEX]])) * Parameter.NO_TWO_SEC_PER_FIVE_MIN
					* param.getDeltat();
			float Rnewdown = param.getRrange()[R] + (param.getDrange()[actionSpace[i][XE_INDEX]]
					- param.getK() * (1 - param.getXGrange()[actionSpace[i][XG_INDEX]])) * Parameter.NO_TWO_SEC_PER_FIVE_MIN
					* param.getDeltat();
			float XMag = Math.abs(param.getDrange()[actionSpace[i][XE_INDEX]] + param.getK()
					* (1 - param.getXGrange()[actionSpace[i][XG_INDEX]]) * (param.getDrange()[actionSpace[i][XE_INDEX]] > 0 ? 1 : -1));
			if (XMag <= param.getBatteryParam()[Parameter.BETA_C]) {
				if (Rnewdown >= Rmin && Rnewup <= Rmax) {
					feasibleActions.add(i);
				}
			}
		}
	}

	public boolean getTieBreak(int action_index, int max_index) {
		if (actionSpace[action_index][XG_INDEX] < actionSpace[max_index][XG_INDEX])
			return true;
		return false;
	}

	public void initialize(Parameter param_) {
		setFeasibleActions(param_);
		setRGnext(param_);
		// setPEnext(param_);
		setCostFunction(param_);
	}

	/**
	 * This function computes the post decision R_t^xE, given xE.
	 * 
	 * @param param
	 * @param action
	 * @return
	 */

	public int getPostDecR(Parameter param, int action) {
		// TODO: Now we set the action to be the same as D.
		float a = param.getDrange()[actionSpace[feasibleActions.get(action)][XE_INDEX]];
		if (Math.abs(a) < 0.01)
			return R;
		float Rnew = param.getRrange()[R] + a * Parameter.NO_TWO_SEC_PER_FIVE_MIN * param.getDeltat()
				* (a > 0 ? param.getBatteryParam()[Parameter.ETA_C] : 1);
		return DiscreteHelpers.getIndex(Rnew, param.getRrange());
	}

	public void setRGnext(Parameter param) {
		RGnext = new ArrayList<int[][]>(feasibleActions.size());
		RGnextProb = new ArrayList<float[]>(feasibleActions.size());

		for (int i = 0; i < feasibleActions.size(); i++) {
			int RpostDec = getPostDecR(param, i);
			int XE = actionSpace[feasibleActions.get(i)][XE_INDEX];
			int XG = actionSpace[feasibleActions.get(i)][XG_INDEX];
			String key;

			if (XE == 0) {
				// xe = -/+ 1
				key = Integer.toString(220);
			} else if (XE == 4) {
				key = Integer.toString(2235);

			} else if (XE == 1) {
				// xe = -0.5
				if (RpostDec >= 15 && RpostDec <= 150) {
					key = Integer.toString(223);
				} else if (RpostDec < 15) {
					key = Integer.toString(22) + Integer.toString(RpostDec / 5);
				} else {
					key = Integer.toString(11) + Integer.toString((RpostDec - 1) / 5);
				}
			} else if (XE == 3) {
				// xe = +0.5
				if (RpostDec + 1 >= 30 && RpostDec - 1 <= 165) {
					key = Integer.toString(2232);
				} else if (RpostDec - 1 > 165) {
					key = Integer.toString(22) + Integer.toString((RpostDec - 1) / 5);
				} else {
					key = Integer.toString(31) + Integer.toString((RpostDec + 1) / 5);
				}
			} else {
				// xe = 0;
				if (XG == 1) {
					key = Integer.toString(21);
				} else if (XG == 0) {
					key = Integer.toString(22);
				} else {
					if (RpostDec >= 30 && RpostDec <= 150) {
						key = Integer.toString(22);
					} else if (RpostDec + 1 < 30) {
						key = Integer.toString(22) + Integer.toString((RpostDec + 1) / 5);
					} else {
						key = Integer.toString(22) + Integer.toString((RpostDec - 1) / 5);
					}
				}
			}

			int[][] tempStates = param.getRGtransOffset().get(key);
			float[] tempTrans = param.getRGtransmat().get(key);
			int[][] newStates = new int[tempStates.length][tempStates[0].length];
			for (int j = 0; j < tempStates.length; j++) {
				// newStates[j][0] = tempStates[j][0] + RpostDec;
				newStates[j][R_INDEX] = Math.max(0, Math.min(tempStates[j][0] + RpostDec, param.getRrange().length - 1));
				// if (newStates[j][0] < 0 || newStates[j][0] > 180) {
				// System.out.println(R + " " + tempStates[j][0] + " key:" + key);
				// }
				newStates[j][G_INDEX] = Math.min(tempStates[j][1] + G, param.getGrange().length - 1);
			}
			RGnext.add(newStates);
			RGnextProb.add(tempTrans);
		}
	}

	/**
	 * Computes the expected cost function E { C_t (S_t, x_t, W_{t+1})}
	 * 
	 * @param param
	 */
	private void setCostFunction(Parameter param) {
		costFunction = EBObjFun.getCurrCost(this, param);
	}

	public int getR() {
		return R;
	}

	public int getG() {
		return G;
	}

	public int getPE() {
		return PE;
	}

	public static int[][] getActionSpace() {
		return actionSpace;
	}

	public int[][] getRGnextStates(int action) {
		return RGnext.get(action);
	}

	public float[] getRGnextProbs(int action) {
		return RGnextProb.get(action);
	}

}
