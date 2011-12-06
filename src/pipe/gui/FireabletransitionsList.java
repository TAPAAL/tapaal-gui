package pipe.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import pipe.dataLayer.Template;
import pipe.gui.graphicElements.Transition;
import pipe.gui.graphicElements.tapn.TimedTransitionComponent;

public class FireabletransitionsList extends JPanel implements ActionListener{
	
	private static final long serialVersionUID = -121639323606689256L;

	public FireabletransitionsList() {
		super(new BorderLayout());
		initPanel();
	}
	
	DefaultListModel transitions;
	JList transitionsList;
	JScrollPane scrollPane;
	JButton fireButton;
	
	public void initPanel(){
		transitions = new DefaultListModel();
		transitionsList = new JList(transitions);
		scrollPane = new JScrollPane(transitionsList);
		
		fireButton = new JButton("Fire");
		fireButton.addActionListener(this);
		
		this.add(scrollPane, BorderLayout.CENTER);
		this.add(fireButton, BorderLayout.SOUTH);
	}
	
	ListItem lastSelected;
	
	public void startReInit(){
		lastSelected = (ListItem)transitionsList.getSelectedValue();
		transitions.clear();
	}
	
	public void reInitDone(){
		updateFireButton();
		
		if(transitions.contains(lastSelected)){
			int i = transitions.indexOf(lastSelected);
			transitionsList.setSelectedIndex(i);
		} else if (transitions.size() > 0){
			transitionsList.setSelectedIndex(0);
		}
	}
	
	public void addTransition(Template template, Transition transition){
		ListItem item = new ListItem(transition, template, template.model().getTransitionByName(transition.getName()).isShared());
		if(!transitions.contains(item)){
			transitions.addElement(item);
		}
	}
	
	public void removeTransition(Template template, Transition transition){
		ListItem item = new ListItem(transition, template, template.model().getTransitionByName(transition.getName()).isShared());
		transitions.removeElement(item);
	}
	
	public static final String FIRE_BUTTON_DEACTIVATED_TOOL_TIP = "No transitions are fireable";
	public static final String FIRE_BUTTON_ENABLED_TOOL_TIP = "Press to fire selected transition";
	
	public void updateFireButton(){
		if(transitions.size() == 0){
			fireButton.setEnabled(false);
			fireButton.setToolTipText(FIRE_BUTTON_DEACTIVATED_TOOL_TIP);
		} else {
			fireButton.setEnabled(true);
			fireButton.setToolTipText(FIRE_BUTTON_ENABLED_TOOL_TIP);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		ListItem item = (ListItem)transitionsList.getSelectedValue();
		
		if(item != null) {
			CreateGui.getAnimator().fireTransition(((TimedTransitionComponent)item.getTransition()).underlyingTransition());
		}
		
	}
	
	class ListItem{
		private Transition transition;
		private Template template;
		private boolean isShared;
		
		public ListItem(Transition transition, Template template, boolean isShared){
			this.transition = transition;
			this.template = template;
			this.isShared = isShared;
		}
		
		@Override
		public String toString() {
			if(isShared()){
				return getTransition().getName() + " (shared)";
			} else {
				return getTemplate() + "." + getTransition().getName();
			}
		}
		
		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof ListItem)){
				return false;
			} else {
				return toString().equals(((ListItem)obj).toString());
			}
		}

		public Transition getTransition() {
			return transition;
		}

		public Template getTemplate() {
			return template;
		}

		public boolean isShared() {
			return isShared;
		}		
	}
}
