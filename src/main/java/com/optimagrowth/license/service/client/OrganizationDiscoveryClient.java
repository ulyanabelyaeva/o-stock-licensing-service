package com.optimagrowth.license.service.client;

import com.optimagrowth.license.model.Organization;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class OrganizationDiscoveryClient {

    /**
     * Используется для взаимодействий с балансировщиком Spring Cloud Load Balancer
     */
    private final DiscoveryClient discoveryClient;

    public OrganizationDiscoveryClient(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    public Organization getOrganization(String organizationId) {
        RestTemplate restTemplate = new RestTemplate();
        List<ServiceInstance> instances =
                discoveryClient.getInstances("organization-service");
        if (instances.isEmpty()) return null;
        String serviceUri = String.format(
                "%s/v1/organization/%s",
                instances.get(0).getUri().toString(),
                organizationId);
        ResponseEntity<Organization> restExchange = restTemplate.exchange(serviceUri, HttpMethod.GET,
                null, Organization.class, organizationId);
        return restExchange.getBody();
    }
}