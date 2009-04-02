package pipe.exception;

import pipe.dataLayer.Place;

public class InvariantViolatedAnimationException extends Exception {

	public InvariantViolatedAnimationException(Place p, float tokensAge) {
		System.err.println("InvariantViolatedAnimationException in place " + p  + " age " + tokensAge + " is two high" );
	}

}
