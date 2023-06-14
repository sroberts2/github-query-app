package uk.gov.companieshouse.gov.uk.githubapi.service.mvn;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.companieshouse.gov.uk.githubapi.util.PomUtils.loadDemoPom;

import uk.gov.companieshouse.githubapi.service.mvn.DependencyManagementParseRule;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

public class DependencyManagementParseRuleTest {

    private final DependencyManagementParseRule dependencyManagementParseRule = new DependencyManagementParseRule();

    @Test
    void testReturnsEmptyMapWhenNoDependencyManagmentBlock() throws Exception {
        final Document pom = loadDemoPom();

        final Map<String, String> result = dependencyManagementParseRule.run(pom);

        assertThat(result).isEmpty();
    }

    @Test
    void testReturnsMapOfVersionsWhenThereIsADependencyManagementBlock() throws Exception {
        final Document pom = loadDemoPom("classpath:poms/dependency-management-pom.xml");

        final Map<String, String> result = dependencyManagementParseRule.run(pom);

        assertThat(result)
            .containsExactlyInAnyOrderEntriesOf(Map.of(
                "spring-boot", "3.1.0",
                "another-one", "233.1.0"
            )
            );
    }
}
