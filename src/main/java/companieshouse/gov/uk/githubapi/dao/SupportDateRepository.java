package companieshouse.gov.uk.githubapi.dao;

import companieshouse.gov.uk.githubapi.model.SupportDate;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SupportDateRepository extends MongoRepository<SupportDate, String> {

}
