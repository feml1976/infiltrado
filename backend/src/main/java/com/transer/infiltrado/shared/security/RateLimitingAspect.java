package com.transer.infiltrado.shared.security;

import com.transer.infiltrado.shared.annotation.RateLimited;
import com.transer.infiltrado.shared.error.TooManyRequestsException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
public class RateLimitingAspect {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingAspect.class);

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Around("@annotation(rateLimited)")
    public Object enforce(ProceedingJoinPoint pjp, RateLimited rateLimited) throws Throwable {
        String ip       = extractIp();
        String identity = extractIdentity(pjp);
        String key      = rateLimited.key() + ":" + digest(ip + ":" + identity);

        Instant now    = Instant.now();
        Bucket  bucket = buckets.computeIfAbsent(key, k -> new Bucket(now));

        synchronized (bucket) {
            Duration windowDuration = Duration.ofMinutes(rateLimited.windowMinutes());

            if (bucket.lockedUntil != null && now.isBefore(bucket.lockedUntil)) {
                long retryAfter = Duration.between(now, bucket.lockedUntil).toSeconds();
                log.warn("Rate limit bloqueado key={} ip={}", rateLimited.key(), maskIp(ip));
                throw new TooManyRequestsException(retryAfter);
            }

            if (bucket.windowStart.plus(windowDuration).isBefore(now)) {
                bucket.windowStart = now;
                bucket.count       = 0;
                bucket.lockedUntil = null;
            }

            bucket.count++;

            if (bucket.count > rateLimited.maxAttempts()) {
                bucket.lockedUntil = now.plus(Duration.ofMinutes(rateLimited.lockoutMinutes()));
                long retryAfter    = rateLimited.lockoutMinutes() * 60L;
                log.warn("Rate limit umbral superado key={} ip={}", rateLimited.key(), maskIp(ip));
                throw new TooManyRequestsException(retryAfter);
            }
        }

        return pjp.proceed();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String extractIp() {
        try {
            var attrs   = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            String xff  = attrs.getRequest().getHeader("X-Forwarded-For");
            if (xff != null && !xff.isBlank()) {
                return xff.split(",")[0].trim();
            }
            return attrs.getRequest().getRemoteAddr();
        } catch (Exception e) {
            return "unknown";
        }
    }

    private static String extractIdentity(ProceedingJoinPoint pjp) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return auth.getName();
        }
        // Unauthenticated (e.g. /login): hash the first string-like argument
        for (Object arg : pjp.getArgs()) {
            if (arg == null) continue;
            // Records with email() accessor (LoginRequest)
            try {
                Object val = arg.getClass().getMethod("email").invoke(arg);
                if (val instanceof String s) return s;
            } catch (Exception ignored) { }
            if (arg instanceof String s) return s;
        }
        return "anon";
    }

    private static String maskIp(String ip) {
        if (ip.contains(":")) {
            String[] parts = ip.split(":", -1);
            int keep = Math.min(4, parts.length);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < keep; i++) {
                if (i > 0) sb.append(':');
                sb.append(parts[i]);
            }
            return sb + ":***";
        }
        int lastDot = ip.lastIndexOf('.');
        return lastDot > 0 ? ip.substring(0, lastDot) + ".***" : "***";
    }

    private static String digest(String value) {
        try {
            MessageDigest md   = MessageDigest.getInstance("SHA-256");
            byte[]        hash = md.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(value.hashCode());
        }
    }

    // ── Inner state ───────────────────────────────────────────────────────────

    private static final class Bucket {
        Instant windowStart;
        int     count      = 0;
        Instant lockedUntil = null;

        Bucket(Instant windowStart) {
            this.windowStart = windowStart;
        }
    }
}
