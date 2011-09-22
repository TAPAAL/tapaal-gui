package dk.aau.cs.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.Template;
import pipe.gui.CreateGui;
import pipe.gui.Zoomer;
import pipe.gui.graphicElements.DataLayer;
import pipe.gui.undo.AddTemplateCommand;
import pipe.gui.undo.RemoveTemplateCommand;
import pipe.gui.undo.RenameTemplateCommand;
import pipe.gui.undo.UndoManager;
import dk.aau.cs.TCTL.visitors.BooleanResult;
import dk.aau.cs.TCTL.visitors.ContainsAtomicPropositionsWithDisabledTemplateVisitor;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.tapn.SharedTransition;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.util.Require;
import dk.aau.cs.util.Tuple;

public class TemplateExplorer extends JPanel {
	private static final long serialVersionUID = -2334464984237161208L;

	// Template explorer panel items
	private JPanel templatePanel;
	private JScrollPane scrollpane;
	private JList templateList;
	private DefaultListModel listModel;

	// Template button panel items
	private JPanel buttonPanel;
	private JButton newTemplateButton;
	private JButton removeTemplateButton;
	private JButton renameButton;
	private JButton copyButton;

	private TabContent parent;
	private UndoManager undoManager;
	private boolean isInAnimationMode;

	private JButton moveUpButton;
	private JButton moveDownButton;

	public TemplateExplorer(TabContent parent) {
		this(parent, false);
	}

	public TemplateExplorer(TabContent parent, boolean hideButtons) {
		this.parent = parent;
		undoManager = parent.drawingSurface().getUndoManager();
		init(hideButtons);
	}

	private void init(boolean hideButtons) {
		setLayout(new BorderLayout());
		isInAnimationMode = false;
		initExplorerPanel();
		initButtonsPanel();

		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Components"), BorderFactory.createEmptyBorder(3, 3, 3, 3)));

		addCreatedComponents(hideButtons);
	}

	private void addCreatedComponents(boolean hideButtons) {
		this.removeAll();
		if (!hideButtons) {
			this.add(templatePanel, BorderLayout.CENTER);
			moveDownButton.setVisible(true);
			moveUpButton.setVisible(true);
			this.add(buttonPanel, BorderLayout.PAGE_END);
		} else {
			this.add(templatePanel, BorderLayout.CENTER);
			moveDownButton.setVisible(false);
			moveUpButton.setVisible(false);
		}
	}

	private void initExplorerPanel() {
		templatePanel = new JPanel(new GridBagLayout());
		listModel = new DefaultListModel();
		for (Template net : parent.allTemplates()) {
			listModel.addElement(net);
		}

		listModel.addListDataListener(new ListDataListener() {
			public void contentsChanged(ListDataEvent arg0) {
				if (listModel.size() == 1) {
					removeTemplateButton.setEnabled(false);
				} else {
					removeTemplateButton.setEnabled(true);
				}
			}

			public void intervalAdded(ListDataEvent arg0) {
				templateList.setSelectedIndex(arg0.getIndex0());
				templateList.ensureIndexIsVisible(arg0.getIndex0());
			}

			public void intervalRemoved(ListDataEvent arg0) {
				int index = (arg0.getIndex0() == 0) ? 0 : (arg0.getIndex0() - 1);
				templateList.setSelectedIndex(index);
				templateList.ensureIndexIsVisible(index);
			}
		});

		templateList = new JList(listModel);
		templateList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		templateList.setSelectedIndex(0);
		templateList.setCellRenderer(new TemplateListCellRenderer(templateList.getCellRenderer()));
		
		TemplateListManager manager = new TemplateListManager(templateList);
		templateList.addListSelectionListener(manager);
		templateList.addMouseListener(manager);

		scrollpane = new JScrollPane(templateList);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 2;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		templatePanel.add(scrollpane, gbc);
		
		moveUpButton = new JButton(new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("resources/Images/Up.png")));
		moveUpButton.setEnabled(false);
		moveUpButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = templateList.getSelectedIndex();
				
				if(index > 0) {
					parent.swapTemplates(index, index-1);
					updateTemplateList();
					templateList.setSelectedIndex(index-1);
				}
			}
		});
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.SOUTH;
		templatePanel.add(moveUpButton,gbc);
		
		moveDownButton = new JButton(new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("resources/Images/Down.png")));
		moveDownButton.setEnabled(false);
		moveDownButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = templateList.getSelectedIndex();
				
				if(index < parent.network().allTemplates().size() - 1) {
					parent.swapTemplates(index, index+1);
					updateTemplateList();
					templateList.setSelectedIndex(index+1);
				}
			}
		});
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.NORTH;
		templatePanel.add(moveDownButton,gbc);
	}

	private void initButtonsPanel() {
		buttonPanel = new JPanel(new GridBagLayout());

		Dimension dimension = new Dimension(82, 28);
		newTemplateButton = new JButton("New");
		newTemplateButton.setEnabled(true);
		newTemplateButton.setPreferredSize(dimension);

		newTemplateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Template template = ShowNewTemplateDialog();
				if (template != null) {
					int index = listModel.size();
					undoManager.addNewEdit(new AddTemplateCommand(TemplateExplorer.this, template, index));
					parent.addTemplate(template);
					parent.drawingSurface().setModel(template.guiModel(), template.model(), template.zoomer());
				}
			}
		});

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		buttonPanel.add(newTemplateButton, gbc);

		removeTemplateButton = new JButton("Remove");
		removeTemplateButton.setEnabled(false);
		removeTemplateButton.setPreferredSize(dimension);

		removeTemplateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = templateList.getSelectedIndex();
				Template template = selectedModel();

				HashSet<TAPNQuery> queriesToDelete = findQueriesAffectedByRemoval(template);
				
				int choice = JOptionPane.NO_OPTION;
				if(!queriesToDelete.isEmpty()){
					StringBuilder warning = buildWarningMessage(queriesToDelete);
		
					choice = JOptionPane.showConfirmDialog(
							CreateGui.getApp(), warning.toString(), "Warning",
							JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		
					if (choice == JOptionPane.YES_OPTION) {
						for (TAPNQuery q : queriesToDelete) {
							parent.removeQuery(q);
						}
					}
				}
				
				if(queriesToDelete.isEmpty() || choice == JOptionPane.YES_OPTION) {
										
					ArrayList<Tuple<TimedTransition, SharedTransition>> transitionsToUnshare = new ArrayList<Tuple<TimedTransition,SharedTransition>>();
					for(TimedTransition transition : template.model().transitions()){
						if(transition.isShared()){
							transitionsToUnshare.add(new Tuple<TimedTransition, SharedTransition>(transition, transition.sharedTransition()));
						}
					}
					
					Command command = new RemoveTemplateCommand(parent, TemplateExplorer.this, template, index, queriesToDelete, transitionsToUnshare);
					undoManager.addNewEdit(command);
					command.redo();
				}
				
			}

			private HashSet<TAPNQuery> findQueriesAffectedByRemoval(Template template) {
				Iterable<TAPNQuery> queries = parent.queries();
				HashSet<TAPNQuery> queriesToDelete = new HashSet<TAPNQuery>();

				for (TimedPlace p : template.model().places()) {
					for (TAPNQuery q : queries) {
						if (q.getProperty().containsAtomicPropositionWithSpecificPlaceInTemplate(template.model().name(), p.name())) {
							queriesToDelete.add(q);
						}
					}
				}
				return queriesToDelete;
			}
			
			private StringBuilder buildWarningMessage(HashSet<TAPNQuery> queriesToDelete) {
				StringBuilder s = new StringBuilder();
				s.append("The following queries are associated with the currently selected objects:\n\n");
				for (TAPNQuery q : queriesToDelete) {
					s.append(q.getName());
					s.append("\n");
				}
				s.append("\nAre you sure you want to remove the current selection and all associated queries?");
				return s;
			}
		});

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		buttonPanel.add(removeTemplateButton, gbc);

		renameButton = new JButton("Rename");
		renameButton.setEnabled(false);
		renameButton.setPreferredSize(dimension);

		renameButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				showRenameTemplateDialog();
				templateList.validate();
			}
		});

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		buttonPanel.add(renameButton, gbc);

		copyButton = new JButton("Copy");
		copyButton.setEnabled(false);
		copyButton.setPreferredSize(dimension);

		copyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Template template = selectedModel().copy();
				
				String name = template.model().name();
				if(parent.network().hasTAPNCalled(name)) {
					int i = 2;
				
					while(parent.network().hasTAPNCalled(name + i)) {
						i++;
					}
					template.model().setName(name + i);
				}
				
				parent.addTemplate(template);
			}
		});

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		buttonPanel.add(copyButton, gbc);
	}

	private Template ShowNewTemplateDialog() {
		String templateName = (String) JOptionPane.showInputDialog(
				parent.drawingSurface(), "Component name:", "Rename Component",
				JOptionPane.PLAIN_MESSAGE, null, null, 
				parent.drawingSurface().getNameGenerator().getNewTemplateName());

		if (templateName != null) {
			if(!isNameAllowed(templateName)) {
				JOptionPane.showMessageDialog(parent.drawingSurface(),
						"Acceptable names for components are defined by the regular expression:\n[a-zA-Z][_a-zA-Z0-9]*\n\nThe new component could not be created.",
						"Error Creating Component",
						JOptionPane.ERROR_MESSAGE);
			}
			else if (parent.network().hasTAPNCalled(templateName)) {
				JOptionPane.showMessageDialog(parent.drawingSurface(),
						"A component named \"" + templateName + "\" already exists.\n\nThe new component could not be created.",
						"Error Creating Component",
						JOptionPane.ERROR_MESSAGE);
			}
			else {
				Template template = createNewTemplate(templateName);
				return template;
			}
		}
		return null;
	}


	private boolean isNameAllowed(String templateName) {
		Require.that(templateName != null, "The template name cannot be null");
		
		return !templateName.isEmpty() && Pattern.matches("[a-zA-Z]([_a-zA-Z0-9])*", templateName);
	}

	private void showRenameTemplateDialog() {
		Template template = selectedModel();

		String newName = (String) JOptionPane.showInputDialog(parent.drawingSurface(), "Component name:", "Rename Component",
				JOptionPane.PLAIN_MESSAGE, null, null, template.model().name());
		
		if (newName == null || template.model().name().equals(newName))
			return;

		if (!isNameAllowed(newName))
			JOptionPane.showMessageDialog(
							parent.drawingSurface(),
							"Acceptable names for components are defined by the regular expression:\n[a-zA-Z][_a-zA-Z0-9]*.\n\nThe component could not be renamed.",
							"Error Renaming Component",
							JOptionPane.ERROR_MESSAGE);
		
		else if (parent.network().hasTAPNCalled(newName)) {
			JOptionPane.showMessageDialog(
							parent.drawingSurface(),
							"A component named \"" + newName + "\" already exists. Please try another name.",
							"Error", JOptionPane.ERROR_MESSAGE);
		} else {
			parent.drawingSurface().getNameGenerator().updateTemplateIndex(newName);
			Command command = new RenameTemplateCommand(this, parent, template.model(), template.model().name(), newName);
			undoManager.addNewEdit(command);
			command.redo();
		}

	}

	public Template createNewTemplate(String name) {
		TimedArcPetriNet tapn = new TimedArcPetriNet(name);

		return new Template(tapn, new DataLayer(), new Zoomer());
	}

	public void removeTemplate(int index, Template template) {
		listModel.remove(index);
		parent.removeTemplate(template);
	}

	public void addTemplate(int index, Template template) {
		listModel.add(index, template);
		parent.addTemplate(template);
	}

	public void openSelectedTemplate() {
		Template tapn = selectedModel();
		if (tapn != null) {
			parent.drawingSurface().setModel(tapn.guiModel(), tapn.model(), tapn.zoomer());
		}
	}

	public Template selectedModel() {
		return (Template) templateList.getSelectedValue();
	}

	public void updateTemplateList() {
		listModel.clear();
		if(isInAnimationMode) {
			for (Template net : parent.activeTemplates()) {
				listModel.addElement(net);
			}
		} else {
			for (Template net : parent.allTemplates()) {
				listModel.addElement(net);
			}
		}
	}

	public void hideButtons() {
		addCreatedComponents(true);
	}

	public void showButtons() {
		addCreatedComponents(false);
	}
	
	public void switchToAnimationMode() {
		hideButtons();
		isInAnimationMode = true;
		updateTemplateList();
	}
	

	public void switchToEditorMode() {
		showButtons();
		isInAnimationMode = false;
		updateTemplateList();
	}
	
	private class TemplateListCellRenderer extends JPanel implements ListCellRenderer {
		private static final long serialVersionUID = 1257272566670437973L;
		private static final String UNCHECK_TO_DEACTIVATE = "Uncheck to deactive component";
		private static final String CHECK_TO_ACTIVATE = "Check to Active component";
		private JCheckBox activeCheckbox = new JCheckBox();
		private ListCellRenderer cellRenderer;
		
		
		public TemplateListCellRenderer(ListCellRenderer renderer) {
			this.cellRenderer = renderer;
			setLayout(new BorderLayout()); 
	        setOpaque(false); 
	        activeCheckbox.setOpaque(false);
		}
		
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			Component renderer = cellRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			removeAll();
			if(!isInAnimationMode) { 
				boolean isActive = ((Template)value).isActive();
				activeCheckbox.setSelected(isActive);
				setToolTipText(isActive ? UNCHECK_TO_DEACTIVATE : CHECK_TO_ACTIVATE);
				add(activeCheckbox, BorderLayout.WEST);
			}
			add(renderer, BorderLayout.CENTER);
			return this;
		}
		
		
	}
	
	private class TemplateListManager extends MouseAdapter implements ListSelectionListener, ActionListener {
		private int checkBoxWidth = new JCheckBox().getPreferredSize().width;
		private ListSelectionModel selectionModel;
		private JList list;
		
		public TemplateListManager(JList list) {
			this.list = list;
			this.selectionModel = list.getSelectionModel();
			this.list.registerKeyboardAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), JComponent.WHEN_FOCUSED);
		}
		
		private void toggleSelection(int index) { 
			if(index<0) 
				return; 
			
			if(!selectionModel.isSelectedIndex(index)) 
				selectionModel.addSelectionInterval(index, index); 
			
			Template item = ((Template)list.getModel().getElementAt(index));
			item.setActive(!item.isActive());
			
			if(parent.numberOfActiveTemplates() == 0) { 
				item.setActive(true);
				JOptionPane.showMessageDialog(parent, "At least one component must be active.", "Cannot Deactive All Components", JOptionPane.INFORMATION_MESSAGE);
			}
			 
			toggleAffectedQueries();
			list.repaint();
		}
		
		private void toggleAffectedQueries() {
			for(TAPNQuery query : parent.queries()) {
				ContainsAtomicPropositionsWithDisabledTemplateVisitor visitor = new ContainsAtomicPropositionsWithDisabledTemplateVisitor(parent.network());
				BooleanResult result = new BooleanResult(true);
				query.getProperty().accept(visitor, result);
				
				if(query.isActive()) {
					if(!result.result())
						query.setActive(false);
				} else {
					if(result.result())
						query.setActive(true);
				}
			}
			
			parent.updateQueryList();
		}
		
		public void mouseClicked(MouseEvent e) {
			int index = templateList.locationToIndex(e.getPoint()); 
			
			if(index<0) 
				return; 
			
			if(e.getX()>templateList.getCellBounds(index, index).x+checkBoxWidth) 
				return; 
			
			toggleSelection(index);
		}

		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting() == false) {
				int index = templateList.getSelectedIndex();
				if (index == -1) {
					removeTemplateButton.setEnabled(false);
					renameButton.setEnabled(false);
					copyButton.setEnabled(false);
					moveUpButton.setEnabled(false);
					moveDownButton.setEnabled(false);
				} else {
					if (buttonPanel != null) {
						if (listModel.size() > 1)
							removeTemplateButton.setEnabled(true);
						renameButton.setEnabled(true);
						copyButton.setEnabled(true);
						
						if(index > 0)
							moveUpButton.setEnabled(true);
						else
							moveUpButton.setEnabled(false);
								
							
						if(index < parent.network().allTemplates().size() - 1)
							moveDownButton.setEnabled(true);
						else
							moveDownButton.setEnabled(false);
					}
					templateList.ensureIndexIsVisible(index);
					openSelectedTemplate();
				}
			}
		}

		public void actionPerformed(ActionEvent e) {
			toggleSelection(list.getSelectedIndex());
		}
		
	}
}
