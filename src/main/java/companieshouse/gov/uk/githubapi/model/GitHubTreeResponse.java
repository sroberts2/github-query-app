package companieshouse.gov.uk.githubapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubTreeResponse {

    @JsonProperty("tree")
    private List<GitHubTree> tree;

    public List<GitHubTree> getTree() {
        return tree;
    }

    public void setTree(List<GitHubTree> tree) {
        this.tree = tree;
    }
}
