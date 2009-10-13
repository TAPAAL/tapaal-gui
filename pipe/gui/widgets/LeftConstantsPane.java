package pipe.gui.widgets;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import pipe.dataLayer.Constant;
import pipe.dataLayer.DataLayer;
import pipe.gui.CreateGui;
import pipe.gui.Pipe;

public class LeftConstantsPane extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7883351020889779067L;
	private JSplitPane splitPane;
	private JPanel constantsPanel;
	private JScrollPane constantsScroller;
	private JPanel addConstantPanel;
	private JLabel constantsLabel;
	
	private JList constantsList;
	private DefaultListModel listModel;
	private JButton editBtn;
	private JButton removeBtn;
	
		
	public LeftConstantsPane(){
		constantsPanel = new JPanel(new BorderLayout());
		addConstantPanel = new JPanel();
		
		listModel = new DefaultListModel();
		constantsList = new JList(listModel);
		constantsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		constantsList.setVisibleRowCount(-1);
		constantsList.setLayoutOrientation(JList.VERTICAL);
		constantsList.setAlignmentX(Component.LEFT_ALIGNMENT);
		constantsList.setAlignmentY(Component.TOP_ALIGNMENT);
		constantsList.addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent e){
				if (e.getValueIsAdjusting() == false) {
					if (constantsList.getSelectedIndex() == -1) {
						editBtn.setEnabled(false);
						removeBtn.setEnabled(false);

					} else {
						removeBtn.setEnabled(true);
						editBtn.setEnabled(true);
					}
				}
			}
		});
		
		addConstantsComponents();
		addConstantsButtons();
		
		splitPane = new JSplitPaneFix(JSplitPane.VERTICAL_SPLIT, constantsPanel, addConstantPanel);
		setLayout(new BorderLayout());
				
		splitPane.setContinuousLayout(true);
		splitPane.setDividerSize(0);
		splitPane.setDividerLocation(0.9);
		splitPane.setResizeWeight(1.0);
		this.add(splitPane);
		
		showConstants();
	}

	private void addConstantsButtons() {
		editBtn = new JButton("Edit");
		editBtn.setEnabled(false);
		editBtn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				Constant c = (Constant)constantsList.getSelectedValue();
				showEditConstantDialog(c.getName(), c.getValue());
			}
		});
		addConstantPanel.add(editBtn);
		
		removeBtn = new JButton("Remove");
		removeBtn.setEnabled(false);
		removeBtn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				String constName = ((Constant)constantsList.getSelectedValue()).getName();
				removeConstant(constName);
			}
		});
		addConstantPanel.add(removeBtn);
		
		JButton addConstantButton = new JButton("Add Constant..");
		addConstantButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				showEditConstantDialog("", 0);
			}
		});
		addConstantPanel.add(addConstantButton);
	}
	
	private void showConstants()
	{
		DataLayer model = CreateGui.getModel();
		if(model == null) return;
		
		listModel.removeAllElements();
		addConstantsToPanel(model);
		constantsList.validate();

	}

	private void addConstantsToPanel(DataLayer model) {
		for(Constant constant : model.getConstants())
		{
			listModel.addElement(constant);
		}
	}

	private void addConstantsComponents()
	{
		addConstantsLabel();
		constantsScroller = new JScrollPane(constantsList);
		constantsPanel.add(constantsScroller, BorderLayout.CENTER);
	}
	
	private void addConstantsLabel() {
		constantsLabel = new JLabel("Constants:");
		constantsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		constantsLabel.setAlignmentY(Component.TOP_ALIGNMENT);
		constantsPanel.add(constantsLabel, BorderLayout.PAGE_START);
	}
	
	private void showEditConstantDialog(String name, int value) {
		EscapableDialog guiDialog = 
			new EscapableDialog(CreateGui.getApp(), Pipe.TOOL + " " + Pipe.VERSION, true);

		Container contentPane = guiDialog.getContentPane();

		// 1 Set layout
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));      

		// 2 Add Place editor
		contentPane.add( new ConstantsDialogPanel(guiDialog.getRootPane(), CreateGui.getModel(), name, value) );

		guiDialog.setResizable(false);     

		// Make window fit contents' preferred size
		guiDialog.pack();

		// Move window to the middle of the screen
		guiDialog.setLocationRelativeTo(null);
		guiDialog.setVisible(true);
		
		showConstants();
	}
	
	protected void removeConstant(String name) {
		DataLayer model = CreateGui.getModel();
		model.removeConstant(name);
		showConstants();
	}
	
}
