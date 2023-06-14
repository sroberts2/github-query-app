package uk.gov.companieshouse.githubapi.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.gov.companieshouse.githubapi.dao.SupportDataRepository;
import uk.gov.companieshouse.githubapi.model.SupportData;
import uk.gov.companieshouse.githubapi.service.SupportDataService;

@Service
public class SupportDataServiceImpl implements SupportDataService {
    private final SupportDataRepository supportDataRepository;

    @Autowired
    public SupportDataServiceImpl(SupportDataRepository supportDataRepository) {
        this.supportDataRepository = supportDataRepository;
    }

    @Override
    public SupportData addSupportData(SupportData supportData) {
        supportDataRepository.save(supportData);
        return supportData;
    }
}
