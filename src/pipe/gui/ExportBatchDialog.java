package pipe.gui;

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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.DOMException;
import dk.aau.cs.gui.FileNameCellRenderer;
import dk.aau.cs.io.LoadedModel;
import dk.aau.cs.io.ModelLoader;
import dk.aau.cs.io.PNMLWriter;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import pipe.dataLayer.DataLayer;
import pipe.gui.widgets.FileBrowser;

public class ExportBatchDialog extends JDialog {

	private static final long serialVersionUID = 8346966786414688380L;
	//Tool tip strings
	//Tool tips for model panel
	private final static String TOOL_TIP_AddFilesButton = "Press to add nets to batch processing";
	private final static String TOOL_TIP_RemoveFilesButton = "Press to remove the currently selected nets";
	private final static String TOOL_TIP_ClearFilesButton = "Press to remove all nets from list";
	private final static String TOOL_TIP_ExportFilesButton = "Press to export all nets in PNML and XML format";
	
	private JPanel filesButtonsPanel;
	private JButton addFilesButton;
	private JButton clearFilesButton;
	private JButton removeFileButton;
	private JButton exportFilesButton;
	private JList fileList;
	private DefaultListModel listModel;
	private List<File> files = new ArrayList<File>();
	private JFileChooser chooser;

	static ExportBatchDialog exportBatchDialog;
	ModelLoader loader = new ModelLoader(new DrawingSurfaceImpl(new DataLayer()));
	
	public static void ShowExportBatchDialog(){
		if(exportBatchDialog == null){
			exportBatchDialog = new ExportBatchDialog(CreateGui.getApp(), "Batch Export", true);
			exportBatchDialog.pack();
			exportBatchDialog.setPreferredSize(exportBatchDialog.getSize());
			//Set the minimum size to 150 less than the preferred, to be consistent with the minimum size of the result panel
			exportBatchDialog.setMinimumSize(new Dimension(exportBatchDialog.getWidth(), exportBatchDialog.getHeight()));
			exportBatchDialog.setLocationRelativeTo(null);
			exportBatchDialog.setResizable(true);
		}
		exportBatchDialog.setVisible(true);
	}
	
	private ExportBatchDialog(Frame frame, String title, boolean modal) {
		super(frame, title, modal);
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				
			}
		});
		initFileList();
	}
	
	private void initFileList() {
		JPanel fileListPanel = new JPanel(new GridBagLayout());
		fileListPanel.setBorder(BorderFactory.createTitledBorder("Files"));

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
				enableButtons();
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
				enableButtons();
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
		
		exportFilesButton = new JButton("Export All Nets and Queries");
		exportFilesButton.setToolTipText(TOOL_TIP_ExportFilesButton);
		exportFilesButton.setEnabled(true);
		exportFilesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportFiles();
				enableButtons();
			}
			private void exportFiles() {
				chooser = new JFileChooser(); 
			    chooser.setCurrentDirectory(new java.io.File("."));
			    chooser.setDialogTitle("Export Batch");
			    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			    chooser.setAcceptAllFileFilterUsed(false);
			    if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION && !(files.isEmpty())) { 
			    	File destinationFile = chooser.getSelectedFile();
			    	try {
			    		for(File file : files) {
					    	Path path = Paths.get(destinationFile.getAbsolutePath() + "/" + file.getName().replaceAll(".xml", ""));
			    			Files.createDirectories(path);
			    			exportModel(file, path);
			    		}
						
			    	}
			    	catch(IOException e){
						System.err.append("An error occurred while exporting the queries.");
			    	}
			    }	    
			}
		});
		
		gbc = new GridBagConstraints();
		gbc.gridx = 3;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		filesButtonsPanel.add(exportFilesButton);
		
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

		setContentPane(fileListPanel);
	}

	private void addFiles() {
		FileBrowser browser = new FileBrowser("Timed-Arc Petri Nets","xml");
		
		File[] filesArray = browser.openFiles();
		if (filesArray.length>0) {
			for (File file : filesArray) {
				if (!files.contains(file)) {
					files.add(file);
					listModel.addElement(file);
				}
			}

			enableButtons();
		}
	}

	private void removeSelectedFiles() {
		for (Object o : fileList.getSelectedValuesList()) {
			File file = (File) o;
			files.remove(file);
			listModel.removeElement(file);
		}

		enableButtons();
	}
	
	private void enableButtons() {
		fileList.setEnabled(true);
		addFilesButton.setEnabled(true);

		if (listModel.size() > 0) {
			clearFilesButton.setEnabled(true);
			removeFileButton.setEnabled(true);
		} else {
			clearFilesButton.setEnabled(false);
			removeFileButton.setEnabled(false);
		}

	}
	private void clearFiles() {
		files.clear();
		listModel.removeAllElements();
	}
	
	private void exportModel(File file, Path path) {
		try {
			LoadedModel loadedModel = loader.load(file);
			exportPNML(path, loadedModel);
			Export.toQueryXML(loadedModel.network(), path.toString() + "/query.xml", loadedModel.queries());			
		}
		catch(Exception e) {
			new MessengerImpl().displayErrorMessage("There was an error trying to export " + file.getName() + " to PNML and query XML");
			System.out.println(e);
		}
	}
	
	private void exportPNML(Path path, LoadedModel loadedModel) throws DOMException, TransformerConfigurationException, IOException, ParserConfigurationException, TransformerException {
		File f = new File(path.toString() + "/model.pnml");
		HashMap<TimedArcPetriNet, DataLayer> guiModel = new HashMap<TimedArcPetriNet, DataLayer>();
		for(pipe.dataLayer.Template template : loadedModel.templates()) {
			guiModel.put(template.model(), template.guiModel());
		}
		PNMLWriter pnmlWriter = new PNMLWriter(loadedModel.network(), guiModel);
		pnmlWriter.savePNML(f);
	}
}
