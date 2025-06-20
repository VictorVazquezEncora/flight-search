package com.encora.victorvazquez.flights_search.utils;

import java.util.Map;
import java.util.stream.Collectors;

public class Utils {
    
    /**
     * Converts a Map of parameters into a URL query string.
     * @param params Map with key-value parameters
     * @return Query string formatted like "param1=value1&param2=value2"
     */
    public static String buildQueryParams(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }
        
        return params.entrySet().stream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
    }

    

    
} 