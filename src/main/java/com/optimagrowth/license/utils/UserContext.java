package com.optimagrowth.license.utils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserContext {
    public static final String CORRELATION_ID = "tmx-correlation-id";
    public static final String AUTH_TOKEN = "Authorization";
    public static final String USER_ID = "tmx-user-id";
    public static final String ORGANIZATION_ID = "tmx-organization-id";
    private String correlationId = "";
    private String authToken = "";
    private String userId = "";
    private String organizationId = "";
}
