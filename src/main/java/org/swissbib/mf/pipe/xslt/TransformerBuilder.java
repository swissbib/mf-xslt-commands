package org.swissbib.mf.pipe.xslt;


import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.util.Optional;

class TransformerBuilder {

    Optional<Transformer> buildTransformer(String path) {
        System.setProperty("javax.xml.transform.TransformerFactory","net.sf.saxon.TransformerFactoryImpl");

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        StreamSource source = null;

        Transformer transformer = null;
        if (new File(path).exists()) {
            source = new StreamSource(path);
            try {
                transformer = transformerFactory.newTransformer(source);
            } catch (TransformerConfigurationException ex) {
                ex.printStackTrace();

            }
        }

        return Optional.ofNullable(transformer);

    }

}
