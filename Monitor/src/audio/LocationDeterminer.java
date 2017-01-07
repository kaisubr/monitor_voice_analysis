package audio;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

/**
 * Hypothesize the location of the call based on word usage and corresponding dialect of English.
 * @author Kannu
 *
 */
public class LocationDeterminer {
	List<String> spokenWords;
	String bestPossibleRegion;
	double scoreBPR;
	double confidence;
	
	double pacificNW;
	double pacificSW;
	double SW;
	double rockyMtn;
	double upperMW;
	double midland;
	double gulfSouth;
	double coastalSouth;
	double NE;
	
	
	
	public LocationDeterminer(List<String> spokenWords) {
		this.spokenWords = spokenWords;
		performAlgorithm();
	}

	/**
	 * Perform the algorithm.
	 * 
	 * Algorithm is based on multiple sources. It uses a point scoring system
	 * where each region is given a score based on the types of words used.
	 * Confidence is based on length of given spoken words and score for the
	 * hypothesized region. The hypothesized region is the region which ends
	 * in the largest score after the process finished. 
	 * 
	 * Result can be returned via getRegion(), and confidence
	 * can be returned via getConfidence().
	 * 
	 * This method does not return any values. It also should not be called
	 * outside this class. It is automatically called when the class is 
	 * constructed.
	 */
	private void performAlgorithm() {
		
		pacificNW = 0;
		pacificSW = 0;
		SW = 0;
		rockyMtn = 0;
		upperMW = 0;
		midland = 0;
		gulfSouth = 0;
		coastalSouth = 0;
		NE = 0;
		
		if (spokenWords.contains("soda")) {
			pacificSW++;
			midland++;
			NE++;
			
		} 
		
		if (spokenWords.contains("pop")) {
			rockyMtn++;
			upperMW++;
			midland++;
		} 
		
		if (spokenWords.contains("coke")) {
			midland += 0.5;
			gulfSouth++;
		}
		
		if (spokenWords.contains("you") && spokenWords.contains("guys")) {
			pacificNW += 0.5;
			pacificSW += 0.5;
			SW += 0.5;
			rockyMtn += 0.5;
			midland += 0.5;
			NE += 0.5;
		} 
		
		if ((spokenWords.contains("you") && spokenWords.contains("all")) ||
				spokenWords.contains("y'all")) {
			gulfSouth += 2;
			coastalSouth += 2;
		}
		
		/**
		 * The following tests contains very strong distinguishing factors, so
		 * corresponding scores are increased at a much higher amount (2, 3, 4...) 
		 */
		
		if ((spokenWords.contains("belly") && spokenWords.contains("sinkers")) || spokenWords.contains("doorknobs") || spokenWords.contains("dunkers") || spokenWords.contains("fatcakes")) {
			midland += 2;
		}
		
		//night before Halloween
		if (spokenWords.contains("mischief") && spokenWords.contains("night")) {
			NE += 2;
		}
		
		if (spokenWords.contains("devil's") && spokenWords.contains("night")) {
			NE += 0.5;
			upperMW += 2;
		}
		
		//fast road
		if (spokenWords.contains("highway")) {
			NE++;
		} else if (spokenWords.contains("freeway")) {
			pacificSW++;
			coastalSouth++;
		} else if (spokenWords.contains("parkway") || spokenWords.contains("throughway") || spokenWords.contains("thru-way")) {
			NE += 2;
		} else if (spokenWords.contains("expressway")) {
			upperMW += 2;
		}
		
		//burial
		if (spokenWords.contains("casket")) {
			NE += 2;
		}
		
		//trash
		if (spokenWords.contains("rubbish") && spokenWords.contains("bin")) {
			NE++;
		}
		
		//underground part of building
		if (spokenWords.contains("cellar")) {
			NE++;
		}
		
		//rolling bug
		//incl. if eq. occur. (say diff. terms referring to same thing in convo. will strengthen hypothesis)
		if (spokenWords.contains("pill") && spokenWords.contains("bug")) {
			NE++;
			pacificSW++;
		}
		
		if (spokenWords.contains("roly") && spokenWords.contains("poly")) {
			NE++;
			pacificSW++;
			midland++;
			gulfSouth++;
		}
		
		if (spokenWords.contains("potato") && spokenWords.contains("bug")) {
			NE++;
			upperMW++;
			pacificSW += 0.5;
			pacificNW += 0.5;
		}
		
		if (spokenWords.contains("sow") && spokenWords.contains("bug")) {
			pacificSW += 0.5;
			NE++;
			pacificNW += 0.25;
		}
		
		if (spokenWords.contains("millipede")) {
			NE++;
		}
		
		if (spokenWords.contains("centipede")) {
			NE++;
		}
		
		
		//food that was bought and taken home
		if (spokenWords.contains("take-out")) {
			NE += 3;
			upperMW += 2;
			coastalSouth += 0.5;
			pacificSW++;
			pacificNW++;
			midland++;
		}
		
		if (spokenWords.contains("carry-out")) {
			NE++;
			upperMW++;
			coastalSouth += 0.25;
			gulfSouth += 0.15;
		}
		
		
		//term for selling things
		if (spokenWords.contains("tag") && spokenWords.contains("sale")) {
			NE += 1.5;
		} 
		
		if (spokenWords.contains("yard") && spokenWords.contains("sale")) {
			NE += 3;
			upperMW += 2;
			coastalSouth++;
			gulfSouth += 0.5;
			pacificSW++;
			pacificNW += 0.5;
		}
		
		if (spokenWords.contains("rummage") && spokenWords.contains("sale")) {
			upperMW ++;
			NE += 0.25;
		}
		
		
		//milk and ice cream; can be bought at an ice cream shop
		if (spokenWords.contains("milkshake") || spokenWords.contains("shake")) {
			NE += 3;
			upperMW += 2.5;
			gulfSouth++;
			coastalSouth += 2;
			pacificNW += 0.5;
			pacificSW ++;
		}
		
		if (spokenWords.contains("frappe")) {
			NE += 1.25;
		}
		
		if (spokenWords.contains("cabinet")) {
			NE += 0.25;
		}
		
		//shoes
		if (spokenWords.contains("tennis") && (spokenWords.contains("shoe") || spokenWords.contains("shoes"))) {
			pacificNW++;
			pacificSW++;
			SW++;
			rockyMtn++;
			upperMW++;
			midland++;
			gulfSouth++;
			coastalSouth++;
		}
		
		if (spokenWords.contains("sneakers")) {
			NE += 3;
			coastalSouth += 0.15;
		}
		
		/**
		 * The following parts of the algorithm has been created
		 * with the aid of a database
		 * 
		 * (SEE http://qz.com/862325/the-great-american-word-mapper/)
		 * (SEE https://sites.google.com/site/wordmapperinfo/)
		 */
		
		//words related to theft
		
		if (spokenWords.contains("fraud")) {
			upperMW++;
			NE++;
			midland += 0.5;
		}
		
		if (spokenWords.contains("theft")) {
			pacificNW++;
			midland+= 0.25;
		}
		
		if (spokenWords.contains("crime")) {
			rockyMtn++;
			NE += 0.4;
			upperMW += 0.2;
			midland+= 0.5;
		}
		
		if (spokenWords.contains("thievery") || spokenWords.contains("theif")) {
			pacificNW += 0.2;
			rockyMtn += 0.1;
			NE += 0.3;
			midland += 0.1;
		}
		
		if (spokenWords.contains("money")) {
			coastalSouth += 4;
			gulfSouth += 3;
			NE += 0.1;
			SW += 0.1;
			upperMW  += 0.2;
		}
		
		//words related to banks
		if (spokenWords.contains("bank")) {
			NE += 2;
			upperMW++;
		}
		
		if (spokenWords.contains("savings")) {
			NE++;
			pacificNW += 0.5;
			pacificSW += 0.5;
			upperMW += 0.75;
		}
		
		//words related to help
		if (spokenWords.contains("suggest")) {
			upperMW++;
			pacificNW += 0.5;
			pacificSW += 0.25;
			NE += 0.5;
			midland+=0.2;
		}
		
		if (spokenWords.contains("recommend")) {
			rockyMtn+=2;
			pacificNW += 0.75;
			pacificSW += 0.75;
			NE += 0.5;
		}
		
		//greetings
		if (spokenWords.contains("sup")) {
			pacificNW++;
			upperMW++;
		}
		
		if (spokenWords.contains("yo")) {
			gulfSouth++;
			coastalSouth+=3;
		}
		
		if (spokenWords.contains("howdy")) {
			rockyMtn+=1.4;
			gulfSouth+=1.2;
		}
		
		if (spokenWords.contains("hey")) {
			midland++;
			upperMW++;
			pacificNW+=0.4;
		}
		
		if (spokenWords.contains("man")) {
			gulfSouth ++;
			coastalSouth+=3;
		}
		
		//words related to disappear/lose
		if (spokenWords.contains("lost") || spokenWords.contains("lose")) {
			NE += 0.5;
			rockyMtn += 0.15;
			upperMW += 0.65;
			gulfSouth++;
		}
		
		if (spokenWords.contains("stolen") || spokenWords.contains("stole") || spokenWords.contains("steal") || spokenWords.contains("stealed")) { //included "stealed" even though not real word because some callers may be using broken or improper English, and "stealed" is a common misconception for the past tense of "steal"
			pacificNW += 1.5;
			pacificSW++;
			rockyMtn++;
			NE += 0.5;
		}
		
		if (spokenWords.contains("disappear") || spokenWords.contains("disappeared")) {
			upperMW++;
			midland++;
		}
		
		
		/**
		 * Compile the results and determine largest variable.
		 */
		
		//map
		TreeMap<Double, String> data = new TreeMap<>();
		data.put(pacificNW, "pacificNW");
		data.put(pacificSW, "pacificSW");
		data.put(SW, "SW");
		data.put(rockyMtn, "rockyMtn");
		data.put(upperMW, "upperMW");
		data.put(midland, "midland");
		data.put(gulfSouth, "gulfSouth");
		data.put(coastalSouth, "coastalSouth");
		data.put(NE, "NE");
		
		for (Double key : data.keySet()) {
			//should be in order
			System.out.println(data.get(key));
		}
		
		//The caller avoided words that the algorithm uses for distinctions
		//Thus the confidence is 0 and there is no best possible region.
		if (data.size() == 0) {
			confidence = 0;
			return;
		}
		
		//highest score = key, bpr = value at key
		scoreBPR = data.lastKey();
		bestPossibleRegion = data.get(scoreBPR);
		
		/**
		 * Using the given data, determine confidence level, where 
		 * confidence, c is
		 * 		0 <= c <= 100
		 */
		
		
		TreeMap<Double, String> backupTree = data;
		
		int dataSize = spokenWords.size();
		int c = (int) scoreBPR;
		
		
		if (dataSize < 20) {
			c-=(20-dataSize);
		}
		
		//check if the highest is distinguishable
		backupTree.pollLastEntry();
		
		if (backupTree.size() == 0) {
			//there is no competition (second place scorer)
			//thus confidence is 0; surely there must be another scorer!
			//however, we can still have a best possible match.
			confidence = 0;
			return;
		}
		
		
			//if the difference between the highest score and the second
			//highest score is greater than two times the second highest
			//score, then the confidence increases by the difference
		if ((scoreBPR - backupTree.lastKey()) > (2 * backupTree.lastKey())) {
			c+=(int)(scoreBPR - backupTree.lastKey());
		}
		
		//confidence increases by half the difference of largest and second largest
		c += (int)(0.5*(scoreBPR - backupTree.lastKey()));
		
		//multiply b/c scores are small
		c = c * 10;
		
		//final remarks
		if (c > 100) c = 100;
		if (c < 0) c = 0;
		confidence = c;
	}

	/**
	 * Get region
	 * @return region
	 */
	public String getBestMatch() {
		return bestPossibleRegion;
	}
	
	/**
	 * Get score of bestPossibleRegion
	 * @return score of best match
	 */
	public double getScoreMatch(){
		return scoreBPR;
	}
	
	/**
	 * Get confidence level (between 1 and 100)
	 * @return percentage in form of double between 1 and 100
	 */
	public Double getConfidence() {
		return confidence;
	}
	
	
	/**
	 * Get spoken words
	 * @return spoken words (this is a param. in constructor)
	 */
	
	public List<String> getSpokenWords() {
		return spokenWords;
	}


	
	/**
	 * Getters for score of each region
	 * @return the score for each region
	 */

	public double getPacificNW() {
		return pacificNW;
	}

	public double getPacificSW() {
		return pacificSW;
	}

	public double getSW() {
		return SW;
	}

	public double getRockyMtn() {
		return rockyMtn;
	}

	public double getUpperMW() {
		return upperMW;
	}

	public double getMidland() {
		return midland;
	}

	public double getGulfSouth() {
		return gulfSouth;
	}

	public double getCoastalSouth() {
		return coastalSouth;
	}

	public double getNE() {
		return NE;
	}
	
	/**
	 * Determine legitimacy of call based on input number and discovered region
	 * @param phNumber phone number
	 * @return if legitimate
	 * @throws Exception bestPossibleRegion was null
	 */
	public boolean compareWithNumber(String phNumber) throws Exception {
		if (bestPossibleRegion == null) {
			throw new Exception("bestPossibleRegion was null; check if performAlgorithm() was run without any errors");
		}
		
		String[] nbn = phNumber.split(""); String areaCode = String.join("", nbn[0], nbn[1], nbn[2]);
		final String path = "src\\audio\\" + "areacode.db";
		BufferedReader br = new BufferedReader(new FileReader(path));
		String line;
		while ((line = br.readLine()) != null) {
			String[] split = line.split(" ");
			String curAC = split[0]; //area code
			String curReg = split[2]; //region 
			if (areaCode.equals(curAC)) {
				//same area code. Now we know curReg is accurate TO CLAIMED CALL ONLY.
				if (curReg.equals(bestPossibleRegion)) {
					//Now we know curReg matches hypothesis, so the call is legitimate
					return true;
				} else {
					//curReg does not match hypothesis, so the claimed region does not match the voice's region
					return false;
				}
			}
			//continue to next line until we find area code.
		}
		
		br.close();
		throw new Exception("Invalid area code " + areaCode + "; not found in database. Please update database or check the phone number input.");
	}
}
