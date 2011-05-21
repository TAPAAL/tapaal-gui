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
			return "";
		}else{
			return String.format("\nOnly markings with at most %1$d tokens were explored.\nTry to increase the number of extra tokens.", totalTokens);
		}
	}
}
