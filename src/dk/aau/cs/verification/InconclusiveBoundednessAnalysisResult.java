package dk.aau.cs.verification;


public class InconclusiveBoundednessAnalysisResult extends
		BoundednessAnalysisResult {

	public InconclusiveBoundednessAnalysisResult() {
		super(0,0,0);
	}
	
	@Override
	public Boundedness boundednessResult() {
		return Boundedness.Inconclusive;
	}
	
	@Override
	public String toString() {
		return "\n\nThe answer is conclusive only if the net is bounded\nfor the given number of extra tokens. It is \nrecommended that you run a boundedness check \nfrom the query dialog.";
	}
}
