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
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


import pipe.gui.CreateGui;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.Pipe;
import pipe.gui.widgets.EscapableDialog;
import pipe.gui.widgets.JSplitPaneFix;
import dk.aau.cs.model.tapn.TimedArcPetriNet;

public class TemplateExplorer extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2334464984237161208L;

	private static long templateID = 0;
	
	private JSplitPane splitPane;
	private DrawingSurfaceImpl drawingSurface;
	
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

	
	
	
	public TemplateExplorer(DrawingSurfaceImpl drawingSurface)
	{
		this.drawingSurface = drawingSurface;
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
						
						openSelectedTemplate();
						
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
				
				// TODO: check if any local places are used in queries and if so warn user that these queries are removed too.
				removeTemplate();
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
		if(renaming){
			TimedArcPetriNet tapn = (TimedArcPetriNet)templateList.getSelectedValue();
			
			String newName = (String)JOptionPane.showInputDialog(drawingSurface, "Template name:", "Rename Template", JOptionPane.PLAIN_MESSAGE, null, null, tapn.getName());
			
			if(newName != null && newName.length() <= 0)
				JOptionPane.showMessageDialog(drawingSurface, "TAPN template could not be renamed:\n\nYou must provide a proper name for the template", "Error Renaming Template", JOptionPane.ERROR_MESSAGE);
			else if(newName != null && newName.length() > 0)
				renameTemplate(newName);
		}
		else
		{
			String templateName = (String)JOptionPane.showInputDialog(drawingSurface, "Template name:", "Rename Template", JOptionPane.PLAIN_MESSAGE, null, null, "New TAPN Template " + (++templateID));
		
			if(templateName != null && templateName.length() <= 0)
				JOptionPane.showMessageDialog(drawingSurface, "New TAPN template could not be created:\n\nYou must provide a proper name for the template", "Error Creating Template", JOptionPane.ERROR_MESSAGE);
			else if(templateName != null && templateName.length() > 0)
				createNewTemplate(templateName);
		}
	}
	
	public void createNewTemplate(String name)
	{
		TimedArcPetriNet tapn = new TimedArcPetriNet(name);
		
		drawingSurface.getTAPNTemplates().add(tapn);
		drawingSurface.openTAPNTemplate(tapn);
	}
	
	private void showTemplates() {
			
		List<TimedArcPetriNet> templates = drawingSurface.getTAPNTemplates().templates();
		
		listModel.removeAllElements();
		for(TimedArcPetriNet tapn : templates)
		{
			listModel.addElement(tapn);
		}
		templateList.validate();
	}

	public void renameTemplate(String newName) {
		TimedArcPetriNet tapn = (TimedArcPetriNet)templateList.getSelectedValue();
		
		tapn.setName(newName);
	}
	
	private void removeTemplate() {
		
		TimedArcPetriNet tapn = (TimedArcPetriNet)templateList.getSelectedValue();
		
		drawingSurface.getTAPNTemplates().remove(tapn);
	}
	
	private void openSelectedTemplate() {
		TimedArcPetriNet tapn = (TimedArcPetriNet)templateList.getSelectedValue();
		
		drawingSurface.openTAPNTemplate(tapn);
		
	}

	
}
