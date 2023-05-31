package companieshouse.gov.uk.githubapi.dao;

import companieshouse.gov.uk.githubapi.model.SupportData;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SupportDataRepository extends MongoRepository<SupportData, String> {

}
