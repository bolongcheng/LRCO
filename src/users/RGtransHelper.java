package users;

import java.util.Hashtable;

import utilities.CSVIO;

public class RGtransHelper {
	private Hashtable<String, int[][]> RGtransState;
	private Hashtable<String, float[]> RGtransProb;
	
	public RGtransHelper(String probfile, String statefile, String indexfile) {
		float[] transMatrices = CSVIO.Read1DArray(probfile);
		int[][] transStates = CSVIO.read2DArray(statefile, transMatrices.length, 2);
		int[] indices = CSVIO.read1DArray(indexfile);
		int count = 0;
		
		RGtransState = new Hashtable<String, int[][]>();
		RGtransProb = new Hashtable<String, float[]>();
		for (int i = 0; i < indices.length; i+=2){
			int len = indices[i];
			String key = Integer.toString(indices[i+1]);
			float[] tempProb = new float[len];
			int[][] tempStates = new int[len][2];
			for (int k = count; k < count+len; k++){
				tempProb[k-count] = transMatrices[k];
				tempStates[k-count][0] = transStates[k][0];
				tempStates[k-count][1] = transStates[k][1];
			}
			RGtransState.put(key, tempStates);
			RGtransProb.put(key, tempProb);
			count+=len;
		}
		
	}

	public Hashtable<String, int[][]> getRGtransState() {
		return RGtransState;
	}

	public Hashtable<String, float[]> getRGtransProb() {
		return RGtransProb;
	}
}
