package dk.aau.cs.petrinet.colors;

public class IntervalBound {
	private int scale;
	private int offset;
	
	public IntervalBound(int scale, int offset){
		this.scale = scale;
		this.offset = offset;
	}

	public IntervalBound(String bound) {
		if(bound.trim().equals("inf")){
			this.scale = 0;
			this.offset = -1;
		}else{
			if(bound.contains("*")){
				this.scale = Integer.parseInt(bound.substring(0, bound.indexOf("*")));
				
				if(bound.contains("+")){
					this.offset = Integer.parseInt(bound.substring(bound.lastIndexOf("+")+1));
				}else{
					this.offset = 0;
				}
			}else{
				this.scale = 0;
				this.offset = Integer.parseInt(bound);
			}
		}
	}
	
	public IntervalBound(IntervalBound other) {
		this.scale = other.scale;
		this.offset = other.offset;
	}

	public boolean goesToInfinity(){
		return scale == 0 && offset == -1;
	}

	public boolean isZero() {
		return scale == 0 && offset == 0;
	}

	public String toString(String valueName) {
		if(goesToInfinity()){
			return "";
		}else if(scale == 0){
			return String.valueOf(offset);
		}else if(offset == 0){
			return String.format("%1$d*%2$s", scale, valueName);
		}
		else{
			return String.format("%1$d*%2$s+%3$d", scale, valueName, offset);
		}
	}
	
	@Override
	public String toString() {
		return toString("val");
	}
}
