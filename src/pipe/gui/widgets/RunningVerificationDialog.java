/**
 * 
 */
package pipe.gui.widgets;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.SwingWorker.StateValue;

import dk.aau.cs.util.MemoryMonitor;

public class RunningVerificationDialog extends JDialog {
	private static final long serialVersionUID = -1943743974346875737L;
	private JButton okButton;
	private long startTimeMs = 0;
	JLabel timerLabel;
	JLabel headLineLabel;
	JLabel progressLabel;
	JLabel resourcesLabel;
	JLabel usageLabel;
	private Timer timer; 
	
	private int memoryTimerCount = 0;
	private int memoryTimerMode = 0;
	private int peakMemory = -1;
	
	private void startMemoryTimer(){
		if(memoryTimer.isRunning()){
			memoryTimer.stop();
		}
		peakMemory = -1;
		memoryTimer.setDelay(50);
		memoryTimerCount = 0;
		memoryTimerMode = 0;
		memoryTimer.start();
	}
	
	private void stopMemoryTimer(){
		if(memoryTimer.isRunning()){
			memoryTimer.stop();
		}
	}
	
	private Timer memoryTimer;
	
	public RunningVerificationDialog(JFrame owner) {
		super(owner, "Verification in Progress", true);		
		initComponents();			
		pack();
	}
	
	public void initComponents() {		
		setLocationRelativeTo(null);
		setLayout(new GridBagLayout());
		
		timer = new Timer(1000, new AbstractAction() {
			private static final long serialVersionUID = 1327695063762640628L;

			public void actionPerformed(ActionEvent e) {
				timerLabel.setText((System.currentTimeMillis() - startTimeMs)
						/1000 + " s");
				usageLabel.setText(peakMemory >= 0? peakMemory + " MB" : "N/A");
			}
		});	
		
		memoryTimer = new Timer(50, new AbstractAction() {
			private static final long serialVersionUID = 1327695063762640628L;

			public void actionPerformed(ActionEvent e) {
				if(MemoryMonitor.isAttached()){
					MemoryMonitor.getUsage();
					peakMemory = MemoryMonitor.getPeakMemoryValue();
					
					if(memoryTimerMode == 0 && memoryTimerCount == 2){
						memoryTimerCount = 0;
						memoryTimerMode++;
						memoryTimer.setDelay(100);
					}else if(memoryTimerMode == 1 && memoryTimerCount == 4){
						memoryTimerCount = 0;
						memoryTimerMode++;
						memoryTimer.setDelay(200);
					}else if(memoryTimerMode == 2 && memoryTimerCount == 5){
						memoryTimerCount = 0;
						memoryTimerMode++;
						memoryTimer.setDelay(1000);
					}else if(memoryTimerMode < 3){
						memoryTimerCount++;
					}
				}
			}
		});	
		
		okButton = new JButton("Interrupt Verification");
		headLineLabel = new JLabel();
		headLineLabel.setText("Verification is running, please wait ...  ");
		progressLabel = new JLabel();
		progressLabel.setText("Elapsed time: ");
		resourcesLabel = new JLabel();
		resourcesLabel.setText("Memory usage: ");
		timerLabel = new JLabel();
		timerLabel.setText("0 s");
		usageLabel = new JLabel();
		usageLabel.setText("initializing...");
		
		Container content = getContentPane();
		content.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
		Insets insets = new Insets(5, 5, 5, 5);
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = insets; 
		content.add(headLineLabel,gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = insets;
		content.add(progressLabel,gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = insets;
		content.add(timerLabel,gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = insets;
		content.add(resourcesLabel,gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = insets;
		content.add(usageLabel,gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridy = 3;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = insets;
		content.add(okButton,gbc);
		
		startTimeMs = System.currentTimeMillis();
		timer.start();
		memoryTimer.start();
	}

	public void setupListeners(final SwingWorker<?, ?> worker) {
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				worker.cancel(true);
			}
		});

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				worker.cancel(true);
			}
		});
		
		worker.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getPropertyName().equals("state")) {
					StateValue stateValue = (StateValue) event.getNewValue();
					if (stateValue.equals(StateValue.DONE)) {								
						setVisible(false);
						timer.stop();
						stopMemoryTimer();
						dispose();						
					}
				}
			}

		});
	}
}
