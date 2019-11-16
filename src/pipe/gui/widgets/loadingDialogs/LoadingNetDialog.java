package pipe.gui.widgets.loadingDialogs;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import pipe.gui.CreateGui;

public class LoadingNetDialog extends JDialog{
	private static final long serialVersionUID = -8867570052239287389L;
	
	public LoadingNetDialog(JFrame frame, String title, boolean modal) {
		super(frame,title,modal);
		setTitle(title);
		setLayout(new FlowLayout());
		initLoadingDialogComponents();
		setType(Window.Type.POPUP);
		pack();
		setSize(400, 300);
		setLocationRelativeTo(frame);
	}
	
	private void initLoadingDialogComponents() {
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());
		ImageIcon loadingGIF = new ImageIcon(CreateGui.imgPath + "ajax-loader.gif");
		
		
		JLabel workingLabel = new JLabel("<html><div style='text-align: center;'>Currently loading the net...</div></html>", SwingConstants.CENTER);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.anchor = GridBagConstraints.NORTH;
		mainPanel.add(workingLabel, gbc);
		
		JLabel statusLabel = new JLabel("Working... ", loadingGIF, JLabel.CENTER);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.anchor = GridBagConstraints.NORTH;
		mainPanel.add(statusLabel, gbc);
		
		setContentPane(mainPanel);
	}
}
