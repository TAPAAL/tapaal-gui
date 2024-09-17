package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.*;
import dk.aau.cs.verification.SMCSettings;

public class SMCQueryVisitor extends LTLQueryVisitor {

    private static final String XML_SMC		    	        = "smc";
    private static final String XML_TIME_BOUND_TAG          = "time-bound";
    private static final String XML_STEP_BOUND_TAG          = "step-bound";
    private static final String XML_FALSE_POS_TAG           = "false-positives";
    private static final String XML_FALSE_NEG_TAG           = "false-negatives";
    private static final String XML_INDIFFERENCE_TAG        = "indifference";
    private static final String XML_CONFIDENCE_TAG          = "confidence";
    private static final String XML_INTERVAL_WIDTH_TAG      = "interval-width";
    private static final String XML_COMPARE_TO_FLOAT_TAG    = "compare-to";

    public String getXMLQueryFor(TCTLAbstractProperty property, String queryName, SMCSettings settings) {
        buildXMLQuery(property, queryName, settings);
        return getFormatted();
    }

    public void buildXMLQuery(TCTLAbstractProperty property, String queryName, SMCSettings settings) {
        xmlQuery.append(startTag(XML_PROP) + queryInfo(queryName) + smcTag(settings) + startTag(XML_FORMULA));
        property.accept(this, null);
        xmlQuery.append(endTag(XML_FORMULA) + endTag(XML_PROP));
    }

    private String smcTag(SMCSettings settings) {
        String tagContent = XML_SMC;
        if(settings.timeBound < Integer.MAX_VALUE)
            tagContent += tagAttribute(XML_TIME_BOUND_TAG, settings.timeBound);
        if(settings.stepBound < Integer.MAX_VALUE)
            tagContent += tagAttribute(XML_STEP_BOUND_TAG, settings.stepBound);
        if(settings.compareToFloat) {
            tagContent += tagAttribute(XML_FALSE_POS_TAG, settings.falsePositives);
            tagContent += tagAttribute(XML_FALSE_NEG_TAG, settings.falseNegatives);
            tagContent += tagAttribute(XML_INDIFFERENCE_TAG, settings.indifferenceWidth);
            tagContent += tagAttribute(XML_COMPARE_TO_FLOAT_TAG, settings.geqThan);
        } else {
            tagContent += tagAttribute(XML_CONFIDENCE_TAG, settings.confidence);
            tagContent += tagAttribute(XML_INTERVAL_WIDTH_TAG, settings.estimationIntervalWidth);
        }
        return emptyElement(tagContent);
    }

    private String tagAttribute(String name, String value) {
        return " " + name + "=\"" + value + "\"";
    }

    private String tagAttribute(String name, float value) {
        return " " + name + "=\"" + String.valueOf(value) + "\"";
    }

    private String tagAttribute(String name, int value) {
        return " " + name + "=\"" + String.valueOf(value) + "\"";
    }
}
