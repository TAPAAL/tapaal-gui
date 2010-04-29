package dk.aau.cs.petrinet.colors;

import java.util.SortedSet;
import java.util.TreeSet;

public class ColorSet {
	private SortedSet<IntegerRange> ranges = new TreeSet<IntegerRange>();

	public void addRange(IntegerRange range){
		ranges.add(range);
	}
}
