package dk.aau.cs.gui.components;

import java.awt.BorderLayout;
import java.awt.Dimension;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Random;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import dk.aau.cs.util.IntervalOperations;
import dk.aau.cs.util.StringComparator;

import org.jetbrains.annotations.NotNull;
import pipe.dataLayer.Template;
import pipe.gui.CreateGui;
import pipe.gui.SimulationControl;
import pipe.gui.graphicElements.Transition;
import pipe.gui.graphicElements.tapn.TimedTransitionComponent;
//TODO clean up!!! 
public class EnabledTransitionsList extends JPanel{

	public EnabledTransitionsList() {
		super(new BorderLayout());
		this.setPreferredSize(new Dimension(0, 300));
		initPanel();
	}

	final DefaultListModel<TransitionListItem> transitions = new DefaultListModel<>();
	final JList<TransitionListItem> transitionsList = new JList<>(transitions);
	final JScrollPane scrollPane = new JScrollPane(transitionsList);
	TransitionListItem lastSelected;

	public void initPanel(){

        transitionsList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2){
					fireSelectedTransition();
				}
				
				if(e.getClickCount() == 1){
					TransitionListItem highlightedItem = transitionsList.getSelectedValue();
					if(highlightedItem != null){
						highlightedItem.getTransition().blink();
					}
				}
			}
		});

        this.add(scrollPane, BorderLayout.CENTER);
	}
	
	public void startReInit(){
		lastSelected = transitionsList.getSelectedValue();
		transitions.clear();
	}

	public void reInitDone(){
		if(SimulationControl.getInstance().isRandomTransitionMode()){
			selectRandom();
			return;
		}
		//Trick to make the "splitter" appear
		//transitions.addElement(new SplitterListItem());
		//sort the transitions
		Object[] temp = transitions.toArray();
		Arrays.sort(temp);
		transitions.clear();
		for(Object item : temp){
			transitions.addElement((TransitionListItem)item);
		}

		if(transitions.contains(lastSelected)){
			int i = transitions.indexOf(lastSelected);
			transitionsList.setSelectedIndex(i);
		} else if (transitions.size() > 0){
			transitionsList.setSelectedIndex(0);
		}
	}

	private void selectRandom() {
		if(transitions.size() == 0){
			return;
		}
		Random r = new Random();
		int randSelect = r.nextInt(transitions.size());
		transitionsList.setSelectedIndex(randSelect);
		
	}

	public void addTransition(Template template, Transition transition){
		TransitionListItem item = new TransitionListItem(transition, template);

		if(!transitions.contains(item)){
			transitions.addElement(item);
		}
	}

    public void fireSelectedTransition(){
		TransitionListItem item = transitionsList.getSelectedValue();

		if(item != null) {
			CreateGui.getAnimator().dFireTransition(((TimedTransitionComponent)item.getTransition()).underlyingTransition());
		}
	}

	interface ListItem extends Comparable<ListItem>{}


	static class TransitionListItem implements ListItem{
		private final Transition transition;
		private final Template template;

		public TransitionListItem(Transition transition, Template template){
			this.transition = transition;
			this.template = template;
		}

		public String toString(boolean showIntervals) {

			String interval = transition.getDInterval() == null || !showIntervals || !transition.isTimed() ? "" : transition.getDInterval().toString() + " ";
			
			String transitionName = getTransition().getName(); 
			if(isShared()){
				transitionName +=  " (shared)";
			} else {
				transitionName = getTemplate() + "." + transitionName;
			}

			return interval + transitionName;
		}
		
		public String toString(){
			if(CreateGui.getApp().isShowingDelayEnabledTransitions()){
				return toString(true);
			} else {
				return toString(false);
			}
		}

		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof TransitionListItem)){
				return false;
			} else {
				return toString().equals(obj.toString());
			}
		}

		public Transition getTransition() {
			return transition;
		}

		public Template getTemplate() {
			return template;
		}

		public boolean isShared() {
			return template.model().getTransitionByName(transition.getName()).isShared();
		}

		public int compareTo(@NotNull ListItem o) {
			if(o instanceof TransitionListItem){
				return compareTo((TransitionListItem)o);
			} else {
				return this.transition.isEnabled() ? -1 : 1;
			}
		}

		public int compareTo(TransitionListItem o) {
			BigDecimal thisLower = IntervalOperations.getRatBound(this.transition.getDInterval().lowerBound()).getBound();
			BigDecimal otherLower = IntervalOperations.getRatBound(o.transition.getDInterval().lowerBound()).getBound();
			StringComparator s = new StringComparator();
			//Sort according to lower bound
			int result = thisLower.compareTo(otherLower);
			//According to strict non strict
			if(result == 0 && this.transition.getDInterval().isLowerBoundNonStrict() != o.transition.getDInterval().isLowerBoundNonStrict()){
				if(this.transition.getDInterval().isLowerBoundNonStrict()){
					result = -1;
				} else {
					result = 1;
				}				
			}
			//According to template name
			if(result == 0){
				result = s.compare(this.template.model().name(), o.template.model().name()); 
			}
			//According to transition name
			if(result == 0){
				result = s.compare(this.transition.getName(), o.transition.getName());
			}
			
			return result;
		}
	}

	public int getNumberOfTransitions() {
		return transitions.size();
	}
}
