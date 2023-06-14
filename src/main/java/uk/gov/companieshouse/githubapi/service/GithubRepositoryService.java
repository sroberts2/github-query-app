package uk.gov.companieshouse.githubapi.service;

import static uk.gov.companieshouse.githubapi.service.GithubApiPaginationService.paginateResponseFromApi;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import uk.gov.companieshouse.githubapi.model.GitHubLeaf;
import uk.gov.companieshouse.githubapi.model.GitHubRepository;
import uk.gov.companieshouse.githubapi.model.GitHubSearchResponse;
import uk.gov.companieshouse.githubapi.model.GitHubTreeResponse;
import uk.gov.companieshouse.githubapi.util.GithubApi;

@Service
public class GithubRepositoryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GithubRepositoryService.class);
    private static final String GITHUB_SEARCH_API_URL = "https://api.github.com/search/repositories";
    private static final int PAGE_SIZE = 100;
    private static final String COMPANIES_HOUSE_ORGANISATION_USER_NAME = "companieshouse";
    private static final String FILTER_QUERY = "user:"
            + COMPANIES_HOUSE_ORGANISATION_USER_NAME 
            + "+language:java";

    private final GithubApi githubApi;
    private final MavenDependenciesParsingService mvnDependenciesParsingService;

    @Autowired
    public GithubRepositoryService(
                final GithubApi githubApi,
                final MavenDependenciesParsingService mvnDependenciesParsingService
    ) {
        this.githubApi = githubApi;
        this.mvnDependenciesParsingService = mvnDependenciesParsingService;
    }

    /**
     * List Java Repositories held by Companies House in Github.
     * @return List of GitHubRepository objects
     */
    public List<GitHubRepository> listJavaRepositories() {
        final List<GitHubSearchResponse> searchResponses = 
                paginateResponseFromApi(this::callGithubSearchApiForRepositories);

        LOGGER.debug("Repositories loaded.");

        return searchResponses.parallelStream()
            .flatMap(searchResponse -> searchResponse.repositories().stream())
            .toList();
    }

    /**
     * Load the dependencies for a given GitHubRepository.
     * @param githubRepository GitHubRepository to load dependencies for
     * @return Map of dependency versions, keyed on normalised dependency name
     */
    public Map<String, String> loadDependencies(final GitHubRepository githubRepository) {
        final GitHubTreeResponse gitHubTreeResponse = getGithubTreeOfRepository(githubRepository);

        // TODO: When need to process non-maven projects change this bit to handle each repository
        return gitHubTreeResponse.tree().parallelStream()
            .filter(tree -> tree.path().strip().equals("pom.xml"))
            .map(tree -> {
                LOGGER.debug("Loading POM for {}", githubRepository.name());
                final String dependenciesFile = loadLeaf(tree.url());

                try {
                    return Optional.of(
                            mvnDependenciesParsingService.parseDependencies(dependenciesFile)
                    );
                } catch (final Exception saxParseException) {
                    LOGGER.warn(
                            "Could not load {} of repo {}",
                            tree.url(),
                            githubRepository.name(),
                            saxParseException
                    );

                    return Optional.<Map<String, String>>empty();
                }
            })
            .filter(Optional::isPresent)
            .findFirst()
            .map(Optional::get)
            .orElse(Map.of());
    }

    private String loadLeaf(final String url) {
        final GitHubLeaf dependenciesFile = githubApi.get(url, GitHubLeaf.class).getBody();

        final String cleanContent = dependenciesFile.content().replaceAll("[^A-Za-z0-9+/=]", "");

        return new String(Base64.getDecoder().decode(cleanContent), StandardCharsets.UTF_8);
    }

    private GitHubTreeResponse getGithubTreeOfRepository(final GitHubRepository githubRepository) {
        final String treesUrl = githubRepository.treesUrl();
        final String normalisedTreesUrl = treesUrl.substring(0, treesUrl.indexOf("{"))
                 + "/"
                 + githubRepository.defaultBranch();

        return githubApi.get(normalisedTreesUrl, GitHubTreeResponse.class).getBody();
    }

    private ResponseEntity<GitHubSearchResponse> callGithubSearchApiForRepositories(
            final int page
    ) {
        final UriComponentsBuilder uriBuilder =
                UriComponentsBuilder.fromHttpUrl(GITHUB_SEARCH_API_URL)
                    .queryParam("q", FILTER_QUERY)
                    .queryParam("page", page)
                    .queryParam("per_page", PAGE_SIZE);

        return githubApi.get(uriBuilder.toUriString(), GitHubSearchResponse.class);
    }

}
