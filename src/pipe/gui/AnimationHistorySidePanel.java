package pipe.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AnimationHistorySidePanel extends JPanel {

    private AnimationHistoryList animBox;

    public AnimationHistorySidePanel() {
        super();

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
        animBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    int selected = animBox.getSelectedIndex();
                    int clicked = animBox.locationToIndex(e.getPoint());

                    if (clicked != -1) {
                        int steps = clicked - selected;
                        Animator anim = CreateGui.getAnimator();
                        if (steps < 0) {
                            for (int i = 0; i < Math.abs(steps); i++) {
                                anim.stepBack();
                            }
                        } else {
                            for (int i = 0; i < Math.abs(steps); i++) {
                                anim.stepForward();
                            }
                        }

                        anim.blinkSelected(animBox.getSelectedValue());
                    }
                }
                // Remove focus
                CreateGui.getApp().requestFocus();
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
