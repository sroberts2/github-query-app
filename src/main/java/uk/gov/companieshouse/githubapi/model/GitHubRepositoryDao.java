package uk.gov.companieshouse.githubapi.model;

import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class GitHubRepositoryDao {
    @Id
    private final String repoName;
    private final Map<String, String> dependencies;


    public GitHubRepositoryDao(final String repoName, final Map<String, String> dependencies) {
        this.repoName = repoName;
        this.dependencies = dependencies;
    }

    public String getRepoName() {
        return repoName;
    }

    public Map<String, String> getDependencies() {
        return Map.copyOf(dependencies);
    }


    @Override
    public String toString() {
        return String.format(
                "GithubRepositoryDao[repoName=%, dependencies=%s]",
                repoName,
                dependencies
        );
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((repoName == null) ? 0 : repoName.hashCode());
        result = prime * result + ((dependencies == null) ? 0 : dependencies.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        GitHubRepositoryDao other = (GitHubRepositoryDao) obj;
        if (repoName == null) {
            if (other.repoName != null) {
                return false;
            }
        } else if (!repoName.equals(other.repoName)) {
            return false;
        }
        if (dependencies == null) {
            if (other.dependencies != null) {
                return false;
            }
        } else if (!dependencies.equals(other.dependencies)) {
            return false;
        }
        return true;
    }

    
}
