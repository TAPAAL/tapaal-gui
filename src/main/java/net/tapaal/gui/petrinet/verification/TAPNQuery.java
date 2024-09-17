package net.tapaal.gui.petrinet.verification;

import dk.aau.cs.TCTL.*;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.verification.QueryType;
import dk.aau.cs.verification.SMCSettings;
import dk.aau.cs.verification.SMCTraceType;

import java.util.ArrayList;

public class TAPNQuery {
    
	public enum TraceOption {
		FASTEST, SOME, NONE
	}

	public enum SearchOption {
		BFS, DFS, RANDOM, BatchProcessingKeepQueryOption, HEURISTIC, OVERAPPROXIMATE, DEFAULT, RANDOMHEURISTIC
	}

	public enum QueryReductionTime {
	    NoTime, ShortestTime, UnlimitedTime
    }

	public enum HashTableSize {
		MB_4, MB_16, MB_64, MB_256, MB_512
	}

	public enum ExtrapolationOption {
		AUTOMATIC, NONE, DIFF, LOCAL, LOW_UP
	}
	
	public enum WorkflowMode{
		NOT_WORKFLOW, WORKFLOW_SOUNDNESS, WORKFLOW_STRONG_SOUNDNESS
	}
	
	public enum QueryCategory{
		Default, CTL, LTL, HyperLTL, SMC
	}
	
	public enum AlgorithmOption{
		CERTAIN_ZERO, LOCAL
	}

	private String name;
	private int capacity;
	private Integer oldCapacity = null;
	private TraceOption traceOption;
	private SearchOption searchOption;
	private ReductionOption reductionOption;
	private boolean symmetry;
	private boolean gcd;
	private boolean pTrie;
	private boolean timeDart;
	private boolean overApproximation;
	private final boolean isColored;
	private HashTableSize hashTableSize;
	private ExtrapolationOption extrapolationOption;
	private InclusionPlaces inclusionPlaces;
	private WorkflowMode workflow;
	private long strongSoundnessBound;
    private boolean useReduction;
    private boolean useColoredReduction;
	private QueryCategory queryCategory = QueryCategory.Default;             // Used by the CTL engine
	private AlgorithmOption algorithmOption = AlgorithmOption.CERTAIN_ZERO;  // Used by the CTL engine

    private ArrayList<String> traceList;

    //Used for unfolding
    private boolean partitioning;
    private boolean colorFixpoint;
    private boolean symmetricVars;
	
	private boolean enableOverApproximation = false;
	private boolean enableUnderApproximation = false;
	private int denominator = 2;
	
	private boolean discreteInclusion = false; // Only for VerifyTAPN

	private TCTLAbstractProperty property = null;
	private boolean isActive = true;
	
	private boolean useSiphontrap = false; 
	private boolean useQueryReduction = true; 
	private boolean useStubbornReduction = true;
    private boolean useTarOption = false;
    private boolean useTarjan = false;
	private boolean useRawVerification = false;
	private String rawVerificationPrompt;

    private SMCSettings smcSettings;
    private boolean benchmark = false;
    private int benchmarkRuns = 100;
    private boolean parallel = true;
    
    public enum VerificationType {
        QUANTITATIVE, QUALITATIVE, SIMULATE;

        public static VerificationType fromOrdinal(int ordinal) {
            switch (ordinal) {
                case 1:
                    return QUALITATIVE;
                case 2:
                    return SIMULATE;
                default:
                    return QUANTITATIVE;
            }
        }

        public static VerificationType fromString(String type) {
            switch (type) {
                case "Qualitative":
                    return QUALITATIVE;
                case "Simulate":
                    return SIMULATE;
                default:
                    return QUANTITATIVE;
            }
        }

        @Override
        public String toString() {
            switch (this) {
                case QUALITATIVE:
                    return "Qualitative";
                case SIMULATE:
                    return "Simulate";
                default:
                    return "Quantitative";
            }
        }
    }

    private VerificationType verificationType = VerificationType.QUANTITATIVE;
    private int numberOfTraces = 1;
    private SMCTraceType smcTraceType = new SMCTraceType();

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the name
	 */

	public void setTraceList(ArrayList<String> traces) {
        this.traceList = traces;
    }
    public ArrayList<String> getTraceList() {
        return this.traceList;
    }

	public String getName() {
		return name;
	}
	
	public boolean isOverApproximationEnabled() {
		return this.enableOverApproximation;
	}
	
	public boolean isUnderApproximationEnabled() {
		return this.enableUnderApproximation;
	}
	
	public boolean isSiphontrapEnabled() {
		return this.useSiphontrap;
	}
	
	public boolean isQueryReductionEnabled() {
		return this.useQueryReduction;
	}
	
	public boolean isStubbornReductionEnabled() {
		return this.useStubbornReduction;
	}

	public boolean isTarOptionEnabled() {
	    return this.useTarOption;
    }

    public void setUseTarOption(boolean useTarOption) {
	    this.useTarOption = useTarOption;
    }

    public boolean isTarjan() {
        return this.useTarjan;
    }

    public void setUseTarjan(boolean useTarjan) {
        this.useTarjan = useTarjan;
    }

	public int approximationDenominator() {
		return this.denominator;
	}

    public void setApproximationDenominator(int denominator) {
        this.denominator = denominator;
    }

	public void setDiscreteInclusion(boolean value){
		discreteInclusion = value;
	}
	
	public boolean discreteInclusion(){
		return discreteInclusion;
	}

	public boolean isColored() {return isColored;}
	
	/**
	 * @param capacity
	 *            the capacity to set
	 */
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public void setOldCapacity(int capacity) {
		this.oldCapacity = capacity;
	}

	/**
	 * @return the capacity
	 */
	public int getCapacity() {
		return capacity;
	}

	public Integer getOldCapacity() {
		return oldCapacity;
	}

	public String getQuery() {
		return property.toString();
	}

	/**
	 * @return the query
	 */
	public TCTLAbstractProperty getProperty() {
		return property;
	}

	/**
	 * @param traceOption
	 *            the traceOption to set
	 */
	public void setTraceOption(TraceOption traceOption) {
		this.traceOption = traceOption;
	}

	/**
	 * @return the traceOption
	 */
	public TraceOption getTraceOption() {
		return traceOption;
	}

	/**
	 * @param searchOption
	 *            the searchOption to set
	 */
	public void setSearchOption(SearchOption searchOption) {
		this.searchOption = searchOption;
	}

	/**
	 * @return the searchOption
	 */
	public SearchOption getSearchOption() {
		return searchOption;
	}

	/**
	 * @param reductionOption
	 *            the reductionOption to set
	 */
	public void setReductionOption(ReductionOption reductionOption) {
		this.reductionOption = reductionOption;
	}

	/**
	 * @return the reductionOption
	 */
	public ReductionOption getReductionOption() {
		return reductionOption;
	}
	
	public boolean useSymmetry() {
		return symmetry;
	}
	
	public boolean useGCD(){
		return gcd;
	}
	
	public void setUseGCD(boolean useGCD){
		gcd = useGCD;
	}
	
	public boolean useTimeDarts(){
		return timeDart;
	}
	
	public void setUseTimeDarts(boolean useTimeDarts){
		timeDart = useTimeDarts;
	}
	
	public boolean usePTrie(){
		return pTrie;
	}
	
	public void setUsePTrie(boolean usePTrie){
		pTrie = usePTrie;
	}

    public boolean usePartitioning(){
        return partitioning;
    }

    public void setUsePartitioning(boolean usePartitioning){
        partitioning = usePartitioning;
    }

    public boolean useSymmetricVars(){
        return symmetricVars;
    }

    public void setUseSymmetricVars(boolean useSymmetricVars){
        symmetricVars = useSymmetricVars;
    }

    public boolean useColorFixpoint(){
        return colorFixpoint;
    }

    public void setUseColorFixpoint(boolean useColorFixpoint){
        colorFixpoint = useColorFixpoint;
    }
	
	public void setUseOverApproximation(boolean useOverApproximation){
		overApproximation = useOverApproximation;
	}

    public void setUseOverApproximationEnabled(boolean useOverApproximationEnabled){
        this.enableOverApproximation = useOverApproximationEnabled;
    }

    public void setUseUnderApproximationEnabled(boolean useUnderApproximationEnabled){
        this.enableUnderApproximation = useUnderApproximationEnabled;
    }

	public void setUseReduction(boolean useReduction){
		this.useReduction = useReduction;
	}

    public void setUseColoredReduction(boolean useReduction){
        this.useColoredReduction = useReduction;
    }
	
	public void setUseSiphontrap(boolean useSiphontrap) {
		this.useSiphontrap = useSiphontrap;
	}
	
	public void setUseQueryReduction(boolean useQueryReduction) {
		this.useQueryReduction = useQueryReduction;
	}
	
	public void setUseStubbornReduction(boolean useStubbornReduction) {
		this.useStubbornReduction = useStubbornReduction;
	}
	
	public boolean useReduction(){
		return useReduction;
	}

    public boolean useColoredReduction(){
        return useColoredReduction;
    }
	
	public boolean useOverApproximation(){
		return overApproximation;
	}

	/**
	 * @param hashTableSize
	 *            the hashTableSize to set
	 */
	public void setHashTableSize(HashTableSize hashTableSize) {
		this.hashTableSize = hashTableSize;
	}

	/**
	 * @return the hashTableSize
	 */
	public HashTableSize getHashTableSize() {
		return hashTableSize;
	}

	/**
	 * @param extrapolationOption
	 *            the extrapolationOption to set
	 */
	public void setExtrapolationOption(ExtrapolationOption extrapolationOption) {
		this.extrapolationOption = extrapolationOption;
	}

	/**
	 * @return the extrapolationOption
	 */
	public ExtrapolationOption getExtrapolationOption() {
		return extrapolationOption;
	}

	public void setRawVerification(boolean useRawVerification) {
		this.useRawVerification = useRawVerification;
	}

	public boolean getRawVerification() {
		return useRawVerification;
	}

	public void setRawVerificationPrompt(String rawVerificationPrompt) {
		this.rawVerificationPrompt = rawVerificationPrompt;
	}

	public String getRawVerificationPrompt() {
		return rawVerificationPrompt;
	}

    public TAPNQuery(String name, int capacity, TCTLAbstractProperty property,
			TraceOption traceOption, SearchOption searchOption,
			ReductionOption reductionOption, boolean symmetry, boolean gcd, boolean timeDart, boolean pTrie, boolean overApproximation, HashTableSize hashTabelSize,
			ExtrapolationOption extrapolationOption, WorkflowMode workflow, boolean isColored
    ) {
		this(name, capacity, property, traceOption, searchOption, reductionOption, symmetry, gcd, timeDart, pTrie, overApproximation, false, hashTabelSize, extrapolationOption, new InclusionPlaces(), isColored);
		this.setWorkflowMode(workflow);
	}

    public TAPNQuery(String name, int capacity, TCTLAbstractProperty property,
			TraceOption traceOption, SearchOption searchOption,
			ReductionOption reductionOption, boolean symmetry, boolean gcd, boolean timeDart, boolean pTrie, boolean overApproximation, HashTableSize hashTabelSize,
			ExtrapolationOption extrapolationOption, WorkflowMode workflow, long strongSoundnessBound, boolean isColored
    ) {
		this(name, capacity, property, traceOption, searchOption, reductionOption, symmetry, gcd, timeDart, pTrie, overApproximation, hashTabelSize, extrapolationOption, workflow, isColored);
		this.setStrongSoundnessBound(strongSoundnessBound);
	}
	
	public TAPNQuery(String name, int capacity, TCTLAbstractProperty property,
			TraceOption traceOption, SearchOption searchOption,
			ReductionOption reductionOption, boolean symmetry, boolean gcd, boolean timeDart, boolean pTrie, HashTableSize hashTabelSize,
			ExtrapolationOption extrapolationOption, boolean isColored) {
		this(name, capacity, property, traceOption, searchOption, reductionOption, symmetry, gcd, timeDart, pTrie, false, false, hashTabelSize, extrapolationOption, new InclusionPlaces(), isColored);
	}
	
	public TAPNQuery(String name, int capacity, TCTLAbstractProperty property,
			TraceOption traceOption, SearchOption searchOption,
			ReductionOption reductionOption, boolean symmetry, boolean gcd, boolean timeDart, boolean pTrie, boolean overApproximation, boolean reduction, HashTableSize hashTabelSize,
			ExtrapolationOption extrapolationOption, InclusionPlaces inclusionPlaces, boolean isColored) {
		this(name, capacity, property, traceOption, searchOption, reductionOption, symmetry, gcd, timeDart, pTrie, overApproximation, reduction, hashTabelSize, extrapolationOption, new InclusionPlaces(), false, false, 0, true, true, true, isColored, false);
	}
	
	public TAPNQuery(String name, int capacity, TCTLAbstractProperty property,
			TraceOption traceOption, SearchOption searchOption,
			ReductionOption reductionOption, boolean symmetry, boolean gcd, boolean timeDart, boolean pTrie, boolean overApproximation, boolean reduction, HashTableSize hashTabelSize,
			ExtrapolationOption extrapolationOption, InclusionPlaces inclusionPlaces, boolean enableOverApproximation, boolean enableUnderApproximation, 
			int approximationDenominator, boolean partitioning, boolean colorFixpoint, boolean symmetricVars, boolean isColored, boolean coloredReduction) {
		this(name, capacity, property, traceOption, searchOption, reductionOption, symmetry, gcd, timeDart, pTrie, overApproximation, reduction, hashTabelSize, extrapolationOption, inclusionPlaces, enableOverApproximation, enableUnderApproximation, approximationDenominator, partitioning, colorFixpoint, symmetricVars, isColored, coloredReduction, false, "-x 1 ");
	}

	public TAPNQuery(String name, int capacity, TCTLAbstractProperty property,
			TraceOption traceOption, SearchOption searchOption,
			ReductionOption reductionOption, boolean symmetry, boolean gcd, boolean timeDart, boolean pTrie, boolean overApproximation, boolean reduction, HashTableSize hashTabelSize,
			ExtrapolationOption extrapolationOption, InclusionPlaces inclusionPlaces, boolean enableOverApproximation, boolean enableUnderApproximation, 
			int approximationDenominator, boolean partitioning, boolean colorFixpoint, boolean symmetricVars, boolean isColored, boolean coloredReduction, boolean useRawVerification, String rawVerificationPrompt) {
		this.setName(name);
		this.setCapacity(capacity);
		this.property = property;
		this.setTraceOption(traceOption);
		this.setSearchOption(searchOption);
		this.setReductionOption(reductionOption);
		this.symmetry = symmetry;
		this.gcd = gcd;
		this.timeDart = timeDart;
		this.pTrie = pTrie;
		this.overApproximation = overApproximation;
		this.setHashTableSize(hashTabelSize);
		this.setExtrapolationOption(extrapolationOption);
		this.inclusionPlaces = inclusionPlaces;
        this.useReduction = reduction;
        this.useColoredReduction = coloredReduction;
		this.enableOverApproximation = enableOverApproximation;
		this.enableUnderApproximation = enableUnderApproximation;
		this.denominator = approximationDenominator;
        this.partitioning = partitioning;
        this.colorFixpoint = colorFixpoint;
        this.symmetricVars = symmetricVars;
        this.isColored = isColored;
		this.useRawVerification = useRawVerification;
		this.rawVerificationPrompt = rawVerificationPrompt;
		this.useRawVerification = useRawVerification;
		this.rawVerificationPrompt = rawVerificationPrompt;
	}

	@Override
	public String toString() {
		return getName();
	}

	public void set(TAPNQuery newQuery) {
		name = newQuery.getName();
		capacity = newQuery.getCapacity();
		oldCapacity = newQuery.getOldCapacity();
		property = newQuery.getProperty();
		traceOption = newQuery.getTraceOption();
		searchOption = newQuery.getSearchOption();
		reductionOption = newQuery.getReductionOption();
		symmetry = newQuery.useSymmetry();
		hashTableSize = newQuery.getHashTableSize();
		extrapolationOption = newQuery.getExtrapolationOption();
		discreteInclusion = newQuery.discreteInclusion();
		inclusionPlaces = newQuery.inclusionPlaces();
		useSiphontrap = newQuery.isSiphontrapEnabled();
		useQueryReduction = newQuery.isQueryReductionEnabled();
		useStubbornReduction = newQuery.isStubbornReductionEnabled();
		useTarOption = newQuery.isTarOptionEnabled();
	}

    public void copyOptions(TAPNQuery query){
        setUseOverApproximation(query.useOverApproximation());
        setUseUnderApproximationEnabled(query.isUnderApproximationEnabled());
        setUseSiphontrap(query.isSiphontrapEnabled());
        setUseQueryReduction(query.isQueryReductionEnabled());
        setUseStubbornReduction(query.isStubbornReductionEnabled());
        setUseTarOption(query.isTarOptionEnabled());
        setApproximationDenominator(query.approximationDenominator());
        setDiscreteInclusion(query.discreteInclusion());
        setCapacity(query.getCapacity());
        setTraceOption(query.getTraceOption());
        setSearchOption(query.getSearchOption());
        setReductionOption(query.getReductionOption());
        setUseSymmetricVars(query.useSymmetricVars());
        setUseColorFixpoint(query.useColorFixpoint());
        setUseGCD(query.useGCD());
        setUseTimeDarts(query.useTimeDarts());
        setUsePTrie(query.usePTrie());
        setUsePartitioning(query.usePartitioning());
        setUseOverApproximationEnabled(query.isOverApproximationEnabled());
        setUseReduction(query.useReduction());
        setExtrapolationOption(query.getExtrapolationOption());
        setUseColoredReduction(query.useColoredReduction());
        setUseTarjan(query.isTarjan());
		setRawVerification(query.getRawVerification());
		setRawVerificationPrompt(query.getRawVerificationPrompt());
        setSmcSettings(query.getSmcSettings());
        setBenchmarkMode(isBenchmarkMode());
        setBenchmarkRuns(getBenchmarkRuns());
        setParallel(isParallel());
    }

    public void setProperty(TCTLAbstractProperty property) {
        this.property = property;
    }

	public InclusionPlaces inclusionPlaces() {
		return inclusionPlaces;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;	
	}
	
	public boolean isActive() {
		return isActive;
	}

	public TAPNQuery copy() {
		TAPNQuery copy = new TAPNQuery(name, capacity, property.copy(), traceOption, searchOption, reductionOption, symmetry, gcd, timeDart, pTrie, overApproximation, useReduction, hashTableSize, extrapolationOption, inclusionPlaces, enableOverApproximation, enableUnderApproximation, denominator, partitioning, colorFixpoint, symmetricVars, isColored, useColoredReduction);
		copy.setDiscreteInclusion(discreteInclusion);
		copy.setActive(isActive);
		copy.setCategory(queryCategory);
		copy.setUseSiphontrap(this.isQueryReductionEnabled());
		copy.setUseQueryReduction(this.isQueryReductionEnabled());
		copy.setUseStubbornReduction(this.isStubbornReductionEnabled());
		copy.setUseTarOption(this.isTarOptionEnabled());
		copy.setSmcSettings(this.getSmcSettings());
        copy.setBenchmarkMode(this.isBenchmarkMode());
        copy.setBenchmarkRuns(this.getBenchmarkRuns());
        copy.setParallel(this.isParallel());
		return copy;
	}
	
	public QueryType queryType(){
		if(property instanceof TCTLEFNode) return QueryType.EF;
		else if(property instanceof TCTLEGNode) return QueryType.EG;
		else if(property instanceof TCTLAFNode) return QueryType.AF;
        else if(queryCategory == QueryCategory.SMC && property instanceof LTLFNode) return QueryType.PF;
        else if(queryCategory == QueryCategory.SMC && property instanceof LTLGNode) return QueryType.PG;
		else return QueryType.AG;
	}

	public WorkflowMode getWorkflowMode() {
		return workflow;
	}

	public void setWorkflowMode(WorkflowMode workflow) {
		this.workflow = workflow;
	}

    public long getStrongSoundnessBound(){
        return strongSoundnessBound;
    }
    
    public void setStrongSoundnessBound(long newval) {
        strongSoundnessBound = newval;
    }
    
    public void setCategory(QueryCategory category){
    	this.queryCategory = category;
    }
    
    public QueryCategory getCategory(){
    	return this.queryCategory;
    }
    
    public void setAlgorithmOption(AlgorithmOption option){
    	this.algorithmOption = option;
    }
    
    public AlgorithmOption getAlgorithmOption(){
    	return this.algorithmOption;
    }

    public SMCSettings getSmcSettings() { return this.smcSettings; }

    public void setSmcSettings(SMCSettings newSettings) { this.smcSettings = newSettings; }

    public boolean isBenchmarkMode() { return benchmark; }
    public void setBenchmarkMode(boolean mode) {
        benchmark = mode;
    }

    public int getBenchmarkRuns() { return benchmarkRuns; }
    public void setBenchmarkRuns(int runs) {
        benchmarkRuns = runs;
    }

    public boolean isParallel() { return parallel; }
    public void setParallel(boolean value) {
        parallel = value;
    }

    public void setVerificationType(VerificationType verificationType) {
        this.verificationType = verificationType;
    }

    public VerificationType getVerificationType() {
        return verificationType;
    }

    public void setNumberOfTraces(int numberOfTraces) {
        this.numberOfTraces = numberOfTraces;
    }

    public int getNumberOfTraces() {
        return numberOfTraces;
    }

    public void setSmcTraceType(SMCTraceType traceType) {
        this.smcTraceType = traceType;
    }

    public SMCTraceType getSmcTraceType() {
        return smcTraceType;
    }

    public boolean isSimulate() {
        return verificationType == VerificationType.SIMULATE;
    }



    public boolean hasUntimedOnlyProperties(){
        if(!(
                property instanceof TCTLAFNode || property instanceof TCTLAGNode ||
                property instanceof TCTLEFNode || property instanceof TCTLEGNode ||
                queryType() == QueryType.PF || queryType() == QueryType.PG
        )){
            return true;
        } else if(property.hasNestedPathQuantifiers()){
            return true;
        }
        return false;
    }

    public TAPNQuery convertPropertyForReducedNet(String templateName){
	    TAPNQuery convertedQuery = copy();
	    convertedQuery.property.convertForReducedNet(templateName);
        return convertedQuery;
    }
}
