package com.github.ecommerce_project.utils;

import org.springframework.security.core.context.SecurityContextHolder;

import com.github.ecommerce_project.models.User;

public class SecurityUtils {
    public static Long getAuthenticatedUserId() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getId();
    }
}
