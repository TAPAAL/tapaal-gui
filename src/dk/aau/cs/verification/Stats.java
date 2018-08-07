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

    // CTL stats
    private long configurations;
    private long markings;
    private long edges;
    private long processedEdges;
    private long processedNEdges;
    private long exploredConfigurations;

	private int minExecutionTime;
	private int maxExecutionTime;
	private ArrayList<Tuple<String, Tuple<BigDecimal, Integer>>> coveredMarking;
	private List<Tuple<String,Integer>> transitionStats;
        private List<Tuple<String,Integer>> placeBoundStats;
	private ReductionStats reductionStats;
	
	public Stats(long discovered, long explored, long stored, List<Tuple<String,Integer>> transitionStats, List<Tuple<String,Integer>> placeBoundStats, int minExecutionTime, int maxExecutionTime, ArrayList<Tuple<String, Tuple<BigDecimal, Integer>>> coveredMarking)
	{
		this(discovered, explored,stored,transitionStats, placeBoundStats, minExecutionTime, maxExecutionTime,coveredMarking, null);
	}
	
	public Stats(long discovered, long explored, long stored, List<Tuple<String,Integer>> transitionStats, List<Tuple<String,Integer>> placeBoundStats, int minExecutionTime, int maxExecutionTime, ArrayList<Tuple<String, Tuple<BigDecimal, Integer>>> coveredMarking, ReductionStats reductionStats)
	{
		this.discovered = discovered;
		this.explored = explored;
		this.stored = stored;	
		this.transitionStats = transitionStats;
        this.placeBoundStats = placeBoundStats;
		this.minExecutionTime = minExecutionTime;
		this.maxExecutionTime = maxExecutionTime;
		this.coveredMarking = coveredMarking;
		this.reductionStats = reductionStats;
	}
	
	public Stats(long discovered, long explored, long stored, List<Tuple<String,Integer>> transitionStats, List<Tuple<String,Integer>> placeBoundStats)
	{
		this(discovered, explored, stored, transitionStats, placeBoundStats, -1, -1, null);
	}
	
	public Stats(long discovered, long explored, long stored, List<Tuple<String,Integer>> transitionStats, List<Tuple<String,Integer>> placeBoundStats, ReductionStats reductionStats)
	{
		this(discovered, explored, stored, transitionStats, placeBoundStats, -1, -1, null, reductionStats);
	}
	
	public Stats(long discovered, long explored, long stored)
	{
		this(discovered, explored, stored, new ArrayList<Tuple<String,Integer>>(), new ArrayList<Tuple<String,Integer>>());
	}

	// CTL stats
	public Stats(long configurations, long markings, long edges, long processedEdges, long processedNEdges, long exploredConfigurations){
        this.configurations = configurations;
        this.markings = markings;
	this.discovered = markings;
        this.edges = edges;
        this.processedEdges = processedEdges;
        this.processedNEdges = processedNEdges;
        this.exploredConfigurations = exploredConfigurations;
	}
	
	public Integer transitionsCount() {
		return transitionStats.size();
	}
	
        public Integer placeBoundCount() {
		return placeBoundStats.size();
	}
        
	public Tuple<String,Integer> getTransitionStats(int index) {
		return transitionStats.get(index);
	}
	
        public Tuple<String,Integer> getPlaceBoundStats(int index) {
		return placeBoundStats.get(index);
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
	
	public ReductionStats getReductionStats(){
		return reductionStats;
	}

    public long getConfigurations() {
        return configurations;
    }
    public long getMarkings() {
        return markings;
    }
    public long getEdges() {
        return edges;
    }

    public long getProcessedEdges() {
        return processedEdges;
    }
    public long getProcessedNEdges() {
        return processedNEdges;
    }
    public long getExploredConfigurations() {
        return exploredConfigurations;
    }
    
    public void addStats(Stats stats) {
    	explored += stats.exploredStates();
    	discovered += stats.discoveredStates();
    	stored += stats.storedStates();
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
