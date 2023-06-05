package companieshouse.gov.uk.githubapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import companieshouse.gov.uk.githubapi.dao.GitHubRepoRepository;
import companieshouse.gov.uk.githubapi.model.GitHubAppData;
import companieshouse.gov.uk.githubapi.model.GitHubRepositoryDao;

@ExtendWith(MockitoExtension.class)
public class AppDataServiceTest {

    @Mock
    private GitHubRepoRepository gitHubRepoRepositoryMock;

    @InjectMocks
    private AppDataService appDataService;

    @Test
    void testWhenCacheIsEmptyThenReturnsEmptyList() {
        when(gitHubRepoRepositoryMock.findAll()).thenReturn(List.of());

        final List<GitHubAppData> result = appDataService.loadAppData();

        assertThat(result).isEmpty();
    }

    @Test
    void testWhenThereIsOneRepositoryWithDataThenReturnsListOfOneItem() {
        when(gitHubRepoRepositoryMock.findAll()).thenReturn(List.of(
            new GitHubRepositoryDao("repo", Map.of(
                "spring-boot", "3.0.6",
                "companies-house-parent", "3.1.2"
            ))
        ));

        final List<GitHubAppData> result = appDataService.loadAppData();

        assertThat(result).containsExactly(new GitHubAppData("repo", "repo", "3.0.6"));
    }

    @Test
    void testWhenThereAreMultipleReposWithDataThenTheseAreReturned() {
        final String repo1SBVersion = "3.0.6";
        final String repo2SBVersion = "2.2.9";
        final String repo3SBVersion = "1.2.6";

        mongoContainsRepositories(
            Map.of(
                "repo1", Map.of(
                    "spring-boot", repo1SBVersion,
                    "companies-house-parent", "1.0,"
                ),
                "repo2",  Map.of(
                    "spring-boot-parent", repo2SBVersion,
                    "companies-house-parent", "1.0.0"
                ),
                "repo3", Map.of(
                    "spring-boot", repo3SBVersion,
                    "companies-house-parent", "1.0.0"
                )
            )
        );

        final List<GitHubAppData> result = appDataService.loadAppData();

        assertThat(result)
            .containsExactlyInAnyOrder(
                new GitHubAppData("repo1", "repo1", repo1SBVersion),
                new GitHubAppData("repo2", "repo2", repo2SBVersion),
                new GitHubAppData("repo3", "repo3", repo3SBVersion)
            );
    }

    @Test
    void testWhenNoActualSpringBootVersionAndCanBeDerivedThenThisIsIndicatedInOutput() {
        final String repo1SBVersion = "5.0.0";

        mongoContainsRepositories(
            Map.of(
                "repo1", Map.of(
                    "spring-framework", repo1SBVersion,
                    "companies-house-parent", "1.0,"
                ),
                "repo2",  Map.of(
                    "companies-house-parent", "1.0.0"
                )
            )
        );

        final List<GitHubAppData> result = appDataService.loadAppData();

        assertThat(result)
            .containsExactlyInAnyOrder(
                new GitHubAppData("repo1", "repo1", "Uses native Spring version " + repo1SBVersion),
                new GitHubAppData("repo2", "repo2", "Uses companies-house-parent pom (version: 1.0.0), no Spring version detected")
            );
    }

    @Test
    void testWhenCannotDetermineSpringVersionThenReturnsCorrectVersion() {
        mongoContainsRepositories(Map.of(
            "repo", Map.of("dependency", "1")
        ));
        
        final List<GitHubAppData> result = appDataService.loadAppData();

        assertThat(result).containsExactly(new GitHubAppData("repo", "repo", "No Spring detected"));
    }

    private void mongoContainsRepositories(final Map<String, Map<String, String>> repos) {
        when(gitHubRepoRepositoryMock.findAll()).thenReturn(
            repos.entrySet().stream().map(entry -> new GitHubRepositoryDao(entry.getKey(), entry.getValue())).toList()
        );
    }
}
