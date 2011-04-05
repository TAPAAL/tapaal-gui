package dk.aau.cs.gui;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import dk.aau.cs.gui.components.TableModel;

public class BatchProcessingDialog extends JPanel {
	private TableModel tableModel;
	private JProgressBar progressBar;
	private JButton startButton;
	private JButton browseFilesButton;
	
	private Collection<File> files;
	
	
	public BatchProcessingDialog() {
		
		
		
		initComponents();
	}


	private void initComponents() {
		setLayout(new GridBagLayout());

		Dimension textFieldDims = new Dimension(400, 25);
		
		browseFilesButton = new JButton("Select Files...");
		browseFilesButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setMultiSelectionEnabled(true);
				int result = fileChooser.showOpenDialog(BatchProcessingDialog.this);
				if(result == JFileChooser.APPROVE_OPTION){
					files = Arrays.asList(fileChooser.getSelectedFiles());
					// TODO: populate table already now ?
				}
			}
		});

		GridBagConstraints gbc = new GridBagConstraints();
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 4;
		gbc.insets = new Insets(0, 0, 0, 10);
		add(browseFilesButton, gbc);

		JLabel progressLabel = new JLabel("Progress: ");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		add(progressLabel,gbc);
		
		progressBar = new JProgressBar();
		progressBar.setMinimumSize(textFieldDims);
		progressBar.setPreferredSize(textFieldDims);
		progressBar.setStringPainted(true);
		progressBar.setString("0 / 0"); // TODO: have this display "x / 20 tasks" or similar
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 0, 0);
		add(progressBar, gbc);

		startButton = new JButton("Start");
		startButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				
			}			
		});
		gbc = new GridBagConstraints();
		gbc.gridx = 3;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0,0,0, 10);
		add(startButton, gbc);

		tableModel = new TableModel();
		JTable table = new JTable(tableModel);
		JScrollPane scrollPane = new JScrollPane(table);
		Dimension scrollPaneDims = new Dimension(800,400);
		scrollPane.setMinimumSize(scrollPaneDims);
		scrollPane.setPreferredSize(scrollPaneDims);
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 4;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.insets = new Insets(10, 10, 0, 10);
		add(scrollPane, gbc);		
	}
}
