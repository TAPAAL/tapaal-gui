package dk.aau.cs.gui.components;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.GraphicAttribute;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import dk.aau.cs.model.tapn.TimeInterval;
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
		lastSelected = (TransitionListItem)transitionsList.getSelectedValue();
		transitions.clear();
	}

	public void reInitDone(){
		updateFireButton();
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

	public void addTransition(Template template, Transition transition){
		TransitionListItem item = new TransitionListItem(transition, template);

		transition.isBlueTransition();
		if(!transitions.contains(item)){
			transitions.addElement(item);
		}
	}

	public void removeTransition(Template template, Transition transition){
		TransitionListItem item = new TransitionListItem(transition, template);
		transitions.removeElement(item);
	}

	public static final String FIRE_BUTTON_DEACTIVATED_TOOL_TIP = "No transitions are enabled";
	public static final String FIRE_BUTTON_ENABLED_TOOL_TIP = "Press to fire the selected transition";

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
		TransitionListItem item = (TransitionListItem)transitionsList.getSelectedValue();

		if(item != null) {
			if(item.getTransition().isEnabled(true)){
				CreateGui.getAnimator().fireTransition(((TimedTransitionComponent)item.getTransition()).underlyingTransition());
			} else {
				CreateGui.getAnimator().dFireTransition(((TimedTransitionComponent)item.getTransition()).underlyingTransition());
			}
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

			String result = getTransition().getName()  + (transition.getDInterval() == null && showIntervals ? 
					"" : " " + transition.getDInterval().toString());
 
			if(isShared()){
				result += " (shared)";
			} else {
				result = getTemplate() + "." + result;
			}

			return "<html><font color=" + (transition.highlighted ? "red" : "blue") + ">" + result + "</font></html>";
		}
		
		public String toString(){
			return toString(true);
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
			if(this.transition.isEnabled() == o.transition.isEnabled()){
				if(this.isShared() == o.isShared()){
					//if the transitions is from different templates - don't change the order
					if(this .template != o.template){
						return 0;
					} else {
						return compareToString(o);
					}
				} else {
					return this.isShared() ?  -1 : 1;
				}
			} else {
				return this.transition.isEnabled() ? -1 : 1;
			}
		}

		private int compareToString(TransitionListItem o){
			StringComparator s = new StringComparator();
			return s.compare(this.transition.getName(), o.transition.getName());
		}
	}

	//This class creates the stippled line shown between the enabled transitions and the blue transitions
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
}
