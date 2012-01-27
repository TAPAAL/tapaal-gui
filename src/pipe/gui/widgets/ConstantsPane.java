package pipe.gui.widgets;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;


import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import pipe.gui.CreateGui;
import pipe.gui.undo.UpdateConstantEdit;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.gui.undo.SortConstantsCommand;
import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.gui.components.ConstantsListModel;

public class ConstantsPane extends JPanel {
	private static final long serialVersionUID = -7883351020889779067L;
	private JPanel constantsPanel;
	private JScrollPane constantsScroller;
	private JPanel buttonsPanel;

	private JList constantsList;
	private ConstantsListModel listModel;
	private JButton editBtn;
	private JButton removeBtn;

	private TabContent parent;
	private JButton moveUpButton;
	private JButton moveDownButton;
	private JButton sortButton;
	
	private static final String toolTipEditConstant = "Edit value of selected constant.";
	private static final String toolTipRemoveConstant = "Remove selected constant.";
	private static final String toolTipNewConstant = "Create a new constant.";
	private static final String toolTipSortConstants = "Sort the constants.";
	//private static final String toolTipGlobalConstantsLabel = "Here you can define a global constant for reuse in different places.";
	

	public ConstantsPane(boolean enableAddButton, TabContent currentTab) {
		parent = currentTab;

		constantsPanel = new JPanel(new GridBagLayout());
		buttonsPanel = new JPanel(new GridBagLayout());

		listModel = new ConstantsListModel(parent.network());
		listModel.addListDataListener(new ListDataListener() {
			public void contentsChanged(ListDataEvent arg0) {
			}

			public void intervalAdded(ListDataEvent arg0) {
				constantsList.setSelectedIndex(arg0.getIndex0());
				constantsList.ensureIndexIsVisible(arg0.getIndex0());
			}

			public void intervalRemoved(ListDataEvent arg0) {
				int index = (arg0.getIndex0() == 0) ? 0 : (arg0.getIndex0() - 1);
				constantsList.setSelectedIndex(index);
				constantsList.ensureIndexIsVisible(index);
			}
		});
		
		constantsList = new JList(listModel);
		constantsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		constantsList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!(e.getValueIsAdjusting())) {
					if (constantsList.getSelectedIndex() == -1) {
						editBtn.setEnabled(false);
						removeBtn.setEnabled(false);

					} else {
						removeBtn.setEnabled(true);
						editBtn.setEnabled(true);
					}
					
					int index = constantsList.getSelectedIndex();
					if(index > 0)
						moveUpButton.setEnabled(true);
					else
						moveUpButton.setEnabled(false);
							
						
					if(index < parent.network().constants().size() - 1)
						moveDownButton.setEnabled(true);
					else
						moveDownButton.setEnabled(false);
				}
			}
		});

		constantsList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (!constantsList.isSelectionEmpty()) {
					if (arg0.getButton() == MouseEvent.BUTTON1 && arg0.getClickCount() == 2) {
						int index = constantsList.locationToIndex(arg0.getPoint());
						ListModel dlm = constantsList.getModel();
						Constant c = (Constant) dlm.getElementAt(index);
						constantsList.ensureIndexIsVisible(index);

						showEditConstantDialog(c);
					}
				}
			}
		});

		addConstantsComponents();
		addConstantsButtons(enableAddButton);

		setLayout(new BorderLayout());
		this.add(constantsPanel, BorderLayout.CENTER);
		this.add(buttonsPanel, BorderLayout.PAGE_END);

		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Global Constants"), 
				BorderFactory.createEmptyBorder(3, 3, 3, 3))
		);
		//this.setToolTipText(toolTipGlobalConstantsLabel);
		//showConstants();
	}

	private void addConstantsButtons(boolean enableAddButton) {
		editBtn = new JButton("Edit");
		editBtn.setEnabled(false);
		editBtn.setToolTipText(toolTipEditConstant);
		editBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Constant c = (Constant) constantsList.getSelectedValue();
				showEditConstantDialog(c);
			}
		});
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.anchor = GridBagConstraints.WEST;
		buttonsPanel.add(editBtn, gbc);

		removeBtn = new JButton("Remove");
		removeBtn.setEnabled(false);
		removeBtn.setToolTipText(toolTipRemoveConstant);
		removeBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String constName = ((Constant) constantsList.getSelectedValue()).name();
				removeConstant(constName);
			}
		});
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.WEST;
		buttonsPanel.add(removeBtn, gbc);

		JButton addConstantButton = new JButton("New");
		addConstantButton.setToolTipText(toolTipNewConstant);
		addConstantButton.setEnabled(enableAddButton);
		addConstantButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showEditConstantDialog(null);
			}
		});
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.anchor = GridBagConstraints.WEST;
		buttonsPanel.add(addConstantButton, gbc);
	}

	public void showConstants() {
		TimedArcPetriNetNetwork model = parent.network();
		if (model == null)
			return;

		listModel.updateAll();

	}


	private void addConstantsComponents() {
		constantsScroller = new JScrollPane(constantsList);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 3;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		constantsPanel.add(constantsScroller, gbc);
		
		moveUpButton = new JButton(new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("resources/Images/Up.png")));
		moveUpButton.setEnabled(false);
		moveUpButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = constantsList.getSelectedIndex();
				
				if(index > 0) {
					parent.swapConstants(index, index-1);
					showConstants();
					constantsList.setSelectedIndex(index-1);
				}
			}
		});
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.SOUTH;
		constantsPanel.add(moveUpButton,gbc);
		
		moveDownButton = new JButton(new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("resources/Images/Down.png")));
		moveDownButton.setEnabled(false);
		moveDownButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = constantsList.getSelectedIndex();
				
				if(index < parent.network().constants().size() - 1) {
					parent.swapConstants(index, index+1);
					showConstants();
					constantsList.setSelectedIndex(index+1);
				}
			}
		});
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.NORTH;
		constantsPanel.add(moveDownButton,gbc);
		
		//Sort button
		sortButton = new JButton("S");
		sortButton.setToolTipText(toolTipSortConstants);
		sortButton.setEnabled(true);
		sortButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Command sortConstantsCommand = new SortConstantsCommand(parent, ConstantsPane.this);
				CreateGui.getDrawingSurface().getUndoManager().addNewEdit(sortConstantsCommand);
				sortConstantsCommand.redo();
			}
		});
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTH;
		constantsPanel.add(sortButton,gbc);
	}

	private void showEditConstantDialog(Constant constant) {	
		ConstantsDialogPanel panel = null;
		if (constant != null)
			try {
				panel = new ConstantsDialogPanel(new JRootPane(),
						parent.network(), constant);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		else
			try {
				panel = new ConstantsDialogPanel(new JRootPane(),
						parent.network());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		panel.showDialog();
		showConstants();
	}

	protected void removeConstant(String name) {
		TimedArcPetriNetNetwork model = parent.network();
		Command edit = model.removeConstant(name);
		if (edit == null) {
			JOptionPane.showMessageDialog(CreateGui.getApp(),
					"You cannot remove a constant that is used in the net.\nRemove all references "
							+ "to the constant in the net and try again.",
					"Constant in use", JOptionPane.ERROR_MESSAGE);
		} else
			parent.drawingSurface().getUndoManager().addNewEdit(edit);

		//showConstants();
	}

	public void setNetwork(TimedArcPetriNetNetwork tapnNetwork) {
		listModel.setNetwork(tapnNetwork);
	}

	public void selectFirst() {
		constantsList.setSelectedIndex(0);
		
	}

}
