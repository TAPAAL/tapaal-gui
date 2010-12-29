package dk.aau.cs.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
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
import pipe.dataLayer.Template;
import pipe.gui.undo.AddTemplateCommand;
import pipe.gui.undo.RemoveTemplateCommand;
import pipe.gui.undo.RenameTemplateCommand;
import pipe.gui.undo.UndoManager;
import pipe.gui.widgets.JSplitPaneFix;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.tapn.TimedArcPetriNet;

public class TemplateExplorer extends JPanel {
	private static final long serialVersionUID = -2334464984237161208L;

	private static long templateID = 0;

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

		if (!hideButtons) {
			initButtonsPanel();
			splitPane = new JSplitPaneFix(JSplitPane.VERTICAL_SPLIT,
					templatePanel, buttonPanel);
			splitPane.setContinuousLayout(true);
			splitPane.setDividerSize(0);
			splitPane.setDividerLocation(0.8);
			splitPane.setResizeWeight(1.0);
			this.add(splitPane);
		} else {
			this.add(templatePanel);
		}
		setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createTitledBorder("Templates"), 
			BorderFactory.createEmptyBorder(3,3,3,3))
		);
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
						if (listModel.size() > 1)
							removeTemplateButton.setEnabled(true);
						renameButton.setEnabled(true);
						copyButton.setEnabled(true);
						templateList.ensureIndexIsVisible(e.getFirstIndex());
						openSelectedTemplate();
					}
				}
			}
		});

		// templateLabel = new JLabel("Templates:");
		// templateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		// templateLabel.setAlignmentY(Component.TOP_ALIGNMENT);
		// templatePanel.add(templateLabel, BorderLayout.PAGE_START);

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
					undoManager.addNewEdit(new AddTemplateCommand(
							TemplateExplorer.this, template, index));
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
				Template<TimedArcPetriNet> template = (Template<TimedArcPetriNet>) templateList
				.getSelectedValue();

				Command command = new RemoveTemplateCommand(
						TemplateExplorer.this, template, index);
				undoManager.addNewEdit(command);
				command.redo();
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
		String templateName = (String) JOptionPane.showInputDialog(parent
				.drawingSurface(), "Template name:", "Rename Template",
				JOptionPane.PLAIN_MESSAGE, null, null, "New TAPN Template "
				+ (++templateID));

		if (templateName != null && templateName.length() <= 0)
			JOptionPane
			.showMessageDialog(
					parent.drawingSurface(),
					"New TAPN template could not be created:\n\nYou must provide a proper name for the template",
					"Error Creating Template",
					JOptionPane.ERROR_MESSAGE);
		else if (templateName != null && templateName.length() > 0) {
			Template<TimedArcPetriNet> template = createNewTemplate(templateName);
			return template;
		}
		return null;
	}

	private void showRenameTemplateDialog() {
		Template<TimedArcPetriNet> template = selectedModel();

		String newName = (String) JOptionPane.showInputDialog(parent
				.drawingSurface(), "Template name:", "Rename Template",
				JOptionPane.PLAIN_MESSAGE, null, null, template.model()
				.getName());
		if (template.model().getName().equals(newName))
			return;

		if (newName != null && newName.length() <= 0)
			JOptionPane
			.showMessageDialog(
					parent.drawingSurface(),
					"TAPN template could not be renamed:\n\nYou must provide a proper name for the template",
					"Error Renaming Template",
					JOptionPane.ERROR_MESSAGE);
		else if (parent.network().hasTAPNCalled(newName)) {
			JOptionPane
			.showMessageDialog(
					parent.drawingSurface(),
					"There is already a template with that name. Try another name.",
					"Error", JOptionPane.ERROR_MESSAGE);
		} else {
			Command command = new RenameTemplateCommand(this, template.model(),
					template.model().getName(), newName);
			undoManager.addNewEdit(command);
			command.redo();
		}

	}

	public Template<TimedArcPetriNet> createNewTemplate(String name) {
		TimedArcPetriNet tapn = new TimedArcPetriNet(name);
		parent.network().add(tapn);

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

	private void openSelectedTemplate() {
		Template<TimedArcPetriNet> tapn = selectedModel();
		parent.drawingSurface().setModel(tapn.guiModel(), tapn.model());
	}

	public Template<TimedArcPetriNet> selectedModel() {
		return (Template<TimedArcPetriNet>) templateList.getSelectedValue();
	}
	
	public void updateTemplateList(){
		listModel.clear();
		for(Template<?> net : parent.templates()){
			listModel.addElement(net);
		}
	}
}
