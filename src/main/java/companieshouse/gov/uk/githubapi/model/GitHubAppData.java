package companieshouse.gov.uk.githubapi.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class GitHubAppData {
    @Id
    //private String id;
    private String name;
    private String springbootversion;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpringbootversion() {
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
