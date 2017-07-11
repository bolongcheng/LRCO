# LRCO
This project is created to implement the sparse low rank VFA from the paper, [Low-Rank Value Function Approximation for Co-optimization of Battery Storage](http://ieeexplore.ieee.org/document/7950964/). It also includes most of the implementation of the paper, [Co-optimizing Battery Storage for the Frequency Regulation and Energy Arbitrage Using Multi-Scale Dynamic Programming](http://ieeexplore.ieee.org/document/7558191/). The project is a java project with the following packages:
* **simulators**
* **solvers**
* **states**
* **users**
* **utilites**

This code requires [matlabcontrol](https://github.com/jakaplan/matlabcontrol/releases), a Java API that allows for calling MATLAB from Java. This can be done by importing the `.jar` file to the Java project. In addition, this project also includes three Matlab routines:

* **random_states.m**
* **svd_approx_partition.m**
* **svd_approx_partitionLS.m**

## simulators

## solvers

## states
`State.java` is the abstract class for state variables, which includes getters and setters for variables such as `valueFunction`, `optActions`, `costFunctions` and `feasibleActions`. There are three base classes derived from the abstract class: `RState.java`, `EBState.java` and `FRState.java`. The base classes also describes the transition functions (and probabilities) for each of the state variable.

 * `RState.java` is the base class that describes the `resource` state, which includes two state variables (R, P^E).
 * `EBState.java` is the base class that describes the *energy basepoint* state, which includes three state variables (R, G, P^E). The `EBState4D.java` is a subclass of `EBState.java`, and has four state variables (R, G, P^E, P^D).
 * `FRState.java` is the base class that describes the *frequency regulation* state variable, which includes three state variables (R, G, D).

The cost functions of the states are implemented in `RObjFun.java`, `EBObjFun.java`, and `FRObjFun.java`, respectively.


## users

## utilities

## MatLab files
`random_states.m` performs the latin-hypercube sampling for a given matrix and the number desired samples per row/column.

`svd_approx_partition.m` computes the sparse low rank approximation with an L1 objective function.

`svd_approx_partitionLS.m` computes the sparse low rank approximation with an L2 objective function.
