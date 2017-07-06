package states;

import utilities.Parameter;

public class EBState4D extends EBState {
    private int PD;

    public EBState4D(Parameter param, int R_, int G_, int PE_, int PD_) {
        super(param, R_, G_, PE_);
        PD = PD_;

        valueFunction = new float[Parameter.NO_FIVE_MIN_PER_HR * 24 + 1];
        optAction = new int[Parameter.NO_FIVE_MIN_PER_HR * 24 + 1];
        for (int i = 0; i < valueFunction.length; i++) {
            valueFunction[i] = Float.NEGATIVE_INFINITY;
            optAction[i] = -1;
        }
    }

    public int getPD() {
        return PD;
    }

}
