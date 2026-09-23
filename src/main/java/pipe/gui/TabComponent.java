/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package pipe.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import dk.aau.cs.util.Require;
import net.tapaal.swinghelpers.ExtendedJTabbedPane;
import pipe.gui.petrinet.PetriNetTab;

/**
 * This class represents the component inside the "head" of a tab. That is, it
 * is the component that displays the name of the model, as well as the "x"
 * button for closing the tab.
 * 
 */
public abstract class TabComponent extends JPanel {

	private final JTabbedPane pane;
	private final JLabel changedIndicator;
	private Point dragStart;
	private boolean dragged;

	public TabComponent(final JTabbedPane pane) {
		super(new FlowLayout(FlowLayout.LEFT, 0, 0));

		Require.notNull(pane, "TabbedPane is null");
		
		this.pane = pane;
		setOpaque(false);

		changedIndicator = new JLabel("●");
		changedIndicator.setForeground(new Color(180, 0, 0));
		changedIndicator.setToolTipText("Unsaved changes");
		changedIndicator.setVisible(false);
		add(changedIndicator);

		// make JLabel read titles from JTabbedPane
		JLabel label = new JLabel() {

			@Override
			public String getText() {
				int i = pane.indexOfTabComponent(TabComponent.this);
				if (i != -1) {
					return pane.getTitleAt(i);
				}
				return null;
			}
		};

		add(label);
		label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
		label.addMouseListener(tabMouseListener);
		label.addMouseMotionListener(tabMouseListener);

		JButton button = new TabButton();
		add(button);
		setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
	}

	public void setChanged(boolean changed) {
		changedIndicator.setVisible(changed);
		revalidate();
		repaint();
	}

	private class TabButton extends JButton {

		public TabButton() {
			int size = 17;
			setPreferredSize(new Dimension(size, size));
			setToolTipText("Close this tab");

			setContentAreaFilled(false);
			setFocusable(false);
			setBorder(BorderFactory.createEtchedBorder());
			setBorderPainted(false);
			addMouseListener(buttonMouseListener);
			setRolloverEnabled(true);
			addActionListener(arg0 -> {
				int index = pane.indexOfTabComponent(TabComponent.this);
				closeTab((PetriNetTab) pane.getComponentAt(index));
			});
		}

		// paint the cross
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g.create();
			// shift the image for pressed buttons
			if (getModel().isPressed()) {
				g2.translate(1, 1);
			}
			g2.setStroke(new BasicStroke(2));
			g2.setColor(Color.BLACK);
			if (getModel().isRollover()) {
				g2.setColor(Color.MAGENTA);
			}
			int delta = 6;
			g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
			g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
			g2.dispose();
		}

	}

	protected abstract void closeTab(PetriNetTab tab);

	private final MouseAdapter tabMouseListener = new MouseAdapter() {
		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				dragStart = e.getPoint();
				dragged = false;
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (dragStart == null || !SwingUtilities.isLeftMouseButton(e)) {
				return;
			}

			if (!dragged && dragStart.distance(e.getPoint()) < 5) {
				return;
			}
			dragged = true;

			Point point = SwingUtilities.convertPoint((Component) e.getSource(), e.getPoint(), pane);
			int currentIndex = pane.indexOfTabComponent(TabComponent.this);
			int targetIndex = pane.indexAtLocation(point.x, point.y);
			if (currentIndex < 0 || targetIndex < 0 || currentIndex == targetIndex) {
				return;
			}

			if (pane instanceof ExtendedJTabbedPane<?> extendedPane) {
				extendedPane.moveTab(currentIndex, targetIndex);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			dragStart = null;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1 && !dragged) {
				int index = pane.indexOfTabComponent(TabComponent.this);
				if (index >= 0) {
					pane.setSelectedIndex(index);
				}
			}
			dragged = false;
		}
	};

	private static final MouseListener buttonMouseListener = new MouseAdapter() {
		@Override
		public void mouseEntered(MouseEvent e) {
			Component component = e.getComponent();
			if (component instanceof AbstractButton) {
				AbstractButton button = (AbstractButton) component;
				button.setBorderPainted(true);
			}
		}

		@Override
		public void mouseExited(MouseEvent e) {
			Component component = e.getComponent();
			if (component instanceof AbstractButton) {
				AbstractButton button = (AbstractButton) component;
				button.setBorderPainted(false);
			}
		}
	};
}
