package companieshouse.gov.uk.githubapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubTree(
    String path,
    String mode,
    String type,
    String url,
    @JsonProperty("sha") String treeSha
) {}

