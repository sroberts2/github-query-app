package uk.gov.companieshouse.githubapi.util;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.companieshouse.githubapi.util.NameUtils.normaliseName;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class NameUtilsTest {
    @ParameterizedTest
    @CsvSource({
        "spring-boot-parent,spring-boot-parent",
        "spring-boot,spring-boot",
        "companieshouse-parent,companieshouse-parent",
        "spring-boot-dependencies,spring-boot",
        "java,java",
        "http-client,http-client"
    })
    void testNormaliseNameWithNoSeparatorUsesHyphen(final String input, final String expected) {
        final String normalisedName = normaliseName(input);

        assertThat(normalisedName).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
        "spring.boot.version,.,spring-boot",
        "java,.,java",
        "http.client.version,.,http-client",
        "http=client=version,=,http-client",
        "http client,=,http-client",
        "commons-lang3.version,.,commons-lang3"
    })
    void testNormaliseNameWithSeparator(final String input, final String separator, final String expected) {
        final String normalisedName = normaliseName(input, separator);

        assertThat(normalisedName).isEqualTo(expected);
    }
}
