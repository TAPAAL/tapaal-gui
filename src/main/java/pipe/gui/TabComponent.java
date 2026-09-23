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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;

import dk.aau.cs.util.Require;
import pipe.gui.petrinet.PetriNetTab;

/**
 * This class represents the component inside the "head" of a tab. That is, it
 * is the component that displays the name of the model, as well as the "x"
 * button for closing the tab.
 * 
 */
public abstract class TabComponent extends JPanel {

	private final JTabbedPane pane;

	public TabComponent(final JTabbedPane pane) {
		super(new FlowLayout(FlowLayout.LEFT, 0, 0));

		Require.notNull(pane, "TabbedPane is null");
		
		this.pane = pane;
		setOpaque(false);

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

		JButton button = new TabButton();
		add(button);
		addMouseListener(tabMouseListener);
		setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
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
			addMouseListener(tabMouseListener);
			setRolloverEnabled(true);
			addActionListener(arg0 -> {
				closeTab();
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

	private void closeTab() {
		int index = pane.indexOfTabComponent(TabComponent.this);
		if (index != -1) {
			closeTab(tabAt(index));
		}
	}

	private PetriNetTab tabAt(int index) {
		return (PetriNetTab) pane.getComponentAt(index);
	}

	private List<PetriNetTab> tabsMatching(TabPredicate predicate) {
		List<PetriNetTab> matchingTabs = new ArrayList<>();
		for (int i = 0; i < pane.getTabCount(); i++) {
			PetriNetTab tab = tabAt(i);
			if (predicate.matches(i, tab)) {
				matchingTabs.add(tab);
			}
		}
		return matchingTabs;
	}

	private void closeTabs(List<PetriNetTab> tabs) {
		tabs.forEach(this::closeTab);
	}

	private JPopupMenu createContextMenu() {
		JPopupMenu menu = new JPopupMenu();
		addMenuItem(menu, "Close", this::closeTab);
		addMenuItem(menu, "Close All", () -> closeTabs(tabsMatching((index, tab) -> true)));
		addMenuItem(menu, "Close Other", () -> {
			int currentIndex = pane.indexOfTabComponent(TabComponent.this);
			closeTabs(tabsMatching((index, tab) -> index != currentIndex));
		});
		addMenuItem(menu, "Close Unmodified Tabs",
			() -> closeTabs(tabsMatching((index, tab) -> !tab.getNetChanged())));
		addMenuItem(menu, "Close Tabs to the Left", () -> {
			int currentIndex = pane.indexOfTabComponent(TabComponent.this);
			closeTabs(tabsMatching((index, tab) -> index < currentIndex));
		});
		addMenuItem(menu, "Close Tabs to the Right", () -> {
			int currentIndex = pane.indexOfTabComponent(TabComponent.this);
			closeTabs(tabsMatching((index, tab) -> index > currentIndex));
		});
		return menu;
	}

	private void addMenuItem(JPopupMenu menu, String label, Runnable action) {
		JMenuItem menuItem = new JMenuItem(label);
		menuItem.addActionListener(e -> action.run());
		menu.add(menuItem);
	}

	private void showContextMenu(MouseEvent e) {
		if (e.isPopupTrigger()) {
			createContextMenu().show(e.getComponent(), e.getX(), e.getY());
		}
	}

	protected abstract void closeTab(PetriNetTab tab);

	@FunctionalInterface
	private interface TabPredicate {
		boolean matches(int index, PetriNetTab tab);
	}

	private final MouseListener tabMouseListener = new MouseAdapter() {
		@Override
		public void mousePressed(MouseEvent e) {
			showContextMenu(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			showContextMenu(e);
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON2) {
				closeTab();
			} else if (e.getButton() == MouseEvent.BUTTON1) {
				selectTab();
			}
		}
	};

	private void selectTab() {
		int index = pane.indexOfTabComponent(TabComponent.this);
		if (index != -1) {
			pane.setSelectedIndex(index);
		}
	}

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
