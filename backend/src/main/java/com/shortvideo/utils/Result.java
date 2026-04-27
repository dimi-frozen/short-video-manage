package com.shortvideo.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> success() {
        return new Result<>(0, null, null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(0, null, data);
    }

    public static <T> Result<T> success(String message) {
        return new Result<>(0, message, null);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(0, message, data);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(1, message, null);
    }

    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }
}
