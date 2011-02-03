package pipe.gui;

import java.util.List;

import pipe.dataLayer.Arc;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.Place;
import pipe.dataLayer.colors.ColorSet;
import pipe.dataLayer.colors.ColoredInhibitorArc;
import pipe.dataLayer.colors.ColoredInputArc;
import pipe.dataLayer.colors.ColoredInterval;
import pipe.dataLayer.colors.ColoredOutputArc;
import pipe.dataLayer.colors.ColoredTimedPlace;
import pipe.dataLayer.colors.ColoredToken;
import pipe.dataLayer.colors.ColoredTransportArc;
import pipe.dataLayer.colors.Preserve;

public class TikZExporterForColoredTAPN extends TikZExporter {

	private final int MULTILINE_SHIFT_VALUE = 3;

	public TikZExporterForColoredTAPN(DataLayer net, String fullpath,
			TikZOutputOption option) {
		super(net, fullpath, option);
	}

	@Override
	protected String getPlaceInvariantString(Place place) {
		StringBuffer invariant = new StringBuffer("");
		ColoredTimedPlace ctp = (ColoredTimedPlace) place;

		String timeInvariant = ctp.getTimeInvariant().toString();
		String colorInvariant = "\\{"
				+ ctp.getColorInvariantStringWithoutSetNotation() + "\\}";
		boolean shiftColorInvariant = false;
		if (!timeInvariant.contains("inf")) {
			invariant.append("label=below:");
			invariant.append(replaceWithMathLatex("\\mathit{age}"
					+ timeInvariant));
			invariant.append(",");
			shiftColorInvariant = true;
		}

		if (!colorInvariant.equals("\\{\\}")) {
			invariant.append("label={");
			if (shiftColorInvariant) {
				invariant.append("[yshift=-");
				invariant.append(MULTILINE_SHIFT_VALUE);
				invariant.append("mm]");
			}
			invariant.append("below: ");
			invariant.append(replaceWithMathLatex("\\mathit{val} \\in "
					+ colorInvariant));
			invariant.append("},");
		}

		return invariant.toString();
	}

	@Override
	protected String getTokenListStringFor(Place place) {
		List<ColoredToken> tokens = ((ColoredTimedPlace) place)
				.getColoredTokens();
		String tokensInPlace = "";
		if (tokens.size() > 0) {
			StringBuffer out = new StringBuffer();

			out.append("structured tokens={\\#");
			out.append(String.valueOf(tokens.size()));
			out.append("},");
			out.append("pin=above:{\\{");
			out.append(tokens.get(0).toString());
			for (int i = 1; i < tokens.size(); i++) {
				out.append(",");
				out.append(tokens.get(i).toString());
			}

			out.append("\\}},");
			tokensInPlace = out.toString();

		}
		return tokensInPlace;
	}

	@Override
	protected String getArcLabels(Arc arc) {
		StringBuffer result = new StringBuffer("");

		ColorSet colorGuard = new ColorSet();
		ColoredInterval timeGuard = new ColoredInterval();

		if (arc instanceof ColoredInputArc) {
			ColoredInputArc cia = (ColoredInputArc) arc;
			timeGuard = cia.getTimeGuard();
			colorGuard = cia.getColorGuard();
		} else if (arc instanceof ColoredTransportArc) {
			ColoredTransportArc cta = (ColoredTransportArc) arc;
			timeGuard = cta.getTimeGuard();
			colorGuard = cta.getColorGuard();
		} else if (arc instanceof ColoredInhibitorArc) {
			ColoredInhibitorArc cia = (ColoredInhibitorArc) arc;
			timeGuard = cia.getTimeGuard();
			colorGuard = cia.getColorGuard();
		}

		String line1 = null;
		String line2 = null;
		boolean usesLine2 = false;

		if (arc instanceof ColoredOutputArc) {
			line1 = ((ColoredOutputArc) arc).getOutputString();
		} else if (arc instanceof ColoredTransportArc
				&& !((ColoredTransportArc) arc).isInPreSet()) {
			ColoredTransportArc cta = (ColoredTransportArc) arc;
			usesLine2 = true;
			Preserve preserves = cta.getPreservation();
			if (preserves == null) {
				preserves = Preserve.AgeAndValue;
			}
			if (preserves.equals(Preserve.Age)) {
				line1 = "\\text{preserve } \\mathit{age} : " + cta.getGroup();
				line2 = cta.getOutputString();
			} else if (preserves.equals(Preserve.Value)) {
				line1 = "\\mathit{age} := 0 : " + cta.getGroup();
				line2 = "\\text{preserve }\\mathit{val}";
			} else {
				line1 = "\\text{preserve } \\mathit{age} : " + cta.getGroup();
				line2 = "\\text{preserve } \\mathit{val}";
			}
		} else {
			line1 = "\\mathit{age} \\in" + timeGuard.toString();
			;
			line2 = "\\mathit{val} \\in \\{"
					+ colorGuard.toStringNoSetNotation() + "\\}";
			usesLine2 = !colorGuard.isEmpty();
			;
		}

		result.append("node[midway,auto");
		if (usesLine2) {
			result.append(",yshift=");
			result.append(MULTILINE_SHIFT_VALUE);
			result.append("mm");
		}
		result.append("] {");
		result.append(replaceWithMathLatex(line1));
		result.append("}");

		if (usesLine2) {
			result.append("node[midway,auto");
			result.append("] {");
			result.append(replaceWithMathLatex(line2));
			result.append("}");
		}

		return result.toString();
	}
}
