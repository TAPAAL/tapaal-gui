package pipe.gui;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.File;
import java.util.Comparator;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.verification.*;
import dk.aau.cs.Messenger;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTrace;
import dk.aau.cs.util.MemoryMonitor;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.util.VerificationCallback;
import pipe.dataLayer.TAPNQuery;

public class RunVerification extends RunVerificationBase {	
	private final IconSelector iconSelector;
	private final VerificationCallback callback;
	public RunVerification(ModelChecker modelChecker, IconSelector selector, Messenger messenger, VerificationCallback callback, String reducedNetFilePath, boolean reduceNetOnly) {
		super(modelChecker, messenger, reducedNetFilePath, reduceNetOnly, null);
		iconSelector = selector;
		this.callback = callback;
	}

    public RunVerification(ModelChecker modelChecker, IconSelector selector, Messenger messenger, VerificationCallback callback) {
        this(modelChecker, selector, messenger, callback, null, false);
    }
	
	public RunVerification(ModelChecker modelChecker, IconSelector selector, Messenger messenger) {
		this(modelChecker, selector, messenger, null, null, false);
	}

	@Override
	protected boolean showResult(VerificationResult<TAPNNetworkTrace> result) {
        if (reduceNetOnly) {
            //If the engine is only called to produce a reduced net, it will fail, but no error message should be shown
            if (result != null && result.stats().transitionsCount() == 0 && result.stats().placeBoundCount() == 0) {
                JOptionPane.showMessageDialog(
                    CreateGui.getApp(),
                    createMessagePanel(result),
                    "Verification Result",
                    JOptionPane.INFORMATION_MESSAGE,
                    iconSelector.getIconFor(result)
                );
            }
        } else if (result != null && !result.error()) {
			if(callback != null){
				callback.run(result);
			}else{
				JOptionPane.showMessageDialog(
						CreateGui.getApp(),
						createMessagePanel(result),
						"Verification Result",
						JOptionPane.INFORMATION_MESSAGE,
						iconSelector.getIconFor(result)
				);
	
				if (!reducedNetOpened && result.getTrace() != null) {
					CreateGui.getAnimator().setTrace(result.getTrace());
				}
			}

		} else {
			
			//Check if the is something like 
			//verifyta: relocation_error:
			///usr/lib32/libnss_msdn4_minimal.so.2 symbol strlen, 
			//version GLIB_2.0 not defined in file libc.so.6 with
			//link time reference
			//is the error as this (often) means the possibility for a uppaal licence key error
			
			String extraInformation = "";
			
			if (result != null && (result.errorMessage().contains("relocation") || result.errorMessage().toLowerCase().contains("internet connection is required for activation"))){
				
				extraInformation = "We detected an error that often arises when UPPAAL is missing a valid Licence file.\n" +
						"Open the UPPAAL GUI while connected to the internet to correct this problem.";
				
			}
			
			String message = "An error occured during the verification." +
			System.getProperty("line.separator") + 	
			System.getProperty("line.separator");
			
			if (!extraInformation.equals("")){
				message += extraInformation +			
				System.getProperty("line.separator") + 	
				System.getProperty("line.separator");
			}
			
			message += "Model checker output:\n" + result.errorMessage();
			
			messenger.displayWrappedErrorMessage(message,"Error during verification");

		}
		return false;
	}

	private String toHTML(String string){
		StringBuilder buffer = new StringBuilder("<html>");
		buffer.append(string.replace(System.getProperty("line.separator"), "<br/>"));
		buffer.append("</html>");
		return buffer.toString();
	}

	private void displayStats(JPanel panel, String stats, String[] explanations){
        String[] statsStrings = stats.split(System.getProperty("line.separator"));
        for (int i = 0; i < statsStrings.length; i++) {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = i+1;
            gbc.insets = new Insets(0,0,0,0);
            gbc.anchor = GridBagConstraints.WEST;
            JLabel statLabel = new JLabel(statsStrings[i]);
            statLabel.setToolTipText(explanations[i]);
            panel.add(statLabel, gbc);
        }

    }

	private JPanel createStatisticsPanel(final VerificationResult<TAPNNetworkTrace> result, boolean transitionPanel) {
		JPanel headLinePanel = new JPanel(new GridBagLayout());
		final JPanel fullPanel = new JPanel(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(15, 0, 15, 15);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.weightx = 2;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		if (transitionPanel) {
			headLinePanel.add(new JLabel(toHTML("Number of times transitions were enabled during the search.\n"), JLabel.LEFT), gbc);
		} else {
			headLinePanel.add(new JLabel(toHTML("Maximum number of tokens per place achieved during the search.\n"), JLabel.LEFT), gbc);
		}

		//Setup table
		TableModel model;

		if (transitionPanel) {
			String[] columnNames = {"Count", "Transition"};
			Object[][] data = extractArrayFromTransitionStatistics(result);
			model = new NonEditableModel(data, columnNames);
		} else {
			String[] columnNames = {"Max Tokens", "Place"};
			Object[][] data = extractArrayFromPlaceBoundStatistics(result);
			model = new NonEditableModel(data, columnNames);
		}
		JTable table = new JTable(model);

		Comparator<Object> comparator = (oo1, oo2) -> {
			boolean isFirstNumeric, isSecondNumeric;
			String o1 = oo1.toString(), o2 = oo2.toString();
			isFirstNumeric = o1.matches("\\d+");
			isSecondNumeric = o2.matches("\\d+");
			if (isFirstNumeric) {
				if (isSecondNumeric) {
					return Integer.valueOf(o2).compareTo(Integer.valueOf(o1));
				} else {
					return -1; // numbers always smaller than letters
				}
			} else {
				if (isSecondNumeric) {
					return 1; // numbers always smaller than letters
				}
			}
			return 0; // we do not compare strings (it is the same all the time)
		};

		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(table.getModel());
		sorter.setComparator(0, comparator);
		table.setRowSorter(sorter);

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);


		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		fullPanel.add(headLinePanel, gbc);

		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		fullPanel.add(scrollPane, gbc);

		// Make window resizeable
		fullPanel.addHierarchyListener(new HierarchyListener() {
			public void hierarchyChanged(HierarchyEvent e) {
				//when the hierarchy changes get the ancestor for the message
				Window window = SwingUtilities.getWindowAncestor(fullPanel);
				//check to see if the ancestor is an instance of Dialog and isn't resizable
				if (window instanceof Dialog) {
					Dialog dialog = (Dialog) window;
					if (!dialog.isResizable()) {
						//set resizable to true
						dialog.setResizable(true);
						dialog.setMinimumSize(new Dimension(350, 300));
						dialog.setPreferredSize(new Dimension(600, 400));
					}
				}
			}
		});

		return fullPanel;
	}

    private JPanel createRawQueryPanel(final String rawOutput) {
        final JPanel fullPanel = new JPanel(new GridBagLayout());

        JTextArea rawQueryLabel = new JTextArea(rawOutput);
        rawQueryLabel.setEditable(false); // set textArea non-editable
        JScrollPane scroll = new JScrollPane(rawQueryLabel);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setPreferredSize(new Dimension(640,400));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        fullPanel.add(scroll, gbc);

        // Make window resizeable
        fullPanel.addHierarchyListener(new HierarchyListener() {
            public void hierarchyChanged(HierarchyEvent e) {
                //when the hierarchy changes get the ancestor for the message
                Window window = SwingUtilities.getWindowAncestor(fullPanel);
                //check to see if the ancestor is an instance of Dialog and isn't resizable
                if (window instanceof Dialog) {
                    Dialog dialog = (Dialog) window;
                    dialog.setMinimumSize(dialog.getPreferredSize());
                    if (!dialog.isResizable()) {
                        //set resizable to true
                        dialog.setResizable(true);
                    }
                }
            }
        });

        return fullPanel;
    }

	private Object[][] extractArrayFromTransitionStatistics(final VerificationResult<TAPNNetworkTrace> result) {
		List<Tuple<String, Integer>> transistionStats = result.getTransitionStatistics();
		Object[][] out = new Object[transistionStats.size()][2];
		for (int i = 0; i < transistionStats.size(); i++) {
			Object[] line = {(transistionStats.get(i).value2() == -1 ? "unknown" : transistionStats.get(i).value2()), transistionStats.get(i).value1()};
			out[i] = line;
		}
		return out;
	}
        
	private Object[][] extractArrayFromPlaceBoundStatistics(final VerificationResult<TAPNNetworkTrace> result) {
		List<Tuple<String,Integer>> placeBoundStats = result.getPlaceBoundStatistics();
		Object[][] out = new Object[placeBoundStats.size()][2];
		for (int i=0;i<placeBoundStats.size();i++) {
			Object[] line = {(placeBoundStats.get(i).value2()==-1 ? "unknown" : placeBoundStats.get(i).value2()),placeBoundStats.get(i).value1()};
			out[i] = line;
		}
		return out;
        }
        
	private JPanel createMessagePanel(final VerificationResult<TAPNNetworkTrace> result) {
		final JPanel panel = new JPanel(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(0,0,15,0);
		gbc.anchor = GridBagConstraints.WEST;		
		panel.add(new JLabel(toHTML(result.getResultString())), gbc);

		// TODO remove this when the engine outputs statistics
		boolean isCTLQuery = result.getQueryResult().isCTL;

		if(modelChecker.supportsStats() && !result.isSolvedUsingStateEquation() && !isCTLQuery){

            displayStats(panel, result.getStatsAsString(), modelChecker.getStatsExplanations());
			
			if(!result.getTransitionStatistics().isEmpty()){
				JButton transitionStatsButton = new JButton("Transition Statistics");
				transitionStatsButton.addActionListener(arg0 -> JOptionPane.showMessageDialog(panel,createStatisticsPanel(result,true) , "Transition Statistics", JOptionPane.INFORMATION_MESSAGE));
				gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.gridy = 4;
				gbc.insets = new Insets(10,0,10,0);
				gbc.anchor = GridBagConstraints.WEST;
				panel.add(transitionStatsButton, gbc);
			}
			if(!result.getPlaceBoundStatistics().isEmpty()){
				JButton placeStatsButton = new JButton("Place-Bound Statistics");
				placeStatsButton.addActionListener(arg0 -> JOptionPane.showMessageDialog(panel,createStatisticsPanel(result,false) , "Place-Bound Statistics", JOptionPane.INFORMATION_MESSAGE));
				gbc = new GridBagConstraints();
				gbc.gridx = 1;
				gbc.gridy = 4;
				gbc.insets = new Insets(10,0,10,0);
				gbc.anchor = GridBagConstraints.WEST;
				panel.add(placeStatsButton, gbc);
			}
			
			if(!result.getReductionResultAsString().isEmpty()){
				JLabel reductionStatsLabel = new JLabel(toHTML(result.getReductionResultAsString()));
				gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.gridy = 6;
				gbc.insets = new Insets(0,0,20,-90);
				gbc.anchor = GridBagConstraints.WEST;

				panel.add(reductionStatsLabel, gbc);

				if(result.reductionRulesApplied()){
                    JButton openReducedButton = new JButton("Open reduced net");
                    openReducedButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            openReducedButton.setEnabled(false);
                            reducedNetOpened = true;
                            File reducedNetFile = new File(reducedNetFilePath);

                            if(reducedNetFile.exists() && reducedNetFile.isFile() && reducedNetFile.canRead()){
                                try {
                                    TabContent reducedNetTab = TabContent.createNewTabFromPNMLFile(reducedNetFile);
                                    reducedNetTab.setInitialName("reduced-" + CreateGui.getAppGui().getCurrentTabName());
                                    TAPNQuery convertedQuery = dataLayerQuery.convertPropertyForReducedNet(reducedNetTab.currentTemplate().toString());
                                    reducedNetTab.addQuery(convertedQuery);
                                    CreateGui.openNewTabFromStream(reducedNetTab);
                                } catch (Exception e1){
                                    JOptionPane.showMessageDialog(CreateGui.getApp(),
                                        e1.getMessage(),
                                        "Error loading reduced net file",
                                        JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        }
                    });
                    gbc = new GridBagConstraints();
                    gbc.gridx = 0;
                    gbc.gridy = 5;
                    gbc.insets = new Insets(0,0,10,0);
                    gbc.anchor = GridBagConstraints.WEST;
                    panel.add(openReducedButton, gbc);
                }
			}
            if (result.getRawOutput() != null) {
                JButton showRawQueryButton = new JButton("Show raw query results");
                showRawQueryButton.addActionListener(arg0 -> {
                        JOptionPane.showMessageDialog(panel, createRawQueryPanel(result.getRawOutput()), "Raw query results", JOptionPane.INFORMATION_MESSAGE);
                });
                gbc = new GridBagConstraints();
                gbc.gridx = 1;
                gbc.gridy = 5;
                gbc.insets = new Insets(0,0,10,0);
                gbc.anchor = GridBagConstraints.WEST;
                panel.add(showRawQueryButton, gbc);
            }
		} else if (modelChecker.supportsStats() && !result.isSolvedUsingStateEquation() && isCTLQuery){
            displayStats(panel, result.getCTLStatsAsString(), modelChecker.getStatsExplanations());

		}

		if(result.isSolvedUsingStateEquation()){
			gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 6;
			gbc.insets = new Insets(0,0,15,0);
			gbc.anchor = GridBagConstraints.WEST;
			panel.add(new JLabel(toHTML("The query was resolved using state equations.")), gbc);
		}
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 7;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.WEST;
		panel.add(new JLabel(result.getVerificationTimeString()), gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 8;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.WEST;
		panel.add(new JLabel("Estimated memory usage: "+MemoryMonitor.getPeakMemory()), gbc);
		
		//Show discrete semantics warning if needed
		QueryResult queryResult = result.getQueryResult();
		if(((queryResult.hasDeadlock() && queryResult.queryType() == QueryType.EF && !queryResult.isQuerySatisfied()) || 
                   (queryResult.hasDeadlock() && queryResult.queryType() == QueryType.AG && queryResult.isQuerySatisfied()) || 
                   (queryResult.queryType() == QueryType.EG && !queryResult.isQuerySatisfied()) || 
		   (queryResult.queryType() == QueryType.AF && queryResult.isQuerySatisfied())) 
				&& modelChecker.useDiscreteSemantics()){
			gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 11;
			gbc.gridwidth = 2;
			gbc.anchor = GridBagConstraints.WEST;
			panel.add(new JLabel("<html><font color=red>The verification answer is guaranteed for<br/>the discrete semantics only (integer delays).</font></html>"), gbc);
		}
		
		return panel;
	}
	
	private static class NonEditableModel extends DefaultTableModel {

		NonEditableModel(Object[][] data, String[] columnNames) {
	        super(data, columnNames);
	    }
	    @Override
	    public boolean isCellEditable(int row, int column) {
	        return false;
	    }
	}
}
