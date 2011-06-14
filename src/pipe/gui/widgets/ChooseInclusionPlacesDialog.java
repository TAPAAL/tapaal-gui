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
import java.util.List;

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
import pipe.gui.Pipe;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedPlace;

public class ChooseInclusionPlacesDialog extends JPanel {
	private static final long serialVersionUID = 7543798264643578137L;
	
	private JRootPane rootPane;
	
	private JList placeList;
	private DefaultListModel listModel;

	private TimedArcPetriNetNetwork tapnNetwork;
	
	
	public ChooseInclusionPlacesDialog(JRootPane rootPane, TimedArcPetriNetNetwork tapnNetwork, List<TimedPlace> inclusionPlaces) {
		this.rootPane = rootPane;
		this.tapnNetwork = tapnNetwork;
		initComponents();
		
		AddPlacesToListModel(inclusionPlaces);
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
		
		
		JButton selectAll = new JButton("Select All");
		selectAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				for(int i = 0; i < listModel.size(); i++) {
					CheckBoxListItem item = (CheckBoxListItem)listModel.getElementAt(i);
					item.setSelected(true);
				}
				placeList.repaint();
			}
		});
		
		
		buttonPanel.add(selectAll);
		
		JButton clearSelection = new JButton("Clear");
		clearSelection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				for(int i = 0; i < listModel.size(); i++) {
					CheckBoxListItem item = (CheckBoxListItem)listModel.getElementAt(i);
					item.setSelected(false);
				}
				placeList.repaint();
			}
		});
		buttonPanel.add(clearSelection);
		
		
		placesPanel.add(buttonPanel, BorderLayout.NORTH);
		
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

	private void AddPlacesToListModel(List<TimedPlace> inclusionPlaces) {
		for(TimedPlace p : tapnNetwork.sharedPlaces())
			listModel.addElement(new CheckBoxListItem(p, inclusionPlaces.contains(p)));
		
		for (TimedArcPetriNet net : tapnNetwork.activeTemplates()) {
			for(TimedPlace place : net.places()) {
				if(!place.isShared())
					listModel.addElement(new CheckBoxListItem(place, inclusionPlaces.contains(place)));
			}
		}
		
	}


	public static List<TimedPlace> showInclusionPlacesDialog(TimedArcPetriNetNetwork tapnNetwork, List<TimedPlace> inclusionPlaces) {
		EscapableDialog guiDialog = new EscapableDialog(CreateGui.getApp(),	Pipe.getProgramName(), true);

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


	private List<TimedPlace> getInclusionPlaces() {
		List<TimedPlace> inclusionPlaces = new ArrayList<TimedPlace>();
		
		for(int i = 0; i < listModel.getSize(); i++) {
			CheckBoxListItem item = (CheckBoxListItem)listModel.getElementAt(i);
			if(item.isSelected())
				inclusionPlaces.add(item.place());
		}
		
		return inclusionPlaces;
	}
	
	private class CheckBoxListItem {
		private TimedPlace place;
		private boolean isSelected;
		
		public CheckBoxListItem(TimedPlace place, boolean selected) {
			this.place = place;
			this.isSelected = selected;
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
			this.cellRenderer = renderer;
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
			this.selectionModel = list.getSelectionModel();
			this.list.registerKeyboardAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), JComponent.WHEN_FOCUSED);
		}
		
		private void toggleSelection(int index) { 
			if(index<0) 
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
		
	}
}
