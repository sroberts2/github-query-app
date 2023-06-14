package companieshouse.gov.uk.githubapi.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;


@ConditionalOnProperty(
        value = "app.retry.enable", havingValue = "true", matchIfMissing = true
)
@Configuration
@EnableRetry
public class EnableRetryConfiguration {
    
}
