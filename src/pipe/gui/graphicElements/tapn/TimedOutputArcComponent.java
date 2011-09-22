package pipe.gui.graphicElements.tapn;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.util.Hashtable;

import pipe.dataLayer.DataLayer;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.Pipe;
import pipe.gui.Zoomer;
import pipe.gui.graphicElements.Arc;
import pipe.gui.graphicElements.ArcPath;
import pipe.gui.graphicElements.NameLabel;
import pipe.gui.graphicElements.PlaceTransitionObject;
import pipe.gui.handler.ArcHandler;
import dk.aau.cs.model.tapn.TimedArcPetriNet;

/**
 * <b>Arc</b> - Petri-Net Normal Arc Class
 * 
 * @see <p>
 *      <a href="..\PNMLSchema\index.html">PNML - Petri-Net XMLSchema
 *      (stNet.xsd)</a>
 * @see </p>
 *      <p>
 *      <a href="..\..\..\UML\dataLayer.html">UML - PNML Package </a>
 *      </p>
 * @version 1.0
 */
public class TimedOutputArcComponent extends Arc {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5588142404135607382L;

	protected Polygon head = new Polygon(new int[] { 0, 5, 0, -5 }, new int[] {
			0, -10, -7, -10 }, 4);

	private dk.aau.cs.model.tapn.TimedOutputArc outputArc;

	/**
	 * Create Petri-Net Arc object
	 * 
	 * @param startPositionXInput
	 *            Start X-axis Position
	 * @param startPositionYInput
	 *            Start Y-axis Position
	 * @param endPositionXInput
	 *            End X-axis Position
	 * @param endPositionYInput
	 *            End Y-axis Position
	 * @param sourceInput
	 *            Arc source
	 * @param targetInput
	 *            Arc target
	 * @param idInput
	 *            Arc id
	 */
	public TimedOutputArcComponent(double startPositionXInput,
			double startPositionYInput, double endPositionXInput,
			double endPositionYInput, PlaceTransitionObject sourceInput,
			PlaceTransitionObject targetInput, int weightInput, String idInput,
			boolean taggedInput) {
		super(startPositionXInput, startPositionYInput, endPositionXInput,
				endPositionYInput, sourceInput, targetInput, weightInput,
				idInput);
	}

	/**
	 * Create Petri-Net Arc object
	 */
	public TimedOutputArcComponent(PlaceTransitionObject newSource) {
		super(newSource);
	}

	public TimedOutputArcComponent(TimedOutputArcComponent arc) {
		label = new NameLabel(zoom);
		
		this.myPath = new ArcPath(this);
		for (int i = 0; i <= arc.myPath.getEndIndex(); i++) {
			this.myPath.addPoint(arc.myPath.getPoint(i).getX(), arc.myPath.getPoint(i).getY(), arc.myPath.getPointType(i));
		}
		this.myPath.createPath();
		this.updateBounds();
		this.id = arc.id;
		this.setSource(arc.getSource());
		this.setTarget(arc.getTarget());
	}

	public TimedOutputArcComponent paste(double despX, double despY,
			boolean toAnotherView) {
		PlaceTransitionObject source = this.getSource().getLastCopy();
		PlaceTransitionObject target = this.getTarget().getLastCopy();

		if (source == null && target == null) {
			// don't paste an arc with neither source nor target
			return null;
		}

		if (source == null) {
			if (toAnotherView) {
				// if the source belongs to another Petri Net, the arc can't be
				// pasted
				return null;
			} else {
				source = this.getSource();
			}
		}

		if (target == null) {
			if (toAnotherView) {
				// if the target belongs to another Petri Net, the arc can't be
				// pasted
				return null;
			} else {
				target = this.getTarget();
			}
		}

		TimedOutputArcComponent copy = new TimedOutputArcComponent(0, 0, // startPoint
				0, 0, // endPoint
				source, target, 1, source.getId() + " to "
						+ target.getId(), false);

		copy.myPath.delete();
		for (int i = 0; i <= this.myPath.getEndIndex(); i++) {
			copy.myPath.addPoint(this.myPath.getPoint(i).getX() + despX,
					this.myPath.getPoint(i).getY() + despY, this.myPath
							.getPointType(i));
			copy.myPath.selectPoint(i);
		}

		source.addConnectFrom(copy);
		target.addConnectTo(copy);

		return copy;
	}

	public TimedOutputArcComponent copy() {
		return new TimedOutputArcComponent(this);
	}

	public void updateLabel(boolean displayConstantNames) {
		label.setText("");
		setLabelPosition();
	}

	@Override
	public void delete() {
		if (outputArc != null)
			outputArc.delete();
		super.delete();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		g2.translate(COMPONENT_DRAW_OFFSET + zoomGrow
				- myPath.getBounds().getX(), COMPONENT_DRAW_OFFSET + zoomGrow
				- myPath.getBounds().getY());

		AffineTransform reset = g2.getTransform();

		if (selected && !ignoreSelection) {
			g2.setPaint(Pipe.SELECTION_LINE_COLOUR);
		} else {
			g2.setPaint(Pipe.ELEMENT_LINE_COLOUR);
		}

		g2.setStroke(new BasicStroke(0.01f * zoom));
		g2.draw(myPath);

		g2.translate(myPath.getPoint(myPath.getEndIndex()).getX(), myPath
				.getPoint(myPath.getEndIndex()).getY());

		g2.rotate(myPath.getEndAngle() + Math.PI);
		g2.setColor(java.awt.Color.WHITE);

		g2.transform(Zoomer.getTransform(zoom));
		g2.setPaint(Pipe.ELEMENT_LINE_COLOUR);

		if (selected && !ignoreSelection) {
			g2.setPaint(Pipe.SELECTION_LINE_COLOUR);
		} else {
			g2.setPaint(Pipe.ELEMENT_LINE_COLOUR);
		}

		g2.setStroke(new BasicStroke(0.8f));
		g2.fillPolygon(head);

		g2.transform(reset);
	}

	public dk.aau.cs.model.tapn.TimedOutputArc underlyingArc() {
		return outputArc;
	}

	public void setUnderlyingArc(dk.aau.cs.model.tapn.TimedOutputArc outputArc) {
		this.outputArc = outputArc;
	}

	public TimedOutputArcComponent copy(TimedArcPetriNet tapn, DataLayer guiModel, Hashtable<PlaceTransitionObject, PlaceTransitionObject> oldToNewMapping) {
		TimedOutputArcComponent arc = new TimedOutputArcComponent(this);
		arc.setSource(oldToNewMapping.get(this.getSource()));
		arc.setTarget(oldToNewMapping.get(this.getTarget()));
		arc.setUnderlyingArc(tapn.getOutputArcFromTransitionAndPlace(tapn.getTransitionByName(outputArc.source().name()), tapn.getPlaceByName(outputArc.destination().name())));
		
		arc.getSource().addConnectFrom(arc);
		arc.getTarget().addConnectTo(arc);
		
		ArcHandler arcHandler = new ArcHandler((DrawingSurfaceImpl)getParent(), arc);
		arc.addMouseListener(arcHandler);
		//arc.addMouseWheelListener(arcHandler);
		arc.addMouseMotionListener(arcHandler);
		
		arc.setGuiModel(guiModel);
		
		return arc;
	}

}
