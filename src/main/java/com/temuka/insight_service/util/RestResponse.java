package com.temuka.insight_service.util;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestResponse<T> {

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private T data;

    public static <T> RestResponse<T> success(String message, T data) {
        return RestResponse.<T>builder()
                .message(message)
                .data(data)
                .build();
    }
}