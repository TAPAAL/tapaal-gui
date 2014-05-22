package pipe.dataLayer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.DOMException;

public interface NetWriter {
	void savePNML(File file) throws NullPointerException, IOException,
			ParserConfigurationException, DOMException,
			TransformerConfigurationException, TransformerException;

	ByteArrayOutputStream savePNML() throws IOException, ParserConfigurationException, DOMException, TransformerConfigurationException, TransformerException;
}
