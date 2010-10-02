package dk.aau.cs.petrinet.degree2converters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import dk.aau.cs.petrinet.Arc;
import dk.aau.cs.petrinet.Degree2Converter;
import dk.aau.cs.petrinet.Location;
import dk.aau.cs.petrinet.Place;
import dk.aau.cs.petrinet.TAPN;
import dk.aau.cs.petrinet.TAPNArc;
import dk.aau.cs.petrinet.TAPNPlace;
import dk.aau.cs.petrinet.TAPNTransition;
import dk.aau.cs.petrinet.TAPNTransportArc;
import dk.aau.cs.petrinet.Transition;
/*  Copyright (c) 2009, Kenneth Yrke Jørgensen <kyrke@cs.aau.dk>
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    * Neither the name of the <ORGANIZATION> nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.   
 */ 
public class CapacityDegree2Converter implements Degree2Converter {

	public TAPN transform(TAPN model) throws Exception {
		HashMap<TAPNPlace, TAPNPlace> oldToNewPlacesMap = new HashMap<TAPNPlace, TAPNPlace>();

		TAPN toReturn = new TAPN();

		// TODO - check that net is concervatative



		// Add all old places
		for (Place p : model.getPlaces()){
			TAPNPlace tmp = new TAPNPlace(p.getName(), ((TAPNPlace)p).getInvariant(), p.getCapacity());

			oldToNewPlacesMap.put((TAPNPlace)p, tmp);
			toReturn.addPlace(tmp);
		}

//		Create the P_lock place.
		TAPNPlace lock = new TAPNPlace("P_lock", "", 0);
		toReturn.addPlace(lock);	

		// Add a tonek to the lock place 
		toReturn.tokens.add(lock);
		
		// Styling
		toReturn.locations.put(lock, new Location(100, 10));

		// For each transitions make the changes
		for (Transition t : model.getTransitions()){
			
			/*
			//If notpresets intersects postset
			List<Place> intersection = new ArrayList<Place>(t.getPresetPlaces());
			intersection.retainAll(t.getPostsetPlaces());
			Logger.log("Size2" + intersection.size());
			
			if ( intersection.size() == 0) {
//				 No cycles, we can move tokens directly
				
				ArrayList<Arc> targets = new ArrayList<Arc>(t.getPostset());
				for (Arc a : t.getPostset()){
					if (a instanceof TAPNTransportArc){
						targets.remove(a);
					}
				}
				
				TAPNTransition transition = new TAPNTransition(t.getName() + "_T0");
				toReturn.addObject(transition);
				
				toReturn.addObject(new TAPNArc(lock, transition, ""));
				
				int i = 1;
				for (Arc a : t.getPreset()){
					
					if (a instanceof TAPNTransportArc){
						toReturn.addObject(new TAPNTransportArc(oldToNewPlacesMap.get(a.getSource()), transition, oldToNewPlacesMap.get(a.getTarget()), ((TAPNArc)a).getGuard()));
					}else{
						
						toReturn.addObject(new TAPNArc(oldToNewPlacesMap.get(a.getSource()), transition, ((TAPNArc)a).getGuard()));
						toReturn.addObject(new Arc(transition, oldToNewPlacesMap.get(targets.get(0).getTarget())));
						targets.remove(0);
					}
					
					if (i < t.getPreset().size()){
						TAPNPlace implace = new TAPNPlace(t.getName() + "_im" + i, "<=0", 0);
						toReturn.addObject(implace);
						
						toReturn.addObject(new Arc(transition, implace));
						
						transition = new TAPNTransition(t.getName() + "_T"+i);
						toReturn.addObject(transition);
						
						toReturn.addObject(new TAPNArc(implace, transition, ""));
						
					}
					
					i++;
					
				}
				toReturn.addObject(new Arc(transition, lock));
				
				
				//We dont need to do any thing else
				continue;
			}*/
			
			// Counter for new places names 
			int j=0;

			//List of normal arcs in the preset.
			ArrayList<TAPNPlace> holdingplacesNormal = new ArrayList<TAPNPlace>();
			ArrayList<TAPNPlace> holdingplacesTransport = new ArrayList<TAPNPlace>();

			//List of normal arcs in the preset.
			ArrayList<TAPNArc> presetNormalArcs = new ArrayList<TAPNArc>();

			//List of Transarcs in the preset 
			ArrayList<TAPNTransportArc> presetTransportArcs = new ArrayList<TAPNTransportArc>();

			// HashMap that telles where a transportArc is connected to, based on an holdingplace
			HashMap<TAPNPlace, TAPNPlace> connectTo = new HashMap<TAPNPlace, TAPNPlace>();

			//Put stuff in the lists
			for (Arc a : t.getPreset()){

				if (a instanceof TAPNTransportArc){
					presetTransportArcs.add((TAPNTransportArc)a);
				}else{
					presetNormalArcs.add((TAPNArc)a);
				}

			}
			
			//Some styling stuff
			float x, y;
			float transitionSizeX = 80;
			float transitionSizeY = 50;

			x = model.locations.get(t).getX();
			y = model.locations.get(t).getY();
			
			// Move the start x and y cord a little to the right
			// based on the size of the preset
			int sizeOfPreset = t.getPreset().size();
			
			// Ajust the start location
			x = x - (transitionSizeX * (sizeOfPreset/2)); 
			y = y-transitionSizeY;
			
			//List of normal Arcs in the postset
			ArrayList<Arc> postsetNormalArcs = new ArrayList<Arc>();

			for (Arc a:t.getPostset()){
				if (!(a instanceof TAPNTransportArc)){ // If not a transport arc
					postsetNormalArcs.add(a);
				}
			}
			Collections.reverse(postsetNormalArcs); // Revers the list of these places

			//
			// START THE REDUCTION
			//

			//Setup 
			TAPNPlace imPlace = null;
			TAPNTransition lastTransition= null;
			TAPNPlace holdingplace = null;
			
			int i=0;

//			If there are any transfortarcs
			if (presetTransportArcs.size() > 0){

				lastTransition = new TAPNTransition(t.getName() + "_T0");
				toReturn.addTransition(lastTransition);
				//Style the place
				toReturn.locations.put(lastTransition, new Location(x, y));
				
				toReturn.add(new TAPNArc(lock, lastTransition, ""));
				
//				Special attension if the preset only has cone
				if (t.preset.size() == 1){
					
					toReturn.add(new Arc(lastTransition, lock));
					
					toReturn.add(new TAPNTransportArc(oldToNewPlacesMap.get(presetTransportArcs.get(0).getSource()), 
							lastTransition, 
							oldToNewPlacesMap.get(t.getPostset().get(0).getTarget()),
							((TAPNArc)t.getPreset().get(0)).getGuard()
							)
					
					);				
					continue;
				}
				
				
				
				holdingplace = new TAPNPlace(t.getName() +"_hp_0","",0);
				toReturn.addPlace(holdingplace);

//				//Do some styling of the places 
				toReturn.locations.put(holdingplace, new Location(x,y+transitionSizeY));
				x = x + transitionSizeX;

				holdingplacesTransport.add(holdingplace);

				toReturn.add(new TAPNTransportArc(
						oldToNewPlacesMap.get(presetTransportArcs.get(0).getSource()), 
						lastTransition, 
						holdingplace,
						((TAPNArc)presetTransportArcs.get(0)).getGuard()
						));
				
				connectTo.put(holdingplace, oldToNewPlacesMap.get(presetTransportArcs.get(0).getTarget()));

				for (i=1;i<presetTransportArcs.size();i++){
					j++;
					TAPNPlace orgplace = presetTransportArcs.get(i).getSource();

					TAPNTransition newtrans = new TAPNTransition(t.getName() + "_T"+j);
					toReturn.addTransition(newtrans);

					//Special handling if it is the last arc and there are no 
					// arcs in the normal preset
					if (i==presetTransportArcs.size()-1 && postsetNormalArcs.size() == 0){
						holdingplace = oldToNewPlacesMap.get(presetTransportArcs.get(i).getTarget());
					} else {
						holdingplace = new TAPNPlace(t.getName() + "_hp"+j, "",0);
						toReturn.addPlace(holdingplace);
						holdingplacesTransport.add(holdingplace);
					}

					toReturn.add(new TAPNTransportArc(oldToNewPlacesMap.get(orgplace), newtrans, holdingplace, presetTransportArcs.get(i).getGuard()));
					connectTo.put(holdingplace, oldToNewPlacesMap.get(presetTransportArcs.get(i).getTarget()));

					//The im places
					imPlace = new TAPNPlace(t.getName()+"_im"+j,"<=0",0);
					toReturn.addPlace(imPlace);
					toReturn.add(new Arc(lastTransition,imPlace));
					toReturn.add(new TAPNArc(imPlace, newtrans,""));

					lastTransition=newtrans;

					//Do some styling of the places 

					toReturn.locations.put(lastTransition, new Location(x, y));
					toReturn.locations.put(holdingplace, new Location(x, y + transitionSizeY));
					toReturn.locations.put(imPlace, new Location(x - transitionSizeX/2, y));
					
					x = x + transitionSizeX;
					
				}


			} else {
				//No transport arcs setup for normal arcs
				lastTransition = new TAPNTransition(t.getName() + "_T0");
				toReturn.addTransition(lastTransition);
				toReturn.add(new TAPNArc(lock, lastTransition, ""));
				
				//Styling
				toReturn.locations.put(lastTransition, new Location(x, y));
				
				//Special attension if the preset only has cone
				if (t.preset.size() == 1){
					
					toReturn.add(new Arc(lastTransition, lock));
					
					toReturn.add(new TAPNArc(oldToNewPlacesMap.get(presetNormalArcs.get(0).getSource()), lastTransition, presetNormalArcs.get(0).getGuard()));
					toReturn.add(new Arc(lastTransition, oldToNewPlacesMap.get(t.getPostset().get(0).getTarget())));
					continue;
				}
				
				
				
				
				//holdingplace = new TAPNPlace(t.getName() +"_hp_0","",0);
				holdingplace = toReturn.getPlaceByName("P_capacity");
				holdingplacesNormal.add(holdingplace);
				//toReturn.addObject(holdingplace);

//				//Do some styling of the places 
				
				//toReturn.locations.put(holdingplace, new Location(x, y+transitionSizeY));
				x=x+transitionSizeX;

				//holdingplacesNormal.add(holdingplace);
				toReturn.add(new TAPNArc(oldToNewPlacesMap.get(presetNormalArcs.get(0).getSource()), lastTransition, presetNormalArcs.get(0).getGuard()));
				toReturn.add(new Arc(lastTransition, holdingplace));

			}

			// If there are any normal arcs?
			if (presetNormalArcs.size() > 0){

				if (presetTransportArcs.size() == 0){ // Have we no transport arcs we need can skip the first place, as this is done
					i=1;
				}else {
					i=0;
				}
				
				for (; i < presetNormalArcs.size()-1;i++){
					j++;

					TAPNPlace orgplace = oldToNewPlacesMap.get(presetNormalArcs.get(i).getSource());

					TAPNTransition newtrans = new TAPNTransition(t.getName() + "_T"+j);
					toReturn.addTransition(newtrans);


					//holdingplace = new TAPNPlace(t.getName() + "_hp"+j, "",0);
					//toReturn.addObject(holdingplace);
					holdingplace = toReturn.getPlaceByName("P_capacity");
					holdingplacesNormal.add(holdingplace);

					toReturn.add(new TAPNArc(orgplace, newtrans,presetNormalArcs.get(i).getGuard()));
					toReturn.add(new Arc(newtrans, holdingplace));

					//The im places
					imPlace = new TAPNPlace(t.getName()+"_im"+j,"<=0",0);
					toReturn.addPlace(imPlace);
					toReturn.add(new Arc(lastTransition,imPlace));
					toReturn.add(new TAPNArc(imPlace, newtrans, ""));

					lastTransition=newtrans;

//					Do some styling of the places 
					toReturn.locations.put(lastTransition, new Location(x,y));
					//toReturn.locations.put(holdingplace, new Location(x,y+transitionSizeY));
					toReturn.locations.put(imPlace, new Location(x-transitionSizeX/2,y));
					
					x= x+transitionSizeX;
					

				}

				// XXX - hacked 
				//If the loop dit not run once i will be 1, so we need to set i to 2
				/*if (i==1){
					i=2;
				}*/
				TAPNPlace orgplace;
				TAPNTransition newtrans;

//				Reverse list of holdingplaces
				Collections.reverse(holdingplacesNormal);

			/*	if (i > 0){
					--i;
				}*/
				orgplace = oldToNewPlacesMap.get(presetNormalArcs.get(i).getSource());
				j++;
				newtrans = new TAPNTransition(t.getName() + "_T"+j);
				toReturn.addTransition(newtrans);

				holdingplace = oldToNewPlacesMap.get(postsetNormalArcs.get(0).getTarget());

				toReturn.add(new TAPNArc(orgplace, newtrans,presetNormalArcs.get(i).getGuard()));
				toReturn.add(new Arc(newtrans, holdingplace));

				//The im places
				imPlace = new TAPNPlace(t.getName()+"_im"+j,"<=0",0);
				toReturn.addPlace(imPlace);
				toReturn.add(new Arc(lastTransition,imPlace));
				toReturn.add(new TAPNArc(imPlace, newtrans, ""));

				lastTransition=newtrans;

//				Do some styling of the places 
				toReturn.locations.put(lastTransition, new Location(x, y+transitionSizeY));
				toReturn.locations.put(imPlace, new Location(x-transitionSizeX/2,y));

				//Go backwared to each holdingplaces
				for (i=0;i<holdingplacesNormal.size();i++){ // One holding place is skiped as we use and direct connection above
					j++;
					holdingplace = toReturn.getPlaceByName("P_capacity");
					orgplace = oldToNewPlacesMap.get(postsetNormalArcs.get(i+1).getTarget()); // Use 1 one as we have connected place 0 above
					
					newtrans = new TAPNTransition(t.getName() + "_T"+j);
					toReturn.addTransition(newtrans);

					toReturn.add(new TAPNArc(holdingplace, newtrans,""));
					toReturn.add(new Arc(newtrans, orgplace));

					//The im places
					imPlace = new TAPNPlace(t.getName()+"_im"+j,"<=0",0);
					toReturn.addPlace(imPlace);
					toReturn.add(new Arc(lastTransition,imPlace));
					toReturn.add(new TAPNArc(imPlace, newtrans,""));

					lastTransition=newtrans;

					//Do some styling of the places 
					//toReturn.locations.put(lastTransition, toReturn.locations.get(holdingplace).yadd(transitionSizeY));
					//toReturn.locations.put(imPlace, toReturn.locations.get(holdingplace).xyadd(transitionSizeX/2,transitionSizeY));
					
				}
			}

			int sizeOfpresetTransportArcs = presetTransportArcs.size();

			i=0;
			if (sizeOfpresetTransportArcs > 0){

				if (presetNormalArcs.size() == 0){
					sizeOfpresetTransportArcs=sizeOfpresetTransportArcs-1; // We have already handled the last
				}
				
				Collections.reverse(holdingplacesTransport);
				
				for (i=0;i<sizeOfpresetTransportArcs;i++){
					j++;
					

					holdingplace = holdingplacesTransport.get(i);
					TAPNPlace target = connectTo.get(holdingplace);

					TAPNTransition newtrans = new TAPNTransition(t.getName() + "_T"+j);
					toReturn.addTransition(newtrans);

					toReturn.add(new TAPNTransportArc(holdingplace, newtrans,target));

					//The im places
					imPlace = new TAPNPlace(t.getName()+"_im"+j,"<=0",0);
					toReturn.addPlace(imPlace);
					toReturn.add(new Arc(lastTransition,imPlace));
					toReturn.add(new TAPNArc(imPlace, newtrans,""));
					lastTransition=newtrans;

//					Do some styling of the places 
					toReturn.locations.put(lastTransition, toReturn.locations.get(holdingplace).yadd(transitionSizeY));
					toReturn.locations.put(imPlace, toReturn.locations.get(holdingplace).xyadd(transitionSizeX/2, transitionSizeY));

				}

			}
			toReturn.add(new Arc(lastTransition,lock));


		}


		// Move tokesn 
		for (Place p : model.tokens){
			toReturn.tokens.add(oldToNewPlacesMap.get(p));
		}
		
		// Do Grafical stuff
		for (Map.Entry<TAPNPlace, TAPNPlace> entry : oldToNewPlacesMap.entrySet()){
			// XXX - Unsafe cast but ok by assumption
			toReturn.addLocation(entry.getValue(), model.locations.get(entry.getKey()));
		}	

		return toReturn;

	}

}
