package uk.gov.companieshouse.githubapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import uk.gov.companieshouse.githubapi.scheduled.FetchGithubRepositories;

@SpringBootApplication
public class GithubapiApplication {

    /**
     * Runs the application.
     * @param args Command line arguments passed to application
     */
    public static void main(String[] args) {
        final ApplicationContext context = SpringApplication.run(GithubapiApplication.class, args);
        final FetchGithubRepositories fetchGithubRepositories = context.getBean(
                FetchGithubRepositories.class
        );

        fetchGithubRepositories.populateRepositoryCache();
    }

}
