package dk.aau.cs.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import pipe.dataLayer.NetType;
import pipe.gui.widgets.JSplitPaneFix;

public class TemplateExplorer extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2334464984237161208L;

	private static int templateNumber = 0;

	private JSplitPane splitPane;
	
	// Template explorer panel items
	private JPanel templatePanel;
	private JLabel templateLabel;
	private JScrollPane scrollpane;
	private JTree templateExplorer;
	
	// Template button panel items
	private JPanel buttonPanel;
	private JButton newTemplateButton;
	private JButton removeTemplateButton;
	
	
	public TemplateExplorer()
	{
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
		
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Templates");
		root.add(new DefaultMutableTreeNode("New Template class"));
		
		templateExplorer = new JTree(root);
		templateExplorer.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		templateExplorer.setAlignmentX(Component.LEFT_ALIGNMENT);
		templateExplorer.setAlignmentY(Component.TOP_ALIGNMENT);
		
		templateLabel = new JLabel("Templates:");
		templateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		templateLabel.setAlignmentY(Component.TOP_ALIGNMENT);
		templatePanel.add(templateLabel, BorderLayout.PAGE_START);
		
		scrollpane = new JScrollPane(templateExplorer);
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
				// TODO Auto-generated method stub
				
			}
		});
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		buttonPanel.add(removeTemplateButton,gbc);
	}


	public void createNewTemplate(String name, NetType netType) {
		
		
	}


	
}
