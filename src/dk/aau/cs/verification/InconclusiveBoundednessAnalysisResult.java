package dk.aau.cs.verification;


public class InconclusiveBoundednessAnalysisResult extends
		BoundednessAnalysisResult {

	public InconclusiveBoundednessAnalysisResult() {
		super(0,0,0, true);
	}
	
	@Override
	public Boundedness boundednessResult() {
		return Boundedness.Inconclusive;
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(System.getProperty("line.separator"));
		buffer.append(System.getProperty("line.separator"));
		buffer.append("The answer is conclusive only if the net is bounded");
		buffer.append(System.getProperty("line.separator"));
		buffer.append("for the given number of extra tokens. It is");
		buffer.append(System.getProperty("line.separator"));
		buffer.append("recommended to run a boundedness check ");
		buffer.append(System.getProperty("line.separator"));
		buffer.append("available in the query dialog.");
		return buffer.toString();
	}
}
