package dk.aau.cs.gui.components;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SpringLayout;

public class MultiSplitPane extends JPanel {
	
	boolean[] isComponentShown;
	boolean isTop = false;
	boolean isBottom = false;
	
	int numberOfComponents;
	JSplitPane top = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	JSplitPane bottom = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	JSplitPane outer = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	
	public MultiSplitPane() {
		super(new GridBagLayout());
		numberOfComponents = 0;
		
		isComponentShown = new boolean[4];
		for(int i = 0; i<isComponentShown.length; i++){
			isComponentShown[i] = false;
		}
		
		top.setResizeWeight(0.5);
		bottom.setResizeWeight(0.5);
		outer.setResizeWeight(0.5);
		
		top.setBorder(null);
		bottom.setBorder(null);
		outer.setBorder(null);
		
		top.setContinuousLayout(true);
		bottom.setContinuousLayout(true);
		outer.setContinuousLayout(true);
		
		top.setOneTouchExpandable(true);
		bottom.setOneTouchExpandable(true);
		outer.setOneTouchExpandable(true);
		
		outer.setTopComponent(top);
		outer.setBottomComponent(bottom);
		makeTopElement(outer);
	}
	
	public Component add(Component comp, int position) {

		switch (position) {
		case 0:
			if(!isComponentShown[0]){
				top.setTopComponent(comp);
				top.setDividerLocation(0.5);
				if(!isComponentShown[1] && (isComponentShown[2] || isComponentShown[3])){
					outer.setDividerLocation(0.5);
				}
				isComponentShown[0] = true;
			}
			break;
		case 1:
			if(!isComponentShown[1]){
				top.setBottomComponent(comp);
				isComponentShown[1] = true;
				top.setDividerLocation(0.5);
				if(!isComponentShown[0] && (isComponentShown[2] || isComponentShown[3])){
					outer.setDividerLocation(0.5);
				}
			}
			break;
		case 2:
			if(!isComponentShown[2]){
				bottom.setTopComponent(comp);
				isComponentShown[2] = true;
				bottom.setDividerLocation(0.5);
				if(!isComponentShown[3] && (isComponentShown[0] || isComponentShown[1])){
					outer.setDividerLocation(0.5);
				}
			}
			break;
		case 3:
			if(!isComponentShown[3]){
				bottom.setBottomComponent(comp);
				isComponentShown[3] = true;
				bottom.setDividerLocation(0.5);
				if(!isComponentShown[2] && (isComponentShown[0] || isComponentShown[1])){
					outer.setDividerLocation(0.5);
				}
			}
			break;
		default:
			break;
		}
		
		this.validate();
		this.repaint();
		return comp;
	}
	
	public void removeComponent(int position){
		switch (position) {
		case 0:
			top.getTopComponent().setVisible(false);
			isComponentShown[0] = false;
			break;
		case 1:
			top.getBottomComponent().setVisible(false);
			isComponentShown[1] = false;
			break;
		case 2:
			bottom.getBottomComponent().setVisible(false);
			isComponentShown[2] = false;
			break;
		case 3:
			top.getBottomComponent().setVisible(false);
			isComponentShown[3] = false;
			break;
		default:
			break;
		}
		
		this.validate();
		this.repaint();
	}
	
	private void makeTopElement(Component comp){
		if(super.getComponentCount() != 0){
			super.remove(0);
		}
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		super.add(comp);
	}
}
