package companieshouse.gov.uk.githubapi.dao;

import companieshouse.gov.uk.githubapi.model.GitHubSearchResponse;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GitHubSearchResponseRepository extends MongoRepository<GitHubSearchResponse, String>  {

    GitHubSearchResponse findTopByOrderByIdDesc();

}
