package uk.gov.companieshouse.gov.uk.githubapi.configuration;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;

import com.mongodb.client.MongoClient;

@Configuration
@MockBean(MongoClient.class)
public class MongoConfiguration {
    
}
