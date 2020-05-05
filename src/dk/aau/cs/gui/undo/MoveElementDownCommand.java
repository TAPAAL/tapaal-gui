package dk.aau.cs.gui.undo;

import pipe.dataLayer.Template;
import pipe.gui.widgets.SidePane;

import javax.swing.*;

public class MoveElementDownCommand extends Command{
    int oldIndex;
    int newIndex;
    SidePane sidePane;
    JList<Template> templateList;
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
