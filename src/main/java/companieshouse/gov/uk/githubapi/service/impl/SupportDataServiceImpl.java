package companieshouse.gov.uk.githubapi.service.impl;

import companieshouse.gov.uk.githubapi.dao.SupportDataRepository;
import companieshouse.gov.uk.githubapi.model.SupportData;
import companieshouse.gov.uk.githubapi.service.SupportDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SupportDataServiceImpl implements SupportDataService {
    private final SupportDataRepository supportDataRepository;

    @Autowired
    public SupportDataServiceImpl(SupportDataRepository supportDataRepository){
        this.supportDataRepository = supportDataRepository;
    }

    @Override
    public SupportData addSupportData(SupportData supportData) {
        supportDataRepository.save(supportData);
        return supportData;
    }
}
