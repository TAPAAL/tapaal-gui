package net.tapaal.gui.DrawingSurfaceManager;

import pipe.gui.canvas.DrawingSurfaceImpl;
import pipe.gui.graphicElements.GraphicalElement;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class AbstractDrawingSurfaceManager {

    protected DrawingSurfaceImpl canvas;

    public void drawingSurfaceMouseClicked(MouseEvent e) {}
    public void drawingSurfaceMousePressed(MouseEvent e) {}
    public void drawingSurfaceMouseReleased(MouseEvent e){}
    public void drawingSurfaceMouseDragged(MouseEvent e) {}
    public void drawingSurfaceMouseWheelMoved(MouseWheelEvent e) {}
    public void drawingSurfaceMouseMoved(MouseEvent e){}

    public enum MouseAction {
        clicked,
        doubleClicked,
        rightClicked,
        pressed,
        released,
        dragged,
        entered,
        exited,
        wheel,
        moved
    }
    public static class DrawingSurfaceEvent {


        public final GraphicalElement pno;
        public final MouseEvent e;
        public final MouseAction a;
        //Mouse Event type eg click mouse over etc

        public DrawingSurfaceEvent(GraphicalElement pno, MouseEvent e, MouseAction a) {
            this.pno = pno;
            this.e = e;
            this.a = a;
        }

    }

    private final Map<Predicate<DrawingSurfaceEvent>, Consumer<DrawingSurfaceEvent>> filter = new LinkedHashMap<>();
    private final AbstractDrawingSurfaceManager next = null;

    public final void registerManager(DrawingSurfaceImpl canvas){
        this.canvas = canvas;
        setupManager();
    }

    public final void deregisterManager(){
        this.canvas = null;
        teardownManager();
    }

    public void setupManager(){}
    public void teardownManager(){}

    public AbstractDrawingSurfaceManager() {
			registerEvents();
    }

    public abstract void registerEvents();
    /*registerEvent(
					(event) -> event.pno instanceof Place && event.a == MouseAction.clicked,
					(event) -> placeClicked((Place) event.pno)
			);
			registerEvent(
					(event) -> event.pno instanceof PlaceTransitionObject,
					(event) -> System.out.println("clicked PTO")
			);
			registerEvent(
					(event) -> event.pno instanceof Transition,
					(event) -> System.out.println("clicked transition")
			);*/


    protected final void registerEvent(
            //Class<? extends PetriNetObject> typeFilter,
            Predicate<DrawingSurfaceEvent> filterCondition,
            Consumer<DrawingSurfaceEvent> action
    ){
        filter.put(filterCondition, action);
    }

    public final void triggerEvent(DrawingSurfaceEvent e){
        //Select and run event from filter
        //If not matched, run next if not null (else ignore)

        /* ALTERNATIVE IMPLEMENTATION
        boolean eventTriggered = false;
        for (Predicate<DrawingSurfaceEvent> predicate : filter.keySet()){
            if (predicate.test(e)) {
                eventTriggered = true;
                filter.get(predicate).accept(e);
            }
        }
        if (!eventTriggered && next !=null) {
            next.triggerEvent(e);
        }
        */
        boolean handled = filter.keySet().stream().filter(f -> (f.test(e))).findFirst().
                map(
                        f->{
                            filter.get(f).accept(e);
                            return true;
                        }).orElse(false);
        if (!handled && next != null) {
            next.triggerEvent(e);
        }

    }

}
