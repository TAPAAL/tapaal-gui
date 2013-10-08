package dk.aau.cs.verification;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.model.tapn.NetworkMarking;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.util.Tuple;

public class Stats {
	private long discovered;
	private long explored;
	private long stored;
	private int minExecutionTime;
	private int maxExecutionTime;
	private NetworkMarking coveredMarking;
	private List<Tuple<String,Integer>> transitionStats;
	
	public Stats(long discovered, long explored, long stored, List<Tuple<String,Integer>> transitionStats, int minExecutionTime, int maxExecutionTime, NetworkMarking coveredMarking)
	{
		this.discovered = discovered;
		this.explored = explored;
		this.stored = stored;	
		this.transitionStats = transitionStats;
		this.minExecutionTime = minExecutionTime;
		this.maxExecutionTime = maxExecutionTime;
		this.coveredMarking = coveredMarking;
	}
	
	public Stats(long discovered, long explored, long stored, List<Tuple<String,Integer>> transitionStats)
	{
		this(discovered, explored, stored, transitionStats, -1, -1, null);
	}
	
	public Stats(long discovered, long explored, long stored)
	{
		this(discovered, explored, stored, new ArrayList<Tuple<String,Integer>>());
	}
	
	public Integer transitionsCount() {
		return transitionStats.size();
	}
	
	public Tuple<String,Integer> getTransitionStats(int index) {
		return transitionStats.get(index);
	}
	
	public long exploredStates() {
		return explored;
	}
	
	public long discoveredStates() {
		return discovered;
	}
	
	public long storedStates() {
		return stored;
	}
	
	public int minimumExecutionTime(){
		return minExecutionTime;
	}
	
	public int maximumExecutionTime(){
		return maxExecutionTime;
	}
	
	public NetworkMarking getCoveredMarking(){
		return coveredMarking;
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Discovered markings: ");
		buffer.append(discovered);
		buffer.append(System.getProperty("line.separator"));
		
		buffer.append("Explored markings: ");
		buffer.append(explored);
		buffer.append(System.getProperty("line.separator"));
		
		buffer.append("Stored markings: ");
		buffer.append(stored);
		return buffer.toString();
	}

	public Stats decomposeCoveredMarking(TimedArcPetriNetNetwork model,
			Tuple<TimedArcPetriNet, NameMapping> transformedModel) {
		if(coveredMarking != null){
			NetworkMarking m = new NetworkMarking();
			for(TimedPlace p : transformedModel.value1().places()){
				List<TimedToken> tokens = coveredMarking.getTokensFor(p);
				if(tokens != null){
					Tuple<String, String> originalName = transformedModel.value2().map(p.name());
					TimedPlace real_p = (originalName.value1() == null || originalName.value1().isEmpty()) ? model.getSharedPlaceByName(originalName.value2()) : model.getTAPNByName(originalName.value1()).getPlaceByName(originalName.value2());
					for(TimedToken t : tokens){
						m.add(new TimedToken(real_p, t.age()));
					}
				}
			}
			coveredMarking = m;
		}
		return this;
	}
}
