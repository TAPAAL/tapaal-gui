package dk.aau.cs.verification;

public class BoundednessAnalysisResult {
	private int maxUsedTokens;
	private int totalTokens;
	private int extraTokens;
	
	public BoundednessAnalysisResult(int totalTokens, int maxUsedTokens, int extraTokens){
		this.maxUsedTokens = maxUsedTokens;
		this.totalTokens = totalTokens;
		this.extraTokens = extraTokens;
	}
	
	@Override
	public String toString() {
		if(boundednessResult().equals(Boundedness.Bounded)){
			return "";
		}else{
			return String.format("\n\nOnly markings with at most %1$d tokens (%2$d extra tokens) \nwere explored. Try to increase the number of extra tokens.", totalTokens, extraTokens);
		}
	}

	public Boundedness boundednessResult(){
		if(maxUsedTokens <= totalTokens) return Boundedness.Bounded;
		else return Boundedness.NotBounded;
	}
	
	public int usedTokens() {
		return maxUsedTokens;
	}
}
