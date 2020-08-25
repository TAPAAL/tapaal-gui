package pipe.gui.widgets;

import dk.aau.cs.gui.Context;
import dk.aau.cs.model.tapn.*;
import net.tapaal.swinghelpers.CustomJSpinner;
import net.tapaal.swinghelpers.GridBagHelper;
import net.tapaal.swinghelpers.WidthAdjustingComboBox;
import pipe.dataLayer.Template;
import pipe.gui.CreateGui;
import pipe.gui.graphicElements.tapn.TimedPlaceComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.Arrays;
import java.util.Set;

import static net.tapaal.swinghelpers.GridBagHelper.Anchor.WEST;
import static net.tapaal.swinghelpers.GridBagHelper.Fill.HORIZONTAL;

public abstract class InvariantEditorPanel extends JPanel{
    JPanel invariantGroup;
    Context context;
    TimedPlaceComponent place;
    JComboBox<String> invRelationConstant;
    JComboBox<String> invRelationNormal;
    int maxNumberOfPlacesToShowAtOnce = 20;
    JButton okButton;
    JCheckBox invariantInf;
    WidthAdjustingComboBox<Object> invConstantsComboBox;
    JRadioButton normalInvRadioButton;
    JRadioButton constantInvRadioButton;
    CustomJSpinner invariantSpinner;
    public InvariantEditorPanel(Context context, TimedPlaceComponent place){
        this.context = context;
        this.place = place;
        this.setLayout(new java.awt.GridBagLayout());
        this.setBorder(javax.swing.BorderFactory.createTitledBorder("Age Invariant"));
        initPanel();
    }

    public void initPanel(){
        invariantGroup = new JPanel(new GridBagLayout());
        invRelationNormal = new JComboBox<>(new String[]{"<=", "<"});
        invRelationConstant = new JComboBox<>(new String[]{"<=", "<"});
        //invariantSpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));

        invariantSpinner = new CustomJSpinner(0, okButton);
        invariantSpinner.addChangeListener(e -> {
            if(!invariantInf.isSelected()){
                if ((Integer) invariantSpinner.getValue() < 1) {
                    invRelationNormal.setModel(new DefaultComboBoxModel<>(new String[] { "<=" }));
                    invRelationNormal.setSelectedItem("<=");
                } else if (invRelationNormal.getModel().getSize() == 1) {
                    invRelationNormal.setModel(new DefaultComboBoxModel<>(new String[] { "<=", "<" }));
                }
            }
        });

        GridBagConstraints gbc = GridBagHelper.as(1,0, HORIZONTAL, new Insets(3, 3, 3, 3));
        invariantGroup.add(invRelationNormal, gbc);

        gbc = GridBagHelper.as(1,1, HORIZONTAL, new Insets(3, 3, 3, 3));
        invariantGroup.add(invRelationConstant, gbc);

        gbc = GridBagHelper.as(2,0, new Insets(3, 3, 3, 3));
        invariantGroup.add(invariantSpinner, gbc);

        invariantInf = new JCheckBox("inf");
        invariantInf.addActionListener(arg0 -> {
            if(!isUrgencyOK()){
                invariantInf.setSelected(true);
                return;
            }
            if (!invariantInf.isSelected()) {
                invRelationNormal.setEnabled(true);
                invariantSpinner.setEnabled(true);
                invRelationNormal.setSelectedItem("<=");
                if ((Integer) invariantSpinner.getValue() < 1) {
                    invRelationNormal.setModel(new DefaultComboBoxModel<>(new String[] { "<=" }));
                } else {
                    invRelationNormal.setModel(new DefaultComboBoxModel<>(new String[] { "<=", "<" }));
                }
            } else {
                invRelationNormal.setEnabled(false);
                invariantSpinner.setEnabled(false);
                invRelationNormal.setSelectedItem("<");
                invRelationNormal.setModel(new DefaultComboBoxModel<>(new String[] { "<" }));
            }

        });
        gbc = GridBagHelper.as(3,0);
        invariantGroup.add(invariantInf, gbc);

        Set<String> constants = context.network().getConstantNames();
        String[] constantArray = constants.toArray(new String[constants.size()]);
        Arrays.sort(constantArray, String.CASE_INSENSITIVE_ORDER);


        invConstantsComboBox = new WidthAdjustingComboBox<>(maxNumberOfPlacesToShowAtOnce);
        invConstantsComboBox.setModel(new DefaultComboBoxModel<>(constantArray));
        //	invConstantsComboBox = new JComboBox(constants.toArray());
        invConstantsComboBox.setMaximumRowCount(20);
        //	invConstantsComboBox.setMinimumSize(new Dimension(100, 30));
        invConstantsComboBox.setPreferredSize(new Dimension(230, 30));
        invConstantsComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                setRelationModelForConstants();
            }
        });

        gbc = GridBagHelper.as(2,1, WEST);
        invariantGroup.add(invConstantsComboBox, gbc);

        normalInvRadioButton = new JRadioButton("Normal");
        normalInvRadioButton.addActionListener(e -> {
            disableInvariantComponents();
            enableNormalInvariantComponents();
        });

        constantInvRadioButton = new JRadioButton("Constant");
        constantInvRadioButton.addActionListener(e -> {
            disableInvariantComponents();
            enableConstantInvariantComponents();
        });
        if (constants.isEmpty()){
            constantInvRadioButton.setEnabled(false);
        }
        ButtonGroup btnGroup = new ButtonGroup();
        btnGroup.add(normalInvRadioButton);
        btnGroup.add(constantInvRadioButton);

        gbc = GridBagHelper.as(0,0, WEST);
        invariantGroup.add(normalInvRadioButton, gbc);

        gbc = GridBagHelper.as(0,1, WEST);
        invariantGroup.add(constantInvRadioButton, gbc);

        TimeInvariant invariantToSet = place.getInvariant();

        if (invariantToSet.isUpperNonstrict()) {
            invRelationNormal.setSelectedItem("<=");
        } else {
            invRelationNormal.setSelectedItem("<");
        }

        if (invariantToSet.upperBound() instanceof Bound.InfBound) {
            invariantSpinner.setEnabled(false);
            invRelationNormal.setModel(new DefaultComboBoxModel<>(new String[] { "<" }));
            invariantInf.setSelected(true);
            invRelationNormal.setSelectedItem("<");
        }

        disableInvariantComponents();
        if (invariantToSet.upperBound() instanceof ConstantBound) {
            enableConstantInvariantComponents();
            constantInvRadioButton.setSelected(true);
            invConstantsComboBox.setSelectedItem(((ConstantBound) invariantToSet.upperBound()).name());
            invRelationConstant.setSelectedItem(invariantToSet.isUpperNonstrict() ? "<=" : "<");
        } else {
            enableNormalInvariantComponents();
            normalInvRadioButton.setSelected(true);
            if (invariantToSet.upperBound() instanceof IntBound) {
                if ((Integer) invariantSpinner.getValue() < 1) {
                    invRelationNormal.setModel(new DefaultComboBoxModel<>(new String[] { "<=" }));
                } else {
                    invRelationNormal.setModel(new DefaultComboBoxModel<>(new String[] { "<=", "<" }));
                }
                invariantSpinner.setValue(invariantToSet.upperBound().value());
                invariantSpinner.setEnabled(true);
                invRelationNormal.setSelectedItem(invariantToSet.isUpperNonstrict() ? "<=" : "<");
                invariantInf.setSelected(false);
            }
        }

        GridBagConstraints gridBagConstraints = GridBagHelper.as(1,4,2, WEST, new Insets(3, 3, 3, 3));
        this.add(invariantGroup, gridBagConstraints);
    }
    public abstract void placeHolder();
    private boolean isUrgencyOK(){
        for(TransportArc arc : CreateGui.getCurrentTab().currentTemplate().model().transportArcs()){
            if(arc.destination().equals(place.underlyingPlace()) && arc.transition().isUrgent()){
                JOptionPane.showMessageDialog(this.getRootPane(), "Transport arcs going through urgent transitions cannot have an invariant at the destination.", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        if(place.underlyingPlace().isShared()){
            for(Template t : CreateGui.getCurrentTab().allTemplates()){
                for(TransportArc arc : t.model().transportArcs()){
                    if(arc.destination().equals(place.underlyingPlace()) && arc.transition().isUrgent()){
                        JOptionPane.showMessageDialog(this.getRootPane(), "Transport arcs going through urgent transitions cannot have an invariant at the destination.", "Error", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                }
            }
        }
        return true;
    }
    protected void enableConstantInvariantComponents() {
        invRelationConstant.setEnabled(true);
        invConstantsComboBox.setEnabled(true);
        setRelationModelForConstants();
    }

    protected void enableNormalInvariantComponents() {
        invRelationNormal.setEnabled(true);
        invariantInf.setEnabled(true);
        invariantSpinner.setValue(0);
        invariantInf.setSelected(true);
        invRelationNormal.setModel(new DefaultComboBoxModel<>(new String[] { "<" }));
    }

    protected void disableInvariantComponents() {
        invRelationNormal.setEnabled(false);
        invRelationConstant.setEnabled(false);
        invariantSpinner.setEnabled(false);
        invConstantsComboBox.setEnabled(false);
        invariantInf.setEnabled(false);
    }
    private void setRelationModelForConstants() {
        int value = context.network().getConstantValue(invConstantsComboBox.getSelectedItem().toString());

        String selected = invRelationConstant.getSelectedItem().toString();
        if (value == 0) {
            invRelationConstant.setModel(new DefaultComboBoxModel<>(new String[] { "<=" }));
        } else {
            invRelationConstant.setModel(new DefaultComboBoxModel<>(new String[] { "<=", "<" }));
        }
        invRelationConstant.setSelectedItem(selected);
    }

    public void removeBorder(){
        this.setBorder(null);
    }
}
