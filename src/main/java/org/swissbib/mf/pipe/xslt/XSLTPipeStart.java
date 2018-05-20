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
 * Created by swissbib on 26.05.17.
 */
public class XSLTPipeStart extends DefaultObjectPipe<String, ObjectReceiver<XSLTObject>> {


    private String templatePath;
    private String weedingTemplatePath;
    private String holdingsTemplatePath;
    private Pattern linePattern = Pattern.compile("<record .*?>.*?</record>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    private Transformer transformer;
    Transformer weedingTransformer;
    Transformer holdingsTransformer;

    public void setTemplate(String templatePath) {

        Optional<Transformer> oT =  new TransformerBuilder().buildTransformer(templatePath);
        //oT. ifPresent(val -> transformer = val);
        if (oT.isPresent()) {
            transformer = oT.get();
        } else {
            throw new MetafactureException("blbl");
        }
    }


    public void setWeedingTemplate(String templatePath) {

        Optional<Transformer> oT =  new TransformerBuilder().buildTransformer(templatePath);
        //oT. ifPresent(val -> transformer = val);
        if (oT.isPresent()) {
            weedingTransformer = oT.get();
        } else {
            throw new MetafactureException("blbl");
        }


    }


    public void setHoldingsTemplate(String templatePath) {

        Optional<Transformer> oT =  new TransformerBuilder().buildTransformer(templatePath);
        //oT. ifPresent(val -> transformer = val);
        if (oT.isPresent()) {
            holdingsTransformer = oT.get();
        } else {
            throw new MetafactureException("blbl");
        }

    }


    public void setUseLineWith(String regex) {
        linePattern = Pattern.compile(regex,Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    }

    @Override
    public void process(String obj) {
        if (linePattern.matcher(obj).find()) {

            Source sourceHoldings = new StreamSource(new StringReader(obj));
            Source sourceWeeding = new StreamSource(new StringReader(obj));

            StringWriter writerHoldings = new StringWriter();
            StringWriter weededRecord = new StringWriter();


            //we fetch all the holdings of a bibliographic record
            Result xsltResultHoldings = new StreamResult(writerHoldings);

            try {

                holdingsTransformer.transform(sourceHoldings,xsltResultHoldings);
                //System.out.println(holdings.toString());

                //after we fetched the holdings we are now going to "weed" the holdings
                //part of the holdings-information should be in the basic bibliographic record we are going to use
                //to present information in the result list
                //the complete holdings information is only needed for the full view of a single record
                Result xsltResultWeeding = new StreamResult(weededRecord);
                this.weedingTransformer.transform(sourceWeeding,xsltResultWeeding);
                //System.out.println(weededRecord.toString());

                Source weededSource = new StreamSource(new StringReader(weededRecord.toString()));




                StringWriter recordTargetBuffer = new StringWriter();
                StreamResult recordTarget = new StreamResult(recordTargetBuffer);


                transformer.transform(weededSource, recordTarget);
                XSLTObject dataObject = new XSLTObject();
                dataObject.record = recordTargetBuffer.toString();
                dataObject.holdings = writerHoldings.toString();
                getReceiver().process(dataObject);


            } catch (TransformerException transException) {
                transException.printStackTrace();
            }


        }

    }

}
