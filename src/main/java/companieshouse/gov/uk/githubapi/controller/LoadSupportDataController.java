package companieshouse.gov.uk.githubapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import companieshouse.gov.uk.githubapi.model.SupportDate;
import companieshouse.gov.uk.githubapi.service.SupportDateService;
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
    private final SupportDateService supportDateService;

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper;

    public LoadSupportDataController(SupportDateService supportDateService, RestTemplate restTemplate,
            ObjectMapper objectMapper){

        this.supportDateService = supportDateService;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    //TODO cron instead of endpoint
    @GetMapping("/support-data-add")
    public void getSpringBootLifecycleDates() throws JsonProcessingException {
        String jsonString = restTemplate.getForObject("https://endoflife.date/api/spring-boot.json", String.class);
        List<SupportDate> info = objectMapper.readValue(jsonString, new TypeReference<List<SupportDate>>() {});
        for (SupportDate sd : info){
            supportDateService.addSupportDate(sd);
        }
    }

}
