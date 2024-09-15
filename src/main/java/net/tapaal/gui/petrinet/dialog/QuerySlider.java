package net.tapaal.gui.petrinet.dialog;

import javax.swing.JSlider;
import javax.swing.JTextField;

public class QuerySlider extends JSlider {
    private double realValue;
    private double desiredMin;
    private double desiredMax;

    public QuerySlider(int value, double desiredMin, double desiredMax) {
        this(value, desiredMin, desiredMax, 100);
    }

    public QuerySlider(int value, double desiredMin, double desiredMax, int resolution) {
        super(0, resolution, value);
        this.desiredMin = desiredMin;
        this.desiredMax = desiredMax;
    }

    public void setRealValue(double realValue) {
        this.realValue = realValue;
    }

    public double getRealValue() {
        return realValue;
    }

    public double getDesiredMin() {
        return desiredMin;
    }

    public double getDesiredMax() {
        return desiredMax;
    }

    public void updateValue(JTextField textField, int precision) {
        int value = getValue();
        double desiredMin = getDesiredMin();
        double desiredMax = getDesiredMax();
        double proportion = (double) value / getMaximum();
        double interpretedValue = desiredMin + proportion * (desiredMax - desiredMin);
        double scale = Math.pow(10, precision);
        double roundedValue = Math.round(interpretedValue * scale) / scale;
        textField.setText(roundedValue + "");
        setRealValue(roundedValue);
        setToolTipText(String.format("Value: %." + precision + "f", roundedValue));
    }
}
