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
        oldConstituents = productType.getConstituents();
    }

    @Override
    public void undo() {
        System.out.println(productType.getName());
        System.out.println("Inserting" + oldColorType);
        System.out.println("Old constituents was "+ oldConstituents);

        Vector<ColorType> newConstituents = productType.getConstituents();
        System.out.println("New constituents are " + newConstituents);
        for (ColorType ct : oldConstituents) {
            if(ct.equals(oldColorType)){
                int index = oldConstituents.indexOf(ct);
                newConstituents.set(index, oldColorType);
            }
        }
        System.out.println("New constituents after update " + newConstituents);
        productType.setConstituents(newConstituents);
    }

    @Override
    public void redo() {
        productType.replaceColorType(newColorType, oldColorType);
    }
}
