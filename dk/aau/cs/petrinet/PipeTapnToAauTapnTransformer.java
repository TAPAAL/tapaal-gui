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
import pipe.dataLayer.TimedInputArcComponent;
import pipe.dataLayer.TimedPlaceComponent;
import pipe.dataLayer.Transition;
import pipe.dataLayer.TransportArc;
import dk.aau.cs.translations.ModelTranslator;

public class PipeTapnToAauTapnTransformer implements ModelTranslator<DataLayer, TimedArcPetriNet> {
	protected DataLayer appModel;
	protected TAPN aAUPetriNet;
	protected int capacity;
	protected HashMap<pipe.dataLayer.PlaceTransitionObject, dk.aau.cs.petrinet.PlaceTransitionObject> PlaceTransitionObjectBookKeeper = new HashMap<pipe.dataLayer.PlaceTransitionObject, dk.aau.cs.petrinet.PlaceTransitionObject>();
	
	public PipeTapnToAauTapnTransformer(){
	}

	public TAPN getAAUTAPN(DataLayer model, int capacity) throws Exception{
		reset(model, capacity);
		
		for ( Place place : appModel.getPlaces() ) {
			transformPlace(place);
		}
		
		for( Transition transition : appModel.getTransitions() ){
			transformTransition(transition);
		}
		
		for ( Arc arc : appModel.getArcs() ){
			if (arc instanceof TransportArc){
				transformTransportArc((TransportArc)arc);
			}else if(arc instanceof pipe.dataLayer.TAPNInhibitorArc){
					transformInhibitorArc((pipe.dataLayer.TAPNInhibitorArc)arc);
			}else if (arc instanceof TimedInputArcComponent){
				transformTimedArc((TimedInputArcComponent)arc);
			}else if (arc instanceof NormalArc){
				transformNormalArc((NormalArc)arc);
			}
		}
		
		return aAUPetriNet;
	}
	
	protected void reset(DataLayer model, int capacity) {
		appModel = model;
		this.capacity = capacity;
		aAUPetriNet = new TAPN();
	}

	protected void transformNormalArc(NormalArc arc) throws Exception {
		dk.aau.cs.petrinet.Arc aAUArc;
		aAUArc = new dk.aau.cs.petrinet.Arc();
		if (aAUArc != null){
			aAUArc.setSource( PlaceTransitionObjectBookKeeper.get( arc.getSource() ) );
			aAUArc.setTarget( PlaceTransitionObjectBookKeeper.get( arc.getTarget() ) );
			aAUPetriNet.add(aAUArc);
		}
	}
	
	protected void transformTimedArc(TimedInputArcComponent arc) throws Exception {
		dk.aau.cs.petrinet.Arc aAUArc;
		aAUArc = new TAPNArc(getGuard(arc));
		if (aAUArc != null){
			aAUArc.setSource( PlaceTransitionObjectBookKeeper.get( arc.getSource() ) );
			aAUArc.setTarget( PlaceTransitionObjectBookKeeper.get( arc.getTarget() ) );
			aAUPetriNet.add(aAUArc);
		}
	}
	
	protected void transformInhibitorArc(pipe.dataLayer.TAPNInhibitorArc arc) throws Exception {
		dk.aau.cs.petrinet.Arc aAUArc;
		aAUArc = new dk.aau.cs.petrinet.TAPNInhibitorArc(getGuard(arc));	
		if (aAUArc != null){
			aAUArc.setSource( PlaceTransitionObjectBookKeeper.get( arc.getSource() ) );
			aAUArc.setTarget( PlaceTransitionObjectBookKeeper.get( arc.getTarget() ) );
			aAUPetriNet.add(aAUArc);
		}
	}
	
	protected void transformTransportArc(TransportArc arc) throws Exception {
		TransportArc end=null;
		
		//We only handel arcs gowing from a place
		if (arc.getSource() instanceof Place) {			
			for (Arc tmpend : appModel.getArcs()){ // TODO: can we do this better, by not searching all arcs?
				if (tmpend instanceof TransportArc){ // must be a transport arc
					if (tmpend.getSource() == arc.getTarget() && arc.getGroupNr() == ((TransportArc)(tmpend)).getGroupNr()){
						// The arc is connected and is the same
						end=(TransportArc)tmpend;
						break;
					}	
				}
			}
			
			if (end==null){
				throw new Exception("Inconsistens model, transport arc group not ended");
			} else {
				TAPNTransportArc aAUArc = new dk.aau.cs.petrinet.TAPNTransportArc(
						(TAPNPlace)PlaceTransitionObjectBookKeeper.get( arc.getSource() ),
						(TAPNTransition)PlaceTransitionObjectBookKeeper.get( end.getSource() ) ,
						(TAPNPlace)PlaceTransitionObjectBookKeeper.get( end.getTarget()),
						getGuard(arc));
				
				aAUPetriNet.add(aAUArc);
			}
		}
	}
	
	protected void transformTransition(Transition transition) {
		TAPNTransition aAUTransition = new TAPNTransition(transition.getName());
		PlaceTransitionObjectBookKeeper.put(transition, aAUTransition);
		aAUPetriNet.addTransition(aAUTransition);
		
		aAUPetriNet.addLocation(aAUTransition, transition.getX(), transition.getY());
	}
	
	protected void transformPlace(Place place) {
		TAPNPlace aAUTimedPlace = new TAPNPlace(place.getName(),getInvariant(place), capacity);
		PlaceTransitionObjectBookKeeper.put(place, aAUTimedPlace);
		aAUPetriNet.addPlace(aAUTimedPlace);
		
		aAUPetriNet.addLocation(aAUTimedPlace, place.getX(), place.getY());
		
		for (int i = 0; i < place.getCurrentMarking(); i++){
			aAUPetriNet.tokens.add(aAUTimedPlace);
		}
	}
	
	private String getGuard(TimedInputArcComponent arc) {
		String guard = arc.getGuardAsString();
		String leftDelim = guard.substring(0,1);
		String rightDelim = guard.substring(guard.length()-1, guard.length());
		String first = guard.substring(1, guard.indexOf(","));
		String second = guard.substring(guard.indexOf(",")+1, guard.length()-1);
		
		boolean isFirstConstant = false;
		boolean isSecondConstant = false;
		
		try{
			Integer.parseInt(first);
		}catch(NumberFormatException e){
			isFirstConstant = true;
		}
		
		try{
			Integer.parseInt(second);
		}catch(NumberFormatException e){
			if(!second.equals("inf")) isSecondConstant = true;
		}
		
		if(isFirstConstant){
			first = String.valueOf(appModel.getConstantValue(first));
		}
		
		if(isSecondConstant){
			second = String.valueOf(appModel.getConstantValue(second));
		}
		
		return leftDelim + first + "," + second + rightDelim;
	}
	private String getInvariant(Place place) {
		String inv = ((TimedPlaceComponent)place).getInvariantAsString();
		String operator = inv.contains("<=") ? "<=" : "<";
		
		String bound = inv.substring(operator.length());
		
		boolean isConstant = false;
		try{
			Integer.parseInt(bound);
		}catch(NumberFormatException e){
			if(!bound.equals("inf")) isConstant = true;
		}
		
		if(isConstant)
			bound = String.valueOf(appModel.getConstantValue(bound));
		
		return operator + bound;
	}
	
	public TimedArcPetriNet transformModel(DataLayer model) throws Exception {
		return getAAUTAPN(model,0);
	}
	
	
}