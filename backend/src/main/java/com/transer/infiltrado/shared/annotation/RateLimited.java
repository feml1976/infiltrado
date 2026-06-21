package com.transer.infiltrado.shared.annotation;

import java.lang.annotation.*;

/**
 * Marca un endpoint para rate limiting.
 * La implementación real se conecta en el Paso 16 (endurecimiento) mediante AOP.
 * Parámetros por defecto alineados con la configuración en application.yml.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimited {
    String key() default "default";
    int maxAttempts() default 5;
    int windowMinutes() default 1;
    int lockoutMinutes() default 5;
}
