package net.tapaal.swinghelpers;

import java.awt.*;

public class GridBagHelper {

    public enum Anchor {
        NORTH(11),
        EAST(13),
        WEST(17),
        NORTHWEST(18);

        public final int value;

        Anchor(int value) {
            this.value = value;
        }
    }

    public static GridBagConstraints as(int gridx, int gridy, Anchor anchor, Insets inset) {
        var gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = gridx;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.anchor = anchor.value;

        if (inset != null) {
            gridBagConstraints.insets = inset;
        }

        return gridBagConstraints;
    }

    public static GridBagConstraints as(int gridx, int gridy, Anchor anchor) {
        return as(gridx, gridy, anchor, null);
    }

}
