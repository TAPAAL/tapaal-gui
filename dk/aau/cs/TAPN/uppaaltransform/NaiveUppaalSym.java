package dk.aau.cs.TAPN.uppaaltransform;

import java.io.PrintStream;
import java.util.ArrayList;

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
/*  Copyright (c) 2009, Kenneth Yrke Jørgensen <kyrke@cs.aau.dk>
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    * Neither the name of the TAPAAL nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.   
 */
public class NaiveUppaalSym implements UppaalTransformer {

	
	
	
	public TAPN transform(TAPN model) {
		
		try {
			model.convertToConservative();
			TAPN model2 = model.convertToDegree2("naive");
			return model2;
		} catch (Exception e) {
			System.err.println("There was an error in making the net conservative");
			e.printStackTrace();
			return null;
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
		uppaalXML.println("typedef scalar[N] pid_t;");
		
		for (Transition t : model.getTransitions()){	
			uppaalXML.println("chan " + t.getName() + ";");
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
		Logger.log("GoGoGo");
		StringBuffer tmp = new StringBuffer();

		// Create the xml for the model
		tmp.append("<template>\n");

		//Name
		if (lock){
			tmp.append("<name x=\"5\" y=\"5\">Lock</name>\n");
		} else{
			tmp.append("<name x=\"5\" y=\"5\">P</name>\n");
			tmp.append("<parameter>const pid_t pid</parameter>\n");
		}

		//Declaration
		tmp.append("<declaration>\n");
		tmp.append("clock x; \n");
		tmp.append("</declaration>\n");

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

			tmp.append("<location id=\"a"+p.getID()+"\" x=\"" + (xcord) +"\" y=\"" + (ycord) +"\">\n");


			tmp.append("<name x=\"" + (xcord) +"\" y=\"" + (ycord) +"\">"+ p.getName() +"</name>\n");

			if (!((TAPNPlace)p).getInvariant().equals("<inf")){
				tmp.append("<label kind=\"invariant\"> x "+ ((TAPNPlace)p).getInvariant().replace("<", "&lt;")+ "</label>");
			}


			if (((TAPNPlace)p).isUrgent()) {
				tmp.append("<urgent/>");
			}

			tmp.append("</location>\n");

		}

		//Init 
		if (lock){
			tmp.append("<init ref=\"a" + model.getPlaceByName("P_lock").getID() + "\"/>\n");
		} else {
			tmp.append("<init ref=\"b0\"/>\n");
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
		
		//Model 
		
		for (Transition t : model.getTransitions()){
		
			Arc presetPlaceOne = t.getPreset().get(0);
			Arc presetPlaceTwo = t.getPreset().get(1);

			Arc postsetPlaceOne = t.getPostset().get(0);
			Arc postsetPlaceTwo = t.getPostset().get(1);

			//We let presetPlaceOne and postsetPlaceTwo be the locking chanin.
			if ( !((presetPlaceOne.getSource().getName().contains("_im")) || (presetPlaceOne.getSource().getName().equals("P_lock"))) ){
				//Swap them
				Logger.log("Swaped arc one " + presetPlaceOne.getSource().getName() + "hmm " + presetPlaceOne.getSource().getName().equals("P_lock") );
				Arc swap = presetPlaceTwo;
				presetPlaceTwo = presetPlaceOne;
				presetPlaceOne = swap;
			}

			if (!((postsetPlaceOne.getTarget().getName().contains("_im")) || (postsetPlaceOne.getTarget().getName().equals("P_lock")))){
				//Swap them
				Logger.log("Swaped arc two " + presetPlaceOne.getSource().getName() );
				Arc swap = postsetPlaceTwo;
				postsetPlaceTwo = postsetPlaceOne;
				postsetPlaceOne = swap;
			}

			Logger.log(presetPlaceOne);
			Logger.log(postsetPlaceOne);
			
			Logger.log(presetPlaceTwo);
			Logger.log(postsetPlaceTwo);

			// Add first arc, we know this is in the chain. 
			tmp.append("<transition>\n");
			tmp.append("<source ref=\"a"+ presetPlaceOne.getSource().getID() +"\"/>\n");
			tmp.append("<target ref=\"a"+ postsetPlaceOne.getTarget().getID() +"\"/>\n");
			tmp.append("<label kind=\"guard\"></label>\n");				
			tmp.append("<label kind=\"synchronisation\">"+ t.getName() +  "!</label>\n");
			tmp.append("<label kind=\"assignment\">x:=0</label>\n");
			tmp.append("</transition>\n");

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
	

		tmp.append("</template>");

		return tmp;

	}

	public void autoTransform(TAPN model, PrintStream uppaalXML, PrintStream queryFile, TAPNQuery query, int numberOftokens) {
		
		TAPN model2 = transform(model);
		transformToUppaal(model2, uppaalXML, numberOftokens);
		try {
			transformQueriesToUppaal(model, numberOftokens, query, queryFile);
		} catch (Exception e) {
			System.err.println("Error generating query for model");
			e.printStackTrace();
		}
	
	}
	
	
	private String transformQueriesToUppaal(int numberOfTemplates, TAPNQuery querie) throws Exception{
//
//		//String toReturn=null;
//		//TODO - Sanity validation, are the uses names in the qyerie in the model?
//
//		String expandpart=null;
//
//		querie.trim(); // Remove ending and beginning spaces
//
//		String quantifier = querie.substring(0, 3);
//
//		//Get the id's that needs to be changed
//		Pattern p = Pattern.compile("[ ][a-zA-Z](([a-zA-Z])*([0-9])*(_)*)*[ ]");
//		//Pattern p = Pattern.compile("[ ][[a-zA-Z](([a-zA-Z])*([0-9])*(_)*)*]*[ ]");
//		//Pattern p = Pattern.compile("[ ][[[a-zA-Z](([a-zA-Z])*([0-9])*(_)*)*]]*[ ]");
//		Matcher m = p.matcher(querie);
//
//		//Make a copy of the querie that we makes the changes in
//		expandpart = querie.toString();
//
//		// TODO put () around the org part, else using or wil fail when appending lock part.
//
//		ArrayList<String> ident = new ArrayList<String>();
//		
//		
//		while (m.find()){
//			boolean found = false;
//			String i = m.group().trim();
//			
//			for (String tmp : ident){
//				
//				if (tmp.equals(i)){
//					found=true;
//					break;		
//				}
//				//Not in the set add it	
//			}
//			if (!found){
//				ident.add(i);
//			}
//
//		}
//		
//		for (String a : ident){
//			
////			Generate a new replacement
//			String tmp = a.trim();
//
//			new StringBuffer();
// 
//			expandpart=expandpart.replaceAll(" "+tmp+" ", "(sum(i:pid_t) P(i)."+ a +")");		
//			
//		}
//		
//		
//		//Translation
//		if (quantifier.substring(1, 3).equals("<>")){
//
//			expandpart=expandpart.replace("<>", "<>(");
//			expandpart=expandpart.concat(") and "); // The stuff before the lock sum part
//
//
//		} else { // This is the "[]" case
//			expandpart=expandpart.replace("[]", "[](");
//			expandpart=expandpart.concat(") or !"); // The stuff before the lock sum part
//
//		}
//		//Lock part 
//
//		StringBuffer toReturn = new StringBuffer(expandpart);
//
//		toReturn.append("( ");
//		toReturn.append("Lock.P_lock == 1 "); //Lock token is not locked
//		toReturn.append(" && Control.finish == 1");
//		toReturn.append(")");
//		
//
//		return toReturn.toString();
		return "";
	}


}
