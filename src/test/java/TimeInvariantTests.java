import java.math.BigDecimal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import dk.aau.cs.model.tapn.Bound;
import dk.aau.cs.model.tapn.IntBound;
import dk.aau.cs.model.tapn.TimeInvariant;
import dk.aau.cs.util.RequireException;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class TimeInvariantTests {
	
	@Test
	public void TimeInvariantConstructor(){
		assertThrows(RequireException.class, () -> new TimeInvariant(true, Bound.Infinity));
	}
	
	@Test
	public void TimeInvariantConstructor_inf(){
		new TimeInvariant(false, Bound.Infinity);
	}
	
	@Test
	public void TimeInvariantConstructor_valid(){
		new TimeInvariant(true, new IntBound(4));
	}
	
	@Test
	public void TimeInvariantConstructor_invalid_zero(){
		assertThrows(RequireException.class, () -> new TimeInvariant(false, new IntBound(0)));
	}
	
	@Test
	public void isSatisfied_test1(){
		TimeInvariant inv = new TimeInvariant(true, new IntBound(5));
		Assertions.assertTrue(inv.isSatisfied(new BigDecimal(4)));
		Assertions.assertTrue(inv.isSatisfied(new BigDecimal(5)));
		Assertions.assertFalse(inv.isSatisfied(new BigDecimal(6)));
	}
	
	@Test
	public void isSatisfied_test2(){
		TimeInvariant inv = new TimeInvariant(false, new IntBound(5));
		Assertions.assertTrue(inv.isSatisfied(new BigDecimal(4.999)));
		Assertions.assertFalse(inv.isSatisfied(new BigDecimal(5)));
		Assertions.assertFalse(inv.isSatisfied(new BigDecimal(6)));
	}
	
	@Test
	public void isSatisfied_test3(){
		TimeInvariant inv = new TimeInvariant(false, Bound.Infinity);
		Assertions.assertTrue(inv.isSatisfied(new BigDecimal(4)));
		Assertions.assertTrue(inv.isSatisfied(new BigDecimal(Integer.MAX_VALUE)));
	}
}
