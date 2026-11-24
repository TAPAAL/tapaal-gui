package net.tapaal.gui;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;

/**
 * A JAXP transformer factory that cannot create an identity transformer.
 *
 * <p>Selecting it through the standard {@code javax.xml.transform.TransformerFactory} system
 * property makes the production writer fail inside its own catch blocks without changing the code
 * under test. Everything except {@link #newTransformer()} delegates to the platform factory.
 */
public final class UnavailableTransformerFactory extends TransformerFactory {
    static final String PROPERTY = TransformerFactory.class.getName();
    public static final String MESSAGE = "Transformer disabled by test";

    private final TransformerFactory platform = TransformerFactory.newDefaultInstance();

    /** Selects this factory until the returned handle is closed. */
    public static AutoCloseable select() {
        String previous = System.setProperty(PROPERTY, UnavailableTransformerFactory.class.getName());
        return () -> {
            if (previous == null) {
                System.clearProperty(PROPERTY);
            } else {
                System.setProperty(PROPERTY, previous);
            }
        };
    }

    @Override
    public Transformer newTransformer() throws TransformerConfigurationException {
        throw new TransformerConfigurationException(MESSAGE);
    }

    @Override
    public Transformer newTransformer(Source source) throws TransformerConfigurationException {
        return platform.newTransformer(source);
    }

    @Override
    public Templates newTemplates(Source source) throws TransformerConfigurationException {
        return platform.newTemplates(source);
    }

    @Override
    public Source getAssociatedStylesheet(Source source, String media, String title, String charset)
        throws TransformerConfigurationException {
        return platform.getAssociatedStylesheet(source, media, title, charset);
    }

    @Override
    public void setURIResolver(URIResolver resolver) {
        platform.setURIResolver(resolver);
    }

    @Override
    public URIResolver getURIResolver() {
        return platform.getURIResolver();
    }

    @Override
    public void setFeature(String name, boolean value) throws TransformerConfigurationException {
        platform.setFeature(name, value);
    }

    @Override
    public boolean getFeature(String name) {
        return platform.getFeature(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        platform.setAttribute(name, value);
    }

    @Override
    public Object getAttribute(String name) {
        return platform.getAttribute(name);
    }

    @Override
    public void setErrorListener(ErrorListener listener) {
        platform.setErrorListener(listener);
    }

    @Override
    public ErrorListener getErrorListener() {
        return platform.getErrorListener();
    }
}
