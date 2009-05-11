package pipe.exception;

import java.math.BigDecimal;

import pipe.dataLayer.Place;

public class InvariantViolatedAnimationException extends Exception {

	public InvariantViolatedAnimationException(Place p, BigDecimal tokensAge) {
		System.err.println("InvariantViolatedAnimationException in place " + p  + " age " + tokensAge + " is two high" );
	}

}
