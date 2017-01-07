package audio;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatabaseManager {
	final static int FILE_EXISTS_EMPTY = 0;
	final static int FILE_DNE = 1;
	final static int FILE_EXISTS_POPULATED = 2;
	
	public final static int THRESHOLD_WEAK = 20;
	public final static int THRESHOLD_NORMAL = 10;
	public final static int THRESHOLD_STRICT = 5;
	
	/**
	 * Check if a file exists, but is empty, if a file does not exists, or if a file exists and is populated.
	 * @return FILE_EXISTS_EMPTY, FILE_DNE, or FILE_EXISTS_POPULATED
	 * @throws IOException 
	 */
	
	public static int checkFile(String databaseName) throws IOException{
		final String path = "src\\audio\\" + databaseName;
		File database = new File(path);
		if (!database.exists()) {
			return FILE_DNE;
		} else {
			//Check if first line is empty
			BufferedReader br = new BufferedReader(new FileReader(path));
			if (br.readLine() == null) {
				br.close();
				return FILE_EXISTS_EMPTY;
			} else {
				br.close();
				return FILE_EXISTS_POPULATED;
			}
		}
	}
	
	/**
	 * 
	 * @param name name of file
	 * @throws IOException
	 */

	public static void makeNewDatabase(String name) throws IOException {
		File file = new File("src\\audio\\" + name);
		if (file.createNewFile()) {
			System.out.println("New database created: " + file.getName());
		} else {
			throw new Error("Database with name " + file.getName() + " already exists.");
		}
	}
	
	/**
	 * 
	 * @param string database name
	 * @throws IOException if db not found
	 */

	public static void makeDBByTemplate(String string) throws IOException {
		
		String initContent = "MONITOR: DATABASE " + string + "; CREATED " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
		
		File empDB = new File("src\\audio\\" + string);
		BufferedWriter bw = new BufferedWriter(new FileWriter(empDB.getAbsoluteFile()));
		bw.write(initContent);
		bw.close();
		
		System.out.println("Added line " + initContent);
	}
	
	/**
	 * @param databaseName name of the database
	 * @param data data on a new line
	 */
	
	public static void newData(String databaseName, String data) {
		try {
			Files.write(Paths.get("src\\audio\\" + databaseName), ("\n" + data).getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			System.out.println("Database not found!");
			e.printStackTrace();
		}
	}
	
	/**
	 * database name is default to "call-information.db"
	 * data must be in the following form: 
	 * 
	 * 	PHONE_NUMBER : DATA_1 :: DATA_2 :: DATA_3 :: (and so on)
	 * 
	 * Types of data: 1) Frequency. 2) Tone. 3+) Other data information 
	 * 
	 * Data greater than or equal to 4 must be in the form [DATA_NAME] = DATA_VALUE :: [DATA_NAME] = DATA_VALUE :: [DATA_NAME] = DATA_VALUE
	 * Data 1 and 2 do not need special formatting 
	 * 
	 * @param data data on a new line
	 */
	
	public static void newData(String data) {
		try {
			Files.write(Paths.get("src\\audio\\" + "call-information.db"), ("\n" + data).getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			System.out.println("Default database not found!");
			e.printStackTrace();
		}
	}
	
	/**
	 * Determine closest match from database, using data mining.
	 * @param frequency The input frequency in Hz.
	 * @param threshold The threshold for the input to be compared against.
	 * @return the value of the results of data mining.
	 * @throws IOException
	 */
	
	public static String[] determineClosestMatch(double frequency, int threshold) throws IOException {
		String[] returnValue = new String[2];
		final String path = "src\\audio\\" + "call-information.db";
		BufferedReader br = new BufferedReader(new FileReader(path));
		String line;
		String initSplit[];
		String currentPhoneNumber;
		String[] currentData = new String[100];
		
		List<String> possibleValues = new ArrayList<>();
		List<Double> corresPossibleValues = new ArrayList<>();
		int lineCount = 0;
		
		while ((line = br.readLine()) != null) {
			lineCount++;
			if (lineCount > 1) {
				initSplit = line.split(":");
				currentPhoneNumber = initSplit[0];
				currentData = String.valueOf(initSplit[1]).split("::");
				//currentData[0] = frequency
				if (Math.abs(Double.valueOf(currentData[0]) - frequency) <= threshold) {
					//This is a close value.
					possibleValues.add(currentPhoneNumber);
					corresPossibleValues.add(Double.valueOf(currentData[0]));
				}
			} else {
				System.out.println("skipping first line");
			}
		}
		
		br.close();
		
		//find smallest of the possible values. (smallest threshold)
		if (possibleValues.size() == 1) {
			returnValue[0] = possibleValues.get(0);
			returnValue[1] = String.valueOf((Math.abs(Double.valueOf(currentData[0]))/frequency)*100); 
			// frequency); //confidence is (frequency gathered from data) / (actual frequency) as a percent, then converted to a string.
			return returnValue;
		} else if (possibleValues.size() > 1) {
			int bestValueIndex = 0;
			double smallestThreshold = Double.MAX_VALUE;
			for (int i = 0; i < possibleValues.size(); i++) {
				double currentThreshold = Math.abs(Double.valueOf(corresPossibleValues.get(i)) - frequency);
				if (currentThreshold < smallestThreshold) {
					
					//we got a new winner!
					smallestThreshold = currentThreshold;
					bestValueIndex = i;
					
				}
			}
			returnValue[0] = possibleValues.get(bestValueIndex);
			
			double past = corresPossibleValues.get(bestValueIndex);
			double cur = frequency;
			
			if (cur < past) {
				returnValue[1] = String.valueOf((cur/past) * 100);
			} else if (cur > past) {
				returnValue[1] = String.valueOf(((2 - (cur/past)) * 100));
			}
			
			return returnValue;
		} else {
			returnValue[0] = "No match! Call may be spoofed.";
			returnValue[1] = "--";
			return returnValue;
		}
		
		
	}
}
