package uk.gov.companieshouse.githubapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubTreeResponse(@JsonProperty("tree") List<GitHubTree> tree) {}
