package dk.aau.cs.petrinet;
/*  Copyright (c) 2009, Kenneth Yrke Jørgensen <kyrke@cs.aau.dk>, Joakim Byg <jokke@cs.aau.dk>
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    * Neither the name of the TAPAAL nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.   
 */ 
import java.util.HashMap;

import pipe.dataLayer.Arc;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.NormalArc;
import pipe.dataLayer.Place;
import pipe.dataLayer.PlaceTransitionObject;
import pipe.dataLayer.TimedArc;
import pipe.dataLayer.TimedPlace;
import pipe.dataLayer.Transition;
import pipe.dataLayer.TransportArc;
import pipe.gui.CreateGui;

public class PipeTapnToAauTapnTransformer {
	private DataLayer appModel;
	private TAPN aAUPetriNet;
	private int capacity;
	private HashMap<pipe.dataLayer.PlaceTransitionObject, dk.aau.cs.petrinet.PlaceTransitionObject> PlaceTransitionObjectBookKeeper = new HashMap();
	
	public PipeTapnToAauTapnTransformer(DataLayer model, int capacity){
		appModel = model;
		aAUPetriNet = new TAPN();
		this.capacity = capacity;
	}
	public TAPN getAAUTAPN() throws Exception{
		for ( Place place : appModel.getPlaces() ) {
			TAPNPlace aAUTimedPlace = new TAPNPlace(place.getName(),((TimedPlace)place).getInvariant(), capacity);
			PlaceTransitionObjectBookKeeper.put(place, aAUTimedPlace);
			aAUPetriNet.addObject(aAUTimedPlace);
			
			aAUPetriNet.addLocation(aAUTimedPlace, place.getX(), place.getY());
			
			for (int i = 0; i < place.getCurrentMarking(); i++){
				aAUPetriNet.tokens.add(aAUTimedPlace);
			}
			
		}
		for( Transition transition : appModel.getTransitions() ){
			TAPNTransition aAUTransition = new TAPNTransition(transition.getName());
			PlaceTransitionObjectBookKeeper.put(transition, aAUTransition);
			aAUPetriNet.addObject(aAUTransition);
			
			aAUPetriNet.addLocation(aAUTransition, transition.getX(), transition.getY());
			
		}
		
		for ( Arc arc : appModel.getArcs() ){
			dk.aau.cs.petrinet.Arc aAUArc = null;
			if (arc instanceof TransportArc){
				TransportArc tmp = (TransportArc)arc;
				TransportArc end=null;
				
				//We only handel arcs gowing from a place
				if (tmp.getSource() instanceof Place) {
					
					for (Arc tmpend : appModel.getArcs()){ // TODO: can we do this better, by not searching all arcs?
						
						if (tmpend instanceof TransportArc){ // must be a transport arc
							
							if (tmpend.getSource() == tmp.getTarget() && tmp.getGroupNr() == ((TransportArc)(tmpend)).getGroupNr()){
								// The arc is connected and is the same
								end=(TransportArc)tmpend;
								break;
							}	
						}
					}
					if (end==null){
						throw new Exception("Inconsistens model, transport arc group not ended");
					} else {
						
						aAUArc = new dk.aau.cs.petrinet.TAPNTransportArc(
								(TAPNPlace)(dk.aau.cs.petrinet.PlaceTransitionObject)PlaceTransitionObjectBookKeeper.get( tmp.getSource() ),
								(TAPNTransition)(dk.aau.cs.petrinet.PlaceTransitionObject)PlaceTransitionObjectBookKeeper.get( end.getSource() ) ,
								(TAPNPlace)(dk.aau.cs.petrinet.PlaceTransitionObject)PlaceTransitionObjectBookKeeper.get( end.getTarget()),
								tmp.getGuard());
						
						
						aAUPetriNet.addObject(aAUArc);
						aAUArc = null;
					}
				}
				
				
				
			}else if (arc instanceof TimedArc){
				aAUArc = new TAPNArc(((TimedArc)arc).getGuard());
			}else if (arc instanceof NormalArc){
				aAUArc = new dk.aau.cs.petrinet.Arc();
			}
			
			if (aAUArc != null){
				aAUArc.setSource( (dk.aau.cs.petrinet.PlaceTransitionObject)PlaceTransitionObjectBookKeeper.get( arc.getSource() ) );
				aAUArc.setTarget( (dk.aau.cs.petrinet.PlaceTransitionObject)PlaceTransitionObjectBookKeeper.get( arc.getTarget() ) );
				aAUPetriNet.addObject(aAUArc);
			}
		}
		return aAUPetriNet;
	}
}