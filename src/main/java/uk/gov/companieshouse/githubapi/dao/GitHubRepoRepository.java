package uk.gov.companieshouse.githubapi.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

import uk.gov.companieshouse.githubapi.model.GitHubRepositoryDao;

public interface GitHubRepoRepository extends MongoRepository<GitHubRepositoryDao, String>  {

}
