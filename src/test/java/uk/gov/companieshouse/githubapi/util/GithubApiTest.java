package uk.gov.companieshouse.githubapi.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.githubapi.util.ApiUtils.makeResponseEntity;

import uk.gov.companieshouse.githubapi.model.GitHubRepository;
import uk.gov.companieshouse.githubapi.model.GitHubSearchResponse;
import uk.gov.companieshouse.githubapi.util.GithubApi;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.util.UriComponentsBuilder;

@SpringBootTest
@TestPropertySource(properties = {
        "github.pat=ghp_123456789012345",
        "app.scheduling.enable=false",
        "app.retry.enable=true",
        "app.retry.delay=10",
        "app.retry.multiplier=2"
})
public class GithubApiTest {
    

    @Value("${github.pat}")
    private String githubPat;

    @MockBean
    private RestOperations restOperationsMock;

    @Autowired
    private GithubApi githubApi;

    @Captor
    private ArgumentCaptor<HttpEntity<?>> httpEntityArgumentCaptor;
    
    @Test
    void testListJavaRepositoriesLoadsSinglePageOfRepositories() {
        final List<GitHubRepository> repositories = List.of(
                new GitHubRepository("repo1", "tree1", "main"),
                new GitHubRepository("repo3", "tree1", "main"),
                new GitHubRepository("repo2", "tree1", "trunk"),
                new GitHubRepository("repo4", "tree1", "master"));

        final GitHubSearchResponse gitHubSearchResponse = new GitHubSearchResponse(
                "1", 4, false, repositories);

        final String expectedUrl = UriComponentsBuilder.fromHttpUrl("https://api.github.com/search/repositories")
                .queryParam("q", "user:companieshouse+language:java")
                .queryParam("page", 1)
                .queryParam("per_page", 100).toUriString();
        
        when(restOperationsMock.exchange(anyString(), eq(HttpMethod.GET), any(), eq(GitHubSearchResponse.class)))
                .thenReturn(makeResponseEntity(gitHubSearchResponse, Optional.empty()));
        
        githubApi.get(expectedUrl, GitHubSearchResponse.class);

        verify(restOperationsMock, times(1)).exchange(eq(expectedUrl), eq(HttpMethod.GET), httpEntityArgumentCaptor.capture(), eq(GitHubSearchResponse.class));

        final HttpEntity<?> httpEntity = httpEntityArgumentCaptor.getValue();
        assertThat(httpEntity.getHeaders().get("Authorization")).isNotNull().containsOnly("Bearer " + githubPat);
    }

    @Test
    void testGetReturnsBody() {
        final List<GitHubRepository> repositories = List.of(
                new GitHubRepository("repo1", "tree1", "main"),
                new GitHubRepository("repo3", "tree1", "main"),
                new GitHubRepository("repo2", "tree1", "trunk"),
                new GitHubRepository("repo4", "tree1", "master"));

        final GitHubSearchResponse gitHubSearchResponse = new GitHubSearchResponse(
                "1", 4, false, repositories);

        when(restOperationsMock.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubSearchResponse.class)
        )).thenReturn(makeResponseEntity(gitHubSearchResponse, Optional.empty()));

        final ResponseEntity<GitHubSearchResponse> result = githubApi.get("https://api.github.com/repositories/search", GitHubSearchResponse.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(result.getBody().repositories()).containsExactlyInAnyOrderElementsOf(repositories);
    }

    @Test
    void testGetThrowsGithubApiCallErrorExceptionWhenHitRateLimitExhausted() {
        when(restOperationsMock.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(GitHubSearchResponse.class)
        )).thenThrow(new HttpClientErrorException(HttpStatusCode.valueOf(429)));

        assertThatThrownBy(() -> githubApi.get("https://api.github.com/repositories/search", GitHubSearchResponse.class))
                .hasCauseInstanceOf(HttpClientErrorException.class);
    }

    @Test
    void testGetRetriesAfterFails() {
        when(restOperationsMock.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(String.class)
        )).thenThrow(new HttpClientErrorException(HttpStatusCode.valueOf(429))).thenReturn(ResponseEntity.ok().body("Hello"));

        final ResponseEntity<String> result = githubApi.get("https://api.github.com/search", String.class);

        assertThat(result.getBody()).isEqualTo("Hello");
    }

}
