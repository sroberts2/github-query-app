package companieshouse.gov.uk.githubapi.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class QueryStringUtility {

    
    private QueryStringUtility() {}

    public static Map<String, List<String>> parseQueryString(final String query) {
        final Map<String, List<String>> parsedQueryString = new HashMap<>();

        // TODO: Tried to use collectors and reducers but nothing seemed to compile
        // This seems to be the only way to convert the list of query parameters
        Arrays.stream(query.split("&"))
            .map(parameter ->  parameter.split("=", 2))
            .forEach(parameter -> {
                if (parsedQueryString.containsKey(parameter[0])) {
                    parsedQueryString.get(parameter[0]).add(parameter[1]);
                } else {
                    final List<String> values = new ArrayList<>();

                    values.add(parameter[1]);

                    parsedQueryString.put(parameter[0], values);
                }
            });

        return parsedQueryString;
    }
}
