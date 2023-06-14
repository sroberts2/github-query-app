package uk.gov.companieshouse.githubapi.service.mvn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.companieshouse.githubapi.util.PomUtils.loadDemoPom;

import uk.gov.companieshouse.githubapi.service.mvn.ParentVersionParseRule;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.w3c.dom.Document;

public class ParentVersionParseRuleTest {

    private final ParentVersionParseRule parentVersionParseRule = new ParentVersionParseRule();

    @Test
    void testWhenParentIsNotCompaniesHouseParentThenAddsAsADependency() throws Exception {
        final Document pom = loadDemoPom();

        final Map<String, String> result = parentVersionParseRule.run(pom);

        assertThat(result).containsEntry("spring-boot-starter-parent", "3.0.6").hasSize(1);
    }

    @Test
    void testWhenParentIsCompaniesHouseParentThenReturnsEmptyMap() throws Exception {
        final Document pom = loadDemoPom("classpath:poms/ch-parent-pom.xml");

        final Map<String, String> result = parentVersionParseRule.run(pom);

        assertThat(result).containsOnly(Map.entry("companies-house-parent", "1.3.1"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "classpath:poms/erroneous/parent-empty-pom.xml",
        "classpath:poms/erroneous/parent-no-groupid-pom.xml",
        "classpath:poms/erroneous/parent-no-artifactid-pom.xml"
    })
    void testWhenParentIsInvalidThenThrowsPoorlyFormattedPomException(String string) throws Exception {
        final Document erroneousPom = loadDemoPom(string);

        assertThatThrownBy(() -> parentVersionParseRule.run(erroneousPom)).hasMessageMatching("Dependency missing required parameter: (artifactId|groupId)");
    }
}
