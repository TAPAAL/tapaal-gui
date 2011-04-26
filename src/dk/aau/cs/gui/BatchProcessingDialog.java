package dk.aau.cs.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker.StateValue;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableCellRenderer;
import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.gui.CreateGui;
import pipe.gui.widgets.FileBrowser;
import dk.aau.cs.gui.components.BatchProcessingResultsTableModel;
import dk.aau.cs.gui.components.MultiLineAutoWrappingToolTip;
import dk.aau.cs.io.batchProcessing.BatchProcessingResultsExporter;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.verification.batchProcessing.BatchProcessingListener;
import dk.aau.cs.verification.batchProcessing.BatchProcessingVerificationOptions;
import dk.aau.cs.verification.batchProcessing.BatchProcessingWorker;
import dk.aau.cs.verification.batchProcessing.FileChangedEvent;
import dk.aau.cs.verification.batchProcessing.StatusChangedEvent;
import dk.aau.cs.verification.batchProcessing.BatchProcessingVerificationOptions.QueryPropertyOption;

public class BatchProcessingDialog extends JDialog {
	private static final long serialVersionUID = -5682084589335908227L;
	
	private static final String name_verifyTAPN = "VerifyTAPN";
	private static final String name_OPTIMIZEDSTANDARD = "UPPAAL: Optimised Standard Reduction";
	private static final String name_STANDARD = "UPPAAL: Standard Reduction";
	private static final String name_BROADCAST = "UPPAAL: Broadcast Reduction";
	private static final String name_BROADCASTDEG2 = "UPPAAL: Broadcast Degree 2 Reduction";
	private static final String name_BFS = "Breadth First Search";
	private static final String name_DFS = "Depth First Search";
	private static final String name_RandomDFS = "Random Depth First Search";
	private static final String name_ClosestToTarget = "Search by Closest To Target First";
	private static final String name_KeepQueryOption = "Let Query Decide";
	private static final String name_SEARCHWHOLESTATESPACE = "Search Whole State Space";

	private JPanel filesButtonsPanel;
	private JButton addFilesButton;
	private JButton clearFilesButton;
	private JButton removeFilesButton;
	private JList fileList;
	private DefaultListModel listModel;
	
	private JLabel statusLabel;
	private JLabel fileStatusLabel;
	private JProgressBar progressBar;
	private JButton startButton;
	private JButton cancelButton;
	private JButton skipFileButton;
	
	private JCheckBox overrideVerificationOptionsCheckbox;
	private JComboBox reductionOption;
	
	private BatchProcessingResultsTableModel tableModel;
	
	private List<File> files = new ArrayList<File>();
	private BatchProcessingWorker currentWorker;

	private JCheckBox symmetryReduction;
	private JPanel reductionOptionsPanel;
	private JPanel searchOptionsPanel;
	private JComboBox searchOption;

	private JButton exportButton;

	private JComboBox queryPropertyOption;

	private JPanel queryPropertyOptionsPanel;

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
		setLayout(new GridBagLayout());
		
		initFileListPanel();
		initVerificationOptionsPanel();
		initMonitorPanel();
		initResultTablePanel();		
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
				if(e.getKeyCode() == KeyEvent.VK_DELETE) {
					removeSelectedFiles();
				}
			}
		});
		
		fileList.addListSelectionListener(new ListSelectionListener() {	

			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					if (fileList.getSelectedIndex() == -1) {
						removeFilesButton.setEnabled(false);
					} else {
						removeFilesButton.setEnabled(true);
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
		fileListPanel.add(scrollpane,gbc);
		
		filesButtonsPanel = new JPanel(new GridBagLayout());
		
		addFilesButton = new JButton("Add Models");
		addFilesButton.addActionListener(new ActionListener(){
			
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
	
		removeFilesButton = new JButton("Remove Models");
		removeFilesButton.setEnabled(false);
		removeFilesButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				removeSelectedFiles();		
			}
		});
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 0, 5);
		filesButtonsPanel.add(removeFilesButton, gbc);
		
		clearFilesButton = new JButton("Clear");
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
		fileListPanel.add(filesButtonsPanel,gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.NORTHEAST;
		gbc.gridheight = 2;
		gbc.insets = new Insets(10, 0, 0, 10);
		add(fileListPanel,gbc);
	}
	
	private void addFiles() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setFileFilter(new FileNameExtensionFilter("TAPAAL models", new String[] { "xml" }));
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setMultiSelectionEnabled(true);
		int result = fileChooser.showOpenDialog(BatchProcessingDialog.this);
		if(result == JFileChooser.APPROVE_OPTION){
			File[] filesArray = fileChooser.getSelectedFiles();
			for(File file : filesArray) {
				if(!files.contains(file)) {
					files.add(file);
					listModel.addElement(file);
				}
			}
			
			enableButtons();
					
		}
	}
	
	private void removeSelectedFiles() {
		for(Object o : fileList.getSelectedValues()) {
			File file = (File)o;
			files.remove(file);
			listModel.removeElement(file);
		}
		
		enableButtons();
	}

	private void initVerificationOptionsPanel() {
		JPanel verificationOptionsPanel = new JPanel(new GridBagLayout());
		verificationOptionsPanel.setBorder(BorderFactory.createTitledBorder("Verification Options"));
		
		overrideVerificationOptionsCheckbox = new JCheckBox("Override verification options");
		overrideVerificationOptionsCheckbox.setSelected(false);
		overrideVerificationOptionsCheckbox.addActionListener(new ActionListener() {
		
			public void actionPerformed(ActionEvent e) {
				if(overrideVerificationOptionsCheckbox.isSelected())
					enabledVerificationOptionButtons();
				else
					disableVerificationOptionsButtons();
			}
		});
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		verificationOptionsPanel.add(overrideVerificationOptionsCheckbox, gbc);
		
		initQueryPropertyOptionsPanel(verificationOptionsPanel);
		initReductionOptionsPanel(verificationOptionsPanel);
		initSearchOptionsPanel(verificationOptionsPanel);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHEAST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weighty = 1.0;
		gbc.weightx = 0;
		gbc.insets = new Insets(10, 0, 0, 0);
		add(verificationOptionsPanel, gbc);
		
		disableVerificationOptionsButtons();
	}
	
	private void initQueryPropertyOptionsPanel(JPanel verificationOptionsPanel) {
		queryPropertyOptionsPanel = new JPanel(new GridBagLayout());
		queryPropertyOptionsPanel.setBorder(BorderFactory.createTitledBorder("Choose Query Property"));
		queryPropertyOptionsPanel.setEnabled(false);
		
		String[] options = new String[] { name_KeepQueryOption, name_SEARCHWHOLESTATESPACE };
		queryPropertyOption = new JComboBox(options);
		queryPropertyOption.setEnabled(false);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		queryPropertyOptionsPanel.add(queryPropertyOption);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(0, 0, 10, 0);
		verificationOptionsPanel.add(queryPropertyOptionsPanel, gbc);
	}


	private void initReductionOptionsPanel(JPanel queryOptionsPanel) {
		reductionOptionsPanel = new JPanel(new GridBagLayout());
		reductionOptionsPanel.setBorder(BorderFactory.createTitledBorder("Choose Reduction Method"));
		reductionOptionsPanel.setEnabled(false);
		
		String[] options = new String[] { name_KeepQueryOption, name_verifyTAPN, name_OPTIMIZEDSTANDARD, name_STANDARD, name_BROADCAST, name_BROADCASTDEG2};
		reductionOption = new JComboBox(options);
		reductionOption.setEnabled(false);
		
		reductionOption.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox source = (JComboBox)e.getSource();
				String selectedItem = (String)source.getSelectedItem();
				if(selectedItem != null) {
					setEnabledOptionsAccordingToCurrentReduction();
				}
			}
		});
	
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		reductionOptionsPanel.add(reductionOption,gbc);
		

		symmetryReduction = new JCheckBox("Use Symmetry Reduction");
		symmetryReduction.setEnabled(false);
		symmetryReduction.setSelected(true);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		reductionOptionsPanel.add(symmetryReduction, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		gbc.weightx = 0.5;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(0, 10, 0, 0);
		queryOptionsPanel.add(reductionOptionsPanel, gbc);
	}
	
	private void setEnabledOptionsAccordingToCurrentReduction() {
		refreshSymmetryReduction();
		refreshSearchOptions();
	}
	
	private void refreshSymmetryReduction() {
		if(((String)reductionOption.getSelectedItem()).equals(name_verifyTAPN)) {
			symmetryReduction.setSelected(true);
			symmetryReduction.setEnabled(false);
		}
		else{
			symmetryReduction.setSelected(symmetryReduction.isSelected());
			symmetryReduction.setEnabled(true);
		}
	}
	
	private void refreshSearchOptions() {
		String currentSearchOption = getSearchOptionAsString();

		String[] options;
		if(getReductionOption() == ReductionOption.VerifyTAPN)
		{
			options = new String[] { name_KeepQueryOption, name_BFS, name_DFS };
		}
		else {
			options = new String[] { name_KeepQueryOption, name_BFS, name_DFS, name_RandomDFS, name_ClosestToTarget };
		}
		
		Dimension d = searchOption.getSize();
		searchOption.removeAllItems();
		boolean selectedOptionStillAvailable = false;	
		for (String s : options) {
			searchOption.addItem(s);
			if (s.equals(currentSearchOption)) {
				selectedOptionStillAvailable = true;
			}
		}
		searchOption.setMinimumSize(d); // stop dropdown box from automatically resizing when we remove options with "longer" names.
		searchOption.setPreferredSize(d);

		if (selectedOptionStillAvailable) {
			searchOption.setSelectedItem(currentSearchOption);
		}
	}
	
	private SearchOption getSearchOption() {
		if(((String)searchOption.getSelectedItem()).equals(name_DFS))
			return SearchOption.DFS;
		else if(((String)searchOption.getSelectedItem()).equals(name_ClosestToTarget))
			return SearchOption.CLOSE_TO_TARGET_FIRST;
		else if(((String)searchOption.getSelectedItem()).equals(name_RandomDFS))
			return SearchOption.RDFS;
		else if(((String)searchOption.getSelectedItem()).equals(name_KeepQueryOption))
			return SearchOption.BatchProcessingKeepQueryOption;
		else
			return SearchOption.BFS;
	}
	
	private String getSearchOptionAsString() {
		return (String)searchOption.getSelectedItem();
	}


	private void disableVerificationOptionsButtons() {
		reductionOptionsPanel.setEnabled(false);		
		for(Component c : reductionOptionsPanel.getComponents())
			c.setEnabled(false);
		
		searchOptionsPanel.setEnabled(false);
		for(Component c : searchOptionsPanel.getComponents())
			c.setEnabled(false);
		
		queryPropertyOptionsPanel.setEnabled(false);
		for(Component c : queryPropertyOptionsPanel.getComponents()) {
			c.setEnabled(false);
		}
	}

	private void enabledVerificationOptionButtons() {
		reductionOptionsPanel.setEnabled(true);
		for(Component c : reductionOptionsPanel.getComponents())
			c.setEnabled(true);
		
		searchOptionsPanel.setEnabled(true);
		for(Component c : searchOptionsPanel.getComponents())
			c.setEnabled(true);
		
		queryPropertyOptionsPanel.setEnabled(true);
		for(Component c : queryPropertyOptionsPanel.getComponents()) {
			c.setEnabled(true);
		}
	}
	
	private BatchProcessingVerificationOptions getVerificationOptions() {
		if(overrideVerificationOptionsCheckbox.isSelected()) {
			return new BatchProcessingVerificationOptions(getQueryPropertyOption(), getSearchOption(), getReductionOption());		
		}
		return null;
	}
	
	private QueryPropertyOption getQueryPropertyOption() {
		String propertyOptionString = (String)queryPropertyOption.getSelectedItem();
		if(propertyOptionString.equals(name_SEARCHWHOLESTATESPACE))
			return QueryPropertyOption.SearchWholeStateSpace;
		else
			return QueryPropertyOption.KeepQueryOption;
	}


	private ReductionOption getReductionOption() {
		String reductionOptionString = (String)reductionOption.getSelectedItem();
		boolean symmetry = symmetryReduction.isSelected();
		
		if (reductionOptionString.equals(name_STANDARD) && !symmetry)
			return ReductionOption.STANDARD;
		else if (reductionOptionString.equals(name_STANDARD) && symmetry)
			return ReductionOption.STANDARDSYMMETRY;
		else if (reductionOptionString.equals(name_OPTIMIZEDSTANDARD) && !symmetry)
			return ReductionOption.OPTIMIZEDSTANDARD;
		else if (reductionOptionString.equals(name_OPTIMIZEDSTANDARD) && symmetry)
			return ReductionOption.OPTIMIZEDSTANDARDSYMMETRY;
		else if (reductionOptionString.equals(name_BROADCAST) && !symmetry)
			return ReductionOption.BROADCAST;
		else if (reductionOptionString.equals(name_BROADCASTDEG2) && !symmetry)
			return ReductionOption.DEGREE2BROADCAST;
		else if (reductionOptionString.equals(name_BROADCASTDEG2) && symmetry)
			return ReductionOption.DEGREE2BROADCASTSYMMETRY;
		else if (reductionOptionString.equals(name_verifyTAPN))
			return ReductionOption.VerifyTAPN;
		else if (reductionOptionString.equals(name_KeepQueryOption))
			return ReductionOption.BatchProcessingKeepQueryOption;
		else
			return ReductionOption.BROADCASTSYMMETRY;
	}
	
	private void initSearchOptionsPanel(JPanel queryOptionsPanel) {
		searchOptionsPanel = new JPanel(new GridBagLayout());
		searchOptionsPanel.setBorder(BorderFactory.createTitledBorder("Choose Search Method"));
		searchOptionsPanel.setEnabled(false);
		
		String[] options = new String[] { name_KeepQueryOption, name_BFS, name_DFS, name_RandomDFS };
		searchOption = new JComboBox(options);
		searchOption.setEnabled(false);
		searchOption.setMinimumSize(searchOption.getSize());
	
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		searchOptionsPanel.add(searchOption,gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.weightx = 0.5;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(10, 10, 0, 0);
		queryOptionsPanel.add(searchOptionsPanel, gbc);
	}

	private void initResultTablePanel() {
		JPanel resultTablePanel = new JPanel(new GridBagLayout());
		resultTablePanel.setBorder(BorderFactory.createTitledBorder("Results"));
		
		exportButton = new JButton("Export");
		exportButton.setEnabled(false);
		exportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String filename = new FileBrowser("CSV file", "csv", "").saveFile();
				if (filename != null) {
					File exportFile = new File(filename);
					BatchProcessingResultsExporter exporter = new BatchProcessingResultsExporter();
					try {
						exporter.exportToCSV(tableModel.getResults(), exportFile);
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(CreateGui.getApp(), "An error occurred while trying to export the results. Please try again", "Error Exporting Results", JOptionPane.ERROR_MESSAGE);
						e1.printStackTrace();
					}
				}
			}
		});
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(0, 0, 5, 0);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		resultTablePanel.add(exportButton, gbc);
		
		tableModel = new BatchProcessingResultsTableModel();
		JTable table = new JTable(tableModel) {
			private static final long serialVersionUID = -146530769055564619L;

			public javax.swing.JToolTip createToolTip() {
				return new MultiLineAutoWrappingToolTip();
			};
		};
		table.getColumn("Query").setCellRenderer(new QueryCellRenderer(true));
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		Dimension scrollPaneDims = new Dimension(850,400);
		scrollPane.setMinimumSize(scrollPaneDims);
		scrollPane.setPreferredSize(scrollPaneDims);
		
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		resultTablePanel.add(scrollPane,gbc);
		
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 4;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.insets = new Insets(10, 5, 5, 5);
		add(resultTablePanel,gbc);
	}

	private void initMonitorPanel() {
		JPanel monitorPanel = new JPanel(new GridBagLayout());
		monitorPanel.setBorder(BorderFactory.createTitledBorder("Monitor"));
	
		JLabel file = new JLabel("File:");
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		monitorPanel.add(file,gbc);
			
		fileStatusLabel = new JLabel("");
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		monitorPanel.add(fileStatusLabel,gbc);
		
		JLabel status = new JLabel("Status:");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		monitorPanel.add(status,gbc);
			
		statusLabel = new JLabel("");
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		monitorPanel.add(statusLabel,gbc);
		
		JLabel progressLabel = new JLabel("Progress: ");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.WEST;
		monitorPanel.add(progressLabel,gbc);
		
		progressBar = new JProgressBar();
		Dimension progressBarDim = new Dimension(400, 25);
		progressBar.setMinimumSize(progressBarDim);
		progressBar.setPreferredSize(progressBarDim);
		progressBar.setStringPainted(true);
		progressBar.setString("0%");
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 0, 0);
		monitorPanel.add(progressBar, gbc);

		startButton = new JButton("Start");
		startButton.setEnabled(false);
		startButton.addActionListener(new ActionListener(){
		
			public void actionPerformed(ActionEvent e) {
				process();
			}			
		});
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0,0,0, 10);
		monitorPanel.add(startButton, gbc);
		
		cancelButton = new JButton("Cancel");
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
		gbc.gridx = 3;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0,0,0, 10);
		monitorPanel.add(cancelButton, gbc);
		
		skipFileButton = new JButton("Skip");
		skipFileButton.setEnabled(false);
		skipFileButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				skipCurrentFile();
			}
		});
		gbc = new GridBagConstraints();
		gbc.gridx = 3;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0,0,0, 10);
		monitorPanel.add(skipFileButton, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.SOUTHEAST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 0;
		gbc.insets = new Insets(10, 0, 0, 0);
		add(monitorPanel,gbc);
	}


	private void process() {
		tableModel.clear();
		currentWorker = new BatchProcessingWorker(files, tableModel, getVerificationOptions());
		currentWorker.addPropertyChangeListener(new PropertyChangeListener(){
		
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals("progress")) {
					int progress = (Integer)evt.getNewValue();
					progressBar.setValue(progress);
					progressBar.setString(progress + "%");
				}else if(evt.getPropertyName().equals("state")){
					if((StateValue)evt.getNewValue() == StateValue.DONE){
						enableButtons();
						cancelButton.setEnabled(false);
						skipFileButton.setEnabled(false);
					}else if((StateValue)evt.getNewValue() == StateValue.STARTED){
						disableButtonsDuringProcessing();
						cancelButton.setEnabled(true);
						skipFileButton.setEnabled(true);
					}
				}
			}
		});
		currentWorker.addBatchProcessingListener(new BatchProcessingListener() {
			
			public void fireStatusChanged(StatusChangedEvent e) {
				statusLabel.setText(e.status());		
			}

	
			public void fireFileChanged(FileChangedEvent e) {
				fileStatusLabel.setText(e.fileName());
			}
		});
		
		currentWorker.execute();
	}


	private void terminateBatchProcessing() {
		if(currentWorker != null && !currentWorker.isDone()){
			boolean cancelled = false;
			do{
				currentWorker.notifyExiting();
				cancelled = currentWorker.cancel(true);
			}while(!cancelled);
			System.out.print("Batch processing terminated");
		}
	}
	
	private void skipCurrentFile() {
		if(currentWorker != null && !currentWorker.isDone()){
			currentWorker.notifySkipCurrentVerification();
		}
	}
	
	
	private void clearFiles() {
		files.clear();
		listModel.removeAllElements();
	}
	
	private void disableButtonsDuringProcessing() {
		addFilesButton.setEnabled(false);
		clearFilesButton.setEnabled(false);
		startButton.setEnabled(false);
		exportButton.setEnabled(false);
		
		overrideVerificationOptionsCheckbox.setEnabled(false);
		disableVerificationOptionsButtons();
	}

	private void enableButtons() {
		addFilesButton.setEnabled(true);
		
		if(listModel.size() > 0) {
			clearFilesButton.setEnabled(true);
			startButton.setEnabled(true);
		} else {
			clearFilesButton.setEnabled(false);
			startButton.setEnabled(false);
		}
		
		if(tableModel.getRowCount() > 0) 
			exportButton.setEnabled(true);
		else
			exportButton.setEnabled(false);
		
		overrideVerificationOptionsCheckbox.setEnabled(true);
		if(overrideVerificationOptionsCheckbox.isSelected())
			enabledVerificationOptionButtons();
		else
			disableVerificationOptionsButtons();
	}
	
	// Custom cell renderer for the file list to only display the name of the file
	// instead of the whole path.
	private class FileNameCellRenderer extends JLabel implements ListCellRenderer {
		private static final long serialVersionUID = 3071924451912979500L;

		
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			if(value instanceof File)
				setText(((File)value).getName());
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
	
	// Custom cell renderer for the Query Column of the result table display the property of the query
	private class QueryCellRenderer extends JLabel  implements TableCellRenderer {
		private static final long serialVersionUID = 3054497986242852099L;
		Border unselectedBorder = null;
	    Border selectedBorder = null;
	    boolean isBordered = true;
	
	    public QueryCellRenderer(boolean isBordered) {
	        this.isBordered = isBordered;
	    }

		public Component getTableCellRendererComponent(JTable table, Object query, boolean isSelected, boolean hasFocus, int row, int column) {
			if (isBordered) {
				if (isSelected) {
					setBackground(table.getSelectionBackground());
					setForeground(table.getSelectionForeground());
					
					if (selectedBorder == null) {
						selectedBorder = BorderFactory.createMatteBorder(2,5,2,5, table.getSelectionBackground());
					}
					setBorder(selectedBorder);
				} else {
					setBackground(table.getBackground());
					setForeground(table.getForeground());
					
					if (unselectedBorder == null) {
						unselectedBorder = BorderFactory.createMatteBorder(2,5,2,5, table.getBackground());
					}
					setBorder(unselectedBorder);
				}
			}
			
			
			setEnabled(table.isEnabled());
			setFont(table.getFont());
			setOpaque(true);
			
			if(query instanceof TAPNQuery) {
				TAPNQuery newQuery = (TAPNQuery)query;
				setToolTipText(generateTooltipTextFromQuery(newQuery));
				setText(newQuery.getName());
			}
			else {
				setText(query.toString());
			}
			return this;
		}

		private String generateTooltipTextFromQuery(TAPNQuery newQuery) {
			StringBuilder s = new StringBuilder();
			s.append("Extra Tokens: ");
			s.append(newQuery.getCapacity());
			s.append("\n\n");
			
			s.append("Search Method: \n");
			if(newQuery.getSearchOption() == SearchOption.DFS)
				s.append(name_DFS);
			else if(newQuery.getSearchOption() == SearchOption.RDFS)
				s.append(name_RandomDFS);
			else if(newQuery.getSearchOption() == SearchOption.CLOSE_TO_TARGET_FIRST)
				s.append(name_ClosestToTarget);
			else
				s.append(name_BFS);
			s.append("\n\n");
			
			s.append("Reduction: \n");
			boolean symmetry = false;
			if (newQuery.getReductionOption() == ReductionOption.STANDARD)
				s.append(name_STANDARD);
			else if (newQuery.getReductionOption() == ReductionOption.STANDARDSYMMETRY) {
				s.append(name_STANDARD);
				symmetry = true;
			} else if (newQuery.getReductionOption() == ReductionOption.OPTIMIZEDSTANDARD)
				s.append(name_OPTIMIZEDSTANDARD);
			else if (newQuery.getReductionOption() == ReductionOption.OPTIMIZEDSTANDARDSYMMETRY) {
				s.append(name_OPTIMIZEDSTANDARD);
				symmetry = true;
			} else if (newQuery.getReductionOption() == ReductionOption.BROADCAST)
				s.append(name_BROADCAST);
			else if (newQuery.getReductionOption() == ReductionOption.DEGREE2BROADCAST)
				s.append(name_BROADCASTDEG2);
			else if (newQuery.getReductionOption() == ReductionOption.DEGREE2BROADCASTSYMMETRY) {
				s.append(name_BROADCASTDEG2);
				symmetry = true;
			} else if (newQuery.getReductionOption() == ReductionOption.VerifyTAPN) {
				s.append(name_verifyTAPN);
				symmetry = true;
			} else {
				s.append(name_BROADCAST);
				symmetry = true;
			}
			
			s.append("\n\n");
			s.append("Symmetry: ");
			s.append(symmetry ? "Yes\n\n" : "No\n\n");
			
			s.append("Query Property:\n" + newQuery.getProperty().toString());
			
			return s.toString();
		}
	}
}
