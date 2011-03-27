package dk.aau.cs.translations.tapn;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.aau.cs.TA.*;
import dk.aau.cs.TCTL.visitors.StandardSymmetryTranslationQueryVisitor;
import dk.aau.cs.TCTL.visitors.StandardTranslationQueryVisitor;
import dk.aau.cs.debug.Logger;
import dk.aau.cs.model.tapn.Bound;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimeInvariant;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.TransportArc;
import dk.aau.cs.petrinet.Arc;
import dk.aau.cs.petrinet.Place;
import dk.aau.cs.petrinet.TAPN;
import dk.aau.cs.petrinet.TAPNArc;
import dk.aau.cs.petrinet.TAPNPlace;
import dk.aau.cs.petrinet.TAPNQuery;
import dk.aau.cs.petrinet.TAPNTransition;
import dk.aau.cs.petrinet.TAPNTransportArc;
import dk.aau.cs.petrinet.Transition;
import dk.aau.cs.translations.Degree2Converter;
import dk.aau.cs.translations.Degree2Pairing;
import dk.aau.cs.translations.ModelTranslator;
import dk.aau.cs.translations.NonOptimizingDegree2Converter;
import dk.aau.cs.translations.QueryTranslator;

/*  Copyright (c) 2009, Kenneth Yrke Jørgensen <kyrke@cs.aau.dk>, Joakim Byg <jokke@cs.aau.dk>
 All rights reserved.

 Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the TAPAAL nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.   
 */
public class StandardTranslation implements ModelTranslator<TimedArcPetriNet, NTA>, QueryTranslator<TAPNQuery, UPPAALQuery> {

	protected static final String ID_TYPE = "pid_t";
	protected static final String ID_TYPE_NAME = "pid";
	protected static final String TOKEN_CLOCK_NAME = "x";
	protected static final String PLOCK = "P_lock";
	protected static final String PCAPACITY = "_BOTTOM_";
	protected static final String INITIALIZE_CHANNEL = "c%1$d%2$s";

	protected static final String CONTROL_TEMPLATE_NAME = "Control";
	protected static final String LOCK_TEMPLATE_NAME = "Lock";
	protected static final String TOKEN_TEMPLATE_NAME = "Token";
	
	private int extraTokens;
	private boolean useSymmetry;
	private int numberOfInitChannels;
	
	private Hashtable<String, Location> namesToLocations = new Hashtable<String, Location>();
	

	public StandardTranslation(int extraTokens, boolean useSymmetry) {
		this.extraTokens = extraTokens;
		this.useSymmetry = useSymmetry;
	}
	
	public NTA transformModel(TimedArcPetriNet model) throws Exception {
		clearLocationMappings();
		numberOfInitChannels = 0;
		
		TimedArcPetriNet degree2Model = null;
		try {
			NonOptimizingDegree2Converter converter = new NonOptimizingDegree2Converter();
			degree2Model = converter.transformModel(model);
		} catch (Exception e) {
			return null;
		}
		
		
		NTA nta = new NTA();
		if (useSymmetry || degree2Model.marking().size() + extraTokens == 0) {
			TimedAutomaton ta = createTokenAutomaton(degree2Model);
			createInitializationTransitionsForTokenAutomata(degree2Model, ta);
			ta.setName(TOKEN_TEMPLATE_NAME);
			ta.setInitLocation(getLocationByName(PCAPACITY));
			if (useSymmetry)
				ta.setParameters("const " + ID_TYPE + " " + ID_TYPE_NAME);
			nta.addTimedAutomaton(ta);
		} else {
			int j = 0;
			for(TimedPlace p : degree2Model.places()) {
				for (TimedToken token : degree2Model.marking().getTokensFor(p)) {
					if (!token.place().name().equals(PLOCK)) {
						clearLocationMappings();
						TimedAutomaton ta = createTokenAutomaton(degree2Model);
						ta.setName(TOKEN_TEMPLATE_NAME + j);
						ta.setInitLocation(getLocationByName(token.place().name()));
						nta.addTimedAutomaton(ta);
						j++;
					}
				}
			}
			
			for (int i = 0; i < extraTokens; i++) {
				clearLocationMappings();
				TimedAutomaton tokenTemplate = createTokenAutomaton(degree2Model);
				tokenTemplate.setInitLocation(getLocationByName(PCAPACITY));
				nta.addTimedAutomaton(tokenTemplate);
				tokenTemplate.setName(TOKEN_TEMPLATE_NAME + String.valueOf(degree2Model.marking().size() - 1 + i));
			}
		}
		
		TimedAutomaton lockTA = createTokenAutomaton(degree2Model);
		lockTA.setName(LOCK_TEMPLATE_NAME);
		lockTA.setInitLocation(getLocationByName(PLOCK));
		nta.addTimedAutomaton(lockTA);
		
		if(useSymmetry)
			nta.addTimedAutomaton(createInitializationAutomata(degree2Model));
		
		nta.setSystemDeclarations(createSystemDeclaration(degree2Model.marking().size()));
		nta.setGlobalDeclarations(createGlobalDeclarations(degree2Model));
		
		
		return nta;
		
	}

	private String createSystemDeclaration(int tokensInModel) {
		if (useSymmetry || tokensInModel + extraTokens == 1) {
			return "system " + CONTROL_TEMPLATE_NAME + ", " + LOCK_TEMPLATE_NAME + ", "	+ TOKEN_TEMPLATE_NAME + ";";
		} else {
			StringBuilder builder = new StringBuilder("system ");
			builder.append(LOCK_TEMPLATE_NAME);

			for (int i = 0; i < extraTokens + tokensInModel - 1; i++) {
				builder.append(", ");
				builder.append(TOKEN_TEMPLATE_NAME);
				builder.append(i);
			}
			builder.append(";");

			return builder.toString();
		}
	}
	
	
	private String createGlobalDeclarations(TimedArcPetriNet degree2Model) {
		StringBuilder builder = new StringBuilder();

		if (useSymmetry) {
			builder.append("const int N = ");
			builder.append(degree2Model.marking().size() + extraTokens - 1);
			builder.append(";\n");
			builder.append("typedef ");
			builder.append("scalar[N] ");
			builder.append(ID_TYPE);
			builder.append(";\n");

			for (int i = 0; i < numberOfInitChannels; i++) {
				builder.append("chan ");
				builder.append(String.format(INITIALIZE_CHANNEL, i, ""));
				builder.append(";\n");
			}
		}

		for (TimedTransition t : degree2Model.transitions()) {
			if(t.presetSize() == 0) {
				continue;
			}
			
			builder.append("chan ");
			builder.append(t.name());
			builder.append(";\n");
		}

		return builder.toString();
	}

	

	private TimedAutomaton createInitializationAutomata(TimedArcPetriNet degree2Model) {
		TimedAutomaton control = new TimedAutomaton();
		control.setName(CONTROL_TEMPLATE_NAME);
		Location initial = createInitializationTransitionsForControlAutomaton(degree2Model, control);
		control.setInitLocation(initial);

		return control;
	}

	private Location createInitializationTransitionsForControlAutomaton(TimedArcPetriNet degree2Model, TimedAutomaton control) {
		if (degree2Model.marking().size() == 1){
			Location finish = new Location("finish", "");
			control.addLocation(finish);
			return finish;
		}
		
		Location first = new Location("", "");
		first.setCommitted(true);
		control.addLocation(first);
		Location prev = first;

		for (int i = 0; i < degree2Model.marking().size() - 2; i++) {
			Location l = new Location("", "");
			l.setCommitted(true);
			control.addLocation(l);

			Edge e = new Edge(prev, l, "", String.format(INITIALIZE_CHANNEL, i, "!"),	"");
			control.addTransition(e);
			prev = l;
		}
		
		Location finish = new Location("finish", "");
		control.addLocation(finish);
		Edge e = new Edge(prev, finish, "", String.format(INITIALIZE_CHANNEL, degree2Model.marking().size() - 2, "!"), "");
		control.addTransition(e);
		return first;
	}

	private void createInitializationTransitionsForTokenAutomata(TimedArcPetriNet degree2Model, TimedAutomaton ta) {
		int i = 0;
		for(TimedPlace p : degree2Model.places()) {
			for (TimedToken token : degree2Model.marking().getTokensFor(p)) {
				if (!p.name().equals(PLOCK) && !p.name().equals(PCAPACITY)) {
					Edge e = new Edge(getLocationByName(PCAPACITY), getLocationByName(p.name()), "", String.format(INITIALIZE_CHANNEL, i, "?"), "");
					ta.addTransition(e);
					i++;
					numberOfInitChannels++;
				}
			}
		}
	}

	protected Location getLocationByName(String name) {
		return namesToLocations.get(name);
	}

	protected void addLocationMapping(String name, Location location) {
		namesToLocations.put(name, location);
	}

	protected void clearLocationMappings() {
		namesToLocations.clear();
	}

	
	private TimedAutomaton createTokenAutomaton(TimedArcPetriNet degree2Model) {
		TimedAutomaton tokenTA = new TimedAutomaton();
		createLocationsForTokenAutomata(degree2Model, tokenTA);
		createEdgesForTokenAutomata(degree2Model, tokenTA);
		tokenTA.setDeclarations(createLocalDeclarations());

		return tokenTA;
	}

	private void createLocationsForTokenAutomata(TimedArcPetriNet degree2Net, TimedAutomaton ta) {
		for (TimedPlace place : degree2Net.places()) {
			Location l = new Location(place.name(), convertInvariant(place));
			ta.addLocation(l);
			addLocationMapping(place.name(), l);
		}
	}
	
	private void createEdgesForTokenAutomata(TimedArcPetriNet degree2Model, TimedAutomaton tokenTA) {
		for(TimedTransition transition : degree2Model.transitions()) {
			if(transition.hasInhibitorArcs())
				throw new RuntimeException("Standard translation does not support inhibitor arcs!");
			
			if(transition.presetSize() == 0)
				continue;
			
			Degree2Pairing pairing = new Degree2Pairing(transition);
			
			for(TimedInputArc inputArc : transition.getInputArcs()) {
				String sync = transition.name() + (isPartOfLockTemplate(inputArc.source().name()) ? "!" : "?");
				String guard = convertGuard(inputArc.interval());
				Edge e = new Edge(getLocationByName(inputArc.source().name()), 
						getLocationByName(pairing.getOutputArcFor(inputArc).destination().name()), 
						guard, sync, createResetExpressionForNormalArc());
				
				tokenTA.addTransition(e);
			}
			
			for(TransportArc transArc : transition.getTransportArcsGoingThrough()) {
				String guard = convertGuard(transArc.interval());
				Edge e = new Edge(getLocationByName(transArc.source().name()), 
						getLocationByName(transArc.destination().name()),
						guard, transition.name() + "?", "");
				tokenTA.addTransition(e);
			}
		}
	}
	
	private String convertGuard(TimeInterval interval) {
		if(interval.equals(TimeInterval.ZERO_INF))
			return "";
		
		StringBuilder builder = new StringBuilder();
		builder.append(TOKEN_CLOCK_NAME);
		if(interval.IsLowerBoundNonStrict())
			builder.append(" >= ");
		else
			builder.append(" > ");
		
		builder.append(interval.lowerBound().value());
		
		if(!interval.upperBound().equals(Bound.Infinity)) {
			builder.append(" && ");
			builder.append(TOKEN_CLOCK_NAME);
			
			if(interval.IsUpperBoundNonStrict())
				builder.append(" <= ");
			else
				builder.append(" < ");
			
			builder.append(interval.upperBound().value());
		}
		
		return builder.toString();
	}

	private String createResetExpressionForNormalArc() {
		return String.format("%1s := 0", TOKEN_CLOCK_NAME);
	}
	
	protected String convertInvariant(TimedPlace place) {
		String inv = "";
		TimeInvariant invariant = place.invariant();
		if (!invariant.equals(TimeInvariant.LESS_THAN_INFINITY)) {
			inv = TOKEN_CLOCK_NAME + " " + invariant.toString(false);
		}

		return inv;
	}
	
	protected String createLocalDeclarations() {
		return "clock " + TOKEN_CLOCK_NAME + ";";
	}
	
	private boolean isPartOfLockTemplate(String name) {
		Pattern pattern = Pattern.compile("^(P_(?:[a-zA-Z][a-zA-Z0-9_]*)_(?:(?:[0-9]*_(?:in|out)|check))|P_lock|P_deadlock)$");

		Matcher matcher = pattern.matcher(name);
		return matcher.find();
	}
	
	public UPPAALQuery transformQuery(TAPNQuery query) throws Exception {
		if(useSymmetry) {
			StandardSymmetryTranslationQueryVisitor visitor = new StandardSymmetryTranslationQueryVisitor();
			return  new StandardUPPAALQuery(visitor.getUppaalQueryFor(query));
		} else {
			StandardTranslationQueryVisitor visitor = new StandardTranslationQueryVisitor(query.getTotalTokens());	
			return new StandardUPPAALQuery(visitor.getUppaalQueryFor(query));
		}
	}
}
