package pipe.gui.graphicElements.tapn;

public class ShowNilToInfinityIntervals {
	private static boolean showNilToinfinityIntervals = true;
	
	public static void toggleShowNilToInfinityIntervals() {
		showNilToinfinityIntervals = !showNilToinfinityIntervals;
	}
	
	public static boolean showNilToInfinityIntervals() {
		return showNilToinfinityIntervals;
	}
}
