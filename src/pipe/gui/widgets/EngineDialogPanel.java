package pipe.gui.widgets;

import java.awt.Dimension;
import java.awt.FlowLayout;
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
import dk.aau.cs.verification.VerifyTAPN.VerifyPN;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPN;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPNDiscreteVerification;

import pipe.gui.CreateGui;
import pipe.gui.FileFinder;
import pipe.gui.MessengerImpl;

public class EngineDialogPanel {	
	private EscapableDialog dialog;
	private JPanel enginePanel;
	private JPanel tapaalPanel;
	private JPanel tapaalDiscretePanel;
	private JPanel untimedPanel;
	private JPanel uppaalPanel;

	JButton closeButton;

	JLabel tapaalLocationLabel = new JLabel("Located: ");
	JLabel tapaalVersionLabel = new JLabel("Version: ");
	JLabel dtapaalLocationLabel = new JLabel("Located: ");
	JLabel dtapaalVersionLabel = new JLabel("Version: ");
	JLabel untimedLocationLabel = new JLabel("Located: ");
	JLabel untimedVersionLabel = new JLabel("Version: ");
	JLabel uppaalLocationLabel = new JLabel("Located: ");
	JLabel uppaalVersionLabel = new JLabel("Version: ");

	JLabel tapaalLocationLabelVal = new JLabel("Not setup");
	JLabel tapaalVersionLabelVal = new JLabel("N/A");
	JLabel dtapaalLocationLabelVal = new JLabel("Not setup");
	JLabel dtapaalVersionLabelVal = new JLabel("N/A");
	JLabel untimedLocationLabelVal = new JLabel("Not setup");
	JLabel untimedVersionLabelVal = new JLabel("N/A");
	JLabel uppaalLocationLabelVal = new JLabel("Not setup");
	JLabel uppaalVersionLabelVal = new JLabel("N/A");

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
		makeDiscreteTapaalPanel();
		makeUntimedPanel();
		makeEnginePanel();
		setPathsAndVersionNumbers();
	}	

	private void selectTapnEngine() {
		FileFinder fileFinder = new FileFinder();
		MessengerImpl messenger = new MessengerImpl();
		String verifytapnpath = null;
		try {
			File file = fileFinder.ShowFileBrowserDialog("Verifytapn", "", (new VerifyTAPN(new FileFinder(), new MessengerImpl())).getPath());
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
			try{
				VerifyTAPN verifyTapn = new VerifyTAPN(fileFinder,messenger);
				verifyTapn.setPath(verifytapnpath);
			}catch(IllegalArgumentException e){
				messenger.displayErrorMessage(e.getMessage(), "Error selecting engine");
			}
			setPathsAndVersionNumbers();
			fitDialog();
		}
	}

	private void selectdTapnEngine() {
		FileFinder fileFinder = new FileFinder();
		MessengerImpl messenger = new MessengerImpl();
		String verifytapnpath = null;
		try {
			File file = fileFinder.ShowFileBrowserDialog("Verifydtapn", "", (new VerifyTAPNDiscreteVerification(new FileFinder(), new MessengerImpl())).getPath());
			if(file != null){
				if(file.getName().matches("^verifydtapn.*(?:\\.exe)?$")){
					verifytapnpath = file.getAbsolutePath();
				}else{
					messenger.displayErrorMessage("The selected executable does not seem to be verifydtapn.");
				}
			}
		} catch (Exception e) {
			messenger.displayErrorMessage("There were errors performing the requested action:\n" + e, "Error");
		}
		if (verifytapnpath != null) {
			try{
				VerifyTAPNDiscreteVerification verifyTapn = new VerifyTAPNDiscreteVerification(fileFinder,messenger);
				verifyTapn.setPath(verifytapnpath);
			}catch(IllegalArgumentException e){
				messenger.displayErrorMessage(e.getMessage(), "Error selecting engine");
			}
			setPathsAndVersionNumbers();
			fitDialog();
		}
	}

	private void selectVerifytaEngine() {
		FileFinder fileFinder = new FileFinder();
		MessengerImpl messenger = new MessengerImpl();
		String verifytapath = null;
		try {
			File file = fileFinder.ShowFileBrowserDialog("Verifyta", "",(new Verifyta()).getPath());

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
			try{
				Verifyta verifyta = new Verifyta(fileFinder,messenger);
				verifyta.setPath(verifytapath);
			}catch(IllegalArgumentException e){
				messenger.displayErrorMessage(e.getMessage(), "Error selecting engine");
			}
			setPathsAndVersionNumbers();
			fitDialog();
		}
	}

	private void selectVerifypnEngine() {
		FileFinder fileFinder = new FileFinder();
		MessengerImpl messenger = new MessengerImpl();
		String verifypnpath = null;
		try {
			File file = fileFinder.ShowFileBrowserDialog("Verifypn", "",(new VerifyPN(new FileFinder(), new MessengerImpl())).getPath());
			if(file != null){
				if(file.getName().matches("^verifypn.*(?:\\.exe)?$")){
					verifypnpath = file.getAbsolutePath();
				}else{
					messenger.displayErrorMessage("The selected executable does not seem to be verifypn.");
				}
			}
		} catch (Exception e) {
			messenger.displayErrorMessage("There were errors performing the requested action:\n" + e, "Error");
		}
		if (verifypnpath != null) {
			try{
				VerifyPN verifyPn = new VerifyPN(fileFinder,messenger);
				verifyPn.setPath(verifypnpath);
			}catch(IllegalArgumentException e){
				messenger.displayErrorMessage(e.getMessage(), "Error selecting engine");
			}
			setPathsAndVersionNumbers();
			fitDialog();
		}
	}

	private void resetVerifytaEngine() {
		Verifyta.reset(); 
		setPathsAndVersionNumbers();
	}

	private void resetVerifytapnEngine() {
		VerifyTAPN.reset();
		setPathsAndVersionNumbers();
	}

	private void resetVerifydtapnEngine() {
		VerifyTAPNDiscreteVerification.reset();
		setPathsAndVersionNumbers();
	}
	
	private void resetVerifypnEngine() {
		VerifyPN.reset();
		setPathsAndVersionNumbers();
	}

	private void fitDialog() {
		if (dialog != null) {
			dialog.pack();
			dialog.setLocationRelativeTo(null);	
		}
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
		VerifyTAPN verifyTAPN = new VerifyTAPN(new FileFinder(), new MessengerImpl());
		String verifytapnPath = verifyTAPN.getPath();
		String verifytapnversion = "";

		if (verifytapnPath == null || verifytapnPath.isEmpty()) {
			verifytapnPath = "Not setup";
			verifytapnversion = "N/A";
		} else {
			verifytapnversion = verifyTAPN.getVersion();
		}

		VerifyTAPNDiscreteVerification verifydTAPN = new VerifyTAPNDiscreteVerification(new FileFinder(), new MessengerImpl());
		String verifydtapnPath = verifydTAPN.getPath();
		String verifydtapnversion = "";

		if (verifydtapnPath == null || verifydtapnPath.isEmpty()) {
			verifydtapnPath = "Not setup";
			verifydtapnversion = "N/A";
		} else {
			verifydtapnversion = verifydTAPN.getVersion();
		}
		
		VerifyPN verifypn = new VerifyPN(new FileFinder(), new MessengerImpl());
		String verifypnpath = verifypn.getPath();
		String verifypnversion = "";

		if (verifypnpath == null || verifypnpath.isEmpty()) {
			verifypnpath = "Not setup";
			verifypnversion = "N/A";
		} else {
			verifypnversion = verifypn.getVersion();
		}
		
		tapaalLocationLabelVal.setText(verifytapnPath);
		tapaalVersionLabelVal.setText(verifytapnversion);
		dtapaalLocationLabelVal.setText(verifydtapnPath);
		dtapaalVersionLabelVal.setText(verifydtapnversion);
		untimedLocationLabelVal.setText(verifypnpath);
		untimedVersionLabelVal.setText(verifypnversion);
		uppaalLocationLabelVal.setText(verifytaPath);
		uppaalVersionLabelVal.setText(verifytaversion);
		fitDialog();
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

		JPanel p = new JPanel(new FlowLayout());
		p.add(tapaalLocationLabel);
		p.add(tapaalLocationLabelVal);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		tapaalInfoPanel.add(p,gbc);

		p = new JPanel(new FlowLayout());
		p.add(tapaalVersionLabel);
		p.add(tapaalVersionLabelVal);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		tapaalInfoPanel.add(p,gbc);

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
				resetVerifytapnEngine();
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

	private void makeDiscreteTapaalPanel() {
		//make tapaal panel
		tapaalDiscretePanel = new JPanel();
		tapaalDiscretePanel.setBorder(BorderFactory.createTitledBorder("Discrete TAPAAL Engine (verifydtapn) Information"));
		tapaalDiscretePanel.setLayout(new GridBagLayout());

		//add info panel to tapaal panel
		JPanel tapaalDiscreteInfoPanel = new JPanel();
		tapaalDiscreteInfoPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel p = new JPanel(new FlowLayout());
		p.add(dtapaalLocationLabel);
		p.add(dtapaalLocationLabelVal);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		tapaalDiscreteInfoPanel.add(p,gbc);

		p = new JPanel(new FlowLayout());
		p.add(dtapaalVersionLabel);
		p.add(dtapaalVersionLabelVal);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		tapaalDiscreteInfoPanel.add(p,gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = smallPanelInsets;
		tapaalDiscretePanel.add(tapaalDiscreteInfoPanel,gbc);

		//add TapaalButtonPanel to  tapaalpanel
		JButton SelectButton = new JButton("Select");
		SelectButton.setMnemonic(KeyEvent.VK_E);
		SelectButton.setToolTipText(toolTipSelect);
		JButton ResetButton = new JButton("Reset");
		ResetButton.setToolTipText(toolTipReset);
		ResetButton.setMnemonic(KeyEvent.VK_T);
		SelectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectdTapnEngine();
			}
		});
		ResetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				resetVerifydtapnEngine();
			}
		});
		JPanel ButtonPanel = new JPanel();
		ButtonPanel.setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.gridx = 0;		
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.anchor = GridBagConstraints.EAST;
		ButtonPanel.add(SelectButton,gbc);
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.insets = buttonInsets;
		gbc.anchor = GridBagConstraints.EAST;		
		ButtonPanel.add(ResetButton,gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = panelInsets;
		tapaalDiscretePanel.add(ButtonPanel,gbc);
	}
	
	private void makeUntimedPanel() {
		//make tapaal panel
		untimedPanel = new JPanel();
		untimedPanel.setBorder(BorderFactory.createTitledBorder("Untimed TAPAAL Engine (verifypn) Information"));
		untimedPanel.setLayout(new GridBagLayout());

		//add info panel to tapaal panel
		JPanel untimedInfoPanel = new JPanel();
		untimedInfoPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
		JPanel p = new JPanel(new FlowLayout());
		p.add(untimedLocationLabel);
		p.add(untimedLocationLabelVal);
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		untimedInfoPanel.add(p,gbc);

		p = new JPanel(new FlowLayout());
		p.add(untimedVersionLabel);
		p.add(untimedVersionLabelVal);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		untimedInfoPanel.add(p,gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = smallPanelInsets;
		untimedPanel.add(untimedInfoPanel,gbc);

		//add TapaalButtonPanel to  tapaalpanel
		JButton SelectButton = new JButton("Select");
		SelectButton.setMnemonic(KeyEvent.VK_E);
		SelectButton.setToolTipText(toolTipSelect);
		JButton ResetButton = new JButton("Reset");
		ResetButton.setToolTipText(toolTipReset);
		ResetButton.setMnemonic(KeyEvent.VK_T);
		SelectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectVerifypnEngine();
			}
		});
		ResetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				resetVerifypnEngine();
			}
		});
		JPanel ButtonPanel = new JPanel();
		ButtonPanel.setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.gridx = 0;		
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.anchor = GridBagConstraints.EAST;
		ButtonPanel.add(SelectButton,gbc);
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.insets = buttonInsets;
		gbc.anchor = GridBagConstraints.EAST;		
		ButtonPanel.add(ResetButton,gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = panelInsets;
		untimedPanel.add(ButtonPanel,gbc);
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

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;

		JPanel p = new JPanel(new FlowLayout());

		p.add(uppaalLocationLabel);
		p.add(uppaalLocationLabelVal);

		uppaalInfoPanel.add(p,gbc);

		p = new JPanel(new FlowLayout());
		p.add(uppaalVersionLabel);
		p.add(uppaalVersionLabelVal);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		uppaalInfoPanel.add(p,gbc);

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
		enginePanel.add(tapaalDiscretePanel,gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = panelInsets;
		enginePanel.add(untimedPanel,gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = panelInsets;
		enginePanel.add(uppaalPanel,gbc);

		JPanel closeButtonPanel = new JPanel();
		closeButtonPanel.setLayout(new GridBagLayout());
		closeButton = new JButton("Close");
		closeButton.setMnemonic(KeyEvent.VK_C);
		closeButton.addAncestorListener(new RequestFocusListener());
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
		gbc.gridy = 4;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0,5,2,5);
		enginePanel.add(closeButtonPanel,gbc);	

	}

	public void showDialog() {
		dialog = new EscapableDialog(CreateGui.getApp(),
				"Selection of Verification Engines", true);
		dialog.add(enginePanel);
		dialog.getRootPane().setDefaultButton(closeButton);
		dialog.setResizable(false);
		dialog.setMinimumSize(minimumSize);
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);

	}
}
