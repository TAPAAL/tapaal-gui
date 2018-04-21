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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
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

	private final static String TOOL_TIP_AddFilesButton = "Press to add nets to batch export";
	private final static String TOOL_TIP_RemoveFilesButton = "Press to remove the currently selected nets";
	private final static String TOOL_TIP_ClearFilesButton = "Press to remove all nets from list";
	private final static String TOOL_TIP_ExportFilesButton = "Press to export all nets in PNML and XML format";
	private final static String TOOL_TIP_UniqueQueryNamesCheckbox = "Give queries unique names when exporting";
	
	private JPanel filesButtonsPanel;
	private JButton addFilesButton;
	private JButton clearFilesButton;
	private JButton removeFileButton;
	private JButton exportFilesButton;
	private JList fileList;
	private DefaultListModel listModel;
	private List<File> files = new ArrayList<File>();
	private String lastPath;
	private JCheckBox uniqueQueryNames;


	static ExportBatchDialog exportBatchDialog;
	ModelLoader loader = new ModelLoader(new DrawingSurfaceImpl(new DataLayer()));
	
	public static void ShowExportBatchDialog(){
		if(exportBatchDialog == null){
			exportBatchDialog = new ExportBatchDialog(CreateGui.getApp(), "Batch Export", true);
			exportBatchDialog.pack();
			exportBatchDialog.setPreferredSize(exportBatchDialog.getSize());
			exportBatchDialog.setMinimumSize(new Dimension(exportBatchDialog.getWidth(), exportBatchDialog.getHeight()));
			exportBatchDialog.setLocationRelativeTo(null);
			exportBatchDialog.setResizable(true);
		}
		exportBatchDialog.setVisible(true);
	}
	
	private ExportBatchDialog(Frame frame, String title, boolean modal) {
		super(frame, title, modal);	
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

		fileList.setCellRenderer(new FileNameCellRenderer());
		GridBagConstraints gbc = new GridBagConstraints();

		uniqueQueryNames = new JCheckBox("Unique Query Names", true);
		uniqueQueryNames.setToolTipText(TOOL_TIP_UniqueQueryNamesCheckbox);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		fileListPanel.add(uniqueQueryNames, gbc);

		JScrollPane scrollpane = new JScrollPane(fileList);
		scrollpane.setMinimumSize(new Dimension(175, 375));
		scrollpane.setPreferredSize(new Dimension(175, 375));

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
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
		exportFilesButton.setEnabled(false);
		exportFilesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportFiles();
				enableButtons();
			}
		});
		gbc = new GridBagConstraints();
		gbc.gridx = 3;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		filesButtonsPanel.add(exportFilesButton, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
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
		for (Object o : fileList.getSelectedValuesList()) {
			File file = (File) o;
			files.remove(file);
			listModel.removeElement(file);
		}

		enableButtons();
	}
	
	private void enableButtons() {
		fileList.setEnabled(true);
		uniqueQueryNames.setEnabled(true);
		addFilesButton.setEnabled(true);

		if (listModel.size() > 0) {
			clearFilesButton.setEnabled(true);
			removeFileButton.setEnabled(true);
			exportFilesButton.setEnabled(true);

		} else {
			clearFilesButton.setEnabled(false);
			removeFileButton.setEnabled(false);
			exportFilesButton.setEnabled(false);
		}

	}
	private void clearFiles() {
		files.clear();
		listModel.removeAllElements();
	}
	
	private void exportFiles() {
		File destinationFile = new File(new FileBrowser("Export Nets", ".", lastPath).saveFile("Export"));
		if(destinationFile != null) {
	    	lastPath = destinationFile.getParent();
	    	try {
	    		for(File file : files) {
			    	Path path = Paths.get(destinationFile.getParent() + "/" + file.getName().replaceAll(".xml", ""));
	    			Files.createDirectories(path);
	    			exportModel(file, path);
	    		}
		    	new MessengerImpl().displayInfoMessage("The selected nets were exported as PNML and XML query files in " + destinationFile.getParent() + ".");
	    	}
	    	catch(Exception e){
				System.err.append("An error occurred while exporting the nets.");
	    	}
		}
    }	    
	
	private void exportModel(File file, Path path) throws Exception {
			LoadedModel loadedModel = loader.load(file);
			exportPNML(path, loadedModel);
			if(!uniqueQueryNames.isSelected())
				Export.toQueryXML(loadedModel.network(), path.toString() + "/query.xml", loadedModel.queries());
			else {
				Export.toQueryXML(loadedModel.network(), path.toString() + "/query.xml", renameQueries(file.getName(), loadedModel.queries()));
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
	
	private Collection<pipe.dataLayer.TAPNQuery> renameQueries(String fileName, Collection<pipe.dataLayer.TAPNQuery> queries){
		Collection<pipe.dataLayer.TAPNQuery> renamedQueries = new ArrayList<pipe.dataLayer.TAPNQuery>(); 
		int index = 1;
		
		for(pipe.dataLayer.TAPNQuery query : queries) {
			pipe.dataLayer.TAPNQuery copy = query;
			copy.setName((fileName.replaceAll(".xml", "") + "." + query.getName() + "-" + index).replaceAll(" ", "_"));
			renamedQueries.add(copy);
		}
		return renamedQueries;
		
	}
}
