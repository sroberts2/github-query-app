package companieshouse.gov.uk.githubapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubLeaf(
    String content
) {
    
}
