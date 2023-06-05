package companieshouse.gov.uk.githubapi.service.mvn;

import org.w3c.dom.Document;

import java.util.Map;

public interface ParseRule {

    Map<String, String> run(final Document xmlDocument);
}
