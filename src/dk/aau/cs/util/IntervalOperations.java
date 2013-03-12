package dk.aau.cs.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

import pipe.gui.Pipe;

import dk.aau.cs.model.tapn.Bound;
import dk.aau.cs.model.tapn.RatBound;
import dk.aau.cs.model.tapn.TimeInterval;

public class IntervalOperations {
	
	private enum BoundToCheck {
		lower, upper
	}
	
	public static RatBound getRatBound(Bound b){
		if (b instanceof RatBound){
			return (RatBound)b;
		} else {
			return new RatBound(new BigDecimal(b.value(), new MathContext(Pipe.AGE_PRECISION)));
		}
	}
	
	public static TimeInterval union(TimeInterval i1, TimeInterval i2){
		if(i1 == null){
			return i2;
		} else if (i2 == null){
			return i1;
		}
		
		//The intervals cannot be unioned to a single interval
		if(intersection(i1, i2) == null){
			return null;
		}
		
		TimeInterval lower = min(i1, i2, BoundToCheck.lower);
		TimeInterval upper = max(i1, i2, BoundToCheck.upper);
		
		return new TimeInterval(lower.IsLowerBoundNonStrict(), lower.lowerBound(), upper.upperBound(), upper.IsUpperBoundNonStrict()); 
	}
	
	
	public static TimeInterval intersection(TimeInterval i1, TimeInterval i2){
		if(i1 == null || i2 == null){
			return null;
		}
		
		TimeInterval upper, lower;
		
		upper = min(i1, i2, BoundToCheck.upper);
		lower = max(i1, i2, BoundToCheck.lower);
		BigDecimal iUp = getRatBound(upper.upperBound()).getBound();
		BigDecimal iLow = getRatBound(lower.lowerBound()).getBound();
		
		if(iUp.compareTo(iLow) > 0 || iUp.compareTo(iLow) == 0 && lower.IsLowerBoundNonStrict() && upper.IsUpperBoundNonStrict() || (upper.upperBound().value() < 0)){
			return new TimeInterval(lower.IsLowerBoundNonStrict(), lower.lowerBound(), upper.upperBound(), upper.IsUpperBoundNonStrict());
		} else {
			return null;
		}
	}
	
	public static TimeInterval min(TimeInterval i1, TimeInterval i2, BoundToCheck b){
		if(i1 == null){
			return i2;
		} else if (i2 == null){
			return i1;
		}
		
		switch (b) {
		case lower:
			BigDecimal b1 = getRatBound(i1.lowerBound()).getBound();
			BigDecimal b2 = getRatBound(i2.lowerBound()).getBound();
			
			if(b1.compareTo(b2) < 0 || b1.compareTo(b2) == 0 && i1.IsLowerBoundNonStrict() && !i2.IsLowerBoundNonStrict()){
				return i1;
			} else {
				return i2;
			}
		case upper:
			b1 = getRatBound(i1.upperBound()).getBound();
			b2 = getRatBound(i2.upperBound()).getBound();
			if(b1.compareTo(BigDecimal.ZERO) >= 0 && b2.compareTo(BigDecimal.ZERO) >= 0){
				if(b1.compareTo(b2) < 0 || b1.compareTo(b2) == 0 && !i1.IsUpperBoundNonStrict() && i2.IsUpperBoundNonStrict()){
					return i1;
				} else {
					return i2;
				}
			} else if (b1.compareTo(BigDecimal.ZERO) < 0){
				return i2;
			} else {
				return i1;
			}
		default:
			return null;
		}
	}
	
	public static TimeInterval max(TimeInterval i1, TimeInterval i2, BoundToCheck b){
		if(i1 == null){
			return i2;
		} else if (i2 == null){
			return i1;
		}
		
		TimeInterval min = min(i1, i2, b);
		if(min.equals(i1)){
			return i2;
		} else {
			return i1;
		}
	}
	
	public static Bound getMaxNoInfBound(TimeInterval interval){
		if(interval.upperBound() instanceof Bound.InfBound){
			return interval.lowerBound();
		} else {
			return interval.upperBound();
		}
	}
	
	/*
	 * Calculates the resulting interval sequence, of the intersection between two interval sequences.
	 */
	public static ArrayList<TimeInterval> intersectingInterval(List<TimeInterval> IS1, List<TimeInterval> IS2){
		ArrayList<TimeInterval> output = new ArrayList<TimeInterval>();
		if(IS1.size() < 1 || IS2.size() < 1){
			return output;
		}
		boolean incrementI = false;

		int i = 0, j = 0;
		
		RatBound i1up, i2up;
		while(i < IS1.size() && j < IS2.size()){
			if(IS1.get(i) != null && IS2.get(j) != null){
				if(IS1.get(i).upperBound() instanceof RatBound){ i1up = ((RatBound)IS1.get(i).upperBound());}
				else{i1up = new RatBound(new BigDecimal(IS1.get(i).upperBound().value(), new MathContext(Pipe.AGE_PRECISION)));}
				if(IS2.get(j).upperBound() instanceof RatBound){ i2up = ((RatBound)IS2.get(j).upperBound());}
				else{i2up = new RatBound(new BigDecimal(IS2.get(j).upperBound().value(), new MathContext(Pipe.AGE_PRECISION)));}

				if(intersection(IS1.get(i), IS2.get(j)) != null){
					output.add(intersection(IS1.get(i), IS2.get(j)));
				}
			
				if(i1up.compareTo(i2up) == -1 || (i2up.compareTo(i1up) == 0 && (IS2.get(j).IsUpperBoundNonStrict() == IS1.get(i).IsUpperBoundNonStrict() || !IS1.get(i).IsUpperBoundNonStrict()))){
					incrementI = true;
				}
				if(i2up.compareTo(i1up) == -1 || (i2up.compareTo(i1up) == 0 && (IS2.get(j).IsUpperBoundNonStrict() == IS1.get(i).IsUpperBoundNonStrict() || !IS2.get(j).IsUpperBoundNonStrict()))){
					j++;
				}
			
				if(incrementI){
					i++;
					incrementI = false;
				}
			} else {
				if(IS1.get(i) == null){
					i++;
				}
				if(IS2.get(j) == null){
					j++;
				}
			}
		}
		return output;
	}
	
	/*
	 * Unions two interval sequences together
	 */
	public static ArrayList<TimeInterval> unionIntervalSequences(List<TimeInterval> IS1, List<TimeInterval> IS2){
		RatBound i1up, i2up, i1lo, i2lo;

		if(IS1.size() < 1){
			return new ArrayList<TimeInterval>(IS2);
		} else if( IS2.size() < 1){
			return new ArrayList<TimeInterval>(IS1);
		}
		
		ArrayList<TimeInterval> output = new ArrayList<TimeInterval>();
		int i = 0, j = 0;
		
		while(i < IS1.size() || j < IS2.size()){
			TimeInterval temp = null;
			while(i < IS1.size() && j < IS2.size()){
				if(IS1.get(i).upperBound() instanceof RatBound){ i1up = ((RatBound)IS1.get(i).upperBound());}
				else{i1up = new RatBound(new BigDecimal(IS1.get(i).upperBound().value(), new MathContext(Pipe.AGE_PRECISION)));}
				if(IS2.get(j).upperBound() instanceof RatBound){ i2up = ((RatBound)IS2.get(j).upperBound());}
				else{i2up = new RatBound(new BigDecimal(IS2.get(j).upperBound().value(), new MathContext(Pipe.AGE_PRECISION)));}
				if(IS1.get(i).lowerBound() instanceof RatBound){ i1lo = ((RatBound)IS1.get(i).lowerBound());}
				else{i1lo = new RatBound(new BigDecimal(IS1.get(i).lowerBound().value(), new MathContext(Pipe.AGE_PRECISION)));}
				if(IS2.get(j).lowerBound() instanceof RatBound){ i2lo = ((RatBound)IS2.get(j).lowerBound());}
				else{i2lo = new RatBound(new BigDecimal(IS2.get(j).lowerBound().value(), new MathContext(Pipe.AGE_PRECISION)));}

				if(intersection(IS1.get(i), IS2.get(j)) != null || (i1lo.compareTo(i2up)==0 && (IS1.get(i).IsLowerBoundNonStrict()||IS2.get(j).IsUpperBoundNonStrict()))
				        || (i2lo.compareTo(i1up)==0 && (IS2.get(j).IsLowerBoundNonStrict()||IS1.get(i).IsUpperBoundNonStrict()))){

					temp = union(temp, union(IS1.get(i), IS2.get(j)));
					
					if(i1up.compareTo(i2up) == 1 || (i1up.compareTo(i2up) == 0 && !IS2.get(j).IsUpperBoundNonStrict() && IS1.get(i).IsUpperBoundNonStrict())){
						j++;
					} else if(i2up.compareTo(i1up) == 1 || (i2up.compareTo(i1up) == 0 && !IS1.get(i).IsUpperBoundNonStrict() && IS2.get(j).IsUpperBoundNonStrict())){
						i++;
					} else {
						break;
					}
				}else{
					break;
				}
			}
			if(temp != null){
				output.add(temp);
				i++;
				j++;
			} else if(i<IS1.size() && j<IS2.size() && min(IS1.get(i), IS2.get(j), BoundToCheck.upper).equals(IS1.get(i))){
				output.add(IS1.get(i));
				i++;
			} else if(i<IS1.size() && j<IS2.size() && min(IS2.get(j), IS1.get(i), BoundToCheck.upper).equals(IS2.get(j))){
				output.add(IS2.get(j));
				j++;
			} 
			if(i > (IS1.size()-1)){
				for(int jTemp = j; jTemp < IS2.size(); jTemp++){
					output.add(IS2.get(jTemp));
				}
				j = IS2.size() + 1;
			}
			if(j > (IS2.size()-1)){
				for(int iTemp = i; iTemp < IS1.size(); iTemp++){
					output.add(IS1.get(iTemp));
				}
				i = IS1.size() + 1;
			}
		}		
		return output;
	}
}
