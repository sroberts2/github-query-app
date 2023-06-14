package uk.gov.companieshouse.githubapi.util;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public final class StreamUtils {
    
    private StreamUtils() {}

    /**
     * Creates a collector capable of combining a Stream of Map.Entry objects
     * into a single Map.
     * @return Collector capable of creating a Map from a Stream of Entries
     */
    public static Collector<Entry<String, String>, ?, Map<String, String>> collectToMap() {
        return Collectors.toMap(
                Map.Entry::getKey, Map.Entry::getValue, (existing, replacement) -> existing
        );
    }
}
