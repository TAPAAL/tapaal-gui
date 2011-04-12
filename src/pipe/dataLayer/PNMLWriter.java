package pipe.dataLayer;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.DOMException;

public interface PNMLWriter {
	public void savePNML(File file) throws NullPointerException, IOException,
			ParserConfigurationException, DOMException,
			TransformerConfigurationException, TransformerException;
}
