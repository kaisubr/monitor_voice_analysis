package audio;

import java.io.IOException;
import java.util.List;

import javax.sound.sampled.AudioFormat;

import javafx.stage.Stage;

public class Autocorrelation {
	List<Double> amplitudeValues;
	Stage primaryStage;
	AudioFormat fmt;
	
	public Autocorrelation(List<Double> amplitudeValues, Stage primaryStage) throws IOException, Error{
		this.amplitudeValues = amplitudeValues;
		this.primaryStage = primaryStage;
		
		checkDatabaseExistence();
	}
	
	public double compute(List<Double> amplitudeSamples) {
		int sampleSize = amplitudeSamples.size();
		double samplingRate = fmt.getSampleRate();
		double samplingFrequency = samplingRate;
		
		//set bounds
		int lowSamplePeriod = (int) (samplingRate/7902);
		int highSamplePeriod= (int) (samplingRate/20);
		double[] res = new double[highSamplePeriod - lowSamplePeriod];
		
		for (int period = lowSamplePeriod; period < highSamplePeriod; period++) {
			double sum = 0;
			//summation
			for (int i = 0; i <= sampleSize - period - 1; i++) {
				//multiply original and shifted signals (lag to peak)
				sum += amplitudeSamples.get(i) * amplitudeSamples.get(i + period);
			}
			double avg = sum / (double) sampleSize;
			
			res[period - lowSamplePeriod] = avg;
		}
		
		//determine highest value & index (time lag)
		double highestValue = Double.MIN_VALUE;
		double highestIndex = -1;
		
		for (int i = 0; i < res.length; i++) {
			if (res[i] > highestValue) {
				highestValue = res[i];
				highestIndex = i;
			}
		}
		
		//get freq
		double freq = samplingRate / (highestIndex + lowSamplePeriod);
		freq = normalizeFrequency(freq);
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

	public void setFormat(AudioFormat format) {
		// TODO Auto-generated method stub
		fmt = format;
	}
}
