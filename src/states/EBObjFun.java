package states;

import utilities.Parameter;

public class EBObjFun {

	public static float[] getCurrCost(EBState state, Parameter param) {
		float[] cost = new float[state.getFeasibleActions().size()];
		for (int i = 0; i < cost.length; i++) {
			int Rpostdec = state.getPostDecR(param, i);
			cost[i] = -1 * param.getPErange()[state.getPE()]
					* (param.getDrange()[EBState.getActionSpace()[state.getFeasibleActions().get(i)][0]] > 0 ? 1
							: param.getBatteryParam()[Parameter.etad])
					* param.getDrange()[EBState.getActionSpace()[state.getFeasibleActions().get(i)][0]] * param.getDeltat()
					* Parameter.NoTwoSecPerFiveMin;
			float rand_cost = 0;
			for (int j = 0; j < state.getRGnextProbs(i).length; i++)
				rand_cost += 1 * (param.getRrange()[state.getRGnextStates(i)[j][0]] - param.getRrange()[Rpostdec])
						* state.getRGnextProbs(i)[j];
			cost[i] += rand_cost*param.getPErange()[state.getPE()];
		}
		return cost;
	}

	public static float getRandCost(Parameter param, EBState state, float PE, int a, int Rnew) {
		return -1 * PE * (param.getDrange()[EBState.getActionSpace()[state.getFeasibleActions().get(a)][0]]
				* (param.getDrange()[EBState.getActionSpace()[state.getFeasibleActions().get(a)][0]] > 0 ? 1
						: param.getBatteryParam()[Parameter.etad]) * param.getDeltat() * Parameter.NoTwoSecPerFiveMin +
						param.getRrange()[Math.abs(Rnew - state.getR())]
								/ ((Rnew - state.getR()) > 0 ? 1 : -1 * param.getBatteryParam()[Parameter.etad]));
	}
	
}
