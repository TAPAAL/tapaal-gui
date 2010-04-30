package dk.aau.cs.TAPN.colorTranslations;

import dk.aau.cs.TAPN.Degree2BroadcastTransformer;
import dk.aau.cs.petrinet.Degree2Converter;
import dk.aau.cs.petrinet.degree2converters.ColoredInhibDegree2Converter;

public class ColoredDegree2BroadcastTransformer extends
		Degree2BroadcastTransformer {

	public ColoredDegree2BroadcastTransformer(int extraTokens,
			boolean useSymmetry) {
		super(extraTokens, useSymmetry);
	}

	
	protected Degree2Converter getDegree2Converter() {
		return new ColoredInhibDegree2Converter();
	}
	
}
