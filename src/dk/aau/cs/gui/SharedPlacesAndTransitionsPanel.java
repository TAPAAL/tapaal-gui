package dk.aau.cs.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import pipe.gui.CreateGui;
import pipe.gui.Pipe;
import pipe.gui.widgets.EscapableDialog;
import dk.aau.cs.model.tapn.SharedPlace;
import dk.aau.cs.model.tapn.SharedTransition;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.util.Require;

public class SharedPlacesAndTransitionsPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final String TRANSITIONS = "Transitions";
	private static final String PLACES = "Places";

	private JList list;
	private SharedPlacesListModel sharedPlacesListModel;
	private SharedTransitionsListModel sharedTransitionsListModel;
	private JComboBox placesTransitionsComboBox;

	public SharedPlacesAndTransitionsPanel(TimedArcPetriNetNetwork network){
		Require.that(network != null, "network cannot be null");

		sharedPlacesListModel = new SharedPlacesListModel(network);
		sharedTransitionsListModel = new SharedTransitionsListModel(network);

		setLayout(new BorderLayout());
		initComponents();

		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Shared"), 
				BorderFactory.createEmptyBorder(3, 3, 3, 3)
		));		
	}

	private void initComponents() {
		list = new JList();
		JScrollPane scrollPane = new JScrollPane(list);

		placesTransitionsComboBox = new JComboBox(new String[]{ PLACES, TRANSITIONS });
		placesTransitionsComboBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				JComboBox source = (JComboBox)e.getSource();
				String selectedItem = (String)source.getSelectedItem();
				if(selectedItem.equals(PLACES)){
					list.setModel(sharedPlacesListModel);
				}else if(selectedItem.equals(TRANSITIONS)){
					list.setModel(sharedTransitionsListModel);
				}
			}		
		});
		placesTransitionsComboBox.setSelectedIndex(0); // Sets up the proper list model

		JPanel buttonPanel = new JPanel();
		JButton renameButton = new JButton("Rename");
		JButton removeButton = new JButton("Remove");
		JButton addButton = new JButton("Add");
		addButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				showSharedTransitionNameDialog();
			}		
		});

		buttonPanel.add(renameButton);
		buttonPanel.add(removeButton);
		buttonPanel.add(addButton);

		add(placesTransitionsComboBox, BorderLayout.PAGE_START);
		add(scrollPane, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.PAGE_END);
	}

	private boolean isDisplayingTransitions(){
		return placesTransitionsComboBox.getSelectedItem().equals(TRANSITIONS);
	}

	private void showSharedTransitionNameDialog() {
		EscapableDialog guiDialog = new EscapableDialog(CreateGui.getApp(), Pipe.TOOL + " " + Pipe.VERSION, true);

		Container contentPane = guiDialog.getContentPane();

		// 1 Set layout
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));

		// 2 Add editor
		JPanel panel = isDisplayingTransitions() ? new SharedTransitionNamePanel(guiDialog.getRootPane(), sharedTransitionsListModel) : new SharedPlaceNamePanel(guiDialog.getRootPane(), sharedPlacesListModel);
		contentPane.add(panel);

		guiDialog.setResizable(false);

		// Make window fit contents' preferred size
		guiDialog.pack();

		// Move window to the middle of the screen
		guiDialog.setLocationRelativeTo(null);
		guiDialog.setVisible(true);
	}

	public class SharedPlacesListModel extends AbstractListModel {
		private static final long serialVersionUID = 1L;
		private TimedArcPetriNetNetwork network;

		public SharedPlacesListModel(TimedArcPetriNetNetwork network){
			Require.that(network != null, "network must not be null");
			this.network = network;
		}

		public Object getElementAt(int index) {
			return network.getSharedPlaceByIndex(index);
		}

		public int getSize() {
			return network.numberOfSharedPlaces();
		}

		public void addElement(SharedPlace place){
			network.add(place);
			fireIntervalAdded(this, network.numberOfSharedPlaces()-1, network.numberOfSharedPlaces());
		}
	}

	public class SharedTransitionsListModel extends AbstractListModel {
		private static final long serialVersionUID = 1L;
		private TimedArcPetriNetNetwork network;

		public SharedTransitionsListModel(TimedArcPetriNetNetwork network){
			Require.that(network != null, "network must not be null");
			this.network = network;
		}

		public Object getElementAt(int index) {
			return network.getSharedTransitionByIndex(index);
		}

		public int getSize() {
			return network.numberOfSharedTransitions();
		}

		public void addElement(SharedTransition transition){
			network.add(transition);
			fireIntervalAdded(this, network.numberOfSharedTransitions()-1, network.numberOfSharedTransitions());
		}
	}
}
