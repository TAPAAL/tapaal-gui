import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.junit.Test;

import dk.aau.cs.model.tapn.Bound;
import dk.aau.cs.model.tapn.IntBound;
import dk.aau.cs.model.tapn.TimeInvariant;
import dk.aau.cs.util.RequireException;

public class TimeInvariantTests {
	
	@Test(expected=RequireException.class)
	public void TimeInvariantConstructor(){
		new TimeInvariant(true, Bound.Infinity);
	}
	
	@Test
	public void TimeInvariantConstructor_inf(){
		new TimeInvariant(false, Bound.Infinity);
	}
	
	@Test
	public void TimeInvariantConstructor_valid(){
		new TimeInvariant(true, new IntBound(4));
	}
	
	@Test(expected=RequireException.class)
	public void TimeInvariantConstructor_invalid_zero(){
		new TimeInvariant(false, new IntBound(0));
	}
	
	@Test
	public void isSatisfied_test1(){
		TimeInvariant inv = new TimeInvariant(true, new IntBound(5));
		assertTrue(inv.isSatisfied(new BigDecimal(4)));
		assertTrue(inv.isSatisfied(new BigDecimal(5)));
		assertFalse(inv.isSatisfied(new BigDecimal(6)));
	}
	
	@Test
	public void isSatisfied_test2(){
		TimeInvariant inv = new TimeInvariant(false, new IntBound(5));
		assertTrue(inv.isSatisfied(new BigDecimal(4.999)));
		assertFalse(inv.isSatisfied(new BigDecimal(5)));
		assertFalse(inv.isSatisfied(new BigDecimal(6)));
	}
	
	@Test
	public void isSatisfied_test3(){
		TimeInvariant inv = new TimeInvariant(false, Bound.Infinity);
		assertTrue(inv.isSatisfied(new BigDecimal(4)));
		assertTrue(inv.isSatisfied(new BigDecimal(Integer.MAX_VALUE)));
	}
}
