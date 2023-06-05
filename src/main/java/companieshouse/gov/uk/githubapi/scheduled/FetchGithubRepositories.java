package companieshouse.gov.uk.githubapi.scheduled;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import companieshouse.gov.uk.githubapi.dao.GitHubRepoRepository;
import companieshouse.gov.uk.githubapi.model.GitHubRepository;
import companieshouse.gov.uk.githubapi.model.GitHubRepositoryDao;
import companieshouse.gov.uk.githubapi.service.GithubRepositoryService;

@Component
public class FetchGithubRepositories {

    private static final String POPULATION_CRON_EXPRESSION = "0 0/10 6-20 * * Mon-Fri";
    private static final Logger LOGGER = LoggerFactory.getLogger(FetchGithubRepositories.class);

    private final GithubRepositoryService githubRepositoryService;
    private final GitHubRepoRepository gitHubRepoRepository;
    private final ScheduledJobState scheduledJobState;

    @Autowired
    public FetchGithubRepositories(final GithubRepositoryService githubRepositoryService, final GitHubRepoRepository gitHubRepoRepository) {
        this(githubRepositoryService, gitHubRepoRepository, new ScheduledJobState());
    }

    public FetchGithubRepositories(final GithubRepositoryService githubRepositoryService, final GitHubRepoRepository gitHubRepoRepository, final ScheduledJobState scheduledJobState) {
        this.githubRepositoryService = githubRepositoryService;
        this.gitHubRepoRepository =  gitHubRepoRepository;
        this.scheduledJobState = scheduledJobState;
    }

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
                final Map<String, String> dependencies = githubRepositoryService.loadDependencies(repository);

                gitHubRepoRepository.save(new GitHubRepositoryDao(repository.name(), dependencies));
            });
        scheduledJobState.updateRunningState(false);

        LOGGER.info("Cache population complete");
    }

    public boolean isRunning() {
        return scheduledJobState.isRunning();
    }
}
