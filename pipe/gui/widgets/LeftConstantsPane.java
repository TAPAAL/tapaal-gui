package pipe.gui.widgets;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import pipe.dataLayer.Constant;
import pipe.dataLayer.DataLayer;
import pipe.gui.CreateGui;
import pipe.gui.Pipe;

public class LeftConstantsPane extends JPanel {
	private JSplitPane splitPane;
	private JPanel constantsPanel;
	private JScrollPane constantsScroller;
	private JPanel addConstantPanel;
		
	public LeftConstantsPane(){
		constantsPanel = new JPanel();
		addConstantPanel = new JPanel(new BorderLayout());
		
		constantsPanel.setLayout(new BoxLayout(constantsPanel, BoxLayout.Y_AXIS));		
		
		JButton addConstantButton = new JButton("Add Constant..");
		addConstantButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				showEditConstantDialog("", 0);
			}
		});
		addConstantButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		addConstantPanel.add(addConstantButton);
		constantsScroller = new JScrollPane(constantsPanel);
		splitPane = new JSplitPaneFix(JSplitPane.VERTICAL_SPLIT, constantsScroller, addConstantPanel);
		((JScrollPane)splitPane.getTopComponent()).setViewportView(constantsPanel);
		setLayout(new BorderLayout());
				
		splitPane.setContinuousLayout(true);
		splitPane.setDividerSize(0);
		splitPane.setDividerLocation(0.9);
		splitPane.setResizeWeight(1.0);
		this.add(splitPane);
		
		showConstants();
	}
	
	private void showConstants()
	{
		DataLayer model = CreateGui.getModel();
		if(model == null) return;
		
		constantsPanel.removeAll();
		for(Constant constant : model.getConstants())
		{
			JPanel panel = createConstantPanel(constant);
			constantsPanel.add(panel);
		}
		constantsPanel.validate();
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
	
	public JPanel createConstantPanel(final Constant constant)
	{
		JPanel panel = new JPanel(new FlowLayout());
		
		JLabel constantLabel = new JLabel(constant.toString());
		JButton editButton = new JButton("Edit");
		editButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				showEditConstantDialog(constant.getName(), constant.getValue());
			}
		});
		
		JButton removeButton = new JButton("Remove");
		removeButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				removeConstant(constant.getName());
			}
		});
		
		panel.add(constantLabel);
		panel.add(editButton);
		panel.add(removeButton);
		
		return panel;
	}

	protected void removeConstant(String name) {
		DataLayer model = CreateGui.getModel();
		model.removeConstant(name);
		showConstants();
	}
	
}
