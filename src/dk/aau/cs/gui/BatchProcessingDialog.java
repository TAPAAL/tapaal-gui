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
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker.StateValue;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import dk.aau.cs.gui.components.TableModel;
import dk.aau.cs.verification.batchProcessing.BatchProcessingListener;
import dk.aau.cs.verification.batchProcessing.BatchProcessingWorker;
import dk.aau.cs.verification.batchProcessing.FileChangedEvent;
import dk.aau.cs.verification.batchProcessing.StatusChangedEvent;

public class BatchProcessingDialog extends JDialog {
	private static final long serialVersionUID = -5682084589335908227L;

	private JButton addFilesButton;
	private JButton clearFilesButton;
	
	private JList fileList;
	private DefaultListModel listModel;
	private JLabel statusLabel;
	private JLabel fileStatusLabel;
	
	private TableModel tableModel;
	private JProgressBar progressBar;
	private JButton startButton;
	private JButton cancelButton;
	private JPanel filesButtonsPanel;
	
	private List<File> files = new ArrayList<File>();
	private BatchProcessingWorker currentWorker;

	private JButton removeFilesButton;

	private JButton skipFileButton;

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
		initMonitorPanel();
		initResultTablePanel();		
	}


	private void initResultTablePanel() {
		JPanel resultTablePanel = new JPanel(new GridBagLayout());
		resultTablePanel.setBorder(BorderFactory.createTitledBorder("Results"));
		tableModel = new TableModel();
		JTable table = new JTable(tableModel);
		JScrollPane scrollPane = new JScrollPane(table);
		Dimension scrollPaneDims = new Dimension(800,400);
		scrollPane.setMinimumSize(scrollPaneDims);
		scrollPane.setPreferredSize(scrollPaneDims);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 3;
		resultTablePanel.add(scrollPane,gbc);
		
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 4;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.insets = new Insets(10, 10, 0, 10);
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
		add(monitorPanel,gbc);
	}


	private void process() {
		tableModel.clear();
		currentWorker = new BatchProcessingWorker(files, tableModel);
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
		scrollpane.setMinimumSize(new Dimension(175, 200));
		scrollpane.setPreferredSize(new Dimension(175, 200));
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 3;
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
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.anchor = GridBagConstraints.NORTHEAST;
		gbc.gridheight = 2;
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
}
