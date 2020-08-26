package dk.aau.cs.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import dk.aau.cs.TCTL.Parsing.ParseException;

public class ModelLoader {

	public ModelLoader(){
	}
	
	public LoadedModel load(File file) throws Exception{		
		TapnXmlLoader newFormatLoader = new TapnXmlLoader();
		try{
            return newFormatLoader.load(file);
        }catch(Throwable e1){
			try {
				TapnLegacyXmlLoader oldFormatLoader = new TapnLegacyXmlLoader();
                return oldFormatLoader.load(file);
            } catch(Throwable e2) {
				throw new ParseException(e1.getMessage());
			}
		}
	}


    public LoadedModel load(InputStream file) throws Exception{
		TapnXmlLoader newFormatLoader = new TapnXmlLoader();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len;
		try {
			while ((len = file.read(buffer)) > -1 ) {
			    baos.write(buffer, 0, len);
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		try{

            return newFormatLoader.load(new ByteArrayInputStream(baos.toByteArray()));

        }catch(Throwable e1){
			try {
				TapnLegacyXmlLoader oldFormatLoader = new TapnLegacyXmlLoader();

                return oldFormatLoader.load(new ByteArrayInputStream(baos.toByteArray()));

            } catch(Throwable e2) {
				throw new ParseException(e1.getMessage());
			}
		}
	}
	
}
