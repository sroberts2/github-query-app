package uk.gov.companieshouse.githubapi.service.mvn;

import static uk.gov.companieshouse.githubapi.util.NameUtils.normaliseName;
import static uk.gov.companieshouse.githubapi.util.StreamUtils.collectToMap;
import static uk.gov.companieshouse.githubapi.util.XmlUtils.getNodeStream;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Component
public class PropertiesParseRule implements ParseRule {

    @Override
    public Map<String, String> run(final Document xmlDocument) {
        
        // Find the <properties> element
        final Element root = xmlDocument.getDocumentElement();
        final NodeList propertiesList = root.getElementsByTagName("properties");
        final Element properties = (Element) propertiesList.item(0);

        if (properties == null) {
            return Map.of();
        }

        return getNodeStream(properties.getChildNodes())
            .filter(node -> node.getNodeName().toLowerCase().strip().endsWith(".version"))
            .flatMap(this::toSingletonMap)
            .collect(collectToMap());
    }

    private Stream<Entry<String, String>> toSingletonMap(final Node node) {
        return Map.of(
                normaliseName(node.getNodeName(), "."),
                node.getTextContent()
        )
            .entrySet()
            .stream();
    }

}
