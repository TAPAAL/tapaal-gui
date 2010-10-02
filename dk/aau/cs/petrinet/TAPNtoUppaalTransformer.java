package dk.aau.cs.petrinet;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.aau.cs.TCTL.visitors.StandardTranslationQueryVisitor;
import dk.aau.cs.debug.Logger;
/*  Copyright (c) 2009, Kenneth Yrke Jørgensen <kyrke@cs.aau.dk>, Joakim Byg <jokke@cs.aau.dk>
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    * Neither the name of the TAPAAL nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.   
 */ 
public class TAPNtoUppaalTransformer {

	TAPN model = null; 
	PrintStream uppaalXML = null;
	int numberOfEkstraTokens = 0;
	boolean symmetricreduction = false;
	boolean drawnice = false;
	boolean capacityreset = false;
	boolean removeUnused = false;
	
	public TAPNtoUppaalTransformer(TAPN model, PrintStream uppaalXML, int numberoftokens) {
		this.model = model;
		this.uppaalXML = uppaalXML;
		this.numberOfEkstraTokens = numberoftokens;
		
	}
	
	public TAPNtoUppaalTransformer(TAPN model, PrintStream uppaalXML, int numberoftokens, boolean symmetricreduction, boolean drawnice, boolean capacityreset, boolean removeUnused) {
		this.symmetricreduction = symmetricreduction;
		this.model = model;
		this.uppaalXML = uppaalXML;
		this.numberOfEkstraTokens = numberoftokens;
		this.drawnice=drawnice;
		this.capacityreset = capacityreset;
		this.removeUnused = removeUnused;
		
	}
	
	public void transform(){
		
		ArrayList<Place> tokens = new ArrayList<Place>();
		// Copy from the model
		for (Place p : model.tokens){
			tokens.add(p);
		}
//		Create Ekstra tokens
		Place capasity = model.getPlaceByName("P_capacity");
		for (int j=0; j < numberOfEkstraTokens;j++){
			tokens.add(capasity);
		}
		
		uppaalXML.println("<nta>");
		
		
		uppaalXML.println("<declaration>");
		
		for (Transition t : model.getTransitions()){
			
			if (((TAPNTransition)t).isUrgent()){
				uppaalXML.println("urgent chan " + t.getName() + ";\n");
			} else {
				uppaalXML.println("chan " + t.getName() + ";\n");
			}
		}
		
		// Symetric reduction
		String arrayinit = "";
		boolean first=true;
		for (Place p : tokens){
			
			if (first){
				first=false;
			}else{
				arrayinit += ",";
			}
			if (p.getName().equals("P_capacity")){
				arrayinit += "1";
			}else {
				arrayinit += "0";
			}
			
		}
		
		if (symmetricreduction){
			uppaalXML.println("bool g[" + tokens.size()+ "] = {"+arrayinit+"};");
		}
		
		
		
		uppaalXML.println("</declaration>");
		
	
		
		
		int numberOfTokens = tokens.size();
		
		//add capasity place to the tokesen for each ekstra token, 
		
		int i = 0;
		for (Place p : tokens){
			StringBuffer a;
			
			if (symmetricreduction && drawnice && removeUnused){
				a=createTemplateReductionNiceDrawingRemoveUnused(uppaalXML,	p, i);
				Logger.log("remove unused");
			} else if (!symmetricreduction && !drawnice){
				a = createTemplate(uppaalXML, p, i);
				Logger.log("normal used");
			} else if (!symmetricreduction && drawnice){
				Logger.log("Created with drawnice");
				a = createTemplateNiceDrawing(uppaalXML, p, i);
			}else if (drawnice && symmetricreduction){
				a = createTemplateReductionNiceDrawing(uppaalXML, p, i);
				Logger.log("drawnice used");			
			}else {
				a = createTemplateReduction(uppaalXML, p, i);
				Logger.log("default used");
			}
			
			String b = a.toString();
			b = b.replace("@@name@@", "Token"+i);
			
			uppaalXML.append(b);
			i++;
		}
		
		
		//System
		uppaalXML.println("<system>");
		
		uppaalXML.append("system Token0");
		for (i=1; i<numberOfTokens;i++){	
			uppaalXML.append(", Token"+i );
		}
		uppaalXML.append(";");
		
		uppaalXML.println("</system>");
		
		uppaalXML.println("</nta>");
		
		
		
	}

	private StringBuffer createTemplateReduction(PrintStream uppaalXML2, Place initialPlace, int templatenumber) {
		
		StringBuffer tmp = new StringBuffer();
		
		// Create the xml for the model
		tmp.append("<template>\n");
		
		//Name
		tmp.append("<name x=\"5\" y=\"5\">@@name@@</name>\n");
		
		//Declaration
		
			tmp.append("<declaration>\n");
			tmp.append("clock x; \n");	
			tmp.append("</declaration>\n");
		
		//Locations
		
		
		for (Place p : model.getPlaces()){
			
			int xcord = 0, ycord = 0;
			
			Location a=null;
			if ((a = model.locations.get(p)) != null){
				xcord= (int)(a.x);
				ycord=  (int)(a.y);
			}
			
			
			tmp.append("<location id=\"a"+p.getID()+"\" x=\"" + (xcord) +"\" y=\"" + (ycord) +"\">\n");
			
			
			tmp.append("<name x=\"" + (xcord) +"\" y=\"" + (ycord) +"\">"+ p.getName() +"</name>\n");
			
			if (!((TAPNPlace)p).getInvariant().equals("<inf")){
				tmp.append("<label kind=\"invariant\" x=\"-66\" y=\"263\"> x "+ ((TAPNPlace)p).getInvariant().replace("<", "&lt;")+ "</label>");
			}
			
			
			if (((TAPNPlace)p).isUrgent()) {
				tmp.append("<urgent/>");
			}
			
			tmp.append("</location>\n");
			
		}
		
		//Init 
		//tmp.append("<init ref=\"@@init@@\"/>\n"); // TODO - fix this
		tmp.append("<init ref=\"a"+initialPlace.getID()+"\"/>\n");

		//transitions
		for (Transition t : model.getTransitions()){
			
			//Vi ved der maksimalt er en transport arc
			
			Place transportarcplace = null;
			ArrayList<Place> nontransportplaces = new ArrayList<Place>();
			for (Arc a : t.getPostset()){
				
				if (a instanceof TAPNTransportArc){
					transportarcplace = (Place)a.getTarget();
				}else {
					nontransportplaces.add((Place)a.getTarget());
				}
			}
			char symbol = '!';
			
			for (Arc a : t.getPreset()){		
				
				tmp.append("<transition>\n");
				tmp.append("<source ref=\"a"+ a.getSource().getID() +"\"/>\n");				
				
				String guard="";
				String tmp2[] = ((TAPNArc)a).getGuard().split(",");
				
				
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
				
				//Symetric reduction
				if (a.getSource().getName().equals("P_capacity") && templatenumber > 0){
					if (!(guard.equals(""))){
						guard+="and";
					}
					guard += "(";
					guard+="g[" + 0 + "]";
					for (int i=1; i < templatenumber; i++){
						guard+="+ g[" + i + "]";
					}
					guard += " == 0)";
					
				}
				
				
				
				if (a instanceof TAPNTransportArc){
					tmp.append("<target ref=\"a"+ transportarcplace.getID() +"\"/>\n");	
					
					if (((TAPNTransition)t).isUrgent()){
						// Dont write guard if urgent transition... (uppaal cant handel it)
						tmp.append("<label kind=\"guard\" x=\"432\" y=\"64\"></label>\n");
					} else {
						tmp.append("<label kind=\"guard\" x=\"432\" y=\"64\">"+ guard +"</label>\n");
					}
					tmp.append("<label kind=\"synchronisation\" x=\"200\" y=\"120\">"+ t.getName() + symbol +"</label>\n");
					tmp.append("<label kind=\"assignment\" x=\"64\" y=\"160\"></label>\n"); // No reset of clock
				}else {
					tmp.append("<target ref=\"a"+ nontransportplaces.get(0).getID() +"\"/>\n");
					if (((TAPNTransition)t).isUrgent()){
						// Dont write guard if urgent transition... (uppaal cant handel it)
						tmp.append("<label kind=\"guard\" x=\"432\" y=\"64\"></label>\n");
					} else {
						tmp.append("<label kind=\"guard\" x=\"432\" y=\"64\">"+ guard +"</label>\n");
					}				
					tmp.append("<label kind=\"synchronisation\" x=\"200\" y=\"120\">"+ t.getName() +  symbol +"</label>\n");
					
					Logger.log(a.getTarget());
					
					if (nontransportplaces.get(0).getName().equals("P_capacity")){
						tmp.append("<label kind=\"assignment\" x=\"10\" y=\"300\">x:=0, g["+ templatenumber +"]:=1</label>\n");
					} else if(a.getSource().getName().equals("P_capacity")) {
						tmp.append("<label kind=\"assignment\" x=\"10\" y=\"200\">x:=0, g["+ templatenumber +"]:=0</label>\n");
					} else {
						tmp.append("<label kind=\"assignment\" x=\"10\" y=\"160\">x:=0</label>\n");
					}
					
					nontransportplaces.remove(0); // Remove used element
				}
				tmp.append("</transition>\n");
				symbol = '?';
			}
			
			
		}
		
		
		tmp.append("</template>");
		
		return tmp;
		
	}
	
	private StringBuffer createTemplate(PrintStream uppaalXML2, Place initialPlace, int templatenumber) {
		
		StringBuffer tmp = new StringBuffer();
		
		// Create the xml for the model
		tmp.append("<template>\n");
		
		//Name
		tmp.append("<name x=\"5\" y=\"5\">@@name@@</name>\n");
		
		//Declaration
		tmp.append("<declaration>\n");
		tmp.append("clock x; \n");		
		tmp.append("</declaration>\n");
		
		//Locations
		for (Place p : model.getPlaces()){
			
			int xcord = 0, ycord = 0;
			
			Location a=null;
			if ((a = model.locations.get(p)) != null){
				xcord= (int)(a.x/2);
				ycord=  (int)(a.y/2);
			}
			
			
			tmp.append("<location id=\"a"+p.getID()+"\" x=\"" + (xcord) +"\" y=\"" + (ycord) +"\">\n");
			
			
			tmp.append("<name x=\"" + (xcord) +"\" y=\"" + (ycord) +"\">"+ p.getName() +"</name>\n");
			
			if (!((TAPNPlace)p).getInvariant().equals("<inf")){
				tmp.append("<label kind=\"invariant\" x=\"-66\" y=\"263\"> x "+ ((TAPNPlace)p).getInvariant().replace("<", "&lt;")+ "</label>");
			}
			
			
			if (((TAPNPlace)p).isUrgent()) {
				tmp.append("<urgent/>");
			}
			
			tmp.append("</location>\n");
			
		}
		
		//Init 
		//tmp.append("<init ref=\"@@init@@\"/>\n"); // TODO - fix this
		tmp.append("<init ref=\"a"+initialPlace.getID()+"\"/>\n");

		//transitions
		for (Transition t : model.getTransitions()){
			
			//Vi ved der maksimalt er en transport arc
			
			Place transportarcplace = null;
			ArrayList<Place> nontransportplaces = new ArrayList<Place>();
			for (Arc a : t.getPostset()){
				
				if (a instanceof TAPNTransportArc){
					transportarcplace = (Place)a.getTarget();
				}else {
					nontransportplaces.add((Place)a.getTarget());
				}
			}
			char symbol = '!';
			
			for (Arc a : t.getPreset()){		
				
				tmp.append("<transition>\n");
				tmp.append("<source ref=\"a"+ a.getSource().getID() +"\"/>\n");				
				
				String guard="";
				String tmp2[] = ((TAPNArc)a).getGuard().split(",");
				
				
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
				
				
				
				if (a instanceof TAPNTransportArc){
					tmp.append("<target ref=\"a"+ transportarcplace.getID() +"\"/>\n");	
					
					if (((TAPNTransition)t).isUrgent()){
						// Dont write guard if urgent transition... (uppaal cant handel it)
						tmp.append("<label kind=\"guard\" x=\"432\" y=\"64\"></label>\n");
					} else {
						tmp.append("<label kind=\"guard\" x=\"432\" y=\"64\">"+ guard +"</label>\n");
					}
					tmp.append("<label kind=\"synchronisation\" x=\"200\" y=\"120\">"+ t.getName() + symbol +"</label>\n");
					tmp.append("<label kind=\"assignment\" x=\"64\" y=\"160\"></label>\n"); // No reset of clock
				}else {
					tmp.append("<target ref=\"a"+ nontransportplaces.get(0).getID() +"\"/>\n");
					if (((TAPNTransition)t).isUrgent()){
						// Dont write guard if urgent transition... (uppaal cant handel it)
						tmp.append("<label kind=\"guard\" x=\"432\" y=\"64\"></label>\n");
					} else {
						tmp.append("<label kind=\"guard\" x=\"432\" y=\"64\">"+ guard +"</label>\n");
					}				
					tmp.append("<label kind=\"synchronisation\" x=\"200\" y=\"120\">"+ t.getName() +  symbol +"</label>\n");
					
					tmp.append("<label kind=\"assignment\" x=\"10\" y=\"160\">x:=0</label>\n");
					
					
					nontransportplaces.remove(0); // Remove used element
				}
				tmp.append("</transition>\n");
				symbol = '?';
			}
			
			
		}
		
		
		tmp.append("</template>");
		
		return tmp;
		
	}
	
	
	private StringBuffer createTemplateReductionNiceDrawing(PrintStream uppaalXML2, Place initialPlace, int templatenumber) {
		Logger.log("GoGoGo");
		StringBuffer tmp = new StringBuffer();

		// Create the xml for the model
		tmp.append("<template>\n");

		//Name
		tmp.append("<name x=\"5\" y=\"5\">@@name@@</name>\n");

		//Declaration
		tmp.append("<declaration>\n");
		tmp.append("clock x; \n");
		tmp.append("</declaration>\n");

		//Locations


		for (Place p : model.getPlaces()){

			int xcord = 0, ycord = 0;

			Location a=null;
			if ((a = model.locations.get(p)) != null){
				xcord= (int)(a.x);
				ycord=  (int)(a.y);
			}

			tmp.append("<location id=\"a"+p.getID()+"\" x=\"" + (xcord) +"\" y=\"" + (ycord) +"\">\n");


			tmp.append("<name x=\"" + (xcord) +"\" y=\"" + (ycord) +"\">"+ p.getName() +"</name>\n");

			if (!((TAPNPlace)p).getInvariant().equals("<inf")){
				tmp.append("<label kind=\"invariant\" x=\"-66\" y=\"263\"> x "+ ((TAPNPlace)p).getInvariant().replace("<", "&lt;")+ "</label>");
			}


			if (((TAPNPlace)p).isUrgent()) {
				tmp.append("<urgent/>");
			}

			tmp.append("</location>\n");

		}

		//Init 
		//tmp.append("<init ref=\"@@init@@\"/>\n"); // TODO - fix this
		tmp.append("<init ref=\"a"+initialPlace.getID()+"\"/>\n");

		//transitions
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
			tmp.append("<label kind=\"guard\" x=\"432\" y=\"64\"></label>\n");				
			tmp.append("<label kind=\"synchronisation\" x=\"200\" y=\"120\">"+ t.getName() +  "!</label>\n");
			tmp.append("<label kind=\"assignment\" x=\"10\" y=\"160\">x:=0</label>\n");
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

			// Symetric reduction
			if (presetPlaceTwo.getSource().getName().equals("P_capacity") && templatenumber > 0){
				if (!(guard.equals(""))){
					guard+="and";
				}
				guard += "(";
				guard+="g[" + 0 + "]";
				for (int i=1; i < templatenumber; i++){
					guard+="+ g[" + i + "]";
				}
				guard += " == 0)";

			}
			
			tmp.append("<transition>\n");
			tmp.append("<source ref=\"a"+ presetPlaceTwo.getSource().getID() +"\"/>\n");
			
			if (presetPlaceTwo instanceof TAPNTransportArc){
				tmp.append("<target ref=\"a"+ postsetPlaceTwo.getTarget().getID() +"\"/>\n");	

				tmp.append("<label kind=\"guard\" x=\"432\" y=\"64\">"+ guard +"</label>\n");

				tmp.append("<label kind=\"synchronisation\" x=\"200\" y=\"120\">"+ t.getName() + "?</label>\n");
				tmp.append("<label kind=\"assignment\" x=\"64\" y=\"160\"></label>\n"); // No reset of clock
			}else {

				tmp.append("<target ref=\"a"+ postsetPlaceTwo.getTarget().getID() +"\"/>\n");
				tmp.append("<label kind=\"guard\" x=\"432\" y=\"64\">"+ guard +"</label>\n");
				tmp.append("<label kind=\"synchronisation\" x=\"200\" y=\"120\">"+ t.getName() + "?</label>\n");

				if ((postsetPlaceTwo.getTarget().getName().equals("P_capacity")) && (presetPlaceTwo.getSource().getName().equals("P_capacity"))){
					tmp.append("<label kind=\"assignment\" x=\"10\" y=\"300\">x:=0, g["+ templatenumber +"]:=0, g["+ templatenumber +"]:=1</label>\n");
				}else if (postsetPlaceTwo.getTarget().getName().equals("P_capacity")){
					tmp.append("<label kind=\"assignment\" x=\"10\" y=\"300\">x:=0, g["+ templatenumber +"]:=1</label>\n");
				} else if(presetPlaceTwo.getSource().getName().equals("P_capacity") ) {
					tmp.append("<label kind=\"assignment\" x=\"10\" y=\"200\">x:=0, g["+ templatenumber +"]:=0</label>\n");
				} else {
					tmp.append("<label kind=\"assignment\" x=\"10\" y=\"160\">x:=0</label>\n");
				}


			}
			tmp.append("</transition>\n");
			

		}
	

		tmp.append("</template>");

		return tmp;

	}

	private StringBuffer createTemplateReductionNiceDrawingRemoveUnused(PrintStream uppaalXML2, Place initialPlace, int templatenumber) {
		StringBuffer tmp = new StringBuffer();

		// Create the xml for the model
		tmp.append("<template>\n");

		//Name
		tmp.append("<name x=\"5\" y=\"5\">@@name@@</name>\n");

		//Declaration
		if (templatenumber != 0){
			tmp.append("<declaration>\n");
			tmp.append("clock x; \n");
			tmp.append("</declaration>\n");
		}
		//Locations
		ArrayList<Place> id = new ArrayList<Place>();
	
		
		for (Place p : model.getPlaces()){
			
			id.add(p);
			
			Logger.log(id.indexOf(p));
			
			int xcord = 0, ycord = 0;

			Location a=null;
			if ((a = model.locations.get(p)) != null){
				xcord= (int)(a.x);
				ycord=  (int)(a.y);
			}
			
			if ((templatenumber==0 && (p.getName().contains("_im") || p.getName().contains("P_lock")) || (templatenumber != 0 && !(p.getName().contains("_im") || p.getName().contains("P_lock"))))){
				tmp.append("<location id=\"a"+p.getID()+"\" x=\"" + (xcord) +"\" y=\"" + (ycord) +"\">\n");
				tmp.append("<name x=\"" + (xcord) +"\" y=\"" + (ycord) +"\">"+ p.getName() +"</name>\n");
				
				if (!((TAPNPlace)p).getInvariant().equals("<inf") && templatenumber!=0){
					tmp.append("<label kind=\"invariant\" x=\"-66\" y=\"263\"> x "+ ((TAPNPlace)p).getInvariant().replace("<", "&lt;")+ "</label>");
				}
				
				if (((TAPNPlace)p).isUrgent()) {
					tmp.append("<urgent/>");
				}
				if (templatenumber==0 && !p.getName().equals("P_lock")){
					tmp.append("<committed/>");
				}
				tmp.append("</location>\n");
			}
		}

		
		
		//Init 
		tmp.append("<init ref=\"a"+initialPlace.getID()+"\"/>\n");

		//transitions
		for (Transition t : model.getTransitions()){
		
			Arc presetPlaceOne = t.getPreset().get(0);
			Arc presetPlaceTwo = t.getPreset().get(1);

			Arc postsetPlaceOne = t.getPostset().get(0);
			Arc postsetPlaceTwo = t.getPostset().get(1);

			Logger.log(id.indexOf(postsetPlaceOne));
			
			//We let presetPlaceOne and postsetPlaceTwo be the locking chanin.
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

			if (templatenumber == 0){

				// Add first arc, we know this is in the chain. 
				tmp.append("<transition>\n");
				tmp.append("<source ref=\"a"+ presetPlaceOne.getSource().getID() +"\"/>\n");
				tmp.append("<target ref=\"a"+ postsetPlaceOne.getTarget().getID() +"\"/>\n");
				tmp.append("<label kind=\"guard\" x=\"432\" y=\"64\"></label>\n");				
				tmp.append("<label kind=\"synchronisation\" x=\"200\" y=\"120\">"+ t.getName() +  "!</label>\n");
				tmp.append("<label kind=\"assignment\" x=\"10\" y=\"160\"></label>\n");
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

				// Symetric reduction
				if (presetPlaceTwo.getSource().getName().equals("P_capacity") && templatenumber > 0){
					if (!(guard.equals(""))){
						guard+="and";
					}
					guard += "(";
					guard+="g[" + 0 + "]";
					for (int i=1; i < templatenumber; i++){
						guard+="+ g[" + i + "]";
					}
					guard += " == 0)";

				}

				tmp.append("<transition>\n");
				tmp.append("<source ref=\"a"+ presetPlaceTwo.getSource().getID() +"\"/>\n");

				if (presetPlaceTwo instanceof TAPNTransportArc){
					tmp.append("<target ref=\"a"+ postsetPlaceTwo.getTarget().getID() +"\"/>\n");	

					tmp.append("<label kind=\"guard\" x=\"432\" y=\"64\">"+ guard +"</label>\n");

					tmp.append("<label kind=\"synchronisation\" x=\"200\" y=\"120\">"+ t.getName() + "?</label>\n");
					tmp.append("<label kind=\"assignment\" x=\"64\" y=\"160\"></label>\n"); // No reset of clock
				}else {

					tmp.append("<target ref=\"a"+ postsetPlaceTwo.getTarget().getID() +"\"/>\n");
					tmp.append("<label kind=\"guard\" x=\"432\" y=\"64\">"+ guard +"</label>\n");
					tmp.append("<label kind=\"synchronisation\" x=\"200\" y=\"120\">"+ t.getName() + "?</label>\n");

					if ((postsetPlaceTwo.getTarget().getName().equals("P_capacity")) && (presetPlaceTwo.getSource().getName().equals("P_capacity"))){
						tmp.append("<label kind=\"assignment\" x=\"10\" y=\"300\">x:=0, g["+ templatenumber +"]:=0, g["+ templatenumber +"]:=1, update("+ id.indexOf(presetPlaceTwo) +", "+ id.indexOf(postsetPlaceTwo)+")</label>\n");
					}else if (postsetPlaceTwo.getTarget().getName().equals("P_capacity")){
						tmp.append("<label kind=\"assignment\" x=\"10\" y=\"300\">x:=0, g["+ templatenumber +"]:=1, update("+ id.indexOf(presetPlaceTwo) +", "+ id.indexOf(postsetPlaceTwo)+")</label>\n");
					} else if(presetPlaceTwo.getSource().getName().equals("P_capacity") ) {
						tmp.append("<label kind=\"assignment\" x=\"10\" y=\"200\">x:=0, g["+ templatenumber +"]:=0, update("+ id.indexOf(presetPlaceTwo) +", "+ id.indexOf(postsetPlaceTwo)+")</label>\n");
					} else {
						tmp.append("<label kind=\"assignment\" x=\"10\" y=\"160\">x:=0, update("+ id.indexOf(presetPlaceTwo) +", "+ id.indexOf(postsetPlaceTwo)+")</label>\n");
					}


				}
				tmp.append("</transition>\n");
			}

		}
	

		tmp.append("</template>");

		return tmp;

	}

	
	private StringBuffer createTemplateNiceDrawing(PrintStream uppaalXML2, Place initialPlace, int templatenumber) {

		StringBuffer tmp = new StringBuffer();

		// Create the xml for the model
		tmp.append("<template>\n");

		//Name
		tmp.append("<name x=\"5\" y=\"5\">@@name@@</name>\n");

		//Declaration
		tmp.append("<declaration>\n");
		tmp.append("clock x; \n");
		tmp.append("</declaration>\n");

		//Locations


		for (Place p : model.getPlaces()){

			int xcord = 0, ycord = 0;

			Location a=null;
			if ((a = model.locations.get(p)) != null){
				xcord= (int)(a.x);
				ycord=  (int)(a.y);
			}

			tmp.append("<location id=\"a"+p.getID()+"\" x=\"" + (xcord) +"\" y=\"" + (ycord) +"\">\n");


			tmp.append("<name x=\"" + (xcord) +"\" y=\"" + (ycord) +"\">"+ p.getName() +"</name>\n");

			if (!((TAPNPlace)p).getInvariant().equals("<inf")){
				tmp.append("<label kind=\"invariant\" x=\"-66\" y=\"263\"> x "+ ((TAPNPlace)p).getInvariant().replace("<", "&lt;")+ "</label>");
			}


			if (((TAPNPlace)p).isUrgent()) {
				tmp.append("<urgent/>");
			}

			tmp.append("</location>\n");

		}

		//Init 
		//tmp.append("<init ref=\"@@init@@\"/>\n"); // TODO - fix this
		tmp.append("<init ref=\"a"+initialPlace.getID()+"\"/>\n");

		//transitions
		for (Transition t : model.getTransitions()){
		
			Arc presetPlaceOne = t.getPreset().get(0);
			Arc presetPlaceTwo = t.getPreset().get(1);

			Arc postsetPlaceOne = t.getPostset().get(0);
			Arc postsetPlaceTwo = t.getPostset().get(1);

			//We let presetPlaceOne and postsetPlaceTwo be the locking chanin.
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

			// Add first arc, we know this is in the chain. 
			tmp.append("<transition>\n");
			tmp.append("<source ref=\"a"+ presetPlaceOne.getSource().getID() +"\"/>\n");
			tmp.append("<target ref=\"a"+ postsetPlaceOne.getTarget().getID() +"\"/>\n");
			tmp.append("<label kind=\"guard\" x=\"432\" y=\"64\"></label>\n");				
			tmp.append("<label kind=\"synchronisation\" x=\"200\" y=\"120\">"+ t.getName() +  "!</label>\n");
			tmp.append("<label kind=\"assignment\" x=\"10\" y=\"160\">x:=0</label>\n");
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

			// Symetric reduction
			tmp.append("<transition>\n");
			tmp.append("<source ref=\"a"+ presetPlaceTwo.getSource().getID() +"\"/>\n");
			
			if (presetPlaceTwo instanceof TAPNTransportArc){
				tmp.append("<target ref=\"a"+ postsetPlaceTwo.getTarget().getID() +"\"/>\n");	

				tmp.append("<label kind=\"guard\" x=\"432\" y=\"64\">"+ guard +"</label>\n");

				tmp.append("<label kind=\"synchronisation\" x=\"200\" y=\"120\">"+ t.getName() + "?</label>\n");
				tmp.append("<label kind=\"assignment\" x=\"64\" y=\"160\"></label>\n"); // No reset of clock
			}else {

				tmp.append("<target ref=\"a"+ postsetPlaceTwo.getTarget().getID() +"\"/>\n");
				tmp.append("<label kind=\"guard\" x=\"432\" y=\"64\">"+ guard +"</label>\n");
				tmp.append("<label kind=\"synchronisation\" x=\"200\" y=\"120\">"+ t.getName() + "?</label>\n");
				tmp.append("<label kind=\"assignment\" x=\"64\" y=\"160\">x=0</label>\n"); // No reset of clock

			}
			tmp.append("</transition>\n");
			

		}
	

		tmp.append("</template>");

		return tmp;

	}
	
	public void transformQueriesToUppaal(int numberOfEkstraTokens, TAPNQuery inputQuery, PrintStream stream) throws Exception {
		stream.println("// Autogenerated by the TAPAAL (www.tapaal.net)");
		stream.println("");

		stream.println("/*");
		stream.println(" " + inputQuery.toString() + " " );
		stream.println("*/");

		StandardTranslationQueryVisitor visitor = new StandardTranslationQueryVisitor(model.getNumberOfTokens() + numberOfEkstraTokens);
		stream.println(visitor.getUppaalQueryFor(inputQuery));

	}
	
	
	
	
}
