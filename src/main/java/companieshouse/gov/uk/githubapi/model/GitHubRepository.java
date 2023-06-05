package companieshouse.gov.uk.githubapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubRepository(
    @JsonProperty("name") String name,
    @JsonProperty("trees_url") String treesUrl,
    @JsonProperty("default_branch") String defaultBranch
) {}
