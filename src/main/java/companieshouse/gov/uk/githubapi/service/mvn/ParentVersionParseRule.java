package companieshouse.gov.uk.githubapi.service.mvn;

import static companieshouse.gov.uk.githubapi.util.XmlUtils.loadDependencySpec;
import static companieshouse.gov.uk.githubapi.util.XmlUtils.getElementsByTagName;
import static companieshouse.gov.uk.githubapi.util.NameUtils.normaliseName;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import companieshouse.gov.uk.githubapi.model.DependencySpec;

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
