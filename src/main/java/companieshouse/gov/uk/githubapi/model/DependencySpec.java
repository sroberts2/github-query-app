package companieshouse.gov.uk.githubapi.model;

import java.util.Optional;

public record DependencySpec(String groupId, String artifactId, Optional<String> version) {}
