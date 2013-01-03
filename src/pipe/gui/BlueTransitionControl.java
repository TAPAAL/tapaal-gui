package pipe.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import pipe.gui.widgets.EscapableDialog;

import dk.aau.cs.model.tapn.simulation.DelayMode;
import dk.aau.cs.model.tapn.simulation.ManualDelayMode;
import dk.aau.cs.model.tapn.simulation.RandomDelayMode;
import dk.aau.cs.model.tapn.simulation.ShortestDelayMode;

public class BlueTransitionControl extends JPanel{

	private static final long serialVersionUID = -5735674907635981304L;
	JSlider bluePrecision;
	JComboBox delayMode;
	
	private BlueTransitionControl() {
		super(new GridBagLayout());
		
		//0 corresponds to 0.00001, 5 corresponds to 1 (   thus x corresponds to 1/(10^(5−x))  )
		bluePrecision = new JSlider(JSlider.HORIZONTAL, 0, 5, 4);
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put(new Integer(0), new JLabel("0.00001"));
		labelTable.put(new Integer(1), new JLabel("0.0001"));
		labelTable.put(new Integer(2), new JLabel("0.001"));
		labelTable.put(new Integer(3), new JLabel("0.01"));
		labelTable.put(new Integer(4), new JLabel("0.1"));
		labelTable.put(new Integer(5), new JLabel("1"));
		
		bluePrecision.setLabelTable(labelTable);
		bluePrecision.setSnapToTicks(true);
		bluePrecision.setMajorTickSpacing(1);
		bluePrecision.setPaintLabels(true);
		bluePrecision.setPaintTicks(true);
		bluePrecision.setPaintTrack(false);
		bluePrecision.setPreferredSize(new Dimension(340, bluePrecision.getPreferredSize().height));
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
		
		String[] items = {ShortestDelayMode.name(), RandomDelayMode.name(), ManualDelayMode.name()};
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
				BorderFactory.createTitledBorder("Delay controller"), 
				BorderFactory.createEmptyBorder(3, 3, 3, 3)));
	}
	
	//0 corresponds to 0.00001, 5 corresponds to 1 (   thus x corresponds to 1/(10^(5−x))  )
	public BigDecimal getValue(){
		return new BigDecimal(1.0/(Math.pow(10.0, (5.0-bluePrecision.getValue()))), new MathContext(Pipe.AGE_PRECISION));
	}
	
	public DelayMode getDelayMode(){
		String selectedItem = (String)delayMode.getSelectedItem();
		
		if(selectedItem == ManualDelayMode.name()){
			return new ManualDelayMode();
		} else if(selectedItem == RandomDelayMode.name()){
			return new RandomDelayMode();
		} else if(selectedItem == ShortestDelayMode.name()){
			return new ShortestDelayMode();
		} else{
			return null;
		}
	}
	
	private static BlueTransitionControl instance;
	private static JButton closeDialogButton;
	private static JDialog dialog;
	
	public static void showBlueTransitionDialog(){
		JPanel contentPane = new JPanel(new GridBagLayout());
		
		closeDialogButton = new JButton("Close");
		closeDialogButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				dialog.setVisible(false);
			}
		});
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(0, 3, 0, 3);
		gbc.fill = GridBagConstraints.BOTH;
		contentPane.add(getInstance(), gbc);
		
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(3, 3, 0, 3);
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		contentPane.add(closeDialogButton, gbc);
		
		dialog = new EscapableDialog(CreateGui.getApp(), "Delay controller", true);
		dialog.setContentPane(contentPane);
		dialog.pack();
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(CreateGui.getApp());
		dialog.setVisible(true);
	}
	
	public static BlueTransitionControl getInstance(){
		if(instance == null){
			instance = new BlueTransitionControl();
		}
		return instance;
	}
}
