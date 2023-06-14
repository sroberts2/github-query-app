package uk.gov.companieshouse.githubapi.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

import uk.gov.companieshouse.githubapi.model.SupportData;

public interface SupportDataRepository extends MongoRepository<SupportData, String> {

}
