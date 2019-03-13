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
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import pipe.gui.*;
import pipe.gui.graphicElements.tapn.TimedTransitionComponent;
import pipe.gui.undo.DeleteTimedTransitionCommand;
import pipe.gui.undo.UndoManager;
import pipe.dataLayer.*;

import dk.aau.cs.gui.TabContent;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.tapn.TimedTransition;

public class StatisticsPanel extends JPanel{

	private static final long serialVersionUID = 5613743579411748200L;
	
	private final int topBottomMargin = 3;
	private final int rightMargin = 10;
	
	private JButton removeOrphans;
    private static final String REMOVE_ORPHANS_TOOL_TIP = "<html>Remove all orphan transitions<br /> (transitions with no arcs attached)<br /> in all components</html>";
    private static final String DIALOG_TITLE = "Statistics";
	
	String[] headLines = {"", "Shown component", "Active components", "All components"};
	
	private static JDialog dialog;
	private Object[][] contents;

	private StatisticsPanel(Object[][] statistics) {
		super(new GridBagLayout());

		this.contents = statistics;

		init();
	}
	
	public static void showStatisticsPanel(Object[][] statistics){
		StatisticsPanel panel = new StatisticsPanel(statistics);
		
		JOptionPane optionPane = new JOptionPane(panel, JOptionPane.INFORMATION_MESSAGE);
		
		dialog = optionPane.createDialog(DIALOG_TITLE);
		
		dialog.pack();
		dialog.setVisible(true);
	}

	private JPanel init() {
		
		addRow(headLines, 0, true);
		
		//Add the content - make space for separators (except the orphan transitions
		for(int i = 0; i < contents.length - 1; i++){
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
		for(int i = 3; i< contents.length*2-2; i += 2){
			jSep = new JSeparator();
			jSep.setPreferredSize(new Dimension(1, 3));
			gbc.gridy = i;
			this.add(jSep, gbc);
		}
		
		//If any orphan transitions - add them
		boolean orphanTransitions = false;
		for(int i = 1; i< contents[contents.length-1].length; i++){
			if(!contents[contents.length-1][i].toString().equals("0")){
				orphanTransitions = true;
			}
		}
		
		if(orphanTransitions){
			jSep = new JSeparator();
			jSep.setPreferredSize(new Dimension(1, 3));
			gbc.gridy = contents.length*2-1;
			this.add(jSep, gbc);
			addRow(contents[contents.length-1], contents.length*2, false);
			jSep = new JSeparator();
			jSep.setPreferredSize(new Dimension(1, 3));
			gbc.gridy = contents.length*2+1;
			this.add(jSep, gbc);
			addButtons(headLines.length, contents.length*2);
		}
		return null;
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
		removeOrphans.setToolTipText(REMOVE_ORPHANS_TOOL_TIP);
		final JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		removeOrphans.addActionListener(new ActionListener() {
			
		
			public void actionPerformed(ActionEvent e) {
				TabContent tab = CreateGui.getCurrentTab();
				Iterable<Template> templates = tab.allTemplates();
				
				UndoManager undoManager = CreateGui.getDrawingSurface().getUndoManager();
				boolean first = true;
				for(Template template : templates){
					List<TimedTransition> orphans = template.model().getOrphanTransitions();
					for(TimedTransition trans : orphans){
						TimedTransitionComponent t = (TimedTransitionComponent)template.guiModel().getTransitionByName(trans.name());
						Command cmd = new DeleteTimedTransitionCommand(t, t.underlyingTransition().model(), template.guiModel(), tab.drawingSurface());
						t.delete();
						if(first){
							undoManager.addNewEdit(cmd);
							first = false;
						} else {
							undoManager.addEdit(cmd);
						}
					}
				}
				
				
				tab.drawingSurface().repaint();
				
				StatisticsPanel.this.removeAll();
				StatisticsPanel.this.init();
				
				JOptionPane optionPane = new JOptionPane(StatisticsPanel.this, JOptionPane.INFORMATION_MESSAGE);
				
				dialog.dispose();
				
				dialog = optionPane.createDialog(DIALOG_TITLE);
				
				dialog.pack();
				dialog.setVisible(true);
			}
		});
		
		buttonsPanel.add(removeOrphans);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = gridHeight + 2;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = gridWidth;
		this.add(buttonsPanel, gbc);
	}
}
