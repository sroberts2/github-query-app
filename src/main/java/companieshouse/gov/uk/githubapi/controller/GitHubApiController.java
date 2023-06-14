package companieshouse.gov.uk.githubapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;

import companieshouse.gov.uk.githubapi.model.GitHubAppData;
import companieshouse.gov.uk.githubapi.scheduled.FetchGithubRepositories;
import companieshouse.gov.uk.githubapi.service.AppDataService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
public class GitHubApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubApiController.class);

    private final AppDataService appDataService;
    private final FetchGithubRepositories fetchGithubRepositories;


    /**
     * Constructor for the GitHubApiController.
     * @param appDataService appDataService to load GitHubAppData
     * @param fetchGithubRepositories fetchGithubRepositories task to fetch GitHub repositories
     */
    @Autowired
    public GitHubApiController(
            final AppDataService appDataService,
            final FetchGithubRepositories fetchGithubRepositories
    ) {
        this.appDataService = appDataService;
        this.fetchGithubRepositories = fetchGithubRepositories;
    }

    /**
     * Get '/' returning the index page with Github Application data on it.
     * @param model model to add the app data to
     * @return the index page
     */
    @GetMapping("/")
    public String getGitHubAppData(Model model) {
        LOGGER.debug("Loading app data");
        final List<GitHubAppData> appDataList = new ArrayList<>(appDataService.loadAppData());
        appDataList.sort(Comparator.comparing(GitHubAppData::id)); // Sort by id alphabetically
        model.addAttribute("appDataList", appDataList);
        LOGGER.info("Index loaded");
        return "index";
    }


    /**
     * Get '/get-data' returning the repos page with GitHub repositories on it.
     * @param model model to add the repos to
     * @return the repos page
     */
    @GetMapping("/get-data")
    public String getData(Model model)  {
        List<GitHubAppData> list = appDataService.loadAppData();
        model.addAttribute("repos", list);
        return "repos";
    }

    /**
     * Populate the database with the GitHub repositories.
     * @return 200 response if successful, 503 if already in progress
     * @throws JsonProcessingException if the JSON cannot be processed
     */
    //TODO cron instead of endpoint
    @GetMapping("/populate-repo-data")
    public ResponseEntity<String> getGitHubApiResponse() throws JsonProcessingException {
        if (fetchGithubRepositories.isRunning()) {
            return ResponseEntity.status(503).body("Already in progress.");
        }

        fetchGithubRepositories.populateRepositoryCache();
        
        return ResponseEntity.ok("");
    }

}
