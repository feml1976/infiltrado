package com.transer.infiltrado.shared.security;

public interface PasswordHasher {
    String hash(String raw);
    boolean matches(String raw, String hash);
}
