package pipe.gui.widgets;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;


import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import dk.aau.cs.gui.undo.*;
import net.tapaal.resourcemanager.ResourceManager;
import pipe.gui.CreateGui;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.gui.components.ConstantsListModel;
import dk.aau.cs.gui.components.NonsearchableJList;
import pipe.gui.MessengerImpl;

public class ConstantsPane extends JPanel implements SidePane {

	private final JPanel constantsPanel;
	private JScrollPane constantsScroller;
	private final JPanel buttonsPanel;

	private final JList<Constant> constantsList;
	private final ConstantsListModel listModel;
	private JButton editBtn;
	private JButton removeBtn;

	private final TabContent parent;
	private JButton moveUpButton;
	private JButton moveDownButton;
	private JButton sortButton;

	private static final String toolTipEditConstant = "Edit the value of the selected constant";
	private static final String toolTipRemoveConstant = "Remove the selected constant";
	private static final String toolTipNewConstant = "Create a new constant";
	private static final String toolTipSortConstants = "Sort the constants alphabetically";
	private final static String toolTipMoveUp = "Move the selected constant up";
	private final static String toolTipMoveDown = "Move the selected constant down";
	//private static final String toolTipGlobalConstantsLabel = "Here you can define a global constant for reuse in different places.";
	Timer timer;


	public ConstantsPane(TabContent currentTab) {
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

		constantsList = new NonsearchableJList<>(listModel);
		constantsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		constantsList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!(e.getValueIsAdjusting())) {
					updateButtons();
				}
			}
		});

		constantsList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (!constantsList.isSelectionEmpty()) {
					
					int index = constantsList.locationToIndex(arg0.getPoint());
					ListModel dlm = constantsList.getModel();
					Constant c = (Constant) dlm.getElementAt(index);
					constantsList.ensureIndexIsVisible(index);
					
					highlightConstant(index);
					
					if (arg0.getButton() == MouseEvent.BUTTON1 && arg0.getClickCount() == 2) {
						showEditConstantDialog(c);
					}
				}
			}
		});
		
		constantsList.addKeyListener(new KeyAdapter() {
		
			public void keyPressed(KeyEvent arg0) {				
				ListModel model = constantsList.getModel();
				if (model.getSize()>0) {
					Constant c = (Constant) model.getElementAt(constantsList.getSelectedIndex());
					if (c != null) {
						if (arg0.getKeyCode() == KeyEvent.VK_LEFT) {										
							if (!(c.lowerBound() == c.value())){
								Command edit = parent.network().updateConstant(c.name(), new Constant(
										c.name(), c.value()-1));
								CreateGui.getCurrentTab().getUndoManager().addNewEdit(edit);
								parent.network().buildConstraints();
							}
						}
						else if (arg0.getKeyCode() == KeyEvent.VK_RIGHT) {
							if (!(c.upperBound() == c.value())){
								Command edit = parent.network().updateConstant(c.name(), new Constant(
										c.name(), c.value()+1));
								CreateGui.getCurrentTab().getUndoManager().addNewEdit(edit);
								parent.network().buildConstraints();
							}
						} 
						else if (arg0.getKeyCode() == KeyEvent.VK_UP) {
							if(constantsList.getSelectedIndex() > 0){
								highlightConstant(constantsList.getSelectedIndex()-1);
							}
						}
						else if (arg0.getKeyCode() == KeyEvent.VK_DOWN) {
							if(constantsList.getSelectedIndex() < constantsList.getModel().getSize()-1){
								highlightConstant(constantsList.getSelectedIndex()+1);
							}
						}
					}
				}
			}
		});
		
		addConstantsComponents();
		addConstantsButtons();
		
		constantsList.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				removeConstantHighlights();
			}
		});

		setLayout(new BorderLayout());
		this.add(constantsPanel, BorderLayout.CENTER);
		this.add(buttonsPanel, BorderLayout.PAGE_END);

		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Global Constants"), 
				BorderFactory.createEmptyBorder(3, 3, 3, 3))
				);
		this.setToolTipText("Declaration of global constants that can be used in intervals and age invariants");

		//this.setToolTipText(toolTipGlobalConstantsLabel);
		//showConstants();
		
		this.addComponentListener(new ComponentListener() {
			int minimumHegiht = ConstantsPane.this.getMinimumSize().height;
			public void componentShown(ComponentEvent e) {
			}
			
			
			public void componentResized(ComponentEvent e) {
				if(ConstantsPane.this.getSize().height <= minimumHegiht){
					sortButton.setVisible(false);
				} else {
					sortButton.setVisible(true);
				}
			}
			
			
			public void componentMoved(ComponentEvent e) {
			}
			
			
			public void componentHidden(ComponentEvent e) {
			}
		});
		
		this.setMinimumSize(new Dimension(this.getMinimumSize().width, this.getMinimumSize().height - sortButton.getMinimumSize().height));

	}

    private void updateButtons() {
        int index = constantsList.getSelectedIndex();
        if (index == -1 || constantsList.getSelectedValuesList().isEmpty()) {
            editBtn.setEnabled(false);
            removeBtn.setEnabled(false);
        } else {
            removeBtn.setEnabled(true);
            editBtn.setEnabled(true);
        }
        sortButton.setEnabled(constantsList.getModel().getSize() >= 2);
        moveUpButton.setEnabled(index > 0 && !constantsList.getSelectedValuesList().isEmpty());
        moveDownButton.setEnabled(index < parent.network().constants().size() - 1 && !constantsList.getSelectedValuesList().isEmpty());
    }
	
	private void highlightConstant(int index){
		ListModel model = constantsList.getModel();
		Constant c = (Constant) model.getElementAt(index);
		if(timer != null) {
			timer.stop();
		}
		if(c != null && !c.hasFocus()){
			for(int i = 0; i < model.getSize(); i++){
				((Constant) model.getElementAt(i)).setFocused(false);
			}
			for(int i = 0; i < model.getSize(); i++){
				((Constant) model.getElementAt(i)).setVisible(true);
			}
			c.setFocused(true);
			CreateGui.getCurrentTab().drawingSurface().repaintAll();
			blinkConstant(c);
		}
	}
	
	public void removeConstantHighlights(){
		ListModel model = constantsList.getModel();
		for(int i = 0; i < model.getSize(); i++){
			((Constant) model.getElementAt(i)).setFocused(false);
		}
		try{
			CreateGui.getCurrentTab().drawingSurface().repaintAll();
		}catch(Exception e){
			// It is okay, the tab has just been closed
		}
	}
	
	private void blinkConstant(final Constant c) {
		timer = new Timer(300, new ActionListener() {
			long startTime = System.currentTimeMillis();
			@Override
			public void actionPerformed(ActionEvent e) {
				if(System.currentTimeMillis() - startTime < 2100) {
					if(!c.getVisible()) {
						c.setVisible(true);
						CreateGui.getCurrentTab().drawingSurface().repaintAll();
					} else {
						c.setVisible(false);
						CreateGui.getCurrentTab().drawingSurface().repaintAll();
					}
				} else {
					((Timer) e.getSource()).stop();
				}
			}
		});
		timer.setRepeats(true);
	    timer.setCoalesce(true);
		timer.restart();
	}

	private void addConstantsButtons() {
		editBtn = new JButton("Edit");
		editBtn.setEnabled(!constantsList.getSelectedValuesList().isEmpty());
		editBtn.setToolTipText(toolTipEditConstant);
		editBtn.addActionListener(e -> {
			Constant c = (Constant) constantsList.getSelectedValue();
			showEditConstantDialog(c);
		});
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.anchor = GridBagConstraints.WEST;
		buttonsPanel.add(editBtn, gbc);

		removeBtn = new JButton("Remove");
		removeBtn.setEnabled(!constantsList.getSelectedValuesList().isEmpty());
		removeBtn.setToolTipText(toolTipRemoveConstant);
		removeBtn.addActionListener(e -> {
			removeConstants();
		});
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.WEST;
		buttonsPanel.add(removeBtn, gbc);

		JButton addConstantButton = new JButton("New");
		addConstantButton.setToolTipText(toolTipNewConstant);
		addConstantButton.setEnabled(true);
		addConstantButton.addActionListener(e -> showEditConstantDialog(null));
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

        updateButtons();
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

		moveUpButton = new JButton(ResourceManager.getIcon("Up.png"));
		moveUpButton.setMargin(new Insets(2,2,2,2));
		moveUpButton.setEnabled(!constantsList.getSelectedValuesList().isEmpty());
		moveUpButton.setToolTipText(toolTipMoveUp);
		moveUpButton.addActionListener(e -> {
			int index = constantsList.getSelectedIndex();

			if(index > 0) {
                Command c = new MoveElementUpCommand(ConstantsPane.this, index, index-1);
                parent.getUndoManager().addNewEdit(c);
                c.redo();
				constantsList.setSelectedIndex(index-1);
			}
		});

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.SOUTH;
		constantsPanel.add(moveUpButton,gbc);

		moveDownButton = new JButton(ResourceManager.getIcon("Down.png"));
		moveDownButton.setMargin(new Insets(2,2,2,2));
		moveDownButton.setEnabled(!constantsList.getSelectedValuesList().isEmpty());
		moveDownButton.setToolTipText(toolTipMoveDown);
		moveDownButton.addActionListener(e -> {
			int index = constantsList.getSelectedIndex();

			if(index < parent.network().constants().size() - 1) {
                Command c = new MoveElementDownCommand(ConstantsPane.this, index, index+1);
                parent.getUndoManager().addNewEdit(c);
                c.redo();
				constantsList.setSelectedIndex(index+1);
			}
		});

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.NORTH;
		constantsPanel.add(moveDownButton,gbc);

		//Sort button
		sortButton = new JButton(ResourceManager.getIcon("Sort.png"));
		sortButton.setMargin(new Insets(2,2,2,2));
		sortButton.setToolTipText(toolTipSortConstants);
		sortButton.setEnabled(false);
		sortButton.addActionListener(e -> {
			Command sortConstantsCommand = new SortConstantsCommand(parent, ConstantsPane.this);
			CreateGui.getCurrentTab().getUndoManager().addNewEdit(sortConstantsCommand);
			sortConstantsCommand.redo();
		});

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTH;
		constantsPanel.add(sortButton,gbc);
	}

	private void showEditConstantDialog(Constant constant) {
		ConstantsDialogPanel panel = new ConstantsDialogPanel(parent.network(), constant);

		panel.showDialog();
		showConstants();
	}

	protected void removeConstants() {
        TimedArcPetriNetNetwork model = parent.network();
        java.util.List<String> unremovableConstants = new ArrayList<>();
        parent.getUndoManager().newEdit();

        for (Object o : constantsList.getSelectedValuesList()) {
            String name = ((Constant)o).name();
            Command command = model.removeConstant(name);
            if (command == null) {
                unremovableConstants.add(name);
            } else {
                parent.getUndoManager().addEdit(command);
            }
        }
        if (unremovableConstants.size() > 0) {
            StringBuilder message = new StringBuilder("The following constants could not be removed: \n");

            for (String name : unremovableConstants) {
                message.append("   - ");
                message.append(name);
                message.append("\n");
            }
            message.append("\nYou cannot remove a constant that is used in the net.\nRemove all references " +
                           "to the constant(s) in the net and try again.");

            JOptionPane.showMessageDialog(CreateGui.getApp(), message.toString(),
                "Constant in use", JOptionPane.ERROR_MESSAGE);
        }
	}

	public void setNetwork(TimedArcPetriNetNetwork tapnNetwork) {
		listModel.setNetwork(tapnNetwork);
	}

	public void selectFirst() {
		constantsList.setSelectedIndex(0);

	}

    @Override
    public void moveUp(int index) {
        parent.swapConstants(index, index-1);
        showConstants();
    }

    @Override
    public void moveDown(int index) {
        parent.swapConstants(index, index+1);
        showConstants();
    }

    @Override
    public JList<Constant> getJList() {
        return constantsList;
    }
}
