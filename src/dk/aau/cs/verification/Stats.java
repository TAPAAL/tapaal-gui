package dk.aau.cs.verification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.aau.cs.model.tapn.LocalTimedMarking;
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
	private ArrayList<Tuple<String, Tuple<BigDecimal, Integer>>> coveredMarking;
	private List<Tuple<String,Integer>> transitionStats;
	
	public Stats(long discovered, long explored, long stored, List<Tuple<String,Integer>> transitionStats, int minExecutionTime, int maxExecutionTime, ArrayList<Tuple<String, Tuple<BigDecimal, Integer>>> coveredMarking)
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
	
	public ArrayList<Tuple<String, Tuple<BigDecimal, Integer>>> getCoveredMarking(){
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
}
