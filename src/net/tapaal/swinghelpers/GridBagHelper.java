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

    public enum Fill {
        HORIZONTAL(2);

        public final int value;

        Fill(int value) {

            this.value = value;
        }
    }

    public static GridBagConstraints as(int gridx, int gridy, Anchor anchor, Fill fill, Insets inset) {
        var gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = gridx;
        gridBagConstraints.gridy = gridy;

        if (anchor != null) {
            gridBagConstraints.anchor = anchor.value;
        }

        if (fill != null) {
            gridBagConstraints.fill = fill.value;
        }

        if (inset != null) {
            gridBagConstraints.insets = inset;
        }

        return gridBagConstraints;
    }
    public static GridBagConstraints as(int gridx, int gridy, Anchor anchor, Insets inset) {
        return as(gridx, gridy, anchor, null, inset);
    }

    public static GridBagConstraints as(int gridx, int gridy, Fill fill, Insets inset) {
        return as(gridx, gridy, null, fill, inset);
    }

    public static GridBagConstraints as(int gridx, int gridy, Anchor anchor) {
        return as(gridx, gridy, anchor, null);
    }

    public static GridBagConstraints as(int gridx, int gridy, Insets inset) {
        return as(gridx, gridy, null, null, inset);
    }

    public static GridBagConstraints as(int gridx, int gridy) {
        return as(gridx, gridy, null, null, null);
    }
    public static GridBagConstraints as(int gridx, int gridy, int gridwidth, Anchor anchor, Insets inset) {
        return as(gridx, gridy, gridwidth, 1, anchor, inset);
    }
    public static GridBagConstraints as(int gridx, int gridy, int gridwidth, int gridtheight, Anchor anchor, Insets inset) {
        var r = as(gridx, gridy, anchor, null, inset );
        r.gridwidth = gridwidth;
        r.gridheight = gridtheight;
        return r;
    }

}
