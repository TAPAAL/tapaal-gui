package net.tapaal.swinghelpers;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Iterator;

/**
 * Helper JTabbedPaned with extra function
 *
 * Adding new tabs automatically add TabComponents
 * TODO: Accepts and returns T types elements
 * Can iterate the tabs
 * TODO: check that input type is T?!??!?
 *
 */
public abstract class ExtendedJTabbedPane<T extends Component> extends JTabbedPane implements Iterable<T>{

    public ExtendedJTabbedPane() {
    }

    public abstract Component generator();


    private void setTabComponent(Component component) {
        int index = indexOfComponent(component);
        if (index >= 0) {
            setTabComponentAt(index, generator());
        }
    }

    @Override
    public Component add(Component component) {
        Component toReturn = super.add(component);
        setTabComponent(toReturn);
        return toReturn;
    }

    @Override
    public Component add(Component component, int index) {
        Component toReturn = super.add(component, index);
        setTabComponent(toReturn);
        return toReturn;
    }

    @Override
    public Component add(String title, Component component) {
        Component toReturn = super.add(title, component);
        setTabComponent(toReturn);
        return toReturn;
    }

    @Override
    public void add(Component component, Object constraints) {
        super.add(component, constraints);
        setTabComponent(component);
    }

    @Override
    public void add(Component component, Object constraints, int index) {
        super.add(component, constraints, index);
        setTabComponent(component);
    }

    @Override
    public void addTab(String title, Component component) {
        super.addTab(title, component);
        setTabComponent(component);
    }

    @Override
    public void addTab(String title, Icon icon, Component component) {
        super.addTab(title, icon, component);
        setTabComponent(component);
    }

    @Override
    public void addTab(String title, Icon icon, Component component, String tip) {
        super.addTab(title, icon, component, tip);
        setTabComponent(component);
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return new TabIterator();
    }

    private class TabIterator implements Iterator<T> {

        private int position = 0;

        @Override
        public boolean hasNext() {
            return position < getTabCount();
        }

        @Override
        public T next() {
            //XXX - known unsafe call, in theory someone could add something not of type T
            //noinspection unchecked
            T toReturn = (T) getTabComponentAt(position);
            position += 1;
            return toReturn;
        }
    }
}
