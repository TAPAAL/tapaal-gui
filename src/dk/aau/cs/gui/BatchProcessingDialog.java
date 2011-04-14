package dk.aau.cs.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker.StateValue;
import javax.swing.filechooser.FileNameExtensionFilter;

import dk.aau.cs.gui.components.TableModel;
import dk.aau.cs.verification.batchProcessing.BatchProcessingListener;
import dk.aau.cs.verification.batchProcessing.BatchProcessingWorker;
import dk.aau.cs.verification.batchProcessing.FileChangedEvent;
import dk.aau.cs.verification.batchProcessing.StatusChangedEvent;

public class BatchProcessingDialog extends JPanel {
	private static final long serialVersionUID = -5682084589335908227L;

	private JButton browseFilesButton;
	private JButton clearFilesButton;
	
	private JList fileList;
	private DefaultListModel listModel;
	private JLabel statusLabel;
	private JLabel fileStatusLabel;
	
	private TableModel tableModel;
	private JProgressBar progressBar;
	private JButton startButton;
	
	private List<File> files;
	private BatchProcessingWorker currentWorker;

	public BatchProcessingDialog() {	

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
		progressBar.setString("0 %"); // TODO: have this display "x / 20 tasks" or similar
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 0, 0);
		monitorPanel.add(progressBar, gbc);

		startButton = new JButton("Start");
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
					}else if((StateValue)evt.getNewValue() == StateValue.STARTED){
						disableButtons();
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
		fileListPanel.setBorder(BorderFactory.createTitledBorder("Files"));
				
		listModel = new DefaultListModel();
		fileList = new JList(listModel);
		fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		fileList.setSelectedIndex(0);
		JScrollPane scrollpane = new JScrollPane(fileList);
		scrollpane.setMinimumSize(new Dimension(175, 200));
		scrollpane.setPreferredSize(new Dimension(175, 200));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.gridwidth = 2;
		fileListPanel.add(scrollpane,gbc);
		
		browseFilesButton = new JButton("Select Files...");
		browseFilesButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				selectFiles();
			}
		});
		
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.insets = new Insets(0, 0, 0, 10);
		fileListPanel.add(browseFilesButton, gbc);
	
		clearFilesButton = new JButton("Clear");
		clearFilesButton.addActionListener(new ActionListener() {	
			@Override
			public void actionPerformed(ActionEvent e) {
				clearFiles();
			}
		});
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.insets = new Insets(0, 0, 0, 10);
		fileListPanel.add(clearFilesButton, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.anchor = GridBagConstraints.NORTHEAST;
		gbc.gridheight = 2;
		add(fileListPanel,gbc);
	}
	
	private void selectFiles() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("TAPAAL models","xml"));
		fileChooser.setMultiSelectionEnabled(true);
		int result = fileChooser.showOpenDialog(BatchProcessingDialog.this);
		if(result == JFileChooser.APPROVE_OPTION){
			File[] filesArray = fileChooser.getSelectedFiles();
			files = new ArrayList<File>(filesArray.length);
			listModel.clear();
			for(File file : filesArray) {
				files.add(file);
				listModel.addElement(file.getName());
			}
		}
	}
	
	private void clearFiles() {
		files.clear();
		listModel.removeAllElements();
	}
	
	private void disableButtons() {
		browseFilesButton.setEnabled(false);
		clearFilesButton.setEnabled(false);
		startButton.setEnabled(false);
		
	}

	private void enableButtons() {
		browseFilesButton.setEnabled(true);
		clearFilesButton.setEnabled(true);
		startButton.setEnabled(true);
	}
}
