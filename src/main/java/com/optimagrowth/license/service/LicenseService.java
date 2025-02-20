package com.optimagrowth.license.service;

import com.optimagrowth.license.model.License;
import com.optimagrowth.license.model.Organization;
import com.optimagrowth.license.repository.LicenseRepository;
import com.optimagrowth.license.service.client.OrganizationDiscoveryClient;
import com.optimagrowth.license.service.client.OrganizationFeignClient;
import com.optimagrowth.license.service.client.OrganizationRestTemplateClient;
import com.optimagrowth.license.utils.UserContextHolder;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Service
public class LicenseService {

    private static final Logger logger = LoggerFactory.getLogger(LicenseService.class);

    private final OrganizationRestTemplateClient organizationRestTemplateClient;
    private final OrganizationDiscoveryClient organizationDiscoveryClient;
    private final OrganizationFeignClient organizationFeignClient;
    private final LicenseRepository licenseRepository;
    private final MessageSource messages;
    private Locale locale;

    @Value("locale")
    private String localeTag;

    public LicenseService(OrganizationRestTemplateClient organizationRestTemplateClient,
                          OrganizationDiscoveryClient organizationDiscoveryClient,
                          OrganizationFeignClient organizationFeignClient,
                          LicenseRepository licenseRepository,
                          MessageSource messageSource) {
        this.organizationRestTemplateClient = organizationRestTemplateClient;
        this.organizationDiscoveryClient = organizationDiscoveryClient;
        this.organizationFeignClient = organizationFeignClient;
        this.licenseRepository = licenseRepository;
        this.messages = messageSource;
    }

    @PostConstruct
    public void init() {
        this.locale = Locale.forLanguageTag(localeTag);
    }

    @CircuitBreaker(name = "licenseService", fallbackMethod= "buildFallbackLicense")
    @RateLimiter(name = "licenseService", fallbackMethod = "buildFallbackLicense")
    @Retry(name = "retryLicenseService", fallbackMethod="buildFallbackLicense")
    @Bulkhead(name= "bulkheadLicenseService", type = Bulkhead.Type.SEMAPHORE)
    public License getLicense(String licenseId, String organizationId) {
        logger.debug("getLicensesByOrganization Correlation id: {}", UserContextHolder.getContext().getCorrelationId());
        sleep();
        License license = licenseRepository.findByOrganizationIdAndLicenseId(organizationId, licenseId);
        if (null == license) {
            throw new IllegalArgumentException(String.format(messages.getMessage("license.search.error.message", null, locale), licenseId, organizationId));
        }
        return license;
    }

    private void sleep() {
        try {
            Thread.sleep(20_000);
            throw new java.util.concurrent.TimeoutException();
        } catch (InterruptedException e) {
            System.out.println("Sleep interrupted");
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    private License buildFallbackLicense(String licenseId, String organizationId, Throwable t){
        License license = new License();
        license.setLicenseId("0000000-00-00000");
        license.setOrganizationId(organizationId);
        license.setProductName("Sorry no licensing information currently available");
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

    public License getLicense(String organizationId,
                              String licenseId,
                              String clientType) {
        License license = licenseRepository.findByOrganizationIdAndLicenseId(organizationId, licenseId);
        if (null == license) {
            throw new IllegalArgumentException(String.format(
                    messages.getMessage("license.search.error.message", null, null),
                    licenseId, organizationId));
        }
        Organization organization = this.retrieveOrganizationInfo(organizationId, clientType);
        if (null != organization) {
            license.setOrganizationName(organization.getName());
            license.setContactName(organization.getContactName());
            license.setContactEmail(organization.getContactEmail());
            license.setContactPhone(organization.getContactPhone());
        }
        return license;
    }

    private Organization retrieveOrganizationInfo(String organizationId, String clientType) {
        return switch (clientType) {
            case "feign" -> organizationFeignClient.getOrganization(organizationId);
            case "discovery" -> organizationDiscoveryClient.getOrganization(organizationId);
            default -> organizationRestTemplateClient.getOrganization(organizationId);
        };

    }
}
