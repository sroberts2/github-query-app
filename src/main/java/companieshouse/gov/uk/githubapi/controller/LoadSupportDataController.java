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
import org.springframework.web.client.RestTemplate;

@Controller
@RequestMapping
public class LoadSupportDataController {


    @Autowired
    private final SupportDataService supportDataService;

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper;

    public LoadSupportDataController(SupportDataService supportDataService, RestTemplate restTemplate,
            ObjectMapper objectMapper){

        this.supportDataService = supportDataService;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    //TODO cron instead of endpoint
    @GetMapping("/support-data-add")
    public void populateSpringBootLifecycleDates() throws JsonProcessingException {
        String jsonString = restTemplate.getForObject("https://endoflife.date/api/spring-boot.json", String.class);
        List<SupportData> info = objectMapper.readValue(jsonString, new TypeReference<List<SupportData>>() {});
        for (SupportData sd : info){
            supportDataService.addSupportData(sd);
        }
    }

}
