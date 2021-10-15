package pipe.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Timer;

import pipe.gui.widgets.EscapableDialog;

public class SimulationControl extends JPanel {
	
	final JSlider simulationSpeed = new JSlider();
	final JCheckBox randomSimulation = new JCheckBox("Enable automatic random simulation");
    final JCheckBox randomMode = new JCheckBox("Choose next transition randomly");
    final Timer timer = new Timer(simulationSpeed.getValue()*20, e -> CreateGui.getCurrentTab().getTransitionFiringComponent().fireSelectedTransition());
    private static boolean defaultIsRandomTrasition;
    private static SimulationControl instance;
	
	public static SimulationControl getInstance(){
		if(instance == null){
			instance = new SimulationControl();
		}
		return instance;
	}
	
	public void addRandomSimulationActionListener(ActionListener l){
		randomSimulation.addActionListener(l);
	}
	
	public void showCheckbox(boolean show){
		randomSimulation.setVisible(show);
	}
	
	private SimulationControl() {
		super(new GridBagLayout());

        simulationSpeed.setSnapToTicks(false);
		simulationSpeed.setMajorTickSpacing(10);
		simulationSpeed.setPaintLabels(false);
		simulationSpeed.setPaintTicks(true);
		simulationSpeed.setPaintTrack(false);
		simulationSpeed.setPreferredSize(new Dimension(340, simulationSpeed.getPreferredSize().height));
		
		simulationSpeed.addChangeListener(e -> {
            setDelay((100 - simulationSpeed.getValue())*20);
        });

        GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.gridx = 0;
		gbc.gridy = 0;
		add(randomSimulation, gbc);

		gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(randomMode, gbc);

        setRandomTransitionMode(defaultIsRandomTrasition);

        gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.gridx = 0;
		gbc.gridy = 2;
		add(new JLabel("Set simulation speed:"), gbc);
		
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.gridx = 0;
		gbc.gridy = 3;
		add(simulationSpeed, gbc);
		
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Simulation controller"), 
				BorderFactory.createEmptyBorder(3, 3, 3, 3)));
		initTimer();
	}
	
	private void setDelay(int delay) {
		timer.setInitialDelay(delay);
		timer.setDelay(delay);
		if(timer.isRunning()){
			timer.restart();
		}
	}
	
	private void initTimer(){
        timer.setRepeats(true);
	}

    public boolean randomSimulation(){
		return randomSimulation.isSelected();
	}
	
	public void start(){
		timer.start();
		CreateGui.getCurrentTab().getTransitionFiringComponent().updateFireButton();
	}
	
	public void stop(){
		timer.stop();
		CreateGui.getCurrentTab().getTransitionFiringComponent().updateFireButton();
	}
	
	public boolean isRunning(){
		return timer.isRunning();
	}
	
	private static EscapableDialog dialog;
	
	public static void startSimulation(){
		JPanel contentPane = new JPanel(new GridBagLayout());
		
		JButton stopSimulationButton = new JButton("Stop");
		stopSimulationButton.addActionListener(arg0 -> dialog.setVisible(false));
		
		getInstance().showCheckbox(false);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(0, 3, 0, 3);
		gbc.fill = GridBagConstraints.BOTH;
		contentPane.add(getInstance(), gbc);
		
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(3, 3, 0, 3);
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		contentPane.add(stopSimulationButton, gbc);
		
		dialog = new EscapableDialog(CreateGui.getApp(), "Simulation controls", true);
		
		dialog.addComponentListener(new ComponentAdapter() {
			public void componentHidden(ComponentEvent e) {
				getInstance().stop();
			}
		});
		
		getInstance().start();
		
		dialog.getRootPane().setDefaultButton(stopSimulationButton);
		dialog.setContentPane(contentPane);
		dialog.pack();
		dialog.setResizable(false);
		
		//Calculate location
		int x = CreateGui.getApp().getLocation().x + CreateGui.getApp().getWidth() - dialog.getWidth() - 30;
		int y = CreateGui.getApp().getLocation().y + 30;
		
		dialog.setLocation(x, y);
		dialog.setVisible(true);
	}

    public boolean isRandomTransitionMode(){
        if(SimulationControl.getInstance().randomSimulation()){
            return true;
        } else {
            return randomMode.isSelected();
        }
    }

    public void setRandomTransitionMode(boolean randomTransition){
        randomMode.setSelected(randomTransition);
    }
    public static boolean isRandomTransition(){
        if(instance != null){
            return getInstance().isRandomTransitionMode();
        } else {
            return defaultIsRandomTrasition;
        }
    }
    public static void setDefaultIsRandomTransition(boolean delayEnabledTransitionIsRandomTransition) {
        defaultIsRandomTrasition = delayEnabledTransitionIsRandomTransition;
    }
}
