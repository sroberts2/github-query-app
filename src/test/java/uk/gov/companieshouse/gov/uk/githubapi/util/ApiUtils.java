package uk.gov.companieshouse.gov.uk.githubapi.util;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.web.util.UriComponentsBuilder;

public class ApiUtils {
    
    private ApiUtils() {}

    public static <T> ResponseEntity<T> makeResponseEntity(
        final T response,
        final Optional<String> linkHeaders
    ) {
        final BodyBuilder builder = ResponseEntity.ok();

        linkHeaders.ifPresent(headers -> builder.header("Link", headers));

        return builder.body(response);
    }

    public static String createLinks(final int pageSize, final int page, final int numberOfPages) {
        final String[] linkTypes = { "prev", "next", "last", "first" };

        return Arrays.stream(linkTypes)
                .map(linkRelationship -> createLink(pageSize, page, numberOfPages, linkRelationship))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.joining(", "));
    }

    private static Optional<String> createLink(final int pageSize, final int page, final int numberOfPages, final String relationship) {
        return switch (relationship) {
            case "prev" -> page > 1 ? Optional.of(link(page - 1, relationship)) : Optional.empty();
            case "next" -> page < numberOfPages ? Optional.of(link(page+1, relationship)) : Optional.empty();
            case "last" -> Optional.empty();
            default -> Optional.of(link(1, relationship));
        };
    }

    private static String link(final int page, final String relationship) {
        final StringBuilder linkBuilder = new StringBuilder();

        final String linkUrl = UriComponentsBuilder.fromHttpUrl("https://api.gitub.com/search/repositories")
                .queryParam("page", page)
                .toUriString();

        return linkBuilder.append("<")
                .append(linkUrl)
                .append(">; rel=\"")
                .append(relationship)
                .append("\"").toString();
    }
}
