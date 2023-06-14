package uk.gov.companieshouse.gov.uk.githubapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import com.mongodb.client.MongoClient;

@SpringBootTest
@TestPropertySource(properties = {
        "github.pat=ghp_123456789012345",
        "app.scheduling.enable=false",
        "app.retry.enable=false"
})
class GithubapiApplicationTests {

	@Test
	void contextLoads() {
	}

}
