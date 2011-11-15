package dk.aau.cs.gui.components;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import dk.aau.cs.util.Require;

public class StatisticsPanel extends JPanel{
	private final int topBottomMargin = 3;
	private final int rightMargin = 10;
	
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
		for(int i = 3; i<contents.length*2; i += 2){
			jSep = new JSeparator();
			jSep.setPreferredSize(new Dimension(1, 3));
			gbc.gridy = i;
			this.add(jSep, gbc);
		}
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
}
