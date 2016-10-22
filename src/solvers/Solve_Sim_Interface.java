package solvers;

import states.State;

public interface Solve_Sim_Interface {
	public void findMax(State s, int t);
	public float findNextStateExpectValue(State s, int actionIndex, int t);
}
