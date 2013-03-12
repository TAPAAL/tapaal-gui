package dk.aau.cs.util;

import java.math.MathContext;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class which implements Compatator<Object> the compare method 
 * compares the results from "toString()" of the objects.
 * If the two objects bouth end in an integer and the first parts
 * are identical the method compares the two numbers and returns the result of this
 * 
 * Thus t2 is considered smaller than t10
 * 
 *
 */
public class StringComparator implements Comparator<Object>{
	

	public int compare(Object o1, Object o2) {
		// Test if really numbers
		Pattern p = Pattern.compile("([0-9]*\\.?[0-9]*).*");
		Matcher m1 = p.matcher(o1.toString());
		Matcher m2 = p.matcher(o2.toString());
		if(m1.matches() && m2.matches()){
			try{
				return Double.compare(Double.parseDouble(m1.group(1)), Double.parseDouble(m2.group(1)));
			}catch(NumberFormatException e){
				// Continue as usual
			}
		}
		// Values are not numbers
		p = Pattern.compile("\\d\\d*$");
		String s1 = o1.toString().toLowerCase();
		String s2 = o2.toString().toLowerCase();
		int p1, p2;
		if((p1 = s1.indexOf("=")) != -1 && (p2 = s2.indexOf("=")) != -1){
			//The strings is constants
			s1 = s1.substring(0, p1).trim();
			s2 = s2.substring(0, p2).trim();
		}
		m1 = p.matcher(s1);
		m2 = p.matcher(s2);
		if(m1.find() && m2.find()){
			if(s1.substring(0, m1.start()).equals(s2.substring(0, m2.start()))){
				int i1 = Integer.parseInt(m1.group());
				int i2 = Integer.parseInt(m2.group());
				
				return i1-i2;
			}
		}
		return s1.compareTo(s2);
	}
}
