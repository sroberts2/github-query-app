package companieshouse.gov.uk.githubapi.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

import companieshouse.gov.uk.githubapi.exception.GithubApiCallErrorException;

@Component
public class GithubApi {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GithubApi.class);

    private final RestOperations restOperations;
    private final String githubAuthenticationToken;

    public GithubApi(final RestOperations restOperations, @Value("${github.pat}") final String githubAuthenticationToken) {
        this.restOperations = restOperations;
        this.githubAuthenticationToken = githubAuthenticationToken;
    }

    @Retryable(
        retryFor = {
            GithubApiCallErrorException.class,
            RestClientException.class
        },
        backoff = @Backoff(delayExpression = "${app.retry.delay}", multiplierExpression = "${app.retry.multiplier}"),
        maxAttempts = 5
    )
    public <T> ResponseEntity<T> get(
        final String url,
        final Class<T> responseClass
    ) {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(githubAuthenticationToken);

        try {
            return this.restOperations.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(httpHeaders),
                responseClass
            );
        } catch (final RestClientException restClientException) {
            if (restClientException instanceof HttpStatusCodeException) {
                final HttpStatusCodeException httpStatusCodeException = (HttpStatusCodeException) restClientException;
                LOGGER.warn("API call to {} failed with status {}", url, httpStatusCodeException.getStatusCode());

                throw new GithubApiCallErrorException("Github API Call failed with: " + httpStatusCodeException.getStatusText(), restClientException);
            } else {
                LOGGER.warn("Api Call failed: {}", restClientException.getMessage());
                throw restClientException;
            }
        }
    }
}
