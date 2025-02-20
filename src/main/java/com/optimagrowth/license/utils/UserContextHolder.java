package com.optimagrowth.license.utils;

public class UserContextHolder {

    private static final ThreadLocal<UserContext> userContext = new ThreadLocal<>();

    public static UserContext getContext() {
        UserContext context = userContext.get();
        if (context == null) {
            context = createEmptyContext();
            userContext.set(context);
        }
        return userContext.get();
    }

    public static void setContext(UserContext context) {
        userContext.set(context);
    }

    public static UserContext createEmptyContext() {
        return new UserContext();
    }
}
