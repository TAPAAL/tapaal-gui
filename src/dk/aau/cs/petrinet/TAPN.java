package dk.aau.cs.petrinet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/*  Copyright (c) 2009, Kenneth Yrke Jørgensen <kyrke@cs.aau.dk>, Joakim Byg <jokke@cs.aau.dk>
 All rights reserved.

 Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the TAPAAL nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.   
 */

public class TAPN extends PetriNet implements TimedArcPetriNet {

	List<TAPNPlace> places = new ArrayList<TAPNPlace>();
	List<TAPNTransition> transitions = new ArrayList<TAPNTransition>();

	List<TAPNTransportArc> transportArcs = new ArrayList<TAPNTransportArc>();
	List<TAPNInhibitorArc> inhibitorArcs = new ArrayList<TAPNInhibitorArc>();
	List<TAPNArc> tapnArcs = new ArrayList<TAPNArc>();
	List<Arc> normalArcs = new ArrayList<Arc>();

	// TODO: Fix to use token class and non-public access
	public LinkedList<Place> tokens = new LinkedList<Place>();// Add each place
																// for each
																// token it has.

	public HashMap<PlaceTransitionObject, Location> locations = new HashMap<PlaceTransitionObject, Location>();

	public boolean isDegree2() {
		boolean isDegree2 = true;

		for (Transition t : getTransitions()) {

			if (t.getPostset().size() > 2 || t.getPreset().size() > 2) {
				isDegree2 = false;
				break;
			}

		}

		return isDegree2;

	}

	public void addPlace(TAPNPlace p) {
		places.add(p);
	}

	public void addTransition(TAPNTransition t) {
		transitions.add(t);
	}

	public void addLocation(PlaceTransitionObject pto, float x, float y) {
		locations.put(pto, new Location(x, y));
	}

	public void addLocation(PlaceTransitionObject pto, Location l) {
		locations.put(pto, l);

	}

	private void updatePreAndPostSets(Arc a, PlaceTransitionObject source,
			PlaceTransitionObject target) {
		// Update model (places and transitions)
		source.addPostset(a);
		target.addPreset(a);
	}

	private void checkArc(Arc a) {
		if (!isArcValid(a)) {
			throw new IllegalArgumentException(
					"The specified arc is not valid. Either it is missing a source or target, or the source or target could not be found.");
		}
	}

	private boolean isArcValid(Arc arc) {
		PlaceTransitionObject source = arc.getSource();
		PlaceTransitionObject target = arc.getTarget();

		if (source == null || target == null) {
			return false;
		}

		// Check that source and target exist
		if (!(places.contains(source) || transitions.contains(source))) {
			return false;
		}

		if (!(places.contains(target) || transitions.contains(target))) {
			return false;
		}

		return true;
	}

	// For backward compatibility with rest of code
	public void add(Arc a) throws Exception {
		if (a instanceof TAPNTransportArc) {
			addArc((TAPNTransportArc) a);
		} else if (a instanceof TAPNInhibitorArc) {
			addArc((TAPNInhibitorArc) a);
		} else if (a instanceof TAPNArc) {
			addArc((TAPNArc) a);
		} else {
			addArc(a);
		}
	}

	public void addArc(Arc a) throws Exception {
		checkArc(a);

		PlaceTransitionObject source = a.getSource();
		PlaceTransitionObject target = a.getTarget();
		if (!(source instanceof Transition && target instanceof Place)) {
			throw new Exception("Invalid source and target for this arc type");
		}

		// Arc temp = new Arc(a); // Morten: Not sure why they copy it..
		// Shouldn't be necessary

		normalArcs.add(a);

		updatePreAndPostSets(a, source, target);
	}

	public void addArc(TAPNTransportArc a) throws Exception {
		checkArc(a);

		// TODO - check intermediate
		TAPNTransition intermediate = a.getIntermediate();
		intermediate.addPreset(a);
		intermediate.addPostset(a);

		transportArcs.add(a);

		updatePreAndPostSets(a, a.getSource(), a.getTarget());
	}

	public void addArc(TAPNArc a) throws Exception {
		// Is arc consistent and valid?
		PlaceTransitionObject source = a.getSource();
		PlaceTransitionObject target = a.getTarget();
		if (!(source instanceof Place && target instanceof Transition)) {
			throw new IllegalArgumentException(
					"Invalid source and target for this arc type");
		}

		// TAPNArc temp = new TAPNArc((TAPNArc)a); // Morten: Not sure why they
		// copy it? Shouldn't be necessary

		tapnArcs.add(a);

		updatePreAndPostSets(a, source, target);
	}

	public void addArc(TAPNInhibitorArc a) throws Exception {
		checkArc(a);

		PlaceTransitionObject source = a.getSource();
		PlaceTransitionObject target = a.getTarget();
		if (!(source instanceof Place && target instanceof Transition)) {
			throw new IllegalArgumentException(
					"Invalid source and target for this arc type");
		}

		inhibitorArcs.add(a);

		((TAPNTransition) target).addToInhibitorSet(a);
	}

	public void removeObject(PlaceTransitionObject pt) throws Exception {
		// Check that pt is in model, else cast an exception

		if (!(places.contains(pt) || transitions.contains(pt))) {
			throw new Exception("PlaceTransition is not in model");
		}

		// Remove the arcs gowing to and from this place
		// and remove the arc from the place/transition from the other end
		LinkedList<Arc> tmpPreset = (LinkedList<Arc>) pt.getPreset();
		LinkedList<Arc> tmpPostset = (LinkedList<Arc>) pt.getPostset();

		// Remove all arcs pointing to this place/tranistion object
		if (pt instanceof TAPNTransition) {
			transitions.remove(pt);
		} else if (pt instanceof Place) {
			places.remove(pt);
		}

		// Remove arcs from preset/postset of place transition objects
		for (Arc a : tmpPreset) {
			a.getSource().removeArc(a);
			this.removeObject(a);
		}
		for (Arc a : tmpPostset) {
			a.getTarget().removeArc(a);
			this.removeObject(a);
		}

	}

	public boolean removeObject(Arc a) {

		PlaceTransitionObject source = a.getSource();
		PlaceTransitionObject target = a.getTarget();

		source.removeArc(a);
		target.removeArc(a);

		if (a instanceof TAPNTransportArc) {
			TAPNTransportArc tmp = (TAPNTransportArc) a;
			TAPNTransition tmp2 = tmp.getIntermediate();
			tmp2.removeArc(a);
			tmp2.removeArc(a);
		}

		if (a instanceof TAPNTransportArc) {
			return transportArcs.remove(a);
		} else if (a instanceof TAPNInhibitorArc) {
			return inhibitorArcs.remove(a);
		} else if (a instanceof TAPNArc) {
			return tapnArcs.remove(a);
		} else
			return normalArcs.remove(a);
	}

	public int getNumberOfTokens() {
		return tokens.size();
	}

	public List<Token> getTokens() {
		ArrayList<Token> toReturn = new ArrayList<Token>();

		for (Place p : tokens) {
			toReturn.add(new Token((TAPNPlace) p));
		}

		return toReturn;
	}

	public List<TAPNPlace> getPlaces() {
		return new ArrayList<TAPNPlace>(places);
	}

	public List<TAPNTransition> getTransitions() {
		return new ArrayList<TAPNTransition>(transitions);
	}

	public List<Arc> getArcs() {
		ArrayList<Arc> arcs = new ArrayList<Arc>(normalArcs);

		arcs.addAll(tapnArcs);
		arcs.addAll(transportArcs);
		arcs.addAll(inhibitorArcs);

		return arcs;
	}

	public void convertToConservative() throws Exception {

		// Add capacity place

		TAPNPlace capacity = new TAPNPlace("P_capacity", "", 0);
		addPlace(capacity);

		// Some styling
		locations.put(capacity, new Location(100, 400));

		// For all trantions add arcs to capacity

		List<TAPNTransition> transitions = getTransitions();

		for (Transition t : transitions) {

			int difference = t.getPostset().size() - t.getPreset().size();

			if (difference < 0) {
				// Add outgowing arcs from trantions to capacity
				for (int i = 0; i > difference; i--) {
					// Add transition
					Arc tmp = new Arc();
					tmp.setSource(t);
					tmp.setTarget(capacity);

					add(tmp);

				}

			} else if (difference > 0) {
				// Add ingowing arcs from trantions to cacacity

				for (int i = 0; i < difference; i++) {
					// Add transition

					TAPNArc tmp = new TAPNArc("");
					tmp.setSource(capacity);
					tmp.setTarget(t);

					add(tmp);

				}

			}

		}

	}

	public TAPNPlace getPlaceByName(String string) {
		for (TAPNPlace p : places) {
			if (p.name.equals(string)) {
				return p;
			}
		}
		return null;
	}

	public void orderPresetRescrition() {

		for (Transition t : this.getTransitions()) {

			// Order the preset of the transition

			List<Arc> preset = t.getPreset();

			Collections.sort(preset, new Comparator<Arc>() {

				public int compare(Arc arg0, Arc arg1) {

					if (arg0 instanceof TAPNArc && arg1 instanceof TAPNArc) {

						int scorea = 0;
						int scoreb = 0;

						TAPNArc a = (TAPNArc) (arg0);
						TAPNArc b = (TAPNArc) (arg1);

						// Count the number of resitionsiton

						String[] tmp = a.getGuard().split(",");

						if (!tmp[0].contains("[0")) {
							scorea = scorea + 2;
						}
						if (!tmp[1].contains("inf)")) {
							scorea++;
						}

						tmp = b.getGuard().split(",");

						if (!tmp[0].contains("[0")) {
							scoreb = scoreb + 2;
						}
						if (!tmp[1].contains("inf)")) {
							scoreb++;
						}

						return scoreb - scorea;
					}

					return 0;
				}
			});

		}

	}

	public List<TAPNInhibitorArc> getInhibitorArcs() {
		return inhibitorArcs;
	}

	public List<TAPNArc> getTAPNArcs() {
		return tapnArcs;
	}

	public List<TAPNTransportArc> getTransportArcs() {
		return transportArcs;
	}

	public List<Arc> getNormalArcs() {
		return normalArcs;
	}

	public void addToken(Token token) {
		tokens.add(token.place());
	}

	public void removePlaceByName(String place) {
		Place toRemove = null;
		for (Place p : places) {
			if (p.getName().equals(place)) {
				toRemove = p;
				break;
			}
		}

		if (toRemove != null) {
			places.remove(toRemove);
		}
	}

	public TAPNTransition getTransitionsByName(String transitionName) {
		for (TAPNTransition trans : transitions) {
			if (trans.getName().equals(transitionName)) {
				return trans;
			}
		}
		return null;
	}
}
