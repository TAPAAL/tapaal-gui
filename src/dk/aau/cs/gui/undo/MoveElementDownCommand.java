package dk.aau.cs.gui.undo;

import pipe.dataLayer.Template;
import pipe.gui.widgets.SidePane;

import javax.swing.*;

public class MoveElementDownCommand extends Command{
    int oldIndex;
    int newIndex;
    SidePane sidePane;
    JList<Template> templateList;
    int currentIndex;
    public MoveElementDownCommand(SidePane sidePane, int oldIndex, int newIndex){
        this.oldIndex = oldIndex;
        this.newIndex = newIndex;
        this.sidePane = sidePane;
        this.currentIndex = sidePane.getJList().getSelectedIndex();
    }

    @Override
    public void redo() {
        if(currentIndex == newIndex){
            sidePane.moveDown(newIndex);
            sidePane.getJList().setSelectedIndex(newIndex+1);
        }else if(currentIndex == newIndex+1){
            sidePane.moveDown(newIndex);
            sidePane.getJList().setSelectedIndex(oldIndex-1);
        } else{
            sidePane.moveDown(newIndex);
        }
    }

    @Override
    public void undo() {
        if(currentIndex == oldIndex){
            sidePane.moveUp(oldIndex);
            sidePane.getJList().setSelectedIndex(oldIndex-1);
        } else if(currentIndex == oldIndex-1){
            sidePane.moveUp(oldIndex);
            sidePane.getJList().setSelectedIndex(newIndex+1);
        }else{
            sidePane.moveUp(oldIndex);
        }
    }
}
