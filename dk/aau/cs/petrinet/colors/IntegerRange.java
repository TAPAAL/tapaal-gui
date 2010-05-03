package dk.aau.cs.petrinet.colors;



public class IntegerRange implements Comparable<IntegerRange> {
	private int from;
	private int to;
	private boolean goesToInfinity = false;

	public IntegerRange(int single, boolean goesToInfinity){
		this(single);
		this.goesToInfinity = goesToInfinity;
	}

	public IntegerRange(int single){
		from = single;
		to = single;
	}

	public IntegerRange(int from, int to){
		if(from > to) throw new IllegalArgumentException();

		this.from = from;
		this.to = to;
	}

	public IntegerRange(String range) {
		if(range.contains("-")){
			if(range.indexOf("-") == range.length()-1){ // Format: x-
				from = Integer.parseInt(range.substring(0,range.length()-1));
				goesToInfinity = true;
			}else{ // Format: x-y
				String[] limits = range.split("-");
				if(limits.length > 2) throw new IllegalArgumentException("does not match range format");

				from = Integer.parseInt(limits[0]);
				to = Integer.parseInt(limits[1]);
			}
		}else{ // Format: x
			from = Integer.parseInt(range);
			to = from;
		}
	}

	public IntegerRange(IntegerRange range) {
		this.to = range.to;
		this.from = range.from;
		this.goesToInfinity = range.goesToInfinity;
	}

	public void setTo(int to) {
		this.to = to;
	}
	public int getTo() {
		return to;
	}
	public void setFrom(int from) {
		this.from = from;
	}
	public int getFrom() {
		return from;
	}

	public int compareTo(IntegerRange range) {
		return from - range.from;
	}

	public String convertToTAGuardString(String valueVarName) {
		if(goesToInfinity){
			return String.format("%1$s >= %2$d", valueVarName, from);
		}else if(from == to){
			return String.format("%1$s == %2$d",valueVarName, from);
		}else {
			return String.format("%1$d <= %2$s  && %2$s <= %3$d", from, valueVarName, to);
		}
	}

	public boolean contains(int outputValue) {
		if(goesToInfinity){
			return from <= outputValue;
		}else{
			return from <= outputValue && outputValue <= to;
		}
	}

	public boolean isEmpty() {
		return from == -1 && to == -1;
	}

	public IntegerRange intersect(IntegerRange other) {
		if(isSingle()){ // this: x
			return other.contains(from) ? new IntegerRange(from) : emptyIntegerRange();
		}else if(isFiniteRange()){ // this: x-y
			int maxFrom = from > other.from ? from : other.from;
			int minTo = other.goesToInfinity || to < other.to ? to : other.to;

			return maxFrom > minTo ? emptyIntegerRange() : new IntegerRange(maxFrom, minTo);
		}else{ // this: x-
			if(other.isSingle()){ // other: z
				return contains(other.from) ? new IntegerRange(other.from) : emptyIntegerRange();
			}else if(other.isFiniteRange()){ // other: z-w
				int maxFrom = from > other.from ? from : other.from;
				int minTo = other.to;

				return maxFrom > minTo ? emptyIntegerRange() : new IntegerRange(maxFrom, minTo);
			}else{ // other: z-
				int maxFrom = from > other.from ? from : other.from;
				return new IntegerRange(maxFrom, true);
			}
		}
	}

	private IntegerRange emptyIntegerRange() {
		return new IntegerRange(-1,-1);
	}

	private boolean isSingle() {
		return !goesToInfinity && from == to;
	}

	private boolean isFiniteRange() {
		return !isSingle() && !goesToInfinity;
	}
	

	@Override
	public String toString() {
		if(isSingle()){
			return String.valueOf(from);
		}else if(isFiniteRange()){
			return String.valueOf(from) + "-" + String.valueOf(to);
		}else{
			return String.valueOf(from) + "-";
		}
	}
}
