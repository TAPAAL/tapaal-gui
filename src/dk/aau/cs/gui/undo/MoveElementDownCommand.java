package dk.aau.cs.gui.undo;

import pipe.gui.widgets.SidePane;

public class MoveElementDownCommand extends Command{
    final int oldIndex;
    final int newIndex;
    final SidePane sidePane;

    public MoveElementDownCommand(SidePane sidePane, int oldIndex, int newIndex){
        this.oldIndex = oldIndex;
        this.newIndex = newIndex;
        this.sidePane = sidePane;
    }

    @Override
    public void redo() {
        if(sidePane.getJList().getSelectedIndex() == oldIndex){
            sidePane.moveDown(oldIndex);
            sidePane.getJList().setSelectedIndex(oldIndex+1);
        }else if(sidePane.getJList().getSelectedIndex() == oldIndex+1){
            sidePane.moveDown(oldIndex);
            sidePane.getJList().setSelectedIndex(newIndex-1);
        } else{
            sidePane.moveDown(oldIndex);
        }
        sidePane.getJList().updateUI();
    }

    @Override
    public void undo() {
        if(sidePane.getJList().getSelectedIndex() == newIndex){
            sidePane.moveUp(newIndex);
            sidePane.getJList().setSelectedIndex(newIndex-1);
        } else if(sidePane.getJList().getSelectedIndex() == newIndex-1){
            sidePane.moveUp(newIndex);
            sidePane.getJList().setSelectedIndex(oldIndex+1);
        }else{
            sidePane.moveUp(newIndex);
        }
        sidePane.getJList().updateUI();
    }
}
