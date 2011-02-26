package dk.aau.cs.model.tapn;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import dk.aau.cs.model.tapn.event.TimedTransitionEvent;
import dk.aau.cs.model.tapn.event.TimedTransitionListener;
import dk.aau.cs.model.tapn.simulation.FiringMode;
import dk.aau.cs.util.Require;

public class TimedTransition extends TAPNElement {
	private static final Pattern namePattern = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");
	
	private String name;
	private List<TimedOutputArc> postset = new ArrayList<TimedOutputArc>();
	private List<TimedInputArc> preset = new ArrayList<TimedInputArc>();
	private List<TransportArc> transportArcsGoingThrough = new ArrayList<TransportArc>();
	private List<TimedInhibitorArc> inhibitorArcs = new ArrayList<TimedInhibitorArc>();

	private SharedTransition sharedTransition;

	private List<TimedTransitionListener> listeners = new ArrayList<TimedTransitionListener>();

	public TimedTransition(String name) {
		setName(name);
	}

	public void addTimedTransitionListener(TimedTransitionListener listener){
		Require.that(listener != null, "listener cannot be null");
		listeners.add(listener);
	}

	public void removeListener(TimedTransitionListener listener){
		Require.that(listener != null, "listener cannot be null");
		listeners.remove(listener);
	}

	public boolean isShared(){
		return sharedTransition != null;
	}

	public SharedTransition sharedTransition(){
		return sharedTransition;
	}

	public void makeShared(SharedTransition sharedTransition){
		Require.that(sharedTransition != null, "sharedTransition cannot be null");

		if(this.sharedTransition != sharedTransition){
			unshare();
			this.sharedTransition = sharedTransition;
			setName(sharedTransition.name());
			fireSharedStateChanged();
		}
	}

	public void unshare() {
		if(isShared()){
			sharedTransition.unshare(this);
			sharedTransition = null;
			fireSharedStateChanged();
		}
	}

	public String name() {
		return name;
	}

	public void setName(String newName) {
		Require.that(newName != null && !newName.isEmpty(), "A timed transition must have a name");
		Require.that(isValid(newName), "The specified name must conform to the pattern [a-zA-Z_][a-zA-Z0-9_]*");
		this.name = newName;
		fireNameChanged();
	}

	private void fireNameChanged(){
		for(TimedTransitionListener listener : listeners){
			listener.nameChanged(new TimedTransitionEvent(this));
		}
	}
	
	private void fireSharedStateChanged() {
		for(TimedTransitionListener listener : listeners){
			listener.sharedStateChanged(new TimedTransitionEvent(this));
		}
	}

	private boolean isValid(String newName) {
		return namePattern.matcher(newName).matches();
	}

	public void addToPreset(TimedInputArc arc) {
		Require.that(arc != null, "Cannot add null to preset");
		preset.add(arc);
	}

	public void addToPostset(TimedOutputArc arc) {
		Require.that(arc != null, "Cannot add null to postset");
		postset.add(arc);
	}


	public void removeFromPreset(TimedInputArc arc) {
		preset.remove(arc);
	}

	public void removeFromPostset(TimedOutputArc arc) {
		postset.remove(arc);
	}

	public void addTransportArcGoingThrough(TransportArc arc) {
		Require.that(arc != null, "Cannot add null to preset");
		transportArcsGoingThrough.add(arc);
	}

	public void removeTransportArcGoingThrough(TransportArc arc) {
		transportArcsGoingThrough.remove(arc);
	}

	public void addInhibitorArc(TimedInhibitorArc arc) {
		inhibitorArcs.add(arc);
	}

	public void removeInhibitorArc(TimedInhibitorArc arc) {
		inhibitorArcs.remove(arc);
	}

	@Override
	public void delete() {
		model().remove(this);
	}

	public int presetSize() {
		return preset.size() + transportArcsGoingThrough.size();
	}

	public boolean isEnabled() {
		if(isShared()){
			return sharedTransition.isEnabled();
		}else{
			return isEnabledAlone();
		}
	}

	public boolean isEnabledAlone() {
		for (TimedInputArc arc : preset) {
			if (!arc.isEnabled())
				return false;
		}
		for (TransportArc arc : transportArcsGoingThrough) {
			if (!arc.isEnabled())
				return false;
		}
		for (TimedInhibitorArc arc : inhibitorArcs) {
			if (!arc.isEnabled())
				return false;
		}
		return true;
	}

	public boolean isEnabledBy(List<TimedToken> tokens) {
		if (presetSize() != tokens.size()) return false;

		boolean validToken = false;
		for (TimedToken token : tokens) {
			for (TimedInputArc inputArc : preset) {
				if (inputArc.source().equals(token.place()) && inputArc.isEnabledBy(token)) {
					validToken = true;
					break;
				}
			}

			for (TransportArc transportArc : transportArcsGoingThrough) {
				if (transportArc.source().equals(token.place()) && transportArc.isEnabledBy(token)) {
					validToken = true;
					break;
				}
			}

			if (!validToken)
				return false;
		}

		return true;
	}

	public List<TimedToken> calculateProducedTokensFrom(List<TimedToken> consumedTokens) {
		// Assume that tokens enables transition

		ArrayList<TimedToken> producedTokens = new ArrayList<TimedToken>();
		for (TimedOutputArc arc : postset) {
			producedTokens.add(new TimedToken(arc.destination()));
		}

		for (TransportArc transportArc : transportArcsGoingThrough) {
			for (TimedToken token : consumedTokens) {
				if (token.place().equals(transportArc.source())) {
					producedTokens.add(new TimedToken(transportArc.destination(), token.age()));
				}
			}
		}

		return producedTokens;
	}

	public List<TimedToken> calculateConsumedTokens(TimedMarking timedMarking, FiringMode firingMode) {
		List<TimedToken> tokensToConsume = new ArrayList<TimedToken>();

		for (TimedInputArc arc : preset) {
			TimedToken token = firingMode.pickTokenFrom(arc.getElligibleTokens());
			tokensToConsume.add(token);
		}

		for (TransportArc arc : transportArcsGoingThrough) {
			TimedToken token = firingMode.pickTokenFrom(arc.getElligibleTokens());
			tokensToConsume.add(token);
		}

		return tokensToConsume;
	}

	public Iterable<TimedInputArc> getInputArcs(){
		return preset;
	}

	public Iterable<TransportArc> getTransportArcsGoingThrough(){
		return transportArcsGoingThrough;
	}

	public TimedTransition copy() {
		return new TimedTransition(this.name);
	}

	@Override
	public String toString() {
		if (model() != null)
			return model().getName() + "." + name;
		else
			return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof TimedTransition))
			return false;
		TimedTransition other = (TimedTransition) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
