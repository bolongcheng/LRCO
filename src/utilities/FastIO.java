package utilities;

import java.io.*;
import java.nio.*;
import java.nio.channels.FileChannel;

/**
 * This class is written for writing and reading arrays in binary format. Uses this java.nio API to
 * open channels and allocate ByteBuffer the size of a single row of the data type. This
 * implementation is about 3-5 times faster than the CSVIO implementation on average. Files are
 * stored in .dat format and follows the Java convention for space allocation: e.g. 4bytes for
 * single-precision float
 * 
 * @author bcheng
 *
 */
public class FastIO {

	public static void write2DFloatArray(String fileName, float[][] arr) {

		File file = new File(fileName);
		try {
			for (int i = 0; i < arr.length; i++) {
				ByteBuffer buffer = createBuffer(arr[i]);
				fasterToFile(file, buffer, i != 0);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(fileName + " written correctly.");
	}

	public static float[][] read2DFloatArray(String fileName, int row, int col) {
		float[][] output = new float[row][col];
		FileInputStream f;
		try {
			f = new FileInputStream(fileName);
			FileChannel ch = f.getChannel();
			ByteBuffer buffer = ByteBuffer.allocate(4 * output[0].length);
			int nRead;
			int i = 0;
			int j = 0;
			try {
				while ((nRead = ch.read(buffer)) != -1 && i < row) {
					if (j == col) {
						j = 0;
						i++;
					}
					if (nRead == 0)
						continue;
					buffer.position(0);
					buffer.limit(nRead);
					while (buffer.hasRemaining()) {
						output[i][j] = buffer.getFloat();
						j++;
					}
					// i++;
					buffer.clear();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		System.out.println(fileName + " read correctly.");
		return output;
	}

	private static void fasterToFile(File file, ByteBuffer buffer, boolean append) throws IOException {
		FileChannel fc = null;
		try {
			fc = new FileOutputStream(file, append).getChannel();
			fc.write(buffer);
		} finally {
			if (fc != null)
				fc.close();
			buffer.rewind();
		}
	}

	private static ByteBuffer createBuffer(float[] arr) {
		ByteBuffer buffer = ByteBuffer.allocate(4 * arr.length);
		int i = 0;
		while (buffer.hasRemaining()) {
			buffer.putFloat(arr[i]);
			i++;
		}
		buffer.rewind();
		return buffer;
	}

}