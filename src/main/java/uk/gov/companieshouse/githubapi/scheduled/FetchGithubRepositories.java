package uk.gov.companieshouse.githubapi.scheduled;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import uk.gov.companieshouse.githubapi.dao.GitHubRepoRepository;
import uk.gov.companieshouse.githubapi.model.GitHubRepository;
import uk.gov.companieshouse.githubapi.model.GitHubRepositoryDao;
import uk.gov.companieshouse.githubapi.service.GithubRepositoryService;

@Component
public class FetchGithubRepositories {

    private static final String POPULATION_CRON_EXPRESSION = "0 0/10 6-20 * * Mon-Fri";
    private static final Logger LOGGER = LoggerFactory.getLogger(FetchGithubRepositories.class);

    private final GithubRepositoryService githubRepositoryService;
    private final GitHubRepoRepository gitHubRepoRepository;
    private final ScheduledJobState scheduledJobState;

    @Autowired
    public FetchGithubRepositories(
            final GithubRepositoryService githubRepositoryService,
            final GitHubRepoRepository gitHubRepoRepository
    ) {
        this(githubRepositoryService, gitHubRepoRepository, new ScheduledJobState());
    }

    /**
     * Constructor to create the task with all arguments.
     * @param githubRepositoryService The GithubRepositoryService to use
     * @param gitHubRepoRepository The GithubRepoRepsository to use
     * @param scheduledJobState The initial ScheduledJobState
     */
    public FetchGithubRepositories(
            final GithubRepositoryService githubRepositoryService,
            final GitHubRepoRepository gitHubRepoRepository,
            final ScheduledJobState scheduledJobState
    ) {
        this.githubRepositoryService = githubRepositoryService;
        this.gitHubRepoRepository =  gitHubRepoRepository;
        this.scheduledJobState = scheduledJobState;
    }

    /**
     * Method to populate the Github Repository cache with the latest version infromation. Spring
     * will automatically run this on a schedule defined by the constant POPULATION_CRON_EXPRESSON.
     */
    @Scheduled(cron = POPULATION_CRON_EXPRESSION)
    public void populateRepositoryCache() {
        if (isRunning()) {
            LOGGER.warn("Already a task updating cache in progress.");

            return;
        }

        scheduledJobState.updateRunningState(true);
        LOGGER.info("Populating the cache with latest repository information");

        final List<GitHubRepository> repositories = githubRepositoryService.listJavaRepositories();

        repositories
                .parallelStream()
                .forEach(repository -> {
                    LOGGER.debug("Updating dependencies of {}", repository.name());
                    final Map<String, String> dependencies =
                            githubRepositoryService.loadDependencies(repository);

                    gitHubRepoRepository.save(
                            new GitHubRepositoryDao(repository.name(), dependencies)
                    );
                });

        scheduledJobState.updateRunningState(false);

        LOGGER.info("Cache population complete");
    }

    public boolean isRunning() {
        return scheduledJobState.isRunning();
    }
}
