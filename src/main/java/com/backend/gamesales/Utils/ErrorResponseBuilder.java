package com.backend.gamesales.Utils;

import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

public class ErrorResponseBuilder {
    public static Map<String, Object> buildErrorResponse(String message, HttpStatus status){
        Map<String,Object>response=new HashMap<>();
        response.put("ERROR", message);
        response.put("status", status.value());
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}
