package companieshouse.gov.uk.githubapi.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import companieshouse.gov.uk.githubapi.dao.GitHubRepoRepository;
import companieshouse.gov.uk.githubapi.model.GitHubAppData;
import companieshouse.gov.uk.githubapi.model.GitHubRepositoryDao;

@Service
public class AppDataService {

    private static final String SPRING_BOOT_DEPENDENCY_PREFIX = "spring-boot";
    private static final String SPRING_FRAMEWORK_DEPENDENCY_PREFIX = "spring-framework";
    private static final String SPRING_VERSION_NOT_RESOLVED = "No Spring detected";
    
    private final GitHubRepoRepository gitHubRepoRepository;

    @Autowired
    public AppDataService(final GitHubRepoRepository gitHubRepoRepository) {
        this.gitHubRepoRepository = gitHubRepoRepository;
    }

    public List<GitHubAppData> loadAppData() {
        return gitHubRepoRepository.findAll()
            .parallelStream()
            .map(this::convertToGitHubAppData)
            .toList();
    }

    private GitHubAppData convertToGitHubAppData(final GitHubRepositoryDao repository) {
        return resolvePossibleSpringBootVersion(repository.getDependencies()).map(
            springBootVersion -> new GitHubAppData(repository.getRepoName(), repository.getRepoName(), springBootVersion)
        ).orElseGet(() -> new GitHubAppData(repository.getRepoName(), repository.getRepoName(), SPRING_VERSION_NOT_RESOLVED));
    }

    private Optional<String> resolvePossibleSpringBootVersion(final Map<String, String> dependencies) {
        final Optional<String> springBootVersion = resolveDependencyVersion(dependencies, SPRING_BOOT_DEPENDENCY_PREFIX);
        
        if (springBootVersion.isPresent()) {
            return springBootVersion;
        }

        final Optional<String> springFrameworkVersion = resolveDependencyVersion(dependencies, SPRING_FRAMEWORK_DEPENDENCY_PREFIX)
            .map(version -> "Uses native Spring version " + version);
        
        if (springFrameworkVersion.isPresent()) {
            return springFrameworkVersion;
        }

        return resolveDependencyVersion(dependencies, "companies-house-parent")
            .map(version -> "Uses companies-house-parent pom (version: " + version + "), no Spring version detected");
    }

    private final Optional<String> resolveDependencyVersion(final Map<String, String> dependencies, final String dependencyPrefix) {
        return dependencies.entrySet().stream()
        .filter(dependency -> dependency.getKey().contains(dependencyPrefix))
        .findAny()
        .map(Map.Entry::getValue);
    }
    
}
