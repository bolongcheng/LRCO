package utilities;

import utilities.Parameter;

/**
 * This class is fore storing the sparse low rank approximations for a single time step. Each rank-1
 * approximation is stored as x-vector, y-vector and a shift value.
 * 
 * @author bcheng
 *
 */

public class VFApprox {
	private Parameter param;
	// first two dimensions are indices of sub-matrix, last dimension contains
	// the decision variables
	private double[][][] x;
	private double[][][] y;
	private double[][] shift;
	// sub_matrices info
	private int rowPart; // how many sub-matrices from top to bottom
	private int colPart; // how many sub-matrices from left to right
	private int sizeRow; // row size of sub-matrices
	private int sizeCol; // column size of sub-matrices
//	private int num_subs; // number of sub-matrices

	public VFApprox(Parameter param_) {
		param = param_;
		rowPart = param.getRowPart();
		colPart = param.getColPart();
		sizeRow = param.getSizeRow();
		sizeCol = param.getSizeCol();
//		num_subs = param.get_num_subs();

		// rowPart and colPart indices are the indices of the sub-matrix.
		// There is one shift for each sub-matrix
		x = new double[sizeRow][rowPart][colPart];
		y = new double[sizeCol][rowPart][colPart];
		shift = new double[rowPart][colPart];
	}

	public void setXVector(double[][][] x_) {
		x = x_;
	}

	public void setXVector(float[] x_in) {
		int l = 0;
		for (int i = 0; i < x.length; i++) {
			for (int j = 0; j < x[i].length; j++) {
				for (int k = 0; k < x[i][j].length; k++) {
					x[i][j][k] = x_in[l];
					l++;
				}
			}
		}

	}

	public void setYVector(double[][][] y_) {
		y = y_;
	}

	public void setYVector(float[] y_in) {
		int l = 0;
		for (int i = 0; i < y.length; i++) {
			for (int j = 0; j < y[i].length; j++) {
				for (int k = 0; k < y[i][j].length; k++) {
					y[i][j][k] = y_in[l];
					l++;
				}
			}
		}

	}

	public void setShift(double[][] shift_) {
		shift = shift_;
	}

	public void setShift(float[] shift_in) {
		int l = 0;
		for (int i = 0; i < shift.length; i++) {
			for (int j = 0; j < shift[i].length; j++) {
				shift[i][j] = shift_in[l];
				l++;
			}
		}

	}

	public float[] getShift1D() {
		float[] output = new float[shift.length * shift[0].length];
		int l = 0;
		for (int i = 0; i < shift.length; i++) {
			for (int j = 0; j < shift[i].length; j++) {
				output[l] = (float) shift[i][j];
				l++;
			}
		}
		return output;
	}

	public double[][][] getXVector() {
		return x;
	}

	public float[] getXVector1D() {
		float[] output = new float[x.length * x[0].length * x[0][0].length];
		int l = 0;
		for (int i = 0; i < x.length; i++) {
			for (int j = 0; j < x[i].length; j++) {
				for (int k = 0; k < x[i][j].length; k++) {
					output[l] = (float) x[i][j][k];
					l++;
				}
			}
		}
		return output;
	}

	public double[][][] getYVector() {
		return y;
	}

	public float[] getYVector1D() {
		float[] output = new float[y.length * y[0].length * y[0][0].length];
		int l = 0;
		for (int i = 0; i < y.length; i++) {
			for (int j = 0; j < y[i].length; j++) {
				for (int k = 0; k < y[i][j].length; k++) {
					output[l] = (float) y[i][j][k];
					l++;
				}
			}
		}
		return output;
	}

	public double getVApprox(int R, int G, int D) {

		// int rowIndex = 0;
		// int colIndex = 0;
		int rowIndex = R / sizeRow;
		int subR = R % sizeRow;
		// System.out.println(rowIndex + "_" + subR );
		// if (R > 1)
		// rowIndex = (int) Math.ceil((double) R / sizeRow) - 1;
		// else if (R == 0)
		// rowIndex = 0;

		int GDInex = G + D * (param.getGrange().length);
		int colIndex = GDInex / sizeCol;
		int subGD = GDInex % sizeCol;
		// System.out.println(GDInex + "_" + colIndex + "_" + subGD);
		// if (GDInex > 1)
		// colIndex = (int) Math.ceil((double) GDInex / sizeCol) - 1;
		// else if (GDInex == 0)
		// colIndex = 0;

		double V_approx = x[subR][rowIndex][colIndex] * y[subGD][rowIndex][colIndex] - shift[rowIndex][colIndex];
		return V_approx;
	}

}
