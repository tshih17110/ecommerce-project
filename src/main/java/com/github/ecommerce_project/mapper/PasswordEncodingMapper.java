package com.github.ecommerce_project.mapper;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PasswordEncodingMapper {

    private final PasswordEncoder passwordEncoder;

    @EncodedMapping
    public String encode(String raw) {
        if (raw == null)
            return null;
        return passwordEncoder.encode(raw);
    }

}
