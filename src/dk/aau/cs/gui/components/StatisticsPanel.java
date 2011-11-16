package dk.aau.cs.gui.components;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import pipe.gui.*;
import pipe.gui.graphicElements.tapn.TimedTransitionComponent;
import pipe.gui.undo.DeleteTimedTransitionCommand;
import pipe.gui.undo.UndoManager;
import pipe.dataLayer.*;

import dk.aau.cs.gui.TabContent;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.util.Require;

public class StatisticsPanel extends JPanel{
	private final int topBottomMargin = 3;
	private final int rightMargin = 10;
	
	private JButton removeOrphans;
	private JButton checkBoundedness;
	
	public StatisticsPanel(Object[][] contents, Object[] headLines) {
		super(new GridBagLayout());
		
		Require.that(contents[0].length == headLines.length, "There should be the same number of headlines and columns");
		
		addRow(headLines, 0, true);
		
		//Add the content - make space for separators
		for(int i = 0; i < contents.length; i++){
			addRow(contents[i], (i+1)*2, false);
		}
		
		//Add the separators
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = headLines.length;
		JSeparator jSep;
		gbc.insets = new Insets(topBottomMargin, 0, topBottomMargin, rightMargin);
		for(int i = 3; i<contents.length*2+2; i += 2){
			jSep = new JSeparator();
			jSep.setPreferredSize(new Dimension(1, 3));
			gbc.gridy = i;
			this.add(jSep, gbc);
		}
		
		//Add buttons
		addButtons(headLines.length, contents.length*2);
		
	}
	
	private void addRow(Object[] row, int rowNumber, boolean isHeadLine){
		GridBagConstraints gbc;
		for(int i = 0; i < row.length; i++){
			gbc = new GridBagConstraints();
			gbc.gridx = i;
			gbc.gridy = rowNumber;
			gbc.anchor = i == 0 ? GridBagConstraints.WEST : GridBagConstraints.EAST;
			gbc.insets = new Insets(0, 0, 0, rightMargin);
			
			if(row[i] != null){
				if(isHeadLine){
					JLabel current = new JLabel(row[i].toString());
					current.setFont(new Font(current.getFont().getName(), Font.BOLD, current.getFont().getSize()));
					this.add(current, gbc);
				} else {
					JLabel current = new JLabel(row[i].toString());
					this.add(current, gbc);
				}
			}
		}
	}
	
	private void addButtons(int gridWidth, int gridHeight){
		removeOrphans = new JButton("Remove orphan transitions");
		removeOrphans.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				TabContent tab = CreateGui.getCurrentTab();
				Iterable<Template> templates = tab.allTemplates();
				UndoManager undoManager = CreateGui.getDrawingSurface().getUndoManager();
				for(Template template : templates){
					List<TimedTransition> orphans = template.model().getOrphanTransitions();
					for(TimedTransition trans : orphans){
						//Shared transistions makes truble
						if(!trans.isShared()){
							TimedTransitionComponent t = (TimedTransitionComponent)template.guiModel().getTransitionByName(trans.name());
							undoManager.addEdit(new DeleteTimedTransitionCommand(t, t.underlyingTransition().model(), template.guiModel(), tab.drawingSurface()));
							t.delete();
						}
					}
				}
				
				tab.drawingSurface().repaint();
			}
		});
		
		checkBoundedness = new JButton("Check boundedness");
		
		JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonsPanel.add(removeOrphans);
		
		buttonsPanel.add(checkBoundedness);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = gridHeight + 2;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = gridWidth;
		this.add(buttonsPanel, gbc);
	}
}
