package pipe.gui.widgets;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import pipe.gui.CreateGui;

public class EngineDialogPanel {	
	private EscapableDialog dialog;
	private JPanel enginePanel;
	
	private String tapaalPath = "Not setup";
	private String uppaalPath = "Not setup";
	private String tapaalVersion = "N/A";
	private String uppaalVersion = "N/A";
	
	//private Dimension minimumSize = new Dimension(400,500);
	
	public EngineDialogPanel() {
		initComponents();		
	}
	
	public void initComponents() {
		enginePanel = new JPanel();
		enginePanel.setLayout(new GridBagLayout());
		
		//make tapaal panel
		JPanel tapaalPanel = new JPanel();
		tapaalPanel.setBorder(BorderFactory.createTitledBorder("Tapaal engine"));
		tapaalPanel.setLayout(new GridBagLayout());
		
		JPanel tapaalInfoPanel = new JPanel();
		tapaalInfoPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		JLabel tapaalLocationLabel = new JLabel("Located: ");
		JLabel tapaalPathLabel = new JLabel(tapaalPath);
		JLabel tapaalVersionLabel = new JLabel("Version: ");
		JLabel tapaalVersionInfoLabel = new JLabel(tapaalVersion);
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		tapaalInfoPanel.add(tapaalLocationLabel,gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		tapaalInfoPanel.add(tapaalPathLabel,gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		tapaalInfoPanel.add(tapaalVersionLabel,gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		tapaalInfoPanel.add(tapaalVersionInfoLabel,gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		tapaalPanel.add(tapaalInfoPanel,gbc);
		
		//add TapaalButtonPanel to  tapaalpanel
		JButton tapaalSelectButton = new JButton("Select");
		JButton tapaalResetButton = new JButton("Reset");
		JPanel tapaalButtonPanel = new JPanel();
		tapaalButtonPanel.setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		//gbc.anchor = GridBagConstraints.WEST;
		tapaalButtonPanel.add(tapaalSelectButton,gbc);
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		//gbc.anchor = GridBagConstraints.EAST;
		tapaalButtonPanel.add(tapaalResetButton,gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		//gbc.fill = GridBagConstraints.HORIZONTAL;
		//gbc.weightx = 0;
		tapaalPanel.add(tapaalButtonPanel,gbc);
		
		
		//make uppaal panel
		JPanel uppaalPanel = new JPanel();
		uppaalPanel.setBorder(BorderFactory.createTitledBorder("Uppaal engine"));
		uppaalPanel.setLayout(new GridBagLayout());
		
		JPanel uppaalInfoPanel = new JPanel();
		uppaalInfoPanel.setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();
		JLabel uppaalLocationLabel = new JLabel("Located: ");
		JLabel uppaalPathLabel = new JLabel(uppaalPath);
		JLabel uppaalVersionLabel = new JLabel("Version: ");
		JLabel uppaalVersionInfoLabel = new JLabel(uppaalVersion);
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		uppaalInfoPanel.add(uppaalLocationLabel,gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		uppaalInfoPanel.add(uppaalPathLabel,gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		uppaalInfoPanel.add(uppaalVersionLabel,gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		uppaalInfoPanel.add(uppaalVersionInfoLabel,gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		uppaalPanel.add(uppaalInfoPanel,gbc);	
		
		//add uppaalButtonPanel to  uppaalPanel
		JButton uppaalSelectButton = new JButton("Select");
		JButton uppaalResetButton = new JButton("Reset");
		JPanel uppaalButtonPanel = new JPanel();
		uppaalButtonPanel.setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		uppaalButtonPanel.add(uppaalSelectButton,gbc);
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.EAST;
		uppaalButtonPanel.add(uppaalResetButton,gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		uppaalPanel.add(uppaalButtonPanel,gbc);

		//add panels to engine panel
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		//gbc.fill = GridBagConstraints.HORIZONTAL;
		enginePanel.add(tapaalPanel,gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		//gbc.fill = GridBagConstraints.VERTICAL;
		enginePanel.add(uppaalPanel,gbc);
		
		JPanel closeButtonPanel = new JPanel();
		closeButtonPanel.setLayout(new GridBagLayout());
		JButton closeButton = new JButton("Close");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;		
		closeButtonPanel.add(closeButton,gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.EAST;
		enginePanel.add(closeButtonPanel,gbc);
		
	}

	public void showDialog() {
		dialog = new EscapableDialog(CreateGui.getApp(),
				"Select verification engines", true);
		dialog.add(enginePanel);
		dialog.setResizable(false);
		//dialog.setMinimumSize(minimumSize);
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
	}
}
