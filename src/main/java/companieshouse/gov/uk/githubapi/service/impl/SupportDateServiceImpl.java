package companieshouse.gov.uk.githubapi.service.impl;

import companieshouse.gov.uk.githubapi.dao.SupportDateRepository;
import companieshouse.gov.uk.githubapi.model.SupportDate;
import companieshouse.gov.uk.githubapi.service.SupportDateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SupportDateServiceImpl implements SupportDateService {
    private final SupportDateRepository supportDateRepository;

    @Autowired
    public SupportDateServiceImpl(SupportDateRepository supportDateRepository){
        this.supportDateRepository = supportDateRepository;
    }

    @Override
    public SupportDate addSupportDate(SupportDate supportDate) {
        supportDateRepository.save(supportDate);
        return supportDate;
    }
}
