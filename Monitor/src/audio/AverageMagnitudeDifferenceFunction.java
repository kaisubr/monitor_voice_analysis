package audio;

import java.io.IOException;
import java.util.List;

import javax.sound.sampled.AudioFormat;

import io.github.kaisubr.Main;
import javafx.scene.Node;
import javafx.stage.Stage;

public class AverageMagnitudeDifferenceFunction {
	List<Double> amplitudeValues;
	Stage primaryStage;
	AudioFormat fmt;
	
	public AverageMagnitudeDifferenceFunction(List<Double> amplitudeValues, Stage primaryStage) throws IOException, Error{
		this.amplitudeValues = amplitudeValues;
		this.primaryStage = primaryStage;
		
		checkDatabaseExistence();
	}
	
	/**
	 * Compute the AMDF.
	 * @param amplitudeSamples a list of amplitudes at samples (n).
	 * @param bufferArray
	 * @param bytesRead
	 * @return
	 */
	public double compute(List<Double> amplitudeSamples, byte[] bufferArray, int bytesRead) {
		//compute AMDF
		
		double[] res = new double[bufferArray.length/2];
		int length = res.length / 2;
		
		for ( int i = 0; i < bytesRead; i+=2 ) {
            // convert two bytes into single value
            int value = (short)((bufferArray[i]&0xFF) | ((bufferArray[i+1]&0xFF) << 8));
            res[i >> 1] = value;
        }
		
		
		double previousSum = 0;
		double previousxChnge = 0;
		double maxSum = 0;
		int sampleLength = 0;
		
		for (int i = 0; i < length; i++) {
			double sum = 0;
			//"summation" (technically, it is a "subtraction")
			for (int n = 0; n < length; n++) {
				sum += Math.abs(res[n] - res[i+n]);
			}
			
			double xChnge = previousSum - sum;
			if (xChnge < 0 && previousxChnge > 0) {
				//dx changes signs
				if (sampleLength == 0) {
					sampleLength = i - 1;
					
				}
			}
			
			previousxChnge = xChnge;
			previousSum = sum;
			maxSum = Math.max(sum, maxSum); //get maximum from last 2 runs
		}
		
		double freq = normalizeFrequency(fmt.getSampleRate()/sampleLength);
		
		Main.getLearnList().add(freq);
		
		return freq;
	}
	
	private static double normalizeFrequency(double hz) {
        while ( hz < 82.41 ) {
            hz = 2*hz;
        }
        while ( hz > 164.81 ) {
            hz = 0.5*hz;
        }
        return hz;
    }
	
	/**
	 * Check whether the database exists and act accordingly.
	 * @throws IOException
	 * @throws Error
	 */
	private void checkDatabaseExistence() throws IOException, Error {
		// TODO Auto-generated method stub
		//Check if database exists
		switch (DatabaseManager.checkFile("call-information.db")) {
			case (DatabaseManager.FILE_DNE): {
				System.out.println("Database does not exist");
				DatabaseManager.makeNewDatabase("call-information.db"); //it will be empty
				break;
			}
			
			case (DatabaseManager.FILE_EXISTS_EMPTY): {
				System.out.println("Database exists but is empty");
				DatabaseManager.makeDBByTemplate("call-information.db");
				break;
			}
			
			case (DatabaseManager.FILE_EXISTS_POPULATED): {
				System.out.println("Database exists and has text");
				break;
			}
			
			default: {
				throw new Error(
						"DatabaseManager.checkFile(database) returned a number other than "
						+ "FILE_DNE, FILE_EXISTS_EMPTY, and FILE_EXISTS_POPULATED"
						);
			}
		}
	}
	
	/**
	 * Set format, used to find sampling rate.
	 * @param format
	 */

	public void setFormat(AudioFormat format) {
		// TODO Auto-generated method stub
		fmt = format;
	}
	
	private Node getNodeById(String string) {
		// TODO Auto-generated method stub
		return primaryStage.getScene().lookup("#" + string);
	}
}
