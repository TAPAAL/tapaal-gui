package pipe.gui;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.NetType;
import pipe.dataLayer.Transition;
import pipe.gui.graphicElements.Arc;
import pipe.gui.graphicElements.ArcPathPoint;
import pipe.gui.graphicElements.Place;
import pipe.gui.graphicElements.tapn.TimedInhibitorArcComponent;
import pipe.gui.graphicElements.tapn.TimedInputArcComponent;
import pipe.gui.graphicElements.tapn.TimedPlaceComponent;
import pipe.gui.graphicElements.tapn.TimedTransitionComponent;
import pipe.gui.graphicElements.tapn.TimedTransportArcComponent;

public class TikZExporter {

	public enum TikZOutputOption {
		FIGURE_ONLY, FULL_LATEX
	}

	private DataLayer net;
	private String fullpath;
	private TikZOutputOption option;
	private double scale = 1.0 / 55.0;

	public TikZExporter(DataLayer net, String fullpath, TikZOutputOption option) {
		this.net = net;
		this.fullpath = fullpath;
		this.option = option;
	}

	public void ExportToTikZ() {
		FileWriter outFile = null;
		PrintWriter out = null;
		try {
			outFile = new FileWriter(fullpath);
			out = new PrintWriter(outFile);

			if (option == TikZOutputOption.FULL_LATEX) {
				out.println("\\documentclass[a4paper]{article}");
				out.println("\\usepackage{tikz}");
				out.println("\\usetikzlibrary{petri,arrows}");
				out.println("\\usepackage{amstext}");
				out.println();
				out.println("\\begin{document}");
				out.println();
				out.println();
			}

			out.println("%% TikZ style options %%");
			out.print(exportTikZstyle());
			out.println();

			out.println("%% TikZ-figure elements %%");
			out.print(exportPlacesWithTokens(net.getPlaces()));
			out.print(exportTransitions(net.getTransitions()));
			out.print(exportArcs(net.getArcs()));

			out.println("\\end{tikzpicture}");
			if (option == TikZOutputOption.FULL_LATEX) {
				out.println("\\end{document}");

			}

		} catch (IOException e) {

		} finally {
			if (out != null)
				out.close();
			if (outFile != null)
				try {
					outFile.close();
				} catch (IOException e2) {

				}
		}
	}

	private StringBuffer exportArcs(Arc[] arcs) {
		StringBuffer out = new StringBuffer();
		for (Arc arc : arcs) {
			String arcPoints = "";
			for (int i = 1; i < arc.getArcPath().getEndIndex(); i++) {
				ArcPathPoint point = arc.getArcPath().getArcPathPoint(i);
				arcPoints += "-- (" + point.getX() * scale + "," + point.getY()
						* scale * (-1) + ") ";
			}

			String arrowType = "";
			if (arc instanceof TimedInhibitorArcComponent) {
				arrowType = "inhibArc";
			} else if (arc instanceof TimedTransportArcComponent) {
				arrowType = "transportArc";
			} else if (arc instanceof TimedInputArcComponent) {
				arrowType = "arc";
			} else {
				arrowType = "arc";
			}

			String arcLabel = getArcLabels(arc);

			out.append("\\draw[");
			out.append(arrowType);
			out.append("] (");
			out.append(arc.getSource().getId());
			out.append(") ");
			out.append(arcPoints);
			out.append("-- (");
			out.append(arc.getTarget().getId());
			out.append(") ");
			out.append(arcLabel);
			out.append(" {};\n");
		}
		return out;
	}

	protected String getArcLabels(Arc arc) {
		String arcLabel = "";
		if (arc instanceof TimedInputArcComponent
				&& !net.netType().equals(NetType.UNTIMED)) {
			if (!(arc.getSource() instanceof TimedTransitionComponent)) {
				arcLabel = "node[midway,auto] {";
				arcLabel += replaceWithMathLatex(((TimedInputArcComponent) arc)
						.getGuardAsString());

				if (arc instanceof TimedTransportArcComponent)
					arcLabel += ":"
							+ ((TimedTransportArcComponent) arc).getGroupNr();

				arcLabel += "}";
			} else {
				if (arc instanceof TimedTransportArcComponent)
					arcLabel = "node[midway,auto] {"
							+ ":" + ((TimedTransportArcComponent) arc).getGroupNr() + "}";
			}
		}
		return arcLabel;
	}

	private StringBuffer exportTransitions(Transition[] transitions) {
		StringBuffer out = new StringBuffer();
		for (Transition trans : transitions) {
			String angle = "";
			if (trans.getAngle() != 0)
				angle = ",rotate=" + String.valueOf(trans.getAngle());

			out.append("\\node[transition");
			out.append(angle);
			out.append(",label=above:");
			out.append(exportMathName(trans.getName()));
			out.append("] at (");
			out.append(trans.getPositionX() * scale);
			out.append(",");
			out.append(trans.getPositionY() * scale * (-1));
			out.append(") (");
			out.append(trans.getId());
			out.append(") {};\n");
			
			if(((TimedTransitionComponent)trans).underlyingTransition().isShared()){
				out.append("\\node[sharedtransition");
				out.append(angle);
				out.append("] at (");
				out.append(trans.getId());
				out.append(".center) { };\n");
			}
		}
		return out;
	}

	private StringBuffer exportPlacesWithTokens(Place[] places) {
		StringBuffer out = new StringBuffer();
		for (Place place : places) {
			String invariant = getPlaceInvariantString(place);
			String tokensInPlace = getTokenListStringFor(place);

			out.append("\\node[place,label=above:");
			out.append(exportMathName(place.getName()));
			out.append(",");
			out.append(invariant);
			out.append(tokensInPlace);
			out.append("] at (");
			out.append(place.getPositionX() * scale);
			out.append(",");
			out.append(place.getPositionY() * scale * (-1));
			out.append(") (");
			out.append(place.getId());
			out.append(") {};\n");
			
			if(((TimedPlaceComponent)place).underlyingPlace().isShared()){
				out.append("\\node[sharedplace] at (");
				out.append(place.getId());
				out.append(".center) { };\n");
			}
		}

		return out;
	}

	protected String getTokenListStringFor(Place place) {
		ArrayList<BigDecimal> tokens = ((TimedPlaceComponent) place).getTokens();
		String tokensInPlace = "";
		if (tokens.size() > 0) {
			if (tokens.size() == 1 && !net.netType().equals(NetType.UNTIMED)) {
				tokensInPlace = "structured tokens={" + tokens.get(0).setScale(1) + "},";
			} else {
				tokensInPlace = exportMultipleTokens(tokens);
			}
		}
		return tokensInPlace;
	}

	protected String getPlaceInvariantString(Place place) {
		if (net.netType().equals(NetType.UNTIMED)) return "";
		String invariant = "";

		if (!((TimedPlaceComponent) place).getInvariantAsString().contains("inf"))
			invariant = "label=below:inv: " + replaceWithMathLatex(((TimedPlaceComponent) place).getInvariantAsString()) + ",";
		return invariant;
	}

	private String exportMultipleTokens(ArrayList<BigDecimal> tokens) {
		StringBuffer out = new StringBuffer();

		out.append("structured tokens={\\#");
		out.append(String.valueOf(tokens.size()));
		out.append("},");
		if (!net.netType().equals(NetType.UNTIMED)) {
			out.append("pin=above:{\\{");
			for (int i = 0; i < tokens.size() - 1; i++) {
				out.append(tokens.get(i).setScale(1));
				out.append(",");
			}
			out.append(tokens.get(tokens.size() - 1).setScale(1));
			out.append("\\}},");
		}
		return out.toString();
	}

	private StringBuffer exportTikZstyle() {
		StringBuffer out = new StringBuffer();

		out.append("\\begin{tikzpicture}[font=\\scriptsize]\n");
		out.append("\\tikzstyle{arc}=[->,>=stealth,thick]\n");

		if (!net.netType().equals(NetType.UNTIMED)) out.append("\\tikzstyle{transportArc}=[->,>=diamond,thick]\n");
		out.append("\\tikzstyle{inhibArc}=[->,>=o,thick]\n");

		out.append("\\tikzstyle{every place}=[minimum size=6mm,thick]\n");
		out.append("\\tikzstyle{every transition} = [fill=black,minimum width=2mm,minimum height=5mm]\n");
		out.append("\\tikzstyle{every token}=[fill=white,text=black]\n");
		out.append("\\tikzstyle{sharedplace}=[place,minimum size=7.5mm,dashed,thin]\n");
		out.append("\\tikzstyle{sharedtransition}=[transition, fill opacity=0, minimum width=3.5mm, minimum height=6.5mm,dashed]\n");
		return out;
	}

	protected String replaceWithMathLatex(String text) {
		return "$" + text.replace("inf", "\\infty").replace("<=", "\\leq ").replace("*", "\\cdot ") + "$";
	}

	private String exportMathName(String name) {
		StringBuffer out = new StringBuffer("$");
		int subscripts = 0;
		for (int i = 0; i < name.length() - 1; i++) {
			char c = name.charAt(i);
			if (c == '_') {
				out.append("_{");
				subscripts++;
			} else {
				out.append(c);
			}
		}

		char last = name.charAt(name.length() - 1);
		if (last == '_') {
			out.append("\\_");
		} else
			out.append(last);

		for (int i = 0; i < subscripts; i++) {
			out.append("}");
		}
		out.append("$");
		return out.toString();
	}

}
