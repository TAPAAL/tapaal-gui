package net.tapaal.gui.undo.Colored;

import net.tapaal.gui.undo.Command;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ProductType;

import java.util.Vector;

public class UpdatePTColorTypeCommand extends Command {
    private final ColorType oldColorType;
    private final ColorType newColorType;
    private final ProductType productType;
    private final Vector<ColorType> oldConstituents;

    public UpdatePTColorTypeCommand(ColorType oldColorType, ColorType newColorType, ProductType productType) {
        this.oldColorType = oldColorType;
        this.newColorType = newColorType;
        this.productType = productType;
        oldConstituents = (Vector<ColorType>) productType.getConstituents().clone();
    }

    @Override
    public void undo() {
        productType.setConstituents(oldConstituents);
    }

    @Override
    public void redo() {
        productType.replaceColorType(newColorType, oldColorType);
    }
}
