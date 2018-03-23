# LRCO
This project is created to implement the sparse low rank VFA from the paper, [Low-Rank Value Function Approximation for Co-optimization of Battery Storage](http://ieeexplore.ieee.org/document/7950964/). It also includes most of the implementation of the paper, [Co-optimizing Battery Storage for the Frequency Regulation and Energy Arbitrage Using Multi-Scale Dynamic Programming](http://ieeexplore.ieee.org/document/7558191/). The project is a java project with the following packages:
* [**states**](#states)
* [**solvers**](#solvers)
* [**simulators**](#simulators)
* [**users**](#users)
* [**utilites**](#utilities)

This code requires [matlabcontrol](https://github.com/jakaplan/matlabcontrol/releases), a Java API that allows for calling MATLAB from Java. This can be done by importing the `.jar` file to the Java project. In addition, this project also includes three Matlab routines:

* **random_states.m**
* **svd_approx_partition.m**
* **svd_approx_partitionLS.m**

## states
[`State.java`](../master/src/states/State.java) is the abstract class for state variables, which includes getters and setters for variables such as `valueFunction`, `optActions`, `costFunctions` and `feasibleActions`. There are three base classes derived from the abstract class: `RState.java`, `EBState.java` and `FRState.java`. The base classes also describes the transition functions (and probabilities) for each of the state variable. The base classes need to implement the methods `setFeasibleActions` which creates the feasible decision space for each state and `getTieBreak` that helps break the tie in case two states have the same value function.

 * [`RState.java`](../master/src/states/RState.java) is the base class that describes the *resource* state, which includes two state variables (R, P^E).
 * [`EBState.java`](../master/src/states/EBState.java) is the base class that describes the *energy basepoint* state, which includes three state variables (R, G, P^E). The [`EBState4D.java`](../master/src/states/EBState4D.java) is a subclass of `EBState.java`, and has four state variables (R, G, P^E, P^D).
 * [`FRState.java`](../master/src/states/FRState.java) is the base class that describes the *frequency regulation* state variable, which includes three state variables (R, G, D).

The cost functions of the states are implemented in [`RObjFun.java`](../master/src/states/RObjFun.java), [`EBObjFun.java`](../master/src/states/EBObjFun.java), and [`FRObjFun.java`](../master/src/states/FRObjFun.java), respectively.

## solvers

[`Solver.java`](../master/src/solvers/Solver.java) is the abstract class for solving the backward dynamic program. The base classes need to implement `findNextStateExpectValue` which computes the expected value function of the next state, for a state and a given decision. In addition, base calsses also need to implement  `populateStates(float[][] terminalValueFunction)`, which creates the state space with the given terminal value function conditions.

[`EBSolver.java`](../master/src/solvers/EBSolver.java) is the base class that solves the MDP for *energy basepoint* sub-problem using backward dynamic programming.

[`FRSolver.java`](../master/src/solvers/FRSolver.java) is the base class that solves the MDP for the *frequency regulation* sub-problem using backward dynamic programming. 

## simulators
[`Simulator.java`](../master/src/simulators/Simulator.java) is the abstract class for simulate the Markov decision process.  
[`EBSimulator.java`](../master/src/simulators/EBSimulator.java) simulates the *energy basepoint* MDP, which is in 5-minute increment for the horizon of 24 hours.
[`FRSimulator.java`](../master/src/simulators/FRSimulator.java) simulates the *frequency regulation* MDP, which is in 4-second increment for the horizon of 5 minutes.

## users
This includes the main files used to run the simulators and the solvers.

## utilities
This package contains programs that mostly facilitate some of the mundane tasks such as input/output, setting parameters, manipulating discrete sets, and some linear algebra computation.

[`CSVIO.java`](../master/src/utilities/CSVIO.java) handles the read/write of 2D arrays from/to `.csv` files. This is the most basic way of storing value functions and other data (e.g. prices), but it becomes very inefficient when the value function is large.

[`FastIO.java`](../master/src/utilities/FastIO.java) handles the read/write of 2D float arrays in binary format. This class uses the `java.nio` API to open channels and allocate `ByteBuffer` the size of a single row of the data type. This implementation is about 3-5 times faster than the `CSVIO` implementation on average. (Note: I have used `.dat` as the suffix of the binary files. Remember to follow the Java convention for space allocation, e.g., 4 bytes for single-precision float.)

[`DiscreteHelpers.java`](../master/src/utilities/DiscreteHelpers.java) contains static methods that help facilitate operations on an array of discrete values, such as convolution, finding the index of an element, interpolation, etc.

[`IOHelpers.java`](../master/src/utilities/IOHelpers.java) has static methods that convert n-D arrays into different dimensions.

[`Parameter.java`](../master/src/utilities/Parameter.java) is the class for handling all of the static parameters and the state space parameters of the problems. The state space parameters define the size of each state variable (discretization levels). We inlcude samples of the static parameter file and state space parameter file.

[`VFApprox.java`](../master/src/utilities/VFApprox.java) is for storing the sparse low rank approximations for a single time step. Each rank-1 approximation is stored as x-vectors, y-vectors and the shift values for all sub-matrices. This classes can output the x-vectors, y-vectors as 1-D arrays. It can also compute the approximated value of a state given the absolute x, y location.


## MatLab files
[`random_states.m`](../master/random_states.m) performs the latin-hypercube sampling for a given matrix and the number desired samples per row/column. The following line can be commented out if the user does not have `CPLEX` installed on the machine. 
```matlab
addpath('/opt/ilog/cplex/matlab');
```
[`svd_approx_partition.m`](../master/svd_approx_partition.m) computes the sparse low rank approximation with an L1 objective function. This code requires `CPLEX` in order to the solve a linear program. 

[`svd_approx_partitionLS.m`](../master/svd_approx_partitionLS.m) computes the sparse low rank approximation with an L2 objective function. 
