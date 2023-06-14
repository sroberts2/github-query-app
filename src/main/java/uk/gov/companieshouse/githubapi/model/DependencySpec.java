package uk.gov.companieshouse.githubapi.model;

import java.util.Optional;

public record DependencySpec(String groupId, String artifactId, Optional<String> version) {}
