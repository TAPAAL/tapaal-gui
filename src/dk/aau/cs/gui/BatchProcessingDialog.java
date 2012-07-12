package dk.aau.cs.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker.StateValue;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableCellRenderer;

import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.gui.CreateGui;
import pipe.gui.widgets.CustomJSpinner;
import pipe.gui.widgets.EscapableDialog;
import pipe.gui.widgets.FileBrowser;
import pipe.gui.widgets.InclusionPlaces.InclusionPlacesOption;
import dk.aau.cs.gui.components.BatchProcessingResultsTableModel;
import dk.aau.cs.gui.components.MultiLineAutoWrappingToolTip;
import dk.aau.cs.io.batchProcessing.BatchProcessingResultsExporter;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.verification.batchProcessing.BatchProcessingListener;
import dk.aau.cs.verification.batchProcessing.BatchProcessingVerificationOptions;
import dk.aau.cs.verification.batchProcessing.BatchProcessingVerificationOptions.QueryPropertyOption;
import dk.aau.cs.verification.batchProcessing.BatchProcessingVerificationOptions.SymmetryOption;
import dk.aau.cs.verification.batchProcessing.BatchProcessingVerificationResult;
import dk.aau.cs.verification.batchProcessing.BatchProcessingWorker;
import dk.aau.cs.verification.batchProcessing.FileChangedEvent;
import dk.aau.cs.verification.batchProcessing.StatusChangedEvent;
import dk.aau.cs.verification.batchProcessing.VerificationTaskCompleteEvent;

public class BatchProcessingDialog extends JDialog {
	private static final long serialVersionUID = -5682084589335908227L;

	private static final String name_verifyTAPN = "TAPAAL Engine (verifytapn)";
	private static final String name_verifyTAPNDiscreteInclusion = "TAPAAL Engine w. Discrete Inclusion";
	private static final String name_verifyTAPNDiscreteVerification = "TAPAAL Engine - Discrete Verification";
	private static final String name_STANDARD = "UPPAAL: Standard Reduction";
	private static final String name_OPTIMIZEDSTANDARD = "UPPAAL: Optimised Standard Reduction";
	private static final String name_BROADCAST = "UPPAAL: Broadcast Reduction";
	private static final String name_BROADCASTDEG2 = "UPPAAL: Broadcast Degree 2 Reduction";
	private static final String name_verifyTAPNWithLegend = "A: "
			+ name_verifyTAPN;
	private static final String name_verifyTAPNDiscreteInclusionWithLegend = "B: "
			+ name_verifyTAPNDiscreteInclusion;
	private static final String name_verifyTAPNDiscreteVerificationWithLegend = "C: "
			+ name_verifyTAPNDiscreteVerification;
	private static final String name_STANDARDWithLegend = "D: " + name_STANDARD;
	private static final String name_OPTIMIZEDSTANDARDWithLegend = "E: "
			+ name_OPTIMIZEDSTANDARD;
	private static final String name_BROADCASTWithLegend = "F: "
			+ name_BROADCAST;
	private static final String name_BROADCASTDEG2WithLegend = "G: "
			+ name_BROADCASTDEG2;
	private static final String name_BFS = "Breadth first search";
	private static final String name_DFS = "Depth first search";
	private static final String name_HEURISTIC = "Heuristic search";
	private static final String name_Random = "Random search";
	private static final String name_KeepQueryOption = "Do not override";
	private static final String name_SEARCHWHOLESTATESPACE = "Search whole state space";
	private static final String name_SYMMETRY = "Yes";
	private static final String name_NOSYMMETRY = "No";

	//Tool tip strings
	//Tool tips for model panel
	private final static String TOOL_TIP_AddFilesButton = "Press to add nets to batch processing";
	private final static String TOOL_TIP_RemoveFilesButton = "Press to remove the currently selected nets";
	private final static String TOOL_TIP_ClearFilesButton = "Press to remove all nets from list";
	
	//Tool tips for override verification panel
	private final static String TOOL_TIP_QueryLabel = null;
	private final static String TOOL_TIP_Query_Property_Option = "Choose to override the queries in the nets";
	private final static String TOOL_TIP_CapacityLabel = null;
	private final static String TOOL_TIP_Number_Of_Extra_Tokens = "Enter the number of extra tokens in the nets";
	private final static String TOOL_TIP_KeepQueryCapacity = "Override the number of extra tokens in the nets";
	private final static String TOOL_TIP_SearchLabel = null;
	private final static String TOOL_TIP_SearchOption = "Choose to override the search options in the nets";
	private final static String TOOL_TIP_SymmetryLabel = null;
	private final static String TOOL_TIP_SymmetryOption = "Choose to override the symmetry reduction in the nets";
	private final static String TOOL_TIP_ReductionLabel = null;
	private final static String TOOL_TIP_ReductionOption = "Choose to override the verification methods in the nets";
	private final static String TOOL_TIP_TimeoutLabel = null;
	private final static String TOOL_TIP_TimeoutValue = "Enter the timeout in seconds";
	private final static String TOOL_TIP_NoTimeoutCheckBox = "Choose whether to use timeout";
	
	//Tool tips for monitor panel
	private final static String TOOL_TIP_FileLabel = "Currently verified net";
	private final static String TOOL_TIP_StatusLabel = "Currently verified query";
	private final static String TOOL_TIP_ProgressLabel = "Progress of the currently running batch";
	private final static String TOOL_TIP_TimeLabel = null;
	private final static String TOOL_TIP_StartButton = "Press to start batch processing";
	private final static String TOOL_TIP_CancelButton = "Press to cancel the whole currently running batch";
	private final static String TOOL_TIP_SkipFileButton = "Press to skip the currently running verification";
	
	//Tool tips for results panel
	private final static String TOOL_TIP_ExportButton = "Press to export batch results into a CVS file";
	private final static String TOOL_TIP_CloseButton = "Press to close the batch processing dialog";
	
	private static String lastPath = null;
	
	ReductionOptionChooser reductionOptionChooser;
	
	private JSplitPane splitpane;
	private JPanel topPanel;
	private JPanel bottomPanel;
	private JPanel monitorPanel;
	
	private JPanel filesButtonsPanel;
	private JButton addFilesButton;
	private JButton clearFilesButton;
	private JButton removeFileButton;
	private JList fileList;
	private DefaultListModel listModel;

	private JLabel statusLabel;
	private JLabel fileStatusLabel;
	private JButton startButton;
	private JButton cancelButton;
	private JButton skipFileButton;
	private JLabel progressLabel;
	private JLabel timerLabel;
	private long startTimeMs = 0;

	private JComboBox searchOption;
	private JButton exportButton;
	private JButton closeButton;
	private JComboBox queryPropertyOption;
	private JPanel verificationOptionsPanel;
	private CustomJSpinner numberOfExtraTokensInNet;
	private JCheckBox keepQueryCapacity;
	private JComboBox symmetryOption;
	private JCheckBox noTimeoutCheckbox;
	private CustomJSpinner timeoutValue;
	private Timer timeoutTimer = new Timer(30000, new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			timeoutCurrentVerificationTask();
		}
	});

	private BatchProcessingResultsTableModel tableModel;

	private List<File> files = new ArrayList<File>();
	private BatchProcessingWorker currentWorker;

	private Timer timer = new Timer(1000, new AbstractAction() {
		private static final long serialVersionUID = 1327695063762640628L;

		public void actionPerformed(ActionEvent e) {
			timerLabel.setText((System.currentTimeMillis() - startTimeMs)
					/ 1000 + " s");
		}
	});

	public BatchProcessingDialog(Frame frame, String title, boolean modal) {
		super(frame, title, modal);
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				terminateBatchProcessing();
			}
		});

		initComponents();
	}

	private void initComponents() {
		setLayout(new FlowLayout());
		
		topPanel = new JPanel(new GridBagLayout());
		bottomPanel = new JPanel(new GridBagLayout());
		
		initFileListPanel();
		initVerificationOptionsPanel();
		initMonitorPanel();
		initResultTablePanel();
		
		splitpane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, bottomPanel);		
		splitpane.setResizeWeight(0);
		splitpane.setDividerSize(10);
		splitpane.setContinuousLayout(true);
		setContentPane(splitpane);
	}

	private void initFileListPanel() {
		JPanel fileListPanel = new JPanel(new GridBagLayout());
		fileListPanel.setBorder(BorderFactory.createTitledBorder("Models"));

		listModel = new DefaultListModel();
		fileList = new JList(listModel);
		fileList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		fileList.setSelectedIndex(0);
		fileList.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					removeSelectedFiles();
				}
			}
		});

		fileList.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {
				if (!(e.getValueIsAdjusting())) {
					if (fileList.getSelectedIndex() == -1) {
						removeFileButton.setEnabled(false);
					} else {
						removeFileButton.setEnabled(true);
					}
				}
			}
		});

		fileList.setCellRenderer(new FileNameCellRenderer());

		JScrollPane scrollpane = new JScrollPane(fileList);
		scrollpane.setMinimumSize(new Dimension(175, 225));
		scrollpane.setPreferredSize(new Dimension(175, 225));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 3;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.insets = new Insets(0, 0, 10, 0);
		fileListPanel.add(scrollpane, gbc);

		filesButtonsPanel = new JPanel(new GridBagLayout());
		
		addFilesButton = new JButton("Add models");
		addFilesButton.setToolTipText(TOOL_TIP_AddFilesButton);
		addFilesButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				addFiles();
			}
		});

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(0, 0, 0, 5);
		filesButtonsPanel.add(addFilesButton, gbc);
		
		removeFileButton = new JButton("Remove models");
		removeFileButton.setToolTipText(TOOL_TIP_RemoveFilesButton);
		removeFileButton.setEnabled(false);
		removeFileButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				removeSelectedFiles();
			}
		});
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 0, 5);
		filesButtonsPanel.add(removeFileButton, gbc);
		
		clearFilesButton = new JButton("Clear");
		clearFilesButton.setToolTipText(TOOL_TIP_ClearFilesButton);
		clearFilesButton.setEnabled(false);
		clearFilesButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				clearFiles();
				enableButtons();
			}
		});

		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		filesButtonsPanel.add(clearFilesButton, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTHEAST;
		fileListPanel.add(filesButtonsPanel, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.NORTHEAST;
		gbc.gridheight = 2;
		gbc.insets = new Insets(10, 5, 0, 5);
		topPanel.add(fileListPanel, gbc);
	}

	private void addFiles() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"TAPAAL models", new String[] { "xml" });
		fileChooser.setFileFilter(filter);
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setFileFilter(filter);
		// JS: added definition of filter in order to fix mac vs. ubuntu filter
		// issues
		fileChooser.setMultiSelectionEnabled(true);
		if (lastPath != null) {
			File path = new File(lastPath);
			if (path.exists()) {
				fileChooser.setCurrentDirectory(path);
			}
		}

		int result = fileChooser.showOpenDialog(BatchProcessingDialog.this);
		if (result == JFileChooser.APPROVE_OPTION) {
			File[] filesArray = fileChooser.getSelectedFiles();
			for (File file : filesArray) {
				lastPath = file.getParent();
				if (!files.contains(file)) {
					files.add(file);
					listModel.addElement(file);
				}
			}

			enableButtons();

		}
	}

	private void removeSelectedFiles() {
		for (Object o : fileList.getSelectedValues()) {
			File file = (File) o;
			files.remove(file);
			listModel.removeElement(file);
		}

		enableButtons();
	}
	
	private void initVerificationOptionsPanel() {
		verificationOptionsPanel = new JPanel(new GridBagLayout());
		verificationOptionsPanel.setBorder(BorderFactory
				.createTitledBorder("Override Verification Options for the Batch"));
		
		initQueryPropertyOptionsComponents();
		initSearchOptionsComponents();
		initSymmetryOptionsComponents();
		initReductionOptionsComponents();
		initCapacityComponents();
		initTimeoutComponents();
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHEAST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weighty = 0;
		gbc.weightx = 0;
		gbc.insets = new Insets(10, 0, 0, 5);
		topPanel.add(verificationOptionsPanel, gbc);
	}

	private void initQueryPropertyOptionsComponents() {
		JLabel queryLabel = new JLabel("Query:");
		queryLabel.setToolTipText(TOOL_TIP_QueryLabel);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(0, 0, 5, 0);
		gbc.anchor = GridBagConstraints.WEST;
		verificationOptionsPanel.add(queryLabel, gbc);

		String[] options = new String[] { name_KeepQueryOption,
				name_SEARCHWHOLESTATESPACE };
		queryPropertyOption = new JComboBox(options);
		queryPropertyOption.setToolTipText(TOOL_TIP_Query_Property_Option);
		
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(0, 0, 5, 0);
		verificationOptionsPanel.add(queryPropertyOption, gbc);
	}

	private void initCapacityComponents() {
		JLabel capacityLabel = new JLabel("Extra tokens:");
		capacityLabel.setToolTipText(TOOL_TIP_CapacityLabel);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.insets = new Insets(0, 0, 5, 0);
		gbc.anchor = GridBagConstraints.WEST;
		verificationOptionsPanel.add(capacityLabel, gbc);

		numberOfExtraTokensInNet = new CustomJSpinner(3);
		numberOfExtraTokensInNet.setMaximumSize(new Dimension(70, 30));
		numberOfExtraTokensInNet.setMinimumSize(new Dimension(70, 30));
		numberOfExtraTokensInNet.setPreferredSize(new Dimension(70, 30));
		numberOfExtraTokensInNet.setEnabled(false);
		numberOfExtraTokensInNet.setToolTipText(TOOL_TIP_Number_Of_Extra_Tokens);

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 5;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 5, 10);
		verificationOptionsPanel.add(numberOfExtraTokensInNet, gbc);
	
		keepQueryCapacity = new JCheckBox(name_KeepQueryOption);
		keepQueryCapacity.setToolTipText(TOOL_TIP_KeepQueryCapacity);
		keepQueryCapacity.setSelected(true);
		keepQueryCapacity.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (keepQueryCapacity.isSelected())
					numberOfExtraTokensInNet.setEnabled(false);
				else
					numberOfExtraTokensInNet.setEnabled(true);
			}
		});

		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 5;
		gbc.insets = new Insets(0, 0, 5, 0);
		gbc.anchor = GridBagConstraints.WEST;
		verificationOptionsPanel.add(keepQueryCapacity, gbc);
	}
	
	private void initTimeoutComponents() {
		JLabel timeoutLabel = new JLabel("Timeout (in seconds): ");
		timeoutLabel.setToolTipText(TOOL_TIP_TimeoutLabel);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 6;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 5, 0);
		verificationOptionsPanel.add(timeoutLabel, gbc);

//		timeoutValue = new JSpinner(new SpinnerNumberModel(30, 5,
//				Integer.MAX_VALUE, 1));
		timeoutValue = new CustomJSpinner(30, 5,Integer.MAX_VALUE);
		timeoutValue.setToolTipText(TOOL_TIP_TimeoutValue);
		timeoutValue.setMaximumSize(new Dimension(70, 30));
		timeoutValue.setMinimumSize(new Dimension(70, 30));
		timeoutValue.setPreferredSize(new Dimension(70, 30));
		timeoutValue.setEnabled(true);

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 6;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 5, 10);
		verificationOptionsPanel.add(timeoutValue, gbc);

		noTimeoutCheckbox = new JCheckBox("Do not use timeout");
		noTimeoutCheckbox.setToolTipText(TOOL_TIP_NoTimeoutCheckBox);
		noTimeoutCheckbox.setSelected(false);
		noTimeoutCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (noTimeoutCheckbox.isSelected())
					timeoutValue.setEnabled(false);
				else
					timeoutValue.setEnabled(true);
			}
		});

		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 6;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 5, 0);
		verificationOptionsPanel.add(noTimeoutCheckbox, gbc);
	}

	private void initReductionOptionsComponents() {
		JLabel reductionLabel = new JLabel("Verification method:");
		reductionLabel.setToolTipText(TOOL_TIP_ReductionLabel);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 5, 0);
		verificationOptionsPanel.add(reductionLabel, gbc);
		
		reductionOptionChooser = new ReductionOptionChooser();
		
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridy = 4;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 5, 0);
		gbc.gridwidth = 2;
		verificationOptionsPanel.add(reductionOptionChooser, gbc);
	}
	
	private void initSymmetryOptionsComponents() {
		JLabel symmetryLabel = new JLabel("Symmetry:");
		symmetryLabel.setToolTipText(TOOL_TIP_SymmetryLabel);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.insets = new Insets(0, 0, 5, 0);
		gbc.anchor = GridBagConstraints.WEST;
		verificationOptionsPanel.add(symmetryLabel, gbc);

		String[] options = new String[] { name_KeepQueryOption, name_SYMMETRY,
				name_NOSYMMETRY };
		symmetryOption = new JComboBox(options);
		symmetryOption.setToolTipText(TOOL_TIP_SymmetryOption);

		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 5, 0);
		gbc.gridwidth = 2;
		verificationOptionsPanel.add(symmetryOption, gbc);
	}

	private SearchOption getSearchOption() {
		if (((String) searchOption.getSelectedItem()).equals(name_DFS))
			return SearchOption.DFS;
		else if (((String) searchOption.getSelectedItem()).equals(name_Random))
			return SearchOption.RANDOM;
		else if (((String) searchOption.getSelectedItem())
				.equals(name_HEURISTIC))
			return SearchOption.HEURISTIC;
		else if (((String) searchOption.getSelectedItem()).equals(name_BFS))
			return SearchOption.BFS;
		else
			return SearchOption.BatchProcessingKeepQueryOption;
	}

	private void disableVerificationOptionsButtons() {
		verificationOptionsPanel.setEnabled(false);
		for (Component c : verificationOptionsPanel.getComponents())
			c.setEnabled(false);
	}

	private void enabledVerificationOptionButtons() {
		verificationOptionsPanel.setEnabled(true);
		for (Component c : verificationOptionsPanel.getComponents())
			c.setEnabled(true);

		numberOfExtraTokensInNet.setEnabled(!keepQueryCapacity.isSelected());
		timeoutValue.setEnabled(useTimeout());
	}
	
	private BatchProcessingVerificationOptions getVerificationOptions() {
		ReductionOption reductionOption = reductionOptionChooser.isOverwriten() ? ReductionOption.BatchProcessingUserDefinedReductions : ReductionOption.BatchProcessingKeepQueryOption;
		
		return new BatchProcessingVerificationOptions(getQueryPropertyOption(),
				keepQueryCapacity.isSelected(), getNumberOfExtraTokens(),
				getSearchOption(), getSymmetryOption(), reductionOption,
				reductionOptionChooser.isDiscreteInclusion(), reductionOptionChooser.getChoosenOptions());
	}

	private int getNumberOfExtraTokens() {
		return (Integer) numberOfExtraTokensInNet.getValue();
	}

	private SymmetryOption getSymmetryOption() {
		String symmetryString = (String) symmetryOption.getSelectedItem();
		if (symmetryString.equals(name_SYMMETRY))
			return SymmetryOption.Yes;
		else if (symmetryString.equals(name_NOSYMMETRY))
			return SymmetryOption.No;
		else
			return SymmetryOption.KeepQueryOption;
	}

	private QueryPropertyOption getQueryPropertyOption() {
		String propertyOptionString = (String) queryPropertyOption
				.getSelectedItem();
		if (propertyOptionString.equals(name_SEARCHWHOLESTATESPACE))
			return QueryPropertyOption.SearchWholeStateSpace;
		else
			return QueryPropertyOption.KeepQueryOption;
	}

	private void initSearchOptionsComponents() {
		JLabel searchLabel = new JLabel("Search order:");
		searchLabel.setToolTipText(TOOL_TIP_SearchLabel);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 5, 0);
		verificationOptionsPanel.add(searchLabel, gbc);

		String[] options = new String[] { name_KeepQueryOption, name_HEURISTIC,
				name_BFS, name_DFS, name_Random };
		searchOption = new JComboBox(options);
		searchOption.setToolTipText(TOOL_TIP_SearchOption);

		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 5, 0);
		gbc.gridwidth = 2;
		verificationOptionsPanel.add(searchOption, gbc);
	}
	
	private void exit() {
		rootPane.getParent().setVisible(false);
	}

	private void initResultTablePanel() {
		JPanel resultTablePanel = new JPanel(new GridBagLayout());
		resultTablePanel.setBorder(BorderFactory.createTitledBorder("Results"));

		exportButton = new JButton("Export as spreadsheet");
		exportButton.setToolTipText(TOOL_TIP_ExportButton);
		exportButton.setEnabled(false);
		exportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportResults();
			}

			private void exportResults() {
				String filename = new FileBrowser("CSV file", "csv", "")
						.saveFile();
				if (filename != null) {
					File exportFile = new File(filename);
					BatchProcessingResultsExporter exporter = new BatchProcessingResultsExporter();
					try {
						exporter.exportToCSV(tableModel.getResults(),
								exportFile);
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(
								CreateGui.getApp(),
								"An error occurred while trying to export the results. Please try again",
								"Error Exporting Results",
								JOptionPane.ERROR_MESSAGE);
						e1.printStackTrace();
					}
				}
			}
		});
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.insets = new Insets(5, 0, 0, 0);
		gbc.anchor = GridBagConstraints.SOUTHWEST;
		resultTablePanel.add(exportButton, gbc);
		
		closeButton = new JButton("Close");
		closeButton.setToolTipText(TOOL_TIP_CloseButton);
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exit();
				
			}
		});
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.insets = new Insets(5, 0, 0, 0);
		gbc.anchor = GridBagConstraints.SOUTHEAST;
		resultTablePanel.add(closeButton, gbc);

		tableModel = new BatchProcessingResultsTableModel();
		final JTable table = new JTable(tableModel) {
			private static final long serialVersionUID = -146530769055564619L;

			public javax.swing.JToolTip createToolTip() {
				ToolTipManager.sharedInstance().setDismissDelay(
						Integer.MAX_VALUE); // disable tooltips disappearing
				ToolTipManager.sharedInstance().setInitialDelay(200);
				return new MultiLineAutoWrappingToolTip();
			};
		};
		ResultTableCellRenderer renderer = new ResultTableCellRenderer(true);
		table.getColumnModel().getColumn(0).setMinWidth(70);
		table.getColumnModel().getColumn(0).setPreferredWidth(70);
		table.getColumnModel().getColumn(0).setMaxWidth(85);
		table.getColumn("Method").setCellRenderer(renderer);
		table.getColumn("Model").setCellRenderer(renderer);
		table.getColumn("Query").setCellRenderer(renderer);
		table.getColumn("Result").setCellRenderer(renderer);
		table.getColumn("Verification Time").setCellRenderer(renderer);

		tableModel.addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent e) {
				if (e.getType() == TableModelEvent.INSERT) {
					table.scrollRectToVisible(table.getCellRect(e.getLastRow(),
							e.getLastRow(), true));
				}
			}
		});

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		Dimension scrollPanePrefDims = new Dimension(850, 250);
		//Set the minimum size to 150 lets than the preferred, to be consistat with theh minimum size of the window
		Dimension scrollPaneMinDims = new Dimension(850, 250-150);
		scrollPane.setMinimumSize(scrollPaneMinDims);
		scrollPane.setPreferredSize(scrollPanePrefDims);
		
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		resultTablePanel.add(scrollPane, gbc);

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.insets = new Insets(0, 5, 5, 5);
		bottomPanel.add(resultTablePanel, gbc);
	}
	
	private void initMonitorPanel() {
		monitorPanel = new JPanel(new GridBagLayout());
		monitorPanel.setBorder(BorderFactory.createTitledBorder("Monitor"));

		JLabel file = new JLabel("Net:");
		file.setToolTipText(TOOL_TIP_FileLabel);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		monitorPanel.add(file, gbc);

		fileStatusLabel = new JLabel("");
		Dimension fileStatusLabelDim = new Dimension(250, 25);
		fileStatusLabel.setPreferredSize(fileStatusLabelDim);
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 3;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		monitorPanel.add(fileStatusLabel, gbc);

		JLabel status = new JLabel("Query:");
		status.setToolTipText(TOOL_TIP_StatusLabel);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		monitorPanel.add(status, gbc);

		statusLabel = new JLabel("");
		Dimension statusLabelDim = new Dimension(250, 25);
		statusLabel.setPreferredSize(statusLabelDim);
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 3;
		gbc.weightx = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		monitorPanel.add(statusLabel, gbc);

		JLabel progress = new JLabel("Progress: ");
		progress.setToolTipText(TOOL_TIP_ProgressLabel);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.WEST;
		monitorPanel.add(progress, gbc);

		progressLabel = new JLabel("");
		Dimension dim = new Dimension(210, 25);
		progressLabel.setPreferredSize(dim);
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 0, 10);
		monitorPanel.add(progressLabel, gbc);

		JLabel time = new JLabel("Time: ");
		time.setToolTipText(TOOL_TIP_TimeLabel);
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 5, 0, 0);
		monitorPanel.add(time, gbc);

		timerLabel = new JLabel("");
		Dimension timerLabelDim = new Dimension(50, 25);
		timerLabel.setMinimumSize(timerLabelDim);
		timerLabel.setPreferredSize(timerLabelDim);
		gbc = new GridBagConstraints();
		gbc.gridx = 3;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 5, 0, 5);
		monitorPanel.add(timerLabel, gbc);

		startButton = new JButton("Start");
		startButton.setToolTipText(TOOL_TIP_StartButton);
		startButton.setMaximumSize(new java.awt.Dimension(85, 25));
		startButton.setMinimumSize(new java.awt.Dimension(85, 25));
		startButton.setPreferredSize(new java.awt.Dimension(85, 25));

		startButton.setEnabled(false);
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				process();
			}
		});
		gbc = new GridBagConstraints();
		gbc.gridx = 4;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 0, 10);
		monitorPanel.add(startButton, gbc);

		cancelButton = new JButton("Cancel");
		cancelButton.setToolTipText(TOOL_TIP_CancelButton);
		cancelButton.setMaximumSize(new java.awt.Dimension(85, 25));
		cancelButton.setMinimumSize(new java.awt.Dimension(85, 25));
		cancelButton.setPreferredSize(new java.awt.Dimension(85, 25));
		
		cancelButton.setEnabled(false);
		cancelButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				terminateBatchProcessing();
				fileStatusLabel.setText("");
				statusLabel.setText("Batch processing cancelled");
				enableButtons();
			}
		});
		gbc = new GridBagConstraints();
		gbc.gridx = 4;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 0, 10);
		monitorPanel.add(cancelButton, gbc);

		skipFileButton = new JButton("Skip");
		skipFileButton.setToolTipText(TOOL_TIP_SkipFileButton);
		skipFileButton.setMaximumSize(new java.awt.Dimension(85, 25));
		skipFileButton.setMinimumSize(new java.awt.Dimension(85, 25));
		skipFileButton.setPreferredSize(new java.awt.Dimension(85, 25));
		
		skipFileButton.setEnabled(false);
		skipFileButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				skipCurrentFile();
			}
		});
		gbc = new GridBagConstraints();
		gbc.gridx = 4;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 0, 10);
		monitorPanel.add(skipFileButton, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.weightx = 0;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(10, 0, 0, 5);
		
		topPanel.add(monitorPanel, gbc);
	}

	private void process() {
		tableModel.clear();
		currentWorker = new BatchProcessingWorker(files, tableModel,
				getVerificationOptions());
		currentWorker.addPropertyChangeListener(new PropertyChangeListener() {

			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals("state")) {
					if ((StateValue) evt.getNewValue() == StateValue.DONE) {
						enableButtons();
						cancelButton.setEnabled(false);
						skipFileButton.setEnabled(false);
						timerLabel.setText("");
						timer.stop();
						timeoutTimer.stop();
					} else if ((StateValue) evt.getNewValue() == StateValue.STARTED) {
						disableButtonsDuringProcessing();
						cancelButton.setEnabled(true);
						skipFileButton.setEnabled(true);
						timerLabel.setText("");
						progressLabel.setText("0 verification tasks completed");
					}
				}
			}
		});
		currentWorker.addBatchProcessingListener(new BatchProcessingListener() {
			public void fireVerificationTaskStarted() {
				if (timer.isRunning())
					timer.restart();
				else
					timer.start();

				if (useTimeout()) {
					if (timeoutTimer.isRunning())
						timeoutTimer.restart();
					else
						timeoutTimer.start();
				}

				startTimeMs = System.currentTimeMillis();
			}

			public void fireVerificationTaskComplete(
					VerificationTaskCompleteEvent e) {
				if (timer.isRunning())
					timer.stop();
				if (timeoutTimer.isRunning())
					timeoutTimer.stop();
				int tasksCompleted = e.verificationTasksCompleted();
				progressLabel.setText(e.verificationTasksCompleted()
						+ " verification task"
						+ (tasksCompleted > 1 ? "s" : "") + " completed");
				timerLabel.setText("");
			}

			public void fireStatusChanged(StatusChangedEvent e) {
				statusLabel.setText(e.status());
			}

			public void fireFileChanged(FileChangedEvent e) {
				fileStatusLabel.setText(e.fileName());
			}

		});

		if (useTimeout())
			setupTimeoutTimer();

		currentWorker.execute();
	}

	private void setupTimeoutTimer() {
		int timeout = (Integer) timeoutValue.getValue();
		timeout = timeout * 1000;
		timeoutTimer.setInitialDelay(timeout);
		timeoutTimer.setDelay(timeout);
		timeoutTimer.setRepeats(false);
	}

	private boolean useTimeout() {
		return !noTimeoutCheckbox.isSelected();
	}

	private void terminateBatchProcessing() {
		if (currentWorker != null && !currentWorker.isDone()) {
			boolean cancelled = false;
			do {
				currentWorker.notifyExiting();
				cancelled = currentWorker.cancel(true);
			} while (!cancelled);
			System.out.print("Batch processing terminated");
		}
	}

	private void skipCurrentFile() {
		if (currentWorker != null && !currentWorker.isDone()) {
			currentWorker.notifySkipCurrentVerification();
		}
	}

	private void timeoutCurrentVerificationTask() {
		if (currentWorker != null && !currentWorker.isDone()) {
			currentWorker.notifyTimeoutCurrentVerificationTask();
		}
	}

	private void clearFiles() {
		files.clear();
		listModel.removeAllElements();
	}

	private void disableButtonsDuringProcessing() {
		addFilesButton.setEnabled(false);
		removeFileButton.setEnabled(false);
		clearFilesButton.setEnabled(false);
		startButton.setEnabled(false);
		exportButton.setEnabled(false);
		fileList.setEnabled(false);

		disableVerificationOptionsButtons();
	}

	private void enableButtons() {
		fileList.setEnabled(true);
		addFilesButton.setEnabled(true);

		if (listModel.size() > 0) {
			clearFilesButton.setEnabled(true);
			startButton.setEnabled(true);
		} else {
			clearFilesButton.setEnabled(false);
			startButton.setEnabled(false);
		}

		if (tableModel.getRowCount() > 0)
			exportButton.setEnabled(true);
		else
			exportButton.setEnabled(false);

		enabledVerificationOptionButtons();
	}

	// Custom cell renderer for the file list to only display the name of the
	// file
	// instead of the whole path.
	private class FileNameCellRenderer extends JLabel implements
			ListCellRenderer {
		private static final long serialVersionUID = 3071924451912979500L;

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			if (value instanceof File)
				setText(((File) value).getName());
			else
				setText(value.toString());
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			setEnabled(list.isEnabled());
			setFont(list.getFont());
			setOpaque(true);
			return this;
		}
	}

	// Custom cell renderer for the Query Column of the result table display the
	// property of the query
	private class ResultTableCellRenderer extends JLabel implements
			TableCellRenderer {
		private static final long serialVersionUID = 3054497986242852099L;
		Border unselectedBorder = null;
		Border selectedBorder = null;
		boolean isBordered = true;

		public ResultTableCellRenderer(boolean isBordered) {
			this.isBordered = isBordered;
		}

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			if (isBordered) {
				if (isSelected) {
					setBackground(table.getSelectionBackground());
					setForeground(table.getSelectionForeground());

					if (selectedBorder == null) {
						selectedBorder = BorderFactory.createMatteBorder(2, 5,
								2, 5, table.getSelectionBackground());
					}
					setBorder(selectedBorder);
				} else {
					boolean isResultColumn = table.getColumnName(column)
							.equals("Result");
					boolean isQueryColumn = table.getColumnName(column).equals(
							"Query");
					if (value != null) {
						if ((isResultColumn && value.toString().equals(
								"Satisfied"))
								|| (isQueryColumn && value.toString().equals(
										"TRUE")))
							setBackground(new Color(91, 255, 91)); // light red
						else if ((isResultColumn && value.toString().equals(
								"Not Satisfied"))
								|| (isQueryColumn && value.toString().equals(
										"FALSE")))
							setBackground(new Color(255, 91, 91)); // light
																	// green
						else
							setBackground(table.getBackground());
					} else {
						setBackground(table.getBackground());
					}
					setForeground(table.getForeground());
					if (unselectedBorder == null) {
						unselectedBorder = BorderFactory.createMatteBorder(2,
								5, 2, 5, table.getBackground());
					}
					setBorder(unselectedBorder);
				}
			}

			setEnabled(table.isEnabled());
			setFont(table.getFont());
			setOpaque(true);

			if (value != null) {
				if (value instanceof TAPNQuery) {
					TAPNQuery newQuery = (TAPNQuery) value;

					setToolTipText(generateTooltipTextFromQuery(newQuery));
					setText(newQuery.getName());
				} else if (table.getColumnName(column).equals(
						"Verification Time")
						|| table.getColumnName(column).equals("Method")) {
					setText(value.toString());
					Point mousePos = table.getMousePosition();
					BatchProcessingVerificationResult result = null;
					if (mousePos != null) {
						result = ((BatchProcessingResultsTableModel) table
								.getModel()).getResult(table
								.rowAtPoint(mousePos));
					}

					if (table.getColumnName(column).equals("Verification Time"))
						setToolTipText(result != null ? generateStatsToolTipText(result)
								: value.toString());
					else
						setToolTipText(result != null ? generateReductionString(result
								.query()) : value.toString());
				} else {
					setToolTipText(value.toString());
					setText(value.toString());
				}
			} else {
				setToolTipText("");
				setText("");
			}

			return this;
		}

		private String generateStatsToolTipText(
				BatchProcessingVerificationResult result) {
			StringBuilder s = new StringBuilder();
			s.append("Verification Time: ");
			s.append((result.verificationTimeInMs() / 1000.0));
			s.append(" s");
			if (result.hasStats()) {
				s.append(System.getProperty("line.separator"));
				s.append(System.getProperty("line.separator"));
				s.append(result.stats().toString());
			}

			return s.toString();
		}

		private String generateTooltipTextFromQuery(TAPNQuery query) {
			StringBuilder s = new StringBuilder();
			s.append("Extra Tokens: ");
			s.append(query.getCapacity());
			s.append("\n\n");

			s.append("Search Method: \n");
			if (query.getSearchOption() == SearchOption.DFS)
				s.append(name_DFS);
			else if (query.getSearchOption() == SearchOption.RANDOM)
				s.append(name_Random);
			else if (query.getSearchOption() == SearchOption.HEURISTIC)
				s.append(name_HEURISTIC);
			else
				s.append(name_BFS);
			s.append("\n\n");

			s.append(generateReductionString(query));

			s.append("\n\n");
			s.append("Symmetry: ");
			s.append(query.useSymmetry() ? "Yes\n\n" : "No\n\n");

			s.append("Query Property:\n");
			if (query.getProperty().toString().equals("AG P0>=0"))
				s.append(name_SEARCHWHOLESTATESPACE);
			else
				s.append(query.getProperty().toString());

			return s.toString();
		}

		private String generateReductionString(TAPNQuery query) {
			StringBuilder s = new StringBuilder();
			if (query != null) {
				s.append("Reduction: \n");
				if (query.getReductionOption() == ReductionOption.STANDARD)
					s.append(name_STANDARD);
				else if (query.getReductionOption() == ReductionOption.OPTIMIZEDSTANDARD)
					s.append(name_OPTIMIZEDSTANDARD);
				else if (query.getReductionOption() == ReductionOption.BROADCAST)
					s.append(name_BROADCAST);
				else if (query.getReductionOption() == ReductionOption.DEGREE2BROADCAST)
					s.append(name_BROADCASTDEG2);
				else if (query.getReductionOption() == ReductionOption.VerifyTAPN) {
					s.append(name_verifyTAPN);
					s.append("\n\n");
					s.append("Discrete Inclusion: ");
					s.append(query.discreteInclusion() ? "Yes" : "No");
					if (query.discreteInclusion()) {
						s.append("\n\n");
						s.append("Discrete Inclusion Places:\n");
						s.append(generateListOfInclusionPlaces(query));
					}
				} else {
					s.append(name_BROADCAST);
				}
			}
			return s.toString();
		}

		private String generateListOfInclusionPlaces(TAPNQuery query) {
			if (query.inclusionPlaces().inclusionOption() == InclusionPlacesOption.AllPlaces)
				return "*ALL*";
			List<TimedPlace> incPlace = query.inclusionPlaces()
					.inclusionPlaces();
			if (incPlace.isEmpty())
				return "*NONE*";
			StringBuilder s = new StringBuilder();
			boolean first = true;
			for (TimedPlace p : incPlace) {
				if (!first)
					s.append(", ");

				s.append(p.toString());
				if (first)
					first = false;
			}
			return s.toString();
		}
	}
	
	public class ReductionOptionChooser extends JPanel{
		private static final long serialVersionUID = 4423387072826316392L;
		
		private JButton chooseReductionOptions;
		ReductionOptionDialog reductionOptionDialog;
		
		private static final String STATUS_TEXT_USERDEF = "Overridden";
		private static final String STATUS_TEXT_DONT_OVERRIDE = "From the query";
		
		public ReductionOptionChooser() {
			super(new GridLayout(1, 0));
			
			reductionOptionDialog = new ReductionOptionDialog(BatchProcessingDialog.this, "Choose Verification Methods for Batch Processing", true);
			reductionOptionDialog.setLocationRelativeTo(BatchProcessingDialog.this);
			reductionOptionDialog.setResizable(false);
			reductionOptionDialog.pack();
			
			chooseReductionOptions = new JButton(STATUS_TEXT_DONT_OVERRIDE);
			chooseReductionOptions.setToolTipText(TOOL_TIP_ReductionOption);
			chooseReductionOptions.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					//reductionOptionDialog.setOverride(true);
					reductionOptionDialog.setVisible(true);
				}
			});
			this.add(chooseReductionOptions);

		}
		
		public List<ReductionOption> getChoosenOptions(){
			return reductionOptionDialog.getChoosenOptions();
		}
		
		public boolean isOverwriten(){
			return reductionOptionDialog.isOverwriten();
		}
		
		public boolean isDiscreteInclusion(){
			return reductionOptionDialog.isDiscreteInclusion();
		}
		
		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
			for(Component c : getComponents()){
				c.setEnabled(enabled);
			}
		}
	}
	
	public class ReductionOptionDialog extends EscapableDialog{
		private static final long serialVersionUID = 5554793741619572092L;
		private static final String TEXT_DONT_OVERRIDE = "Do not override the verification method";
		private static final String TEXT_OVERRIDE = "Override the verification method";
		
		
		private JRadioButton dontOverride;
		private JRadioButton override;
		private JCheckBox verifyTAPN;
		private JCheckBox verifyTAPNDiscreteInclusion;
		private JCheckBox STANDARD;
		private JCheckBox OPTIMIZEDSTANDARD;
		private JCheckBox BROADCAST;
		private JCheckBox BROADCASTDEG2;
		private JCheckBox verifyTAPNDiscreteVerification;
		
		JButton selectAll;
		JButton deselectAll;
		
		private JPanel content;
		
		private JPanel leftPanel;
		private JPanel rightPanel;
		
		public ReductionOptionDialog(JDialog dialog, String string, boolean modal) {
			super(dialog, string, modal);
			
			initComponents();
		}
		
		private void initComponents(){
			content = new JPanel(new GridBagLayout());
			
			initLeftPanel();
			initRightPanel();
			
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.anchor = GridBagConstraints.NORTHWEST;
			content.add(leftPanel, gbc);
			
			gbc = new GridBagConstraints();
			gbc.gridx = 1;
			gbc.gridy = 0;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.anchor = GridBagConstraints.NORTHWEST;
			
			JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
			//sep.setSize(10, sep.getSize().height);
			content.add(sep, gbc);
			
			gbc = new GridBagConstraints();
			gbc.gridx = 2;
			gbc.gridy = 0;
			gbc.anchor = GridBagConstraints.NORTHWEST;
			content.add(rightPanel);
			
			content.setBorder(BorderFactory.createTitledBorder("Verification Method"));
			
			JButton closeButton = new JButton("Save");
			rootPane.setDefaultButton(closeButton);
			closeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					ReductionOptionDialog.this.setVisible(false);
					
				}
			});
			
			this.getContentPane().setLayout(new GridBagLayout());

			gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			this.getContentPane().add(content, gbc);
			gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.anchor = GridBagConstraints.EAST;
			this.getContentPane().add(closeButton, gbc);
			
		}
		
		private void initRightPanel() {
			rightPanel = new JPanel(new GridBagLayout());
			
			verifyTAPN = new JCheckBox(name_verifyTAPNWithLegend);
			//verifyTAPN.setMnemonic('A');
			verifyTAPN.setEnabled(false);
			
			verifyTAPNDiscreteInclusion = new JCheckBox(name_verifyTAPNDiscreteInclusionWithLegend);
			//verifyTAPNDiscreteInclusion.setMnemonic('B');
			verifyTAPNDiscreteInclusion.setEnabled(false);
			
			STANDARD = new JCheckBox(name_STANDARDWithLegend);
			//STANDARD.setMnemonic('C');
			STANDARD.setEnabled(false);
			
			OPTIMIZEDSTANDARD = new JCheckBox(name_OPTIMIZEDSTANDARDWithLegend);
			//OPTIMIZEDSTANDARD.setMnemonic('D');
			OPTIMIZEDSTANDARD.setEnabled(false);
			
			BROADCAST = new JCheckBox(name_BROADCASTWithLegend);
			//BROADCAST.setMnemonic('E');
			BROADCAST.setEnabled(false);
			
			BROADCASTDEG2 = new JCheckBox(name_BROADCASTDEG2WithLegend);
			//BROADCASTDEG2.setMnemonic('F');
			BROADCASTDEG2.setEnabled(false);
			
			verifyTAPNDiscreteVerification = new JCheckBox(name_verifyTAPNDiscreteVerificationWithLegend);
			verifyTAPNDiscreteVerification.setEnabled(false);
			
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.insets = new Insets(5, 5, 0, 5);
			gbc.anchor = GridBagConstraints.WEST;
			rightPanel.add(verifyTAPN, gbc);
			
			gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.insets = new Insets(0, 5, 0, 5);
			gbc.anchor = GridBagConstraints.WEST;
			rightPanel.add(verifyTAPNDiscreteInclusion, gbc);
			
			gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 2;
			gbc.insets = new Insets(0, 5, 0, 5);
			gbc.anchor = GridBagConstraints.WEST;
			rightPanel.add(verifyTAPNDiscreteVerification, gbc);
			
			gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 3;
			gbc.insets = new Insets(0, 5, 0, 5);
			gbc.anchor = GridBagConstraints.WEST;
			rightPanel.add(STANDARD, gbc);
			
			gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 4;
			gbc.insets = new Insets(0, 5, 0, 5);
			gbc.anchor = GridBagConstraints.WEST;
			rightPanel.add(OPTIMIZEDSTANDARD, gbc);
			
			gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 5;
			gbc.insets = new Insets(0, 5, 0, 5);
			gbc.anchor = GridBagConstraints.WEST;
			rightPanel.add(BROADCAST, gbc);
			
			gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 6;
			gbc.insets = new Insets(0, 5, 5	, 5);
			gbc.anchor = GridBagConstraints.WEST;
			rightPanel.add(BROADCASTDEG2, gbc);
		}

		private void initLeftPanel() {
			
			leftPanel = new JPanel(new GridLayout(0, 1));
			dontOverride = new JRadioButton(TEXT_DONT_OVERRIDE);
			dontOverride.setSelected(true);
			
			override = new JRadioButton(TEXT_OVERRIDE);
			override.setSelected(false);
			
			ButtonGroup group = new ButtonGroup();
			group.add(dontOverride);
			group.add(override);
			
			dontOverride.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent arg0) {
					toggle();
				}
			});
			
			selectAll = new JButton("Select all");
			selectAll.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					setAll(true);
				}
			});
			
			deselectAll = new JButton("Select none");
			deselectAll.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					setAll(false);
				}
			});
			
			selectAll.setEnabled(false);
			deselectAll.setEnabled(false);
			
			leftPanel.add(dontOverride);
			leftPanel.add(override);
			leftPanel.add(selectAll);
			leftPanel.add(deselectAll);
		}

		private void toggle(){
			if(dontOverride.isSelected()){
				reductionOptionChooser.chooseReductionOptions.setText(ReductionOptionChooser.STATUS_TEXT_DONT_OVERRIDE);
				verifyTAPN.setEnabled(false);
				verifyTAPNDiscreteInclusion.setEnabled(false);
				STANDARD.setEnabled(false);
				OPTIMIZEDSTANDARD.setEnabled(false);
				BROADCAST.setEnabled(false);
				BROADCASTDEG2.setEnabled(false);
				verifyTAPNDiscreteVerification.setEnabled(false);
				selectAll.setEnabled(false);
				deselectAll.setEnabled(false);
			} else {
				reductionOptionChooser.chooseReductionOptions.setText(ReductionOptionChooser.STATUS_TEXT_USERDEF);
				verifyTAPN.setEnabled(true);
				verifyTAPNDiscreteInclusion.setEnabled(true);
				STANDARD.setEnabled(true);
				OPTIMIZEDSTANDARD.setEnabled(true);
				BROADCAST.setEnabled(true);
				BROADCASTDEG2.setEnabled(true);
				verifyTAPNDiscreteVerification.setEnabled(true);
				selectAll.setEnabled(true);
				deselectAll.setEnabled(true);
			}
		}
		
		private void setAll(boolean selected){
			verifyTAPN.setSelected(selected);
			verifyTAPNDiscreteInclusion.setSelected(selected);
			STANDARD.setSelected(selected);
			OPTIMIZEDSTANDARD.setSelected(selected);
			BROADCAST.setSelected(selected);
			BROADCASTDEG2.setSelected(selected);
			verifyTAPNDiscreteVerification.setSelected(selected);
		}
		
		public List<ReductionOption> getChoosenOptions(){
			ArrayList<ReductionOption> result = new ArrayList<ReductionOption>();
			if(verifyTAPN.isSelected()){
				result.add(ReductionOption.VerifyTAPN);
			}
			if(verifyTAPNDiscreteVerification.isSelected()){
				result.add(ReductionOption.VerifyTAPNdiscreteVerification);
			}
			if(STANDARD.isSelected()){
				result.add(ReductionOption.STANDARD);
			}
			if(OPTIMIZEDSTANDARD.isSelected()){
				result.add(ReductionOption.OPTIMIZEDSTANDARD);
			}
			if(BROADCAST.isSelected()){
				result.add(ReductionOption.BROADCAST);
			}
			if(BROADCASTDEG2.isSelected()){
				result.add(ReductionOption.DEGREE2BROADCAST);
			}
			return result;
		}
		
		public boolean isOverwriten(){
			return !dontOverride.isSelected();
		}
		
		public boolean isDiscreteInclusion(){
			return verifyTAPNDiscreteInclusion.isSelected();
		}
		
		public void setOverride(boolean override){
			if(override){
				this.override.setSelected(true);
			} else {
				this.dontOverride.setSelected(true);			
			}
		}
	}
}


