package uk.gov.companieshouse.githubapi.service.mvn;

import java.util.Map;

import org.w3c.dom.Document;

public interface ParseRule {

    Map<String, String> run(final Document xmlDocument);
}
