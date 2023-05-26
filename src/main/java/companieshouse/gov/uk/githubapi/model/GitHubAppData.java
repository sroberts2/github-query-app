package companieshouse.gov.uk.githubapi.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class GitHubAppData {
    @Id
    private String name;
    private String springbootversion;

    public String getId() {
        return name;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpringBootVersion() {
        return springbootversion;
    }

    public void setSpringbootversion(String springbootversion) {
        this.springbootversion = springbootversion;
    }

    @Override
    public String toString() {
        return "GitHubAppData{" +
                "name='" + name + '\'' +
                ", springbootversion='" + springbootversion + '\'' +
                '}';
    }
}
