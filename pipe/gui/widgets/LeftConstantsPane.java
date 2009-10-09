package pipe.gui.widgets;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import pipe.dataLayer.Constant;
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
		
		JButton addConstantButton = new JButton("Add Constant..");
		addConstantButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				EscapableDialog guiDialog = 
					new EscapableDialog(CreateGui.getApp(), Pipe.TOOL + " " + Pipe.VERSION, true);

				Container contentPane = guiDialog.getContentPane();

				// 1 Set layout
				contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));      

				// 2 Add Place editor
				contentPane.add( new ConstantsDialogPanel(guiDialog.getRootPane(), new Constant()) );

				guiDialog.setResizable(false);     

				// Make window fit contents' preferred size
				guiDialog.pack();

				// Move window to the middle of the screen
				guiDialog.setLocationRelativeTo(null);
				guiDialog.setVisible(true);
			}
		});
		addConstantButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		addConstantPanel.add(addConstantButton);
		constantsScroller = new JScrollPane(constantsPanel);
		splitPane = new JSplitPaneFix(JSplitPane.VERTICAL_SPLIT, constantsScroller, addConstantPanel);
		((JScrollPane)splitPane.getTopComponent()).setViewportView(constantsPanel);
		
		splitPane.setContinuousLayout(true);
		splitPane.setDividerSize(0);
		splitPane.setDividerLocation(0.9);
		splitPane.setResizeWeight(1.0);
		this.add(splitPane);
	}
	
}
