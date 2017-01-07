package audio;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import javafx.stage.Stage;

/**
 * Learn: 
 * 	- Analyzes the audio read from input WAV.
 *  - Time is not of consideration in this.
 * @author Kannu
 *
 */
public class DiscreteFourierTransform {
	List<Double> amplitudeValues;
	Stage primaryStage;
	AudioFormat fmt;
	
	public DiscreteFourierTransform(List<Double> amplitudeValues, Stage primaryStage) throws IOException, Error{
		this.amplitudeValues = amplitudeValues;
		this.primaryStage = primaryStage;
		checkDatabaseExistence();
	}
	
	/**
	 * <b><u><h1>Perform the DFT.</h1></u></b>
	 * <br>
	 * <b>Variables/notation:</b>
	 * <br>
	 * N: number total samples		<br>
	 * n: number at sample			<br>
	 * Xk: kth freq bin				<br>
	 * xn: nth sample value			<br>
	 * j: imaginary number			<br>
	 * fr: freq res.				<br>
	 * 		fr(n): fr @ sample n = n * fs/N			<br>
	 * fs: sampling frequency		<br>
	 * 		fs = sampleSize/60		<br>
	 * nyqLim: fs/2					<br>
	 * mag: magnitude of vector ( = amplitude )		<br>
	 * ph: phase change				<br>
	 * 
	 * bn:  (2pikn)/n
	 * k: frequency
	 * xk: frequency at signal k
	 * 
	 * in the future: do the dft for multiple channels
	 * 
	 * @param amplitudeValues list with amplitudes at every sample, from 0 to N.
	 * 
	 */
	public double compute(List<Double> amplitudeValues){
		
		int sampleSize = amplitudeValues.size();
		
		double fs = fmt.getSampleRate();
		
		double[] realFrequency = new double[4500]; //frequency at sample
		
		double[] imaginaryFrequency = new double[4500]; //frequency at sample
		
		double[] frequencyResolution = new double[sampleSize + 1]; //fr @ sample n = n * fs/N
		
		double[] magnitude = new double[4500]; //(int) Math.ceil(sampleSize/2f)
		
		double xn = -1f; //amplitude at n
		
		double bn = -1f; //(2 * pi * k) / sampSize
		
		//get signal level for each frequency for each sample
		for (int sampleNumber = 0; sampleNumber <= sampleSize; sampleNumber++) {
			
			for (int k = 0; k < 4500; k++) {
				double realSum = 0;
				double imagSum = 0;
				for (int n = 0; n < sampleSize; n++) {
					//summation
					xn = amplitudeValues.get(n);
					bn = ( 2 * Math.PI * k )/sampleSize;
					realSum += (xn * Math.cos(bn));
					imagSum += ( (xn * Math.sin(bn)) );
					
				}
				
				//signal frequency
				realFrequency[k] = realSum;
				imaginaryFrequency[k] = imagSum;
				
			}
			
			//calculate magnitude for each signal frequency
			for (int i = 0; i < 4500; i++) { // (sampleSize/2 - 1)
				
				magnitude[i] = Math.sqrt(
						realFrequency[i]*realFrequency[i] + imaginaryFrequency[i]*imaginaryFrequency[i]
					);
				
			}
			
			//find peak magnitude and peak index for each magnitude
			double maxMagnitude = magnitude[0];
			int maxMagnitudeIndex = -1;
			
			for (int i = 0; i < magnitude.length; i++) {
				if (magnitude[i] >= maxMagnitude) {
					maxMagnitude = magnitude[i];
					maxMagnitudeIndex = i;
				}
			}
			
			
			//fr @ sample n = n * fs/N
			frequencyResolution[sampleNumber] = maxMagnitudeIndex * (fs/(sampleSize));
		}
		
		
		
		return calculateAverage(frequencyResolution);
		
	}

	private double calculateAverage(double[] frequencyResolution) {
		// TODO Auto-generated method stub
		double sum = 0f;
		for (double i : frequencyResolution) {
			sum += i;
		}
		return sum/frequencyResolution.length;
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

	public void setFormat(AudioFormat format) {
		// TODO Auto-generated method stub
		fmt = format;
	}
	
}
