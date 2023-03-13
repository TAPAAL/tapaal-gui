package net.tapaal.gui.petrinet.dialog;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.SwingWorker.StateValue;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import dk.aau.cs.verification.VerifyTAPN.VerifyDTAPN;
import dk.aau.cs.verification.VerifyTAPN.VerifyPN;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPN;
import net.tapaal.gui.petrinet.undo.AddFileBatchProcessingCommand;
import net.tapaal.gui.petrinet.undo.Command;
import net.tapaal.gui.petrinet.undo.RemoveFileBatchProcessingCommand;
import net.tapaal.gui.petrinet.verification.TAPNQuery;
import net.tapaal.gui.petrinet.verification.TAPNQuery.SearchOption;
import pipe.gui.FileFinder;
import pipe.gui.MessengerImpl;
import pipe.gui.TAPAALGUI;
import net.tapaal.swinghelpers.CustomJSpinner;
import pipe.gui.petrinet.undo.UndoManager;
import pipe.gui.swingcomponents.filebrowser.FileBrowser;
import net.tapaal.gui.petrinet.widgets.QueryPane;
import net.tapaal.gui.petrinet.verification.InclusionPlaces.InclusionPlacesOption;
import net.tapaal.gui.swingcomponents.MultiLineAutoWrappingToolTip;
import dk.aau.cs.io.batchProcessing.BatchProcessingResultsExporter;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.util.MemoryMonitor;
import dk.aau.cs.util.StringComparator;
import dk.aau.cs.verification.VerificationOptions;
import dk.aau.cs.verification.batchProcessing.BatchProcessingListener;
import dk.aau.cs.verification.batchProcessing.BatchProcessingVerificationOptions;
import dk.aau.cs.verification.batchProcessing.BatchProcessingVerificationResult;
import dk.aau.cs.verification.batchProcessing.BatchProcessingWorker;
import dk.aau.cs.verification.batchProcessing.FileChangedEvent;
import dk.aau.cs.verification.batchProcessing.StatusChangedEvent;
import dk.aau.cs.verification.batchProcessing.VerificationTaskCompleteEvent;

public class BatchProcessingDialog extends JDialog {
	private static final String name_verifyTAPN = "TAPAAL Continuous Engine (verifytapn)";
	private static final String name_verifyTAPNDiscreteVerificationTimeDartPTrie = "TAPAAL Discrete Engine w. Time Darts and PTrie";
	private static final String name_verifyTAPNDiscreteVerificationTimeDart = "TAPAAL Discrete Engine w. Time Darts";
	private static final String name_verifyTAPNDiscreteVerificationPTrie = "TAPAAL Discrete Engine w. PTries";
	private static final String name_verifyTAPNDiscreteVerificationNone = "TAPAAL Discrete Engine (verifydtapn) w. no Optimizations";
	private static final String name_COMBI = "UPPAAL: Optimized Broadcast Reduction";
	private static final String name_STANDARD = "UPPAAL: Standard Reduction";
	private static final String name_OPTIMIZEDSTANDARD = "UPPAAL: Optimised Standard Reduction";
	private static final String name_BROADCAST = "UPPAAL: Broadcast Reduction";
	private static final String name_BROADCASTDEG2 = "UPPAAL: Broadcast Degree 2 Reduction";
	private static final String name_UNTIMED = "TAPAAL Untimed CTL Engine (verifypn)";

	private static final String name_BFS = "Breadth first search";
	private static final String name_DFS = "Depth first search";
	private static final String name_HEURISTIC = "Heuristic search";
	private static final String name_Random = "Random search";
	private static final String name_KeepQueryOption = "Do not override";
	private static final String name_SEARCHWHOLESTATESPACE = "Search whole state space";
	private static final String name_SOUNDNESS = "Soundness";
	private static final String name_STRONGSOUNDNESS = "Strong Soundness";
	private static final String name_EXISTDEADLOCK = "Existence of a deadlock";
	private static final String name_NONE_APPROXIMATION = "None";
	private static final String name_OVER_APPROXIMATION = "Over-approximation";
	private static final String name_UNDER_APPROXIMATION = "Under-approximation";

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
	private final static String TOOL_TIP_Help = "See the options available for the specific engine";
    private final static String TOOL_TIP_DefaultOption = "Choose to keep the same verification methods as in the nets";
    private final static String TOOL_TIP_VerifyTAPNOption = "Choose to override the verification methods in the nets to verifyTAPN";
    private final static String TOOL_TIP_VerifyPNOption = "Choose to override the verification methods in the nets to verifyPN";
    private final static String TOOL_TIP_VerifyDTAPNOption = "Choose to override the verification methods in the nets to verifyDTAPN";
	private final static String TOOL_TIP_TimeoutLabel = null;
	private final static String TOOL_TIP_TimeoutValue = "Enter the timeout in seconds";
	private final static String TOOL_TIP_NoTimeoutCheckBox = "Choose whether to use timeout";
	private final static String TOOL_TIP_OOMLabel = null;
	private final static String TOOL_TIP_OOMValue = "<html>Enter the maximum amount of available memory to the verification.<br>Verification is skipped as soon as it is detected that this amount of memory is exceeded.</html>";
	private final static String TOOL_TIP_NoOOMCheckBox = "Choose whether to use memory restrictions";
	
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
	
	private final static String NOT_SATISFIED_STRING_STRONG_SOUNDNESS = "Not Strongly Sound";
	private final static String NOT_SATISFIED_STRING_SOUNDNESS = "Not Sound";
	private final static String NOT_SATISFIED_STRING = "Not Satisfied";
	private final static String SATISFIED_STRING = "Satisfied";
	private final static String SATISFIED_SOUNDNESS_STRING = "Sound";
	private final static String SATISFIED_STRONG_SOUNDNESS_STRING = "Strongly Sound";

	private static String lastPath = null;
	
    HelpDialog helpDialogTAPN;
    HelpDialog helpDialogPN;
    HelpDialog helpDialogDTAPN;

	private JSplitPane splitpane;
	private JPanel topPanel;
	private JPanel bottomPanel;
	private JPanel monitorPanel;
	
	private JPanel filesButtonsPanel;
	private JButton addFilesButton;
	private JButton clearFilesButton;
	private JButton removeFileButton;
	private JList<File> fileList;
	private DefaultListModel<File> listModel;

	private JLabel statusLabel;
	private JLabel fileStatusLabel;
	private JButton startButton;
	private JButton cancelButton;
	private JButton skipFileButton;
	private JLabel progressLabel;
	private JLabel timerLabel;
	private JLabel memory;
	private long startTimeMs = 0;

	private JButton exportButton;
	private JButton closeButton;
	private JComboBox<String> queryPropertyOption;
	private JPanel verificationOptionsPanel;
	private CustomJSpinner numberOfExtraTokensInNet;
	private JCheckBox keepQueryCapacity;
    private JRadioButton defaultYes;
    private JRadioButton defaultNo;
	private ButtonGroup defaultOptions;
    private JTextField optionsTAPN;
    private JCheckBox checkboxTAPN;
    private JButton helpTAPN;
    private JTextField optionsPN;
    private JCheckBox checkboxPN;
    private JButton helpPN;
    private JTextField optionsDTAPN;
    private JCheckBox checkboxDTAPN;
    private JButton helpDTAPN;
    private JCheckBox noTimeoutCheckbox;
	private JCheckBox noOOMCheckbox;
	private CustomJSpinner timeoutValue;
	private CustomJSpinner oomValue;
	private final JList<TAPNQuery> ListOfQueries;
	
	private final Timer timeoutTimer = new Timer(30000, e -> timeoutCurrentVerificationTask());

	private BatchProcessingResultsTableModel tableModel;

	private final List<File> files = new ArrayList<File>();
	private BatchProcessingWorker currentWorker;
	private final UndoManager undoManager = new UndoManager(null);
	
	private final Timer timer = new Timer(1000, new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			timerLabel.setText((System.currentTimeMillis() - startTimeMs) / 1000 + " s");
			memory.setText(peakMemory >= 0? peakMemory + " MB" : "N/A");
		}
	});
	
	private static int memoryTimerCount = 0;
	private static int memoryTimerMode = 0;
	private static int peakMemory = -1;
	
	private void startMemoryTimer(){
		if(memoryTimer.isRunning()){
			memoryTimer.stop();
		}
		memoryTimer.setDelay(50);
		memoryTimerCount = 0;
		memoryTimerMode = 0;
		peakMemory = -1;
		memoryTimer.start();
	}
	
	private void stopMemoryTimer(){
		if(memoryTimer.isRunning()){
			memoryTimer.stop();
		}
		MemoryMonitor.detach();
	}
	
	private final Timer memoryTimer = new Timer(50, new AbstractAction() {
	    public void actionPerformed(ActionEvent e) {
			if (MemoryMonitor.isAttached()) {
				MemoryMonitor.getUsage();
				peakMemory = MemoryMonitor.getPeakMemoryValue();
				
				if (useOOM() && MemoryMonitor.getPeakMemoryValue() > (Integer) oomValue.getValue()) {
					oomCurrentVerificationTask();
				}
			}
			if (memoryTimerMode == 0 && memoryTimerCount == 2) {
				memoryTimerCount = 0;
				memoryTimerMode++;
				memoryTimer.setDelay(100);
			} else if (memoryTimerMode == 1 && memoryTimerCount == 4) {
				memoryTimerCount = 0;
				memoryTimerMode++;
				memoryTimer.setDelay(200);
			} else if (memoryTimerMode == 2 && memoryTimerCount == 5) {
				memoryTimerCount = 0;
				memoryTimerMode++;
				memoryTimer.setDelay(1000);
			} else if (memoryTimerMode < 3) {
				memoryTimerCount++;
			}
		}
	});
	
	static BatchProcessingDialog batchProcessingDialog;
	
	/* ListOfQueries is used throughout the class to check if BatchProcessing was called from QueryPane
	(should maybe be boolean)
	*/
	public static void showBatchProcessingDialog(JList<TAPNQuery> ListOfQueries){
		if (ListOfQueries.getModel().getSize() != 0) {
			batchProcessingDialog = null;
		}
		if (batchProcessingDialog == null) {
			batchProcessingDialog = new BatchProcessingDialog(TAPAALGUI.getApp(), "Batch Processing", true, ListOfQueries);
			batchProcessingDialog.pack();
			batchProcessingDialog.setPreferredSize(batchProcessingDialog.getSize());
			//Set the minimum size to 150 less than the preferred, to be consistent with the minimum size of the result panel
			batchProcessingDialog.setMinimumSize(new Dimension(batchProcessingDialog.getWidth(), batchProcessingDialog.getHeight()-150));
			batchProcessingDialog.setLocationRelativeTo(null);
			batchProcessingDialog.setResizable(true);
		}
        batchProcessingDialog.toggleDefault();
		batchProcessingDialog.setVisible(true);
	}

	private BatchProcessingDialog(Frame frame, String title, boolean modal, JList<TAPNQuery> ListOfQueries) {
		super(frame, title, modal);
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				if (!(isQueryListEmpty())) {
					batchProcessingDialog = null;
				}
				terminateBatchProcessing();
			}
		});
		this.ListOfQueries = ListOfQueries;

		initComponents();
		makeShortcuts();
		//Runs the BatchProcessing if it is called from the QueryPane
		if (!(isQueryListEmpty())) {
			process();
		}
	}

	private void initComponents() {
		setLayout(new FlowLayout());
		
		topPanel = new JPanel(new GridBagLayout());
		bottomPanel = new JPanel(new GridBagLayout());
		
		initFileListPanel();
		initVerificationOptionsPanel();
		initMonitorPanel();
		initResultTablePanel();
		setFileListToTempFile();
		
		splitpane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, bottomPanel);		
		splitpane.setResizeWeight(0);
		splitpane.setDividerSize(10);
		splitpane.setContinuousLayout(true);
		setContentPane(splitpane);
	}
	
	private void setFileListToTempFile() {
		if (!(isQueryListEmpty())) {
			files.add(QueryPane.getTemporaryFile());
		}
	}
	
	private boolean isQueryListEmpty() {
		return ListOfQueries.getModel().getSize() == 0;
	}

	private void initFileListPanel() {
		JPanel fileListPanel = new JPanel(new GridBagLayout());
		fileListPanel.setBorder(BorderFactory.createTitledBorder("Models"));

		listModel = new DefaultListModel<>();
		fileList = new JList<>(listModel);
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

		fileList.addListSelectionListener(e -> {
			if (!(e.getValueIsAdjusting())) {
				if (fileList.getSelectedIndex() == -1) {
					removeFileButton.setEnabled(false);
				} else {
					removeFileButton.setEnabled(true);
				}
			}
		});

		fileList.setCellRenderer(new FileNameCellRenderer<>());

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
		addFilesButton.addActionListener(arg0 -> addFiles());

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
		removeFileButton.addActionListener(arg0 -> removeSelectedFiles());
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 0, 5);
		filesButtonsPanel.add(removeFileButton, gbc);
		
		clearFilesButton = new JButton("Clear");
		clearFilesButton.setToolTipText(TOOL_TIP_ClearFilesButton);
		clearFilesButton.setEnabled(false);
		clearFilesButton.addActionListener(e -> {
			clearFiles();
			enableButtons();
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
		//Hides the file list panel if batch processing is run from the tabcomponent
		if(!(isQueryListEmpty())) {
			fileListPanel.setVisible(false);
		}
	}

	private void addFiles() {
		FileBrowser browser = FileBrowser.constructor("Timed-Arc Petri Nets","tapn", "xml", lastPath);
		File[] filesArray = browser.openFiles();
		if (filesArray.length>0) {
            undoManager.newEdit();
            for (File file : filesArray) {
				lastPath = file.getParent();
				if (!files.contains(file)) {
                    Command c = new AddFileBatchProcessingCommand(listModel,file, files, this );
                    c.redo();
                    undoManager.addEdit(c);
				}
			}
            toggleDefault();
		}
	}

	private void removeSelectedFiles() {
	    undoManager.newEdit();
		for (Object o : fileList.getSelectedValuesList()) {
			File file = (File) o;
            Command c = new RemoveFileBatchProcessingCommand(listModel,file, files, this );
            c.redo();
            undoManager.addEdit(c);
		}
	}
	
	private void initVerificationOptionsPanel() {
		verificationOptionsPanel = new JPanel(new GridBagLayout());
		verificationOptionsPanel.setBorder(BorderFactory
            .createTitledBorder("Override Verification Options for the Batch"));

		initQueryPropertyOptionsComponents();
        initDefaultOptionsComponents();
        initTAPNOptionsComponents();
        initPNOptionsComponents();
        initDTAPNOptionsComponents();
		initCapacityComponents();
		initTimeoutComponents();
		initOOMComponents();
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHEAST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weighty = 0;
		gbc.weightx = 0;
		gbc.insets = new Insets(10, 0, 0, 5);
		topPanel.add(verificationOptionsPanel, gbc);
		//Hides the verification options if batch processing is run from the tabcomponent
		if(!(isQueryListEmpty())) {
			verificationOptionsPanel.setVisible(false);
		}
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

		String[] options = new String[] {
		    name_KeepQueryOption,
            name_SEARCHWHOLESTATESPACE,
            name_EXISTDEADLOCK,
            name_SOUNDNESS,
            name_STRONGSOUNDNESS
		};
		queryPropertyOption = new JComboBox<>(options);
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
		keepQueryCapacity.addActionListener(e -> {
		    if (keepQueryCapacity.isSelected()) {
		        numberOfExtraTokensInNet.setEnabled(false);
		    } else {
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
		gbc.gridy = 7;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 5, 0);
		verificationOptionsPanel.add(timeoutLabel, gbc);

		timeoutValue = new CustomJSpinner(30, 5,Integer.MAX_VALUE);
		timeoutValue.setToolTipText(TOOL_TIP_TimeoutValue);
		timeoutValue.setMaximumSize(new Dimension(70, 30));
		timeoutValue.setMinimumSize(new Dimension(70, 30));
		timeoutValue.setPreferredSize(new Dimension(70, 30));
		timeoutValue.setEnabled(true);

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 7;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 5, 10);
		verificationOptionsPanel.add(timeoutValue, gbc);

		noTimeoutCheckbox = new JCheckBox("Do not use timeout");
		noTimeoutCheckbox.setToolTipText(TOOL_TIP_NoTimeoutCheckBox);
		noTimeoutCheckbox.setSelected(false);
		noTimeoutCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (noTimeoutCheckbox.isSelected()) {
                    timeoutValue.setEnabled(false);
                } else {
                    timeoutValue.setEnabled(true);
                }
			}
		});

		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 7;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 5, 0);
		verificationOptionsPanel.add(noTimeoutCheckbox, gbc);
	}
	
	private void initOOMComponents() {
		JLabel oomLabel = new JLabel("Max memory (in MB): ");
		oomLabel.setToolTipText(TOOL_TIP_OOMLabel);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 6;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 5, 0);
		verificationOptionsPanel.add(oomLabel, gbc);

		oomValue = new CustomJSpinner(2048,1,Integer.MAX_VALUE);
		oomValue.setToolTipText(TOOL_TIP_OOMValue);
		oomValue.setMaximumSize(new Dimension(70, 30));
		oomValue.setMinimumSize(new Dimension(70, 30));
		oomValue.setPreferredSize(new Dimension(70, 30));
		oomValue.setEnabled(true);

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 6;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 5, 10);
		verificationOptionsPanel.add(oomValue, gbc);

		noOOMCheckbox = new JCheckBox("Do not limit memory usage");
		noOOMCheckbox.setToolTipText(TOOL_TIP_NoOOMCheckBox);
		noOOMCheckbox.setSelected(false);
		noOOMCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (noOOMCheckbox.isSelected()) {
                    oomValue.setEnabled(false);
                } else {
                    oomValue.setEnabled(true);
                }
			}
		});

		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 6;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 5, 0);
		verificationOptionsPanel.add(noOOMCheckbox, gbc);
	}

    private void initDefaultOptionsComponents() {
        JLabel reductionLabel = new JLabel("Default verification method:");
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 0);
        verificationOptionsPanel.add(reductionLabel, gbc);

        defaultYes = new JRadioButton("Yes", true);
        defaultYes.setToolTipText(TOOL_TIP_DefaultOption);
        defaultNo = new JRadioButton("No", false);
        defaultNo.setToolTipText(TOOL_TIP_DefaultOption);

        defaultOptions = new ButtonGroup();
        defaultOptions.add(defaultYes);
        defaultOptions.add(defaultNo);

        defaultYes.addChangeListener(e -> toggleDefault());
        defaultNo.addChangeListener(e -> toggleDefault());

        gbc.gridx = 1;
        verificationOptionsPanel.add(defaultYes, gbc);
        gbc.gridx = 2;
        verificationOptionsPanel.add(defaultNo, gbc);
    }

    private void toggleDefault() {
        if (defaultYes.isSelected()) {
            checkboxTAPN.setSelected(false);
            checkboxPN.setSelected(false);
            checkboxDTAPN.setSelected(false);
            helpTAPN.setEnabled(false);
            helpPN.setEnabled(false);
            helpDTAPN.setEnabled(false);
            optionsTAPN.setEnabled(false);
            optionsPN.setEnabled(false);
            optionsDTAPN.setEnabled(false);
        }
        checkboxTAPN.setEnabled(defaultNo.isSelected());
        checkboxPN.setEnabled(defaultNo.isSelected());
        checkboxDTAPN.setEnabled(defaultNo.isSelected());
    }

    private void initTAPNOptionsComponents() {
        checkboxTAPN = new JCheckBox("VerifyTAPN");
        checkboxTAPN.setEnabled(false);
        checkboxTAPN.addActionListener(e -> {
            optionsTAPN.setEnabled(checkboxTAPN.isSelected());
            helpTAPN.setEnabled(checkboxTAPN.isSelected());
        });
        checkboxTAPN.setToolTipText(TOOL_TIP_VerifyTAPNOption);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 0);
        verificationOptionsPanel.add(checkboxTAPN, gbc);

        optionsTAPN = new JTextField();
        optionsTAPN.setEnabled(false);
        optionsTAPN.setPreferredSize(new Dimension(200,30));
        gbc.gridx = 1;
        verificationOptionsPanel.add(optionsTAPN, gbc);

        helpDialogTAPN = new HelpDialog(BatchProcessingDialog.this, "Options for verifyTAPN", ModalityType.MODELESS, new VerifyTAPN(new FileFinder(), new MessengerImpl()).getHelpOptions());

        helpTAPN = new JButton("Help");
        helpTAPN.setToolTipText(TOOL_TIP_Help);
        helpTAPN.setEnabled(false);
        helpTAPN.addActionListener(e -> helpDialogTAPN.setVisible(true));

        gbc.gridx = 2;
        verificationOptionsPanel.add(helpTAPN, gbc);
    }

    private void initPNOptionsComponents() {
        checkboxPN = new JCheckBox("VerifyPN");
        checkboxPN.setEnabled(false);
        checkboxPN.addActionListener(e -> {
            optionsPN.setEnabled(checkboxPN.isSelected());
            helpPN.setEnabled(checkboxPN.isSelected());
        });
        checkboxPN.setToolTipText(TOOL_TIP_VerifyPNOption);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 0);
        verificationOptionsPanel.add(checkboxPN, gbc);

        optionsPN = new JTextField();
        optionsPN.setEnabled(false);
        optionsPN.setPreferredSize(new Dimension(200,30));
        gbc.gridx = 1;
        verificationOptionsPanel.add(optionsPN, gbc);

        helpDialogPN = new HelpDialog(BatchProcessingDialog.this, "Options for verifyPN", ModalityType.MODELESS, new VerifyPN(new FileFinder(), new MessengerImpl()).getHelpOptions());

        helpPN = new JButton("Help");
        helpPN.setToolTipText(TOOL_TIP_Help);
        helpPN.setEnabled(false);
        helpPN.addActionListener(e -> helpDialogPN.setVisible(true));

        gbc.gridx = 2;
        verificationOptionsPanel.add(helpPN, gbc);
    }

    private void initDTAPNOptionsComponents() {
        checkboxDTAPN = new JCheckBox("VerifyDTAPN");
        checkboxDTAPN.setEnabled(false);
        checkboxDTAPN.addActionListener(e -> {
            optionsDTAPN.setEnabled(checkboxDTAPN.isSelected());
            helpDTAPN.setEnabled(checkboxDTAPN.isSelected());
        });
        checkboxDTAPN.setToolTipText(TOOL_TIP_VerifyDTAPNOption);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 0);
        verificationOptionsPanel.add(checkboxDTAPN, gbc);

        optionsDTAPN = new JTextField();
        optionsDTAPN.setEnabled(false);
        optionsDTAPN.setPreferredSize(new Dimension(200,30));
        gbc.gridx = 1;
        verificationOptionsPanel.add(optionsDTAPN, gbc);

        helpDialogDTAPN = new HelpDialog(BatchProcessingDialog.this, "Options for verifyDTAPN", ModalityType.MODELESS, new VerifyDTAPN(new FileFinder(), new MessengerImpl()).getHelpOptions());

        helpDTAPN = new JButton("Help");
        helpDTAPN.setToolTipText(TOOL_TIP_Help);
        helpDTAPN.setEnabled(false);
        helpDTAPN.addActionListener(e -> helpDialogDTAPN.setVisible(true));

        gbc.gridx = 2;
        verificationOptionsPanel.add(helpDTAPN, gbc);
    }

	private void disableVerificationOptionsButtons() {
		verificationOptionsPanel.setEnabled(false);
		for (Component c : verificationOptionsPanel.getComponents()) {
            c.setEnabled(false);
        }
	}

	private void enabledVerificationOptionButtons() {
		verificationOptionsPanel.setEnabled(true);
		for (Component c : verificationOptionsPanel.getComponents()) {
            c.setEnabled(true);
        }

		numberOfExtraTokensInNet.setEnabled(!keepQueryCapacity.isSelected());
		//approximationDenominator.setEnabled(!approximationDenominatorCheckbox.isSelected());
		timeoutValue.setEnabled(useTimeout());
	}

	private BatchProcessingVerificationOptions getVerificationOptions() {
		return new BatchProcessingVerificationOptions(
		    (Integer) numberOfExtraTokensInNet.getValue(),
            SearchOption.BatchProcessingKeepQueryOption,
            ReductionOption.BatchProcessingKeepQueryOption,
            false, false, false, false,
            0,
            new ArrayList<>()
        );
    }

	private int getNumberOfExtraTokens() {
		return (Integer) numberOfExtraTokensInNet.getValue();
	}
	
	private void exit() {
		terminateBatchProcessing();
		rootPane.getParent().setVisible(false);
		// Resets batch processing when exiting if batch processing was called from the tab
		if (!(isQueryListEmpty())) {
			batchProcessingDialog = null;
		}
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
				String filename = FileBrowser.constructor("CSV file", "csv", lastPath).saveFile("results");

				if (filename != null) {
					File exportFile = new File(filename);
					lastPath = exportFile.getParent();
					BatchProcessingResultsExporter exporter = new BatchProcessingResultsExporter();
					try {
						exporter.exportToCSV(tableModel.getResults(), exportFile);
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(
								TAPAALGUI.getApp(),
								"An error occurred while trying to export the results. Please try again",
								"Error Exporting Results",
								JOptionPane.ERROR_MESSAGE
						);
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
		closeButton.addActionListener(e -> exit());

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.insets = new Insets(5, 0, 0, 0);
		gbc.anchor = GridBagConstraints.SOUTHEAST;
		resultTablePanel.add(closeButton, gbc);

		tableModel = new BatchProcessingResultsTableModel();
		final JTable table = new JTable(tableModel) {
			public javax.swing.JToolTip createToolTip() {
				ToolTipManager.sharedInstance().setDismissDelay(
						Integer.MAX_VALUE); // disable tooltips disappearing
				ToolTipManager.sharedInstance().setInitialDelay(200);
				return new MultiLineAutoWrappingToolTip();
			}
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
		table.getColumn("Memory Usage").setCellRenderer(renderer);

		tableModel.addTableModelListener(e -> {
			if (e.getType() == TableModelEvent.INSERT) {
				table.scrollRectToVisible(table.getCellRect(e.getLastRow(), e.getLastRow(), true));
			}
		});
		
		// Enable sorting
		Comparator<Object> comparator = new StringComparator();
		
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(table.getModel());
		for(int i = 0; i < table.getColumnCount(); i++){
			sorter.setComparator(i, comparator);
		}
		table.setRowSorter(sorter);

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
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
		gbc.gridwidth = 1;
		gbc.weightx = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		monitorPanel.add(statusLabel, gbc);
		
		JLabel memoryLabel = new JLabel("Memory: ");
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 5, 0, 0);
		monitorPanel.add(memoryLabel, gbc);

		memory = new JLabel("");
		Dimension timerLabelDim = new Dimension(70, 25);
		memory.setMinimumSize(timerLabelDim);
		memory.setPreferredSize(timerLabelDim);
		gbc = new GridBagConstraints();
		gbc.gridx = 3;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 5, 0, 5);
		monitorPanel.add(memory, gbc);

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
		Dimension memoryLabelDim = new Dimension(70, 25);
		timerLabel.setMinimumSize(memoryLabelDim);
		timerLabel.setPreferredSize(memoryLabelDim);
		gbc = new GridBagConstraints();
		gbc.gridx = 3;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 5, 0, 5);
		monitorPanel.add(timerLabel, gbc);

		startButton = new JButton("Start");
		startButton.setToolTipText(TOOL_TIP_StartButton);
		startButton.setMaximumSize(new Dimension(85, 25));
		startButton.setMinimumSize(new Dimension(85, 25));
		startButton.setPreferredSize(new Dimension(85, 25));

		startButton.setEnabled(false);
		startButton.addActionListener(e -> process());
		gbc = new GridBagConstraints();
		gbc.gridx = 4;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 0, 10);
		monitorPanel.add(startButton, gbc);

		cancelButton = new JButton("Cancel");
		cancelButton.setToolTipText(TOOL_TIP_CancelButton);
		cancelButton.setMaximumSize(new Dimension(85, 25));
		cancelButton.setMinimumSize(new Dimension(85, 25));
		cancelButton.setPreferredSize(new Dimension(85, 25));
		
		cancelButton.setEnabled(false);
		cancelButton.addActionListener(e -> {
			terminateBatchProcessing();
			fileStatusLabel.setText("");
			statusLabel.setText("Batch processing cancelled");
			enableButtons();
		});

		gbc = new GridBagConstraints();
		gbc.gridx = 4;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 0, 10);
		monitorPanel.add(cancelButton, gbc);

		skipFileButton = new JButton("Skip");
		skipFileButton.setToolTipText(TOOL_TIP_SkipFileButton);
		skipFileButton.setMaximumSize(new Dimension(85, 25));
		skipFileButton.setMinimumSize(new Dimension(85, 25));
		skipFileButton.setPreferredSize(new Dimension(85, 25));
		
		skipFileButton.setEnabled(false);
		skipFileButton.addActionListener(e -> skipCurrentFile());

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
		if (defaultYes.isSelected()) {
            currentWorker = new BatchProcessingWorker(files, tableModel, null);
        } else {
            Map<ReductionOption, String> verificationOptions = Map.of(
                ReductionOption.VerifyTAPN, optionsTAPN.getText(),
                ReductionOption.VerifyPN, optionsPN.getText(),
                ReductionOption.VerifyDTAPN, optionsDTAPN.getText()
            );
            currentWorker = new BatchProcessingWorker(files, tableModel, verificationOptions);
        }
		currentWorker.addPropertyChangeListener(evt -> {
            if (evt.getPropertyName().equals("state")) {
                if (evt.getNewValue() == StateValue.DONE) {
                    enableButtons();
                    cancelButton.setEnabled(false);
                    skipFileButton.setEnabled(false);
                    timerLabel.setText("");
                    timer.stop();
                    stopMemoryTimer();
                    timeoutTimer.stop();
                } else if (evt.getNewValue() == StateValue.STARTED) {
                    disableButtonsDuringProcessing();
                    cancelButton.setEnabled(true);
                    skipFileButton.setEnabled(true);
                    memory.setText("");
                    timerLabel.setText("");
                    progressLabel.setText("0 verification tasks completed");
                }
            }
        });
		currentWorker.addBatchProcessingListener(new BatchProcessingListener() {
			public void fireVerificationTaskStarted() {
				if (timer.isRunning()) {
                    timer.restart();
                } else {
                    timer.start();
                }
				
				startMemoryTimer();

				if (useTimeout()) {
					if (timeoutTimer.isRunning()) {
                        timeoutTimer.restart();
                    } else {
                        timeoutTimer.start();
                    }
				}

				startTimeMs = System.currentTimeMillis();
			}

			public void fireVerificationTaskComplete(VerificationTaskCompleteEvent e) {
				if (timer.isRunning()) {
					timer.stop();
				}
				stopMemoryTimer();
				if (timeoutTimer.isRunning()) {
					timeoutTimer.stop();
				}
				int tasksCompleted = e.verificationTasksCompleted();
				progressLabel.setText(e.verificationTasksCompleted()
						+ " verification task"
						+ (tasksCompleted > 1 ? "s" : "") + " completed");
				timerLabel.setText("");
				memory.setText("");
			}

			public void fireStatusChanged(StatusChangedEvent e) {
				statusLabel.setText(e.status());
			}

			public void fireFileChanged(FileChangedEvent e) {
				if(!(isQueryListEmpty())) {
					fileStatusLabel.setText(TAPAALGUI.getAppGui().getCurrentTabName());
				} else {
                    fileStatusLabel.setText(e.fileName());
                }
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
	
	private boolean useOOM() {
		return !noOOMCheckbox.isSelected();
	}

	private void terminateBatchProcessing() {
		if (currentWorker != null && !currentWorker.isDone()) {
			boolean cancelled = false;
			do {
				currentWorker.notifyExiting();
				cancelled = currentWorker.cancel(true);
			} while (!cancelled);
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
	
	private void oomCurrentVerificationTask() {
		if (currentWorker != null && !currentWorker.isDone()) {
			currentWorker.notifyOOMCurrentVerificationTask();
		}
	}

	private void clearFiles() {
        undoManager.newEdit();

        for (Object o : listModel.toArray()) {
            File file = (File)o;
            Command c = new RemoveFileBatchProcessingCommand(listModel,file, files, this );
            c.redo();
            undoManager.addEdit(c);
        }
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

	public void enableButtons() {
		fileList.setEnabled(true);
		addFilesButton.setEnabled(true);

		if (listModel.size() > 0) {
			clearFilesButton.setEnabled(true);
			startButton.setEnabled(true);
			removeFileButton.setEnabled(true);
		} else {
			clearFilesButton.setEnabled(false);
			startButton.setEnabled(false);
			removeFileButton.setEnabled(false);
		}

		if (tableModel.getRowCount() > 0)
			exportButton.setEnabled(true);
		else
			exportButton.setEnabled(false);

		enabledVerificationOptionButtons();
	}

	// Custom cell renderer for the Query Column of the result table display the
	// property of the query
	private class ResultTableCellRenderer extends JLabel implements TableCellRenderer {

		Border unselectedBorder = null;
		Border selectedBorder = null;
		boolean isBordered = true;

		public ResultTableCellRenderer(boolean isBordered) {
			this.isBordered = isBordered;
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
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
								SATISFIED_STRING) || 
								value.toString().equals(SATISFIED_SOUNDNESS_STRING) || value.toString().equals(SATISFIED_STRONG_SOUNDNESS_STRING))
								|| (isQueryColumn && value.toString().equals(
										"TRUE")))
							setBackground(new Color(91, 255, 91)); // light green
						else if ((isResultColumn && (value.toString().equals(
								NOT_SATISFIED_STRING) || 
								value.toString().equals(NOT_SATISFIED_STRING_STRONG_SOUNDNESS) || value.toString().equals(NOT_SATISFIED_STRING_SOUNDNESS)))
								|| (isQueryColumn && value.toString().equals(
										"FALSE")))
							setBackground(new Color(255, 91, 91)); // light  red
						else if (isResultColumn && value.toString().equals(
								"Inconclusive"))
							setBackground(new Color(255, 255, 120)); // light yellow
						else
							setBackground(table.getBackground());
					} else {
						setBackground(table.getBackground());
					}
					setForeground(table.getForeground());
					if (unselectedBorder == null) {
						unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getBackground());
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
				} else if (table.getColumnName(column).equals("Verification Time")
						|| table.getColumnName(column).equals("Method")
						|| table.getColumnName(column).equals("Memory Usage")) {
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
					else if (table.getColumnName(column).equals("Memory Usage"))
						setToolTipText(result != null ? generateMemoryToolTipText(result)
								: value.toString());
					else
						setToolTipText(result != null ? generateReductionString(result.query()) : value.toString());
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

		private String generateStatsToolTipText(BatchProcessingVerificationResult result) {
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
		
		private String generateMemoryToolTipText(BatchProcessingVerificationResult result) {
			StringBuilder s = new StringBuilder();
			s.append("Peak memory usage (estimate): ");
			s.append(result.verificationMemory());
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
			
			s.append("\n\n");
			s.append("Stubborn Reduction: ");
			s.append(query.isStubbornReductionEnabled() ? "Yes\n\n" : "No\n\n");

            s.append("\n\n");
            s.append("Trace Abstract Refinement: ");
            s.append(query.isTarOptionEnabled() ? "Yes\n\n" : "No\n\n");

            s.append("\n\n");
            s.append("USe Tarjan: ");
            s.append(query.isTarjan() ? "Yes\n\n" : "No\n\n");

			s.append("Query Property:\n");
                        s.append(query.getProperty().toString());
			
			s.append("\n\n");
			s.append("Approximation method: ");
			if (query.isOverApproximationEnabled()) {
				s.append(name_OVER_APPROXIMATION);
			} else if (query.isUnderApproximationEnabled()) {
				s.append(name_UNDER_APPROXIMATION);
			} else {
				s.append(name_NONE_APPROXIMATION);
			}
			s.append("\n");
			s.append("Approximation Constant: ");
			s.append(query.approximationDenominator());

			return s.toString();
		}

		private String generateReductionString(TAPNQuery query) {
			StringBuilder s = new StringBuilder();
			if (query != null) {
				s.append("Reduction: \n");
				if (query.getReductionOption() == ReductionOption.COMBI)
					s.append(name_COMBI);
				else if (query.getReductionOption() == ReductionOption.STANDARD)
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
				} else if(query.getReductionOption() == ReductionOption.VerifyDTAPN) {
					if(query.useTimeDarts() && query.usePTrie()){
						s.append(name_verifyTAPNDiscreteVerificationTimeDartPTrie);
					} else if(query.useTimeDarts()){
						s.append(name_verifyTAPNDiscreteVerificationTimeDart);
					} else if(query.usePTrie()){
						s.append(name_verifyTAPNDiscreteVerificationPTrie);
					} else {
						s.append(name_verifyTAPNDiscreteVerificationNone);
					}
				} else if(query.getReductionOption() == ReductionOption.VerifyPN || query.getReductionOption() == ReductionOption.VerifyPNApprox || query.getReductionOption() == ReductionOption.VerifyPNReduce){
//					if(query.useReduction())
//						s.append(name_UNTIMED_REDUCE);
//					else if(query.useOverApproximation())
//						s.append(name_UNTIMED_APPROX);
//					else
						s.append(name_UNTIMED);
				} else {
					s.append(name_BROADCAST);
				}
				
				VerificationOptions options = currentWorker.getVerificationOptionsFromQuery(query);
				s.append("\n\nEngine flags: \n");
				s.append(options.toString() + "PAS PÅ"); // TODO LENA OBS
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

	public class HelpDialog extends JDialog {
        private JPanel content;
        private JScrollPane scroll;
        private JTextArea helpInfo;

        public HelpDialog(JDialog dialog, String string, ModalityType modal, String text) {
            super(dialog, string, modal);

            initComponents(text);

            this.setLocationRelativeTo(BatchProcessingDialog.this);
            this.setResizable(true);
            this.pack();
            this.setLocationByPlatform(true);
        }

        private void initComponents(String text){
            content = new JPanel(new GridBagLayout());

            helpInfo = new JTextArea(text);
            helpInfo.setEditable(false); // set textArea non-editable
            scroll = new JScrollPane(helpInfo);
            scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
            scroll.setMinimumSize(new Dimension(640,400));
            scroll.setPreferredSize(new Dimension(640,400));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 1;
            gbc.weighty = 1;
            content.add(scroll, gbc);

            this.getContentPane().setLayout(new GridBagLayout());

            gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 1;
            gbc.weighty = 1;
            this.getContentPane().add(content, gbc);
        }
    }

	private void makeShortcuts(){
        int shortcutkey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        ActionMap am = splitpane.getActionMap();
        am.put("undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                undoManager.undo();
            }
        });
        am.put("redo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                undoManager.redo();
            }
        });
        InputMap im = splitpane.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW);
        im.put(KeyStroke.getKeyStroke('Z', shortcutkey), "undo");
        im.put(KeyStroke.getKeyStroke('Y', shortcutkey), "redo");
    }
}


