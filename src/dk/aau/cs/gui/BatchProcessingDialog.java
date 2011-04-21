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
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolTip;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker.StateValue;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.TAPNQuery.SearchOption;
import dk.aau.cs.gui.components.BatchProcessingResultsTableModel;
import dk.aau.cs.gui.components.MultiLineAutoWrappingToolTip;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.verification.batchProcessing.BatchProcessingListener;
import dk.aau.cs.verification.batchProcessing.BatchProcessingVerificationOptions;
import dk.aau.cs.verification.batchProcessing.BatchProcessingWorker;
import dk.aau.cs.verification.batchProcessing.FileChangedEvent;
import dk.aau.cs.verification.batchProcessing.StatusChangedEvent;

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
			@Override
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
		gbc.insets = new Insets(0, 0, 10, 0);
		fileListPanel.add(scrollpane,gbc);
		
		filesButtonsPanel = new JPanel(new GridBagLayout());
		
		addFilesButton = new JButton("Add Models");
		addFilesButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				addFiles();
			}
		});
		
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(0, 0, 0, 5);
		filesButtonsPanel.add(addFilesButton, gbc);
	
		removeFilesButton = new JButton("Remove Models");
		removeFilesButton.setEnabled(false);
		removeFilesButton.addActionListener(new ActionListener() {
			@Override
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
			@Override
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
			@Override
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
		gbc.anchor = GridBagConstraints.NORTHWEST;
		verificationOptionsPanel.add(overrideVerificationOptionsCheckbox, gbc);
		
		initReductionOptionsPanel(verificationOptionsPanel);
		initSearchOptionsPanel(verificationOptionsPanel);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(10, 0, 0, 0);
		add(verificationOptionsPanel, gbc);
		
		disableVerificationOptionsButtons();
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
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.weightx = 0.5;
		gbc.weighty = 0.5;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(10, 10, 0, 0);
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
		
		searchOption.removeAllItems();
		boolean selectedOptionStillAvailable = false;	
		for (String s : options) {
			searchOption.addItem(s);
			if (s.equals(currentSearchOption)) {
				selectedOptionStillAvailable = true;
			}
		}

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
	}

	private void enabledVerificationOptionButtons() {
		reductionOptionsPanel.setEnabled(true);
		for(Component c : reductionOptionsPanel.getComponents())
			c.setEnabled(true);
		
		searchOptionsPanel.setEnabled(true);
		for(Component c : searchOptionsPanel.getComponents())
			c.setEnabled(true);
	}
	
	private BatchProcessingVerificationOptions getVerificationOptions() {
		if(overrideVerificationOptionsCheckbox.isSelected()) {
			return new BatchProcessingVerificationOptions(getSearchOption(), getReductionOption());		
		}
		return null;
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
		
		String[] options = new String[] { name_KeepQueryOption, name_BFS, name_DFS, name_RandomDFS, name_ClosestToTarget };
		searchOption = new JComboBox(options);
		searchOption.setEnabled(false);
	
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		searchOptionsPanel.add(searchOption,gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0.5;
		gbc.weighty = 0.5;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(10, 10, 0, 0);
		queryOptionsPanel.add(searchOptionsPanel, gbc);
	}

	private void initResultTablePanel() {
		JPanel resultTablePanel = new JPanel(new GridBagLayout());
		resultTablePanel.setBorder(BorderFactory.createTitledBorder("Results"));
		tableModel = new BatchProcessingResultsTableModel();
		JTable table = new JTable(tableModel);
//		{
//			public javax.swing.JToolTip createToolTip() {
//				return new MultiLineAutoWrappingToolTip();
//			};
//		};
		table.getColumn("Query").setCellRenderer(new QueryCellRenderer(true));
		JScrollPane scrollPane = new JScrollPane(table);
		Dimension scrollPaneDims = new Dimension(850,400);
		scrollPane.setMinimumSize(scrollPaneDims);
		scrollPane.setPreferredSize(scrollPaneDims);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = 0;
		gbc.gridy = 2;
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
			@Override
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
			@Override
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
			@Override
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
		gbc.anchor = GridBagConstraints.SOUTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(10, 0, 0, 0);
		add(monitorPanel,gbc);
	}


	private void process() {
		tableModel.clear();
		currentWorker = new BatchProcessingWorker(files, tableModel, getVerificationOptions());
		currentWorker.addPropertyChangeListener(new PropertyChangeListener(){
			@Override
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
						disableButtons();
						cancelButton.setEnabled(true);
						skipFileButton.setEnabled(true);
					}
				}
			}
		});
		currentWorker.addBatchProcessingListener(new BatchProcessingListener() {
			@Override
			public void fireStatusChanged(StatusChangedEvent e) {
				statusLabel.setText(e.status());		
			}

			@Override
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
	
	private void disableButtons() {
		addFilesButton.setEnabled(false);
		clearFilesButton.setEnabled(false);
		startButton.setEnabled(false);
		
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
	}
	
	// Custom cell renderer for the file list to only display the name of the file
	// instead of the whole path.
	private class FileNameCellRenderer extends JLabel implements ListCellRenderer {
		private static final long serialVersionUID = 3071924451912979500L;

		@Override
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
				setToolTipText(newQuery.getProperty().toString());
				setText(newQuery.getName());
			}
			else {
				setText(query.toString());
			}
			return this;
		}
	}
}
