package com.optimagrowth.license.service.client;

import com.optimagrowth.license.model.Organization;
import com.optimagrowth.license.repository.OrganizationRedisRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class OrganizationRestTemplateClient {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationRestTemplateClient.class);

    private final OrganizationRedisRepository organizationRedisRepository;
    private final RestTemplate restTemplate;

    public OrganizationRestTemplateClient(OrganizationRedisRepository organizationRedisRepository,
                                          RestTemplate restTemplate) {
        this.organizationRedisRepository = organizationRedisRepository;
        this.restTemplate = restTemplate;
    }

    @CircuitBreaker(name = "organizationService")
    public Organization getOrganization(String organizationId) {
        Organization organization = this.checkRedisCache(organizationId);
        if (organization != null){
            logger.debug("Successfully retrieved by id {} from the redis cache: {}", organizationId, organization);
            return organization;
        }
        logger.debug("Unable to locate organization from the redis cache: {}.", organizationId);

        ResponseEntity<Organization> restExchange =
                restTemplate.exchange(
                        "http://gateway-server/organization/v1/organization/{organizationId}",
                        HttpMethod.GET, null,
                        Organization.class, organizationId);
        organization = restExchange.getBody();
        /*Save the record from cache*/
        if (organization != null) {
            cacheOrganizationObject(organization);
        }
        return organization;
    }

    private Organization checkRedisCache(String organizationId) {
        try {
            return organizationRedisRepository.findById(organizationId).orElse(null);
        }catch (Exception ex){
            logger.error("Error when trying to retrieve organization {} check Redis Cache. Exception {}", organizationId, ex);
            return null;
        }
    }

    private void cacheOrganizationObject(Organization organization) {
        try {
            organizationRedisRepository.save(organization);
        }catch (Exception ex){
            logger.error("Unable to cache organization {} in Redis. Exception {}", organization.getId(), ex);
        }
    }
}