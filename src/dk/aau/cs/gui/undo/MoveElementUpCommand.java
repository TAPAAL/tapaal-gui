package dk.aau.cs.gui.undo;

import pipe.gui.widgets.SidePane;

public class MoveElementUpCommand extends Command{
    final int oldIndex;
    final int newIndex;
    final SidePane sidePane;

    public MoveElementUpCommand(SidePane sidePane, int oldIndex, int newIndex){
        this.oldIndex = oldIndex;
        this.newIndex = newIndex;
        this.sidePane = sidePane;
    }

    @Override
    public void redo() {
        if(sidePane.getJList().getSelectedIndex() == oldIndex){
            sidePane.moveUp(oldIndex);
            sidePane.getJList().setSelectedIndex(oldIndex-1);
            sidePane.getJList().ensureIndexIsVisible(oldIndex-1);
        } else if(sidePane.getJList().getSelectedIndex() == oldIndex-1){
            sidePane.moveUp(oldIndex);
            sidePane.getJList().setSelectedIndex(newIndex+1);
            sidePane.getJList().ensureIndexIsVisible(newIndex+1);
        }else{
            sidePane.moveUp(oldIndex);
        }
        sidePane.getJList().updateUI();

    }

    @Override
    public void undo() {
        if(sidePane.getJList().getSelectedIndex() == newIndex){
            sidePane.moveDown(newIndex);
            sidePane.getJList().setSelectedIndex(newIndex+1);
            sidePane.getJList().ensureIndexIsVisible(newIndex+1);
        }else if(sidePane.getJList().getSelectedIndex() == newIndex+1){
            sidePane.moveDown(newIndex);
            sidePane.getJList().setSelectedIndex(oldIndex-1);
            sidePane.getJList().ensureIndexIsVisible(oldIndex-1);
        } else{
            sidePane.moveDown(newIndex);
        }
        sidePane.getJList().updateUI();
    }
}
