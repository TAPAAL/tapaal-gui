package dk.aau.cs.gui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Random;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import dk.aau.cs.util.IntervalOperations;
import dk.aau.cs.util.StringComparator;

import pipe.dataLayer.Template;
import pipe.gui.CreateGui;
import pipe.gui.graphicElements.Transition;
import pipe.gui.graphicElements.tapn.TimedTransitionComponent;
//TODO clean up!!! 
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
	TransitionListItem lastSelected;

	public void initPanel(){
		transitions = new DefaultListModel();
		transitionsList = new JList(transitions);
		transitionsList.setCellRenderer(new EnabledTransitionListCellRenderer());

		transitionsList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2){
					fireSelectedTransition();
					CreateGui.getApp().setRandomAnimationMode(false);
				}
				
				if(e.getClickCount() == 1){
					TransitionListItem highlightedItem = (TransitionListItem)transitionsList.getSelectedValue();
					if(highlightedItem != null){
						highlightedItem.getTransition().blink();
					}
				}
			}
		});

		scrollPane = new JScrollPane(transitionsList);

		this.add(scrollPane, BorderLayout.CENTER);
	}
	
	public void startReInit(){
		lastSelected = (TransitionListItem)transitionsList.getSelectedValue();
		transitions.clear();
	}

	public void reInitDone(){
		if(CreateGui.getCurrentTab().getDelayEnabledTransitionControl().isRandomTransitionMode()){
			selectRandom();
			return;
		}
		//Trick to make the "splitter" appear
		//transitions.addElement(new SplitterListItem());
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

		transition.isDelayEnabled();
		if(!transitions.contains(item)){
			transitions.addElement(item);
		}
	}

	public void removeTransition(Template template, Transition transition){
		TransitionListItem item = new TransitionListItem(transition, template);
		transitions.removeElement(item);
	}

	public void fireSelectedTransition(){
		TransitionListItem item = (TransitionListItem)transitionsList.getSelectedValue();

		if(item != null) {
			CreateGui.getAnimator().dFireTransition(((TimedTransitionComponent)item.getTransition()).underlyingTransition());
		}
	}

	interface ListItem extends Comparable<ListItem>{}

	class SplitterListItem implements ListItem{

		@Override
		public int compareTo(ListItem o) {
			if(o instanceof TransitionListItem){
				return o.compareTo(this);
			} else {
				return 0;
			}
		}

		@Override
		public String toString() {
			return "_";
		}

	}

	class TransitionListItem implements ListItem{
		private Transition transition;
		private Template template;

		public TransitionListItem(Transition transition, Template template){
			this.transition = transition;
			this.template = template;
		}

		public String toString(boolean showIntervals) {

			String interval = transition.getDInterval() == null || !showIntervals ? 
					"" : transition.getDInterval().toString() + " ";
			
			String transitionName = getTransition().getName(); 
			if(isShared()){
				transitionName +=  " (shared)";
			} else {
				transitionName = getTemplate() + "." + transitionName;
			}
			String result = interval + transitionName;

			return result;
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
				return toString().equals(((TransitionListItem)obj).toString());
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

		public int compareTo(ListItem o) {
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
			if(result == 0 && this.transition.getDInterval().IsLowerBoundNonStrict() != o.transition.getDInterval().IsLowerBoundNonStrict()){
				if(this.transition.getDInterval().IsLowerBoundNonStrict()){
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

	//This class creates the stippled line shown between the enabled transitions and the delay-enabled transitions
	class EnabledTransitionListCellRenderer extends DefaultListCellRenderer{

		private static final long serialVersionUID = -835675414373311136L;

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			if(value != null && value instanceof SplitterListItem){
				JLabel separator = new JLabel();
				separator.setBorder(new DashBorder());
				separator.setPreferredSize(new Dimension(1, 1));
				return separator;
			} else {
				return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			}
		}

		class DashBorder implements Border {
			private final Insets	insets	= new Insets(1, 1, 1, 1);
			private final int	 length	= 5;
			private final int	 space	= 3;
			public boolean isBorderOpaque() {
				return false;
			}
			public void paintBorder(Component c, Graphics g, int x, int y,
					int width, int height) {
				g.setColor(Color.BLACK);
				// --- draw horizontal ---
				for (int i = 0; i < width; i += length) {
					g.drawLine(i, y, i + length, y);
					g.drawLine(i, height - 1, i + length, height - 1);
					i += space;
				}
				// --- draw vertical ---
				for (int i = 0; i < height; i += length) {
					g.drawLine(0, i, 0, i + length);
					g.drawLine(width - 1, i, width - 1, i + length);
					i += space;
				}
			}
			public Insets getBorderInsets(Component c) {
				return insets;
			}
		}
	}

	public int getNumberOfTransitions() {
		return transitions.size();
	}
}
