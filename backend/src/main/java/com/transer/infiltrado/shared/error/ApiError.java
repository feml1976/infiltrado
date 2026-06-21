package com.transer.infiltrado.shared.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(int status, String mensaje, Map<String, String> errores) {

    public static ApiError of(int status, String mensaje) {
        return new ApiError(status, mensaje, null);
    }

    public static ApiError ofValidacion(int status, String mensaje, Map<String, String> errores) {
        return new ApiError(status, mensaje, errores);
    }
}
