package companieshouse.gov.uk.githubapi.util;

import companieshouse.gov.uk.githubapi.exception.PoolyFormattedDependenciesFileException;
import companieshouse.gov.uk.githubapi.model.DependencySpec;

import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class XmlUtils {
    
    private XmlUtils() {}

    public static Optional<DependencySpec> loadDependencySpec(final Optional<Element> element) {
        return element.map(XmlUtils::loadDependencySpec);
    }

    /**
     * From the supplied Element, load the DependencySpec.
     * @param element The element to load the DependencySpec from
     * @return The DependencySpec
     */
    public static DependencySpec loadDependencySpec(final Element element) {
        final NodeList nodeList = element.getChildNodes();
        
        final Optional<String> groupId = getChildValue(nodeList, "groupId");
        final Optional<String> artifactId = getChildValue(nodeList, "artifactId");
        final Optional<String> version = getChildValue(nodeList, "version");

        return groupId
                .flatMap(group -> artifactId.map(
                        artifact -> new DependencySpec(group, artifact, version)
                ))
                .orElseThrow(() -> 
                    new PoolyFormattedDependenciesFileException(
                        "Dependency missing required parameter: artifactId"
                    )
                );
    }

    /**
     * Get the first element with the supplied tag name.
     * @param element The element to search
     * @param tagName The tag name to search for
     * @return The first element with the supplied tag name
     */
    public static Optional<Element> getElementsByTagName(
            final Element element, final String tagName
    ) {
        final NodeList parentNodeList = element.getElementsByTagName(tagName);

        return Optional.ofNullable((Element) parentNodeList.item(0));
    } 

    /**
     * Get the Child Nodes from the nodeList with tag name matching the expectedTagName.
     * @param nodeList The nodeList to search
     * @param expectedTagName The tag name to search for
     * @return The Nodes from the nodeList with tag name matching the expectedTagName
     */
    public static Stream<Node> getNodeStream(
            final NodeList nodeList, final String expectedTagName
    ) {
        return getNodeStream(nodeList)
                .filter(node -> node.getNodeName().strip().equals(expectedTagName));
    }

    /**
     * Get the Child Nodes from the nodeList.
     * @param nodeList The nodeList to search
     * @return The Nodes from the nodeList with tag name matching the expectedTagName
     */
    public static Stream<Node> getNodeStream(final NodeList nodeList) {
        return IntStream.range(0, nodeList.getLength())
                .boxed()
                .map(index -> nodeList.item(index));
    }

    /**
     * Get the value of the first child node with the supplied tag name.
     * @param childNodes The child nodes to search
     * @param tag The tag name to search for
     * @return The value of the first child node with the supplied tag name
     */
    private static Optional<String> getChildValue(final NodeList childNodes, final String tag) {
        return getNodeStream(childNodes, tag)
            .map(Node::getTextContent)
            .findFirst();
    }
}
