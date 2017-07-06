package states;

import utilities.Parameter;

public class FRObjFun {

    public static float[] getCurrCost(FRState state, Parameter param) {
        float[] cost = new float[state.getFeasibleActions().size()];
        for (int i = 0; i < cost.length; i++) {
            float action = param.getDrange()[state.getFeasibleActions().get(i)];
            cost[i] = -1 * param.getPE()
                    * (state.getxE() * (state.getxE() > 0 ? 1 : param.getBatteryParam()[Parameter.ETA_D])
                    + (action > 0 ? action : param.getBatteryParam()[Parameter.ETA_D] * action))
                    * param.getDeltat();
        }
        return cost;
    }

}
