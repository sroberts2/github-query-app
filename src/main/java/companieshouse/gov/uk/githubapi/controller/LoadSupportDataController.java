package companieshouse.gov.uk.githubapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import companieshouse.gov.uk.githubapi.model.SupportData;
import companieshouse.gov.uk.githubapi.service.SupportDataService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestOperations;

@Controller
@RequestMapping
public class LoadSupportDataController {


    @Autowired
    private final SupportDataService supportDataService;

    private final RestOperations restTemplate;

    private final ObjectMapper objectMapper;

    /**
     * Constructor for the LoadSupportDataController.
     * @param supportDataService supportDataService to load SupportData
     * @param restTemplate restTemplate to make API calls
     * @param objectMapper objectMapper to map JSON to Java
     */
    public LoadSupportDataController(
                SupportDataService supportDataService,
                RestOperations restTemplate,
                ObjectMapper objectMapper
    ) {

        this.supportDataService = supportDataService;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Populate the database with the Spring Boot lifecycle dates.
     * @throws JsonProcessingException if the JSON cannot be processed
     */
    //TODO cron instead of endpoint
    @GetMapping("/support-data-add")
    public void populateSpringBootLifecycleDates() throws JsonProcessingException {
        String jsonString = restTemplate.getForObject(
                "https://endoflife.date/api/spring-boot.json", String.class
        );
        List<SupportData> info = objectMapper.readValue(
                jsonString,
                new TypeReference<List<SupportData>>() {}
        );
        for (SupportData sd : info) {
            supportDataService.addSupportData(sd);
        }
    }

}
