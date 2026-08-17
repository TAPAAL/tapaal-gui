package pipe.gui.graph;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;

import java.awt.event.MouseAdapter;
import java.awt.Cursor;
import java.awt.event.MouseEvent;

public class DraggableChartPanel extends ChartPanel {
    private int lastX;
    private int lastY;
    private final Range originalDomainAxisRange;
    private final Range originalRangeAxisRange;

    public DraggableChartPanel(JFreeChart chart) {
        super(chart);
        var plot = chart.getPlot();
        if (plot instanceof XYPlot xyPlot) {
            originalDomainAxisRange = xyPlot.getDomainAxis().getRange();
            originalRangeAxisRange = xyPlot.getRangeAxis().getRange();
        } else {
            originalDomainAxisRange = null;
            originalRangeAxisRange = null;
        }

        setMouseWheelEnabled(true);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastX = e.getX();
                lastY = e.getY();
                setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                setDomainZoomable(false); // Disable zooming
                setRangeZoomable(false);  // Disable zooming
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                setCursor(Cursor.getDefaultCursor());
                setDomainZoomable(true);
                setRangeZoomable(true);
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                var deltaX = e.getX() - lastX;
                var deltaY = e.getY() - lastY;

                moveChart(deltaX, deltaY);

                lastX = e.getX();
                lastY = e.getY();
            }
        });
    }

    public void resetView() {
        if (getChart().getPlot() instanceof XYPlot plot && originalDomainAxisRange != null) {
            plot.getDomainAxis().setRange(originalDomainAxisRange);
            plot.getRangeAxis().setRange(originalRangeAxisRange);
        }
    }

    private void moveChart(int deltaX, int deltaY) {
        var plot = getChart().getPlot();
        if (plot instanceof XYPlot xyPlot) {
            var domainAxis = xyPlot.getDomainAxis();
            var rangeAxis = xyPlot.getRangeAxis();

            var domainShift = domainAxis.getRange().getLength() * deltaX / getWidth();
            var rangeShift = rangeAxis.getRange().getLength() * deltaY / getHeight();

            domainAxis.setRange(domainAxis.getLowerBound() - domainShift, domainAxis.getUpperBound() - domainShift);
            rangeAxis.setRange(rangeAxis.getLowerBound() + rangeShift, rangeAxis.getUpperBound() + rangeShift);
        }
    }
}
