package companieshouse.gov.uk.githubapi.service;

import static companieshouse.gov.uk.githubapi.util.ApiUtils.createLinks;
import static companieshouse.gov.uk.githubapi.util.ApiUtils.makeResponseEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestOperations;
import org.springframework.web.util.UriComponentsBuilder;

import companieshouse.gov.uk.githubapi.model.GitHubLeaf;
import companieshouse.gov.uk.githubapi.model.GitHubRepository;
import companieshouse.gov.uk.githubapi.model.GitHubSearchResponse;
import companieshouse.gov.uk.githubapi.model.GitHubTree;
import companieshouse.gov.uk.githubapi.model.GitHubTreeResponse;
import companieshouse.gov.uk.githubapi.util.GithubApi;
import companieshouse.gov.uk.githubapi.util.QueryStringUtility;
import companieshouse.gov.uk.githubapi.util.SequenceAnswer;

@SpringBootTest
@TestPropertySource(properties = {
        "github.pat=ghp_123456789012345",
        "app.scheduling.enable=false"
})
public class GithubRepositoryServiceTest {

    @Value("${github.pat}")
    private String githubPat;

    @MockBean
    private RestOperations restOperationsMock;

    @MockBean
    private GithubApi githubApiMock;

    @MockBean
    private MavenDependenciesParsingService mvnDependenciesParsingServiceMock;

    @Autowired
    private GithubRepositoryService githubRepositoryService;

    @Captor
    private ArgumentCaptor<String> urlArgumentCaptor;

    @Captor
    private ArgumentCaptor<HttpEntity<?>> httpEntityArgumentCaptor;

    @Value("classpath:demopom.xml")
    private Resource demoPom;

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
        
        when(githubApiMock.get(expectedUrl, GitHubSearchResponse.class))
            .thenReturn(makeResponseEntity(gitHubSearchResponse, Optional.empty()));

        githubRepositoryService.listJavaRepositories();

        verify(githubApiMock, times(1)).get(expectedUrl, GitHubSearchResponse.class);
    }

    @Test
    void testListJavaRepositoriesReturnsSinglePageOfRepositories() {
        final List<GitHubRepository> repositories = List.of(
                new GitHubRepository("repo1", "tree1", "main"),
                new GitHubRepository("repo3", "tree1", "main"),
                new GitHubRepository("repo2", "tree1", "trunk"),
                new GitHubRepository("repo4", "tree1", "master"));

        final GitHubSearchResponse gitHubSearchResponse = new GitHubSearchResponse(
                "1", 4, false, repositories);
        
        when(githubApiMock.get(anyString(), eq(GitHubSearchResponse.class)))
            .thenReturn(makeResponseEntity(gitHubSearchResponse, Optional.empty()));

        final List<GitHubRepository> result = githubRepositoryService.listJavaRepositories();

        assertThat(result).containsExactlyInAnyOrderElementsOf(repositories);
    }

    @Test
    void testListJavaRepositoriesCanHandlePaginatedResponse() {
        final int numberOfPages = 10;
        final int pageSize = 100;

        final List<List<GitHubRepository>> pagesOfRepos = ninePagesOfRepositories(numberOfPages, pageSize);

        final List<ResponseEntity<GitHubSearchResponse>> responses = createResponses(numberOfPages, pageSize, pagesOfRepos);

        final List<GitHubRepository> lastPage = createLastPageOfRepositories(24);

        final ResponseEntity<GitHubSearchResponse> lastResponse = makeResponseEntity(
                new GitHubSearchResponse(randomString(12), pageSize, false, lastPage),
                Optional.of(createLinks(pageSize, numberOfPages, numberOfPages)));
        
        when(githubApiMock.get(anyString(), eq(GitHubSearchResponse.class)))
            .thenAnswer(new SequenceAnswer<ResponseEntity<GitHubSearchResponse>>(responses.iterator(), lastResponse));

        githubRepositoryService.listJavaRepositories();

        verify(githubApiMock, times(10)).get(urlArgumentCaptor.capture(), eq(GitHubSearchResponse.class));

        SoftAssertions.assertSoftly(softAssert -> {
            final List<URI> requests = urlArgumentCaptor.getAllValues().stream().map(url -> {
                    try {
                            return new URI(url);
                    } catch (final URISyntaxException e) {
                            throw new RuntimeException(e);
                    }
            }).toList();

            softAssert.assertThat(requests).anyMatch(request -> {
                final Map<String, String> queryString = parseQueryString(request.getQuery());

                return queryString.containsKey("page");
            }).withFailMessage("Expected each request to have a page parameter");
        
            IntStream.range(0, 10)
                .forEach(page -> {
                    final URI request = requests.get(page);

                    final Map<String, String> queryString = parseQueryString(request.getQuery());
                    final Integer expectedPage = page + 1;

                    softAssert.assertThat(queryString.get("page")).isEqualTo(expectedPage.toString());
                });
        });
    }

    @Test
    void testListReposReturnsAllReposWhenResponseIsPaginated() {
        final int numberOfPages = 10;
        final int pageSize = 100;

        final List<List<GitHubRepository>> pagesOfRepos = ninePagesOfRepositories(numberOfPages, pageSize);

        final List<ResponseEntity<GitHubSearchResponse>> responses = createResponses(numberOfPages, pageSize, pagesOfRepos);

        final List<GitHubRepository> lastPage = createLastPageOfRepositories(24);

        final ResponseEntity<GitHubSearchResponse> lastResponse = makeResponseEntity(
                new GitHubSearchResponse(randomString(12), pageSize, false, lastPage),
                Optional.of(createLinks(pageSize, numberOfPages, numberOfPages)));
        
        final List<GitHubRepository> expectedRepositories = new ArrayList<>(lastPage);

        expectedRepositories.addAll(
            pagesOfRepos.stream()
                .flatMap(page -> page.stream())
                .toList()
        );
        
        when(githubApiMock.get(anyString(), eq(GitHubSearchResponse.class)))
            .thenAnswer(new SequenceAnswer<ResponseEntity<GitHubSearchResponse>>(responses.iterator(), lastResponse));

        final List<GitHubRepository> gitHubRepositories = githubRepositoryService.listJavaRepositories();

        assertThat(gitHubRepositories).containsExactlyInAnyOrderElementsOf(expectedRepositories);

    }

    @Test
    void testLoadDependenciesLoadsTreesForThatRepository() {
        final GitHubTreeResponse treeResponse = new GitHubTreeResponse(List.of());

        when(githubApiMock.get(anyString(), any())).thenReturn(ResponseEntity.ok(treeResponse));

        final GitHubRepository githubRepository = new GitHubRepository("awesomerepo1", "https://api.gh.com/trees/awesome1{/sha}", "main");

        githubRepositoryService.loadDependencies(githubRepository);

        verify(githubApiMock, times(1)).get("https://api.gh.com/trees/awesome1/main", GitHubTreeResponse.class);
    }

    @Test
    void testLoadDependencesLoadsPomXmlWhenPresent() {
        String url = "https://api.github.com/repos/companieshouse/github-query-app/git/blobs/b446a5e886ca4d885233484705873716c3f58356";
        final GitHubTreeResponse treeResponse = new GitHubTreeResponse(List.of(
            new GitHubTree("pom.xml", "100644", "blob", url, "b44fa5e886ca4d885233484705873716c3f58356"),
            new GitHubTree("README.md", "100644", "blob", "https://blah/readme", "b44fa5e886ca4d885233484705873716c3f58356")
        ));

        when(githubApiMock.get(anyString(), eq(GitHubTreeResponse.class))).thenReturn(ResponseEntity.ok(treeResponse));
        when(githubApiMock.get(anyString(), eq(GitHubLeaf.class))).thenReturn(ResponseEntity.ok(new GitHubLeaf("")));

        final GitHubRepository githubRepository = new GitHubRepository("awesomerepo1", "https://api.gh.com/trees/awesome1{/sha}", "main");

        githubRepositoryService.loadDependencies(githubRepository);

        verify(githubApiMock, times(1)).get(url, GitHubLeaf.class);
        verify(githubApiMock, never()).get(eq("https://blah/readme"), any());
    }

    @Test
    void testLoadDependenciesReturnsDependenciesWithinPom() throws Exception {
        String url = "https://api.github.com/repos/companieshouse/github-query-app/git/blobs/b446a5e886ca4d885233484705873716c3f58356";
        final GitHubTreeResponse treeResponse = new GitHubTreeResponse(List.of(
            new GitHubTree("pom.xml", "100644", "blob", url, "b44fa5e886ca4d885233484705873716c3f58356"),
            new GitHubTree("README.md", "100644", "blob", "https://blah/readme", "b44fa5e886ca4d885233484705873716c3f58356")
        ));

        final GitHubRepository githubRepository = new GitHubRepository("awesomerepo1", "https://api.gh.com/trees/awesome1{/sha}", "main");
        final Map<String, String> dependencyMap = Map.of("spring.boot", "3.0.0");
        final String pom = new String(Files.readAllBytes(demoPom.getFile().toPath()));

        when(githubApiMock.get(anyString(), eq(GitHubTreeResponse.class))).thenReturn(ResponseEntity.ok(treeResponse));

        when(githubApiMock.get(anyString(), eq(GitHubLeaf.class))).thenReturn(ResponseEntity.ok(new GitHubLeaf(base64Encode(pom))));

        when(mvnDependenciesParsingServiceMock.parseDependencies(anyString())).thenReturn(dependencyMap);

        final Map<String, String> result = githubRepositoryService.loadDependencies(githubRepository);

        assertThat(result).containsExactlyEntriesOf(dependencyMap);
        verify(mvnDependenciesParsingServiceMock, times(1)).parseDependencies(pom);
    }

    @Test
    void testLoadDependenciesCleansBase64BeforeLoad() throws Exception {
        String url = "https://api.github.com/repos/companieshouse/github-query-app/git/blobs/b446a5e886ca4d885233484705873716c3f58356";
        final GitHubTreeResponse treeResponse = new GitHubTreeResponse(List.of(
            new GitHubTree("pom.xml", "100644", "blob", url, "b44fa5e886ca4d885233484705873716c3f58356"),
            new GitHubTree("README.md", "100644", "blob", "https://blah/readme", "b44fa5e886ca4d885233484705873716c3f58356")
        ));

        final GitHubRepository githubRepository = new GitHubRepository("awesomerepo1", "https://api.gh.com/trees/awesome1{/sha}", "main");
        final Map<String, String> dependencyMap = Map.of("spring.boot", "3.0.0");
        final String pom = new String(Files.readAllBytes(demoPom.getFile().toPath()));

        when(githubApiMock.get(anyString(), eq(GitHubTreeResponse.class))).thenReturn(ResponseEntity.ok(treeResponse));

        when(githubApiMock.get(anyString(), eq(GitHubLeaf.class))).thenReturn(ResponseEntity.ok(new GitHubLeaf(base64Encode(pom) + "@£$%^&*()")));

        when(mvnDependenciesParsingServiceMock.parseDependencies(anyString())).thenReturn(dependencyMap);

        githubRepositoryService.loadDependencies(githubRepository);

        verify(mvnDependenciesParsingServiceMock, times(1)).parseDependencies(pom);
    }

    @Test
    void testLoadDependenciesReturnsEmptyMapWhenThereAreNoPomFiles() {
        final GitHubTreeResponse treeResponse = new GitHubTreeResponse(List.of(
            new GitHubTree("README.md", "100644", "blob", "https://blah/readme", "b44fa5e886ca4d885233484705873716c3f58356")
        ));
        final GitHubRepository githubRepository = new GitHubRepository("awesomerepo1", "https://api.gh.com/trees/awesome1{/sha}", "main");

        when(githubApiMock.get(anyString(), eq(GitHubTreeResponse.class))).thenReturn(ResponseEntity.ok(treeResponse));

        final Map<String, String> result = githubRepositoryService.loadDependencies(githubRepository);

        assertThat(result).isEmpty();
    }

    private String base64Encode(final String toBeEncoded) {
        final byte[] encodeBytes = toBeEncoded.getBytes(StandardCharsets.UTF_8);

        return Base64.getEncoder().encodeToString(encodeBytes);
    } 
    
    private List<GitHubRepository> createLastPageOfRepositories(final int pageSize) {
        return IntStream.range(0, pageSize)
                .boxed()
                .map(repoNumber -> randomRepoName(repoNumber + 100))
                .map(
                        repoName -> new GitHubRepository(repoName, "https://github.com/" + repoName, "main"))
                .toList();
    }

    private List<ResponseEntity<GitHubSearchResponse>> createResponses(final int numberOfPages, final int pageSize,
            final List<List<GitHubRepository>> pagesOfRepos) {
        return IntStream.range(0, pagesOfRepos.size())
                .boxed()
                .map(index -> makeResponseEntity(
                        new GitHubSearchResponse(randomString(12), pageSize, false, pagesOfRepos.get(index)),
                        Optional.of(
                                createLinks(pageSize, index + 1, numberOfPages))))
                .toList();
    }

    private List<List<GitHubRepository>> ninePagesOfRepositories(final int numberOfPages, final int pageSize) {
        return IntStream.range(0, numberOfPages - 1)
                .boxed()
                .map(pageNumber -> IntStream
                        .range(0, pageSize)
                        .boxed()
                        .map(repoNumber -> randomRepoName(repoNumber + 1))
                        .map(
                                repoName -> new GitHubRepository(repoName, "https://github.com/" + repoName, "main"))
                        .toList()
                )
                .toList();
    }

    private String randomRepoName(final int counter) {
        return "repo-" + counter + "-" + randomString(5);
    }

    private String randomString(final int length) {
        final byte[] bytesArray = new byte[length];

        new Random().nextBytes(bytesArray);

        return new String(bytesArray, StandardCharsets.UTF_8);
    }

    private Map<String, String> parseQueryString(final String queryString) {
        return QueryStringUtility.parseQueryString(queryString)
            .entrySet()
            .stream()
            .map(entry -> Map.entry(entry.getKey(), entry.getValue().get(0)))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
