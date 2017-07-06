package states;

import java.util.List;

import utilities.Parameter;

public abstract class State {
    protected int[] optAction;
    protected float[] valueFunction;
    protected List<Integer> feasibleActions;
    protected float[] costFunction;

    public void setValueFunction(float v, int t) {
        valueFunction[t] = v;
    }

    public float getValueFunction(int t) {
        return valueFunction[t];
    }

    public void setOptAction(int action, int t) {
        optAction[t] = action;
    }

    public int getOptAction(int t) {
        return optAction[t];
    }

    public abstract void setFeasibleActions(Parameter param_);

    public List<Integer> getFeasibleActions() {
        return feasibleActions;
    }

    public abstract boolean getTieBreak(int actionIndex, int maxIndex);

    public float getCostFunction(int actionIndex) {
        return costFunction[actionIndex];
    }

    public abstract void initialize(Parameter param);

    public float[] getValueFunction() {
        return valueFunction;
    }


}
