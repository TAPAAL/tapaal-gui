package pipe.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

public class BlueTransitionControl extends JPanel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5735674907635981304L;
	JSlider bluePrecision;
	
	public BlueTransitionControl() {
		super(new BorderLayout());
		
		//0 corresponds to 0.0001, 4 corresponds to 1 (   thus x corresponds to 1/(10^(4−x))  )
		bluePrecision = new JSlider(JSlider.HORIZONTAL, 0, 4, 3);
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put(new Integer(0), new JLabel(Double.toString(0.0001)));
		labelTable.put(new Integer(1), new JLabel(Double.toString(0.001)));
		labelTable.put(new Integer(2), new JLabel(Double.toString(0.01)));
		labelTable.put(new Integer(3), new JLabel(Double.toString(0.1)));
		labelTable.put(new Integer(4), new JLabel(Double.toString(1)));
		
		bluePrecision.setLabelTable(labelTable);
		bluePrecision.setSnapToTicks(true);
		bluePrecision.setMajorTickSpacing(1);
		bluePrecision.setPaintLabels(true);
		bluePrecision.setPaintTicks(true);
		
		add(new JLabel("Set the delay granularity"), BorderLayout.NORTH);
		add(bluePrecision, BorderLayout.SOUTH);
		
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Blue transitions controller"), 
				BorderFactory.createEmptyBorder(3, 3, 3, 3)));
		this.setPreferredSize(new Dimension(275, 100));
		this.setMinimumSize(new Dimension(275, 100));
		
	}
	
	public BigDecimal getValue(){
		return new BigDecimal(1.0/(Math.pow(10.0, (4.0-bluePrecision.getValue()))), new MathContext(Pipe.AGE_PRECISION));
	}
}
