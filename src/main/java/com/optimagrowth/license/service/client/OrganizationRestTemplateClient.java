package com.optimagrowth.license.service.client;

import com.optimagrowth.license.model.Organization;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class OrganizationRestTemplateClient {

    /**
     * Заменили стандартный RestTemplate на KeycloakRestTemplate для удобной передачи заголовка Authorization
     * */
    private final RestTemplate restTemplate;

    public OrganizationRestTemplateClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @CircuitBreaker(name = "organizationService")
    public Organization getOrganization(String organizationId) {
        ResponseEntity<Organization> restExchange =
                restTemplate.exchange(
                        "http://gateway-server/organization/v1/organization/{organizationId}",
                        HttpMethod.GET, null,
                        Organization.class, organizationId);
        return restExchange.getBody();
    }
}