package dk.aau.cs.gui.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import dk.aau.cs.util.StringComparator;

import pipe.dataLayer.Template;
import pipe.gui.CreateGui;
import pipe.gui.graphicElements.Transition;
import pipe.gui.graphicElements.tapn.TimedTransitionComponent;

public class EnabledTransitionsList extends JPanel{
	
	private static final long serialVersionUID = -121639323606689256L;

	public EnabledTransitionsList() {
		super(new BorderLayout());
		this.setPreferredSize(new Dimension(0, 300));
		initPanel();
	}
	
	DefaultListModel transitions;
	JList transitionsList;
	JScrollPane scrollPane;
	JButton fireButton;
	ListItem lastSelected;
	
	public void initPanel(){
		transitions = new DefaultListModel();
		transitionsList = new JList(transitions);
		
		transitionsList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2){
					fireSelectedTransition();
				}
			}
		});
		
		scrollPane = new JScrollPane(transitionsList);
		
		fireButton = new JButton("Fire");
		fireButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fireSelectedTransition();
			}
		});
		
		this.add(scrollPane, BorderLayout.CENTER);
		this.add(fireButton, BorderLayout.SOUTH);
	}
	
	public void startReInit(){
		lastSelected = (ListItem)transitionsList.getSelectedValue();
		transitions.clear();
	}
	
	public void reInitDone(){
		updateFireButton();
		
		//sort the transitions
		Object[] temp = (Object[])transitions.toArray();
		Arrays.sort(temp);
		transitions.clear();
		for(Object item : temp){
			transitions.addElement(item);
		}
		
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
	
	private void fireSelectedTransition(){
		ListItem item = (ListItem)transitionsList.getSelectedValue();
		
		if(item != null) {
			CreateGui.getAnimator().fireTransition(((TimedTransitionComponent)item.getTransition()).underlyingTransition());
		}
	}
	
	class ListItem implements Comparable<ListItem>{
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

		@Override
		public int compareTo(ListItem o) {

			if(this.isShared() == o.isShared()){
				//if the transitions is from different templates - don't change the order
				if(this .template != o.template){
					return 0;
				} else {
					return compareToString(o);
				}
			} else {
				if(this.isShared()){
					return -1;
				} else {
					return 1;
				}
			}
		}
		
		private int compareToString(ListItem o){
			StringComparator s = new StringComparator();
			return s.compare(this.transition.getName(), o.transition.getName());
			
		}
	}
}
