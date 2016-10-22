package states;

import java.util.List;

import utilities.Parameter;

public class EBState extends State {

	// state variables S_t = (R, G_t, D_t, P^E_t).
	// We use the indexing system for each state variable
	protected int R; // R_t: resource
	protected int G; // G_t: perf score
	protected int PE;// P^E_t: 5 min LMP

	// first element is xE, second element is xG
	protected List<int[]> feasibleActions;

	protected List<int[]> Rnext;
	protected List<int[]> Gnext;
	protected int[] PEnext;
	protected List<float[]> Rnextprob;
	protected List<float[]> Gnextprob;
	protected float[][] PEnextprob;
	protected float[] FiveMinCost;
	protected float[] RandomCost;

	public static final int PLUS = 0;
	public static final int MINUS = 1;

	public void setFeasibleActions(Parameter param_) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean getTieBreak(int action_index, int maxIndex) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public float getCurrCost(int action_index) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void initialize(Parameter param) {
		// TODO Auto-generated method stub
		
	}

}
