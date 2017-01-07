package io.github.kaisubr;

import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import audio.LocationDeterminer;
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
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;

public class WordAnalysisGUI extends Application {
	Stage primaryStage;
	BorderPane rootLayout;
	
	Slider ampSlider;
	Slider freqSlider;
	TargetDataLine line;
	
	boolean stopCapture;
	Task<Double> captureAudio;
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		experimental("given", "2345675846");
		
		stopCapture = true;
		
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("Word Type Analysis");
		initRootLayout();
		
		final String[] phoneNumber = {"0000000000"};
		
		Button startMic = (Button) getNodeById("startLearn_button");
		TextField phoneNumber_TF = (TextField) getNodeById("numberLearn_input");
		Button stopMic = (Button) getNodeById("stopLearn_button");
		
		ampSlider = (Slider) getNodeById("amp_slider");
		freqSlider = (Slider) getNodeById("freq_slider");
		
		phoneNumber_TF.setOnKeyReleased(new EventHandler<KeyEvent>(){

			@Override
			public void handle(KeyEvent event) {
				// TODO Auto-generated method stub
				if (phoneNumber_TF.getLength() == 10) {
					startMic.setDisable(false);
					phoneNumber[0] = phoneNumber_TF.getText();
				} else {
					startMic.setDisable(true);
				}
			}
			
		});
		
		startMic.setOnMouseClicked(new EventHandler<MouseEvent>(){

			@Override
			public void handle(MouseEvent arg0) {
				// TODO Auto-generated method stub
				stopMic.setDisable(false);
				
				//Start mic recording now.
				try {
					wta();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			
		});
	}
	
	/**
	 * Experimental purposes
	 * @param spoken
	 * @param phoneNumber
	 * @throws Exception if area code is invalid
	 */
	private void experimental(String spoken, String phoneNumber) throws Exception {
		// TODO Auto-generated method stub
		//eg "Yo man, sup. Dis is da company where we ask stuff. Roly poly yo yo yo yo"
		List<String> spokenText = Arrays.asList(
				(spoken).replaceAll("\\W", " ").toLowerCase().split(" ")
				);
		System.out.println(spokenText.toString());
		LocationDeterminer dl = new LocationDeterminer(spokenText);
		System.out.println(dl.getBestMatch());
		System.out.println(dl.getScoreMatch());
		System.out.println(dl.getConfidence());
		System.out.println(dl.compareWithNumber(phoneNumber));
	}

	/**
	 * Begin word to text analysis (WTA) via microphone input.
	 * @throws IOException 
	 */
	
	private void wta() throws IOException {
		// TODO Auto-generated method stub
		stopCapture = false;

		List<String> spokenText = new ArrayList<String>();
		
		Configuration c = new Configuration();
		c.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
		c.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
		c.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");
		c.setSampleRate(8000);
		
		LiveSpeechRecognizer recognizer = new LiveSpeechRecognizer(c);
		recognizer.startRecognition(true); //true = clear cached data
		
		System.out.println("Started!");
		
		SpeechResult result;
		
		while (!stopCapture) {
			result = recognizer.getResult();
			spokenText.add(result.getHypothesis());
			System.out.println(result.getHypothesis());
			recognizer.stopRecognition();
			recognizer.startRecognition(false); //false = don't clear cached data
		}
		
		
		
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
            loader.setLocation(Main.class.getResource("views/WTA.fxml"));
            rootLayout = (BorderPane) loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            scene.getStylesheets().add("styles/WTA.css");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

}
