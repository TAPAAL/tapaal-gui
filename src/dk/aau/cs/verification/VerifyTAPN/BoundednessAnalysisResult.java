package dk.aau.cs.verification.VerifyTAPN;

public class BoundednessAnalysisResult {
	private int maxUsedTokens;
	private int totalTokens;
	
	public BoundednessAnalysisResult(int totalTokens, int maxUsedTokens){
		this.maxUsedTokens = maxUsedTokens;
		this.totalTokens = totalTokens;
	}
	
	@Override
	public String toString() {
		if(maxUsedTokens <= totalTokens){
			return "The net is bounded.";
		}else{
			return "The net is unbounded.";
		}
	}
}
