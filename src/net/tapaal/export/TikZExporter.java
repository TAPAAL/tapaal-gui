package net.tapaal.export;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.Variable;
import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.TimedToken;

import net.tapaal.gui.petrinet.Context;
import pipe.gui.petrinet.dataLayer.DataLayer;
import pipe.gui.TAPAALGUI;
import pipe.gui.petrinet.graphicElements.*;
import pipe.gui.petrinet.graphicElements.tapn.TimedInhibitorArcComponent;
import pipe.gui.petrinet.graphicElements.tapn.TimedInputArcComponent;
import pipe.gui.petrinet.graphicElements.tapn.TimedPlaceComponent;
import pipe.gui.petrinet.graphicElements.tapn.TimedTransitionComponent;
import pipe.gui.petrinet.graphicElements.tapn.TimedTransportArcComponent;

public class TikZExporter {

	public enum TikZOutputOption {
		FIGURE_ONLY, FULL_LATEX
	}

	private final DataLayer net;
	private final String fullpath;
	private final TikZOutputOption option;

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
				out.println("\\usetikzlibrary{petri,arrows,positioning}");
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

            out.println(exportGlobalVariables());

			out.println("\\end{tikzpicture}");
			if (option == TikZOutputOption.FULL_LATEX) {
				out.println("\\end{document}");

			}

		} catch (IOException e) {

		} finally {
			if (out != null) {
                out.close();
            }
			if (outFile != null) {
                try {
                    outFile.close();
                } catch (IOException e2) {

                }
            }
		}
	}

	private StringBuffer exportArcs(Arc[] arcs) {
		StringBuffer out = new StringBuffer();

		for (Arc arc : arcs) {
			String arcPoints = "";

			for (int i = 1; i < arc.getArcPath().getEndIndex(); i++) {
                ArcPathPoint currentPoint = arc.getArcPath().getArcPathPoint(i);

                if(currentPoint.getPointType() == ArcPathPoint.STRAIGHT) {
                    arcPoints += "to [bend right=0] (" + (currentPoint.getX()) + "," + (currentPoint.getY() * (-1)) + ") ";
                } else if (currentPoint.getPointType() == ArcPathPoint.CURVED) {
                    double xCtrl1 = Math.round(currentPoint.getControl1().getX());
                    double yCtrl1 = Math.round(currentPoint.getControl1().getY() * (-1));
                    double xCtrl2 = Math.round(currentPoint.getControl2().getX());
                    double yCtrl2 = Math.round(currentPoint.getControl2().getY() * (-1));

                    arcPoints += " .. controls(" + xCtrl1 + "," + yCtrl1 + ")";
                    arcPoints += " and ";
                    arcPoints += "(" + xCtrl2 + "," + yCtrl2 + ") .. ";
                    arcPoints += "(" + currentPoint.getX() + "," + currentPoint.getY() * (-1) + ") ";
                }
			}

			String arrowType = getArcArrowType(arc);

            out.append("% Arc between " + arc.getSource().getName() + " and " + arc.getTarget().getName() + "\n");
			out.append("\\draw[");
			out.append(arrowType);
			out.append(",pos=0.0] (");
			out.append(arc.getSource().getId());
			out.append(") ");
			out.append(arcPoints);
			out.append("to node[bend right=0,auto,align=left]");
			out.append(" {");
			out.append(handleNameLabel(arc.getNameLabel().getText()));
            out.append("} ");
			out.append("(" + arc.getTarget().getId() + ");\n");
		}
		return out;
	}

    private String getArcArrowType(Arc arc) {
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
        return arrowType;
    }

	private StringBuffer exportTransitions(Transition[] transitions) {
		StringBuffer out = new StringBuffer();
		for (Transition trans : transitions) {
			String angle = "";
			if (trans.getAngle() != 0)
				angle = ",rotate=-" + (trans.getAngle());


			out.append("\\node[transition");
			out.append(angle);
			out.append(handlePlaceAndTransitionLabel(trans.getNameLabel().getText(), trans.getName(), trans.getAttributesVisible()));
			out.append("] at (");
			out.append((trans.getPositionX()));
			out.append(',');
			out.append((trans.getPositionY() * (-1)));
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
                        
			if(((TimedTransitionComponent)trans).underlyingTransition().isUrgent()){
				out.append("\\node[urgenttransition");
				out.append(angle);
				out.append("] at (");
				out.append(trans.getId());
				out.append(".center) { };\n");
			}
            if (((TimedTransitionComponent)trans).underlyingTransition().isUncontrollable()) {
                out.append("\\node[uncontrollabletransition");
                out.append(angle);
                out.append("] at (");
                out.append(trans.getId());
                out.append(".center) { };\n");
            }
		}
		return out;
	}

	private String handlePlaceAndTransitionLabel(String nameLabelText, String name, boolean isVisible) {
	    if(!isVisible)
	        return "";

	    String result = ", label={[align=left,label distance=0cm]90:";
	    result += "$\\mathrm{" + (name.replace("_","\\_")) + "}$";
	    result += handleNameLabel(nameLabelText);
	    result += "}";
	    return result;
    }

	private StringBuffer exportPlacesWithTokens(Place[] places) {
		StringBuffer out = new StringBuffer();

		for (Place place : places) {
			String invariant = "$" + getPlaceInvariantString(place) + "$";
			String tokensInPlace = getTokenListStringFor(place);

			out.append("\\node[place");
			out.append(handlePlaceAndTransitionLabel(place.getNameLabel().getText(), place.getName(), place.getAttributesVisible()));
			out.append("] at (");
			out.append(place.getPositionX());
			out.append(',');
			out.append(place.getPositionY() * (-1));
			out.append(") (");
			out.append(place.getId());
			out.append(") {};\n");

            exportPlaceTokens(place, out, ((TimedPlaceComponent) place).underlyingPlace().tokens().size());
			
			if(((TimedPlaceComponent)place).underlyingPlace().isShared()){
				out.append("\\node[sharedplace ");
                out.append("] at (");
				out.append(place.getId());
				out.append(".center) { };\n");
			}
		}
		return out;
	}

	private String handleNameLabel(String nameLabel) {
	    String nameLabelsString = "";
        String[] labelsInName = nameLabel.split("\n");
        for(int i = 0; i < labelsInName.length; i++) {

            if(labelsInName[i].contains("[")) {
                nameLabelsString += escapeSpacesInAndOrNot(replaceWithMathLatex(labelsInName[i]));
                nameLabelsString += "\\\\";
            }
            else if(!labelsInName[i].isEmpty()){
                nameLabelsString += escapeSpacesInAndOrNot(replaceWithMathLatex(labelsInName[i]));
                nameLabelsString += "\\\\";
            } else {
                nameLabelsString += "\\\\";
            }
        }
        return nameLabelsString;
    }

    private String escapeSpacesInAndOrNot(String str) {
        return str.replace(" and ", "\\ and\\ ").replace(" or", "\\ or\\ ").replace(" not", "\\ not\\ ");
    }

	private void exportPlaceTokens(Place place, StringBuffer out, int numOfTokens) {
        // Dot radius
        final double tRadius = 1;

        // Token dot position offsets
        final double tLeftX = 7;
        final double tRightX = 7;
        final double tTopY = 7;
        final double tBotY = 7;

        boolean isTimed = TAPAALGUI.getApp().getCurrentTab().getLens().isTimed();

        double placeXpos = (place.getPositionX());
        double placeYpos = (place.getPositionY() * (-1));
        double xPos, yPos;

        if(isTimed && numOfTokens > 0) {
            switch (numOfTokens) {
                case 2:
                    out.append("\\node at ("); // Top
                    out.append(placeXpos);
                    out.append(",");
                    out.append(placeYpos + 4);
                    out.append(")");
                    out.append("{0,0};\n");

                    out.append("\\node at ("); // Bottom
                    out.append(placeXpos);
                    out.append(",");
                    out.append(placeYpos - 5);
                    out.append(")");
                    out.append("{0,0};\n");
                    return;
                case 1:
                    out.append("\\node at ("); // Top
                    out.append(placeXpos);
                    out.append(",");
                    out.append(placeYpos);
                    out.append(")");
                    out.append("{0,0};\n");
                    return;
                default:
                    out.append("\\node at (");
                    out.append(placeXpos);
                    out.append(",");
                    out.append(placeYpos);
                    out.append(")");
                    out.append("{$\\mathrm{");
                    out.append("\\#" + numOfTokens + "}$};\n");
                    return;
            }
        } else if(numOfTokens > 5 && !isTimed ) {
            out.append("\\node at (");
            out.append(placeXpos);
            out.append(",");
            out.append(placeYpos);
            out.append(")");
            out.append("{$\\mathrm{");
            out.append("\\#" + numOfTokens + "}$};\n");
            return;
        }

        switch (numOfTokens) {
            case 5: // middle
                out.append("\\node at (");
                out.append(placeXpos);
                out.append(",");
                out.append(placeYpos);
                out.append(")");
                out.append("[circle,fill,inner sep=");
                out.append(tRadius);
                out.append("pt]{};\n");
                /* falls through */
            case 4: // top left
                out.append("\\node at (");
                out.append(placeXpos - tLeftX);
                out.append(",");
                out.append(placeYpos + tTopY);
                out.append(")");
                out.append("[circle,fill,inner sep=");
                out.append(tRadius);
                out.append("pt]{};\n");
                /* falls through */
            case 3:
                if(numOfTokens == 5 || numOfTokens == 4) { // top right
                    xPos = placeXpos + tRightX;
                    yPos = placeYpos + tTopY;
                } else { // top left
                    xPos = placeXpos - tLeftX;
                    yPos = placeYpos + tTopY;
                }
                out.append("\\node at (");
                out.append(xPos);
                out.append(",");
                out.append(yPos);
                out.append(")");
                out.append("[circle,fill,inner sep=");
                out.append(tRadius);
                out.append("pt]{};\n");
                /* falls through */
            case 2:
                if(numOfTokens == 5 || numOfTokens == 4) { // bottom left
                    xPos = placeXpos - tLeftX;
                    yPos = placeYpos - tBotY;
                } else if (numOfTokens == 3){ // middle
                    xPos = placeXpos;
                    yPos = placeYpos;
                } else { // left middle
                    xPos = placeXpos - tLeftX;
                    yPos = placeYpos;
                }
                out.append("\\node at (");
                out.append(xPos);
                out.append(",");
                out.append(yPos);
                out.append(")");
                out.append("[circle,fill,inner sep=");
                out.append(tRadius);
                out.append("pt]{};\n");
                /* falls through */
            case 1:
                if(numOfTokens == 5 || numOfTokens == 4 || numOfTokens == 3) { // bottom right
                    xPos = placeXpos + tRightX;
                    yPos = placeYpos - tBotY;
                } else if (numOfTokens == 2){ // right middle
                    xPos = placeXpos + tRightX;
                    yPos = placeYpos;
                } else { // middle
                    xPos = placeXpos;
                    yPos = placeYpos;
                }
                out.append("\\node at (");
                out.append(xPos);
                out.append(",");
                out.append(yPos);
                out.append(")");
                out.append("[circle,fill,inner sep=");
                out.append(tRadius);
                out.append("pt]{};\n");
            default:
                break;
        }
    }

    private StringBuffer exportGlobalVariables() {
        StringBuffer out = new StringBuffer();

	    Context context = new Context(TAPAALGUI.getCurrentTab());
        List<ColorType> listColorTypes = context.network().colorTypes();
        List<Constant> constantsList = new ArrayList<>(context.network().constants());
        List<Variable> variableList = context.network().variables();

        if(!context.network().isColored()) {
            if(constantsList.isEmpty()) {
                return out;
            }
            out.append("%%Global box which contains global color types, variables and/or constants.\n");
            out.append("\\node [globalBox] (globalBox) at (current bounding box.north west) [anchor=south west] {");
            exportConstants(constantsList, out);
            out.append("};");
            return out;
        }

        if((listColorTypes.isEmpty() && constantsList.isEmpty() && variableList.isEmpty()))
            return out;

        out.append("%%Global box which contains global color types, variables and/or constants.\n");
        out.append("\\node [globalBox] (globalBox) at (current bounding box.north west) [anchor=south west] {");

        exportColorTypes(listColorTypes, out);

        if(listColorTypes.size() > 0 && (variableList.size() > 0 || constantsList.size() > 0)) {
            out.append("\\\\");
        }

        exportVariables(variableList, out);

        if(variableList.size() > 0 && !constantsList.isEmpty()) {
            out.append("\\\\");
        }

        exportConstants(constantsList, out);

        out.append("};");

        return out;
    }

    private void exportColorTypes(List<ColorType> listColorTypes, StringBuffer out) {
	    String stringColorList = "";
        for(int i = 0; i < listColorTypes.size(); i++) {
            if(i == 0) {
                out.append("Color Types:\\\\");
            }
            out.append("$\\mathit{" + listColorTypes.get(i).getName() + "}$ \\textbf{is} ");

            if(listColorTypes.get(i).isProductColorType()) {
                out.append("$\\mathit{<");
                for(int x = 0; x < listColorTypes.get(i).getProductColorTypes().size(); x++) {
                    stringColorList += listColorTypes.get(i).getProductColorTypes().get(x).getName().replace("_", "\\_");

                    if(x != listColorTypes.get(i).getProductColorTypes().size() - 1){
                        stringColorList += ", ";
                    }
                }
                out.append(stringColorList + ">}$\\\\");
                stringColorList = "";

            } else if(listColorTypes.get(i).isIntegerRange()) {
                out.append("$\\mathit{");
                if(listColorTypes.get(i).size() > 1) {
                    int listSize = listColorTypes.get(i).size();
                    out.append("[" + listColorTypes.get(i).getColors().get(0).getColorName().replace("_","\\_") + ".." + listColorTypes.get(i).getColors().get(listSize - 1).getColorName().replace("_","\\_") + "]");
                } else {
                    out.append("[" + listColorTypes.get(i).getFirstColor().getColorName().replace("_","\\_") + "]");
                }
                out.append("}$\\\\");

            } else {
                out.append("$\\mathit{[");
                for(int x = 0; x < listColorTypes.get(i).getColors().size(); x++) {
                    stringColorList += listColorTypes.get(i).getColors().get(x).getName().replace("_","\\_");

                    if(x != listColorTypes.get(i).getColors().size() - 1){
                        stringColorList += ", ";
                    }
                }
                out.append(stringColorList + "]}$\\\\");
                stringColorList = "";
            }
        }
    }

    private void exportVariables(List<Variable> variableList, StringBuffer out) {
	    String result = "";
        for(int i = 0; i < variableList.size(); i++) {
            if (i == 0) {
                result += "Variables:\\\\ ";
            }
            result += "$\\mathit{" + variableList.get(i).getName().replace("_","\\_") + " \\textbf{ in } " + variableList.get(i).getColorType().getName().replace("_","\\_") + "}$";
            if(i != variableList.size() - 1) {
                result += "\\\\";
            }
        }
        out.append(result);
    }

    private void exportConstants(List<Constant> constantsList, StringBuffer out) {
        String result = "";

        for(int i = 0; i < constantsList.size(); i++) {
            if(i == 0) {
                result += "Constants:\\\\";
            }
            result += "$\\mathit{" + constantsList.get(i).toString().replace("_","\\_") + "}$";
            if(i != constantsList.size() - 1) {
                result += "\\\\";
            }
        }
        out.append(result);
    }

	protected String getTokenListStringFor(Place place) {
		List<TimedToken> tokens = ((TimedPlaceComponent) place).underlyingPlace().tokens();

		String tokensInPlace = "";
		if (tokens.size() > 0) {
            if (tokens.size() == 1) {
				tokensInPlace = ", structured tokens={" + tokens.get(0).age().setScale(1) + "},";
			} else {
				tokensInPlace = exportMultipleTokens(tokens);
			}
		}
		return tokensInPlace;
	}

	protected String getPlaceInvariantString(Place place) {
        String invariant = "";

		if (!((TimedPlaceComponent) place).getInvariantAsString().contains("inf"))
			invariant = replaceWithMathLatex(((TimedPlaceComponent) place).getInvariantAsString()) + "};\n";
		return invariant;
	}

	private String exportMultipleTokens(List<TimedToken> tokens) {
		StringBuilder out = new StringBuilder();

		out.append(", structured tokens={\\#");
		out.append(tokens.size());

		out.append("},");
        out.append("pin=above:{\\{");
        for (int i = 0; i < tokens.size() - 1; i++) {
            out.append(tokens.get(i).age().setScale(1));
            out.append(',');
        }
        out.append(tokens.get(tokens.size() - 1).age().setScale(1));
        out.append("\\}},");
        return out.toString();
	}

	private StringBuffer exportTikZstyle() {
		StringBuffer out = new StringBuffer();

		out.append("\\begin{tikzpicture}[font=\\scriptsize, xscale=0.45, yscale=0.45, x=1.33pt, y=1.33pt]\n");
                out.append("%% the figure can be scaled by changing xscale and yscale or the size of the x- and y-coordinates\n");
                out.append("%% positions of place/transition labels that are currently fixed to label=135 degrees\n");
                out.append("%% these can be adjusted by adjusting either the coordinates, the label distance or the label degree\n");
                out.append("%% that is placed right after the 'label-distance'. 90: is above (default), 180 is left, 270 is below etc.\n");
                out.append("%% The curving of arcs can be done by adjusting bend left/right=XX\n");
                out.append("%% labels may be slightly skewed compared to the Tapaal drawing due to rounding.\n");
                out.append("%% This can be adjusted by tuning the coordinates of the label, or the degree (see above)\n");
                out.append("%% The box containing global variables can also be moved by adjusting the anchor points / bounding box in the [globalBox] node at the end of the Tikz document\n");
		out.append("\\tikzstyle{arc}=[->,>=stealth,thick]\n");

        out.append("\\tikzstyle{transportArc}=[->,>=diamond,thick]\n");
		out.append("\\tikzstyle{inhibArc}=[->,>=o,thick]\n");

		out.append("\\tikzstyle{every place}=[minimum size=6mm,thick]\n");
		out.append("\\tikzstyle{every transition} = [fill=black,minimum width=2mm,minimum height=5mm]\n");
		out.append("\\tikzstyle{every token}=[fill=white,text=black]\n");
		out.append("\\tikzstyle{sharedplace}=[place,minimum size=7.5mm,dashed,thin]\n");
		out.append("\\tikzstyle{sharedtransition}=[transition, fill opacity=0, minimum width=3.5mm, minimum height=6.5mm,dashed]\n");
		out.append("\\tikzstyle{urgenttransition}=[place,fill=white,minimum size=2.0mm,thin]\n");
        out.append("\\tikzstyle{uncontrollabletransition}=[transition,fill=white,draw=black,very thick]\n");
        out.append("\\tikzstyle{globalBox} = [draw,thick,align=left]");
        return out;
	}

	protected String replaceWithMathLatex(String text) {
	    String[] stringList = text.split(" ");
	    String result = "";

	    for(int i = 0; i < stringList.length; i++) {
	        result += "$\\mathit{" + replaceSpecialSymbols(stringList[i]) + "}$ ";
        }
	    return result;
	}

	protected String replaceSpecialSymbols(String text) {
	    return text.replace("inf", "\\infty").replace("<=", "\\leq").replace("*", "\\cdot")
                   .replace("\u2192", "\\rightarrow").replace("\u221E", "\\infty");
    }
}
