package dk.aau.cs.petrinet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pipe.gui.undo.AddPetriNetObjectEdit;
import pipe.io.NewTransitionRecord;

import com.sun.org.apache.bcel.internal.generic.GETSTATIC;

import dk.aau.cs.petrinet.degree2converters.CapacityDegree2Converter;
import dk.aau.cs.petrinet.degree2converters.NaiveDegree2Converter;
import dk.aau.cs.petrinet.degree2converters.degree2minimal;

/*  Copyright (c) 2009, Kenneth Yrke Jørgensen <kyrke@cs.aau.dk>, Joakim Byg <jokke@cs.aau.dk>
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    * Neither the name of the TAPAAL nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.   
 */ 

public class TAPN extends PetriNet {

	LinkedList<Place> places = new LinkedList<Place>();
	LinkedList<Transition> transitions = new LinkedList<Transition>();

	LinkedList<Arc> arcs = new LinkedList<Arc>();
	
	public LinkedList<Place> tokens = new LinkedList<Place>();//Add each place for each token it has.

	public HashMap<PlaceTransitionObject, Location> locations = new HashMap<PlaceTransitionObject, Location>(); 

	
	private Degree2Converter degree2converter = new NaiveDegree2Converter();
		
		
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
			for (i=0; i < numberOfTemplates-1; i++){
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

		int i=0;
		toReturn.append("(( ");
		for (i=0; i < numberOfTemplates-1; i++){
			toReturn.append("Token" + i + ".P_lock + " );
		}
		toReturn.append("Token" + i + ".P_lock" );

		toReturn.append(" )==1 )");

		return toReturn.toString();
	}

	public void transformQueriesToUppaal(int numberOfEkstraTokens, String inputQuery, PrintStream stream) throws Exception {
		stream.println("// Autogenerated by the TAPAAL (www.tapaal.net)");
		stream.println("");

		stream.println("/*");
		stream.println(" " + inputQuery + " " );
		stream.println("*/");

		stream.println(transformQueriesToUppaal(numberOfEkstraTokens + tokens.size(), inputQuery));

	}


	public void addObject(TAPNPlace p){
		places.add(p); 								  							
	}

	public void addObject(TAPNTransition t){
		transitions.add(t); 
	}

	public void addLocation(PlaceTransitionObject pto, float x, float y){
		locations.put(pto, new Location(x, y));
	}

	public void addLocation(PlaceTransitionObject pto, Location l) {
		locations.put(pto, l);

	}


	public void addObject(Arc a) throws Exception{

		PlaceTransitionObject source = a.getSource();
		PlaceTransitionObject target = a.getTarget();

		if (source == null || target== null){
			throw new Exception("Source or tageet cant be null");
		}
		
		// Check that source and target exist
		if (!(places.contains(source) || transitions.contains(source))){
			throw new Exception("Source not found");
		}

		if (!(places.contains(target) || transitions.contains(target))){
			throw new Exception("Target not found");	
		}


		if (a instanceof TAPNTransportArc) {

			// TODO - check intermediate
			TAPNTransportArc tmp = (TAPNTransportArc)a;

			TAPNTransition tmp2 = tmp.getIntermediate();

			tmp2.addPreset(a);
			tmp2.addPostset(a);

			arcs.add(tmp);


		} else if (a instanceof TAPNArc) {

			// Is arc consistent and valid?

			if (!(source instanceof Place && target instanceof Transition)){
				throw new Exception("Invalid source and target for this arc type");
			}

			TAPNArc temp = new TAPNArc((TAPNArc)a);

			arcs.add(temp);			

		} else if (a instanceof Arc ) {
			if (!(source instanceof Transition && target instanceof Place)){
				throw new Exception("Invalid source and target for this arc type");
			}

			Arc temp = new Arc(a);

			arcs.add(temp);


		} else {
			throw new Exception("Arc type not supported");
		}

		// Update model (places and transitions)
		source.addPostset(a);
		target.addPreset(a);


	}



	public void removeObject(PlaceTransitionObject pt) throws Exception{
		//Check that pt is in model, else cast an exception

		if (!(places.contains(pt) || transitions.contains(pt))){
			throw new Exception("PlaceTransition is not in model");
		}

		//Remove the arcs gowing to and from this place
		// and remove the arc from the place/transition from the other end
		LinkedList<Arc> tmpPreset = (LinkedList<Arc>) pt.getPreset();
		LinkedList<Arc> tmpPostset = (LinkedList<Arc>) pt.getPostset();


		//Remove all arcs pointing to this place/tranistion object
		if (pt instanceof TAPNTransition){
			transitions.remove((Transition)pt);
		} else if (pt instanceof Place ) {
			places.remove((Place)pt);
		} 

		// Remove arcs from preset/postset of place transition objects
		for (Arc a : tmpPreset){
			a.getSource().removeArc(a);
			this.removeObject(a);
		}
		for (Arc a : tmpPostset){
			a.getTarget().removeArc(a);
			this.removeObject(a);
		} 


	}

	public boolean removeObject(Arc a){

		PlaceTransitionObject source= a.getSource();
		PlaceTransitionObject target = a .getTarget();

		source.removeArc(a);
		target.removeArc(a);

		if (a instanceof TAPNTransportArc){
			TAPNTransportArc tmp = (TAPNTransportArc)a;
			TAPNTransition tmp2 = tmp.getIntermediate();
			tmp2.removeArc(a);
			tmp2.removeArc(a);
		}

		return arcs.remove(a);
	}



	public List<Place> getPlaces(){
		return (List<Place>) places.clone();
	}

	public List<Transition> getTransitions(){
		return (List<Transition>) transitions.clone();
	}

	public List<Arc> getArcs(){
		return (List<Arc>) arcs.clone();
	}


	public void convertToConservative() throws Exception{

		// Add capacity place

		TAPNPlace capacity = new TAPNPlace("P_capacity", "", 0);
		addObject(capacity);

		//Some styling
		locations.put(capacity, new Location(100,400));

		//For all trantions add arcs to capacity 

		List<Transition> transitions = getTransitions();

		for (Transition t : transitions) {

			int difference = t.getPostset().size() - t.getPreset().size();


			if (difference < 0){
				// Add outgowing arcs from trantions to capacity
				for (int i=0; i > difference; i--) {
					//Add transition
					Arc tmp = new Arc();
					tmp.setSource(t);
					tmp.setTarget(capacity);

					addObject(tmp);

				}


			} else if (difference > 0){
				// Add ingowing arcs from trantions to cacacity

				for (int i=0; i< difference; i++) {
					//Add transition

					TAPNArc tmp = new TAPNArc("");
					tmp.setSource(capacity);
					tmp.setTarget(t);

					addObject(tmp);

				}

			} 

		}

	}
	
	
	
	public TAPN convertToDegree2() throws Exception{
		
		return degree2converter.transform(this);
		
	}
	
	
	public TAPN convertToDegree2(String method) throws Exception{
	
		//TODO - check that net is concervatative
		if (method.equals("capacity")){
			degree2converter = new CapacityDegree2Converter();
			return convertToDegree2();
		} else if (method.equals("minimal")){
			degree2converter = new degree2minimal();
			return convertToDegree2();
		}else {
			degree2converter = new NaiveDegree2Converter();
			return convertToDegree2();
		}
		
	}		
	


	public void exportToPIPExml(OutputStream o){

		PrintStream output = new PrintStream(o);

		//output.println("<!-- This file is generated by the TAPN tool -->");
		//output.println("<!-- -- >");
		//output.println("");

		output.println("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>");

		output.println("<pnml>");
		output.println("<net id=\"Net-One\" type=\"P/T net\">");

		for (Place p : getPlaces()){
			//Begin places
			Location l = locations.get(p);
			if (l == null ){
				l = new Location(10,10);
				System.err.println("No location found for " + p);
			}

			output.println("<place id=\""+ p.getID() +"\">");
			output.println("<graphics>");
			output.println("<position x=\""+ l.getX() +"\" y=\""+ l.getY() +"\"/>");
			output.println("</graphics>");

			output.println("<name>");
			
			// XXX hack to mark urgent places
			if (((TAPNPlace)p).isUrgent){
				output.println("<value>"+ p.getName() +"!!!</value>");
			} else {
				output.println("<value>"+ p.getName() +"</value>");
			}
			
			output.println("<graphics>");
			output.println("<offset x=\"-5.0\" y=\"35.0\"/>");
			output.println("</graphics>");
			output.println("</name>");

			output.println("<initialMarking>");
			
			//Get the number of tokens
			int numberOfTokens = 0;
			for (Place p2 : tokens){
				if (p2 == p){
					numberOfTokens++;
				}
			}
			
			output.println("<value>"+numberOfTokens+"</value>");
			
			
			output.println("<graphics>");
			output.println("<offset x=\"0.0\" y=\"0.0\"/>");
			output.println("</graphics>");
			output.println("</initialMarking>");
			output.println("<capacity>");
			output.println("<value>0</value>");
			output.println("</capacity>");

			output.println("<invariant>");
			output.println("<value>"+((TAPNPlace)p).getInvariant().replace("<", "&lt;")+"</value>");
			output.println("</invariant>");
			output.println("</place>");
		}

		for (Transition t : getTransitions()){
			//Add transitions
			Location l = locations.get(t);
			if (l == null ){
				l = new Location(10,10);
				System.err.println("No location found for " + t);
			}

			output.println("<transition id=\""+ t.getID() +"\">");
			output.println("<graphics>");
			output.println("<position x=\""+ l.getX() +"\" y=\""+ l.getY() +"\"/>");
			output.println("</graphics>");
			output.println("<name>");
			output.println("<value>"+ t.getName() +"</value>");
			output.println("<graphics>");
			output.println("<offset x=\"-5.0\" y=\"35.0\"/>");
			output.println("</graphics>");
			output.println("</name>");
			output.println("<orientation>");
			output.println("<value>0</value>");
			output.println("</orientation>");
			output.println("<rate>");
			output.println("<value>1.0</value>");
			output.println("</rate>");
			output.println("<timed>");
			
			if (((TAPNTransition)t).getisUrgens()){
				output.println("<value>true</value>"); // XXX - just a havck until PIPE/TAPN supports urgent transitions
			} else {
				output.println("<value>false</value>");
			}
			
			output.println("</timed>");
			output.println("<infiniteServer>");
			output.println("<value>false</value>");
			output.println("</infiniteServer>");
			output.println("<priority>");
			output.println("<value>1</value>");
			output.println("</priority>");
			output.println("</transition>");


		}


		// Arcs

		for (Arc a : getArcs()){
			if (!(a instanceof TAPNTransportArc)){
				output.println("<arc id=\""+ a.getSource().getID()  +" to "+ a.getTarget().getID() +"\" source=\""+ a.getSource().getID() +"\" target=\""+ a.getTarget().getID() +"\">");
				output.println("<graphics/>");
				output.println("<inscription>");

				String tmp = "";
				if (a instanceof TAPNArc) {
					tmp = ((TAPNArc)a).getGuard();
				}

				output.println("<value>"+ tmp +"</value>");
				output.println("<graphics/>");
				output.println("</inscription>");
				output.println("<tagged>");
				output.println("<value>false</value>");
				output.println("</tagged>");
				output.println("<type value=\"timed\" />");
				output.println("</arc>");
			} else {
				System.out.println("kyrke: Printing transport ARC");


				output.println("<arc id=\""+ a.getSource().getID()  +" to "+ ((TAPNTransportArc)a).getIntermediate().getID() +"\" source=\""+ a.getSource().getID() +"\" target=\""+ ((TAPNTransportArc)a).getIntermediate().getID() +"\">");
				output.println("<graphics/>");
				output.println("<inscription>");

				String tmp = "";
				if (a instanceof TAPNArc) {
					tmp = ((TAPNArc)a).getGuard();
				}

				output.println("<value>"+ tmp + ":1" +  "</value>");
				output.println("<graphics/>");
				output.println("</inscription>");
				output.println("<tagged>");
				output.println("<value>false</value>");
				output.println("</tagged>");
				output.println("<type value=\"transport\"/>");
				output.println("</arc>");		


				//Arc 2
				output.println("<arc id=\""+ ((TAPNTransportArc)a).getIntermediate().getID()  +" to "+ a.getTarget().getID() +"\" source=\""+ ((TAPNTransportArc)a).getIntermediate().getID() +"\" target=\""+ a.getTarget().getID() +"\">");
				output.println("<graphics/>");
				output.println("<inscription>");
				output.println("<value>"+ tmp + ":1" +  "</value>");
				output.println("<graphics/>");
				output.println("</inscription>");
				output.println("<tagged>");
				output.println("<value>false</value>");
				output.println("</tagged>");
				output.println("<type value=\"transport\"/>");
				output.println("</arc>");	
			}
		}

		output.println("</net>");
		output.println("</pnml>");



	}
	public void exportToDOT(OutputStream o){


		PrintStream output = new PrintStream(o);

		output.println("## This file is generated by the TAPN tool");
		output.println("## ");
		output.println("");
		output.println("digraph TAPN {");
		// Create the places 



		String list = "";
		for (Place p : getPlaces()){
			list += p.getID()+"; ";
		}

		output.println("node [shape=circle]; " + list);

		list = "";
		for (Transition t : getTransitions()){
			list += t.getID()+"; ";
		}
		output.println("node [shape=box]; " + list);


		for (Arc a : getArcs()){

			output.println(a.getSource().getID()+"->"+a.getTarget().getID()+";");

		}

		output.println("}");
	}


	// XXX
	void debug(TAPN toReturn){
		try {
			toReturn.exportToDOT(new PrintStream(new File("/tmp/test1")));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String command = "/tmp/script.sh";
		try {
			throw new IOException("dasd");
			//	Process child = Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("lal");
	}

	public Place getPlaceByName(String string) {
		for (Place p : places){
			if (p.name.equals(string)){
				return p;
			}
		}
		return null;
	}

	public TAPN convertToDegree2capacity() throws Exception {
		
		return convertToDegree2("capacity");
	}

	public void orderPresetRescrition() {
	
			for (Transition t : this.getTransitions()){
				
				// Order the preset of the transition
				
				List<Arc> preset = t.getPreset();
				
				
				Collections.sort(preset, new Comparator<Arc>(){

					public int compare(Arc arg0, Arc arg1) {
						
						if (arg0 instanceof TAPNArc &&
								arg1 instanceof TAPNArc){
							
							int scorea = 0;
							int scoreb = 0;
							
							TAPNArc a = (TAPNArc)(arg0);
							TAPNArc b = (TAPNArc)(arg1);
							
							//Count the number of resitionsiton
							
							String[] tmp = a.getGuard().split(",");
							
							if (!tmp[0].contains("[0")){
								scorea = scorea+2;
							}
							if (!tmp[1].contains("inf)")){
								scorea++;
							}
							
							
							tmp = b.getGuard().split(",");
							
							if (!tmp[0].contains("[0")){
								scoreb = scoreb+2;
							}
							if (!tmp[1].contains("inf)")){
								scoreb++;
							}
							
							return scoreb-scorea;
						}
						
						
						return 0;
					}
				});
				
			}
		
	}

}
