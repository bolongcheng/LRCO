package states;

import utilities.Parameter;

public class RObjFun {
    public static float[] getCurrCost(RState state, Parameter param) {
        float[] cost = new float[state.getFeasibleActions().size()];
        for (int i = 0; i < cost.length; i++) {
            cost[i] = -1 * param.getPErange()[state.getPE()]
                    * (param.getDrange()[state.getFeasibleActions().get(i)] >= 0 ? 1
                    : param.getBatteryParam()[Parameter.ETA_D])
                    * param.getDrange()[state.getFeasibleActions().get(i)] * param.getDeltat()
                    * Parameter.NO_TWO_SEC_PER_FIVE_MIN;
            System.currentTimeMillis();
        }
        return cost;
    }
}
