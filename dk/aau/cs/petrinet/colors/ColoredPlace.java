package dk.aau.cs.petrinet.colors;

import java.util.List;

import dk.aau.cs.petrinet.TAPNPlace;

public class ColoredPlace extends TAPNPlace {
	private List<ColoredToken> coloredTokens;
	
	public ColoredPlace(String name, ColoredInterval timeInvariant, ColorSet colorInvariant){
		super(name,"",0);
	}

	public List<ColoredToken> getColoredTokens() {
		return coloredTokens;
	}
	
	public void addColoredToken(ColoredToken token){
		coloredTokens.add(token);
	}
}
