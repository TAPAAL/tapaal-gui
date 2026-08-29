package net.tapaal.gui.petrinet.dialog;

import java.math.BigDecimal;

import javax.swing.JSlider;
import javax.swing.JTextField;

public class QuerySlider extends JSlider {
    private double realValue;
    private double desiredMin;
    private double desiredMax;
    private boolean isLog;

    public QuerySlider(int value, double desiredMin, double desiredMax) {
        this(value, desiredMin, desiredMax, 100, false);
    }

    public QuerySlider(int value, double desiredMin, double desiredMax, boolean isLog) {
        this(value, desiredMin, desiredMax, 100, isLog);
    }

    public QuerySlider(int value, double desiredMin, double desiredMax, int resolution) {
        this(value, desiredMin, desiredMax, resolution, false);
    }

    public QuerySlider(int value, double desiredMin, double desiredMax, int resolution, boolean isLog) {
        super(0, resolution, value);
        this.desiredMin = desiredMin;
        this.desiredMax = desiredMax;
        this.isLog = isLog;
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
    
    public boolean isLog() {
        return isLog;
    }

    public void updateValue(JTextField textField, int precision) {
        int value = getValue();
        double desiredMin = getDesiredMin();
        double desiredMax = getDesiredMax();
        double proportion = (double) value / getMaximum();
        
        double interpretedValue;
        if (isLog) {
            double logMin = Math.log(desiredMin);
            double logMax = Math.log(desiredMax);
            interpretedValue = Math.exp(logMin + proportion * (logMax - logMin));
        } else {
            interpretedValue = desiredMin + proportion * (desiredMax - desiredMin);
        }

        double scale = Math.pow(10, precision);
        double roundedValue = Math.round(interpretedValue * scale) / scale;
        textField.setText(formatPlain(roundedValue));
        setRealValue(roundedValue);
        setToolTipText(String.format("Value: %." + precision + "f", roundedValue));
    }

    private String formatPlain(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }
}