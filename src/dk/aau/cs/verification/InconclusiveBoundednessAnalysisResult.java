package dk.aau.cs.verification;


public class InconclusiveBoundednessAnalysisResult extends
		BoundednessAnalysisResult {

	public InconclusiveBoundednessAnalysisResult() {
		super(0,0);
	}
	
	@Override
	public Boundedness boundednessResult() {
		return Boundedness.Inconclusive;
	}
	
	@Override
	public String toString() {
		return "\nThe answer is conclusive only if the net is bounded\nfor the given number of extra tokens.";
	}
}
