package pipe.gui.petrinet.graphicElements.tapn;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import javax.swing.*;

import net.tapaal.gui.petrinet.TAPNLens;
import dk.aau.cs.model.CPN.Expressions.GuardExpression;
import pipe.gui.TAPAALGUI;
import pipe.gui.Constants;
import pipe.gui.petrinet.graphicElements.Transition;
import pipe.gui.swingcomponents.EscapableDialog;
import pipe.gui.petrinet.editor.TAPNTransitionEditor;
import net.tapaal.gui.petrinet.Context;
import net.tapaal.gui.petrinet.undo.Command;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.event.TimedTransitionEvent;
import dk.aau.cs.model.tapn.event.TimedTransitionListener;

public class TimedTransitionComponent extends Transition {

	private dk.aau.cs.model.tapn.TimedTransition transition;
	private final dk.aau.cs.model.tapn.event.TimedTransitionListener listener;
	private GeneralPath dashedOutline;

	public TimedTransitionComponent(int positionXInput, int positionYInput, dk.aau.cs.model.tapn.TimedTransition transition, TAPNLens lens) {
		super(positionXInput, positionYInput);
		this.transition = transition;
		listener = timedTransitionListener();
		transition.addTimedTransitionListener(listener);
		this.lens = lens;

	}

	public TimedTransitionComponent(
        int positionXInput,
        int positionYInput,
        String idInput,
        int nameOffsetXInput,
        int nameOffsetYInput,
        int angleInput,
        TAPNLens lens
    ) {
		super(
		    positionXInput,
            positionYInput,
            idInput,
            nameOffsetXInput,
            nameOffsetYInput,
            angleInput
        );
		listener = timedTransitionListener();
		attributesVisible = true;
        this.lens = lens;
	}

	private TimedTransitionListener timedTransitionListener(){
		return new TimedTransitionListener() {
			public void nameChanged(TimedTransitionEvent e) {
				TimedTransition source = e.source();
				TimedTransitionComponent.super.setName(source.name());
			}

			public void sharedStateChanged(TimedTransitionEvent e) { repaint(); }
		};
	}

    @Override
    public JPopupMenu getPopup(MouseEvent e) {
        final var popup = super.getPopup(e);

        popup.add(new JPopupMenu.Separator());

        if (lens.isTimed()) {
            final var urgentAction = new JCheckBoxMenuItem();
            urgentAction.setSelected(isUrgent());
            urgentAction.setAction(
                new AbstractAction("Urgent") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        TAPAALGUI.getCurrentTab().guiModelManager.toggleUrgentTrans(); //XXX: guiModelManager should prop be passed via popup generator
                    }
                }
            );
            popup.add(urgentAction);
        }

        if (lens.isGame()) {
            final var uncontrollableAction = new JCheckBoxMenuItem();
            uncontrollableAction.setSelected(isUncontrollable());
            uncontrollableAction.setAction(
                new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        TAPAALGUI.getCurrentTab().guiModelManager.toggleUncontrollableTrans(); //XXX: guiModelManager should prop be passed via popup generator
                    }
                }
            );
        }


        return popup;
    }

    @Override
	public void showEditor() {
		// Build interface
		EscapableDialog guiDialog = new EscapableDialog(TAPAALGUI.getApp(), "Edit Transition", true);

		Container contentPane = guiDialog.getContentPane();

		// 1 Set layout
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));

		// 2 Add Place editor
		contentPane.add(new TAPNTransitionEditor(guiDialog, this, new Context(TAPAALGUI.getCurrentTab())));

		guiDialog.setResizable(true);

		// Make window fit contents' preferred size
		guiDialog.pack();

		// Move window to the middle of the screen
		guiDialog.setLocationRelativeTo(null);
		guiDialog.setVisible(true);
	}

	@Override
	public boolean isTransitionEnabled() {
		return transition.isEnabled();
	}

	@Override
	public boolean isDelayEnabled() {
		return transition.isDEnabled();
	}
	
	public TimeInterval getDInterval(){
		return transition.getdInterval();
	}

	public dk.aau.cs.model.tapn.TimedTransition underlyingTransition() {
		return transition;
	}

	public void setUnderlyingTransition(TimedTransition transition) {
		if(this.transition != null && listener != null){
			transition.removeListener(listener);
		}
		transition.addTimedTransitionListener(listener);
		this.transition = transition;
		this.setName(transition.name());
		repaint();
	}

	@Override
	public void setName(String nameInput) {
		transition.setName(nameInput);
		super.setName(nameInput);
	}

	public GuardExpression getGuardExpression() {
	    return transition.getGuard();
    }

    public void setGuardExpression(GuardExpression expression) {
	    transition.setGuard(expression);
    }

	public String getName() {
		return transition != null ? transition.name() : "";
	}
	
	public boolean isUrgent(){
		return transition.isUrgent();
	}
	
	public void setUrgent(boolean value){
		transition.setUrgent(value);
	}

    public boolean isUncontrollable() {
        return transition.isUncontrollable();
    }

    public void setUncontrollable(boolean isUncontrollable) {
        transition.setUncontrollable(isUncontrollable);
    }

	public boolean hasUntimedPreset(){
		return transition.hasUntimedPreset();
	}
	
	@Override
	public void update(boolean displayConstantNames) {
        getNameLabel().setText("");
		if(transition != null) {
			getNameLabel().setName(transition.name());
			getNameLabel().setVisible(attributesVisible);
			getNameLabel().zoomUpdate(getZoom());
            if(lens.isStochastic()) {
                getNameLabel().setText("\n" + underlyingTransition().getDistribution().summary());
            }
			if(underlyingTransition().getGuard() != null && lens.isColored()){
                super.update(displayConstantNames);
                pnName.setText(pnName.getText() + "\n" + buildGuardString(this.underlyingTransition().getGuard().toString()));
            }
		}

		repaint();
	}

	private String buildGuardString(String str){
	    var strArray = str.split(" ");
	    int counter = 0;
	    StringBuilder builder  = new StringBuilder();
	    int maxAndOr = 5;
        for(String subStr : strArray){
            if(subStr.equals("and") || subStr.equals("or")){
                counter++;
            }
            if(counter >= maxAndOr){
                builder.append("\n");
                counter = 0;
            }
            builder.append(subStr + " ");
        }

        return builder.toString();

    }

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (transition.isShared()) {
			Graphics2D graphics = (Graphics2D)g;
			Stroke oldStroke = graphics.getStroke();

			BasicStroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, new float[] {5.0f}, 0.0f);
			graphics.setStroke(dashed);

			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			graphics.draw(dashedOutline);

			graphics.setStroke(oldStroke);
		}
        if (!isUncontrollable()) {
            super.fillTransition(g);
        }
		if (isUrgent()) {
		    g.setColor(isUncontrollable() ? Color.BLACK : Color.WHITE);
			g.fillOval(11, 11, 8,8);
		}
	}

	@Override
	protected void constructTransition() {
		super.constructTransition();
		double x = ((componentWidth - TRANSITION_WIDTH - Constants.DASHED_PADDING) / 2);
		double y = -Constants.DASHED_PADDING/2;
		double width = TRANSITION_WIDTH + Constants.DASHED_PADDING;
		double height = TRANSITION_HEIGHT + Constants.DASHED_PADDING;
		dashedOutline = new GeneralPath(new Rectangle2D.Double(x, y, width, height));
	}

	@Override
	public Command rotate(int angleInc) {
		dashedOutline.transform(AffineTransform.getRotateInstance(Math.toRadians(angleInc), (componentWidth) / 2, (componentHeight)  / 2));
		return super.rotate(angleInc);
	}

	public TimedTransitionComponent copy(TimedArcPetriNet tapn) {
		TimedTransitionComponent transitionComponent = new TimedTransitionComponent(getOriginalX(), getOriginalY(), id, getNameOffsetX(), getNameOffsetY(), getAngle(), lens);
		transitionComponent.setUnderlyingTransition(tapn.getTransitionByName(transition.name()));
        transitionComponent.setUncontrollable(isUncontrollable());

		return transitionComponent;
	}
	
	Window dIntervalWindow = null;
	
	public void showDInterval(boolean show) {
		
		if (dIntervalWindow != null){
			dIntervalWindow.dispose();
		}
		
		// Build interface
		if (show && (transition.getdInterval() != null) && isTimed()) {
			dIntervalWindow = new Window(new Frame());
			dIntervalWindow.add(new JTextArea(transition.getdInterval().toString()));
			
			dIntervalWindow.getComponent(0).setBackground(Color.lightGray);

			// Make window fit contents' preferred size
			dIntervalWindow.pack();

			// Move window to the middle of the screen
			dIntervalWindow.setLocationRelativeTo(this);
			dIntervalWindow.setLocation(dIntervalWindow.getLocation().x, dIntervalWindow.getLocation().y - 20);
			
			dIntervalWindow.setVisible(show);
		}
	}
}
