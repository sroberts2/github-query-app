package uk.gov.companieshouse.githubapi.service;

import static uk.gov.companieshouse.githubapi.util.StreamUtils.collectToMap;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import uk.gov.companieshouse.githubapi.exception.PoolyFormattedDependenciesFileException;
import uk.gov.companieshouse.githubapi.service.mvn.ParseRule;

@Component
public class MavenDependenciesParsingService {

    private static final Pattern PROPERTY_VALUE_CONSUMER_PATTERN =
            Pattern.compile("\\$\\{[^\\}]+\\}");
    
    private final List<ParseRule> parseRules;
    private final DocumentBuilderFactory documentBuilderFactory;

    @Autowired
    public MavenDependenciesParsingService(
            final List<ParseRule> parseRules,
            final DocumentBuilderFactory documentBuilderFactory
    ) {
        this.parseRules = parseRules;
        this.documentBuilderFactory = documentBuilderFactory;
    }

    /**
     * Parse the dependencies file supplied.
     * @param dependenciesFile Contents of a Maven dependencies file (i.e. pom.xml)
     * @return Map containing the dependencies from the supplied file (keyed on dependency name)
     */
    public Map<String, String> parseDependencies(final String dependenciesFile) {
        try {
            final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            final InputSource inputSource = new InputSource(new StringReader(dependenciesFile));

            final Document document = documentBuilder.parse(inputSource);

            return parseRules.stream()
                .flatMap(rule -> rule.run(document).entrySet().stream())
                .filter(entry -> 
                        !PROPERTY_VALUE_CONSUMER_PATTERN.matcher(entry.getValue()).matches()
                )
                .collect(collectToMap());
        } catch (
        final ParserConfigurationException | IOException | SAXException 
                parserConfigurationException
        ) {
            throw new PoolyFormattedDependenciesFileException(
                    "Could not parse POM",
                    parserConfigurationException
            );
        }
    }
    
}
