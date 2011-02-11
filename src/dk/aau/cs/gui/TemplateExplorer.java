package dk.aau.cs.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.PetriNetObject;
import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.Template;
import pipe.dataLayer.TimedPlaceComponent;
import pipe.gui.CreateGui;
import pipe.gui.undo.AddTemplateCommand;
import pipe.gui.undo.RemoveTemplateCommand;
import pipe.gui.undo.RenameTemplateCommand;
import pipe.gui.undo.UndoManager;
import pipe.gui.widgets.JSplitPaneFix;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.util.Require;

public class TemplateExplorer extends JPanel {
	private static final long serialVersionUID = -2334464984237161208L;

	private JSplitPane splitPane;

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

	public TemplateExplorer(TabContent parent) {
		this(parent, false);
	}

	public TemplateExplorer(TabContent parent, boolean hideButtons) {
		this.parent = parent;
		undoManager = parent.drawingSurface().getUndoManager();
		this.setLayout(new BorderLayout());
		init(hideButtons);
	}

	private void init(boolean hideButtons) {
		initExplorerPanel();

		initButtonsPanel();
		splitPane = new JSplitPaneFix(JSplitPane.VERTICAL_SPLIT, templatePanel,
				buttonPanel);
		splitPane.setContinuousLayout(true);
		splitPane.setDividerSize(0);
		splitPane.setDividerLocation(0.8);
		splitPane.setResizeWeight(1.0);

		setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createTitledBorder("Templates"), BorderFactory
				.createEmptyBorder(3, 3, 3, 3)));

		addCreatedComponents(hideButtons);
	}

	private void addCreatedComponents(boolean hideButtons) {
		this.removeAll();
		if (!hideButtons) {
			splitPane.setTopComponent(templatePanel);
			this.add(splitPane);
		} else {
			this.add(templatePanel);
		}
	}

	private void initExplorerPanel() {
		templatePanel = new JPanel(new BorderLayout());
		listModel = new DefaultListModel();
		for (Template<?> net : parent.templates()) {
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
			}

			public void intervalRemoved(ListDataEvent arg0) {
				templateList.setSelectedIndex(arg0.getIndex0() == 0 ? 0 : arg0
						.getIndex0() - 1);
			}
		});

		templateList = new JList(listModel);
		templateList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		templateList.setSelectedIndex(0);
		templateList.setVisibleRowCount(-1);
		templateList.setLayoutOrientation(JList.VERTICAL);
		templateList.setAlignmentX(Component.LEFT_ALIGNMENT);
		templateList.setAlignmentY(Component.TOP_ALIGNMENT);
		templateList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					if (templateList.getSelectedIndex() == -1) {
						removeTemplateButton.setEnabled(false);
						renameButton.setEnabled(false);
						copyButton.setEnabled(false);
					} else {
						if (buttonPanel != null) {
							if (listModel.size() > 1)
								removeTemplateButton.setEnabled(true);
							renameButton.setEnabled(true);
							copyButton.setEnabled(true);
						}
						templateList.ensureIndexIsVisible(e.getFirstIndex());
						openSelectedTemplate();
					}
				}
			}
		});

		scrollpane = new JScrollPane(templateList);
		templatePanel.add(scrollpane, BorderLayout.CENTER);

	}

	private void initButtonsPanel() {
		buttonPanel = new JPanel(new GridBagLayout());

		Dimension dimension = new Dimension(82, 23);
		newTemplateButton = new JButton("New");
		newTemplateButton.setEnabled(true);
		newTemplateButton.setPreferredSize(dimension);

		newTemplateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Template<TimedArcPetriNet> template = ShowNewTemplateDialog();
				if (template != null) {
					int index = listModel.size();
					listModel.addElement(template);
					undoManager.addNewEdit(new AddTemplateCommand(TemplateExplorer.this, template, index));
					parent.addTemplate(template);
					parent.drawingSurface().setModel(template.guiModel(), template.model());
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
				// TODO: check if any local places are used in queries and if so
				// warn user that these queries are removed too.
				// removeSelectedTemplate();
				// listModel.remove(templateList.getSelectedIndex());
				int index = templateList.getSelectedIndex();
				Template<TimedArcPetriNet> template = (Template<TimedArcPetriNet>) templateList.getSelectedValue();

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
					Command command = new RemoveTemplateCommand(TemplateExplorer.this, template, index);
					undoManager.addNewEdit(command);
					command.redo();
				}
				
			}

			private HashSet<TAPNQuery> findQueriesAffectedByRemoval(Template<TimedArcPetriNet> template) {
				Iterable<TAPNQuery> queries = parent.queries();
				HashSet<TAPNQuery> queriesToDelete = new HashSet<TAPNQuery>();

				for (TimedPlace p : template.model().places()) {
					for (TAPNQuery q : queries) {
						if (q.getProperty().containsAtomicPropositionWithSpecificPlaceInTemplate(template.model().getName(), p.name())) {
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
				// copy tapn
				// add copy
				// update UI
			}
		});

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		buttonPanel.add(copyButton, gbc);
	}

	private Template<TimedArcPetriNet> ShowNewTemplateDialog() {
		String templateName = (String) JOptionPane.showInputDialog(
				parent.drawingSurface(), "Template name:", "Rename Template",
				JOptionPane.PLAIN_MESSAGE, null, null, 
				parent.drawingSurface().getNameGenerator().getNewTemplateName());

		if (templateName != null) {
			if(!isNameAllowed(templateName)) {
				JOptionPane.showMessageDialog(parent.drawingSurface(),
						"Acceptable names for templates are defined by the regular expression:\n[a-zA-Z][_a-zA-Z0-9]*\n\nThe new template could not be created.",
						"Error Creating Template",
						JOptionPane.ERROR_MESSAGE);
			}
			else if (parent.network().hasTAPNCalled(templateName)) {
				JOptionPane.showMessageDialog(parent.drawingSurface(),
						"A template named \"" + templateName + "\" already exists.\n\nThe new template could not be created.",
						"Error Creating Template",
						JOptionPane.ERROR_MESSAGE);
			}
			else {
				Template<TimedArcPetriNet> template = createNewTemplate(templateName);
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
		Template<TimedArcPetriNet> template = selectedModel();

		String newName = (String) JOptionPane.showInputDialog(parent
				.drawingSurface(), "Template name:", "Rename Template",
				JOptionPane.PLAIN_MESSAGE, null, null, template.model().getName());
		if (newName == null || template.model().getName().equals(newName))
			return;

		if (!isNameAllowed(newName))
			JOptionPane.showMessageDialog(
							parent.drawingSurface(),
							"Acceptable names for templates are defined by the regular expression:\n[a-zA-Z][_a-zA-Z0-9]*.\n\nThe template could not be renamed.",
							"Error Renaming Template",
							JOptionPane.ERROR_MESSAGE);
		
		else if (parent.network().hasTAPNCalled(newName)) {
			JOptionPane.showMessageDialog(
							parent.drawingSurface(),
							"A template named \"" + newName + "\" already exists. Please try another name.",
							"Error", JOptionPane.ERROR_MESSAGE);
		} else {
			Command command = new RenameTemplateCommand(this, template.model(),	template.model().getName(), newName);
			undoManager.addNewEdit(command);
			command.redo();
		}

	}

	public Template<TimedArcPetriNet> createNewTemplate(String name) {
		TimedArcPetriNet tapn = new TimedArcPetriNet(name);

		return new Template<TimedArcPetriNet>(tapn, new DataLayer());
	}

	public void removeTemplate(int index, Template<TimedArcPetriNet> template) {
		listModel.remove(index);
		parent.network().remove(template.model());
	}

	public void addTemplate(int index, Template<TimedArcPetriNet> template) {
		listModel.add(index, template);
		parent.network().add(template.model());
	}

	public void openSelectedTemplate() {
		Template<TimedArcPetriNet> tapn = selectedModel();
		if (tapn != null) {
			parent.drawingSurface().setModel(tapn.guiModel(), tapn.model());
		}
	}

	public Template<TimedArcPetriNet> selectedModel() {
		return (Template<TimedArcPetriNet>) templateList.getSelectedValue();
	}

	public void setSelectedGuiModel(DataLayer guiModel) {
		selectedModel().setGuiModel(guiModel);
	}

	public void updateTemplateList() {
		listModel.clear();
		for (Template<?> net : parent.templates()) {
			listModel.addElement(net);
		}
	}

	public void hideButtons() {
		this.removeAll();
		addCreatedComponents(true);
	}

	public void showButtons() {
		this.removeAll();
		addCreatedComponents(false);
	}
}
