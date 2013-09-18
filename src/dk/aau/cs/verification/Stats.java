package dk.aau.cs.verification;

import java.util.ArrayList;
import java.util.List;


import dk.aau.cs.util.Tuple;

public class Stats {
	private long discovered;
	private long explored;
	private long stored;
	private int minExecutionTime;
	private List<Tuple<String,Integer>> transitionStats;
	
	public Stats(long discovered, long explored, long stored, List<Tuple<String,Integer>> transitionStats, int minExecutionTime)
	{
		this.discovered = discovered;
		this.explored = explored;
		this.stored = stored;	
		this.transitionStats = transitionStats;
		this.minExecutionTime = minExecutionTime;
	}
	
	public Stats(long discovered, long explored, long stored)
	{
		this.discovered = discovered;
		this.explored = explored;
		this.stored = stored;	
		this.transitionStats = new ArrayList<Tuple<String,Integer>>(); 	
		this.minExecutionTime = -1;
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
	
	public long minimumExecutionTime(){
		return minExecutionTime;
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
