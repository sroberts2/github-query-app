package companieshouse.gov.uk.githubapi.dao;

import companieshouse.gov.uk.githubapi.model.GitHubAppData;
import companieshouse.gov.uk.githubapi.model.GitHubSearchResponse;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GitHubAppDataRepository extends MongoRepository<GitHubAppData, String>  {

}
