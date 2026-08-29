package dk.aau.cs.verification;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import dk.aau.cs.model.tapn.simulation.TAPNNetworkTrace;
import dk.aau.cs.util.Tuple;

public class StatisticsPanel {
    public static JPanel createPanel(final VerificationResult<TAPNNetworkTrace> result, boolean transitionPanel) {
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

    private static Object[][] extractArrayFromTransitionStatistics(final VerificationResult<TAPNNetworkTrace> result) {
        List<Tuple<String, Number>> transitionStats = result.getTransitionStatistics();
        Object[][] out = new Object[transitionStats.size()][2];
        DecimalFormat formatter = new DecimalFormat("#.######");
        
        for (int i = 0; i < transitionStats.size(); ++i) {
            Number value = transitionStats.get(i).value2();
            Object displayValue;
            if (value.doubleValue() == -1) {
                displayValue = "unknown";
            } else if (value instanceof Integer || value.doubleValue() == value.intValue()) {
                displayValue = value.intValue();
            } else {
                displayValue = formatter.format(value);
            }
            
            Object[] line = {displayValue, transitionStats.get(i).value1()};
            out[i] = line;
        }
    
        return out;
    }

	private static Object[][] extractArrayFromPlaceBoundStatistics(final VerificationResult<TAPNNetworkTrace> result) {
        List<Tuple<String,Number>> placeBoundStats = result.getPlaceBoundStatistics();
        Object[][] out = new Object[placeBoundStats.size()][2];
        DecimalFormat formatter = new DecimalFormat("#.######");
        
        for (int i = 0; i < placeBoundStats.size(); i++) {
            Number value = placeBoundStats.get(i).value2();
            Object displayValue;
            
            if (value.doubleValue() == -1) {
                displayValue = "unknown";
            } else if (value instanceof Integer || value.doubleValue() == value.intValue()) {
                displayValue = value.intValue();
            } else {
                displayValue = formatter.format(value);
            }
            
            Object[] line = {displayValue, placeBoundStats.get(i).value1()};
            out[i] = line;
        }
    
        return out;
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

    private static String toHTML(String string){
        return
            "<html>" + string.replace(System.getProperty("line.separator"), "<br/>") + "</html>";
	}
}
