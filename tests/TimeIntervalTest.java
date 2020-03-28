import org.junit.jupiter.api.Test;

import dk.aau.cs.model.tapn.Bound;
import dk.aau.cs.model.tapn.IntBound;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.util.RequireException;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class TimeIntervalTest  {
	
	@Test
	public void TimeIntervalConstructor(){
		assertThrows(RequireException.class, ()-> new TimeInterval(false, new IntBound(0), new IntBound(0), true));
	}
	
	@Test
	public void TimeIntervalConstructor_intervaltype2(){
		assertThrows(RequireException.class, ()-> new TimeInterval(true, new IntBound(5), new IntBound(5), false));
	}
	
	@Test
	public void TimeIntervalConstructor_intervaltype3(){
		assertThrows(RequireException.class, ()-> new TimeInterval(false, new IntBound(5), new IntBound(5), false));
	}
	
	@Test
	public void TimeIntervalConstructor_intervaltype4(){
		assertThrows(RequireException.class, ()-> new TimeInterval(true, new IntBound(6), new IntBound(5), false));
	}
	
	@Test
	public void TimeIntervalConstructor_intervaltype5(){
		assertThrows(RequireException.class, ()-> new TimeInterval(false, new IntBound(6), new IntBound(5), false));
	}
	
	@Test
	public void TimeInterval_InfinityUpper(){
		new TimeInterval(true, new IntBound(6), Bound.Infinity, false);
	}
	
	@Test
	public void TimeInterval_InfinityUpper_included(){
		assertThrows(RequireException.class, ()-> new TimeInterval(true, new IntBound(6), Bound.Infinity, true));
	}
	
	@Test
	public void TimeInterval_Infinity_lower(){
		assertThrows(RequireException.class, ()-> new TimeInterval(true, Bound.Infinity, Bound.Infinity, false));
	}
	
	
	
	@Test()
	public void timeInterval(){
		new TimeInterval(false, new IntBound(4), new IntBound(5), false);
	}
}
