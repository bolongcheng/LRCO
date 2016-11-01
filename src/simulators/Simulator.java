package simulators;

import solvers.Solver;
import utilities.Parameter;

public abstract class Simulator {
	protected Parameter param;
	protected Solver solver;
	protected float[][][] path;
	protected int numTrial;

	public void LoadSolver(Solver solver_) {
		solver = solver_;
	}

	public abstract void RunSimulation();

	public float[][][] getPath() {
		return path;
	}
}
