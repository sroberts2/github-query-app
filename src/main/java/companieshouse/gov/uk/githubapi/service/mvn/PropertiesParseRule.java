package companieshouse.gov.uk.githubapi.service.mvn;

import static companieshouse.gov.uk.githubapi.util.NameUtils.normaliseName;
import static companieshouse.gov.uk.githubapi.util.XmlUtils.getNodeStream;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
            .flatMap(node -> Map.of(normaliseName(node.getNodeName(), "."), node.getTextContent()).entrySet().stream())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}
