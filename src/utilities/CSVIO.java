package utilities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class CSVIO {
	
	// for float matrix
	public static float[][] read2DFloatArray(String fileName, int numRow, int numCol) {
		if (!fileName.endsWith("csv"))
			fileName = fileName + ".csv";
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		float [][] output = new float[numRow][numCol];
		int i = 0;
		try {
			br = new BufferedReader(new FileReader(fileName));
			while ((line = br.readLine()) != null && i < numRow) {
				// use comma as separator
				String [] input = line.split(cvsSplitBy);
				for (int j = 0; j < numCol; j++) {
					output[i][j] = Float.parseFloat(input[j]);
				}
				i++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		if (i != numRow) 
			System.out.println("Input Dimensions do not match! SIZE =" + i);
		System.out.println(fileName + " read correctly.");
		return output;
	}
	
	// for float matrix
	public static void write2DFloatArray(String fileName, float[][] VF) {
		try {
			if (!fileName.endsWith("csv"))
				fileName = fileName + ".csv";
			BufferedWriter br = new BufferedWriter(new FileWriter(fileName));
			for (int i = 0; i < VF.length; i++) {
				StringBuilder sb = new StringBuilder();
				for (int j = 0; j < VF[i].length; j++) {
					sb.append(VF[i][j]);
					if (j < VF[i].length - 1)
						sb.append(",");
				}
				sb.append("\n");
				br.write(sb.toString());
			}
			br.close();
			System.out.println(fileName + " written successfully");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// For int array
	public static int[] read1DIntArray (String fileName) {
		int[] days = null;
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		try {
			br = new BufferedReader(new FileReader(fileName));
			while ((line = br.readLine()) != null) {
				// use comma as separator
				String[] input = line.split(cvsSplitBy);
				days = new int[input.length];
				for (int i = 0; i < input.length; i++) {
					days[i] = Integer.parseInt(input[i]);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println(fileName + " read correctly.");
		return days;
	}
	
	// For float array
	public static float[] read1DFloatArray (String fileName) {
		float[] output = null;
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		try {
			br = new BufferedReader(new FileReader(fileName));
			while ((line = br.readLine()) != null) {
				// use comma as separator
				String[] input = line.split(cvsSplitBy);
				output = new float[input.length];
				for (int i = 0; i < input.length; i++) {
					output[i] = Float.parseFloat(input[i]);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println(fileName + " read correctly.");
		return output;
	}
	
	// for int array
	public static void write1DIntArray (String fileName, int[] output) {
		try {
			if (!fileName.endsWith("csv"))
				fileName = fileName + ".csv";
			BufferedWriter br = new BufferedWriter(new FileWriter(fileName));
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < output.length; i++) {
				sb.append(output[i]);
				if (i < output.length - 1)
					sb.append(",");
			}
			sb.append("\n");
			br.write(sb.toString());
			br.close();
			System.out.println(fileName + " written successfully");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// for int matrix
	public static int[][] read2DIntArray(String fileName, int numRow, int numCol) {
		if (!fileName.endsWith("csv"))
			fileName = fileName + ".csv";
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		int [][] output = new int[numRow][numCol];
		int i = 0;
		try {
			br = new BufferedReader(new FileReader(fileName));
			while ((line = br.readLine()) != null && i < numRow) {
				// use comma as separator
				String [] input = line.split(cvsSplitBy);
				for (int j = 0; j < numCol; j++) {
					output[i][j] = Integer.parseInt(input[j]);
				}
				i++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		if (i != numRow) 
			System.out.println("Input Dimensions do not match! SIZE =" + i);
		System.out.println(fileName + " read correctly.");
		return output;
	}
	
	// for int matrix
	public static void write2DIntArray(String fileName, int[][] output) {
		try {
			if (!fileName.endsWith("csv"))
				fileName = fileName + ".csv";
			BufferedWriter br = new BufferedWriter(new FileWriter(fileName));
			for (int i = 0; i < output.length; i++) {
				StringBuilder sb = new StringBuilder();
				for (int j = 0; j < output[i].length; j++) {
					sb.append(output[i][j]);
					if (j < output[i].length - 1)
						sb.append(",");
				}
				sb.append("\n");
				br.write(sb.toString());
			}
			br.close();
			System.out.println(fileName + " written successfully");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}

