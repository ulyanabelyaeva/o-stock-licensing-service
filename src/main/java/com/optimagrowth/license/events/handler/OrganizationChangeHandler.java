package com.optimagrowth.license.events.handler;

import com.optimagrowth.license.events.model.OrganizationChangeModel;
import com.optimagrowth.license.repository.OrganizationRedisRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class OrganizationChangeHandler {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationChangeHandler.class);

    private final OrganizationRedisRepository organizationRedisRepository;

    public OrganizationChangeHandler(OrganizationRedisRepository organizationRedisRepository) {
        this.organizationRedisRepository = organizationRedisRepository;
    }

    @Bean
    public Consumer<OrganizationChangeModel> processMessage() {
        return message -> {
            switch (message.getAction()) {
                case "CREATED":
                    logger.debug("Received a CREATED message from the organization service for organization id {}", message.getOrganizationId());
                    break;
                case "UPDATED":
                    logger.debug("Received a UPDATED message from the organization service for organization id {}", message.getOrganizationId());
                    organizationRedisRepository.deleteById(message.getOrganizationId());
                    break;
                case "DELETED":
                    logger.debug("Received a DELETED message from the organization service for organization id {}", message.getOrganizationId());
                    organizationRedisRepository.deleteById(message.getOrganizationId());
                    break;
                default:
                    logger.error("Received an UNKNOWN message from the organization service of type {}", message.getType());
                    break;
            }
        };
    }

}
