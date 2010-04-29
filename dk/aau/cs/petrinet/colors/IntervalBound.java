package dk.aau.cs.petrinet.colors;

import pipe.dataLayer.colors.IntOrConstant;

public class IntervalBound {
	private int scale;
	private int offset;
	
	public IntervalBound(int scale, int offset){
		this.scale = scale;
		this.offset = offset;
	}

	public IntervalBound(String bound) {
		if(bound.equals("inf")){
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
}
