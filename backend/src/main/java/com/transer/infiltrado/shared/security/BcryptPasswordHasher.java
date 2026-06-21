package com.transer.infiltrado.shared.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
class BcryptPasswordHasher implements PasswordHasher {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String hash(String raw) {
        return encoder.encode(raw);
    }

    @Override
    public boolean matches(String raw, String hash) {
        return encoder.matches(raw, hash);
    }
}
