package utilities;

public class IOHelpers {
	/**
	 * Convert a 3-dimensional matrix to 2-dimensional matrix (float), by flattening out the first
	 * two dimensions.
	 * 
	 * @param input
	 * @return
	 */
	public static float[][] convert3Dto2D(float[][][] input) {
		float[][] output = new float[input.length * input[0].length][input[0][0].length];

		for (int i = 0; i < input.length; i++) {
			for (int j = 0; j < input[0].length; j++) {
				output[i * input[0].length + j] = input[i][j];
			}
		}
		return output;
	}

	public static float[][] convert1Dto2D(float[] input, int firstDimLength, int secDimLength) {
		int k = 0;
		float[][] output = new float[firstDimLength][secDimLength];
		for (int i = 0; i < firstDimLength; i++){
			for (int j = 0; j < secDimLength; j++){
				output[i][j] = input[k];
				k++;
			}
		}
		return output;
	}

}
