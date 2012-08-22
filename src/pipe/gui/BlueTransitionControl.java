package pipe.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.UIManager;

import dk.aau.cs.model.tapn.simulation.DelayMode;
import dk.aau.cs.model.tapn.simulation.ManualDelayMode;
import dk.aau.cs.model.tapn.simulation.RandomDelayMode;
import dk.aau.cs.model.tapn.simulation.YoungestDelayMode;

import java.lang.reflect.Field;

public class BlueTransitionControl extends JPanel{
	private static final String YOUNGEST = "Youngest";
	private static final String RANDOM = "Random";
	private static final String MANUAL = "Manual";
	/**
	 * 
	 */
	private static final long serialVersionUID = -5735674907635981304L;
	JSlider bluePrecision;
	JComboBox delayMode;
	
	public BlueTransitionControl() {
		super(new GridBagLayout());
		
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
		bluePrecision.setPaintTrack(false);
		//UIManager.put("Slider.paintValue", false);
		//UIManager.getLookAndFeelDefaults().put("Slider.paintValue", false);
		
		//TODO is this good? it's the only soloution I've found
		//It makes sure the value of the slider is NOT written 
		//above the knob.
		/*Class<?> sliderUIClass;
		try {
			sliderUIClass = Class.forName("javax.swing.plaf.synth.SynthSliderUI");
			Field paintValue = sliderUIClass.getDeclaredField("paintValue");
	        paintValue.setAccessible(true);
	        paintValue.set(bluePrecision.getUI(), false);
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		
		String[] items = {"Youngest", "Random", "Manual"};
		delayMode = new JComboBox(items);
        
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.gridx = 0;
		gbc.gridy = 0;
		add(new JLabel("Set the delay granularity"), gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.gridx = 0;
		gbc.gridy = 1;
		add(bluePrecision, gbc);
		
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 2;
		add(new JLabel("Delay Mode:"), gbc);
		
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.gridx = 1;
		gbc.gridy = 2;
		add(delayMode, gbc);
		
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Blue transitions controller"), 
				BorderFactory.createEmptyBorder(3, 3, 3, 3)));
	}
	//0 corresponds to 0.0001, 4 corresponds to 1 (   thus x corresponds to 1/(10^(4−x))  )
	public BigDecimal getValue(){
		return new BigDecimal(1.0/(Math.pow(10.0, (4.0-bluePrecision.getValue()))), new MathContext(Pipe.AGE_PRECISION));
	}
	
	public DelayMode getDelayMode(){
		String selectedItem = (String)delayMode.getSelectedItem();
		
		if(selectedItem == MANUAL){
			return new ManualDelayMode();
		} else if(selectedItem == RANDOM){
			return new RandomDelayMode();
		} else if(selectedItem == YOUNGEST){
			return new YoungestDelayMode();
		} else{
			return null;
		}
	}
}
