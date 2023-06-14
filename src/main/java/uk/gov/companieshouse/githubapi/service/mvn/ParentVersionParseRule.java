package uk.gov.companieshouse.githubapi.service.mvn;

import static uk.gov.companieshouse.githubapi.util.NameUtils.normaliseName;
import static uk.gov.companieshouse.githubapi.util.XmlUtils.getElementsByTagName;
import static uk.gov.companieshouse.githubapi.util.XmlUtils.loadDependencySpec;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import uk.gov.companieshouse.githubapi.model.DependencySpec;

@Component
public class ParentVersionParseRule implements ParseRule {

    @Override
    public Map<String, String> run(Document xmlDocument) {
        
        final Element root = xmlDocument.getDocumentElement();
        final Optional<Element> parent = getElementsByTagName(root, "parent");

        final Optional<DependencySpec> dependencySpec = loadDependencySpec(parent);

        return dependencySpec
            .flatMap(spec -> 
                spec.version()
                    .map(version -> Map.of(normaliseName(spec.artifactId()), version))
            ).orElse(Map.of());
    }
    
}
