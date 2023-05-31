package companieshouse.gov.uk.githubapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.mongodb.core.mapping.Document;

@JsonIgnoreProperties(ignoreUnknown = true)
@Document("github_repository")
public class GitHubRepository {

    @JsonProperty("name")
    private String name;

    @JsonProperty("trees_url")
    private String treesUrl;

    @JsonProperty("default_branch")
    private String defaultBranch;

    // Getters and setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTreesUrl() {
        return treesUrl;
    }

    public void setTreesUrl(String treesUrl) {
        this.treesUrl = treesUrl;
    }

    public String getDefaultBranch() {
        return defaultBranch;
    }

    public void setDefaultBranch(String defaultBranch) {
        this.defaultBranch = defaultBranch;
    }

    @Override
    public String toString() {
        return "GitHubRepository{" +
                "name='" + name + '\'' +
                ", treesUrl='" + treesUrl + '\'' +
                ", defaultBranch='" + defaultBranch + '\'' +
                '}';
    }
}
