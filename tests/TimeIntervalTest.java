import org.junit.Test;

import dk.aau.cs.model.tapn.Bound;
import dk.aau.cs.model.tapn.IntBound;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.util.RequireException;

public class TimeIntervalTest  {
	
	@Test(expected=RequireException.class)
	public void TimeIntervalConstructor(){
		new TimeInterval(false, new IntBound(0), new IntBound(0), true);
	}
	
	@Test(expected=RequireException.class)
	public void TimeIntervalConstructor_intervaltype2(){
		new TimeInterval(true, new IntBound(5), new IntBound(5), false);
	}
	
	@Test(expected=RequireException.class)
	public void TimeIntervalConstructor_intervaltype3(){
		new TimeInterval(false, new IntBound(5), new IntBound(5), false);
	}
	
	@Test(expected=RequireException.class)
	public void TimeIntervalConstructor_intervaltype4(){
		new TimeInterval(true, new IntBound(6), new IntBound(5), false);
	}
	
	@Test(expected=RequireException.class)
	public void TimeIntervalConstructor_intervaltype5(){
		new TimeInterval(false, new IntBound(6), new IntBound(5), false);
	}
	
	@Test
	public void TimeInterval_InfinityUpper(){
		new TimeInterval(true, new IntBound(6), Bound.Infinity, false);
	}
	
	@Test(expected=RequireException.class)
	public void TimeInterval_InfinityUpper_included(){
		new TimeInterval(true, new IntBound(6), Bound.Infinity, true);
	}
	
	@Test(expected=RequireException.class)
	public void TimeInterval_Infinity_lower(){
		new TimeInterval(true, Bound.Infinity, Bound.Infinity, false);
	}
	
	
	
	@Test()
	public void timeInterval(){
		new TimeInterval(false, new IntBound(4), new IntBound(5), false);
	}
}
