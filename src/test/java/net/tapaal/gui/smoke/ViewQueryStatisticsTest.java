package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import net.tapaal.gui.petrinet.dialog.StatisticsPanel;

import javax.swing.JLabel;
import java.awt.GridBagLayout;
import java.util.List;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

class ViewQueryStatisticsTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void statisticsDistinguishesShownActiveAndAllComponents() throws Exception {
        gui.open("view-query-fixtures/components.tapn");
        gui.menu("Tools", "Net statistics");
        var dialog = gui.awaitDialog("Statistics");
        gui.onEdt(() -> {
            var panel = GuiHarness.find(dialog, StatisticsPanel.class, candidate -> true);
            assertEquals(List.of("Shown component", "Active components", "All components"), row(panel, ""));
            assertEquals(List.of("1", "2", "3"), row(panel, "Number of components considered:"));
            assertEquals(List.of("2", "3", "4"), row(panel, "Number of places:"));
            assertEquals(List.of("2", "2", "2"), row(panel, "Number of input arcs:"));
            assertEquals(List.of("1", "4", "6"), row(panel, "Number of tokens:"));
            return null;
        });
        gui.clickButton(dialog, "OK");
        gui.awaitClosed(dialog);
    }

    private List<String> row(StatisticsPanel panel, String heading) {
        var layout = (GridBagLayout) panel.getLayout();
        var label = GuiHarness.find(panel, JLabel.class, candidate -> heading.equals(candidate.getText().trim()));
        int row = layout.getConstraints(label).gridy;
        var cells = new TreeMap<Integer, String>();
        for (var cell : GuiHarness.findAll(panel, JLabel.class, candidate -> true)) {
            var position = layout.getConstraints(cell);
            if (position.gridy == row && position.gridx > 0) {
                cells.put(position.gridx, cell.getText());
            }
        }
        return List.copyOf(cells.values());
    }
}
