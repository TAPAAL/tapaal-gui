package pipe.gui.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import dk.aau.cs.util.Pair;
import pipe.gui.TAPAALGUI;
import pipe.gui.swingcomponents.filebrowser.FileBrowser;

import java.awt.Color;
import java.awt.Component;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class GraphExporter {
    private static final double COLOR_NORMALIZER = 255.0;

    public static void exportPiecewiseToTikz(List<Graph> pieces) {
        exportPiecewiseToTikz(pieces, TAPAALGUI.getApp());
    }

    public static void exportPointPlotToTikz(Graph graph) {
        exportPointPlotToTikz(graph, TAPAALGUI.getApp());
    }

    public static void exportToTikz(AbstractGraph graph) {
        exportToTikz(graph, TAPAALGUI.getApp());
    }
    
    public static void exportPiecewiseToTikz(List<Graph> pieces, Component parent) {
        if (pieces == null || pieces.isEmpty()) {
            throw new IllegalArgumentException("Cannot export a piecewise graph with empty data.");
        }

        String defaultName = getDefaultName(pieces);
        Pair<String, GraphExporterOptions> fileNameAndOptions = displayExportGui(parent, defaultName);
        String path = fileNameAndOptions.getFirst();
        GraphExporterOptions options = fileNameAndOptions.getSecond();
        if (path == null || path.isEmpty() || options == null) {
            return;
        }
        
        options.setPiecewise(true);
        writeTikzGraph(pieces, path, options);
    }

    public static void exportPointPlotToTikz(Graph graph, Component parent) {
        if (graph == null || graph.isEmpty()) {
            throw new IllegalArgumentException("Cannot export graph(s) with empty data.");
        }

        String defaultName = getDefaultName(graph);
        Pair<String, GraphExporterOptions> fileNameAndOptions = displayExportGui(parent, defaultName);
        String path = fileNameAndOptions.getFirst();
        GraphExporterOptions options = fileNameAndOptions.getSecond();
        if (path == null || path.isEmpty() || options == null) {
            return;
        }

        options.setPointPlot(true);
        writeTikzGraph(Collections.singletonList(graph), path, options);
    }

    public static void exportToTikz(AbstractGraph graph, Component parent) {
        if (graph == null || graph.isEmpty()) {
            throw new IllegalArgumentException("Cannot export graph(s) with empty data.");
        }

        String defaultName = getDefaultName(graph); 
        Pair<String, GraphExporterOptions> fileNameAndOptions = displayExportGui(parent, defaultName);
        String path = fileNameAndOptions.getFirst();
        GraphExporterOptions options = fileNameAndOptions.getSecond();
        if (path == null || path.isEmpty() || options == null) {
            return;
        }

        if (graph instanceof MultiGraph) {
            options.setShowLegend(true);
            writeTikzGraph(((MultiGraph)graph).getGraphs(), path, options);
        } else {
            writeTikzGraph(Collections.singletonList((Graph)graph), path, options);
        }
    }

    private static Pair<String, GraphExporterOptions> displayExportGui(Component parent, String defaultName) {
        Object[] possibilities = {"Only the TikZ figure",
                                  "Full compilable LaTex including your figure"};

        String figureOptions = (String)JOptionPane.showInputDialog(
            parent,
            "Choose how you would like your TikZ figure outputted: \n",
            "Export to TikZ", JOptionPane.PLAIN_MESSAGE,
            null, possibilities, "Only the TikZ figure");

        GraphExporterOptions options = null;
        String path = null;
        if (figureOptions != null) {
            options = new GraphExporterOptions();
            if (figureOptions.equals(possibilities[0])) {
                options.setStandalone(false);
            } else {
                options.setStandalone(true);
            }

            path = FileBrowser.constructor("TikZ figure", "tex", defaultName).saveFile();
        }

        return new Pair<>(path, options);
    }

    private static void writeTikzGraph(List<Graph> graphs, String path, GraphExporterOptions options) {
        StringBuilder tikzCode = new StringBuilder();
        if (options.isStandalone()) {
            tikzCode.append("\\documentclass{standalone}\n")
                    .append("\\usepackage{pgfplots}\n")
                    .append("\\pgfplotsset{compat=1.18}\n")
                    .append("\\begin{document}\n");
        }

        tikzCode.append("\\begin{tikzpicture}\n")
                .append("\\begin{axis}[\n")
                .append("\twidth=\\textwidth,\n")
                .append("\tscaled y ticks=false,\n")
                .append("\ty tick label style={/pgf/number format/fixed},\n");

        Graph firstGraph = graphs.get(0);
        tikzCode.append("\txlabel={").append(escapeLatex(firstGraph.getXAxisLabel())).append("},\n")
                .append("\tylabel={").append(escapeLatex(firstGraph.getYAxisLabel())).append("},\n")
                .append("\tgrid=major,\n")
                .append("\tline width=1.2pt,\n");

        if (options.showLegend()) {
            tikzCode.append("\tlegend style={at={(0.98,0.02)},anchor=south east},\n")
                    .append("\tlegend cell align={left}\n");
        }
        
        tikzCode.append("]\n");
        
        ColorGenerator colorGenerator = new ColorGenerator();

        Color plotColor = null;
        if (options.isPiecewise()) {
            plotColor = colorGenerator.nextColor();
        } 

        for (Graph graph : graphs) {
            if (!options.isPiecewise()) {
                plotColor = colorGenerator.nextColor();
            }

            tikzCode.append("\\addplot[")
                    .append(options.isPointPlot() ? "only marks," : "")
                    .append("color={rgb,1:red,")
                    .append(plotColor.getRed() / COLOR_NORMALIZER)
                    .append("; green,")
                    .append(plotColor.getGreen() / COLOR_NORMALIZER)
                    .append("; blue,")
                    .append(plotColor.getBlue() / COLOR_NORMALIZER)
                    .append("}] coordinates {\n");

            List<GraphPoint> points = reducePoints(graph.getPoints(), options.getResolution());
            for (GraphPoint point : points) {
                tikzCode.append("\t(")
                    .append(point.getX())
                    .append(", ")
                    .append(point.getY())
                    .append(")\n");
            }

            tikzCode.append("};\n");
            if (options.showLegend()) {
                tikzCode.append("\\addlegendentry{")
                        .append(escapeLatex(graph.getName()))
                        .append("}\n");
            }
        }
        
        boolean showMean = (graphs.size() == 1 || options.isPiecewise()) && firstGraph.getMean() != null;
        if (showMean) {
            double mean = firstGraph.getMean();
            tikzCode.append("\\draw[black,dotted,thick] (axis cs:")
                    .append(mean)
                    .append(",\\pgfkeysvalueof{/pgfplots/ymin}) -- (axis cs:")
                    .append(mean)
                    .append(",\\pgfkeysvalueof{/pgfplots/ymax});\n");
        }

        tikzCode.append("\\end{axis}\n")
                .append("\\end{tikzpicture}\n");

        if (options.isStandalone()) {
            tikzCode.append("\\end{document}\n");
        }

        try {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
                writer.write(tikzCode.toString());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error writing graph to tikz", e);
        }
    }

    private static String escapeLatex(String text) {
        return text.replace("&", "\\&")
                   .replace("%", "\\%")
                   .replace("$", "\\$")
                   .replace("#", "\\#")
                   .replace("_", "\\_")
                   .replace("{", "\\{")
                   .replace("}", "\\}")
                   .replace("~", "\\textasciitilde")
                   .replace("^", "\\textasciicircum")
                   .replace("\\", "\\textbackslash");
    }

    private static String getDefaultName(AbstractGraph graph) {
        return graph.getName().toLowerCase().replaceAll(" ", "_") + ".tex";
    }

    private static String getDefaultName(List<Graph> pieces) {
        return pieces.get(0).getName().toLowerCase().replaceAll(" ", "_") + ".tex";
    }

    private static List<GraphPoint> reducePoints(List<GraphPoint> points, GraphExporterOptions.Resolution resolution) {
        if (resolution == GraphExporterOptions.Resolution.HIGH) {
            return points;
        }

        List<GraphPoint> reduced = new LinkedList<>();
        reduced.add(points.get(0));
        
        for (int i = 1; i < points.size() - 1; i += resolution.getStep()) {
            reduced.add(points.get(i));
        }
        
        if (!reduced.contains(points.get(points.size() - 1))) {
            reduced.add(points.get(points.size() - 1));
        }
        
        return reduced;
    }
}