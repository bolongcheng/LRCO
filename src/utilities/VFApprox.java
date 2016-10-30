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
	private int row_part; // how many sub-matrices from top to bottom
	private int col_part; // how many sub-matrices from left to right
	private int size_row; // row size of sub-matrices
	private int size_col; // column size of sub-matrices
	private int num_subs; // number of sub-matrices

	public VFApprox(Parameter param_) {
		param = param_;
		row_part = param.get_row_part();
		col_part = param.get_col_part();
		size_row = param.get_size_row();
		size_col = param.get_size_col();
		num_subs = param.get_num_subs();

		// row_part and col_part indices are the indices of the sub-matrix.
		// There is one shift for each sub-matrix
		x = new double[size_row][row_part][col_part];
		y = new double[size_col][row_part][col_part];
		shift = new double[row_part][col_part];
	}

	public void set_x_vector(double[][][] x_) {
		x = x_;
	}

	public void set_x_vector(float[] x_in) {
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

	public void set_y_vector(double[][][] y_) {
		y = y_;
	}

	public void set_y_vector(float[] y_in) {
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

	public void set_shift(double[][] shift_) {
		shift = shift_;
	}

	public void set_shift(float[] shift_in) {
		int l = 0;
		for (int i = 0; i < shift.length; i++) {
			for (int j = 0; j < shift[i].length; j++) {
				shift[i][j] = shift_in[l];
				l++;
			}
		}

	}

	public float[] get_shift1d() {
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

	public double[][][] get_x_vector() {
		return x;
	}

	public float[] get_x_vector1d() {
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

	public double[][][] get_y_vector() {
		return y;
	}

	public float[] get_y_vector1d() {
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

	public double get_V_approx(int R, int G, int D) {

		// int row_idx = 0;
		// int col_idx = 0;
		int row_idx = R / size_row;
		int sub_R = R % size_row;
		// System.out.println(row_idx + "_" + sub_R );
		// if (R > 1)
		// row_idx = (int) Math.ceil((double) R / size_row) - 1;
		// else if (R == 0)
		// row_idx = 0;

		int g_d_idx = G + D * (param.getGrange().length);
		int col_idx = g_d_idx / size_col;
		int sub_gd = g_d_idx % size_col;
		// System.out.println(g_d_idx + "_" + col_idx + "_" + sub_gd);
		// if (g_d_idx > 1)
		// col_idx = (int) Math.ceil((double) g_d_idx / size_col) - 1;
		// else if (g_d_idx == 0)
		// col_idx = 0;

		double V_approx = x[sub_R][row_idx][col_idx] * y[sub_gd][row_idx][col_idx] - shift[row_idx][col_idx];
		return V_approx;
	}

}
