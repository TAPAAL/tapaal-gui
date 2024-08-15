package net.tapaal.gui.petrinet.verification;

import dk.aau.cs.Messenger;
import dk.aau.cs.TCTL.TCTLAFNode;
import dk.aau.cs.TCTL.TCTLAGNode;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.TCTL.TCTLEGNode;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTrace;
import dk.aau.cs.TCTL.*;
import dk.aau.cs.io.LoadedModel;
import dk.aau.cs.io.PNMLoader;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTrace;
import dk.aau.cs.model.tapn.simulation.TimedArcPetriNetTrace;
import dk.aau.cs.util.FormatException;
import dk.aau.cs.util.MemoryMonitor;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.util.VerificationCallback;
import dk.aau.cs.verification.*;
import dk.aau.cs.verification.VerifyTAPN.ColorBindingParser;
import net.tapaal.gui.petrinet.TAPNLens;
import net.tapaal.swinghelpers.GridBagHelper;
import pipe.gui.MessengerImpl;
import pipe.gui.TAPAALGUI;
import pipe.gui.graph.Graph;
import pipe.gui.graph.GraphDialog;
import pipe.gui.graph.GraphPoint;
import pipe.gui.petrinet.PetriNetTab;
import pipe.gui.petrinet.dataLayer.DataLayer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Comparator;

import static java.util.Objects.nonNull;
import static net.tapaal.swinghelpers.GridBagHelper.Anchor.WEST;

public class RunVerification extends RunVerificationBase {	
	private final IconSelector iconSelector;
	private final VerificationCallback callback;

	public RunVerification(ModelChecker modelChecker, IconSelector selector, Messenger messenger, VerificationCallback callback, HashMap<TimedArcPetriNet, DataLayer> guiModels, String reducedNetFilePath, boolean reduceNetOnly) {
		super(modelChecker, messenger, guiModels, reducedNetFilePath, reduceNetOnly, null);
		iconSelector = selector;
		this.callback = callback;
	}

    public RunVerification(ModelChecker modelChecker, IconSelector selector, Messenger messenger, VerificationCallback callback,HashMap<TimedArcPetriNet, DataLayer> guiModels) {
        this(modelChecker, selector, messenger, callback, guiModels, null, false);
    }

    public RunVerification(ModelChecker modelChecker, IconSelector selector, Messenger messenger) {
        this(modelChecker, selector, messenger, null, null, null, false);
    }

	@Override
	protected boolean showResult(VerificationResult<TAPNNetworkTrace> result) {
        if (reduceNetOnly) {
            //If the engine is only called to produce a reduced net, it will fail, but no error message should be shown
            if (result != null && result.stats().transitionsCount() == 0 && result.stats().placeBoundCount() == 0) {
                JOptionPane.showMessageDialog(
                    TAPAALGUI.getApp(),
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
						TAPAALGUI.getApp(),
						createMessagePanel(result),
						"Verification Result",
						JOptionPane.INFORMATION_MESSAGE,
						iconSelector.getIconFor(result)
				);

                if (options.traceOption() != TAPNQuery.TraceOption.NONE) {
                    if (!reducedNetOpened && nonNull(result.getTrace()) && nonNull(TAPAALGUI.getAnimator())) {
                        if ((lens != null && lens.isColored()) || model.isColored()) {
                            int dialogResult = JOptionPane.showConfirmDialog(null, "There is a trace that will be displayed in a new tab on the unfolded net/query.", "Open trace", JOptionPane.OK_CANCEL_OPTION);
                            if (dialogResult == JOptionPane.OK_OPTION) {
                                TAPAALGUI.openNewTabFromStream(result.getUnfoldedTab());
                            } else return false;
                        }
                        if (result.getTraceMap() == null) {
                            TAPAALGUI.getAnimator().setTrace(result.getTrace());
                        } else {
                            Map<String, TAPNNetworkTrace> traceMap = new HashMap<>();
                            for (String key : result.getTraceMap().keySet()) {
                                if (query.toString().contains(key)) {
                                    traceMap.put(key, result.getTraceMap().get(key));
                                }
                            }
                            TAPAALGUI.getAnimator().setTrace(result.getTrace(), traceMap);
                        }

                        if (result.getUnfoldedTab() != null) {
                            ColorBindingParser parser = new ColorBindingParser();
                            parser.addBindings(result.getUnfoldedTab().getModel(), result.getRawOutput());
                        }
                    } else {
                        if ((
                            //XXX: this is not complete, we need a better way to signal the engine could not create a trace
                            (query.getProperty() instanceof TCTLEFNode && result.isQuerySatisfied()) ||
                            (query.getProperty() instanceof TCTLAGNode && !result.isQuerySatisfied()) ||
                            (query.getProperty() instanceof TCTLEGNode && result.isQuerySatisfied()) ||
                            (query.getProperty() instanceof TCTLAFNode && !result.isQuerySatisfied()))
                        ) {
                            String message = "A trace exists but could not be generated by the engine.\n\n";
                            messenger.displayWrappedErrorMessage(message, "No trace generated");
                        }
                    }
                }


			}
		} else {
			//Check if the is something like
			//verifyta: relocation_error:
			///usr/lib32/libnss_msdn4_minimal.so.2 symbol strlen, 
			//version GLIB_2.0 not defined in file libc.so.6 with
			//link time reference
			//is the error as this (often) means the possibility for a uppaal licence key error

            if (((lens != null && lens.isColored()) || model.isColored())) {
                if (result != null && result.errorMessage().contains("Only weight=1")) {
                    String[] split1 = result.errorMessage().split("between ", 2);
                    String[] names;
                    String message = "The unfolding of this colored net created an unfolded P/T that contains weights on arcs.\n" +
                        "The verification of such a net is not supported by the continuous timed engine verifytapn.";
                    if (split1.length > 1) {
                        names = split1[1].split(" and | ", 2);
                        if (names != null && names.length > 1) {
                            String place = names[0];
                            String transition = names[1].strip();
                            message += "\nThe arc between" + place + " and " + transition + " is causing that the unfolded net is weighted.";
                        }
                    }
                    message += "\n\nThe problem can be also caused by the presence of an inhibitor arc.";
                    messenger.displayInfoMessage(message, "Could not verify the query");
                    return false;
                }
            }
			
			String extraInformation = "";
			if (result != null && (result.errorMessage().contains("relocation") || result.errorMessage().toLowerCase().contains("internet connection is required for activation"))){
				extraInformation = "We detected an error that often arises when UPPAAL is missing a valid Licence file.\n" +
						"Open the UPPAAL GUI while connected to the internet to correct this problem.";
			}
			
			String message = "An error occurred during the verification." +
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
        return
            "<html>" + string.replace(System.getProperty("line.separator"), "<br/>") + "</html>";
	}

	private int displayStats(JPanel panel, String stats, String[] explanations, int startOffset){
        String[] statsStrings = stats.split(System.getProperty("line.separator"));

        for (int i = 0; i < statsStrings.length; i++) {
            GridBagConstraints gbc = GridBagHelper.as(i / 4, (i%4)+startOffset, WEST, new Insets(0,0,0,0));
            JLabel statLabel = new JLabel(statsStrings[i]);
            if(explanations.length > i)
                statLabel.setToolTipText(explanations[i]);
            panel.add(statLabel, gbc);
        }
        return startOffset+statsStrings.length;
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
		List<Tuple<String, Integer>> transitionStats = result.getTransitionStatistics();
		Object[][] out = new Object[transitionStats.size()][2];
		for (int i = 0; i < transitionStats.size(); i++) {
			Object[] line = {(transitionStats.get(i).value2() == -1 ? "unknown" : transitionStats.get(i).value2()), transitionStats.get(i).value1()};
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

        if (result.getBound() != -1 && result.getBound() < result.getQueryResult().boundednessAnalysis().usedTokens() &&
                !result.getQueryResult().toString().toLowerCase().contains("only markings with")) {
            gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.WEST;
            panel.add(new JLabel("<html><br/><br/>Only markings with at most " + result.getBound() + " tokens were explored<br/><br/></html>"), gbc);
        }

		// TODO remove this when the engine outputs statistics
		boolean isCTLQuery = result.getQueryResult().isCTL;
        int rowOffset = 1;
        JButton showRawQueryButton = null;
		if(modelChecker.supportsStats() && !result.isSolvedUsingQuerySimplification() && !isCTLQuery){
            rowOffset = displayStats(panel, result.getStatsAsString(), modelChecker.getStatsExplanations(), 1);

            if(!result.getTransitionStatistics().isEmpty()) {
                if (!result.getTransitionStatistics().isEmpty()) {
                    JButton transitionStatsButton = new JButton("Transition Statistics");
                    transitionStatsButton.addActionListener(arg0 -> JOptionPane.showMessageDialog(panel, createStatisticsPanel(result, true), "Transition Statistics", JOptionPane.INFORMATION_MESSAGE));
                    gbc = GridBagHelper.as(0,4, WEST, new Insets(10, 0, 10, 0));
                    panel.add(transitionStatsButton, gbc);
                }
                if (!result.getPlaceBoundStatistics().isEmpty()) {
                    JButton placeStatsButton = new JButton("Place-Bound Statistics");
                    placeStatsButton.addActionListener(arg0 -> JOptionPane.showMessageDialog(panel, createStatisticsPanel(result, false), "Place-Bound Statistics", JOptionPane.INFORMATION_MESSAGE));
                    gbc = GridBagHelper.as(1,4, WEST, new Insets(10, 0, 10, 0));
                    panel.add(placeStatsButton, gbc);
                }
            }
            if (result.getRawOutput() != null) {
                showRawQueryButton = new JButton("Show raw query results");
                showRawQueryButton.addActionListener(arg0 -> JOptionPane.showMessageDialog(panel, createRawQueryPanel(result.getRawOutput()), "Raw query results", JOptionPane.INFORMATION_MESSAGE));
                gbc = GridBagHelper.as(1, 5, WEST, new Insets(0,0,10,0));
                panel.add(showRawQueryButton, gbc);
            }
			if(!result.getReductionResultAsString().isEmpty()){

                JLabel reductionStatsLabel = new JLabel(toHTML(result.getReductionResultAsString()));

				gbc = GridBagHelper.as(0,6, WEST, new Insets(0,0,10,-90));

				panel.add(reductionStatsLabel, gbc);

				if(result.reductionRulesApplied()){
                    JButton openReducedButton = new JButton("Open reduced net");
                    openReducedButton.addActionListener(e -> {
                        openReducedButton.setEnabled(false);
                        reducedNetOpened = true;

                        File reducedNetFile = new File(reducedNetFilePath);

                        if(reducedNetFile.exists() && reducedNetFile.isFile() && reducedNetFile.canRead()){
                            try {
                                PetriNetTab reducedNetTab = PetriNetTab.createNewTabFromPNMLFile(reducedNetFile);
                                //Ensure that a net was created by the query reduction
                                if(reducedNetTab.currentTemplate().guiModel().getPlaces().length > 0
                                    || reducedNetTab.currentTemplate().guiModel().getTransitions().length > 0){
                                    reducedNetTab.setInitialName("reduced-" + TAPAALGUI.getAppGui().getCurrentTabName());
                                    TAPNQuery convertedQuery = dataLayerQuery.convertPropertyForReducedNet(reducedNetTab.currentTemplate().toString());
                                    reducedNetTab.addQuery(convertedQuery);
                                    TAPAALGUI.openNewTabFromStream(reducedNetTab);
                                }
                            } catch (Exception e1){
                                JOptionPane.showMessageDialog(TAPAALGUI.getApp(),
                                    e1.getMessage(),
                                    "Error loading reduced net file",
                                    JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    });

                    gbc = GridBagHelper.as(0,5, WEST, new Insets(0,0,10,0));
                    panel.add(openReducedButton, gbc);
                }
                if (!result.getPlaceBoundStatistics().isEmpty()) {
                    JButton placeStatsButton = new JButton("Place-Bound Statistics");
                    placeStatsButton.addActionListener(arg0 -> JOptionPane.showMessageDialog(panel, createStatisticsPanel(result, false), "Place-Bound Statistics", JOptionPane.INFORMATION_MESSAGE));

                    gbc = GridBagHelper.as(1,4, WEST, new Insets(10, 0, 10, 0));
                    panel.add(placeStatsButton, gbc);
                }
            }
            rowOffset = 6;

		} else if (modelChecker.supportsStats() && !result.isSolvedUsingQuerySimplification() && isCTLQuery){
            rowOffset = displayStats(panel, result.getCTLStatsAsString(), modelChecker.getStatsExplanations(), 1);
            if (result.getRawOutput() != null) {
                showRawQueryButton = new JButton("Show raw query results");
                showRawQueryButton.addActionListener(arg0 -> JOptionPane.showMessageDialog(panel, createRawQueryPanel(result.getRawOutput()), "Raw query results", JOptionPane.INFORMATION_MESSAGE));
                gbc = GridBagHelper.as(1, rowOffset+1, WEST, new Insets(0,0,10,0));
                panel.add(showRawQueryButton, gbc);
            }
		}

        if (modelChecker.supportsStats() && result.stats() instanceof SMCStats) {
            SMCStats stats = (SMCStats)result.stats();

            List<Graph> graphs = new ArrayList<>();

            List<GraphPoint> cumulativeStepPoints = stats.getCumulativeStepPoints();
            if (!cumulativeStepPoints.isEmpty()) {
                graphs.add(new Graph("Cumulative Probability / Step", cumulativeStepPoints));
            }
            
            List<GraphPoint> cumulativeDelayPoints = stats.getCumulativeDelayPoints();
            if (!cumulativeDelayPoints.isEmpty()) {
                graphs.add(new Graph("Cumulative Probability / Delay", cumulativeDelayPoints));
            }  

            if (!graphs.isEmpty()) {
                GraphDialog graphFrame = new GraphDialog(graphs, "SMC Statistics");
    
                String btnText = graphs.size() == 1 ? "Show graph" : "Show graphs";

                JButton showGraphButton = new JButton(btnText);
                gbc = GridBagHelper.as(1, rowOffset+2, WEST, new Insets(0,0,10,0));
                panel.add(showGraphButton, gbc);
                showGraphButton.addActionListener(arg0 -> graphFrame.display());
            }
        }

        if (result.isResolvedUsingSkeletonPreprocessor()) {
            gbc = GridBagHelper.as(0, rowOffset+2, GridBagHelper.Anchor.WEST, new Insets(0,0,15,0));
            gbc.gridwidth = 2;
            panel.add(new JLabel(toHTML("The query was answered by Skeleton Analysis preprocessing.")), gbc);
        }


        gbc = GridBagHelper.as(0, rowOffset+3, WEST, new Insets(0,0,15,0));
        gbc.gridwidth = 2;
		if(result.isSolvedUsingQuerySimplification()){
			panel.add(new JLabel(toHTML("The query was resolved using Query Simplification.")), gbc);
		} else if (result.isSolvedUsingTraceAbstractRefinement()){
            panel.add(new JLabel(toHTML("The query was resolved using Trace Abstraction Refinement.")), gbc);
        } else if (result.isSolvedUsingSiphonTrap()) {
            panel.add(new JLabel(toHTML("The query was resolved using Siphon Trap.")), gbc);
        }

		if (showRawQueryButton == null && result.getRawOutput() != null) {
            showRawQueryButton = new JButton("Show raw query results");
            showRawQueryButton.addActionListener(arg0 -> JOptionPane.showMessageDialog(panel, createRawQueryPanel(result.getRawOutput()), "Raw query results", JOptionPane.INFORMATION_MESSAGE));
            rowOffset++;
            gbc = GridBagHelper.as(1, rowOffset+3, WEST, new Insets(0,0,10,0));
            panel.add(showRawQueryButton, gbc);
        }
		
		gbc = GridBagHelper.as(0, rowOffset+4, WEST);
    	gbc.gridwidth = 2;
		panel.add(new JLabel(result.getVerificationTimeString()), gbc);

        gbc = GridBagHelper.as(0, rowOffset+5, WEST);
        gbc.gridwidth = 2;
		panel.add(new JLabel("Estimated memory usage: "+MemoryMonitor.getPeakMemory()), gbc);
		
		//Show discrete semantics warning if needed
		QueryResult queryResult = result.getQueryResult();
        if (((queryResult.hasDeadlock() && queryResult.queryType() == QueryType.EF && !queryResult.isQuerySatisfied()) ||
            (queryResult.hasDeadlock() && queryResult.queryType() == QueryType.AG && queryResult.isQuerySatisfied()) ||
            (queryResult.queryType() == QueryType.EG && !queryResult.isQuerySatisfied()) ||
            (queryResult.queryType() == QueryType.AF && queryResult.isQuerySatisfied()))
            && modelChecker.useDiscreteSemantics()) {

            gbc = GridBagHelper.as(0, rowOffset+8, WEST);
            gbc.gridwidth = 2;
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
