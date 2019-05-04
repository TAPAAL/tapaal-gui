package pipe.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import dk.aau.cs.model.tapn.TimedArcPetriNet;

public class SmartDrawDialog extends JDialog {
	private static final long serialVersionUID = 6116530047981607501L;
	
	JPanel mainPanel;
	TimedArcPetriNet currentTapn = CreateGui.getDrawingSurface().getModel();
	double xSpacing;
	double ySpacing;
	
	static SmartDrawDialog smartDrawDialog;
	public static void showSmartDrawDialog() {
		if(smartDrawDialog == null){
			smartDrawDialog = new SmartDrawDialog(CreateGui.getApp(), "Smart Draw", true);
			smartDrawDialog.pack();
			smartDrawDialog.setPreferredSize(smartDrawDialog.getSize());
			smartDrawDialog.setMinimumSize(new Dimension(smartDrawDialog.getWidth(), smartDrawDialog.getHeight()));
			smartDrawDialog.setLocationRelativeTo(null);
			smartDrawDialog.setResizable(true);
		}
		smartDrawDialog.setVisible(true);
	}

	private SmartDrawDialog(Frame frame, String title, boolean modal) {
		super(frame, title, modal);
		initComponents();
	}
	
	private void initComponents() {
		setLayout(new FlowLayout());
		mainPanel = new JPanel(new GridBagLayout());
		
		JButton draw = new JButton("Smart Draw");
		draw.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				draw();
			}
		});
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		mainPanel.add(draw, gbc);
		
		setContentPane(mainPanel);
	}
	
	
	public void draw() {
		System.out.println("Draw");
	}
}
