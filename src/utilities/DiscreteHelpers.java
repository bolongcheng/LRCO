package utilities;

import java.util.Arrays;

public class DiscreteHelpers {
    /**
     * Return the index of an element of the array. Use the floor operation for approximate values.
     *
     * @param value
     * @param array
     * @return
     */
    public static int getIndex(float value, float[] array) {
        // int i = 0;
        float smallDiff = (array[1] - array[0]) / 2;
        if (value <= array[0])
            return 0;
        if (value >= array[array.length - 1])
            return array.length - 1;
        int temp = Arrays.binarySearch(array, value + smallDiff);
        return temp < 0 ? -temp - 2 : temp;
    }

    public static int getRevIndex(float value, float[] array) {
        int i = 0;
        float smallDiff = (array[1] - array[0]) / 2;
        while (i < array.length && array[i] >= value + smallDiff)
            i++;
        return i > 0 ? i - 1 : 0;

    }

    /**
     * Convolution of two arrays.
     *
     * @param leftside
     * @param rightside
     * @return
     */
    public static float[] convolute(float[] leftside, float[] rightside) {
        /*
         * we assume there is one overlapping of leftside [-3, -2, -1 , 0] and rightside [0, 1, 2]
		 * the output length = leftside.length + rightside.length - 1.
		 */
        float[] output = new float[leftside.length + rightside.length - 1];
        for (int i = 0; i < output.length; i++) {
            float sum = 0;
            for (int j = 0; j < leftside.length; j++) {
                if (i - j >= 0 && i - j < rightside.length)
                    sum += leftside[j] * rightside[i - j];
            }
            output[i] = sum;
        }

        return output;

    }

    /**
     * Generate a random variable of from the distribution of pdf. urv is a uniform random variable.
     *
     * @param urv
     * @param pdf
     * @return
     */
    public static int getProb(float urv, float[] pdf) {
        int i = 0;
        while (i < pdf.length) {
            urv -= pdf[i];
            if (urv <= 0)
                break;
            i++;
        }
        return Math.min(i, pdf.length - 1);

    }

    // /**
    // * The List version of the previous method.
    // *
    // * @param urv
    // * @param pdf
    // * @return
    // */
    // public static int getProb(float urv, List<Float> pdf) {
    // int i = 0;
    // while (i < pdf.size()) {
    // urv -= pdf.get(i);
    // if (urv <= 0)
    // break;
    // i++;
    // }
    // return Math.min(i, pdf.size() - 1);
    //
    // }

    /**
     * Bilinear Interpolation of a matrix of m x n dimension to rlength x glength dimension. where
     * rlength is an integral multiple of m and glength is an integral multiple of n.
     *
     * @param vFTerm
     * @param rlength
     * @param glength
     * @return
     */
    public static float[][] interpolate(float[][] vFTerm, int rlength, int glength) {
        // NOTE make sure
        float[][] newMatrix = new float[rlength][glength];
        int xmultiple = 1;
        int ymultiple = 1;

        boolean bigEven = false;
        boolean smallEven = false;
        // copy the original gridlines
        if (rlength % 2 == 0) {
            bigEven = true;
        }
        if (vFTerm.length % 2 == 0) {
            smallEven = true;
        }

        if (!(bigEven ^ smallEven)) {
            xmultiple = (rlength - 1) / (vFTerm.length - 1);
            ymultiple = (glength - 1) / (vFTerm[0].length - 1);
        } else if (bigEven && !smallEven) {
            xmultiple = rlength / (vFTerm.length - 1);
            ymultiple = glength / (vFTerm[0].length - 1);
        } else {
            xmultiple = rlength / vFTerm.length;
            ymultiple = glength / vFTerm[0].length;
        }

        for (int i = 0; i < vFTerm.length; i++) {
            for (int j = 0; j < vFTerm[0].length; j++) {
                if (i * xmultiple < rlength - 1 && j * ymultiple < glength - 1)
                    newMatrix[i * xmultiple][j * ymultiple] = vFTerm[i][j];
            }
        }

        // interpolate the y - edges
        for (int i = 0; i < vFTerm.length; i++) {
            for (int j = 0; j < vFTerm[0].length - 1; j++) {
                for (int k = 1; k < ymultiple; k++) {
                    if (i * xmultiple < rlength - 1)
                        newMatrix[i * xmultiple][j * ymultiple + k] = ((float) (ymultiple - k)) / ymultiple
                                * vFTerm[i][j] + ((float) k) / ymultiple * vFTerm[i][j + 1];
                }
            }
        }
        float[] temp = null;
        if (bigEven && !smallEven) {
            temp = new float[glength];
            for (int j = 0; j < vFTerm[0].length - 1; j++) {
                for (int k = 0; k < ymultiple; k++) {
                    temp[j * ymultiple + k] = ((float) (ymultiple - k)) / ymultiple * vFTerm[vFTerm.length - 1][j]
                            + ((float) k) / ymultiple * vFTerm[vFTerm.length - 1][j + 1];
                }
            }

        }
        // interpolate the x direction
        for (int j = 0; j < glength; j++) {
            for (int i = 0; i < vFTerm.length - 1; i++) {
                for (int k = 1; k < xmultiple; k++) {
                    if (i < vFTerm.length - 2)
                        newMatrix[i * xmultiple + k][j] = (float) (xmultiple - k) / xmultiple
                                * newMatrix[i * xmultiple][j]
                                + (float) k / xmultiple * newMatrix[(i + 1) * xmultiple][j];
                    else
                        newMatrix[i * xmultiple + k][j] = (float) (xmultiple - k) / xmultiple
                                * newMatrix[i * xmultiple][j] + (float) k / xmultiple * temp[j];
                }
            }
        }

        // extrapolate
        if (bigEven && smallEven) {
            for (int i = 0; i < rlength - 1; i++) {
                float diff = newMatrix[i][(vFTerm[0].length - 1) * ymultiple]
                        - newMatrix[i][(vFTerm[0].length - 1) * ymultiple - 1];
                for (int k = 1; k < ymultiple; k++)
                    newMatrix[i][(vFTerm[0].length - 1) * ymultiple + k] = k * diff
                            + newMatrix[i][(vFTerm[0].length - 1) * ymultiple];
            }
            for (int j = 0; j < glength; j++) {
                float diff = newMatrix[(vFTerm.length - 1) * xmultiple][j]
                        - newMatrix[(vFTerm.length - 1) * xmultiple - 1][j];
                for (int k = 1; k < xmultiple; k++)
                    newMatrix[(vFTerm.length - 1) * xmultiple + k][j] = k * diff
                            + newMatrix[(vFTerm.length - 1) * xmultiple][j];
            }
        }
        return newMatrix;
    }

    /**
     * Get the column-wise sub-array of a two dimensional array.
     *
     * @param input
     * @param startCol starting column index
     * @param endCol   ending column index (non-inclusive)
     * @return
     */
    public static float[][] getSubArrayByColumn(float[][] input, int startCol, int endCol) {
        float[][] output = new float[input.length][endCol - startCol];
        for (int i = 0; i < input.length; i++) {
            output[i] = Arrays.copyOfRange(input[i], startCol, endCol);
        }
        return output;
    }

}
