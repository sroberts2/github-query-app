package companieshouse.gov.uk.githubapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import companieshouse.gov.uk.githubapi.scheduled.FetchGithubRepositories;

@SpringBootApplication
public class GithubapiApplication {

	public static void main(String[] args) {
		final ApplicationContext context = SpringApplication.run(GithubapiApplication.class, args);
		final FetchGithubRepositories fetchGithubRepositories = context.getBean(FetchGithubRepositories.class);

		fetchGithubRepositories.populateRepositoryCache();
	}

}
