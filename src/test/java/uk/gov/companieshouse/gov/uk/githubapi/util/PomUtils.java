package uk.gov.companieshouse.gov.uk.githubapi.util;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.util.ResourceUtils;
import org.w3c.dom.Document;

public class PomUtils {
    

    private PomUtils() {}
 
    public static Document loadDemoPom() throws Exception {
        return loadDemoPom("classpath:demopom.xml");
    }

    public static Document loadDemoPom(final String resourcePath) throws Exception {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        return documentBuilder.parse(ResourceUtils.getFile(resourcePath));
    }
}
