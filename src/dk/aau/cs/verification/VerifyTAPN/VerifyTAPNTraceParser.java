package dk.aau.cs.verification.VerifyTAPN;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedMarking;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.simulation.TapaalTrace;
import dk.aau.cs.model.tapn.simulation.TimeDelayStep;
import dk.aau.cs.model.tapn.simulation.TimedTrace;
import dk.aau.cs.model.tapn.simulation.TimedTransitionStep;


public class VerifyTAPNTraceParser {

	private TimedArcPetriNet tapn;
	
	public VerifyTAPNTraceParser(TimedArcPetriNet tapn) {
		this.tapn = tapn;
	}

	public TapaalTrace parseTrace(BufferedReader reader) {
		TimedTrace trace = new TimedTrace();
		try {
			String line;
			TimedMarking previousMarking = null;
			TimedTransition previousTransition = null;
			TimedTransitionStep previousTransitionFiring = null;
			while (reader.ready() && (line = reader.readLine()) != null) {

				if (line == null || line.isEmpty())
					break; // we are done parsing trace, exit outer loop

				if(line.contains("Marking")) {
					TimedMarking marking = parseMarking(line);
					if (previousTransitionFiring != null) {
						List<TimedToken> consumedTokens = findConsumedTokensBetween(previousMarking, marking);
						trace.add(new TimedTransitionStep(previousTransition, consumedTokens));
						previousTransition = null;
					}
					previousMarking = marking;
				} else if(line.contains("Delay")) {
					BigDecimal delay = parseDelay(line);
					trace.add(new TimeDelayStep(delay));
				} else if(line.contains("Transition")) {
					previousTransition = parseTransition(line);
				}
			}
		} catch (IOException e) {
			return null;
		}

		return trace;
	}

	private TimedMarking parseMarking(String markingString) {
		TimedMarking marking = new TimedMarking();
		String[] tokens = markingString.split(" ");
		Pattern pattern = Pattern.compile("\\((\\w+),(\\d+(?:\\.\\d+)?)\\)");
		
		for(int i = 1; i < tokens.length; i++) { // tokens[0] contains the string "Marking:"
			Matcher matcher = pattern.matcher(tokens[i]);
			matcher.find();
			String place = matcher.group(1);
			String age = matcher.group(2);
			marking.add(new TimedToken(tapn.getPlaceByName(place), new BigDecimal(age)));
		}
		
		return marking;
	}

	private List<TimedToken> findConsumedTokensBetween(TimedMarking previousMarking, TimedMarking marking) {
		ArrayList<TimedToken> consumedTokens = new ArrayList<TimedToken>();
		
		boolean tokenFound = false;
		for(TimedPlace p : tapn.places()) {
			for(TimedToken t : previousMarking.getTokensFor(p)) {
				for(TimedToken t2 : marking.getTokensFor(p)) {
					if(t.equals(t2)) {
						tokenFound = true;
						break;
					}
				}
				
				if(!tokenFound) {
					consumedTokens.add(t);
				}
			}
		}
		
		return consumedTokens;
		
	}
	
	private BigDecimal parseDelay(String delayLine) {
		String[] split = delayLine.split(" ");
		Pattern p = Pattern.compile("\\d+(?:\\.\\d+)?");
		Matcher m = p.matcher(split[1]);
		m.find();
		
		return new BigDecimal(m.group(0));
	}
	
	private TimedTransition parseTransition(String transitionLine) {
		String[] split = transitionLine.split(" ");
		return tapn.getTransitionByName(split[1]); 
	}

}
