package dk.aau.cs.verification;

public class BoundednessAnalysisResult {
	private int maxUsedTokens;
	private int totalTokens;
	private int extraTokens;
        private boolean isUPPAAL;
	
	public BoundednessAnalysisResult(int totalTokens, int maxUsedTokens, int extraTokens){
		this.maxUsedTokens = maxUsedTokens;
		this.totalTokens = totalTokens;
		this.extraTokens = extraTokens;
                this.isUPPAAL = false;
	}
        
        public BoundednessAnalysisResult(int totalTokens, int maxUsedTokens, int extraTokens, boolean isUPPAAL){
                this(totalTokens, maxUsedTokens, extraTokens);
                this.isUPPAAL = isUPPAAL;
	}
	
	@Override
	public String toString() {
		if(boundednessResult().equals(Boundedness.Bounded)){
			return "";
		}else{
			StringBuffer buffer = new StringBuffer();
			buffer.append(System.getProperty("line.separator"));
			buffer.append(System.getProperty("line.separator"));
			buffer.append(String.format("Only markings with at most %1$d tokens (%2$d extra tokens)", totalTokens, extraTokens));
			buffer.append(System.getProperty("line.separator"));
			buffer.append("were explored. Try to increase the number of extra tokens.");
			return buffer.toString();
		}
	}

	public Boundedness boundednessResult(){
		if(maxUsedTokens <= totalTokens) return Boundedness.Bounded;
		else return Boundedness.NotBounded;
	}
	
	public int usedTokens() {
		return maxUsedTokens;
	}
	
	public int totalTokens(){
		return totalTokens;
	}
	
	public int tokensInNet(){
		return totalTokens - extraTokens;
	}
        
        public boolean isUPPAAL() {
            return isUPPAAL;
        }
	
}
