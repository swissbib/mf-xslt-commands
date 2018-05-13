package org.swissbib.mf.pipe.xslt;

import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.helpers.DefaultObjectPipe;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.regex.Pattern;

/**
 * Created by swissbib on 26.05.17.
 */
public class XSLTPipeStart extends DefaultObjectPipe<String, ObjectReceiver<XSLTObject>> {


    private String templatePath;
    private String weedingTemplatePath;
    private String holdingsTemplatePath;
    private Pattern linePattern = Pattern.compile("<record .*?>.*?</record>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    Transformer transformer;
    Transformer weedingTransformer;
    Transformer holdingsTransformer;

    public void setTemplate(String templatePath) {

        System.setProperty("javax.xml.transform.TransformerFactory","net.sf.saxon.TransformerFactoryImpl");


        this.templatePath = templatePath;

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        StreamSource source = null;

        if (new File(templatePath).exists()) {
            source = new StreamSource(templatePath);
            try {
                transformer = transformerFactory.newTransformer(source);
            } catch (TransformerConfigurationException ex) {
                ex.printStackTrace();

            }
        }



    }


    public void setWeedingTemplate(String templatePath) {

        System.setProperty("javax.xml.transform.TransformerFactory","net.sf.saxon.TransformerFactoryImpl");


        this.weedingTemplatePath = templatePath;

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        StreamSource source = null;

        if (new File(templatePath).exists()) {
            source = new StreamSource(templatePath);
            try {
                this.weedingTransformer = transformerFactory.newTransformer(source);
            } catch (TransformerConfigurationException ex) {
                ex.printStackTrace();

            }
        }



    }


    public void setHoldingsTemplate(String templatePath) {

        System.setProperty("javax.xml.transform.TransformerFactory","net.sf.saxon.TransformerFactoryImpl");


        this.holdingsTemplatePath = templatePath;

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        StreamSource source = null;

        if (new File(templatePath).exists()) {
            source = new StreamSource(templatePath);

            try {
                this.holdingsTransformer = transformerFactory.newTransformer(source);
            } catch (TransformerConfigurationException ex) {
                ex.printStackTrace();

            }

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
