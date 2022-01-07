package pipe.gui.canvas;

import pipe.gui.Constants;

import java.awt.geom.AffineTransform;

/**
 * @author Tim Kimber
 * @author Pere Bonet - Minor changes
 */
public class Zoomer {

	private int percent;

	public Zoomer(){
		this(Constants.ZOOM_DEFAULT);
	}
	
	public Zoomer(int pct) {
		percent = pct;
	}

	public boolean zoomOut() {
		return setPercent(percent - Constants.ZOOM_DELTA);
	}

	public boolean zoomIn() {
			return setPercent(percent + Constants.ZOOM_DELTA);
	}

	private boolean validZoom(int newPercent){
        return newPercent <= Constants.ZOOM_MAX && newPercent >= Constants.ZOOM_MIN;
		
	}
	public int getPercent() {
		return percent;
	}

	private boolean setPercent(int newPercent) {
		if (validZoom(newPercent)) {
			percent = newPercent;
			return true;
		}
		return false;
	}

	public boolean setZoom(int newPercent) {
		return setPercent(newPercent);
	}

    public static AffineTransform getTransform(int zoom) {
        return AffineTransform.getScaleInstance(zoom * 0.01, zoom * 0.01);
    }

    public static double getScaleFactor(int zoom) {
        return zoom * 0.01;
    }

	public static int getZoomedValue(int x, int zoom) {
		return (int) Math.round((x * zoom * 0.01));
	}

    /**
     * @deprecated use int getUnzoomedValue(int x, int zoom)
     */
    @Deprecated
	public static int getZoomedValue(double x, int zoom) {
        return (int) Math.round((x * zoom * 0.01));
	}

	public static int getUnzoomedValue(int x, int zoom) {
		return (int) Math.round((x / (zoom * 0.01)));
	}

    /**
     * @deprecated use int getUnzoomedValue(int x, int zoom)
     */
	@Deprecated
	public static int getUnzoomedValue(double x, int zoom) {
		return (int) Math.round(x / (zoom * 0.01));
	}

}
