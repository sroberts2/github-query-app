package companieshouse.gov.uk.githubapi.dao;

import companieshouse.gov.uk.githubapi.model.GitHubRepositoryDao;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface GitHubRepoRepository extends MongoRepository<GitHubRepositoryDao, String>  {

}
