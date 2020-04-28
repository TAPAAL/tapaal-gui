import dk.aau.cs.model.tapn.*;
import dk.aau.cs.util.RequireException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
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

	//XXX tests used when refactoring toString to call toString(true)
	@Test
	public void IntervalToStringTrueIsSameAsIntervalToString() {
		TimeInterval t = new TimeInterval(true, new IntBound(0), Bound.Infinity, false);
		TimeInterval t2 = new TimeInterval(true, new ConstantBound(new Constant("k", 5)),new IntBound(8), false);

		Assertions.assertEquals(t.toString(), t.toString(true));
        Assertions.assertEquals(t.toString(), t.toString(false));

        Assertions.assertEquals(t2.toString(), t2.toString(true));
        Assertions.assertNotEquals(t2.toString(), t2.toString(false));

	}
	
	@Test()
	public void timeInterval(){
		new TimeInterval(false, new IntBound(4), new IntBound(5), false);
	}
}
