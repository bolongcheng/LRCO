package states;

import java.util.ArrayList;

import utilities.DiscreteHelpers;
import utilities.Parameter;

public class RState extends State {

	private int R; // R_t: resource
	private int PE;// P^E_t: 5 min LMP

	private int[] Rnext;
	private int[] PEnext;
	private float[][] PEnextprob;

	public RState(Parameter param_, int R_, int PE_) {
		R = R_;
		PE = PE_;

		valueFunction = new float[Parameter.NO_FIVE_MIN_PER_HR * 24 + 1];
		optAction = new int[Parameter.NO_FIVE_MIN_PER_HR * 24 + 1];
		for (int i = 0; i < valueFunction.length; i++) {
			valueFunction[i] = Float.NEGATIVE_INFINITY;
			optAction[i] = -1;
		}
	}

	private void setCostFunction(Parameter param) {
		costFunction = RObjFun.getCurrCost(this, param);
	}

	private void setRnext(Parameter param) {
		Rnext = new int[feasibleActions.size()];
		for (int i = 0; i < feasibleActions.size(); i++) {
			float a = param.getDrange()[feasibleActions.get(i)];
			float Rnew = param.getRrange()[R] + a * Parameter.NO_TWO_SEC_PER_FIVE_MIN * param.getDeltat()
					* (a > 0 ? param.getBatteryParam()[Parameter.ETA_C] : 1);
			Rnext[i] = DiscreteHelpers.getIndex(Rnew, param.getRrange());
		}
	}

	public void setFeasibleActions(Parameter param) {
		float Rmin = param.getRrange()[0];
		float Rmax = param.getRrange()[param.getRrange().length - 1];
		feasibleActions = new ArrayList<Integer>();
		// We use Drange for setting xE (eco basepoint)
		for (int i = 0; i < param.getDrange().length; i++) {
			float Rnew = param.getRrange()[R]
					+ param.getDrange()[i] * Parameter.NO_TWO_SEC_PER_FIVE_MIN * param.getDeltat();
			float XMag = Math.abs(param.getDrange()[i]);
			if (XMag <= param.getBatteryParam()[Parameter.BETA_C]) {
				if (Rnew >= Rmin && Rnew <= Rmax) {
					feasibleActions.add(i);
				}
			}
		}
		if (feasibleActions.isEmpty()) {
			feasibleActions.add(DiscreteHelpers.getIndex(0, param.getDrange()));
		}

	}

	public boolean getTieBreak(int actionIndex, int maxIndex) {
		if (Rnext[actionIndex] > Rnext[maxIndex])
			return true;
		return false;
	}

	public void initialize(Parameter param) {
		setFeasibleActions(param);
		setRnext(param);
		setCostFunction(param);

	}

	public int getR() {
		return R;
	}

	public int getPE() {
		return PE;
	}

	public int getRnext(int actionIndex) {
		return Rnext[actionIndex];
	}

}
