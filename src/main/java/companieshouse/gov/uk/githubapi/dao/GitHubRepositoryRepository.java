package companieshouse.gov.uk.githubapi.dao;

import companieshouse.gov.uk.githubapi.model.GitHubRepository;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GitHubRepositoryRepository extends MongoRepository<GitHubRepository, String> {

}