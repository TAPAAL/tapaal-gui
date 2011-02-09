package dk.aau.cs.petrinet.colors;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.petrinet.TAPNPlace;

public class ColoredPlace extends TAPNPlace {
	private List<ColoredToken> coloredTokens;
	private ColoredTimeInvariant timeInvariant;
	private ColorSet colorInvariant;

	public ColoredPlace(String name, ColoredTimeInvariant timeInvariant,
			ColorSet colorInvariant) {
		super(name, "", 0);
		coloredTokens = new ArrayList<ColoredToken>();
		this.timeInvariant = timeInvariant;
		this.colorInvariant = colorInvariant;
	}

	public List<ColoredToken> getColoredTokens() {
		return coloredTokens;
	}

	public void addColoredToken(ColoredToken token) {
		coloredTokens.add(token);
	}

	public ColorSet getColorInvariant() {
		return colorInvariant;
	}

	public ColoredTimeInvariant getTimeInvariant() {
		return timeInvariant;
	}
}
