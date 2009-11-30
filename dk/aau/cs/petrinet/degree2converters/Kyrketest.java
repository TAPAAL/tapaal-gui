package dk.aau.cs.petrinet.degree2converters;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.aau.cs.debug.Logger;
import dk.aau.cs.petrinet.Location;
import dk.aau.cs.petrinet.Place;
import dk.aau.cs.petrinet.PlaceTransitionObject;
import dk.aau.cs.petrinet.TAPN;
import dk.aau.cs.petrinet.TAPNArc;
import dk.aau.cs.petrinet.TAPNPlace;
import dk.aau.cs.petrinet.TAPNTransition;
import dk.aau.cs.petrinet.TAPNTransportArc;
import dk.aau.cs.petrinet.Transition;
import dk.aau.cs.petrinet.Arc;
/*  Copyright (c) 2009, Kenneth Yrke Jørgensen <kyrke@cs.aau.dk>
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    * Neither the name of the <ORGANIZATION> nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.   
 */
public class Kyrketest {

	TAPN model=null;
	HashMap<TAPNPlace, TAPNPlace> oldToNewPlacesMap = new HashMap<TAPNPlace, TAPNPlace>();

	PrintStream uppaalXML = null;
	int numberOfEkstraTokens = 0;

	public Kyrketest(TAPN model) {
		this.model = model;
	}

	public Kyrketest(TAPN model, PrintStream uppaalXML, int numberoftokens) {
		this.model = model;
		this.uppaalXML = uppaalXML;
		this.numberOfEkstraTokens = numberoftokens;
	}

	public TAPN transform(TAPN model) throws Exception{

		TAPN toReturn= new TAPN();

		// Add all old places
		for (Place p : model.getPlaces()){
			TAPNPlace tmp = new TAPNPlace(p.getName(), ((TAPNPlace)p).getInvariant(), p.getCapacity());

			oldToNewPlacesMap.put((TAPNPlace)p, tmp);
			toReturn.addPlace(tmp);
		}

//		Create the P_lock place.
		TAPNPlace lock = new TAPNPlace("P_lock", "", 0);
		toReturn.addPlace(lock);	

		//TAPNPlace capacity = new TAPNPlace("P_capacity", "", 0);
		//toReturn.addObject(capacity);	

		// Add a tonek to the lock place 
		toReturn.tokens.add(lock);


		for (Transition t : model.getTransitions()){

			//If degree-2 or less then keep it		
			if (t.getPreset().size() <= 2 && t.getPostset().size() <=2){


				TAPNTransition transition = new TAPNTransition(t.getName());
				toReturn.addTransition(transition);

				for (Arc a : t.getPreset()){
					toReturn.add(cloneArc(a,transition));
				}

				for (Arc a : t.getPostset()){			
					toReturn.add(cloneArc(a, transition));	
				}
				continue;
			} else{
				// The normal way

				Logger.log("Making normal degree-2 reduction");
			}

		}

		// Move tokesn 
		for (Place p : model.tokens){
			toReturn.tokens.add(oldToNewPlacesMap.get(p));
		}

		return toReturn;
	}

	private Arc cloneArc(Arc a, TAPNTransition newtrans){
		Arc toReturn=null;

		PlaceTransitionObject source=a.getSource();
		PlaceTransitionObject target=a.getTarget();

		if (a instanceof TAPNArc){

			String guard = ((TAPNArc)a).getGuard();		
			toReturn = new TAPNArc(oldToNewPlacesMap.get(source), newtrans, guard);

		}else if (a instanceof Arc){
			toReturn = new Arc(newtrans, oldToNewPlacesMap.get(target));
		}
		//Handle transport arc

		if (toReturn==null){
			System.err.println();
		}

		return toReturn;
	}




	public void transformToUppaal(PrintStream stream, int numberoftokens){

		this.numberOfEkstraTokens = numberoftokens;
		
		uppaalXML = stream;

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

		uppaalXML.println("bool g[" + tokens.size()+ "] = {"+arrayinit+"};");
		uppaalXML.println("bool lock=false;");

		uppaalXML.println("</declaration>");


		int numberOfTokens = tokens.size();

		//add capasity place to the tokesen for each ekstra token, 

		int i = 0;
		for (Place p : tokens){
			StringBuffer a;

			a = createTemplateReduction(uppaalXML, p, i);

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

		Logger.log("Creating stuff");
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


		for (Place p : model.getPlaces()){

			int xcord = 0, ycord = 0;

			Location a=null;
			if ((a = model.locations.get(p)) != null){
				//xcord= (int)(a.x);
				//ycord=  (int)(a.y);
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

			
			if (t.getPreset().size()==1 && templatenumber!=0){
				tmp.append(createTransition(t.getPreset().get(0), t.getPostset().get(0), t.getName(),templatenumber));
			}

			if (t.getPreset().size()>=2){
				
				Arc presetPlaceOne = t.getPreset().get(0);
				Arc presetPlaceTwo = t.getPreset().get(1);

				Arc postsetPlaceOne = t.getPostset().get(0);
				Arc postsetPlaceTwo = t.getPostset().get(1);

				if ( (!((presetPlaceOne.getSource().getName().contains("_im")) || (presetPlaceOne.getSource().getName().equals("P_lock"))) ||
						!((presetPlaceTwo.getSource().getName().contains("_im")) || (presetPlaceTwo.getSource().getName().equals("P_lock")))) && 
						templatenumber!=0){

					Logger.log("Hmm the new way " + templatenumber );
					//It the new way 
					tmp.append(createTransition(t.getPreset().get(0), t.getPostset().get(0), t.getName(), templatenumber, '!'));
					tmp.append(createTransition(t.getPreset().get(1), t.getPostset().get(1), t.getName(),templatenumber, '?'));

				} else {
					//Its the old way

					Logger.log("The old way!!");

				}

			}



			/*
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
			}*/

		}


		tmp.append("</template>");

		return tmp;

	}

	private StringBuffer createTransition(Arc arc, Arc arc2, String name, int templatenumber, char syncchar) {

		StringBuffer tmp = new StringBuffer();
//		The second arc
		String guard="";
		String tmp2[] = ((TAPNArc)arc).getGuard().split(",");

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
		if (arc.getSource().getName().equals("P_capacity") && templatenumber > 0){
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

		if (!(guard.equals(""))){
			guard+=" and ";
		}

		guard+="lock==0";

		tmp.append("<transition>\n");
		tmp.append("<source ref=\"a"+ arc.getSource().getID() +"\"/>\n");

		if (arc instanceof TAPNTransportArc){
			tmp.append("<target ref=\"a"+ arc2.getTarget().getID() +"\"/>\n");	

			tmp.append("<label kind=\"guard\" x=\"432\" y=\"64\">"+ guard +"</label>\n");

			tmp.append("<label kind=\"synchronisation\" x=\"200\" y=\"120\">"+ name + syncchar + "</label>\n");
			tmp.append("<label kind=\"assignment\" x=\"64\" y=\"160\"></label>\n"); // No reset of clock
		}else {

			tmp.append("<target ref=\"a"+ arc2.getTarget().getID() +"\"/>\n");
			tmp.append("<label kind=\"guard\" x=\"432\" y=\"64\">"+ guard +"</label>\n");
			tmp.append("<label kind=\"synchronisation\" x=\"200\" y=\"120\">"+ name + syncchar + "</label>\n");

			if ((arc2.getTarget().getName().equals("P_capacity")) && (arc.getSource().getName().equals("P_capacity"))){
				tmp.append("<label kind=\"assignment\" x=\"10\" y=\"300\">x:=0, g["+ templatenumber +"]:=0, g["+ templatenumber +"]:=1</label>\n");
			}else if (arc2.getTarget().getName().equals("P_capacity")){
				tmp.append("<label kind=\"assignment\" x=\"10\" y=\"300\">x:=0, g["+ templatenumber +"]:=1</label>\n");
			} else if(arc.getSource().getName().equals("P_capacity") ) {
				tmp.append("<label kind=\"assignment\" x=\"10\" y=\"200\">x:=0, g["+ templatenumber +"]:=0</label>\n");
			} else {
				tmp.append("<label kind=\"assignment\" x=\"10\" y=\"160\">x:=0</label>\n");
			}


		}
		tmp.append("</transition>\n");

		return tmp;
	}

	private StringBuffer createTransition(Arc arc, Arc arc2, String name, int templatenumber) {

		StringBuffer tmp = new StringBuffer();
//		The second arc
		String guard="";
		String tmp2[] = ((TAPNArc)arc).getGuard().split(",");

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
		if (arc.getSource().getName().equals("P_capacity") && templatenumber > 0){
			if (!(guard.equals(""))){
				guard+=" and ";
			}
			guard += "(";
			guard+="g[" + 0 + "]";
			for (int i=1; i < templatenumber; i++){
				guard+="+ g[" + i + "]";
			}
			guard += " == 0)";

		}


		if (!(guard.equals(""))){
			guard+=" and ";
		}

		guard+="lock==0";

		tmp.append("<transition>\n");
		tmp.append("<source ref=\"a"+ arc.getSource().getID() +"\"/>\n");


		tmp.append("<target ref=\"a"+ arc2.getTarget().getID() +"\"/>\n");
		tmp.append("<label kind=\"guard\" x=\"432\" y=\"64\">"+ guard +"</label>\n");

		if (arc instanceof TAPNTransportArc){

			//tmp.append("<label kind=\"synchronisation\" x=\"200\" y=\"120\">"+ name + syncchar + "</label>\n");
			tmp.append("<label kind=\"assignment\" x=\"64\" y=\"160\"></label>\n"); // No reset of clock
		}else {

			//tmp.append("<label kind=\"synchronisation\" x=\"200\" y=\"120\">"+ name + syncchar + "</label>\n");

			if ((arc2.getTarget().getName().equals("P_capacity")) && (arc.getSource().getName().equals("P_capacity"))){
				tmp.append("<label kind=\"assignment\" x=\"10\" y=\"300\">x:=0, g["+ templatenumber +"]:=0, g["+ templatenumber +"]:=1</label>\n");
			}else if (arc2.getTarget().getName().equals("P_capacity")){
				tmp.append("<label kind=\"assignment\" x=\"10\" y=\"300\">x:=0, g["+ templatenumber +"]:=1</label>\n");
			} else if(arc.getSource().getName().equals("P_capacity") ) {
				tmp.append("<label kind=\"assignment\" x=\"10\" y=\"200\">x:=0, g["+ templatenumber +"]:=0</label>\n");
			} else {
				tmp.append("<label kind=\"assignment\" x=\"10\" y=\"160\">x:=0</label>\n");
			}


		}
		tmp.append("</transition>\n");

		return tmp;
	}

	String transformQueriesToUppaal(int numberOfTemplates, String querie) throws Exception{

		//String toReturn=null;
		//TODO - Sanity validation, are the uses names in the qyerie in the model?

		String expandpart=null;

		querie.trim(); // Remove ending and beginning spaces

		String quantifier = querie.substring(0, 3);

		//Get the id's that needs to be changed
		Pattern p = Pattern.compile("[ ][a-zA-Z](([a-zA-Z])*([0-9])*(_)*)*[ ]");
		//Pattern p = Pattern.compile("[ ][[a-zA-Z](([a-zA-Z])*([0-9])*(_)*)*]*[ ]");
		//Pattern p = Pattern.compile("[ ][[[a-zA-Z](([a-zA-Z])*([0-9])*(_)*)*]]*[ ]");
		Matcher m = p.matcher(querie);

		//Make a copy of the querie that we makes the changes in
		expandpart = querie.toString();

		// TODO put () around the org part, else using or wil fail when appending lock part.

		ArrayList<String> ident = new ArrayList<String>();


		while (m.find()){
			boolean found = false;
			String i = m.group().trim();

			for (String tmp : ident){

				if (tmp.equals(i)){
					found=true;
					break;		
				}
				//Not in the set add it	
			}
			if (!found){
				ident.add(i);
			}

		}

		for (String a : ident){

//			Generate a new replacement
			String tmp = a.trim();

			StringBuffer newstring = new StringBuffer();

			newstring.append(" (");
			//Generate the new string 
			int i =0;
			for (i=1; i < numberOfTemplates-1; i++){ //Start from 1 no lock token
				newstring.append("Token" +i + "." + tmp + " + ");
			}
			newstring.append("Token" +i + "." + tmp);
			newstring.append(") ");

//			Replace string 
			expandpart=expandpart.replaceAll(" "+tmp+" ", newstring.toString());		

		}


		//Translation
		if (quantifier.substring(1, 3).equals("<>")){

			expandpart=expandpart.replace("<>", "<>(");
			expandpart=expandpart.concat(") and "); // The stuff before the lock sum part


		} else { // This is the "[]" case
			expandpart=expandpart.replace("[]", "[](");
			expandpart=expandpart.concat(") or !"); // The stuff before the lock sum part

		}
		//Lock part 

		StringBuffer toReturn = new StringBuffer(expandpart);

		/*int i=0;
		toReturn.append("(( ");
		for (i=0; i < numberOfTemplates-1; i++){
			toReturn.append("Token" + i + ".P_lock + " );
		}*/
		toReturn.append("(Token0.P_lock==1)");

		return toReturn.toString();
	}

	public void transformQueriesToUppaal(int numberOfEkstraTokens, String inputQuery, PrintStream stream) throws Exception {
		stream.println("// Autogenerated by the TAPAAL (www.tapaal.net)");
		stream.println("");

		stream.println("/*");
		stream.println(" " + inputQuery + " " );
		stream.println("*/");

		stream.println(transformQueriesToUppaal(numberOfEkstraTokens + model.tokens.size(), inputQuery));

	}

}
