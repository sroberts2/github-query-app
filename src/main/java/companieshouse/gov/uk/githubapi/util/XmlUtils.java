package companieshouse.gov.uk.githubapi.util;

import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import companieshouse.gov.uk.githubapi.exception.PoolyFormattedDependenciesFileException;
import companieshouse.gov.uk.githubapi.model.DependencySpec;

public final class XmlUtils {
    
    private XmlUtils() {}

    public static Optional<DependencySpec> loadDependencySpec(final Optional<Element> element) {
        return element.map(XmlUtils::loadDependencySpec);
    }

    public static DependencySpec loadDependencySpec(final Element element) {
        final NodeList nodeList = element.getChildNodes();
        
        final Optional<String> groupId = getChildValue(nodeList, "groupId");
        final Optional<String> artifactId = getChildValue(nodeList, "artifactId");
        final Optional<String> version = getChildValue(nodeList, "version");

        return groupId
            .flatMap(group -> artifactId.map(artifact -> new DependencySpec(group, artifact, version)))
            .orElseThrow(() -> 
                new PoolyFormattedDependenciesFileException(
                    "Dependency missing required parameter: artifactId"
                )
            );
    }

    public static Optional<Element> getElementsByTagName(final Element element, final String tagName) {
        final NodeList parentNodeList = element.getElementsByTagName(tagName);

        return Optional.ofNullable((Element) parentNodeList.item(0));
    } 

    public static Stream<Node> getNodeStream(final NodeList nodeList, final String expectedTagName) {
        return getNodeStream(nodeList)
            .filter(node -> node.getNodeName().strip().equals(expectedTagName));
    }

    public static Stream<Node> getNodeStream(final NodeList nodeList) {
        return IntStream.range(0, nodeList.getLength())
            .boxed()
            .map(index -> nodeList.item(index));
    }

    private static Optional<String> getChildValue(final NodeList childNodes, final String tag) {
        return getNodeStream(childNodes, tag)
            .map(Node::getTextContent)
            .findFirst();
    }
}
