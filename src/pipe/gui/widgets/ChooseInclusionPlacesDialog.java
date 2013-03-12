package pipe.gui.widgets;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import pipe.gui.CreateGui;
import pipe.gui.widgets.InclusionPlaces.InclusionPlacesOption;
import dk.aau.cs.model.tapn.SharedPlace;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedPlace;

public class ChooseInclusionPlacesDialog extends JPanel {
	private static final long serialVersionUID = 7543798264643578137L;
	
	private JRootPane rootPane;
	
	private JList placeList;
	private DefaultListModel listModel;

	private TimedArcPetriNetNetwork tapnNetwork;

	private JCheckBox userSpecifiedCheckBox;
	private JCheckBox AllPlacesCheckBox;
	private JButton clearSelection;
	private JButton selectAll;
	
	
	public ChooseInclusionPlacesDialog(JRootPane rootPane, TimedArcPetriNetNetwork tapnNetwork, InclusionPlaces inclusionPlaces) {
		this.rootPane = rootPane;
		this.tapnNetwork = tapnNetwork;
		initComponents();
		
		setupFromInput(inclusionPlaces);
	}


	private void initComponents() {
		setLayout(new GridBagLayout());
	
		JPanel placesPanel = new JPanel(new BorderLayout());
		placesPanel.setBorder(BorderFactory.createTitledBorder("Choose the places eligible for discrete inclusion checking"));
		
		listModel = new DefaultListModel();
		placeList = new JList(listModel);
		placeList.setCellRenderer(new InclusionPlacesCellRenderer(placeList.getCellRenderer()));
		
		InclusionPlacesListManager manager = new InclusionPlacesListManager(placeList);
		placeList.addListSelectionListener(manager);
		placeList.addMouseListener(manager);
		
		placeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		placeList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		placeList.setVisibleRowCount(0);
		placeList.setEnabled(false);
		
		JScrollPane scrollpane = new JScrollPane(placeList);
		scrollpane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		placesPanel.add(scrollpane, BorderLayout.CENTER);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.insets = new Insets(10, 0, 0, 0);
		add(placesPanel, gbc);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		JPanel checkboxPanel = new JPanel(new BorderLayout());
		AllPlacesCheckBox = new JCheckBox("Use all places for inclusion checking");
		AllPlacesCheckBox.setSelected(true);
		AllPlacesCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				userSpecifiedCheckBox.setSelected(!AllPlacesCheckBox.isSelected());
				placeList.setEnabled(userSpecifiedCheckBox.isSelected());
				clearSelection.setEnabled(userSpecifiedCheckBox.isSelected());
				selectAll.setEnabled(userSpecifiedCheckBox.isSelected());
			}
		});
		
		userSpecifiedCheckBox = new JCheckBox("Manually select places eligible for inclusion checking");
		userSpecifiedCheckBox.setSelected(false);
		userSpecifiedCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AllPlacesCheckBox.setSelected(!userSpecifiedCheckBox.isSelected());
				placeList.setEnabled(userSpecifiedCheckBox.isSelected());
				clearSelection.setEnabled(userSpecifiedCheckBox.isSelected());
				selectAll.setEnabled(userSpecifiedCheckBox.isSelected());
			}
		});
		
		checkboxPanel.add(AllPlacesCheckBox, BorderLayout.NORTH);
		checkboxPanel.add(userSpecifiedCheckBox, BorderLayout.SOUTH);
		buttonPanel.add(checkboxPanel);
		placesPanel.add(buttonPanel, BorderLayout.NORTH);
		
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		selectAll = new JButton("Select All");
		selectAll.setEnabled(false);
		selectAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				for(int i = 0; i < listModel.size(); i++) {
					CheckBoxListItem item = (CheckBoxListItem)listModel.getElementAt(i);
					item.setSelected(true);
				}
				placeList.repaint();
			}
		});
		panel.add(selectAll);
		
		
		clearSelection = new JButton("Clear Selection");
		clearSelection.setEnabled(false);
		clearSelection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				for(int i = 0; i < listModel.size(); i++) {
					CheckBoxListItem item = (CheckBoxListItem)listModel.getElementAt(i);
					item.setSelected(false);
				}
				placeList.repaint();
			}
		});
		panel.add(clearSelection);
		placesPanel.add(panel, BorderLayout.SOUTH);
		
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
					exit();	
			}
		});
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(10, 0, 0, 0);
		add(okButton,gbc);
	}
	
	private void exit() {
		rootPane.getParent().setVisible(false);
	}

	private void setupFromInput(InclusionPlaces inclusionPlaces) {
		addPlacesToPlaceList(inclusionPlaces);
	
		
		boolean allPlaces = inclusionPlaces.inclusionOption() == InclusionPlacesOption.AllPlaces;
		AllPlacesCheckBox.setSelected(allPlaces);
		userSpecifiedCheckBox.setSelected(!allPlaces);
		placeList.setEnabled(!allPlaces);
		clearSelection.setEnabled(!allPlaces);
		selectAll.setEnabled(!allPlaces);
	}


	private void addPlacesToPlaceList(InclusionPlaces inclusionPlaces) {
		List<TimedPlace> incPlaces = inclusionPlaces.inclusionPlaces();
		boolean allPlaces = inclusionPlaces.inclusionOption() == InclusionPlacesOption.AllPlaces;
		
		Collection<SharedPlace> sharedPlaces = tapnNetwork.sharedPlaces();
		Vector<TimedPlace> tempPlaces = new Vector<TimedPlace>(sharedPlaces);
		sortPlacesByName(tempPlaces);
		for(TimedPlace p : tempPlaces)
			listModel.addElement(new CheckBoxListItem(p, allPlaces || incPlaces.contains(p)));
		
		for (TimedArcPetriNet net : tapnNetwork.activeTemplates()) {
			tempPlaces = new Vector<TimedPlace>(net.places());
			sortPlacesByName(tempPlaces);
			for(TimedPlace place : tempPlaces) {
				if(!place.isShared())
					listModel.addElement(new CheckBoxListItem(place, allPlaces || incPlaces.contains(place)));
			}
		}
	}


	private void sortPlacesByName(Vector<TimedPlace> tempPlaces) {
		Collections.sort(tempPlaces, new Comparator<TimedPlace>() {
			public int compare(TimedPlace o1, TimedPlace o2) {
				return o1.name().compareToIgnoreCase(o2.name());
			}
		});
	}


	public static InclusionPlaces showInclusionPlacesDialog(TimedArcPetriNetNetwork tapnNetwork, InclusionPlaces inclusionPlaces) {
		EscapableDialog guiDialog = new EscapableDialog(CreateGui.getApp(),	"Choose Inclustion Places", true);

		Container contentPane = guiDialog.getContentPane();

		// 1 Set layout
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));

		// 2 Add query editor
		ChooseInclusionPlacesDialog dialog = new ChooseInclusionPlacesDialog(guiDialog.getRootPane(), tapnNetwork, inclusionPlaces);
		contentPane.add(dialog);

		guiDialog.setResizable(true);

		guiDialog.setPreferredSize(new Dimension(550,500));
		guiDialog.setMinimumSize(new Dimension(450,400));
		
		// Make window fit contents' preferred size
		guiDialog.pack();

		// Move window to the middle of the screen
		guiDialog.setLocationRelativeTo(null);
		guiDialog.setVisible(true);

		return dialog.getInclusionPlaces();
	}


	private InclusionPlaces getInclusionPlaces() {
		if(AllPlacesCheckBox.isSelected()) 
			return new InclusionPlaces();
		else {
			List<TimedPlace> inclusionPlaces = new ArrayList<TimedPlace>();
			
			for(int i = 0; i < listModel.getSize(); i++) {
				CheckBoxListItem item = (CheckBoxListItem)listModel.getElementAt(i);
				if(item.isSelected())
					inclusionPlaces.add(item.place());
			}
			
			return new InclusionPlaces(InclusionPlacesOption.UserSpecified, inclusionPlaces);
		}
	}
	
	private class CheckBoxListItem {
		private TimedPlace place;
		private boolean isSelected;
		
		public CheckBoxListItem(TimedPlace place, boolean selected) {
			this.place = place;
			isSelected = selected;
		}
		
		public TimedPlace place() {
			return place;
		}

		public boolean isSelected() {
			return isSelected;
		}
		
		public void setSelected(boolean isSelected) {
			this.isSelected = isSelected;
		}
	
		public String toString() {
			return place.toString();
		}
	}
	
	private class InclusionPlacesCellRenderer extends JPanel implements ListCellRenderer {
		private static final long serialVersionUID = 1257272566670437973L;
		private static final String UNCHECK_TO_DEACTIVATE = "Uncheck to exclude place from inclusion check.";
		private static final String CHECK_TO_ACTIVATE = "Check to make place eligible for inclusion check.";
		private JCheckBox activeCheckbox = new JCheckBox();
		private ListCellRenderer cellRenderer;
		
		
		public InclusionPlacesCellRenderer(ListCellRenderer renderer) {
			cellRenderer = renderer;
			setLayout(new BorderLayout()); 
	        setOpaque(false); 
	        activeCheckbox.setOpaque(false);
		}
		
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			Component renderer = cellRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			removeAll();
			boolean selected = ((CheckBoxListItem)value).isSelected();
			activeCheckbox.setSelected(selected);
			setToolTipText(selected ? UNCHECK_TO_DEACTIVATE : CHECK_TO_ACTIVATE);
			add(activeCheckbox, BorderLayout.WEST);
			add(renderer, BorderLayout.CENTER);
			return this;
		}
		
		
	}
	
	private class InclusionPlacesListManager extends MouseAdapter implements ListSelectionListener, ActionListener {
		private ListSelectionModel selectionModel;
		private JList list;
		
		public InclusionPlacesListManager(JList list) {
			this.list = list;
			selectionModel = list.getSelectionModel();
			this.list.registerKeyboardAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), JComponent.WHEN_FOCUSED);
		}
		
		private void toggleSelection(int index) { 
			if(index<0 || !list.isEnabled()) 
				return; 
			
			if(!selectionModel.isSelectedIndex(index)) 
				selectionModel.addSelectionInterval(index, index); 
			
			CheckBoxListItem item = ((CheckBoxListItem)list.getModel().getElementAt(index));
			item.setSelected(!item.isSelected());

			list.repaint();
		}
		
		public void mouseClicked(MouseEvent e) {
			int index = list.locationToIndex(e.getPoint()); 
			
			if(index<0) 
				return; 
			
			toggleSelection(index);
		}

		public void valueChanged(ListSelectionEvent e) {
		}

		public void actionPerformed(ActionEvent e) {
			toggleSelection(list.getSelectedIndex());
		}
		
//		private class PlaceNameComparer implements Comparator<TimedPlace> {
//			public int compare(TimedPlace p1, TimedPlace p2) {
//				return p1.name().compareTo(p2.name());
//			}
//		}
	}
	
	
}
