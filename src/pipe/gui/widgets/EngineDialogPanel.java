package pipe.gui.widgets;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import dk.aau.cs.verification.UPPAAL.Verifyta;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPN;

import pipe.gui.CreateGui;
import pipe.gui.FileFinder;
import pipe.gui.FileFinderImpl;
import pipe.gui.MessengerImpl;

public class EngineDialogPanel {	
	private EscapableDialog dialog;
	private JPanel enginePanel;
	private JPanel tapaalPanel;
	private JPanel uppaalPanel;
	
	JLabel tapaalLocationLabel = new JLabel("Located: ");
	JLabel tapaalVersionLabel = new JLabel("Version: ");
	JLabel uppaalLocationLabel = new JLabel("Located: ");
	JLabel uppaalVersionLabel = new JLabel("Version: ");
	
	private Dimension minimumSize = new Dimension(320,1);
	private Insets panelInsets = new Insets(5, 5, 5, 5);
	private Insets smallPanelInsets = new Insets(0, 5, 0, 5);
	private Insets buttonInsets = new Insets(0, 5, 0, 0);
	
	private String toolTipSelect = "Select a path to the verification engine.";
	private String toolTipReset = "Reset the path to the verification engine.";
	
	public EngineDialogPanel() {
		initComponents();		
	}
	
	public void initComponents() {
		makeTapaalPanel();
		makeUppaalPanel();
		makeEnginePanel();
		setPathsAndVersionNumbers();
	}	
	
	private void selectTapnEngine() {
		FileFinder fileFinder = new FileFinderImpl();
		MessengerImpl messenger = new MessengerImpl();
		String verifytapnpath = null;
		try {
			File file = fileFinder.ShowFileBrowserDialog("Verifytapn", "");
			if(file != null){
				if(file.getName().matches("^verifytapn.*(?:\\.exe)?$")){
					verifytapnpath = file.getAbsolutePath();
				}else{
					messenger.displayErrorMessage("The selected executable does not seem to be verifytapn.");
				}
			}
		} catch (Exception e) {
			messenger.displayErrorMessage("There were errors performing the requested action:\n" + e, "Error");
		}
		if (verifytapnpath != null) {
			VerifyTAPN verifyTapn = new VerifyTAPN(fileFinder,messenger);
			verifyTapn.setVerifyTapnPath(verifytapnpath);
			tapaalLocationLabel.setText("Located: "+verifytapnpath);
			tapaalVersionLabel.setText("Version: "+verifyTapn.getVersion());
			fitDialog();
		}
	}
	
	private void selectVerifytaEngine() {
		FileFinder fileFinder = new FileFinderImpl();
		MessengerImpl messenger = new MessengerImpl();
		String verifytapath = null;
		try {
			File file = fileFinder.ShowFileBrowserDialog("Uppaal Verifyta", "");
			
			if(file != null){
				if(file.getName().matches("^verifyta(?:\\d.*)?(?:\\.exe)?$")){
					verifytapath = file.getAbsolutePath();
				}else{
					messenger.displayErrorMessage("The selected executable does not seem to be verifyta.");
				}
			}

		} catch (Exception e) {
			messenger.displayErrorMessage(
					"There were errors performing the requested action:\n"
							+ e, "Error");
		}
		if (verifytapath != null) {
			Verifyta verifyta = new Verifyta(fileFinder,messenger);
			verifyta.setVerifytaPath(verifytapath);
			uppaalLocationLabel.setText("Located: "+verifytapath);
			uppaalVersionLabel.setText("Version: "+verifyta.getVersion());
			fitDialog();
		}
	}
	
	private void resetVerifytaEngine() {
		Verifyta.reset(); Verifyta.trySetupFromEnvironmentVariable();
		uppaalLocationLabel.setText("Located: Not setup");
		uppaalVersionLabel.setText("Version: N/A");
		fitDialog();
	}
	
	private void resetTapnEngine() {
		VerifyTAPN.reset();
		tapaalLocationLabel.setText("Located: Not setup");
		tapaalVersionLabel.setText("Version: N/A");
		fitDialog();
	}
	
	private void fitDialog() {
		dialog.pack();
		dialog.setLocationRelativeTo(null);
	}
	
	private void exit() {
		dialog.setVisible(false);
	}
	
	private void setPathsAndVersionNumbers() {
		Verifyta verifyta = new Verifyta();
		String verifytaPath = verifyta.getPath();
		String verifytaversion = "";
		if (verifytaPath == null || verifytaPath.isEmpty()) {
			verifytaPath = "Not setup";
			verifytaversion = "N/A";
		} else {
			verifytaversion = verifyta.getVersion();
		}
		VerifyTAPN verifyTAPN = new VerifyTAPN(new FileFinderImpl(), new MessengerImpl());
		String verifytapnPath = verifyTAPN.getPath();
		String verifytapnversion = "";

		if (verifytapnPath == null || verifytapnPath.isEmpty()) {
			verifytapnPath = "Not setup";
			verifytapnversion = "N/A";
		} else {
			verifytapnversion = verifyTAPN.getVersion();
		}
		tapaalLocationLabel.setText(tapaalLocationLabel.getText()+verifytapnPath);
		tapaalVersionLabel.setText(tapaalVersionLabel.getText()+verifytapnversion);
		uppaalLocationLabel.setText(uppaalLocationLabel.getText()+verifytaPath);
		uppaalVersionLabel.setText(uppaalVersionLabel.getText()+verifytaversion);
	}

	private void makeTapaalPanel() {
		//make tapaal panel
		tapaalPanel = new JPanel();
		tapaalPanel.setBorder(BorderFactory.createTitledBorder("TAPAAL Engine (verifytapn) Information"));
		tapaalPanel.setLayout(new GridBagLayout());

		//add info panel to tapaal panel
		JPanel tapaalInfoPanel = new JPanel();
		tapaalInfoPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		tapaalLocationLabel = new JLabel("Located: ");
		tapaalVersionLabel = new JLabel("Version: ");

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		tapaalInfoPanel.add(tapaalLocationLabel,gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		tapaalInfoPanel.add(tapaalVersionLabel,gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = smallPanelInsets;
		tapaalPanel.add(tapaalInfoPanel,gbc);

		//add TapaalButtonPanel to  tapaalpanel
		JButton tapaalSelectButton = new JButton("Select");
		tapaalSelectButton.setMnemonic(KeyEvent.VK_E);
		tapaalSelectButton.setToolTipText(toolTipSelect);
		JButton tapaalResetButton = new JButton("Reset");
		tapaalResetButton.setToolTipText(toolTipReset);
		tapaalResetButton.setMnemonic(KeyEvent.VK_T);
		tapaalSelectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectTapnEngine();
			}
		});
		tapaalResetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				resetTapnEngine();
			}
		});
		JPanel tapaalButtonPanel = new JPanel();
		tapaalButtonPanel.setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.gridx = 0;		
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.anchor = GridBagConstraints.EAST;
		tapaalButtonPanel.add(tapaalSelectButton,gbc);
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.insets = buttonInsets;
		gbc.anchor = GridBagConstraints.EAST;		
		tapaalButtonPanel.add(tapaalResetButton,gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = panelInsets;
		tapaalPanel.add(tapaalButtonPanel,gbc);
	}

	private void makeUppaalPanel() {
		//make uppaal panel
		uppaalPanel = new JPanel();
		uppaalPanel.setBorder(BorderFactory.createTitledBorder("UPPAAL Engine (verifyta) Information"));
		uppaalPanel.setLayout(new GridBagLayout());

		//add info panel to uppal panel
		JPanel uppaalInfoPanel = new JPanel();
		uppaalInfoPanel.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		uppaalLocationLabel = new JLabel("Located: ");
		uppaalVersionLabel = new JLabel("Version: ");

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		uppaalInfoPanel.add(uppaalLocationLabel,gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		uppaalInfoPanel.add(uppaalVersionLabel,gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = smallPanelInsets;
		uppaalPanel.add(uppaalInfoPanel,gbc);	

		//add uppaalButtonPanel to  uppaalPanel
		JButton uppaalSelectButton = new JButton("Select");
		uppaalSelectButton.setMnemonic(KeyEvent.VK_S);
		uppaalSelectButton.setToolTipText(toolTipSelect);
		JButton uppaalResetButton = new JButton("Reset");
		uppaalResetButton.setMnemonic(KeyEvent.VK_R);
		uppaalResetButton.setToolTipText(toolTipReset);
		uppaalSelectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectVerifytaEngine();
			}
		});
		uppaalResetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				resetVerifytaEngine();
			}
		});
		JPanel uppaalButtonPanel = new JPanel();
		uppaalButtonPanel.setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.anchor = GridBagConstraints.EAST;
		uppaalButtonPanel.add(uppaalSelectButton,gbc);
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = buttonInsets;
		uppaalButtonPanel.add(uppaalResetButton,gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = panelInsets;
		uppaalPanel.add(uppaalButtonPanel,gbc);
	}

	private void makeEnginePanel() {
		enginePanel = new JPanel();
		enginePanel.setLayout(new GridBagLayout());		
		
		//add panels to engine panel
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = panelInsets;
		enginePanel.add(tapaalPanel,gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = panelInsets;
		enginePanel.add(uppaalPanel,gbc);
		
		JPanel closeButtonPanel = new JPanel();
		closeButtonPanel.setLayout(new GridBagLayout());
		JButton closeButton = new JButton("Close");
		closeButton.setMnemonic(KeyEvent.VK_C);
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exit();
			}
		});
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;		
		closeButtonPanel.add(closeButton,gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0,5,2,5);
		enginePanel.add(closeButtonPanel,gbc);		
	}
	
	public void showDialog() {
		dialog = new EscapableDialog(CreateGui.getApp(),
				"Select verification engines", true);
		dialog.add(enginePanel);
		dialog.setResizable(false);
		dialog.setMinimumSize(minimumSize);
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
	}
}
