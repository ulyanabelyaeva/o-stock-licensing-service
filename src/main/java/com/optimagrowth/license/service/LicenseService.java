package com.optimagrowth.license.service;

import com.optimagrowth.license.model.License;
import com.optimagrowth.license.repository.LicenseRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Locale;
import java.util.UUID;

@Service
public class LicenseService {

    private final LicenseRepository licenseRepository;
    private final MessageSource messages;
    private Locale locale;

    @Value("locale")
    private String localeTag;

    public LicenseService(LicenseRepository licenseRepository,
                          MessageSource messages) {
        this.licenseRepository = licenseRepository;
        this.messages = messages;
    }

    @PostConstruct
    public void init(){
        this.locale = Locale.forLanguageTag(localeTag);
    }

    public License getLicense(String licenseId, String organizationId) {
        License license = licenseRepository.findByOrganizationIdAndLicenseId(organizationId, licenseId);
        if (null == license) {
            throw new IllegalArgumentException(String.format(messages.getMessage("license.search.error.message", null, locale), licenseId, organizationId));
        }
        return license;
    }

    public License createLicense(License license) {
        license.setLicenseId(UUID.randomUUID().toString());
        licenseRepository.save(license);
        return license;
    }

    public License updateLicense(License license) {
        licenseRepository.save(license);
        return license;
    }

    public String deleteLicense(String licenseId) {
        License license = new License();
        license.setLicenseId(licenseId);
        licenseRepository.delete(license);
        return String.format(messages.getMessage("license.delete.message", null, locale), licenseId);
    }
}
