package states;

import utilities.Parameter;

public class EBObjFun {

	public static float[] getCurrCost(EBState state, Parameter param) {
		float[] cost = new float[state.getFeasibleActions().size()];
		for (int i = 0; i < cost.length; i++) {
			int Rpostdec = state.getPostDecR(param, i);
			cost[i] = -1 * param.getPErange()[state.getPE()]
					* (param.getDrange()[EBState.getActionSpace()[state.getFeasibleActions()
							.get(i)][EBState.XE_INDEX]] > 0 ? 1 : param.getBatteryParam()[Parameter.ETA_D])
					* param.getDrange()[EBState.getActionSpace()[state.getFeasibleActions().get(i)][EBState.XE_INDEX]]
					* param.getDeltat() * Parameter.NO_TWO_SEC_PER_FIVE_MIN;
			float rand_cost = 0;
			for (int j = 0; j < state.getRGnextProbs(i).length; j++)
				rand_cost += (param.getRrange()[state.getRGnextStates(i)[j][EBState.R_INDEX]]
						- param.getRrange()[Rpostdec]) * state.getRGnextProbs(i)[j];
			cost[i] += -1 * rand_cost * param.getPErange()[state.getPE()];
		}
		return cost;
	}

	public static float getRandCost(Parameter param, EBState state, float PE, int a, int Rnew) {
		return -1 * PE
				* (param.getDrange()[EBState.getActionSpace()[state.getFeasibleActions().get(a)][EBState.XE_INDEX]]
						* (param.getDrange()[EBState.getActionSpace()[state.getFeasibleActions()
								.get(a)][EBState.XE_INDEX]] > 0 ? 1 : param.getBatteryParam()[Parameter.ETA_D])
						* param.getDeltat() * Parameter.NO_TWO_SEC_PER_FIVE_MIN
						+ param.getRrange()[Math.abs(Rnew - state.getR())]
								/ ((Rnew - state.getR()) > 0 ? 1 : -1 * param.getBatteryParam()[Parameter.ETA_D]));
	}

}
