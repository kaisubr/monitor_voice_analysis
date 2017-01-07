package io.github.kaisubr;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import audio.AverageMagnitudeDifferenceFunction;
import audio.DatabaseManager;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class Main extends Application {

	Stage primaryStage;
	BorderPane rootLayout;
	TargetDataLine line;
	boolean stopCapture;
	Task<Double> captureAudio;
	static Label avgResults;
	Slider ampSlider;
	Slider freqSlider;
	AverageMagnitudeDifferenceFunction amdf;
	double globalFreq;
	static List<Double> frequencyArrayPerSample = new ArrayList<>();
	static List<Long> latencyTimesPerSample = new ArrayList<>();
	
	@Override
	public void start(Stage primaryStage) {
		
		stopCapture = true;
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("Voice Frequency Analysis");
		initRootLayout();
		
		
		final String[] phoneNumber = {"0000000000"};
		
		Button startLearnButton = (Button) getNodeById("startLearn_button");
		avgResults = (Label) getNodeById("results_label");
		TextField phoneNumber_TF = (TextField) getNodeById("numberLearn_input");
		Button stopLearnButton = (Button) getNodeById("stopLearn_button");
		
		Button startTestButton = (Button) getNodeById("startTest_button");
		Button stopTestButton = (Button) getNodeById("stopTest_button");
		Label predictionLabel = (Label) getNodeById("prediction_label");
		Label confidenceLabel = (Label) getNodeById("confidence_label");
		
		ampSlider = (Slider) getNodeById("amp_slider");
		freqSlider = (Slider) getNodeById("freq_slider");
		
		phoneNumber_TF.setOnKeyReleased(new EventHandler<KeyEvent>(){

			@Override
			public void handle(KeyEvent event) {
				// TODO Auto-generated method stub
				if (phoneNumber_TF.getLength() == 10) {
					startLearnButton.setDisable(false);
					phoneNumber[0] = phoneNumber_TF.getText();
				} else {
					startLearnButton.setDisable(true);
				}
			}
			
		});
		
		startLearnButton.setOnMouseClicked(new EventHandler<MouseEvent>(){

			@Override
			public void handle(MouseEvent arg0) {
				// TODO Auto-generated method stub
				stopLearnButton.setDisable(false);
				
				//Start mic recording now.
				startMicRecording("LEARN");
			}

			
		});
		
		stopLearnButton.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent arg0) {
				// TODO Auto-generated method stub
				//stop mic recording now
				stopCapture = true; captureAudio.cancel();
				ampSlider.valueProperty().unbind();
				freqSlider.valueProperty().unbind();
				avgResults.textProperty().unbind();
				avgResults.setText("Computing average...");
				double avgFreq = calculateAverage(frequencyArrayPerSample);
				avgResults.setText(String.valueOf(avgFreq) + " Hz");
				avgResults.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
				
				DatabaseManager.newData((String) phoneNumber[0] + " : " + avgFreq);
				
			}
			
		});
		
		startTestButton.setOnMouseClicked(new EventHandler<MouseEvent>(){

			@Override
			public void handle(MouseEvent arg0) {
				// TODO Auto-generated method stub
				//start mic recording
				stopTestButton.setDisable(false);
				startMicRecording("TEST");
			}
			
		});
		
		stopTestButton.setOnMouseClicked(new EventHandler<MouseEvent>(){

			@Override
			public void handle(MouseEvent event) {
				// TODO Auto-generated method stub
				stopCapture = true; captureAudio.cancel();
				ampSlider.valueProperty().unbind();
				freqSlider.valueProperty().unbind();
				double avgFreq = calculateAverage(frequencyArrayPerSample);
				try {
					String[] closestMatch = 
							DatabaseManager.determineClosestMatch(
									avgFreq, 
									DatabaseManager.THRESHOLD_NORMAL
							);
					predictionLabel.setText(closestMatch[0]);
					confidenceLabel.setText(closestMatch[1]);
					
					System.out.println("Average latency: (ms) " + calculateAverageLatency(latencyTimesPerSample));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
		});
		
		
	}

	/**
	 * Begin recording audio from microphone and act accordingly based on type input.
	 * @param type Can be "LEARN" or "TEST".
	 */
	private void startMicRecording(String type) {
		// TODO Auto-generated method stub
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Begin audio capture");
		alert.setHeaderText("Audio Capture");
		alert.setContentText("Press OK to begin audio capture. To simulate a call ending, press the \"Stop Recording\" button. Once the call has ended, the information will automatically be stored in the database.");
		alert.showAndWait();
		
		
		AudioFormat format = new AudioFormat(44100,16,1,true,false);
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
		
		if (!AudioSystem.isLineSupported(info)) {
		      System.out.println("line not supported");
		}
		
		// Obtain and open the line.
		try {
		    line = (TargetDataLine) AudioSystem.getLine(info);
		    line.open(format);
		} catch (LineUnavailableException ex) {
		    System.out.println("line unvailable"); 
		}
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		stopCapture = false;
		byte[] bufferArray = new byte[2400];
		List<Double> amplitudeSamples = new ArrayList<>();
		double[] freq = new double[1];
		
		try {
			amdf = new AverageMagnitudeDifferenceFunction(amplitudeSamples, primaryStage);
		} catch (IOException | Error e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		captureAudio = new Task<Double>() {

			@Override
			protected Double call() throws Exception {
				// TODO Auto-generated method stub
				
				line.start();
				
				while (!stopCapture) {
					
					if (isCancelled()) {
						break;
					}
					
					int bytesRead = -1;
					
					if ((bytesRead = line.read(bufferArray, 0, bufferArray.length)) > 0) {
						long startTime = System.currentTimeMillis();
						
						double rmsValue = calculateRootMeanSquaredLevel(
								//getLastElements(bufferArray, 100)
								bufferArray
						);
						
						updateValue(rmsValue);
						
						amplitudeSamples.add(
								rmsValue
							);
						
						amdf.setFormat(line.getFormat());
						freq[0] = (amdf.compute(amplitudeSamples, bufferArray, bytesRead));
						updateMessage(String.valueOf(freq[0]) + " Hz");
						updateProgress(freq[0], 3500);

						long endTime = System.currentTimeMillis();

						Main.getLatencyList().add(endTime - startTime);
						Thread.sleep(250);
					} else {
						System.out.println("false");
					}
					
					
					
				}
				
				line.close();
				
				return freq[0];
			}

			
		};
		
		ampSlider.valueProperty().bind(captureAudio.valueProperty());
		freqSlider.valueProperty().bind(captureAudio.workDoneProperty());
		if (type.equals("LEARN")) {
			avgResults.textProperty().bind(captureAudio.messageProperty());
		} else {
			//test
			globalFreq = freq[0];
		}
		
		new Thread(captureAudio).start();
	}
	
	/**
	 * Calculate average of list (with doubles)
	 * @param frequencyRes The list of frequency resolutions
	 * @return the average value.
	 */
	private double calculateAverage(List<Double> frequencyRes) {
		// TODO Auto-generated method stub
		double sum = 0f;
		for (double i : frequencyRes) {
			sum += i;
		}
		return sum/frequencyRes.size();
	}
	
	/**
	 * Calculate average of list (with longs)
	 * @param frequencyRes The list of frequency resolutions
	 * @return the average value.
	 */
	private long calculateAverageLatency(List<Long> latencyTimes) {
		// TODO Auto-generated method stub
		long sum = 0;
		for (long i : latencyTimes) {
			sum += i;
		}
		return sum/latencyTimes.size();
	}

	/**
	 * @param bufferArray the array
	 * @param i the last elements to return
	 * @return the last i elements of the array
	 */
	public byte[] getLastElements(byte[] bufferArray, int i) {
		// TODO Auto-generated method stub
		if (i >= bufferArray.length) {
			return bufferArray;
		} else {
			byte[] lastElements = new byte[i];
			for (int n = 0; n < i; n++) {
				lastElements[n] = bufferArray[bufferArray.length - 1 - n];
			}
			return lastElements;
		}
	}
	
	/**
	 * Calculate RMS.
	 * @param inputAudio a byte array
	 * @return the RMS for the byte array.
	 */

	protected Double calculateRootMeanSquaredLevel(byte[] inputAudio) {
		// TODO Auto-generated method stub
		
		int n = inputAudio.length;
		int sumOfSquares = 0;
		for (int i = 0; i < n; i++) {
			sumOfSquares += Math.pow(inputAudio[i], 2);
		}
		
		return Math.pow(sumOfSquares/n, 0.5);
	}

	/**
	 * Get node, given the id. A node is a type of element, like a Label or Button.
	 * @param string The ID used in the FXML file.
	 * @return the node
	 */
	private Node getNodeById(String string) {
		// TODO Auto-generated method stub
		return primaryStage.getScene().lookup("#" + string);
	}

	/**
	 * Initialize the root layout from the FXML file.
	 */
	private void initRootLayout() {
		// TODO Auto-generated method stub
		try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("views/Home.fxml"));
            rootLayout = (BorderPane) loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            scene.getStylesheets().add("styles/Home.css");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	public static void main(String[] args) {
		launch(args);
	}
	
	/**
	 * 
	 * @return frequency array per sample array. Used by AverageMagnitudeDifferenceFunction to add frequencies.
	 */
	public static List<Double> getLearnList(){
		return frequencyArrayPerSample;
	}
	
	/**
	 * 
	 * @return latency list. Used by AverageMagnitudeDifferenceFunction to add latencies.
	 */
	public static List<Long> getLatencyList() {
		return latencyTimesPerSample;
	}
	

}
