package com.example.tasteebackend.model.enums;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    ROLE_ADMIN, ROLE_RESTAURANT, ROLE_CUSTOMER, ROLE_DELIVERY;

    @Override
    public String getAuthority() {
        return name();
    }
}