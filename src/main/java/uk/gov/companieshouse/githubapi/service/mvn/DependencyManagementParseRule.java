package uk.gov.companieshouse.githubapi.service.mvn;

import static uk.gov.companieshouse.githubapi.util.NameUtils.normaliseName;
import static uk.gov.companieshouse.githubapi.util.StreamUtils.collectToMap;
import static uk.gov.companieshouse.githubapi.util.XmlUtils.getElementsByTagName;
import static uk.gov.companieshouse.githubapi.util.XmlUtils.getNodeStream;
import static uk.gov.companieshouse.githubapi.util.XmlUtils.loadDependencySpec;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@Component
public class DependencyManagementParseRule implements ParseRule {

    @Override
    public Map<String, String> run(final Document xmlDocument) {
        
        final Element root = xmlDocument.getDocumentElement();
        final Optional<Element> dependencyManagement = 
                getElementsByTagName(root, "dependencyManagement");

        return dependencyManagement.map(
            element -> {
                final NodeList children = element.getElementsByTagName("dependencies");
                final Element dependencies = (Element) children.item(0);

                return getNodeStream(dependencies.getChildNodes(), "dependency")
                    .map(node -> loadDependencySpec((Element) node))
                    .filter(spec -> spec.version().isPresent())
                    .map(spec -> Map.of(normaliseName(spec.artifactId()), spec.version().get()))
                    .flatMap(map -> map.entrySet().stream())
                    .collect(collectToMap());
            }
        ).orElseGet(() -> Map.of());
    }
}
