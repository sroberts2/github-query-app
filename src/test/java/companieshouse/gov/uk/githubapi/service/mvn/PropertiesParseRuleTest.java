package companieshouse.gov.uk.githubapi.service.mvn;

import static companieshouse.gov.uk.githubapi.util.PomUtils.loadDemoPom;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

public class PropertiesParseRuleTest {
    

    private static Document demoPom;

    private final PropertiesParseRule propertiesParseRule = new PropertiesParseRule();

    @BeforeAll
    static void setupClass() throws Exception {
        demoPom = loadDemoPom();
    }

    @Test
    void testLoadsVersionsFromPropertiesSectionOfPom() {
        final Map<String, String> result = propertiesParseRule.run(demoPom);

        assertThat(result).containsExactlyInAnyOrderEntriesOf(Map.of(
            "java", "17",
            "http-client", "5.2.1",
            "apache-commons-lang3", "3.12.0"
        ));
    }

    @Test
    void testWhenPropertiesEmptyThenReturnsEmptyMap() throws Exception {
        final Document noProperties = loadDemoPom("classpath:poms/no-properties-pom.xml");

        final Map<String, String> result = propertiesParseRule.run(noProperties);

        assertThat(result).isEmpty();
    }

    @Test
    void testWhenNoPropertiesTagThenReturnsEmptyMap() throws Exception {
        final Document noProperties = loadDemoPom("classpath:poms/no-properties-tag-pom.xml");

        final Map<String, String> result = propertiesParseRule.run(noProperties);

        assertThat(result).isEmpty();

    }
}
