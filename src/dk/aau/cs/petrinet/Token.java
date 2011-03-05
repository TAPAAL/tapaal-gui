package dk.aau.cs.petrinet;

import java.math.BigDecimal;

/**
 * @Deprecated use dk.aau.cs.model.tapn.TimedToken instead
 */
@Deprecated
public class Token {
	private TAPNPlace place;
	private BigDecimal age;

	public Token(TAPNPlace place) {
		this(place, BigDecimal.ZERO);
	}

	public Token(TAPNPlace place, BigDecimal age) {
		this.place = place;
		this.age = age;

	}

	public TAPNPlace place() {
		return place;
	}

	public BigDecimal age() {
		return age;
	}

	@Override
	public String toString() {
		return "(" + place.getName() + ", age:" + age + ")";
	}
}
