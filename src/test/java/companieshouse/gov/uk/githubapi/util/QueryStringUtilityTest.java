package companieshouse.gov.uk.githubapi.util;

import org.junit.jupiter.api.Test;

import static companieshouse.gov.uk.githubapi.util.QueryStringUtility.parseQueryString;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

public class QueryStringUtilityTest {
    @Test
    void testParseQueryString() {
        final String query = "one=two&three=four&one=five&six=1000";
        final Map<String, List<String>> expected = Map.of(
            "one", List.of("two", "five"),
            "three", List.of("four"),
            "six", List.of("1000")
        );

        assertThat(parseQueryString(query))
            .containsAllEntriesOf(expected);
    }
}
