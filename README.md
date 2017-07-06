# LRCO
This project is created to implement the sparse low rank VFA from the paper, [Low-Rank Value Function Approximation for Co-optimization of Battery Storage](http://ieeexplore.ieee.org/document/7950964/). The project has a java project with the following packages:
* **simulators**
* **solvers**
* **states**
* **users**
* **utilites**

The Java code requires [matlabcontrol](https://github.com/jakaplan/matlabcontrol/releases), a Java API that allows for calling MATLAB from Java. This can be done by importing the `.jar` file to the Java project. In addition, this project also includes three Matlab routines:

* **random_states.m**
* **svd_approx_partition.m**
* **svd_approx_partitionLS.m**

## simulators

## solvers

## states

## users

## utilities

## MatLab files
`random_states.m` performs the latin-hypercube sampling for a givin matrix and desired samples per row/column.

`svd_approx_partition` is the routine that computes the sparse low rank approximation for L1 objective function.

`svd_approx_partitionLS` is the routine that computes the sparse low rank approximation for L2 objective function.
