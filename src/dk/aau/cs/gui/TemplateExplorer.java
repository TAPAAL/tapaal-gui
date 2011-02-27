package dk.aau.cs.gui;

import java.awt.BorderLayout;
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
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.Template;
import pipe.gui.CreateGui;
import pipe.gui.undo.AddTemplateCommand;
import pipe.gui.undo.RemoveTemplateCommand;
import pipe.gui.undo.RenameTemplateCommand;
import pipe.gui.undo.UndoManager;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.util.Require;

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
		initExplorerPanel();
		initButtonsPanel();

		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Templates"),	BorderFactory.createEmptyBorder(3, 3, 3, 3)));

		addCreatedComponents(hideButtons);
	}

	private void addCreatedComponents(boolean hideButtons) {
		this.removeAll();
		if (!hideButtons) {
			this.add(templatePanel, BorderLayout.CENTER);
			this.add(buttonPanel, BorderLayout.PAGE_END);
		} else {
			this.add(templatePanel, BorderLayout.CENTER);
		}
	}

	private void initExplorerPanel() {
		templatePanel = new JPanel(new BorderLayout());
		listModel = new DefaultListModel();
		for (Template net : parent.templates()) {
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
				Template template = ShowNewTemplateDialog();
				if (template != null) {
					int index = listModel.size();
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
					Command command = new RemoveTemplateCommand(parent, TemplateExplorer.this, template, index, queriesToDelete);
					undoManager.addNewEdit(command);
					command.redo();
				}
				
			}

			private HashSet<TAPNQuery> findQueriesAffectedByRemoval(Template template) {
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
				Template template = selectedModel().copy();
				
				String name = template.model().getName();
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

		String newName = (String) JOptionPane.showInputDialog(parent.drawingSurface(), "Template name:", "Rename Template",
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
			parent.drawingSurface().getNameGenerator().updateTemplateIndex(newName);
			Command command = new RenameTemplateCommand(this, template.model(),	template.model().getName(), newName);
			undoManager.addNewEdit(command);
			command.redo();
		}

	}

	public Template createNewTemplate(String name) {
		TimedArcPetriNet tapn = new TimedArcPetriNet(name);

		return new Template(tapn, new DataLayer());
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
			parent.drawingSurface().setModel(tapn.guiModel(), tapn.model());
		}
	}

	public Template selectedModel() {
		return (Template) templateList.getSelectedValue();
	}

	public void updateTemplateList() {
		listModel.clear();
		for (Template net : parent.templates()) {
			listModel.addElement(net);
		}
	}

	public void hideButtons() {
		addCreatedComponents(true);
	}

	public void showButtons() {
		addCreatedComponents(false);
	}
}
