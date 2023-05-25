package companieshouse.gov.uk.githubapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GitHubTree {
    private String path;
    private String mode;
    private String type;
    private String url;

    @JsonProperty("sha")
    private String treeSha;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTreeSha() {
        return treeSha;
    }

    public void setTreeSha(String treeSha) {
        this.treeSha = treeSha;
    }

    @Override
    public String
    toString() {
        return "GitHubTree{" +
                "path='" + path + '\'' +
                ", mode='" + mode + '\'' +
                ", type='" + type + '\'' +
                ", url='" + url + '\'' +
                ", treeSha='" + treeSha + '\'' +
                '}';
    }
}

