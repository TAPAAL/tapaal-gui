package dk.aau.cs.gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import pipe.gui.CreateGui;
import pipe.gui.undo.UndoManager;
import pipe.gui.widgets.EscapableDialog;

import dk.aau.cs.gui.components.NonsearchableJList;
import dk.aau.cs.gui.undo.AddSharedPlaceCommand;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.gui.undo.SortSharedPlacesCommand;
import dk.aau.cs.gui.undo.SortSharedTransitionsCommand;
import dk.aau.cs.model.tapn.SharedPlace;
import dk.aau.cs.model.tapn.SharedTransition;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.util.Require;
import dk.aau.cs.util.RequireException;

public class SharedPlacesAndTransitionsPanel extends JPanel {
	private static final String TRANSITION_IS_USED_MESSAGE = "<html>The shared transition is used in one or more components.<br/>TAPAAL will unshare all transitions under this name,<br/>but leave the transitions in the components.</html>";
	private static final String PLACE_IS_USED_MESSAGE = "<html>The shared place is used in one or more components.<br/>TAPAAL will unshare all places under this name,<br/>but leave the places in the components.</html>";

	private static final long serialVersionUID = 1L;
	private static final String TRANSITIONS = "Transitions";
	private static final String PLACES = "Places";

	private JList list;
	private SharedPlacesListModel sharedPlacesListModel;
	private SharedTransitionsListModel sharedTransitionsListModel;
	private JComboBox placesTransitionsComboBox;
	private UndoManager undoManager;
	private NameGenerator nameGenerator;
	private TabContent tab;

	private JButton renameButton = new JButton("Rename");
	private JButton removeButton  = new JButton("Remove");
	private JButton addButton = new JButton("New");
	private JButton moveUpButton;
	private JButton moveDownButton;
	private JButton sortButton;
	
	private static final String toolTipNewPlace = "Create a new place";
	private static final String toolTipRemovePlace = "Remove the selected place";
	private static final String toolTipRenamePlace = "Rename the selected place";
	private static final String toolTipSortTransitions = "Sort the shared transitions alphabetically";
	private static final String toolTipSortPlaces = "Sort the shared places alphabetically";
	private final static String toolTipMoveUp = "Move the selected item up";
	private final static String toolTipMoveDown = "Move the selected item down";
	
	//private static final String toolTipSharedPlacesPanel = "Here you can manage the shared places.<html><br/></html>Shared places can link different components.";
	private static final String toolTipNewTransition = "Create a new transition";
	private static final String toolTipRenameTransition = "Rename the selected transition";
	//private static final String toolTipSharedTransitionsPanel = "Here you can manage the shared transitions.<html><br/></html>" +
		//	"Shared transitions can link different components.";
	private static final String toolTipRemoveTransition ="Remove the selected transition";
	private static final String toolTipChangeBetweenPlacesAndTransitions = "Switch between shared places and transitions";

	public SharedPlacesAndTransitionsPanel(TabContent tab){
		Require.that(tab != null, "tab cannot be null");

		undoManager = tab.drawingSurface().getUndoManager();
		nameGenerator = tab.drawingSurface().getNameGenerator();
		this.tab = tab;

		sharedPlacesListModel = new SharedPlacesListModel(tab.network());
		sharedTransitionsListModel = new SharedTransitionsListModel(tab.network());

		setLayout(new GridBagLayout());
		initComponents();

		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Shared Places and Transitions"), 
				BorderFactory.createEmptyBorder(3, 3, 3, 3)
		));
		
		this.setToolTipText("Shared places and transitions define the interface among the different components");
		this.addComponentListener(new ComponentListener() {
			int minimumHegiht = SharedPlacesAndTransitionsPanel.this.getMinimumSize().height;
			public void componentShown(ComponentEvent e) {
			}
			
			
			public void componentResized(ComponentEvent e) {
				if(SharedPlacesAndTransitionsPanel.this.getSize().height <= minimumHegiht){
					sortButton.setVisible(false);
				} else {
					sortButton.setVisible(true);
				}
			}
			
		
			public void componentMoved(ComponentEvent e) {
			}
			
			
			public void componentHidden(ComponentEvent e) {
			}
		});
		
		this.setMinimumSize(new Dimension(this.getMinimumSize().width, this.getMinimumSize().height - sortButton.getMinimumSize().height));
	}
	
	public void setNetwork(TimedArcPetriNetNetwork network) {
		sharedPlacesListModel.setNetwork(network);
		sharedTransitionsListModel.setNetwork(network);
	}

	private void initComponents() {
		JPanel listPanel = new JPanel(new GridBagLayout());
		
		list = new NonsearchableJList();
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if(!e.getValueIsAdjusting()){
					JList source = (JList)e.getSource();
					if(source.getSelectedIndex() == -1){
						removeButton.setEnabled(false);
						renameButton.setEnabled(false);
					}else{
						removeButton.setEnabled(true);
						if(list.getSelectedIndices().length == 1) {
							renameButton.setEnabled(true);
						}
						else
							renameButton.setEnabled(false);
					}
					
					int index = list.getSelectedIndex();
					if(index > 0 && list.getSelectedIndices().length == 1)
						moveUpButton.setEnabled(true);
					else
						moveUpButton.setEnabled(false);
								
					if(isDisplayingTransitions()) {
						if(index < sharedTransitionsListModel.getSize() - 1 && list.getSelectedIndices().length == 1)
							moveDownButton.setEnabled(true);
						else
							moveDownButton.setEnabled(false);
						
						if (sharedTransitionsListModel.getSize() >=2) {
							sortButton.setEnabled(true);
						} else
							sortButton.setEnabled(false);
					} else {
						if(index < sharedPlacesListModel.getSize() - 1 && list.getSelectedIndices().length == 1)
							moveDownButton.setEnabled(true);
						else
							moveDownButton.setEnabled(false);
						
						if (sharedPlacesListModel.getSize() >=2) {
							sortButton.setEnabled(true);
						} else
							sortButton.setEnabled(false);
					}
				}
			}
		});
		list.addMouseListener(createDoubleClickMouseAdapter());
		
		JScrollPane scrollPane = new JScrollPane(list);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridheight = 3;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		listPanel.add(scrollPane, gbc);
		
		moveUpButton = new JButton(new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("resources/Images/Up.png")));
		moveUpButton.setEnabled(false);
		moveUpButton.setToolTipText(toolTipMoveUp);
		moveUpButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = list.getSelectedIndex();
				
				if(index > 0) {
					if(isDisplayingTransitions())
						sharedTransitionsListModel.swap(index, index-1);
					else
						sharedPlacesListModel.swap(index, index-1);
					list.setSelectedIndex(index-1);
					list.ensureIndexIsVisible(index-1);
				}
			}
		});
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.SOUTH;
		listPanel.add(moveUpButton,gbc);
		
		moveDownButton = new JButton(new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("resources/Images/Down.png")));
		moveDownButton.setEnabled(false);
		moveDownButton.setToolTipText(toolTipMoveDown);
		moveDownButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = list.getSelectedIndex();
				
				if(isDisplayingTransitions()) {
					if(index < sharedTransitionsListModel.getSize() - 1) {
						sharedTransitionsListModel.swap(index, index+1);
						list.setSelectedIndex(index+1);
						list.ensureIndexIsVisible(index+1);
						
					}
				} else {
					if(index < sharedPlacesListModel.getSize() - 1) {
						sharedPlacesListModel.swap(index, index+1);
						list.setSelectedIndex(index+1);
						list.ensureIndexIsVisible(index+1);
					}
				}
			}
		});
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.NORTH;
		listPanel.add(moveDownButton,gbc);
		
		//Sort button
		sortButton = new JButton(new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("resources/Images/Sort.png")));
		sortButton.setToolTipText(toolTipSortPlaces);
		sortButton.setEnabled(false);
		sortButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(isDisplayingTransitions()){
					Command c = new SortSharedTransitionsCommand(sharedTransitionsListModel);
					undoManager.addNewEdit(c);
					c.redo();
				} else {
					Command c = new SortSharedPlacesCommand(sharedPlacesListModel);
					undoManager.addNewEdit(c);
					c.redo();
				}
			}
		});
		sortButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				if(isDisplayingTransitions()){
					sortButton.setToolTipText(toolTipSortTransitions);
				} else {
					sortButton.setToolTipText(toolTipSortPlaces);
				}
			}
		});

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTH;
		listPanel.add(sortButton,gbc);
		
		placesTransitionsComboBox = new JComboBox(new String[]{ PLACES, TRANSITIONS });
		placesTransitionsComboBox.setToolTipText(toolTipChangeBetweenPlacesAndTransitions);
		placesTransitionsComboBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				JComboBox source = (JComboBox)e.getSource();
				String selectedItem = (String)source.getSelectedItem();
				if(selectedItem.equals(PLACES)){
					list.setModel(sharedPlacesListModel);
					renameButton.setToolTipText(toolTipRenamePlace);
					addButton.setToolTipText(toolTipNewPlace);
					removeButton.setToolTipText(toolTipRemovePlace);
				}else if(selectedItem.equals(TRANSITIONS)){
					list.setModel(sharedTransitionsListModel);
					renameButton.setToolTipText(toolTipRenameTransition);
					addButton.setToolTipText(toolTipNewTransition);
					removeButton.setToolTipText(toolTipRemoveTransition);
				}
				
				if(list.getModel().getSize() > 0) {
					list.setSelectedIndex(0);
					list.ensureIndexIsVisible(0);
				} else {
					moveDownButton.setEnabled(false);
					moveUpButton.setEnabled(false);
					sortButton.setEnabled(false);
				}
				if (list.getModel().getSize() <= 0){
					renameButton.setEnabled(false);
					removeButton.setEnabled(false);
				}
			}		
		});
		placesTransitionsComboBox.setSelectedIndex(0); // Sets up the proper list model
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		listPanel.add(placesTransitionsComboBox, gbc);
		
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.weighty = 1;
		add(listPanel, gbc);
		
		JPanel buttonPanel = new JPanel();
		renameButton.setEnabled(false);
		if (isDisplayingTransitions()){
			renameButton.setToolTipText(toolTipRenameTransition);
		}
		else {
			renameButton.setToolTipText(toolTipRenamePlace);
		}
		renameButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				if(isDisplayingTransitions()){
					showSharedTransitionNameDialog((SharedTransition)list.getSelectedValue());
				}else{
					showSharedPlaceNameDialog((SharedPlace)list.getSelectedValue());
				}
			}		
		});
		removeButton.setEnabled(false);
		if (isDisplayingTransitions()){
			removeButton.setToolTipText(toolTipRemoveTransition);
		}
		else {
			removeButton.setToolTipText(toolTipRemovePlace);
		}
		removeButton.addActionListener(new DeleteSharedPlaceOrTransition(list, this, tab, sharedPlacesListModel, sharedTransitionsListModel,nameGenerator));

		if (isDisplayingTransitions()){
			addButton.setToolTipText(toolTipNewTransition);
		}
		else {
			addButton.setToolTipText(toolTipNewPlace);
		}
		addButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				if(isDisplayingTransitions()){
					showSharedTransitionNameDialog(null);
				}else{
					showSharedPlaceNameDialog(null);
				}
			}		
		});

		buttonPanel.add(renameButton);
		buttonPanel.add(removeButton);
		buttonPanel.add(addButton);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		add(buttonPanel, gbc);
	}

	private MouseListener createDoubleClickMouseAdapter() {
		return new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (!list.isSelectionEmpty()) {
					if (arg0.getButton() == MouseEvent.BUTTON1 && arg0.getClickCount() == 2) {
						if(isDisplayingTransitions()){
							showSharedTransitionNameDialog((SharedTransition)list.getSelectedValue());
						}else{
							showSharedPlaceNameDialog((SharedPlace)list.getSelectedValue());
						}
					}
				}
			}
		};
	}

	protected boolean isDisplayingTransitions(){
		return placesTransitionsComboBox.getSelectedItem().equals(TRANSITIONS);
	}

	private void showSharedTransitionNameDialog(SharedTransition transitionToEdit) {
		EscapableDialog guiDialog = new EscapableDialog(CreateGui.getApp(), "Edit Shared Transition", true);
		Container contentPane = guiDialog.getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
		JPanel panel = new SharedTransitionNamePanel(guiDialog.getRootPane(), sharedTransitionsListModel, undoManager, nameGenerator, new Context(tab), transitionToEdit);
		contentPane.add(panel);

		guiDialog.setResizable(false);
		guiDialog.pack();
		guiDialog.setLocationRelativeTo(null);
		guiDialog.setVisible(true);
	}

	private void showSharedPlaceNameDialog(SharedPlace placeToEdit) {
		EscapableDialog guiDialog = new EscapableDialog(CreateGui.getApp(), "Edit Shared Place", true);
		Container contentPane = guiDialog.getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));

		JPanel panel = new SharedPlaceNamePanel(guiDialog.getRootPane(), sharedPlacesListModel, new Context(tab), placeToEdit);
		contentPane.add(panel);

		guiDialog.setResizable(false);
		guiDialog.pack();
		guiDialog.setLocationRelativeTo(null);
		guiDialog.setVisible(true);
	}

	public void removeSharedPlace(SharedPlace place){
		if(sharedPlacesListModel.network.getSharedPlaceByName(place.name()) != null){
			sharedPlacesListModel.removeElement(place);
		}
	}
	
	public void addSharedPlace(SharedPlace place){
		addSharedPlace(place, false);
	}
	public void addSharedPlace(SharedPlace place, boolean multiAdd){
		sharedPlacesListModel.addElement(place, multiAdd);
	}
	
	public void removeSharedTransition(SharedTransition transition){
		if(sharedTransitionsListModel.network.getSharedTransitionByName(transition.name()) != null){
			sharedTransitionsListModel.removeElement(transition);
		}
	}
	public void addSharedTransition(SharedTransition transition){
		addSharedTransition(transition, false);
	}
	public void addSharedTransition(SharedTransition transition, boolean multiAdd){
		sharedTransitionsListModel.addElement(transition, multiAdd);
	}

	public class SharedPlacesListModel extends AbstractListModel {
		private static final long serialVersionUID = 1L;
		private TimedArcPetriNetNetwork network;

		public SharedPlacesListModel(TimedArcPetriNetNetwork network){
			Require.that(network != null, "network must not be null");
			this.network = network;
			
			addListDataListener(new ListDataListener() {
				public void intervalAdded(ListDataEvent arg0) {
					list.setSelectedIndex(arg0.getIndex0());
					list.ensureIndexIsVisible(arg0.getIndex0());
				}

				public void intervalRemoved(ListDataEvent arg0) {
					int index = (arg0.getIndex0() == 0) ? 0 : (arg0.getIndex0() - 1);
					list.setSelectedIndex(index);
					list.ensureIndexIsVisible(index);
				}

				public void contentsChanged(ListDataEvent e) {
				}
			});
		}

		public void swap(int currentIndex, int newIndex) {
			network.swapSharedPlaces(currentIndex, newIndex);
		}
		
		public SharedPlace[] sort(){
			SharedPlace[] oldOrder = network.sortSharedPlaces();
			fireContentsChanged(this, 0, getSize());
			return oldOrder;
		}
		
		public void undoSort(SharedPlace[] oldOrder) {
			network.undoSort(oldOrder);
			fireContentsChanged(this, 0, getSize());
		}

		public Object getElementAt(int index) {
			return network.getSharedPlaceByIndex(index);
		}

		public int getSize() {
			return network.numberOfSharedPlaces();
		}

		public void addElement(SharedPlace place){
			addElement(place, false);
		}
		public void addElement(SharedPlace place, boolean multiAdd){
			network.add(place, multiAdd);
			fireIntervalAdded(this, network.numberOfSharedPlaces()-1, network.numberOfSharedPlaces());
		}

		public void removeElement(SharedPlace place) {
			network.remove(place);
			fireContentsChanged(this, 0, getSize());
			int numElements = list.getModel().getSize();
			if(numElements <= 1) {
				moveDownButton.setEnabled(false);
				moveUpButton.setEnabled(false);
				sortButton.setEnabled(false);
			}
			if (numElements <= 0) {
				removeButton.setEnabled(false);
				renameButton.setEnabled(false);
			}
		}

		public void updatedName(){
			fireContentsChanged(this, 0, getSize());
		}

		public void setNetwork(TimedArcPetriNetNetwork network) {
			Require.that(network != null, "network cannot be null");
			this.network = network;
			fireContentsChanged(this, 0, network.numberOfSharedPlaces());
		}

	}

	public class SharedTransitionsListModel extends AbstractListModel {
		private static final long serialVersionUID = 1L;
		private TimedArcPetriNetNetwork network;

		public SharedTransitionsListModel(TimedArcPetriNetNetwork network){
			Require.that(network != null, "network must not be null");
			this.network = network;
			
			addListDataListener(new ListDataListener() {
				public void intervalAdded(ListDataEvent arg0) {
					list.setSelectedIndex(arg0.getIndex0());
					list.ensureIndexIsVisible(arg0.getIndex0());
				}

				public void intervalRemoved(ListDataEvent arg0) {
					int index = (arg0.getIndex0() == 0) ? 0 : (arg0.getIndex0() - 1);
					list.setSelectedIndex(index);
					list.ensureIndexIsVisible(index);
				}

				public void contentsChanged(ListDataEvent e) {
				}
			});
		}

		public Object getElementAt(int index) {
			return network.getSharedTransitionByIndex(index);
		}
		
		public void swap(int currentIndex, int newIndex) {
			network.swapSharedTransitions(currentIndex, newIndex);
		}
		
		public SharedTransition[] sort(){
			SharedTransition[] oldOrder = network.sortSharedTransitions();
			fireContentsChanged(this, 0, getSize());
			return oldOrder;
		}
		
		public void undoSort(SharedTransition[] oldOrder) {
			network.undoSort(oldOrder);
			fireContentsChanged(this, 0, getSize());
		}

		public int getSize() {
			return network.numberOfSharedTransitions();
		}
		public void addElement(SharedTransition transition){
			addElement(transition, false);
		}

		public void addElement(SharedTransition transition, boolean multiAdd){
			network.add(transition, multiAdd);
			fireIntervalAdded(this, network.numberOfSharedTransitions()-1, network.numberOfSharedTransitions());
		}

		public void removeElement(SharedTransition transition) {
			network.remove(transition);
			fireContentsChanged(this, 0, getSize());
			int numElements = list.getModel().getSize();
			if(numElements <= 1) {
				moveDownButton.setEnabled(false);
				moveUpButton.setEnabled(false);
				sortButton.setEnabled(false);
			}
			if (numElements <= 0) {
				removeButton.setEnabled(false);
				renameButton.setEnabled(false);
			}
		}

		public void updatedName() {
			fireContentsChanged(this, 0, getSize());
		}

		public void setNetwork(TimedArcPetriNetNetwork network) {
			Require.that(network != null, "network cannot be null");
			this.network = network;
			fireContentsChanged(this, 0, network.numberOfSharedTransitions());
		}
	}
}
