package pipe.gui.petrinet.animation;

import pipe.gui.TAPAALGUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;

public class AnimationHistorySidePanel extends JPanel {

    private AnimationHistoryList animBox;
    private Animator animator;

    public AnimationHistorySidePanel(Animator animator) {
        super();
        this.animator = animator;

        initComponents();
    }

    /**
     * @deprecated - should not be used, should be passed as argument
     * Only implemented while refactoring animatior -- kyrke 2020-05-18
     */
    @Deprecated
    public AnimationHistoryList getAnimationHistoryList() {
        return animBox;
    }

    private void initComponents() {

        //BorderLayout.Center fill the parent components size
        setLayout(new BorderLayout());

        animBox = new AnimationHistoryList();
        var inputMap = animBox.getInputMap(JComponent.WHEN_FOCUSED);
        inputMap.put(KeyStroke.getKeyStroke("UP"), "simulator.previousComponent");
        inputMap.put(KeyStroke.getKeyStroke("DOWN"), "simulator.nextComponent");
        animBox.getActionMap().put("simulator.previousComponent", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                TAPAALGUI.getCurrentTab().previousComponent();
            }
        });
        animBox.getActionMap().put("simulator.nextComponent", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                TAPAALGUI.getCurrentTab().nextComponent();
            }
        });
        animBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    var clicked = animBox.locationToIndex(e.getPoint());

                    if (clicked != -1) {
                        animator.seekToMarking(clicked);
                        animator.blinkSelected(animBox.getSelectedValue());
                    }
                }
                // Remove focus
                TAPAALGUI.getAppGui().requestFocus();
            }
        });

        JScrollPane animationHistoryScrollPane = new JScrollPane(animBox);

        //Add 10 pixel to the minimumsize of the scrollpane
        animationHistoryScrollPane.setMinimumSize(
            new Dimension(
                animationHistoryScrollPane.getMinimumSize().width,
                animationHistoryScrollPane.getMinimumSize().height + 20
            )
        );
        add(animationHistoryScrollPane, BorderLayout.CENTER);

        setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Simulation History"),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)
            )
        );
        
    }

}
