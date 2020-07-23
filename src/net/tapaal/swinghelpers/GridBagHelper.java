package net.tapaal.swinghelpers;

import java.awt.*;

public class GridBagHelper {

    public static GridBagConstraints as(int gridx, int gridy, int anchor, Insets inset) {
        var gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = gridx;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.anchor = anchor;

        if (inset != null) {
            gridBagConstraints.insets = inset;
        }

        return gridBagConstraints;
    }

    public static GridBagConstraints as(int gridx, int gridy, int anchor) {
        return as(gridx, gridy, anchor, null);
    }

}
