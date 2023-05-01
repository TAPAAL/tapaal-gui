package net.tapaal.gui.petrinet.dialog;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.SwingWorker.StateValue;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.event.TableModelEvent;
import javax.swing.table.*;

import dk.aau.cs.verification.VerifyTAPN.VerifyPN;
import dk.aau.cs.verification.VerifyTAPN.VerifyDTAPN;
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
	private static final String name_verifyTAPN = "TAPAAL Continuous Engine (VerifyTAPN)";
	private static final String name_verifyTAPNDiscreteVerificationTimeDartPTrie = "TAPAAL Discrete Engine w. Time Darts and PTrie";
	private static final String name_verifyTAPNDiscreteVerificationTimeDart = "TAPAAL Discrete Engine w. Time Darts";
	private static final String name_verifyTAPNDiscreteVerificationPTrie = "TAPAAL Discrete Engine w. PTries";
	private static final String name_verifyTAPNDiscreteVerificationNone = "TAPAAL Discrete Engine (VerifyDTAPN) w. no Optimizations";
	private static final String name_COMBI = "UPPAAL: Optimized Broadcast Reduction";
	private static final String name_STANDARD = "UPPAAL: Standard Reduction";
	private static final String name_OPTIMIZEDSTANDARD = "UPPAAL: Optimised Standard Reduction";
	private static final String name_BROADCAST = "UPPAAL: Broadcast Reduction";
	private static final String name_BROADCASTDEG2 = "UPPAAL: Broadcast Degree 2 Reduction";
	private static final String name_UNTIMED = "TAPAAL Untimed CTL Engine (VerifyPN)";

	private static final String name_BFS = "Breadth first search";
	private static final String name_DFS = "Depth first search";
	private static final String name_HEURISTIC = "Heuristic search";
	private static final String name_Random = "Random search";
	private static final String name_NONE_APPROXIMATION = "None";
	private static final String name_OVER_APPROXIMATION = "Over-approximation";
	private static final String name_UNDER_APPROXIMATION = "Under-approximation";

	//Tool tip strings
	//Tool tips for model panel
	private final static String TOOL_TIP_AddFilesButton = "Press to add nets to batch processing";
	private final static String TOOL_TIP_RemoveFilesButton = "Press to remove the currently selected nets";
	private final static String TOOL_TIP_ClearFilesButton = "Press to remove all nets from list";
	
	//Tool tips for override verification panel
	private final static String TOOL_TIP_Help = "See the options available for the specific engine";
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

	private JPanel optionsPanel;
	private DefaultTableModel optionsTable;
	private JTable verificationTable;
	private JComboBox<String> engines;
    private HelpDialog helpDialogPN;
    private HelpDialog helpDialogDTAPN;
    private HelpDialog helpDialogTAPN;

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
	private JPanel verificationOptionsPanel;
    private JCheckBox noTimeoutCheckbox;
	private JCheckBox noOOMCheckbox;
	private CustomJSpinner timeoutValue;
	private CustomJSpinner oomValue;
	private final JList<TAPNQuery> ListOfQueries;
	
	private final Timer timeoutTimer = new Timer(30000, e -> timeoutCurrentVerificationTask());

	private BatchProcessingResultsTableModel tableModel;

	private final List<File> files = new ArrayList<>();
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
		gbc.gridheight = 4;
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

        initOptionsTable();
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHEAST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weighty = 0;
		gbc.weightx = 0;
		gbc.insets = new Insets(10, 0, 0, 5);
		topPanel.add(verificationOptionsPanel, gbc);
		// Hides the verification options if batch processing is run from the tab component
		if(!(isQueryListEmpty())) {
			verificationOptionsPanel.setVisible(false);
		}
	}

    private void initOptionsTable() {
        optionsPanel = new JPanel(new GridBagLayout());

        String[] engineNames = {"Default", "VerifyPN", "VerifyDTAPN", "VerifyTAPN"};
        engines = new JComboBox<>(engineNames);

        Object[] columnNames = {"Run", "Option", "Verification options", "Keep k-bound", "Engine"};
        Object[][] data = {{Boolean.TRUE, 0, "Default", Boolean.TRUE, "Default"}};

        optionsTable = new DefaultTableModel(data, columnNames) {
            public Class<?> getColumnClass(int column) {
                return getValueAt(0, column).getClass();
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 1;
            }
        };

        verificationTable = new JTable(optionsTable);
        verificationTable.getTableHeader().setOpaque(true);
        verificationTable.getTableHeader().setBackground(Color.white);
        verificationTable.setPreferredScrollableViewportSize(verificationTable.getPreferredSize());
        verificationTable.getColumn(columnNames[0]).setMinWidth(30);
        verificationTable.getColumn(columnNames[0]).setMaxWidth(30);
        verificationTable.getColumn(columnNames[1]).setMinWidth(50);
        verificationTable.getColumn(columnNames[1]).setMaxWidth(50);
        verificationTable.getColumn(columnNames[3]).setMinWidth(100);
        verificationTable.getColumn(columnNames[3]).setMaxWidth(100);
        verificationTable.getColumn(columnNames[4]).setMinWidth(100);
        verificationTable.getColumn(columnNames[4]).setMaxWidth(100);
        verificationTable.changeSelection(0, 0, false, false);

        optionsTable.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.INSERT) {
                verificationTable.scrollRectToVisible(verificationTable.getCellRect(e.getLastRow(), e.getLastRow(), true));
            }
        });

        verificationTable.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(engines));

        JScrollPane scrollPane = new JScrollPane(verificationTable);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        Dimension scrollPanePrefDims = new Dimension(850, 250);
        Dimension scrollPaneMinDims = new Dimension(850, 250-150);
        scrollPane.setMinimumSize(scrollPaneMinDims);
        scrollPane.setPreferredSize(scrollPanePrefDims);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 6;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        optionsPanel.add(scrollPane, gbc);

        initVerificationButtons();

        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 5, 5, 5);
        verificationOptionsPanel.add(optionsPanel, gbc);
    }

    private void initVerificationButtons() {
        JButton addOptionButton = new JButton("Add");
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        optionsPanel.add(addOptionButton, gbc);
        addOptionButton.addActionListener(e ->
            optionsTable.addRow(new Object[]{Boolean.TRUE, optionsTable.getRowCount(), "-x 1", Boolean.TRUE, "VerifyPN"})
        );

        JButton copyOptionButton = new JButton("Copy");
        gbc.gridx = 1;
        optionsPanel.add(copyOptionButton, gbc);
        copyOptionButton.addActionListener(e -> {
            int row = verificationTable.getSelectedRow();
            optionsTable.addRow(new Object[]{
                verificationTable.getValueAt(row, 0),
                optionsTable.getRowCount(),
                verificationTable.getValueAt(row, 2),
                verificationTable.getValueAt(row, 3),
                verificationTable.getValueAt(row, 4)
            });
        });

        JButton removeOptionButton = new JButton("Remove");
        gbc.gridx = 2;
        optionsPanel.add(removeOptionButton, gbc);
        removeOptionButton.addActionListener(e -> {
            optionsTable.removeRow(verificationTable.getSelectedRow());
            for (int rowIndex = 0; rowIndex < verificationTable.getRowCount(); rowIndex++) {
                verificationTable.setValueAt(rowIndex, rowIndex, 1);
            }
        });

        helpDialogPN = new HelpDialog(
            BatchProcessingDialog.this,
            "Options for VerifyPN",
            ModalityType.MODELESS,
            new VerifyPN(new FileFinder(), new MessengerImpl()).getHelpOptions());

        JButton helpPN = new JButton("Help VerifyPN");
        gbc.gridx = 3;
        optionsPanel.add(helpPN, gbc);
        helpPN.setToolTipText(TOOL_TIP_Help);
        helpPN.addActionListener(e -> helpDialogPN.setVisible(true));

        helpDialogDTAPN = new HelpDialog(
            BatchProcessingDialog.this,
            "Options for VerifyDTAPN",
            ModalityType.MODELESS,
            new VerifyDTAPN(new FileFinder(), new MessengerImpl()).getHelpOptions());

        JButton helpDTAPN = new JButton("Help VerifyDTAPN");
        gbc.gridx = 4;
        optionsPanel.add(helpDTAPN, gbc);
        helpDTAPN.setToolTipText(TOOL_TIP_Help);
        helpDTAPN.addActionListener(e -> helpDialogDTAPN.setVisible(true));

        helpDialogTAPN = new HelpDialog(
            BatchProcessingDialog.this,
            "Options for VerifyTAPN",
            ModalityType.MODELESS,
            new VerifyTAPN(new FileFinder(), new MessengerImpl()).getHelpOptions());

        JButton helpTAPN = new JButton("Help VerifyTAPN");
        gbc.gridx = 5;
        optionsPanel.add(helpTAPN, gbc);
        helpTAPN.setToolTipText(TOOL_TIP_Help);
        helpTAPN.addActionListener(e -> helpDialogTAPN.setVisible(true));
    }
	
	private void initTimeoutComponents() {
		JLabel timeoutLabel = new JLabel("Timeout (in seconds): ");
		timeoutLabel.setToolTipText(TOOL_TIP_TimeoutLabel);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 5, 0);
		monitorPanel.add(timeoutLabel, gbc);

		timeoutValue = new CustomJSpinner(30, 5,Integer.MAX_VALUE);
		timeoutValue.setToolTipText(TOOL_TIP_TimeoutValue);
		timeoutValue.setMaximumSize(new Dimension(70, 30));
		timeoutValue.setMinimumSize(new Dimension(70, 30));
		timeoutValue.setPreferredSize(new Dimension(70, 30));
		timeoutValue.setEnabled(true);

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 5, 10);
		monitorPanel.add(timeoutValue, gbc);

		noTimeoutCheckbox = new JCheckBox("Do not use timeout");
		noTimeoutCheckbox.setToolTipText(TOOL_TIP_NoTimeoutCheckBox);
		noTimeoutCheckbox.setSelected(false);
		noTimeoutCheckbox.addActionListener(e -> {
            if (noTimeoutCheckbox.isSelected()) {
                timeoutValue.setEnabled(false);
            } else {
                timeoutValue.setEnabled(true);
            }
        });

		gbc = new GridBagConstraints();
		gbc.gridx = 4;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 5, 0);
		monitorPanel.add(noTimeoutCheckbox, gbc);
	}
	
	private void initOOMComponents() {
		JLabel oomLabel = new JLabel("Max memory (in MB): ");
		oomLabel.setToolTipText(TOOL_TIP_OOMLabel);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 5, 0);
		monitorPanel.add(oomLabel, gbc);

		oomValue = new CustomJSpinner(2048,1,Integer.MAX_VALUE);
		oomValue.setToolTipText(TOOL_TIP_OOMValue);
		oomValue.setMaximumSize(new Dimension(70, 30));
		oomValue.setMinimumSize(new Dimension(70, 30));
		oomValue.setPreferredSize(new Dimension(70, 30));
		oomValue.setEnabled(true);

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 5, 10);
		monitorPanel.add(oomValue, gbc);

		noOOMCheckbox = new JCheckBox("Do not limit memory usage");
		noOOMCheckbox.setToolTipText(TOOL_TIP_NoOOMCheckBox);
		noOOMCheckbox.setSelected(false);
		noOOMCheckbox.addActionListener(e -> {
            if (noOOMCheckbox.isSelected()) {
                oomValue.setEnabled(false);
            } else {
                oomValue.setEnabled(true);
            }
        });

		gbc = new GridBagConstraints();
		gbc.gridx = 4;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 5, 0);
		monitorPanel.add(noOOMCheckbox, gbc);
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

		timeoutValue.setEnabled(useTimeout());
	}

	private List<BatchProcessingVerificationOptions> getVerificationOptions() {
        List<BatchProcessingVerificationOptions> data = new ArrayList<>();
        for (int i = 0; i < verificationTable.getRowCount(); i++) {
            if (verificationTable.getValueAt(i, 0) == Boolean.FALSE) continue;

            String engine = (String) verificationTable.getValueAt(i, 4);
            ReductionOption reductionOption;
            switch (engine) {
                case "VerifyPN":
                    reductionOption = ReductionOption.VerifyPN;
                    break;
                case "VerifyDTAPN":
                    reductionOption = ReductionOption.VerifyDTAPN;
                    break;
                case "VerifyTAPN":
                    reductionOption = ReductionOption.VerifyTAPN;
                    break;
                default:
                    reductionOption = null;
            }

            data.add(new BatchProcessingVerificationOptions(
                (int) verificationTable.getValueAt(i, 1),
                (String) verificationTable.getValueAt(i, 2),
                (boolean) verificationTable.getValueAt(i, 3),
                reductionOption
            ));
        }
		return data;
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
				ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE); // disable tooltips disappearing
				ToolTipManager.sharedInstance().setInitialDelay(200);
				return new MultiLineAutoWrappingToolTip();
			}
		};
		ResultTableCellRenderer renderer = new ResultTableCellRenderer(true);
        table.getTableHeader().setBackground(Color.white);
        table.getColumnModel().getColumn(0).setMinWidth(70);
		table.getColumnModel().getColumn(0).setPreferredWidth(70);
		table.getColumnModel().getColumn(0).setMaxWidth(85);
        table.getColumnModel().getColumn(4).setMinWidth(100);
        table.getColumnModel().getColumn(4).setMaxWidth(150);
        table.getColumnModel().getColumn(5).setMinWidth(100);
        table.getColumnModel().getColumn(5).setMaxWidth(150);
        table.getColumn("Option").setCellRenderer(renderer);
		table.getColumn("Model").setCellRenderer(renderer);
		table.getColumn("Query").setCellRenderer(renderer);
		table.getColumn("Result").setCellRenderer(renderer);
		table.getColumn("Verification Time").setCellRenderer(renderer);
		table.getColumn("Memory Usage").setCellRenderer(renderer);

        table.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent mouseEvent) {
                JTable source =(JTable) mouseEvent.getSource();
                Point point = mouseEvent.getPoint();
                int row = source.rowAtPoint(point);
                if (mouseEvent.getClickCount() == 2 && source.getSelectedRow() != -1) {
                    String rawOutput = tableModel.getResult(row).getRawOutput();
                    if (rawOutput != null && !rawOutput.equals("")) {
                        final JPanel panel = new JPanel(new GridBagLayout());
                        JOptionPane.showMessageDialog(panel, createRawQueryPanel(tableModel.getResult(row).getRawOutput()), "Raw results", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });

		tableModel.addTableModelListener(e -> {
			if (e.getType() == TableModelEvent.INSERT) {
				table.scrollRectToVisible(table.getCellRect(e.getLastRow(), e.getLastRow(), true));
			}
		});
		
		// Enable sorting
		Comparator<Object> comparator = new StringComparator();
		
		TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
		for(int i = 0; i < table.getColumnCount(); i++){
			sorter.setComparator(i, comparator);
		}
		table.setRowSorter(sorter);

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		Dimension scrollPanePrefDims = new Dimension(850, 250);
		//Set the minimum size to 150 lets than the preferred, to be consistent with the minimum size of the window
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

    private JPanel createRawQueryPanel(final String rawOutput) {
        final JPanel fullPanel = new JPanel(new GridBagLayout());

        JTextArea rawQueryLabel = new JTextArea(rawOutput);
        rawQueryLabel.setEditable(false); // set textArea non-editable
        JScrollPane scroll = new JScrollPane(rawQueryLabel);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setPreferredSize(new Dimension(640,400));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        fullPanel.add(scroll, gbc);

        // Make window resizeable
        fullPanel.addHierarchyListener(new HierarchyListener() {
            public void hierarchyChanged(HierarchyEvent e) {
                //when the hierarchy changes get the ancestor for the message
                Window window = SwingUtilities.getWindowAncestor(fullPanel);
                //check to see if the ancestor is an instance of Dialog and isn't resizable
                if (window instanceof Dialog) {
                    Dialog dialog = (Dialog) window;
                    dialog.setMinimumSize(dialog.getPreferredSize());
                    if (!dialog.isResizable()) {
                        //set resizable to true
                        dialog.setResizable(true);
                    }
                }
            }
        });

        return fullPanel;
    }
	
	private void initMonitorPanel() {
		monitorPanel = new JPanel(new GridBagLayout());
		monitorPanel.setBorder(BorderFactory.createTitledBorder("Monitor"));

        initOOMComponents();
        initTimeoutComponents();

		JLabel file = new JLabel("Net:");
		file.setToolTipText(TOOL_TIP_FileLabel);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.WEST;
		monitorPanel.add(file, gbc);

		fileStatusLabel = new JLabel("");
		Dimension fileStatusLabelDim = new Dimension(250, 25);
		fileStatusLabel.setPreferredSize(fileStatusLabelDim);
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.gridwidth = 3;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		monitorPanel.add(fileStatusLabel, gbc);

		JLabel status = new JLabel("Query:");
		status.setToolTipText(TOOL_TIP_StatusLabel);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.WEST;
		monitorPanel.add(status, gbc);

		statusLabel = new JLabel("");
		Dimension statusLabelDim = new Dimension(250, 25);
		statusLabel.setPreferredSize(statusLabelDim);
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.gridwidth = 1;
		gbc.weightx = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		monitorPanel.add(statusLabel, gbc);
		
		JLabel memoryLabel = new JLabel("Memory: ");
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 5, 0, 0);
		monitorPanel.add(memoryLabel, gbc);

		memory = new JLabel("");
		Dimension timerLabelDim = new Dimension(70, 25);
		memory.setMinimumSize(timerLabelDim);
		memory.setPreferredSize(timerLabelDim);
		gbc = new GridBagConstraints();
		gbc.gridx = 3;
		gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 5, 0, 5);
		monitorPanel.add(memory, gbc);

		JLabel progress = new JLabel("Progress: ");
		progress.setToolTipText(TOOL_TIP_ProgressLabel);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.anchor = GridBagConstraints.WEST;
		monitorPanel.add(progress, gbc);

		progressLabel = new JLabel("");
		Dimension dim = new Dimension(210, 25);
		progressLabel.setPreferredSize(dim);
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 4;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 0, 10);
		monitorPanel.add(progressLabel, gbc);
		
		JLabel time = new JLabel("Time: ");
		time.setToolTipText(TOOL_TIP_TimeLabel);
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 4;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 5, 0, 0);
		monitorPanel.add(time, gbc);

		timerLabel = new JLabel("");
		Dimension memoryLabelDim = new Dimension(70, 25);
		timerLabel.setMinimumSize(memoryLabelDim);
		timerLabel.setPreferredSize(memoryLabelDim);
		gbc = new GridBagConstraints();
		gbc.gridx = 3;
		gbc.gridy = 4;
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
		gbc.gridy = 2;
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
		gbc.gridy = 3;
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
		gbc.gridy = 4;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 0, 10);
		monitorPanel.add(skipFileButton, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.weightx = 0;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(10, 0, 0, 5);
		
		topPanel.add(monitorPanel, gbc);
	}

	private void process() {
		tableModel.clear();

        currentWorker = new BatchProcessingWorker(files, tableModel, getVerificationOptions());

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

		if (!isQueryListEmpty() || listModel.size() > 0) {
			clearFilesButton.setEnabled(true);
			startButton.setEnabled(true);
		} else {
			clearFilesButton.setEnabled(false);
			startButton.setEnabled(false);
		}

        removeFileButton.setEnabled(fileList.getSelectedIndices().length > 0);
        exportButton.setEnabled(tableModel.getRowCount() > 0);

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
					boolean isResultColumn = table.getColumnName(column).equals("Result");
					boolean isQueryColumn = table.getColumnName(column).equals("Query");
					if (value != null) {
						if ((isResultColumn && value.toString().equals(SATISFIED_STRING) || value.toString().equals(SATISFIED_SOUNDNESS_STRING) ||
                            value.toString().equals(SATISFIED_STRONG_SOUNDNESS_STRING)) || (isQueryColumn && value.toString().equals("TRUE"))) {
                            setBackground(new Color(91, 255, 91)); // light green
                        } else if ((isResultColumn && (value.toString().equals(NOT_SATISFIED_STRING) || value.toString().equals(NOT_SATISFIED_STRING_STRONG_SOUNDNESS) ||
                            value.toString().equals(NOT_SATISFIED_STRING_SOUNDNESS))) || (isQueryColumn && value.toString().equals("FALSE"))) {
                            setBackground(new Color(255, 91, 91)); // light red
                        } else if (isResultColumn && value.toString().equals( "Inconclusive")) {
                            setBackground(new Color(255, 255, 120)); // light yellow
                        } else {
                            setBackground(table.getBackground());
                        }
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
                        || table.getColumnName(column).equals("Option")
                        || table.getColumnName(column).equals("Memory Usage")
                        || table.getColumnName(column).equals("Result")) {
                    setText(value.toString());
                    Point mousePos = table.getMousePosition();
                    BatchProcessingVerificationResult result = null;
                    if (mousePos != null) {
                        result = ((BatchProcessingResultsTableModel) table
                            .getModel()).getResult(table
                            .rowAtPoint(mousePos));
                    }

                    if (table.getColumnName(column).equals("Verification Time"))
                        setToolTipText(result != null ? generateStatsToolTipText(result) : value.toString());
                    else if (table.getColumnName(column).equals("Memory Usage"))
                        setToolTipText(result != null ? generateMemoryToolTipText(result) : value.toString());
                    else if (table.getColumnName(column).equals("Result"))
                        setToolTipText(generateResultToolTipText(result, value));
                    else
                        setToolTipText(result != null ? generateReductionString(result.query(), result.getOptionNumber()) : value.toString());
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
            return "Verification Time: " + (result.verificationTimeInMs() / 1000.0) + " s";
		}
		
		private String generateMemoryToolTipText(BatchProcessingVerificationResult result) {
            return "Peak memory usage (estimate): " + result.verificationMemory();
		}

		private String generateResultToolTipText(BatchProcessingVerificationResult result, Object value) {
		    if (result == null || result.getRawOutput() == null) return value.toString();
		    return result.getRawOutput();
        }

		private String generateTooltipTextFromQuery(TAPNQuery query) {
            return "Query Property:\n" + query.getProperty().toString();
		}

		private String generateReductionString(TAPNQuery query, int optionNumber) {
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
				} else if (query.getReductionOption() == ReductionOption.VerifyPN || query.getReductionOption() == ReductionOption.VerifyPNApprox || query.getReductionOption() == ReductionOption.VerifyPNReduce){
					s.append(name_UNTIMED);
				} else {
					s.append(name_BROADCAST);
				}

				s.append("\n\nEngine flags: \n");
				String options = (String) verificationTable.getValueAt(optionNumber, 2);
				if (options.equalsIgnoreCase("default")) {
				    s.append(currentWorker.getVerificationOptionsFromQuery(query).toString());
                } else {
				    if ((boolean) verificationTable.getValueAt(optionNumber, 3)) {
                        Pattern pattern = Pattern.compile("\\s*(-k|--k-bound)\\s*(\\d+)\\s*", Pattern.CASE_INSENSITIVE);
                        Matcher matcher = pattern.matcher(options);
                        if (matcher.find()) {
                            options = options.replaceFirst(matcher.group(), matcher.group(1) + " " + query.getCapacity() + " ");
                        } else {
                            options += " -k ";
                            options += query.getCapacity();
                        }
                    }
				    s.append(options);
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


