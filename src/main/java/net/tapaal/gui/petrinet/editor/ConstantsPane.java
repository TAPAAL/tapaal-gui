package net.tapaal.gui.petrinet.editor;

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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;


import javax.swing.*;


import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.Variable;
import dk.aau.cs.util.Require;
import net.tapaal.gui.petrinet.undo.Command;
import net.tapaal.gui.petrinet.undo.SortConstantsCommand;
import net.tapaal.gui.petrinet.Template;
import pipe.gui.TAPAALGUI;
import pipe.gui.petrinet.PetriNetTab;
import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import net.tapaal.gui.swingcomponents.NonsearchableJList;
import pipe.gui.petrinet.undo.UndoManager;
import net.tapaal.gui.petrinet.widgets.SidePane;


public class ConstantsPane extends JPanel implements SidePane {

	private final JPanel constantsPanel;
    private final JPanel buttonsPanel;

    private final ColorTypesListModel colorTypesListModel;
    private final VariablesListModel variablesListModel;
	private final JList<?> list;
	private final ConstantsListModel constantsListModel;
	private JButton editBtn;
	private JButton removeBtn;
	private JButton addConstantButton;
    private JButton manuallyEditBtn;

	private final PetriNetTab tab;
	private JButton moveUpButton;
	private JButton moveDownButton;
	private JButton sortButton;

    private static final String titleBorder = "Global constants, Color types and Variables";
    private static final String titleBorderNoColor = "Global constants";
    private static final String titleBorderSMC = "Global constants and SMC Settings";
    private static final String titleBorderToolTip = "<html>Declaration of colors types, color variables and global integer constants.<br>To see a summary list press SHIFT + F</html>";
    private static final String titleBorderToolTipNoColor = "Declaration of global constants that can be used in intervals and age invariants";
    private static final String titleBorderToolTipSmc = "Declaration of global constants that can be used in intervals and age invariants, and editing of SMC engine settings";

    private static final String CONSTANTS = "Constants";
    private static final String COLORTYPES = "Color type";
    private static final String VARIABLES = "Variables";
    private static final String SMC_SETTINGS = "SMC Settings";
    private final JComboBox<String> constantsColorTypesVariablesComboBox = new JComboBox<>(new String[]{COLORTYPES, VARIABLES, CONSTANTS});


    private static final String toolTipComboBox = "Switch between constants, colors and variables";
    private static final String toolTipManuallyEdit = "Manualy edit the values of all constants, color types and variables";
    private static final String toolTipEditConstant = "Edit the value of the selected constant";
    private static final String toolTipRemoveConstant = "Remove the selected constant";
    private static final String toolTipNewConstant = "Create a new constant";
    private static final String toolTipSortConstants = "Sort the constants alphabetically";
    private final static String toolTipMoveUpConstant = "Move the selected constant up";
    private final static String toolTipMoveDownConstant = "Move the selected constant down";
    private final static String toolTipConstantList = "Shows every constant. displays the name and its assigned value";

    private static final String toolTipEditVariable = "Edit the value of the selected variable";
    private static final String toolTipRemoveVariable = "Remove the selected variable";
    private static final String toolTipNewVariable = "Create a new Variable";
    private static final String toolTipSortVariables = "sort the variables alphabetically";
    private static final String toolTipMoveUpVariable = "Move the selected variable up";
    private static final String toolTipMoveDownVariable = "Move the selected variable down";
    private static final String toolTipVariableList = "Displays all variables. Is only used for CPN. Shows";

    private static final String toolTipEditColorType = "Edit the value of the selected color type";
    private static final String toolTipRemoveColorType = "Remove the selected color type";
    private static final String toolTipNewColorType = "Create a new color type";
    private static final String toolTipSortColorTypes = "Sort the color types alphabetically";
    private static final String toolTipMoveUpColorType = "Move the selected color type up";
    private static final String toolTipMoveDownColorType = "Move down the selected color type";
    private static final String toolTipColorTypeList = "Shows all color types and product types. Product types are noted with DOMAIN. only usable in CPN";
    Timer timer;


	public ConstantsPane(PetriNetTab currentTab) {
		tab = currentTab;
        list = new NonsearchableJList<>();
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		constantsPanel = new JPanel(new GridBagLayout());
		buttonsPanel = new JPanel(new GridBagLayout());
        colorTypesListModel = new ColorTypesListModel(tab.network());
        variablesListModel = new VariablesListModel(tab.network());
		constantsListModel = new ConstantsListModel(tab.network());
		constantsListModel.addListDataListener(new ListDataListener() {
			public void contentsChanged(ListDataEvent arg0) {				
			}

			public void intervalAdded(ListDataEvent arg0) {
				list.setSelectedIndex(arg0.getIndex0());
				list.ensureIndexIsVisible(arg0.getIndex0());
			}

			public void intervalRemoved(ListDataEvent arg0) {
				int index = (arg0.getIndex0() == 0) ? 0 : (arg0.getIndex0() - 1);
				list.setSelectedIndex(index);
				list.ensureIndexIsVisible(index);
			}
			
		});

        list.addListSelectionListener(e -> {
            //if (!e.getValueIsAdjusting()) {
                updateButtons();
            //}

        });
        list.setCellRenderer(new ColortypeListCellRenderer());
		list.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (!list.isSelectionEmpty()) {
					
					int index = list.locationToIndex(arg0.getPoint());
					ListModel dlm = list.getModel();

					list.ensureIndexIsVisible(index);
					
					highlightConstant(index);
					
					if (arg0.getButton() == MouseEvent.BUTTON1 && arg0.getClickCount() == 2) {
					    if(isDisplayingGlobalConstants()) {
                            Constant c = (Constant) dlm.getElementAt(index);
                            showEditConstantDialog(c);
                        } else if(isDisplayingVariables()){
                            Variable v = (Variable) dlm.getElementAt(index);
                            ArrayList<String> messages = new ArrayList<>();
                            if(tab.network().canVariableBeRemoved(v,messages)) {
                                showEditVariableDialog(v);
                            }else{
                                String message = "Variable cannot be edited for the following reasons: \n\n";
                                message += String.join("\n", messages);
                                JOptionPane.showMessageDialog(TAPAALGUI.getApp(), message, "Could not edit variable", JOptionPane.WARNING_MESSAGE);
                            }
                        } else{
                            ColorType ct = (ColorType) dlm.getElementAt(index);
                            if((ct).equals(ColorType.COLORTYPE_DOT)) {
                                JOptionPane.showMessageDialog(null, "Dot color cannot be edited");
                            }else {
                                showEditColorTypeDialog(ct);
                            }
					    }
					}
				}
			}
		});
		
		list.addKeyListener(new KeyAdapter() {
		
			public void keyPressed(KeyEvent arg0) {				
				ListModel model = list.getModel();
				if (model.getSize()>0 && isDisplayingGlobalConstants()) {
					Constant c = (Constant) model.getElementAt(list.getSelectedIndex());
					if (c != null) {
						if (arg0.getKeyCode() == KeyEvent.VK_LEFT) {										
							if (!(c.lowerBound() == c.value())){
								Command edit = tab.network().updateConstant(c.name(), new Constant(c.name(), c.value()-1));
								currentTab.getUndoManager().addNewEdit(edit);
								tab.network().buildConstraints();
							}
						}
						else if (arg0.getKeyCode() == KeyEvent.VK_RIGHT) {
							if (!(c.upperBound() == c.value())){
								Command edit = tab.network().updateConstant(c.name(), new Constant(c.name(), c.value()+1));
								currentTab.getUndoManager().addNewEdit(edit);
								tab.network().buildConstraints();
							}
						} 
						else if (arg0.getKeyCode() == KeyEvent.VK_UP) {
							if(list.getSelectedIndex() > 0){
								highlightConstant(list.getSelectedIndex()-1);
							}
						}
						else if (arg0.getKeyCode() == KeyEvent.VK_DOWN) {
							if(list.getSelectedIndex() < list.getModel().getSize()-1){
								highlightConstant(list.getSelectedIndex()+1);
							}
						}
					}
				}
			}
		});

		addConstantsComponents();
		addConstantsButtons();
		
		list.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
			    removeConstantHighlights();
			}
		});

		setLayout(new BorderLayout());
		this.add(constantsPanel, BorderLayout.CENTER);
		this.add(buttonsPanel, BorderLayout.PAGE_END);

		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(titleBorder),
				BorderFactory.createEmptyBorder(3, 3, 3, 3))
				);
		this.setToolTipText(titleBorderToolTip);
		
		this.addComponentListener(new ComponentListener() {
			final int minimumHegiht = ConstantsPane.this.getMinimumSize().height;
			public void componentShown(ComponentEvent e) {
			}
			
			
			public void componentResized(ComponentEvent e) {
                sortButton.setVisible(ConstantsPane.this.getSize().height > minimumHegiht);
			}
			
			
			public void componentMoved(ComponentEvent e) {
			}
			
			
			public void componentHidden(ComponentEvent e) {
			}
		});

        this.setMinimumSize(new Dimension(this.getMinimumSize().width, this.getMinimumSize().height - sortButton.getMinimumSize().height));
        this.setPreferredSize(new Dimension(this.getMinimumSize().width, this.getMinimumSize().height - sortButton.getMinimumSize().height));
		hideIrrelevantInformation();
        enableButtons(false);
	}

	private void hideIrrelevantInformation(){
	    if(!tab.getLens().isColored()){
	        constantsColorTypesVariablesComboBox.setVisible(false);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(titleBorderNoColor),
                BorderFactory.createEmptyBorder(3, 3, 3, 3))
            );
            this.setToolTipText(titleBorderToolTipNoColor);
            list.setModel(constantsListModel);
            list.setToolTipText(toolTipConstantList);
            addConstantButton.setToolTipText(toolTipNewConstant);
            editBtn.setToolTipText(toolTipEditConstant);
            removeBtn.setToolTipText(toolTipRemoveConstant);
            moveDownButton.setToolTipText(toolTipMoveDownConstant);
            moveUpButton.setToolTipText(toolTipMoveUpConstant);
            sortButton.setToolTipText(toolTipSortConstants);
            constantsColorTypesVariablesComboBox.setSelectedItem(CONSTANTS);

        } else {
	        list.setModel(colorTypesListModel);
            constantsColorTypesVariablesComboBox.setVisible(true);
        }
    }

    // XXX We have to update functions here, one called manually and one called by callback value changed
    //  I think the they might have overlooked the updateButtons method when adding colors. I think the enabled
    //  buttons uses a index before its updated. Therefore, we can't just merge the functions.
    private void updateButtons() {
        int index = list.getSelectedIndex();
        if (index == -1 || list.getSelectedValuesList().isEmpty()) {
            editBtn.setEnabled(false);
            removeBtn.setEnabled(false);
            moveDownButton.setEnabled(false);
        } else {
            if (list.getSelectedValue() != null && list.getSelectedValue().equals(ColorType.COLORTYPE_DOT)) {
                editBtn.setEnabled(false);
                removeBtn.setEnabled(false);
            } else {
                removeBtn.setEnabled(true);
                editBtn.setEnabled(true);
            }
            moveDownButton.setEnabled(index < list.getModel().getSize() - 1 && !list.getSelectedValuesList().isEmpty());
        }
        sortButton.setEnabled(list.getModel().getSize() >= 2);
        moveUpButton.setEnabled(index > 0 && !list.getSelectedValuesList().isEmpty());
    }

    private void enableButtons(boolean afterRemoving) {
        if (list.getSelectedIndex() != -1 && list.getModel().getSize() > 0) {
            if (afterRemoving) {
                if (list.getSelectedIndex() == list.getModel().getSize()){
                    // XXX this is a hack to workarround that remove seemt to not select an element so we just disable all
                    //  fixes issue #1971422. Should fix it properly in the future. -- kyrke 2022-05-08
                    editBtn.setEnabled(false);
                    removeBtn.setEnabled(false);
                    moveDownButton.setEnabled(false);
                    moveUpButton.setEnabled(false);
                } else if (list.getSelectedIndex() + 1 == list.getModel().getSize()) {
                    moveDownButton.setEnabled(false);
                }
            } else {
                editBtn.setEnabled(true);
                removeBtn.setEnabled(true);

                if (list.getSelectedIndex() > 0) {
                    moveUpButton.setEnabled(true);
                }
                if (list.getSelectedIndex() != list.getModel().getSize() - 1) {
                    moveDownButton.setEnabled(true);
                }
            }
            if (list.getSelectedValue() != null && list.getSelectedValue().equals(ColorType.COLORTYPE_DOT)) {
                editBtn.setEnabled(false);
                removeBtn.setEnabled(false);
            }
        } else {
            editBtn.setEnabled(false);
            removeBtn.setEnabled(false);
            moveDownButton.setEnabled(false);
            moveUpButton.setEnabled(false);
        }
    }

	private void highlightConstant(int index){
        //TODO: Implement for colors and variables
        if(isDisplayingColorTypes() || isDisplayingVariables()){
	        return;
        }
		ListModel model = list.getModel();
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
			tab.drawingSurface().repaintAll();
			blinkConstant(c);
		}
	}
	
	public void removeConstantHighlights(){
	    //TODO: Implement for colors and variables
        if(isDisplayingColorTypes() || isDisplayingVariables()){
            return;
        }
		ListModel model = list.getModel();
		for(int i = 0; i < model.getSize(); i++){
			((Constant) model.getElementAt(i)).setFocused(false);
		}
		try{
			tab.drawingSurface().repaintAll();
		}catch(Exception e){
			// It is okay, the tab has just been closed
		}
	}
	
	private void blinkConstant(final Constant c) {
		timer = new Timer(300, new ActionListener() {
			final long startTime = System.currentTimeMillis();
			@Override
			public void actionPerformed(ActionEvent e) {
				if(System.currentTimeMillis() - startTime < 2100) {
					if(!c.getVisible()) {
						c.setVisible(true);
						tab.drawingSurface().repaintAll();
					} else {
						c.setVisible(false);
						tab.drawingSurface().repaintAll();
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

		editBtn.setEnabled(false);
		editBtn.setToolTipText(toolTipEditColorType);

		editBtn.addActionListener(e -> {
            if (isDisplayingVariables()) {
                Variable v = (Variable)  list.getSelectedValue();
                ArrayList<String> messages = new ArrayList<>();
                if (tab.network().canVariableBeRemoved(v,messages)) {
                    showEditVariableDialog(v);
                } else {
                    String message = "Variable cannot be edited for the following reasons: \n\n";
                    message += String.join("\n", messages);
                    JOptionPane.showMessageDialog(TAPAALGUI.getApp(), message, "Could not edit variable", JOptionPane.WARNING_MESSAGE);
                }
            }
            else if (isDisplayingGlobalConstants()) {
                Constant c = (Constant) list.getSelectedValue();
                showEditConstantDialog(c);
            }
            else {
                ColorType ct = (ColorType) list.getSelectedValue();
                if ((ct).equals(ColorType.COLORTYPE_DOT)) {
                    JOptionPane.showMessageDialog(null, "Dot color cannot be edited");
                } else {
                    showEditColorTypeDialog(ct);
                }
            }
		});
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.anchor = GridBagConstraints.WEST;
		buttonsPanel.add(editBtn, gbc);

		removeBtn = new JButton("Remove");

		removeBtn.setEnabled(false);
		removeBtn.setToolTipText(toolTipRemoveColorType);

		removeBtn.addActionListener(e -> {
            if (isDisplayingGlobalConstants()) {
                removeConstants();
            }
            else if (isDisplayingVariables()) {
                removeVariables();
            }
            else {
                removeColorType();
            }
		});
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.WEST;
		buttonsPanel.add(removeBtn, gbc);

		addConstantButton = new JButton("New");
		addConstantButton.setToolTipText(toolTipNewColorType);
		addConstantButton.setEnabled(true);
		addConstantButton.addActionListener(e ->  {
            if (isDisplayingGlobalConstants()) {
                showEditConstantDialog(null);
            }
            else if (isDisplayingVariables()) {
                showEditVariableDialog(null);
            }
            else if (isDisplayingColorTypes()) {
                showEditColorTypeDialog(null);
            }
            enableButtons(false);
        });
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.anchor = GridBagConstraints.WEST;
		buttonsPanel.add(addConstantButton, gbc);

        manuallyEditBtn = new JButton("Manual edit");
        manuallyEditBtn.setToolTipText(toolTipManuallyEdit);

        manuallyEditBtn.addActionListener(e -> {
            showEditManuallyDialog();
        });

        gbc = new GridBagConstraints();
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        buttonsPanel.add(manuallyEditBtn, gbc);
	}

	public void showConstants() {
		TimedArcPetriNetNetwork model = tab.network();
		if (model == null)
			return;

		constantsListModel.updateAll();
		updateButtons();
	}

	private void addConstantsComponents() {

        constantsColorTypesVariablesComboBox.setToolTipText(toolTipComboBox);
        constantsColorTypesVariablesComboBox.addActionListener(e -> {
            JComboBox<String> source = (JComboBox) e.getSource();
            String selectedItem = Objects.requireNonNull(source.getSelectedItem()).toString();
            switch (selectedItem) {
                case CONSTANTS:
                    list.setModel(constantsListModel);
                    list.setToolTipText(toolTipConstantList);
                    addConstantButton.setToolTipText(toolTipNewConstant);
                    editBtn.setToolTipText(toolTipEditConstant);
                    removeBtn.setToolTipText(toolTipRemoveConstant);
                    moveDownButton.setToolTipText(toolTipMoveDownConstant);
                    moveUpButton.setToolTipText(toolTipMoveUpConstant);
                    sortButton.setToolTipText(toolTipSortConstants);
                    break;
                case COLORTYPES:
                    list.setModel(colorTypesListModel);
                    list.setToolTipText(toolTipColorTypeList);
                    addConstantButton.setToolTipText(toolTipNewColorType);
                    editBtn.setToolTipText(toolTipEditColorType);
                    removeBtn.setToolTipText(toolTipRemoveColorType);
                    moveDownButton.setToolTipText(toolTipMoveDownColorType);
                    moveUpButton.setToolTipText(toolTipMoveUpColorType);
                    sortButton.setToolTipText(toolTipSortColorTypes);
                    break;
                case VARIABLES:
                    list.setModel(variablesListModel);
                    list.setToolTipText(toolTipVariableList);
                    addConstantButton.setToolTipText(toolTipNewVariable);
                    editBtn.setToolTipText(toolTipEditVariable);
                    removeBtn.setToolTipText(toolTipRemoveVariable);
                    moveDownButton.setToolTipText(toolTipMoveDownVariable);
                    moveUpButton.setToolTipText(toolTipMoveUpVariable);
                    sortButton.setToolTipText(toolTipSortVariables);
                    break;
            }
            if(list.getModel().getSize() > 0) {
                list.setSelectedIndex(0);
                list.ensureIndexIsVisible(0);
            }
            enableButtons(false);
        });
        // GBC for the scroller
        GridBagConstraints gbc = new GridBagConstraints();

        constantsColorTypesVariablesComboBox.setBackground(Color.white);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        constantsPanel.add(constantsColorTypesVariablesComboBox, gbc);

        JScrollPane constantsScroller = new JScrollPane(list);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridheight = 3;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        constantsPanel.add(constantsScroller, gbc);

        moveUpButton = new JButton(new ImageIcon(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("Images/Up.png"))));
        moveUpButton.setMargin(new Insets(2,2,2,2));
        moveUpButton.setEnabled(false);
        moveUpButton.setToolTipText(toolTipMoveUpColorType);
        moveUpButton.addActionListener(e -> {
            int index = list.getSelectedIndex();
            if (isDisplayingVariables() && index > 0) {
                tab.network().swapVariables(index, index-1);
                variablesListModel.updateName();
                list.setSelectedIndex(index-1);}
            else if(isDisplayingGlobalConstants() && index > 0) {
                tab.swapConstants(index, index-1);
                showConstants();
                list.setSelectedIndex(index-1);
            }
            else if (isDisplayingColorTypes() && index > 0) {
                tab.network().swapColorTypes(index, index-1);
                colorTypesListModel.updateName();
                list.setSelectedIndex(index-1);
            }
        });

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTH;
        constantsPanel.add(moveUpButton,gbc);

        moveDownButton = new JButton(new ImageIcon(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("Images/Down.png"))));
        moveDownButton.setMargin(new Insets(2,2,2,2));
        moveDownButton.setEnabled(false);
        moveDownButton.setToolTipText(toolTipMoveDownColorType);
        moveDownButton.addActionListener(e -> {
            int index = list.getSelectedIndex();
            if (isDisplayingVariables() && index < tab.network().numberOfVariables() - 1) {
                tab.network().swapVariables(index, index + 1);
                variablesListModel.updateName();
                list.setSelectedIndex(index + 1);
            }
            else if(isDisplayingGlobalConstants() && index < tab.network().constants().size() - 1) {
                tab.swapConstants(index, index+1);
                showConstants();
                list.setSelectedIndex(index + 1);
            }
            else if (isDisplayingColorTypes() && index < tab.network().numberOfColorTypes() - 1) {
                tab.network().swapColorTypes(index, index+1);
                colorTypesListModel.updateName();
                list.setSelectedIndex(index + 1);
            }
        });

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.NORTH;
        constantsPanel.add(moveDownButton,gbc);

        //Sort button
        sortButton = new JButton(new ImageIcon(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("Images/Sort.png"))));
        sortButton.setMargin(new Insets(2,2,2,2));
        sortButton.setToolTipText(toolTipSortColorTypes);
        sortButton.setEnabled(false);
        sortButton.addActionListener(e -> {
            if (isDisplayingGlobalConstants()) {
                Command sortConstantsCommand = new SortConstantsCommand(tab, ConstantsPane.this);
                tab.getUndoManager().addNewEdit(sortConstantsCommand);
                sortConstantsCommand.redo();
            }
            else if (isDisplayingVariables()) {
                variablesListModel.sort();
            }
            else {
                colorTypesListModel.sort();
            }
        });

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        //gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        constantsPanel.add(sortButton,gbc);

	}

	private void showEditConstantDialog(Constant constant) {
		ConstantsDialogPanel panel = new ConstantsDialogPanel(tab.network(), constant);

		panel.showDialog();
		showConstants();
	}
    private void showEditVariableDialog(Variable variable) {
        VariablesDialogPanel panel = null;
        if (variable != null) {
            try {
                panel = new VariablesDialogPanel(new JRootPane(), variablesListModel, tab.network(), variable, tab.getUndoManager());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                panel = new VariablesDialogPanel(new JRootPane(), variablesListModel, tab.network(), tab.getUndoManager());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        panel.showDialog();
        variablesListModel.updateName();

    }
    private void showEditColorTypeDialog(ColorType colorType) {
        ColorTypeDialogPanel panel;
        UndoManager undoManager = tab.getUndoManager();
        if (colorType != null) {
            panel = new ColorTypeDialogPanel(colorTypesListModel, tab.network(), colorType, undoManager);
        } else {
            panel = new ColorTypeDialogPanel(colorTypesListModel, tab.network(), undoManager);
        }
        panel.showDialog();
    }

    private void showEditManuallyDialog() {
        UndoManager undoManager = tab.getUndoManager();
        ManuallyEditDialogPanel panel = new ManuallyEditDialogPanel(colorTypesListModel, variablesListModel,constantsListModel, tab.network(), undoManager);
        panel.showDialog();
    }

	protected void removeConstants() {
        TimedArcPetriNetNetwork model = tab.network();
        java.util.List<String> unremovableConstants = new ArrayList<>();
        tab.getUndoManager().newEdit();

        for (Object o : list.getSelectedValuesList()) {
            String name = ((Constant)o).name();
            Command command = model.removeConstant(name);
            if (command == null) {
                unremovableConstants.add(name);
            } else {
                tab.getUndoManager().addEdit(command);
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

            JOptionPane.showMessageDialog(TAPAALGUI.getApp(), message.toString(),
                "Constant in use", JOptionPane.ERROR_MESSAGE);
        }
	}

	private void removeVariables() {
        for (Object variable : list.getSelectedValuesList()) {
            if (variable instanceof Variable) {
                variablesListModel.removeElement((Variable) variable);
            }
        }
    }

    private void removeColorType() {
        for (Object colorType : list.getSelectedValuesList()) {
            if (colorType instanceof ColorType) {
                if (colorType.equals(ColorType.COLORTYPE_DOT)) {
                    JOptionPane.showMessageDialog(null, "Dot color cannot be removed");
                } else {
                    colorTypesListModel.removeElement((ColorType) colorType);
                }
            }
        }
    }

	public void setNetwork(TimedArcPetriNetNetwork tapnNetwork) {
		constantsListModel.setNetwork(tapnNetwork);
	}

	public void selectFirst() {
		list.setSelectedIndex(0);

	}

    protected boolean isDisplayingGlobalConstants() {
        return constantsColorTypesVariablesComboBox.getSelectedItem().equals(CONSTANTS);
    }

    protected boolean isDisplayingColorTypes() {
        return constantsColorTypesVariablesComboBox.getSelectedItem().equals(COLORTYPES);
    }

    protected boolean isDisplayingVariables() {
        return constantsColorTypesVariablesComboBox.getSelectedItem().equals(VARIABLES);
    }

    @Override
    public void moveUp(int index) {
        tab.swapConstants(index, index-1);
        showConstants();
    }

    @Override
    public void moveDown(int index) {
        tab.swapConstants(index, index+1);
        showConstants();
    }

    @Override
    public JList<?> getJList() {
        return list;
    }

    public class VariablesListModel extends AbstractListModel {
        private static final long serialVersionUID = 1L;
        private TimedArcPetriNetNetwork network;

        public VariablesListModel(TimedArcPetriNetNetwork network) {
            this.network = network;

            addListDataListener(new ListDataListener() {
                public void intervalAdded(ListDataEvent arg0) {
                    list.setSelectedIndex(arg0.getIndex0());
                    list.ensureIndexIsVisible(arg0.getIndex0());
                }

                public void intervalRemoved(ListDataEvent arg0) {
                    int index = (arg0.getIndex0() == 0) ? 0 : (arg0.getIndex0() - 1);
                    list.setSelectedIndex(index);
                    list.ensureIndexIsVisible(index);
                }

                public void contentsChanged(ListDataEvent e) {
                }
            });
        }

        public Object getElementAt(int index) {
            return network.getVariableByIndex(index);
        }

        public Variable[] sort() {
            Variable[] oldOrder = network.sortVariables();
            fireContentsChanged(this, 0, getSize());
            return oldOrder;
        }

        public void undoSort(Variable[] oldOrder) {
            network.undoSort(oldOrder);
            fireContentsChanged(this, 0, getSize());
        }

        public int getSize() {
            return network.numberOfVariables();
        }

        public void addElement(Variable variable) {
            network.add(variable);
            fireIntervalAdded(this, network.numberOfVariables()-1, network.numberOfVariables());
        }

        public void removeElement(Variable variable) {
            UndoManager undoManager = tab.getUndoManager();
            undoManager.newEdit();
            ArrayList<String> messages = new ArrayList<>();
            network.remove(variable, variablesListModel, undoManager, messages);
            if(messages.isEmpty()){
                //Since we just removed our selection everything is false
                enableButtons(true);
                updateName();
            } else{
                String message = "Variable could not be removed for the following reasons: \n\n";
                message += String.join("\n", messages);
                JOptionPane.showMessageDialog(TAPAALGUI.getApp(), message, "Could not remove variable", JOptionPane.WARNING_MESSAGE);
            }
        }

        public void updateName() {
            fireContentsChanged(this, 0, getSize());
            for (Template activeTemplate : tab.activeTemplates()) {
                activeTemplate.guiModel().repaintAll(true);
            }
        }

        public void setNetwork(TimedArcPetriNetNetwork network) {
            Require.that(network != null, "network cannot be null");
            this.network = network;
            fireContentsChanged(this, 0, network.numberOfVariables());
        }
    }
    public class ColorTypesListModel extends AbstractListModel {
        private static final long serialVersionUID = 1L;
        private TimedArcPetriNetNetwork network;

        public ColorTypesListModel(TimedArcPetriNetNetwork network) {
            Require.that(network != null, "network must not be null");

            addListDataListener(new ListDataListener() {
                public void intervalAdded(ListDataEvent arg0) {
                    list.setSelectedIndex(arg0.getIndex0());
                    list.ensureIndexIsVisible(arg0.getIndex0());
                }

                public void intervalRemoved(ListDataEvent arg0) {
                    int index = (arg0.getIndex0() == 0) ? 0 : (arg0.getIndex0() - 1);
                    list.setSelectedIndex(index);
                    list.ensureIndexIsVisible(index);
                }

                public void contentsChanged(ListDataEvent e) {
                }
            });
            setNetwork(network);
        }

        public ColorType[] sort() {
            ColorType[] oldOrder = network.sortColorTypes();
            fireContentsChanged(this, 0, getSize());
            return oldOrder;
        }

        public int getSize() {
            return network.numberOfColorTypes();
        }

        public Object getElementAt(int i) {
            return network.getColorTypeByIndex(i);
        }

        public void addElement(ColorType colorType) {
            network.add(colorType);
            fireIntervalAdded(this, network.numberOfColorTypes()-1, network.numberOfColorTypes());
        }

        @Override
        public void fireContentsChanged(Object source, int index0, int index1) {
            super.fireContentsChanged(source, index0, index1);
            if (editBtn != null) updateButtons();
        }

        public void removeElement(ColorType colorType) {
            UndoManager undoManager = tab.getUndoManager();
            undoManager.newEdit();
            ArrayList<String> messages = new ArrayList<>();
            network.remove(colorType, colorTypesListModel, undoManager, messages);
            if(messages.isEmpty()){
                //Since we just removed our selection everything is false
                enableButtons(true);
                updateName();
            } else{
                String message = "Color type could not be removed for the following reasons: \n\n";
                message += String.join("", messages);
                JOptionPane.showMessageDialog(TAPAALGUI.getApp(), message, "Could not remove color type", JOptionPane.WARNING_MESSAGE);
            }

        }

        public void updateName() {
            fireContentsChanged(this, 0, getSize());
            for (Template activeTemplate : tab.activeTemplates()) {
                activeTemplate.guiModel().repaintAll(true);
            }
        }

        public void setNetwork(TimedArcPetriNetNetwork network) {
            Require.that(network != null, "network cannot be null");
            this.network = network;
            fireContentsChanged(this, 0, network.numberOfColorTypes());
        }
    }
}
