package companieshouse.gov.uk.githubapi.dao;

import companieshouse.gov.uk.githubapi.model.GitHubAppData;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GitHubAppDataRepository extends MongoRepository<GitHubAppData, String>  {

}
