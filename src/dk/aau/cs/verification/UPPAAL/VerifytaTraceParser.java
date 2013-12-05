package dk.aau.cs.verification.UPPAAL;

import java.io.BufferedReader;
import java.io.IOException;

import dk.aau.cs.debug.Logger;
import dk.aau.cs.model.NTA.trace.SymbolicState;
import dk.aau.cs.model.NTA.trace.TimeDelayFiringAction;
import dk.aau.cs.model.NTA.trace.TransitionFiring;
import dk.aau.cs.model.NTA.trace.UppaalTrace;
import dk.aau.cs.translations.ReductionOption;

public class VerifytaTraceParser {
	public UppaalTrace parseTrace(BufferedReader reader, VerifytaOptions translation) {
		UppaalTrace trace = new UppaalTrace();
		try {
			String line;
			SymbolicState previousState = null;
			TransitionFiring previousTransitionFiring = null;
			boolean nextIsState = false;
			if (translation.getReduction() == ReductionOption.COMBI){
				String previous = "";
				double delayValue = 0;
				while (reader.ready()) {
					StringBuffer buffer = new StringBuffer();
					while ((line = reader.readLine()) != null && !line.isEmpty()) {
						buffer.append(line);
						buffer.append('\n');
					}
					String element = buffer.toString().replaceAll("_BOTTOMOUT_", "_BOTTOMIN_");
					if (line == null && element.isEmpty())
						break; // we are done parsing trace, exit outer loop
					if (nextIsState) { // untimed trace
						int indexMax = element.indexOf("Max=");
						int max = Integer.parseInt(element.substring(indexMax+4, indexMax+5));
						int previousParenthesis = previous.indexOf(")");
						if(previousParenthesis!=-1){
							String previousTemp = "";
							while(!previous.substring(previousParenthesis-1, previousParenthesis).equals("n")){
								previousTemp = previous.substring(previousParenthesis-1, previousParenthesis) + previousTemp;
								previousParenthesis = previousParenthesis-1;
							}
							max = Integer.parseInt(previousTemp.substring(0, previousTemp.indexOf(".")))+1;
						}
						boolean test = true;
						int end = 0;
						String newElement = element;
						while(test){
							int start = element.indexOf("X_", end);
							if (start==-1) {
								test = false;
							} else {
								int equal = element.indexOf("=", start);
								end = element.indexOf(" ", equal);
								String place = element.substring(start+2, equal);
								int numberOfTokens = Integer.parseInt(element.substring(equal+1, end));	
								int temp = -1;
								for (int i = 1; i <= numberOfTokens; i++){
									int parenthesis = newElement.indexOf(")");
									int countIndex = newElement.indexOf("count", 0);
									if (countIndex==-1){
										countIndex = newElement.indexOf("X_",0);
									}
									int ending = newElement.length();
									if (previous.indexOf("." + place,temp+1)!=-1){
										int temp2 = previous.indexOf("." + place,temp+1);
										temp = previous.indexOf("." + place,temp+1);
										String tempName = "";
										while(!previous.substring(temp2-1, temp2).equals("n")){
											tempName = previous.substring(temp2-1, temp2) + tempName;
											temp2 = temp2-1;
										}
										if (delayValue!=0){
											int TokenClockIndex = previous.indexOf("Token" + tempName + ".x=", temp+1);
											int clockEqual = previous.indexOf("=", TokenClockIndex);
											int clockSpace = previous.indexOf(" ", clockEqual);
											double clock = Double.parseDouble(previous.substring(clockEqual+1, clockSpace))+delayValue;
											newElement = newElement.substring(0, parenthesis) + "Token" + tempName + "." + place + " " + newElement.substring(parenthesis, countIndex) + "Token" + tempName + ".x=" + clock + " " + newElement.substring(countIndex,ending);
										} else {
											int TokenClockIndex = previous.indexOf("Token" + tempName + ".x=", temp+1);
											int clockEqual = previous.indexOf("=", TokenClockIndex);
											int clockSpace = previous.indexOf(" ", clockEqual);
											double clock = Double.parseDouble(previous.substring(clockEqual+1, clockSpace));
											newElement = newElement.substring(0, parenthesis) + "Token" + tempName + "." + place + " " + newElement.substring(parenthesis, countIndex) + "Token" + tempName + ".x=" + clock + " " + newElement.substring(countIndex,ending);
										}
									} else { 
										newElement = newElement.substring(0, parenthesis) + "Token" + max + "." + place + " " + newElement.substring(parenthesis, countIndex) + "Token" + max + ".x=0 " + newElement.substring(countIndex,ending);
										max = max + 1;
									}
								}
							}
						}
						delayValue = 0;
						previous = newElement;
						SymbolicState state = SymbolicState.parse("State:\n" + newElement);
						trace.addState(state);
						previousState = state;
						if (previousTransitionFiring != null) {
							previousTransitionFiring.setTargetState(state);
							previousTransitionFiring = null;
						}
						nextIsState = false;
					} else if (element.contains("State\n")) { // untimed trace
						nextIsState = true;
					} else if (element.contains("State:\n")) { // timed trace
						int indexMax = element.indexOf("Max=");
						int indexSpace = element.indexOf(" ", indexMax);
						int max = Integer.parseInt(element.substring(indexMax+4, indexSpace)+1);
						int previousParenthesis = previous.indexOf(")");
						if(previousParenthesis!=-1){
							String previousTemp = previous.substring(0,previousParenthesis);
							int nextToken = previousTemp.indexOf("Token",0);
							while(nextToken!=-1){
								int nextDot = previousTemp.indexOf(".",nextToken);
								max = Math.max(max, Integer.parseInt(previousTemp.substring(nextToken+5, nextDot))+1);
								nextToken = previousTemp.indexOf("Token",nextDot);
							}
						}
						boolean test = true;
						int end = 0;
						String newElement = element;
						while(test){
							int start = element.indexOf("X_", end);
							if (start==-1) {
								test = false;
							} else {
								int equal = element.indexOf("=", start);
								end = element.indexOf(" ", equal);
								String place = element.substring(start+2, equal);
								int numberOfTokens = Integer.parseInt(element.substring(equal+1, end));	
								int temp = -1;
								for (int i = 1; i <= numberOfTokens; i++){
									int parenthesis = newElement.indexOf(")");
									int countIndex = newElement.indexOf("count", 0);
									if (countIndex==-1){
										countIndex = newElement.indexOf("X_",0);
									}
									int ending = newElement.length();
									if (previous.indexOf("." + place,temp+1)!=-1){
										int temp2 = previous.indexOf("." + place,temp+1);
										temp = previous.indexOf("." + place,temp+1);
										String tempName = "";
										while(!previous.substring(temp2-1, temp2).equals("n")){
											tempName = previous.substring(temp2-1, temp2) + tempName;
											temp2 = temp2-1;
										}
										if (delayValue!=0){
											int TokenClockIndex = previous.indexOf("Token" + tempName + ".x=", temp+1);
											int clockEqual = previous.indexOf("=", TokenClockIndex);
											int clockSpace = previous.indexOf(" ", clockEqual);
											double clock = Double.parseDouble(previous.substring(clockEqual+1, clockSpace))+delayValue;
											newElement = newElement.substring(0, parenthesis) + "Token" + tempName + "." + place + " " + newElement.substring(parenthesis, countIndex) + "Token" + tempName + ".x=" + clock + " " + newElement.substring(countIndex,ending);
										} else {
											int TokenClockIndex = previous.indexOf("Token" + tempName + ".x=", temp+1);
											int clockEqual = previous.indexOf("=", TokenClockIndex);
											int clockSpace = previous.indexOf(" ", clockEqual);
											double clock = Double.parseDouble(previous.substring(clockEqual+1, clockSpace));
											newElement = newElement.substring(0, parenthesis) + "Token" + tempName + "." + place + " " + newElement.substring(parenthesis, countIndex) + "Token" + tempName + ".x=" + clock + " " + newElement.substring(countIndex,ending);
										}
									} else { 
										newElement = newElement.substring(0, parenthesis) + "Token" + max + "." + place + " " + newElement.substring(parenthesis, countIndex) + "Token" + max + ".x=0 " + newElement.substring(countIndex,ending);
										max = max + 1;
									}
								}
							}
						}
						delayValue = 0;
						previous = newElement;
						Logger.log("state " + newElement);
						SymbolicState state = SymbolicState.parse(newElement);
						trace.addState(state);
						previousState = state;
						if (previousTransitionFiring != null) {
							previousTransitionFiring.setTargetState(state);
							previousTransitionFiring = null;
						}
					} else if (element.contains("Delay:")) {
						Logger.log("Delay " + element);
						int colon = element.indexOf(":");
						int end = element.indexOf("\n");
						delayValue = Double.parseDouble(element.substring(colon+2, end));
						TimeDelayFiringAction delay = TimeDelayFiringAction.parse(
								previousState, element);
						trace.addFiringAction(delay);
					} else if (element.contains("Transitions:")) {
						if(!element.contains("__fill_remove_from_trace__!")){
							boolean test = true;
							int end = 0;
							while(test){
								int start = element.indexOf(":= X_", end);
								if (start==-1) {
									test = false;
								} else {
									int space = element.indexOf(" ", start+5);
									end = element.indexOf(",", space);
									String place = element.substring(start+5, space);
									String sign = element.substring(space+1, space+2);
									int numberOfTokens = Integer.parseInt(element.substring(space+3, end));
									for (int i = 1; i <= numberOfTokens; i++){
										if (sign.equals("-")){
											int tokenIndex = previous.indexOf("."+ place);
											String tokenName = "";
											while(!previous.substring(tokenIndex-1, tokenIndex).equals("n")){
												tokenName = previous.substring(tokenIndex-1, tokenIndex) + tokenName;
												tokenIndex = tokenIndex-1;
											}
											int exclamation = element.indexOf("!");
											String transition = "";
											while(!element.substring(exclamation-1, exclamation).equals(" ")){
												transition = element.substring(exclamation-1, exclamation) + transition;
												exclamation = exclamation-1;
											}
											element = element + "Token" + tokenName + "." + place + "->Token" + tokenName + "._BOTTOMIN_ { 1, " + transition + "?, x := 0 }\n";
										}
									}
								}
							}
							Logger.log("transition " + element);
							TransitionFiring transition = TransitionFiring.parse(
									previousState, element);
							trace.addFiringAction(transition);
							previousTransitionFiring = transition;
						}
					}
				}
			} else {
				while (reader.ready()) {
					StringBuffer buffer = new StringBuffer();
					while ((line = reader.readLine()) != null && !line.isEmpty()) {
						buffer.append(line);
						buffer.append('\n');
					}

					String element = buffer.toString();

					if (line == null && element.isEmpty())
						break; // we are done parsing trace, exit outer loop

					if (nextIsState) { // untimed trace
						SymbolicState state = SymbolicState.parse("State:\n" + element);
						trace.addState(state);
						previousState = state;
						if (previousTransitionFiring != null) {
							previousTransitionFiring.setTargetState(state);
							previousTransitionFiring = null;
						}
						nextIsState = false;
					} else if (element.contains("State\n")) { // untimed trace
						nextIsState = true;
					} else if (element.contains("State:\n")) { // timed trace
						SymbolicState state = SymbolicState.parse(element);
						trace.addState(state);
						previousState = state;
						if (previousTransitionFiring != null) {
							previousTransitionFiring.setTargetState(state);
							previousTransitionFiring = null;
						}
					} else if (element.contains("Delay:")) {
						TimeDelayFiringAction delay = TimeDelayFiringAction.parse(
								previousState, element);
						trace.addFiringAction(delay);
					} else if (element.contains("Transitions:")) {
						TransitionFiring transition = TransitionFiring.parse(
								previousState, element);
						trace.addFiringAction(transition);
						previousTransitionFiring = transition;
					}
				}
			}
		} catch (IOException e) {
			return null;
		}

		return trace.isEmpty() ? null : trace;
	}
}
