package dk.aau.cs.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.Template;
import pipe.gui.CreateGui;
import pipe.gui.Zoomer;
import pipe.gui.undo.AddTemplateCommand;
import pipe.gui.undo.RemoveTemplateCommand;
import pipe.gui.undo.RenameTemplateCommand;
import pipe.gui.undo.ToggleTemplateActivationCommand;
import pipe.gui.undo.UndoManager;
import pipe.gui.widgets.EscapableDialog;
import pipe.gui.widgets.RequestFocusListener;
import dk.aau.cs.TCTL.visitors.BooleanResult;
import dk.aau.cs.TCTL.visitors.ContainsPlaceWithDisabledTemplateVisitor;
import dk.aau.cs.gui.components.NonsearchableJList;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.gui.undo.SortTemplatesCommand;
import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.SharedTransition;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.util.Require;
import dk.aau.cs.util.StringComparator;
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
	private JButton sortButton;
	
	private static final String toolTipNewComponent ="Create a new component";
	private static final String toolTipRemoveComponent ="Remove the selected component";
	private static final String toolTipCopyComponent ="Copy the selected component";
	private static final String toolTipRenameComponent="Rename the selected component";
	private final static String toolTipSortComponents = "Sort the components alphabetically";
	private final static String toolTipMoveUp = "Move the selected component up";
	private final static String toolTipMoveDown = "Move the selected component down";
    //private static final String toolTipComponents ="Here you can manage the different components of the Net.<html><br/></html>" +
    	//	"A Net can be broken up in several components and connected via shared places and transitions.";
	
	public TemplateExplorer(TabContent parent) {
		this(parent, false);
	}

	public TemplateExplorer(TabContent parent, boolean hideButtons) {
		this.parent = parent;
		undoManager = parent.drawingSurface().getUndoManager();
		init(hideButtons);
	}
	
	public void selectPrevious(){
		int index = templateList.getSelectedIndex()-1;
		if(index == -1)	index = listModel.getSize()-1;
		templateList.setSelectedIndex(index);
	}
	
	public void selectNext(){
		int index = templateList.getSelectedIndex()+1;
		if(index == listModel.size())	index = 0;
		templateList.setSelectedIndex(index);
	}
	
	public Integer indexOfSelectedTemplate() {
		return templateList.getSelectedIndex();
	}
	
	public void restoreSelectedTemplate(Integer value) {
		templateList.setSelectedIndex(value);
	}

	private void init(boolean hideButtons) {
		setLayout(new BorderLayout());
		isInAnimationMode = false;
		initExplorerPanel();
		initButtonsPanel();

		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Components"), BorderFactory.createEmptyBorder(3, 3, 3, 3)));
	    this.setToolTipText("List of components. Click a component to display it.");
		addCreatedComponents(hideButtons);
		
		this.addComponentListener(new ComponentListener() {
			int minimumHegiht = TemplateExplorer.this.getMinimumSize().height + sortButton.getMinimumSize().height;
			public void componentShown(ComponentEvent e) {
			}
			
			
			public void componentResized(ComponentEvent e) {
				
				if(!isInAnimationMode){
					if(TemplateExplorer.this.getSize().height <= minimumHegiht){
						sortButton.setVisible(false);
					} else {
						sortButton.setVisible(true);
					}
				}
			}
			
			
			public void componentMoved(ComponentEvent e) {
			}
			
			
			public void componentHidden(ComponentEvent e) {
			}
		});
	}

	private void addCreatedComponents(boolean hideButtons) {
		this.removeAll();
		if (!hideButtons) {
			this.add(templatePanel, BorderLayout.CENTER);
			moveDownButton.setVisible(true);
			moveUpButton.setVisible(true);
			sortButton.setVisible(true);
			this.add(buttonPanel, BorderLayout.PAGE_END);
			
		} else {
			this.add(templatePanel, BorderLayout.CENTER);
			moveDownButton.setVisible(false);
			moveUpButton.setVisible(false);
			sortButton.setVisible(false);
			
			//Makes the jpanel auto calculate it's minimum size
			this.setMinimumSize(null);
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
				if (CreateGui.getCurrentTab().numberOfActiveTemplates() > 1) {
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

		templateList = new NonsearchableJList(listModel);

		templateList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		templateList.setSelectedIndex(0);
		templateList.setCellRenderer(new TemplateListCellRenderer(templateList.getCellRenderer()));
		
		TemplateListManager manager = new TemplateListManager(templateList);
		templateList.addListSelectionListener(manager);
		templateList.addMouseListener(manager);
		
		//templateList.setFocusTraversalKeysEnabled(false);

		scrollpane = new JScrollPane(templateList);
		//Add 10 pixel to the minimumsize of the scrollpane
		scrollpane.setMinimumSize(new Dimension(scrollpane.getMinimumSize().width, scrollpane.getMinimumSize().height + 20));
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 3;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		templatePanel.add(scrollpane, gbc);
		
		moveUpButton = new JButton(new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("resources/Images/Up.png")));
		moveUpButton.setEnabled(false);
		moveUpButton.setToolTipText(toolTipMoveUp);
		moveUpButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = templateList.getSelectedIndex();
				
				if(index > 0) {
					parent.swapTemplates(index, index-1);
					Object o = listModel.getElementAt(index);
                    listModel.setElementAt(listModel.getElementAt(index-1), index);
                    listModel.setElementAt(o, index-1);
                    templateList.ensureIndexIsVisible(index+1);
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
		moveDownButton.setToolTipText(toolTipMoveDown);
		moveDownButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = templateList.getSelectedIndex();
				
				if(index < parent.network().allTemplates().size() - 1) {
					parent.swapTemplates(index, index+1);
					Object o = listModel.getElementAt(index);
                    listModel.setElementAt(listModel.getElementAt(index+1), index);
                    listModel.setElementAt(o, index+1);
                    templateList.ensureIndexIsVisible(index+1);
					templateList.setSelectedIndex(index+1);
				}
			}
		});
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.NORTH;
		templatePanel.add(moveDownButton,gbc);
		
		//Sort button
		sortButton = new JButton(new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("resources/Images/Sort.png")));
		sortButton.setToolTipText(toolTipSortComponents);
		sortButton.setEnabled(false);
		sortButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Command command = new SortTemplatesCommand(parent, TemplateExplorer.this, templateList, listModel);
				undoManager.addNewEdit(command);
				command.redo();
			}
		});
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTH;
		templatePanel.add(sortButton,gbc);
	}

	private void initButtonsPanel() {
		buttonPanel = new JPanel(new GridBagLayout());

		Dimension dimension = new Dimension(82, 28);
		newTemplateButton = new JButton("New");
		newTemplateButton.setEnabled(true);
		newTemplateButton.setPreferredSize(dimension);
		newTemplateButton.setToolTipText(toolTipNewComponent);

		newTemplateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ShowNewTemplateDialog("");
			}
		});

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		buttonPanel.add(newTemplateButton, gbc);

		removeTemplateButton = new JButton("Remove");
		removeTemplateButton.setEnabled(false);
		removeTemplateButton.setPreferredSize(dimension);
		removeTemplateButton.setToolTipText(toolTipRemoveComponent);

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
				
				for (TimedTransition t : template.model().transitions()) {
					for (TAPNQuery q : queries) {
						if (q.getProperty().containsAtomicPropositionWithSpecificTransitionInTemplate(template.model().name(), t.name())) {
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
					s.append('\n');
				}
				s.append("\nAre you sure you want to remove the current selection and all associated queries?");
				return s;
			}
		});

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		buttonPanel.add(removeTemplateButton, gbc);

		renameButton = new JButton("Rename");
		renameButton.setEnabled(false);
		renameButton.setPreferredSize(dimension);
		renameButton.setToolTipText(toolTipRenameComponent);

		renameButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				showRenameTemplateDialog("");
				templateList.validate();
			}
		});

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		buttonPanel.add(renameButton, gbc);

		copyButton = new JButton("Copy");
		copyButton.setEnabled(false);
		copyButton.setPreferredSize(dimension);
		copyButton.setToolTipText(toolTipCopyComponent);

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
				
				int index = listModel.size();
				undoManager.addNewEdit(new AddTemplateCommand(TemplateExplorer.this, template, index));
				
				parent.addTemplate(template);
				ArrayList<Constant> tmp = new ArrayList<Constant>();
				for(Constant c : parent.network().constants()){
					tmp.add(new Constant(c.name(), c.value()));
				}
				for(Constant c : tmp){
					parent.network().updateConstant(c.name(), c);
				}
			}
		});

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		buttonPanel.add(copyButton, gbc);
	}
	
	private EscapableDialog dialog;
	private JPanel container;
	private JTextField nameTextField;
	private Dimension size;
	private JLabel nameLabel;
	private JPanel buttonContainer;
	private JButton okButton;
	private JButton cancelButton;
	private JPanel nameContainer;
	
	private void onOKRenameTemplate() {		
		Template template = selectedModel();			
		String newName = nameTextField.getText().trim();
		if (newName == null || template.model().name().equals(newName)) {
			exit();
			return;
		}
		if (!isNameAllowed(newName)) {
			JOptionPane.showMessageDialog(
							parent.drawingSurface(),
							"Acceptable names for components are defined by the regular expression:\n[a-zA-Z][_a-zA-Z0-9]*.\n\nThe component could not be renamed.",
							"Error Renaming Component",
							JOptionPane.ERROR_MESSAGE);
			exit();
			showRenameTemplateDialog(newName);
			return;
		}
		else if (parent.network().hasTAPNCalled(newName) && !template.model().name().equalsIgnoreCase(newName)) {
			JOptionPane.showMessageDialog(
							parent.drawingSurface(),
							"A component named \"" + newName + "\" already exists. Try another name.",
							"Error", JOptionPane.ERROR_MESSAGE);
			exit();
			showRenameTemplateDialog(newName);
			return;
		} else {
			parent.drawingSurface().getNameGenerator().updateTemplateIndex(newName);
			Command command = new RenameTemplateCommand(this, parent, template.model(), template.model().name(), newName);
			undoManager.addNewEdit(command);
			command.redo();
		}
		exit();
	}
	
	private void onOK() {
		Template template = null;		
		String templateName = nameTextField.getText().trim();		
		if (templateName != null) {
			if(!isNameAllowed(templateName)) {
				JOptionPane.showMessageDialog(parent.drawingSurface(),
						"Acceptable names for components are defined by the regular expression:\n[a-zA-Z][_a-zA-Z0-9]*\n\nThe new component could not be created.",
						"Error Creating Component",
						JOptionPane.ERROR_MESSAGE);
				exit();
				ShowNewTemplateDialog(templateName);
				return;
			}
			else if (parent.network().hasTAPNCalled(templateName)) {
				JOptionPane.showMessageDialog(parent.drawingSurface(),
						"A component named \"" + templateName + "\" already exists.\n\nThe new component could not be created.",
						"Error Creating Component",
						JOptionPane.ERROR_MESSAGE);
				exit();
				ShowNewTemplateDialog(templateName);
				return;
			}
			else {
				template = createNewTemplate(templateName);
			}
		}
		if (template != null) {
			int index = listModel.size();
			undoManager.addNewEdit(new AddTemplateCommand(TemplateExplorer.this, template, index));
			parent.addTemplate(template);
			parent.drawingSurface().setModel(template.guiModel(), template.model(), template.zoomer());
		}
		exit();
	}
	
	private void exit() {
		dialog.setVisible(false);
	}
	
	private void initComponentsOfNewTemplateDialog(String nameToShow) {
		container = new JPanel();
		container.setLayout(new GridBagLayout());
		nameContainer = new JPanel();
		nameContainer.setLayout(new GridBagLayout());
		size = new Dimension(330, 25);

		nameTextField = new javax.swing.JTextField();	
		nameTextField.setPreferredSize(size);
		nameTextField.setText(nameToShow);	
		nameTextField.addAncestorListener(new RequestFocusListener());
		nameTextField.addActionListener(new ActionListener() {			
			
			public void actionPerformed(ActionEvent e) {
				okButton.requestFocusInWindow();
				okButton.doClick();
			}
		});
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.WEST;
		//gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(4, 4, 2, 4);
		nameContainer.add(nameTextField,gbc);
		
		nameLabel = new JLabel(); 
		nameLabel.setText("Name of component: ");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(4, 4, 2, 4);
		gbc.anchor = GridBagConstraints.WEST;
		nameContainer.add(nameLabel,gbc);
		
		buttonContainer = new JPanel();
		buttonContainer.setLayout(new GridBagLayout());

		okButton = new JButton();
		okButton.setText("OK");
		okButton.setMaximumSize(new java.awt.Dimension(100, 25));
		okButton.setMinimumSize(new java.awt.Dimension(100, 25));
		okButton.setPreferredSize(new java.awt.Dimension(100, 25));
		okButton.setMnemonic(KeyEvent.VK_O);
		gbc = new GridBagConstraints();		
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = java.awt.GridBagConstraints.WEST;
		gbc.insets = new java.awt.Insets(5, 5, 5, 5);
		buttonContainer.add(okButton,gbc);
		
		cancelButton = new JButton();
		cancelButton.setText("Cancel");
		cancelButton.setMaximumSize(new java.awt.Dimension(100, 25));
		cancelButton.setMinimumSize(new java.awt.Dimension(100, 25));
		cancelButton.setPreferredSize(new java.awt.Dimension(100, 25));
		cancelButton.setMnemonic(KeyEvent.VK_C);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = java.awt.GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.EAST;
		buttonContainer.add(cancelButton,gbc);		
		
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onOK();
			}
		});
		
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exit();
			}
		});
		
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 8, 5, 8);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.EAST;
		container.add(buttonContainer,gbc);
		
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 8, 5, 8);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.WEST;
		container.add(nameContainer,gbc);
	}

	private void ShowNewTemplateDialog(String nameToShow) {
		dialog = new EscapableDialog(CreateGui.getApp(),
				"Enter Component Name", true);
		initComponentsOfNewTemplateDialog(nameToShow);
		dialog.add(container);
		dialog.setResizable(false);
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
	}


	private boolean isNameAllowed(String templateName) {
		Require.that(templateName != null, "The template name cannot be null");
		
		return !templateName.isEmpty() && Pattern.matches("[a-zA-Z]([_a-zA-Z0-9])*", templateName);
	}
	
	private void initComponentsOfRenameTemplateDialog(String oldname) {
		container = new JPanel();
		container.setLayout(new GridBagLayout());
		nameContainer = new JPanel();
		nameContainer.setLayout(new GridBagLayout());
		size = new Dimension(330, 25);

		nameTextField = new javax.swing.JTextField();	
		nameTextField.setPreferredSize(size);
		nameTextField.setText(oldname);
		nameTextField.addAncestorListener(new RequestFocusListener());
		nameTextField.addActionListener(new ActionListener() {			
			
			public void actionPerformed(ActionEvent e) {
				okButton.requestFocusInWindow();
				okButton.doClick();
			}
		});
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.WEST;
		//gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(4, 4, 2, 4);
		nameContainer.add(nameTextField,gbc);
		
		nameLabel = new JLabel(); 
		nameLabel.setText("Name of component: ");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(4, 4, 2, 4);
		gbc.anchor = GridBagConstraints.WEST;
		nameContainer.add(nameLabel,gbc);
		
		buttonContainer = new JPanel();
		buttonContainer.setLayout(new GridBagLayout());

		okButton = new JButton();
		okButton.setText("OK");
		okButton.setMaximumSize(new java.awt.Dimension(100, 25));
		okButton.setMinimumSize(new java.awt.Dimension(100, 25));
		okButton.setPreferredSize(new java.awt.Dimension(100, 25));
		okButton.setMnemonic(KeyEvent.VK_O);
		gbc = new GridBagConstraints();		
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = java.awt.GridBagConstraints.WEST;
		gbc.insets = new java.awt.Insets(5, 5, 5, 5);
		buttonContainer.add(okButton,gbc);
		
		cancelButton = new JButton();
		cancelButton.setText("Cancel");
		cancelButton.setMaximumSize(new java.awt.Dimension(100, 25));
		cancelButton.setMinimumSize(new java.awt.Dimension(100, 25));
		cancelButton.setPreferredSize(new java.awt.Dimension(100, 25));
		cancelButton.setMnemonic(KeyEvent.VK_C);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = java.awt.GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.EAST;
		buttonContainer.add(cancelButton,gbc);		
		
		okButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				onOKRenameTemplate();
			}
		});
		
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exit();
			}
		});
		
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 8, 5, 8);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.EAST;
		container.add(buttonContainer,gbc);
		
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 8, 5, 8);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.WEST;
		container.add(nameContainer,gbc);

	}

	private void showRenameTemplateDialog(String nameToShow) {		
		dialog = new EscapableDialog(CreateGui.getApp(),
				"Enter Component Name", true);
		Template template = selectedModel();
		if (nameToShow.equals("")){
			initComponentsOfRenameTemplateDialog(template.model().name());
		}
		else {
			initComponentsOfRenameTemplateDialog(nameToShow);
		}
		dialog.add(container);
		dialog.setResizable(false);
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
	}

	public Template createNewTemplate(String name) {
		TimedArcPetriNet tapn = new TimedArcPetriNet(name);

		return new Template(tapn, new DataLayer(), new Zoomer());
	}

	public void removeTemplate(int index, Template template) {
		listModel.removeElement(template);
		parent.removeTemplate(template);
		templateList.setSelectedIndex(index);
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
		parent.drawingSurface().repaintAll();
	}

	public Template selectedModel() {
		return (Template) templateList.getSelectedValue();
	}

	public void updateTemplateList() {
		int selectedIndex = templateList.getSelectedIndex();
		DefaultListModel newList = new DefaultListModel();
		
		if(isInAnimationMode) {
			for (Template net : parent.activeTemplates()) {
				newList.addElement(net);
			}
		} else {
			for (Template net : parent.allTemplates()) {
				newList.addElement(net);
			}
		}
		// When removing a component, the listModel has already been updated but the index is invalid (-1), thus we select the last component as the active one
		// When adding a component, this function updates the listModel thus it has a new length and the index should be corrected accordingly
		templateList.setSelectedIndex(selectedIndex);
		if(newList.size() != listModel.size() || selectedIndex == -1){
			selectedIndex = newList.size()-1;
		}
		listModel = newList;
		templateList.setModel(listModel);
		templateList.setSelectedIndex(selectedIndex);
	}
	
	public void selectFirst() {
			templateList.setSelectedIndex(0);
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
		private static final String UNCHECK_TO_DEACTIVATE = "Uncheck to deactive the component";
		private static final String CHECK_TO_ACTIVATE = "Check to active the component";
		private JCheckBox activeCheckbox = new JCheckBox();
		private ListCellRenderer cellRenderer;
		
		
		public TemplateListCellRenderer(ListCellRenderer renderer) {
			cellRenderer = renderer;
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
				setToolTipText((isActive)? UNCHECK_TO_DEACTIVATE : CHECK_TO_ACTIVATE);
				add(activeCheckbox, BorderLayout.WEST);
			} else {
				
				setToolTipText(null);
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
			selectionModel = list.getSelectionModel();
			this.list.registerKeyboardAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), JComponent.WHEN_FOCUSED);
		}
		
		private void toggleSelection(int index) { 
			if(index<0) 
				return; 

			Template item = ((Template)list.getModel().getElementAt(index));


			if(!selectionModel.isSelectedIndex(index)) {
				selectionModel.addSelectionInterval(index, index);
			}

			boolean newValue =!item.isActive();
			item.setActive(newValue);

			if(parent.numberOfActiveTemplates() == 0) {
				//We got an error, about the change
				item.setActive(!newValue);
				JOptionPane.showMessageDialog(parent, "At least one component must be active.", "Cannot Deactive All Components", JOptionPane.INFORMATION_MESSAGE);
			} else {
				//The change was ok, record it to undo/redo history
				undoManager.addNewEdit(new ToggleTemplateActivationCommand(parent.getTemplateExplorer(), item, newValue));
			}


			if (!selectedModel().isActive()){
				removeTemplateButton.setEnabled(true);
			}else {
				if (CreateGui.getCurrentTab().numberOfActiveTemplates() <= 1) {
					removeTemplateButton.setEnabled(false);
				} else {
					removeTemplateButton.setEnabled(true);
				}
			}
			 
			toggleAffectedQueries();
			list.repaint();
		}
		
		private void toggleAffectedQueries() {
			for(TAPNQuery query : parent.queries()) {
				ContainsPlaceWithDisabledTemplateVisitor visitor = new ContainsPlaceWithDisabledTemplateVisitor(parent.network());
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
			
			if (!isInAnimationMode){
				toggleSelection(index);
			}
			
		}

		public void valueChanged(ListSelectionEvent e) {
			if (!(e.getValueIsAdjusting())) {
				int index = templateList.getSelectedIndex();
				if (index == -1) {
					removeTemplateButton.setEnabled(false);
					renameButton.setEnabled(false);
					copyButton.setEnabled(false);
					moveUpButton.setEnabled(false);
					moveDownButton.setEnabled(false);
					sortButton.setEnabled(false);
				} else {
					if (buttonPanel != null) {
						if (CreateGui.getCurrentTab().numberOfActiveTemplates() > 1){
							removeTemplateButton.setEnabled(true);
						}else{
							if (selectedModel().isActive()){
								removeTemplateButton.setEnabled(false);
							} else {
								removeTemplateButton.setEnabled(true);
							}
						}
						renameButton.setEnabled(true);
						copyButton.setEnabled(true);
						if(templateList.getModel().getSize() >= 2)
							sortButton.setEnabled(true);
						else
							sortButton.setEnabled(false);
						
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
			if (!isInAnimationMode){
				toggleSelection(list.getSelectedIndex());
			}
		}
		
	}
}
