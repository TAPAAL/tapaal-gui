package dk.aau.cs.TA;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
/*  Copyright (c) 2009, Kenneth Yrke Jørgensen <kyrke@cs.aau.dk>
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    * Neither the name of the TAPAAL nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.   
 */

import dk.aau.cs.debug.Logger;

public class UppaalTrace {

	public static ArrayList<String> parseUppaalTrace(BufferedReader traceReader) throws Exception{
		ArrayList<String> trace = new ArrayList<String>();
		ArrayList<String> trace2 = new ArrayList<String>();

		String line;
		boolean collect = false;
		while ((line = traceReader.readLine()) != null){

			if (line.contains("Delay:")){
				trace.add(line.replace("Delay:", "").trim());
			}else if (line.contains("Transitions:")){
				collect = true;

			}else if (line.contains("State:")){
				collect = false;

			}else{
				if (collect){
					if (!line.equals("") && !line.contains("tau")){
						String tmp = line.trim();

						int first = tmp.indexOf("{");
						int second = tmp.indexOf("}");

						tmp = tmp.substring(first+1, second);

						String tmp2[] = tmp.split(",");

						tmp = tmp2[1].replace("!", "").replace("?", "");
						collect=false;
						trace.add(tmp.trim());
					}
				}
			}

		}


		String last = null;
		int lastnumber = 0;
		for (String s : trace){
			Logger.log(s);
			String tmp2[] = s.split("_");
			
			int tmp=0;
			try {
			 tmp = Integer.parseInt(tmp2[1].replace("T", ""));
			} catch (Exception e) {
				tmp=0;
			}
			Logger.log(lastnumber + " " + tmp);
			if ((!tmp2[0].equals(last)) ||  tmp <= lastnumber){
				trace2.add(tmp2[0]);
				last = tmp2[0];
				
			}
			lastnumber = tmp;

		}

		

		return trace2;	
	}

	
	/* Author: Kenneth Yrke Jørgensen <kenneth@yrke.dk> 
	 * 2009
	 * 
	 * 
	 */
	
	public static ArrayList<FiringAction> parseUppaalTraceAdvanced(BufferedReader traceReader) throws Exception{
		ArrayList<FiringAction> trace = new ArrayList<FiringAction>();
		ArrayList<FiringAction> trace2 = new ArrayList<FiringAction>();
		ArrayList<Float> tmpAgeOfTokens = new ArrayList<Float>();
		
		/// THIS CODE WILL NOT WORK IF WE DONT HAVE THE LOCKING TOKEN
		// AND THE SIMPLE REDUCTION
		
		String line;
		boolean collect = false;
		int turn = 0;
		DiscreetFiringAction dfa = new DiscreetFiringAction();
		while ((line = traceReader.readLine()) != null){
			Logger.log(line);

			if (line.contains("EXCEPTION: Clock valuation.")){
				System.err.println("Uppaal can not generate the trace.");
				return null;
			}else if (line.contains("Delay:")){
				trace.add(new TimeDelayFiringAction(Float.parseFloat(line.replace("Delay:", "").trim())));
			}else if (line.contains("Transitions:")){
				collect = true;

			}else if (line.contains("State:")){
				tmpAgeOfTokens.clear(); // Clear old values
				
				traceReader.readLine(); // Read and disregard line
				line =  traceReader.readLine();
				
				String[] tmp = line.split(" ");
				
//				Save the age of all tokens
				
				for (String s : tmp){
					
					if (!(s.contains("#depth"))){
						
						
						String[] tmp2 = s.trim().split("=");
						
						tmpAgeOfTokens.add(Float.parseFloat(tmp2[1]));
						
						
					}
					
				}		
				
				collect = false;
				continue;

			}else{
				if (collect && turn <= 1){
					if (!line.equals("") && !line.contains("tau")){
												
						String tmp = line.trim();

						//Get the transition fired
						
						int first = tmp.indexOf("{");
						int second = tmp.indexOf("}");

						tmp = tmp.substring(first+1, second);

						String tmp2[] = tmp.split(",");

						tmp = tmp2[1].replace("!", "").replace("?", "");
						String transitionname = tmp;
						
						
						
						//Get the token consumed
						
						tmp2 = line.trim().split("\\.");
						
						
						tmp = tmp2[0].replace("Token", "");
						
						if (tmp.equals("Control")){
							collect = false;
							turn=0;
							continue;
						}

						if (!tmp.equals("Lock") && !tmp.equals("")){
							int tokenConsumed = Integer.parseInt(tmp.replace("P(", "").replace(")", ""));
							String placename = tmp2[1].split("->")[0];

							dfa.setTrasition(transitionname.trim());
							dfa.addConsumedToken(placename, tmpAgeOfTokens.get(tokenConsumed));
						}
						//Add the tokens consumed

						turn++;

						if (turn==2){
							turn=0;

							collect=false;

							trace.add(dfa);
							dfa = new DiscreetFiringAction();

						}
						
						
					} else {
						turn=0;

						collect=false;

						trace.add(dfa);
						dfa = new DiscreetFiringAction();
					}
				}
			}

		}


		
		
		String last = null;
		int lastnumber = 0;
		
		DiscreetFiringAction dfatmp = new DiscreetFiringAction();
		//trace2.add(dfatmp);
		
		for (FiringAction fa : trace){
			
			if (fa instanceof DiscreetFiringAction){
				//Do stuff
				
				DiscreetFiringAction dfa1 = (DiscreetFiringAction)fa;
				String tmp2[] = dfa1.getTransition().split("_");
				int tmp2length = tmp2.length; 
				
				int tmp=0;
				try {
				 tmp = Integer.parseInt(tmp2[tmp2length-1].replace("T", ""));
				} catch (Exception e) {
					tmp=0;
				}
				//Logger.log(lastnumber + " " + tmp);
				if ((!tmp2[0].equals(last)) ||  tmp <= lastnumber){
					
					dfatmp = new DiscreetFiringAction();
					
					dfatmp.setTrasition(dfa1.getTransition().replace("_T"+tmp, ""));
					trace2.add(dfatmp);
					
					
					
					
					last = tmp2[0];
					
				}
				lastnumber = tmp;


				
				HashMap<String, ArrayList<Float>> tmpConsumedTokens = dfa1.getConsumedTokensList();
				
				for (String s : tmpConsumedTokens.keySet()){
					
					//Only add the consumed token if it is consumed from one of
					//the original places
					if (!((s.contains("P_lock")) || (s.contains("P_capacity")) || (s.contains("_im")) || (s.contains("_hp_"))) ){
						Logger.log("Added consumed token " + s + " - "+ tmpConsumedTokens.get(s).get(0));
						dfatmp.addConsumedToken(s, tmpConsumedTokens.get(s).get(0)); // XXX - only takes first token
					}
					
					
				}
				
				
			} else {
				//Just add the time delay
				trace2.add(fa);
				lastnumber=0;
				last="";
			}
			
			
		}
		
		

		return trace2;	
	}

}
