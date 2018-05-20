package org.swissbib.mf.pipe.xslt;

import org.metafacture.framework.MetafactureException;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.helpers.DefaultObjectPipe;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Created by swissbib on 29.05.17.
 */
public class XSLTPipeStep extends DefaultObjectPipe<XSLTObject, ObjectReceiver<XSLTObject>> {

    private String templatePath;
    private Pattern linePattern = Pattern.compile("<record .*?>.*?</record>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    Transformer transformer;


    public void setTemplate(String templatePath) {

        Optional<Transformer> oT =  new TransformerBuilder().buildTransformer(templatePath);
        //oT. ifPresent(val -> transformer = val);
        if (oT.isPresent()) {
            transformer = oT.get();
        } else {
            throw new MetafactureException("blbl");
        }

    }


    public void setUseLineWith(String regex) {
        linePattern = Pattern.compile(regex,Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    }

    @Override
    public void process(XSLTObject obj) {

        if (linePattern.matcher(obj.record).find()) {
            Source recordSource =  new StreamSource(new StringReader(obj.record));
            StringWriter recordTargetBuffer = new StringWriter();
            StreamResult recordTarget = new StreamResult(recordTargetBuffer);

            try {

                transformer.transform(recordSource, recordTarget);
                obj.record =  recordTargetBuffer.toString();
                getReceiver().process(obj);

            } catch (TransformerException transException) {
                transException.printStackTrace();
            }


        }

    }



}
