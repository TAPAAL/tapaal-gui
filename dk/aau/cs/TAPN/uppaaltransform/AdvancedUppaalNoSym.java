package dk.aau.cs.TAPN.uppaaltransform;

import java.io.PrintStream;
import java.util.ArrayList;

import dk.aau.cs.TCTL.visitors.OptimizedStandardTranslationQueryVisitor;
import dk.aau.cs.debug.Logger;
import dk.aau.cs.petrinet.Arc;
import dk.aau.cs.petrinet.Location;
import dk.aau.cs.petrinet.Place;
import dk.aau.cs.petrinet.TAPN;
import dk.aau.cs.petrinet.TAPNArc;
import dk.aau.cs.petrinet.TAPNPlace;
import dk.aau.cs.petrinet.TAPNQuery;
import dk.aau.cs.petrinet.TAPNTransportArc;
import dk.aau.cs.petrinet.Transition;
import dk.aau.cs.petrinet.degree2converters.KyrketestUppaalSym;
/*  Copyright (c) 2009, Kenneth Yrke Jørgensen <kyrke@cs.aau.dk>
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    * Neither the name of the TAPAAL nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.   
 */
public class AdvancedUppaalNoSym implements UppaalTransformer {

	public void autoTransform(TAPN model, PrintStream uppaalXML, PrintStream queryFile, TAPNQuery query, int numberOfTokens) {
		try {
			model.convertToConservative();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		TAPN model2 = transform(model);
		transformToUppaal(model2, uppaalXML, numberOfTokens);
		try {
			transformQueriesToUppaal(model2, numberOfTokens, query, queryFile);
		} catch (Exception e) {
			System.err.println("Error generating query for model");
			e.printStackTrace();
		}
	}

	public void transformQueriesToUppaal(TAPN model, int numberOfEkstraTokens, TAPNQuery inputQuery, PrintStream stream) throws Exception {

		
		stream.println("// Autogenerated by the TAPAAL (www.tapaal.net)");
		stream.println("");

		stream.println("/*");
		stream.println(" " + inputQuery + " " );
		stream.println("*/");

		stream.println(transformQueriesToUppaal(numberOfEkstraTokens + model.tokens.size(), inputQuery));
		
	}

	public void transformToUppaal(TAPN model, PrintStream uppaalXML,
			int numberOfEkstraTokens) {
		
		ArrayList<Place> tokens = new ArrayList<Place>();
		// Copy from the model

		for (Place p : model.tokens){
			if (!p.getName().equals("P_lock")){
				tokens.add(p);
			}
		}

		
		//Create Ekstra tokens
		Place capacity = model.getPlaceByName("P_capacity");
		for (int j=0; j < numberOfEkstraTokens;j++){
			tokens.add(capacity);
		}
		
		uppaalXML.println("<nta>");
		
		
		uppaalXML.println("<declaration>");
		
		uppaalXML.println("const int N = "+ tokens.size() +";");
		uppaalXML.println("typedef int[1,N] pid_t;");
		
		uppaalXML.println("bool lock = false;");
		
		for (Transition t : model.getTransitions()){	
			if (t.getPreset().size() == 1 && t.getPostset().size() == 1){
				uppaalXML.println("broadcast chan " + t.getName() + ";");
			} else {
				uppaalXML.println("chan " + t.getName() + ";");
			}
		}
		
		for (int i = 0; i < tokens.size(); i++){
			//Create the control chans
			uppaalXML.println("chan c" + i + ";");
		}
		
		uppaalXML.println("</declaration>");
		
		StringBuffer a;
		
		a = createTemplateControl(tokens);
		uppaalXML.append(a);
		
		Logger.log("Finished Control token");
		
		a = createTemplateByModel(model,tokens, true);
		uppaalXML.append(a);
		
		a = createTemplateByModel(model,tokens, false);
		uppaalXML.append(a);
		
		
		//System
		uppaalXML.println("<system>");
		uppaalXML.append("system Control, Lock, P;");
		uppaalXML.println("</system>");
		
		uppaalXML.println("</nta>");

		
	}

	public TAPN transform(TAPN model) {
		
		KyrketestUppaalSym a = new KyrketestUppaalSym(model);
		try {
			return a.transform(model);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}


private StringBuffer createTemplateControl(ArrayList<Place> tokens) {
		
		StringBuffer tmp = new StringBuffer();

		// Create the xml for the model
		tmp.append("<template>\n");

		//Name
		tmp.append("<name x=\"5\" y=\"5\">Control</name>\n");


		//Locations
		int xcord = 10, ycord = 10;

		int i=0;
		for (i=0; i < tokens.size(); i++){ 

			xcord += 10;ycord += 10;

			tmp.append("<location id=\"b"+i+"\" x=\"" + (xcord) +"\" y=\"" + (ycord) +"\">\n");
			tmp.append("<name x=\"" + (xcord) +"\" y=\"" + (ycord) +"\"></name>\n");
			tmp.append("<committed/>");
			tmp.append("</location>\n");

		}
		xcord += 10;ycord += 10;
		tmp.append("<location id=\"b"+ i +"\" x=\"" + (xcord) +"\" y=\"" + (ycord) +"\">\n");
		tmp.append("<name x=\"" + (xcord) +"\" y=\"" + (ycord) +"\">finish</name>\n");
		tmp.append("</location>\n");


		//Init 
		//tmp.append("<init ref=\"@@init@@\"/>\n"); // TODO - fix this
		tmp.append("<init ref=\"b0\"/>\n");

		//Transitions
				
		for (i=0; i < tokens.size(); i++){
		
			tmp.append("<transition>\n");
			tmp.append("<source ref=\"b"+ i +"\"/>\n");
			tmp.append("<target ref=\"b"+ (i+1) +"\"/>\n");	
			tmp.append("<label kind=\"synchronisation\">c"+i+"!</label>\n");
			tmp.append("</transition>\n");
			
		}
		tmp.append("</template>");

		return tmp;
		
		
		
	}
	
	private StringBuffer createTemplateByModel(TAPN model, ArrayList<Place> tokens, boolean lock) {
		StringBuffer tmp = new StringBuffer();

		Logger.log("Creating stuff");
		// Create the xml for the model
		tmp.append("<template>\n");

		//Name
		if (lock){
			tmp.append("<name  x=\"5\" y=\"5\">Lock</name>\n");
		} else{
			tmp.append("<name x=\"5\" y=\"5\">P</name>\n");	
			tmp.append("<parameter>const pid_t pid</parameter>\n");
		}

		//Declaration
		if (!lock){
			tmp.append("<declaration>\n");
			tmp.append("clock x; \n");
			tmp.append("</declaration>\n");
		}
		
		//Locations
		if (!lock){
			tmp.append("<location id=\"b0\" x=\"10\" y=\"10\">\n");
			tmp.append("<name x=\"10\" y=\"10\"></name>\n");
			tmp.append("<committed/>");
			tmp.append("</location>\n");
		}


		for (Place p : model.getPlaces()){

			int xcord = 0, ycord = 0;

			Location a=null;
			if ((a = model.locations.get(p)) != null){
				xcord= (int)(a.getX());
				ycord=  (int)(a.getY());
			}

			if ((lock && (p.getName().contains("_im") || p.getName().contains("P_lock")) || (!lock && !(p.getName().contains("_im") || p.getName().contains("P_lock"))))){
				tmp.append("<location id=\"a"+p.getID()+"\" x=\"" + (xcord) +"\" y=\"" + (ycord) +"\">\n");
				tmp.append("<name x=\"" + (xcord) +"\" y=\"" + (ycord) +"\">"+ p.getName() +"</name>\n");

				if (!((TAPNPlace)p).getInvariant().equals("<inf") && !lock){
					tmp.append("<label kind=\"invariant\"> x "+ ((TAPNPlace)p).getInvariant().replace("<", "&lt;")+ "</label>");
				}

				if (((TAPNPlace)p).isUrgent()) {
					tmp.append("<urgent/>");
				}
				if (lock && !p.getName().equals("P_lock")){
					tmp.append("<committed/>");
				}
				tmp.append("</location>\n");
			}
		}

		//Init 
		if (!lock){
			tmp.append("<init ref=\"b0\"/>\n");
		} else {
			tmp.append("<init ref=\"a"+model.getPlaceByName("P_lock").getID()+"\"/>\n");	
		}

		//transitions
		
		//Setup
		
		if (!lock){
			
			for (int i = 0; i < tokens.size(); i++){
					tmp.append("<transition>\n");
					tmp.append("<source ref=\"b0\"/>\n");
					tmp.append("<target ref=\"a"+ tokens.get(i).getID() +"\"/>\n");
					tmp.append("<label kind=\"synchronisation\">c"+ i +  "?</label>\n");
					tmp.append("</transition>\n");
				
			}
		}
		
		for (Transition t : model.getTransitions()){

			
			if (t.getPreset().size()==1 && t.getPostset().size()==1 && !lock){
				Logger.log("The new way 1!! " +t);
				tmp.append(createTransition(t.getPreset().get(0), t.getPostset().get(0), t.getName(),lock));
			}

			if (t.getPreset().size()>=2 || t.getPostset().size() >=2){
				
				Arc presetPlaceOne = t.getPreset().get(0);
				Arc presetPlaceTwo = t.getPreset().get(1);
				
				Arc postsetPlaceOne = t.getPostset().get(0);
				Arc postsetPlaceTwo = t.getPostset().get(1);

				
				//Order the transportarcs to point to the right targets
				if (presetPlaceOne instanceof TAPNTransportArc){ 
					if (!(presetPlaceOne == postsetPlaceOne)){
						Arc swap = postsetPlaceOne;
						postsetPlaceOne = postsetPlaceTwo;
						postsetPlaceTwo = swap;
					}
				}
				if (presetPlaceTwo instanceof TAPNTransportArc){ 
					if (!(presetPlaceTwo == postsetPlaceTwo)){
						Arc swap = postsetPlaceTwo;
						postsetPlaceTwo = postsetPlaceOne;
						postsetPlaceOne = swap;
						
					}
				}

				
//				We let presetPlaceOne and postsetPlaceTwo be the locking chanin.
				if ( !((presetPlaceOne.getSource().getName().contains("_im")) || (presetPlaceOne.getSource().getName().equals("P_lock"))) ){
					//Swap them

					Arc swap = presetPlaceTwo;
					presetPlaceTwo = presetPlaceOne;
					presetPlaceOne = swap;
				}

				if (!((postsetPlaceOne.getTarget().getName().contains("_im")) || (postsetPlaceOne.getTarget().getName().equals("P_lock")))){
					//Swap them

					Arc swap = postsetPlaceTwo;
					postsetPlaceTwo = postsetPlaceOne;
					postsetPlaceOne = swap;
				}
				Logger.log("" + presetPlaceTwo + postsetPlaceTwo);
				
				/*if ( !lock && (!((presetPlaceOne.getSource().getName().contains("_im")) || (presetPlaceOne.getSource().getName().equals("P_lock"))) ||
						!((presetPlaceTwo.getSource().getName().contains("_im")) || (presetPlaceTwo.getSource().getName().equals("P_lock"))))){
					*/
				if ((!((presetPlaceOne.getSource().getName().contains("_im")) || (presetPlaceOne.getSource().getName().equals("P_lock"))))){
					if (!lock){
						Logger.log("The new way 2" + t);

						//It the new way 
						tmp.append(createTransition(presetPlaceOne, postsetPlaceOne, t.getName(), lock, '!'));
						tmp.append(createTransition(presetPlaceTwo, postsetPlaceTwo, t.getName(), lock, '?'));
					}	
				} else {
					//Its the old way

					Logger.log("The old way!! " + t);
					
					

					if (lock){
						// Add first arc, we know this is in the chain. 
						tmp.append("<transition>\n");
						tmp.append("<source ref=\"a"+ presetPlaceOne.getSource().getID() +"\"/>\n");
						tmp.append("<target ref=\"a"+ postsetPlaceOne.getTarget().getID() +"\"/>\n");
						
						if (presetPlaceOne.getSource().getName().equals("P_lock")){
							tmp.append("<label kind=\"guard\">lock==0</label>\n");	
						} else{
							tmp.append("<label kind=\"guard\"></label>\n");
						}
										
						tmp.append("<label kind=\"synchronisation\">"+ t.getName() +  "!</label>\n");
						if (presetPlaceOne.getSource().getName().equals("P_lock")){
							tmp.append("<label kind=\"assignment\">lock=1</label>\n");
						}else if (postsetPlaceOne.getTarget().getName().equals("P_lock")){
							tmp.append("<label kind=\"assignment\">lock=0</label>\n");
						}   else {
							tmp.append("<label kind=\"assignment\"></label>\n");	
						}
						tmp.append("</transition>\n");

					} else {
						//The second arc
						String guard="";
						String tmp2[] = ((TAPNArc)presetPlaceTwo).getGuard().split(",");

						// XXX TODO what if there is no guard? kyrke

						if (tmp2.length > 1){
							if (!(tmp2[0].equals("[0"))) { // not [0
								if (tmp2[0].charAt(0) == '('){
									guard += "x &gt; " + tmp2[0].substring(1, tmp2[0].length());
								} else {
									guard += "x &gt;=" + tmp2[0].substring(1, tmp2[0].length());
								}
							}
							if (!(tmp2[0].equals("[0")) && !(tmp2[1].equals("inf)"))){
								guard += " &amp;&amp; ";
							}
							if (!(tmp2[1].equals("inf)"))) { // not inf
								if (tmp2[1].charAt(tmp2[1].length()-1) == ')'){
									guard += "x &lt;" + tmp2[1].substring(0, tmp2[1].length()-1);
								} else {
									guard += " x &lt;=" +  tmp2[1].substring(0, tmp2[1].length()-1);
								}
							}
						}

						
						tmp.append("<transition>\n");
						tmp.append("<source ref=\"a"+ presetPlaceTwo.getSource().getID() +"\"/>\n");

						if (presetPlaceTwo instanceof TAPNTransportArc){
							tmp.append("<target ref=\"a"+ postsetPlaceTwo.getTarget().getID() +"\"/>\n");	

							tmp.append("<label kind=\"guard\">"+ guard +"</label>\n");

							tmp.append("<label kind=\"synchronisation\">"+ t.getName() + "?</label>\n");
							tmp.append("<label kind=\"assignment\"></label>\n"); // No reset of clock
						}else {

							tmp.append("<target ref=\"a"+ postsetPlaceTwo.getTarget().getID() +"\"/>\n");
							tmp.append("<label kind=\"guard\">"+ guard +"</label>\n");
							tmp.append("<label kind=\"synchronisation\">"+ t.getName() + "?</label>\n");

							tmp.append("<label kind=\"assignment\">x:=0</label>\n");
						


						}
						tmp.append("</transition>\n");
					}
				}

			}
		} 

		tmp.append("</template>");

		return tmp;

	}

	private StringBuffer createTransition(Arc arc, Arc arc2, String name, boolean lock, char syncchar) {

		StringBuffer tmp = new StringBuffer();
//		The second arc
		String guard="";
		String tmp2[] = ((TAPNArc)arc).getGuard().split(",");

		// XXX TODO what if there is no guard? kyrke

		if(syncchar=='!'){
			guard = "lock == 0";
		}
		
		String tmpguard="";
		if (tmp2.length > 1){
			if (!(tmp2[0].equals("[0"))) { // not [0
				if (tmp2[0].charAt(0) == '('){
					tmpguard += "x &gt; " + tmp2[0].substring(1, tmp2[0].length());
				} else {
					tmpguard += "x &gt;=" + tmp2[0].substring(1, tmp2[0].length());
				}
			}
			if (!(tmp2[0].equals("[0")) && !(tmp2[1].equals("inf)"))){
				tmpguard += " &amp;&amp; ";
			}
			if (!(tmp2[1].equals("inf)"))) { // not inf
				if (tmp2[1].charAt(tmp2[1].length()-1) == ')'){
					tmpguard += "x &lt;" + tmp2[1].substring(0, tmp2[1].length()-1);
				} else {
					tmpguard += " x &lt;=" +  tmp2[1].substring(0, tmp2[1].length()-1);
				}
			}
		}
		
		if (!tmpguard.equals("") && !guard.equals("")){
			guard+= " &amp;&amp; " + tmpguard;
		} else if (!tmpguard.equals("")){
			guard = tmpguard;
		}

				
		tmp.append("<transition>\n");
		tmp.append("<source ref=\"a"+ arc.getSource().getID() +"\"/>\n");

		if (arc instanceof TAPNTransportArc){
			tmp.append("<target ref=\"a"+ arc2.getTarget().getID() +"\"/>\n");	

			tmp.append("<label kind=\"guard\">"+ guard +"</label>\n");

			tmp.append("<label kind=\"synchronisation\">"+ name + syncchar + "</label>\n");
			tmp.append("<label kind=\"assignment\"></label>\n"); // No reset of clock
		}else {

			tmp.append("<target ref=\"a"+ arc2.getTarget().getID() +"\"/>\n");
			tmp.append("<label kind=\"guard\">"+ guard +"</label>\n");
			tmp.append("<label kind=\"synchronisation\">"+ name + syncchar + "</label>\n");

			tmp.append("<label kind=\"assignment\">x:=0</label>\n");
			


		}
		tmp.append("</transition>\n");

		return tmp;
	}

	private StringBuffer createTransition(Arc arc, Arc arc2, String name, boolean lock) {

		StringBuffer tmp = new StringBuffer();
//		The second arc
		String guard="";
		String tmp2[] = ((TAPNArc)arc).getGuard().split(",");

		// XXX TODO what if there is no guard? kyrke


		guard = "lock == 0";
		
		String tmpguard="";
		if (tmp2.length > 1){
			if (!(tmp2[0].equals("[0"))) { // not [0
				if (tmp2[0].charAt(0) == '('){
					tmpguard += "x &gt; " + tmp2[0].substring(1, tmp2[0].length());
				} else {
					tmpguard += "x &gt;=" + tmp2[0].substring(1, tmp2[0].length());
				}
			}
			if (!(tmp2[0].equals("[0")) && !(tmp2[1].equals("inf)"))){
				tmpguard += " &amp;&amp; ";
			}
			if (!(tmp2[1].equals("inf)"))) { // not inf
				if (tmp2[1].charAt(tmp2[1].length()-1) == ')'){
					tmpguard += "x &lt;" + tmp2[1].substring(0, tmp2[1].length()-1);
				} else {
					tmpguard += " x &lt;=" +  tmp2[1].substring(0, tmp2[1].length()-1);
				}
			}
		}
		if (!tmpguard.equals("")){
			guard+= " &amp;&amp; " + tmpguard;
		}

		

		tmp.append("<transition>\n");
		tmp.append("<source ref=\"a"+ arc.getSource().getID() +"\"/>\n");


		tmp.append("<target ref=\"a"+ arc2.getTarget().getID() +"\"/>\n");
		tmp.append("<label kind=\"guard\">"+ guard +"</label>\n");

		if (arc instanceof TAPNTransportArc){

			//tmp.append("<label kind=\"synchronisation\">"+ name + syncchar + "</label>\n");
			tmp.append("<label kind=\"assignment\"></label>\n"); // No reset of clock
			tmp.append("<label kind=\"synchronisation\">"+ name +"!</label>\n");
		}else {

			tmp.append("<label kind=\"synchronisation\">"+ name +"!</label>\n");

			
			tmp.append("<label kind=\"assignment\">x:=0</label>\n");
			


		}
		tmp.append("</transition>\n");

		return tmp;
	}
	
	private String transformQueriesToUppaal(int numberOfTemplates, TAPNQuery inputQuery) throws Exception{
		OptimizedStandardTranslationQueryVisitor visitor = new OptimizedStandardTranslationQueryVisitor();
		return visitor.getUppaalQueryFor(inputQuery);
	}


}
