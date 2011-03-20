package dk.aau.cs.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map.Entry;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import pipe.dataLayer.Arc;
import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.Template;
import pipe.dataLayer.TimedInhibitorArcComponent;
import pipe.dataLayer.TimedInputArcComponent;
import pipe.dataLayer.TimedOutputArcComponent;
import pipe.dataLayer.TimedPlaceComponent;
import pipe.dataLayer.TimedTransitionComponent;
import pipe.dataLayer.TransportArcComponent;
import pipe.gui.CreateGui;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.Pipe;
import pipe.gui.undo.DeleteTimedInhibitorArcCommand;
import pipe.gui.undo.DeleteTimedInputArcCommand;
import pipe.gui.undo.DeleteTimedOutputArcCommand;
import pipe.gui.undo.DeleteTimedPlaceCommand;
import pipe.gui.undo.DeleteTimedTransitionCommand;
import pipe.gui.undo.DeleteTransportArcCommand;
import pipe.gui.undo.UndoManager;
import pipe.gui.widgets.EscapableDialog;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.gui.undo.DeleteQueriesCommand;
import dk.aau.cs.gui.undo.DeleteSharedPlaceCommand;
import dk.aau.cs.gui.undo.DeleteSharedTransitionCommand;
import dk.aau.cs.gui.undo.RenameTimedPlaceCommand;
import dk.aau.cs.gui.undo.RenameTimedTransitionCommand;
import dk.aau.cs.gui.undo.UnsharePlaceCommand;
import dk.aau.cs.gui.undo.UnshareTransitionCommand;
import dk.aau.cs.model.tapn.LocalTimedPlace;
import dk.aau.cs.model.tapn.SharedPlace;
import dk.aau.cs.model.tapn.SharedTransition;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.util.Require;

public class SharedPlacesAndTransitionsPanel extends JPanel {
	private static final String TRANSITION_IS_USED_MESSAGE = "<html>The shared transition may be used in one or more components.<br/>TAPAAL will unshare all transitions under this name,<br/>but leave the transitions in the components.</html>";
	private static final String PLACE_IS_USED_MESSAGE = "<html>The shared place may be used in one or more components.<br/>TAPAAL will unshare all places under this name,<br/>but leave the places in the components.</html>";

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

	private JButton renameButton;
	private JButton removeButton;

	public SharedPlacesAndTransitionsPanel(TabContent tab){
		Require.that(tab != null, "tab cannot be null");

		this.undoManager = tab.drawingSurface().getUndoManager();
		this.nameGenerator = tab.drawingSurface().getNameGenerator();
		this.tab = tab;

		sharedPlacesListModel = new SharedPlacesListModel(tab.network());
		sharedTransitionsListModel = new SharedTransitionsListModel(tab.network());

		setLayout(new BorderLayout());
		initComponents();

		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Shared"), 
				BorderFactory.createEmptyBorder(3, 3, 3, 3)
		));		
	}
	
	public void setNetwork(TimedArcPetriNetNetwork network) {
		sharedPlacesListModel.setNetwork(network);
		sharedTransitionsListModel.setNetwork(network);
	}

	private void initComponents() {
		list = new JList();
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if(!e.getValueIsAdjusting()){
					JList source = (JList)e.getSource();
					if(source.getSelectedIndex() == -1){
						removeButton.setEnabled(false);
						renameButton.setEnabled(false);
					}else{
						removeButton.setEnabled(true);
						renameButton.setEnabled(true);
					}
				}
			}
		});
		list.addMouseListener(createDoubleClickMouseAdapter());

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
		renameButton = new JButton("Rename");
		renameButton.setEnabled(false);
		renameButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				if(isDisplayingTransitions()){
					showSharedTransitionNameDialog((SharedTransition)list.getSelectedValue());
				}else{
					showSharedPlaceNameDialog((SharedPlace)list.getSelectedValue());
				}
			}		
		});
		removeButton = new JButton("Remove");
		removeButton.setEnabled(false);
		removeButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				if(list.getSelectedValue() != null){
					JCheckBox checkBox = new JCheckBox("Delete from all components");

					JLabel label = new JLabel(isDisplayingTransitions() ? TRANSITION_IS_USED_MESSAGE : PLACE_IS_USED_MESSAGE);
					Object[] params = {label, checkBox};
					int choice = JOptionPane.showConfirmDialog(CreateGui.getApp(), params, "Warning", JOptionPane.WARNING_MESSAGE);
					if(choice == JOptionPane.OK_OPTION){
						boolean deleteFromTemplates = checkBox.isSelected();

						undoManager.newEdit();
						if(isDisplayingTransitions()){
							deleteSharedTransition(deleteFromTemplates);
						}else{
							deleteSharedPlace(deleteFromTemplates);
						}
					}
				}
			}

			private void deleteSharedPlace(boolean deleteFromTemplates) {
				SharedPlace sharedPlace = (SharedPlace)list.getSelectedValue();
				Collection<TAPNQuery> affectedQueries = findAffectedQueries(sharedPlace);
				if(affectedQueries.size() > 0){
					StringBuffer buffer = new StringBuffer("The following queries contains the shared place and will also be deleted:");
					buffer.append(System.getProperty("line.separator"));
					buffer.append(System.getProperty("line.separator"));
					
					for(TAPNQuery query : affectedQueries){
						buffer.append(query.getName());
						buffer.append(System.getProperty("line.separator"));
					}
					buffer.append(System.getProperty("line.separator"));
					buffer.append("Do you want to continue?");
					int choice = JOptionPane.showConfirmDialog(SharedPlacesAndTransitionsPanel.this, buffer.toString(), "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
					if(choice == JOptionPane.NO_OPTION) return;
					
					Command cmd = new DeleteQueriesCommand(tab, affectedQueries);
					cmd.redo();
					undoManager.addEdit(cmd);
				}
				if(deleteFromTemplates){
					for(Template template : tab.templates()){ // TODO: Get rid of pipe references somehow
						TimedPlaceComponent place = (TimedPlaceComponent)template.guiModel().getPlaceByName(sharedPlace.name());
						if(place != null){
							for(Arc arc : place.getPreset()){
								Command cmd = createDeleteArcCommand(template, arc, tab.drawingSurface()); 
								cmd.redo();
								undoManager.addEdit(cmd);
							}

							for(Arc arc : place.getPostset()){
								Command cmd = createDeleteArcCommand(template, arc, tab.drawingSurface());
								cmd.redo();
								undoManager.addEdit(cmd);
							}

							Command cmd = new DeleteTimedPlaceCommand(place, template.model(), template.guiModel(), tab.drawingSurface());
							cmd.redo();
							undoManager.addEdit(cmd);
						}
					}
					tab.drawingSurface().repaint();
					sharedPlacesListModel.removeElement(sharedPlace);
					undoManager.addEdit(new DeleteSharedPlaceCommand(sharedPlace, sharedPlacesListModel));
				}else{
					Hashtable<LocalTimedPlace, String> createdPlaces = new Hashtable<LocalTimedPlace, String>();
					for(Template template : tab.templates()){
						TimedPlace place = template.model().getPlaceByName(sharedPlace.name());
						TimedPlaceComponent component = (TimedPlaceComponent) template.guiModel().getPlaceByName(sharedPlace.name());
						if(place != null){
							String name = nameGenerator.getNewPlaceName(template.model());
							LocalTimedPlace localPlace = new LocalTimedPlace(name);
							createdPlaces.put(localPlace, name);
							Command cmd = new UnsharePlaceCommand(template.model(), sharedPlace, localPlace, component);
							cmd.redo();
							undoManager.addEdit(cmd);
						}
					}
					Command deleteCmd = new DeleteSharedPlaceCommand(sharedPlace, sharedPlacesListModel);
					deleteCmd.redo();
					undoManager.addEdit(deleteCmd);
					
					// We introduced temporary name before, to avoid exceptions, so we rename the places to the correct names here
					for(Entry<LocalTimedPlace, String> entry : createdPlaces.entrySet()){
						Command renameCmd = new RenameTimedPlaceCommand(tab, entry.getKey(), entry.getValue(), sharedPlace.name());
						renameCmd.redo();
						undoManager.addEdit(renameCmd);
					}
				}
			}

			private Collection<TAPNQuery> findAffectedQueries(SharedPlace sharedPlace) {
				ArrayList<TAPNQuery> queries = new ArrayList<TAPNQuery>();
				for(TAPNQuery query : tab.queries()){
					// TODO: add queries
				}
				return queries;
			}

			private Command createDeleteArcCommand(Template template, Arc arc, DrawingSurfaceImpl drawingSurface) {
				if(arc instanceof TimedInhibitorArcComponent){
					return new DeleteTimedInhibitorArcCommand((TimedInhibitorArcComponent)arc, template.model(), template.guiModel(), drawingSurface);
				}else if(arc instanceof TransportArcComponent){
					TransportArcComponent component = (TransportArcComponent)arc;
					return new DeleteTransportArcCommand(component, component.underlyingTransportArc(), template.model(), template.guiModel(), drawingSurface);
				}else if(arc instanceof TimedInputArcComponent){
					return new DeleteTimedInputArcCommand((TimedInputArcComponent)arc, template.model(), template.guiModel(), drawingSurface);
				}else{
					return new DeleteTimedOutputArcCommand((TimedOutputArcComponent)arc, template.model(), template.guiModel(), drawingSurface);
				}
			}

			private void deleteSharedTransition(boolean deleteFromTemplates) {
				SharedTransition sharedTransition = (SharedTransition)list.getSelectedValue();
				if(deleteFromTemplates){
					for(Template template : tab.templates()){ // TODO: Get rid of pipe references somehow
						TimedTransitionComponent transition = (TimedTransitionComponent)template.guiModel().getTransitionByName(sharedTransition.name());
						undoManager.addEdit(new DeleteTimedTransitionCommand(transition, transition.underlyingTransition().model(), template.guiModel(), tab.drawingSurface()));
						transition.delete();
					}
					tab.drawingSurface().repaint();
					sharedTransitionsListModel.removeElement(sharedTransition);
					undoManager.addEdit(new DeleteSharedTransitionCommand(sharedTransition, sharedTransitionsListModel));
				}else{
					Collection<TimedTransition> copy = sharedTransition.transitions();
					for(TimedTransition transition : copy){
						transition.unshare();
						undoManager.addEdit(new UnshareTransitionCommand(sharedTransition, transition));
					}
					sharedTransitionsListModel.removeElement(sharedTransition);
					undoManager.addEdit(new DeleteSharedTransitionCommand(sharedTransition, sharedTransitionsListModel));
					for(TimedTransition transition : copy){
						String name = nameGenerator.getNewTransitionName(transition.model());
						// We add this invisible transition renaming to avoid problems with undo
						undoManager.addEdit(new RenameTimedTransitionCommand(transition, name, transition.name())); 
					}
				}
			}		
		});

		JButton addButton = new JButton("Add");
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

		add(placesTransitionsComboBox, BorderLayout.PAGE_START);
		add(scrollPane, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.PAGE_END);
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

	private boolean isDisplayingTransitions(){
		return placesTransitionsComboBox.getSelectedItem().equals(TRANSITIONS);
	}

	private void showSharedTransitionNameDialog(SharedTransition transitionToEdit) {
		EscapableDialog guiDialog = new EscapableDialog(CreateGui.getApp(), Pipe.TOOL + " " + Pipe.VERSION, true);
		Container contentPane = guiDialog.getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
		JPanel panel = new SharedTransitionNamePanel(guiDialog.getRootPane(), sharedTransitionsListModel, undoManager, nameGenerator, transitionToEdit);
		contentPane.add(panel);

		guiDialog.setResizable(false);
		guiDialog.pack();
		guiDialog.setLocationRelativeTo(null);
		guiDialog.setVisible(true);
	}

	private void showSharedPlaceNameDialog(SharedPlace placeToEdit) {
		EscapableDialog guiDialog = new EscapableDialog(CreateGui.getApp(), Pipe.TOOL + " " + Pipe.VERSION, true);
		Container contentPane = guiDialog.getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));

		JPanel panel = new SharedPlaceNamePanel(guiDialog.getRootPane(), sharedPlacesListModel, undoManager, nameGenerator, placeToEdit);
		contentPane.add(panel);

		guiDialog.setResizable(false);
		guiDialog.pack();
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

		public void removeElement(SharedPlace place) {
			network.remove(place);
			fireContentsChanged(this, 0, getSize());
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

		public void removeElement(SharedTransition transition) {
			network.remove(transition);
			fireContentsChanged(this, 0, getSize());
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
