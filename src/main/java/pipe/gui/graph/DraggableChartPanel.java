package pipe.gui.graph;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.axis.ValueAxis;

import java.awt.event.MouseAdapter;
import java.awt.Cursor;
import java.awt.event.MouseEvent;

public class DraggableChartPanel extends ChartPanel {
    private int lastX;
    private int lastY;

    public DraggableChartPanel(JFreeChart chart) {
        super(chart);
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
                int deltaX = e.getX() - lastX;
                int deltaY = e.getY() - lastY;

                moveChart(deltaX, deltaY);

                lastX = e.getX();
                lastY = e.getY();
            }
        });
    }

    private void moveChart(int deltaX, int deltaY) {
        Plot plot = getChart().getPlot();
        if (plot instanceof XYPlot) {
            XYPlot xyPlot = (XYPlot) plot;
            ValueAxis domainAxis = xyPlot.getDomainAxis();
            ValueAxis rangeAxis = xyPlot.getRangeAxis();

            double domainShift = domainAxis.getRange().getLength() * deltaX / getWidth();
            double rangeShift = rangeAxis.getRange().getLength() * deltaY / getHeight();

            domainAxis.setRange(domainAxis.getLowerBound() - domainShift, domainAxis.getUpperBound() - domainShift);
            rangeAxis.setRange(rangeAxis.getLowerBound() + rangeShift, rangeAxis.getUpperBound() + rangeShift);
        }
    }
}