package pipe.gui.widgets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.colors.ColoredTimedPlace;
import pipe.gui.undo.UndoManager;

public class RemoveTokenPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2685053954985939625L;
	private JRootPane rootPane;
	private ColoredTimedPlace place;
	private UndoManager undoManager;
	private TimedArcPetriNetNetwork model;
	private JPanel tokenPanel;
	private JLabel tokenHelpLabel;
	private JLabel tokenHelpLabel2;
	private TokenTableModel tokenTableModel;
	private JTable tokenTable;
	private JPanel buttonPanel;
	private JButton okButton;
	private JButton cancelButton;
	
	public RemoveTokenPanel(JRootPane rootPane, ColoredTimedPlace place, TimedArcPetriNetNetwork model,UndoManager undoManager){
		this.rootPane = rootPane;
		this.place = place;
		this.undoManager = undoManager;
		this.model = model;
		
		initComponents();
	}
	
	private void initComponents(){
		setLayout(new GridBagLayout());
		initTokenPanel();
		initButtonPanel();
	}

	private void initButtonPanel() {
		java.awt.GridBagConstraints gridBagConstraints;
		buttonPanel = new javax.swing.JPanel();
		buttonPanel.setLayout(new java.awt.GridBagLayout());

		okButton = new javax.swing.JButton();
		okButton.setText("OK");
		okButton.setMaximumSize(new java.awt.Dimension(75, 25));
		okButton.setMinimumSize(new java.awt.Dimension(75, 25));
		okButton.setPreferredSize(new java.awt.Dimension(75, 25));
		rootPane.setDefaultButton(okButton);
		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				if(tokenTable.getSelectedRowCount() > 0){
					int[] rows = tokenTable.getSelectedRows();
					int[] modelIndices = new int[rows.length];
					for(int i = 0; i < rows.length; i++){
						modelIndices[i] = tokenTable.convertRowIndexToModel(rows[i]);
					}
					
					for(int i = modelIndices.length-1; i >= 0; i--){
						tokenTableModel.removeColoredToken(modelIndices[i]);
					}
					
					undoManager.addNewEdit(place.setColoredTokens(tokenTableModel.getTokens()));
					model.buildConstraints();
				}
				exit();
			}
		});
	
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 9);
		buttonPanel.add(okButton, gridBagConstraints);

		cancelButton = new javax.swing.JButton();
		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				exit();
			}
		});

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 10);
		buttonPanel.add(cancelButton, gridBagConstraints);
		
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		add(buttonPanel, gridBagConstraints);
	}

	protected void exit() {
		rootPane.getParent().setVisible(false);
	}

	private void initTokenPanel() {
		tokenPanel = new JPanel(new GridBagLayout());
		tokenPanel.setBorder(BorderFactory.createTitledBorder("Tokens"));

		tokenHelpLabel = new JLabel("Select the tokens to remove.");
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(3,3,0,3);
		tokenPanel.add(tokenHelpLabel, gbc);

		tokenHelpLabel2 = new JLabel("Hold CTRL to select multiple tokens.");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0,3,3,3);
		tokenPanel.add(tokenHelpLabel2, gbc);

		tokenTableModel = new TokenTableModel(place);
		tokenTable = new JTable(tokenTableModel);
		Dimension dims = new Dimension(150,133);
		tokenTable.setPreferredScrollableViewportSize(dims);

		DefaultTableCellRenderer render = new DefaultTableCellRenderer();
		render.setHorizontalAlignment(SwingConstants.RIGHT);
		tokenTable.getColumn("Value").setCellRenderer(render);
		DefaultTableCellRenderer notEditableRenderer = new DefaultTableCellRenderer();
		Color color = (Color)UIManager.get("TextField.disabledBackground");
		notEditableRenderer.setBackground(color);
		tokenTable.getColumn("Age").setCellRenderer(notEditableRenderer);
		tokenTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		JScrollPane pane = new JScrollPane(tokenTable);
		pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.BOTH;
		//gbc.insets = new Insets(3,3,3,3);
		tokenPanel.add(pane, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.VERTICAL;
		//gridBagConstraints.insets = new Insets(0,0,0,5);
		add(tokenPanel, gbc);
	}	
}
