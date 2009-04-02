package dk.aau.cs.petrinet;

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
/*  Copyright (c) 2009, Kenneth Yrke Jørgensen <kyrke@cs.aau.dk>, Joakim Byg <jokke@cs.aau.dk>
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    * Neither the name of the TAPAAL nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.   
 */ 
public class TAPNFactory {
/*	
	public TAPNFactory(DataLayer model, int capacity){
		appModel = model;
		aAUPetriNet = new TAPN();
		this.capacity = capacity
	}
*/	
	/*public TAPNFactory(){
		//XXX Does it do stuff?
	}
	
	public PetriNet getTAPN(DataLayer model, int capacity){
		DataLayer appModel = model;
		TAPN aAUPetriNet = new TAPN();
		HashMap<pipe.dataLayer.PlaceTransitionObject, dk.aau.cs.petrinet.PlaceTransitionObject> placeTransitionObjectBookKeeper = new HashMap();

		for ( Place place : appModel.getPlaces() ) {
			TAPNPlace aAUTimedPlace = new TAPNPlace(place.getId(),((TimedPlace)place).getInvariant(), capacity);
			placeTransitionObjectBookKeeper.put(place, aAUTimedPlace);
			aAUPetriNet.addObject(aAUTimedPlace);
		}
		for( Transition transition : appModel.getTransitions() ){
			TAPNTransition aAUTransition = new TAPNTransition();
			placeTransitionObjectBookKeeper.put(transition, aAUTransition);
			aAUPetriNet.addObject(aAUTransition);
		}
		for ( Arc arc : appModel.getArcs() ){
			dk.aau.cs.petrinet.Arc aAUArc = null;
			if (arc instanceof TransportArc){
				if ( ((TransportArc) arc).isInPreSet() ){
					TAPNArc inGoingArc = new TAPNArc(((TimedArc) arc).getGuard());
					dk.aau.cs.petrinet.Arc outGoingArc = new dk.aau.cs.petrinet.Arc( placeTransitionObjectBookKeeper.get(((TransportArc) arc).getBuddy().getSource()), placeTransitionObjectBookKeeper.get(((TransportArc)arc).getBuddy().getTarget()) );
					//aAUArc = new TAPNTransportArc(inGoingArc, (dk.aau.cs.petrinet.Transition)placeTransitionObjectBookKeeper.get(arc.getTarget()) , outGoingArc);
					
					//now we handle where the arcs go
					inGoingArc.setSource( placeTransitionObjectBookKeeper.get( arc.getSource() ) );
					inGoingArc.setTarget( placeTransitionObjectBookKeeper.get( arc.getTarget() ) );
					inGoingArc.getSource().addPostset(inGoingArc);
					inGoingArc.getTarget().addPreset(inGoingArc);
					try {
						aAUPetriNet.addObject(inGoingArc);
					} catch (Exception e) {
						aAUArc.getSource().removePostset(aAUArc);
						aAUArc.getTarget().removePreset(aAUArc);
						
						e.printStackTrace();
					}

					outGoingArc.setSource( placeTransitionObjectBookKeeper.get( arc.getTarget()) );
					outGoingArc.setTarget( placeTransitionObjectBookKeeper.get( ((TransportArc)arc).getBuddy().getTarget()) );
					outGoingArc.getSource().addPostset(outGoingArc);
					outGoingArc.getTarget().addPreset(outGoingArc);
					try {
						aAUPetriNet.addObject(outGoingArc);
					} catch (Exception e) {
						aAUArc.getSource().removePostset(aAUArc);
						aAUArc.getTarget().removePreset(aAUArc);
						
						e.printStackTrace();
					}

					try {
						aAUPetriNet.addObject(aAUArc);
					} catch (Exception e) {
						aAUArc.getSource().removePostset(aAUArc);
						aAUArc.getTarget().removePreset(aAUArc);
						
						e.printStackTrace();
					}
				}
				// else it is already included by its preSet Buddy
				else {
					aAUArc = null;
				}
			}else if (arc instanceof TimedArc){
				aAUArc = new TAPNArc( ((TimedArc) arc).getGuard() );
			}else if (arc instanceof NormalArc){
				aAUArc = new dk.aau.cs.petrinet.Arc();
			}
			if (aAUArc != null){
				//we handle where the arcs go 
				//-except for transport arcs, since they have already been handled
				if ( ! (aAUArc instanceof TAPNTransportArc) ){
			
					aAUArc.setSource( (dk.aau.cs.petrinet.PlaceTransitionObject)placeTransitionObjectBookKeeper.get( arc.getSource() ) );
					aAUArc.setTarget( (dk.aau.cs.petrinet.PlaceTransitionObject)placeTransitionObjectBookKeeper.get( arc.getTarget() ) );
					aAUArc.getSource().addPostset(aAUArc);
					aAUArc.getTarget().addPreset(aAUArc);
					try {
						aAUPetriNet.addObject(aAUArc);
					} catch (Exception e) {
						aAUArc.getSource().removePostset(aAUArc);
						aAUArc.getTarget().removePreset(aAUArc);
						
						e.printStackTrace();
					}
				}
			}
		}
		return aAUPetriNet;
	}*/
}