package dk.aau.cs.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import dk.aau.cs.model.tapn.TimedArcPetriNet;

import pipe.dataLayer.DataLayer;
import pipe.gui.CreateGui;
import pipe.gui.Pipe;
import pipe.gui.widgets.EscapableDialog;
import pipe.gui.widgets.JSplitPaneFix;

public class TemplateExplorer extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2334464984237161208L;
	
	private HashMap<TimedArcPetriNet, String> tapnNameMap;

	private JSplitPane splitPane;
	
	// Template explorer panel items
	private JPanel templatePanel;
	private JLabel templateLabel;
	private JScrollPane scrollpane;
	private JList templateList;
	private DefaultListModel listModel;
	
	// Template button panel items
	private JPanel buttonPanel;
	private JButton newTemplateButton;
	private JButton removeTemplateButton;
	private JButton renameButton;
	private JButton copyButton;
	
	
	public TemplateExplorer()
	{
		tapnNameMap = new HashMap<TimedArcPetriNet, String>();
		
		this.setLayout(new BorderLayout());
		init();
	}
	
	private void init()
	{
		initExplorerPanel();
		initButtonsPanel();
		
		splitPane = new JSplitPaneFix(JSplitPane.VERTICAL_SPLIT, templatePanel, buttonPanel);
		splitPane.setContinuousLayout(true);
		splitPane.setDividerSize(0);
		splitPane.setDividerLocation(0.8);
		splitPane.setResizeWeight(1.0);
		this.add(splitPane);
	}
	
	private void initExplorerPanel() {
		templatePanel = new JPanel(new BorderLayout());
		
		listModel = new DefaultListModel();
		templateList = new JList(listModel);
		templateList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		templateList.setVisibleRowCount(-1);
		templateList.setLayoutOrientation(JList.VERTICAL);
		templateList.setAlignmentX(Component.LEFT_ALIGNMENT);
		templateList.setAlignmentY(Component.TOP_ALIGNMENT);
		templateList.addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent e){
				if (e.getValueIsAdjusting() == false) {
					if (templateList.getSelectedIndex() == -1) {
						removeTemplateButton.setEnabled(false);
						renameButton.setEnabled(false);
						copyButton.setEnabled(false);
					} else {
						removeTemplateButton.setEnabled(true);
						renameButton.setEnabled(true);
						copyButton.setEnabled(true);
					}
				}
			}
		});
		templateList.addMouseListener(new MouseAdapter(){

			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(!templateList.isSelectionEmpty()){
					if(arg0.getButton() == MouseEvent.BUTTON1 && arg0.getClickCount() == 2){
						int index = templateList.locationToIndex(arg0.getPoint());
						templateList.ensureIndexIsVisible(index);						
						
						// save current model
						// close current model
						// open selected model on DrawinSurface 
					}	
				}				
			}
		});
		
		
		templateLabel = new JLabel("Templates:");
		templateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		templateLabel.setAlignmentY(Component.TOP_ALIGNMENT);
		templatePanel.add(templateLabel, BorderLayout.PAGE_START);
		
		scrollpane = new JScrollPane(templateList);
		templatePanel.add(scrollpane, BorderLayout.CENTER);
		
	}

	private void initButtonsPanel() {
		buttonPanel = new JPanel(new GridBagLayout());
		
		Dimension dimension = new Dimension(82,23);
		newTemplateButton = new JButton("New");
		newTemplateButton.setEnabled(true);
		newTemplateButton.setPreferredSize(dimension);
		
		newTemplateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ShowNewTemplateDialog(false);
				showTemplates();
			}

			
		});
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		buttonPanel.add(newTemplateButton,gbc);
		
		removeTemplateButton = new JButton("Remove");
		removeTemplateButton.setEnabled(false);
		removeTemplateButton.setPreferredSize(dimension);
		
		removeTemplateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TimedArcPetriNet tapn = (TimedArcPetriNet)templateList.getSelectedValue();
				
				// remove tapn from model
				
				tapnNameMap.remove(tapn);
				showTemplates();
				
			}
		});
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		buttonPanel.add(removeTemplateButton,gbc);
		
		renameButton = new JButton("Rename");
		renameButton.setEnabled(false);
		renameButton.setPreferredSize(dimension);
		
		renameButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				ShowNewTemplateDialog(true);
				showTemplates();
			}
		});
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		buttonPanel.add(renameButton,gbc);
		
		copyButton = new JButton("Copy");
		copyButton.setEnabled(false);
		copyButton.setPreferredSize(dimension);
		
		copyButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				TimedArcPetriNet tapn = (TimedArcPetriNet)templateList.getSelectedValue();
				
				// copy tapn
				// add copy
				// update UI
			}
		});
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		buttonPanel.add(copyButton,gbc);
	}


	private void ShowNewTemplateDialog(boolean renaming) {
		EscapableDialog guiDialog = 
			new EscapableDialog(CreateGui.getApp(), Pipe.getProgramName(), true);
	
		Container contentPane = guiDialog.getContentPane();
	
		// 1 Set layout
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));      
	
		// 2 Add query editor
		NewTemplatePanel tp;
		if(!renaming)
			tp = new NewTemplatePanel(guiDialog.getRootPane(), this);
		else {
			TimedArcPetriNet tapn = (TimedArcPetriNet)templateList.getSelectedValue();
			String name = tapnNameMap.get(tapn);
			tp = new NewTemplatePanel(guiDialog.getRootPane(), this, name);
		}
		contentPane.add( tp );
	
		guiDialog.setResizable(false); 
	
	
		// Make window fit contents' preferred size
		guiDialog.pack();
	
		// Move window to the middle of the screen
		guiDialog.setLocationRelativeTo(null);
		guiDialog.setVisible(true);
	}
	
	public void createNewTemplate(String name)
	{
		DataLayer model = CreateGui.getModel();
		TimedArcPetriNet tapn = new TimedArcPetriNet();
	//	model.addTimedArcPetriNet(tapn);
		tapnNameMap.put(tapn, name);
	}
	
	private void showTemplates() {
		DataLayer model = CreateGui.getModel();
		
		// add tapns from model to UI
		
	}

	public void renameTemplate(String oldName, String newName) {
		TimedArcPetriNet tapn = (TimedArcPetriNet)templateList.getSelectedValue();
		
		tapnNameMap.remove(tapn);
		tapnNameMap.put(tapn, newName);
		
	}

	
}
