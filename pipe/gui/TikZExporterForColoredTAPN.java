package pipe.gui;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.Place;
import pipe.dataLayer.TimedPlace;
import pipe.dataLayer.colors.ColoredTimedPlace;
import pipe.dataLayer.colors.ColoredToken;
import pipe.gui.TikZExporter.TikZOutputOption;

public class TikZExporterForColoredTAPN extends TikZExporter {

	public TikZExporterForColoredTAPN(DataLayer net, String fullpath,
			TikZOutputOption option) {
		super(net, fullpath, option);
	}

	protected String getPlaceInvariantString(Place place) {
		StringBuffer invariant = new StringBuffer("");
		ColoredTimedPlace ctp = (ColoredTimedPlace)place;

		String timeInvariant = ctp.getTimeInvariant().toString();
		String colorInvariant = ctp.getColorInvariantString();
		boolean shiftColorInvariant = false;
		if(!timeInvariant.contains("inf")){
			invariant.append("label=below: age ");
			invariant.append(replaceWithMathLatex(timeInvariant));
			invariant.append(",");
			shiftColorInvariant = true;
		}	
		
		if(!colorInvariant.isEmpty()){
			invariant.append("label={");
			if(shiftColorInvariant){
				invariant.append("[yshift=-4mm]");
			}
			invariant.append("below: val ");
			invariant.append(replaceWithMathLatex("\\in" + colorInvariant));
			invariant.append("},");
		}

		return invariant.toString();
	}

	protected String getTokenListStringFor(Place place) {
		List<ColoredToken> tokens =((ColoredTimedPlace)place).getColoredTokens();
		String tokensInPlace = "";
		if(tokens.size() > 0)
		{
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
}
