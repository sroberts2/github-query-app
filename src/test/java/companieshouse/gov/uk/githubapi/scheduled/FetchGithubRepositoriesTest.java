package companieshouse.gov.uk.githubapi.scheduled;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;


import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import companieshouse.gov.uk.githubapi.dao.GitHubRepoRepository;
import companieshouse.gov.uk.githubapi.model.GitHubRepository;
import companieshouse.gov.uk.githubapi.model.GitHubRepositoryDao;
import companieshouse.gov.uk.githubapi.service.GithubRepositoryService;

@ExtendWith(MockitoExtension.class)
public class FetchGithubRepositoriesTest {

    @Mock
    private GithubRepositoryService githubRepositoryServiceMock;

    @Mock
    private GitHubRepoRepository gitHubRepoRepositoryMock;

    @Mock
    private ScheduledJobState scheduledJobStateMock;

    @InjectMocks
    private FetchGithubRepositories fetchGithubRepositories;

    @Captor
    private ArgumentCaptor<GitHubRepository> gitHubRepositoryArgumentCaptor;

    @Test
    void testPopulateRepositoryCacheLoadsJavaRepositories() {
        fetchGithubRepositories.populateRepositoryCache();

        verify(githubRepositoryServiceMock, times(1)).listJavaRepositories();
    }

    @Test
    void testPopulateRepositoryCacheLoadsDependenciesOfEachRepository() {
        List<GitHubRepository> repos = List.of(
            new GitHubRepository("my-java-library-one", "trees.com", "main"),
            new GitHubRepository("my-java-library-two", "trees2.com", "master")
        );
        when(githubRepositoryServiceMock.listJavaRepositories()).thenReturn(repos);

        fetchGithubRepositories.populateRepositoryCache();

        verify(githubRepositoryServiceMock, times(2)).loadDependencies(gitHubRepositoryArgumentCaptor.capture());

        assertThat(gitHubRepositoryArgumentCaptor.getAllValues()).hasSize(2).containsExactlyInAnyOrderElementsOf(repos);
    }

    @Test
    void testPopulateRepositoryCacheAddsTheDependencyInfoToCache() {
        GitHubRepository repo = new GitHubRepository("my-java-library-one", "trees.com", "main");
        when(githubRepositoryServiceMock.listJavaRepositories()).thenReturn(List.of(
                repo
            )
        );

        when(githubRepositoryServiceMock.loadDependencies(repo)).thenReturn(
            Map.of("spring", "3.0.0", "jacoco", "0.8.0")
        );

        fetchGithubRepositories.populateRepositoryCache();

        verify(gitHubRepoRepositoryMock, times(1)).save(new GitHubRepositoryDao("my-java-library-one", Map.of("spring", "3.0.0", "jacoco", "0.8.0")));
    }

    @Test
    void testUpdatesStateWhenRun() {
        GitHubRepository repo = new GitHubRepository("my-java-library-one", "trees.com", "main");
        when(githubRepositoryServiceMock.listJavaRepositories()).thenReturn(List.of(
                repo
            )
        );

        when(githubRepositoryServiceMock.loadDependencies(repo)).thenReturn(
            Map.of("spring", "3.0.0", "jacoco", "0.8.0")
        );

        fetchGithubRepositories.populateRepositoryCache();

        verify(scheduledJobStateMock, times(1)).updateRunningState(eq(true));
        verify(scheduledJobStateMock, times(1)).updateRunningState(eq(false));
    }

    @ParameterizedTest
    @ValueSource(
        booleans = {
            true, false
        }
    )
    void testIsRunningReturnsCorrectState(final boolean state) {
        when(scheduledJobStateMock.isRunning()).thenReturn(state);

        final boolean isRunning = fetchGithubRepositories.isRunning();

        assertThat(isRunning).isEqualTo(state);
    }
}
