package net.tapaal.gui.petrinet.editor;

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
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
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
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import pipe.gui.petrinet.PetriNetTab;
import net.tapaal.gui.petrinet.undo.MoveElementDownCommand;
import net.tapaal.gui.petrinet.undo.MoveElementUpCommand;
import net.tapaal.resourcemanager.ResourceManager;
import net.tapaal.swinghelpers.GridBagHelper;
import net.tapaal.swinghelpers.SwingHelper;
import pipe.gui.petrinet.dataLayer.DataLayer;
import net.tapaal.gui.petrinet.verification.TAPNQuery;
import net.tapaal.gui.petrinet.Template;
import pipe.gui.TAPAALGUI;
import pipe.gui.canvas.Zoomer;
import net.tapaal.gui.petrinet.undo.AddTemplateCommand;
import net.tapaal.gui.petrinet.undo.RemoveTemplateCommand;
import net.tapaal.gui.petrinet.undo.RenameTemplateCommand;
import net.tapaal.gui.petrinet.undo.ToggleTemplateActivationCommand;
import pipe.gui.petrinet.undo.UndoManager;
import pipe.gui.swingcomponents.EscapableDialog;
import net.tapaal.swinghelpers.RequestFocusListener;
import dk.aau.cs.TCTL.visitors.BooleanResult;
import dk.aau.cs.TCTL.visitors.ContainsPlaceWithDisabledTemplateVisitor;
import net.tapaal.gui.swingcomponents.NonsearchableJList;
import net.tapaal.gui.petrinet.undo.Command;
import net.tapaal.gui.petrinet.undo.SortTemplatesCommand;
import dk.aau.cs.model.tapn.SharedTransition;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.util.Require;
import dk.aau.cs.util.Tuple;
import net.tapaal.gui.petrinet.widgets.SidePane;

import static net.tapaal.swinghelpers.GridBagHelper.Anchor.*;
import static net.tapaal.swinghelpers.GridBagHelper.Fill.BOTH;

public class TemplateExplorer extends JPanel implements SidePane {

	// Template explorer panel items
	private JPanel templatePanel;
	private JScrollPane scrollpane;
	private JList<Template> templateList;
	private DefaultListModel<Template> listModel;

	// Template button panel items
	private JPanel buttonPanel;
	private JButton newTemplateButton;
	private JButton removeTemplateButton;
	private JButton renameButton;
	private JButton copyButton;

	private final PetriNetTab parent;
	private final UndoManager undoManager;
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
	
	public TemplateExplorer(PetriNetTab parent) {
		this(parent, false);
	}

	public TemplateExplorer(PetriNetTab parent, boolean hideButtons) {
		this.parent = parent;
		undoManager = parent.getUndoManager();
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
			final int minimumHegiht = TemplateExplorer.this.getMinimumSize().height + sortButton.getMinimumSize().height;

			public void componentResized(ComponentEvent e) {
				if(!isInAnimationMode){
                    sortButton.setVisible(TemplateExplorer.this.getSize().height > minimumHegiht);
				}
			}

            public void componentShown(ComponentEvent e) {}
			public void componentMoved(ComponentEvent e) {}
			public void componentHidden(ComponentEvent e) {}
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
		listModel = new DefaultListModel<>();
		for (Template net : parent.allTemplates()) {
			listModel.addElement(net);
		}

		listModel.addListDataListener(new ListDataListener() {
			public void contentsChanged(ListDataEvent arg0) {
                removeTemplateButton.setEnabled(parent.numberOfActiveTemplates() <= 1);
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

		templateList = new NonsearchableJList<>(listModel);

		templateList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		templateList.setSelectedIndex(0);
		templateList.setCellRenderer(new TemplateListCellRenderer<>(templateList.getCellRenderer()));
		
		TemplateListManager manager = new TemplateListManager(templateList);
		templateList.addListSelectionListener(manager);
		templateList.addMouseListener(manager);

		scrollpane = new JScrollPane(templateList);
		//Add 10 pixel to the minimumsize of the scrollpane
		scrollpane.setMinimumSize(new Dimension(scrollpane.getMinimumSize().width, scrollpane.getMinimumSize().height + 20));
		
		var gbc = GridBagHelper.as(0, 0, NORTHWEST, BOTH);
		gbc.gridheight = 3;
		gbc.weightx = 1;
		gbc.weighty = 1;

		templatePanel.add(scrollpane, gbc);
		
		moveUpButton = new JButton(ResourceManager.getIcon("Up.png"));
		moveUpButton.setMargin(new Insets(2,2,2,2));
		moveUpButton.setEnabled(false);
		moveUpButton.setToolTipText(toolTipMoveUp);
		moveUpButton.addActionListener(e -> {
            int index = templateList.getSelectedIndex();

            if(index > 0) {
                Command c = new MoveElementUpCommand(TemplateExplorer.this, index, index-1);
                undoManager.addNewEdit(c);
                c.redo();
                templateList.ensureIndexIsVisible(index+1);
                templateList.setSelectedIndex(index-1);
            }
        });
		
		gbc = GridBagHelper.as(1, 0, SOUTH);
		templatePanel.add(moveUpButton,gbc);
		
		moveDownButton = new JButton(ResourceManager.getIcon("Down.png"));
		moveDownButton.setMargin(new Insets(2,2,2,2));
		moveDownButton.setEnabled(false);
		moveDownButton.setToolTipText(toolTipMoveDown);
		moveDownButton.addActionListener(e -> {
            int index = templateList.getSelectedIndex();

            if(index < parent.network().allTemplates().size() - 1) {
                Command c = new MoveElementDownCommand(TemplateExplorer.this, index, index+1);
                undoManager.addNewEdit(c);
                c.redo();
                templateList.ensureIndexIsVisible(index+1);
                templateList.setSelectedIndex(index+1);
            }
        });
		
		gbc = GridBagHelper.as(1, 1, NORTH);
		templatePanel.add(moveDownButton,gbc);
		
		//Sort button
		sortButton = new JButton(ResourceManager.getIcon("Sort.png"));
		sortButton.setMargin(new Insets(2,2,2,2));
		sortButton.setToolTipText(toolTipSortComponents);
		sortButton.setEnabled(false);
		sortButton.addActionListener(e -> {
			Command command = new SortTemplatesCommand(parent, TemplateExplorer.this, templateList, listModel);
			undoManager.addNewEdit(command);
			command.redo();
		});
		
		gbc = GridBagHelper.as(1,2, NORTH, GridBagHelper.Fill.HORIZONTAL);
		templatePanel.add(sortButton,gbc);
	}

	private void initButtonsPanel() {
		buttonPanel = new JPanel(new GridBagLayout());

		Dimension dimension = new Dimension(82, 28);
		newTemplateButton = new JButton("New");
		newTemplateButton.setEnabled(true);
		newTemplateButton.setPreferredSize(dimension);
		newTemplateButton.setToolTipText(toolTipNewComponent);

		newTemplateButton.addActionListener(arg0 -> ShowNewTemplateDialog(""));

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
							TAPAALGUI.getApp(), warning.toString(), "Warning",
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

		gbc = GridBagHelper.as(0, 1, WEST);
		buttonPanel.add(removeTemplateButton, gbc);

		renameButton = new JButton("Rename");
		renameButton.setEnabled(false);
		renameButton.setPreferredSize(dimension);
		renameButton.setToolTipText(toolTipRenameComponent);

		renameButton.addActionListener(arg0 -> {
            showRenameTemplateDialog("");
            templateList.validate();
        });

		gbc = GridBagHelper.as(1, 0, WEST);
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
			}
		});

		gbc = GridBagHelper.as(0, 0, WEST);
		buttonPanel.add(copyButton, gbc);
	}
	
	private EscapableDialog dialog;
	private JPanel container;
	private JTextField nameTextField;
    private JLabel nameLabel;
	private JPanel buttonContainer;
	private JButton okButton;
	private JButton cancelButton;
	private JPanel nameContainer;
	
	private void onOKRenameTemplate() {		
		Template template = selectedModel();			
		String newName = nameTextField.getText().trim();
		if (template.model().name().equals(newName)) {
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
		else if (parent.network().hasTAPNCalled(newName) && !template.model().name().equals(newName)) {
			JOptionPane.showMessageDialog(
							parent.drawingSurface(),
							"A component named \"" + newName + "\" already exists. Try another name.",
							"Error", JOptionPane.ERROR_MESSAGE);
			exit();
			showRenameTemplateDialog(newName);
			return;
		} else {
			parent.getNameGenerator().updateTemplateIndex(newName);
			Command command = new RenameTemplateCommand(this, parent, template.model(), template.model().name(), newName);
			undoManager.addNewEdit(command);
			command.redo();
		}
		exit();
	}
	
	private void onOK() {
        String templateName = nameTextField.getText().trim();
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
            Template template = createNewTemplate(templateName);

            int index = listModel.size();
            undoManager.addNewEdit(new AddTemplateCommand(TemplateExplorer.this, template, index));
            parent.addTemplate(template);
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

        nameTextField = new javax.swing.JTextField();
        SwingHelper.setPreferredWidth(nameTextField,330);
		nameTextField.setText(nameToShow);
		nameTextField.addAncestorListener(new RequestFocusListener());
		nameTextField.addActionListener(e -> {
			okButton.requestFocusInWindow();
			okButton.doClick();
		});

		var gbc = GridBagHelper.as(0, 1, 1, WEST, new Insets(4, 4, 2, 4));
		nameContainer.add(nameTextField,gbc);
		
		nameLabel = new JLabel(); 
		nameLabel.setText("Name of component: ");
		gbc = GridBagHelper.as(0, 0, 1, WEST, new Insets(4, 4, 2, 4));
		nameContainer.add(nameLabel,gbc);
		
		buttonContainer = new JPanel();
		buttonContainer.setLayout(new GridBagLayout());

		okButton = new JButton();
		okButton.setText("OK");
		okButton.setMaximumSize(new java.awt.Dimension(100, 25));
		okButton.setMinimumSize(new java.awt.Dimension(100, 25));
		okButton.setPreferredSize(new java.awt.Dimension(100, 25));
		okButton.setMnemonic(KeyEvent.VK_O);
		gbc = GridBagHelper.as(1,0, WEST, new Insets(5, 5, 5, 5));
		buttonContainer.add(okButton,gbc);
		
		cancelButton = new JButton();
		cancelButton.setText("Cancel");
		cancelButton.setMaximumSize(new java.awt.Dimension(100, 25));
		cancelButton.setMinimumSize(new java.awt.Dimension(100, 25));
		cancelButton.setPreferredSize(new java.awt.Dimension(100, 25));
		cancelButton.setMnemonic(KeyEvent.VK_C);
		gbc = GridBagHelper.as(0, 0, GridBagConstraints.RELATIVE, EAST);
		buttonContainer.add(cancelButton,gbc);		
		
		okButton.addActionListener(e -> onOK());
		
		cancelButton.addActionListener(e -> exit());
		
		gbc = GridBagHelper.as(0,1,1,EAST, new Insets(0, 8, 5, 8));
		container.add(buttonContainer,gbc);
		
		gbc = GridBagHelper.as(0, 0, 1, WEST, new Insets(0, 8, 5, 8));
		container.add(nameContainer,gbc);
	}

	private void ShowNewTemplateDialog(String nameToShow) {
		dialog = new EscapableDialog(TAPAALGUI.getApp(), "Enter Component Name", true);

		initComponentsOfNewTemplateDialog(nameToShow);

		// setResizable seems to be platform dependent so use scrolling as a fallback
        JScrollPane scrollPane = new JScrollPane(container);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setBorder(null);

		dialog.add(scrollPane);
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

		nameContainer = new JPanel();
		nameContainer.setLayout(new GridBagLayout());

		nameTextField = new javax.swing.JTextField();
		SwingHelper.setPreferredWidth(nameTextField,330);
		nameTextField.setText(oldname);
		nameTextField.addAncestorListener(new RequestFocusListener());
		nameTextField.addActionListener(e -> {
			okButton.requestFocusInWindow();
			okButton.doClick();
		});
		var gbc = GridBagHelper.as(0,1, 1, WEST, new Insets(4, 4, 2, 4));
		nameContainer.add(nameTextField,gbc);
		
		nameLabel = new JLabel(); 
		nameLabel.setText("Name of component: ");

		gbc = GridBagHelper.as(0,0,1,WEST,new Insets(4, 4, 2, 4));
		nameContainer.add(nameLabel,gbc);
		
		buttonContainer = new JPanel();
		buttonContainer.setLayout(new GridBagLayout());

		okButton = new JButton();
		okButton.setText("OK");
		okButton.setMaximumSize(new java.awt.Dimension(100, 25));
		okButton.setMinimumSize(new java.awt.Dimension(100, 25));
		okButton.setPreferredSize(new java.awt.Dimension(100, 25));
		okButton.setMnemonic(KeyEvent.VK_O);

		gbc = GridBagHelper.as(1,0, WEST, new Insets(5, 5, 5, 5));
		buttonContainer.add(okButton,gbc);
		
		cancelButton = new JButton();
		cancelButton.setText("Cancel");
		cancelButton.setMaximumSize(new java.awt.Dimension(100, 25));
		cancelButton.setMinimumSize(new java.awt.Dimension(100, 25));
		cancelButton.setPreferredSize(new java.awt.Dimension(100, 25));
		cancelButton.setMnemonic(KeyEvent.VK_C);

		gbc = GridBagHelper.as(0,0, GridBagConstraints.RELATIVE, EAST);
		buttonContainer.add(cancelButton,gbc);		
		
		okButton.addActionListener(e -> onOKRenameTemplate());
		
		cancelButton.addActionListener(e -> exit());

        container = new JPanel();
        container.setLayout(new GridBagLayout());
		
		gbc = GridBagHelper.as(0,1,1,EAST,new Insets(0, 8, 5, 8));
		container.add(buttonContainer,gbc);
		
		gbc = GridBagHelper.as(0,0,1,WEST,new Insets(0, 8, 5, 8));
		container.add(nameContainer,gbc);

	}

	private void showRenameTemplateDialog(String nameToShow) {		
		dialog = new EscapableDialog(TAPAALGUI.getApp(), "Enter Component Name", true);
		Template template = selectedModel();
		if (nameToShow.equals("")){
			initComponentsOfRenameTemplateDialog(template.model().name());
		}
		else {
			initComponentsOfRenameTemplateDialog(nameToShow);
		}

		// setResizable seems to be platform dependent so use scrolling as a fallback
        JScrollPane scrollPane = new JScrollPane(container);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setBorder(null);

		dialog.add(scrollPane);
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
			parent.changeToTemplate(tapn);
		}
	}

	public Template selectedModel() {
		return templateList.getSelectedValue();
	}

    public void selectTemplate(Template template) {
        for (int i = 0; i < listModel.getSize(); i++) {
            if (listModel.getElementAt(i).equals(template)) {
                templateList.setSelectedIndex(i);
                return;
            }
        }
    }

	public void updateTemplateList() {
		int selectedIndex = templateList.getSelectedIndex();
		DefaultListModel<Template> newList = new DefaultListModel<>();
		
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

    @Override
    public void moveUp(int index) {
        parent.swapTemplates(index, index-1);
        Template o = listModel.getElementAt(index);
        listModel.setElementAt(listModel.getElementAt(index-1), index);
        listModel.setElementAt(o, index-1);
    }

    @Override
    public void moveDown(int index) {
        parent.swapTemplates(index, index+1);
        Template o = listModel.getElementAt(index);
        listModel.setElementAt(listModel.getElementAt(index+1), index);
        listModel.setElementAt(o, index+1);
    }
    @Override
    public JList<Template> getJList(){
	    return templateList;
    }

    private class TemplateListCellRenderer<T> extends JPanel implements ListCellRenderer<T> {

		private static final String UNCHECK_TO_DEACTIVATE = "Uncheck to deactive the component";
		private static final String CHECK_TO_ACTIVATE = "Check to active the component";
		private final JCheckBox activeCheckbox = new JCheckBox();
		private final ListCellRenderer<T> cellRenderer;
		
		
		public TemplateListCellRenderer(ListCellRenderer<T> renderer) {
			cellRenderer = renderer;
			setLayout(new BorderLayout()); 
	        setOpaque(false); 
	        activeCheckbox.setOpaque(false);
		}

        @Override
        public Component getListCellRendererComponent(JList<? extends T> list, T value, int index, boolean isSelected, boolean cellHasFocus) {
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
		private final int checkBoxWidth = new JCheckBox().getPreferredSize().width;
		private final ListSelectionModel selectionModel;
		private final JList<Template> list;
		
		public TemplateListManager(JList<Template> list) {
			this.list = list;
			selectionModel = list.getSelectionModel();
			this.list.registerKeyboardAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), JComponent.WHEN_FOCUSED);
		}
		
		private void toggleSelection(int index) { 
			if(index<0) 
				return; 

			Template item = list.getModel().getElementAt(index);


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
                removeTemplateButton.setEnabled(parent.numberOfActiveTemplates() > 1);
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
			
			if(e.getX()>templateList.getCellBounds(index, index).x+checkBoxWidth) {
                return;
            }

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
						if (parent.numberOfActiveTemplates() > 1){
							removeTemplateButton.setEnabled(true);
						}else{
                            removeTemplateButton.setEnabled(!selectedModel().isActive());
						}
						renameButton.setEnabled(true);
						copyButton.setEnabled(true);
                        sortButton.setEnabled(templateList.getModel().getSize() >= 2);

                        moveUpButton.setEnabled(index > 0);
                        moveDownButton.setEnabled(index < parent.network().allTemplates().size() - 1);
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
