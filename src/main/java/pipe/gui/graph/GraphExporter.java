package pipe.gui.graph;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import dk.aau.cs.util.Pair;
import pipe.gui.TAPAALGUI;
import pipe.gui.graph.GraphExporterOptions.LegendPosition;
import pipe.gui.swingcomponents.filebrowser.FileBrowser;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
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
        exportToTikz(graph, parent, null);
    }

    public static void exportToTikz(AbstractGraph graph, Component parent, Map<String, Color> colorMappings) {
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

        options.setColorMappings(colorMappings);

        if (graph instanceof MultiGraph) {
            options.setShowLegend(true);
            options.setMultiGraph(true);
            writeTikzGraph(((MultiGraph)graph).getGraphs(), path, options);
        } else {
            writeTikzGraph(Collections.singletonList((Graph)graph), path, options);
        }
    }

    private static Pair<String, GraphExporterOptions> displayExportGui(Component parent, String defaultName) {
        Object[] possibilities = {"Only the TikZ figure",
                            "Full compilable LaTex including your figure"};
        JPanel panel = new JPanel(new GridLayout(5, 1, 5, 5));
        
        panel.add(new JLabel("Export type:"));
        JComboBox<Object> outputBox = new JComboBox<>(possibilities);
        panel.add(outputBox);
        
        panel.add(new JLabel("Legend position:"));
        JComboBox<LegendPosition> legendBox = new JComboBox<>(LegendPosition.values());
        panel.add(legendBox);

        JPanel sizePanel = new JPanel(new GridLayout(1, 4, 5, 5));
        sizePanel.add(new JLabel("Width:"));
        JTextField widthField = new JTextField("1.0");
        sizePanel.add(widthField);
        sizePanel.add(new JLabel("Height:"));
        JTextField heightField = new JTextField("1.0");
        sizePanel.add(heightField);
        panel.add(sizePanel);

        DocumentFilter numberFilter = new javax.swing.text.DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string == null) return;
                if (string.matches("\\d*\\.?\\d*")) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text == null) return;
                String newText = fb.getDocument().getText(0, fb.getDocument().getLength()) + text;
                if (newText.matches("\\d*\\.?\\d*")) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        };

        ((AbstractDocument)widthField.getDocument()).setDocumentFilter(numberFilter);
        ((AbstractDocument)heightField.getDocument()).setDocumentFilter(numberFilter);

        int result = JOptionPane.showConfirmDialog(
            parent, panel, "Export to TikZ",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        GraphExporterOptions options = null;
        String path = null;
        if (result == JOptionPane.OK_OPTION) {
            options = new GraphExporterOptions();
            options.setStandalone(outputBox.getSelectedIndex() == 1);
            options.setLegendPosition((LegendPosition)legendBox.getSelectedItem());
            
            try {
                double width = Double.parseDouble(widthField.getText());
                double height = Double.parseDouble(heightField.getText());
                options.setWidthMultiplier(width);
                options.setHeightMultiplier(height);
            } catch (NumberFormatException e) {
                options.setWidthMultiplier(1.0);
                options.setHeightMultiplier(1.0);
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
                .append("\twidth=").append(options.getWidthMultiplier()).append("\\textwidth,\n")
                .append("\theight=").append(options.getHeightMultiplier()).append("\\textwidth,\n")
                .append("\tscaled y ticks=false,\n")
                .append("\ty tick label style={/pgf/number format/fixed},\n");

        Graph firstGraph = graphs.get(0);
        tikzCode.append("\txlabel={").append(escapeLatex(firstGraph.getXAxisLabel())).append("},\n")
                .append("\tylabel={").append(escapeLatex(firstGraph.getYAxisLabel())).append("},\n")
                .append("\tgrid=major,\n")
                .append("\tline width=1.2pt,\n");

        if (options.showLegend()) {
            LegendPosition pos = options.getLegendPosition();
            tikzCode.append("\tlegend style={at={")
                    .append(pos.getCoordinates())
                    .append("},anchor=")
                    .append(pos.toString().toLowerCase())
                    .append("},\n")
                    .append("\tlegend cell align=left\n");
        }
        
        tikzCode.append("]\n");
        
        Map<String, Color> colorMappings = options.getColorMappings();
        boolean genColors = colorMappings == null;
        if (genColors) {
            colorMappings = new HashMap<>();
        }

        ColorGenerator colorGenerator = new ColorGenerator();
        Color plotColor = null;
        for (Graph graph : graphs) {
            String style = "solid,";
            if (options.isMultiGraph()) {
                String[] nameParts = graph.getName().split(" - ");
                String observation = nameParts[0];
                String property = nameParts[1];

                plotColor = options.getColorMappings().get(observation);
                if (plotColor == null) {
                    plotColor = colorGenerator.nextColor();
                    colorMappings.put(observation, plotColor);
                }

                if (property.startsWith("Max")) {
                    style = "dash pattern=on 2pt off 2pt,";
                } else if (property.startsWith("Min")) {
                    style = "dash pattern=on 4pt off 4pt,";
                }
            } else if (options.isPiecewise()) {
                plotColor = plotColor != null ? plotColor : colorGenerator.nextColor();
            } else {
                plotColor = colorGenerator.nextColor();
            }

            tikzCode.append("\\addplot[")
                    .append(options.isPointPlot() ? "only marks," : "")
                    .append(style)
                    .append("forget plot,")
                    .append("color={rgb,1:red,")
                    .append(plotColor.getRed() / COLOR_NORMALIZER)
                    .append("; green,")
                    .append(plotColor.getGreen() / COLOR_NORMALIZER)
                    .append("; blue,")
                    .append(plotColor.getBlue() / COLOR_NORMALIZER)
                    .append("}] coordinates {\n");

            List<GraphPoint> points = reducePoints(graph.getPoints(), options.getResolution());
            tikzCode.append("\t");
            for (GraphPoint point : points) {
                tikzCode.append("(")
                        .append(point.getX())
                        .append(", ")
                        .append(point.getY())
                        .append(") ");
            }

            tikzCode.append("};\n");
            if (options.showLegend()) {
                if (options.isMultiGraph()) {
                    String[] nameParts = graph.getName().split(" - ");
                    String property = nameParts[1];
                    if (!property.startsWith("Avg")) continue;

                    tikzCode.append("\\addlegendimage{solid,color={rgb,1:red,")
                            .append(plotColor.getRed() / COLOR_NORMALIZER)
                            .append("; green,")
                            .append(plotColor.getGreen() / COLOR_NORMALIZER)
                            .append("; blue,")
                            .append(plotColor.getBlue() / COLOR_NORMALIZER)
                            .append("}");
                }

                tikzCode.append("}\n\\addlegendentry{")
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