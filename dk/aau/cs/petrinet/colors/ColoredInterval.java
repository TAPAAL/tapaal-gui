package dk.aau.cs.petrinet.colors;

public class ColoredInterval {
	private char lowerParenthesis;
	private IntervalBound lower;
	private IntervalBound upper;
	private char upperParenthesis;
	
	public ColoredInterval(String interval){
		lowerParenthesis = interval.charAt(0);
		upperParenthesis = interval.charAt(interval.length()-1);
		
		String[] split = interval.split(",");
		lower = new IntervalBound(split[0].substring(1));
		upper = new IntervalBound(split[1].substring(0,split[1].length()-1));
	}
}
