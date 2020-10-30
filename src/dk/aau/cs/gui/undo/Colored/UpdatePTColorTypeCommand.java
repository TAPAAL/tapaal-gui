package dk.aau.cs.gui.undo.Colored;

import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ProductType;

import java.util.Vector;

public class UpdatePTColorTypeCommand extends Command {
    private ColorType oldColorType;
    private ColorType newColorType;
    private ProductType productType;
    private Vector<ColorType> oldConstituents;

    public UpdatePTColorTypeCommand(ColorType oldColorType, ColorType newColorType, ProductType productType) {
        this.oldColorType = oldColorType;
        this.newColorType = newColorType;
        this.productType = productType;
        oldConstituents = (Vector<ColorType>) productType.getConstituents().clone();
    }

    @Override
    public void undo() {
/*        Vector<ColorType> newConstituents = productType.getConstituents();
        for (ColorType ct : oldConstituents) {
            if(ct.equals(oldColorType)){
                int index = oldConstituents.indexOf(ct);
                newConstituents.set(index, oldColorType);
            }
        }
        productType.setConstituents(newConstituents);*/

        productType.setConstituents(oldConstituents);
    }

    @Override
    public void redo() {
        productType.replaceColorType(newColorType, oldColorType);
    }
}
