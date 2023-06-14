package uk.gov.companieshouse.githubapi.service;

import static uk.gov.companieshouse.githubapi.util.QueryStringUtility.parseQueryString;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

public class GithubApiPaginationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GithubApiPaginationService.class);
    private static final Pattern NEXT_LINK_PATTERN = Pattern.compile(
            "rel=\"next\"",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    private static final Pattern LINK_PATTERN = Pattern.compile("<([^>]+)>", Pattern.DOTALL);

    private GithubApiPaginationService() {}

    /**
     * Paginate through the GitHub API responses.
     * @param pageSupplier The function to get a single page of results from the API
     * @return A list of all the items from the API
     */
    public static <T> List<T> paginateResponseFromApi(
            final Function<Integer, ResponseEntity<T>> pageSupplier
    ) {
        final List<T> allItems = new ArrayList<>();

        Optional<Integer> page = Optional.of(1);

        while (page.isPresent()) {
            final int pageNumber = page.get();
            LOGGER.debug("Loading page {} of results", pageNumber);
            
            final ResponseEntity<T> response = pageSupplier.apply(pageNumber);

            allItems.add(response.getBody());

            final HttpHeaders httpHeaders = response.getHeaders();

            final List<String> linkHeaders = httpHeaders.get("Link");
            final Stream<String> linkStream = linkHeaders == null
                    ? Stream.empty()
                    : linkHeaders.parallelStream();
            
            page = linkStream
                .flatMap(header -> Arrays.stream(header.split(",")))
                .filter(link -> NEXT_LINK_PATTERN.matcher(link).find())
                .map(link -> link.split(";"))
                .map(linkParts -> 
                        Arrays.stream(linkParts).map(String::strip).toArray(String[]::new)
                )
                .map(linkParts -> linkParts[0])
                .findFirst()
                .map(GithubApiPaginationService::getRequestedPageNumber);
        }

        return allItems;

    }

    private static int getRequestedPageNumber(final String url) {
        try {

            final Matcher linkMatcher = LINK_PATTERN.matcher(url);
            
            if (linkMatcher.matches()) {
                final String link = linkMatcher.group(1);
                
                final URL linkUrl = new URL(link);

                final Map<String, List<String>> queryString = parseQueryString(linkUrl.getQuery());

                if (queryString.containsKey("page")) {
                    return Integer.parseInt(queryString.get("page").get(0));
                }
            }
        } catch (final MalformedURLException urlException) {
            throw new RuntimeException(urlException);
        }

        throw new RuntimeException("Could not determine the page requested");
    }
}
