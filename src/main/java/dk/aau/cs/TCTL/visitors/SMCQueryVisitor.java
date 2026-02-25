package dk.aau.cs.TCTL.visitors;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.TCTL.*;
import dk.aau.cs.verification.SMCSettings;
import dk.aau.cs.verification.observations.Observation;

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
    private static final String XML_NUMERIC_PFRECISION      = "numeric-precision";
    private static final String XML_OBSERVATIONS            = "observations";

    public String getXMLQueryFor(TCTLAbstractProperty property, String queryName, SMCSettings settings) {
        return getXMLQueryFor(property, queryName, settings, false);
    }

    public String getXMLQueryFor(TCTLAbstractProperty property, String queryName, SMCSettings settings, boolean legacy) {
        buildXMLQuery(property, queryName, settings, true, legacy);
        return getFormatted();
    }

    public void buildXMLQuery(TCTLAbstractProperty property, String queryName, SMCSettings settings) {
        buildXMLQuery(property, queryName, settings, false, false);
    }

    public void buildXMLQuery(TCTLAbstractProperty property, String queryName, SMCSettings settings, boolean discardDisabled) {
        buildXMLQuery(property, queryName, settings, discardDisabled, false);
    }

    public void buildXMLQuery(TCTLAbstractProperty property, String queryName, SMCSettings settings, boolean discardDisabled, boolean legacy) {
        this.legacy = legacy;
        xmlQuery.append(startTag(XML_PROP) + queryInfo(queryName) + smcTag(settings));

        List<Observation> observations = settings.getObservations();
        if (!observations.isEmpty()) {
            xmlQuery.append(observationTag(observations, discardDisabled, legacy));
        }
            
        xmlQuery.append(startTag(XML_FORMULA));
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

        tagContent += tagAttribute(XML_NUMERIC_PFRECISION, settings.getNumericPrecision());

        return emptyElement(tagContent);
    }

    private String observationTag(List<Observation> observations, boolean discardDisabled, boolean legacy) {
        String observationXml = startTag(XML_OBSERVATIONS); 
        List<Observation> observationsCopy = new ArrayList<>(observations);
        if (discardDisabled) {
            observationsCopy.removeIf(observation -> !observation.isEnabled());
        }

        for (Observation observation : observationsCopy) {
            observationXml += observation.toXml(legacy);
        }

        observationXml += endTag(XML_OBSERVATIONS);

        return observationXml;
    }

    private String tagAttribute(String name, float value) {
        return " " + name + "=\"" + String.valueOf(value) + "\"";
    }

    private String tagAttribute(String name, long value) {
        return " " + name + "=\"" + Long.toUnsignedString(value) + "\"";
    }

    private String tagAttribute(String name, int value) {
        return " " + name + "=\"" + String.valueOf(value) + "\"";
    }
}
