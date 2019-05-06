package pipe.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.DOMException;
import dk.aau.cs.gui.FileNameCellRenderer;
import dk.aau.cs.gui.components.ExportBatchResultTableModel;
import dk.aau.cs.io.LoadedModel;
import dk.aau.cs.io.ModelLoader;
import dk.aau.cs.io.PNMLWriter;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.util.StringComparator;
import pipe.dataLayer.DataLayer;
import pipe.gui.widgets.filebrowser.FileBrowser;

public class ExportBatchDialog extends JDialog {

	private static final long serialVersionUID = 8346966786414688380L;

	private final static String TOOL_TIP_AddFilesButton = "Press to add nets to batch export";
	private final static String TOOL_TIP_RemoveFilesButton = "Press to remove the currently selected nets";
	private final static String TOOL_TIP_ClearFilesButton = "Press to remove all nets from list";
	private final static String TOOL_TIP_ExportFilesButton = "Press to export all nets in PNML and XML format";
	private final static String TOOL_TIP_UniqueQueryNamesCheckbox = "Give queries unique names when exporting";
	private final static String NAME_SuccesString = "Succeeded";
	private final static String NAME_SuccesStringOrphanTransitionsRemoved = "Succeeded, orphan transitions removed";
	private final static String NAME_FailStringFolderExists = "Failed as the subfolder already exists";
	private final static String NAME_FailStringParseError = "Failed due to net/query parsing error";

	
	private JPanel filesButtonsPanel;
	private JPanel mainPanel;
	private JButton addFilesButton;
	private JButton clearFilesButton;
	private JButton removeFileButton;
	private JButton exportFilesButton;
	private JPanel chooserPanel;
	private JTextField destinationPathField;
	private JList fileList;
	private DefaultListModel listModel;
	private List<File> files = new ArrayList<File>();
	private String lastExportPath;
	private String lastSelectPath;
	private JCheckBox uniqueQueryNames;
	private File destinationFile;
	private ExportBatchResultTableModel tableModel;
	
	private Thread progressBarThread;
	private JProgressBar progressBar;
	private JDialog progressBarContainer;
	static boolean noOrphanTransitions = false;

	static ExportBatchDialog exportBatchDialog;
	ModelLoader loader = new ModelLoader();
	
	public static boolean isDialogVisible() {
		if (exportBatchDialog != null) {
			return exportBatchDialog.isVisible();
		} else {
			return false;
		}
	}
	
	public static void setNoOrphanTransitions(boolean value) {
		noOrphanTransitions = value;
	}
	
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
		initComponents();
	}
	
	private void initComponents() {
		setLayout(new FlowLayout());
		mainPanel = new JPanel(new GridBagLayout());
		
		initFileList();
		initChooserPanel();
		initProgressPanel();
		
		setContentPane(mainPanel);
	}
	
	private void initProgressPanel() {
		JPanel resultPanel = new JPanel(new GridBagLayout());
		resultPanel.setBorder(BorderFactory.createTitledBorder("Exported Files"));
		
		tableModel = new ExportBatchResultTableModel();
		final JTable resultTable = new JTable(tableModel){
			private static final long serialVersionUID = 8524549736351991872L;

			public String getToolTipText(MouseEvent e) {
				String tip= null;
				java.awt.Point point = e.getPoint();
				int rowIndex = rowAtPoint(point);
				int colIndex = columnAtPoint(point);
				
				try {
					tip = getValueAt(rowIndex, colIndex).toString();
				}
				catch (RuntimeException e1) {
					tip = "";
				}
				return tip;
			}
		};
		//for coloring cells
		resultTable.getColumn("Status").setCellRenderer(new ExportResultTableCellRenderer(true));
		//for tooltips
		resultTable.getColumn("Destination").setCellRenderer(new ExportResultTableCellRenderer(true));
				
		// Enable sorting
		Comparator<Object> comparator = new StringComparator();
		
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(resultTable.getModel());
		for(int i = 0; i < resultTable.getColumnCount(); i++){
			sorter.setComparator(i, comparator);
		}
		resultTable.setRowSorter(sorter);
		
		JScrollPane scrollPane = new JScrollPane(resultTable);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		resultPanel.add(scrollPane, gbc);
		
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 0, 10, 5);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		
		mainPanel.add(resultPanel, gbc);
	}
	
	private void initChooserPanel() {
		chooserPanel = new JPanel(new GridBagLayout());
		chooserPanel.setBorder(BorderFactory.createTitledBorder("Export to destination"));
		
		destinationPathField = new JTextField("", 30);
		destinationPathField.setEditable(true);
		destinationPathField.getDocument().addDocumentListener(new DocumentListener() {			
			@Override
			public void changedUpdate(DocumentEvent e) {
				textFieldChanged();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				textFieldChanged();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				textFieldChanged();				
			}
		});
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.WEST;
		chooserPanel.add(destinationPathField, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 10, 0, 0);
		JButton destinationPathSelector = new JButton("Select destination folder");
		destinationPathSelector.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectDestinationPath();
				destinationPathField.setText(destinationFile.getParent());
				enableButtons();
			}
		});
		chooserPanel.add(destinationPathSelector, gbc);
		

		uniqueQueryNames = new JCheckBox("Use unique query names", true);
		uniqueQueryNames.setToolTipText(TOOL_TIP_UniqueQueryNamesCheckbox);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(10, 0, 0, 0);
		chooserPanel.add(uniqueQueryNames, gbc);
		
		exportFilesButton = new JButton("Export All Nets and Queries");
		exportFilesButton.setToolTipText(TOOL_TIP_ExportFilesButton);
		exportFilesButton.setEnabled(false);
		exportFilesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tableModel.clear();
				exportFiles();
				enableButtons();
			}
		});
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(10, 10, 0, 0);

		chooserPanel.add(exportFilesButton, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.insets = new Insets(10, 0, 10, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		
		mainPanel.add(chooserPanel, gbc);
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

		JScrollPane scrollpane = new JScrollPane(fileList);
		scrollpane.setMinimumSize(new Dimension(175, 375));
		scrollpane.setPreferredSize(new Dimension(250, 375));

		gbc = new GridBagConstraints();
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
		gbc.insets = new Insets(10, 5, 10, 5);
		
		mainPanel.add(fileListPanel, gbc);
	}
	
	private void initProgressBar() {
		progressBarContainer = new JDialog(exportBatchDialog, "Exporting...", true);
		progressBar = new JProgressBar(0, fileList.getModel().getSize());
		
		progressBarContainer.add(BorderLayout.CENTER, progressBar);
		progressBarContainer.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		progressBarContainer.setSize(400, 100);
		progressBarContainer.setLocationRelativeTo(exportBatchDialog);
		progressBarContainer.setVisible(false);
		progressBar.setStringPainted(true);
		progressBar.setString("Exported Nets: 0 of " + fileList.getModel().getSize());
		progressBar.setValue(0);
		
		progressBarThread = new Thread(new Runnable() {
			public void run() {
				progressBarContainer.setVisible(true);
			}
		});
	}

	private void addFiles() {
		FileBrowser browser = FileBrowser.constructor("Timed-Arc Petri Nets","tapn", "xml", lastSelectPath);
		
		File[] filesArray = browser.openFiles();
		if (filesArray.length>0) {
			for (File file : filesArray) {
				lastSelectPath = file.getParent();
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
		} else {
			clearFilesButton.setEnabled(false);
			removeFileButton.setEnabled(false);
		}
		if(listModel.size() > 0 && destinationFile != null && new File(destinationPathField.getText()).exists()) {
			exportFilesButton.setEnabled(true);
		}
		else
			exportFilesButton.setEnabled(false);

	}
	private void clearFiles() {
		files.clear();
		listModel.removeAllElements();
	}
	
	private void selectDestinationPath() {
		String chosenFile = FileBrowser.constructor("Select an export folder", ".", lastExportPath).saveFile("Export");
		if(chosenFile != null) {
			destinationFile = new File(chosenFile);
			lastExportPath = chosenFile;
		}
		else return;
	}
	
	private void exportFiles() {
		//loading bar
		initProgressBar();
		
		if(destinationFile != null && destinationFile.exists()) {
    		String destPath = destinationFile.isFile() ? destinationFile.getParent() : destinationFile.getAbsolutePath();
			lastExportPath = destPath;
			progressBarThread.start();
    		for(File file : files) {
	    		Path path = Paths.get(destPath + "/" + file.getName().replaceAll(".xml", "").replaceAll(".tapn", ""));
    			try {
			    	if(!(Files.exists(path))) {
		    			Files.createDirectories(path);
		    			exportModel(file, path);
		    			tableModel.addResult(noOrphanTransitions == false ? new String[]{file.getName(), path.toString(), NAME_SuccesString} 
		    			: new String[]{file.getName(), path.toString(), NAME_SuccesStringOrphanTransitionsRemoved});
			    	}
			    	else {
		    			tableModel.addResult(new String[]{file.getName(), path.toString(), NAME_FailStringFolderExists});
			    	}
    			}
    			catch(Exception e){
	    			tableModel.addResult(new String[]{file.getName(), path.toString() , NAME_FailStringParseError});
    	    	}
    			//For the loading bar
    			progressBar.setString("Exported Nets: " + files.indexOf(file) + " of " + files.size());
    			progressBar.setValue(files.indexOf(file));
    			progressBar.paintImmediately(new Rectangle(0, 0, progressBar.getWidth(), progressBar.getHeight()));
    			noOrphanTransitions = false;
    			//reset loading bar when done
    			if(progressBar.getValue() == files.size()-1) {
    				progressBarContainer.setVisible(false);
    				progressBar.setValue(0);
    			}
	    	}	
		}
		else if(destinationFile == null) {
			new MessengerImpl().displayErrorMessage("Please choose a folder for exporting");
		}
		else if(!(destinationFile.exists())) {
			new MessengerImpl().displayErrorMessage("The chosen path does not exist");
		}
    }
	public void textFieldChanged() {
		destinationFile = new File(destinationPathField.getText());
		enableButtons();
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
			copy.setName((fileName.replaceAll(".xml", "") + "." + query.getName() + "-" + index).replaceAll(" ", "_").replaceAll(".tapn", ""));
			renamedQueries.add(copy);
			index++;
		}
		return renamedQueries;
		
	}
	private class ExportResultTableCellRenderer extends JLabel implements
	TableCellRenderer {
	private static final long serialVersionUID = -1054925029991763163L;
	Border unselectedBorder = null;
	Border selectedBorder = null;
	boolean isBordered = true;
	
	public ExportResultTableCellRenderer(boolean isBordered) {
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
							.equals("Status");
					if (value != null) {
						if ((isResultColumn && (value.toString().equals(NAME_SuccesString)) || value.toString().equals(NAME_SuccesStringOrphanTransitionsRemoved))) {
							setBackground(new Color(91, 255, 91)); // light green
						}
						else if (isResultColumn && value.toString().equals(NAME_FailStringParseError)) {
							setBackground(new Color(255, 91, 91)); // light  red
						}
						else if (isResultColumn && value.toString().equals(NAME_FailStringFolderExists)) {
							setBackground(new Color(255, 255, 120)); // light yellow
						}
						else
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
				if (table.getColumnName(column).equals(
						"Destination")) {
					setText(".../"+new File(value.toString()).getName());
					Point mousePos = table.getMousePosition();
					String[] result = null;
					if (mousePos != null) {
						result = ((ExportBatchResultTableModel) table
								.getModel()).getResult(table
								.rowAtPoint(mousePos));
					}
					setToolTipText(result != null ? generateDestinationTooltip(result) : value.toString());
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
		
		private String generateDestinationTooltip(String[] result) {
			String fullFilePath = result[1];
			
			return fullFilePath;
		}
	}
}


