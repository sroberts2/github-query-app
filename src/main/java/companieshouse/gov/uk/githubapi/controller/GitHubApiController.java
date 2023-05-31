package companieshouse.gov.uk.githubapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import companieshouse.gov.uk.githubapi.dao.GitHubAppDataRepository;
import companieshouse.gov.uk.githubapi.dao.GitHubRepositoryRepository;
import companieshouse.gov.uk.githubapi.model.GitHubAppData;
import companieshouse.gov.uk.githubapi.model.GitHubRepository;
import companieshouse.gov.uk.githubapi.model.GitHubSearchResponse;
import companieshouse.gov.uk.githubapi.model.GitHubTree;
import companieshouse.gov.uk.githubapi.model.GitHubTreeResponse;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@Controller
@RequestMapping
public class GitHubApiController {

    @Autowired
    private final GitHubRepositoryRepository gitHubRepositoryRepository;

    @Autowired
    private final GitHubAppDataRepository gitHubAppDataRepository;

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper;



    @Autowired
    public GitHubApiController(GitHubRepositoryRepository gitHubRepositoryRepository,
            GitHubAppDataRepository gitHubAppDataRepository, RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        this.gitHubRepositoryRepository = gitHubRepositoryRepository;
        this.gitHubAppDataRepository = gitHubAppDataRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/")
    public String getGitHubAppData(Model model) {
        List<GitHubAppData> appDataList = gitHubAppDataRepository.findAll();
        appDataList.sort(Comparator.comparing(GitHubAppData::getId)); // Sort by id alphabetically
        model.addAttribute("appDataList", appDataList);
        return "index";
    }



    //TODO cron instead of endpoint
    @GetMapping("/populate-repo-data")
    public ResponseEntity<String> getGitHubApiResponse() throws JsonProcessingException {

        boolean hasNext = true;
        List<GitHubRepository> repoList = new ArrayList<>();
        int perPage = 100;
        int page = 0;

        while (hasNext) {
            page++;
            StringBuilder allResults = new StringBuilder();
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("https://api.github.com/search/repositories")
                    .queryParam("q", "user:companieshouse+language:java")
                    .queryParam("page", page)
                    .queryParam("per_page", perPage);
            ResponseEntity<GitHubSearchResponse> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, null, GitHubSearchResponse.class);
            GitHubSearchResponse searchResponse = response.getBody();
            repoList.addAll(processGitHubSearchResponse(searchResponse));
            HttpHeaders httpHeaders = response.getHeaders();
            List<String> list = httpHeaders.get("Link");
            Pattern pattern = Pattern.compile("rel=\"Next\"", Pattern.CASE_INSENSITIVE);
            StringBuilder sb = new StringBuilder();
            for (String link: list){
                sb.append(link);
            }
            Matcher matcher = pattern.matcher(sb.toString());
            hasNext = matcher.find();
        }
        gitHubRepositoryRepository.deleteAll();
        gitHubRepositoryRepository.saveAll(repoList);
//<https://api.github.com/search/repositories?q=user%3Acompanieshouse+language%3Ajava&per_page=10&page=2>; rel="next", <https://api.github.com/search/repositories?q=user%3Acompanieshouse+language%3Ajava&per_page=10&page=20>; rel="last"

        populateAppData(repoList);

        return ResponseEntity.ok("");
    }


    public ResponseEntity<String> populateAppData(List<GitHubRepository> repoList) throws JsonProcessingException {
        AtomicInteger i = new AtomicInteger();
        repoList.forEach(e ->{
            i.getAndIncrement();
            GitHubAppData gitHubAppData = new GitHubAppData();
            gitHubAppData.setName(e.getName());
            System.out.println(i+": "+e.getName());
            String url = e.getTreesUrl().substring(0, e.getTreesUrl().length()-6)+"/"+e.getDefaultBranch();
            GitHubTreeResponse treeResponse = restTemplate.getForObject(url, GitHubTreeResponse.class);

            if (treeResponse != null && treeResponse.getTree() != null) {
                for (GitHubTree tree : treeResponse.getTree()) {
                    if ("pom.xml".equals(tree.getPath())) {
                        //System.out.println(tree.getUrl());
                        String jsonString = restTemplate.getForObject(tree.getUrl(), String.class);
                        JsonParser jsonParser = JsonParserFactory.getJsonParser();
                        Object jsonObject = jsonParser.parseMap(jsonString);
                        String content = "";
                        // Extract the content field from the JSON object
                        if (jsonObject instanceof Map) {
                            Map<String, Object> jsonMap = (Map<String, Object>) jsonObject;
                            content = (String) jsonMap.get("content");
                        }
                        String cleanContent = content.replaceAll("[^A-Za-z0-9+/=]", "");

                        byte[] decodedBytes = Base64.getDecoder().decode(cleanContent);

                        // Convert byte array to String
                        String decodedXml = new String(decodedBytes, StandardCharsets.UTF_8);
                        gitHubAppData.setSpringbootversion(getSpringBootVersion(decodedXml));
                        gitHubAppDataRepository.save(gitHubAppData);

                    }
                }
            }
            //sleep for two seconds
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        });
        return ResponseEntity.ok("");
    }

    private static List<GitHubRepository> processGitHubSearchResponse(GitHubSearchResponse searchResponse) {
        List<GitHubRepository> resultList = new ArrayList<>();
        for (GitHubRepository repository : searchResponse.getRepositories()) {
            resultList.add(repository);
        }
        return resultList;
    }

    public static String getSpringBootVersion(String pomString) {
        try {
            // Create a DocumentBuilder
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            InputSource inputSource = new InputSource(new StringReader(pomString));

            // Parse the XML and obtain the Document
            Document document = builder.parse(inputSource);

            // Find the <properties> element
            Element root = document.getDocumentElement();
            NodeList propertiesList = root.getElementsByTagName("properties");
            Element properties = (Element) propertiesList.item(0);

            // Find the spring boot version
            String springBootVersion = getProperty(properties, "spring-boot.version");
            if (springBootVersion == null) {
                springBootVersion = getProperty(properties, "spring.boot.version");
            }
            if (springBootVersion == null) {
                springBootVersion = getProperty(properties, "spring-boot-dependencies.version");
            }
            if (springBootVersion == null) {
                springBootVersion = getProperty(properties, "version.spring.boot");
            }
            if (springBootVersion == null){
                springBootVersion = getProperty(properties, "spring-framework.version");
                if (springBootVersion == null) {
                  springBootVersion = getProperty(properties, "spring.framework.version");
                }
                if (springBootVersion != null){
                    springBootVersion = "Uses native Spring version "+springBootVersion;
                }
            }

            if (springBootVersion == null) {
                NodeList parentList = root.getElementsByTagName("parent");
                Element parent = (Element) parentList.item(0);
                String artifactId = getProperty(parent, "artifactId");
                if (artifactId != null && artifactId.equals("spring-boot-starter-parent")) {
                    springBootVersion = getProperty(parent, "version");
                } else if (artifactId != null && artifactId.equals("companies-house-parent")) {
                    springBootVersion = "Uses companies-house-parent pom, no Spring version detected";
                } else if(artifactId != null){
                    springBootVersion = "Uses parent pom of "+artifactId;
                }
            }
            if (springBootVersion == null){
                springBootVersion = "No Spring detected";
            }
            return springBootVersion;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private static String getProperty(Element properties, String propertyName) {
        if (properties != null) {
          NodeList propertyList = properties.getElementsByTagName(propertyName);
          if (propertyList.getLength() > 0) {
            Element property = (Element) propertyList.item(0);
            return property.getTextContent();
          }
        }
        return null;
    }
}
