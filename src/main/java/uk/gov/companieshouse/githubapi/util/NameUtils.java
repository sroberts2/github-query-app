package uk.gov.companieshouse.githubapi.util;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class NameUtils {
    
    private static final List<String> UNDESIRED_SUFFIXES = List.of(
            "dependencies",
            "version"
    );

    private static final Pattern UNDESIRED_CHARACTERS_PATTERN = Pattern.compile("[^A-Za-z0-9\\-]");

    public static String normaliseName(final String name) {
        return normaliseName(name, "-");
    }

    /**
     * Removes undesired suffixes from the name and replaces any undesired characters with the
     * name supplied.
     * @param name The name to normalise
     * @param separator The separator separating words in the name
     * @return The name without any undesired suffixes and with any undesired characters replaced
     */
    public static String normaliseName(final String name, final String separator) {
        final String canonicalSeparator = separator.equals(".") ? "\\." : separator;

        final String reversed = StringUtils.reverseDelimited(name, separator.charAt(0));

        final String[] parts = reversed.split(canonicalSeparator, 2);

        final String minusSuffix = UNDESIRED_SUFFIXES.stream()
                .filter(suffix -> suffix.equals(parts[0]))
                .map(suffix -> StringUtils.reverseDelimited(parts[1], separator.charAt(0)))
                .findFirst()
                .orElse(name);
        
        return UNDESIRED_CHARACTERS_PATTERN.matcher(minusSuffix).replaceAll("-");
    }
}
